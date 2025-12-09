package org.project.project.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ImpersonationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(false);

        // Verificar que la sesión es válida antes de acceder a atributos
        if (session != null) {
            try {
                // Verificar si está en modo impersonación de forma segura
                Boolean isImpersonating = (Boolean) session.getAttribute("impersonating");
                if (Boolean.TRUE.equals(isImpersonating)) {
                    // Recuperar el contexto de seguridad guardado
                    SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
                    if (securityContext != null) {
                        SecurityContextHolder.setContext(securityContext);
                    }
                }
            } catch (IllegalStateException e) {
                // Sesión invalidada - continuar sin impersonación
                System.out.println("🚨 [ImpersonationFilter] Sesión invalidada detectada, continuando sin impersonación");
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Guardar el contexto actual en la sesión si estamos impersonando (solo si sesión válida)
            if (session != null) {
                try {
                    Boolean isImpersonating = (Boolean) session.getAttribute("impersonating");
                    if (Boolean.TRUE.equals(isImpersonating)) {
                        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                    }
                } catch (IllegalStateException e) {
                    // Sesión invalidada - ignorar
                    System.out.println("🚨 [ImpersonationFilter] No se puede guardar contexto, sesión invalidada");
                }
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Inicialización del filtro
    }

    @Override
    public void destroy() {
        // Limpieza del filtro
    }
}