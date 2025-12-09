package org.project.project.controller.rest;

import org.project.project.service.ProjectService;
import org.project.project.service.UserService;
import org.project.project.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller para operaciones AJAX en proyectos
 * Proporciona endpoints JSON para cargar proyectos sin recarga de página
 * 
 * @author DevPortal Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectRestController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    /**
     * GET /api/projects/personal
     * Obtiene proyectos personales del usuario autenticado
     * 
     * @param category Categoría para filtrar (opcional)
     * @param search Texto de búsqueda (opcional)
     * @param sort Orden: 'name', 'recent', 'oldest' (opcional, default: 'recent')
     * @param page Número de página 0-indexed (opcional, default: 0)
     * @param principal Usuario autenticado
     * @return JSON con proyectos y metadata de paginación
     * 
     * Ejemplo: GET /api/projects/personal?page=0&category=Backend&search=api&sort=recent
     * 
     * Response:
     * {
     *   "projects": [...],
     *   "totalPages": 3,
     *   "currentPage": 0,
     *   "hasNext": true,
     *   "startIndex": 1,
     *   "endIndex": 12,
     *   "totalProjects": 25
     * }
     */
    @GetMapping("/personal")
    public ResponseEntity<Map<String, Object>> getPersonalProjects(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            Principal principal) {

        // Obtener usuario autenticado
        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        // Obtener proyectos paginados (12 por página)
        List<Map<String, Object>> projects = projectService.obtenerProyectosPersonalesPaginado(
            currentUser.getUsuarioId(), category, search, sort, page
        );

        // Obtener conteo total para calcular paginación
        List<Map<String, Object>> allProjects = projectService.obtenerProyectosPersonales(
            currentUser.getUsuarioId(), category, search, sort
        );

        Map<String, Object> stats = projectService.obtenerEstadisticasProyectosUsuario(currentUser.getUsuarioId());

        // Preparar respuesta con metadata
        Map<String, Object> response = new HashMap<>();
        response.put("projects", projects);
        response.put("totalProjects", allProjects.size());
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) allProjects.size() / 12));
        response.put("hasNext", projects.size() >= 12);
        response.put("startIndex", (page * 12) + 1);
        response.put("endIndex", Math.min((page + 1) * 12, allProjects.size()));
        response.put("stats", stats);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/projects/team
     * Obtiene proyectos de equipos del usuario autenticado
     * 
     * @param category Categoría para filtrar (opcional)
     * @param search Texto de búsqueda (opcional)
     * @param sort Orden: 'name', 'recent', 'oldest' (opcional, default: 'recent')
     * @param page Número de página 0-indexed (opcional, default: 0)
     * @param principal Usuario autenticado
     * @return JSON con proyectos y metadata de paginación
     */
    @GetMapping("/team")
    public ResponseEntity<Map<String, Object>> getTeamProjects(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        List<Map<String, Object>> projects = projectService.obtenerProyectosEquiposPaginado(
            currentUser.getUsuarioId(), category, search, sort, page
        );

        List<Map<String, Object>> allProjects = projectService.obtenerProyectosEquipos(
            currentUser.getUsuarioId(), category, search, sort
        );

        Map<String, Object> stats = projectService.obtenerEstadisticasProyectosUsuario(currentUser.getUsuarioId());

        Map<String, Object> response = new HashMap<>();
        response.put("projects", projects);
        response.put("totalProjects", allProjects.size());
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) allProjects.size() / 12));
        response.put("hasNext", projects.size() >= 12);
        response.put("startIndex", (page * 12) + 1);
        response.put("endIndex", Math.min((page + 1) * 12, allProjects.size()));
        response.put("stats", stats);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/projects/other
     * Obtiene otros proyectos públicos (donde el usuario NO participa)
     * 
     * @param category Categoría para filtrar (opcional)
     * @param search Texto de búsqueda (opcional)
     * @param sort Orden: 'name', 'recent', 'oldest' (opcional, default: 'recent')
     * @param page Número de página 0-indexed (opcional, default: 0)
     * @param principal Usuario autenticado
     * @return JSON con proyectos y metadata de paginación
     */
    @GetMapping("/other")
    public ResponseEntity<Map<String, Object>> getOtherProjects(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        List<Map<String, Object>> projects = projectService.obtenerOtrosProyectosPublicosPaginado(
            currentUser.getUsuarioId(), category, search, sort, page
        );

        List<Map<String, Object>> allProjects = projectService.obtenerOtrosProyectosPublicos(
            currentUser.getUsuarioId(), category, search, sort
        );

        Map<String, Object> stats = projectService.obtenerEstadisticasProyectosUsuario(currentUser.getUsuarioId());

        Map<String, Object> response = new HashMap<>();
        response.put("projects", projects);
        response.put("totalProjects", allProjects.size());
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) allProjects.size() / 12));
        response.put("hasNext", projects.size() >= 12);
        response.put("startIndex", (page * 12) + 1);
        response.put("endIndex", Math.min((page + 1) * 12, allProjects.size()));
        response.put("stats", stats);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/projects/all
     * Obtiene TODOS los proyectos donde participo (personal + equipos)
     * 
     * @param category Categoría para filtrar (opcional)
     * @param search Texto de búsqueda (opcional)
     * @param sort Orden: 'name', 'recent', 'oldest' (opcional, default: 'recent')
     * @param page Número de página 0-indexed (opcional, default: 0)
     * @param principal Usuario autenticado
     * @return JSON con proyectos y metadata de paginación
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllMyProjects(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        // Obtener proyectos paginados donde participo (12 por página)
        List<Map<String, Object>> projects = projectService.obtenerProyectosEnLosQueParticipoPersonalesYEquiposPaginado(
            currentUser.getUsuarioId(), category, search, sort, page
        );

        // Obtener conteo total para calcular paginación
        List<Map<String, Object>> allProjects = projectService.obtenerProyectosEnLosQueParticipoPersonalesYEquipos(
            currentUser.getUsuarioId(), category, search, sort
        );

        Map<String, Object> stats = projectService.obtenerEstadisticasProyectosUsuario(currentUser.getUsuarioId());

        Map<String, Object> response = new HashMap<>();
        response.put("projects", projects);
        response.put("totalProjects", allProjects.size());
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) allProjects.size() / 12));
        response.put("hasNext", projects.size() >= 12);
        response.put("startIndex", (page * 12) + 1);
        response.put("endIndex", Math.min((page + 1) * 12, allProjects.size()));
        response.put("stats", stats);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/projects/stats
     * Obtiene estadísticas de proyectos del usuario
     * 
     * @param principal Usuario autenticado
     * @return JSON con estadísticas de proyectos
     * 
     * Response:
     * {
     *   "personalCount": 5,
     *   "teamCount": 3,
     *   "totalCount": 8
     * }
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProjectStats(Principal principal) {
        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
        Map<String, Object> stats = projectService.obtenerEstadisticasProyectosUsuario(currentUser.getUsuarioId());
        return ResponseEntity.ok(stats);
    }
}
