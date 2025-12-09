package org.project.project.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class EventBasedLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ApplicationEventPublisher eventPublisher;

    public EventBasedLoginSuccessHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String username = authentication.getName();

        // Publicar evento para actualizar última conexión (asíncrono)
        eventPublisher.publishEvent(new UserLoginEvent(username));

        try {
            // Determinar redirección basada en autoridades
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority().replace("ROLE_", "").toLowerCase())
                    .orElse("dev");

            String redirectUrl = "/devportal/" + role + "/" + username + "/dashboard";
            System.out.println("✅ Login exitoso para " + username + ", redirigiendo a: " + redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            System.err.println("❌ Error en redirección: " + e.getMessage());
            response.sendRedirect("/dashboard");
        }
    }

    // Evento interno
    public static class UserLoginEvent {
        private final String username;

        public UserLoginEvent(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }
}