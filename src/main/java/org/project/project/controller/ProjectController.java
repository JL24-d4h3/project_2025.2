package org.project.project.controller;

import org.project.project.model.entity.Proyecto;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.Categoria;
import org.project.project.model.entity.Equipo;
import org.project.project.model.entity.Token;
import org.project.project.repository.ProyectoRepository;
import org.project.project.repository.CategoriaRepository;
import org.project.project.service.ProjectService;
import org.project.project.service.UserService;
import org.project.project.service.TeamService;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/devportal/{rol}/{username}/projects")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private org.project.project.repository.EquipoHasProyectoRepository equipoHasProyectoRepository;

    @Autowired
    private org.project.project.service.RepositoryService repositoryService;

    @Autowired
    private org.project.project.service.TokenService tokenService;

    // =================== VISTAS PRINCIPALES ===================

    /**
     * Vista principal de proyectos - Lista todos los proyectos del usuario
     * Ruta: /devportal/{rol}/{username}/projects
     */
    @GetMapping
    public String showProjects(@PathVariable String rol,
                               @PathVariable String username,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) String sort,
                               @RequestParam(required = false) String filter,
                               @RequestParam(defaultValue = "0") int page,
                               Model model,
                               Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        // Verificar que el usuario puede acceder a esta vista
        if (!canAccessUserProjects(currentUser, username, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                    + "/" + currentUser.getUsername() + "/projects";
        }

        // 🔧 FIX: Normalizar parámetros vacíos a null (evita bug en queries)
        category = (category != null && category.trim().isEmpty()) ? null : category;
        search = (search != null && search.trim().isEmpty()) ? null : search;
        sort = (sort != null && sort.trim().isEmpty()) ? null : sort;

        // Obtener estadísticas del usuario
        Map<String, Object> stats = projectService.obtenerEstadisticasProyectosUsuario(currentUser.getUsuarioId());

        // ✅ PAGINACIÓN: Obtener proyectos donde formo parte (12 por página)
        List<Map<String, Object>> myProjects = projectService.obtenerTodosProyectosUsuarioPaginado(currentUser.getUsuarioId(), category, search, sort, page);

        // Obtener TODOS los proyectos donde participo (para mostrar el contador correcto)
        List<Map<String, Object>> allParticipatingProjects = projectService.obtenerProyectosParticipacion(currentUser.getUsuarioId(), category, search, sort);

        // ✅ PAGINACIÓN: Obtener otros proyectos públicos (12 por página)
        List<Map<String, Object>> otherProjects = projectService.obtenerOtrosProyectosPublicosPaginado(currentUser.getUsuarioId(), category, search, sort, page);
        
        // Obtener TODOS los otros proyectos (para mostrar contador total correcto)
        List<Map<String, Object>> allOtherProjects = projectService.obtenerOtrosProyectosPublicos(currentUser.getUsuarioId(), category, search, sort);

        // 🔍 DEBUG: Verificar tamaño de listas en cada página
        System.out.println("📊 PROYECTOS - Página " + page + ":");
        System.out.println("  - otherProjects.size() (paginados): " + otherProjects.size());
        System.out.println("  - allOtherProjects.size() (todos): " + allOtherProjects.size());
        System.out.println("  - Filtros: category=" + category + ", search=" + search + ", sort=" + sort);

        // Calcular información de paginación
        int totalOtherProjects = allOtherProjects.size();
        int totalPages = (int) Math.ceil((double) totalOtherProjects / 12);
        int startIndex = (page * 12) + 1;
        int endIndex = Math.min((page + 1) * 12, totalOtherProjects);
        boolean hasNextMyProjects = myProjects.size() >= 12;
        boolean hasNextOtherProjects = otherProjects.size() >= 12;
        
        // Obtener categorías para filtros
        List<Categoria> categories = projectService.obtenerTodasCategorias();

        // Obtener equipos del usuario para navegación
        List<Map<String, Object>> teams = projectService.obtenerEquiposUsuario(currentUser.getUsuarioId());

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("currentUser", currentUser); // Para navbar
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);
        model.addAttribute("role", rol); // Para navbar
        model.addAttribute("rol", rol); // Para navbar - consistencia
        model.addAttribute("myProjects", myProjects);
        model.addAttribute("allParticipatingProjects", allParticipatingProjects);
        model.addAttribute("otherProjects", otherProjects);
        model.addAttribute("allOtherProjects", allOtherProjects);
        model.addAttribute("categories", categories);
        model.addAttribute("teams", teams);
        model.addAttribute("stats", stats);
        model.addAttribute("currentView", "overview");
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNextMyProjects", hasNextMyProjects);
        model.addAttribute("hasNextOtherProjects", hasNextOtherProjects);
        model.addAttribute("totalOtherProjects", totalOtherProjects);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("currentNavSection", "projects");

        return "project/overview";
    }

    /**
     * Vista de proyectos personales
     * Ruta: /devportal/{rol}/{username}/projects/personal-projects
     */
    @GetMapping("/personal-projects")
    public String showPersonalProjects(@PathVariable String rol,
                                       @PathVariable String username,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(required = false) String search,
                                       @RequestParam(required = false) String sort,
                                       Model model,
                                       Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        if (!canAccessUserProjects(currentUser, username, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                    + "/" + currentUser.getUsername() + "/projects/personal-projects";
        }

        List<Map<String, Object>> projects = projectService.obtenerProyectosPersonales(currentUser.getUsuarioId(), category, search, sort);
        List<Categoria> categories = projectService.obtenerTodasCategorias();
        Map<String, Object> stats = projectService.obtenerEstadisticasProyectosUsuario(currentUser.getUsuarioId());

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);
        model.addAttribute("projects", projects);
        model.addAttribute("categories", categories);
        model.addAttribute("stats", stats);
        model.addAttribute("currentView", "personal");
        model.addAttribute("currentSection", "personal-projects");
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentNavSection", "projects");

        return "project/dashboard";
    }

    /**
     * Vista de proyectos grupales
     * Ruta: /devportal/{rol}/{username}/projects/team-projects
     */
    @GetMapping("/team-projects")
    public String showTeamProjects(@PathVariable String rol,
                                   @PathVariable String username,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(required = false) String search,
                                   @RequestParam(required = false) String sort,
                                   Model model,
                                   Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        if (!canAccessUserProjects(currentUser, username, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                    + "/" + currentUser.getUsername() + "/projects/team-projects";
        }

        List<Map<String, Object>> projects = projectService.obtenerProyectosEquipos(currentUser.getUsuarioId(), category, search, sort);
        List<Map<String, Object>> teams = projectService.obtenerEquiposUsuario(currentUser.getUsuarioId());
        List<Categoria> categories = projectService.obtenerTodasCategorias();
        Map<String, Object> stats = projectService.obtenerEstadisticasProyectosUsuario(currentUser.getUsuarioId());

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);
        model.addAttribute("projects", projects);
        model.addAttribute("teams", teams);
        model.addAttribute("categories", categories);
        model.addAttribute("stats", stats);
        model.addAttribute("currentView", "team");
        model.addAttribute("currentSection", "team-projects");
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentNavSection", "projects");

        return "project/dashboard";
    }

    /**
     * Vista de proyectos de un equipo específico
     * Ruta: /devportal/{rol}/{username}/projects/team-projects/team-{teamId}
     */
    @GetMapping("/team-projects/team-{teamId}")
    public String showSpecificTeamProjects(@PathVariable String rol,
                                           @PathVariable String username,
                                           @PathVariable Long teamId,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) String search,
                                           @RequestParam(required = false) String sort,
                                           Model model,
                                           Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        if (!canAccessUserProjects(currentUser, username, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                    + "/" + currentUser.getUsername() + "/projects/team-projects";
        }

        List<Map<String, Object>> projects = projectService.obtenerProyectosEquipoEspecifico(currentUser.getUsuarioId(), teamId, category, search, sort);
        Map<String, Object> teamInfo = projectService.obtenerInformacionEquipo(teamId);
        List<Categoria> categories = projectService.obtenerTodasCategorias();
        Map<String, Object> stats = projectService.obtenerEstadisticasProyectosUsuario(currentUser.getUsuarioId());

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("projects", projects);
        model.addAttribute("teamInfo", teamInfo);
        model.addAttribute("categories", categories);
        model.addAttribute("stats", stats);
        model.addAttribute("currentView", "specific-team");
        model.addAttribute("currentSection", "team-projects");
        model.addAttribute("currentTeamId", teamId);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentNavSection", "projects");

        return "project/dashboard";
    }

    /**
     * Vista de proyectos donde participo
     * Ruta: /devportal/{rol}/{username}/projects/i-am-part-of
     */
    @GetMapping("/i-am-part-of")
    public String showParticipatingProjects(@PathVariable String rol,
                                            @PathVariable String username,
                                            @RequestParam(required = false) String category,
                                            @RequestParam(required = false) String search,
                                            @RequestParam(required = false) String sort,
                                            Model model,
                                            Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        if (!canAccessUserProjects(currentUser, username, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                    + "/" + currentUser.getUsername() + "/projects/i-am-part-of";
        }

        List<Map<String, Object>> projects = projectService.obtenerProyectosParticipacion(currentUser.getUsuarioId(), category, search, sort);
        List<Categoria> categories = projectService.obtenerTodasCategorias();
        Map<String, Object> stats = projectService.obtenerEstadisticasProyectosUsuario(currentUser.getUsuarioId());

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);
        model.addAttribute("projects", projects);
        model.addAttribute("categories", categories);
        model.addAttribute("stats", stats);
        model.addAttribute("currentView", "participating");
        model.addAttribute("currentSection", "i-am-part-of");
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSort", sort);

        return "project/dashboard";
    }

    /**
     * Vista de otros proyectos públicos
     * Ruta: /devportal/{rol}/{username}/projects/other-projects
     */
    @GetMapping("/other-projects")
    public String showOtherProjects(@PathVariable String rol,
                                    @PathVariable String username,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) String search,
                                    @RequestParam(required = false) String sort,
                                    Model model,
                                    Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        if (!canAccessUserProjects(currentUser, username, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                    + "/" + currentUser.getUsername() + "/projects/other-projects";
        }

        List<Map<String, Object>> projects = projectService.obtenerOtrosProyectosPublicos(currentUser.getUsuarioId(), category, search, sort);
        List<Categoria> categories = projectService.obtenerTodasCategorias();
        Map<String, Object> stats = projectService.obtenerEstadisticasProyectosUsuario(currentUser.getUsuarioId());

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);
        model.addAttribute("projects", projects);
        model.addAttribute("categories", categories);
        model.addAttribute("stats", stats);
        model.addAttribute("currentView", "other");
        model.addAttribute("currentSection", "other-projects");
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSort", sort);

        return "project/dashboard";
    }

    /**
     * Vista detallada de un proyecto específico
     * Ruta: /devportal/{rol}/{username}/projects/.../P-{projectId}
     */
    @GetMapping("/P-{projectId}")
    public String showProjectDetails(@PathVariable String rol,
                                     @PathVariable String username,
                                     @PathVariable Long projectId,
                                     Model model,
                                     Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        if (!canAccessUserProjects(currentUser, username, rol)) {
            return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                    + "/" + currentUser.getUsername() + "/projects";
        }

        Map<String, Object> project = projectService.obtenerDetallesProyecto(currentUser.getUsuarioId(), projectId);

        if (project == null) {
            return "redirect:/devportal/" + rol + "/" + username + "/projects?error=project-not-found";
        }

        // Verificar permisos para ver este proyecto
        String userPermission = (String) project.get("privilegio_usuario_actual");
        if ("SIN_ACCESO".equals(userPermission)) {
            return "redirect:/devportal/" + rol + "/" + username + "/projects?error=access-denied";
        }

        List<Map<String, Object>> collaborators = projectService.obtenerColaboradoresProyecto(projectId);
        List<Map<String, Object>> recentActivity = projectService.obtenerActividadRecienteProyecto(projectId);
        Map<String, Object> projectStats = projectService.obtenerEstadisticasProyecto(projectId);
        List<Map<String, Object>> repositories = projectService.obtenerRepositoriosProyecto(projectId);
        List<Map<String, Object>> nodes = projectService.obtenerNodosProyecto(projectId);

        // Verificar si el usuario actual es el creador del proyecto
        Long projectCreatedBy = (Long) project.get("created_by");
        boolean isCreator = projectCreatedBy != null && projectCreatedBy.equals(currentUser.getUsuarioId());
        
        // DEBUG: Logging para verificar los valores
        System.out.println("=== DEBUG PROJECT DETAILS ===");
        System.out.println("projectCreatedBy: " + projectCreatedBy + " (type: " + (projectCreatedBy != null ? projectCreatedBy.getClass().getName() : "null") + ")");
        System.out.println("currentUser.getUsuarioId(): " + currentUser.getUsuarioId() + " (type: " + currentUser.getUsuarioId().getClass().getName() + ")");
        System.out.println("isCreator: " + isCreator);
        System.out.println("project.get('created_by'): " + project.get("created_by") + " (type: " + (project.get("created_by") != null ? project.get("created_by").getClass().getName() : "null") + ")");
        System.out.println("propietario_proyecto: " + project.get("propietario_proyecto") + " (type: " + (project.get("propietario_proyecto") != null ? project.get("propietario_proyecto").getClass().getName() : "null") + ")");
        System.out.println("proyecto_id: " + project.get("proyecto_id"));
        System.out.println("=============================");

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);
        model.addAttribute("project", project);
        model.addAttribute("collaborators", collaborators);
        model.addAttribute("recentActivity", recentActivity);
        model.addAttribute("projectStats", projectStats);
        model.addAttribute("repositories", repositories);
        model.addAttribute("nodes", nodes);
        model.addAttribute("userPermission", userPermission);
        model.addAttribute("isCreator", isCreator);

        return "project/detail";
    }

    // =================== FORMULARIOS PARA CRUD ===================

    /**
     * Mostrar formulario para crear nuevo proyecto personal
     */
    @GetMapping("/create/personal")
    public String showCreatePersonalProjectForm(@PathVariable String rol,
                                                @PathVariable String username,
                                                Model model,
                                                Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);
        List<Categoria> categories = projectService.obtenerTodasCategorias();

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("categories", categories);
        model.addAttribute("proyecto", new Proyecto());
        model.addAttribute("projectType", "personal");
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);

        return "project/create";
    }

    /**
     * Mostrar formulario para crear nuevo proyecto de equipo (GRUPO)
     */
    @GetMapping("/create/group")
    public String showCreateGroupProjectForm(@PathVariable String rol,
                                            @PathVariable String username,
                                            Model model,
                                            Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);
        List<Categoria> categories = projectService.obtenerTodasCategorias();

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("categories", categories);
        model.addAttribute("proyecto", new Proyecto());
        model.addAttribute("projectType", "team");
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);
        // Agregar lista de TODOS los equipos creados por el usuario
        model.addAttribute("equipos", teamService.obtenerTodosEquiposDelUsuario(currentUser.getUsuarioId()));

        return "project/create";
    }

    /**
     * Mostrar formulario para crear nuevo proyecto empresarial
     */
    @GetMapping("/create/enterprise")
    public String showCreateEnterpriseProjectForm(@PathVariable String rol,
                                                  @PathVariable String username,
                                                  Model model,
                                                  Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);
        List<Categoria> categories = projectService.obtenerTodasCategorias();

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("categories", categories);
        model.addAttribute("proyecto", new Proyecto());
        model.addAttribute("projectType", "enterprise");
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);
        // Agregar lista de TODOS los equipos creados por el usuario
        model.addAttribute("equipos", teamService.obtenerTodosEquiposDelUsuario(currentUser.getUsuarioId()));

        return "project/create";
    }

    /**
     * Mostrar formulario para crear nuevo proyecto (ruta genérica - redirige a personal)
     */
    @GetMapping("/create")
    public String showCreateProjectForm(@PathVariable String rol,
                                        @PathVariable String username,
                                        Model model,
                                        Principal principal) {

        return "redirect:/devportal/" + rol + "/" + username + "/projects/create/personal";
    }

    /**
     * Procesar creación de proyecto
     */
    @PostMapping("/create")
    @org.springframework.transaction.annotation.Transactional
    public String createProject(@PathVariable String rol,
                                @PathVariable String username,
                                @ModelAttribute Proyecto proyecto,
                                @RequestParam(value = "categoriaId", required = false) Long categoriaId,
                                @RequestParam(value = "projectType", required = false, defaultValue = "personal") String projectType,
                                @RequestParam(value = "equipoIds", required = false) Long[] equipoIds,
                                @RequestParam(value = "propietarioNombre", required = false) String propietarioNombre,
                                @RequestParam Map<String, String> allParams,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            // INICIAR DEBUGGING
            System.out.println("\n╔════════════════════════════════════════════════════════════════");
            System.out.println("║ 🚀 POST /create - CREAR PROYECTO");
            System.out.println("║ Usuario: " + (principal != null ? principal.getName() : "null"));
            System.out.println("║ Rol: " + rol);
            System.out.println("║ Username: " + username);
            System.out.println("╚════════════════════════════════════════════════════════════════\n");
            
            System.out.println("📥 PARÁMETROS RECIBIDOS:");
            System.out.println("  projectType: '" + projectType + "'");
            System.out.println("  proyecto.nombreProyecto: '" + proyecto.getNombreProyecto() + "'");
            System.out.println("  proyecto.descripcionProyecto: '" + proyecto.getDescripcionProyecto() + "'");
            System.out.println("  categoriaId: " + categoriaId);
            System.out.println("  propietarioNombre: '" + propietarioNombre + "'");
            System.out.println("  equipoIds: " + (equipoIds != null ? java.util.Arrays.toString(equipoIds) : "null"));
            System.out.println("  allParams.size(): " + allParams.size());
            System.out.println("  temporaryTeamsCount: '" + allParams.get("temporaryTeamsCount") + "'");
            
            Usuario currentUser = userService.obtenerUsuarioActual(principal, username);
            System.out.println("  currentUser: " + currentUser.getUsername() + " (ID: " + currentUser.getUsuarioId() + ")");

            // DEBUG: Log del projectType recibido
            System.out.println("🔧 DEBUG: projectType recibido = " + projectType);
            System.out.println("🔧 DEBUG: propietarioNombre recibido = " + propietarioNombre);

            // CRÍTICO: Establecer createdBy AQUÍ porque ahora es NOT NULL en BD
            proyecto.setCreatedBy(currentUser);
            System.out.println("🔧 DEBUG: createdBy establecido a usuario = " + currentUser.getUsername());

            // Establecer el tipo de propietario según la selección del usuario
            switch (projectType.toLowerCase()) {
                case "personal":
                    proyecto.setPropietarioProyecto(Proyecto.PropietarioProyecto.USUARIO);
                    // Para proyectos personales, el nombre es el username del usuario
                    proyecto.setPropietarioNombre(currentUser.getUsername());
                    System.out.println("🔧 DEBUG: Establecido como USUARIO - propietarioNombre = " + currentUser.getUsername());
                    break;
                case "team":
                    proyecto.setPropietarioProyecto(Proyecto.PropietarioProyecto.GRUPO);
                    // Para proyectos grupales, usar el nombre ingresado por el usuario
                    if (propietarioNombre != null && !propietarioNombre.trim().isEmpty()) {
                        proyecto.setPropietarioNombre(propietarioNombre.trim());
                    } else {
                        throw new RuntimeException("El nombre del grupo es requerido para proyectos grupales");
                    }
                    System.out.println("🔧 DEBUG: Establecido como GRUPO - propietarioNombre = " + propietarioNombre);
                    break;
                case "enterprise":
                    proyecto.setPropietarioProyecto(Proyecto.PropietarioProyecto.EMPRESA);
                    // Para proyectos empresariales, usar el nombre ingresado por el usuario
                    if (propietarioNombre != null && !propietarioNombre.trim().isEmpty()) {
                        proyecto.setPropietarioNombre(propietarioNombre.trim());
                    } else {
                        throw new RuntimeException("El nombre de la empresa es requerido para proyectos empresariales");
                    }
                    System.out.println("🔧 DEBUG: Establecido como EMPRESA - propietarioNombre = " + propietarioNombre);
                    break;
                default:
                    proyecto.setPropietarioProyecto(Proyecto.PropietarioProyecto.USUARIO);
                    proyecto.setPropietarioNombre(currentUser.getUsername());
                    System.out.println("🔧 DEBUG: Establecido como USUARIO (default) - propietarioNombre = " + currentUser.getUsername());
            }

            System.out.println("🔧 DEBUG: PropietarioProyecto antes de guardar = " + proyecto.getPropietarioProyecto());
            System.out.println("🔧 DEBUG: PropietarioNombre antes de guardar = " + proyecto.getPropietarioNombre());
            System.out.println("🔧 DEBUG: CreatedBy antes de guardar = " + (proyecto.getCreatedBy() != null ? proyecto.getCreatedBy().getUsername() : "null"));

            Proyecto savedProject = projectService.guardarProyecto(proyecto, categoriaId);

            System.out.println("🔧 DEBUG: PropietarioProyecto después de guardar = " + savedProject.getPropietarioProyecto());
            System.out.println("🔧 DEBUG: PropietarioNombre después de guardar = " + savedProject.getPropietarioNombre());
            System.out.println("🔧 DEBUG: CreatedBy después de guardar = " + (savedProject.getCreatedBy() != null ? savedProject.getCreatedBy().getUsername() : "null"));
            System.out.println("🔧 DEBUG: Proyecto guardado con ID = " + savedProject.getProyectoId());

            // Asociar equipos si se seleccionaron
            if (equipoIds != null && equipoIds.length > 0) {
                System.out.println("🔧 DEBUG: Asociando " + equipoIds.length + " equipos al proyecto " + savedProject.getProyectoId());
                for (Long equipoId : equipoIds) {
                    // Saltar si es ID temporal (negativo)
                    if (equipoId < 0) {
                        System.out.println("🔧 DEBUG: ID temporal detectado: " + equipoId + " - Se procesará después de crear los equipos");
                        continue;
                    }
                    
                    // Obtener el permiso para este equipo
                    String privilegio = allParams.getOrDefault("privilegioEquipo_" + equipoId, "LECTOR");
                    System.out.println("🔧 DEBUG: Equipo " + equipoId + " con permisos: " + privilegio);
                    
                    // Asociar equipo al proyecto
                    teamService.asociarEquipoAProyecto(equipoId, savedProject.getProyectoId());
                    
                    // Actualizar permisos
                    teamService.actualizarPermisosEquipoEnProyecto(equipoId, savedProject.getProyectoId(), privilegio);
                }
            }
            
            // Procesar equipos temporales
            System.out.println("\n========== PROCESANDO EQUIPOS TEMPORALES ==========");
            System.out.println("📋 allParams keys: " + allParams.keySet());
            System.out.println("🔍 temporaryTeamsCount valor: " + allParams.get("temporaryTeamsCount"));
            
            String temporaryTeamsCountStr = allParams.get("temporaryTeamsCount");
            if (temporaryTeamsCountStr != null && !temporaryTeamsCountStr.isEmpty()) {
                try {
                    int temporaryTeamsCount = Integer.parseInt(temporaryTeamsCountStr);
                    System.out.println("✅ [DEBUG] Procesando " + temporaryTeamsCount + " equipos temporales");
                    
                    for (int i = 0; i < temporaryTeamsCount; i++) {
                        String teamName = allParams.get("temporaryTeamName_" + i);
                        String teamDesc = allParams.get("temporaryTeamDesc_" + i);
                        String permission = allParams.getOrDefault("temporaryTeamPermission_" + i, "LECTOR");
                        
                        System.out.println("  [Equipo " + i + "] nombre='" + teamName + "', desc='" + teamDesc + "', permisos='" + permission + "'");
                        
                        if (teamName != null && !teamName.trim().isEmpty()) {
                            System.out.println("  ➕ Creando equipo temporal: " + teamName + " con permisos: " + permission);
                            
                            // Crear el equipo
                            Equipo nuevoEquipo = teamService.crearEquipoEnProyecto(teamName, savedProject.getProyectoId(), currentUser.getUsuarioId());
                            System.out.println("  ✅ Equipo creado con ID: " + nuevoEquipo.getEquipoId());
                            
                            // Actualizar permisos
                            teamService.actualizarPermisosEquipoEnProyecto(nuevoEquipo.getEquipoId(), savedProject.getProyectoId(), permission);
                            System.out.println("  ✅ Permisos asignados");
                            
                            System.out.println("✅ [DEBUG] Equipo temporal '" + teamName + "' creado exitosamente con ID: " + nuevoEquipo.getEquipoId());
                        } else {
                            System.out.println("  ❌ Nombre vacío, saltando este equipo");
                        }
                    }
                    System.out.println("========== FIN EQUIPOS TEMPORALES ==========\n");
                } catch (NumberFormatException e) {
                    System.out.println("❌ [DEBUG] Error: temporaryTeamsCount no es un número válido: '" + temporaryTeamsCountStr + "'");
                    logger.warn("Error: temporaryTeamsCount no es válido: " + temporaryTeamsCountStr, e);
                } catch (Exception e) {
                    System.out.println("❌ [DEBUG] Error procesando equipos temporales: " + e.getMessage());
                    e.printStackTrace();
                    logger.warn("Error procesando equipos temporales: " + e.getMessage(), e);
                }
            } else {
                System.out.println("⚠️ [DEBUG] temporaryTeamsCount es NULL o VACÍO");
                System.out.println("========== FIN EQUIPOS TEMPORALES ==========\n");
            }

            redirectAttributes.addFlashAttribute("success", "Proyecto creado exitosamente");
            System.out.println("✅ PROYECTO CREADO EXITOSAMENTE - Redirigiendo a P-" + savedProject.getProyectoId());
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + savedProject.getProyectoId();
        } catch (Exception e) {
            System.out.println("\n❌❌❌ ERROR AL CREAR PROYECTO ❌❌❌");
            System.out.println("Mensaje: " + e.getMessage());
            System.out.println("Tipo de excepción: " + e.getClass().getName());
            
            // Mostrar la causa raíz si existe
            Throwable cause = e.getCause();
            if (cause != null) {
                System.out.println("Causa raíz: " + cause.getMessage());
                System.out.println("Tipo de causa: " + cause.getClass().getName());
            }
            
            // Stack trace completo
            e.printStackTrace();
            
            // Mensaje de error mejorado para el usuario
            String errorMsg = e.getMessage();
            if (cause != null && cause.getMessage() != null) {
                errorMsg = cause.getMessage();
            }
            
            redirectAttributes.addFlashAttribute("error", "Error al crear el proyecto: " + errorMsg);
            return "redirect:/devportal/" + rol + "/" + username + "/projects/create/" + projectType;
        }
    }

    // =================== RUTAS PARA REPOSITORIOS EN PROYECTOS ===================

    /**
     * Mostrar formulario para crear repositorio desde un proyecto
     * Ruta: /devportal/{rol}/{username}/projects/P-{projectId}/repositories/create
     */
    @GetMapping("/P-{projectId}/repositories/create")
    public String showCreateRepositoryFormFromProject(@PathVariable String rol,
                                                      @PathVariable String username,
                                                      @PathVariable Long projectId,
                                                      @RequestParam(required = false) String type,
                                                      Model model,
                                                      Principal principal) {

        System.out.println("=== DEBUG: ProjectController.showCreateRepositoryFormFromProject() ===");
        System.out.println("Project ID: " + projectId);
        System.out.println("Role: " + rol);
        System.out.println("Username: " + username);
        System.out.println("Repository Type: " + type);
        System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));

        try {
            Usuario currentUser = userService.obtenerUsuarioActual(principal, username);
            System.out.println("Current User: " + (currentUser != null ? currentUser.getUsername() : "null"));

            // Verificar que el proyecto existe y el usuario tiene acceso
            Proyecto proyecto = proyectoRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
            System.out.println("Project found: " + proyecto.getNombreProyecto());

            // Obtener categorías para el formulario
            List<Categoria> categories = projectService.obtenerTodasCategorias();
            System.out.println("Categories loaded: " + (categories != null ? categories.size() : "null"));
            
            // Obtener equipos del usuario para repositorios colaborativos
            List<Map<String, Object>> equipos = teamService.obtenerTodosEquiposDelUsuario(currentUser.getUsuarioId());
            System.out.println("User teams loaded: " + (equipos != null ? equipos.size() : "null"));

            model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("projectId", projectId);
            model.addAttribute("categories", categories);
            model.addAttribute("equipos", equipos);
            model.addAttribute("repositoryType", type); // personal o colaborativo
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);
            model.addAttribute("fromProject", true); // Indicar que viene desde proyecto

            System.out.println("Model attributes added successfully");
            System.out.println("Returning view: project/create-repository");

            return "project/create-repository";

        } catch (Exception e) {
            System.err.println("Error en showCreateRepositoryFormFromProject: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId;
        }
    }

    /**
     * Procesar creación de repositorio desde un proyecto
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/repositories/create
     */
    @PostMapping("/P-{projectId}/repositories/create")
    public String processCreateRepositoryFromProject(@PathVariable String rol,
                                                     @PathVariable String username,
                                                     @PathVariable Long projectId,
                                                     @RequestParam String nombreRepositorio,
                                                     @RequestParam(required = false) String descripcionRepositorio,
                                                     @RequestParam String tipoRepositorio,
                                                     @RequestParam String visibilidadRepositorio,
                                                     @RequestParam(required = false) Long categoriaId,
                                                     @RequestParam(required = false) List<Long> equipoIds,
                                                     @RequestParam Map<String, String> allParams,
                                                     Model model,
                                                     Principal principal,
                                                     RedirectAttributes redirectAttributes) {
        try {
            Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

            // Verificar que el proyecto existe
            proyectoRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

            // Crear el repositorio usando el servicio de repositorios

            Map<String, Object> repositoryData = new HashMap<>();
            repositoryData.put("nombre_repositorio", nombreRepositorio);
            repositoryData.put("descripcion_repositorio", descripcionRepositorio);
            repositoryData.put("visibilidad_repositorio", visibilidadRepositorio);
            repositoryData.put("tipo_repositorio", tipoRepositorio);
            repositoryData.put("proyecto_id", projectId); // Importante: vincular con el proyecto

            if (categoriaId != null) {
                repositoryData.put("categoria_id", categoriaId);
            }

            Map<String, Object> result = repositoryService.crearRepositorio(currentUser.getUsuarioId(), repositoryData);

            // Verificar si la creación fue exitosa
            Boolean success = (Boolean) result.get("success");
            if (success != null && success) {
                Long repositoryId = ((Number) result.get("repositorio_id")).longValue();
                
                // Procesar equipos asociados (solo para repositorios colaborativos)
                if ("COLABORATIVO".equalsIgnoreCase(tipoRepositorio)) {
                    System.out.println("Procesando equipos para repositorio colaborativo R-" + repositoryId);
                    
                    // Procesar equipos existentes seleccionados
                    if (equipoIds != null && !equipoIds.isEmpty()) {
                        for (Long equipoId : equipoIds) {
                            try {
                                String privilegio = allParams.getOrDefault("privilegioEquipo_" + equipoId, "LECTOR");
                                System.out.println("Asignando equipo " + equipoId + " al repositorio " + repositoryId + " con privilegio " + privilegio);
                                teamService.asignarEquipoARepositorio(equipoId, repositoryId, privilegio);
                            } catch (Exception e) {
                                System.err.println("Error asignando equipo " + equipoId + ": " + e.getMessage());
                            }
                        }
                    }
                    
                    // Procesar equipos temporales
                    String temporaryTeamsCountStr = allParams.get("temporaryTeamsCount");
                    if (temporaryTeamsCountStr != null && !temporaryTeamsCountStr.isEmpty()) {
                        try {
                            int temporaryTeamsCount = Integer.parseInt(temporaryTeamsCountStr);
                            System.out.println("Procesando " + temporaryTeamsCount + " equipos temporales");
                            
                            for (int i = 0; i < temporaryTeamsCount; i++) {
                                String teamName = allParams.get("temporaryTeamName_" + i);
                                String teamDesc = allParams.get("temporaryTeamDesc_" + i);
                                String teamPerm = allParams.getOrDefault("temporaryTeamPerm_" + i, "LECTOR");
                                
                                if (teamName != null && !teamName.trim().isEmpty()) {
                                    System.out.println("Creando equipo temporal '" + teamName + "' para repositorio R-" + repositoryId);
                                    
                                    // Crear equipo usando TeamService (crearEquipoEnRepositorio)
                                    Equipo nuevoEquipo = teamService.crearEquipoEnRepositorio(teamName, repositoryId, currentUser.getUsuarioId());
                                    
                                    if (nuevoEquipo != null) {
                                        System.out.println("Equipo temporal creado con ID: " + nuevoEquipo.getEquipoId());
                                        
                                        // Actualizar permisos si es diferente de LECTOR
                                        if (!"LECTOR".equals(teamPerm)) {
                                            teamService.actualizarPermisosEquipoEnRepositorio(nuevoEquipo.getEquipoId(), repositoryId, teamPerm);
                                            System.out.println("Permisos actualizados a " + teamPerm);
                                        }
                                    }
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Error parseando temporaryTeamsCount: " + e.getMessage());
                        }
                    }
                }
                
                redirectAttributes.addFlashAttribute("success", "Repositorio creado exitosamente y vinculado al proyecto");
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/repositories/R-" + repositoryId;
            } else {
                String errorMessage = (String) result.get("message");
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId;
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el repositorio: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId;
        }
    }



    /**
     * Mostrar formulario para editar repositorio desde proyecto
     * Ruta: /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repositoryId}/edit
     */
    @GetMapping("/P-{projectId}/repositories/R-{repositoryId}/edit")
    public String showEditRepositoryFromProject(@PathVariable String rol,
                                                @PathVariable String username,
                                                @PathVariable Long projectId,
                                                @PathVariable Long repositoryId,
                                                Model model,
                                                Principal principal) {
        try {
            Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

            // Verificar que el proyecto existe
            Proyecto proyecto = proyectoRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

            // Obtener detalles del repositorio
            Map<String, Object> repository = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repositoryId);

            if (repository == null) {
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "?error=repository-not-found";
            }

            // Obtener categorías para el select
            List<Categoria> categories = categoriaRepository.findAll();

            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("repositorio", repository);
            model.addAttribute("categories", categories);
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);

            return "project/edit-repository";

        } catch (Exception e) {
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "?error=edit-repository-error";
        }
    }

    /**
     * Procesar edición de repositorio desde proyecto
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repositoryId}/edit
     */
    @PostMapping("/P-{projectId}/repositories/R-{repositoryId}/edit")
    public String processEditRepositoryFromProject(@PathVariable String rol,
                                                   @PathVariable String username,
                                                   @PathVariable Long projectId,
                                                   @PathVariable Long repositoryId,
                                                   @RequestParam String nombreRepositorio,
                                                   @RequestParam(required = false) String descripcionRepositorio,
                                                   @RequestParam String tipoRepositorio,
                                                   @RequestParam String visibilidadRepositorio,
                                                   @RequestParam(required = false) Long categoriaId,
                                                   Model model,
                                                   Principal principal,
                                                   RedirectAttributes redirectAttributes) {
        try {
            Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

            // Verificar que el proyecto existe
            proyectoRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

            // Preparar datos para actualización
            Map<String, Object> updateData = new java.util.HashMap<>();
            updateData.put("nombre_repositorio", nombreRepositorio);
            updateData.put("descripcion_repositorio", descripcionRepositorio);
            updateData.put("visibilidad_repositorio", visibilidadRepositorio);
            updateData.put("tipo_repositorio", tipoRepositorio);

            if (categoriaId != null) {
                updateData.put("categoria_id", categoriaId);
            }

            // Actualizar repositorio usando el servicio
            Map<String, Object> result = repositoryService.actualizarRepositorio(currentUser.getUsuarioId(), repositoryId, updateData);

            // Verificar si la actualización fue exitosa
            Boolean success = (Boolean) result.get("success");
            if (success != null && success) {
                redirectAttributes.addFlashAttribute("success", "Repositorio actualizado correctamente");
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/repositories/R-" + repositoryId;
            } else {
                String errorMsg = (String) result.get("message");
                redirectAttributes.addFlashAttribute("error", errorMsg != null ? errorMsg : "Error al actualizar el repositorio");
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/repositories/R-" + repositoryId + "/edit";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al actualizar el repositorio: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/repositories/R-" + repositoryId + "/edit";
        }
    }

    /**
     * Mostrar formulario para editar proyecto
     */
    @GetMapping("/P-{projectId}/edit")
    public String showEditProjectForm(@PathVariable String rol,
                                      @PathVariable String username,
                                      @PathVariable Long projectId,
                                      Model model,
                                      Principal principal) {

        System.out.println("=== DEBUG: ProjectController.showEditProjectForm() ===");
        System.out.println("Project ID: " + projectId);
        System.out.println("Role: " + rol);
        System.out.println("Username: " + username);
        System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));

        try {
            Usuario currentUser = userService.obtenerUsuarioActual(principal, username);
            System.out.println("Current User: " + (currentUser != null ? currentUser.getUsername() : "null"));

            if (!canAccessUserProjects(currentUser, username, rol)) {
                System.out.println("Access denied for user: " + principal.getName());
                return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                        + "/" + currentUser.getUsername() + "/projects";
            }

            // Obtener proyecto directamente desde el repositorio
            Proyecto proyecto = proyectoRepository.findById(projectId).orElse(null);
            System.out.println("Project found: " + (proyecto != null ? proyecto.getNombreProyecto() : "null"));

            if (proyecto == null) {
                System.out.println("Project not found for ID: " + projectId);
                return "redirect:/devportal/" + rol + "/" + username + "/projects?error=project-not-found";
            }

            List<Categoria> categories = projectService.obtenerTodasCategorias();
            System.out.println("Categories loaded: " + (categories != null ? categories.size() : "null"));

            // Añadir datos dinámicos basados en el usuario actual
            model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);
            model.addAttribute("categories", categories);
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("baseUrl", "/devportal");
            model.addAttribute("projectId", "P-" + projectId);

            System.out.println("Model attributes added successfully");
            System.out.println("Returning view: project/edit");

            return "project/edit";
        } catch (Exception e) {
            System.err.println("Error en showEditProjectForm: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/devportal/" + rol + "/" + username + "/projects?error=edit-project-error";
        }
    }

    /**
     * Procesar actualización de proyecto
     */
    @PostMapping("/P-{projectId}/edit")
    public String updateProject(@PathVariable String rol,
                                @PathVariable String username,
                                @PathVariable Long projectId,
                                @ModelAttribute Proyecto projectDetails,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

            if (!canAccessUserProjects(currentUser, username, rol)) {
                return "redirect:/devportal/" + currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase()
                        + "/" + currentUser.getUsername() + "/projects";
            }

            System.out.println("Actualizando proyecto " + projectId + " con datos: " + projectDetails.getNombreProyecto());

            projectService.actualizarProyecto(projectId, projectDetails);
            redirectAttributes.addFlashAttribute("success", "Proyecto actualizado exitosamente");
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId;
        } catch (Exception e) {
            System.err.println("Error al actualizar proyecto: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el proyecto: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/edit";
        }
    }

    /**
     * Eliminar proyecto
     */
    @PostMapping("/P-{projectId}/delete")
    public String deleteProject(@PathVariable String rol,
                                @PathVariable String username,
                                @PathVariable Long projectId,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            projectService.eliminarProyecto(projectId);
            redirectAttributes.addFlashAttribute("success", "Proyecto eliminado exitosamente");
            return "redirect:/devportal/" + rol + "/" + username + "/projects";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el proyecto: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId;
        }
    }

    // =================== RUTAS PARA REPOSITORIOS DENTRO DE PROYECTOS ===================

    /**
     * Ver repositorio específico dentro de un proyecto
     * Ruta: /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repositoryId}
     */
    @GetMapping("/P-{projectId}/repositories/R-{repositoryId}")
    public String showRepositoryInProject(@PathVariable String rol,
                                          @PathVariable String username,
                                          @PathVariable Long projectId,
                                          @PathVariable Long repositoryId,
                                          Model model,
                                          Principal principal) {

        System.out.println("=== DEBUG: showRepositoryInProject ===");
        System.out.println("URL rol: " + rol);
        System.out.println("URL username: " + username);
        System.out.println("Project ID: " + projectId);
        System.out.println("Repository ID: " + repositoryId);
        System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
        System.out.println("Current User: " + (currentUser != null ? currentUser.getUsername() : "null"));

        if (currentUser == null) {
            System.out.println("Current user is null - redirecting to login");
            return "redirect:/signin";
        }

        try {
            // PRIMERO: Obtener repositorio para verificar permisos específicos
            System.out.println("Calling repositoryService.obtenerDetallesRepositorio with userId: " + currentUser.getUsuarioId() + ", repositoryId: " + repositoryId);
            Map<String, Object> repositoryData = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repositoryId);
            System.out.println("Repository data retrieved: " + (repositoryData != null ? repositoryData.keySet() : "null"));

            if (repositoryData == null) {
                System.out.println("Repository data is null - returning 404");
                return "error/404";
            }

            // Verificar que el usuario actual es el CREADOR del repositorio
            // Solo el creador puede acceder al repositorio desde la vista de proyecto
            String userPermission = (String) repositoryData.get("privilegio_usuario_actual");
            System.out.println("User permission on repository: " + userPermission);
            
            if (!"PROPIETARIO".equals(userPermission)) {
                System.out.println("Access denied - user is not the repository owner (current permission: " + userPermission + ")");
                String currentUserRole = currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase();
                return "redirect:/devportal/" + currentUserRole + "/" + currentUser.getUsername() 
                        + "/projects/P-" + projectId + "?error=repository-access-denied";
            }

            // Verificar que el repositorio está asociado al proyecto
            // El repositorio puede estar asociado a múltiples proyectos
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> proyectosAsociados = (List<Map<String, Object>>) repositoryData.get("proyectos_asociados");
            boolean repositorioPerteneceAlProyecto = false;
            
            if (proyectosAsociados != null && !proyectosAsociados.isEmpty()) {
                for (Map<String, Object> proyecto : proyectosAsociados) {
                    Long proyectoIdAsociado = ((Number) proyecto.get("proyecto_id")).longValue();
                    if (projectId.equals(proyectoIdAsociado)) {
                        repositorioPerteneceAlProyecto = true;
                        break;
                    }
                }
            } else if (repositoryData.get("proyecto_id") != null) {
                // Fallback: verificar proyecto_id único (legacy)
                repositorioPerteneceAlProyecto = projectId.equals(repositoryData.get("proyecto_id"));
            }
            
            System.out.println("Repository belongs to project P-" + projectId + ": " + repositorioPerteneceAlProyecto);
            if (!repositorioPerteneceAlProyecto) {
                System.out.println("Repository not associated with this project - returning 404");
                return "error/404";
            }

            // Obtener información adicional del proyecto
            System.out.println("Getting project details for projectId: " + projectId);
            Map<String, Object> proyectoData = projectService.obtenerDetallesProyecto(currentUser.getUsuarioId(), projectId);
            System.out.println("Project data retrieved: " + (proyectoData != null ? "not null" : "null"));
            if (proyectoData == null) {
                System.out.println("Project data is null - returning 404");
                return "error/404";
            }

            // Obtener colaboradores del repositorio
            System.out.println("Getting collaborators for repositoryId: " + repositoryId);
            List<Map<String, Object>> collaborators = repositoryService.obtenerColaboradoresRepositorio(repositoryId);
            System.out.println("Collaborators retrieved: " + (collaborators != null ? collaborators.size() : "null"));

            // Crear objeto Proyecto para compatibilidad con la vista
            System.out.println("Finding project entity by ID: " + projectId);
            Proyecto proyecto = proyectoRepository.findById(projectId).orElse(null);
            System.out.println("Project entity found: " + (proyecto != null ? proyecto.getNombreProyecto() : "null"));

            // Crear estadísticas básicas
            System.out.println("Creating stats and preparing model");
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("total_commits", "N/A");
            stats.put("total_branches", "N/A");

            System.out.println("Adding attributes to model");
            model.addAttribute("repository", repositoryData);
            model.addAttribute("proyecto", proyecto); // Usar 'proyecto' como espera la vista
            model.addAttribute("collaborators", collaborators);
            model.addAttribute("stats", stats);
            model.addAttribute("actividades", new java.util.ArrayList<>());
            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("currentUser", currentUser); // Para el navbar
            // Usar el rol y username del usuario ACTUAL, no de la URL
            String currentUserRole = currentUser.getRoles().iterator().next().getNombreRol().toString().toLowerCase();
            model.addAttribute("rol", currentUserRole); // Cambiado de 'userRole' a 'rol' para el navbar
            model.addAttribute("userRole", currentUserRole); // Mantener para compatibilidad con breadcrumbs
            model.addAttribute("username", currentUser.getUsername());
            model.addAttribute("isOwner", repositoryData.get("privilegio_usuario_actual").equals("PROPIETARIO"));
            model.addAttribute("canEdit", repositoryData.get("privilegio_usuario_actual").equals("PROPIETARIO") ||
                    repositoryData.get("privilegio_usuario_actual").equals("ADMIN"));

            System.out.println("Returning view: project/detail_repository");
            return "project/detail_repository";

        } catch (Exception e) {
            System.err.println("Exception in showRepositoryInProject: " + e.getMessage());
            e.printStackTrace();
            return "error/500";
        }
    }

    // =================== MÉTODOS DE UTILIDAD ===================

    private boolean canAccessUserProjects(Usuario currentUser, String requestedUsername, String requestedRole) {
        System.out.println("=== DEBUG: canAccessUserProjects ===");
        System.out.println("Current user username: '" + currentUser.getUsername() + "'");
        System.out.println("Requested username: '" + requestedUsername + "'");
        System.out.println("Requested role: '" + requestedRole + "'");
        System.out.println("Usernames equal: " + currentUser.getUsername().equals(requestedUsername));

        // El usuario puede acceder a sus propios proyectos
        if (currentUser.getUsername().equals(requestedUsername)) {
            System.out.println("Access granted - same username");
            return true;
        }

        // Los administradores pueden acceder a cualquier vista (para impersonación)
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(rol -> "SA".equals(rol.getNombreRol().toString()));
        System.out.println("Is admin: " + isAdmin);

        if (isAdmin) {
            System.out.println("Access granted - is admin");
        } else {
            System.out.println("Access denied - not same user and not admin");
        }

        return isAdmin;
    }

    // =================== ROLES Y GESTIÓN DE EQUIPO ===================

    /**
     * Listar roles de un proyecto
     * Ruta: /devportal/{rol}/{username}/projects/P-{projectId}/roles
     */
    @GetMapping("/P-{projectId}/roles")
    public String showProjectRoles(@PathVariable String rol,
                                   @PathVariable String username,
                                   @PathVariable Long projectId,
                                   Model model,
                                   Principal principal) {
        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        try {
            Proyecto proyecto = proyectoRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

            // Validar que el usuario es creador
            if (!proyecto.getCreatedBy().getUsuarioId().equals(currentUser.getUsuarioId())) {
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "?error=access-denied";
            }

            // Obtener roles activos
            List<org.project.project.model.dto.ProjectRoleDTO> roles = projectService.getProjectRoleService()
                    .listarRolesActivos(projectId);

            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("roles", roles);
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);

            return "project/roles-management";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar roles: " + e.getMessage());
            return "project/error";
        }
    }

    /**
     * Formulario para crear rol
     * Ruta: /devportal/{rol}/{username}/projects/P-{projectId}/roles/create-roles
     */
    @GetMapping("/P-{projectId}/roles/create-roles")
    public String showCreateRoleForm(@PathVariable String rol,
                                     @PathVariable String username,
                                     @PathVariable Long projectId,
                                     @RequestParam(required = false, defaultValue = "roles") String returnTo,
                                     Model model,
                                     Principal principal) {
        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        try {
            Proyecto proyecto = proyectoRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

            if (!proyecto.getCreatedBy().getUsuarioId().equals(currentUser.getUsuarioId())) {
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId;
            }

            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);
            model.addAttribute("returnTo", returnTo);

            return "project/create-role";

        } catch (Exception e) {
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId;
        }
    }

    /**
     * Procesar creación de rol
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/roles/create-roles
     */
    @PostMapping("/P-{projectId}/roles/create-roles")
    public String createRole(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long projectId,
                            @RequestParam String nombreRol,
                            @RequestParam(required = false) String descripcion,
                            @RequestParam(required = false, defaultValue = "roles") String returnTo,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        try {
            projectService.getProjectRoleService()
                    .crearRol(projectId, nombreRol, descripcion, currentUser.getUsuarioId());

            redirectAttributes.addFlashAttribute("success", "Rol creado exitosamente");
            
            // Redirigir según de dónde viene
            String redirectPath;
            if ("invite-E".equals(returnTo)) {
                redirectPath = "/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/invite-E";
            } else if ("invite-G".equals(returnTo)) {
                redirectPath = "/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/invite-G";
            } else {
                redirectPath = "/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/roles";
            }
            
            return "redirect:" + redirectPath;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/roles/create-roles?returnTo=" + returnTo;
        }
    }

    /**
     * Eliminar rol
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/roles/{roleId}/delete
     */
    @PostMapping("/P-{projectId}/roles/{roleId}/delete")
    public String deleteRole(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long projectId,
                            @PathVariable Long roleId,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        try {
            projectService.getProjectRoleService()
                    .eliminarRol(roleId, currentUser.getUsuarioId());

            redirectAttributes.addFlashAttribute("success", "Rol eliminado exitosamente");
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/invite-E";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/invite-E";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/invite-E";
        }
    }

    /**
     * Formulario para editar rol
     * Ruta: /devportal/{rol}/{username}/projects/P-{projectId}/roles/update-roles
     */
    @GetMapping("/P-{projectId}/roles/update-roles")
    public String showUpdateRoleForm(@PathVariable String rol,
                                     @PathVariable String username,
                                     @PathVariable Long projectId,
                                     @RequestParam(required = false) Long roleId,
                                     Model model,
                                     Principal principal) {
        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        try {
            Proyecto proyecto = proyectoRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

            if (!proyecto.getCreatedBy().getUsuarioId().equals(currentUser.getUsuarioId())) {
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId;
            }

            List<org.project.project.model.dto.ProjectRoleDTO> roles = projectService.getProjectRoleService()
                    .listarTodosRoles(projectId);

            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("roles", roles);
            model.addAttribute("selectedRoleId", roleId);
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);

            return "project/update-role";

        } catch (Exception e) {
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/roles";
        }
    }

    /**
     * Procesar actualización de rol
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/roles/update-roles
     */
    @PostMapping("/P-{projectId}/roles/update-roles")
    public String updateRole(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long projectId,
                            @RequestParam Long roleId,
                            @RequestParam String nombreRol,
                            @RequestParam(required = false) String descripcion,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        try {
            projectService.getProjectRoleService()
                    .actualizarRol(roleId, nombreRol, descripcion, currentUser.getUsuarioId());

            redirectAttributes.addFlashAttribute("success", "Rol actualizado exitosamente");
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/roles";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/roles/update-roles";
        }
    }

    /**
     * Listar miembros del proyecto
     * Ruta: /devportal/{rol}/{username}/projects/P-{projectId}/members
     */
    @GetMapping("/P-{projectId}/members")
    public String showProjectMembers(@PathVariable String rol,
                                     @PathVariable String username,
                                     @PathVariable Long projectId,
                                     Model model,
                                     Principal principal) {
        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        try {
            Proyecto proyecto = proyectoRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

            List<org.project.project.model.dto.ProjectMemberDTO> members = projectService.getProjectTeamService()
                    .listarMiembros(projectId, currentUser.getUsuarioId());

            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("members", members);
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);
            model.addAttribute("isCreator", proyecto.getCreatedBy().getUsuarioId().equals(currentUser.getUsuarioId()));

            return "project/members-management";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar miembros: " + e.getMessage());
            return "project/error";
        }
    }

    /**
     * Formulario para invitar usuarios (grupal)
     * Ruta: /devportal/{rol}/{username}/projects/P-{projectId}/invite-G
     */
    @GetMapping("/P-{projectId}/invite-G")
    public String showInviteGroupForm(@PathVariable String rol,
                                      @PathVariable String username,
                                      @PathVariable Long projectId,
                                      Model model,
                                      Principal principal) {
        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        try {
            logger.info("🎯 [ProjectController] showInviteGroupForm - projectId: {}, currentUser: {}", projectId, currentUser.getUsername());
            
            Proyecto proyecto = proyectoRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

            logger.info("📋 [ProjectController] Proyecto encontrado: {}, CreatedBy: {}", proyecto.getNombreProyecto(), proyecto.getCreatedBy().getUsername());

            // Verificar que el usuario sea el propietario
            boolean isOwner = proyecto.getCreatedBy().getUsuarioId().equals(currentUser.getUsuarioId());
            
            logger.info("🔐 [ProjectController] isOwner: {}", isOwner);

            if (!isOwner) {
                logger.warn("⚠️ [ProjectController] Usuario {} no tiene permisos para invitar a proyecto {}", currentUser.getUsername(), projectId);
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId;
            }

            List<org.project.project.model.dto.ProjectRoleDTO> roles = projectService.getProjectRoleService()
                    .listarRolesActivos(projectId);

            logger.info("✅ [ProjectController] Roles activos obtenidos: {}", roles.size());

            // ✅ FIX: Obtener equipos asociados a ESTE proyecto específico
            // Solo el creador del proyecto puede invitar, entonces buscamos equipos del proyecto
            List<Equipo> equipos = teamService.obtenerEquiposDelProyecto(projectId);
            logger.info("✅ [ProjectController] Equipos del proyecto obtenidos: {}", equipos.size());

            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("roles", roles);
            model.addAttribute("equipos", equipos);
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);
            model.addAttribute("invitationType", "GROUP");

            return "project/invite-users";

        } catch (Exception e) {
            logger.error("❌ [ProjectController] Error en showInviteGroupForm: {}", e.getMessage(), e);
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId;
        }
    }

    /**
     * Formulario para invitar usuarios (empresarial)
     * Ruta: /devportal/{rol}/{username}/projects/P-{projectId}/invite-E
     */
    @GetMapping("/P-{projectId}/invite-E")
    public String showInviteEnterpriseForm(@PathVariable String rol,
                                           @PathVariable String username,
                                           @PathVariable Long projectId,
                                           Model model,
                                           Principal principal) {
        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        try {
            Proyecto proyecto = proyectoRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

            if (!proyecto.getCreatedBy().getUsuarioId().equals(currentUser.getUsuarioId())) {
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId;
            }

            if (!Proyecto.PropietarioProyecto.EMPRESA.equals(proyecto.getPropietarioProyecto())) {
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId 
                        + "?error=not-enterprise";
            }

            List<org.project.project.model.dto.ProjectRoleDTO> roles = projectService.getProjectRoleService()
                    .listarRolesActivos(projectId);

            // Obtener dominio corporativo del usuario logueado
            String dominioCorporativo = projectService.extraerDominioCorporativo(currentUser.getCorreo());
            
            // Obtener usuarios con dominio corporativo
            List<Map<String, Object>> corporateUsers = projectService.obtenerUsuariosConDominioCorporativo(
                    currentUser.getCorreo(), 
                    currentUser.getUsuarioId()
            );

            // ✅ FIX: Obtener equipos asociados a ESTE proyecto específico
            List<Equipo> equipos = teamService.obtenerEquiposDelProyecto(projectId);
            logger.info("✅ [ProjectController] Equipos del proyecto (empresarial) obtenidos: {}", equipos.size());

            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);  // Para chatbot widget
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("roles", roles);
            model.addAttribute("equipos", equipos);
            model.addAttribute("corporateUsers", corporateUsers);
            model.addAttribute("corporateDomain", dominioCorporativo);
            model.addAttribute("userRole", rol);
            model.addAttribute("username", username);
            model.addAttribute("invitationType", "ENTERPRISE");

            return "project/invite-users";

        } catch (Exception e) {
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId;
        }
    }

    /**
     * Procesar invitación de usuarios
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/invite (grupal o empresarial)
     */
    @PostMapping("/P-{projectId}/invite")
    public String processInviteUsers(@PathVariable String rol,
                                     @PathVariable String username,
                                     @PathVariable Long projectId,
                                     @RequestParam String emails,
                                     @RequestParam String rolIds,
                                     @RequestParam String equipoIds,
                                     @RequestParam String permission,
                                     @RequestParam String invitationType, // GROUP o ENTERPRISE
                                     @RequestParam(required = false) Integer temporaryTeamsCount,
                                     HttpServletRequest httpRequest,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        logger.info("╔════════════════════════════════════════════════════════╗");
        logger.info("║          PROCESANDO INVITACIÓN DE USUARIOS            ║");
        logger.info("╚════════════════════════════════════════════════════════╝");
        logger.info("📋 Datos recibidos:");
        logger.info("   - Tipo de invitación: {}", invitationType);
        logger.info("   - Emails raw: {}", emails);
        logger.info("   - RolIds raw: {}", rolIds);
        logger.info("   - EquipoIds raw: {}", equipoIds);
        logger.info("   - Permission: {}", permission);
        logger.info("   - Temporary teams count: {}", temporaryTeamsCount);
        
        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        try {
            // Parse JSON arrays from form data
            org.project.project.model.dto.InviteUserRequest request = parseInviteRequest(emails, rolIds, equipoIds, permission);
            
            // Parse temporary team names if present
            if (temporaryTeamsCount != null && temporaryTeamsCount > 0) {
                Map<Long, String> teamNamesMap = new java.util.HashMap<>();
                List<Long> equipoIdsList = request.getEquipoIds();
                
                for (int i = 0; i < temporaryTeamsCount; i++) {
                    String paramName = "temporaryTeamName_" + i;
                    String teamName = httpRequest.getParameter(paramName);
                    
                    if (teamName != null && !teamName.trim().isEmpty()) {
                        // Find the corresponding negative ID from equipoIds
                        if (equipoIdsList != null && i < equipoIdsList.size()) {
                            Long equipoId = equipoIdsList.get(i);
                            if (equipoId < 0) {
                                teamNamesMap.put(equipoId, teamName.trim());
                                logger.info("📝 Mapped temporary team: ID={} -> Name={}", equipoId, teamName);
                            }
                        }
                    }
                }
                
                request.setTeamNamesMap(teamNamesMap);
                logger.info("✅ Team names map created with {} entries", teamNamesMap.size());
            }
            
            logger.info("🔍 [INVITE] Parsed request: {}", request);

            Map<String, Object> result;

            if ("ENTERPRISE".equalsIgnoreCase(invitationType)) {
                logger.info("🏢 Llamando a invitarUsuariosEmpresarial...");
                result = projectService.getProjectTeamService()
                        .invitarUsuariosEmpresarial(projectId, request, currentUser.getUsuarioId());
            } else {
                logger.info("👥 Llamando a invitarUsuariosGrupal...");
                result = projectService.getProjectTeamService()
                        .invitarUsuariosGrupal(projectId, request, currentUser.getUsuarioId());
            }

            Boolean success = (Boolean) result.get("success");
            String message = (String) result.get("mensaje");
            
            logger.info("📊 Resultado de invitación:");
            logger.info("   - Success: {}", success);
            logger.info("   - Message: {}", message);

            if (success) {
                redirectAttributes.addFlashAttribute("success", message);
            } else {
                redirectAttributes.addFlashAttribute("error", message);
            }

            // Redirigir a la página del proyecto (no a /members)
            String redirectUrl = "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId;
            logger.info("✅ Redirigiendo a: {}", redirectUrl);
            return redirectUrl;

        } catch (Exception e) {
            logger.error("❌ Error procesando invitación: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            // Redirigir a la página de invitación correcta según el tipo
            String invitePage = "ENTERPRISE".equalsIgnoreCase(invitationType) ? "/invite-E" : "/invite-G";
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + invitePage;
        }
    }

    /**
     * Remover miembro del proyecto
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/members/remove
     */
    @PostMapping("/P-{projectId}/members/remove")
    public String removeMember(@PathVariable String rol,
                              @PathVariable String username,
                              @PathVariable Long projectId,
                              @RequestParam Long userId,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

        try {
            projectService.getProjectTeamService()
                    .removerMiembro(projectId, userId, currentUser.getUsuarioId());

            redirectAttributes.addFlashAttribute("success", "Miembro removido exitosamente");
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/members";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/members";
        }
    }

    /**
     * DEPRECADO: Movido a ProyectoInvitacionController
     * Aceptar invitación a proyecto via token
     * Ruta: GET /invitations/accept?token=XXX
     */
    /*
    @GetMapping("/projects/invitations/accept")
    public String acceptProjectInvitation(@RequestParam String token,
                                         RedirectAttributes redirectAttributes) {
        try {
            // Validar token
            Token invitationToken = tokenService.validarTokenInvitacion(token);
            if (invitationToken == null) {
                redirectAttributes.addFlashAttribute("error", "El token de invitación es inválido o ha expirado");
                return "redirect:/devportal/dashboard";
            }

            Usuario usuario = invitationToken.getUsuario();
            logger.info("✅ Token válido. Usuario: {}", usuario.getCorreo());

            redirectAttributes.addFlashAttribute("success", "¡Invitación aceptada! Ya eres miembro del proyecto.");
            
            // Revocar el token después de usarlo
            tokenService.revocarToken(invitationToken);

            // Redirigir al dashboard o a la página de proyectos del usuario
            return "redirect:/devportal/" + usuario.getUsername() + "/projects";

        } catch (Exception e) {
            logger.error("❌ Error aceptando invitación: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/devportal/dashboard";
        }
    }
    */

    /**
     * DEPRECADO: Movido a ProyectoInvitacionController
     * Rechazar invitación a proyecto via token
     * Ruta: GET /invitations/decline?token=XXX
     */
    /*
    @GetMapping("/projects/invitations/decline")
    public String declineProjectInvitation(@RequestParam String token,
                                          RedirectAttributes redirectAttributes) {
        try {
            // Validar token
            Token invitationToken = tokenService.validarTokenInvitacion(token);
            if (invitationToken == null) {
                redirectAttributes.addFlashAttribute("error", "El token de invitación es inválido o ha expirado");
                return "redirect:/devportal/dashboard";
            }

            Usuario usuario = invitationToken.getUsuario();
            logger.info("⚠️ Invitación rechazada por usuario: {}", usuario.getCorreo());

            redirectAttributes.addFlashAttribute("success", "Has rechazado la invitación al proyecto.");
            
            // Revocar el token después de usarlo
            tokenService.revocarToken(invitationToken);

            // Redirigir al dashboard
            return "redirect:/devportal/dashboard";

        } catch (Exception e) {
            logger.error("❌ Error rechazando invitación: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/devportal/dashboard";
        }
    }
    */

    /**
     * Endpoint para validar nombre de equipo en tiempo real
     * Verifica si un nombre de equipo ya existe en el proyecto
     */
    @GetMapping("/P-{projectId}/validate-team-name")
    @ResponseBody
    public Map<String, Object> validateTeamName(@PathVariable Long projectId,
                                                 @RequestParam String teamName,
                                                 Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (teamName == null || teamName.trim().isEmpty()) {
                response.put("valid", false);
                response.put("message", "El nombre del equipo no puede estar vacío");
                return response;
            }
            
            boolean exists = equipoHasProyectoRepository
                    .existsByProjectIdAndTeamName(projectId, teamName.trim());
            
            response.put("valid", !exists);
            if (exists) {
                response.put("message", "Este nombre de equipo ya existe en el proyecto");
            } else {
                response.put("message", "Nombre disponible");
            }
            
        } catch (Exception e) {
            logger.error("Error validating team name: {}", e.getMessage());
            response.put("valid", false);
            response.put("message", "Error al validar el nombre");
        }
        
        return response;
    }

    /**
     * Parse JSON strings from form data into InviteUserRequest DTO
     */
    private org.project.project.model.dto.InviteUserRequest parseInviteRequest(
            String emailsJson, String rolIdsJson, String equipoIdsJson, String permission) {
        
        org.project.project.model.dto.InviteUserRequest request = new org.project.project.model.dto.InviteUserRequest();
        
        // Parse emails
        try {
            if (emailsJson != null && !emailsJson.trim().isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                request.setEmails(mapper.readValue(emailsJson, 
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            }
        } catch (Exception e) {
            logger.error("❌ Error parsing emails: {}", e.getMessage());
            request.setEmails(List.of());
        }
        
        // Parse rolIds
        try {
            if (rolIdsJson != null && !rolIdsJson.trim().isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                request.setRolIds(mapper.readValue(rolIdsJson, 
                    mapper.getTypeFactory().constructCollectionType(List.class, Long.class)));
            }
        } catch (Exception e) {
            logger.error("❌ Error parsing rolIds: {}", e.getMessage());
            request.setRolIds(List.of());
        }
        
        // Parse equipoIds
        try {
            if (equipoIdsJson != null && !equipoIdsJson.trim().isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                request.setEquipoIds(mapper.readValue(equipoIdsJson, 
                    mapper.getTypeFactory().constructCollectionType(List.class, Long.class)));
            }
        } catch (Exception e) {
            logger.error("❌ Error parsing equipoIds: {}", e.getMessage());
            request.setEquipoIds(List.of());
        }
        
        request.setPermission(permission);
        request.setSendEmail(true);
        
        return request;
    }
}
