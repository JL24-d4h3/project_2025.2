package org.project.project.model.dto.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO de respuesta para operaciones de API (crear, actualizar, eliminar).
 * 
 * Proporciona información sobre el resultado de la operación:
 * - Éxito o fallo
 * - Mensaje descriptivo
 * - ID de la API creada/modificada
 * - URL de redirección (opcional)
 * 
 * Usado por el frontend para:
 * - Mostrar notificaciones de éxito/error
 * - Redirigir al catálogo o página de la API
 * - Actualizar la UI con el ID de la API creada
 * 
 * @author GitHub Copilot
 * @since 2025-10-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseDTO {
    
    /**
     * Indica si la operación fue exitosa
     * - true: Operación completada correctamente
     * - false: Operación falló
     */
    private Boolean success;
    
    /**
     * Mensaje descriptivo del resultado (requerido)
     * 
     * Ejemplos de éxito:
     * - "API 'Usuarios v1.0.0' creada exitosamente en estado BORRADOR"
     * - "Nueva versión 'Usuarios v2.0.0' creada con documentación copiada"
     * - "API eliminada correctamente"
     * 
     * Ejemplos de error:
     * - "Ya existe una API con el nombre 'Usuarios'"
     * - "Error al subir contrato YAML a Google Cloud Storage"
     * - "No tienes permisos para modificar esta API"
     * - "La versión 2.0.0 ya existe para esta API"
     */
    private String message;
    
    /**
     * ID de la API creada o modificada (opcional)
     * 
     * Solo se incluye en operaciones de creación/modificación exitosas
     * Null en operaciones de eliminación o cuando hay error
     * 
     * El frontend puede usar este ID para:
     * - Redirigir a la página de detalle: /apis/{apiId}
     * - Actualizar listas sin recargar
     * - Realizar operaciones posteriores
     */
    private Long apiId;
    
    /**
     * URL de redirección después de la operación (opcional)
     * 
     * Ejemplos:
     * - "/catalogo?filter=misApis" → Después de crear API
     * - "/apis/{apiId}" → Después de actualizar
     * - "/catalogo" → Después de eliminar
     * - null → Cuando hay error o no se requiere redirección
     * 
     * El frontend puede ignorar esto si prefiere manejar la navegación localmente
     */
    private String redirectUrl;
    
    /**
     * Método auxiliar para crear respuesta de éxito
     * 
     * @param message Mensaje de éxito
     * @param apiId ID de la API
     * @param redirectUrl URL de redirección
     * @return ApiResponseDTO configurado
     */
    public static ApiResponseDTO success(String message, Long apiId, String redirectUrl) {
        return ApiResponseDTO.builder()
                .success(true)
                .message(message)
                .apiId(apiId)
                .redirectUrl(redirectUrl)
                .build();
    }
    
    /**
     * Método auxiliar para crear respuesta de error
     * 
     * @param message Mensaje de error
     * @return ApiResponseDTO configurado
     */
    public static ApiResponseDTO error(String message) {
        return ApiResponseDTO.builder()
                .success(false)
                .message(message)
                .apiId(null)
                .redirectUrl(null)
                .build();
    }
}
