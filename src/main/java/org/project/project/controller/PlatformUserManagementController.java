package org.project.project.controller;

import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.Rol;
import org.project.project.service.PlatformUserManagementService;
import org.project.project.service.UserService;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de usuarios de la plataforma
 * Solo accesible para usuarios con rol PO
 */
@Controller
@RequestMapping("/devportal/{rol}/{username}/platform-user-management")
@PreAuthorize("hasRole('PO')")
public class PlatformUserManagementController {

    private static final Logger logger = LoggerFactory.getLogger(PlatformUserManagementController.class);

    private final PlatformUserManagementService platformUserManagementService;
    private final UserService userService;

    public PlatformUserManagementController(PlatformUserManagementService platformUserManagementService,
                                          UserService userService) {
        this.platformUserManagementService = platformUserManagementService;
        this.userService = userService;
    }

    /**
     * Vista principal de gestión de usuarios
     * GET /devportal/{rol}/{username}/platform-user-management
     */
    @GetMapping
    public String showUserManagement(@PathVariable String rol,
                                   @PathVariable String username,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(defaultValue = "fechaCreacion") String sortBy,
                                   @RequestParam(defaultValue = "desc") String sortDir,
                                   @RequestParam(required = false) String search,
                                   Model model,
                                   Principal principal) {

        // ✅ FIX: Validar que principal no sea nulo
        if (principal == null) {
            logger.warn("❌ Principal es nulo - usuario no autenticado intentando acceder a platform-user-management");
            return "redirect:/signin?error=not-authenticated";
        }

        // Verificar acceso
        Usuario currentUser = userService.buscarPorUsername(principal.getName());
        if (!canAccessUserManagement(currentUser, username, rol)) {
            return "redirect:/access-denied";
        }

        // Obtener métricas de la plataforma
        Map<String, Object> metricas = platformUserManagementService.obtenerMetricasPlataforma();

        // Obtener usuarios con paginación
        Page<Usuario> usuariosPage = platformUserManagementService.obtenerUsuariosPlataforma(
            page, size, sortBy, sortDir, search);

        // Obtener roles disponibles para formularios
        List<Rol> rolesDisponibles = platformUserManagementService.obtenerRolesPlataforma();

        // Agregar atributos al modelo
        model.addAttribute("rol", rol);
        model.addAttribute("username", username);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("metricas", metricas);
        model.addAttribute("usuariosPage", usuariosPage);
        model.addAttribute("rolesDisponibles", rolesDisponibles);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        model.addAttribute("currentSort", sortBy);
        model.addAttribute("currentSortDir", sortDir);
        model.addAttribute("currentSearch", search != null ? search : "");
        model.addAttribute("currentNavSection", "platform-user-management");

        return "po/platform-user-management";
    }

    /**
     * Vista de perfil de usuario específico
     * GET /devportal/{rol}/{username}/platform-user-management/view-user-{userId}-profile
     */
    @GetMapping("/view-user-{userId}-profile")
    public String viewUserProfile(@PathVariable String rol,
                                @PathVariable String username,
                                @PathVariable Long userId,
                                Model model,
                                Principal principal) {

        // Verificar acceso
        Usuario currentUser = userService.buscarPorUsername(principal.getName());
        if (!canAccessUserManagement(currentUser, username, rol)) {
            return "redirect:/access-denied";
        }

        try {
            // Obtener usuario por ID
            Usuario usuario = platformUserManagementService.obtenerUsuarioPorId(userId);

            // Agregar al modelo
            model.addAttribute("role", rol);
            model.addAttribute("username", username);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("usuario", usuario);
            model.addAttribute("currentNavSection", "platform-user-management");

            return "po/user-profile-view";

        } catch (Exception e) {
            model.addAttribute("error", "No se pudo cargar el perfil del usuario");
            return "redirect:/devportal/" + rol + "/" + username + "/platform-user-management";
        }
    }

    /**
     * Vista de formulario para crear nuevo usuario
     * GET /devportal/{rol}/{username}/platform-user-management/create-new-user
     */
    @GetMapping("/create-new-user")
    public String showCreateUserForm(@PathVariable String rol,
                                   @PathVariable String username,
                                   Model model,
                                   Principal principal) {

        // Verificar acceso
        Usuario currentUser = userService.buscarPorUsername(principal.getName());
        if (!canAccessUserManagement(currentUser, username, rol)) {
            return "redirect:/access-denied";
        }

        // Obtener roles disponibles
        logger.info("Obteniendo roles disponibles para crear usuario...");
        List<Rol> rolesDisponibles = platformUserManagementService.obtenerRolesPlataforma();
        logger.info("Roles disponibles obtenidos: {}", rolesDisponibles != null ? rolesDisponibles.size() : "null");

        if (rolesDisponibles != null) {
            for (Rol r : rolesDisponibles) {
                logger.info("- Rol disponible: {} (ID: {}, Desc: {})", r.getNombreRol(), r.getRolId(), r.getDescripcionRol());
            }
        }

        model.addAttribute("role", rol);
        model.addAttribute("username", username);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("rolesDisponibles", rolesDisponibles);
        model.addAttribute("usuario", new Usuario()); // Usuario vacío para el formulario

        return "po/create-user-form";
    }

    /**
     * Vista de formulario para invitar usuario
     * GET /devportal/{rol}/{username}/platform-user-management/invite-new-user
     */
    @GetMapping("/invite-new-user")
    public String showInviteUserForm(@PathVariable String rol,
                                   @PathVariable String username,
                                   Model model,
                                   Principal principal) {

        logger.info("=== INICIANDO showInviteUserForm ===");
        logger.info("Rol: {}, Username: {}, Principal: {}", rol, username, principal.getName());

        try {
            // Verificar acceso
            Usuario currentUser = userService.buscarPorUsername(principal.getName());
            logger.info("Usuario actual encontrado: {}", currentUser != null ? currentUser.getUsername() : "null");

            if (!canAccessUserManagement(currentUser, username, rol)) {
                logger.warn("Acceso denegado para usuario: {}", principal.getName());
                return "redirect:/access-denied";
            }

            // Obtener roles disponibles
            logger.info("Obteniendo roles disponibles...");
            List<Rol> rolesDisponibles = platformUserManagementService.obtenerRolesPlataforma();
            logger.info("Roles disponibles obtenidos: {}", rolesDisponibles != null ? rolesDisponibles.size() : "null");

            // Agregar atributos al modelo
            model.addAttribute("rol", rol);  // CORREGIDO: era "role" ahora es "rol"
            model.addAttribute("username", username);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("rolesDisponibles", rolesDisponibles);
            model.addAttribute("currentNavSection", "platform-user-management");

            logger.info("Modelo configurado correctamente, retornando vista: po/invite-user-form");
            return "po/invite-user-form";

        } catch (Exception e) {
            logger.error("Error en showInviteUserForm: ", e);
            model.addAttribute("error", "Error al cargar formulario de invitación: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/platform-user-management";
        }
    }

    // ================ API ENDPOINTS ================

    /**
     * API: Buscar usuarios con filtros AJAX
     * GET /devportal/{rol}/{username}/platform-user-management/api/search
     */
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchUsers(@PathVariable String rol,
                                                          @PathVariable String username,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(defaultValue = "fechaCreacion") String sortBy,
                                                          @RequestParam(defaultValue = "desc") String sortDir,
                                                          @RequestParam(required = false) String search,
                                                          Principal principal) {

        try {
            // Verificar acceso
            Usuario currentUser = userService.buscarPorUsername(principal.getName());
            if (!canAccessUserManagement(currentUser, username, rol)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Obtener usuarios
            Page<Usuario> usuariosPage = platformUserManagementService.obtenerUsuariosPlataforma(
                page, size, sortBy, sortDir, search);

            // Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("content", usuariosPage.getContent());
            response.put("totalElements", usuariosPage.getTotalElements());
            response.put("totalPages", usuariosPage.getTotalPages());
            response.put("currentPage", page);
            response.put("hasNext", usuariosPage.hasNext());
            response.put("hasPrevious", usuariosPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Error al buscar usuarios");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * API: Crear nuevo usuario
     * POST /devportal/{rol}/{username}/platform-user-management/api/create-user
     */
    @PostMapping("/api/create-user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createUser(@PathVariable String rol,
                                                         @PathVariable String username,
                                                         @RequestBody Map<String, Object> userData,
                                                         Principal principal) {

        try {
            // Verificar acceso
            Usuario currentUser = userService.buscarPorUsername(principal.getName());
            if (!canAccessUserManagement(currentUser, username, rol)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Crear usuario desde datos del formulario
            Usuario nuevoUsuario = mapearDatosUsuario(userData);
            List<Long> roleIds = mapearRoleIds(userData.get("roles"));

            // Crear usuario
            Usuario usuarioCreado = platformUserManagementService.crearUsuarioPlataforma(nuevoUsuario, roleIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario creado exitosamente");
            response.put("userId", usuarioCreado.getUsuarioId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al crear usuario: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * API: Invitar usuario
     * POST /devportal/{rol}/{username}/platform-user-management/api/invite-user
     */
    @PostMapping("/api/invite-user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> inviteUser(@PathVariable String rol,
                                                         @PathVariable String username,
                                                         @RequestBody Map<String, Object> inviteData,
                                                         Principal principal) {

        try {
            // Verificar acceso
            Usuario currentUser = userService.buscarPorUsername(principal.getName());
            if (!canAccessUserManagement(currentUser, username, rol)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            String email = (String) inviteData.get("email");
            Long rolId = Long.valueOf(inviteData.get("rolId").toString());

            // Crear invitación
            Usuario usuarioInvitado = platformUserManagementService.crearInvitacionUsuario(email, rolId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invitación enviada exitosamente a " + email);
            response.put("userId", usuarioInvitado.getUsuarioId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al enviar invitación: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * API: Habilitar usuario
     * PATCH /devportal/{rol}/{username}/platform-user-management/api/{userId}/enable
     */
    @PatchMapping("/api/{userId}/enable")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> enableUser(@PathVariable String rol,
                                                         @PathVariable String username,
                                                         @PathVariable Long userId,
                                                         Principal principal) {

        logger.info("=== INICIANDO enableUser ===");
        logger.info("userId: {}, rol: {}, username: {}, principal: {}", userId, rol, username, principal.getName());

        try {
            // Verificar acceso
            Usuario currentUser = userService.buscarPorUsername(principal.getName());
            logger.info("Usuario que solicita habilitar: {}", currentUser != null ? currentUser.getUsername() : "null");

            if (!canAccessUserManagement(currentUser, username, rol)) {
                logger.warn("ACCESO DENEGADO para habilitar usuario. CurrentUser: {}, TargetUser: {}, Rol: {}",
                    currentUser != null ? currentUser.getUsername() : "null", username, rol);
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Acceso denegado para habilitar usuario");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            logger.info("✅ ACCESO CONCEDIDO - Llamando al servicio para habilitar usuario ID: {}", userId);
            Usuario usuario = platformUserManagementService.habilitarUsuario(userId);
            logger.info("✅ HABILITAR EXITOSO - Usuario: {}, Nuevo estado: {}", usuario.getUsername(), usuario.getEstadoUsuario());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario habilitado exitosamente");
            response.put("newStatus", usuario.getEstadoUsuario().name());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error al habilitar usuario: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al habilitar usuario: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * API: Inhabilitar usuario
     * PATCH /devportal/{rol}/{username}/platform-user-management/api/{userId}/disable
     */
    @PatchMapping("/api/{userId}/disable")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> disableUser(@PathVariable String rol,
                                                          @PathVariable String username,
                                                          @PathVariable Long userId,
                                                          Principal principal) {

        logger.info("=== INICIANDO disableUser ===");
        logger.info("userId: {}, rol: {}, username: {}, principal: {}", userId, rol, username, principal.getName());

        try {
            // Verificar acceso
            Usuario currentUser = userService.buscarPorUsername(principal.getName());
            logger.info("Usuario que solicita inhabilitar: {}", currentUser != null ? currentUser.getUsername() : "null");

            if (!canAccessUserManagement(currentUser, username, rol)) {
                logger.warn("ACCESO DENEGADO para inhabilitar usuario. CurrentUser: {}, TargetUser: {}, Rol: {}",
                    currentUser != null ? currentUser.getUsername() : "null", username, rol);
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Acceso denegado para inhabilitar usuario");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            logger.info("✅ ACCESO CONCEDIDO - Llamando al servicio para inhabilitar usuario ID: {}", userId);
            Usuario usuario = platformUserManagementService.inhabilitarUsuario(userId);
            logger.info("✅ INHABILITAR EXITOSO - Usuario: {}, Nuevo estado: {}", usuario.getUsername(), usuario.getEstadoUsuario());
            logger.info("Usuario inhabilitado. Nuevo estado: {}", usuario.getEstadoUsuario());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario inhabilitado exitosamente");
            response.put("newStatus", usuario.getEstadoUsuario().name());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error al inhabilitar usuario: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al inhabilitar usuario: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * API: Eliminar usuario
     * DELETE /devportal/{rol}/{username}/platform-user-management/api/{userId}
     */
    @DeleteMapping("/api/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String rol,
                                                         @PathVariable String username,
                                                         @PathVariable Long userId,
                                                         Principal principal) {

        logger.info("╔════════════════════════════════════════════════════════════════╗");
        logger.info("║          INICIO DELETE USER - CONTROLLER                      ║");
        logger.info("╚════════════════════════════════════════════════════════════════╝");
        logger.info("📋 Parámetros recibidos:");
        logger.info("   - userId: {} (type: {})", userId, userId.getClass().getSimpleName());
        logger.info("   - rol: {}", rol);
        logger.info("   - username: {}", username);
        logger.info("   - principal: {}", principal != null ? principal.getName() : "NULL");

        try {
            logger.info("🔐 PASO 1/4: Verificando acceso del usuario actual...");
            // Verificar acceso
            Usuario currentUser = userService.buscarPorUsername(principal.getName());
            logger.info("   ✅ Usuario actual encontrado: {} (ID: {})", currentUser.getUsername(), currentUser.getUsuarioId());
            
            if (!canAccessUserManagement(currentUser, username, rol)) {
                logger.warn("   ❌ ACCESO DENEGADO - Usuario no tiene permisos");
                logger.warn("   - Usuario actual: {}", currentUser.getUsername());
                logger.warn("   - Username solicitado: {}", username);
                logger.warn("   - Rol solicitado: {}", rol);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            logger.info("   ✅ Acceso verificado correctamente");

            logger.info("🔍 PASO 2/4: Verificando existencia del usuario a eliminar...");
            Usuario usuarioAEliminar = platformUserManagementService.obtenerUsuarioPorId(userId);
            logger.info("   ✅ Usuario a eliminar encontrado:");
            logger.info("      - ID: {}", usuarioAEliminar.getUsuarioId());
            logger.info("      - Username: {}", usuarioAEliminar.getUsername());
            logger.info("      - Email: {}", usuarioAEliminar.getCorreo());
            logger.info("      - Estado: {}", usuarioAEliminar.getEstadoUsuario());
            logger.info("      - Roles: {}", usuarioAEliminar.getRoles().stream()
                .map(r -> r.getNombreRol().toString())
                .toList());

            logger.info("🗑️ PASO 3/4: Llamando al servicio para ELIMINAR usuario (FORZADO)...");
            
            // Usar eliminación forzada por defecto para asegurar que funcione
            platformUserManagementService.eliminarUsuario(userId, true);
            
            logger.info("✅ PASO 4/4: Usuario eliminado exitosamente");
            logger.info("╔════════════════════════════════════════════════════════════════╗");
            logger.info("║          DELETE USER COMPLETADO - CONTROLLER                  ║");
            logger.info("╚════════════════════════════════════════════════════════════════╝");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario eliminado exitosamente");

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            logger.error("❌ ERROR: Usuario no encontrado");
            logger.error("   - userId buscado: {}", userId);
            logger.error("   - Mensaje: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Usuario no encontrado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            
        } catch (Exception e) {
            logger.error("╔════════════════════════════════════════════════════════════════╗");
            logger.error("║          ERROR CRÍTICO EN DELETE USER                         ║");
            logger.error("╚════════════════════════════════════════════════════════════════╝");
            logger.error("❌ Tipo de excepción: {}", e.getClass().getName());
            logger.error("❌ Mensaje de error: {}", e.getMessage());
            logger.error("❌ Stack trace completo:", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al eliminar usuario: " + e.getMessage());
            error.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ================ MÉTODOS AUXILIARES ================

    private boolean canAccessUserManagement(Usuario currentUser, String requestedUsername, String requestedRole) {
        // Solo POs pueden acceder a gestión de usuarios
        boolean hasPoRole = currentUser.getRoles().stream()
            .anyMatch(rol -> rol.getNombreRol() == Rol.NombreRol.PO);

        // Verificar que coincida username y rol
        boolean matchesUserAndRole = currentUser.getUsername().equals(requestedUsername) &&
                                   requestedRole.equalsIgnoreCase("po");

        return hasPoRole && matchesUserAndRole;
    }

    private Usuario mapearDatosUsuario(Map<String, Object> datos) {
        Usuario usuario = new Usuario();

        usuario.setUsername((String) datos.get("username"));
        usuario.setNombreUsuario((String) datos.get("nombreUsuario"));
        usuario.setApellidoPaterno((String) datos.get("apellidoPaterno"));
        usuario.setApellidoMaterno((String) datos.get("apellidoMaterno"));
        usuario.setDni((String) datos.get("dni"));
        usuario.setCorreo((String) datos.get("correo"));
        usuario.setTelefono((String) datos.get("telefono"));
        usuario.setDireccionUsuario((String) datos.get("direccionUsuario"));
        usuario.setHashedPassword((String) datos.get("password"));

        // Campos opcionales
        if (datos.get("fotoPerfil") != null) {
            usuario.setFotoPerfil((String) datos.get("fotoPerfil"));
        }

        return usuario;
    }

    private List<Long> mapearRoleIds(Object rolesObj) {
        if (rolesObj == null) {
            return new ArrayList<>();
        }

        if (rolesObj instanceof List) {
            List<?> rolesList = (List<?>) rolesObj;
            List<Long> roleIds = new ArrayList<>();

            for (Object roleItem : rolesList) {
                if (roleItem instanceof Number) {
                    roleIds.add(((Number) roleItem).longValue());
                } else if (roleItem instanceof String) {
                    try {
                        roleIds.add(Long.valueOf((String) roleItem));
                    } catch (NumberFormatException e) {
                        // Ignorar valores inválidos
                    }
                }
            }

            return roleIds;
        }

        return new ArrayList<>();
    }

    // =================== ENDPOINTS PARA PROCESAR FORMULARIOS ===================

    /**
     * Procesar creación de nuevo usuario
     * POST /devportal/{rol}/{username}/platform-user-management/create-user
     */
    @PostMapping("/create-user")
    public String createUser(@PathVariable String rol,
                           @PathVariable String username,
                           @ModelAttribute Usuario usuario,
                           @RequestParam Long rolId,
                           @RequestParam(defaultValue = "false") boolean sendWelcomeEmail,
                           RedirectAttributes redirectAttributes,
                           Principal principal) {

        logger.info("=== INICIANDO createUser ===");
        logger.info("Parámetros recibidos:");
        logger.info("- rol: {}", rol);
        logger.info("- username: {}", username);
        logger.info("- rolId: {}", rolId);
        logger.info("- sendWelcomeEmail: {}", sendWelcomeEmail);
        logger.info("- usuario.username: {}", usuario.getUsername());
        logger.info("- usuario.correo: {}", usuario.getCorreo());
        logger.info("- usuario.nombreUsuario: {}", usuario.getNombreUsuario());
        logger.info("- usuario.apellidoPaterno: {}", usuario.getApellidoPaterno());
        logger.info("- usuario.dni: {}", usuario.getDni());

        try {
            // Verificar acceso
            logger.info("Verificando acceso del usuario...");
            Usuario currentUser = userService.buscarPorUsername(principal.getName());
            logger.info("Usuario actual: {}", currentUser != null ? currentUser.getUsername() : "null");

            if (!canAccessUserManagement(currentUser, username, rol)) {
                logger.warn("Acceso denegado para usuario: {}", principal.getName());
                return "redirect:/access-denied";
            }

            // Crear el usuario usando el servicio
            logger.info("Llamando al servicio para crear usuario...");
            List<Long> roleIds = Arrays.asList(rolId);
            Usuario nuevoUsuario = platformUserManagementService.crearUsuarioPlataforma(
                usuario, roleIds);

            logger.info("Usuario creado exitosamente con ID: {}", nuevoUsuario.getUsuarioId());

            redirectAttributes.addFlashAttribute("successMessage",
                "Usuario '" + nuevoUsuario.getUsername() + "' creado exitosamente. Se ha enviado un enlace al correo " +
                nuevoUsuario.getCorreo() + " para configurar su contraseña.");

            return "redirect:/devportal/" + rol + "/" + username + "/platform-user-management";

        } catch (Exception e) {
            logger.error("Error al crear usuario: ", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error al crear usuario: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/platform-user-management/create-new-user";
        }
    }

    /**
     * Procesar envío de invitación
     * POST /devportal/{rol}/{username}/platform-user-management/send-invitation
     */
    @PostMapping("/send-invitation")
    public String sendInvitation(@PathVariable String rol,
                               @PathVariable String username,
                               @RequestParam String email,
                               @RequestParam(required = false) String nombre,
                               @RequestParam(required = false) String apellido,
                               @RequestParam Long rolId,
                               @RequestParam(defaultValue = "14") int diasExpiracion,
                               @RequestParam(required = false) String mensajePersonalizado,
                               @RequestParam(defaultValue = "true") boolean requirePasswordChange,
                               @RequestParam(defaultValue = "false") boolean sendCopyToSender,
                               RedirectAttributes redirectAttributes,
                               Principal principal) {

        logger.info("=== INICIANDO sendInvitation ===");
        logger.info("Parámetros recibidos:");
        logger.info("- rol: {}", rol);
        logger.info("- username: {}", username);
        logger.info("- email: {}", email);
        logger.info("- nombre: {}", nombre);
        logger.info("- apellido: {}", apellido);
        logger.info("- rolId: {}", rolId);
        logger.info("- diasExpiracion: {}", diasExpiracion);
        logger.info("- mensajePersonalizado: {}", mensajePersonalizado);
        logger.info("- requirePasswordChange: {}", requirePasswordChange);
        logger.info("- sendCopyToSender: {}", sendCopyToSender);
        logger.info("- principal: {}", principal.getName());

        try {
            // Verificar acceso
            logger.info("Verficando acceso del usuario...");
            Usuario currentUser = userService.buscarPorUsername(principal.getName());
            logger.info("Usuario actual: {}", currentUser != null ? currentUser.getUsername() : "null");

            if (!canAccessUserManagement(currentUser, username, rol)) {
                logger.warn("Acceso denegado para usuario: {}", principal.getName());
                return "redirect:/access-denied";
            }

            // Llamar al servicio para crear la invitación
            logger.info("Llamando al servicio para crear invitación...");
            Usuario usuarioInvitado = platformUserManagementService.crearInvitacionUsuario(email, rolId);
            logger.info("Invitación creada exitosamente para usuario ID: {}", usuarioInvitado.getUsuarioId());

            redirectAttributes.addFlashAttribute("successMessage",
                "Invitación enviada exitosamente a " + email);

            logger.info("Rediriegiendo a la página principal de gestión de usuarios");
            return "redirect:/devportal/" + rol + "/" + username + "/platform-user-management";

        } catch (Exception e) {
            logger.error("Error al enviar invitación: ", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error al enviar invitación: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/platform-user-management/invite-new-user";
        }
    }
}