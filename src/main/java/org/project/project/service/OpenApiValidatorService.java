package org.project.project.service;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para validar contratos OpenAPI.
 * 
 * Valida que el contenido YAML/JSON cumpla con el estándar OpenAPI 3.0.x
 * antes de ser almacenado en GCS y la base de datos.
 * 
 * Características:
 * - Valida sintaxis YAML/JSON
 * - Valida estructura OpenAPI 3.0.x
 * - Detecta errores y advertencias
 * - Valida campos requeridos (info, paths, etc.)
 * 
 * @author GitHub Copilot
 * @since 2025-10-30
 */
@Service
@Slf4j
public class OpenApiValidatorService {

    /**
     * Valida un contrato OpenAPI.
     * 
     * @param contratoContent Contenido del contrato en formato YAML o JSON
     * @return ValidationResult con resultado de la validación
     */
    public ValidationResult validarContrato(String contratoContent) {
        log.info("🔍 Iniciando validación de contrato OpenAPI (tamaño: {} caracteres)", 
                contratoContent.length());

        if (contratoContent == null || contratoContent.trim().isEmpty()) {
            log.warn("❌ Contrato vacío");
            return ValidationResult.error("El contrato no puede estar vacío");
        }

        try {
            // Configurar opciones de parsing
            ParseOptions parseOptions = new ParseOptions();
            parseOptions.setResolve(true); // Resolver referencias $ref
            parseOptions.setResolveFully(true); // Resolver completamente

            // Parsear el contrato OpenAPI
            SwaggerParseResult result = new OpenAPIV3Parser().readContents(
                    contratoContent, 
                    null, 
                    parseOptions
            );

            OpenAPI openAPI = result.getOpenAPI();
            List<String> messages = result.getMessages();

            // Verificar si el parsing fue exitoso
            if (openAPI == null) {
                log.error("❌ Error al parsear contrato OpenAPI: {}", messages);
                return ValidationResult.error(
                        "El contrato no es un OpenAPI válido: " + String.join(", ", messages)
                );
            }

            // Verificar mensajes de error críticos
            if (messages != null && !messages.isEmpty()) {
                // Filtrar solo errores críticos
                List<String> criticalErrors = messages.stream()
                        .filter(msg -> msg.toLowerCase().contains("error") 
                                    || msg.toLowerCase().contains("required")
                                    || msg.toLowerCase().contains("missing"))
                        .toList();

                if (!criticalErrors.isEmpty()) {
                    log.warn("⚠️ Advertencias al parsear contrato: {}", criticalErrors);
                    return ValidationResult.warning(
                            "El contrato tiene advertencias: " + String.join(", ", criticalErrors),
                            openAPI
                    );
                }
            }

            // Validaciones adicionales de seguridad
            ValidationResult securityValidation = validarSeguridad(contratoContent);
            if (!securityValidation.isValid()) {
                return securityValidation;
            }

            // Validaciones de estructura mínima
            ValidationResult structureValidation = validarEstructuraMinima(openAPI);
            if (!structureValidation.isValid()) {
                return structureValidation;
            }

            log.info("✅ Contrato OpenAPI válido: {} v{}", 
                    openAPI.getInfo().getTitle(), 
                    openAPI.getInfo().getVersion());

            return ValidationResult.success(openAPI);

        } catch (Exception e) {
            log.error("❌ Error inesperado al validar contrato: {}", e.getMessage(), e);
            return ValidationResult.error("Error al validar contrato: " + e.getMessage());
        }
    }

    /**
     * Valida aspectos de seguridad del contrato.
     * 
     * @param contratoContent Contenido del contrato
     * @return ValidationResult con resultado de la validación
     */
    private ValidationResult validarSeguridad(String contratoContent) {
        // 1. Validar tamaño máximo (prevenir ataques de denegación de servicio)
        if (contratoContent.length() > 1_000_000) { // 1 MB
            log.warn("❌ Contrato demasiado grande: {} caracteres", contratoContent.length());
            return ValidationResult.error(
                    "El contrato es demasiado grande (máximo 1 MB)"
            );
        }

        // 2. Detectar contenido potencialmente malicioso
        String lowerContent = contratoContent.toLowerCase();
        
        // Detectar scripts maliciosos
        if (lowerContent.contains("<script>") || lowerContent.contains("javascript:")) {
            log.error("❌ Contrato contiene código malicioso (scripts)");
            return ValidationResult.error(
                    "El contrato contiene código potencialmente malicioso"
            );
        }

        // Detectar inyección SQL
        if (lowerContent.matches(".*drop\\s+table.*") || 
            lowerContent.matches(".*delete\\s+from.*") ||
            lowerContent.matches(".*truncate\\s+table.*")) {
            log.error("❌ Contrato contiene posible inyección SQL");
            return ValidationResult.error(
                    "El contrato contiene contenido sospechoso"
            );
        }

        // Detectar URLs sospechosas (permitir solo HTTPS en producción)
        // Esta validación es opcional y puede ajustarse según necesidades
        
        return ValidationResult.success(null);
    }

    /**
     * Valida que el contrato tenga la estructura mínima requerida.
     * 
     * @param openAPI Objeto OpenAPI parseado
     * @return ValidationResult con resultado de la validación
     */
    private ValidationResult validarEstructuraMinima(OpenAPI openAPI) {
        // 1. Validar que tenga info
        if (openAPI.getInfo() == null) {
            log.error("❌ Contrato sin sección 'info'");
            return ValidationResult.error("El contrato debe tener una sección 'info'");
        }

        // 2. Validar que info tenga title y version
        if (openAPI.getInfo().getTitle() == null || openAPI.getInfo().getTitle().trim().isEmpty()) {
            log.error("❌ Contrato sin 'info.title'");
            return ValidationResult.error("El contrato debe tener un título (info.title)");
        }

        if (openAPI.getInfo().getVersion() == null || openAPI.getInfo().getVersion().trim().isEmpty()) {
            log.error("❌ Contrato sin 'info.version'");
            return ValidationResult.error("El contrato debe tener una versión (info.version)");
        }

        // 3. Validar que tenga al menos un path (opcional - puede estar vacío al inicio)
        if (openAPI.getPaths() == null || openAPI.getPaths().isEmpty()) {
            log.warn("⚠️ Contrato sin paths definidos");
            // No retornar error, solo advertir
        }

        return ValidationResult.success(openAPI);
    }

    /**
     * Clase para encapsular el resultado de la validación.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final boolean hasWarnings;
        private final String message;
        private final OpenAPI openAPI;

        private ValidationResult(boolean valid, boolean hasWarnings, String message, OpenAPI openAPI) {
            this.valid = valid;
            this.hasWarnings = hasWarnings;
            this.message = message;
            this.openAPI = openAPI;
        }

        public static ValidationResult success(OpenAPI openAPI) {
            return new ValidationResult(true, false, "Contrato válido", openAPI);
        }

        public static ValidationResult warning(String message, OpenAPI openAPI) {
            return new ValidationResult(true, true, message, openAPI);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, false, message, null);
        }

        public boolean isValid() {
            return valid;
        }

        public boolean hasWarnings() {
            return hasWarnings;
        }

        public String getMessage() {
            return message;
        }

        public OpenAPI getOpenAPI() {
            return openAPI;
        }
    }
}
