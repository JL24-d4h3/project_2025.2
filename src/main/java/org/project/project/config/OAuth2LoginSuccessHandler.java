package org.project.project.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.project.project.model.entity.Rol;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // Buscamos el usuario en nuestra base de datos
        Usuario usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la BD después de la autenticación OAuth2."));

        // Guardamos el usuario en la sesión HTTP para uso futuro
        request.getSession().setAttribute("usuario", usuario);

        // Lógica para determinar el rol y redirigir
        // Para este ejemplo, tomamos el primer rol que encontremos.
        // Puedes hacer esta lógica más robusta si un usuario tiene múltiples roles.
        String role = usuario.getRoles().stream()
                .findFirst()
                .map(Rol::getNombreRol)
                .orElse("default") // Un rol por defecto si no se encuentra ninguno
                .toLowerCase();

        String username = usuario.getUsername();

        String redirectUrl = "/devportal/" + role + "/" + username + "/dashboard";
        response.sendRedirect(redirectUrl);
    }
}
