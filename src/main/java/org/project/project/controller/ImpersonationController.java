package org.project.project.controller;

import org.project.project.service.ImpersonacionService;
import org.project.project.service.UserService;
import org.project.project.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/impersonacion")
public class ImpersonationController {

    @Autowired
    private ImpersonacionService impersonacionService;
    
    @Autowired
    private UserService userService;

    @PostMapping("/finalizar")
    @ResponseBody
    public ResponseEntity<?> finalizarImpersonacionActual(HttpSession session, Authentication authentication) {
        try {
            // Obtener información de la impersonación desde la sesión
            Boolean isImpersonating = (Boolean) session.getAttribute("impersonating");
            String originalSuperadminUsername = (String) session.getAttribute("originalSuperadmin");
            Long impersonatedUserId = (Long) session.getAttribute("impersonatedUserId");
            
            if (isImpersonating == null || !isImpersonating) {
                return ResponseEntity.status(400)
                    .body(java.util.Map.of("success", false, "message", "No hay una impersonación activa"));
            }
            
            if (originalSuperadminUsername == null) {
                return ResponseEntity.status(400)
                    .body(java.util.Map.of("success", false, "message", "No se puede restaurar la sesión original"));
            }
            
            // Finalizar la impersonación en la base de datos si tenemos el ID
            if (impersonatedUserId != null) {
                impersonacionService.finalizarImpersonacion(impersonatedUserId);
            }
            
            // RESTAURAR LA SESIÓN DEL SUPERADMIN ORIGINAL
            try {
                Usuario originalSuperadmin = userService.buscarPorUsernameOEmail(originalSuperadminUsername);
                if (originalSuperadmin != null) {
                    // Crear las autoridades del superadmin (ROLE_SA)
                    List<SimpleGrantedAuthority> authorities = originalSuperadmin.getRoles().stream()
                        .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombreRol().name()))
                        .collect(Collectors.toList());
                    
                    // Crear UserDetails para el superadmin
                    UserDetails userDetails = User.builder()
                        .username(originalSuperadmin.getUsername())
                        .password(originalSuperadmin.getHashedPassword())
                        .authorities(authorities)
                        .build();
                    
                    // Crear nueva autenticación para el superadmin
                    UsernamePasswordAuthenticationToken newAuth = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    
                    // Establecer en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                    
                    // Actualizar el contexto de seguridad en la sesión
                    session.setAttribute("SPRING_SECURITY_CONTEXT", 
                        SecurityContextHolder.getContext());
                }
            } catch (Exception e) {
                System.err.println("Error restaurando sesión del superadmin: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Limpiar los atributos de impersonación de la sesión
            session.removeAttribute("impersonating");
            session.removeAttribute("originalSuperadmin");
            session.removeAttribute("impersonatedUserId");
            session.removeAttribute("impersonatedUsername");
            
            return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "message", "Impersonación finalizada exitosamente. Regresando a SuperAdmin...",
                "redirectUrl", "/devportal/sa/impersonar-usuario"
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(java.util.Map.of("success", false, "message", "Error al finalizar impersonación: " + e.getMessage()));
        }
    }

    @GetMapping("/check-status")
    @ResponseBody
    public ResponseEntity<?> checkImpersonationStatus(HttpSession session) {
        try {
            // Verificar si hay una impersonación activa en la sesión
            Boolean isImpersonating = (Boolean) session.getAttribute("impersonating");
            String originalUser = (String) session.getAttribute("originalSuperadmin");
            String impersonatedUsername = (String) session.getAttribute("impersonatedUsername");
            
            return ResponseEntity.ok(java.util.Map.of(
                "isImpersonating", isImpersonating != null && isImpersonating,
                "originalUser", originalUser != null ? originalUser : "",
                "impersonatedUser", impersonatedUsername != null ? impersonatedUsername : ""
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(java.util.Map.of("error", "Error al verificar estado de impersonación: " + e.getMessage()));
        }
    }
}