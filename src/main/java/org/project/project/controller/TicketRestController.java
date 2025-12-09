package org.project.project.controller;

import lombok.RequiredArgsConstructor;
import org.project.project.model.entity.Ticket;
import org.project.project.service.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketRestController {

    private final TicketService ticketService;

    // =================== TICKET CREATION ===================

    @PostMapping
    public ResponseEntity<Ticket> createTicket(
            @RequestParam Long reportedByUserId,
            @RequestParam String subject,
            @RequestParam String body,
            @RequestParam Ticket.TipoTicket type,
            @RequestParam(required = false) Long projectId) {

        Ticket ticket;
        if (projectId != null) {
            ticket = ticketService.createTicketWithProject(reportedByUserId, subject, body, type, projectId);
        } else {
            ticket = ticketService.createPublicTicket(reportedByUserId, subject, body, type);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @PostMapping("/public")
    public ResponseEntity<Ticket> createPublicTicket(
            @RequestParam Long reportedByUserId,
            @RequestParam String subject,
            @RequestParam String body,
            @RequestParam Ticket.TipoTicket type) {

        Ticket ticket = ticketService.createPublicTicket(reportedByUserId, subject, body, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    // =================== TICKET QUERIES ===================

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        return ticketService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Ticket>> getTicketsByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        if (page > 0 || size != 20) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> tickets = ticketService.findAllByUserId(userId, pageable);
            return ResponseEntity.ok(tickets.getContent());
        }

        List<Ticket> tickets = ticketService.findAllByUserId(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/reported-by/{userId}")
    public ResponseEntity<List<Ticket>> getTicketsReportedByUser(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        if (page > 0 || size != 20) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> tickets = ticketService.findByReportedByUserId(userId, pageable);
            return ResponseEntity.ok(tickets.getContent());
        }

        List<Ticket> tickets = ticketService.findByReportedByUserId(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/assigned-to/{userId}")
    public ResponseEntity<List<Ticket>> getTicketsAssignedToUser(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        if (page > 0 || size != 20) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> tickets = ticketService.findByAssignedToUserId(userId, pageable);
            return ResponseEntity.ok(tickets.getContent());
        }

        List<Ticket> tickets = ticketService.findByAssignedToUserId(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Ticket>> getTicketsByProjectId(
            @PathVariable Long projectId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        if (page > 0 || size != 20) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> tickets = ticketService.findByProjectId(projectId, pageable);
            return ResponseEntity.ok(tickets.getContent());
        }

        List<Ticket> tickets = ticketService.findByProjectId(projectId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Ticket>> getPublicTickets(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        if (page > 0 || size != 20) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> tickets = ticketService.findPublicTickets(pageable);
            return ResponseEntity.ok(tickets.getContent());
        }

        List<Ticket> tickets = ticketService.findPublicTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Ticket>> getTicketsByStatus(
            @PathVariable Ticket.EstadoTicket status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        if (page > 0 || size != 20) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> tickets = ticketService.findByStatus(status, pageable);
            return ResponseEntity.ok(tickets.getContent());
        }

        List<Ticket> tickets = ticketService.findByStatus(status);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/stage/{stage}")
    public ResponseEntity<List<Ticket>> getTicketsByStage(
            @PathVariable Ticket.EtapaTicket stage,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        if (page > 0 || size != 20) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> tickets = ticketService.findByStage(stage, pageable);
            return ResponseEntity.ok(tickets.getContent());
        }

        List<Ticket> tickets = ticketService.findByStage(stage);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Ticket>> getTicketsByPriority(
            @PathVariable Ticket.PrioridadTicket priority,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        if (page > 0 || size != 20) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> tickets = ticketService.findByPriority(priority, pageable);
            return ResponseEntity.ok(tickets.getContent());
        }

        List<Ticket> tickets = ticketService.findByPriority(priority);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Ticket>> getTicketsByType(
            @PathVariable Ticket.TipoTicket type,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        if (page > 0 || size != 20) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> tickets = ticketService.findByType(type, pageable);
            return ResponseEntity.ok(tickets.getContent());
        }

        List<Ticket> tickets = ticketService.findByType(type);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/open")
    public ResponseEntity<List<Ticket>> getOpenTickets() {
        List<Ticket> tickets = ticketService.findOpenTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/closed")
    public ResponseEntity<List<Ticket>> getClosedTickets() {
        List<Ticket> tickets = ticketService.findClosedTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Ticket>> searchTickets(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        if (page > 0 || size != 20) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> tickets = ticketService.searchByKeyword(keyword, pageable);
            return ResponseEntity.ok(tickets.getContent());
        }

        List<Ticket> tickets = ticketService.searchByKeyword(keyword);
        return ResponseEntity.ok(tickets);
    }

    // =================== TICKET UPDATES ===================

    @PutMapping("/{id}/subject")
    public ResponseEntity<Ticket> updateSubject(
            @PathVariable Long id,
            @RequestParam String subject) {
        try {
            Ticket updated = ticketService.updateTicketSubject(id, subject);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/body")
    public ResponseEntity<Ticket> updateBody(
            @PathVariable Long id,
            @RequestParam String body) {
        try {
            Ticket updated = ticketService.updateTicketBody(id, body);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/assign/{userId}")
    public ResponseEntity<Ticket> assignTicket(
            @PathVariable Long id,
            @PathVariable Long userId) {
        try {
            Ticket assigned = ticketService.assignTicket(id, userId);
            return ResponseEntity.ok(assigned);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/unassign")
    public ResponseEntity<Ticket> unassignTicket(@PathVariable Long id) {
        try {
            Ticket unassigned = ticketService.unassignTicket(id);
            return ResponseEntity.ok(unassigned);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Ticket> updateStatus(
            @PathVariable Long id,
            @RequestParam Ticket.EstadoTicket status) {
        try {
            Ticket updated = ticketService.updateTicketStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/stage")
    public ResponseEntity<Ticket> updateStage(
            @PathVariable Long id,
            @RequestParam Ticket.EtapaTicket stage) {
        try {
            Ticket updated = ticketService.updateTicketStage(id, stage);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/priority")
    public ResponseEntity<Ticket> updatePriority(
            @PathVariable Long id,
            @RequestParam Ticket.PrioridadTicket priority) {
        try {
            Ticket updated = ticketService.updateTicketPriority(id, priority);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<Ticket> closeTicket(@PathVariable Long id) {
        try {
            Ticket closed = ticketService.closeTicket(id);
            return ResponseEntity.ok(closed);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<Ticket> resolveTicket(@PathVariable Long id) {
        try {
            Ticket resolved = ticketService.resolveTicket(id);
            return ResponseEntity.ok(resolved);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Ticket> rejectTicket(@PathVariable Long id) {
        try {
            Ticket rejected = ticketService.rejectTicket(id);
            return ResponseEntity.ok(rejected);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/link-project/{projectId}")
    public ResponseEntity<Ticket> linkToProject(
            @PathVariable Long id,
            @PathVariable Long projectId) {
        try {
            Ticket linked = ticketService.linkToProject(id, projectId);
            return ResponseEntity.ok(linked);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/unlink-project")
    public ResponseEntity<Ticket> unlinkFromProject(@PathVariable Long id) {
        try {
            Ticket unlinked = ticketService.unlinkFromProject(id);
            return ResponseEntity.ok(unlinked);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        try {
            ticketService.deleteTicket(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // =================== STATISTICS ===================

    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserTicketStats(@PathVariable Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("reportedTickets", ticketService.countByReportedByUserId(userId));
        stats.put("assignedTickets", ticketService.countByAssignedToUserId(userId));
        stats.put("recentTickets", ticketService.countRecentTicketsByUser(userId, LocalDateTime.now().minusDays(7)));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/global")
    public ResponseEntity<Map<String, Object>> getGlobalTicketStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTickets", ticketService.findAll().size());
        stats.put("openTickets", ticketService.countOpenTickets());
        stats.put("unassignedTickets", ticketService.countUnassignedTickets());
        stats.put("publicTickets", ticketService.countPublicTickets());

        stats.put("byStatus", Map.of(
                "enviado", ticketService.countByStatus(Ticket.EstadoTicket.ENVIADO),
                "recibido", ticketService.countByStatus(Ticket.EstadoTicket.RECIBIDO)
        ));

        stats.put("byStage", Map.of(
                "pendiente", ticketService.countByStage(Ticket.EtapaTicket.PENDIENTE),
                "enProgreso", ticketService.countByStage(Ticket.EtapaTicket.EN_PROGRESO),
                "resuelto", ticketService.countByStage(Ticket.EtapaTicket.RESUELTO),
                "cerrado", ticketService.countByStage(Ticket.EtapaTicket.CERRADO),
                "rechazado", ticketService.countByStage(Ticket.EtapaTicket.RECHAZADO)
        ));

        stats.put("byPriority", Map.of(
                "baja", ticketService.countByPriority(Ticket.PrioridadTicket.BAJA),
                "media", ticketService.countByPriority(Ticket.PrioridadTicket.MEDIA),
                "alta", ticketService.countByPriority(Ticket.PrioridadTicket.ALTA)
        ));

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/project/{projectId}")
    public ResponseEntity<Map<String, Object>> getProjectTicketStats(@PathVariable Long projectId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTickets", ticketService.countByProjectId(projectId));
        return ResponseEntity.ok(stats);
    }
}
