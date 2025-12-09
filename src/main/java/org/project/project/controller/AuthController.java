package org.project.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.project.project.model.dto.LoginRequest;
import org.project.project.model.dto.SignupRequest;
import org.project.project.model.entity.Rol;
import org.project.project.model.entity.Token;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.RolRepository;
import org.project.project.service.EmailService;
import org.project.project.service.TokenService;
import org.project.project.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          PasswordEncoder passwordEncoder,
                          RolRepository rolRepository) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.rolRepository = rolRepository;
    }

    @GetMapping("/signin")
    public String showLoginForm(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String fromDashboard,
                                Model model,
                                HttpServletResponse response) {
        System.out.println("📋 [AuthController] Mostrando formulario de login. Error: " + error);

        // 🔒 Headers de cache-control para prevenir cache de página de login
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🔍 [AuthController] Estado de autenticación actual: " +
                (authentication != null ? authentication.getName() + " (" + authentication.getClass().getSimpleName() + ")" : "null"));

        // ❌ NO REDIRIGIR si viene desde dashboard (prevenir bucle infinito)
        if ("true".equals(fromDashboard)) {
            System.out.println("⚠️ [AuthController] Detectado bucle desde dashboard - mostrando login");
            model.addAttribute("error", "Error al cargar el dashboard. Por favor, vuelve a iniciar sesión.");
            model.addAttribute("loginRequest", new LoginRequest());
            return "signin";
        }

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            System.out.println("↩️ [AuthController] Usuario ya autenticado, redirigiendo a dashboard");

            // ✅ Obtener rol y username para redirección correcta
            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority().replace("ROLE_", "").toLowerCase())
                    .orElse("dev");

            String redirectUrl = "/devportal/" + role + "/" + username + "/dashboard";
            System.out.println("✅ [AuthController] Redirigiendo usuario autenticado a: " + redirectUrl);

            // Redirección normal - el dashboard se encargará de limpiar el historial
            return "redirect:" + redirectUrl;
        }

        if (error != null) {
            System.out.println("❌ [AuthController] Mostrando error de login: " + error);
            model.addAttribute("error", "Credenciales inválidas");
        }

        model.addAttribute("loginRequest", new LoginRequest());
        System.out.println("📝 [AuthController] Mostrando página de signin");
        return "signin";
    }

    @PostMapping("/signup")
    public String processRegistration(SignupRequest signupRequest, RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            if (userService.existePorUsernameOEmail(signupRequest.getUsername(), signupRequest.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "El usuario o correo ya existe");
                return "redirect:/signup";
            }

            Usuario usuario = new Usuario();
            usuario.setUsername(signupRequest.getUsername());
            usuario.setNombreUsuario(signupRequest.getNombre());
            usuario.setApellidoPaterno(signupRequest.getApellidoPaterno());
            usuario.setApellidoMaterno(signupRequest.getApellidoMaterno());
            usuario.setDni(signupRequest.getDni());
            usuario.setCorreo(signupRequest.getEmail());
            usuario.setDireccionUsuario(signupRequest.getDireccion());
            usuario.setHashedPassword(passwordEncoder.encode(signupRequest.getPassword()));

            // Guardar usuario con código de verificación
            userService.guardarUsuarioConCodigo(usuario);

            // Guardar email en sesión para el flujo de verificación
            session.setAttribute("registrationEmail", signupRequest.getEmail());

            System.out.println("📝 Usuario registrado exitosamente: " + signupRequest.getEmail());
            System.out.println("🔄 Redirigiendo a /verify-registration?email=" + signupRequest.getEmail());

            return "redirect:/verify-registration?email=" + signupRequest.getEmail();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al intentar registrar al usuario: " + e.getMessage());
            return "redirect:/signup";
        }
    }

    @GetMapping("/signup")
    public String showRegisterForm(@RequestParam(required = false) String error,
                                   Model model,
                                   HttpServletResponse response) {
        // 🔒 Headers de cache-control para prevenir cache de página de registro
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            System.out.println("↩️ [AuthController] Usuario ya autenticado en signup, redirigiendo a dashboard");

            // ✅ Obtener rol y username para redirección correcta
            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority().replace("ROLE_", "").toLowerCase())
                    .orElse("dev");

            String redirectUrl = "/devportal/" + role + "/" + username + "/dashboard";
            System.out.println("✅ [AuthController] Redirigiendo usuario autenticado a: " + redirectUrl);

            // Redirección normal - el dashboard se encargará de limpiar el historial
            return "redirect:" + redirectUrl;
        }

        if (error != null) {
            model.addAttribute("error", "Credenciales inválidas");
        }
        model.addAttribute("signupRequest", new SignupRequest());
        return "signup";
    }

    /* --------------------- INVITATION FLOW ---------------------- */

    @GetMapping("/complete-profile")
    public String showCompleteProfileFromInvitation(@RequestParam(value = "token", required = false) String token,
                                                    Model model,
                                                    HttpSession session,
                                                    HttpServletResponse response) {
        System.out.println("🎟️ [AuthController] Procesando invitación con token: " + (token != null ? token.substring(0, Math.min(8, token.length())) + "..." : "NULL"));

        // Headers de seguridad
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // ✅ FIX: Validar que token no sea nulo
        if (token == null || token.trim().isEmpty()) {
            System.out.println("❌ Token de invitación faltante");
            model.addAttribute("error", "Token de invitación no proporcionado. Por favor verifica el enlace.");
            return "signin";
        }

        try {
            // Validar token de invitación
            Token validToken = tokenService.validarTokenInvitacion(token);
            if (validToken == null) {
                System.out.println("❌ Token de invitación inválido o expirado");
                model.addAttribute("error", "El enlace de invitación es inválido o ha expirado.");
                return "signin";
            }

            Usuario usuario = validToken.getUsuario();
            System.out.println("✅ Token válido para usuario: " + usuario.getCorreo());

            // Verificar que el usuario aún necesita completar el perfil
            if (usuario.getAccesoUsuario() == Usuario.AccesoUsuario.SI) {
                System.out.println("⚠️ Usuario ya completó su perfil");
                model.addAttribute("info", "Tu cuenta ya está activada. Puedes iniciar sesión normalmente.");
                return "signin";
            }

            // Preparar datos para el formulario
            model.addAttribute("usuario", usuario);
            model.addAttribute("token", token);
            model.addAttribute("isInvitation", true);

            // Obtener el rol asignado
            String rolAsignado = usuario.getRoles().stream()
                    .findFirst()
                    .map(r -> r.getNombreRol().getDescripcion())
                    .orElse("Usuario");
            model.addAttribute("rolAsignado", rolAsignado);

            System.out.println("✅ Mostrando formulario de completar perfil por invitación");
            return "complete-profile-invitation";

        } catch (Exception e) {
            System.out.println("❌ Error procesando invitación: " + e.getMessage());
            model.addAttribute("error", "Error al procesar la invitación. Contacta al administrador.");
            return "signin";
        }
    }

    @PostMapping("/complete-profile")
    public String processCompleteProfileFromInvitation(@RequestParam(value = "token", required = false) String token,
                                                       @ModelAttribute Usuario formData,
                                                       @RequestParam String password,
                                                       @RequestParam String confirmPassword,
                                                       HttpSession session,
                                                       RedirectAttributes redirectAttributes) {
        System.out.println("📝 [AuthController] Procesando completar perfil por invitación");
        
        // ✅ FIX: Validar que token no sea nulo
        if (token == null || token.trim().isEmpty()) {
            System.out.println("❌ Token de invitación faltante en POST");
            redirectAttributes.addFlashAttribute("error", "Token de invitación no proporcionado.");
            return "redirect:/signin";
        }

        try {
            // Validar token
            Token validToken = tokenService.validarTokenInvitacion(token);
            if (validToken == null) {
                redirectAttributes.addFlashAttribute("error", "El enlace de invitación ha expirado.");
                return "redirect:/signin";
            }

            Usuario usuario = validToken.getUsuario();

            // Validar contraseñas
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
                redirectAttributes.addFlashAttribute("usuario", formData);
                redirectAttributes.addFlashAttribute("token", token);
                return "redirect:/complete-profile?token=" + token;
            }

            if (password.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
                redirectAttributes.addFlashAttribute("usuario", formData);
                redirectAttributes.addFlashAttribute("token", token);
                return "redirect:/complete-profile?token=" + token;
            }

            // Verificar que el username no exista
            if (!usuario.getUsername().equals(formData.getUsername()) &&
                    userService.existePorUsernameOEmail(formData.getUsername(), null)) {
                redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya está en uso. Elige otro.");
                redirectAttributes.addFlashAttribute("usuario", formData);
                redirectAttributes.addFlashAttribute("token", token);
                return "redirect:/complete-profile?token=" + token;
            }

            // Actualizar datos del usuario
            usuario.setUsername(formData.getUsername());
            usuario.setNombreUsuario(formData.getNombreUsuario());
            usuario.setApellidoPaterno(formData.getApellidoPaterno());
            usuario.setApellidoMaterno(formData.getApellidoMaterno());
            usuario.setDni(formData.getDni());
            usuario.setTelefono(formData.getTelefono());
            usuario.setDireccionUsuario(formData.getDireccionUsuario());
            usuario.setHashedPassword(passwordEncoder.encode(password));
            usuario.setAccesoUsuario(Usuario.AccesoUsuario.SI); // Permitir acceso después de completar

            // Guardar usuario actualizado
            Usuario usuarioActualizado = userService.guardarUsuario(usuario);

            // Revocar token de invitación
            tokenService.revocarToken(validToken);

            // Generar código de verificación
            tokenService.generarCodigoVerificacion(usuarioActualizado);

            // Guardar email en sesión para verificación
            session.setAttribute("registrationEmail", usuarioActualizado.getCorreo());

            System.out.println("✅ Perfil completado exitosamente para: " + usuarioActualizado.getCorreo());

            redirectAttributes.addFlashAttribute("success",
                    "¡Perfil completado! Se ha enviado un código de verificación a tu correo.");

            return "redirect:/verify-registration?email=" + usuarioActualizado.getCorreo();

        } catch (Exception e) {
            System.out.println("❌ Error completando perfil: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al completar el perfil. Inténtalo nuevamente.");
            redirectAttributes.addFlashAttribute("usuario", formData);
            redirectAttributes.addFlashAttribute("token", token);
            return "redirect:/complete-profile?token=" + token;
        }
    }

    /* --------------------- OAUTH2: RUTA ÚNICA ---------------------- */

    @GetMapping("/auth/finish-profile")
    public String showFinishProfileForm(Model model, HttpSession session, HttpServletResponse response) {
        // Headers extremadamente restrictivos para prevenir caché y navegación hacia atrás
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, private");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.addHeader("Cache-Control", "no-store");
        response.addHeader("Cache-Control", "max-age=0");

        // Verificar que el usuario esté autenticado vía OAuth2
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            System.out.println("❌ Usuario no autenticado vía OAuth2, redirigiendo a signin");
            return "redirect:/signin?error=oauth_required";
        }

        if (!model.containsAttribute("usuario")) {
            Usuario transientUsuario = (Usuario) session.getAttribute("usuario");
            String oauthProvider = (String) session.getAttribute("oauth_provider");

            System.out.println("transientUsuario: " + transientUsuario);
            System.out.println("🔍 OAuth provider en sesión: " + oauthProvider);

            if (transientUsuario == null) {
                System.out.println("❌ No hay usuario transiente en sesión, redirigiendo a signin");
                return "redirect:/signin";
            }

            // Verificación adicional: si el usuario ya tiene ID, no debería estar aquí
            if (transientUsuario.getUsuarioId() != null) {
                System.out.println("⚠️  Usuario ya tiene ID persistente, redirigiendo a dashboard");
                String role = transientUsuario.getRoles().stream()
                        .findFirst()
                        .map(rol -> rol.getNombreRol().name().toLowerCase())
                        .orElse("dev");
                return "redirect:/devportal/" + role + "/" + transientUsuario.getUsername() + "/dashboard";
            }

            model.addAttribute("usuario", transientUsuario);

            // ✅ PERSONALIZAR MENSAJE SEGÚN PROVEEDOR
            String providerDisplayName = "OAuth2";
            if (oauthProvider != null) {
                switch (oauthProvider.toLowerCase()) {
                    case "google":
                        providerDisplayName = "Google";
                        break;
                    case "github":
                        providerDisplayName = "GitHub";
                        break;
                    case "microsoft":
                        providerDisplayName = "Microsoft";
                        break;
                    default:
                        providerDisplayName = "OAuth2";
                }
            }
            model.addAttribute("providerName", providerDisplayName);
        }

        System.out.println("✅ Mostrando formulario de complete-profile con protección completa");
        return "complete-profile";
    }

    @PostMapping("/auth/save-profile")
    public String saveProfile(@ModelAttribute Usuario usuario,
                              Authentication authentication,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        System.out.println("💾 [saveProfile] Procesando guardado de perfil OAuth2");
        System.out.println("📋 [saveProfile] Datos recibidos - Username: " + usuario.getUsername());
        System.out.println("📋 [saveProfile] Nombre: " + usuario.getNombreUsuario());
        System.out.println("📋 [saveProfile] Email: " + usuario.getCorreo());

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            System.out.println("❌ [saveProfile] No es OAuth2AuthenticationToken");
            return "redirect:/signin?error=oauth_error";
        }

        OAuth2User oAuth2User = (OAuth2User) oauthToken.getPrincipal();
        if (oAuth2User == null) {
            System.out.println("❌ [saveProfile] OAuth2User es null");
            return "redirect:/signin?error=oauth_error";
        }

        if (userService.existePorUsernameOEmail(usuario.getUsername(), null)) {
            System.out.println("❌ [saveProfile] Username ya existe: " + usuario.getUsername());
            redirectAttributes.addFlashAttribute("usuario", usuario);
            redirectAttributes.addFlashAttribute("error",
                    "El nombre de usuario '" + usuario.getUsername() + "' ya está en uso. Por favor, elige otro.");
            return "redirect:/auth/finish-profile";
        }

        // Detectar proveedor dinámicamente: google, github, microsoft, etc.
        String proveedor = oauthToken.getAuthorizedClientRegistrationId();
        System.out.println("🔐 [saveProfile] Proveedor OAuth2: " + proveedor);

        usuario.setProveedor(proveedor);
        usuario.setIdProveedor(oAuth2User.getName());
        usuario.setHashedPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setEstadoUsuario(Usuario.EstadoUsuario.HABILITADO);
        usuario.setAccesoUsuario(Usuario.AccesoUsuario.SI); // ✅ IMPORTANTE: Dar acceso

        // ✅ FORZAR ROL DEV para OAuth2
        Rol devRol = rolRepository.findByRoleName(Rol.NombreRol.DEV).orElseGet(() -> {
            System.out.println("⚠️ [saveProfile] Rol DEV no encontrado, creando uno nuevo");
            Rol newRol = new Rol();
            newRol.setNombreRol(Rol.NombreRol.DEV);
            return rolRepository.save(newRol);
        });
        Set<Rol> roles = new HashSet<>();
        roles.add(devRol);
        usuario.setRoles(roles);

        System.out.println("💾 [saveProfile] Guardando usuario en base de datos...");
        Usuario savedUsuario = userService.guardarUsuario(usuario);
        System.out.println("✅ [saveProfile] Usuario guardado con ID: " + savedUsuario.getUsuarioId());
        System.out.println("✅ [saveProfile] Username final: " + savedUsuario.getUsername());

        // ✅ CRÍTICO: Actualizar sesión y eliminar usuario transiente
        session.setAttribute("usuario", savedUsuario);
        session.removeAttribute("oauth_provider");

        // ✅ Crear autenticación con UserDetails (no OAuth2User)
        Set<GrantedAuthority> authorities = savedUsuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombreRol().name().toUpperCase()))
                .collect(Collectors.toSet());

        // ✅ IMPORTANTE: Usar UsernamePasswordAuthenticationToken en lugar de OAuth2AuthenticationToken
        // Esto previene que el OAuth2LoginSuccessHandler se ejecute nuevamente
        org.springframework.security.core.userdetails.User userDetails =
                (org.springframework.security.core.userdetails.User) org.springframework.security.core.userdetails.User
                        .builder()
                        .username(savedUsuario.getUsername())
                        .password(savedUsuario.getHashedPassword())
                        .authorities(authorities)
                        .build();

        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities);

        SecurityContextHolder.getContext().setAuthentication(newAuth);
        System.out.println("✅ [saveProfile] Autenticación actualizada con UsernamePasswordAuthenticationToken");
        System.out.println("✅ [saveProfile] Principal: " + newAuth.getName());
        System.out.println("✅ [saveProfile] Authorities: " + newAuth.getAuthorities());

        // Redirigir al dashboard específico por rol
        String role = savedUsuario.getRoles().stream()
                .findFirst()
                .map(rol -> rol.getNombreRol().name().toLowerCase())
                .orElse("dev");
        String username = savedUsuario.getUsername();

        String redirectUrl = "/devportal/" + role + "/" + username + "/dashboard";
        System.out.println("🚀 [saveProfile] Redirigiendo a: " + redirectUrl);

        return "redirect:" + redirectUrl;
    }

    // Endpoint para manejar URLs incorrectas con oidc_user
    @GetMapping("/devportal/oidc_user/{providerUserId}/**")
    public String handleOidcUserRedirect(@PathVariable String providerUserId, HttpSession session) {
        System.out.println("⚠️  Usuario intentó acceder con oidc_user ID: " + providerUserId);

        // Verificar si hay usuario en sesión que esté completando perfil
        Usuario transientUsuario = (Usuario) session.getAttribute("usuario");
        if (transientUsuario != null && transientUsuario.getIdProveedor() != null &&
                transientUsuario.getIdProveedor().equals(providerUserId)) {
            System.out.println("✅ Redirigiendo usuario en proceso de complete-profile");
            return "redirect:/auth/finish-profile";
        }

        // Si no hay usuario en sesión o no coincide, redirigir a signin
        System.out.println("❌ No hay usuario válido en sesión, redirigiendo a signin");
        return "redirect:/signin?error=invalid_session";
    }

    /* --------------------- SUPER ADMINISTRATOR ACCESS ---------------------- */

    @GetMapping("/sa-access")
    public String showSAAccessForm(@RequestParam(required = false) String error, Model model) {
        // Verificar si ya está autenticado como SA
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {

            // Si está autenticado, verificar si es SA
            boolean isSA = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_SA"));

            if (isSA) {
                // Obtener el usuario para redirect correcto
                try {
                    Usuario user = userService.buscarPorUsernameOEmail(authentication.getName());
                    return "redirect:/devportal/sa/" + user.getUsername() + "/dashboard";
                } catch (Exception e) {
                    // Fallback si no se puede obtener el usuario
                    return "redirect:/sa-access?error=session_error";
                }
            }
        }

        if (error != null) {
            switch (error) {
                case "access_denied":
                    model.addAttribute("error", "Acceso denegado. Solo Super Administradores pueden acceder.");
                    break;
                case "invalid_credentials":
                    model.addAttribute("error", "Usuario o contraseña inválidos.");
                    break;
                case "account_disabled":
                    model.addAttribute("error", "La cuenta está deshabilitada.");
                    break;
                default:
                    model.addAttribute("error", "Error de autenticación.");
            }
        }

        System.out.println("🔐 Mostrando formulario de acceso para Super Administrador");
        return "sa-access";
    }

    @PostMapping("/sa-login")
    public String processSALogin(@RequestParam String username,
                                 @RequestParam String password,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        System.out.println("🔐 Intentando autenticación de SA para usuario: " + username);
        try {
            Usuario usuario = userService.buscarPorUsernameOEmail(username);
            if (usuario == null) {
                return "redirect:/sa-access?error=invalid_credentials";
            }
            boolean isSA = usuario.getRoles().stream()
                    .anyMatch(rol -> rol.getNombreRol() == Rol.NombreRol.SA);
            if (!isSA) {
                return "redirect:/sa-access?error=access_denied";
            }
            if (!passwordEncoder.matches(password, usuario.getHashedPassword())) {
                return "redirect:/sa-access?error=invalid_credentials";
            }
            if (usuario.getEstadoUsuario() != Usuario.EstadoUsuario.HABILITADO) {
                return "redirect:/sa-access?error=account_disabled";
            }
            // Generar token de verificación y enviarlo por correo
            Token token = tokenService.generarTokenSA(usuario);
            emailService.enviarCorreoTokenSA(usuario, token);
            session.setAttribute("saUsuario", usuario);
            session.setAttribute("saTokenId", token.getValorToken());
            return "sa-token";
        } catch (Exception e) {
            return "redirect:/sa-access?error=invalid_credentials";
        }
    }

    @PostMapping("/sa-token")
    public String processSAToken(@RequestParam(value = "token", required = false) String token,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        System.out.println("🔐 Procesando token SA: " + token);
        
        // ✅ FIX: Validar que token no sea nulo
        if (token == null || token.trim().isEmpty()) {
            System.out.println("❌ Token SA faltante");
            model.addAttribute("error", "Token no proporcionado. Por favor verifica el enlace.");
            return "sa-token";
        }

        Usuario usuario = (Usuario) session.getAttribute("saUsuario");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Sesión inválida. Inicia sesión nuevamente.");
            return "redirect:/sa-access";
        }

        Token validToken = tokenService.validarTokenSA(token, usuario);
        if (validToken == null) {
            model.addAttribute("error", "Token inválido o expirado. Revisa tu correo o solicita uno nuevo.");
            model.addAttribute("email", usuario.getCorreo());
            return "sa-token";
        }

        try {
            // Autenticar al SA y redirigir al dashboard
            Set<GrantedAuthority> authorities = usuario.getRoles().stream()
                    .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombreRol().name()))
                    .collect(Collectors.toSet());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(usuario, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // Limpiar sesión
            session.removeAttribute("saUsuario");
            session.removeAttribute("saTokenId");

            // Revocar token usado
            tokenService.revocarToken(validToken);

            System.out.println("✅ SA autenticado exitosamente: " + usuario.getUsername());

            return "redirect:/devportal/sa/" + usuario.getUsername() + "/dashboard";

        } catch (Exception e) {
            System.out.println("❌ Error autenticando SA: " + e.getMessage());
            model.addAttribute("error", "Error en la autenticación. Inténtalo nuevamente.");
            model.addAttribute("email", usuario.getCorreo());
            return "sa-token";
        }
    }

    /* --------------------- LOGOUT ---------------------- */

    @Autowired
    private org.project.project.service.ImpersonacionService impersonacionService;

    @PostMapping("/auth/logout")
    public String logout(HttpSession session) {
        // ============================================================================
        // 🔒 FINALIZACIÓN AUTOMÁTICA DE IMPERSONACIÓN AL CERRAR SESIÓN
        // ============================================================================
        try {
            Boolean isImpersonating = (Boolean) session.getAttribute("impersonating");
            Long impersonatedUserId = (Long) session.getAttribute("impersonatedUserId");
            String impersonatedUsername = (String) session.getAttribute("impersonatedUsername");

            if (Boolean.TRUE.equals(isImpersonating) && impersonatedUserId != null) {
                System.out.println("🔒 [Logout] Impersonación activa detectada durante logout");
                System.out.println("   Usuario impersonado: " + impersonatedUsername + " (ID: " + impersonatedUserId + ")");
                System.out.println("   Finalizando impersonación automáticamente...");

                // Finalizar la impersonación en la base de datos
                impersonacionService.finalizarImpersonacion(impersonatedUserId);

                // Limpiar atributos de impersonación de la sesión
                session.removeAttribute("impersonating");
                session.removeAttribute("originalSuperadmin");
                session.removeAttribute("impersonatedUserId");
                session.removeAttribute("impersonatedUsername");

                System.out.println("✅ [Logout] Impersonación finalizada exitosamente");
            }
        } catch (Exception e) {
            System.err.println("⚠️  [Logout] Error al finalizar impersonación durante logout: " + e.getMessage());
            e.printStackTrace();
            // Continuar con el logout normal aunque falle la finalización
        }
        // ============================================================================

        SecurityContextHolder.clearContext();
        session.invalidate();
        return "redirect:/signin?logout";
    }

    @Autowired
    private TokenService tokenService;
    @Autowired
    private EmailService emailService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @GetMapping("/verify-code")
    public String showVerifyCodeForm(@RequestParam(required = false) String email,
                                     @RequestParam(required = false) String error,
                                     Model model,
                                     HttpSession session) {

        System.out.println("🔍 GET /verify-code - Email param: " + email);

        // Verificar que hay un proceso de reset en curso
        String sessionEmail = (String) session.getAttribute("resetEmail");
        System.out.println("🔍 Session resetEmail: " + sessionEmail);

        if (sessionEmail == null) {
            System.out.println("❌ No hay resetEmail en sesión, redirigiendo a forgot-password");
            return "redirect:/forgot-password?error=session_expired";
        }

        // Usar el email de la sesión si no se proporciona en la URL
        if (email == null || !sessionEmail.equals(email)) {
            email = sessionEmail;
        }

        // Verificar si el usuario no fue encontrado (por seguridad, mostrar la vista pero sin funcionalidad real)
        Boolean userNotFound = (Boolean) session.getAttribute("userNotFound");
        if (userNotFound != null && userNotFound) {
            model.addAttribute("simulateOnly", true);
        }

        if (error != null) {
            switch (error) {
                case "invalid_code":
                    model.addAttribute("error", "Código inválido. Verifica que hayas ingresado correctamente los 6 dígitos.");
                    break;
                case "expired_code":
                    model.addAttribute("error", "El código ha expirado. Solicita uno nuevo.");
                    break;
                case "session_expired":
                    model.addAttribute("error", "Tu sesión ha expirado. Inicia el proceso nuevamente.");
                    break;
                default:
                    model.addAttribute("error", "Error de verificación. Inténtalo nuevamente.");
            }
        }

        model.addAttribute("email", email);

        System.out.println("✅ Mostrando verify-code.html con email: " + email);
        return "verify-code";
    }

    @PostMapping("/resend-code")
    public String resendVerificationCode(HttpSession session, RedirectAttributes redirectAttributes) {
        String sessionEmail = (String) session.getAttribute("resetEmail");

        if (sessionEmail == null) {
            redirectAttributes.addFlashAttribute("error", "Tu sesión ha expirado. Inicia el proceso nuevamente.");
            return "redirect:/forgot-password";
        }

        try {
            Usuario usuario = userService.buscarPorEmail(sessionEmail);

            // Generar nuevo código
            Token newToken = tokenService.generarCodigoRecuperacionContrasena(usuario);

            // Enviar por email
            emailService.enviarCodigoRestablecimientoContrasena(usuario, newToken);

            // Actualizar sesión con nuevo token
            session.setAttribute("resetTokenId", newToken.getValorToken());

            System.out.println("🔄 Código reenviado para: " + sessionEmail);

            redirectAttributes.addFlashAttribute("success", "Nuevo código enviado a tu correo.");
            return "redirect:/verify-code?email=" + sessionEmail;

        } catch (Exception e) {
            System.out.println("❌ Error reenviando código: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al reenviar el código. Inténtalo nuevamente.");
            return "redirect:/verify-code?email=" + sessionEmail;
        }
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes,
                                        HttpSession session) {
        System.out.println("🔐 Iniciando proceso de recuperación para email: " + email);

        try {
            // Validar formato de email
            if (!isValidEmail(email)) {
                System.out.println("❌ Email inválido: " + email);
                redirectAttributes.addFlashAttribute("error", "Por favor, ingresa un correo electrónico válido.");
                return "redirect:/forgot-password";
            }

            System.out.println("📧 Email válido, buscando usuario: " + email);

            // Buscar usuario por email
            Usuario usuario = null;
            try {
                usuario = userService.buscarPorEmail(email);
                System.out.println("✅ Usuario encontrado: " + usuario.getCorreo() + ", Estado: " + usuario.getEstadoUsuario());
            } catch (Exception e) {
                System.out.println("⚠️ Usuario no encontrado para email: " + email);
                // Por seguridad, simular que el proceso continúa pero guardar en sesión que no existe
                session.setAttribute("resetEmail", email);
                session.setAttribute("userNotFound", true);
                // Mostrar mensaje de éxito genérico y redirigir a verify-code para mantener flujo consistente
                redirectAttributes.addFlashAttribute("success",
                        "Si el correo existe en nuestro sistema, recibirás un código de verificación en breve.");
                return "redirect:/verify-code?email=" + email;
            }

            // Verificar que el usuario esté habilitado
            if (usuario.getEstadoUsuario() != Usuario.EstadoUsuario.HABILITADO) {
                System.out.println("❌ Usuario no habilitado: " + usuario.getEstadoUsuario());
                redirectAttributes.addFlashAttribute("error",
                        "La cuenta asociada a este correo no está activa. Contacta al administrador.");
                return "redirect:/forgot-password";
            }

            // Verificar si es usuario OAuth2 (no tiene contraseña tradicional)
            if (usuario.getProveedor() != null && !usuario.getProveedor().isEmpty()) {
                System.out.println("❌ Usuario OAuth2, proveedor: " + usuario.getProveedor());
                redirectAttributes.addFlashAttribute("error",
                        "Esta cuenta usa " + usuario.getProveedor().toUpperCase() + " para iniciar sesión. " +
                                "No puedes cambiar la contraseña aquí.");
                return "redirect:/forgot-password";
            }

            System.out.println("✅ Usuario válido para reset de password");

            // Generar código de verificación de 6 dígitos
            Token token = tokenService.generarCodigoRecuperacionContrasena(usuario);

            // Enviar código por email
            emailService.enviarCodigoRestablecimientoContrasena(usuario, token);

            // Guardar información en sesión para el siguiente paso
            session.setAttribute("resetEmail", email);
            session.setAttribute("resetTokenId", token.getValorToken());

            System.out.println("✅ Código de verificación generado y enviado para: " + email);
            System.out.println("🔄 Redirigiendo a /verify-code?email=" + email);

            // Redirigir a la página de verificación de código
            return "redirect:/verify-code?email=" + email;

        } catch (Exception e) {
            System.out.println("❌ Error en proceso de recuperación: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Ocurrió un error al procesar tu solicitud. Inténtalo nuevamente.");
            return "redirect:/forgot-password";
        }
    }

    @PostMapping("/verify-code")
    public String processVerifyCode(@RequestParam("digit1") String digit1,
                                    @RequestParam("digit2") String digit2,
                                    @RequestParam("digit3") String digit3,
                                    @RequestParam("digit4") String digit4,
                                    @RequestParam("digit5") String digit5,
                                    @RequestParam("digit6") String digit6,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        String sessionEmail = (String) session.getAttribute("resetEmail");
        if (sessionEmail == null) {
            redirectAttributes.addFlashAttribute("error", "Tu sesión ha expirado. Inicia el proceso nuevamente.");
            return "redirect:/forgot-password";
        }

        // Verificar si es una simulación (usuario no encontrado)
        Boolean userNotFound = (Boolean) session.getAttribute("userNotFound");
        if (userNotFound != null && userNotFound) {
            // Simular error de código inválido por seguridad
            return "redirect:/verify-code?email=" + sessionEmail + "&error=invalid_code";
        }

        // Combinar los dígitos
        String code = digit1 + digit2 + digit3 + digit4 + digit5 + digit6;

        System.out.println("🔍 Verificando código: " + code + " para email: " + sessionEmail);

        try {
            // Buscar usuario
            Usuario usuario = userService.buscarPorEmail(sessionEmail);

            // Validar código
            Token validToken = tokenService.validarCodigoRecuperacionContrasena(code, usuario);
            if (validToken == null) {
                return "redirect:/verify-code?email=" + sessionEmail + "&error=invalid_code";
            }

            // Código válido, proceder al reset de contraseña
            session.setAttribute("resetToken", validToken.getValorToken());
            session.setAttribute("resetUserId", usuario.getUsuarioId());

            // Limpiar flag de simulación
            session.removeAttribute("userNotFound");

            System.out.println("✅ Código verificado correctamente para: " + sessionEmail);

            return "redirect:/reset-password";

        } catch (Exception e) {
            System.out.println("❌ Error verificando código: " + e.getMessage());
            return "redirect:/verify-code?email=" + sessionEmail + "&error=invalid_code";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(required = false) String token,
                                        Model model,
                                        RedirectAttributes redirectAttributes,
                                        HttpSession session) {

        // Nuevo flujo: verificar si viene desde verify-code
        String resetToken = (String) session.getAttribute("resetToken");
        Long resetUserId = (Long) session.getAttribute("resetUserId");
        String resetEmail = (String) session.getAttribute("resetEmail");

        if (resetToken != null && resetUserId != null) {
            // Nuevo flujo con código verificado
            model.addAttribute("email", resetEmail);
            model.addAttribute("step", 3); // Paso 3 del proceso
            return "reset-password";
        }

        // Flujo legacy con token en URL (mantener compatibilidad)
        if (token != null) {
            Token validToken = tokenService.validarTokenRecuperacionContrasena(token);
            if (validToken == null) {
                redirectAttributes.addFlashAttribute("error", "El enlace de recuperación es inválido o ha expirado.");
                return "redirect:/forgot-password";
            }
            model.addAttribute("token", token);
            model.addAttribute("legacy", true);
            return "reset-password";
        }

        // No hay token válido ni sesión activa
        redirectAttributes.addFlashAttribute("error", "Acceso no autorizado. Inicia el proceso de recuperación.");
        return "redirect:/forgot-password";
    }

    private boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Patrón básico para validar email
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam(required = false) String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {

        System.out.println("🔐 Procesando cambio de contraseña");

        // Validar contraseñas
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");

            // Determinar a dónde redirigir según el flujo
            String resetToken = (String) session.getAttribute("resetToken");
            if (resetToken != null) {
                return "redirect:/reset-password"; // Nuevo flujo
            } else if (token != null) {
                return "redirect:/reset-password?token=" + token; // Legacy
            }
            return "redirect:/forgot-password";
        }

        if (!isPasswordValid(password)) {
            redirectAttributes.addFlashAttribute("error",
                    "La contraseña debe tener al menos 8 caracteres, incluyendo mayúsculas, minúsculas, números y símbolos.");

            String resetToken = (String) session.getAttribute("resetToken");
            if (resetToken != null) {
                return "redirect:/reset-password";
            } else if (token != null) {
                return "redirect:/reset-password?token=" + token;
            }
            return "redirect:/forgot-password";
        }

        try {
            Usuario usuario = null;
            Token validToken = null;

            // Nuevo flujo: verificar sesión
            String resetToken = (String) session.getAttribute("resetToken");
            Long resetUserId = (Long) session.getAttribute("resetUserId");

            if (resetToken != null && resetUserId != null) {
                System.out.println("📱 Usando nuevo flujo con código verificado");
                usuario = userService.buscarUsuarioPorId(resetUserId);
                validToken = tokenService.validarTokenRecuperacionContrasena(resetToken);

                if (validToken == null) {
                    redirectAttributes.addFlashAttribute("error", "Tu sesión ha expirado. Inicia el proceso nuevamente.");
                    return "redirect:/forgot-password";
                }
            }
            // Flujo legacy: usar token de URL
            else if (token != null) {
                System.out.println("🔗 Usando flujo legacy con token URL");
                validToken = tokenService.validarTokenRecuperacionContrasena(token);
                if (validToken == null) {
                    redirectAttributes.addFlashAttribute("error", "El enlace de recuperación es inválido o ha expirado.");
                    return "redirect:/forgot-password";
                }
                usuario = validToken.getUsuario();
            }
            else {
                redirectAttributes.addFlashAttribute("error", "Acceso no autorizado.");
                return "redirect:/forgot-password";
            }

            // Actualizar contraseña
            usuario.setHashedPassword(passwordEncoder.encode(password));
            userService.guardarUsuario(usuario);

            // Revocar token de recuperación
            tokenService.revocarToken(validToken);

            // Generar código de verificación para activar cuenta
            tokenService.generarCodigoVerificacion(usuario);

            // Limpiar sesión
            session.removeAttribute("resetToken");
            session.removeAttribute("resetUserId");
            session.removeAttribute("resetEmail");

            System.out.println("✅ Contraseña establecida exitosamente para: " + usuario.getCorreo());
            System.out.println("📱 Código de verificación enviado para habilitar cuenta");

            // Establecer email en sesión para verificación
            session.setAttribute("registrationEmail", usuario.getCorreo());

            // Redirigir a verificación de código para habilitar cuenta
            redirectAttributes.addFlashAttribute("success",
                    "Contraseña establecida exitosamente. Te hemos enviado un código de verificación para activar tu cuenta.");
            return "redirect:/verify-registration?email=" + usuario.getCorreo();

        } catch (Exception e) {
            System.out.println("❌ Error restableciendo contraseña: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al restablecer la contraseña. Inténtalo nuevamente.");
            return "redirect:/forgot-password";
        }
    }

    // ==================== VERIFICACIÓN DE REGISTRO ====================

    @GetMapping("/verify-registration")
    public String showVerifyRegistrationForm(@RequestParam(required = false) String email,
                                             @RequestParam(required = false) String error,
                                             Model model, HttpSession session) {
        System.out.println("🔍 GET /verify-registration - Email param: " + email);

        // Obtener email de sesión o parámetro
        String sessionEmail = (String) session.getAttribute("registrationEmail");
        System.out.println("📧 Session registrationEmail: " + sessionEmail);

        if (email == null && sessionEmail != null) {
            email = sessionEmail;
        }

        if (email == null) {
            System.out.println("❌ No se encontró email para verificación");
            return "redirect:/signup";
        }

        // Verificar que el usuario existe y está pendiente de activación
        try {
            Usuario usuario = userService.buscarPorEmail(email);
            if (usuario.getEstadoUsuario() == Usuario.EstadoUsuario.HABILITADO) {
                System.out.println("✅ Usuario ya está habilitado, redirigiendo a login");
                return "redirect:/signin";
            }
        } catch (Exception e) {
            System.out.println("❌ Usuario no encontrado: " + email);
            return "redirect:/signup";
        }

        model.addAttribute("email", email);

        if (error != null) {
            if ("invalid_code".equals(error)) {
                model.addAttribute("error", "Código incorrecto. Inténtalo nuevamente.");
            } else if ("expired_code".equals(error)) {
                model.addAttribute("error", "El código ha expirado. Se ha enviado un nuevo código.");
            } else {
                model.addAttribute("error", "Error en la verificación. Inténtalo nuevamente.");
            }
        }

        System.out.println("✅ Mostrando verify-registration.html con email: " + email);
        return "verify-registration";
    }

    @PostMapping("/verify-registration")
    public String processVerifyRegistration(@RequestParam("digit1") String digit1,
                                            @RequestParam("digit2") String digit2,
                                            @RequestParam("digit3") String digit3,
                                            @RequestParam("digit4") String digit4,
                                            @RequestParam("digit5") String digit5,
                                            @RequestParam("digit6") String digit6,
                                            RedirectAttributes redirectAttributes,
                                            HttpSession session) {

        String sessionEmail = (String) session.getAttribute("registrationEmail");

        if (sessionEmail == null) {
            System.out.println("❌ No hay email en sesión para verificación de registro");
            return "redirect:/signup";
        }

        String code = digit1 + digit2 + digit3 + digit4 + digit5 + digit6;
        System.out.println("🔍 Verificando código de registro: " + code + " para email: " + sessionEmail);

        try {
            if (tokenService.validarCodigoVerificacion(sessionEmail, code)) {
                System.out.println("✅ Código de registro verificado correctamente para: " + sessionEmail);

                // Activar la cuenta del usuario
                Usuario usuario = userService.buscarPorEmail(sessionEmail);
                usuario.setEstadoUsuario(Usuario.EstadoUsuario.HABILITADO);
                usuario.setAccesoUsuario(Usuario.AccesoUsuario.SI);
                userService.actualizarUsuario(usuario.getUsuarioId(), usuario);

                System.out.println("✅ Usuario habilitado: " + sessionEmail + " - Estado: " + usuario.getEstadoUsuario() + " - Acceso: " + usuario.getAccesoUsuario());

                // Limpiar sesión
                session.removeAttribute("registrationEmail");

                redirectAttributes.addFlashAttribute("success",
                        "¡Cuenta verificada exitosamente! Ahora puedes iniciar sesión.");
                return "redirect:/signin";

            } else {
                System.out.println("❌ Código de registro inválido para: " + sessionEmail);
                return "redirect:/verify-registration?email=" + sessionEmail + "&error=invalid_code";
            }

        } catch (Exception e) {
            System.out.println("❌ Error verificando código de registro: " + e.getMessage());
            return "redirect:/verify-registration?email=" + sessionEmail + "&error=invalid_code";
        }
    }

    @PostMapping("/resend-registration-code")
    public String resendRegistrationCode(HttpSession session, RedirectAttributes redirectAttributes) {
        String sessionEmail = (String) session.getAttribute("registrationEmail");

        if (sessionEmail == null) {
            return "redirect:/signup";
        }

        try {
            Usuario usuario = userService.buscarPorEmail(sessionEmail);
            tokenService.generarCodigoVerificacion(usuario);

            System.out.println("📧 Código de registro reenviado a: " + sessionEmail);
            redirectAttributes.addFlashAttribute("success", "Se ha enviado un nuevo código a tu correo.");

        } catch (Exception e) {
            System.out.println("❌ Error reenviando código: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al reenviar el código. Inténtalo nuevamente.");
        }

        return "redirect:/verify-registration?email=" + sessionEmail;
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verificarCorreo(@RequestParam(value = "token", required = false) String valorToken) {
        // ✅ FIX: Validar que token no sea nulo
        if (valorToken == null || valorToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Token de verificación no proporcionado.");
        }
        
        try {
            tokenService.verificarToken(valorToken);
            return ResponseEntity.ok("✅ Correo verificado con éxito. Ya puedes iniciar sesión.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }
}
