package org.project.project.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.project.project.model.entity.Feedback;

import java.math.BigDecimal;

/**
 * DTO para crear un nuevo feedback
 * Usado en el formulario de creación de feedback
 */
@Data
public class CreateFeedbackDTO {

    @NotNull(message = "El ID de documentación es requerido")
    private Long documentationId;

    @NotBlank(message = "El comentario es requerido")
    @Size(min = 10, max = 2000, message = "El comentario debe tener entre 10 y 2000 caracteres")
    private String comment;

    @NotNull(message = "La calificación es requerida")
    @DecimalMin(value = "1.0", message = "La calificación debe ser al menos 1.0")
    @DecimalMax(value = "5.0", message = "La calificación debe ser como máximo 5.0")
    private BigDecimal rating;

//    @NotNull(message = "El tipo de feedback es requerido")
//    private Feedback.TipoFeedback feedbackType;
}
