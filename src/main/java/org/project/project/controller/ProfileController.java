package org.project.project.controller;

import org.project.project.model.entity.Usuario;
import org.project.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/devportal/{role}/{username}/profile")
    public String viewProfile(@PathVariable String role,
                             @PathVariable String username,
                             Model model,
                             Authentication authentication) {
        
        log.info("Viewing profile for: {}", username);
        
        try {
            Usuario currentUser = userService.buscarPorUsername(username);
            
            String authUsername = authentication.getName();
            if (!authUsername.equals(username)) {
                return "redirect:/devportal/" + role + "/" + authUsername + "/dashboard";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("role", role);
            model.addAttribute("rol", role);  // Para compatibilidad con navbar
            model.addAttribute("username", username);
            
            // Si es SuperAdmin, usar vista específica
            if ("sa".equalsIgnoreCase(role)) {
                return "ver-perfil-sa";
            }
            
            return "ver-perfil";
            
        } catch (Exception e) {
            log.error("Error loading profile: {}", e.getMessage());
            return "redirect:/devportal/" + role + "/" + username + "/dashboard";
        }
    }

    @PostMapping("/devportal/{role}/{username}/profile/update")
    @CacheEvict(value = "usuarios", allEntries = true)
    public String updateProfile(@PathVariable String role,
                               @PathVariable String username,
                               @RequestParam(required = false) String telefono,
                               @RequestParam(required = false) String direccionUsuario,
                               @RequestParam(required = false) String estadoCivil,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        
        try {
            String authUsername = authentication.getName();
            if (!authUsername.equals(username)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso");
                return "redirect:/devportal/" + role + "/" + username + "/profile";
            }
            
            Usuario usuario = userService.buscarPorUsername(username);
            
            if (telefono != null) usuario.setTelefono(telefono.trim());
            if (direccionUsuario != null) usuario.setDireccionUsuario(direccionUsuario.trim());
            
            if (estadoCivil != null && !estadoCivil.isEmpty()) {
                usuario.setEstadoCivil(Usuario.EstadoCivil.valueOf(estadoCivil));
            }
            
            userService.actualizarUsuario(usuario.getUsuarioId(), usuario);
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        
        return "redirect:/devportal/" + role + "/" + username + "/profile";
    }

    @PostMapping("/devportal/{role}/{username}/profile/upload-photo")
    @ResponseBody
    @CacheEvict(value = "usuarios", allEntries = true)
    public ResponseEntity<Map<String, Object>> uploadPhoto(@PathVariable String role,
                                                          @PathVariable String username,
                                                          @RequestParam("profilePhoto") MultipartFile file,
                                                          Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String authUsername = authentication.getName();
            if (!authUsername.equals(username)) {
                response.put("success", false);
                response.put("message", "No autorizado");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "El archivo está vacío");
                return ResponseEntity.badRequest().body(response);
            }
            
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "Solo se permiten imágenes");
                return ResponseEntity.badRequest().body(response);
            }
            
            long maxSize = 5 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                response.put("success", false);
                response.put("message", "Archivo muy grande (máximo 5MB)");
                return ResponseEntity.badRequest().body(response);
            }
            
            Usuario usuario = userService.buscarPorUsername(username);
            usuario.setFotoPerfilData(file.getBytes());
            usuario.setFotoPerfilMimeType(contentType);
            usuario.setFotoPerfilSizeBytes(file.getSize());
            usuario.setFotoPerfil(null);
            
            userService.actualizarUsuario(usuario.getUsuarioId(), usuario);
            
            String photoUrl = "/devportal/" + role + "/" + username + "/profile/photo?t=" + System.currentTimeMillis();
            
            response.put("success", true);
            response.put("photoUrl", photoUrl);
            response.put("message", "Foto actualizada correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/devportal/{role}/{username}/profile/photo")
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable String role,
                                                  @PathVariable String username) {
        
        try {
            Usuario usuario = userService.buscarPorUsername(username);
            
            if (usuario.getFotoPerfilData() != null && usuario.getFotoPerfilData().length > 0) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(
                    usuario.getFotoPerfilMimeType() != null 
                        ? usuario.getFotoPerfilMimeType() 
                        : "image/jpeg"
                ));
                headers.setContentLength(usuario.getFotoPerfilData().length);
                headers.setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
                
                return new ResponseEntity<>(usuario.getFotoPerfilData(), headers, HttpStatus.OK);
            }
            else if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isEmpty()) {
                return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(usuario.getFotoPerfil()))
                    .build();
            }
            else {
                return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/img/default-avatar.png"))
                    .build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/img/default-avatar.png"))
                .build();
        }
    }

    @PostMapping("/devportal/{role}/{username}/profile/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changePassword(@PathVariable String role,
                                                             @PathVariable String username,
                                                             @RequestBody Map<String, String> passwordData,
                                                             Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String authUsername = authentication.getName();
            if (!authUsername.equals(username)) {
                response.put("success", false);
                response.put("message", "No autorizado");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            String confirmPassword = passwordData.get("confirmPassword");
            
            if (currentPassword == null || newPassword == null || confirmPassword == null) {
                response.put("success", false);
                response.put("message", "Todos los campos son obligatorios");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "Las contraseñas no coinciden");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (newPassword.length() < 8) {
                response.put("success", false);
                response.put("message", "Mínimo 8 caracteres");
                return ResponseEntity.badRequest().body(response);
            }
            
            Usuario usuario = userService.buscarPorUsername(username);
            
            if (!passwordEncoder.matches(currentPassword, usuario.getHashedPassword())) {
                response.put("success", false);
                response.put("message", "Contraseña actual incorrecta");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            userService.actualizarContrasena(username, newPassword);
            
            response.put("success", true);
            response.put("message", "Contraseña cambiada exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
