package org.project.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.project.model.dto.CreateFeedbackDTO;
import org.project.project.model.dto.DocumentationSimpleDTO;
import org.project.project.model.dto.FeedbackResponseDTO;
import org.project.project.model.entity.Usuario;
import org.project.project.service.FeedbackService;
import org.project.project.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador híbrido para gestión de feedbacks
 * Maneja vistas Thymeleaf + endpoints REST API
 */
@Controller
@RequestMapping("/devportal/{rol}/{username}/feedback")
@RequiredArgsConstructor
@Slf4j
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    // ========== HELPER: Obtener usuario autenticado ==========

    /**
     * Obtiene el usuario desde username del path
     */
    private Usuario obtenerUsuario(String username) throws Exception {
        Usuario usuario = userService.buscarPorUsername(username);
        if (usuario == null) {
            throw new Exception("Usuario no encontrado: " + username);
        }
        return usuario;
    }

    // ========== VISTAS THYMELEAF ==========

    /**
     * GET /feedback (redirige a /feedback/sent por defecto)
     */
    @GetMapping
    public String redirectToSent(@PathVariable String rol, @PathVariable String username) {
        return "redirect:/devportal/" + rol + "/" + username + "/feedback/sent";
    }

    /**
     * GET /feedback/sent
     * Vista de feedbacks enviados con paginación de 6 elementos por página
     */
    @GetMapping("/sent")
    public String showSentFeedbacks(
            @PathVariable String rol, 
            @PathVariable String username,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Showing sent feedbacks for user {}", usuario.getUsuarioId());

            // Obtener estadísticas
            Map<String, Object> stats = feedbackService.getStatistics(usuario.getUsuarioId());
            
            // Obtener todos los feedbacks enviados
            List<FeedbackResponseDTO> allSentFeedbacks = feedbackService.getMyFeedbacks(usuario.getUsuarioId());
            
            // Paginación manual (6 por página)
            int pageSize = 6;
            int sentStart = page * pageSize;
            int sentEnd = Math.min(sentStart + pageSize, allSentFeedbacks.size());
            List<FeedbackResponseDTO> sentFeedbacks = allSentFeedbacks.subList(
                Math.min(sentStart, allSentFeedbacks.size()), 
                Math.min(sentEnd, allSentFeedbacks.size())
            );
            int sentTotalPages = (int) Math.ceil((double) allSentFeedbacks.size() / pageSize);

            // Pasar datos a la vista
            // Obtener el usuario actual para el chatbot widget

            model.addAttribute("Usuario", usuario);
            model.addAttribute("stats", stats);
            model.addAttribute("sentFeedbacks", sentFeedbacks);
            model.addAttribute("receivedFeedbacks", List.of()); // Vacío para esta vista
            model.addAttribute("sentCurrentPage", page);
            model.addAttribute("sentTotalPages", sentTotalPages);
            model.addAttribute("receivedCurrentPage", 0);
            model.addAttribute("receivedTotalPages", 0);
            model.addAttribute("activeTab", "sent");
            model.addAttribute("usuario", usuario);
            model.addAttribute("currentUser", usuario);
            model.addAttribute("rol", rol);
            model.addAttribute("username", username);
            model.addAttribute("currentNavSection", "feedback");

            return "feedback/overview";

        } catch (Exception e) {
            log.error("Error loading sent feedbacks", e);
            model.addAttribute("error", "Error al cargar los feedbacks enviados");
            return "error/500";
        }
    }

    /**
     * GET /feedback/received
     * Vista de feedbacks recibidos con paginación de 6 elementos por página
     */
    @GetMapping("/received")
    public String showReceivedFeedbacks(
            @PathVariable String rol, 
            @PathVariable String username,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Showing received feedbacks for user {}", usuario.getUsuarioId());

            // Obtener estadísticas
            Map<String, Object> stats = feedbackService.getStatistics(usuario.getUsuarioId());
            
            // Obtener todos los feedbacks recibidos
            List<FeedbackResponseDTO> allReceivedFeedbacks = feedbackService.getReceivedFeedbacks(usuario.getUsuarioId());
            
            // Paginación manual (6 por página)
            int pageSize = 6;
            int receivedStart = page * pageSize;
            int receivedEnd = Math.min(receivedStart + pageSize, allReceivedFeedbacks.size());
            List<FeedbackResponseDTO> receivedFeedbacks = allReceivedFeedbacks.subList(
                Math.min(receivedStart, allReceivedFeedbacks.size()), 
                Math.min(receivedEnd, allReceivedFeedbacks.size())
            );
            int receivedTotalPages = (int) Math.ceil((double) allReceivedFeedbacks.size() / pageSize);

            // Pasar datos a la vista
            // Obtener el usuario actual para el chatbot widget

            model.addAttribute("Usuario", usuario);
            model.addAttribute("stats", stats);
            model.addAttribute("sentFeedbacks", List.of()); // Vacío para esta vista
            model.addAttribute("receivedFeedbacks", receivedFeedbacks);
            model.addAttribute("sentCurrentPage", 0);
            model.addAttribute("sentTotalPages", 0);
            model.addAttribute("receivedCurrentPage", page);
            model.addAttribute("receivedTotalPages", receivedTotalPages);
            model.addAttribute("activeTab", "received");
            model.addAttribute("usuario", usuario);
            model.addAttribute("currentUser", usuario);
            model.addAttribute("rol", rol);
            model.addAttribute("username", username);
            model.addAttribute("currentNavSection", "feedback");

            return "feedback/overview";

        } catch (Exception e) {
            log.error("Error loading received feedbacks", e);
            model.addAttribute("error", "Error al cargar los feedbacks recibidos");
            return "error/500";
        }
    }

    /**
     * GET /feedback/list
     * Vista con tabs: Mis Feedbacks / Feedbacks Recibidos
     * Lógica de tab activo:
     * - Si hay feedbacks recibidos pendientes → tab "recibidos" activo
     * - Caso contrario → tab "enviados" activo
     */
    @GetMapping("/list")
    public String showFeedbackList(@PathVariable String rol, @PathVariable String username, Model model) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Showing feedback list for user {}", usuario.getUsuarioId());

            // Obtener feedbacks
            List<FeedbackResponseDTO> myFeedbacks = feedbackService.getMyFeedbacks(usuario.getUsuarioId());
            List<FeedbackResponseDTO> receivedFeedbacks = feedbackService.getReceivedFeedbacks(usuario.getUsuarioId());

            // Determinar tab activo (inteligente)
            Map<String, Object> stats = feedbackService.getStatistics(usuario.getUsuarioId());
            Long pendingReceived = (Long) stats.get("pendingReceived");
            String activeTab = (pendingReceived > 0) ? "received" : "sent";

            // Pasar datos a la vista
            // Obtener el usuario actual para el chatbot widget

            model.addAttribute("Usuario", usuario);
            model.addAttribute("myFeedbacks", myFeedbacks);
            model.addAttribute("receivedFeedbacks", receivedFeedbacks);
            model.addAttribute("activeTab", activeTab);
            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", rol);
            model.addAttribute("username", username);

            return "feedback/list-feedbacks";

        } catch (Exception e) {
            log.error("Error loading feedback list", e);
            model.addAttribute("error", "Error al cargar la lista de feedbacks");
            return "error/500";
        }
    }

    /**
     * GET /feedback/create
     * Formulario para crear nuevo feedback
     */
    @GetMapping("/create")
    public String showCreateForm(@PathVariable String rol, @PathVariable String username, Model model) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Showing create feedback form for user {}", usuario.getUsuarioId());

            // Crear DTO vacío para el formulario
            model.addAttribute("Usuario", usuario);
            model.addAttribute("createFeedbackDTO", new CreateFeedbackDTO());
            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", rol);
            model.addAttribute("username", username);

            return "feedback/create";

        } catch (Exception e) {
            log.error("Error loading create feedback form", e);
            model.addAttribute("error", "Error al cargar el formulario");
            return "error/500";
        }
    }

    /**
     * GET /feedback/F-{id}
     * Vista de detalle de un feedback específico
     */
    @GetMapping("/F-{id:[0-9]+}")
    public String showFeedbackDetail(@PathVariable("id") Long feedbackId, @PathVariable String rol, @PathVariable String username, Model model) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Showing feedback detail F-{} for user {}", feedbackId, usuario.getUsuarioId());

            // Obtener feedback (valida permiso internamente)
            FeedbackResponseDTO feedback = feedbackService.getFeedbackById(feedbackId, usuario.getUsuarioId());

            // Determinar si el usuario es el receptor (para mostrar botones de acción)
            List<FeedbackResponseDTO> receivedFeedbacks = feedbackService.getReceivedFeedbacks(usuario.getUsuarioId());
            boolean isReceiver = receivedFeedbacks.stream()
                    .anyMatch(f -> f.getFeedbackId().equals(feedbackId));

            // Obtener el usuario actual para el chatbot widget

            model.addAttribute("Usuario", usuario);
            model.addAttribute("feedback", feedback);
            model.addAttribute("isReceiver", isReceiver);
            model.addAttribute("usuario", usuario);
            model.addAttribute("rol", rol);
            model.addAttribute("username", username);

            return "feedback/detail";

        } catch (SecurityException e) {
            log.warn("Access denied to feedback F-{}", feedbackId);
            model.addAttribute("error", "No tienes permiso para ver este feedback");
            return "error/403";
        } catch (IllegalArgumentException e) {
            log.warn("Feedback F-{} not found", feedbackId);
            model.addAttribute("error", "Feedback no encontrado");
            return "error/404";
        } catch (Exception e) {
            log.error("Error loading feedback detail F-{}", feedbackId, e);
            model.addAttribute("error", "Error al cargar el detalle del feedback");
            return "error/500";
        }
    }


    // ========== REST API ENDPOINTS ==========

    /**
     * POST /feedback/create
     * Crear nuevo feedback (API endpoint)
     */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createFeedback(
            @PathVariable String rol,
            @PathVariable String username,
            @Valid @RequestBody CreateFeedbackDTO dto,
            BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validar errores de binding
            if (bindingResult.hasErrors()) {
                response.put("success", false);
                response.put("message", "Datos inválidos");
                response.put("errors", bindingResult.getAllErrors());
                return ResponseEntity.badRequest().body(response);
            }

            Usuario usuario = obtenerUsuario(username);
            log.info("Creating feedback for documentation {} by user {}", dto.getDocumentationId(), usuario.getUsuarioId());

            // Crear feedback
            FeedbackResponseDTO createdFeedback = feedbackService.createFeedback(dto, usuario.getUsuarioId());

            response.put("success", true);
            response.put("message", "Feedback creado exitosamente");
            response.put("feedback", createdFeedback);
            response.put("redirectUrl", "/devportal/" + rol + "/" + username + "/feedback/F-" + createdFeedback.getFeedbackId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid documentation ID: {}", dto.getDocumentationId());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Error creating feedback", e);
            response.put("success", false);
            response.put("message", "Error al crear el feedback");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * PUT /feedback/{id}
     * Actualizar feedback existente
     */
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateFeedback(
            @PathVariable("id") Long feedbackId,
            @PathVariable String rol,
            @PathVariable String username,
            @Valid @RequestBody CreateFeedbackDTO dto,
            BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (bindingResult.hasErrors()) {
                response.put("success", false);
                response.put("message", "Datos inválidos");
                response.put("errors", bindingResult.getAllErrors());
                return ResponseEntity.badRequest().body(response);
            }

            Usuario usuario = obtenerUsuario(username);
            log.info("Updating feedback {} by user {}", feedbackId, usuario.getUsuarioId());

            // Actualizar feedback
            FeedbackResponseDTO updatedFeedback = feedbackService.updateFeedback(feedbackId, dto, usuario.getUsuarioId());

            response.put("success", true);
            response.put("message", "Feedback actualizado exitosamente");
            response.put("feedback", updatedFeedback);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            log.warn("Access denied to update feedback {}", feedbackId);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Feedback {} not found", feedbackId);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            log.error("Error updating feedback {}", feedbackId, e);
            response.put("success", false);
            response.put("message", "Error al actualizar el feedback");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * DELETE /feedback/{id}
     * Eliminar feedback
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteFeedback(
            @PathVariable("id") Long feedbackId,
            @PathVariable String rol,
            @PathVariable String username) {

        Map<String, Object> response = new HashMap<>();

        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Deleting feedback {} by user {}", feedbackId, usuario.getUsuarioId());

            feedbackService.deleteFeedback(feedbackId, usuario.getUsuarioId());

            response.put("success", true);
            response.put("message", "Feedback eliminado exitosamente");
            response.put("redirectUrl", "/devportal/" + rol + "/" + username + "/feedback");

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            log.warn("Access denied to delete feedback {}", feedbackId);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Feedback {} not found", feedbackId);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            log.error("Error deleting feedback {}", feedbackId, e);
            response.put("success", false);
            response.put("message", "Error al eliminar el feedback");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * PATCH /feedback/{id}/review
     * Marcar feedback como REVISADO
     */
    @PatchMapping("/{id}/review")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsReviewed(
            @PathVariable("id") Long feedbackId,
            @PathVariable String rol,
            @PathVariable String username) {

        Map<String, Object> response = new HashMap<>();

        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Marking feedback {} as REVIEWED by user {}", feedbackId, usuario.getUsuarioId());

            FeedbackResponseDTO updatedFeedback = feedbackService.markAsReviewed(feedbackId, usuario.getUsuarioId());

            response.put("success", true);
            response.put("message", "Feedback marcado como revisado");
            response.put("feedback", updatedFeedback);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            log.warn("Access denied to review feedback {}", feedbackId);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Feedback {} not found", feedbackId);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            log.error("Error reviewing feedback {}", feedbackId, e);
            response.put("success", false);
            response.put("message", "Error al marcar feedback como revisado");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * PATCH /feedback/{id}/resolve
     * Marcar feedback como RESUELTO
     */
    @PatchMapping("/{id}/resolve")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsResolved(
            @PathVariable("id") Long feedbackId,
            @PathVariable String rol,
            @PathVariable String username) {

        Map<String, Object> response = new HashMap<>();

        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Marking feedback {} as RESOLVED by user {}", feedbackId, usuario.getUsuarioId());

            FeedbackResponseDTO updatedFeedback = feedbackService.markAsResolved(feedbackId, usuario.getUsuarioId());

            response.put("success", true);
            response.put("message", "Feedback marcado como resuelto");
            response.put("feedback", updatedFeedback);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            log.warn("Access denied to resolve feedback {}", feedbackId);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Feedback {} not found", feedbackId);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            log.error("Error resolving feedback {}", feedbackId, e);
            response.put("success", false);
            response.put("message", "Error al marcar feedback como resuelto");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /feedback/search-documentations
     * Búsqueda AJAX para Select2
     * Retorna documentaciones en formato compatible con Select2
     */
    @GetMapping("/search-documentations")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchDocumentations(
            @RequestParam(value = "q", required = false, defaultValue = "") String searchTerm,
            @PathVariable String username) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validar usuario autenticado
            obtenerUsuario(username);

            log.info("Searching documentations with term: {}", searchTerm);

            // Buscar documentaciones
            List<DocumentationSimpleDTO> results = feedbackService.searchDocumentationsForSelect2(searchTerm);

            // Formato Select2: { results: [{id, text}, ...] }
            List<Map<String, Object>> select2Results = results.stream()
                    .map(dto -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", dto.getDocumentationId());
                        item.put("text", dto.getDisplayText());
                        return item;
                    })
                    .toList();

            response.put("results", select2Results);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error searching documentations", e);
            response.put("results", List.of());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
