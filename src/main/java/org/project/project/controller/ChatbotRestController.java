package org.project.project.controller;

import lombok.RequiredArgsConstructor;
import org.project.project.model.entity.ChatbotConversacion;
import org.project.project.model.entity.Ticket;
import org.project.project.service.ChatbotService;
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

/**
 * 🤖 REST API Controller para Flowise Chatbot
 *
 * Este controlador centraliza toda la información necesaria para que el chatbot
 * pueda acceder a datos del usuario, proyectos, repositorios, APIs y documentación.
 *
 * Base URL: /api/chatbot
 *
 * Endpoints principales:
 * - GET /api/chatbot/session - Información completa del usuario autenticado
 * - GET /api/chatbot/context - Contexto completo para el chatbot (usuario + proyectos + repos + apis)
 * - GET /api/chatbot/documentation/search - Buscar en documentación de APIs
 * - GET /api/chatbot/files/search - Buscar archivos en repositorios/proyectos
 * - GET /api/chatbot/activity - Resumen de actividad del usuario
 *
 * @author TelDev Team
 * @version 1.0
 * @since 2025-11-04
 */
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotRestController {

    private final ChatbotService chatbotService;
    private final TicketService ticketService;

    // =================== CRITICAL ENDPOINTS FOR CHATBOT ===================

    /**
     * 🔴 CRÍTICO: Endpoint de sesión completa del usuario
     * Retorna TODA la información del usuario en una sola llamada:
     * - Datos personales
     * - Proyectos
     * - Repositorios
     * - APIs
     * - Tickets
     * - Estadísticas
     *
     * GET /api/chatbot/session?userId={userId}
     */
    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getUserSession(@RequestParam Long userId) {
        try {
            Map<String, Object> session = chatbotService.getUserCompleteSession(userId);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * 🔴 CRÍTICO: Listar proyectos del usuario
     * GET /api/chatbot/projects/user/{userId}
     */
    @GetMapping("/projects/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserProjects(@PathVariable Long userId) {
        try {
            List<Map<String, Object>> projects = chatbotService.getUserProjects(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("totalProyectos", projects.size());
            response.put("proyectos", projects);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * 🔴 CRÍTICO: Listar repositorios del usuario
     * GET /api/chatbot/repositories/user/{userId}
     */
    @GetMapping("/repositories/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserRepositories(@PathVariable Long userId) {
        try {
            List<Map<String, Object>> repositories = chatbotService.getUserRepositories(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("totalRepositorios", repositories.size());
            response.put("repositorios", repositories);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * 🔴 CRÍTICO: Listar APIs del usuario
     * GET /api/chatbot/apis/user/{userId}
     */
    @GetMapping("/apis/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserApis(@PathVariable Long userId) {
        try {
            List<Map<String, Object>> apis = chatbotService.getUserApis(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("totalApis", apis.size());
            response.put("apis", apis);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * 🔴 CRÍTICO: Listar tickets del usuario
     * GET /api/chatbot/tickets/user/{userId}
     */
    @GetMapping("/tickets/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserTickets(@PathVariable Long userId) {
        try {
            List<Map<String, Object>> tickets = chatbotService.getUserTickets(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("totalTickets", tickets.size());
            response.put("tickets", tickets);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * 🔴 CRÍTICO: Crear ticket desde el chatbot
     * POST /api/chatbot/tickets/create
     * 
     * Endpoint público para creación de tickets desde el chatbot sin autenticación.
     * Mapea los tipos del chatbot (BUG, FEATURE, SOPORTE) a los tipos del backend.
     */
    @PostMapping("/tickets/create")
    public ResponseEntity<Map<String, Object>> createTicket(
            @RequestParam Long reportedByUserId,
            @RequestParam String subject,
            @RequestParam String body,
            @RequestParam String type) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Mapear tipo del chatbot a tipo del backend
            Ticket.TipoTicket tipoTicket;
            switch (type.toUpperCase()) {
                case "INCIDENCIA":
                case "BUG":
                    tipoTicket = Ticket.TipoTicket.INCIDENCIA;
                    break;
                case "REQUERIMIENTO":
                case "FEATURE":
                case "MEJORA":
                    tipoTicket = Ticket.TipoTicket.REQUERIMIENTO;
                    break;
                case "CONSULTA":
                case "SOPORTE":
                case "DOCUMENTACION":
                    tipoTicket = Ticket.TipoTicket.CONSULTA;
                    break;
                default:
                    tipoTicket = Ticket.TipoTicket.CONSULTA;
            }
            
            // Crear ticket usando el servicio
            Ticket ticket = ticketService.createPublicTicket(reportedByUserId, subject, body, tipoTicket);
            
            // Preparar respuesta en formato esperado por el chatbot
            response.put("success", true);
            response.put("ticketId", ticket.getTicketId());
            response.put("asunto", ticket.getAsuntoTicket());
            response.put("tipo", ticket.getTipoTicket().toString());
            response.put("estado", ticket.getEstadoTicket().toString());
            response.put("prioridad", ticket.getPrioridadTicket().toString());
            response.put("fechaCreacion", ticket.getFechaCreacion());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // =================== CONVERSATION ENDPOINTS ===================

    @PostMapping("/conversations")
    public ResponseEntity<ChatbotConversacion> createConversation(
            @RequestParam Long userId,
            @RequestParam String userMessage,
            @RequestParam String botResponse) {
        ChatbotConversacion conversation = chatbotService.createConversation(userId, userMessage, botResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
    }

    @PostMapping("/conversations/with-context")
    public ResponseEntity<ChatbotConversacion> createConversationWithContext(
            @RequestParam Long userId,
            @RequestParam String userMessage,
            @RequestParam String botResponse,
            @RequestParam String context) {
        ChatbotConversacion conversation = chatbotService.createConversationWithContext(userId, userMessage, botResponse, context);
        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<ChatbotConversacion> getConversationById(@PathVariable Long id) {
        return chatbotService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/conversations/user/{userId}")
    public ResponseEntity<List<ChatbotConversacion>> getConversationsByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        if (page > 0 || size != 20) {
            Pageable pageable = PageRequest.of(page, size);
            Page<ChatbotConversacion> conversations = chatbotService.findByUserId(userId, pageable);
            return ResponseEntity.ok(conversations.getContent());
        }
        List<ChatbotConversacion> conversations = chatbotService.findByUserId(userId);
        return ResponseEntity.ok(conversations);
    }

    @PutMapping("/conversations/{id}")
    public ResponseEntity<ChatbotConversacion> updateConversation(
            @PathVariable Long id,
            @RequestParam String userMessage,
            @RequestParam String botResponse) {
        try {
            ChatbotConversacion updated = chatbotService.updateConversation(id, userMessage, botResponse);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/conversations/{id}/resolve")
    public ResponseEntity<ChatbotConversacion> markAsResolved(@PathVariable Long id) {
        try {
            ChatbotConversacion resolved = chatbotService.markAsResolved(id);
            return ResponseEntity.ok(resolved);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/conversations/{id}/close")
    public ResponseEntity<ChatbotConversacion> closeConversation(@PathVariable Long id) {
        try {
            ChatbotConversacion closed = chatbotService.closeConversation(id);
            return ResponseEntity.ok(closed);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/conversations/{id}/archive")
    public ResponseEntity<ChatbotConversacion> archiveConversation(@PathVariable Long id) {
        try {
            ChatbotConversacion archived = chatbotService.archiveConversation(id);
            return ResponseEntity.ok(archived);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/conversations/{conversationId}/link-ticket/{ticketId}")
    public ResponseEntity<ChatbotConversacion> linkTicketToConversation(
            @PathVariable Long conversationId,
            @PathVariable Long ticketId) {
        try {
            ChatbotConversacion linked = chatbotService.linkTicketToConversation(conversationId, ticketId);
            return ResponseEntity.ok(linked);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        try {
            chatbotService.deleteConversation(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // =================== QUERY ENDPOINTS ===================

    @GetMapping("/conversations/status/{status}")
    public ResponseEntity<List<ChatbotConversacion>> getConversationsByStatus(
            @PathVariable ChatbotConversacion.EstadoConversacion status) {
        List<ChatbotConversacion> conversations = chatbotService.findByStatus(status);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/topic/{topic}")
    public ResponseEntity<List<ChatbotConversacion>> getConversationsByTopic(
            @PathVariable ChatbotConversacion.TemaConversacion topic) {
        List<ChatbotConversacion> conversations = chatbotService.findByTopic(topic);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/search")
    public ResponseEntity<List<ChatbotConversacion>> searchConversations(
            @RequestParam String keyword,
            @RequestParam(required = false) Long userId) {
        List<ChatbotConversacion> conversations;
        if (userId != null) {
            conversations = chatbotService.searchByUserAndKeyword(userId, keyword);
        } else {
            conversations = chatbotService.searchByKeyword(keyword);
        }
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/with-tickets")
    public ResponseEntity<List<ChatbotConversacion>> getConversationsWithTickets() {
        List<ChatbotConversacion> conversations = chatbotService.findConversationsWithTickets();
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/ticket/{ticketId}")
    public ResponseEntity<ChatbotConversacion> getConversationByTicketId(@PathVariable Long ticketId) {
        return chatbotService.findByTicketId(ticketId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =================== STATISTICS ENDPOINTS ===================

    @GetMapping("/conversations/stats/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserConversationStats(@PathVariable Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConversations", chatbotService.countByUserId(userId));

        List<ChatbotConversacion> userConversations = chatbotService.findByUserId(userId);
        long unresolvedCount = userConversations.stream()
                .filter(c -> !c.getIntentoResuelto())
                .count();

        stats.put("unresolvedConversations", unresolvedCount);
        stats.put("recentConversations", chatbotService.countRecentConversationsByUser(
                userId, LocalDateTime.now().minusDays(7)
        ));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/conversations/stats/global")
    public ResponseEntity<Map<String, Object>> getGlobalConversationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConversations", chatbotService.findAll().size());
        stats.put("unresolvedConversations", chatbotService.countUnresolvedConversations());
        stats.put("activeConversations", chatbotService.countByStatus(ChatbotConversacion.EstadoConversacion.ACTIVA));
        stats.put("closedConversations", chatbotService.countByStatus(ChatbotConversacion.EstadoConversacion.CERRADA));
        stats.put("archivedConversations", chatbotService.countByStatus(ChatbotConversacion.EstadoConversacion.ARCHIVADA));
        return ResponseEntity.ok(stats);
    }

    // =================== AI-SPECIFIC ENDPOINTS ===================

    @PutMapping("/conversations/{id}/tokens")
    public ResponseEntity<ChatbotConversacion> updateTokenUsage(
            @PathVariable Long id,
            @RequestParam int tokensUsed) {
        try {
            ChatbotConversacion updated = chatbotService.updateTokenUsage(id, tokensUsed);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/conversations/{id}/ai-model")
    public ResponseEntity<ChatbotConversacion> setAiModel(
            @PathVariable Long id,
            @RequestParam String modelName) {
        try {
            ChatbotConversacion updated = chatbotService.setAiModel(id, modelName);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/conversations/{id}/title")
    public ResponseEntity<ChatbotConversacion> updateTitle(
            @PathVariable Long id,
            @RequestParam String title) {
        try {
            ChatbotConversacion updated = chatbotService.updateTitle(id, title);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/conversations/{id}/topic")
    public ResponseEntity<ChatbotConversacion> updateTopic(
            @PathVariable Long id,
            @RequestParam ChatbotConversacion.TemaConversacion topic) {
        try {
            ChatbotConversacion updated = chatbotService.updateTopic(id, topic);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/conversations/{id}/nosql-id")
    public ResponseEntity<ChatbotConversacion> setNoSqlMessagesId(
            @PathVariable Long id,
            @RequestParam String nosqlId) {
        try {
            ChatbotConversacion updated = chatbotService.setNoSqlMessagesId(id, nosqlId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
