package org.project.project.model.dto;

import lombok.Data;
import org.project.project.model.entity.Feedback;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuestas de feedback
 * Usado para devolver información completa de un feedback sin exponer datos sensibles
 */
@Data
public class FeedbackResponseDTO {

    private Long feedbackId;
    private String comment;
    private BigDecimal rating;
//    private Feedback.TipoFeedback feedbackType;
//    private Feedback.EstadoFeedback feedbackStatus;
    private LocalDateTime createdAt;

    // Usuario que creó el feedback
    private Long userId;
    private String userName;
    private String userFullName;

    // Documentación asociada
    private Long documentationId;
    private String apiName;
    private String documentationSection;

    // Si fue revisado por alguien
    private Long reviewedBy;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
}
