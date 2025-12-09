package org.project.project.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro para prevenir caché de páginas autenticadas y de autenticación.
 * Evita que el usuario pueda usar el botón "Atrás" del navegador
 * para volver a páginas anteriores después del login.
 *
 * MEJORADO: Aplica headers anti-caché SUPER ESTRICTOS cuando hay un usuario autenticado
 * o cuando se detecta impersonación activa
 */
@Component
public class NoCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();

        // Verificar si hay usuario autenticado
        boolean isAuthenticated = false;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            isAuthenticated = auth != null &&
                    auth.isAuthenticated() &&
                    !(auth instanceof AnonymousAuthenticationToken);
        } catch (Exception e) {
            // Error al obtener autenticación - ignorar
        }

        // Verificar si hay impersonación activa
        boolean isImpersonating = false;
        try {
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                Boolean impersonatingAttr = (Boolean) session.getAttribute("impersonating");
                isImpersonating = Boolean.TRUE.equals(impersonatingAttr);
            }
        } catch (IllegalStateException e) {
            // Sesión invalidada - ignorar
        }

        // Aplicar headers anti-caché a:
        // 1. Rutas autenticadas (/devportal/*)
        // 2. Páginas de autenticación (/signin, /signup, /login)
        if (uri.startsWith("/devportal/") ||
                uri.equals("/dashboard") ||
                uri.equals("/signin") ||
                uri.equals("/signup") ||
                uri.startsWith("/login") ||
                uri.startsWith("/oauth2/") ||
                uri.startsWith("/auth/")) {

            if (isAuthenticated || isImpersonating) {
                // 🔒 HEADERS SUPER ESTRICTOS PARA USUARIOS AUTENTICADOS
                // Prevenir CUALQUIER tipo de caché del navegador
                httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private, max-age=0, s-maxage=0, proxy-revalidate");
                httpResponse.setHeader("Pragma", "no-cache");
                httpResponse.setDateHeader("Expires", -1); // Expirado en el pasado
                httpResponse.setHeader("X-Content-Type-Options", "nosniff");
                httpResponse.setHeader("X-Frame-Options", "DENY"); // Prevenir iframe
                httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
                // ⚠️ REMOVIDO Clear-Site-Data porque borra las cookies de sesión (JSESSIONID)
                // httpResponse.setHeader("Clear-Site-Data", "\"cache\", \"storage\"");

                String mode = isImpersonating ? "Modo Impersonación" : "Usuario Autenticado";
                System.out.println("🔒 [NoCacheFilter] Headers anti-caché ESTRICTOS aplicados (" + mode + ") - URI: " + uri);
            } else {
                // Headers normales anti-caché para páginas de autenticación sin usuario autenticado
                httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private, max-age=0");
                httpResponse.setHeader("Pragma", "no-cache");
                httpResponse.setDateHeader("Expires", 0);
                httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            }
        }

        chain.doFilter(request, response);
    }
}

