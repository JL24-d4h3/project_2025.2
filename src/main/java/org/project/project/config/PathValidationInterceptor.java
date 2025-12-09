package org.project.project.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Interceptor para validar path variables y query parameters.
 * Redirige automáticamente a rutas válidas cuando detecta valores incorrectos.
 */
@Component
public class PathValidationInterceptor implements HandlerInterceptor {

    // Patrones de validación para path variables
    private static final Pattern PROJECT_ID_PATTERN = Pattern.compile("^\\d+$"); // Solo números para projectId
    private static final Pattern REPOSITORY_ID_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$"); // Permitir punto (.) para OAuth2 usernames
    private static final Pattern ROLE_PATTERN = Pattern.compile("^(dev|po|qa|sa)$");
    private static final Pattern TICKET_ID_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern API_ID_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern TEAM_ID_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern NODE_ID_PATTERN = Pattern.compile("^\\d+$");
    
    // Patrones de validación para query parameters comunes
    private static final Pattern REPOSITORY_TYPE_PATTERN = Pattern.compile("^(personal|colaborativo)$");
    private static final Pattern VISIBILITY_PATTERN = Pattern.compile("^(PUBLICO|PRIVADO)$");
    private static final Pattern PROJECT_TYPE_PATTERN = Pattern.compile("^(PERSONAL|GRUPAL|EMPRESA)$");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        String uri = request.getRequestURI();
        
        // Solo validar rutas de /devportal/
        if (!uri.startsWith("/devportal/")) {
            return true;
        }

        // Obtener path variables del request
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        
        if (pathVariables == null || pathVariables.isEmpty()) {
            return true;
        }

        // Validar role
        String role = pathVariables.get("role");
        if (role != null && !ROLE_PATTERN.matcher(role).matches()) {
            response.sendRedirect("/signin");
            return false;
        }

        // Validar username
        String username = pathVariables.get("username");
        if (username != null && !USERNAME_PATTERN.matcher(username).matches()) {
            response.sendRedirect("/signin");
            return false;
        }

        // Validar projectId (solo números)
        String projectId = pathVariables.get("projectId");
        if (projectId != null && !PROJECT_ID_PATTERN.matcher(projectId).matches()) {
            String redirectUrl = buildFallbackUrl(uri, role, username, "projects");
            response.sendRedirect(redirectUrl);
            return false;
        }
        
        // Validar teamId
        String teamId = pathVariables.get("teamId");
        if (teamId != null && !TEAM_ID_PATTERN.matcher(teamId).matches()) {
            String redirectUrl = buildFallbackUrl(uri, role, username, "projects/team-projects");
            response.sendRedirect(redirectUrl);
            return false;
        }
        
        // Validar nodeId
        String nodeId = pathVariables.get("nodeId");
        if (nodeId != null && !NODE_ID_PATTERN.matcher(nodeId).matches()) {
            String redirectUrl = buildFallbackUrl(uri, role, username, "projects");
            response.sendRedirect(redirectUrl);
            return false;
        }

        // Validar repositoryId
        String repositoryId = pathVariables.get("repositoryId");
        if (repositoryId != null && !REPOSITORY_ID_PATTERN.matcher(repositoryId).matches()) {
            String redirectUrl = buildFallbackUrl(uri, role, username, "repositories");
            response.sendRedirect(redirectUrl);
            return false;
        }

        // Validar ticketId
        String ticketId = pathVariables.get("ticketId");
        if (ticketId != null && !TICKET_ID_PATTERN.matcher(ticketId).matches()) {
            String redirectUrl = buildFallbackUrl(uri, role, username, "tickets");
            response.sendRedirect(redirectUrl);
            return false;
        }

        // Validar apiId
        String apiId = pathVariables.get("apiId");
        if (apiId != null && !API_ID_PATTERN.matcher(apiId).matches()) {
            String redirectUrl = buildFallbackUrl(uri, role, username, "apis");
            response.sendRedirect(redirectUrl);
            return false;
        }
        
        // =================== VALIDAR QUERY PARAMETERS ===================
        
        // Validar type parameter (para repository/create?type=...)
        String typeParam = request.getParameter("type");
        if (typeParam != null && uri.contains("/repository/create")) {
            if (!REPOSITORY_TYPE_PATTERN.matcher(typeParam).matches()) {
                // Remover el parámetro inválido y redirigir a la misma URL sin el parámetro
                String cleanUrl = uri.replace(request.getContextPath(), "");
                response.sendRedirect(cleanUrl);
                return false;
            }
        }

        return true;
    }

    /**
     * Construye URL de fallback para redirigir al usuario
     */
    private String buildFallbackUrl(String originalUri, String role, String username, String section) {
        if (role != null && username != null) {
            return "/devportal/" + role + "/" + username + "/" + section;
        }
        return "/signin";
    }
}
