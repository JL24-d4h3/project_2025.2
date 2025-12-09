package org.project.project.service;

import org.project.project.model.entity.RolProyecto;
import org.project.project.model.entity.Proyecto;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.AsignacionRolProyecto;
import org.project.project.model.dto.ProjectRoleDTO;
import org.project.project.repository.RolProyectoRepository;
import org.project.project.repository.ProyectoRepository;
import org.project.project.repository.UsuarioRepository;
import org.project.project.repository.AsignacionRolProyectoRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class ProjectRoleService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectRoleService.class);

    @Autowired
    private RolProyectoRepository rolProyectoRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AsignacionRolProyectoRepository asignacionRolProyectoRepository;

    // =================== CREAR ROLES ===================

    @Transactional
    public RolProyecto crearRol(Long proyectoId, String nombreRol, String descripcion, Long usuarioId) {
        logger.info("╔════════════════════════════════════════════════════════╗");
        logger.info("║         CREAR ROL PROYECTO                             ║");
        logger.info("╚════════════════════════════════════════════════════════╝");
        logger.info("📋 Proyecto ID: {}, Nombre Rol: {}, Creador ID: {}", proyectoId, nombreRol, usuarioId);

        try {
            // Validar proyecto existe
            Proyecto proyecto = proyectoRepository.findById(proyectoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado: " + proyectoId));
            logger.info("✅ Proyecto encontrado: {}", proyecto.getNombreProyecto());

            // Validar usuario existe
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));
            logger.info("✅ Usuario creador encontrado: {}", usuario.getUsername());

            // Validar que usuario sea creador del proyecto
            if (!proyecto.getCreatedBy().getUsuarioId().equals(usuarioId)) {
                logger.warn("❌ Usuario {} no es creador del proyecto {}", usuarioId, proyectoId);
                throw new IllegalArgumentException("Solo el creador puede crear roles");
            }
            logger.info("✅ Usuario es creador del proyecto");

            // Validar que nombre no sea duplicado
            if (rolProyectoRepository.existsByProyecto_ProyectoIdAndNombreRolProyecto(proyectoId, nombreRol)) {
                logger.warn("❌ Ya existe rol con nombre '{}' en proyecto {}", nombreRol, proyectoId);
                throw new IllegalArgumentException("Ya existe un rol con ese nombre en el proyecto");
            }
            logger.info("✅ Nombre de rol único");

            // Crear rol
            RolProyecto nuevoRol = new RolProyecto();
            nuevoRol.setProyecto(proyecto);
            nuevoRol.setNombreRolProyecto(nombreRol);
            nuevoRol.setDescripcionRolProyecto(descripcion);
            nuevoRol.setCreadoPor(usuario);
            nuevoRol.setCreadoEn(LocalDateTime.now());
            nuevoRol.setActualizadoEn(LocalDateTime.now());
            nuevoRol.setActivo(true);

            RolProyecto rolGuardado = rolProyectoRepository.save(nuevoRol);
            logger.info("✅ Rol creado exitosamente: {}", rolGuardado.getRolProyectoId());

            logger.info("╔════════════════════════════════════════════════════════╗");
            logger.info("║         ROL CREADO EXITOSAMENTE                        ║");
            logger.info("╚════════════════════════════════════════════════════════╝");
            return rolGuardado;

        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            logger.error("❌ Error validación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("❌ Error crítico al crear rol: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear rol: " + e.getMessage(), e);
        }
    }

    // =================== ACTUALIZAR ROLES ===================

    @Transactional
    public RolProyecto actualizarRol(Long rolId, String nombreRol, String descripcion, Long usuarioId) {
        logger.info("📝 Actualizando rol {} por usuario {}", rolId, usuarioId);

        try {
            RolProyecto rol = rolProyectoRepository.findById(rolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + rolId));

            // Validar que sea creador
            if (!rol.getCreadoPor().getUsuarioId().equals(usuarioId)) {
                logger.warn("❌ Usuario {} no puede editar rol {}", usuarioId, rolId);
                throw new IllegalArgumentException("Solo el creador puede actualizar este rol");
            }

            // Actualizar
            rol.setNombreRolProyecto(nombreRol);
            rol.setDescripcionRolProyecto(descripcion);
            rol.setActualizadoPor(usuarioRepository.findById(usuarioId).get());
            rol.setActualizadoEn(LocalDateTime.now());

            RolProyecto rolActualizado = rolProyectoRepository.save(rol);
            logger.info("✅ Rol actualizado: {}", rolActualizado.getRolProyectoId());
            return rolActualizado;

        } catch (Exception e) {
            logger.error("❌ Error al actualizar rol: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar rol: " + e.getMessage(), e);
        }
    }

    // =================== ELIMINAR ROLES ===================

    @Transactional
    public void eliminarRol(Long rolId, Long usuarioId) {
        logger.info("🗑️  Eliminando rol {} por usuario {}", rolId, usuarioId);

        try {
            RolProyecto rol = rolProyectoRepository.findById(rolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + rolId));

            // Validar que sea creador
            if (!rol.getCreadoPor().getUsuarioId().equals(usuarioId)) {
                logger.warn("❌ Usuario {} no puede eliminar rol {}", usuarioId, rolId);
                throw new IllegalArgumentException("Solo el creador puede eliminar este rol");
            }

            // Validar que no haya usuarios asignados
            long usuariosAsignados = asignacionRolProyectoRepository.countByRolProyectoId(rolId);
            if (usuariosAsignados > 0) {
                logger.warn("❌ No se puede eliminar rol {} porque tiene {} usuarios asignados", rolId, usuariosAsignados);
                throw new IllegalArgumentException("No se puede eliminar un rol que tiene usuarios asignados");
            }

            rolProyectoRepository.delete(rol);
            logger.info("✅ Rol eliminado: {}", rolId);

        } catch (Exception e) {
            logger.error("❌ Error al eliminar rol: {}", e.getMessage(), e);
            throw new RuntimeException("Error al eliminar rol: " + e.getMessage(), e);
        }
    }

    // =================== LISTAR ROLES ===================

    @Transactional(readOnly = true)
    public List<ProjectRoleDTO> listarRolesActivos(Long proyectoId) {
        logger.info("📋 Listando roles activos del proyecto {}", proyectoId);

        try {
            // Validar proyecto existe
            proyectoRepository.findById(proyectoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado: " + proyectoId));

            List<RolProyecto> roles = rolProyectoRepository.findActiveRolesByProyectoId(proyectoId);
            logger.info("✅ Se encontraron {} roles activos", roles.size());

            return roles.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("❌ Error al listar roles: {}", e.getMessage(), e);
            throw new RuntimeException("Error al listar roles: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<ProjectRoleDTO> listarTodosRoles(Long proyectoId) {
        logger.info("📋 Listando todos los roles del proyecto {}", proyectoId);

        try {
            proyectoRepository.findById(proyectoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado: " + proyectoId));

            List<RolProyecto> roles = rolProyectoRepository.findByProyecto_ProyectoId(proyectoId);
            logger.info("✅ Se encontraron {} roles", roles.size());

            return roles.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("❌ Error al listar roles: {}", e.getMessage(), e);
            throw new RuntimeException("Error al listar roles: " + e.getMessage(), e);
        }
    }

    // =================== OBTENER ROL INDIVIDUAL ===================

    @Transactional(readOnly = true)
    public RolProyecto obtenerRol(Long rolId) {
        logger.info("🔍 Buscando rol {}", rolId);
        return rolProyectoRepository.findById(rolId)
                .orElseThrow(() -> {
                    logger.warn("❌ Rol no encontrado: {}", rolId);
                    return new ResourceNotFoundException("Rol no encontrado: " + rolId);
                });
    }

    // =================== VALIDACIONES ===================

    public boolean usuarioTiene3RolesEnProyecto(Long usuarioId, Long proyectoId) {
        long count = asignacionRolProyectoRepository.findByUsuarioIdAndProyectoId(usuarioId, proyectoId)
                .size();
        logger.info("👤 Usuario {} tiene {} roles en proyecto {}", usuarioId, count, proyectoId);
        return count >= 3;
    }

    public boolean existeRolConNombre(Long proyectoId, String nombre) {
        boolean existe = rolProyectoRepository.existsByProyecto_ProyectoIdAndNombreRolProyecto(proyectoId, nombre);
        logger.info("🔍 Rol con nombre '{}' en proyecto {}: {}", nombre, proyectoId, existe);
        return existe;
    }

    public long contarRolesEnProyecto(Long proyectoId) {
        long count = rolProyectoRepository.countByProyecto_ProyectoId(proyectoId);
        logger.info("📊 Proyecto {} tiene {} roles", proyectoId, count);
        return count;
    }

    // =================== HELPERS ===================

    private ProjectRoleDTO convertToDTO(RolProyecto rol) {
        ProjectRoleDTO dto = new ProjectRoleDTO();
        dto.setRolProyectoId(rol.getRolProyectoId());
        dto.setNombreRolProyecto(rol.getNombreRolProyecto());
        dto.setDescripcionRolProyecto(rol.getDescripcionRolProyecto());
        dto.setCreadoPorNombre(rol.getCreadoPor() != null ? rol.getCreadoPor().getUsername() : "Unknown");
        dto.setCreadoPorId(rol.getCreadoPor() != null ? rol.getCreadoPor().getUsuarioId() : null);
        dto.setCreadoEn(rol.getCreadoEn());
        dto.setActualizadoEn(rol.getActualizadoEn());
        dto.setActivo(rol.getActivo());
        dto.setUsuariosAsignados((long) rol.getAsignaciones().size());
        return dto;
    }
}
