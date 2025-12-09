package org.project.project.model.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.project.project.model.entity.Reporte;

/**
 * Implementación del validador para tipos de reporte
 */
public class ValidTipoReporteValidator implements ConstraintValidator<ValidTipoReporte, String> {

    @Override
    public void initialize(ValidTipoReporte annotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        try {
            Reporte.TipoReporte.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
