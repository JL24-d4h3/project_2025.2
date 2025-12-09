package org.project.project.controller;

import jakarta.servlet.http.HttpSession;
import org.project.project.service.UserService;
import org.project.project.model.entity.Usuario;
import org.project.project.model.dto.TicketSummaryView;
import org.project.project.model.dto.AvailableTicketView;
import org.project.project.model.dto.FollowUpTicketView;
import org.project.project.model.entity.Ticket;
import org.project.project.repository.UsuarioRepository;
import org.project.project.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/devportal/{rol}/{usuario}/tickets")
public class TicketViewController {

    private final TicketService ticketService;
    private final UsuarioRepository usuarioRepository;
    private final UserService userService;

    public TicketViewController(TicketService ticketService, UsuarioRepository usuarioRepository, UserService userService) {
        this.ticketService = ticketService;
        this.usuarioRepository = usuarioRepository;
        this.userService = userService;
    }

    private Long resolveUsuarioIdByUsername(String username, HttpSession session) {
        if (username == null || username.isBlank()) return null;
        // cache simple en sesión para evitar buscar repetidamente
        Object cached = session.getAttribute("usuarioId");
        Object cachedUser = session.getAttribute("username");
        if (cached instanceof Long && (cachedUser != null && username.equals(cachedUser.toString()))) {
            return (Long) cached;
        }
        return usuarioRepository.findByUsername(username)
                .map(u -> {
                    session.setAttribute("usuarioId", u.getUsuarioId());
                    session.setAttribute("username", username);
                    return u.getUsuarioId();
                })
                .orElse(null);
    }

    private void ensureSessionRole(HttpSession session, String rol) {
        if (rol != null) session.setAttribute("rol", rol);
    }
    
    /**
     * Verifica si el usuario actual puede acceder a los tickets del usuario especificado
     */
    private boolean canAccessUserTickets(Usuario currentUser, String targetUsername, String targetRole) {
        if (currentUser == null) {
            return false;
        }
        
        // El usuario puede acceder a sus propios tickets
        if (currentUser.getUsername().equals(targetUsername)) {
            return true;
        }
        
        // SA puede acceder a cualquier ticket
        String currentRole = currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase();
        if ("sa".equals(currentRole)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Configura las variables comunes del modelo para las vistas de tickets
     */
    private void configureCommonModelAttributes(Model model, String rol, String usuario, Long usuarioId) {
        model.addAttribute("usuarioId", usuarioId);
        model.addAttribute("rol", rol);
        model.addAttribute("usuario", usuario);
        model.addAttribute("username", usuario);
        model.addAttribute("currentNavSection", "tickets");
        model.addAttribute("currentUser", usuarioRepository.findByUsername(usuario).orElse(null));
    }

    @GetMapping
    public String listarMisTickets(@PathVariable("rol") String rol,
                                   @PathVariable("usuario") String usuario,
                                   Model model,
                                   Principal principal,
                                   RedirectAttributes ra) {
        
        Usuario currentUser = userService.buscarPorUsername(principal.getName());
        
        if (!canAccessUserTickets(currentUser, usuario, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase() 
                   + "/" + currentUser.getUsername() + "/tickets";
        }
        
        Long uid = currentUser.getUsuarioId();
        
        // Obtener resumen de tickets creados por el usuario
        List<TicketSummaryView> tickets = ticketService.listarResumenPorUsuario(uid);
        
        // Obtener tickets en seguimiento (asignados al usuario)
        List<FollowUpTicketView> seguimiento = ticketService.listarSeguimiento(uid);
        
        // Contar total de tickets en la plataforma
        long totalTickets = ticketService.contarTodosLosTickets();

        model.addAttribute("tickets", tickets);
        model.addAttribute("seguimiento", seguimiento);
        model.addAttribute("totalTickets", totalTickets);

        // Configurar atributos siguiendo el patrón de ProjectController
        model.addAttribute("user", currentUser);
        model.addAttribute("userRole", rol);
        model.addAttribute("username", usuario);
        model.addAttribute("rol", rol);
        model.addAttribute("usuario", usuario);
        model.addAttribute("currentNavSection", "tickets");
        model.addAttribute("usuarioId", uid);
        
        return "ticket/mis-tickets";
    }

    @GetMapping("/available")
    public String disponibles(@PathVariable("rol") String rol,
                              @PathVariable("usuario") String usuario,
                              Model model,
                              Principal principal,
                              RedirectAttributes ra) {
        
        Usuario currentUser = userService.buscarPorUsername(principal.getName());
        
        if (!canAccessUserTickets(currentUser, usuario, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase() 
                   + "/" + currentUser.getUsername() + "/tickets/available";
        }
        
        Long uid = currentUser.getUsuarioId();
        
        // Obtener tickets disponibles (sin asignar)
        List<AvailableTicketView> disponibles = ticketService.listarDisponibles(uid);
        
        // Inicializar lista de tickets de proyecto disponibles (puede estar vacía)
        List<AvailableTicketView> disponiblesProyecto = new java.util.ArrayList<>();

        model.addAttribute("disponibles", disponibles);
        model.addAttribute("disponiblesProyecto", disponiblesProyecto);
        model.addAttribute("user", currentUser);
        model.addAttribute("userRole", rol);
        model.addAttribute("username", usuario);
        model.addAttribute("rol", rol);
        model.addAttribute("usuario", usuario);
        model.addAttribute("currentNavSection", "tickets");
        
        return "ticket/tickets-disponibles";
    }

    @PostMapping("/{ticketId}/take")
    public String tomar(@PathVariable("rol") String rol,
                        @PathVariable("usuario") String usuario,
                        @PathVariable("ticketId") Long ticketId,
                        HttpSession session,
                        RedirectAttributes ra) {
        ensureSessionRole(session, rol);
        Long uid = resolveUsuarioIdByUsername(usuario, session);
        if (uid == null) {
            ra.addFlashAttribute("error", "No se pudo determinar el usuario. Inicie sesión nuevamente.");
            return "redirect:/signin";
        }
        try {
            // Usar el método tomarTicket que actualiza estado a RECIBIDO
            ticketService.tomarTicket(uid, ticketId);
            ra.addFlashAttribute("exito", "Tomaste el ticket correctamente. Ahora aparece en 'Tickets asignados'.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/devportal/" + rol + "/" + usuario + "/tickets";
    }

    @GetMapping("/assigned/{ticketId}")
    public String detalleAsignado(@PathVariable("rol") String rol,
                                  @PathVariable("usuario") String usuario,
                                  @PathVariable("ticketId") Long ticketId,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes ra) {
        ensureSessionRole(session, rol);
        Long uid = resolveUsuarioIdByUsername(usuario, session);
        if (uid == null) {
            ra.addFlashAttribute("error", "No se pudo determinar el usuario. Inicie sesión nuevamente.");
            return "redirect:/signin";
        }

        // Obtener ticket y verificar que esté asignado al usuario
        Ticket ticket = ticketService.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        if (ticket.getAsignadoA() == null || !ticket.getAsignadoA().getUsuarioId().equals(uid)) {
            ra.addFlashAttribute("error", "No tienes acceso a este ticket");
            return "redirect:/devportal/" + rol + "/" + usuario + "/tickets";
        }

        boolean isAssignee = true;
        boolean isCreator = ticket.getReportadoPor() != null && ticket.getReportadoPor().getUsuarioId().equals(uid);

        model.addAttribute("ticket", ticket);
        model.addAttribute("usuarioId", uid);
        model.addAttribute("rol", rol);
        model.addAttribute("usuario", usuario);
        model.addAttribute("username", usuario);
        model.addAttribute("isAssignee", isAssignee);
        model.addAttribute("isCreator", isCreator);
        model.addAttribute("prioridades", Ticket.PrioridadTicket.values());
        model.addAttribute("currentNavSection", "tickets");
        model.addAttribute("currentUser", usuarioRepository.findByUsername(usuario).orElse(null));
        return "ticket/detalle-ticket";
    }

    @GetMapping("/{ticketId}")
    public String detalleTicket(@PathVariable("rol") String rol,
                                @PathVariable("usuario") String usuario,
                                @PathVariable("ticketId") Long ticketId,
                                HttpSession session,
                                Model model,
                                RedirectAttributes ra) {
        ensureSessionRole(session, rol);
        Long uid = resolveUsuarioIdByUsername(usuario, session);
        if (uid == null) {
            ra.addFlashAttribute("error", "No se pudo determinar el usuario. Inicie sesión nuevamente.");
            return "redirect:/signin";
        }

        // Obtener ticket
        Ticket ticket = ticketService.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        boolean isAssignee = ticket.getAsignadoA() != null && ticket.getAsignadoA().getUsuarioId().equals(uid);
        boolean isCreator = ticket.getReportadoPor() != null && ticket.getReportadoPor().getUsuarioId().equals(uid);

        model.addAttribute("ticket", ticket);
        model.addAttribute("usuarioId", uid);
        model.addAttribute("rol", rol);
        model.addAttribute("usuario", usuario);
        model.addAttribute("username", usuario);
        model.addAttribute("isAssignee", isAssignee);
        model.addAttribute("isCreator", isCreator);
        model.addAttribute("prioridades", Ticket.PrioridadTicket.values());
        model.addAttribute("currentNavSection", "tickets");
        model.addAttribute("currentUser", usuarioRepository.findByUsername(usuario).orElse(null));
        return "ticket/detalle-ticket";
    }

    @GetMapping("/new")
    public String formularioNuevo(@PathVariable("rol") String rol,
                                  @PathVariable("usuario") String usuario,
                                  HttpSession session,
                                  Model model,
                                  Principal principal,
                                  RedirectAttributes ra) {
        ensureSessionRole(session, rol);
        Long uid = resolveUsuarioIdByUsername(usuario, session);
        if (uid == null) {
            ra.addFlashAttribute("error", "No se pudo determinar el usuario. Inicie sesión nuevamente.");
            return "redirect:/signin";
        }
        
        // Obtener proyectos del usuario
        Usuario currentUser = userService.buscarPorUsername(principal.getName());
        model.addAttribute("proyectosUsuario", currentUser.getProyectos());
        
        model.addAttribute("usuarioId", uid);
        model.addAttribute("rol", rol);
        model.addAttribute("usuario", usuario);
        model.addAttribute("username", usuario);
        model.addAttribute("tipos", Ticket.TipoTicket.values());
        model.addAttribute("currentNavSection", "tickets");
        model.addAttribute("currentUser", currentUser);
        return "ticket/nuevo-ticket";
    }

    @PostMapping("/new")
    public String crearTicket(@PathVariable("rol") String rol,
                              @PathVariable("usuario") String usuario,
                              @RequestParam("asunto") String asunto,
                              @RequestParam("cuerpo") String cuerpo,
                              @RequestParam("tipo") String tipo,
                              @RequestParam(value = "proyectoId", required = false) Long proyectoId,
                              HttpSession session,
                              RedirectAttributes ra) {
        ensureSessionRole(session, rol);
        Long uid = resolveUsuarioIdByUsername(usuario, session);
        if (uid == null) {
            ra.addFlashAttribute("error", "No se pudo determinar el usuario. Inicie sesión nuevamente.");
            return "redirect:/signin";
        }
        if (asunto == null || asunto.isBlank() || cuerpo == null || cuerpo.isBlank() || tipo == null || tipo.isBlank()) {
            ra.addFlashAttribute("error", "Asunto, descripción y tipo son obligatorios.");
            return "redirect:/devportal/" + rol + "/" + usuario + "/tickets/new";
        }
        
        try {
            Ticket.TipoTicket tipoTicket = Ticket.TipoTicket.valueOf(tipo);

            // Crear ticket público o de proyecto según proyectoId
            if (proyectoId != null && proyectoId > 0) {
                ticketService.createTicketWithProject(uid, asunto, cuerpo, tipoTicket, proyectoId);
                ra.addFlashAttribute("exito", "Ticket de proyecto creado correctamente");
            } else {
                ticketService.createPublicTicket(uid, asunto, cuerpo, tipoTicket);
                ra.addFlashAttribute("exito", "Ticket público creado correctamente");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al crear ticket: " + e.getMessage());
        }
        
        return "redirect:/devportal/" + rol + "/" + usuario + "/tickets";
    }

    @GetMapping("/avaible")
    public String disponiblesAlias(@PathVariable("rol") String rol,
                                   @PathVariable("usuario") String usuario) {
        return "redirect:/devportal/" + rol + "/" + usuario + "/tickets/available";
    }

    @PostMapping("/assigned/{ticketId}/draft")
    public String guardarBorrador(@PathVariable("rol") String rol,
                                  @PathVariable("usuario") String usuario,
                                  @PathVariable("ticketId") Long ticketId,
                                  @RequestParam("prioridad") String prioridad,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        Long uid = resolveUsuarioIdByUsername(usuario, session);
        if (uid == null) {
            ra.addFlashAttribute("error", "No se pudo determinar el usuario. Inicie sesión nuevamente.");
            return "redirect:/signin";
        }
        try {
            // Actualizar prioridad del ticket
            Ticket.PrioridadTicket nuevaPrioridad = Ticket.PrioridadTicket.valueOf(prioridad);
            ticketService.updateTicketPriority(ticketId, nuevaPrioridad);
            ra.addFlashAttribute("exito", "Borrador guardado");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/devportal/" + rol + "/" + usuario + "/tickets/assigned/" + ticketId;
    }

    @PostMapping("/assigned/{ticketId}/respond")
    public String responder(@PathVariable("rol") String rol,
                            @PathVariable("usuario") String usuario,
                            @PathVariable("ticketId") Long ticketId,
                            @RequestParam("prioridad") String prioridad,
                            @RequestParam("respuesta") String respuesta,
                            HttpSession session,
                            RedirectAttributes ra) {
        Long uid = resolveUsuarioIdByUsername(usuario, session);
        if (uid == null) {
            ra.addFlashAttribute("error", "No se pudo determinar el usuario. Inicie sesión nuevamente.");
            return "redirect:/signin";
        }
        try {
            // Actualizar prioridad y resolver ticket
            Ticket.PrioridadTicket nuevaPrioridad = Ticket.PrioridadTicket.valueOf(prioridad);
            ticketService.updateTicketPriority(ticketId, nuevaPrioridad);
            ticketService.resolveTicket(ticketId);
            ra.addFlashAttribute("exito", "Respuesta enviada. Ticket marcado como RESUELTO.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/devportal/" + rol + "/" + usuario + "/tickets";
    }

    @PostMapping("/assigned/{ticketId}/reject")
    public String rechazar(@PathVariable("rol") String rol,
                           @PathVariable("usuario") String usuario,
                           @PathVariable("ticketId") Long ticketId,
                           HttpSession session,
                           RedirectAttributes ra) {
        Long uid = resolveUsuarioIdByUsername(usuario, session);
        if (uid == null) {
            ra.addFlashAttribute("error", "No se pudo determinar el usuario. Inicie sesión nuevamente.");
            return "redirect:/signin";
        }
        try {
            ticketService.rejectTicket(ticketId);
            ra.addFlashAttribute("exito", "Ticket rechazado.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/devportal/" + rol + "/" + usuario + "/tickets";
    }

    @PostMapping("/{ticketId}/accept")
    public String creadorAceptar(@PathVariable("rol") String rol,
                                 @PathVariable("usuario") String usuario,
                                 @PathVariable("ticketId") Long ticketId,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        Long uid = resolveUsuarioIdByUsername(usuario, session);
        if (uid == null) {
            ra.addFlashAttribute("error", "No se pudo determinar el usuario. Inicie sesión nuevamente.");
            return "redirect:/signin";
        }
        try {
            ticketService.closeTicket(ticketId);
            ra.addFlashAttribute("exito", "Respuesta aceptada. Ticket cerrado.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/devportal/" + rol + "/" + usuario + "/tickets/" + ticketId;
    }

    @PostMapping("/{ticketId}/not-accept")
    public String creadorNoAceptar(@PathVariable("rol") String rol,
                                   @PathVariable("usuario") String usuario,
                                   @PathVariable("ticketId") Long ticketId,
                                   HttpSession session,
                                   RedirectAttributes ra) {
        Long uid = resolveUsuarioIdByUsername(usuario, session);
        if (uid == null) {
            ra.addFlashAttribute("error", "No se pudo determinar el usuario. Inicie sesión nuevamente.");
            return "redirect:/signin";
        }
        try {
            // Cambiar etapa a EN_PROGRESO (es EtapaTicket, no EstadoTicket)
            ticketService.updateTicketStage(ticketId, Ticket.EtapaTicket.EN_PROGRESO);
            ra.addFlashAttribute("exito", "Respuesta no aceptada. Ticket regresó a EN_PROGRESO.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/devportal/" + rol + "/" + usuario + "/tickets/" + ticketId;
    }

    @PostMapping("/{ticketId}/not-accept-reset")
    public String creadorNoAceptarYReset(@PathVariable("rol") String rol,
                                         @PathVariable("usuario") String usuario,
                                         @PathVariable("ticketId") Long ticketId,
                                         HttpSession session,
                                         RedirectAttributes ra) {
        Long uid = resolveUsuarioIdByUsername(usuario, session);
        if (uid == null) {
            ra.addFlashAttribute("error", "No se pudo determinar el usuario. Inicie sesión nuevamente.");
            return "redirect:/signin";
        }
        try {
            // Desasignar y cambiar a ENVIADO
            ticketService.unassignTicket(ticketId);
            ticketService.updateTicketStatus(ticketId, Ticket.EstadoTicket.ENVIADO);
            ticketService.updateTicketStage(ticketId, Ticket.EtapaTicket.PENDIENTE);
            ra.addFlashAttribute("exito", "Respuesta no aceptada y seguimiento reiniciado. El ticket vuelve a PENDIENTE.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/devportal/" + rol + "/" + usuario + "/tickets/" + ticketId;
    }
}
