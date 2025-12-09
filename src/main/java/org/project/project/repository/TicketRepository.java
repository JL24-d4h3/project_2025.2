package org.project.project.repository;

import org.project.project.model.entity.Ticket;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.Proyecto;
import org.project.project.model.dto.TicketSummaryView;
import org.project.project.model.dto.FollowUpTicketView;
import org.project.project.model.dto.AvailableTicketView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // =================== BÚSQUEDAS BÁSICAS ===================

    // Find by ID
    Optional<Ticket> findByTicketId(Long ticketId);

    // Find by reported user
    List<Ticket> findByReportadoPorOrderByFechaCreacionDesc(Usuario usuario);

    Page<Ticket> findByReportadoPorOrderByFechaCreacionDesc(Usuario usuario, Pageable pageable);

    List<Ticket> findByReportadoPor_UsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    Page<Ticket> findByReportadoPor_UsuarioIdOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);

    // Find by assigned user
    List<Ticket> findByAsignadoAOrderByFechaCreacionDesc(Usuario usuario);

    Page<Ticket> findByAsignadoAOrderByFechaCreacionDesc(Usuario usuario, Pageable pageable);

    List<Ticket> findByAsignadoA_UsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    Page<Ticket> findByAsignadoA_UsuarioIdOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);

    // Find by project
    List<Ticket> findByProyectoOrderByFechaCreacionDesc(Proyecto proyecto);

    Page<Ticket> findByProyectoOrderByFechaCreacionDesc(Proyecto proyecto, Pageable pageable);

    List<Ticket> findByProyecto_ProyectoIdOrderByFechaCreacionDesc(Long proyectoId);

    Page<Ticket> findByProyecto_ProyectoIdOrderByFechaCreacionDesc(Long proyectoId, Pageable pageable);

    // Find public tickets (no project)
    List<Ticket> findByProyectoIsNullOrderByFechaCreacionDesc();

    Page<Ticket> findByProyectoIsNullOrderByFechaCreacionDesc(Pageable pageable);

    // =================== BÚSQUEDAS POR ESTADO Y ETAPA ===================

    // Find by status
    List<Ticket> findByEstadoTicketOrderByFechaCreacionDesc(Ticket.EstadoTicket estado);

    Page<Ticket> findByEstadoTicketOrderByFechaCreacionDesc(Ticket.EstadoTicket estado, Pageable pageable);

    // Find by stage
    List<Ticket> findByEtapaTicketOrderByFechaCreacionDesc(Ticket.EtapaTicket etapa);

    Page<Ticket> findByEtapaTicketOrderByFechaCreacionDesc(Ticket.EtapaTicket etapa, Pageable pageable);

    // Find by type
    List<Ticket> findByTipoTicketOrderByFechaCreacionDesc(Ticket.TipoTicket tipo);

    Page<Ticket> findByTipoTicketOrderByFechaCreacionDesc(Ticket.TipoTicket tipo, Pageable pageable);

    // Find by priority
    List<Ticket> findByPrioridadTicketOrderByFechaCreacionDesc(Ticket.PrioridadTicket prioridad);

    Page<Ticket> findByPrioridadTicketOrderByFechaCreacionDesc(Ticket.PrioridadTicket prioridad, Pageable pageable);

    // =================== COMBINACIONES COMPLEJAS ===================

    // Find by user and status
    List<Ticket> findByReportadoPorAndEstadoTicket(Usuario usuario, Ticket.EstadoTicket estado);

    // Find by user and stage
    List<Ticket> findByReportadoPorAndEtapaTicket(Usuario usuario, Ticket.EtapaTicket etapa);

    // Find by assigned user and status
    List<Ticket> findByAsignadoAAndEstadoTicket(Usuario usuario, Ticket.EstadoTicket estado);

    // Find by assigned user and stage
    List<Ticket> findByAsignadoAAndEtapaTicket(Usuario usuario, Ticket.EtapaTicket etapa);

    // Find by project and status
    List<Ticket> findByProyectoAndEstadoTicket(Proyecto proyecto, Ticket.EstadoTicket estado);

    // Find by project and stage
    List<Ticket> findByProyectoAndEtapaTicket(Proyecto proyecto, Ticket.EtapaTicket etapa);

    // =================== BÚSQUEDAS POR FECHA ===================

    // Find by date range
    List<Ticket> findByFechaCreacionBetweenOrderByFechaCreacionDesc(LocalDateTime start, LocalDateTime end);

    // Find created after date
    List<Ticket> findByFechaCreacionAfterOrderByFechaCreacionDesc(LocalDateTime date);

    // Find closed tickets
    List<Ticket> findByFechaCierreIsNotNullOrderByFechaCierreDesc();

    // Find open tickets
    List<Ticket> findByFechaCierreIsNullOrderByFechaCreacionDesc();

    // =================== ESTADÍSTICAS ===================

    // Count tickets by user
    long countByReportadoPor(Usuario usuario);

    long countByReportadoPor_UsuarioId(Long usuarioId);

    // Count by assigned user
    long countByAsignadoA(Usuario usuario);

    long countByAsignadoA_UsuarioId(Long usuarioId);

    // Count by project
    long countByProyecto(Proyecto proyecto);

    long countByProyecto_ProyectoId(Long proyectoId);

    // Count public tickets
    long countByProyectoIsNull();

    // Count by status
    long countByEstadoTicket(Ticket.EstadoTicket estado);

    // Count by stage
    long countByEtapaTicket(Ticket.EtapaTicket etapa);

    // Count by priority
    long countByPrioridadTicket(Ticket.PrioridadTicket prioridad);

    // Count unassigned tickets
    long countByAsignadoAIsNull();

    // Count open tickets
    long countByFechaCierreIsNull();

    // =================== QUERIES PERSONALIZADAS ===================

    @Query("SELECT t FROM Ticket t WHERE t.reportadoPor.usuarioId = :userId OR t.asignadoA.usuarioId = :userId ORDER BY t.fechaCreacion DESC")
    List<Ticket> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Ticket t WHERE (t.reportadoPor.usuarioId = :userId OR t.asignadoA.usuarioId = :userId) ORDER BY t.fechaCreacion DESC")
    Page<Ticket> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.asuntoTicket LIKE %:keyword% OR t.cuerpoTicket LIKE %:keyword%")
    List<Ticket> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT t FROM Ticket t WHERE t.asuntoTicket LIKE %:keyword% OR t.cuerpoTicket LIKE %:keyword%")
    Page<Ticket> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.proyecto.proyectoId = :projectId AND t.estadoTicket = :status")
    List<Ticket> findByProjectAndStatus(@Param("projectId") Long projectId, @Param("status") Ticket.EstadoTicket status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.reportadoPor.usuarioId = :userId AND t.fechaCreacion >= :since")
    long countRecentTicketsByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT t FROM Ticket t WHERE t.prioridadTicket = :priority AND t.fechaCierre IS NULL ORDER BY t.fechaCreacion ASC")
    List<Ticket> findOpenTicketsByPriority(@Param("priority") Ticket.PrioridadTicket priority);

    @Modifying
    @Transactional
    @Query("DELETE FROM Ticket t WHERE t.reportadoPor.usuarioId = :usuarioId")
    void deleteByReportadoPorUsuarioId(@Param("usuarioId") Long usuarioId);
    @Modifying
    @Transactional
    @Query("DELETE FROM Ticket t WHERE t.asignadoA.usuarioId = :usuarioId")
    void deleteByAsignadoAUsuarioId(@Param("usuarioId") Long usuarioId);

    // =================== PROJECTION QUERIES ===================

    @Query("SELECT t.ticketId as ticketId, t.asuntoTicket as asuntoTicket, " +
           "t.fechaCreacion as fechaCreacion, t.fechaCierre as fechaCierre, " +
           "t.etapaTicket as etapaTicket " +
           "FROM Ticket t WHERE t.reportadoPor.usuarioId = :usuarioId " +
           "ORDER BY t.fechaCreacion DESC")
    List<TicketSummaryView> findTicketSummaryByReportadoPor_UsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT t.ticketId as ticketId, t.asuntoTicket as asuntoTicket, " +
           "t.reportadoPor.username as creadorUsuario, t.fechaCreacion as fechaCreacion, " +
           "t.etapaTicket as etapaTicket " +
           "FROM Ticket t WHERE t.asignadoA.usuarioId = :usuarioId " +
           "ORDER BY t.fechaCreacion DESC")
    List<FollowUpTicketView> findFollowUpByReportadoPor_UsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT t.ticketId as ticketId, t.asuntoTicket as asuntoTicket, " +
           "t.reportadoPor.username as creadorUsuario " +
           "FROM Ticket t WHERE t.asignadoA IS NULL " +
           "ORDER BY t.fechaCreacion DESC")
    List<AvailableTicketView> findAvailableByAsignadoAIsNull();
}
