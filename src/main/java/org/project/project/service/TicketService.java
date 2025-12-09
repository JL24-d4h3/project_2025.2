package org.project.project.service;

import lombok.RequiredArgsConstructor;
import org.project.project.model.entity.Ticket;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.Proyecto;
import org.project.project.model.dto.TicketSummaryView;
import org.project.project.model.dto.FollowUpTicketView;
import org.project.project.model.dto.AvailableTicketView;
import org.project.project.repository.TicketRepository;
import org.project.project.repository.UsuarioRepository;
import org.project.project.repository.ProyectoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProyectoRepository proyectoRepository;

    // =================== TICKET CREATION ===================

    @Transactional
    public Ticket createTicket(Long reportedByUserId, String subject, String body, Ticket.TipoTicket type) {
        Usuario reportedBy = usuarioRepository.findById(reportedByUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + reportedByUserId));

        Ticket ticket = new Ticket();
        ticket.setReportadoPor(reportedBy);
        ticket.setAsuntoTicket(subject);
        ticket.setCuerpoTicket(body);
        ticket.setTipoTicket(type);
        ticket.setEstadoTicket(Ticket.EstadoTicket.ENVIADO);
        ticket.setEtapaTicket(Ticket.EtapaTicket.PENDIENTE);
        ticket.setPrioridadTicket(Ticket.PrioridadTicket.MEDIA);
        ticket.setFechaCreacion(LocalDateTime.now());

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket createTicketWithProject(Long reportedByUserId, String subject, String body,
                                          Ticket.TipoTicket type, Long projectId) {
        Ticket ticket = createTicket(reportedByUserId, subject, body, type);

        Proyecto proyecto = proyectoRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        ticket.setProyecto(proyecto);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket createPublicTicket(Long reportedByUserId, String subject, String body, Ticket.TipoTicket type) {
        return createTicket(reportedByUserId, subject, body, type);
    }

    // =================== TICKET UPDATES ===================

    @Transactional
    public Ticket updateTicketSubject(Long ticketId, String newSubject) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setAsuntoTicket(newSubject);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket updateTicketBody(Long ticketId, String newBody) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setCuerpoTicket(newBody);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket assignTicket(Long ticketId, Long assignedToUserId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        Usuario assignedTo = usuarioRepository.findById(assignedToUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + assignedToUserId));

        ticket.setAsignadoA(assignedTo);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket unassignTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setAsignadoA(null);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket updateTicketStatus(Long ticketId, Ticket.EstadoTicket newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setEstadoTicket(newStatus);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket updateTicketStage(Long ticketId, Ticket.EtapaTicket newStage) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setEtapaTicket(newStage);

        // Auto-close ticket if stage is CERRADO
        if (newStage == Ticket.EtapaTicket.CERRADO && ticket.getFechaCierre() == null) {
            ticket.setFechaCierre(LocalDateTime.now());
        }

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket updateTicketPriority(Long ticketId, Ticket.PrioridadTicket newPriority) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setPrioridadTicket(newPriority);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket closeTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setEtapaTicket(Ticket.EtapaTicket.CERRADO);
        ticket.setFechaCierre(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket resolveTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setEtapaTicket(Ticket.EtapaTicket.RESUELTO);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket rejectTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setEtapaTicket(Ticket.EtapaTicket.RECHAZADO);
        ticket.setFechaCierre(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    // =================== QUERY METHODS ===================

    public Optional<Ticket> findById(Long ticketId) {
        return ticketRepository.findById(ticketId);
    }

    public List<Ticket> findAllByUserId(Long userId) {
        return ticketRepository.findAllByUserId(userId);
    }

    public Page<Ticket> findAllByUserId(Long userId, Pageable pageable) {
        return ticketRepository.findAllByUserId(userId, pageable);
    }

    public List<Ticket> findByReportedByUserId(Long userId) {
        return ticketRepository.findByReportadoPor_UsuarioIdOrderByFechaCreacionDesc(userId);
    }

    public Page<Ticket> findByReportedByUserId(Long userId, Pageable pageable) {
        return ticketRepository.findByReportadoPor_UsuarioIdOrderByFechaCreacionDesc(userId, pageable);
    }

    public List<Ticket> findByAssignedToUserId(Long userId) {
        return ticketRepository.findByAsignadoA_UsuarioIdOrderByFechaCreacionDesc(userId);
    }

    public Page<Ticket> findByAssignedToUserId(Long userId, Pageable pageable) {
        return ticketRepository.findByAsignadoA_UsuarioIdOrderByFechaCreacionDesc(userId, pageable);
    }

    public List<Ticket> findByProjectId(Long projectId) {
        return ticketRepository.findByProyecto_ProyectoIdOrderByFechaCreacionDesc(projectId);
    }

    public Page<Ticket> findByProjectId(Long projectId, Pageable pageable) {
        return ticketRepository.findByProyecto_ProyectoIdOrderByFechaCreacionDesc(projectId, pageable);
    }

    public List<Ticket> findPublicTickets() {
        return ticketRepository.findByProyectoIsNullOrderByFechaCreacionDesc();
    }

    public Page<Ticket> findPublicTickets(Pageable pageable) {
        return ticketRepository.findByProyectoIsNullOrderByFechaCreacionDesc(pageable);
    }

    public List<Ticket> findByStatus(Ticket.EstadoTicket status) {
        return ticketRepository.findByEstadoTicketOrderByFechaCreacionDesc(status);
    }

    public Page<Ticket> findByStatus(Ticket.EstadoTicket status, Pageable pageable) {
        return ticketRepository.findByEstadoTicketOrderByFechaCreacionDesc(status, pageable);
    }

    public List<Ticket> findByStage(Ticket.EtapaTicket stage) {
        return ticketRepository.findByEtapaTicketOrderByFechaCreacionDesc(stage);
    }

    public Page<Ticket> findByStage(Ticket.EtapaTicket stage, Pageable pageable) {
        return ticketRepository.findByEtapaTicketOrderByFechaCreacionDesc(stage, pageable);
    }

    public List<Ticket> findByType(Ticket.TipoTicket type) {
        return ticketRepository.findByTipoTicketOrderByFechaCreacionDesc(type);
    }

    public Page<Ticket> findByType(Ticket.TipoTicket type, Pageable pageable) {
        return ticketRepository.findByTipoTicketOrderByFechaCreacionDesc(type, pageable);
    }

    public List<Ticket> findByPriority(Ticket.PrioridadTicket priority) {
        return ticketRepository.findByPrioridadTicketOrderByFechaCreacionDesc(priority);
    }

    public Page<Ticket> findByPriority(Ticket.PrioridadTicket priority, Pageable pageable) {
        return ticketRepository.findByPrioridadTicketOrderByFechaCreacionDesc(priority, pageable);
    }

    public List<Ticket> findOpenTickets() {
        return ticketRepository.findByFechaCierreIsNullOrderByFechaCreacionDesc();
    }

    public List<Ticket> findClosedTickets() {
        return ticketRepository.findByFechaCierreIsNotNullOrderByFechaCierreDesc();
    }

    public List<Ticket> searchByKeyword(String keyword) {
        return ticketRepository.searchByKeyword(keyword);
    }

    public Page<Ticket> searchByKeyword(String keyword, Pageable pageable) {
        return ticketRepository.searchByKeyword(keyword, pageable);
    }

    public List<Ticket> findOpenTicketsByPriority(Ticket.PrioridadTicket priority) {
        return ticketRepository.findOpenTicketsByPriority(priority);
    }

    // =================== STATISTICS METHODS ===================

    public long countByReportedByUserId(Long userId) {
        return ticketRepository.countByReportadoPor_UsuarioId(userId);
    }

    public long countByAssignedToUserId(Long userId) {
        return ticketRepository.countByAsignadoA_UsuarioId(userId);
    }

    public long countByProjectId(Long projectId) {
        return ticketRepository.countByProyecto_ProyectoId(projectId);
    }

    public long countPublicTickets() {
        return ticketRepository.countByProyectoIsNull();
    }

    public long countByStatus(Ticket.EstadoTicket status) {
        return ticketRepository.countByEstadoTicket(status);
    }

    public long countByStage(Ticket.EtapaTicket stage) {
        return ticketRepository.countByEtapaTicket(stage);
    }

    public long countByPriority(Ticket.PrioridadTicket priority) {
        return ticketRepository.countByPrioridadTicket(priority);
    }

    public long countUnassignedTickets() {
        return ticketRepository.countByAsignadoAIsNull();
    }

    public long countOpenTickets() {
        return ticketRepository.countByFechaCierreIsNull();
    }

    public long countRecentTicketsByUser(Long userId, LocalDateTime since) {
        return ticketRepository.countRecentTicketsByUser(userId, since);
    }

    // =================== UTILITY METHODS ===================

    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public Page<Ticket> findAll(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    @Transactional
    public void deleteTicket(Long ticketId) {
        ticketRepository.deleteById(ticketId);
    }

    @Transactional
    public Ticket linkToProject(Long ticketId, Long projectId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        Proyecto proyecto = proyectoRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        ticket.setProyecto(proyecto);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket unlinkFromProject(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setProyecto(null);
        return ticketRepository.save(ticket);
    }

    // =================== MISSING METHODS FROM CONTROLLER ===================

    public List<TicketSummaryView> listarResumenPorUsuario(Long uid) {
        return ticketRepository.findTicketSummaryByReportadoPor_UsuarioId(uid);
    }

    public List<FollowUpTicketView> listarSeguimiento(Long uid) {
        return ticketRepository.findFollowUpByReportadoPor_UsuarioId(uid);
    }

    public long contarTodosLosTickets() {
        return ticketRepository.count();
    }

    public List<AvailableTicketView> listarDisponibles(Long uid) {
        return ticketRepository.findAvailableByAsignadoAIsNull();
    }

    @Transactional
    public void tomarTicket(Long userId, Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Assign ticket, change stage to EN_PROGRESO and status to RECIBIDO
        ticket.setAsignadoA(usuario);
        ticket.setEtapaTicket(Ticket.EtapaTicket.EN_PROGRESO);
        ticket.setEstadoTicket(Ticket.EstadoTicket.RECIBIDO);
        ticketRepository.save(ticket);
    }

    public Ticket obtenerTicketAsignadoDeUsuario(Long userId, Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        
        // Verify ticket is assigned to this user
        if (ticket.getAsignadoA() == null || !ticket.getAsignadoA().getUsuarioId().equals(userId)) {
            throw new RuntimeException("Ticket is not assigned to user with id: " + userId);
        }
        
        return ticket;
    }

    public Ticket obtenerTicketDeUsuario(Long userId, Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        
        // Verify ticket was created by this user
        if (!ticket.getReportadoPor().getUsuarioId().equals(userId)) {
            throw new RuntimeException("Ticket does not belong to user with id: " + userId);
        }
        
        return ticket;
    }

    @Transactional
    public void crearTicketParaProyecto(Long uid, Long proyectoId, String asunto, String cuerpo, String tipo) {
        Usuario usuario = usuarioRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + uid));
        
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + proyectoId));
        
        Ticket ticket = new Ticket();
        ticket.setAsuntoTicket(asunto);
        ticket.setCuerpoTicket(cuerpo);
        ticket.setReportadoPor(usuario);
        ticket.setProyecto(proyecto);
        ticket.setTipoTicket(Ticket.TipoTicket.valueOf(tipo.toUpperCase()));
        ticket.setEstadoTicket(Ticket.EstadoTicket.ENVIADO);
        ticket.setEtapaTicket(Ticket.EtapaTicket.PENDIENTE);
        ticket.setPrioridadTicket(Ticket.PrioridadTicket.MEDIA);
        ticket.setFechaCreacion(LocalDateTime.now());
        
        ticketRepository.save(ticket);
    }

    @Transactional
    public void crearTicketParaUsuario(Long uid, String asunto, String cuerpo, String tipo) {
        Usuario usuario = usuarioRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + uid));
        
        Ticket ticket = new Ticket();
        ticket.setAsuntoTicket(asunto);
        ticket.setCuerpoTicket(cuerpo);
        ticket.setReportadoPor(usuario);
        ticket.setProyecto(null); // Public ticket
        ticket.setTipoTicket(Ticket.TipoTicket.valueOf(tipo.toUpperCase()));
        ticket.setEstadoTicket(Ticket.EstadoTicket.ENVIADO);
        ticket.setEtapaTicket(Ticket.EtapaTicket.PENDIENTE);
        ticket.setPrioridadTicket(Ticket.PrioridadTicket.MEDIA);
        ticket.setFechaCreacion(LocalDateTime.now());
        
        ticketRepository.save(ticket);
    }

    @Transactional
    public void guardarBorradorRespuesta(Long userId, Long ticketId, String prioridad) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        
        // Verify ticket is assigned to this user
        if (ticket.getAsignadoA() == null || !ticket.getAsignadoA().getUsuarioId().equals(userId)) {
            throw new RuntimeException("Ticket is not assigned to user with id: " + userId);
        }
        
        // Save draft - just update priority for now
        ticket.setPrioridadTicket(Ticket.PrioridadTicket.valueOf(prioridad.toUpperCase()));
        ticketRepository.save(ticket);
    }

    @Transactional
    public void responderTicket(Long userId, Long ticketId, String prioridad) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        
        // Verify ticket is assigned to this user
        if (ticket.getAsignadoA() == null || !ticket.getAsignadoA().getUsuarioId().equals(userId)) {
            throw new RuntimeException("Ticket is not assigned to user with id: " + userId);
        }
        
        // Mark as resolved
        ticket.setPrioridadTicket(Ticket.PrioridadTicket.valueOf(prioridad.toUpperCase()));
        ticket.setEtapaTicket(Ticket.EtapaTicket.RESUELTO);
        ticketRepository.save(ticket);
    }

    @Transactional
    public void rechazarTicket(Long userId, Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        
        // Verify ticket is assigned to this user
        if (ticket.getAsignadoA() == null || !ticket.getAsignadoA().getUsuarioId().equals(userId)) {
            throw new RuntimeException("Ticket is not assigned to user with id: " + userId);
        }
        
        // Reject ticket
        ticket.setEtapaTicket(Ticket.EtapaTicket.RECHAZADO);
        ticket.setAsignadoA(null); // Unassign
        ticketRepository.save(ticket);
    }

    @Transactional
    public void creadorAceptarRespuesta(Long userId, Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        
        // Verify user is the creator
        if (!ticket.getReportadoPor().getUsuarioId().equals(userId)) {
            throw new RuntimeException("User is not the creator of ticket with id: " + ticketId);
        }
        
        // Close ticket
        ticket.setEtapaTicket(Ticket.EtapaTicket.CERRADO);
        ticket.setFechaCierre(LocalDateTime.now());
        ticketRepository.save(ticket);
    }

    @Transactional
    public void creadorRechazarRespuesta(Long userId, Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        
        // Verify user is the creator
        if (!ticket.getReportadoPor().getUsuarioId().equals(userId)) {
            throw new RuntimeException("User is not the creator of ticket with id: " + ticketId);
        }
        
        // Send back to EN_PROGRESO
        ticket.setEtapaTicket(Ticket.EtapaTicket.EN_PROGRESO);
        ticketRepository.save(ticket);
    }

    @Transactional
    public void creadorRechazarYCambiarSeguidor(Long userId, Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        
        // Verify user is the creator
        if (!ticket.getReportadoPor().getUsuarioId().equals(userId)) {
            throw new RuntimeException("User is not the creator of ticket with id: " + ticketId);
        }
        
        // Reset to PENDIENTE and unassign
        ticket.setEtapaTicket(Ticket.EtapaTicket.PENDIENTE);
        ticket.setAsignadoA(null);
        ticketRepository.save(ticket);
    }
}
