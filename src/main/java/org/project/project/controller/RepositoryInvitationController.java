package org.project.project.controller;

import org.project.project.model.entity.RepositorioInvitacion;
import org.project.project.model.entity.Usuario;
import org.project.project.service.RepositoryInvitationService;
import org.project.project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/invitations/repository")
public class RepositoryInvitationController {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryInvitationController.class);

    @Autowired
    private RepositoryInvitationService invitationService;

    @Autowired
    private UserService userService;

    /**
     * Mostrar página de aceptación de invitación
     * GET /invitations/repository/accept?token=xxx
     */
    @GetMapping("/accept")
    public String showAcceptInvitationPage(@RequestParam String token, Model model, Principal principal) {
        logger.info("╔════════════════════════════════════════════════════════╗");
        logger.info("║     MOSTRANDO PÁGINA DE INVITACIÓN A REPOSITORIO       ║");
        logger.info("╚════════════════════════════════════════════════════════╝");
        logger.info("📋 Token recibido: {}", token);
        
        try {
            // Obtener detalles de la invitación
            RepositorioInvitacion invitacion = invitationService.obtenerInvitacionPorToken(token);
            
            logger.info("✅ Invitación encontrada:");
            logger.info("   - Repositorio: {}", invitacion.getRepositorio().getNombreRepositorio());
            logger.info("   - Usuario invitado: {}", invitacion.getUsuarioInvitado().getCorreo());
            logger.info("   - Permiso: {}", invitacion.getPermiso());
            logger.info("   - Estado: {}", invitacion.getEstado());
            logger.info("   - Invitado por: {}", invitacion.getInvitadoPor().getCorreo());
            
            // Agregar información al modelo
            model.addAttribute("invitacion", invitacion);
            model.addAttribute("token", token);
            
            return "repository/accept-invitation";
            
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error con el token de invitación: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "error/invitation-error";
            
        } catch (Exception e) {
            logger.error("❌ Error inesperado al cargar invitación", e);
            model.addAttribute("error", "Error al cargar la invitación");
            return "error/500";
        }
    }

    /**
     * Aceptar invitación a repositorio
     * POST /invitations/repository/accept
     * 
     * IMPORTANTE: NO requiere autenticación previa - el token identifica al usuario
     */
    @PostMapping("/accept")
    public String acceptInvitation(@RequestParam String token, 
                                   RedirectAttributes redirectAttributes,
                                   Principal principal) {
        logger.info("╔════════════════════════════════════════════════════════╗");
        logger.info("║     PROCESANDO ACEPTACIÓN DE INVITACIÓN REPOSITORIO    ║");
        logger.info("╚════════════════════════════════════════════════════════╝");
        logger.info("📋 Token: {}", token);
        logger.info("👤 Usuario autenticado: {}", principal != null ? principal.getName() : "NO AUTENTICADO");
        
        try {
            // Aceptar invitación (el servicio valida el token y agrega al usuario)
            logger.info("🔄 Llamando a invitationService.aceptarInvitacion()...");
            RepositorioInvitacion invitacion = invitationService.aceptarInvitacion(token);
            
            logger.info("✅ Invitación aceptada exitosamente");
            logger.info("   - Repositorio ID: {}", invitacion.getRepositorio().getRepositorioId());
            logger.info("   - Repositorio Nombre: {}", invitacion.getRepositorio().getNombreRepositorio());
            logger.info("   - Usuario invitado ID: {}", invitacion.getUsuarioInvitado().getUsuarioId());
            logger.info("   - Usuario invitado Email: {}", invitacion.getUsuarioInvitado().getCorreo());
            logger.info("   - Permiso otorgado: {}", invitacion.getPermiso());

            // Obtener datos del usuario invitado para redirección
            Usuario usuarioInvitado = invitacion.getUsuarioInvitado();
            String repositorioId = "R-" + invitacion.getRepositorio().getRepositorioId();
            String rol = determinarRolUsuario(usuarioInvitado);
            
            logger.info("🔀 Usuario debe iniciar sesión y será redirigido al repositorio");
            
            // Mensaje de éxito
            redirectAttributes.addFlashAttribute("success", 
                "¡Invitación aceptada! Inicia sesión con tu cuenta (" + usuarioInvitado.getCorreo() + 
                ") para acceder al repositorio '" + invitacion.getRepositorio().getNombreRepositorio() + "'");
            
            // Redirigir a login con returnUrl al repositorio
            String returnUrl = "/devportal/" + rol + "/" + usuarioInvitado.getUsername() + "/repositories/" + repositorioId;
            logger.info("🔀 Return URL después de login: {}", returnUrl);
            
            return "redirect:/signin?returnUrl=" + returnUrl;
            
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error validando invitación: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/invitations/repository/accept?token=" + token;
            
        } catch (Exception e) {
            logger.error("❌❌❌ ERROR CRÍTICO al aceptar invitación ❌❌❌", e);
            logger.error("❌ Mensaje: {}", e.getMessage());
            logger.error("❌ Tipo: {}", e.getClass().getName());
            logger.error("❌ Stack trace completo:", e);
            
            redirectAttributes.addFlashAttribute("error", 
                "Error al procesar la invitación: " + e.getMessage());
            return "redirect:/invitations/repository/accept?token=" + token;
        }
    }

    /**
     * Rechazar invitación a repositorio
     * POST /invitations/repository/reject
     */
    @PostMapping("/reject")
    public String rejectInvitation(@RequestParam String token, 
                                   RedirectAttributes redirectAttributes,
                                   Principal principal) {
        logger.info("╔════════════════════════════════════════════════════════╗");
        logger.info("║     PROCESANDO RECHAZO DE INVITACIÓN REPOSITORIO       ║");
        logger.info("╚════════════════════════════════════════════════════════╝");
        logger.info("📋 Token: {}", token);
        
        try {
            // Rechazar invitación
            RepositorioInvitacion invitacion = invitationService.rechazarInvitacion(token);
            
            logger.info("✅ Invitación rechazada exitosamente");
            logger.info("   - Repositorio: {}", invitacion.getRepositorio().getNombreRepositorio());
            
            redirectAttributes.addFlashAttribute("info", 
                "Has rechazado la invitación al repositorio '" + invitacion.getRepositorio().getNombreRepositorio() + "'");
            
            // Redirigir al home del usuario
            if (principal != null) {
                Usuario usuario = userService.buscarPorUsername(principal.getName());
                String rol = determinarRolUsuario(usuario);
                return "redirect:/devportal/" + rol + "/" + usuario.getUsername() + "/repositories/collaborative-repositories";
            } else {
                return "redirect:/";
            }
            
        } catch (Exception e) {
            logger.error("❌ Error al rechazar invitación", e);
            redirectAttributes.addFlashAttribute("error", "Error al procesar el rechazo");
            return "redirect:/invitations/repository/accept?token=" + token;
        }
    }

    /**
     * Determinar rol del usuario para URL
     */
    private String determinarRolUsuario(Usuario usuario) {
        return usuario.getRoles().iterator().next().getNombreRol().toString().toLowerCase();
    }
}
