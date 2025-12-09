package org.project.project.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        System.out.println("Error detected - Status: " + status + ", Message: " + errorMessage + ", URI: " + requestUri);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            // Si el código es 200, no es realmente un error, redirigir al inicio
            if (statusCode == HttpStatus.OK.value()) {
                return "redirect:/";
            }
            
            // ✅ Si es 404 y el usuario está autenticado, intentar limpiar la URL
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                    String username = auth.getName();
                    String uri = requestUri != null ? requestUri.toString() : "";
                    
                    System.out.println("🔍 404 detectado para usuario autenticado. URI: " + uri);
                    
                    // Intentar limpiar la URL manteniendo la sección válida
                    String cleanedUrl = cleanInvalidUrl(uri, username);
                    
                    if (cleanedUrl != null && !cleanedUrl.equals(uri)) {
                        System.out.println("✅ Redirigiendo a URL limpia: " + cleanedUrl);
                        return "redirect:" + cleanedUrl;
                    }
                }
                
                // Si no está autenticado o no se pudo limpiar, mostrar 404 normal
                model.addAttribute("statusCode", statusCode);
                model.addAttribute("errorMessage", "Página no encontrada");
                model.addAttribute("requestUri", requestUri != null ? requestUri.toString() : "");
                model.addAttribute("title", "Página no encontrada");
                model.addAttribute("description", "La página que buscas no existe o ha sido movida.");
                return "error/404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("title", "Error interno del servidor");
                model.addAttribute("description", "Ha ocurrido un error inesperado en el servidor.");
                model.addAttribute("statusCode", statusCode);
                model.addAttribute("errorMessage", errorMessage != null ? errorMessage.toString() : "Error desconocido");
                model.addAttribute("requestUri", requestUri != null ? requestUri.toString() : "URI desconocida");
                return "error/500";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("title", "Acceso denegado");
                model.addAttribute("description", "No tienes permisos para acceder a este recurso.");
                model.addAttribute("statusCode", statusCode);
                model.addAttribute("errorMessage", errorMessage != null ? errorMessage.toString() : "Error desconocido");
                model.addAttribute("requestUri", requestUri != null ? requestUri.toString() : "URI desconocida");
                return "error/403";
            }
        }
        
        model.addAttribute("title", "Error");
        model.addAttribute("description", "Ha ocurrido un error inesperado.");
        return "error/generic";
    }
    
    /**
     * Limpia una URL inválida manteniendo la sección válida más cercana.
     * Ejemplos:
     * - /devportal/po/mlopez/reportsjajajar → /devportal/po/mlopez/reports
     * - /devportal/po/mlopez/catalogXYZ123 → /devportal/po/mlopez/catalog
     * - /devportal/dev/usuario/projects/P-3abc → /devportal/dev/usuario/projects
     */
    private String cleanInvalidUrl(String uri, String username) {
        if (uri == null || uri.isEmpty()) {
            return null;
        }
        
        System.out.println("🧹 Limpiando URL: " + uri);
        
        // Patrón esperado: /devportal/{role}/{username}/{section}/...
        String[] parts = uri.split("/");
        
        // Validar estructura mínima: ["", "devportal", "role", "username"]
        if (parts.length < 4 || !"devportal".equals(parts[1])) {
            System.out.println("❌ URL no tiene estructura devportal válida");
            return null;
        }
        
        String role = parts[2];
        String uriUsername = parts[3];
        
        // Validar rol
        if (!role.matches("^(dev|po|qa|sa)$")) {
            System.out.println("❌ Rol inválido: " + role);
            return null;
        }
        
        // Validar username
        if (!uriUsername.equals(username)) {
            System.out.println("⚠️ Username en URL no coincide con usuario autenticado");
            return null;
        }
        
        // Si solo tiene el prefijo base, agregar dashboard
        if (parts.length == 4) {
            String result = "/devportal/" + role + "/" + username + "/dashboard";
            System.out.println("✅ URL limpia (sin sección): " + result);
            return result;
        }
        
        // Extraer la sección (5ta parte)
        String section = parts[4];
        System.out.println("🔍 Sección detectada: " + section);
        
        // Limpiar caracteres no alfabéticos y guiones de la sección
        // Ejemplos: "reportsjajajar" → "reports", "catalogXYZ123" → "catalog"
        String cleanedSection = section.replaceAll("[^a-z-]", "");
        System.out.println("🧹 Sección limpia: " + cleanedSection);
        
        // Lista de secciones válidas en la plataforma
        String[] validSections = {
            "dashboard", "projects", "repositories", "apis", "catalog",
            "tickets", "reports", "users", "teams", "settings",
            "profile", "notifications", "forums", "documentation"
        };
        
        // Verificar si la sección limpia es válida
        boolean isSectionValid = false;
        for (String validSection : validSections) {
            if (validSection.equals(cleanedSection)) {
                isSectionValid = true;
                break;
            }
        }
        
        // Si la sección es válida, retornar la URL limpia (solo hasta la sección)
        if (isSectionValid) {
            String result = "/devportal/" + role + "/" + username + "/" + cleanedSection;
            System.out.println("✅ URL limpia con sección válida: " + result);
            return result;
        }
        
        // Si la sección no es válida, redirigir al dashboard
        String result = "/devportal/" + role + "/" + username + "/dashboard";
        System.out.println("⚠️ Sección inválida, redirigiendo a dashboard: " + result);
        return result;
    }
}