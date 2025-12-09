package org.project.project.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.project.project.model.entity.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class OidcUserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // Permitir acceso a recursos estáticos, signin, signup y auth endpoints
        if (requestURI.startsWith("/assets/") ||
                requestURI.startsWith("/css/") ||
                requestURI.startsWith("/js/") ||
                requestURI.startsWith("/images/") ||
                requestURI.startsWith("/favicon.ico") ||
                requestURI.equals("/signin") ||
                requestURI.equals("/signup") ||
                requestURI.startsWith("/auth/") ||
                requestURI.equals("/") ||
                requestURI.startsWith("/oauth2/") ||
                requestURI.startsWith("/login/oauth2/")) {
            return true;
        }

        // Verificar si hay un usuario OAuth2 en proceso de completar perfil
        HttpSession session = request.getSession(false);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (session != null && authentication instanceof OAuth2AuthenticationToken) {
            Usuario transientUsuario = (Usuario) session.getAttribute("usuario");

            // Si hay un usuario transiente (sin ID persistente), está completando perfil
            if (transientUsuario != null && transientUsuario.getUsuarioId() == null) {
                System.out.println("🚫 INTERCEPTOR ESTRICTO: Usuario en proceso de completar perfil intentó acceder a: " + requestURI);
                System.out.println("🔒 Forzando permanencia en complete-profile...");
                response.sendRedirect("/auth/finish-profile");
                return false;
            }
        }

        // Interceptar cualquier URL que contenga "oidc_user" (caso adicional de seguridad)
        if (requestURI.contains("oidc_user")) {
            System.out.println("🚫 INTERCEPTOR: Bloqueando acceso a URL con oidc_user: " + requestURI);

            if (session != null && authentication instanceof OAuth2AuthenticationToken) {
                Usuario transientUsuario = (Usuario) session.getAttribute("usuario");

                if (transientUsuario != null && transientUsuario.getUsuarioId() == null) {
                    System.out.println("✅ Usuario en proceso de complete-profile, redirigiendo...");
                    response.sendRedirect("/auth/finish-profile");
                    return false;
                }
            }

            System.out.println("❌ Usuario no válido para oidc_user, redirigiendo a signin");
            response.sendRedirect("/signin?error=invalid_session");
            return false;
        }

        return true;
    }
}