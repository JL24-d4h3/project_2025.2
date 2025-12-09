package org.project.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.project.project.model.entity.*;
import org.project.project.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ProyectoInvitacionService {

    private static final Logger logger = LoggerFactory.getLogger(ProyectoInvitacionService.class);

    @Autowired
    private ProyectoInvitacionRepository invitacionRepository;

    @Autowired
    private UsuarioHasProyectoRepository usuarioHasProyectoRepository;

    @Autowired
    private AsignacionRolProyectoRepository asignacionRolProyectoRepository;

    @Autowired
    private RolProyectoRepository rolProyectoRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioHasEquipoRepository usuarioHasEquipoRepository;
    
    @Autowired
    private EquipoHasProyectoRepository equipoHasProyectoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Crear invitación pendiente (NO agrega al usuario aún)
     * IMPORTANTE: Los equipos temporales (IDs negativos) se crean aquí
     */
    @Transactional
    public ProyectoInvitacion crearInvitacion(
            Proyecto proyecto,
            Usuario usuarioInvitado,
            Usuario invitadoPor,
            List<Long> rolIds,
            String permiso,
            List<Long> equipoIds,
            Map<Long, String> teamNamesMap,
            String tipoInvitacion,
            String token) {

        logger.info("📨 Creando invitación pendiente:");
        logger.info("   - Proyecto: {}", proyecto.getNombreProyecto());
        logger.info("   - Usuario: {}", usuarioInvitado.getCorreo());
        logger.info("   - Tipo: {}", tipoInvitacion);
        logger.info("   - Equipos recibidos: {}", equipoIds);
        logger.info("   - Nombres de equipos: {}", teamNamesMap);

        try {
            // ✅ FIX: Procesar equipos temporales (IDs negativos) y crear los equipos reales
            List<Long> equiposReales = new java.util.ArrayList<>();
            if (equipoIds != null) {
                for (Long equipoId : equipoIds) {
                    if (equipoId < 0) {
                        // Es un equipo temporal - crear ahora
                        logger.info("🆕 Creando equipo temporal con ID negativo: {}", equipoId);
                        
                        // ✅ FIX: Usar el nombre real del equipo del mapa
                        String nombreEquipo = (teamNamesMap != null && teamNamesMap.containsKey(equipoId)) 
                            ? teamNamesMap.get(equipoId)
                            : "Equipo de " + proyecto.getNombreProyecto() + " - " + usuarioInvitado.getNombreUsuario();
                        
                        logger.info("📝 Nombre del equipo a crear: {}", nombreEquipo);
                        
                        // ✅ VALIDACIÓN: Verificar si el nombre del equipo ya existe en el proyecto
                        boolean nombreExiste = equipoHasProyectoRepository.existsByProjectIdAndTeamName(
                            proyecto.getProyectoId(), nombreEquipo);
                        
                        if (nombreExiste) {
                            String errorMsg = "El equipo '" + nombreEquipo + "' ya existe en este proyecto. Por favor use un nombre diferente.";
                            logger.error("❌ {}", errorMsg);
                            throw new IllegalArgumentException(errorMsg);
                        }
                        
                        Equipo nuevoEquipo = new Equipo();
                        nuevoEquipo.setNombreEquipo(nombreEquipo);
                        nuevoEquipo.setCreadoPor(invitadoPor);
                        nuevoEquipo.setFechaCreacion(LocalDateTime.now());
                        
                        Equipo equipoGuardado = equipoRepository.save(nuevoEquipo);
                        logger.info("✅ Equipo creado con ID real: {}", equipoGuardado.getEquipoId());
                        
                        // Asociar equipo al proyecto
                        EquipoHasProyectoId ehpId = new EquipoHasProyectoId(equipoGuardado.getEquipoId(), proyecto.getProyectoId());
                        EquipoHasProyecto ehp = new EquipoHasProyecto();
                        ehp.setId(ehpId);
                        ehp.setEquipo(equipoGuardado);
                        ehp.setProyecto(proyecto);
                        equipoHasProyectoRepository.save(ehp);
                        logger.info("✅ Equipo asociado al proyecto");
                        
                        equiposReales.add(equipoGuardado.getEquipoId());
                    } else {
                        // Equipo existente
                        equiposReales.add(equipoId);
                    }
                }
            }
            
            logger.info("📋 Equipos finales (reales): {}", equiposReales);

            ProyectoInvitacion invitacion = new ProyectoInvitacion();
            invitacion.setProyecto(proyecto);
            invitacion.setUsuarioInvitado(usuarioInvitado);
            invitacion.setInvitadoPor(invitadoPor);
            invitacion.setPermiso(permiso);
            invitacion.setTipoInvitacion(tipoInvitacion);
            invitacion.setEstado(ProyectoInvitacion.EstadoInvitacion.PENDIENTE);
            invitacion.setToken(token);
            invitacion.setFechaInvitacion(LocalDateTime.now());
            invitacion.setFechaExpiracion(LocalDateTime.now().plusDays(7));

            // Guardar roles y equipos REALES como JSON
            invitacion.setRolesJson(objectMapper.writeValueAsString(rolIds));
            invitacion.setEquiposJson(objectMapper.writeValueAsString(equiposReales));

            ProyectoInvitacion saved = invitacionRepository.save(invitacion);
            logger.info("✅ Invitación creada con ID: {}", saved.getInvitacionId());

            return saved;

        } catch (Exception e) {
            logger.error("❌ Error al crear invitación", e);
            throw new RuntimeException("Error al crear invitación: " + e.getMessage(), e);
        }
    }

    /**
     * Aceptar invitación y agregar usuario al proyecto
     */
    @Transactional
    public ProyectoInvitacion aceptarInvitacion(String token) {
        logger.info("✅ Aceptando invitación con token: {}", token);

        ProyectoInvitacion invitacion = invitacionRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invitación no encontrada o token inválido"));

        // Validar estado
        if (invitacion.getEstado() != ProyectoInvitacion.EstadoInvitacion.PENDIENTE) {
            throw new IllegalArgumentException("Esta invitación ya fue procesada (estado: " + invitacion.getEstado() + ")");
        }

        // Validar expiración
        if (invitacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            invitacion.setEstado(ProyectoInvitacion.EstadoInvitacion.EXPIRADA);
            invitacionRepository.save(invitacion);
            throw new IllegalArgumentException("Esta invitación ha expirado");
        }

        try {
            // Parsear datos JSON
            List<Long> rolIds = Arrays.asList(objectMapper.readValue(invitacion.getRolesJson(), Long[].class));
            List<Long> equipoIds = Arrays.asList(objectMapper.readValue(invitacion.getEquiposJson(), Long[].class));

            // Agregar usuario al proyecto (AHORA SÍ)
            agregarUsuarioAlProyecto(
                    invitacion.getProyecto(),
                    invitacion.getUsuarioInvitado(),
                    invitacion.getPermiso(),
                    rolIds,
                    equipoIds
            );

            // Actualizar estado de la invitación
            invitacion.setEstado(ProyectoInvitacion.EstadoInvitacion.ACEPTADA);
            invitacion.setFechaRespuesta(LocalDateTime.now());
            invitacionRepository.save(invitacion);

            logger.info("✅ Invitación aceptada y usuario agregado al proyecto");

            return invitacion;

        } catch (Exception e) {
            logger.error("❌ Error al aceptar invitación", e);
            throw new RuntimeException("Error al procesar la invitación: " + e.getMessage(), e);
        }
    }

    /**
     * Rechazar invitación (NO agrega al usuario)
     */
    @Transactional
    public ProyectoInvitacion rechazarInvitacion(String token) {
        logger.info("❌ Rechazando invitación con token: {}", token);

        ProyectoInvitacion invitacion = invitacionRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invitación no encontrada o token inválido"));

        // Validar estado
        if (invitacion.getEstado() != ProyectoInvitacion.EstadoInvitacion.PENDIENTE) {
            throw new IllegalArgumentException("Esta invitación ya fue procesada (estado: " + invitacion.getEstado() + ")");
        }

        // Actualizar estado
        invitacion.setEstado(ProyectoInvitacion.EstadoInvitacion.RECHAZADA);
        invitacion.setFechaRespuesta(LocalDateTime.now());
        invitacionRepository.save(invitacion);

        logger.info("✅ Invitación rechazada");

        return invitacion;
    }

    /**
     * Agregar usuario al proyecto (lógica original de _invitarUsuario)
     */
    private void agregarUsuarioAlProyecto(
            Proyecto proyecto,
            Usuario usuario,
            String permiso,
            List<Long> rolIds,
            List<Long> equipoIds) {

        logger.info("➕ Agregando usuario al proyecto:");
        logger.info("   - Usuario: {}", usuario.getCorreo());
        logger.info("   - Proyecto: {}", proyecto.getNombreProyecto());
        logger.info("   - Permiso: {}", permiso);

        // Validar permiso
        UsuarioHasProyecto.PrivilegioUsuarioProyecto privilegio;
        try {
            privilegio = UsuarioHasProyecto.PrivilegioUsuarioProyecto.valueOf(permiso);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Permiso inválido: {}, usando LECTOR por defecto", permiso);
            privilegio = UsuarioHasProyecto.PrivilegioUsuarioProyecto.LECTOR;
        }

        // 1. Crear usuario_has_proyecto
        UsuarioHasProyectoId uhpId = new UsuarioHasProyectoId(usuario.getUsuarioId(), proyecto.getProyectoId());
        UsuarioHasProyecto usuarioHasProyecto = new UsuarioHasProyecto();
        usuarioHasProyecto.setId(uhpId);
        usuarioHasProyecto.setUsuario(usuario);
        usuarioHasProyecto.setProyecto(proyecto);
        usuarioHasProyecto.setPrivilegio(privilegio);
        usuarioHasProyecto.setFechaUsuarioProyecto(LocalDateTime.now());
        usuarioHasProyectoRepository.save(usuarioHasProyecto);
        logger.info("✅ usuario_has_proyecto creado con privilegio: {}", privilegio);

        // 2. Agregar usuario a los equipos
        if (equipoIds != null && !equipoIds.isEmpty()) {
            for (Long equipoId : equipoIds) {
                try {
                    Equipo equipo = equipoRepository.findById(equipoId).orElse(null);
                    if (equipo != null) {
                        UsuarioHasEquipoId uheId = new UsuarioHasEquipoId(usuario.getUsuarioId(), equipo.getEquipoId());
                        UsuarioHasEquipo usuarioHasEquipo = new UsuarioHasEquipo();
                        usuarioHasEquipo.setId(uheId);
                        usuarioHasEquipo.setUsuario(usuario);
                        usuarioHasEquipo.setEquipo(equipo);
                        usuarioHasEquipoRepository.save(usuarioHasEquipo);
                        logger.info("✅ Usuario asignado al equipo {}", equipoId);
                    }
                } catch (Exception e) {
                    logger.warn("⚠️ Error asignando usuario a equipo {}: {}", equipoId, e.getMessage());
                }
            }
        }

        // 3. Asignar roles
        for (Long rolId : rolIds) {
            try {
                RolProyecto rol = rolProyectoRepository.findById(rolId).orElse(null);
                if (rol != null) {
                    AsignacionRolProyectoId arpId = new AsignacionRolProyectoId(
                            rol.getRolProyectoId(),
                            usuario.getUsuarioId(),
                            proyecto.getProyectoId()
                    );
                    AsignacionRolProyecto asignacion = new AsignacionRolProyecto();
                    asignacion.setId(arpId);
                    asignacion.setRolProyecto(rol);
                    asignacion.setUsuarioHasProyecto(usuarioHasProyecto);
                    asignacionRolProyectoRepository.save(asignacion);
                    logger.info("✅ Rol {} asignado", rolId);
                }
            } catch (Exception e) {
                logger.warn("⚠️ Error asignando rol {}: {}", rolId, e.getMessage());
            }
        }

        logger.info("✅ Usuario agregado al proyecto exitosamente");
    }
}
