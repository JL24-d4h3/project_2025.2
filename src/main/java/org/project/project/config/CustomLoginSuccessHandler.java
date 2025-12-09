package org.project.project.config;

import org.project.project.model.entity.Usuario;
import org.project.project.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    public CustomLoginSuccessHandler(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        System.out.println("🎉 [CustomLoginSuccessHandler] Login exitoso iniciado para: " + authentication.getName());
        System.out.println("🔑 [CustomLoginSuccessHandler] Tipo de autenticación: " + authentication.getClass().getSimpleName());
        System.out.println("🏷️ [CustomLoginSuccessHandler] Autoridades: " + authentication.getAuthorities());

        // Determinar redirección basada en autoridades de Spring Security (no lazy)
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> {
                    String roleStr = authority.getAuthority().replace("ROLE_", "").toLowerCase();
                    System.out.println("🎭 [CustomLoginSuccessHandler] Rol detectado: " + roleStr);
                    return roleStr;
                })
                .orElse("dev");

        // Actualizar última conexión del usuario (asíncrono para evitar lazy loading issues)
        try {
            String username = authentication.getName();
            System.out.println("🔍 [CustomLoginSuccessHandler] Buscando usuario: " + username);

            Usuario usuario = userService.buscarPorUsernameOEmail(username);
            if (usuario != null) {
                System.out.println("✅ [CustomLoginSuccessHandler] Usuario encontrado, actualizando última conexión");
                usuario.setUltimaConexion(LocalDateTime.now());
                usuario.setActividadUsuario(Usuario.ActividadUsuario.ACTIVO);
                userService.guardarUsuario(usuario);
            }
        } catch (Exception e) {
            System.err.println("❌ [CustomLoginSuccessHandler] Error actualizando última conexión: " + e.getMessage());
            // No interrumpir el flujo de login por este error
        }

        // Redirección específica por rol
        String redirectUrl;
        if ("sa".equals(role)) {
            // SA tiene su propio dashboard en SystemAdministratorController
            redirectUrl = "/devportal/sa/" + authentication.getName() + "/dashboard";
            System.out.println("� [CustomLoginSuccessHandler] SA detectado, redirigiendo a: " + redirectUrl);
        } else {
            // Otros roles usan el DashboardController
            redirectUrl = "/devportal/" + role + "/" + authentication.getName() + "/dashboard";
            System.out.println("🚀 [CustomLoginSuccessHandler] Redirigiendo a: " + redirectUrl);
        }

        response.sendRedirect(redirectUrl);
    }
}
