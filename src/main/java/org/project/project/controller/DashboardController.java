package org.project.project.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.project.project.model.entity.Usuario;
import org.project.project.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String handleLoginRedirect() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/signin";
        }

        String username = authentication.getName();

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("dev") // Rol por defecto: dev
                .toLowerCase();

        return "redirect:/devportal/" + role + "/" + username + "/dashboard";
    }

    @GetMapping("/devportal/{role}/{username}/dashboard")
    public String showDashboard(@PathVariable String role, 
                                @PathVariable String username, 
                                Model model,
                                HttpServletResponse response) {

        // Añadir cabeceras para prevenir el caché del navegador
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/signin";
        }

        String authUsername = authentication.getName();
        String authRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("dev") // Rol por defecto: dev
                .toLowerCase();

        if (!authUsername.equals(username) || !authRole.equals(role)) {
            return "redirect:/devportal/" + authRole + "/" + authUsername + "/dashboard";
        }

        Usuario usuario = userService.findByUsernameOrEmail(authUsername);
        model.addAttribute("usuario", usuario);

        return role + "/dashboard";
    }
}
