package org.project.project.controller;

import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.Rol;
import org.project.project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador para el chatbot de Flowise
 */
@Controller
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);
    private final UserService userService;

    public ChatbotController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Página de prueba del chatbot de Flowise
     * Requiere autenticación y muestra información del usuario en sesión
     */
    @GetMapping("/devportal/{rol}/{username}/chatbot-test")
    public String showChatbotTest(@PathVariable String rol,
                                   @PathVariable String username,
                                   Authentication authentication,
                                   Model model) {
        logger.info("🤖 [CHATBOT] Accediendo a página de prueba del chatbot");
        logger.info("👤 [CHATBOT] Rol: {}, Username: {}", rol, username);

        try {
            // Verificar autenticación
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("⚠️ [CHATBOT] Usuario no autenticado, redirigiendo a signin");
                return "redirect:/signin";
            }

            String authenticatedUsername = authentication.getName();
            logger.info("👤 [CHATBOT] Usuario autenticado: {}", authenticatedUsername);

            // Obtener información del usuario
            Usuario usuario = userService.buscarPorUsername(authenticatedUsername);

            if (usuario == null) {
                logger.error("❌ [CHATBOT] Usuario no encontrado: {}", authenticatedUsername);
                return "error/404";
            }

            // Obtener roles del usuario
            Set<Rol> roles = usuario.getRoles();
            String rolesStr = "Usuario";
            if (roles != null && !roles.isEmpty()) {
                rolesStr = roles.stream()
                    .map(r -> r.getNombreRol().toString())
                    .collect(Collectors.joining(", "));
            }

            // Agregar información al modelo (siguiendo el patrón de SandboxController)
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);
            model.addAttribute("currentNavSection", "chatbot");
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombreCompleto", usuario.getNombreUsuario());
            model.addAttribute("email", usuario.getCorreo());
            model.addAttribute("rol", rolesStr);

            logger.info("✅ [CHATBOT] Datos del usuario agregados al modelo correctamente");
            logger.info("📊 [CHATBOT] Usuario ID: {}, Email: {}, Nombre: {}", 
                usuario.getUsuarioId(), usuario.getCorreo(), usuario.getNombreUsuario());

            return "chatbot-test";

        } catch (Exception e) {
            logger.error("❌ [CHATBOT] Error al cargar página de chatbot: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar el chatbot: " + e.getMessage());
            return "error/500";
        }
    }
    
    /**
     * Página de DEBUG para verificar variables de Thymeleaf
     */
    @GetMapping("/devportal/{rol}/{username}/chatbot-debug")
    public String showChatbotDebug(@PathVariable String rol,
                                    @PathVariable String username,
                                    Authentication authentication,
                                    Model model) {
        logger.info("🐛 [DEBUG] Accediendo a página de debug del chatbot");

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/signin";
            }

            String authenticatedUsername = authentication.getName();
            Usuario usuario = userService.buscarPorUsername(authenticatedUsername);

            if (usuario == null) {
                return "error/404";
            }

            Set<Rol> roles = usuario.getRoles();
            String rolesStr = "Usuario";
            if (roles != null && !roles.isEmpty()) {
                rolesStr = roles.stream()
                    .map(r -> r.getNombreRol().toString())
                    .collect(Collectors.joining(", "));
            }

            // Mismo modelo que chatbot-test
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);
            model.addAttribute("currentNavSection", "chatbot");
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombreCompleto", usuario.getNombreUsuario());
            model.addAttribute("email", usuario.getCorreo());
            model.addAttribute("rol", rolesStr);

            logger.info("🐛 [DEBUG] Variables: username={}, email={}, userId={}", 
                username, usuario.getCorreo(), usuario.getUsuarioId());

            return "chatbot-debug";

        } catch (Exception e) {
            logger.error("❌ [DEBUG] Error: {}", e.getMessage(), e);
            return "error/500";
        }
    }
}
