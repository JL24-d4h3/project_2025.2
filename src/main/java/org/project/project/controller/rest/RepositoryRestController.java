package org.project.project.controller.rest;

import org.project.project.service.RepositoryService;
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
 * REST Controller para operaciones AJAX en repositorios
 * Proporciona endpoints JSON para cargar repositorios sin recarga de página
 * 
 * @author DevPortal Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/repositories")
public class RepositoryRestController {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private UserService userService;

    /**
     * GET /api/repositories/personal
     * Obtiene repositorios personales del usuario autenticado
     * 
     * @param category Categoría para filtrar (opcional)
     * @param search Texto de búsqueda (opcional)
     * @param sort Orden: 'name', 'recent', 'oldest' (opcional, default: 'recent')
     * @param page Número de página 0-indexed (opcional, default: 0)
     * @param principal Usuario autenticado
     * @return JSON con repositorios y metadata de paginación
     * 
     * Ejemplo: GET /api/repositories/personal?page=0&category=Backend&search=api&sort=recent
     * 
     * Response:
     * {
     *   "repositories": [...],
     *   "totalPages": 3,
     *   "currentPage": 0,
     *   "hasNext": true,
     *   "startIndex": 1,
     *   "endIndex": 12,
     *   "totalRepositories": 25
     * }
     */
    @GetMapping("/personal")
    public ResponseEntity<Map<String, Object>> getPersonalRepositories(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            Principal principal) {

        // Obtener usuario autenticado
        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        // Obtener repositorios paginados (12 por página)
        List<Map<String, Object>> repositories = repositoryService.obtenerRepositoriosPersonalesPaginado(
            currentUser.getUsuarioId(), category, search, sort, page
        );

        // Obtener conteo total para calcular paginación
        List<Map<String, Object>> allRepositories = repositoryService.obtenerRepositoriosPersonales(
            currentUser.getUsuarioId(), category, search, sort
        );

        // Obtener estadísticas para actualizar contadores
        Map<String, Object> stats = repositoryService.obtenerEstadisticasRepositoriosUsuario(currentUser.getUsuarioId());
        
        // Preparar respuesta con metadata
        Map<String, Object> response = new HashMap<>();
        response.put("repositories", repositories);
        response.put("totalRepositories", allRepositories.size());
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) allRepositories.size() / 12));
        response.put("hasNext", repositories.size() >= 12);
        response.put("startIndex", (page * 12) + 1);
        response.put("endIndex", Math.min((page + 1) * 12, allRepositories.size()));
        response.put("stats", stats);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/repositories/collaborative
     * Obtiene repositorios colaborativos del usuario autenticado
     * 
     * @param category Categoría para filtrar (opcional)
     * @param search Texto de búsqueda (opcional)
     * @param sort Orden: 'name', 'recent', 'oldest' (opcional, default: 'recent')
     * @param page Número de página 0-indexed (opcional, default: 0)
     * @param principal Usuario autenticado
     * @return JSON con repositorios y metadata de paginación
     */
    @GetMapping("/collaborative")
    public ResponseEntity<Map<String, Object>> getCollaborativeRepositories(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        List<Map<String, Object>> repositories = repositoryService.obtenerRepositoriosColaborativosPaginado(
            currentUser.getUsuarioId(), category, search, sort, page
        );

        List<Map<String, Object>> allRepositories = repositoryService.obtenerRepositoriosColaborativos(
            currentUser.getUsuarioId(), category, search, sort
        );

        Map<String, Object> stats = repositoryService.obtenerEstadisticasRepositoriosUsuario(currentUser.getUsuarioId());

        Map<String, Object> response = new HashMap<>();
        response.put("repositories", repositories);
        response.put("totalRepositories", allRepositories.size());
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) allRepositories.size() / 12));
        response.put("hasNext", repositories.size() >= 12);
        response.put("startIndex", (page * 12) + 1);
        response.put("endIndex", Math.min((page + 1) * 12, allRepositories.size()));
        response.put("stats", stats);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/repositories/other
     * Obtiene otros repositorios (donde el usuario NO participa)
     * 
     * @param category Categoría para filtrar (opcional)
     * @param search Texto de búsqueda (opcional)
     * @param sort Orden: 'name', 'recent', 'oldest' (opcional, default: 'recent')
     * @param page Número de página 0-indexed (opcional, default: 0)
     * @param principal Usuario autenticado
     * @return JSON con repositorios y metadata de paginación
     */
    @GetMapping("/other")
    public ResponseEntity<Map<String, Object>> getOtherRepositories(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        List<Map<String, Object>> repositories = repositoryService.obtenerOtrosRepositoriosPaginado(
            currentUser.getUsuarioId(), category, search, sort, page
        );

        List<Map<String, Object>> allRepositories = repositoryService.obtenerOtrosRepositorios(
            currentUser.getUsuarioId(), category, search, sort
        );

        Map<String, Object> stats = repositoryService.obtenerEstadisticasRepositoriosUsuario(currentUser.getUsuarioId());

        Map<String, Object> response = new HashMap<>();
        response.put("repositories", repositories);
        response.put("totalRepositories", allRepositories.size());
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) allRepositories.size() / 12));
        response.put("hasNext", repositories.size() >= 12);
        response.put("startIndex", (page * 12) + 1);
        response.put("endIndex", Math.min((page + 1) * 12, allRepositories.size()));
        response.put("stats", stats);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/repositories/all
     * Obtiene todos mis repositorios (personal + colaborativo)
     * 
     * @param category Categoría para filtrar (opcional)
     * @param search Texto de búsqueda (opcional)
     * @param sort Orden: 'name', 'recent', 'oldest' (opcional, default: 'recent')
     * @param page Número de página 0-indexed (opcional, default: 0)
     * @param principal Usuario autenticado
     * @return JSON con repositorios y metadata de paginación
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllMyRepositories(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            Principal principal) {

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

        List<Map<String, Object>> repositories = repositoryService.obtenerTodosMisRepositoriosPaginado(
            currentUser.getUsuarioId(), category, search, sort, page
        );

        List<Map<String, Object>> allRepositories = repositoryService.obtenerTodosMisRepositorios(
            currentUser.getUsuarioId(), category, search, sort
        );

        Map<String, Object> stats = repositoryService.obtenerEstadisticasRepositoriosUsuario(currentUser.getUsuarioId());

        Map<String, Object> response = new HashMap<>();
        response.put("repositories", repositories);
        response.put("totalRepositories", allRepositories.size());
        response.put("currentPage", page);
        response.put("totalPages", (int) Math.ceil((double) allRepositories.size() / 12));
        response.put("hasNext", repositories.size() >= 12);
        response.put("startIndex", (page * 12) + 1);
        response.put("endIndex", Math.min((page + 1) * 12, allRepositories.size()));
        response.put("stats", stats);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/repositories/stats
     * Obtiene estadísticas de repositorios del usuario
     * 
     * @param principal Usuario autenticado
     * @return JSON con estadísticas de repositorios
     * 
     * Response:
     * {
     *   "personalCount": 5,
     *   "collaborativeCount": 3,
     *   "totalCount": 8
     * }
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getRepositoryStats(Principal principal) {
        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
        Map<String, Object> stats = repositoryService.obtenerEstadisticasRepositoriosUsuario(currentUser.getUsuarioId());
        return ResponseEntity.ok(stats);
    }
}
