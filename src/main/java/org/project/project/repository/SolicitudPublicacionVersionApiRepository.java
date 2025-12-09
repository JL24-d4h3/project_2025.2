package org.project.project.repository;

import org.project.project.model.entity.SolicitudPublicacionVersionApi;
import org.project.project.model.entity.SolicitudPublicacionVersionApi.EstadoSolicitud;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar solicitudes de publicación de APIs.
 * 
 * Responsabilidades principales:
 * - Buscar solicitudes por estado (para panel QA)
 * - Verificar si existe solicitud activa para una API (bloquear edición)
 * - Obtener solicitudes pendientes de un usuario DEV
 * - Consultas para reportes y estadísticas
 */
@Repository
public interface SolicitudPublicacionVersionApiRepository 
        extends JpaRepository<SolicitudPublicacionVersionApi, Long> {

    // ========== CONSULTAS PARA PANEL QA ==========
    
    /**
     * Obtiene todas las solicitudes en un estado específico
     * Ordenadas por fecha de creación (más recientes primero)
     * 
     * Usado en: Panel QA principal (/qa/{username}/solicitudes)
     */
    List<SolicitudPublicacionVersionApi> findByEstadoOrderByCreadoEnDesc(EstadoSolicitud estado);
    
    /**
     * Obtiene solicitudes en múltiples estados
     * Útil para mostrar PENDIENTE + EN_REVISION juntas
     */
    List<SolicitudPublicacionVersionApi> findByEstadoInOrderByCreadoEnDesc(List<EstadoSolicitud> estados);

    /**
     * Obtiene solicitudes pendientes y en revisión (solicitudes activas para QA)
     * @return Lista de solicitudes que requieren atención de QA
     */
    @Query("SELECT s FROM SolicitudPublicacionVersionApi s " +
           "WHERE s.estado IN ('PENDIENTE', 'EN_REVISION') " +
           "ORDER BY s.creadoEn DESC")
    List<SolicitudPublicacionVersionApi> findSolicitudesActivasParaQA();

    // ========== CONSULTAS PARA VALIDACIÓN DE EDICIÓN ==========
    
    /**
     * Verifica si existe alguna solicitud activa (PENDIENTE o EN_REVISION) para una API
     * 
     * Usado en: APIService.puedeEditarAPI() antes de permitir editar
     * 
     * @param apiId ID de la API a verificar
     * @return Optional con la solicitud activa, o empty si no hay ninguna
     */
    @Query("SELECT s FROM SolicitudPublicacionVersionApi s " +
           "WHERE s.api.apiId = :apiId " +
           "AND s.estado IN ('PENDIENTE', 'EN_REVISION')")
    Optional<SolicitudPublicacionVersionApi> findSolicitudActivaByApiId(@Param("apiId") Long apiId);
    
    /**
     * Verifica si existe solicitud activa para una versión específica de API
     * (Más específico que findSolicitudActivaByApiId)
     */
    @Query("SELECT s FROM SolicitudPublicacionVersionApi s " +
           "WHERE s.versionApi.versionId = :versionId " +
           "AND s.estado IN ('PENDIENTE', 'EN_REVISION')")
    Optional<SolicitudPublicacionVersionApi> findSolicitudActivaByVersionId(@Param("versionId") Long versionId);
    
    /**
     * Verifica si existe alguna solicitud activa (devuelve true/false directamente)
     * Más eficiente que findSolicitudActivaByApiId cuando solo necesitas saber si existe
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM SolicitudPublicacionVersionApi s " +
           "WHERE s.api.apiId = :apiId " +
           "AND s.estado IN ('PENDIENTE', 'EN_REVISION')")
    boolean existeSolicitudActivaParaApi(@Param("apiId") Long apiId);

    // ========== CONSULTAS PARA DEV (Dashboard) ==========
    
    /**
     * Obtiene solicitudes creadas por un usuario específico
     * Ordenadas por fecha (más recientes primero)
     * 
     * Usado en: Dashboard del DEV para ver estado de sus solicitudes
     */
    List<SolicitudPublicacionVersionApi> findByGeneradoPorOrderByCreadoEnDesc(Usuario generadoPor);
    
    /**
     * Obtiene solicitudes de un usuario en un estado específico
     */
    List<SolicitudPublicacionVersionApi> findByGeneradoPorAndEstadoOrderByCreadoEnDesc(
        Usuario generadoPor, 
        EstadoSolicitud estado
    );
    
    /**
     * Obtiene solicitudes activas de un usuario (PENDIENTE + EN_REVISION)
     * Para mostrar "Tienes X solicitudes pendientes de revisión"
     */
    @Query("SELECT s FROM SolicitudPublicacionVersionApi s " +
           "WHERE s.generadoPor.usuarioId = :usuarioId " +
           "AND s.estado IN ('PENDIENTE', 'EN_REVISION') " +
           "ORDER BY s.creadoEn DESC")
    List<SolicitudPublicacionVersionApi> findSolicitudesActivasByUsuarioId(@Param("usuarioId") Long usuarioId);

    // ========== CONSULTAS PARA AUDITORÍA Y REPORTES ==========
    
    /**
     * Obtiene todas las solicitudes aprobadas por un QA específico
     * Para métricas de QA: "Has aprobado X solicitudes este mes"
     */
    List<SolicitudPublicacionVersionApi> findByAprobadoPorAndEstadoOrderByFechaResolucionDesc(
        Usuario aprobadoPor,
        EstadoSolicitud estado
    );
    
    /**
     * Cuenta solicitudes por estado para estadísticas
     */
    long countByEstado(EstadoSolicitud estado);
    
    /**
     * Cuenta solicitudes aprobadas en un rango de fechas
     * Para reportes: "Se aprobaron X APIs esta semana"
     */
    @Query("SELECT COUNT(s) FROM SolicitudPublicacionVersionApi s " +
           "WHERE s.estado = 'APROBADO' " +
           "AND s.fechaResolucion BETWEEN :fechaInicio AND :fechaFin")
    long countAprobadasEntreFechas(
        @Param("fechaInicio") LocalDateTime fechaInicio, 
        @Param("fechaFin") LocalDateTime fechaFin
    );
    
    /**
     * Obtiene tiempo promedio de resolución de solicitudes
     * Para métricas: "Tiempo promedio de revisión: 2.5 días"
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, s.creadoEn, s.fechaResolucion)) " +
           "FROM SolicitudPublicacionVersionApi s " +
           "WHERE s.estado IN ('APROBADO', 'RECHAZADO') " +
           "AND s.fechaResolucion IS NOT NULL")
    Double findTiempoPromedioResolucionEnHoras();

    // ========== CONSULTAS PARA HISTORIAL ==========
    
    /**
     * Obtiene historial completo de solicitudes de una API específica
     * Ordenado por fecha de creación (más reciente primero)
     * 
     * Usado en: Vista de detalles de API para mostrar historial de publicaciones
     */
    List<SolicitudPublicacionVersionApi> findByApiApiIdOrderByCreadoEnDesc(Long apiId);
    
    /**
     * Obtiene historial de solicitudes de una versión específica
     * Puede haber múltiples solicitudes para la misma versión (rechazadas y vueltas a enviar)
     */
    List<SolicitudPublicacionVersionApi> findByVersionApiVersionIdOrderByCreadoEnDesc(Long versionId);
    
    /**
     * Obtiene la última solicitud (más reciente) de una API, sin importar estado
     * Para mostrar estado actual: "Última solicitud: RECHAZADA hace 2 días"
     */
    Optional<SolicitudPublicacionVersionApi> findFirstByApiApiIdOrderByCreadoEnDesc(Long apiId);

    // ========== CONSULTAS AVANZADAS ==========
    
    /**
     * Busca solicitudes por nombre de API (búsqueda para QA)
     * Para panel QA con búsqueda: "Buscar API por nombre"
     */
    @Query("SELECT s FROM SolicitudPublicacionVersionApi s " +
           "WHERE LOWER(s.api.nombreApi) LIKE LOWER(CONCAT('%', :nombreApi, '%')) " +
           "AND s.estado IN ('PENDIENTE', 'EN_REVISION') " +
           "ORDER BY s.creadoEn DESC")
    List<SolicitudPublicacionVersionApi> findSolicitudesActivasByNombreApi(@Param("nombreApi") String nombreApi);
    
    /**
     * Obtiene solicitudes rechazadas de un usuario (para que corrija)
     * Muestra: "Tienes X APIs rechazadas que necesitan corrección"
     */
    @Query("SELECT s FROM SolicitudPublicacionVersionApi s " +
           "WHERE s.generadoPor.usuarioId = :usuarioId " +
           "AND s.estado = 'RECHAZADO' " +
           "ORDER BY s.fechaResolucion DESC")
    List<SolicitudPublicacionVersionApi> findSolicitudesRechazadasByUsuarioId(@Param("usuarioId") Long usuarioId);
    
    /**
     * Verifica si un usuario tiene solicitudes pendientes (para validaciones)
     * Útil para: "No puedes crear otra solicitud hasta resolver la anterior"
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM SolicitudPublicacionVersionApi s " +
           "WHERE s.generadoPor.usuarioId = :usuarioId " +
           "AND s.estado IN ('PENDIENTE', 'EN_REVISION')")
    boolean usuarioTieneSolicitudesPendientes(@Param("usuarioId") Long usuarioId);

    // ========== CONSULTAS PARA NOTIFICACIONES ==========
    
    /**
     * Obtiene solicitudes creadas en las últimas X horas
     * Para notificaciones: "Nuevas solicitudes en las últimas 24 horas"
     */
    @Query("SELECT s FROM SolicitudPublicacionVersionApi s " +
           "WHERE s.estado = 'PENDIENTE' " +
           "AND s.creadoEn >= :fechaDesde " +
           "ORDER BY s.creadoEn DESC")
    List<SolicitudPublicacionVersionApi> findSolicitudesPendientesDesde(@Param("fechaDesde") LocalDateTime fechaDesde);
}
