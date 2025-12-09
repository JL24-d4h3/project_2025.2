package org.project.project.repository;

import org.project.project.model.entity.Reporte;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    // Buscar por autor
    List<Reporte> findByAutor(Usuario autor);

    List<Reporte> findByAutorUsuarioId(Long autorId);

    // Buscar por título (búsqueda parcial)
    List<Reporte> findByTituloReporteContainingIgnoreCase(String titulo);

    // Buscar por tipo de reporte
    List<Reporte> findByTipoReporte(Reporte.TipoReporte tipo);

    // Buscar por estado
    List<Reporte> findByEstadoReporte(Reporte.EstadoReporte estado);

    // Buscar reportes publicados
    @Query("SELECT r FROM Reporte r WHERE r.estadoReporte = 'PUBLICADO'")
    List<Reporte> findPublishedReports();

    // Buscar reportes en borrador
    @Query("SELECT r FROM Reporte r WHERE r.estadoReporte = 'BORRADOR'")
    List<Reporte> findDraftReports();

    // Buscar por rango de fechas de creación
    List<Reporte> findByCreadoEnBetween(LocalDateTime inicio, LocalDateTime fin);

    // Buscar reportes por tipo y estado
    List<Reporte> findByTipoReporteAndEstadoReporte(Reporte.TipoReporte tipo, Reporte.EstadoReporte estado);

    // Buscar reportes por autor y estado
    List<Reporte> findByAutorAndEstadoReporte(Usuario autor, Reporte.EstadoReporte estado);

    List<Reporte> findByAutorUsuarioIdAndEstadoReporte(Long autorId, Reporte.EstadoReporte estado);

    // Buscar reportes más recientes
    List<Reporte> findTop10ByOrderByCreadoEnDesc();

    // Contar reportes por autor
    long countByAutor(Usuario autor);

    long countByAutorUsuarioId(Long autorId);

    // Contar reportes por estado
    long countByEstadoReporte(Reporte.EstadoReporte estado);

    // Contar reportes por tipo
    long countByTipoReporte(Reporte.TipoReporte tipo);

    // Buscar reportes actualizados recientemente
    @Query("SELECT r FROM Reporte r WHERE r.actualizadoEn >= :fecha ORDER BY r.actualizadoEn DESC")
    List<Reporte> findRecentlyUpdated(@Param("fecha") LocalDateTime fecha);

    // Buscar reportes donde un usuario es colaborador
    @Query("SELECT r FROM Reporte r JOIN r.colaboradores c WHERE c.usuario.usuarioId = :usuarioId")
    List<Reporte> findReportsByCollaboratorUserId(@Param("usuarioId") Long usuarioId);

    // Buscar reportes relacionados con APIs específicas
    @Query("SELECT DISTINCT r FROM Reporte r JOIN r.apisRelacionadas ra WHERE ra.api.apiId = :apiId")
    List<Reporte> findReportsByApiId(@Param("apiId") Long apiId);

    // Buscar reportes relacionados con proyectos específicos
    @Query("SELECT DISTINCT r FROM Reporte r JOIN r.proyectosRelacionados rp WHERE rp.proyecto.proyectoId = :proyectoId")
    List<Reporte> findReportsByProjectId(@Param("proyectoId") Long proyectoId);

    // Buscar reportes relacionados con repositorios específicos
    @Query("SELECT DISTINCT r FROM Reporte r JOIN r.repositoriosRelacionados rr WHERE rr.repositorio.repositorioId = :repositorioId")
    List<Reporte> findReportsByRepositoryId(@Param("repositorioId") Long repositorioId);

    // Buscar reportes relacionados con tickets específicos
    @Query("SELECT DISTINCT r FROM Reporte r JOIN r.ticketsRelacionados rt WHERE rt.ticket.ticketId = :ticketId")
    List<Reporte> findReportsByTicketId(@Param("ticketId") Long ticketId);

    // Buscar reportes con adjuntos
    @Query("SELECT DISTINCT r FROM Reporte r WHERE SIZE(r.adjuntos) > 0")
    List<Reporte> findReportsWithAttachments();

    // Buscar reportes sin publicar de un autor
    @Query("SELECT r FROM Reporte r WHERE r.autor.usuarioId = :autorId AND r.estadoReporte <> 'PUBLICADO'")
    List<Reporte> findUnpublishedReportsByAuthor(@Param("autorId") Long autorId);

    // Verificar si un usuario tiene acceso a un reporte (como autor o colaborador)
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reporte r " +
           "LEFT JOIN r.colaboradores c " +
           "WHERE r.reporteId = :reporteId AND " +
           "(r.autor.usuarioId = :usuarioId OR c.usuario.usuarioId = :usuarioId)")
    boolean hasUserAccessToReport(@Param("reporteId") Long reporteId, @Param("usuarioId") Long usuarioId);

    // Contar reportes por tipo y autor
    long countByTipoReporteAndAutorUsuarioId(Reporte.TipoReporte tipoReporte, Long autorId);

    // Buscar reportes por autor y tipo
    List<Reporte> findByAutorUsuarioIdAndTipoReporte(Long autorId, Reporte.TipoReporte tipoReporte);

    // Buscar reportes por autor, tipo y estado
    List<Reporte> findByAutorUsuarioIdAndTipoReporteAndEstadoReporte(
            Long autorId, 
            Reporte.TipoReporte tipoReporte, 
            Reporte.EstadoReporte estadoReporte);
    
    // ========== QUERIES PARA ENVIADOS/RECIBIDOS ==========
    
    /**
     * Obtener reportes ENVIADOS por un usuario (creados por él)
     * Ordenados por fecha de creación descendente
     * PAGINACIÓN: Usar con Pageable si es necesario
     */
    @Query("SELECT r FROM Reporte r " +
           "WHERE r.autor.usuarioId = :usuarioId " +
           "ORDER BY r.creadoEn DESC")
    List<Reporte> findSentReportsByUserId(@Param("usuarioId") Long usuarioId);
    
    /**
     * Obtener reportes RECIBIDOS por un usuario
     * Lógica simplificada: Reportes donde el usuario NO es el autor
     * pero está en la tabla usuario_has_reporte (recipients)
     */
    @Query("SELECT r FROM Reporte r " +
           "JOIN UsuarioHasReporte uhr ON uhr.reporte.reporteId = r.reporteId " +
           "WHERE uhr.usuario.usuarioId = :usuarioId " +
           "AND r.autor.usuarioId != :usuarioId " +
           "ORDER BY r.creadoEn DESC")
    List<Reporte> findReceivedReportsByUserId(@Param("usuarioId") Long usuarioId);
    
    /**
     * Buscar reportes ENVIADOS con filtros combinados
     * @param usuarioId ID del usuario autor
     * @param tipo Tipo de reporte (puede ser null)
     * @param estado Estado del reporte (puede ser null)
     * @param searchTerm Término de búsqueda en título/descripción (puede ser null, ya debe estar en minúsculas)
     * @param fechaDesde Fecha desde (puede ser null)
     * @param fechaHasta Fecha hasta (puede ser null)
     */
    @Query("SELECT r FROM Reporte r " +
           "WHERE r.autor.usuarioId = :usuarioId " +
           "AND (:tipo IS NULL OR r.tipoReporte = :tipo) " +
           "AND (:estado IS NULL OR r.estadoReporte = :estado) " +
           "AND (:searchTerm IS NULL OR " +
           "     r.tituloReporte LIKE :searchTerm OR " +
           "     r.descripcionReporte LIKE :searchTerm) " +
           "AND (:fechaDesde IS NULL OR r.creadoEn >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR r.creadoEn <= :fechaHasta) " +
           "ORDER BY r.creadoEn DESC")
    List<Reporte> searchSentReports(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") Reporte.TipoReporte tipo,
            @Param("estado") Reporte.EstadoReporte estado,
            @Param("searchTerm") String searchTerm,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta
    );
    
    /**
     * Buscar reportes RECIBIDOS con filtros combinados
     * EXCLUYE reportes en estado BORRADOR para que no aparezcan en bandeja de recibidos
     * @param searchTerm Término de búsqueda (debe venir con % y en minúsculas)
     */
    @Query("SELECT r FROM Reporte r " +
           "JOIN UsuarioHasReporte uhr ON uhr.reporte.reporteId = r.reporteId " +
           "WHERE uhr.usuario.usuarioId = :usuarioId " +
           "AND r.autor.usuarioId != :usuarioId " +
           "AND r.estadoReporte != 'BORRADOR' " +
           "AND (:tipo IS NULL OR r.tipoReporte = :tipo) " +
           "AND (:estado IS NULL OR r.estadoReporte = :estado) " +
           "AND (:searchTerm IS NULL OR " +
           "     r.tituloReporte LIKE :searchTerm OR " +
           "     r.descripcionReporte LIKE :searchTerm) " +
           "AND (:fechaDesde IS NULL OR r.creadoEn >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR r.creadoEn <= :fechaHasta) " +
           "ORDER BY r.creadoEn DESC")
    List<Reporte> searchReceivedReports(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") Reporte.TipoReporte tipo,
            @Param("estado") Reporte.EstadoReporte estado,
            @Param("searchTerm") String searchTerm,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta
    );
    
    /**
     * Contar reportes ENVIADOS
     */
    @Query("SELECT COUNT(r) FROM Reporte r WHERE r.autor.usuarioId = :usuarioId")
    long countSentReportsByUserId(@Param("usuarioId") Long usuarioId);
    
    /**
     * Contar reportes RECIBIDOS
     * EXCLUYE reportes en estado BORRADOR para que no aparezcan en bandeja de recibidos
     */
    @Query("SELECT COUNT(r) FROM Reporte r " +
           "JOIN UsuarioHasReporte uhr ON uhr.reporte.reporteId = r.reporteId " +
           "WHERE uhr.usuario.usuarioId = :usuarioId " +
           "AND r.autor.usuarioId != :usuarioId " +
           "AND r.estadoReporte != 'BORRADOR'")
    long countReceivedReportsByUserId(@Param("usuarioId") Long usuarioId);
    
    /**
     * Contar reportes por tipo (para métricas dinámicas)
     */
    @Query("SELECT COUNT(r) FROM Reporte r " +
           "WHERE r.autor.usuarioId = :usuarioId " +
           "AND r.tipoReporte = :tipo")
    long countByTipoAndAutor(@Param("usuarioId") Long usuarioId, @Param("tipo") Reporte.TipoReporte tipo);
}
