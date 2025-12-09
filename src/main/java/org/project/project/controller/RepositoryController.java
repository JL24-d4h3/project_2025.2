package org.project.project.controller;

import org.project.project.model.entity.Equipo;
import org.project.project.model.entity.Repositorio;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.Categoria;
import org.project.project.service.RepositoryService;
import org.project.project.service.TeamService;
import org.project.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/devportal/{rol}/{username}/repositories")
public class RepositoryController {

    private static final Logger log = LoggerFactory.getLogger(RepositoryController.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private TeamService teamService;

    // =================== VISTAS PRINCIPALES ===================

    /**
     * Vista principal de repositorios - Lista todos los repositorios del usuario
     * Ruta: /devportal/{rol}/{username}/repositories
     */
    @GetMapping
    public String showRepositories(@PathVariable String rol,
                                   @PathVariable String username,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(required = false) String search,
                                   @RequestParam(required = false) String sort,
                                   @RequestParam(required = false) String filter,
                                   @RequestParam(defaultValue = "0") int page,
                                   Model model,
                                   Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        // Verificar que el usuario puede acceder a esta vista
        if (!canAccessUserRepositories(currentUser, username, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                    + "/" + currentUser.getUsername() + "/repositories";
        }

        // 🔧 FIX: Normalizar parámetros vacíos a null (evita bug en queries)
        category = (category != null && category.trim().isEmpty()) ? null : category;
        search = (search != null && search.trim().isEmpty()) ? null : search;
        sort = (sort != null && sort.trim().isEmpty()) ? null : sort;

        // Obtener estadísticas del usuario
        Map<String, Object> stats = repositoryService.obtenerEstadisticasRepositoriosUsuario(currentUser.getUsuarioId());

        // ✅ PAGINACIÓN: Obtener OTROS repositorios (públicos y privados) - 12 por página
        List<Map<String, Object>> repositories = repositoryService.obtenerOtrosRepositoriosPaginado(currentUser.getUsuarioId(), category, search, sort, page);
        
        // Obtener TODOS los otros repositorios (para mostrar contador total correcto)
        List<Map<String, Object>> allRepositories = repositoryService.obtenerOtrosRepositorios(currentUser.getUsuarioId(), category, search, sort);

        // 🔍 DEBUG: Verificar tamaño de listas en cada página
        System.out.println("📊 REPOSITORIOS - Página " + page + ":");
        System.out.println("  - repositories.size() (paginados): " + repositories.size());
        System.out.println("  - allRepositories.size() (todos): " + allRepositories.size());
        System.out.println("  - Filtros: category=" + category + ", search=" + search + ", sort=" + sort);

        // Calcular información de paginación
        int totalRepositories = allRepositories.size();
        int totalPages = (int) Math.ceil((double) totalRepositories / 12);
        int startIndex = (page * 12) + 1;
        int endIndex = Math.min((page + 1) * 12, totalRepositories);
        boolean hasNextRepositories = repositories.size() >= 12;
        
        // Obtener categorías para filtros
        List<Categoria> categories = repositoryService.obtenerTodasCategorias();

        // Obtener proyectos del usuario para navegación
        List<Map<String, Object>> projects = repositoryService.obtenerProyectosUsuario(currentUser.getUsuarioId());

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("currentUser", currentUser); // Para navbar
        model.addAttribute("repositories", repositories);
        model.addAttribute("allRepositories", allRepositories);
        model.addAttribute("categories", categories);
        model.addAttribute("projects", projects);
        model.addAttribute("stats", stats);
        model.addAttribute("currentView", "other");
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNextRepositories", hasNextRepositories);
        model.addAttribute("totalRepositories", totalRepositories);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);

        // Variables para navegación consistente
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);
        model.addAttribute("rol", rol); // Para navbar
        model.addAttribute("currentNavSection", "repositories");

        return "repository/dashboard";
    }

    /**
     * Vista de repositorios personales
     * Ruta: /devportal/{rol}/{username}/repositories/personal-repositories
     */
    @GetMapping("/personal-repositories")
    public String showPersonalRepositories(@PathVariable String rol,
                                           @PathVariable String username,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) String search,
                                           @RequestParam(required = false) String sort,
                                           Model model,
                                           Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        if (!canAccessUserRepositories(currentUser, username, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                    + "/" + currentUser.getUsername() + "/repositories/personal-repositories";
        }

        List<Map<String, Object>> repositories = repositoryService.obtenerRepositoriosPersonales(currentUser.getUsuarioId(), category, search, sort);
        List<Categoria> categories = repositoryService.obtenerTodasCategorias();
        Map<String, Object> stats = repositoryService.obtenerEstadisticasRepositoriosUsuario(currentUser.getUsuarioId());

        // 🔧 FIX: Agregar atributos de paginación (sin paginación en esta vista)
        int totalRepositories = repositories.size();
        
        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("repositories", repositories);
        model.addAttribute("allRepositories", repositories); // 🔧 FIX: Agregar para Thymeleaf
        model.addAttribute("categories", categories);
        model.addAttribute("stats", stats);
        model.addAttribute("currentView", "personal");
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", 0); // 🔧 FIX: Página por defecto
        model.addAttribute("totalPages", totalRepositories > 0 ? 1 : 0); // 🔧 FIX: Una sola página
        model.addAttribute("hasNextRepositories", false); // 🔧 FIX: Sin siguiente
        model.addAttribute("totalRepositories", totalRepositories);
        model.addAttribute("startIndex", totalRepositories > 0 ? 1 : 0);
        model.addAttribute("endIndex", totalRepositories);

        // Variables para navegación consistente
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);
        model.addAttribute("currentNavSection", "repositories");

        return "repository/dashboard";
    }



    /**
     * Vista de repositorios colaborativos donde participo
     * Ruta: /devportal/{rol}/{username}/repositories/collaborative-repositories
     */
    @GetMapping("/collaborative-repositories")
    public String showCollaborativeRepositories(@PathVariable String rol,
                                                @PathVariable String username,
                                                @RequestParam(required = false) String category,
                                                @RequestParam(required = false) String search,
                                                @RequestParam(required = false) String sort,
                                                Model model,
                                                Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        if (!canAccessUserRepositories(currentUser, username, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                    + "/" + currentUser.getUsername() + "/repositories/collaborative-repositories";
        }

        List<Map<String, Object>> repositories = repositoryService.obtenerRepositoriosColaborativos(currentUser.getUsuarioId(), category, search, sort);
        List<Categoria> categories = repositoryService.obtenerTodasCategorias();
        Map<String, Object> stats = repositoryService.obtenerEstadisticasRepositoriosUsuario(currentUser.getUsuarioId());

        // 🔧 FIX: Agregar atributos de paginación (sin paginación en esta vista)
        int totalRepositories = repositories.size();

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("repositories", repositories);
        model.addAttribute("allRepositories", repositories); // 🔧 FIX: Agregar para Thymeleaf
        model.addAttribute("categories", categories);
        model.addAttribute("stats", stats);
        model.addAttribute("currentView", "collaborative");
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", 0); // 🔧 FIX: Página por defecto
        model.addAttribute("totalPages", totalRepositories > 0 ? 1 : 0); // 🔧 FIX: Una sola página
        model.addAttribute("hasNextRepositories", false); // 🔧 FIX: Sin siguiente
        model.addAttribute("totalRepositories", totalRepositories);
        model.addAttribute("startIndex", totalRepositories > 0 ? 1 : 0);
        model.addAttribute("endIndex", totalRepositories);

        // Variables para navegación consistente
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);

        return "repository/dashboard";
    }

    /**
     * Vista de otros repositorios públicos
     * Ruta: /devportal/{rol}/{username}/repositories/other-repositories
     */
    @GetMapping("/other-repositories")
    public String showOtherRepositories(@PathVariable String rol,
                                        @PathVariable String username,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) String search,
                                        @RequestParam(required = false) String sort,
                                        Model model,
                                        Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        if (!canAccessUserRepositories(currentUser, username, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                    + "/" + currentUser.getUsername() + "/repositories/other-repositories";
        }

        List<Map<String, Object>> repositories = repositoryService.obtenerOtrosRepositoriosPublicos(currentUser.getUsuarioId(), category, search, sort);
        List<Categoria> categories = repositoryService.obtenerTodasCategorias();
        Map<String, Object> stats = repositoryService.obtenerEstadisticasRepositoriosUsuario(currentUser.getUsuarioId());

        // 🔧 FIX: Agregar atributos de paginación (sin paginación en esta vista)
        int totalRepositories = repositories.size();

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("repositories", repositories);
        model.addAttribute("allRepositories", repositories); // 🔧 FIX: Agregar para Thymeleaf
        model.addAttribute("categories", categories);
        model.addAttribute("stats", stats);
        model.addAttribute("currentView", "other");
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", 0); // 🔧 FIX: Página por defecto
        model.addAttribute("totalPages", totalRepositories > 0 ? 1 : 0); // 🔧 FIX: Una sola página
        model.addAttribute("hasNextRepositories", false); // 🔧 FIX: Sin siguiente
        model.addAttribute("totalRepositories", totalRepositories);
        model.addAttribute("startIndex", totalRepositories > 0 ? 1 : 0);
        model.addAttribute("endIndex", totalRepositories);

        // Variables para navegación consistente
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);

        return "repository/dashboard";
    }

    /**
     * Vista de repositorios de los que formo parte (personal + colaborativo)
     * Ruta: /devportal/{rol}/{username}/repositories/i-am-part-of
     */
    @GetMapping("/i-am-part-of")
    public String showIAmPartOfRepositories(@PathVariable String rol,
                                            @PathVariable String username,
                                            @RequestParam(required = false) String category,
                                            @RequestParam(required = false) String search,
                                            @RequestParam(required = false) String sort,
                                            Model model,
                                            Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        if (!canAccessUserRepositories(currentUser, username, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                    + "/" + currentUser.getUsername() + "/repositories/i-am-part-of";
        }

        System.out.println("🔍 REPOSITORY CONTROLLER I-AM-PART-OF - Usuario: " + currentUser.getUsername() + " (ID: " + currentUser.getUsuarioId() + ")");

        List<Map<String, Object>> repositories = repositoryService.obtenerTodosMisRepositorios(currentUser.getUsuarioId(), category, search, sort);
        System.out.println("📊 REPOSITORY CONTROLLER I-AM-PART-OF - Repositorios obtenidos: " + repositories.size());

        List<Categoria> categories = repositoryService.obtenerTodasCategorias();
        Map<String, Object> stats = repositoryService.obtenerEstadisticasRepositoriosUsuario(currentUser.getUsuarioId());
        System.out.println("📈 REPOSITORY CONTROLLER I-AM-PART-OF - Stats obtenidas");
        if (stats != null) {
            System.out.println("  - Stats keys: " + stats.keySet());
        }

        // 🔧 FIX: Agregar atributos de paginación (sin paginación en esta vista)
        int totalRepositories = repositories.size();

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("repositories", repositories);
        model.addAttribute("allRepositories", repositories); // 🔧 FIX: Agregar para Thymeleaf
        model.addAttribute("categories", categories);
        model.addAttribute("stats", stats);
        model.addAttribute("currentView", "i-am-part-of");
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", 0); // 🔧 FIX: Página por defecto
        model.addAttribute("totalPages", totalRepositories > 0 ? 1 : 0); // 🔧 FIX: Una sola página
        model.addAttribute("hasNextRepositories", false); // 🔧 FIX: Sin siguiente
        model.addAttribute("totalRepositories", totalRepositories);
        model.addAttribute("startIndex", totalRepositories > 0 ? 1 : 0);
        model.addAttribute("endIndex", totalRepositories);

        // Variables para navegación consistente
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);

        return "repository/dashboard";
    }

    /**
     * Vista detallada de un repositorio específico
     * Ruta: /devportal/{rol}/{username}/repositories/.../R-{repositoryId}
     */
    @GetMapping("/R-{repositoryId}")
    public String showRepositoryDetails(@PathVariable String rol,
                                        @PathVariable String username,
                                        @PathVariable Long repositoryId,
                                        Model model,
                                        Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        if (!canAccessUserRepositories(currentUser, username, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                    + "/" + currentUser.getUsername() + "/repositories";
        }

        Map<String, Object> repository = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repositoryId);

        if (repository == null) {
            return "redirect:/devportal/" + rol + "/" + username + "/repositories?error=repository-not-found";
        }

        // Verificar permisos para ver este repositorio
        String userPermission = (String) repository.get("privilegio_usuario_actual");
        if ("SIN_ACCESO".equals(userPermission)) {
            return "redirect:/devportal/" + rol + "/" + username + "/repositories?error=access-denied";
        }

        List<Map<String, Object>> collaborators = repositoryService.obtenerColaboradoresRepositorio(repositoryId);
        List<Map<String, Object>> recentActivity = repositoryService.obtenerActividadRecienteRepositorio(repositoryId);
        Map<String, Object> repositoryStats = repositoryService.obtenerEstadisticasRepositorio(repositoryId);
        
        // Obtener TODOS los proyectos asociados a este repositorio
        List<Map<String, Object>> associatedProjects = repositoryService.obtenerProyectosDeRepositorio(repositoryId);

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("repository", repository);
        model.addAttribute("collaborators", collaborators);
        model.addAttribute("actividades", recentActivity);
        model.addAttribute("stats", repositoryStats);
        model.addAttribute("privilegio", userPermission);
        model.addAttribute("associatedProjects", associatedProjects);

        // Variables para navegación consistente
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);

        return "repository/detail";
    }

    // =================== API ENDPOINTS PARA CRUD ===================

    /**
     * API: Crear nuevo repositorio
     */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createRepository(@RequestBody Map<String, Object> repositoryData, Principal principal) {
        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            Map<String, Object> result = repositoryService.crearRepositorio(currentUser.getUsuarioId(), repositoryData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API: Actualizar repositorio
     */
    @PutMapping("/api/{repositoryId}")
    @ResponseBody
    public ResponseEntity<?> updateRepository(@PathVariable Long repositoryId,
                                              @RequestBody Map<String, Object> repositoryData,
                                              Principal principal) {
        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            Map<String, Object> result = repositoryService.actualizarRepositorio(currentUser.getUsuarioId(), repositoryId, repositoryData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API: Eliminar repositorio
     */
    @DeleteMapping("/api/{repositoryId}")
    @ResponseBody
    public ResponseEntity<?> deleteRepository(@PathVariable Long repositoryId, Principal principal) {
        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            repositoryService.eliminarRepositorio(currentUser.getUsuarioId(), repositoryId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API: Obtener información de un repositorio
     */
    @GetMapping("/api/{repositoryId}")
    @ResponseBody
    public ResponseEntity<?> getRepositoryInfo(@PathVariable Long repositoryId, Principal principal) {
        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            Map<String, Object> repository = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repositoryId);
            return ResponseEntity.ok(repository);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =================== PÁGINAS DE CREACIÓN Y EDICIÓN ===================







    // =================== NUEVAS RUTAS CREATE Y EDIT ===================

    /**
     * Mostrar formulario de crear repositorio
     * Ruta: /devportal/{rol}/{username}/repositories/create
     */
    @GetMapping("/create")
    public String showCreateForm(@PathVariable String rol,
                                 @PathVariable String username,
                                 @RequestParam(required = false) String type,
                                 Model model,
                                 Principal principal) {

        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

            if (!canAccessUserRepositories(currentUser, username, rol)) {
                return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                        + "/" + currentUser.getUsername() + "/repositories/create";
            }

            List<Categoria> categories = repositoryService.obtenerTodasCategorias();
            List<Map<String, Object>> projects = repositoryService.obtenerProyectosPropiosUsuario(currentUser.getUsuarioId());
            
            // Obtener equipos del usuario para selección (solo si es colaborativo)
            List<Equipo> equipos = new ArrayList<>();
            if ("colaborativo".equalsIgnoreCase(type)) {
                equipos = teamService.obtenerEquiposDelUsuarioParaInvitar(currentUser.getUsuarioId());
            }

            model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("categories", categories);
            model.addAttribute("projects", projects);
            model.addAttribute("equipos", equipos);
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);
            model.addAttribute("repositoryType", type); // personal o colaborativo

            return "repository/create";

        } catch (Exception e) {
            log.error("Error en showCreateForm: ", e);
            return "error/500";
        }
    }

    /**
     * Procesar creación de repositorio
     * Ruta: POST /devportal/{rol}/{username}/repositories/create
     */
    @PostMapping("/create")
    public String processCreate(@PathVariable String rol,
                                @PathVariable String username,
                                @RequestParam String nombreRepositorio,
                                @RequestParam(required = false) String descripcionRepositorio,
                                @RequestParam String tipoRepositorio,
                                @RequestParam String visibilidadRepositorio,
                                @RequestParam(required = false) Long categoriaId,
                                @RequestParam(required = false) List<Long> proyectoIds,
                                @RequestParam Map<String, String> allParams,
                                Model model,
                                Principal principal) {

        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

            if (!canAccessUserRepositories(currentUser, username, rol)) {
                return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                        + "/" + currentUser.getUsername() + "/repositories/create";
            }

            // DEBUG: Ver qué proyectoIds se recibieron
            System.out.println("=== DEBUG: processCreate ===");
            System.out.println("proyectoIds recibidos (List<Long>): " + proyectoIds);
            System.out.println("allParams contiene: " + allParams);
            
            // Extraer proyectoIds manualmente de allParams por si Spring no lo parseó bien
            List<Long> manualProyectoIds = allParams.entrySet().stream()
                .filter(e -> e.getKey().equals("proyectoIds"))
                .map(e -> Long.parseLong(e.getValue()))
                .toList();
            System.out.println("proyectoIds extraídos manualmente: " + manualProyectoIds);

            // Crear el repositorio usando el servicio
            Map<String, Object> repositoryData = new HashMap<>();
            repositoryData.put("nombre_repositorio", nombreRepositorio);
            repositoryData.put("descripcion_repositorio", descripcionRepositorio);
            repositoryData.put("visibilidad_repositorio", visibilidadRepositorio);
            repositoryData.put("tipo_repositorio", tipoRepositorio);

            if (categoriaId != null) {
                repositoryData.put("categoria_id", categoriaId);
            }

            // Usar la lista manual si la automática está vacía o nula
            List<Long> finalProyectoIds = (proyectoIds != null && !proyectoIds.isEmpty()) ? proyectoIds : manualProyectoIds;
            
            if (finalProyectoIds != null && !finalProyectoIds.isEmpty()) {
                System.out.println("Guardando " + finalProyectoIds.size() + " proyectos: " + finalProyectoIds);
                repositoryData.put("proyecto_ids", finalProyectoIds);
            } else {
                System.out.println("No se recibieron proyectoIds");
            }

            Map<String, Object> result = repositoryService.crearRepositorio(currentUser.getUsuarioId(), repositoryData);

            // Verificar si la creación fue exitosa
            Boolean success = (Boolean) result.get("success");
            if (success != null && success) {
                Long repositoryId = ((Number) result.get("repositorio_id")).longValue();
                
                // Procesar equipos asociados (solo para repositorios colaborativos)
                if ("COLABORATIVO".equalsIgnoreCase(tipoRepositorio)) {
                    log.info("Procesando equipos para repositorio colaborativo R-{}", repositoryId);
                    
                    Long propietarioEquipoId = null; // El primer equipo asignado será el propietario
                    
                    // Procesar equipos existentes seleccionados
                    List<String> equipoIds = allParams.entrySet().stream()
                        .filter(e -> e.getKey().equals("equipoIds"))
                        .map(Map.Entry::getValue)
                        .toList();
                    
                    for (String equipoIdStr : equipoIds) {
                        try {
                            Long equipoId = Long.parseLong(equipoIdStr);
                            if (equipoId > 0) { // Equipos existentes tienen IDs positivos
                                String privilegio = allParams.getOrDefault("privilegioEquipo_" + equipoId, "LECTOR");
                                log.info("Asignando equipo {} al repositorio {} con privilegio {}", equipoId, repositoryId, privilegio);
                                teamService.asignarEquipoARepositorio(equipoId, repositoryId, privilegio);
                                
                                // El primer equipo asignado se convierte en propietario
                                if (propietarioEquipoId == null) {
                                    propietarioEquipoId = equipoId;
                                }
                            }
                        } catch (NumberFormatException e) {
                            log.warn("ID de equipo inválido: {}", equipoIdStr);
                        }
                    }
                    
                    // Procesar equipos temporales
                    String temporaryTeamsCountStr = allParams.get("temporaryTeamsCount");
                    if (temporaryTeamsCountStr != null && !temporaryTeamsCountStr.isEmpty()) {
                        try {
                            int temporaryTeamsCount = Integer.parseInt(temporaryTeamsCountStr);
                            log.info("Procesando {} equipos temporales", temporaryTeamsCount);
                            
                            for (int i = 0; i < temporaryTeamsCount; i++) {
                                String teamName = allParams.get("temporaryTeamName_" + i);
                                String permission = allParams.getOrDefault("temporaryTeamPermission_" + i, "LECTOR");
                                
                                if (teamName != null && !teamName.trim().isEmpty()) {
                                    log.info("Creando equipo temporal '{}' para repositorio R-{}", teamName, repositoryId);
                                    
                                    // Crear el equipo temporal y asignarlo al repositorio
                                    Equipo nuevoEquipo = teamService.crearEquipoEnRepositorio(teamName, repositoryId, currentUser.getUsuarioId());
                                    // Actualizar permisos si es diferente de LECTOR (que es el valor por defecto)
                                    if (!"LECTOR".equals(permission)) {
                                        teamService.actualizarPermisosEquipoEnRepositorio(nuevoEquipo.getEquipoId(), repositoryId, permission);
                                    }
                                    
                                    // El primer equipo asignado se convierte en propietario
                                    if (propietarioEquipoId == null) {
                                        propietarioEquipoId = nuevoEquipo.getEquipoId();
                                    }
                                    
                                    log.info("Equipo temporal '{}' creado con ID: {}", teamName, nuevoEquipo.getEquipoId());
                                }
                            }
                        } catch (NumberFormatException e) {
                            log.warn("temporaryTeamsCount inválido: {}", temporaryTeamsCountStr, e);
                        }
                    }
                    
                    // Actualizar propietario_id del repositorio si se asignó algún equipo
                    if (propietarioEquipoId != null) {
                        log.info("Actualizando propietario_id del repositorio R-{} a equipo {}", repositoryId, propietarioEquipoId);
                        repositoryService.actualizarPropietarioRepositorio(repositoryId, propietarioEquipoId);
                    }
                }
                
                // Redirigir al repositorio creado
                return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repositoryId;
            } else {
                // Si hubo error, agregar mensaje de error y mostrar formulario
                String errorMessage = (String) result.get("message");
                model.addAttribute("error", errorMessage != null ? errorMessage : "Error desconocido al crear el repositorio");

                // Recargar datos del formulario
                List<Categoria> categories = repositoryService.obtenerTodasCategorias();
                List<Map<String, Object>> projects = repositoryService.obtenerProyectosPropiosUsuario(currentUser.getUsuarioId());
                List<Equipo> equipos = new ArrayList<>();
                if ("colaborativo".equalsIgnoreCase(tipoRepositorio)) {
                    equipos = teamService.obtenerEquiposDelUsuarioParaInvitar(currentUser.getUsuarioId());
                }
                model.addAttribute("user", currentUser);
                model.addAttribute("Usuario", currentUser);  // Para chatbot widget
                model.addAttribute("categories", categories);
                model.addAttribute("projects", projects);
                model.addAttribute("equipos", equipos);
                model.addAttribute("userRole", rol);
                model.addAttribute("username", username);
                model.addAttribute("repositoryType", tipoRepositorio.toLowerCase());

                return "repository/create";
            }

        } catch (Exception e) {
            log.error("Error creating repository: ", e);

            // En caso de error, volver al formulario con mensaje
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            List<Categoria> categories = repositoryService.obtenerTodasCategorias();

            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("categories", categories);
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);
            model.addAttribute("error", "Error al crear el repositorio: " + e.getMessage());

            return "repository/create";
        }
    }

    /**
     * Mostrar formulario de editar repositorio
     * Ruta: /devportal/{rol}/{username}/repositories/R-{repositoryId}/edit
     */
    @GetMapping("/R-{repositoryId}/edit")
    public String showEditForm(@PathVariable String rol,
                               @PathVariable String username,
                               @PathVariable Long repositoryId,
                               Model model,
                               Principal principal) {

        try {
            System.out.println("🚀 === EDIT REPOSITORY REQUEST ===");
            System.out.println("📋 Repository ID: " + repositoryId);
            System.out.println("👤 Role: " + rol);
            System.out.println("🆔 Username: " + username);
            System.out.println("🔐 Principal: " + (principal != null ? principal.getName() : "null"));

            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            System.out.println("✅ Current user found: " + currentUser.getUsername());

            if (!canAccessUserRepositories(currentUser, username, rol)) {
                System.out.println("Access denied, redirecting...");
                return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                        + "/" + currentUser.getUsername() + "/repositories";
            }

            // Obtener el repositorio directamente para edición
            System.out.println("🔍 Getting repository entity for ID: " + repositoryId);
            Repositorio repositorio = null;
            try {
                repositorio = repositoryService.buscarRepositorioPorId(repositoryId);
                System.out.println("✅ Repository found: " + repositorio.getNombreRepositorio());
                System.out.println("📊 Repository details: ID=" + repositorio.getRepositorioId() +
                        ", Tipo=" + repositorio.getTipoRepositorio());
            } catch (Exception e) {
                System.err.println("🔥 Repository not found or access denied: " + e.getMessage());
                e.printStackTrace();
                return "error/404";
            }

            // Verificar permisos con el servicio de detalles
            Map<String, Object> repositoryData = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repositoryId);
            if (repositoryData == null) {
                System.out.println("User doesn't have access to this repository");
                return "redirect:/devportal/" + rol + "/" + username + "/repositories?error=access-denied";
            }

            String userPermission = (String) repositoryData.get("privilegio_usuario_actual");
            if ("SIN_ACCESO".equals(userPermission) || "SOLO_LECTURA".equals(userPermission)) {
                System.out.println("User doesn't have edit permissions: " + userPermission);
                return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repositoryId + "?error=no-edit-permission";
            }

            List<Categoria> categories = repositoryService.obtenerTodasCategorias();
            System.out.println("Categories loaded: " + categories.size());

            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("repositorio", repositorio);
            model.addAttribute("categories", categories);
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);

            System.out.println("Returning view: repository/edit");
            return "repository/edit";

        } catch (Exception e) {
            System.err.println("Error en showEditForm: " + e.getMessage());
            e.printStackTrace();
            log.error("Error en showEditForm: ", e);
            return "error/500";
        }
    }

    /**
     * Procesar edición de repositorio
     * Ruta: /devportal/{rol}/{username}/repositories/R-{repositoryId}/edit
     */
    @PostMapping("/R-{repositoryId}/edit")
    public String processEdit(@PathVariable String rol,
                              @PathVariable String username,
                              @PathVariable Long repositoryId,
                              @RequestParam String nombreRepositorio,
                              @RequestParam(required = false) String descripcionRepositorio,
                              @RequestParam String tipoRepositorio,
                              @RequestParam String visibilidadRepositorio,
                              @RequestParam(required = false) Long categoriaId,
                              Model model,
                              Principal principal) {

        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

            if (!canAccessUserRepositories(currentUser, username, rol)) {
                return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                        + "/" + currentUser.getUsername() + "/repositories/R-" + repositoryId + "/edit";
            }

            // Actualizar el repositorio usando el servicio
            Map<String, Object> repositoryData = new HashMap<>();
            repositoryData.put("nombre_repositorio", nombreRepositorio);
            repositoryData.put("descripcion_repositorio", descripcionRepositorio);
            repositoryData.put("visibilidad_repositorio", visibilidadRepositorio);
            repositoryData.put("tipo_repositorio", tipoRepositorio);

            if (categoriaId != null) {
                repositoryData.put("categoria_id", categoriaId);
            }

            repositoryService.actualizarRepositorio(currentUser.getUsuarioId(), repositoryId, repositoryData);

            // Redirigir al repositorio actualizado
            return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repositoryId + "?success=Repositorio actualizado correctamente";

        } catch (Exception e) {
            log.error("Error updating repository: ", e);
            System.err.println("🔥 ERROR EN PROCESS EDIT: " + e.getMessage());
            e.printStackTrace();

            // En caso de error, volver al formulario con mensaje
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            try {
                Repositorio repositorio = repositoryService.buscarRepositorioPorId(repositoryId);
                model.addAttribute("repositorio", repositorio);
                System.out.println("✅ PROCESS EDIT ERROR: Repositorio recuperado correctamente como entidad");
            } catch (Exception ex) {
                System.err.println("🔥 PROCESS EDIT ERROR: No se pudo recuperar repositorio: " + ex.getMessage());
                Map<String, Object> repositoryData = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repositoryId);
                model.addAttribute("repositorio", repositoryData);
                System.out.println("⚠️ PROCESS EDIT ERROR: Usando datos de Map en lugar de entidad");
            }

            List<Categoria> categories = repositoryService.obtenerTodasCategorias();

            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("categories", categories);
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);
            model.addAttribute("error", "Error al actualizar el repositorio: " + e.getMessage());

            return "repository/edit";
        }
    }

    // =================== INVITACIONES A REPOSITORIOS ===================

    /**
     * Mostrar formulario para invitar colaboradores al repositorio
     * Ruta: GET /devportal/{rol}/{username}/repositories/R-{repositoryId}/invite
     */
    @GetMapping("/R-{repositoryId}/invite")
    public String showInviteForm(@PathVariable String rol,
                                  @PathVariable String username,
                                  @PathVariable Long repositoryId,
                                  Model model,
                                  Principal principal) {
        try {
            log.info("╔════════════════════════════════════════════════════════╗");
            log.info("║     ACCESO A FORMULARIO DE INVITACIÓN REPOSITORIO      ║");
            log.info("╚════════════════════════════════════════════════════════╝");
            log.info("📋 Parámetros recibidos:");
            log.info("   - Rol: {}", rol);
            log.info("   - Username: {}", username);
            log.info("   - Repository ID: {}", repositoryId);
            log.info("   - Principal: {}", principal != null ? principal.getName() : "NULL");
            
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            log.info("👤 Usuario actual obtenido: ID={}, Username={}", 
                     currentUser.getUsuarioId(), currentUser.getUsername());
            
            if (!canAccessUserRepositories(currentUser, username, rol)) {
                log.warn("⚠️ Usuario no puede acceder a repositorios de otro usuario");
                return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                        + "/" + currentUser.getUsername() + "/repositories";
            }

            // Obtener datos del repositorio
            log.info("🔍 Obteniendo detalles del repositorio R-{}", repositoryId);
            Map<String, Object> repository = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repositoryId);
            
            if (repository == null) {
                log.error("❌ Repositorio R-{} no encontrado", repositoryId);
                return "redirect:/devportal/" + rol + "/" + username + "/repositories?error=repository-not-found";
            }
            
            log.info("✅ Repositorio encontrado: {}", repository.get("nombre_repositorio"));

            // Verificar que el usuario es propietario/editor del repositorio
            String userPermission = (String) repository.get("privilegio_usuario_actual");
            log.info("🔐 Permiso detectado para usuario: {}", userPermission);
            
            if (!"PROPIETARIO".equals(userPermission) && !"EDITOR".equals(userPermission)) {
                log.warn("⚠️ Acceso denegado - Usuario: {}, Permiso: {}", 
                         currentUser.getUsername(), userPermission);
                return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repositoryId 
                        + "?error=no-permission";
            }
            
            log.info("✅ Permiso verificado correctamente: {}", userPermission);

            // Obtener equipos asociados al repositorio
            List<Equipo> equipos = teamService.obtenerEquiposDelRepositorio(repositoryId);
            log.info("✅ [RepositoryController] Equipos del repositorio R-{} obtenidos: {}", repositoryId, equipos.size());

            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("repository", repository);
            model.addAttribute("equipos", equipos);
            model.addAttribute("rol", rol);
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);

            return "repository/invite-users";

        } catch (Exception e) {
            log.error("❌ [RepositoryController] Error en showInviteForm: {}", e.getMessage(), e);
            return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repositoryId;
        }
    }

    /**
     * Endpoint para validar nombre de equipo en tiempo real
     * Verifica si un nombre de equipo ya existe en el repositorio
     */
    @GetMapping("/R-{repositoryId}/validate-team-name")
    @ResponseBody
    public Map<String, Object> validateTeamName(@PathVariable Long repositoryId,
                                                 @RequestParam String teamName,
                                                 Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (teamName == null || teamName.trim().isEmpty()) {
                response.put("valid", false);
                response.put("message", "El nombre del equipo no puede estar vacío");
                return response;
            }
            
            // Verificar si el nombre ya existe en algún equipo del repositorio
            boolean exists = teamService.existsTeamByNameInRepository(repositoryId, teamName.trim());
            
            response.put("valid", !exists);
            if (exists) {
                response.put("message", "Este nombre de equipo ya existe en el repositorio");
            } else {
                response.put("message", "Nombre disponible");
            }
            
        } catch (Exception e) {
            log.error("Error al validar nombre de equipo: {}", e.getMessage());
            response.put("valid", false);
            response.put("message", "Error al validar el nombre del equipo");
        }
        
        return response;
    }

    /**
     * Procesar invitaciones al repositorio
     * Ruta: POST /devportal/{rol}/{username}/repositories/R-{repositoryId}/invite
     */
    @PostMapping("/R-{repositoryId}/invite")
    public String processInvite(@PathVariable String rol,
                                @PathVariable String username,
                                @PathVariable Long repositoryId,
                                @RequestParam String emails,
                                @RequestParam String permission,
                                @RequestParam(required = false) String equipoIds,
                                @RequestParam(required = false) Integer temporaryTeamsCount,
                                @RequestParam Map<String, String> allParams,
                                Principal principal,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║     PROCESANDO INVITACIÓN A REPOSITORIO               ║");
        log.info("╚════════════════════════════════════════════════════════╝");
        log.info("📋 Datos recibidos:");
        log.info("   - Emails raw: {}", emails);
        log.info("   - Permission: {}", permission);
        log.info("   - EquipoIds (raw string): {}", equipoIds);
        log.info("   - Temporary teams count: {}", temporaryTeamsCount);
        
        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        try {
            // Parsear equipoIds de string a List<Long>
            List<Long> equipoIdsList = parseEquipoIds(equipoIds);
            log.info("🔧 EquipoIds parseados (raw): {}", equipoIdsList);
            
            // FILTRAR los -1 (son placeholders para equipos temporales que se crearán ahora)
            if (equipoIdsList != null) {
                equipoIdsList = equipoIdsList.stream()
                    .filter(id -> id != null && id >= 0)
                    .collect(java.util.stream.Collectors.toList());
                log.info("🔧 EquipoIds filtrados (sin -1): {}", equipoIdsList);
            }
            
            // Parsear emails
            List<String> emailList = parseEmails(emails);
            log.info("📧 Emails parseados: {}", emailList);

            if (emailList.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se proporcionaron emails válidos");
                return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repositoryId + "/invite";
            }

            // Procesar equipos temporales si existen
            if (temporaryTeamsCount != null && temporaryTeamsCount > 0) {
                for (int i = 0; i < temporaryTeamsCount; i++) {
                    String teamName = allParams.get("temporaryTeamName_" + i);
                    
                    if (teamName != null && !teamName.trim().isEmpty()) {
                        // Crear el equipo temporal
                        log.info("📝 Creando equipo temporal: {}", teamName);
                        Equipo nuevoEquipo = teamService.crearEquipoEnRepositorio(
                            teamName.trim(), 
                            repositoryId, 
                            currentUser.getUsuarioId()
                        );
                        
                        if (nuevoEquipo != null) {
                            // Agregar el ID del equipo creado a la lista
                            if (equipoIdsList == null) {
                                equipoIdsList = new ArrayList<>();
                            }
                            equipoIdsList.add(nuevoEquipo.getEquipoId());
                            log.info("✅ Equipo temporal creado con ID: {}", nuevoEquipo.getEquipoId());
                        }
                    }
                }
            }

            // Llamar al servicio para procesar las invitaciones
            Map<String, Object> result = repositoryService.invitarColaboradores(
                repositoryId, 
                emailList, 
                permission,
                equipoIdsList,
                currentUser.getUsuarioId()
            );

            Boolean success = (Boolean) result.get("success");
            String message = (String) result.get("mensaje");
            
            log.info("📊 Resultado de invitación:");
            log.info("   - Success: {}", success);
            log.info("   - Message: {}", message);

            if (success) {
                redirectAttributes.addFlashAttribute("success", message);
            } else {
                redirectAttributes.addFlashAttribute("error", message);
            }

            return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repositoryId;

        } catch (Exception e) {
            log.error("❌ Error procesando invitación al repositorio: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repositoryId + "/invite";
        }
    }

    /**
     * Helper method para parsear emails separados por comas
     */
    private List<String> parseEmails(String emailsRaw) {
        List<String> emails = new ArrayList<>();
        log.info("🔍 parseEmails - Input raw: '{}'", emailsRaw);
        
        if (emailsRaw != null && !emailsRaw.trim().isEmpty()) {
            // Eliminar corchetes, comillas y espacios: '["email@test.com"]' -> 'email@test.com'
            String cleaned = emailsRaw.replaceAll("[\\[\\]\"\\s]", "");
            log.info("🔍 parseEmails - Cleaned: '{}'", cleaned);
            
            if (!cleaned.isEmpty()) {
                String[] emailArray = cleaned.split(",");
                log.info("🔍 parseEmails - Split result: {} elementos", emailArray.length);
                
                for (String email : emailArray) {
                    String trimmed = email.trim();
                    log.info("🔍 parseEmails - Processing: '{}'", trimmed);
                    if (!trimmed.isEmpty()) {
                        emails.add(trimmed);
                    }
                }
            }
        }
        
        log.info("🔍 parseEmails - Final result: {}", emails);
        return emails;
    }

    private List<Long> parseEquipoIds(String equipoIdsRaw) {
        List<Long> equipoIds = new ArrayList<>();
        if (equipoIdsRaw != null && !equipoIdsRaw.trim().isEmpty()) {
            // Eliminar corchetes y espacios: "[1, 2, 3]" -> "1,2,3"
            String cleaned = equipoIdsRaw.replaceAll("[\\[\\]\\s]", "");
            
            if (!cleaned.isEmpty()) {
                String[] idArray = cleaned.split(",");
                for (String id : idArray) {
                    try {
                        Long parsedId = Long.parseLong(id.trim());
                        equipoIds.add(parsedId);
                    } catch (NumberFormatException e) {
                        log.warn("⚠️ No se pudo parsear equipoId: {}", id);
                    }
                }
            }
        }
        return equipoIds;
    }

    // =================== MÉTODOS DE UTILIDAD ===================

    private boolean canAccessUserRepositories(Usuario currentUser, String requestedUsername, String requestedRole) {
        // El usuario puede acceder a sus propios repositorios
        if (currentUser.getUsername().equals(requestedUsername)) {
            return true;
        }

        // Los administradores pueden acceder a cualquier vista (para impersonación)
        return currentUser.getRoles().stream()
                .anyMatch(rol -> "SA".equals(rol.getNombreRol().toString()));
    }
}
