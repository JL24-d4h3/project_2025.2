package org.project.project.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DebugFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        // Solo loguear requests importantes, no recursos estáticos
        if (!path.contains(".css") && !path.contains(".js") && !path.contains(".png") &&
                !path.contains(".jpg") && !path.contains(".ico") && !path.contains("/assets/")) {

            System.out.println("🌐 [DebugFilter] REQUEST: " + httpRequest.getMethod() + " " + path);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                System.out.println("👤 [DebugFilter] Authentication: " + auth.getClass().getSimpleName());
                System.out.println("🏷️  [DebugFilter] Principal: " + auth.getName());
                System.out.println("⚡ [DebugFilter] Authorities: " + auth.getAuthorities());
                System.out.println("🔐 [DebugFilter] Authenticated: " + auth.isAuthenticated());
            } else {
                System.out.println("❌ [DebugFilter] NO AUTHENTICATION - Usuario anónimo");
            }

            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                System.out.println("📦 [DebugFilter] Session ID: " + session.getId());
                if (session.getAttribute("usuario") != null) {
                    System.out.println("👤 [DebugFilter] Session usuario: " + session.getAttribute("usuario"));
                }
            } else {
                System.out.println("📦 [DebugFilter] NO SESSION");
            }
            System.out.println("---");
        }

        chain.doFilter(request, response);
    }
}