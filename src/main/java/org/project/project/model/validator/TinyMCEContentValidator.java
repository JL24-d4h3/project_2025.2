package org.project.project.model.validator;

import org.springframework.stereotype.Component;

/**
 * Validador para contenido HTML proveniente de TinyMCE
 * Verifica seguridad y limpieza del contenido
 */
@Component
public class TinyMCEContentValidator {

    private static final int MIN_CONTENT_LENGTH = 10;
    private static final int MAX_CONTENT_LENGTH = 50000;

    /**
     * Valida que el contenido sea seguro y cumpla los límites
     */
    public boolean isValidContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        // Remover etiquetas HTML para contar caracteres "reales"
        String plainText = stripHtmlTags(content);
        
        return plainText.length() >= MIN_CONTENT_LENGTH &&
               plainText.length() <= MAX_CONTENT_LENGTH;
    }

    /**
     * Obtiene mensaje de error específico
     */
    public String getValidationError(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "El contenido no puede estar vacío";
        }

        String plainText = stripHtmlTags(content);
        
        if (plainText.length() < MIN_CONTENT_LENGTH) {
            return String.format("El contenido debe tener al menos %d caracteres", MIN_CONTENT_LENGTH);
        }

        if (plainText.length() > MAX_CONTENT_LENGTH) {
            return String.format("El contenido no puede exceder %d caracteres", MAX_CONTENT_LENGTH);
        }

        return null;
    }

    /**
     * Limpia el contenido HTML de script maliciosos
     */
    public String sanitizeHtmlContent(String content) {
        if (content == null) {
            return null;
        }

        // Remover event handlers
        content = content.replaceAll("on\\w+\\s*=", "");
        
        // Remover script tags
        content = content.replaceAll("<script[^>]*>.*?</script>", "");
        
        // Remover iframe tags
        content = content.replaceAll("<iframe[^>]*>.*?</iframe>", "");

        return content;
    }

    /**
     * Extrae texto plano sin etiquetas HTML
     */
    private String stripHtmlTags(String html) {
        return html.replaceAll("<[^>]*>", "").trim();
    }

    /**
     * Valida que las etiquetas HTML sean solo las permitidas por TinyMCE
     */
    public boolean hasOnlyAllowedTags(String content) {
        if (content == null) {
            return true;
        }

        // Etiquetas permitidas desde TinyMCE estándar
        String[] allowedTags = {
            "p", "br", "strong", "b", "em", "i", "u", "h1", "h2", "h3", "h4", "h5", "h6",
            "ol", "ul", "li", "a", "img", "blockquote", "table", "tr", "td", "th",
            "div", "span", "code", "pre"
        };

        String lowerContent = content.toLowerCase();
        for (String tag : allowedTags) {
            lowerContent = lowerContent.replace("<" + tag, "@@")
                                       .replace("<" + tag + " ", "@@")
                                       .replace("</" + tag + ">", "@@");
        }

        // Si quedan etiquetas, significa que hay no permitidas
        return !lowerContent.matches(".*<[^>]+>.*");
    }
}
