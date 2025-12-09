package org.project.project.controller;

import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.UsuarioHasProyecto;
import org.project.project.repository.UsuarioHasProyectoRepository;
import org.project.project.service.UserService;
import org.project.project.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects/invitations")
public class ProjectInvitationController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectInvitationController.class);

    @Autowired
    private UsuarioHasProyectoRepository usuarioHasProyectoRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    /**
     * Verificar si usuario fue invitado a proyecto (GET /api/projects/{proyectoId}/invitations/status)
     * Retorna el estado de la invitación
     */
    @GetMapping("/proyecto/{proyectoId}/status")
    public ResponseEntity<Map<String, Object>> checkInvitationStatus(@PathVariable Long proyectoId,
                                                                      Principal principal) {
        logger.info("🔍 Verificando estado de invitación para proyecto {}", proyectoId);

        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "mensaje", "Debe estar autenticado"
                ));
            }

            Usuario usuario = userService.buscarPorUsername(principal.getName());
            Optional<UsuarioHasProyecto> membresia = usuarioHasProyectoRepository
                    .findById_UserIdAndId_ProjectId(usuario.getUsuarioId(), proyectoId);

            if (membresia.isEmpty()) {
                logger.warn("❌ Usuario {} no es miembro del proyecto {}", usuario.getCorreo(), proyectoId);
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "mensaje", "No hay registro de invitación para este proyecto"
                ));
            }

            UsuarioHasProyecto relacion = membresia.get();
            logger.info("✅ Usuario es miembro desde: {}", relacion.getFechaUsuarioProyecto());

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("usuarioId", usuario.getUsuarioId());
            resultado.put("correo", usuario.getCorreo());
            resultado.put("proyectoId", proyectoId);
            resultado.put("privilegio", relacion.getPrivilegio());
            resultado.put("fechaUnion", relacion.getFechaUsuarioProyecto());

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.error("❌ Error verificando invitación: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error al verificar invitación"
            ));
        }
    }

    /**
     * Listar invitaciones pendientes del usuario (GET /api/projects/invitations/pending)
     * Retorna proyectos donde el usuario fue recientemente agregado
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingInvitations(Principal principal) {
        logger.info("📋 Listando invitaciones pendientes del usuario");

        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "mensaje", "Debe estar autenticado"
                ));
            }

            Usuario usuario = userService.buscarPorUsername(principal.getName());
            
            // Obtener todos los proyectos del usuario
            List<UsuarioHasProyecto> proyectosUsuario = usuarioHasProyectoRepository
                    .findById_UserId(usuario.getUsuarioId());

            logger.info("✅ Se encontraron {} proyectos para el usuario", proyectosUsuario.size());

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("total", proyectosUsuario.size());
            resultado.put("invitaciones", proyectosUsuario.stream().map(relacion -> Map.of(
                    "usuarioId", relacion.getId().getUsuarioId(),
                    "proyectoId", relacion.getId().getProyectoId(),
                    "nombreProyecto", relacion.getProyecto().getNombreProyecto(),
                    "creadorId", relacion.getProyecto().getCreatedBy() != null ? 
                            relacion.getProyecto().getCreatedBy().getUsuarioId() : null,
                    "correoCreador", relacion.getProyecto().getCreatedBy() != null ? 
                            relacion.getProyecto().getCreatedBy().getCorreo() : "N/A",
                    "privilegio", relacion.getPrivilegio(),
                    "fechaInvitacion", relacion.getFechaUsuarioProyecto()
            )).toList());

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.error("❌ Error listando invitaciones: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error al listar invitaciones"
            ));
        }
    }

    /**
     * Aceptar invitación a proyecto (POST /api/projects/invitations/accept/{proyectoId})
     * Confirma que el usuario acepta la invitación (solo para auditoría)
     */
    @PostMapping("/accept/{proyectoId}")
    public ResponseEntity<Map<String, Object>> acceptInvitation(@PathVariable Long proyectoId,
                                                                 Principal principal) {
        logger.info("✅ Usuario aceptando invitación al proyecto {}", proyectoId);

        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "mensaje", "Debe estar autenticado"
                ));
            }

            Usuario usuario = userService.buscarPorUsername(principal.getName());
            Optional<UsuarioHasProyecto> relacion = usuarioHasProyectoRepository
                    .findById_UserIdAndId_ProjectId(usuario.getUsuarioId(), proyectoId);

            if (relacion.isEmpty()) {
                logger.warn("❌ Usuario no es miembro del proyecto");
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "mensaje", "No existe invitación para este proyecto"
                ));
            }

            // Actualizar timestamp de aceptación
            UsuarioHasProyecto membresia = relacion.get();
            membresia.setFechaUsuarioProyecto(LocalDateTime.now());
            usuarioHasProyectoRepository.save(membresia);

            logger.info("✅ Invitación aceptada");

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("mensaje", "Invitación aceptada exitosamente");
            resultado.put("proyectoId", proyectoId);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.error("❌ Error aceptando invitación: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error al aceptar invitación"
            ));
        }
    }

    /**
     * Rechazar/Remover invitación (DELETE /api/projects/invitations/{proyectoId})
     * Remueve al usuario del proyecto
     */
    @DeleteMapping("/proyecto/{proyectoId}")
    public ResponseEntity<Map<String, Object>> rejectInvitation(@PathVariable Long proyectoId,
                                                                 Principal principal) {
        logger.info("❌ Usuario rechazando/removiendo del proyecto {}", proyectoId);

        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "mensaje", "Debe estar autenticado"
                ));
            }

            Usuario usuario = userService.buscarPorUsername(principal.getName());
            Optional<UsuarioHasProyecto> relacion = usuarioHasProyectoRepository
                    .findById_UserIdAndId_ProjectId(usuario.getUsuarioId(), proyectoId);

            if (relacion.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "mensaje", "No se encontró la relación usuario-proyecto"
                ));
            }

            // Eliminar relación
            usuarioHasProyectoRepository.delete(relacion.get());
            logger.info("✅ Usuario removido del proyecto");

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("mensaje", "Has sido removido del proyecto");
            resultado.put("proyectoId", proyectoId);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            logger.error("❌ Error rechazando invitación: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "mensaje", "Error al procesar la solicitud"
            ));
        }
    }
}
