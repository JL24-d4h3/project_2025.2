package org.project.project.service;

import jakarta.servlet.http.HttpSession;
import org.project.project.model.entity.Rol;
import org.project.project.model.entity.Usuario;
import org.project.project.model.dto.LoginRequest;
import org.project.project.model.dto.SignupRequest;
import org.project.project.repository.RolRepository;
import org.project.project.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthService(AuthenticationManager authenticationManager,
                       RolRepository rolRepository,
                       UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       UserService userService) {
        this.authenticationManager = authenticationManager;
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    /**
     * Maneja el proceso de inicio de sesión tradicional
     */
    @Transactional
    public AuthResponse login(LoginRequest loginRequest, HttpSession session) {
        try {
            // Intentar autenticar al usuario
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword()
                )
            );

            // Establecer la autenticación en el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Obtener el usuario y actualizar su información
            Usuario usuario = userService.findByUsernameOrEmail(loginRequest.getUsernameOrEmail());
            usuario.setUltimaConexion(LocalDateTime.now());
            usuario.setActividadUsuario(Usuario.ActividadUsuario.ACTIVO);
            usuarioRepository.save(usuario);

            // Guardar el usuario en la sesión
            session.setAttribute("usuario", usuario);

            // Construir la respuesta
            return AuthResponse.builder()
                .success(true)
                .redirectUrl(determinarUrlRedireccion(usuario))
                .user(usuario)
                .build();

        } catch (AuthenticationException e) {
            return AuthResponse.builder()
                .success(false)
                .error("Credenciales inválidas")
                .build();
        }
    }

    /**
     * Maneja el proceso de registro de nuevos usuarios
     */
    @Transactional
    public AuthResponse signup(SignupRequest signupRequest) {
        try {
            // Verificar si el usuario ya existe
            if (userService.existsByUsernameOrEmail(signupRequest.getUsername(), signupRequest.getEmail())) {
                return AuthResponse.builder()
                    .success(false)
                    .error("El usuario o correo electrónico ya está en uso")
                    .build();
            }

            // Crear nuevo usuario
            Usuario usuario = new Usuario();
            usuario.setUsername(signupRequest.getUsername());
            usuario.setCorreo(signupRequest.getEmail());
            usuario.setNombreUsuario(signupRequest.getNombre());
            usuario.setApellidoPaterno(signupRequest.getApellidoPaterno());
            usuario.setApellidoMaterno(signupRequest.getApellidoMaterno());
            usuario.setDni(signupRequest.getDni());
            usuario.setDireccionUsuario(signupRequest.getDireccion());
            usuario.setHashedPassword(passwordEncoder.encode(signupRequest.getPassword()));
            usuario.setFechaCreacion(LocalDateTime.now());
            usuario.setEstadoUsuario(Usuario.EstadoUsuario.HABILITADO);
            usuario.setActividadUsuario(Usuario.ActividadUsuario.ACTIVO);
            usuario.setAccesoUsuario(Usuario.AccesoUsuario.SI);
            usuario.setCodigoUsuario(generateUniqueUserCode());

            // Asignar rol por defecto (DEV)
            Set<Rol> roles = new HashSet<>();
            roles.add(rolRepository.findByNombreRol("DEV")
                .orElseThrow(() -> new RuntimeException("Error: Rol DEV no encontrado.")));
            usuario.setRoles(roles);

            // Guardar usuario
            usuarioRepository.save(usuario);

            return AuthResponse.builder()
                .success(true)
                .message("Usuario registrado exitosamente")
                .redirectUrl("/signin")
                .build();

        } catch (Exception e) {
            return AuthResponse.builder()
                .success(false)
                .error("Error al registrar el usuario: " + e.getMessage())
                .build();
        }
    }

    /**
     * Maneja el proceso de cierre de sesión
     */
    public void logout(HttpSession session) {
        // Obtener el usuario actual
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario != null) {
            // Actualizar última actividad
            usuario.setActividadUsuario(Usuario.ActividadUsuario.INACTIVO);
            usuario.setUltimaConexion(LocalDateTime.now());
            usuarioRepository.save(usuario);
        }

        // Limpiar el contexto de seguridad y la sesión
        SecurityContextHolder.clearContext();
        session.invalidate();
    }

    /**
     * Determina la URL de redirección basada en el rol del usuario
     */
    private String determinarUrlRedireccion(Usuario usuario) {
        String role = usuario.getRoles().stream()
            .findFirst()
            .map(rol -> rol.getNombreRol().toLowerCase())
            .orElse("dev");

        return "/devportal/" + role + "/" + usuario.getUsername() + "/dashboard";
    }

    /**
     * Genera un código único para nuevos usuarios
     */
    private String generateUniqueUserCode() {
        String code;
        do {
            code = "USR" + String.format("%06d", (int)(Math.random() * 1000000));
        } while (usuarioRepository.existsByCodigoUsuario(code));
        return code;
    }
}

