package org.project.project.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.project.project.model.dto.DashboardStatsDTO;
import org.project.project.model.dto.TeamInfoDTO;
import org.project.project.model.dto.TeamMemberDTO;
import org.project.project.service.DashboardService;
import org.project.project.service.UserService;
import org.project.project.model.entity.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador para el Dashboard - Completamente nuevo
 */
@Controller
public class DashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    private final DashboardService dashboardService;
    private final UserService userService;
    
    public DashboardController(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }
    
    // =================== DASHBOARD PRINCIPAL ===================
    
    @GetMapping("/devportal/{role}/{username}/dashboard")
    public String showDashboard(@PathVariable String role, 
                                @PathVariable String username, 
                                Authentication authentication, 
                                Model model,
                                HttpServletResponse response) {
        // 🔒 Headers de cache-control para prevenir cache del dashboard
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        logger.info("╔══════════════════════════════════════════════════════════════════════════");
        logger.info("║ 🎯 [DASHBOARD CONTROLLER] MÉTODO EJECUTADO - showDashboard()");
        logger.info("║ 📍 Path Variables: role='{}', username='{}'", role, username);
        logger.info("║ 👤 Authentication object: {}", authentication != null ? authentication.getClass().getName() : "NULL");
        logger.info("║ 🔑 Principal: {}", authentication != null ? authentication.getName() : "NULL");
        logger.info("║ ⚡ Authorities: {}", authentication != null ? authentication.getAuthorities() : "N/A");
        logger.info("║ 📊 Model: {}", model.getClass().getName());
        logger.info("╚══════════════════════════════════════════════════════════════════════════");
        
        try {
            // Verificar autenticación y autorización
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("╔══════════════════════════════════════════════════════════════════════════");
                logger.warn("║ ⚠️ [DASHBOARD CONTROLLER] Authentication failed");
                logger.warn("║ 🔴 Redirecting to signin with fromDashboard=true");
                logger.warn("╚══════════════════════════════════════════════════════════════════════════");
                return "redirect:/signin?fromDashboard=true";
            }
            
            String authenticatedUsername = authentication.getName();
            logger.info("✅ [DASHBOARD CONTROLLER] Authentication OK - Username: {}", authenticatedUsername);
            
            if (!authenticatedUsername.equals(username)) {
                logger.warn("Username mismatch: {} vs {}", authenticatedUsername, username);
                return "error/403";
            }
            
            // Validar rol
            if (!role.matches("dev|po|qa")) {
                logger.warn("Invalid role: {}", role);
                return "error/404";
            }
            
            logger.info("Searching for user: {}", username);
            // Obtener usuario
            Usuario usuario = null;
            try {
                usuario = userService.buscarPorUsername(username);
            } catch (Exception e) {
                logger.error("Error searching for user {}: {}", username, e.getMessage(), e);
                model.addAttribute("error", "Error buscando usuario: " + e.getMessage());
                return "error/500";
            }
            
            if (usuario == null) {
                logger.error("User not found: {}", username);
                return "error/404";
            }
            
            logger.info("User found: {} (ID: {})", usuario.getNombreUsuario(), usuario.getUsuarioId());
            
            // Obtener estadísticas del dashboard
            logger.info("Getting dashboard stats for user ID: {}", usuario.getUsuarioId());
            DashboardStatsDTO stats = dashboardService.obtenerEstadisticasCompletas(usuario.getUsuarioId());
            logger.info("Stats obtained: Projects={}, Teams={}, Repos={}", stats.getTotalProjects(), stats.getTotalTeams(), stats.getTotalRepositories());
            
            // Obtener equipos del usuario
            logger.info("Getting teams for user ID: {}", usuario.getUsuarioId());
            List<TeamInfoDTO> equipos = dashboardService.obtenerEquiposDelUsuario(usuario.getUsuarioId());
            logger.info("Teams found: {}", equipos.size());
            
            // Agregar datos al modelo
            model.addAttribute("username", username);
            model.addAttribute("user", usuario);
            model.addAttribute("Usuario", usuario);  // Para chatbot widget
            model.addAttribute("role", role);
            model.addAttribute("rol", role);  // Para compatibilidad con navbar
            model.addAttribute("stats", stats);
            model.addAttribute("equipos", equipos);
            model.addAttribute("currentNavSection", "dashboard");
            
            String viewName = role + "/dashboard";
            logger.info("Returning view: {}", viewName);
            
            // Retornar la vista correspondiente al rol
            return viewName;
            
        } catch (Exception e) {
            logger.error("Dashboard error for role={}, username={}", role, username, e);
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
            return "error/500";
        }
    }

    
    // =================== API ENDPOINTS ===================
    
    /**
     * API para obtener estadísticas del dashboard
     */
    @GetMapping("/devportal/{role}/{username}/api/stats")
    @ResponseBody
    public ResponseEntity<DashboardStatsDTO> getStats(
            @PathVariable String role, 
            @PathVariable String username, 
            Authentication authentication) {
        
        try {
            // Verificar autenticación
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).build();
            }
            
            String authenticatedUsername = authentication.getName();
            if (!authenticatedUsername.equals(username)) {
                return ResponseEntity.status(403).build();
            }
            
            // Obtener usuario
            Usuario usuario = userService.buscarPorUsername(username);
            if (usuario == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Obtener estadísticas
            DashboardStatsDTO stats = dashboardService.obtenerEstadisticasCompletas(usuario.getUsuarioId());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * API para obtener miembros de un equipo
     */
    @GetMapping("/devportal/{role}/{username}/api/teams/{teamId}/members")
    @ResponseBody
    public ResponseEntity<List<TeamMemberDTO>> getTeamMembers(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable Integer teamId,
            Authentication authentication) {
        
        try {
            // Verificar autenticación
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).build();
            }
            
            String authenticatedUsername = authentication.getName();
            if (!authenticatedUsername.equals(username)) {
                return ResponseEntity.status(403).build();
            }
            
            // Obtener miembros del equipo
            List<TeamMemberDTO> miembros = dashboardService.obtenerMiembrosDelEquipo(teamId);
            
            return ResponseEntity.ok(miembros);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * API para obtener equipos del usuario
     */
    @GetMapping("/devportal/{role}/{username}/api/teams")
    @ResponseBody
    public ResponseEntity<List<TeamInfoDTO>> getUserTeams(
            @PathVariable String role,
            @PathVariable String username,
            Authentication authentication) {
        
        try {
            // Verificar autenticación
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).build();
            }
            
            String authenticatedUsername = authentication.getName();
            if (!authenticatedUsername.equals(username)) {
                return ResponseEntity.status(403).build();
            }
            
            // Obtener usuario
            Usuario usuario = userService.buscarPorUsername(username);
            if (usuario == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Obtener equipos
            List<TeamInfoDTO> equipos = dashboardService.obtenerEquiposDelUsuario(usuario.getUsuarioId());
            
            return ResponseEntity.ok(equipos);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}