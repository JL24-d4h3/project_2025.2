package org.project.project.controller;

import org.project.project.model.entity.ProyectoInvitacion;
import org.project.project.service.ProyectoInvitacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/invitations")
public class ProyectoInvitacionController {

    private static final Logger logger = LoggerFactory.getLogger(ProyectoInvitacionController.class);

    @Autowired
    private ProyectoInvitacionService invitacionService;

    /**
     * Aceptar invitación de proyecto
     * NOTA: Esta ruta es pública (no requiere autenticación)
     */
    @GetMapping("/accept")
    public String aceptarInvitacion(
            @RequestParam(value = "token", required = false) String token,
            Model model) {
        
        logger.info("✅ Procesando aceptación de invitación con token: {}", token);
        
        // ✅ FIX: Validar que token no sea nulo
        if (token == null || token.trim().isEmpty()) {
            logger.error("❌ Token de invitación faltante o vacío");
            model.addAttribute("success", false);
            model.addAttribute("error", "Token de invitación no proporcionado. Por favor verifica el enlace.");
            return "project/invitation-success";
        }
        
        try {
            ProyectoInvitacion invitacion = invitacionService.aceptarInvitacion(token);
            
            logger.info("✅ Invitación aceptada exitosamente");
            logger.info("   - Usuario: {}", invitacion.getUsuarioInvitado().getCorreo());
            logger.info("   - Proyecto: {}", invitacion.getProyecto().getNombreProyecto());
            
            // ✅ FIX: Redirect para evitar re-procesamiento al recargar la página
            // Esto previene el error 500 cuando el usuario recarga después de aceptar
            return "redirect:/invitations/success?accepted=true&project=" + 
                   java.net.URLEncoder.encode(invitacion.getProyecto().getNombreProyecto(), "UTF-8");
            
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error al aceptar invitación: {}", e.getMessage());
            model.addAttribute("success", false);
            model.addAttribute("error", e.getMessage());
            return "project/invitation-success";
            
        } catch (Exception e) {
            logger.error("❌ Error inesperado al aceptar invitación", e);
            model.addAttribute("success", false);
            model.addAttribute("error", "Error al procesar la invitación. Por favor contacta al administrador.");
            return "project/invitation-success";
        }
    }

    /**
     * Rechazar invitación de proyecto
     * NOTA: Esta ruta es pública (no requiere autenticación)
     */
    @GetMapping("/decline")
    public String rechazarInvitacion(
            @RequestParam(value = "token", required = false) String token,
            Model model) {
        
        logger.info("❌ Procesando rechazo de invitación con token: {}", token);
        
        // ✅ FIX: Validar que token no sea nulo
        if (token == null || token.trim().isEmpty()) {
            logger.error("❌ Token de invitación faltante o vacío");
            model.addAttribute("success", false);
            model.addAttribute("error", "Token de invitación no proporcionado. Por favor verifica el enlace.");
            return "project/invitation-success";
        }
        
        try {
            ProyectoInvitacion invitacion = invitacionService.rechazarInvitacion(token);
            
            logger.info("✅ Invitación rechazada exitosamente");
            logger.info("   - Usuario: {}", invitacion.getUsuarioInvitado().getCorreo());
            logger.info("   - Proyecto: {}", invitacion.getProyecto().getNombreProyecto());
            
            // ✅ FIX: Redirect para evitar re-procesamiento al recargar la página
            return "redirect:/invitations/success?declined=true&project=" + 
                   java.net.URLEncoder.encode(invitacion.getProyecto().getNombreProyecto(), "UTF-8");
            
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error al rechazar invitación: {}", e.getMessage());
            model.addAttribute("success", false);
            model.addAttribute("error", e.getMessage());
            return "project/invitation-success";
            
        } catch (Exception e) {
            logger.error("❌ Error inesperado al rechazar invitación", e);
            model.addAttribute("success", false);
            model.addAttribute("error", "Error al procesar la invitación. Por favor contacta al administrador.");
            return "project/invitation-success";
        }
    }
    
    /**
     * Mostrar página de éxito después de procesar invitación
     * Este endpoint maneja el redirect para evitar re-procesamiento del token
     */
    @GetMapping("/success")
    public String mostrarExito(
            @RequestParam(value = "accepted", required = false) Boolean accepted,
            @RequestParam(value = "declined", required = false) Boolean declined,
            @RequestParam(value = "project", required = false) String projectName,
            Model model) {
        
        logger.info("📄 Mostrando página de éxito de invitación");
        
        if (Boolean.TRUE.equals(accepted)) {
            model.addAttribute("success", true);
            model.addAttribute("declined", false);
            model.addAttribute("message", "¡Invitación aceptada exitosamente!");
            model.addAttribute("projectName", projectName);
            logger.info("✅ Invitación aceptada - Proyecto: {}", projectName);
            
        } else if (Boolean.TRUE.equals(declined)) {
            model.addAttribute("success", true);
            model.addAttribute("declined", true);
            model.addAttribute("message", "Invitación rechazada");
            model.addAttribute("projectName", projectName);
            logger.info("❌ Invitación rechazada - Proyecto: {}", projectName);
            
        } else {
            model.addAttribute("success", false);
            model.addAttribute("error", "Parámetros de invitación inválidos");
            logger.warn("⚠️ Parámetros inválidos en /invitations/success");
        }
        
        return "project/invitation-success";
    }
}
