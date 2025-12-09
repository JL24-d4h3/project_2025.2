package org.project.project.service;

import org.project.project.model.entity.API;
import org.project.project.model.entity.VersionAPI;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.Categoria;
import org.project.project.model.entity.Etiqueta;
import org.project.project.model.entity.Documentacion;
import org.project.project.model.entity.Enlace;
import org.project.project.model.entity.Clasificacion;
import org.project.project.model.dto.api.CrearApiDTO;
import org.project.project.model.dto.api.ApiResponseDTO;
import org.project.project.model.dto.api.SeccionCmsDTO;
import org.project.project.model.dto.api.EnlaceReferenciaDTO;
import org.project.project.repository.*;
import org.project.project.repository.DocumentationRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class APIService {

    // =====================================================================
    // FASE 0.4: Umbral para estrategia de almacenamiento híbrido
    // =====================================================================
    /**
     * Tamaño máximo de contenido Markdown para guardar en BD.
     * Contenido < 64KB → BD (recurso.markdown_content)
     * Contenido >= 64KB → GCS (enlace.direccion_almacenamiento)
     */
    private static final int MARKDOWN_SIZE_THRESHOLD = 64 * 1024; // 64KB

    @Autowired
    private APIRepository apiRepository;

    @Autowired
    private VersionAPIRepository versionAPIRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EtiquetaRepository etiquetaRepository;

    @Autowired
    private DocumentationRepository documentationRepository;

    @Autowired
    private ContenidoRepository contenidoRepository;

    @Autowired
    private EnlaceRepository enlaceRepository;

    @Autowired
    private org.project.project.repository.RecursoRepository recursoRepository;

    @Autowired
    private ClasificacionRepository clasificacionRepository;

    @Autowired
    private ApiContractStorageService storageService;

    @Autowired
    private GoogleCloudStorageService googleCloudStorageService;

    @Autowired
    private OpenApiValidatorService openApiValidatorService;

    @Autowired
    private SolicitudPublicacionService solicitudPublicacionService;

    public List<API> listarApis() {
        return apiRepository.findAll();
    }

    public API buscarApiPorId(Long id) {
        return apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API no encontrada con id: " + id));
    }

    public API guardarApi(API api) {
        // La fecha de creación se maneja automáticamente por DEFAULT CURRENT_TIMESTAMP en la BD
        if (api.getCreadoEn() == null) {
            api.setCreadoEn(LocalDateTime.now());
        }
        return apiRepository.save(api);
    }

    public API actualizarApi(Long id, API apiDetalles) {
        API api = buscarApiPorId(id);
        api.setNombreApi(apiDetalles.getNombreApi());
        api.setDescripcionApi(apiDetalles.getDescripcionApi());
        api.setEstadoApi(apiDetalles.getEstadoApi());
        // La fecha de creación no se debería actualizar
        return apiRepository.save(api);
    }

    public void eliminarApi(Long id) {
        API api = buscarApiPorId(id);
        apiRepository.delete(api);
    }

    @Transactional
    public API crearApiCompleta(String username, String nombreApi, String descripcion,
                                String version, String estado, List<Long> categoryIds,
                                List<Long> tagIds, String contractContent, String baseUrl,
                                String documentationUrl) {

        log.info("🚀 Iniciando creación de API completa para usuario: {}", username);

        // Buscar el usuario
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        log.info("👤 Usuario encontrado: ID={}, Username={}", usuario.getUsuarioId(), usuario.getUsername());

        // Crear la API - SIEMPRE en estado BORRADOR
        API api = new API();
        api.setNombreApi(nombreApi);
        api.setDescripcionApi(descripcion);
        api.setEstadoApi(API.EstadoApi.BORRADOR); // Ignorar el parámetro estado, siempre BORRADOR
        api.setCreadoPor(usuario); // Usuario que crea la API
        api.setCreadoEn(LocalDateTime.now()); // Fecha de creación
        
        log.info("📝 API creada en estado BORRADOR (estado recibido '{}' ignorado)", estado);

        // Agregar categorías si se seleccionaron
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Categoria> categorias = categoriaRepository.findAllById(categoryIds);
            api.setCategorias(new HashSet<>(categorias));
        } else {
            api.setCategorias(new HashSet<>());
        }

        // Agregar etiquetas si se seleccionaron
        if (tagIds != null && !tagIds.isEmpty()) {
            // Convertir Long a Integer para las etiquetas
            List<Integer> etiquetaIds = tagIds.stream().map(Long::intValue).toList();
            List<Etiqueta> etiquetas = etiquetaRepository.findAllById(etiquetaIds);
            api.setEtiquetas(new HashSet<>(etiquetas));
        } else {
            api.setEtiquetas(new HashSet<>());
        }

        // Guardar la API primero para obtener el ID
        api = apiRepository.save(api);
        log.info("💾 API guardada con ID: {}", api.getApiId());

        // Crear la documentación para la API
        Documentacion documentacion = new Documentacion();
        documentacion.setApi(api);
        documentacion.setSeccionDocumentacion("Documentación principal"); // Valor por defecto
        documentacion = documentationRepository.save(documentacion);
        log.info("📄 Documentación creada con ID: {}", documentacion.getDocumentacionId());

        // Establecer relación bidireccional
        api.setDocumentacion(documentacion);
        api = apiRepository.save(api);

        // Crear la primera versión de la API
        VersionAPI versionAPI = new VersionAPI();
        versionAPI.setNumeroVersion(version);
        versionAPI.setDescripcionVersion(descripcion); // La primera versión tiene la misma descripción que la API
        versionAPI.setApi(api);
        versionAPI.setCreadoPor(usuario); // ✅ FIX: Usar setCreadoPor() en lugar de setCreador()
        versionAPI.setCreadoEn(LocalDateTime.now()); // ✅ FIX: Establecer fecha de creación
        versionAPI.setFechaLanzamiento(LocalDateTime.now().toLocalDate());
        versionAPI.setDocumentacion(documentacion); // Asignar documentación

        log.info("📝 VersionAPI creada: Version={}, CreadorId={}, ApiId={}",
                version, usuario.getUsuarioId(), api.getApiId());

        // Guardar el contrato en GCS y obtener la URL
        try {
            String contractUrl = storageService.saveApiContract(api.getApiId(), version, contractContent);
            versionAPI.setContratoApiUrl(contractUrl);
        } catch (Exception e) {
            throw new RuntimeException("Error guardando el contrato en el almacenamiento: " + e.getMessage(), e);
        }

        // Guardar la versión API explícitamente
        VersionAPI savedVersionAPI = versionAPIRepository.save(versionAPI);
        log.info("💾 VersionAPI guardada con ID: {}, Descripción: {}",
                savedVersionAPI.getVersionId(), savedVersionAPI.getDescripcionVersion());

        // Agregar la versión guardada a la API
        api.setVersiones(new HashSet<>());
        api.getVersiones().add(savedVersionAPI);

        // Guardar la API con su versión
        API savedApi = apiRepository.save(api);
        log.info("✅ API completa guardada exitosamente: API_ID={}, Primera versión creada con ID_CREADOR={}",
                savedApi.getApiId(), usuario.getUsuarioId());

        return savedApi;
    }

    @Transactional
    public API actualizarApiConNuevaVersion(String username, Long apiId, String nuevaVersion,
                                            String descripcionVersion, String nuevoEstado,
                                            List<Long> categoryIds, List<Long> tagIds,
                                            String contractContent, String baseUrl,
                                            String documentationUrl) {

        log.info("🔄 Iniciando actualización de API ID {} con nueva versión {} para usuario: {}",
                apiId, nuevaVersion, username);

        // Buscar el usuario
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        log.info("👤 Usuario encontrado: ID={}, Username={}", usuario.getUsuarioId(), usuario.getUsername());

        // Buscar la API
        API api = buscarApiPorId(apiId);

        log.info("✅ Usuario autenticado {} puede actualizar la API {}", username, api.getNombreApi());
        
        // Validar que la API puede ser editada (debe estar en BORRADOR y sin solicitud activa)
        if (!solicitudPublicacionService.puedeEditarAPI(apiId)) {
            throw new IllegalStateException(
                "No se puede actualizar la API porque tiene una solicitud de publicación activa o no está en estado BORRADOR"
            );
        }
        
        log.info("✅ Validación pasada: API {} puede ser editada", apiId);

        // Actualizar el estado de la API si cambió
        if (nuevoEstado != null && !nuevoEstado.equals(api.getEstadoApi().toString())) {
            api.setEstadoApi(API.EstadoApi.valueOf(nuevoEstado));
            log.info("📝 Estado de API actualizado a: {}", nuevoEstado);
        }

        // Actualizar categorías si se seleccionaron
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Categoria> categorias = categoriaRepository.findAllById(categoryIds);
            api.setCategorias(new HashSet<>(categorias));
            log.info("🏷️ Categorías actualizadas: {} categorías", categorias.size());
        } else {
            api.setCategorias(new HashSet<>());
        }

        // Actualizar etiquetas si se seleccionaron
        if (tagIds != null && !tagIds.isEmpty()) {
            // Convertir Long a Integer para las etiquetas
            List<Integer> etiquetaIds = tagIds.stream().map(Long::intValue).toList();
            List<Etiqueta> etiquetas = etiquetaRepository.findAllById(etiquetaIds);
            api.setEtiquetas(new HashSet<>(etiquetas));
            log.info("🏷️ Etiquetas actualizadas: {} etiquetas", etiquetas.size());
        } else {
            api.setEtiquetas(new HashSet<>());
        }

        // Obtener la documentación existente de la API
        Documentacion documentacion = api.getDocumentacion();
        if (documentacion == null) {
            // Si no existe documentación, crear una nueva
            documentacion = new Documentacion();
            documentacion.setApi(api);
            documentacion.setSeccionDocumentacion("Documentación de " + api.getNombreApi());
            documentacion = documentationRepository.save(documentacion);
            log.info("📄 Nueva documentación creada con ID: {}", documentacion.getDocumentacionId());
        } else {
            // Si ya existe, reutilizar la documentación existente
            log.info("📄 Reutilizando documentación existente con ID: {}", documentacion.getDocumentacionId());
        }

        // Crear la nueva versión de la API
        VersionAPI nuevaVersionAPI = new VersionAPI();
        nuevaVersionAPI.setNumeroVersion(nuevaVersion);
        nuevaVersionAPI.setDescripcionVersion(descripcionVersion); // Nueva descripción para esta versión
        nuevaVersionAPI.setApi(api);
        nuevaVersionAPI.setCreadoPor(usuario); // ✅ FIX: Usar setCreadoPor() en lugar de setCreador()
        nuevaVersionAPI.setCreadoEn(LocalDateTime.now()); // Timestamp de auditoría
        nuevaVersionAPI.setFechaLanzamiento(LocalDateTime.now().toLocalDate());
        nuevaVersionAPI.setDocumentacion(documentacion); // Asignar documentación

        log.info("📝 Nueva VersionAPI creada: Version={}, CreadorId={}, ApiId={}",
                nuevaVersion, usuario.getUsuarioId(), api.getApiId());

        // Guardar el contrato en GCS y obtener la URL
        try {
            String contractUrl = storageService.saveApiContract(api.getApiId(), nuevaVersion, contractContent);
            nuevaVersionAPI.setContratoApiUrl(contractUrl);
            log.info("☁️ Contrato guardado en: {}", contractUrl);
        } catch (Exception e) {
            throw new RuntimeException("Error guardando el contrato en el almacenamiento: " + e.getMessage(), e);
        }

        // Guardar la nueva versión API explícitamente
        VersionAPI savedVersionAPI = versionAPIRepository.save(nuevaVersionAPI);
        log.info("💾 Nueva VersionAPI guardada con ID: {}, Descripción: {}",
                savedVersionAPI.getVersionId(), savedVersionAPI.getDescripcionVersion());

        // Agregar la nueva versión a la API
        if (api.getVersiones() == null) {
            api.setVersiones(new HashSet<>());
        }
        api.getVersiones().add(savedVersionAPI);

        // Guardar la API actualizada
        API savedApi = apiRepository.save(api);
        log.info("✅ API actualizada exitosamente: API_ID={}, Nueva versión {} creada con ID_CREADOR={}",
                savedApi.getApiId(), nuevaVersion, usuario.getUsuarioId());

        return savedApi;
    }

    // =====================================================================
    // NUEVOS MÉTODOS PARA CREACIÓN CON DTOs - PHASE 3
    // =====================================================================

    /**
     * Crea una API básica usando DTOs (PHASE 3 - sin archivos CMS).
     * 
     * Flujo:
     * 1. Validar datos básicos (nombre único, versión válida)
     * 2. Crear entidad API en estado BORRADOR
     * 3. Subir contrato YAML a GCS
     * 4. Crear Enlace para el contrato
     * 5. Crear VersionAPI con URL del contrato
     * 6. Vincular categorías y etiquetas
     * 7. Crear Documentacion vacía (sin secciones CMS aún)
     * 
     * @param dto DTO con datos de la API (Tab 1 y Tab 2 del wizard)
     * @return ApiResponseDTO con resultado de la operación
     */
    @Transactional
    public ApiResponseDTO crearApiBasica(CrearApiDTO dto) {
        log.info("🚀 [PHASE 3] Iniciando creación de API básica: nombre={}, version={}",
                dto.getNombre(), dto.getVersion());

        try {
            // 1. Validar que el nombre de la API no exista
            if (apiRepository.findByNombreApi(dto.getNombre()).isPresent()) {
                log.warn("❌ Ya existe una API con el nombre: {}", dto.getNombre());
                return ApiResponseDTO.error("Ya existe una API con el nombre '" + dto.getNombre() + "'");
            }

            // 2. Validar el contrato OpenAPI antes de procesar
            log.info("🔍 Validando contrato OpenAPI antes de crear la API");
            OpenApiValidatorService.ValidationResult validationResult = 
                    openApiValidatorService.validarContrato(dto.getContratoYaml());

            if (!validationResult.isValid()) {
                log.error("❌ Contrato OpenAPI inválido: {}", validationResult.getMessage());
                return ApiResponseDTO.error("Contrato OpenAPI inválido: " + validationResult.getMessage());
            }

            if (validationResult.hasWarnings()) {
                log.warn("⚠️ Contrato tiene advertencias: {}", validationResult.getMessage());
                // Continuar pero loggear las advertencias
            } else {
                log.info("✅ Contrato OpenAPI validado correctamente");
            }

            // 3. Buscar el usuario creador
            Usuario usuario = usuarioRepository.findByUsername(dto.getCreadoPorUsername())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontrado: " + dto.getCreadoPorUsername()));
            log.info("👤 Usuario encontrado: ID={}, Username={}",
                    usuario.getUsuarioId(), usuario.getUsername());

            // 4. Crear entidad API (siempre en estado BORRADOR)
            API api = new API();
            api.setNombreApi(dto.getNombre());
            api.setDescripcionApi(dto.getDescripcion());
            api.setEstadoApi(API.EstadoApi.BORRADOR); // Siempre BORRADOR al crear
            api.setCreadoEn(LocalDateTime.now());
            api.setCreadoPor(usuario);

            // 5. Vincular categorías
            if (dto.getCategoriaIds() != null && !dto.getCategoriaIds().isEmpty()) {
                List<Categoria> categorias = categoriaRepository.findAllById(dto.getCategoriaIds());
                api.setCategorias(new HashSet<>(categorias));
                log.info("🏷️ Categorías vinculadas: {}", categorias.size());
            } else {
                api.setCategorias(new HashSet<>());
            }

            // 6. Vincular etiquetas
            if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
                List<Integer> etiquetaIds = dto.getTagIds().stream()
                        .map(Long::intValue)
                        .toList();
                List<Etiqueta> etiquetas = etiquetaRepository.findAllById(etiquetaIds);
                api.setEtiquetas(new HashSet<>(etiquetas));
                log.info("🏷️ Etiquetas vinculadas: {}", etiquetas.size());
            } else {
                api.setEtiquetas(new HashSet<>());
            }

            // 7. Guardar API para obtener ID
            api = apiRepository.save(api);
            log.info("💾 API guardada con ID: {}", api.getApiId());

            // 8. Crear Documentacion vacía (sin secciones CMS - PHASE 4)
            Documentacion documentacion = new Documentacion();
            documentacion.setApi(api);
            documentacion.setSeccionDocumentacion("Documentación de " + api.getNombreApi());
            documentacion = documentationRepository.save(documentacion);
            log.info("📄 Documentación vacía creada con ID: {}", documentacion.getDocumentacionId());

            // Establecer relación bidireccional
            api.setDocumentacion(documentacion);
            api = apiRepository.save(api);

            // 9. Crear VersionAPI con placeholder para contratoApiUrl
            VersionAPI versionAPI = new VersionAPI();
            versionAPI.setNumeroVersion(dto.getVersion());
            versionAPI.setDescripcionVersion(dto.getDescripcion());
            versionAPI.setApi(api);
            versionAPI.setCreadoPor(usuario); // ✅ FIX: Usar setCreadoPor() en lugar de setCreador()
            versionAPI.setCreadoEn(LocalDateTime.now()); // ✅ FIX: Establecer fecha de creación
            versionAPI.setFechaLanzamiento(LocalDateTime.now().toLocalDate());
            versionAPI.setDocumentacion(documentacion);
            versionAPI.setContratoApiUrl("pending"); // Placeholder temporal

            // 10. Guardar VersionAPI para obtener ID
            versionAPI = versionAPIRepository.save(versionAPI);
            log.info("💾 VersionAPI guardada con ID: {} (contrato pendiente)", versionAPI.getVersionId());

            // 11. Subir contrato YAML a GCS usando GoogleCloudStorageService (usando número de versión)
            String contentType = dto.getContratoYaml().trim().startsWith("{")
                    ? "application/json"
                    : "application/x-yaml";

            String gcsPath = googleCloudStorageService.uploadContract(
                    api.getApiId(),
                    versionAPI.getNumeroVersion(), // ✅ FIX: Usar número de versión semántica (ej: "1.0.0")
                    dto.getContratoYaml(),
                    contentType,
                    usuario.getUsuarioId()
            );
            log.info("☁️ Contrato subido a GCS: {}", gcsPath);

            // 12. Actualizar VersionAPI con la ruta real del contrato
            versionAPI.setContratoApiUrl(gcsPath);
            versionAPI = versionAPIRepository.save(versionAPI);
            log.info("✅ VersionAPI actualizada con URL del contrato");

            // 13. Crear Enlace para el contrato (usando API como contexto ya que VERSION_API no existe en la BD)
            Enlace enlaceContrato = new Enlace();
            enlaceContrato.setDireccionAlmacenamiento(gcsPath);
            enlaceContrato.setNombreArchivo("contract." + (contentType.contains("json") ? "json" : "yaml"));
            enlaceContrato.setFechaCreacionEnlace(LocalDateTime.now());
            enlaceContrato.setContextoType(Enlace.ContextoType.API);
            enlaceContrato.setContextoId(api.getApiId());
            enlaceContrato.setTipoEnlace(Enlace.TipoEnlace.STORAGE);
            enlaceContrato.setEstadoEnlace(Enlace.EstadoEnlace.ACTIVO);
            enlaceContrato.setCreadoPor(usuario);

            enlaceContrato = enlaceRepository.save(enlaceContrato);
            log.info("🔗 Enlace de contrato creado con ID: {}", enlaceContrato.getEnlaceId());

            // 13. Vincular versión a la API
            if (api.getVersiones() == null) {
                api.setVersiones(new HashSet<>());
            }
            api.getVersiones().add(versionAPI);
            api = apiRepository.save(api);

            log.info("✅ API básica creada exitosamente: API_ID={}, VersionAPI_ID={}",
                    api.getApiId(), versionAPI.getVersionId());

            return ApiResponseDTO.success(
                    "API '" + api.getNombreApi() + " v" + dto.getVersion() + "' creada exitosamente en estado BORRADOR",
                    api.getApiId(),
                    "/catalogo?filter=misApis"
            );

        } catch (ResourceNotFoundException e) {
            log.error("❌ Error: {}", e.getMessage());
            return ApiResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error al crear API básica: {}", e.getMessage(), e);
            return ApiResponseDTO.error("Error al crear API: " + e.getMessage());
        }
    }

    /**
     * Crea una API completa con documentación CMS (PHASE 4 - sin archivos, solo texto).
     * 
     * Flujo:
     * 1-13. Igual que crearApiBasica() (API + VersionAPI + Contrato)
     * 14. Procesar secciones CMS del DTO
     * 15. Crear entidades Contenido para cada sección
     * 16. Vincular Contenidos a la Documentacion
     * 17. Procesar enlaces de referencia de cada sección
     * 18. Crear entidades Enlace para los enlaces de referencia
     * 
     * @param dto DTO con datos de la API (Tab 1, Tab 2 y Tab 3 del wizard)
     * @return ApiResponseDTO con resultado de la operación
     */
    @Transactional
    public ApiResponseDTO crearApiConDocumentacion(CrearApiDTO dto) {
        log.info("🚀 [PHASE 4] Iniciando creación de API con documentación CMS: nombre={}, version={}, secciones={}",
                dto.getNombre(), dto.getVersion(), 
                dto.getSeccionesCms() != null ? dto.getSeccionesCms().size() : 0);

        // 1-13. Crear API básica (reutilizar lógica existente)
        ApiResponseDTO resultadoBasico = crearApiBasica(dto);

        if (resultadoBasico.getSuccess() == null || !resultadoBasico.getSuccess()) {
            log.error("❌ Error al crear API básica: {}", resultadoBasico.getMessage());
            return resultadoBasico;
        }

        // 14. Si no hay secciones CMS, retornar el resultado básico
        if (dto.getSeccionesCms() == null || dto.getSeccionesCms().isEmpty()) {
            log.info("ℹ️ No hay secciones CMS para procesar, API básica creada");
            return resultadoBasico;
        }

        try {
            // 15. Obtener la API recién creada
            Long apiId = resultadoBasico.getApiId();
            API api = apiRepository.findById(apiId)
                    .orElseThrow(() -> new ResourceNotFoundException("API no encontrada: " + apiId));

            // 16. Obtener la Documentacion asociada
            Documentacion documentacion = api.getDocumentacion();
            if (documentacion == null) {
                throw new IllegalStateException("La API no tiene Documentacion asociada");
            }

            // 17. Obtener el usuario creador
            Usuario usuario = usuarioRepository.findByUsername(dto.getCreadoPorUsername())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontrado: " + dto.getCreadoPorUsername()));

            // 18. Obtener la versión creada (la más reciente)
            VersionAPI versionAPI = api.getVersiones().stream()
                    .filter(v -> v.getNumeroVersion().equals(dto.getVersion()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("VersionAPI no encontrada"));

            // 19. Procesar cada sección CMS
            log.info("📝 Procesando {} secciones CMS", dto.getSeccionesCms().size());
            int seccionesCreadas = 0;

            for (SeccionCmsDTO seccionDto : dto.getSeccionesCms()) {
                log.info("📄 Procesando sección: {}", seccionDto.getTitulo());

                // 20. Crear entidad Contenido (solo titulo y orden, no texto)
                org.project.project.model.entity.Contenido contenido = 
                        new org.project.project.model.entity.Contenido();
                
                contenido.setTituloContenido(seccionDto.getTitulo());
                contenido.setOrden(seccionDto.getOrden());
                
                // Buscar clasificación en la BD
                Clasificacion clasificacion;
                if (seccionDto.getTipoContenido() != null && !seccionDto.getTipoContenido().trim().isEmpty()) {
                    clasificacion = clasificacionRepository
                            .findByTipoContenidoTexto(seccionDto.getTipoContenido())
                            .orElseGet(() -> {
                                log.warn("⚠️ Clasificación '{}' no encontrada, usando 'OTRO'", 
                                        seccionDto.getTipoContenido());
                                return clasificacionRepository
                                        .findByTipoContenidoTexto("OTRO")
                                        .orElse(null);
                            });
                } else {
                    clasificacion = clasificacionRepository
                            .findByTipoContenidoTexto("OTRO")
                            .orElse(null);
                }
                
                if (clasificacion == null) {
                    throw new IllegalStateException("No se encontró clasificación 'OTRO' en la base de datos");
                }
                contenido.setClasificacion(clasificacion);

                contenido.setFechaCreacion(LocalDateTime.now());
                contenido.setDocumentacion(documentacion);
                contenido.setVersionApi(versionAPI);

                // 21. Guardar Contenido
                contenido = contenidoRepository.save(contenido);
                seccionesCreadas++;
                log.info("✅ Contenido creado: ID={}, Titulo={}",
                        contenido.getContenidoId(), contenido.getTituloContenido());

                // 21.1. Guardar texto Markdown si existe
                // 21.1. Guardar texto Markdown con estrategia híbrida (BD < 64KB, GCS >= 64KB)
                if (seccionDto.getContenido() != null && !seccionDto.getContenido().trim().isEmpty()) {
                    String markdownContenido = seccionDto.getContenido();
                    
                    if (debeGuardarMarkdownEnBD(markdownContenido)) {
                        //  Estrategia 1: Contenido pequeño  Guardar en BD (campo markdown_content)
                        org.project.project.model.entity.Recurso recursoMarkdown = 
                                new org.project.project.model.entity.Recurso();
                        recursoMarkdown.setNombreArchivo("contenido_markdown.md");
                        recursoMarkdown.setMimeType("text/markdown");
                        recursoMarkdown.setFormatoRecurso("md");
                        recursoMarkdown.setTipoRecurso(org.project.project.model.entity.Recurso.TipoRecurso.OTRO);
                        recursoMarkdown.setContenido(contenido);
                        recursoMarkdown.setMarkdownContent(markdownContenido); //  Guardar en BD
                        
                        // Crear Enlace tipo TEXTO_CONTENIDO
                        Enlace enlaceTexto = new Enlace();
                        enlaceTexto.setTipoEnlace(Enlace.TipoEnlace.TEXTO_CONTENIDO);
                        enlaceTexto.setEstadoEnlace(Enlace.EstadoEnlace.ACTIVO);
                        enlaceTexto.setNombreArchivo("contenido_markdown.md");
                        enlaceTexto.setDireccionAlmacenamiento("BD:markdown_content"); //  Placeholder (contenido en campo BD)
                        enlaceTexto.setFechaCreacionEnlace(LocalDateTime.now());
                        enlaceTexto.setCreadoPor(usuario);
                        enlaceTexto.setContextoType(Enlace.ContextoType.CONTENIDO);
                        enlaceTexto.setContextoId(contenido.getContenidoId());
                        enlaceTexto = enlaceRepository.save(enlaceTexto);
                        
                        recursoMarkdown.setEnlace(enlaceTexto);
                        recursoMarkdown = recursoRepository.save(recursoMarkdown);
                        
                        log.info(" Texto Markdown guardado en BD: Contenido ID={}, Recurso ID={}, Tamaño={} bytes",
                                contenido.getContenidoId(), recursoMarkdown.getRecursoId(),
                                markdownContenido.getBytes(StandardCharsets.UTF_8).length);
                        
                    } else {
                        //  Estrategia 2: Contenido grande  Subir a GCS
                        org.project.project.model.entity.Recurso recursoMarkdown = 
                                new org.project.project.model.entity.Recurso();
                        recursoMarkdown.setNombreArchivo("contenido_markdown.md");
                        recursoMarkdown.setMimeType("text/markdown");
                        recursoMarkdown.setFormatoRecurso("md");
                        recursoMarkdown.setTipoRecurso(org.project.project.model.entity.Recurso.TipoRecurso.OTRO);
                        recursoMarkdown.setContenido(contenido);
                        recursoMarkdown.setMarkdownContent(null); //  NULL porque va a GCS
                        
                        // Crear Enlace tipo STORAGE
                        Enlace enlaceStorage = new Enlace();
                        enlaceStorage.setTipoEnlace(Enlace.TipoEnlace.STORAGE);
                        enlaceStorage.setEstadoEnlace(Enlace.EstadoEnlace.ACTIVO);
                        enlaceStorage.setNombreArchivo("contenido_markdown.md");
                        enlaceStorage.setDireccionAlmacenamiento("PENDING_UPLOAD"); // Temporal
                        enlaceStorage.setFechaCreacionEnlace(LocalDateTime.now());
                        enlaceStorage.setCreadoPor(usuario);
                        enlaceStorage.setContextoType(Enlace.ContextoType.CONTENIDO);
                        enlaceStorage.setContextoId(contenido.getContenidoId());
                        enlaceStorage = enlaceRepository.save(enlaceStorage);
                        
                        recursoMarkdown.setEnlace(enlaceStorage);
                        recursoMarkdown = recursoRepository.save(recursoMarkdown);
                        
                        // Convertir String a MultipartFile para subir a GCS
                        byte[] markdownBytes = markdownContenido.getBytes(StandardCharsets.UTF_8);
                        MultipartFile markdownFile = new MultipartFile() {
                            @Override
                            public String getName() { return "file"; }
                            
                            @Override
                            public String getOriginalFilename() { return "contenido_markdown.md"; }
                            
                            @Override
                            public String getContentType() { return "text/markdown"; }
                            
                            @Override
                            public boolean isEmpty() { return markdownBytes.length == 0; }
                            
                            @Override
                            public long getSize() { return markdownBytes.length; }
                            
                            @Override
                            public byte[] getBytes() { return markdownBytes; }
                            
                            @Override
                            public java.io.InputStream getInputStream() {
                                return new java.io.ByteArrayInputStream(markdownBytes);
                            }
                            
                            @Override
                            public void transferTo(java.io.File dest) throws java.io.IOException {
                                java.nio.file.Files.write(dest.toPath(), markdownBytes);
                            }
                        };
                        
                        // Subir a GCS
                        String gcsPath = googleCloudStorageService.uploadRecurso(
                                apiId,
                                versionAPI.getNumeroVersion(),
                                recursoMarkdown.getRecursoId(),
                                markdownFile,
                                usuario.getUsuarioId()
                        );
                        
                        // Actualizar enlace con ruta GCS
                        enlaceStorage.setDireccionAlmacenamiento(gcsPath);
                        enlaceRepository.save(enlaceStorage);
                        
                        log.info(" Texto Markdown subido a GCS: Contenido ID={}, Recurso ID={}, Tamaño={} bytes, GCS={}",
                                contenido.getContenidoId(), recursoMarkdown.getRecursoId(),
                                markdownBytes.length, gcsPath);
                    }
                }
                // 21.2. Guardar enlaces de referencia si existen
                if (seccionDto.getEnlaces() != null && !seccionDto.getEnlaces().isEmpty()) {
                    int ordenEnlace = 1;
                    for (EnlaceReferenciaDTO enlaceDto : seccionDto.getEnlaces()) {
                        if (enlaceDto != null && enlaceDto.getUrl() != null && !enlaceDto.getUrl().trim().isEmpty()) {
                            Enlace enlaceReferencia = new Enlace();
                            enlaceReferencia.setTipoEnlace(Enlace.TipoEnlace.ENLACE_EXTERNO);
                            enlaceReferencia.setEstadoEnlace(Enlace.EstadoEnlace.ACTIVO);
                            // Usar descripción como nombre si existe, sino usar tipo o genérico
                            String nombreArchivo = enlaceDto.getDescripcion() != null && !enlaceDto.getDescripcion().trim().isEmpty()
                                    ? enlaceDto.getDescripcion()
                                    : (enlaceDto.getTipo() != null ? enlaceDto.getTipo() : "referencia_" + ordenEnlace);
                            enlaceReferencia.setNombreArchivo(nombreArchivo);
                            enlaceReferencia.setDireccionAlmacenamiento(enlaceDto.getUrl()); // URL externa
                            enlaceReferencia.setFechaCreacionEnlace(LocalDateTime.now());
                            enlaceReferencia.setCreadoPor(usuario);
                            enlaceReferencia.setContextoType(Enlace.ContextoType.CONTENIDO);
                            enlaceReferencia.setContextoId(contenido.getContenidoId());
                            enlaceRepository.save(enlaceReferencia);
                            ordenEnlace++;
                        }
                    }
                    log.info("✅ {} enlaces de referencia guardados para Contenido ID={}", 
                            ordenEnlace - 1, contenido.getContenidoId());
                }

                // 22. Procesar archivos adjuntos si existen
                if (seccionDto.getArchivos() != null && !seccionDto.getArchivos().isEmpty()) {
                    log.info("📎 Procesando {} archivos adjuntos para sección '{}'",
                            seccionDto.getArchivos().size(), seccionDto.getTitulo());

                    for (org.springframework.web.multipart.MultipartFile archivo : seccionDto.getArchivos()) {
                        if (archivo.isEmpty()) {
                            log.warn("⚠️ Archivo vacío omitido");
                            continue;
                        }

                        try {
                            // 25. Crear entidad Recurso (metadata del archivo)
                            org.project.project.model.entity.Recurso recurso = 
                                    new org.project.project.model.entity.Recurso();
                            
                            recurso.setNombreArchivo(archivo.getOriginalFilename());
                            recurso.setMimeType(archivo.getContentType());
                            recurso.setFormatoRecurso(obtenerExtension(archivo.getOriginalFilename()));
                            recurso.setTipoRecurso(mapearTipoRecurso(archivo.getContentType()));
                            recurso.setContenido(contenido);

                            // 26. Crear entidad Enlace tipo STORAGE (temporal, sin GCS path todavía)
                            Enlace enlaceStorage = new Enlace();
                            enlaceStorage.setNombreArchivo(archivo.getOriginalFilename());
                            enlaceStorage.setTipoEnlace(Enlace.TipoEnlace.STORAGE);
                            enlaceStorage.setEstadoEnlace(Enlace.EstadoEnlace.ACTIVO);
                            enlaceStorage.setFechaCreacionEnlace(LocalDateTime.now());
                            enlaceStorage.setCreadoPor(usuario);
                            enlaceStorage.setContextoType(Enlace.ContextoType.CONTENIDO);
                            enlaceStorage.setContextoId(contenido.getContenidoId());
                            // Asignar placeholder temporal para direccion_almacenamiento (NOT NULL)
                            enlaceStorage.setDireccionAlmacenamiento("PENDING_UPLOAD");
                            
                            // Guardar enlace para obtener ID
                            enlaceStorage = enlaceRepository.save(enlaceStorage);
                            
                            // Asignar enlace al recurso
                            recurso.setEnlace(enlaceStorage);
                            
                            // Guardar recurso para obtener ID
                            recurso = recursoRepository.save(recurso);

                            // 27. Subir archivo a GCS (usando número de versión semántica)
                            String gcsPath = googleCloudStorageService.uploadRecurso(
                                    apiId,
                                    versionAPI.getNumeroVersion(), // ✅ FIX: Usar número de versión semántica
                                    recurso.getRecursoId(),
                                    archivo,
                                    usuario.getUsuarioId()
                            );

                            // 28. Actualizar enlace con la ruta GCS
                            enlaceStorage.setDireccionAlmacenamiento(gcsPath);
                            enlaceStorage = enlaceRepository.save(enlaceStorage);

                            log.info("✅ Archivo subido: ID={}, Nombre={}, GCS={}",
                                    recurso.getRecursoId(), recurso.getNombreArchivo(), gcsPath);

                        } catch (Exception e) {
                            log.error("❌ Error al subir archivo '{}': {}",
                                    archivo.getOriginalFilename(), e.getMessage(), e);
                            // No lanzar excepción, continuar con otros archivos
                        }
                    }
                }
            }

            log.info("✅ API con documentación creada exitosamente: API_ID={}, Secciones CMS={}",
                    apiId, seccionesCreadas);

            return ApiResponseDTO.success(
                    "API '" + api.getNombreApi() + " v" + dto.getVersion() + 
                    "' creada exitosamente con " + seccionesCreadas + " secciones de documentación",
                    apiId,
                    "/catalogo?filter=misApis"
            );

        } catch (Exception e) {
            log.error("❌ Error al procesar secciones CMS: {}", e.getMessage(), e);
            // La API básica ya fue creada, pero falló la documentación
            return ApiResponseDTO.error(
                    "API creada pero error al procesar documentación: " + e.getMessage()
            );
        }
    }

    /**
     * Copia toda la documentación CMS de una versión a otra (PHASE 4).
     * 
     * Usado cuando se crea una nueva versión con opción "copiar documentación".
     * 
     * Flujo:
     * 1. Obtener todas las entidades Contenido de la versión origen
     * 2. Para cada Contenido:
     *    a. Duplicar entidad Contenido
     *    b. Vincular a la nueva Documentacion
     *    c. Obtener todos los Enlaces vinculados al Contenido original
     *    d. Duplicar Enlaces de tipo URL_EXTERNO
     * 3. Retornar número de elementos copiados
     * 
     * IMPORTANTE: Este método solo copia contenido sin archivos (PHASE 4).
     * La copia de archivos se implementará en PHASE 5.
     * 
     * @param documentacionOrigenId ID de la Documentacion origen
     * @param documentacionDestinoId ID de la Documentacion destino
     * @param usuarioId ID del usuario que ejecuta la copia
     * @return Map con estadísticas de la copia (contenidosCopiados, enlacesCopiados)
     */
    @Transactional
    public java.util.Map<String, Integer> copiarDocumentacion(
            Long documentacionOrigenId,
            Long documentacionDestinoId,
            Long usuarioId) {

        log.info("📋 Iniciando copia de documentación: origen={}, destino={}, usuario={}",
                documentacionOrigenId, documentacionDestinoId, usuarioId);

        int contenidosCopiados = 0;
        int enlacesCopiados = 0;

        try {
            // 1. Validar que existe documentación origen
            if (!documentationRepository.existsById(documentacionOrigenId)) {
                throw new ResourceNotFoundException(
                        "Documentacion origen no encontrada: " + documentacionOrigenId);
            }

            // 2. Obtener documentación destino
            Documentacion docDestino = documentationRepository.findById(documentacionDestinoId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Documentacion destino no encontrada: " + documentacionDestinoId));

            // 3. Obtener usuario
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontrado: " + usuarioId));

            // 2. Obtener todos los Contenidos de la documentación origen
            List<org.project.project.model.entity.Contenido> contenidosOrigen = 
                    documentationRepository.findContenidosByDocumentacionId(documentacionOrigenId);

            if (contenidosOrigen.isEmpty()) {
                log.info("ℹ️ No hay contenidos para copiar en documentación {}", documentacionOrigenId);
                return java.util.Map.of("contenidosCopiados", 0, "enlacesCopiados", 0);
            }

            log.info("📄 Encontrados {} contenidos para copiar", contenidosOrigen.size());

            // 3. Copiar cada Contenido
            for (org.project.project.model.entity.Contenido contenidoOrigen : contenidosOrigen) {
                log.info("📝 Copiando contenido: {}", contenidoOrigen.getTituloContenido());

                // 4. Duplicar entidad Contenido
                org.project.project.model.entity.Contenido contenidoNuevo = 
                        new org.project.project.model.entity.Contenido();
                
                contenidoNuevo.setTituloContenido(contenidoOrigen.getTituloContenido());
                contenidoNuevo.setOrden(contenidoOrigen.getOrden());
                contenidoNuevo.setClasificacion(contenidoOrigen.getClasificacion());
                contenidoNuevo.setFechaCreacion(LocalDateTime.now());
                contenidoNuevo.setDocumentacion(docDestino);
                contenidoNuevo.setVersionApi(contenidoOrigen.getVersionApi());

                // 5. Guardar Contenido nuevo
                contenidoNuevo = contenidoRepository.save(contenidoNuevo);
                contenidosCopiados++;
                log.info("✅ Contenido copiado: ID_origen={}, ID_nuevo={}",
                        contenidoOrigen.getContenidoId(), contenidoNuevo.getContenidoId());

                // 6. Obtener Enlaces vinculados al Contenido original
                List<Enlace> enlacesOrigen = enlaceRepository.findByContextoTypeAndContextoId(
                        Enlace.ContextoType.CONTENIDO,
                        contenidoOrigen.getContenidoId()
                );

                // 7. Copiar enlaces de tipo STORAGE
                for (Enlace enlaceOrigen : enlacesOrigen) {
                    if (enlaceOrigen.getTipoEnlace() == Enlace.TipoEnlace.STORAGE) {
                        // 7b. Copiar enlace STORAGE (requiere copiar archivo físico en GCS)
                        log.info("📎 Copiando archivo STORAGE: {}", enlaceOrigen.getNombreArchivo());
                        
                        try {
                            // Obtener el Recurso asociado al enlace origen
                            org.project.project.model.entity.Recurso recursoOrigen = 
                                    enlaceOrigen.getRecursos().stream().findFirst().orElse(null);
                            
                            if (recursoOrigen == null) {
                                log.warn("⚠️ No se encontró Recurso para enlace {}", 
                                        enlaceOrigen.getEnlaceId());
                                continue;
                            }

                            // Crear nuevo Enlace (temporal, sin GCS path todavía)
                            Enlace enlaceNuevo = new Enlace();
                            enlaceNuevo.setNombreArchivo(enlaceOrigen.getNombreArchivo());
                            enlaceNuevo.setDireccionAlmacenamiento("PENDING_COPY"); // Placeholder temporal
                            enlaceNuevo.setTipoEnlace(Enlace.TipoEnlace.STORAGE);
                            enlaceNuevo.setEstadoEnlace(Enlace.EstadoEnlace.ACTIVO);
                            enlaceNuevo.setFechaCreacionEnlace(LocalDateTime.now());
                            enlaceNuevo.setCreadoPor(usuario);
                            enlaceNuevo.setContextoType(Enlace.ContextoType.CONTENIDO);
                            enlaceNuevo.setContextoId(contenidoNuevo.getContenidoId());
                            
                            // Guardar enlace para obtener ID
                            enlaceNuevo = enlaceRepository.save(enlaceNuevo);

                            // Crear nuevo Recurso
                            org.project.project.model.entity.Recurso recursoNuevo = 
                                    new org.project.project.model.entity.Recurso();
                            recursoNuevo.setNombreArchivo(recursoOrigen.getNombreArchivo());
                            recursoNuevo.setMimeType(recursoOrigen.getMimeType());
                            recursoNuevo.setFormatoRecurso(recursoOrigen.getFormatoRecurso());
                            recursoNuevo.setTipoRecurso(recursoOrigen.getTipoRecurso());
                            recursoNuevo.setContenido(contenidoNuevo);
                            recursoNuevo.setEnlace(enlaceNuevo);
                            
                            // Guardar recurso para obtener ID
                            recursoNuevo = recursoRepository.save(recursoNuevo);

                            // TODO: Copiar archivo físico en GCS
                            // Esto requiere obtener apiId, sourceVersionId, targetVersionId
                            // Se implementará cuando se necesite copiar versiones completas
                            log.warn("⚠️ Copia física del archivo en GCS pendiente (requiere más contexto)");

                            enlacesCopiados++;
                            log.info("✅ Enlace STORAGE copiado: ID_origen={}, ID_nuevo={} (archivo físico pendiente)",
                                    enlaceOrigen.getEnlaceId(), enlaceNuevo.getEnlaceId());
                            
                        } catch (Exception e) {
                            log.error("❌ Error al copiar archivo STORAGE: {}", e.getMessage(), e);
                            // No lanzar excepción, continuar con otros archivos
                        }
                        
                    } else {
                        log.info("ℹ️ Enlace tipo {} no copiado", enlaceOrigen.getTipoEnlace());
                    }
                }
            }

            log.info("✅ Documentación copiada exitosamente: {} contenidos, {} enlaces",
                    contenidosCopiados, enlacesCopiados);

            return java.util.Map.of(
                    "contenidosCopiados", contenidosCopiados,
                    "enlacesCopiados", enlacesCopiados
            );

        } catch (Exception e) {
            log.error("❌ Error al copiar documentación: {}", e.getMessage(), e);
            throw new RuntimeException("Error al copiar documentación: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    // MÉTODOS AUXILIARES PRIVADOS
    // =====================================================================

    /**
     * Obtiene la extensión de un archivo desde su nombre.
     * 
     * @param filename Nombre del archivo con extensión
     * @return Extensión sin el punto (ej: "pdf", "png"), o "unknown" si no tiene
     */
    private String obtenerExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        int lastDot = filename.lastIndexOf('.');
        return filename.substring(lastDot + 1).toLowerCase();
    }

    /**
     * Mapea el MIME type a un TipoRecurso del enum.
     * 
     * @param mimeType MIME type del archivo (ej: "application/pdf")
     * @return TipoRecurso correspondiente
     */
    private org.project.project.model.entity.Recurso.TipoRecurso mapearTipoRecurso(String mimeType) {
        if (mimeType == null) {
            return org.project.project.model.entity.Recurso.TipoRecurso.OTRO;
        }

        // Normalizar a minúsculas
        mimeType = mimeType.toLowerCase();

        // Mapeo por tipo principal
        if (mimeType.startsWith("text/")) {
            return org.project.project.model.entity.Recurso.TipoRecurso.TEXTO;
        } else if (mimeType.startsWith("image/")) {
            return org.project.project.model.entity.Recurso.TipoRecurso.IMAGEN;
        } else if (mimeType.startsWith("audio/")) {
            return org.project.project.model.entity.Recurso.TipoRecurso.AUDIO;
        } else if (mimeType.startsWith("video/")) {
            return org.project.project.model.entity.Recurso.TipoRecurso.VIDEO;
        }

        // Mapeo por subtipo específico
        if (mimeType.contains("pdf") || 
            mimeType.contains("msword") || 
            mimeType.contains("wordprocessingml") ||
            mimeType.contains("spreadsheet") ||
            mimeType.contains("presentation")) {
            return org.project.project.model.entity.Recurso.TipoRecurso.DOCUMENTO;
        }

        // Archivos de código fuente
        if (mimeType.contains("javascript") || 
            mimeType.contains("java") ||
            mimeType.contains("python") ||
            mimeType.contains("json") ||
            mimeType.contains("xml") ||
            mimeType.contains("yaml") ||
            mimeType.equals("text/x-python") ||
            mimeType.equals("text/x-java-source")) {
            return org.project.project.model.entity.Recurso.TipoRecurso.CODIGO;
        }

        // Gráficas y diagramas (SVG, diagrams.net, etc.)
        if (mimeType.contains("svg") || 
            mimeType.contains("vnd.ms-excel") ||
            mimeType.contains("chart")) {
            return org.project.project.model.entity.Recurso.TipoRecurso.GRAFICA;
        }

        return org.project.project.model.entity.Recurso.TipoRecurso.OTRO;
    }

    // =====================================================================
    // FASE 0.4: M�todo helper para estrategia de almacenamiento h�brido
    // =====================================================================
    /**
     * Determina si el contenido Markdown debe guardarse en BD o en GCS.
     * 
     * Estrategia:
     * - Contenido < 64KB  BD (recurso.markdown_content + tipo_enlace=TEXTO_CONTENIDO)
     * - Contenido >= 64KB  GCS (enlace.direccion_almacenamiento + tipo_enlace=STORAGE)
     * 
     * @param contenido Contenido Markdown a evaluar
     * @return true si debe guardarse en BD, false si debe subirse a GCS
     */
    private boolean debeGuardarMarkdownEnBD(String contenido) {
        if (contenido == null || contenido.trim().isEmpty()) {
            return false; // Contenido vac�o no se guarda
        }
        
        // Calcular tama�o en bytes (UTF-8)
        int sizeInBytes = contenido.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        
        boolean enBD = sizeInBytes < MARKDOWN_SIZE_THRESHOLD;
        
        log.debug(" Contenido Markdown: {} bytes  Estrategia: {}", sizeInBytes, enBD ? "BD" : "GCS");
        
        return enBD;
    }
}
