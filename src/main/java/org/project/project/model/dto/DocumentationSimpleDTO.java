package org.project.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado para documentación
 * Usado principalmente en Select2 para búsqueda y selección de documentaciones
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentationSimpleDTO {

    private Long documentationId;
    private String apiName;
    private String section;
    private String displayText; // Texto combinado: "API Pagos - Sección de Autenticación"

    /**
     * Constructor para queries JPQL que solo reciben 3 parámetros
     */
    public DocumentationSimpleDTO(Long documentationId, String apiName, String section) {
        this.documentationId = documentationId;
        this.apiName = apiName;
        this.section = section;
        this.displayText = buildDisplayText(apiName, section);
    }

    /**
     * Construye el texto de visualización combinando API y sección
     */
    private String buildDisplayText(String apiName, String section) {
        if (section != null && !section.trim().isEmpty()) {
            return apiName + " - " + section;
        }
        return apiName + " - Sin sección";
    }
}
