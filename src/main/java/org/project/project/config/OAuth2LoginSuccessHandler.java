package org.project.project.config;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.Rol;
import org.project.project.repository.UsuarioRepository;
import org.project.project.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        System.out.println("===== OAUTH2 LOGIN SUCCESS HANDLER EJECUTADO =====");
        System.out.println("Authentication class: " + authentication.getClass().getName());
        System.out.println("Authentication principal class: " + authentication.getPrincipal().getClass().getName());
        System.out.println("Authentication authorities: " + authentication.getAuthorities());
        System.out.println("Authentication name: " + authentication.getName());

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String providerUserId = oAuth2User.getName();
        String firstLogin = (String) oAuth2User.getAttribute("first_login");

        String userType = oAuth2User.getClass().getSimpleName();
        System.out.println("Tipo de usuario OAuth2: " + userType);

        String provider = null;
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            provider = ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            System.out.println("Proveedor detectado: " + provider);
        }

        System.out.println("OAuth2 login attempt - Email: " + email + ", Provider ID: " + providerUserId);
        System.out.println("Primera vez (String): " + firstLogin);
        System.out.println("Primera vez (Object): " + oAuth2User.getAttribute("first_login"));
        System.out.println("OAuth2 User attributes: " + oAuth2User.getAttributes());
        System.out.println("OAuth2 User authorities: " + oAuth2User.getAuthorities());

        if ("google".equals(provider) && email != null) {
            System.out.println("[GOOGLE OIDC] Verificando first_login espec?ficamente...");
            Object firstLoginAttr = oAuth2User.getAttribute("first_login");
            System.out.println("[GOOGLE OIDC] first_login attribute: " + firstLoginAttr + " (tipo: " + (firstLoginAttr != null ? firstLoginAttr.getClass() : "null") + ")");

            if (firstLoginAttr != null) {
                firstLogin = String.valueOf(firstLoginAttr);
                System.out.println("[GOOGLE OIDC] first_login convertido a String: " + firstLogin);
            }
        }

        if ("true".equals(firstLogin)) {
            System.out.println("PRIMERA VEZ con OAuth2/OIDC: " + email);
            System.out.println("REDIRIGIENDO A FORMULARIO complete-profile");

            Usuario usuarioTemporal = crearUsuarioTemporalParaFormulario(email, oAuth2User, providerUserId, provider);
            request.getSession().setAttribute("usuario", usuarioTemporal);
            request.getSession().setAttribute("oauth_provider", provider);

            response.sendRedirect("/auth/finish-profile");
            return;
        }

        System.out.println("Buscando usuario existente en BD con email: " + email);
        Usuario usuario = usuarioRepository.findByCorreo(email).orElse(null);

        if (usuario == null) {
            System.out.println("ERROR: Usuario deber?a existir pero no se encontr?: " + email);
            response.sendRedirect("/signin?error=oauth2_user_not_found");
            return;
        }

        System.out.println("Usuario EXISTENTE encontrado: " + usuario.getUsername() + " (ID: " + usuario.getUsuarioId() + ")");
        System.out.println("Roles actuales: " + usuario.getRoles().stream().map(rol -> rol.getNombreRol().name()).toList());

        // ✅ FORZAR ROL DEV para usuarios OAuth2
        boolean tienRolDev = usuario.getRoles().stream()
                .anyMatch(rol -> rol.getNombreRol() == org.project.project.model.entity.Rol.NombreRol.DEV);

        if (!tienRolDev) {
            System.out.println("⚠️ Usuario OAuth2 sin rol DEV. Asignando rol DEV automáticamente: " + email);
            
            // Buscar o crear el rol DEV
            Rol rolDev = rolRepository.findByRoleName(Rol.NombreRol.DEV)
                    .orElseThrow(() -> new RuntimeException("Rol DEV no encontrado en la base de datos"));
            
            // Asignar el rol DEV al usuario
            if (usuario.getRoles() == null) {
                usuario.setRoles(new HashSet<>());
            }
            usuario.getRoles().add(rolDev);
            
            // Guardar cambios
            usuarioRepository.save(usuario);
            System.out.println("✅ Rol DEV asignado exitosamente a: " + usuario.getUsername());
        }

        request.getSession().setAttribute("usuario", usuario);

        // ✅ CRÍTICO: Crear UserDetails con el username del usuario, NO el ID del proveedor OAuth2
        // El Dashboard espera que authentication.getName() devuelva el username
        Set<org.springframework.security.core.GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + rol.getNombreRol().name()))
                .collect(java.util.stream.Collectors.toSet());

        org.springframework.security.core.userdetails.User userDetails = 
                (org.springframework.security.core.userdetails.User) org.springframework.security.core.userdetails.User
                    .builder()
                    .username(usuario.getUsername())  // ✅ USAR EL USERNAME DEL USUARIO, NO EL EMAIL
                    .password(usuario.getHashedPassword() != null ? usuario.getHashedPassword() : "")
                    .authorities(authorities)
                    .build();

        // ✅ Crear nueva autenticación con UserDetails (NO OAuth2User)
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken newAuth = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails,  // ✅ UserDetails con username correcto
                        null,
                        authorities);
        
        newAuth.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                .buildDetails(request));

        // ✅ CRÍTICO: Actualizar el contexto de seguridad Y guardarlo en la sesión
        org.springframework.security.core.context.SecurityContext securityContext = 
                org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(newAuth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);
        
        // ✅ CRÍTICO: Guardar explícitamente el SecurityContext en la sesión HTTP
        request.getSession().setAttribute(
                org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext
        );

        System.out.println("✅ Authentication actualizada con username: " + usuario.getUsername());
        System.out.println("✅ Authentication.getName(): " + newAuth.getName());
        System.out.println("✅ Authorities: " + newAuth.getAuthorities());
        System.out.println("✅ SecurityContext guardado en sesión HTTP: " + request.getSession().getId());

        String role = "dev";
        String username = usuario.getUsername();

        String redirectUrl = "/devportal/" + role + "/" + username + "/dashboard";
        System.out.println("OAuth2 success - Redirecting to " + redirectUrl);
        
        // Redirección normal - el dashboard se encargará de limpiar el historial
        response.sendRedirect(redirectUrl);
    }

    private Usuario crearUsuarioTemporalParaFormulario(String email, OAuth2User oAuth2User, String providerUserId, String provider) {
        System.out.println("Creando usuario temporal para formulario complete-profile...");
        System.out.println("Proveedor: " + provider);

        Usuario usuarioTemporal = new Usuario();
        usuarioTemporal.setCorreo(email);
        usuarioTemporal.setIdProveedor(providerUserId);
        usuarioTemporal.setFechaCreacion(LocalDateTime.now());

        String fullName = oAuth2User.getAttribute("name");
        if (fullName != null && !fullName.isEmpty()) {
            String[] names = fullName.split("\\s+");
            if (names.length > 0) {
                usuarioTemporal.setNombreUsuario(names[0]);
            }
            if (names.length > 1) {
                usuarioTemporal.setApellidoPaterno(names[1]);
            }
            if (names.length > 2) {
                StringBuilder materno = new StringBuilder();
                for (int i = 2; i < names.length; i++) {
                    materno.append(names[i]).append(" ");
                }
                usuarioTemporal.setApellidoMaterno(materno.toString().trim());
            } else {
                usuarioTemporal.setApellidoMaterno("");
            }
        } else {
            usuarioTemporal.setNombreUsuario("Usuario");
            usuarioTemporal.setApellidoPaterno("OAuth2");
            usuarioTemporal.setApellidoMaterno("");
        }

        String suggestedUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9.-]", "_");
        usuarioTemporal.setUsername(suggestedUsername);

        String pictureUrl = oAuth2User.getAttribute("picture");
        if (pictureUrl != null) {
            usuarioTemporal.setFotoPerfil(pictureUrl);
        }

        usuarioTemporal.setEstadoUsuario(Usuario.EstadoUsuario.HABILITADO);
        usuarioTemporal.setDireccionUsuario("");
        usuarioTemporal.setDni("");

        System.out.println("Usuario temporal OAuth2 preparado para formulario: " + usuarioTemporal.getUsername() + " (" + usuarioTemporal.getCorreo() + ")");
        return usuarioTemporal;
    }
}
