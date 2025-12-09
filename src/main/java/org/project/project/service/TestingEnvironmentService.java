package org.project.project.service;

import org.project.project.model.entity.EntornoPrueba;
import org.project.project.model.entity.API;
import org.project.project.model.entity.VersionAPI;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.EntornoPruebaRepository;
import org.project.project.repository.APIRepository;
import org.project.project.repository.VersionAPIRepository;
import org.project.project.repository.UsuarioRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TestingEnvironmentService {

    @Autowired
    private EntornoPruebaRepository entornoPruebaRepository;

    @Autowired
    private APIRepository apiRepository;

    @Autowired
    private VersionAPIRepository versionAPIRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // =================== CRUD OPERATIONS ===================

    /**
     * Listar todos los entornos de prueba
     */
    @Transactional(readOnly = true)
    public List<EntornoPrueba> listarEntornos() {
        return entornoPruebaRepository.findAll();
    }

    /**
     * Buscar entorno por ID
     */
    @Transactional(readOnly = true)
    public EntornoPrueba buscarEntornoPorId(Long id) {
        return entornoPruebaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entorno de prueba no encontrado con id: " + id));
    }

    /**
     * Crear nuevo entorno de prueba
     */
    public EntornoPrueba crearEntorno(EntornoPrueba entorno, Long usuarioId, Long apiId, Long versionId) {
        // Asignar usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));
        entorno.setUsuario(usuario);

        // Asignar API
        API api = apiRepository.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("API no encontrada con id: " + apiId));
        entorno.setApi(api);

        // Asignar versión si se proporciona
        if (versionId != null) {
            VersionAPI version = versionAPIRepository.findById(versionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Versión de API no encontrada con id: " + versionId));
            entorno.setVersion(version);
        }

        // Configurar valores por defecto
        if (entorno.getFechaCreacion() == null) {
            entorno.setFechaCreacion(LocalDateTime.now());
        }
        if (entorno.getEstadoEntorno() == null) {
            entorno.setEstadoEntorno(EntornoPrueba.EstadoEntorno.ACTIVO);
        }
        if (entorno.getLimiteLlamadasDia() == null) {
            entorno.setLimiteLlamadasDia(1000);
        }
        if (entorno.getLlamadasRealizadas() == null) {
            entorno.setLlamadasRealizadas(0);
        }

        return entornoPruebaRepository.save(entorno);
    }

    /**
     * Actualizar entorno de prueba
     */
    public EntornoPrueba actualizarEntorno(Long id, EntornoPrueba entornoDetalles) {
        EntornoPrueba entorno = buscarEntornoPorId(id);

        entorno.setNombreEntorno(entornoDetalles.getNombreEntorno());
        entorno.setDescripcionEntorno(entornoDetalles.getDescripcionEntorno());

        if (entornoDetalles.getEstadoEntorno() != null) {
            entorno.setEstadoEntorno(entornoDetalles.getEstadoEntorno());
        }
        if (entornoDetalles.getLimiteLlamadasDia() != null) {
            entorno.setLimiteLlamadasDia(entornoDetalles.getLimiteLlamadasDia());
        }
        if (entornoDetalles.getFechaExpiracion() != null) {
            entorno.setFechaExpiracion(entornoDetalles.getFechaExpiracion());
        }

        return entornoPruebaRepository.save(entorno);
    }

    /**
     * Eliminar entorno de prueba
     */
    public void eliminarEntorno(Long id) {
        EntornoPrueba entorno = buscarEntornoPorId(id);
        entornoPruebaRepository.delete(entorno);
    }

    // =================== BÚSQUEDAS ESPECÍFICAS ===================

    /**
     * Listar entornos de un usuario
     */
    @Transactional(readOnly = true)
    public List<EntornoPrueba> listarEntornosPorUsuario(Long usuarioId) {
        return entornoPruebaRepository.findByUsuario_UsuarioId(usuarioId);
    }

    /**
     * Listar entornos activos de un usuario
     */
    @Transactional(readOnly = true)
    public List<EntornoPrueba> listarEntornosActivosPorUsuario(Long usuarioId) {
        return entornoPruebaRepository.findActiveEnvironmentsByUsuarioId(usuarioId);
    }

    /**
     * Listar entornos de una API
     */
    @Transactional(readOnly = true)
    public List<EntornoPrueba> listarEntornosPorApi(Long apiId) {
        return entornoPruebaRepository.findByApi_ApiId(apiId);
    }

    /**
     * Listar entornos activos de una API
     */
    @Transactional(readOnly = true)
    public List<EntornoPrueba> listarEntornosActivosPorApi(Long apiId) {
        return entornoPruebaRepository.findActiveEnvironmentsByApiId(apiId);
    }

    /**
     * Listar entornos de un usuario para una API específica
     */
    @Transactional(readOnly = true)
    public List<EntornoPrueba> listarEntornosPorUsuarioYApi(Long usuarioId, Long apiId) {
        return entornoPruebaRepository.findByUsuario_UsuarioIdAndApi_ApiId(usuarioId, apiId);
    }

    /**
     * Listar entornos por estado
     */
    @Transactional(readOnly = true)
    public List<EntornoPrueba> listarEntornosPorEstado(EntornoPrueba.EstadoEntorno estado) {
        return entornoPruebaRepository.findByEstadoEntorno(estado);
    }

    // =================== GESTIÓN DE ESTADO ===================

    /**
     * Activar entorno
     */
    public EntornoPrueba activarEntorno(Long id) {
        EntornoPrueba entorno = buscarEntornoPorId(id);
        entorno.setEstadoEntorno(EntornoPrueba.EstadoEntorno.ACTIVO);
        return entornoPruebaRepository.save(entorno);
    }

    /**
     * Pausar entorno
     */
    public EntornoPrueba pausarEntorno(Long id) {
        EntornoPrueba entorno = buscarEntornoPorId(id);
        entorno.setEstadoEntorno(EntornoPrueba.EstadoEntorno.PAUSADO);
        return entornoPruebaRepository.save(entorno);
    }

    /**
     * Suspender entorno
     */
    public EntornoPrueba suspenderEntorno(Long id) {
        EntornoPrueba entorno = buscarEntornoPorId(id);
        entorno.setEstadoEntorno(EntornoPrueba.EstadoEntorno.PAUSADO);
        return entornoPruebaRepository.save(entorno);
    }

    /**
     * Archivar entorno (marcar como eliminado)
     */
    public EntornoPrueba archivarEntorno(Long id) {
        EntornoPrueba entorno = buscarEntornoPorId(id);
        entorno.setEstadoEntorno(EntornoPrueba.EstadoEntorno.ELIMINADO);
        return entornoPruebaRepository.save(entorno);
    }

    // =================== GESTIÓN DE LLAMADAS ===================

    /**
     * Incrementar contador de llamadas
     */
    public EntornoPrueba registrarLlamada(Long id) {
        EntornoPrueba entorno = buscarEntornoPorId(id);

        // Verificar si el entorno está activo
        if (entorno.getEstadoEntorno() != EntornoPrueba.EstadoEntorno.ACTIVO) {
            throw new IllegalStateException("El entorno no está activo");
        }

        // Verificar límite de llamadas
        if (entorno.getLlamadasRealizadas() >= entorno.getLimiteLlamadasDia()) {
            throw new IllegalStateException("Límite de llamadas diarias alcanzado");
        }

        entorno.setLlamadasRealizadas(entorno.getLlamadasRealizadas() + 1);
        entorno.setUltimaLlamada(LocalDateTime.now());

        return entornoPruebaRepository.save(entorno);
    }

    /**
     * Resetear contador de llamadas (para uso diario)
     */
    public EntornoPrueba resetearContadorLlamadas(Long id) {
        EntornoPrueba entorno = buscarEntornoPorId(id);
        entorno.setLlamadasRealizadas(0);
        return entornoPruebaRepository.save(entorno);
    }

    /**
     * Verificar si un entorno puede realizar más llamadas
     */
    @Transactional(readOnly = true)
    public boolean puedeRealizarLlamadas(Long id) {
        EntornoPrueba entorno = buscarEntornoPorId(id);
        return entorno.getEstadoEntorno() == EntornoPrueba.EstadoEntorno.ACTIVO
                && entorno.getLlamadasRealizadas() < entorno.getLimiteLlamadasDia();
    }

    // =================== GESTIÓN DE EXPIRACIÓN ===================

    /**
     * Listar entornos expirados
     */
    @Transactional(readOnly = true)
    public List<EntornoPrueba> listarEntornosExpirados() {
        return entornoPruebaRepository.findExpiredEnvironments(LocalDateTime.now());
    }

    /**
     * Procesar entornos expirados (marcarlos como expirados)
     */
    public int procesarEntornosExpirados() {
        List<EntornoPrueba> expirados = listarEntornosExpirados();
        int contador = 0;

        for (EntornoPrueba entorno : expirados) {
            entorno.setEstadoEntorno(EntornoPrueba.EstadoEntorno.EXPIRADO);
            entornoPruebaRepository.save(entorno);
            contador++;
        }

        return contador;
    }

    /**
     * Extender fecha de expiración de un entorno
     */
    public EntornoPrueba extenderExpiracion(Long id, int dias) {
        EntornoPrueba entorno = buscarEntornoPorId(id);

        if (entorno.getFechaExpiracion() == null) {
            entorno.setFechaExpiracion(LocalDateTime.now().plusDays(dias));
        } else {
            entorno.setFechaExpiracion(entorno.getFechaExpiracion().plusDays(dias));
        }

        return entornoPruebaRepository.save(entorno);
    }

    // =================== ESTADÍSTICAS ===================

    /**
     * Contar entornos activos de un usuario
     */
    @Transactional(readOnly = true)
    public long contarEntornosActivosDeUsuario(Long usuarioId) {
        return entornoPruebaRepository.countActiveEnvironmentsByUsuarioId(usuarioId);
    }

    /**
     * Listar entornos que alcanzaron su límite de llamadas
     */
    @Transactional(readOnly = true)
    public List<EntornoPrueba> listarEntornosEnLimite() {
        return entornoPruebaRepository.findEnvironmentsAtLimit();
    }

    /**
     * Verificar si un usuario puede crear más entornos
     */
    @Transactional(readOnly = true)
    public boolean puedeCrearMasEntornos(Long usuarioId, int limiteMaximo) {
        long entornosActivos = contarEntornosActivosDeUsuario(usuarioId);
        return entornosActivos < limiteMaximo;
    }
}
