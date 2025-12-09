package org.project.project.controller;

import org.project.project.model.entity.Equipo;
import org.project.project.model.entity.Usuario;
import org.project.project.service.TeamService;
import org.project.project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/devportal")
public class TeamController {

    private static final Logger logger = LoggerFactory.getLogger(TeamController.class);

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    // =================== LISTAR EQUIPOS ===================

    /**
     * Ruta: /devportal/{rol}/{username}/teams
     * Muestra todos los equipos del usuario (proyecto + repositorio)
     */
    @GetMapping("/{rol}/{username}/teams")
    public String listarEquipos(
            @PathVariable String rol,
            @PathVariable String username,
            Principal principal,
            Model model) {
        
        logger.info("📋 [TeamController] Accediendo a lista de equipos - rol: {}, username: {}", rol, username);

        try {
            Usuario usuario = userService.obtenerUsuarioActual(principal, username);

            // Obtener equipos de proyectos GRUPALES
            List<Map<String, Object>> equiposProyectosGrupales = teamService.obtenerEquiposProyectosGrupalesUsuario(usuario.getUsuarioId());
            
            // Obtener equipos de proyectos EMPRESARIALES
            List<Map<String, Object>> equiposProyectosEmpresariales = teamService.obtenerEquiposProyectosEmpresarialesUsuario(usuario.getUsuarioId());
            
            // Obtener equipos de repositorios COLABORATIVOS
            List<Map<String, Object>> equiposRepositorios = teamService.obtenerEquiposRepositoriosUsuario(usuario.getUsuarioId());

            model.addAttribute("rol", rol);
            model.addAttribute("username", username);
            model.addAttribute("usuario", usuario);
            model.addAttribute("currentUser", usuario); // Para navbar
            model.addAttribute("currentNavSection", "teams"); // Para navbar
            model.addAttribute("equiposProyectosGrupales", equiposProyectosGrupales);
            model.addAttribute("equiposProyectosEmpresariales", equiposProyectosEmpresariales);
            model.addAttribute("equiposRepositorios", equiposRepositorios);
            model.addAttribute("totalEquipos", equiposProyectosGrupales.size() + equiposProyectosEmpresariales.size() + equiposRepositorios.size());

            logger.info("✅ [TeamController] Equipos cargados - Proyectos Grupales: {}, Proyectos Empresariales: {}, Repositorios: {}", 
                        equiposProyectosGrupales.size(), equiposProyectosEmpresariales.size(), equiposRepositorios.size());

            return "teams/teams-list";

        } catch (Exception e) {
            logger.error("❌ [TeamController] Error al cargar equipos", e);
            model.addAttribute("error", "Error al cargar los equipos");
            return "error/404-error";
        }
    }

    // =================== CREAR EQUIPO EN PROYECTO ===================

    /**
     * Ruta: /devportal/{rol}/{username}/teams/created-at-P
     * Muestra formulario para crear equipo en un proyecto (GRUPO o EMPRESA)
     */
    @GetMapping("/{rol}/{username}/teams/created-at-P")
    public String mostrarCrearEquipoProyecto(
            @PathVariable String rol,
            @PathVariable String username,
            Principal principal,
            Model model) {
        
        logger.info("➕ [TeamController] Mostrando formulario crear equipo en proyecto - rol: {}, username: {}", rol, username);

        try {
            Usuario usuario = userService.obtenerUsuarioActual(principal, username);

            // Obtener proyectos grupales
            List<Map<String, Object>> proyectosGrupales = teamService.obtenerProyectosGrupalesUsuario(usuario.getUsuarioId());
            
            // Obtener proyectos empresariales
            List<Map<String, Object>> proyectosEmpresariales = teamService.obtenerProyectosEmpresarialesUsuario(usuario.getUsuarioId());

            model.addAttribute("rol", rol);
            model.addAttribute("username", username);
            model.addAttribute("usuario", usuario);
            model.addAttribute("proyectosGrupales", proyectosGrupales);
            model.addAttribute("proyectosEmpresariales", proyectosEmpresariales);

            logger.info("✅ [TeamController] Proyectos cargados - Grupales: {}, Empresariales: {}", 
                        proyectosGrupales.size(), proyectosEmpresariales.size());

            return "teams/teams-create-project";

        } catch (Exception e) {
            logger.error("❌ [TeamController] Error al mostrar formulario crear equipo en proyecto", e);
            model.addAttribute("error", "Error al cargar el formulario");
            return "error/404-error";
        }
    }

    /**
     * POST: /devportal/{rol}/{username}/teams/created-at-P
     * Guarda el nuevo equipo en uno o más proyectos
     */
    @PostMapping("/{rol}/{username}/teams/created-at-P")
    public String crearEquipoEnProyecto(
            @PathVariable String rol,
            @PathVariable String username,
            @RequestParam String nombreEquipo,
            @RequestParam(name = "proyectosIds", required = false) Long[] proyectosIds,
            @RequestParam Map<String, String> allParams,
            Principal principal,
            Model model) {
        
        logger.info("💾 [TeamController] Guardando equipo '{}' en {} proyectos", nombreEquipo, 
                   (proyectosIds != null ? proyectosIds.length : 0));

        try {
            // Validar que se haya seleccionado al menos un proyecto
            if (proyectosIds == null || proyectosIds.length == 0) {
                logger.warn("⚠️ [TeamController] No se seleccionó ningún proyecto");
                model.addAttribute("error", "Debes seleccionar al menos un proyecto");
                return "redirect:/devportal/" + rol + "/" + username + "/teams/created-at-P";
            }

            Usuario usuario = userService.obtenerUsuarioActual(principal, username);

            // Crear equipo con el primer proyecto (la relación con otros se crea después)
            String permisoPrimerProyecto = allParams.getOrDefault("permisos_" + proyectosIds[0], "EDITOR");
            Equipo equipoCreado = teamService.crearEquipoEnProyecto(nombreEquipo, proyectosIds[0], usuario.getUsuarioId());
            
            // Actualizar permisos del primer proyecto
            teamService.actualizarPermisosEquipoEnProyecto(equipoCreado.getEquipoId(), proyectosIds[0], permisoPrimerProyecto);
            logger.info("✅ [TeamController] Equipo '{}' creado exitosamente con ID: {} - Permisos: {}", 
                       nombreEquipo, equipoCreado.getEquipoId(), permisoPrimerProyecto);

            // Si hay más de un proyecto, crear relaciones adicionales con sus permisos respectivos
            if (proyectosIds.length > 1) {
                logger.info("📌 [TeamController] Creando relaciones con {} proyectos adicionales", proyectosIds.length - 1);
                for (int i = 1; i < proyectosIds.length; i++) {
                    String permiso = allParams.getOrDefault("permisos_" + proyectosIds[i], "EDITOR");
                    teamService.asociarEquipoAProyecto(equipoCreado.getEquipoId(), proyectosIds[i]);
                    teamService.actualizarPermisosEquipoEnProyecto(equipoCreado.getEquipoId(), proyectosIds[i], permiso);
                    logger.info("✅ [TeamController] Equipo {} asociado al proyecto {} con permisos: {}", 
                               equipoCreado.getEquipoId(), proyectosIds[i], permiso);
                }
            }

            // Redirigir al detalle del equipo recién creado
            return "redirect:/devportal/" + rol + "/" + username + "/teams/team-" + equipoCreado.getEquipoId();

        } catch (Exception e) {
            logger.error("❌ [TeamController] Error al crear equipo en proyecto", e);
            model.addAttribute("error", "Error al crear el equipo: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/teams/created-at-P";
        }
    }

    // =================== CREAR EQUIPO EN REPOSITORIO ===================

    /**
     * Ruta: /devportal/{rol}/{username}/teams/created-at-R
     * Muestra formulario para crear equipo en un repositorio (COLABORATIVO)
     */
    @GetMapping("/{rol}/{username}/teams/created-at-R")
    public String mostrarCrearEquipoRepositorio(
            @PathVariable String rol,
            @PathVariable String username,
            Principal principal,
            Model model) {
        
        logger.info("➕ [TeamController] Mostrando formulario crear equipo en repositorio - rol: {}, username: {}", rol, username);

        try {
            Usuario usuario = userService.obtenerUsuarioActual(principal, username);

            // Obtener repositorios colaborativos
            List<Map<String, Object>> repositoriosColaborativos = teamService.obtenerRepositoriosColaborativosUsuario(usuario.getUsuarioId());

            model.addAttribute("rol", rol);
            model.addAttribute("username", username);
            model.addAttribute("usuario", usuario);
            model.addAttribute("repositoriosColaborativos", repositoriosColaborativos);

            logger.info("✅ [TeamController] Repositorios cargados - Total: {}", repositoriosColaborativos.size());

            return "teams/teams-create-repository";

        } catch (Exception e) {
            logger.error("❌ [TeamController] Error al mostrar formulario crear equipo en repositorio", e);
            model.addAttribute("error", "Error al cargar el formulario");
            return "error/404-error";
        }
    }

    /**
     * POST: /devportal/{rol}/{username}/teams/created-at-R
     * Guarda el nuevo equipo en el repositorio
     */
    @PostMapping("/{rol}/{username}/teams/created-at-R")
    public String crearEquipoEnRepositorio(
            @PathVariable String rol,
            @PathVariable String username,
            @RequestParam String nombreEquipo,
            @RequestParam Long repositorioId,
            Principal principal,
            Model model) {
        
        logger.info("💾 [TeamController] Guardando equipo '{}' en repositorio: {}", nombreEquipo, repositorioId);

        try {
            Usuario usuario = userService.obtenerUsuarioActual(principal, username);

            // Crear equipo y obtener el ID retornado
            Equipo equipoCreado = teamService.crearEquipoEnRepositorio(nombreEquipo, repositorioId, usuario.getUsuarioId());

            logger.info("✅ [TeamController] Equipo '{}' creado exitosamente con ID: {}", nombreEquipo, equipoCreado.getEquipoId());

            // Redirigir al detalle del equipo recién creado
            return "redirect:/devportal/" + rol + "/" + username + "/teams/team-" + equipoCreado.getEquipoId();

        } catch (Exception e) {
            logger.error("❌ [TeamController] Error al crear equipo en repositorio", e);
            model.addAttribute("error", "Error al crear el equipo: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/teams/created-at-R";
        }
    }

    // =================== VER DETALLE EQUIPO ===================

    /**
     * Ruta: /devportal/{rol}/{username}/teams/team-{equipoId}
     * Muestra los detalles del equipo con todos los miembros
     */
    @GetMapping("/{rol}/{username}/teams/team-{equipoId}")
    public String verDetalleEquipo(
            @PathVariable String rol,
            @PathVariable String username,
            @PathVariable Long equipoId,
            Principal principal,
            Model model) {
        
        logger.info("🔍 [TeamController] Viendo detalle del equipo: {} - rol: {}, username: {}", equipoId, rol, username);

        try {
            Usuario usuario = userService.obtenerUsuarioActual(principal, username);

            Map<String, Object> detallesEquipo = teamService.obtenerDetallesEquipo(equipoId);

            model.addAttribute("rol", rol);
            model.addAttribute("username", username);
            model.addAttribute("usuario", usuario);
            model.addAttribute("detallesEquipo", detallesEquipo);
            model.addAttribute("equipo", detallesEquipo.get("equipo"));
            model.addAttribute("miembros", detallesEquipo.get("miembros"));
            model.addAttribute("cantidadMiembros", detallesEquipo.get("cantidadMiembros"));

            logger.info("✅ [TeamController] Detalle del equipo cargado - {} miembros", detallesEquipo.get("cantidadMiembros"));

            return "teams/team-detail";

        } catch (Exception e) {
            logger.error("❌ [TeamController] Error al obtener detalle del equipo", e);
            model.addAttribute("error", "Error al cargar el equipo");
            return "error/404-error";
        }
    }

    // =================== EDITAR EQUIPO ===================

    /**
     * Ruta: /devportal/{rol}/{username}/teams/team-{equipoId}/edit
     * Muestra formulario para editar el nombre del equipo
     */
    @GetMapping("/{rol}/{username}/teams/team-{equipoId}/edit")
    public String mostrarEditarEquipo(
            @PathVariable String rol,
            @PathVariable String username,
            @PathVariable Long equipoId,
            Principal principal,
            Model model) {
        
        logger.info("✏️ [TeamController] Mostrando formulario editar equipo: {} - rol: {}, username: {}", equipoId, rol, username);

        try {
            Usuario usuario = userService.obtenerUsuarioActual(principal, username);

            Map<String, Object> detallesEquipo = teamService.obtenerDetallesEquipo(equipoId);

            model.addAttribute("rol", rol);
            model.addAttribute("username", username);
            model.addAttribute("usuario", usuario);
            model.addAttribute("equipo", detallesEquipo.get("equipo"));
            model.addAttribute("nombreEquipo", detallesEquipo.get("nombreEquipo"));
            model.addAttribute("equipoId", equipoId);

            logger.info("✅ [TeamController] Formulario editar equipo cargado");

            return "teams/team-edit";

        } catch (Exception e) {
            logger.error("❌ [TeamController] Error al mostrar formulario editar equipo", e);
            model.addAttribute("error", "Error al cargar el formulario");
            return "error/404-error";
        }
    }

    /**
     * POST: /devportal/{rol}/{username}/teams/team-{equipoId}/edit
     * Guarda los cambios del equipo
     */
    @PostMapping("/{rol}/{username}/teams/team-{equipoId}/edit")
    public String guardarEditarEquipo(
            @PathVariable String rol,
            @PathVariable String username,
            @PathVariable Long equipoId,
            @RequestParam String nombreEquipo,
            Principal principal,
            Model model) {
        
        logger.info("💾 [TeamController] Guardando cambios del equipo: {} - nuevo nombre: {}", equipoId, nombreEquipo);

        try {
            teamService.actualizarEquipo(equipoId, nombreEquipo);

            logger.info("✅ [TeamController] Equipo {} actualizado exitosamente", equipoId);
            model.addAttribute("success", "Equipo actualizado exitosamente");

            return "redirect:/devportal/" + rol + "/" + username + "/teams/team-" + equipoId;

        } catch (Exception e) {
            logger.error("❌ [TeamController] Error al actualizar equipo", e);
            model.addAttribute("error", "Error al actualizar el equipo: " + e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/teams/team-" + equipoId + "/edit";
        }
    }
}
