package org.project.project.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validador personalizado para tipos de reporte
 * Asegura que solo se usen tipos válidos
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTipoReporteValidator.class)
@Documented
public @interface ValidTipoReporte {
    String message() default "Tipo de reporte inválido. Valores permitidos: TICKET, API, PROYECTO, REPOSITORIO, DOCUMENTACION, FORO, GENERAL";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
