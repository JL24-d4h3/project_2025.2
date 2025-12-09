package org.project.project.controller;

import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.Rol;
import org.project.project.model.entity.Impersonacion;
import org.project.project.model.entity.Categoria;
import org.project.project.service.UserService;
import org.project.project.service.ImpersonacionService;
import org.project.project.service.CategoriaService;
import org.project.project.service.PlatformUserManagementService;
import org.project.project.service.SystemAdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@PreAuthorize("hasRole('SA')")
public class SystemAdministratorController {

    private static final Logger log = LoggerFactory.getLogger(SystemAdministratorController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ImpersonacionService impersonacionService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private PlatformUserManagementService platformUserManagementService;

    @Autowired
    private SystemAdministratorService systemAdministratorService;

    // ==========================================
    // HELPER METHODS
    // ==========================================

    /**
     * Genera una ruta dinámica para vistas basada en el rol del usuario
     * @param role Rol del usuario (sa, po, dev, qa)
     * @param viewName Nombre de la vista
     * @return Ruta completa de la vista
     */
    private String buildViewPath(String role, String viewName) {
        return role.toLowerCase() + "/" + viewName;
    }

    /**
     * Genera una redirección dinámica basada en el rol y username
     * @param role Rol del usuario (sa, po, dev, qa)
     * @param username Username del usuario
     * @param page Página de destino
     * @return URL de redirección completa
     */
    private String buildRedirectPath(String role, String username, String page) {
        return "redirect:/devportal/" + role.toLowerCase() + "/" + username + "/" + page;
    }

    // ==========================================
    // RUTAS CON PATRÓN /devportal/sa/{username}/ (originales)
    // ==========================================

    @GetMapping("/devportal/sa/{username}/dashboard")
    public String superadminDashboard(@PathVariable String username, Model model, Authentication authentication) {
        try {
            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());

            // Verificar que el username coincida con el usuario autenticado
            if (!currentUser.getUsername().equals(username)) {
                return "redirect:/devportal/sa/" + currentUser.getUsername() + "/dashboard-panel";
            }

            // Redirigir directamente al dashboard-panel (nueva página principal)
            return "redirect:/devportal/sa/" + username + "/dashboard-panel";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/signin";
        }
    }

    // ==========================================
    // RUTAS CON PATRÓN /devportal/sa/ (para funcionalidades específicas)
    // ==========================================

    @GetMapping("/devportal/sa/crear")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        return buildViewPath("sa", "create-user-fill");
    }

    @PostMapping("/devportal/sa/crear")
    public String processCreateForm(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            Usuario savedUser = userService.guardarUsuario(usuario);
            redirectAttributes.addFlashAttribute("successMessage", "¡Usuario " + savedUser.getUsername() + " creado con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el usuario: " + e.getMessage());
        }
        return "redirect:/devportal/sa/crear";
    }

    @GetMapping("/devportal/sa/listar")
    public String listUsers(Model model) {
        List<Usuario> usuarios = userService.listarUsuarios();
        model.addAttribute("usuarios", usuarios);
        System.out.println(usuarios);
        return buildViewPath("sa", "manage-users");
    }

    @DeleteMapping("/devportal/sa/listar/{id}")
    public String eliminarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes, @PathVariable Long id) {

        try {
            redirectAttributes.addFlashAttribute("successMessage", "¡Usuario " + userService.eliminarUsuario(id).getUsername() + " eliminado con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/devportal/sa/listar";
    }

    @GetMapping("/devportal/sa/{username}/manage-users")
    public String gestionarUsuarios(@PathVariable String username, Model model, Authentication authentication) {
        try {
            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());

            // Verificar que el username coincida con el usuario autenticado
            if (!currentUser.getUsername().equals(username)) {
                return buildRedirectPath("sa", currentUser.getUsername(), "manage-users");
            }

            String currentRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .orElse("USER");

            // Obtener TODOS los usuarios de la base de datos sin filtrar por rol ni estado
            // Esto incluye: SA, PO, DEV, QA, usuarios creados manualmente, invitados por PO, etc.
            List<Usuario> usuarios = userService.listarUsuarios();

            model.addAttribute("usuario", currentUser);
            model.addAttribute("role", currentRole);
            model.addAttribute("usuarios", usuarios);

            return buildViewPath(currentRole, "manage-users");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error al cargar la lista de usuarios: " + e.getMessage());
            return buildViewPath("sa", "manage-users");
        }
    }

    @PostMapping("/devportal/sa/toggle-estado/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleEstadoUsuario(@PathVariable Long id) {
        try {
            Usuario u = userService.buscarUsuarioPorId(id);
            if (u == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Usuario no encontrado"));
            }

            Usuario.EstadoUsuario nuevoEstado = u.getEstadoUsuario().name().equals("HABILITADO") ?
                    Usuario.EstadoUsuario.INHABILITADO : Usuario.EstadoUsuario.HABILITADO;

            u.setEstadoUsuario(nuevoEstado);
            userService.actualizarUsuario(id, u);

            return ResponseEntity.ok(Map.of("success", true, "nuevoEstado", nuevoEstado.name()));
        } catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error al cambiar el estado: " + e.getMessage()));
        }
    }

    @PostMapping("/devportal/sa/usuarios/{id}/estado")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoUsuario(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            Usuario u = userService.buscarUsuarioPorId(id);
            if (u == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Usuario no encontrado"));
            }

            String estadoString = request.get("estado");
            if (estadoString == null || estadoString.trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("success", false, "message", "Estado no proporcionado"));
            }

            Usuario.EstadoUsuario nuevoEstado;
            try {
                nuevoEstado = Usuario.EstadoUsuario.valueOf(estadoString.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(400).body(Map.of("success", false, "message", "Estado inválido: " + estadoString));
            }

            u.setEstadoUsuario(nuevoEstado);
            userService.actualizarUsuario(id, u);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "nuevoEstado", nuevoEstado.name(),
                    "message", "Estado del usuario actualizado correctamente"
            ));
        } catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error al cambiar el estado: " + e.getMessage()));
        }
    }

    @GetMapping("/devportal/sa/{username}/dashboard-panel")
    @PreAuthorize("hasRole('SA')")
    public String verPanelDashboard(@PathVariable String username, Model model, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/signin";
            }

            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());
            if (currentUser == null) {
                return "redirect:/signin";
            }

            // Verificar que el username coincida con el usuario autenticado
            if (!currentUser.getUsername().equals(username)) {
                return buildRedirectPath("sa", currentUser.getUsername(), "dashboard-panel");
            }

            String currentRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .orElse("USER");

            // Obtener métricas del dashboard usando el nuevo servicio
            Map<String, Object> dashboardMetrics = systemAdministratorService.getAllGeneralMetrics();

            model.addAttribute("usuario", currentUser);
            model.addAttribute("role", currentRole);
            model.addAttribute("metrics", dashboardMetrics);

            return buildViewPath(currentRole, "dashboard-panel");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error al cargar el panel de dashboard: " + e.getMessage());
            return "redirect:/signin";
        }
    }

    @GetMapping("/devportal/sa/dashboard-metrics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
        try {
            Map<String, Object> metrics = systemAdministratorService.getAllGeneralMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error al obtener métricas: " + e.getMessage()));
        }
    }

    // ==========================================
    // RUTAS DE COMPATIBILIDAD Y REDIRECCIONES
    // ==========================================

    // ==========================================
    // RUTAS DE COMPATIBILIDAD (REDIRECCIONES DE RUTAS ANTIGUAS)
    // ==========================================

    @GetMapping("/devportal/sa/crear-nuevo-usuario")
    public String crearNuevoUsuarioRedirect(Authentication authentication) {
        try {
            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());
            return buildRedirectPath("sa", currentUser.getUsername(), "create-new-user");
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/signin";
        }
    }

    @GetMapping("/devportal/sa/gestionar-usuarios")
    public String gestionarUsuariosRedirect(Authentication authentication) {
        try {
            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());
            return buildRedirectPath("sa", currentUser.getUsername(), "manage-users");
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/signin";
        }
    }

    @GetMapping("/devportal/sa/ver-panel-dashboard")
    public String verPanelDashboardRedirect(Authentication authentication) {
        try {
            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());
            return buildRedirectPath("sa", currentUser.getUsername(), "dashboard-panel");
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/signin";
        }
    }

    @GetMapping("/devportal/sa/impersonar-usuario")
    public String impersonarUsuarioRedirect(Authentication authentication) {
        try {
            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());
            return buildRedirectPath("sa", currentUser.getUsername(), "impersonate-user");
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/signin";
        }
    }

    @GetMapping("/devportal/sa/gestionar-categorias")
    public String gestionarCategoriasRedirect(Authentication authentication) {
        try {
            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());
            return buildRedirectPath("sa", currentUser.getUsername(), "manage-categories");
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/signin";
        }
    }

    @GetMapping("/devportal/sa")
    public String superadminHomeRedirect(Authentication authentication) {
        try {
            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());
            return buildRedirectPath("sa", currentUser.getUsername(), "dashboard-panel");
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/signin";
        }
    }

    // ==========================================
    // FUNCIONALIDAD DE IMPERSONACIÓN
    // ==========================================

    @GetMapping("/devportal/sa/{username}/impersonate-user")
    public String mostrarVistaPimpersonacion(@PathVariable String username, Model model, Authentication authentication) {
        try {
            // ✅ FIX: Validar que authentication no sea nulo
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("❌ Authentication es nulo o no autenticado - redirigiendo a signin");
                return "redirect:/signin?error=not-authenticated";
            }

            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());
            
            // ✅ FIX: Validar que el usuario actual exista
            if (currentUser == null) {
                log.warn("❌ Usuario no encontrado para authentication.getName(): {}", authentication.getName());
                return "redirect:/signin?error=user-not-found";
            }

            // Verificar que el username coincida con el usuario autenticado
            if (!currentUser.getUsername().equals(username)) {
                return buildRedirectPath("sa", currentUser.getUsername(), "impersonate-user");
            }

            String currentRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .orElse("USER");

            // Obtener TODOS los usuarios de la base de datos sin filtrar por rol ni estado
            // Esto incluye: SA, PO, DEV, QA, usuarios creados manualmente, invitados por PO, etc.
            List<Usuario> usuarios = new java.util.ArrayList<>();
            try {
                usuarios = userService.listarUsuarios();
                log.info("Usuarios cargados para impersonación: {}", usuarios.size());
            } catch (Exception e) {
                log.error("Error obteniendo usuarios: ", e);
                usuarios = new java.util.ArrayList<>();
            }

            // Obtener impersonaciones activas de forma segura
            List<Impersonacion> impersonacionesActivas = new java.util.ArrayList<>();
            try {
                impersonacionesActivas = impersonacionService.obtenerImpersonacionesActivas();
            } catch (Exception e) {
                System.out.println("Error obteniendo impersonaciones activas: " + e.getMessage());
                impersonacionesActivas = new java.util.ArrayList<>();
            }

            model.addAttribute("usuario", currentUser);
            model.addAttribute("role", currentRole);
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("impersonacionesActivas", impersonacionesActivas);

            return buildViewPath(currentRole, "impersonate-user");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error al cargar la vista de impersonación: " + e.getMessage());
            model.addAttribute("usuarios", new java.util.ArrayList<>());
            model.addAttribute("impersonacionesActivas", new java.util.ArrayList<>());
            return buildViewPath("sa", "impersonate-user");
        }
    }

    @PostMapping("/devportal/sa/iniciar-impersonacion/{usuarioId}")
    @ResponseBody
    public ResponseEntity<?> iniciarImpersonacion(@PathVariable Long usuarioId,
                                                  Authentication authentication,
                                                  HttpSession session,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {
        try {
            // Verificar que el usuario actual es superadmin
            String currentRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .orElse("USER");

            System.out.println("=== DEBUG IMPERSONACION ===");
            System.out.println("Usuario actual: " + authentication.getName());
            System.out.println("Rol actual: " + currentRole);
            System.out.println("Autoridades: " + authentication.getAuthorities());

            if (!"SA".equals(currentRole)) {
                return ResponseEntity.status(403)
                        .body(Map.of("success", false, "message", "No tienes permisos para impersonar usuarios. Tu rol actual es: " + currentRole));
            }

            // ✅ NUEVA VALIDACIÓN: Verificar si el SuperAdmin ya tiene una impersonación activa
            Usuario superAdmin = userService.buscarPorUsername(authentication.getName());
            if (superAdmin != null) {
                java.util.Optional<Impersonacion> impersonacionActivaExistente = 
                    impersonacionService.obtenerImpersonacionActiva(superAdmin.getUsuarioId());
                
                if (impersonacionActivaExistente.isPresent()) {
                    Impersonacion impActiva = impersonacionActivaExistente.get();
                    String usuarioActivoImpersonado = impActiva.getUsuario() != null ? 
                        impActiva.getUsuario().getUsername() : "desconocido";
                    
                    System.out.println("🚫 [Impersonación Bloqueada] SuperAdmin '" + authentication.getName() + 
                                     "' ya tiene una impersonación activa con usuario '" + usuarioActivoImpersonado + 
                                     "' (ID: " + impActiva.getImpersonacionId() + ")");
                    
                    return ResponseEntity.status(409) // HTTP 409 Conflict
                            .body(Map.of(
                                "success", false, 
                                "message", "Ya tienes una impersonación activa con el usuario '" + usuarioActivoImpersonado + 
                                          "'. Debes finalizarla antes de iniciar otra.",
                                "impersonacionActivaId", impActiva.getImpersonacionId(),
                                "usuarioImpersonadoActivo", usuarioActivoImpersonado
                            ));
                }
            }

            // Obtener el usuario a impersonar
            Usuario usuarioImpersonado = userService.buscarUsuarioPorId(usuarioId);
            if (usuarioImpersonado == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("success", false, "message", "Usuario no encontrado"));
            }

            // Verificar que el usuario objetivo no sea SuperAdmin
            boolean isTargetSuperAdmin = usuarioImpersonado.getRoles().stream()
                    .anyMatch(rol -> "SA".equals(rol.getNombreRol().name()));

            if (isTargetSuperAdmin) {
                System.out.println("INTENTO DE IMPERSONACION BLOQUEADO: Se intentó impersonar a SuperAdmin " + usuarioImpersonado.getUsername());
                return ResponseEntity.status(403)
                        .body(Map.of("success", false,
                                "message", "No se puede impersonar a otro SuperAdministrador por razones de seguridad"));
            }

            // Verificar que el usuario esté habilitado
            if (usuarioImpersonado.getEstadoUsuario() != Usuario.EstadoUsuario.HABILITADO) {
                return ResponseEntity.status(403)
                        .body(Map.of("success", false,
                                "message", "No se puede impersonar a un usuario inhabilitado"));
            }

            System.out.println("Impersonación autorizada para usuario: " + usuarioImpersonado.getUsername() + " (Roles: " +
                    usuarioImpersonado.getRoles().stream().map(r -> r.getNombreRol().name()).collect(Collectors.joining(", ")) + ")");

            // Guardar información del superadmin original en la sesión
            session.setAttribute("originalSuperadmin", authentication.getName());
            session.setAttribute("impersonating", true);
            session.setAttribute("impersonatedUserId", usuarioId);
            session.setAttribute("impersonatedUsername", usuarioImpersonado.getUsername());

            // Iniciar impersonación en la base de datos
            Impersonacion impersonacion = impersonacionService.iniciarImpersonacion(usuarioId);

            // CAMBIAR EL CONTEXTO DE SEGURIDAD - IMPERSONAR REALMENTE AL USUARIO
            String rolImpersonado = usuarioImpersonado.getRoles().stream()
                    .findFirst()
                    .map(rol -> rol.getNombreRol().name().toLowerCase())
                    .orElse("dev");

            // Crear las autoridades del usuario impersonado
            List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities =
                    usuarioImpersonado.getRoles().stream()
                            .map(rol -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + rol.getNombreRol().name()))
                            .collect(Collectors.toList());

            // Crear UserDetails para el usuario impersonado
            org.springframework.security.core.userdetails.UserDetails userDetails =
                    org.springframework.security.core.userdetails.User.builder()
                            .username(usuarioImpersonado.getUsername())
                            .password(usuarioImpersonado.getHashedPassword())
                            .authorities(authorities)
                            .build();

            // Crear nueva autenticación
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken newAuth =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities);

            // Establecer en el contexto de seguridad
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(newAuth);

            // Guardar el contexto de seguridad en la sesión
            session.setAttribute("SPRING_SECURITY_CONTEXT",
                    org.springframework.security.core.context.SecurityContextHolder.getContext());

            // Determinar la URL de redirección según el rol
            String redirectUrl = determinarUrlSegunRol(rolImpersonado, usuarioImpersonado.getUsername());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Impersonación iniciada exitosamente",
                    "redirectUrl", redirectUrl,
                    "usuarioImpersonado", usuarioImpersonado.getUsername(),
                    "rolImpersonado", rolImpersonado,
                    "impersonacionId", impersonacion.getImpersonacionId()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error al iniciar impersonación: " + e.getMessage()));
        }
    }

    private String determinarUrlSegunRol(String rol, String username) {
        return switch (rol.toLowerCase()) {
            case "dev" -> "/devportal/dev/" + username + "/dashboard";
            case "qa" -> "/devportal/qa/" + username + "/dashboard";
            case "po" -> "/devportal/po/" + username + "/dashboard";
            case "sa" -> "/devportal/sa/" + username + "/dashboard";
            default -> "/dashboard";
        };
    }

    @PostMapping("/devportal/sa/finalizar-impersonacion/{usuarioId}")
    @ResponseBody
    public ResponseEntity<?> finalizarImpersonacion(@PathVariable Long usuarioId) {
        try {
            impersonacionService.finalizarImpersonacion(usuarioId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Impersonación finalizada exitosamente",
                    "redirectUrl", "/devportal/sa/impersonate-user"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error al finalizar impersonación: " + e.getMessage()));
        }
    }

    // Endpoint temporal para debug de roles
    @GetMapping("/devportal/sa/debug-roles")
    @ResponseBody
    public ResponseEntity<?> debugRoles(Authentication authentication) {
        try {
            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());

            Map<String, Object> debugInfo = new java.util.HashMap<>();
            debugInfo.put("username", currentUser.getUsername());
            debugInfo.put("userId", currentUser.getUsuarioId());
            debugInfo.put("authorities", authentication.getAuthorities().toString());
            debugInfo.put("roles", currentUser.getRoles().stream()
                    .map(rol -> rol.getNombreRol().toString())
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==========================================
    // FUNCIONALIDAD DE GESTIÓN DE CATEGORÍAS
    // ==========================================

    @GetMapping("/devportal/sa/{username}/manage-categories")
    public String gestionarCategorias(
            @PathVariable String username,
            @RequestParam(value = "seccion", defaultValue = "TODAS") String seccion,
            @RequestParam(value = "tipo", defaultValue = "TODAS") String tipo, // Para compatibilidad
            Model model,
            Authentication authentication) {
        log.info("=== ACCESO A GESTIONAR CATEGORÍAS ===");

        if (authentication == null) {
            log.warn("NO AUTHENTICATION - Usuario anónimo intentando acceder a gestionar categorías");
            model.addAttribute("error", "Acceso no autorizado");
            return "redirect:/sa-access";
        }

        String authenticatedUsername = authentication.getName();
        log.info("AUTHENTICATION OK - Usuario autenticado: {}", authenticatedUsername);
        log.info("Authorities: {}", authentication.getAuthorities());

        try {
            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());
            if (currentUser == null) {
                throw new RuntimeException("Usuario no encontrado: " + authentication.getName());
            }

            // Verificar que el username coincida con el usuario autenticado
            if (!currentUser.getUsername().equals(username)) {
                return buildRedirectPath("sa", currentUser.getUsername(), "manage-categories");
            }

            log.info("USUARIO ENCONTRADO - ID: {}, Username: {}, Roles: {}",
                    currentUser.getUsuarioId(), currentUser.getUsername(),
                    currentUser.getRoles().stream().map(rol -> rol.getNombreRol().toString()).collect(Collectors.toList()));

            // Usar seccion preferentemente, sino tipo para compatibilidad
            String seccionActual = seccion;
            if ("TODAS".equals(seccion) && !tipo.equals("TODAS")) {
                seccionActual = tipo; // Usar tipo si se pasó como parámetro legacy
            }

            // Validar sección de categoría
            if (!Arrays.asList("APIS", "PROYECTOS", "REPOSITORIOS", "FORO", "OTRO", "TODAS").contains(seccionActual.toUpperCase())) {
                seccionActual = "TODAS";
            }

            // Obtener categorías según la sección
            List<Categoria> categorias;
            if ("TODAS".equals(seccionActual.toUpperCase())) {
                categorias = categoriaService.listarTodasLasCategorias();
                log.info("CATEGORÍAS CARGADAS - Mostrando TODAS las categorías, Total: {}", categorias.size());
            } else {
                categorias = categoriaService.findCategoriasPorSeccion(seccionActual.toUpperCase());
                log.info("CATEGORÍAS CARGADAS - Sección: {}, Total: {}", seccionActual, categorias.size());
            }

            // Obtener estadísticas por sección
            Map<String, Object> estadisticas = categoriaService.getEstadisticasPorSeccion(seccionActual.toUpperCase());

            String currentRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .orElse("USER");

            model.addAttribute("usuario", currentUser);
            model.addAttribute("currentUser", currentUser); // Para compatibilidad con template
            model.addAttribute("role", currentRole);
            model.addAttribute("categorias", categorias);
            model.addAttribute("seccionActual", seccionActual.toUpperCase());
            model.addAttribute("tipoActual", seccionActual.toUpperCase()); // Para compatibilidad
            model.addAttribute("estadisticas", estadisticas);

            return buildViewPath(currentRole, "manage-categories");
        } catch (Exception e) {
            log.error("ERROR al cargar gestionar categorías para usuario: {}", authenticatedUsername, e);
            model.addAttribute("error", "Error al cargar la página: " + e.getMessage());
            return buildViewPath("sa", "manage-categories");
        }
    }

    @PostMapping("/devportal/sa/crear-categoria")
    @ResponseBody
    public ResponseEntity<?> crearCategoria(@RequestParam String nombreCategoria,
                                            @RequestParam String descripcionCategoria,
                                            @RequestParam(required = false) String seccionCategoria,
                                            @RequestParam(required = false) String tipoCategoria) { // Para compatibilidad
        try {
            log.info("Creando nueva categoría: {} - {} - Sección: {}", nombreCategoria, descripcionCategoria, seccionCategoria);

            Categoria nuevaCategoria = new Categoria();
            nuevaCategoria.setNombreCategoria(nombreCategoria);
            nuevaCategoria.setDescripcionCategoria(descripcionCategoria);

            // Manejar sección de categoría
            if (seccionCategoria != null && !seccionCategoria.equals("TODAS")) {
                try {
                    Categoria.SeccionCategoria seccion = Categoria.SeccionCategoria.valueOf(seccionCategoria.toUpperCase());
                    nuevaCategoria.setSeccionCategoria(seccion);
                } catch (IllegalArgumentException e) {
                    log.warn("Sección inválida: {}, categoría sin sección", seccionCategoria);
                }
            }

            Categoria categoriaGuardada = categoriaService.guardarCategoria(nuevaCategoria);

            String mensaje = "Categoría creada exitosamente";
            if (seccionCategoria != null && !seccionCategoria.equals("TODAS")) {
                mensaje += " en la sección " + seccionCategoria.toUpperCase();
            }

            log.info("Categoría creada exitosamente: ID {}", categoriaGuardada.getIdCategoria());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", mensaje,
                    "categoria", categoriaGuardada
            ));
        } catch (Exception e) {
            log.error("Error al crear categoría", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error al crear la categoría: " + e.getMessage()));
        }
    }

    @PostMapping("/devportal/sa/editar-categoria/{id}")
    @ResponseBody
    public ResponseEntity<?> editarCategoria(@PathVariable Long id,
                                             @RequestParam String nombreCategoria,
                                             @RequestParam String descripcionCategoria,
                                             @RequestParam(required = false) String seccionCategoria) {
        try {
            Categoria categoriaActualizada = new Categoria();
            categoriaActualizada.setNombreCategoria(nombreCategoria);
            categoriaActualizada.setDescripcionCategoria(descripcionCategoria);

            // Manejar sección de categoría
            if (seccionCategoria != null && !seccionCategoria.equals("TODAS")) {
                try {
                    Categoria.SeccionCategoria seccion = Categoria.SeccionCategoria.valueOf(seccionCategoria.toUpperCase());
                    categoriaActualizada.setSeccionCategoria(seccion);
                } catch (IllegalArgumentException e) {
                    log.warn("Sección inválida: {}, manteniendo sección actual", seccionCategoria);
                    categoriaActualizada.setSeccionCategoria(null);
                }
            } else {
                categoriaActualizada.setSeccionCategoria(null);
            }

            Categoria categoria = categoriaService.actualizarCategoria(id, categoriaActualizada);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Categoría actualizada exitosamente",
                    "categoria", categoria
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error al actualizar la categoría: " + e.getMessage()));
        }
    }

    @DeleteMapping("/devportal/sa/eliminar-categoria/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarCategoria(@PathVariable Long id) {
        try {
            categoriaService.eliminarCategoria(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Categoría eliminada exitosamente"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error al eliminar la categoría: " + e.getMessage()));
        }
    }

    @GetMapping("/devportal/sa/obtener-categoria/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerCategoria(@PathVariable Long id) {
        try {
            return categoriaService.buscarCategoriaPorId(id)
                    .map(categoria -> ResponseEntity.ok(Map.of(
                            "success", true,
                            "categoria", categoria
                    )))
                    .orElse(ResponseEntity.status(404)
                            .body(Map.of("success", false, "message", "Categoría no encontrada")));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error al obtener la categoría: " + e.getMessage()));
        }
    }

    // ==========================================
    // FUNCIONALIDAD DE GESTIÓN DE USUARIOS (SIMILAR A PO)
    // ==========================================

    /**
     * Vista de formulario para crear nuevo usuario
     * GET /devportal/sa/{username}/create-new-user
     */
    @GetMapping("/devportal/sa/{username}/create-new-user")
    public String showCreateUserForm(@PathVariable String username, Model model, Authentication authentication) {

        // Verificar acceso
        Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());
        if (!canAccessSuperAdminFunction(currentUser, username)) {
            return "redirect:/access-denied";
        }

        // Obtener roles disponibles (incluyendo SA para SuperAdmin)
        log.info("Obteniendo roles disponibles para crear usuario...");
        List<Rol> rolesDisponibles = platformUserManagementService.obtenerTodosLosRolesSuperAdmin();
        log.info("Roles disponibles obtenidos: {}", rolesDisponibles != null ? rolesDisponibles.size() : "null");

        model.addAttribute("role", "SA");
        model.addAttribute("username", username);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("rolesDisponibles", rolesDisponibles);
        model.addAttribute("usuario", new Usuario()); // Usuario vacío para el formulario

        return buildViewPath("sa", "create-user-fill");
    }

    /**
     * Vista de formulario para invitar usuario
     * GET /devportal/sa/{username}/invite-new-user
     */
    @GetMapping("/devportal/sa/{username}/invite-new-user")
    public String showInviteUserForm(@PathVariable String username, Model model, Authentication authentication) {

        log.info("=== INICIANDO showInviteUserForm SuperAdmin ===");
        log.info("Username: {}, Principal: {}", username, authentication.getName());

        try {
            // Verificar acceso
            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());
            log.info("Usuario actual encontrado: {}", currentUser != null ? currentUser.getUsername() : "null");

            if (!canAccessSuperAdminFunction(currentUser, username)) {
                log.warn("Acceso denegado para usuario: {}", authentication.getName());
                return "redirect:/access-denied";
            }

            // Obtener roles disponibles (incluyendo SA para SuperAdmin)
            log.info("Obteniendo roles disponibles...");
            List<Rol> rolesDisponibles = platformUserManagementService.obtenerTodosLosRolesSuperAdmin();
            log.info("Roles disponibles obtenidos: {}", rolesDisponibles != null ? rolesDisponibles.size() : "null");

            // Agregar atributos al modelo
            model.addAttribute("rol", "sa");
            model.addAttribute("username", username);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("rolesDisponibles", rolesDisponibles);
            model.addAttribute("currentNavSection", "user-management");

            log.info("Modelo configurado correctamente, retornando vista: sa/invite-user-fill");
            return buildViewPath("sa", "invite-user-fill");

        } catch (Exception e) {
            log.error("Error en showInviteUserForm: ", e);
            model.addAttribute("error", "Error al cargar formulario de invitación: " + e.getMessage());
            return buildRedirectPath("sa", username, "dashboard-panel");
        }
    }

    /**
     * Procesar creación de nuevo usuario
     * POST /devportal/sa/{username}/create-user
     */
    @PostMapping("/devportal/sa/{username}/create-user")
    public String createUser(@PathVariable String username,
                             @ModelAttribute Usuario usuario,
                             @RequestParam Long rolId,
                             @RequestParam(defaultValue = "false") boolean sendWelcomeEmail,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) {

        log.info("=== INICIANDO createUser SuperAdmin ===");
        log.info("Parámetros recibidos:");
        log.info("- username: {}", username);
        log.info("- rolId: {}", rolId);
        log.info("- sendWelcomeEmail: {}", sendWelcomeEmail);
        log.info("- usuario.username: {}", usuario.getUsername());
        log.info("- usuario.correo: {}", usuario.getCorreo());
        log.info("- usuario.nombreUsuario: {}", usuario.getNombreUsuario());

        try {
            // Verificar acceso
            log.info("Verificando acceso del usuario...");
            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());
            log.info("Usuario actual: {}", currentUser != null ? currentUser.getUsername() : "null");

            if (!canAccessSuperAdminFunction(currentUser, username)) {
                log.warn("Acceso denegado para usuario: {}", authentication.getName());
                return "redirect:/access-denied";
            }

            // Crear el usuario usando el servicio ESPECÍFICO para SuperAdmin
            log.info("Llamando al servicio SA para crear usuario...");
            List<Long> roleIds = Arrays.asList(rolId);
            Usuario nuevoUsuario = platformUserManagementService.crearUsuarioPlataformaSuperAdmin(
                    usuario, roleIds);

            log.info("Usuario creado exitosamente con ID: {}", nuevoUsuario.getUsuarioId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Usuario '" + nuevoUsuario.getUsername() + "' creado exitosamente. Se ha enviado un enlace al correo " +
                            nuevoUsuario.getCorreo() + " para configurar su contraseña.");

            return buildRedirectPath("sa", username, "dashboard-panel");

        } catch (Exception e) {
            log.error("Error al crear usuario: ", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error al crear usuario: " + e.getMessage());
            return buildRedirectPath("sa", username, "create-new-user");
        }
    }

    /**
     * Procesar envío de invitación
     * POST /devportal/sa/{username}/send-invitation
     */
    @PostMapping("/devportal/sa/{username}/send-invitation")
    public String sendInvitation(@PathVariable String username,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String nombre,
                                 @RequestParam(required = false) String apellido,
                                 @RequestParam Long rolId,
                                 @RequestParam(defaultValue = "14") int diasExpiracion,
                                 @RequestParam(required = false) String mensajePersonalizado,
                                 @RequestParam(defaultValue = "true") boolean requirePasswordChange,
                                 @RequestParam(defaultValue = "false") boolean sendCopyToSender,
                                 RedirectAttributes redirectAttributes,
                                 Authentication authentication) {

        log.info("=== INICIANDO sendInvitation SuperAdmin ===");
        log.info("Parámetros recibidos:");
        log.info("- username: {}", username);
        log.info("- email: {}", email);
        log.info("- rolId: {}", rolId);
        log.info("- principal: {}", authentication.getName());

        try {
            // Verificar acceso
            log.info("Verificando acceso del usuario...");
            Usuario currentUser = userService.buscarPorUsernameOEmail(authentication.getName());
            log.info("Usuario actual: {}", currentUser != null ? currentUser.getUsername() : "null");

            if (!canAccessSuperAdminFunction(currentUser, username)) {
                log.warn("Acceso denegado para usuario: {}", authentication.getName());
                return "redirect:/access-denied";
            }

            // Llamar al servicio para crear la invitación
            log.info("Llamando al servicio para crear invitación...");
            Usuario usuarioInvitado = platformUserManagementService.crearInvitacionUsuarioSuperAdmin(email, rolId);
            log.info("Invitación creada exitosamente para usuario ID: {}", usuarioInvitado.getUsuarioId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Invitación enviada exitosamente a " + email);

            log.info("Redirigiendo al dashboard");
            return buildRedirectPath("sa", username, "dashboard-panel");

        } catch (Exception e) {
            log.error("Error al enviar invitación: ", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error al enviar invitación: " + e.getMessage());
            return buildRedirectPath("sa", username, "invite-new-user");
        }
    }

    /**
     * Método auxiliar para verificar acceso a funciones de SuperAdmin
     */
    private boolean canAccessSuperAdminFunction(Usuario currentUser, String requestedUsername) {
        // Solo SuperAdmins pueden acceder a estas funciones
        boolean hasSaRole = currentUser.getRoles().stream()
                .anyMatch(rol -> rol.getNombreRol() == Rol.NombreRol.SA);

        // Verificar que coincida username
        boolean matchesUser = currentUser.getUsername().equals(requestedUsername);

        return hasSaRole && matchesUser;
    }

    // ==========================================
    // API REST ENDPOINTS PARA MÉTRICAS AVANZADAS
    // ==========================================

    /**
     * Obtiene métricas generales del sistema
     * GET /api/sa/metrics/general
     */
    @GetMapping("/api/sa/metrics/general")
    @ResponseBody
    @PreAuthorize("hasRole('SA')")
    public ResponseEntity<Map<String, Object>> getGeneralMetrics() {
        try {
            Map<String, Object> metrics = systemAdministratorService.getGeneralMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error al obtener métricas generales: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener métricas generales: " + e.getMessage()));
        }
    }

    /**
     * Obtiene métricas específicas de proyectos
     * GET /api/sa/metrics/proyectos
     */
    @GetMapping("/api/sa/metrics/proyectos")
    @ResponseBody
    @PreAuthorize("hasRole('SA')")
    public ResponseEntity<Map<String, Object>> getProyectosMetrics() {
        try {
            Map<String, Object> metrics = systemAdministratorService.getProjectsChartData();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error al obtener métricas de proyectos: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener métricas de proyectos: " + e.getMessage()));
        }
    }

    /**
     * Obtiene métricas específicas de usuarios
     * GET /api/sa/metrics/usuarios
     */
    @GetMapping("/api/sa/metrics/usuarios")
    @ResponseBody
    @PreAuthorize("hasRole('SA')")
    public ResponseEntity<Map<String, Object>> getUsuariosMetrics() {
        try {
            Map<String, Object> metrics = systemAdministratorService.getUsersChartData();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error al obtener métricas de usuarios: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener métricas de usuarios: " + e.getMessage()));
        }
    }

    /**
     * Obtiene métricas específicas de repositorios
     * GET /api/sa/metrics/repositorios
     */
    @GetMapping("/api/sa/metrics/repositorios")
    @ResponseBody
    @PreAuthorize("hasRole('SA')")
    public ResponseEntity<Map<String, Object>> getRepositoriosMetrics() {
        try {
            Map<String, Object> metrics = systemAdministratorService.getRepositoriesChartData();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error al obtener métricas de repositorios: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener métricas de repositorios: " + e.getMessage()));
        }
    }

    /**
     * Obtiene métricas específicas de APIs
     * GET /api/sa/metrics/apis
     */
    @GetMapping("/api/sa/metrics/apis")
    @ResponseBody
    @PreAuthorize("hasRole('SA')")
    public ResponseEntity<Map<String, Object>> getApisMetrics() {
        try {
            Map<String, Object> metrics = systemAdministratorService.getApisMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error al obtener métricas de APIs: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener métricas de APIs: " + e.getMessage()));
        }
    }

    /**
     * Obtiene métricas específicas de tickets
     * GET /api/sa/metrics/tickets
     */
    @GetMapping("/api/sa/metrics/tickets")
    @ResponseBody
    @PreAuthorize("hasRole('SA')")
    public ResponseEntity<Map<String, Object>> getTicketsMetrics() {
        try {
            Map<String, Object> metrics = systemAdministratorService.getTicketsChartData();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error al obtener métricas de tickets: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener métricas de tickets: " + e.getMessage()));
        }
    }

    /**
     * Obtiene datos paginados para tablas específicas
     * GET /api/sa/metrics/table/{category}?page=0&size=10&search=
     */
    @GetMapping("/api/sa/metrics/table/{category}")
    @ResponseBody
    @PreAuthorize("hasRole('SA')")
    public ResponseEntity<Map<String, Object>> getTableData(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {
        try {
            Map<String, Object> tableData = systemAdministratorService.getTableData(category, page, size, search);
            return ResponseEntity.ok(tableData);
        } catch (Exception e) {
            log.error("Error al obtener datos de tabla para {}: ", category, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener datos de tabla: " + e.getMessage()));
        }
    }

    /**
     * Obtiene datos específicos para gráficos
     * GET /api/sa/metrics/chart/{category}/{chartType}
     */
    @GetMapping("/api/sa/metrics/chart/{category}/{chartType}")
    @ResponseBody
    @PreAuthorize("hasRole('SA')")
    public ResponseEntity<Map<String, Object>> getChartData(
            @PathVariable String category,
            @PathVariable String chartType) {
        try {
            Map<String, Object> chartData = systemAdministratorService.getChartData(category, chartType);
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            log.error("Error al obtener datos de gráfico {}/{}: ", category, chartType, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener datos de gráfico: " + e.getMessage()));
        }
    }

    /**
     * Obtiene datos para gráfico de tickets (prioridad y estado)
     * GET /api/sa/metrics/charts/tickets
     */
    @GetMapping("/api/sa/metrics/charts/tickets")
    @ResponseBody
    @PreAuthorize("hasRole('SA')")
    public ResponseEntity<Map<String, Object>> getTicketsChartData() {
        try {
            Map<String, Object> chartData = systemAdministratorService.getTicketsChartData();
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            log.error("Error al obtener datos de gráfico de tickets: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener datos de tickets: " + e.getMessage()));
        }
    }

    /**
     * Obtiene datos para gráfico de usuarios (rol y estado)
     * GET /api/sa/metrics/charts/users
     */
    @GetMapping("/api/sa/metrics/charts/users")
    @ResponseBody
    @PreAuthorize("hasRole('SA')")
    public ResponseEntity<Map<String, Object>> getUsersChartData() {
        try {
            Map<String, Object> chartData = systemAdministratorService.getUsersChartData();
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            log.error("Error al obtener datos de gráfico de usuarios: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener datos de usuarios: " + e.getMessage()));
        }
    }

    /**
     * Obtiene datos para gráfico de proyectos (estado y complejidad)
     * GET /api/sa/metrics/charts/projects
     */
    @GetMapping("/api/sa/metrics/charts/projects")
    @ResponseBody
    @PreAuthorize("hasRole('SA')")
    public ResponseEntity<Map<String, Object>> getProjectsChartData() {
        try {
            Map<String, Object> chartData = systemAdministratorService.getProjectsChartData();
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            log.error("Error al obtener datos de gráfico de proyectos: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener datos de proyectos: " + e.getMessage()));
        }
    }

    /**
     * Obtiene datos para gráfico de repositorios (tipo y estado)
     * GET /api/sa/metrics/charts/repositories
     */
    @GetMapping("/api/sa/metrics/charts/repositories")
    @ResponseBody
    @PreAuthorize("hasRole('SA')")
    public ResponseEntity<Map<String, Object>> getRepositoriesChartData() {
        try {
            Map<String, Object> chartData = systemAdministratorService.getRepositoriesChartData();
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            log.error("Error al obtener datos de gráfico de repositorios: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener datos de repositorios: " + e.getMessage()));
        }
    }

    /**
     * Obtiene resumen ejecutivo de todas las métricas
     * GET /api/sa/metrics/dashboard-summary
     */
    @GetMapping("/api/sa/metrics/dashboard-summary")
    @ResponseBody
    @PreAuthorize("hasRole('SA')")
    public ResponseEntity<Map<String, Object>> getMetricsDashboardSummary() {
        try {
            Map<String, Object> summary = systemAdministratorService.getDashboardSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error al obtener resumen del dashboard de métricas: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al obtener resumen del dashboard: " + e.getMessage()));
        }
    }

    // ==========================================
    // EXPORTACIÓN DE REPORTES DE MÉTRICAS
    // ==========================================

    /**
     * Exporta las métricas del dashboard en formato CSV
     * GET /api/sa/metrics/export/csv
     */
    @GetMapping("/api/sa/metrics/export/csv")
    @PreAuthorize("hasRole('SA')")
    public void exportMetricsToCSV(
            @RequestParam(required = false) String category,
            HttpServletResponse response,
            Authentication authentication) {
        try {
            log.info("Exportando métricas a CSV - Usuario: {}, Categoría: {}", 
                    authentication.getName(), category);

            // Configurar la respuesta HTTP
            response.setContentType("text/csv; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            String filename = "metricas_dashboard_" + 
                    (category != null ? category + "_" : "") + 
                    java.time.LocalDate.now() + ".csv";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            // Obtener métricas
            Map<String, Object> metrics = systemAdministratorService.getAllGeneralMetrics();

            // Escribir el CSV
            java.io.PrintWriter writer = response.getWriter();
            
            // Escribir BOM para UTF-8 (para que Excel lo reconozca)
            writer.write('\ufeff');
            
            // Encabezado
            writer.println("REPORTE DE MÉTRICAS DEL DEVELOPER PORTAL");
            writer.println("Fecha de generación:," + java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            writer.println("Generado por:," + authentication.getName());
            writer.println();
            
            // Métricas generales
            writer.println("MÉTRICAS GENERALES");
            writer.println("Categoría,Valor");
            writer.println("Total Usuarios," + metrics.getOrDefault("totalUsuarios", 0));
            writer.println("Total Proyectos," + metrics.getOrDefault("totalProyectos", 0));
            writer.println("Total Repositorios," + metrics.getOrDefault("totalRepositorios", 0));
            writer.println("Total Tickets," + metrics.getOrDefault("totalTickets", 0));
            writer.println("Total APIs," + metrics.getOrDefault("totalApis", 0));
            writer.println("Total Categorías," + metrics.getOrDefault("totalCategorias", 0));
            writer.println("Usuarios Activos," + metrics.getOrDefault("usuariosActivos", 0));
            writer.println();

            // Métricas por categoría si está especificada
            if (category != null && !category.isEmpty()) {
                exportCategoryMetricsToCSV(category, writer);
            } else {
                // Exportar todas las categorías
                exportAllCategoriesMetricsToCSV(writer, metrics);
            }

            writer.flush();
            log.info("Exportación CSV completada exitosamente");

        } catch (Exception e) {
            log.error("Error al exportar métricas a CSV: ", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Error al generar el reporte CSV");
            } catch (Exception ex) {
                log.error("Error al enviar respuesta de error: ", ex);
            }
        }
    }

    /**
     * Exporta las métricas del dashboard en formato Excel (XLSX)
     * GET /api/sa/metrics/export/excel
     */
    @GetMapping("/api/sa/metrics/export/excel")
    @PreAuthorize("hasRole('SA')")
    public void exportMetricsToExcel(
            @RequestParam(required = false) String category,
            HttpServletResponse response,
            Authentication authentication) {
        try {
            log.info("Exportando métricas a Excel - Usuario: {}, Categoría: {}", 
                    authentication.getName(), category);

            // Configurar la respuesta HTTP
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = "metricas_dashboard_" + 
                    (category != null ? category + "_" : "") + 
                    java.time.LocalDate.now() + ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            // Crear el workbook de Excel
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            
            // Crear estilos
            org.apache.poi.ss.usermodel.CellStyle headerStyle = createHeaderStyle(workbook);
            org.apache.poi.ss.usermodel.CellStyle titleStyle = createTitleStyle(workbook);
            org.apache.poi.ss.usermodel.CellStyle dataStyle = createDataStyle(workbook);

            // Obtener métricas
            Map<String, Object> metrics = systemAdministratorService.getAllGeneralMetrics();

            // Crear hoja de resumen general
            createGeneralSummarySheet(workbook, metrics, headerStyle, titleStyle, dataStyle, authentication);

            // Crear hojas por categoría
            if (category != null && !category.isEmpty()) {
                createCategorySheet(workbook, category, headerStyle, titleStyle, dataStyle);
            } else {
                // Crear hojas para todas las categorías
                createAllCategorySheets(workbook, metrics, headerStyle, titleStyle, dataStyle);
            }

            // Escribir el archivo
            workbook.write(response.getOutputStream());
            workbook.close();
            
            log.info("Exportación Excel completada exitosamente");

        } catch (Exception e) {
            log.error("Error al exportar métricas a Excel: ", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Error al generar el reporte Excel");
            } catch (Exception ex) {
                log.error("Error al enviar respuesta de error: ", ex);
            }
        }
    }

    // Métodos auxiliares para CSV

    private void exportCategoryMetricsToCSV(String category, java.io.PrintWriter writer) throws Exception {
        Map<String, Object> categoryData = null;
        
        switch (category.toLowerCase()) {
            case "proyectos":
                categoryData = systemAdministratorService.getProyectosMetrics();
                writer.println("MÉTRICAS DE PROYECTOS");
                break;
            case "usuarios":
                categoryData = systemAdministratorService.getUsuariosMetrics();
                writer.println("MÉTRICAS DE USUARIOS");
                break;
            case "repositorios":
                categoryData = systemAdministratorService.getRepositoriosMetrics();
                writer.println("MÉTRICAS DE REPOSITORIOS");
                break;
            case "apis":
                categoryData = systemAdministratorService.getApisMetrics();
                writer.println("MÉTRICAS DE APIs");
                break;
            case "tickets":
                categoryData = systemAdministratorService.getTicketsMetrics();
                writer.println("MÉTRICAS DE TICKETS");
                break;
        }

        if (categoryData != null) {
            exportMapToCSV(categoryData, writer);
        }
    }

    private void exportAllCategoriesMetricsToCSV(java.io.PrintWriter writer, Map<String, Object> metrics) {
        // Proyectos
        writer.println("MÉTRICAS DE PROYECTOS");
        Map<String, Object> proyectosMetrics = (Map<String, Object>) metrics.get("proyectosMetrics");
        if (proyectosMetrics != null) {
            exportMapToCSV(proyectosMetrics, writer);
        }
        writer.println();

        // Usuarios
        writer.println("MÉTRICAS DE USUARIOS");
        Map<String, Object> usuariosMetrics = (Map<String, Object>) metrics.get("usuariosMetrics");
        if (usuariosMetrics != null) {
            exportMapToCSV(usuariosMetrics, writer);
        }
        writer.println();

        // Repositorios
        writer.println("MÉTRICAS DE REPOSITORIOS");
        Map<String, Object> repositoriosMetrics = (Map<String, Object>) metrics.get("repositoriosMetrics");
        if (repositoriosMetrics != null) {
            exportMapToCSV(repositoriosMetrics, writer);
        }
        writer.println();

        // APIs
        writer.println("MÉTRICAS DE APIs");
        Map<String, Object> apisMetrics = (Map<String, Object>) metrics.get("apisMetrics");
        if (apisMetrics != null) {
            exportMapToCSV(apisMetrics, writer);
        }
        writer.println();

        // Tickets
        writer.println("MÉTRICAS DE TICKETS");
        Map<String, Object> ticketsMetrics = (Map<String, Object>) metrics.get("ticketsMetrics");
        if (ticketsMetrics != null) {
            exportMapToCSV(ticketsMetrics, writer);
        }
    }

    private void exportMapToCSV(Map<String, Object> data, java.io.PrintWriter writer) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Ignorar campos de error
            if ("error".equals(key)) {
                continue;
            }

            if (value instanceof List) {
                List<?> list = (List<?>) value;
                if (!list.isEmpty() && list.get(0) instanceof Map) {
                    // Es una lista de mapas (tabla de datos)
                    writer.println();
                    writer.println(formatKey(key));
                    List<Map<String, Object>> tableData = (List<Map<String, Object>>) list;
                    
                    // Encabezados
                    if (!tableData.isEmpty()) {
                        Map<String, Object> firstRow = tableData.get(0);
                        writer.println(firstRow.keySet().stream()
                                .map(this::formatKey)
                                .collect(Collectors.joining(",")));
                        
                        // Datos
                        for (Map<String, Object> row : tableData) {
                            writer.println(row.values().stream()
                                    .map(v -> v != null ? escapeCSV(v.toString()) : "")
                                    .collect(Collectors.joining(",")));
                        }
                    }
                    writer.println();
                } else if (!list.isEmpty()) {
                    // Lista de valores simples
                    writer.println(formatKey(key) + "," + list.stream()
                            .map(v -> v != null ? escapeCSV(v.toString()) : "")
                            .collect(Collectors.joining("; ")));
                }
            } else if (value instanceof Map) {
                // Valor es un Map anidado
                writer.println();
                writer.println(formatKey(key));
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
                    writer.println(formatKey(nestedEntry.getKey()) + "," + 
                            (nestedEntry.getValue() != null ? escapeCSV(nestedEntry.getValue().toString()) : "0"));
                }
                writer.println();
            } else {
                // Valor simple
                writer.println(formatKey(key) + "," + (value != null ? escapeCSV(value.toString()) : "0"));
            }
        }
    }

    private String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String formatKey(String key) {
        // Convertir camelCase a Title Case con espacios
        return key.replaceAll("([A-Z])", " $1")
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .trim()
                .substring(0, 1).toUpperCase() + 
                key.replaceAll("([A-Z])", " $1")
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .trim()
                .substring(1);
    }

    // Métodos auxiliares para Excel

    private org.apache.poi.ss.usermodel.CellStyle createHeaderStyle(org.apache.poi.ss.usermodel.Workbook workbook) {
        org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        style.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        return style;
    }

    private org.apache.poi.ss.usermodel.CellStyle createTitleStyle(org.apache.poi.ss.usermodel.Workbook workbook) {
        org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
        return style;
    }

    private org.apache.poi.ss.usermodel.CellStyle createDataStyle(org.apache.poi.ss.usermodel.Workbook workbook) {
        org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        return style;
    }

    private void createGeneralSummarySheet(
            org.apache.poi.ss.usermodel.Workbook workbook,
            Map<String, Object> metrics,
            org.apache.poi.ss.usermodel.CellStyle headerStyle,
            org.apache.poi.ss.usermodel.CellStyle titleStyle,
            org.apache.poi.ss.usermodel.CellStyle dataStyle,
            Authentication authentication) {
        
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Resumen General");
        int rowNum = 0;

        // Título
        org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE MÉTRICAS DEL DEVELOPER PORTAL");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));

        // Información del reporte
        rowNum++;
        org.apache.poi.ss.usermodel.Row infoRow1 = sheet.createRow(rowNum++);
        infoRow1.createCell(0).setCellValue("Fecha de generación:");
        infoRow1.createCell(1).setCellValue(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        org.apache.poi.ss.usermodel.Row infoRow2 = sheet.createRow(rowNum++);
        infoRow2.createCell(0).setCellValue("Generado por:");
        infoRow2.createCell(1).setCellValue(authentication.getName());

        rowNum++;

        // Métricas generales
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue("Métrica");
        headerCell1.setCellStyle(headerStyle);
        org.apache.poi.ss.usermodel.Cell headerCell2 = headerRow.createCell(1);
        headerCell2.setCellValue("Valor");
        headerCell2.setCellStyle(headerStyle);

        // Datos
        addDataRow(sheet, rowNum++, "Total Usuarios", metrics.getOrDefault("totalUsuarios", 0), dataStyle);
        addDataRow(sheet, rowNum++, "Total Proyectos", metrics.getOrDefault("totalProyectos", 0), dataStyle);
        addDataRow(sheet, rowNum++, "Total Repositorios", metrics.getOrDefault("totalRepositorios", 0), dataStyle);
        addDataRow(sheet, rowNum++, "Total Tickets", metrics.getOrDefault("totalTickets", 0), dataStyle);
        addDataRow(sheet, rowNum++, "Total APIs", metrics.getOrDefault("totalApis", 0), dataStyle);
        addDataRow(sheet, rowNum++, "Total Categorías", metrics.getOrDefault("totalCategorias", 0), dataStyle);
        addDataRow(sheet, rowNum++, "Usuarios Activos", metrics.getOrDefault("usuariosActivos", 0), dataStyle);

        // Ajustar anchos de columna
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.setColumnWidth(0, sheet.getColumnWidth(0) + 2000);
    }

    private void addDataRow(org.apache.poi.ss.usermodel.Sheet sheet, int rowNum, String label, Object value,
            org.apache.poi.ss.usermodel.CellStyle dataStyle) {
        org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum);
        org.apache.poi.ss.usermodel.Cell cell1 = row.createCell(0);
        cell1.setCellValue(label);
        cell1.setCellStyle(dataStyle);
        
        org.apache.poi.ss.usermodel.Cell cell2 = row.createCell(1);
        if (value instanceof Number) {
            cell2.setCellValue(((Number) value).doubleValue());
        } else {
            cell2.setCellValue(value != null ? value.toString() : "0");
        }
        cell2.setCellStyle(dataStyle);
    }

    private void createCategorySheet(
            org.apache.poi.ss.usermodel.Workbook workbook,
            String category,
            org.apache.poi.ss.usermodel.CellStyle headerStyle,
            org.apache.poi.ss.usermodel.CellStyle titleStyle,
            org.apache.poi.ss.usermodel.CellStyle dataStyle) throws Exception {
        
        Map<String, Object> categoryData = null;
        String sheetName = "";

        switch (category.toLowerCase()) {
            case "proyectos":
                categoryData = systemAdministratorService.getProyectosMetrics();
                sheetName = "Proyectos";
                break;
            case "usuarios":
                categoryData = systemAdministratorService.getUsuariosMetrics();
                sheetName = "Usuarios";
                break;
            case "repositorios":
                categoryData = systemAdministratorService.getRepositoriosMetrics();
                sheetName = "Repositorios";
                break;
            case "apis":
                categoryData = systemAdministratorService.getApisMetrics();
                sheetName = "APIs";
                break;
            case "tickets":
                categoryData = systemAdministratorService.getTicketsMetrics();
                sheetName = "Tickets";
                break;
        }

        if (categoryData != null) {
            createDataSheet(workbook, sheetName, categoryData, headerStyle, titleStyle, dataStyle);
        }
    }

    private void createAllCategorySheets(
            org.apache.poi.ss.usermodel.Workbook workbook,
            Map<String, Object> metrics,
            org.apache.poi.ss.usermodel.CellStyle headerStyle,
            org.apache.poi.ss.usermodel.CellStyle titleStyle,
            org.apache.poi.ss.usermodel.CellStyle dataStyle) {

        // Proyectos
        Map<String, Object> proyectosMetrics = (Map<String, Object>) metrics.get("proyectosMetrics");
        if (proyectosMetrics != null) {
            createDataSheet(workbook, "Proyectos", proyectosMetrics, headerStyle, titleStyle, dataStyle);
        }

        // Usuarios
        Map<String, Object> usuariosMetrics = (Map<String, Object>) metrics.get("usuariosMetrics");
        if (usuariosMetrics != null) {
            createDataSheet(workbook, "Usuarios", usuariosMetrics, headerStyle, titleStyle, dataStyle);
        }

        // Repositorios
        Map<String, Object> repositoriosMetrics = (Map<String, Object>) metrics.get("repositoriosMetrics");
        if (repositoriosMetrics != null) {
            createDataSheet(workbook, "Repositorios", repositoriosMetrics, headerStyle, titleStyle, dataStyle);
        }

        // APIs
        Map<String, Object> apisMetrics = (Map<String, Object>) metrics.get("apisMetrics");
        if (apisMetrics != null) {
            createDataSheet(workbook, "APIs", apisMetrics, headerStyle, titleStyle, dataStyle);
        }

        // Tickets
        Map<String, Object> ticketsMetrics = (Map<String, Object>) metrics.get("ticketsMetrics");
        if (ticketsMetrics != null) {
            createDataSheet(workbook, "Tickets", ticketsMetrics, headerStyle, titleStyle, dataStyle);
        }
    }

    private void createDataSheet(
            org.apache.poi.ss.usermodel.Workbook workbook,
            String sheetName,
            Map<String, Object> data,
            org.apache.poi.ss.usermodel.CellStyle headerStyle,
            org.apache.poi.ss.usermodel.CellStyle titleStyle,
            org.apache.poi.ss.usermodel.CellStyle dataStyle) {

        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet(sheetName);
        int rowNum = 0;

        // Título
        org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("MÉTRICAS DE " + sheetName.toUpperCase());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

        rowNum++;

        // Procesar cada entrada del mapa
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof List) {
                List<?> list = (List<?>) value;
                if (!list.isEmpty() && list.get(0) instanceof Map) {
                    // Es una tabla de datos
                    List<Map<String, Object>> tableData = (List<Map<String, Object>>) list;
                    
                    // Subtítulo
                    org.apache.poi.ss.usermodel.Row subtitleRow = sheet.createRow(rowNum++);
                    org.apache.poi.ss.usermodel.Cell subtitleCell = subtitleRow.createCell(0);
                    subtitleCell.setCellValue(formatKey(key));
                    subtitleCell.setCellStyle(titleStyle);
                    
                    if (!tableData.isEmpty()) {
                        // Encabezados de la tabla
                        Map<String, Object> firstRow = tableData.get(0);
                        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
                        int colNum = 0;
                        for (String columnName : firstRow.keySet()) {
                            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(colNum++);
                            headerCell.setCellValue(formatKey(columnName));
                            headerCell.setCellStyle(headerStyle);
                        }
                        
                        // Datos de la tabla
                        for (Map<String, Object> row : tableData) {
                            org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(rowNum++);
                            colNum = 0;
                            for (Object cellValue : row.values()) {
                                org.apache.poi.ss.usermodel.Cell dataCell = dataRow.createCell(colNum++);
                                if (cellValue instanceof Number) {
                                    dataCell.setCellValue(((Number) cellValue).doubleValue());
                                } else {
                                    dataCell.setCellValue(cellValue != null ? cellValue.toString() : "");
                                }
                                dataCell.setCellStyle(dataStyle);
                            }
                        }
                        
                        // Autosize columns
                        for (int i = 0; i < firstRow.size(); i++) {
                            sheet.autoSizeColumn(i);
                            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
                        }
                    }
                    rowNum++;
                }
            } else {
                // Valor simple
                addDataRow(sheet, rowNum++, formatKey(key), value, dataStyle);
            }
        }

        // Ajustar anchos de columna
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
}
