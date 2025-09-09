package org.project.project.controller;

import jakarta.servlet.http.HttpSession;
import org.project.project.model.dto.LoginRequest;
import org.project.project.model.dto.SignupRequest;
import org.project.project.model.entity.Usuario;
import org.project.project.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/signin")
    public String showLoginForm(@RequestParam(required = false) String error, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/dashboard";
        }

        if (error != null) {
            model.addAttribute("error", "Credenciales inválidas");
        }
        model.addAttribute("loginRequest", new LoginRequest());
        return "signin";
    }

    @PostMapping("/signup")
    public String processRegistration(SignupRequest signupRequest, RedirectAttributes redirectAttributes) {
        try {
            if (userService.existsByUsernameOrEmail(signupRequest.getUsername(), signupRequest.getEmail())) {
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

            userService.guardarUsuario(usuario);

            redirectAttributes.addFlashAttribute("success", "Registro exitoso. Por favor, inicia sesión.");
            return "redirect:/signin";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al intentar registrar al usuario: " + e.getMessage());
            return "redirect:/signup";
        }
    }

    @GetMapping("/signup")
    public String showRegisterForm(@RequestParam(required = false) String error, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/dashboard";
        }

        if (error != null) {
            model.addAttribute("error", "Credenciales inválidas");
        }
        model.addAttribute("signupRequest", new SignupRequest());
        return "signup";
    }

    @PostMapping("/auth/logout")
    public String logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.invalidate();
        return "redirect:/signin?logout";
    }

    // --- ENDPOINT TEMPORAL PARA ACTUALIZAR CONTRASEÑAS ---
    @PostMapping("/update-password")
    @ResponseBody
    public String updatePassword(@RequestParam String username, @RequestParam String password) {
        try {
            userService.updatePassword(username, password);
            return "Contraseña para el usuario '" + username + "' actualizada correctamente. Intenta iniciar sesión de nuevo.";
        } catch (Exception e) {
            return "Error al actualizar la contraseña: " + e.getMessage();
        }
    }
}
