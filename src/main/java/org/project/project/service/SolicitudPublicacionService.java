package org.project.project.service;

import org.project.project.model.entity.API;
import org.project.project.model.entity.SolicitudPublicacionVersionApi;
import org.project.project.model.entity.SolicitudPublicacionVersionApi.EstadoSolicitud;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.VersionAPI;
import org.project.project.repository.APIRepository;
import org.project.project.repository.SolicitudPublicacionVersionApiRepository;
import org.project.project.repository.UsuarioRepository;
import org.project.project.repository.VersionAPIRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar el workflow de QA de publicación de APIs.
 * 
 * Responsabilidades:
 * - Crear solicitudes de publicación (DEV solicita revisión)
 * - Aprobar/Rechazar solicitudes (QA revisa)
 * - Cancelar solicitudes (DEV cancela)
 * - Validar si una API puede ser editada
 * - Gestionar cambios de estado de API según workflow
 */
@Slf4j
@Service
public class SolicitudPublicacionService {

    @Autowired
    private SolicitudPublicacionVersionApiRepository solicitudRepository;
    
    @Autowired
    private APIRepository apiRepository;
    
    @Autowired
    private VersionAPIRepository versionAPIRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    // ========== CREAR SOLICITUD ==========
    
    /**
     * Crea una nueva solicitud de publicación.
     * 
     * Flujo:
     * 1. Valida que API esté en BORRADOR
     * 2. Valida que no exista solicitud activa
     * 3. Cambia estado de API a QA
     * 4. Crea solicitud en estado PENDIENTE
     * 
     * @param apiId ID de la API
     * @param versionId ID de la versión a publicar
     * @param username Username del DEV que solicita
     * @return Solicitud creada
     * @throws IllegalStateException si API no está en BORRADOR o ya tiene solicitud activa
     */
    @Transactional
    public SolicitudPublicacionVersionApi crearSolicitud(Long apiId, Long versionId, String username) {
        log.info("🚀 Creando solicitud de publicación - API: {}, Version: {}, Usuario: {}", 
                 apiId, versionId, username);

        // 1. Buscar usuario
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        // 2. Buscar API
        API api = apiRepository.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("API no encontrada con ID: " + apiId));

        // 3. Validar estado BORRADOR
        if (api.getEstadoApi() != API.EstadoApi.BORRADOR) {
            log.warn("⚠️ Intento de crear solicitud para API que no está en BORRADOR: {}", api.getNombreApi());
            throw new IllegalStateException(
                "Solo puedes solicitar revisión de APIs en estado BORRADOR. " +
                "Estado actual: " + api.getEstadoApi()
            );
        }

        // 4. Verificar que no exista solicitud activa
        if (solicitudRepository.existeSolicitudActivaParaApi(apiId)) {
            log.warn("⚠️ Ya existe una solicitud activa para API: {}", api.getNombreApi());
            throw new IllegalStateException(
                "Ya existe una solicitud de publicación activa para esta API. " +
                "Espera a que sea resuelta o cancela la solicitud anterior."
            );
        }

        // 5. Buscar versión
        VersionAPI version = versionAPIRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Versión no encontrada con ID: " + versionId));

        // 6. Validar que la versión pertenece a la API
        if (!version.getApi().getApiId().equals(apiId)) {
            throw new IllegalArgumentException(
                "La versión especificada no pertenece a la API"
            );
        }

        // 7. Validar que la versión esté desplegada y funcionando (ACTIVE)
        if (version.getDeploymentStatus() == null || 
            version.getDeploymentStatus() != VersionAPI.DeploymentStatus.ACTIVE) {
            log.warn("⚠️ Intento de solicitar QA para versión NO desplegada - API: {}, Versión: {}, Estado: {}", 
                     api.getNombreApi(), version.getNumeroVersion(), version.getDeploymentStatus());
            throw new IllegalStateException(
                "Solo puedes solicitar revisión QA de APIs que estén desplegadas y funcionando. " +
                "Por favor, despliega tu API en Cloud Run primero (estado ACTIVE). " +
                "Estado actual de deployment: " + 
                (version.getDeploymentStatus() != null ? version.getDeploymentStatus() : "No desplegado")
            );
        }
        log.info("✅ Validación deployment ACTIVE exitosa - API: {}, URL: {}", 
                 api.getNombreApi(), version.getCloudRunUrl());

        // 8. Cambiar estado de API a QA
        api.setEstadoApi(API.EstadoApi.QA);
        api.setActualizadoPor(usuario);
        api.setActualizadoEn(LocalDateTime.now());
        apiRepository.save(api);
        log.info("📝 API '{}' cambiada a estado QA", api.getNombreApi());

        // 9. Crear solicitud
        SolicitudPublicacionVersionApi solicitud = new SolicitudPublicacionVersionApi();
        solicitud.setApi(api);
        solicitud.setVersionApi(version);
        solicitud.setGeneradoPor(usuario);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setCreadoEn(LocalDateTime.now());

        SolicitudPublicacionVersionApi savedSolicitud = solicitudRepository.save(solicitud);
        
        log.info("✅ Solicitud de publicación creada exitosamente - ID: {}, API: {}, Estado: {}", 
                 savedSolicitud.getSolicitudPublicacionId(), 
                 api.getNombreApi(), 
                 savedSolicitud.getEstado());

        return savedSolicitud;
    }

    // ========== APROBAR SOLICITUD (QA) ==========
    
    /**
     * Aprueba una solicitud de publicación (acción de QA).
     * 
     * Flujo:
     * 1. Valida que solicitud esté activa (PENDIENTE o EN_REVISION)
     * 2. Marca solicitud como APROBADO
     * 3. Cambia estado de API a PRODUCCION
     * 
     * @param solicitudId ID de la solicitud
     * @param usernameQA Username del QA que aprueba
     * @return Solicitud aprobada
     * @throws IllegalStateException si solicitud ya está resuelta
     */
    @Transactional
    public SolicitudPublicacionVersionApi aprobarSolicitud(Long solicitudId, String usernameQA) {
        log.info("✅ Aprobando solicitud - ID: {}, QA: {}", solicitudId, usernameQA);

        // 1. Buscar QA
        Usuario qa = usuarioRepository.findByUsername(usernameQA)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario QA no encontrado: " + usernameQA));

        // 2. Buscar solicitud
        SolicitudPublicacionVersionApi solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

        // 3. Validar que pueda resolver
        if (!solicitud.puedeResolver()) {
            log.warn("⚠️ Intento de aprobar solicitud ya resuelta: {}", solicitud);
            throw new IllegalStateException(
                "Esta solicitud ya fue resuelta. Estado actual: " + solicitud.getEstado()
            );
        }

        // 4. Marcar solicitud como aprobada
        solicitud.aprobar(qa);
        solicitudRepository.save(solicitud);

        // 5. Cambiar estado de API a PRODUCCION
        API api = solicitud.getApi();
        api.setEstadoApi(API.EstadoApi.PRODUCCION);
        api.setActualizadoPor(qa);
        api.setActualizadoEn(LocalDateTime.now());
        apiRepository.save(api);

        log.info("🚀 Solicitud aprobada - API '{}' publicada en PRODUCCION por {}", 
                 api.getNombreApi(), usernameQA);

        return solicitud;
    }

    // ========== RECHAZAR SOLICITUD (QA) ==========
    
    /**
     * Rechaza una solicitud de publicación (acción de QA).
     * 
     * Flujo:
     * 1. Valida que solicitud esté activa
     * 2. Marca solicitud como RECHAZADO
     * 3. Cambia estado de API a BORRADOR (para que DEV corrija)
     * 
     * @param solicitudId ID de la solicitud
     * @param usernameQA Username del QA que rechaza
     * @return Solicitud rechazada
     */
    @Transactional
    public SolicitudPublicacionVersionApi rechazarSolicitud(Long solicitudId, String usernameQA) {
        log.info("❌ Rechazando solicitud - ID: {}, QA: {}", solicitudId, usernameQA);

        // 1. Buscar QA
        Usuario qa = usuarioRepository.findByUsername(usernameQA)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario QA no encontrado: " + usernameQA));

        // 2. Buscar solicitud
        SolicitudPublicacionVersionApi solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

        // 3. Validar que pueda resolver
        if (!solicitud.puedeResolver()) {
            log.warn("⚠️ Intento de rechazar solicitud ya resuelta: {}", solicitud);
            throw new IllegalStateException(
                "Esta solicitud ya fue resuelta. Estado actual: " + solicitud.getEstado()
            );
        }

        // 4. Marcar solicitud como rechazada
        solicitud.rechazar(qa);
        solicitudRepository.save(solicitud);

        // 5. Cambiar estado de API a BORRADOR (para corrección)
        API api = solicitud.getApi();
        api.setEstadoApi(API.EstadoApi.BORRADOR);
        api.setActualizadoPor(qa);
        api.setActualizadoEn(LocalDateTime.now());
        apiRepository.save(api);

        log.info("🔄 Solicitud rechazada - API '{}' devuelta a BORRADOR para corrección", 
                 api.getNombreApi());

        return solicitud;
    }

    // ========== CANCELAR SOLICITUD (DEV) ==========
    
    /**
     * Cancela una solicitud de publicación (acción del DEV que la creó).
     * 
     * Flujo:
     * 1. Valida que solicitud pueda cancelarse
     * 2. Marca solicitud como CANCELADO
     * 3. Cambia estado de API a BORRADOR
     * 
     * @param solicitudId ID de la solicitud
     * @param username Username del DEV que cancela
     * @return Solicitud cancelada
     */
    @Transactional
    public SolicitudPublicacionVersionApi cancelarSolicitud(Long solicitudId, String username) {
        log.info("⏸️ Cancelando solicitud - ID: {}, Usuario: {}", solicitudId, username);

        // 1. Buscar usuario
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        // 2. Buscar solicitud
        SolicitudPublicacionVersionApi solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

        // 3. Validar que el usuario es el creador
        if (!solicitud.getGeneradoPor().getUsuarioId().equals(usuario.getUsuarioId())) {
            log.warn("⚠️ Usuario {} intentó cancelar solicitud que no creó", username);
            throw new IllegalStateException(
                "Solo puedes cancelar solicitudes que tú creaste"
            );
        }

        // 4. Validar que pueda cancelar
        if (!solicitud.puedeCancelar()) {
            log.warn("⚠️ Intento de cancelar solicitud ya resuelta: {}", solicitud);
            throw new IllegalStateException(
                "Esta solicitud ya fue resuelta y no puede cancelarse. Estado: " + solicitud.getEstado()
            );
        }

        // 5. Marcar solicitud como cancelada
        solicitud.cancelar();
        solicitudRepository.save(solicitud);

        // 6. Cambiar estado de API a BORRADOR
        API api = solicitud.getApi();
        api.setEstadoApi(API.EstadoApi.BORRADOR);
        api.setActualizadoPor(usuario);
        api.setActualizadoEn(LocalDateTime.now());
        apiRepository.save(api);

        log.info("🔄 Solicitud cancelada - API '{}' devuelta a BORRADOR", api.getNombreApi());

        return solicitud;
    }

    // ========== MARCAR EN REVISIÓN (QA) ==========
    
    /**
     * Marca una solicitud como "En Revisión" cuando QA empieza a trabajar.
     * (Opcional - QA puede ir directo a aprobar/rechazar)
     * 
     * @param solicitudId ID de la solicitud
     * @param usernameQA Username del QA que empieza revisión
     */
    @Transactional
    public void marcarEnRevision(Long solicitudId, String usernameQA) {
        log.info("🔍 Marcando solicitud en revisión - ID: {}, QA: {}", solicitudId, usernameQA);

        SolicitudPublicacionVersionApi solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

        solicitud.marcarEnRevision();
        solicitudRepository.save(solicitud);

        log.info("📝 Solicitud {} marcada como EN_REVISION", solicitudId);
    }

    // ========== CONSULTAS Y VALIDACIONES ==========
    
    /**
     * Verifica si una API puede ser editada.
     * 
     * Reglas:
     * - Solo APIs en BORRADOR pueden editarse
     * - No debe tener solicitud activa (PENDIENTE o EN_REVISION)
     * 
     * @param apiId ID de la API
     * @return true si puede editarse, false si no
     */
    public boolean puedeEditarAPI(Long apiId) {
        // 1. Buscar API
        API api = apiRepository.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("API no encontrada con ID: " + apiId));

        // 2. Verificar estado BORRADOR
        if (api.getEstadoApi() != API.EstadoApi.BORRADOR) {
            log.debug("❌ API {} no puede editarse - Estado: {}", apiId, api.getEstadoApi());
            return false;
        }

        // 3. Verificar que no haya solicitud activa
        boolean tieneSolicitudActiva = solicitudRepository.existeSolicitudActivaParaApi(apiId);
        if (tieneSolicitudActiva) {
            log.debug("❌ API {} no puede editarse - Tiene solicitud activa", apiId);
            return false;
        }

        log.debug("✅ API {} puede editarse", apiId);
        return true;
    }
    
    /**
     * Busca la solicitud activa para una API específica
     * 
     * @param apiId ID de la API
     * @return Optional con la solicitud activa, o empty si no existe
     */
    public java.util.Optional<SolicitudPublicacionVersionApi> buscarSolicitudActivaPorApi(Long apiId) {
        log.debug("🔍 Buscando solicitud activa para API: {}", apiId);
        return solicitudRepository.findSolicitudActivaByApiId(apiId);
    }
    
    /**
     * Verifica si existe una solicitud activa para una API
     * 
     * @param apiId ID de la API
     * @return true si existe solicitud activa (PENDIENTE o EN_REVISION)
     */
    public boolean existeSolicitudActiva(Long apiId) {
        boolean existe = solicitudRepository.existeSolicitudActivaParaApi(apiId);
        log.debug("🔍 API {} tiene solicitud activa: {}", apiId, existe);
        return existe;
    }
    
    /**
     * Obtiene solicitudes pendientes para el panel QA
     */
    public List<SolicitudPublicacionVersionApi> obtenerSolicitudesParaQA() {
        return solicitudRepository.findSolicitudesActivasParaQA();
    }
    
    /**
     * Obtiene solicitudes de un usuario DEV
     */
    public List<SolicitudPublicacionVersionApi> obtenerSolicitudesDeUsuario(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return solicitudRepository.findByGeneradoPorOrderByCreadoEnDesc(usuario);
    }
    
    /**
     * Obtiene una solicitud por ID
     */
    public SolicitudPublicacionVersionApi obtenerSolicitudPorId(Long solicitudId) {
        return solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));
    }
    
    /**
     * Cuenta solicitudes pendientes (para badge en navbar)
     */
    public long contarSolicitudesPendientes() {
        return solicitudRepository.countByEstado(EstadoSolicitud.PENDIENTE);
    }
}
