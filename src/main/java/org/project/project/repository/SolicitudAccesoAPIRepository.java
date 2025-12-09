package org.project.project.repository;

import org.project.project.model.entity.SolicitudAccesoAPI;
import org.project.project.model.entity.SolicitudAccesoAPI.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar solicitudes de acceso a APIs
 * Con queries especializadas para el sistema de aprobación por PROVIDER
 */
@Repository
public interface SolicitudAccesoAPIRepository extends JpaRepository<SolicitudAccesoAPI, Long> {
    
    // ========== QUERIES POR USUARIO DEV ==========
    
    /**
     * Busca todas las solicitudes de un usuario DEV, ordenadas por fecha descendente
     */
    List<SolicitudAccesoAPI> findBySolicitanteUsuarioIdOrderByFechaSolicitudDesc(Long usuarioId);
    
    /**
     * Busca solicitudes de un DEV por estado
     */
    List<SolicitudAccesoAPI> findBySolicitanteUsuarioIdAndEstadoSolicitudOrderByFechaSolicitudDesc(
        Long usuarioId, 
        EstadoSolicitud estado
    );
    
    /**
     * Verifica si ya existe una solicitud PENDIENTE o APROBADA para un usuario en una versión
     * (evita duplicados)
     */
    @Query("SELECT s FROM SolicitudAccesoAPI s WHERE s.solicitante.usuarioId = :usuarioId " +
           "AND s.versionApi.versionId = :versionId " +
           "AND s.estadoSolicitud IN ('PENDIENTE', 'APROBADO')")
    Optional<SolicitudAccesoAPI> findActiveSolicitudByUsuarioAndVersion(
        @Param("usuarioId") Long usuarioId,
        @Param("versionId") Long versionId
    );
    
    // ========== QUERIES POR VERSIÓN ==========
    
    /**
     * Busca todas las solicitudes para una versión específica
     */
    List<SolicitudAccesoAPI> findByVersionApiVersionIdOrderByFechaSolicitudDesc(Long versionId);
    
    /**
     * Busca solicitudes pendientes de una versión específica
     */
    List<SolicitudAccesoAPI> findByVersionApiVersionIdAndEstadoSolicitudOrderByFechaSolicitudDesc(
        Long versionId, 
        EstadoSolicitud estado
    );
    
    // ========== QUERIES PARA PROVIDER (APROBADOR) ==========
    
    /**
     * Busca solicitudes pendientes de versiones creadas por un PROVIDER
     * CRÍTICO: Solo muestra versiones cuyo creado_por = usuarioId del PROVIDER
     */
    @Query("SELECT s FROM SolicitudAccesoAPI s " +
           "WHERE s.versionApi.creadoPor.usuarioId = :providerId " +
           "AND s.estadoSolicitud = 'PENDIENTE' " +
           "ORDER BY s.fechaSolicitud DESC")
    List<SolicitudAccesoAPI> findPendientesByProviderVersionCreator(@Param("providerId") Long providerId);
    
    /**
     * Busca todas las solicitudes (cualquier estado) de versiones creadas por un PROVIDER
     */
    @Query("SELECT s FROM SolicitudAccesoAPI s " +
           "WHERE s.versionApi.creadoPor.usuarioId = :providerId " +
           "ORDER BY s.fechaSolicitud DESC")
    List<SolicitudAccesoAPI> findAllByProviderVersionCreator(@Param("providerId") Long providerId);
    
    /**
     * Cuenta solicitudes pendientes para un PROVIDER
     */
    @Query("SELECT COUNT(s) FROM SolicitudAccesoAPI s " +
           "WHERE s.versionApi.creadoPor.usuarioId = :providerId " +
           "AND s.estadoSolicitud = 'PENDIENTE'")
    Long countPendientesByProviderVersionCreator(@Param("providerId") Long providerId);
    
    // ========== QUERIES POR API (FALLBACK) ==========
    
    /**
     * Busca solicitudes pendientes de un API completa (todas sus versiones)
     * FALLBACK: Si la versión no tiene creado_por, usa el creado_por del API
     */
    @Query("SELECT s FROM SolicitudAccesoAPI s " +
           "WHERE (s.versionApi.creadoPor.usuarioId = :providerId " +
           "       OR (s.versionApi.creadoPor IS NULL AND s.api.creadoPor.usuarioId = :providerId)) " +
           "AND s.estadoSolicitud = 'PENDIENTE' " +
           "ORDER BY s.fechaSolicitud DESC")
    List<SolicitudAccesoAPI> findPendientesByProviderWithFallback(@Param("providerId") Long providerId);
    
    // ========== QUERIES DE ESTADÍSTICAS ==========
    
    /**
     * Cuenta solicitudes por estado para una API
     */
    @Query("SELECT COUNT(s) FROM SolicitudAccesoAPI s " +
           "WHERE s.api.apiId = :apiId AND s.estadoSolicitud = :estado")
    Long countByApiAndEstado(@Param("apiId") Long apiId, @Param("estado") EstadoSolicitud estado);
    
    /**
     * Cuenta solicitudes por estado para una versión
     */
    @Query("SELECT COUNT(s) FROM SolicitudAccesoAPI s " +
           "WHERE s.versionApi.versionId = :versionId AND s.estadoSolicitud = :estado")
    Long countByVersionAndEstado(@Param("versionId") Long versionId, @Param("estado") EstadoSolicitud estado);
}

