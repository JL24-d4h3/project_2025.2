package org.project.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.project.model.dto.CreateFeedbackDTO;
import org.project.project.model.dto.DocumentationSimpleDTO;
import org.project.project.model.dto.FeedbackResponseDTO;
import org.project.project.model.entity.Documentacion;
import org.project.project.model.entity.Feedback;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.DocumentacionRepository;
import org.project.project.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar feedbacks de documentaciones
 * Lógica universal: cualquier usuario que crea versiones de API recibe feedbacks en esas documentaciones
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final DocumentacionRepository documentacionRepository;
    private final UserService userService;

    // ========== CREATE ==========

    /**
     * Crea un nuevo feedback
     * Valida que la documentación exista y que el usuario esté autenticado
     */
    @Transactional
    public FeedbackResponseDTO createFeedback(CreateFeedbackDTO dto, Long authenticatedUserId) {
        log.info("Creating feedback for documentation {} by user {}", dto.getDocumentationId(), authenticatedUserId);

        // Validar que la documentación existe
        Documentacion documentacion = documentacionRepository.findById(dto.getDocumentationId())
                .orElseThrow(() -> new IllegalArgumentException("Documentación no encontrada"));

        // Obtener usuario autenticado
        Usuario usuario = userService.buscarUsuarioPorId(authenticatedUserId);

        // Crear entidad feedback
        Feedback feedback = new Feedback();
        feedback.setDocumentacion(documentacion);
        feedback.setUsuario(usuario);
        feedback.setComentario(dto.getComment());
        feedback.setPuntuacion(dto.getRating());
//        feedback.setTipoFeedback(dto.getFeedbackType());
//        feedback.setEstadoFeedback(Feedback.EstadoFeedback.PENDIENTE);
        feedback.setFechaFeedback(LocalDateTime.now());

        // Guardar
        Feedback savedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback {} created successfully", savedFeedback.getFeedbackId());

        return toResponseDTO(savedFeedback);
    }

    // ========== READ ==========

    /**
     * Obtiene todos los feedbacks CREADOS POR el usuario
     * (Pestaña "Mis Feedbacks")
     */
    @Transactional(readOnly = true)
    public List<FeedbackResponseDTO> getMyFeedbacks(Long userId) {
        log.info("Fetching feedbacks created by user {}", userId);
        List<Feedback> feedbacks = feedbackRepository.findByUserIdOrderByDateDesc(userId);
        log.info("Found {} feedbacks created by user {}", feedbacks.size(), userId);
        
        // Log de verificación
        feedbacks.forEach(f -> {
            log.debug("Feedback F-{}: creado por usuario {} ({})", 
                f.getFeedbackId(), 
                f.getUsuario().getUsuarioId(), 
                f.getUsuario().getNombreUsuario());
        });
        
        return feedbacks.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los feedbacks RECIBIDOS por el usuario
     * (Pestaña "Feedbacks Recibidos")
     * Basado en: usuario creó versiones → obtener APIs → obtener documentaciones → obtener feedbacks
     */
    @Transactional(readOnly = true)
    public List<FeedbackResponseDTO> getReceivedFeedbacks(Long userId) {
        log.info("Fetching received feedbacks for user {}", userId);
        List<Feedback> feedbacks = feedbackRepository.findReceivedFeedbacksByUserId(userId);
        return feedbacks.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un feedback por ID
     * Valida que el usuario tenga permiso (es creador del feedback O es receptor de ese feedback)
     */
    @Transactional(readOnly = true)
    public FeedbackResponseDTO getFeedbackById(Long feedbackId, Long authenticatedUserId) {
        log.info("Fetching feedback {} for user {}", feedbackId, authenticatedUserId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback no encontrado"));

        // Validar permiso: es el creador O es el receptor del feedback
        boolean isCreator = feedback.getUsuario().getUsuarioId().equals(authenticatedUserId);
        boolean isReceiver = isUserReceiverOfFeedback(feedback, authenticatedUserId);

        if (!isCreator && !isReceiver) {
            throw new SecurityException("No tienes permiso para ver este feedback");
        }

        return toResponseDTO(feedback);
    }

    // ========== UPDATE ==========

    /**
     * Actualiza un feedback existente
     * Solo el creador puede actualizar
     */
    @Transactional
    public FeedbackResponseDTO updateFeedback(Long feedbackId, CreateFeedbackDTO dto, Long authenticatedUserId) {
        log.info("Updating feedback {} by user {}", feedbackId, authenticatedUserId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback no encontrado"));

        // Validar que el usuario es el creador
        if (!feedback.getUsuario().getUsuarioId().equals(authenticatedUserId)) {
            throw new SecurityException("Solo el creador puede actualizar este feedback");
        }

        // Actualizar campos
        feedback.setComentario(dto.getComment());
        feedback.setPuntuacion(dto.getRating());
//        feedback.setTipoFeedback(dto.getFeedbackType());

        Feedback updatedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback {} updated successfully", feedbackId);

        return toResponseDTO(updatedFeedback);
    }

    /**
     * Marca un feedback como REVISADO
     * Solo el receptor del feedback puede marcar como revisado
     */
    @Transactional
    public FeedbackResponseDTO markAsReviewed(Long feedbackId, Long authenticatedUserId) {
        log.info("Marking feedback {} as REVIEWED by user {}", feedbackId, authenticatedUserId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback no encontrado"));

        // Validar que el usuario es receptor del feedback
        if (!isUserReceiverOfFeedback(feedback, authenticatedUserId)) {
            throw new SecurityException("Solo el receptor puede marcar este feedback como revisado");
        }

        // Actualizar estado
//        feedback.setEstadoFeedback(Feedback.EstadoFeedback.REVISADO);
//        feedback.setFechaRevision(LocalDateTime.now());

        Usuario reviewer = userService.buscarUsuarioPorId(authenticatedUserId);
//        feedback.setRevisadoPor(reviewer);

        Feedback updatedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback {} marked as REVIEWED", feedbackId);

        return toResponseDTO(updatedFeedback);
    }

    /**
     * Marca un feedback como RESUELTO
     * Solo el receptor del feedback puede marcar como resuelto
     */
    @Transactional
    public FeedbackResponseDTO markAsResolved(Long feedbackId, Long authenticatedUserId) {
        log.info("Marking feedback {} as RESOLVED by user {}", feedbackId, authenticatedUserId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback no encontrado"));

        // Validar que el usuario es receptor del feedback
        if (!isUserReceiverOfFeedback(feedback, authenticatedUserId)) {
            throw new SecurityException("Solo el receptor puede marcar este feedback como resuelto");
        }

        // Actualizar estado
//        feedback.setEstadoFeedback(Feedback.EstadoFeedback.RESUELTO);
//        feedback.setFechaRevision(LocalDateTime.now());

        Usuario reviewer = userService.buscarUsuarioPorId(authenticatedUserId);
//        feedback.setRevisadoPor(reviewer);

        Feedback updatedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback {} marked as RESOLVED", feedbackId);

        return toResponseDTO(updatedFeedback);
    }

    // ========== DELETE ==========

    /**
     * Elimina un feedback
     * Solo el creador puede eliminar
     */
    @Transactional
    public void deleteFeedback(Long feedbackId, Long authenticatedUserId) {
        log.info("Deleting feedback {} by user {}", feedbackId, authenticatedUserId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback no encontrado"));

        // Validar que el usuario es el creador
        if (!feedback.getUsuario().getUsuarioId().equals(authenticatedUserId)) {
            throw new SecurityException("Solo el creador puede eliminar este feedback");
        }

        feedbackRepository.delete(feedback);
        log.info("Feedback {} deleted successfully", feedbackId);
    }

    // ========== SEARCH ==========

    /**
     * Busca documentaciones para Select2 AJAX
     * Retorna DTOs ligeros sin cargar entidades completas
     * Si searchTerm está vacío, devuelve las primeras documentaciones
     */
    @Transactional(readOnly = true)
    public List<DocumentationSimpleDTO> searchDocumentationsForSelect2(String searchTerm) {
        log.info("Searching documentations with term: {}", searchTerm);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // Retornar primeras documentaciones cuando no hay búsqueda
            log.info("No search term provided, returning initial documentations");
            List<DocumentationSimpleDTO> allDocs = feedbackRepository.findTop10DocumentationsForSelect2();
            // Limitar a 10 resultados manualmente
            return allDocs.size() > 10 ? allDocs.subList(0, 10) : allDocs;
        }
        
        return feedbackRepository.searchDocumentationsForSelect2(searchTerm.trim());
    }

    // ========== STATISTICS ==========

    /**
     * Obtiene estadísticas para la vista overview
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics(Long userId) {
        log.info("Calculating statistics for user {}", userId);

        Map<String, Object> stats = new HashMap<>();

        // Total feedbacks enviados
        Long totalSent = feedbackRepository.countByUserId(userId);
        stats.put("totalSent", totalSent);

        // Total feedbacks recibidos
        Long totalReceived = (long) feedbackRepository.findReceivedFeedbacksByUserId(userId).size();
        stats.put("totalReceived", totalReceived);

        // Feedbacks recibidos pendientes
//        Long pendingReceived = feedbackRepository.countReceivedFeedbacksByUserIdAndStatus(
//                userId, Feedback.EstadoFeedback.PENDIENTE);
//        stats.put("pendingReceived", pendingReceived);

        // Calificación promedio de feedbacks enviados
        BigDecimal avgRating = feedbackRepository.getAverageRatingByUserId(userId);
        stats.put("averageRating", avgRating != null ? avgRating : BigDecimal.ZERO);

        log.info("Statistics calculated: {}", stats);
        return stats;
    }

    // ========== HELPER METHODS ==========

    /**
     * Convierte entidad Feedback a FeedbackResponseDTO
     * Evita exponer datos sensibles del usuario
     */
    private FeedbackResponseDTO toResponseDTO(Feedback feedback) {
        FeedbackResponseDTO dto = new FeedbackResponseDTO();

        // Datos del feedback
        dto.setFeedbackId(feedback.getFeedbackId());
        dto.setComment(feedback.getComentario());
        dto.setRating(feedback.getPuntuacion());
//        dto.setFeedbackType(feedback.getTipoFeedback());
//        dto.setFeedbackStatus(feedback.getEstadoFeedback());
        dto.setCreatedAt(feedback.getFechaFeedback());
//        dto.setReviewedAt(feedback.getFechaRevision());

        // Datos del usuario (sin información sensible)
        Usuario usuario = feedback.getUsuario();
        dto.setUserId(usuario.getUsuarioId());
        dto.setUserName(usuario.getNombreUsuario());
        dto.setUserFullName(usuario.getApellidoPaterno() + " " + usuario.getApellidoMaterno() + ", " + usuario.getNombreUsuario());

        // Datos de la documentación
        Documentacion documentacion = feedback.getDocumentacion();
        dto.setDocumentationId(documentacion.getDocumentacionId());
        dto.setApiName(documentacion.getApi().getNombreApi());
        dto.setDocumentationSection(documentacion.getSeccionDocumentacion());

        // Datos del revisor (si existe)
//        if (feedback.getRevisadoPor() != null) {
//            Usuario reviewer = feedback.getRevisadoPor();
//            dto.setReviewedBy(reviewer.getUsuarioId());
//            dto.setReviewedByName(reviewer.getApellidoPaterno() + " " + reviewer.getApellidoMaterno() + ", " + reviewer.getNombreUsuario());
//        }

        return dto;
    }

    /**
     * Valida si un usuario es receptor de un feedback
     * (Usuario creó versiones de la API a la que pertenece la documentación del feedback)
     */
    private boolean isUserReceiverOfFeedback(Feedback feedback, Long userId) {
        Long apiId = feedback.getDocumentacion().getApi().getApiId();
        List<Feedback> receivedFeedbacks = feedbackRepository.findReceivedFeedbacksByUserId(userId);
        
        return receivedFeedbacks.stream()
                .anyMatch(f -> f.getFeedbackId().equals(feedback.getFeedbackId()));
    }
}
