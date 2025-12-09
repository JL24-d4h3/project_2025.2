package org.project.project.config;

import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // Log solo requests relevantes para autenticación
        if (requestURI.contains("/auth/login") || requestURI.contains("/signin") ||
                requestURI.contains("/dashboard") || requestURI.contains("/devportal")) {

            System.out.println("🌐 [AuthLoggingFilter] " + method + " " + requestURI);

            if ("/auth/login".equals(requestURI) && "POST".equals(method)) {
                String username = httpRequest.getParameter("usernameOrEmail");
                String password = httpRequest.getParameter("password");
                System.out.println("🔐 [AuthLoggingFilter] Intento de login - Usuario: " + username +
                        ", Password length: " + (password != null ? password.length() : "null"));
            }
        }

        chain.doFilter(request, response);

        // Log response status for auth-related requests
        if (requestURI.contains("/auth/login") || requestURI.contains("/signin")) {
            System.out.println("📤 [AuthLoggingFilter] Response status para " + requestURI + ": " + httpResponse.getStatus());
        }
    }
}