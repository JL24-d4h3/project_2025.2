package org.project.project.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.project.project.model.entity.Categoria;
import org.project.project.repository.CategoriaHasApiRepository;
import org.project.project.repository.CategoriaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar las categorías y el portal de APIs
 */
@Slf4j
@Controller
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaHasApiRepository categoriaHasApiRepository;
    
    public CategoriaController(CategoriaRepository categoriaRepository, 
                              CategoriaHasApiRepository categoriaHasApiRepository) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaHasApiRepository = categoriaHasApiRepository;
    }

    /**
     * Muestra el portal de APIs con todas las categorías y conteos de APIs
     * 
     * @param role Rol del usuario (po, dev, qa)
     * @param username Nombre de usuario
     * @param model Modelo para la vista
     * @param response Respuesta HTTP para configurar headers de caché
     * @return Nombre de la vista del portal
     */
    @GetMapping("devportal/{role}/{username}/portal")
    public String showApiPortal(@PathVariable String role,
                                @PathVariable String username,
                                Model model,
                                HttpServletResponse response) {
        // Añadir cabeceras para prevenir el caché del navegador
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // Validar autenticación
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Usuario no autenticado intentando acceder al portal");
            return "redirect:/signin";
        }

        // Obtener datos del usuario autenticado
        String authUsername = authentication.getName();
        String authRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("dev")
                .toLowerCase();

        // Verificar autorización - que el usuario acceda a su propia URL
        if (!authUsername.equals(username) || !authRole.equals(role)) {
            log.warn("Usuario {} sin permisos para acceder como {}/{}", authUsername, role, username);
            return "redirect:/devportal/" + authRole + "/" + authUsername + "/portal";
        }

        try {
            // Obtener todas las categorías
            List<Categoria> categorias = categoriaRepository.findAll();
            log.info("📋 Cargando portal para usuario {}: {} categorías encontradas", authUsername, categorias.size());

            // Calcular el conteo de APIs por categoría
            Map<Long, Long> apiCounts = categorias.stream()
                    .collect(Collectors.toMap(
                            Categoria::getIdCategoria,
                            c -> categoriaHasApiRepository.countByCategory_CategoryId(c.getIdCategoria())
                    ));

            // Log de debug para mostrar los conteos
            apiCounts.forEach((categoryId, count) -> 
                log.debug("Categoría ID {}: {} APIs", categoryId, count));
        
            // Añadir atributos al modelo
            model.addAttribute("userRole", role);
            model.addAttribute("role", role);
            model.addAttribute("rol", role);  // Para compatibilidad con navbar
            model.addAttribute("username", username);
            model.addAttribute("categorias", categorias);
            model.addAttribute("apiCounts", apiCounts);
            model.addAttribute("currentNavSection", "catalog");
            
            log.info("✅ Portal cargado exitosamente para {}/{}", role, username);
            return "home/portal";
            
        } catch (Exception e) {
            log.error("❌ Error cargando portal para usuario {}: {}", authUsername, e.getMessage(), e);
            model.addAttribute("error", "Error cargando el portal de APIs");
            return "error/500";
        }
    }
}