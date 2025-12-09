package org.project.project.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Interceptor para validar rutas del DevPortal y redirigir a rutas válidas cuando se detecten URLs incorrectas
 *
 * Ejemplos de validación:
 * - /devportal/dev/user/repositories3124dfgfh → /devportal/dev/user/repositories
 * - /devportal/dev/user/apis/create14dthghd → /devportal/dev/user/apis/create
 * - /devportal/po/user/reports25fsssssf → /devportal/po/user/reports
 */
@Component
public class RouteValidationInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RouteValidationInterceptor.class);

    // Pattern para capturar: /devportal/{rol}/{username}/{path}
    private static final Pattern DEVPORTAL_PATTERN = Pattern.compile(
            "^/devportal/([a-z]+)/([a-zA-Z0-9._-]+)/(.+)$"
    );

    // Rutas válidas conocidas (sin parámetros adicionales)
    private static final String[] VALID_ROUTES = {
            "dashboard",
            "dashboard-panel",
            "manage-users",
            "impersonate-user",
            "manage-categories",
            "create-new-user",
            "invite-new-user",
            "repositories",
            "projects",
            "tickets",
            "apis",
            "reports",
            "catalog",
            "profile",
            "settings",
            "notifications",
            "teams",
            "platform-user-management",
            "test-environment",
            "chatbot-test",
            // API REST - Documentación
            "documentations",
            "user",
            "profile/photo",
            "profile/update",
            "profile/upload-photo",
            "profile/change-password"
    };

    // Rutas válidas con sub-paths (permiten continuación)
    private static final String[] VALID_ROUTES_WITH_SUBPATHS = {
            "apis/create",
            "apis/edit",
            "projects/P-**",
            "projects/P-**/detail",
            "projects/P-**/edit",
            "projects/P-**/overview",
            "projects/P-**/roles",
            "projects/P-**/roles/create-roles",
            "projects/P-**/roles/update-roles",
            "projects/P-**/roles/delete",
            "projects/P-**/members",
            "projects/P-**/members/remove",
            "projects/P-**/members/**/profile",
            "projects/P-**/invite-G",
            "projects/P-**/invite-E",
            "projects/P-**/accept-invitation/**",
            "projects/P-**/decline-invitation/**",
            "projects/P-**/create-repository",
            "projects/P-**/repositories",
            "projects/P-**/repositories/create",
            "projects/P-**/repositories/R-**",
            "projects/P-**/repositories/R-**/detail",
            "projects/P-**/repositories/R-**/edit",
            "projects/P-**/repositories/R-**/overview",
            "projects/P-**/files",
            "projects/P-**/files/**",
            "projects/P-**/files/N-**",
            "projects/create",
            "projects/create/personal",
            "projects/create/group",
            "projects/create/enterprise",
            "projects/edit",
            "projects/accept-invitation/**",
            "projects/decline-invitation/**",
            "repositories/R-**",
            "repositories/R-**/detail",
            "repositories/R-**/edit",
            "repositories/R-**/overview",
            "repositories/R-**/files",
            "repositories/R-**/files/**",
            "repositories/R-**/files/N-**",
            "repositories/create",
            "repositories/edit",
            "repositories/P-**/roles",
            "repositories/P-**/roles/create-roles",
            "repositories/P-**/roles/update-roles",
            "repositories/P-**/roles/delete",
            "repositories/P-**/invite",
            "repositories/P-**/members",
            "repositories/P-**/members/remove",
            "tickets/create",
            "tickets/edit",
            "tickets/T-**",
            "tickets/T-**/detail",
            "tickets/T-**/edit",
            "reports/create",
            "reports/view",
            "reports/R-**",
            "reports/R-**/detail",
            "reports/R-**/edit",
            "feedback",
            "feedback/list",
            "feedback/create",
            "feedback/F-**",
            "feedback/F-**/detail",
            "feedback/search-documentations",
            "catalog/view",
            "catalog/C-**",
            "catalog/C-**/detail",
            "catalog/api-**/v-**",
            "teams",
            "teams/created-at-P",
            "teams/created-at-R",
            "teams/team-**",
            "teams/team-**/edit",
            "platform-user-management/create-new-user",
            "platform-user-management/invite-new-user",
            "platform-user-management/view-user",
            "platform-user-management/view-user/**",
            "platform-user-management/edit-user/**",
            "toggle-estado",
            "complete-profile",
            "invitations/**",
            "invitations/accept",
            "invitations/decline",
            // ============================================================================
            // 🤖 API REST - CHATBOT ENDPOINTS
            // ============================================================================
            "api/chatbot/session",
            "api/chatbot/projects/user/**",
            "api/chatbot/repositories/user/**",
            "api/chatbot/apis/user/**",
            "api/chatbot/tickets/user/**",
            "api/chatbot/conversations",
            "api/chatbot/conversations/with-context",
            "api/chatbot/conversations/**",
            "api/chatbot/conversations/user/**",
            "api/chatbot/conversations/**/resolve",
            "api/chatbot/conversations/**/close",
            "api/chatbot/conversations/**/archive",
            "api/chatbot/conversations/**/link-ticket/**",
            "api/chatbot/conversations/status/**",
            "api/chatbot/conversations/topic/**",
            "api/chatbot/conversations/search",
            "api/chatbot/conversations/with-tickets",
            "api/chatbot/conversations/ticket/**",
            "api/chatbot/conversations/stats/user/**",
            "api/chatbot/conversations/stats/global",
            "api/chatbot/conversations/**/tokens",
            "api/chatbot/conversations/**/ai-model",
            "api/chatbot/conversations/**/title",
            "api/chatbot/conversations/**/topic",
            "api/chatbot/conversations/**/nosql-id",
            // ============================================================================
            // 🎫 API REST - TICKETS
            // ============================================================================
            "api/tickets",
            "api/tickets/public",
            "api/tickets/**",
            "api/tickets/user/**",
            "api/tickets/reported-by/**",
            "api/tickets/assigned-to/**",
            "api/tickets/project/**",
            "api/tickets/status/**",
            "api/tickets/stage/**",
            "api/tickets/priority/**",
            "api/tickets/type/**",
            "api/tickets/open",
            "api/tickets/closed",
            "api/tickets/search",
            "api/tickets/**/assign/**",
            "api/tickets/**/unassign",
            "api/tickets/**/status",
            "api/tickets/**/stage",
            "api/tickets/**/priority",
            "api/tickets/**/close",
            "api/tickets/**/resolve",
            "api/tickets/**/reject",
            "api/tickets/**/subject",
            "api/tickets/**/body",
            "api/tickets/**/link-project/**",
            "api/tickets/**/unlink-project",
            "api/tickets/stats/user/**",
            "api/tickets/stats/global",
            "api/tickets/stats/project/**",
            // ============================================================================
            // 👤 API REST - USUARIO
            // ============================================================================
            "user/**",
            "user/check-exists",
            "user/validate-field",
            // ============================================================================
            // 📚 API REST - DOCUMENTACIÓN
            // ============================================================================
            "documentations/**",
            "documentations/search",
            "documentations/category/**",
            "documentations/api/**",
            // ============================================================================
            // 📁 API REST - ARCHIVOS (Repositorios y Proyectos)
            // ============================================================================
            "api/repositories/**/files",
            "api/repositories/**/files/**",
            "api/repositories/**/folders",
            "api/projects/**/files",
            "api/projects/**/files/**",
            "api/projects/**/folders",
            "api/projects/**/files/upload",
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        logger.info("═══════════════════════════════════════════════════════════════════════════");
        logger.info("🎯 [RouteValidation] NUEVO REQUEST");
        logger.info("   Método: {} | URI: {}", method, requestURI);

        // Solo validar rutas de devportal
        if (!requestURI.startsWith("/devportal/")) {
            logger.debug("⏭️  [RouteValidation] Omitiendo validación - No es ruta /devportal/");
            return true;
        }

        // ============================================================================
        // 🔒 VALIDACIÓN DE IMPERSONACIÓN - PREVENIR NAVEGACIÓN A RUTAS DE SUPERADMIN
        // ============================================================================
        HttpSession session = request.getSession(false);
        if (session != null) {
            try {
                Boolean isImpersonating = (Boolean) session.getAttribute("impersonating");
                String impersonatedUsername = (String) session.getAttribute("impersonatedUsername");

                if (Boolean.TRUE.equals(isImpersonating)) {
                    logger.warn("🔒 [ImpersonationProtection] Usuario en modo impersonación detectado");
                    logger.warn("   Usuario impersonado: {}", impersonatedUsername);
                    logger.warn("   Intentando acceder a: {}", requestURI);

                    // Bloquear acceso a rutas de SuperAdmin (excepto finalizar impersonación)
                    if (requestURI.matches("^/devportal/sa/.*") &&
                            !requestURI.contains("/finalizar-impersonacion") &&
                            !requestURI.equals("/devportal/sa")) {

                        // Obtener el rol del usuario impersonado desde la sesión
                        String targetRole = determineRoleFromSession(session);
                        String redirectUrl = "/devportal/" + targetRole + "/" + impersonatedUsername + "/dashboard";

                        logger.error("🚫 [ImpersonationProtection] ACCESO BLOQUEADO A RUTA DE SUPERADMIN");
                        logger.error("   Ruta bloqueada: {}", requestURI);
                        logger.error("   Redirigiendo a: {}", redirectUrl);
                        logger.info("═══════════════════════════════════════════════════════════════════════════");

                        response.sendRedirect(redirectUrl);
                        return false; // Bloquear acceso
                    }
                }
            } catch (IllegalStateException e) {
                logger.warn("⚠️  [ImpersonationProtection] Sesión invalidada, continuando sin validación de impersonación");
            }
        }
        // ============================================================================

        if ("POST".equalsIgnoreCase(method) ||
                "PUT".equalsIgnoreCase(method) ||
                "DELETE".equalsIgnoreCase(method)) {
            logger.debug("⏭️  [RouteValidation] Omitiendo validación para método {}: {}", method, requestURI);
            return true;
        }
        // No validar recursos estáticos
        if (requestURI.contains("/static/") ||
                requestURI.contains("/css/") ||
                requestURI.contains("/js/") ||
                requestURI.contains("/img/") ||
                requestURI.contains("/assets/") ||
                requestURI.contains("/uploads/") ||
                requestURI.endsWith(".css") ||
                requestURI.endsWith(".js") ||
                requestURI.endsWith(".jpg") ||
                requestURI.endsWith(".png") ||
                requestURI.endsWith(".gif") ||
                requestURI.endsWith(".ico")) {
            logger.debug("⏭️  [RouteValidation] Omitiendo validación - Recurso estático detectado");
            return true;
        }

        // No validar APIs (endpoints que devuelven JSON)
        if (requestURI.contains("/api/")) {
            logger.debug("⏭️  [RouteValidation] Omitiendo validación - Endpoint API detectado");
            return true;
        }

        logger.debug("🔍 [RouteValidation] Validando ruta: {}", requestURI);

        Matcher matcher = DEVPORTAL_PATTERN.matcher(requestURI);

        if (matcher.matches()) {
            String rol = matcher.group(1);
            String username = matcher.group(2);
            String path = matcher.group(3);

            logger.info("📋 [RouteValidation] Extracted | Rol: '{}' | Username: '{}' | Path: '{}'", rol, username, path);

            // Validar la ruta
            String validPath = validateAndCorrectPath(path);

            if (!validPath.equals(path)) {
                // Ruta inválida detectada - redirigir a la ruta válida
                String correctedURL = "/devportal/" + rol + "/" + username + "/" + validPath;

                logger.warn("⚠️ [RouteValidation] RUTA INVÁLIDA DETECTADA");
                logger.warn("   Original URL: {}", requestURI);
                logger.warn("   Corrected URL: {}", correctedURL);
                logger.info("═══════════════════════════════════════════════════════════════════════════");

                response.sendRedirect(correctedURL);
                return false; // Detener procesamiento
            } else {
                logger.info("✅ [RouteValidation] RUTA VÁLIDA - Permitiendo acceso");
            }
        } else {
            logger.warn("⚠️ [RouteValidation] No coincide con patrón /devportal: {}", requestURI);
        }

        logger.info("═══════════════════════════════════════════════════════════════════════════");
        return true; // Continuar con la petición
    }

    /**
     * Valida y corrige una ruta removiendo caracteres adicionales no válidos
     */
    private String validateAndCorrectPath(String path) {
        logger.info("🔬 [DEBUG] Iniciando validación de ruta: '{}'", path);

        // Remover query string si existe
        if (path.contains("?")) {
            String before = path;
            path = path.substring(0, path.indexOf("?"));
            logger.info("🔬 [DEBUG] Query string detectado. Antes: '{}' → Después: '{}'", before, path);
        }

        // Verificar si es una ruta válida exacta
        logger.info("🔬 [DEBUG] Verificando contra {} rutas válidas exactas", VALID_ROUTES.length);
        for (String validRoute : VALID_ROUTES) {
            if (path.equals(validRoute)) {
                logger.info("✅ [RouteValidation] Ruta válida exacta encontrada: '{}'", path);
                return path; // Ruta válida exacta
            }
        }

        // Verificar si es una ruta válida con sub-path
        logger.info("🔬 [DEBUG] Verificando contra {} rutas con sub-paths", VALID_ROUTES_WITH_SUBPATHS.length);
        for (String validRouteWithSubpath : VALID_ROUTES_WITH_SUBPATHS) {
            // Ruta exacta
            if (path.equals(validRouteWithSubpath)) {
                logger.info("✅ [RouteValidation] Ruta exacta con sub-path encontrada: '{}'", path);
                return path;
            }

            // Rutas con patrones wildcard
            if (validRouteWithSubpath.contains("**")) {
                // Crear patrón más flexible: projects/P-** puede ser projects/P-123 o projects/P-123/roles
                String basePattern = validRouteWithSubpath.replaceAll("\\*\\*", "[a-zA-Z0-9-_.]+");
                logger.debug("🔬 [DEBUG] Probando patrón wildcard: '{}' → patrón regex: '{}'", validRouteWithSubpath, basePattern);

                // Permitir la ruta base exacta
                if (path.matches("^" + basePattern + "$")) {
                    logger.info("✅ [RouteValidation] Coincidencia exacta con patrón wildcard: '{}' matchea con '{}'", path, validRouteWithSubpath);
                    return path;
                }

                // Permitir sub-rutas dentro del patrón
                if (path.matches("^" + basePattern + "/.*$")) {
                    logger.info("✅ [RouteValidation] Coincidencia con sub-ruta en patrón wildcard: '{}' matchea con '{}/**'", path, validRouteWithSubpath);
                    return path;
                }
            }

            // Verificar si la ruta comienza con un sub-path válido y tiene parámetros adicionales válidos
            if (path.startsWith(validRouteWithSubpath + "/")) {
                String remainder = path.substring(validRouteWithSubpath.length() + 1);
                logger.debug("🔬 [DEBUG] Sub-ruta detectada en '{}': base='{}', remainder='{}'", path, validRouteWithSubpath, remainder);

                // Si el resto es un número, ID con patrón válido
                if (remainder.matches("^[0-9]+$") ||
                        remainder.matches("^[0-9]+-profile$") ||
                        remainder.matches("^[a-zA-Z0-9-_/]+$")) {
                    logger.info("✅ [RouteValidation] Remainder válido en sub-ruta: '{}'", remainder);
                    return path;
                }
            }
        }

        // Si la ruta tiene caracteres extraños al final, intentar limpiarla
        logger.info("🔬 [DEBUG] Buscando rutas válidas con caracteres extraños...");
        for (String validRoute : VALID_ROUTES) {
            if (path.startsWith(validRoute)) {
                String remainder = path.substring(validRoute.length());
                logger.debug("🔬 [DEBUG] Ruta comienza con '{}', remainder: '{}'", validRoute, remainder);

                // Si no hay nada más, es válida
                if (remainder.isEmpty()) {
                    logger.info("✅ [RouteValidation] Ruta válida (sin remainder): '{}'", validRoute);
                    return validRoute;
                }

                // Si hay un slash seguido de contenido válido
                if (remainder.startsWith("/")) {
                    String afterSlash = remainder.substring(1);
                    logger.debug("🔬 [DEBUG] Contenido después de slash: '{}'", afterSlash);

                    if (afterSlash.matches("^[0-9]+$") ||
                            afterSlash.matches("^[0-9]+-profile$") ||
                            afterSlash.matches("^view-[0-9]+$") ||
                            afterSlash.matches("^edit-[0-9]+$") ||
                            afterSlash.matches("^create$") ||
                            afterSlash.matches("^edit$") ||
                            afterSlash.matches("^[a-zA-Z0-9-_/]+$")) {
                        logger.info("✅ [RouteValidation] Contenido válido después de slash: '{}'", afterSlash);
                        return path;
                    }
                }

                // Si hay basura, limpiar
                if (remainder.matches(".*[0-9]+[a-zA-Z]+.*") ||
                        remainder.matches(".*[a-zA-Z]+[0-9]+.*")) {
                    logger.warn("⚠️ [RouteValidation] Detectada basura en ruta: '{}' - Limpiando a '{}'", path, validRoute);
                    return validRoute;
                }
            }
        }

        // Lo mismo para rutas con sub-paths
        logger.info("🔬 [DEBUG] Buscando rutas con sub-paths con caracteres extraños...");
        for (String validRouteWithSubpath : VALID_ROUTES_WITH_SUBPATHS) {
            if (path.startsWith(validRouteWithSubpath)) {
                String remainder = path.substring(validRouteWithSubpath.length());
                logger.debug("🔬 [DEBUG] Ruta con sub-path comienza con '{}', remainder: '{}'", validRouteWithSubpath, remainder);

                if (remainder.isEmpty()) {
                    logger.info("✅ [RouteValidation] Ruta válida con sub-path (sin remainder): '{}'", validRouteWithSubpath);
                    return validRouteWithSubpath;
                }

                // Si hay basura al final, limpiar
                if (remainder.matches(".*[0-9]+[a-zA-Z]+.*") ||
                        remainder.matches(".*[a-zA-Z]+[0-9]+.*")) {
                    logger.warn("⚠️ [RouteValidation] Detectada basura en ruta con sub-path: '{}' - Limpiando a '{}'", path, validRouteWithSubpath);
                    return validRouteWithSubpath;
                }
            }
        }

        // Si no se pudo validar, retornar "dashboard" por defecto
        logger.error("❌ [RouteValidation] RUTA NO RECONOCIDA: '{}' - Redirigiendo a 'dashboard'", path);
        logger.error("❌ [RouteValidation] Rutas válidas exactas: {}", java.util.Arrays.toString(VALID_ROUTES));
        logger.error("❌ [RouteValidation] Rutas con sub-paths válidas: {}", java.util.Arrays.toString(VALID_ROUTES_WITH_SUBPATHS));
        return "dashboard";
    }

    /**
     * Determina el rol del usuario impersonado desde la sesión
     * Busca en el contexto de seguridad las autoridades del usuario
     */
    private String determineRoleFromSession(jakarta.servlet.http.HttpSession session) {
        try {
            // Intentar obtener el contexto de seguridad desde la sesión
            org.springframework.security.core.context.SecurityContext securityContext =
                    (org.springframework.security.core.context.SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");

            if (securityContext != null && securityContext.getAuthentication() != null) {
                // Obtener la primera autoridad (rol) del usuario
                String authority = securityContext.getAuthentication().getAuthorities().stream()
                        .findFirst()
                        .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                        .orElse("dev");

                logger.debug("🔍 [ImpersonationProtection] Rol detectado desde SecurityContext: {}", authority);
                return authority.toLowerCase();
            }
        } catch (Exception e) {
            logger.warn("⚠️  [ImpersonationProtection] Error al obtener rol desde sesión: {}", e.getMessage());
        }

        // Fallback: retornar 'dev' por defecto
        logger.debug("🔍 [ImpersonationProtection] No se pudo determinar rol, usando 'dev' por defecto");
        return "dev";
    }
}
