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

@Service
public class RepositoryInvitationService {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryInvitationService.class);

    @Autowired
    private RepositorioInvitacionRepository invitacionRepository;

    @Autowired
    private UsuarioHasRepositorioRepository usuarioHasRepositorioRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioHasEquipoRepository usuarioHasEquipoRepository;
    
    @Autowired
    private EquipoHasRepositorioRepository equipoHasRepositorioRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Crear invitación pendiente para REPOSITORIO (NO agrega al usuario aún)
     * IMPORTANTE: Los equipos temporales (IDs negativos) se crean aquí
     */
    @Transactional
    public RepositorioInvitacion crearInvitacionRepositorio(
            Repositorio repositorio,
            Usuario usuarioInvitado,
            Usuario invitadoPor,
            String permiso,
            List<Long> equipoIds,
            String token) {

        logger.info("📨 Creando invitación pendiente para REPOSITORIO:");
        logger.info("   - Repositorio: {}", repositorio.getNombreRepositorio());
        logger.info("   - Usuario: {}", usuarioInvitado.getCorreo());
        logger.info("   - Permiso: {}", permiso);
        logger.info("   - Equipos recibidos: {}", equipoIds);

        try {
            // ✅ Procesar equipos temporales (IDs negativos) y crear los equipos reales
            List<Long> equiposReales = new java.util.ArrayList<>();
            if (equipoIds != null) {
                for (Long equipoId : equipoIds) {
                    // Solo agregar IDs válidos (positivos) - los equipos temporales ya fueron creados en el controlador
                    if (equipoId > 0) {
                        equiposReales.add(equipoId);
                    }
                }
            }
            
            logger.info("📋 Equipos finales (reales): {}", equiposReales);

            RepositorioInvitacion invitacion = new RepositorioInvitacion();
            invitacion.setRepositorio(repositorio);
            invitacion.setUsuarioInvitado(usuarioInvitado);
            invitacion.setInvitadoPor(invitadoPor);
            invitacion.setPermiso(permiso);
            invitacion.setEstado(RepositorioInvitacion.EstadoInvitacion.PENDIENTE);
            invitacion.setToken(token);
            invitacion.setFechaInvitacion(LocalDateTime.now());
            invitacion.setFechaExpiracion(LocalDateTime.now().plusDays(7));

            // Guardar equipos REALES como JSON
            invitacion.setEquiposJson(objectMapper.writeValueAsString(equiposReales));

            RepositorioInvitacion saved = invitacionRepository.save(invitacion);
            logger.info("✅ Invitación de repositorio creada con ID: {}", saved.getInvitacionId());

            return saved;

        } catch (Exception e) {
            logger.error("❌ Error al crear invitación de repositorio", e);
            throw new RuntimeException("Error al crear invitación de repositorio: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener invitación por token (sin aceptar)
     */
    public RepositorioInvitacion obtenerInvitacionPorToken(String token) {
        logger.info("🔍 Buscando invitación con token: {}", token);
        
        RepositorioInvitacion invitacion = invitacionRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invitación no encontrada o token inválido"));
        
        logger.info("✅ Invitación encontrada - Repositorio: {}, Usuario: {}, Estado: {}", 
                   invitacion.getRepositorio().getNombreRepositorio(),
                   invitacion.getUsuarioInvitado().getCorreo(),
                   invitacion.getEstado());
        
        return invitacion;
    }

    /**
     * Aceptar invitación y agregar usuario al repositorio
     */
    @Transactional
    public RepositorioInvitacion aceptarInvitacion(String token) {
        logger.info("╔════════════════════════════════════════════════════════╗");
        logger.info("║     ACEPTANDO INVITACIÓN A REPOSITORIO (SERVICIO)      ║");
        logger.info("╚════════════════════════════════════════════════════════╝");
        logger.info("🔑 Token: {}", token);

        logger.info("🔍 Paso 1: Buscando invitación en base de datos...");
        RepositorioInvitacion invitacion = invitacionRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invitación no encontrada o token inválido"));
        
        logger.info("✅ Invitación encontrada:");
        logger.info("   - ID Invitación: {}", invitacion.getInvitacionId());
        logger.info("   - Repositorio: {} (ID: {})", 
                   invitacion.getRepositorio().getNombreRepositorio(),
                   invitacion.getRepositorio().getRepositorioId());
        logger.info("   - Usuario invitado: {} (ID: {})", 
                   invitacion.getUsuarioInvitado().getCorreo(),
                   invitacion.getUsuarioInvitado().getUsuarioId());
        logger.info("   - Permiso: {}", invitacion.getPermiso());
        logger.info("   - Estado actual: {}", invitacion.getEstado());
        logger.info("   - Equipos JSON: {}", invitacion.getEquiposJson());

        // Validar estado
        logger.info("🔍 Paso 2: Validando estado de invitación...");
        if (invitacion.getEstado() != RepositorioInvitacion.EstadoInvitacion.PENDIENTE) {
            logger.error("❌ Estado inválido: {}", invitacion.getEstado());
            throw new IllegalArgumentException("Esta invitación ya fue procesada (estado: " + invitacion.getEstado() + ")");
        }
        logger.info("✅ Estado válido: PENDIENTE");

        // Validar expiración
        logger.info("🔍 Paso 3: Validando fecha de expiración...");
        logger.info("   - Fecha actual: {}", LocalDateTime.now());
        logger.info("   - Fecha expiración: {}", invitacion.getFechaExpiracion());
        if (invitacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            logger.error("❌ Invitación expirada");
            invitacion.setEstado(RepositorioInvitacion.EstadoInvitacion.EXPIRADA);
            invitacionRepository.save(invitacion);
            throw new IllegalArgumentException("Esta invitación ha expirado");
        }
        logger.info("✅ Invitación no ha expirado");

        try {
            // Parsear datos JSON
            logger.info("🔍 Paso 4: Parseando equipos desde JSON...");
            List<Long> equipoIds = Arrays.asList(objectMapper.readValue(invitacion.getEquiposJson(), Long[].class));
            logger.info("✅ Equipos parseados: {}", equipoIds);

            // Agregar usuario al repositorio (AHORA SÍ)
            logger.info("🔍 Paso 5: Agregando usuario al repositorio...");
            agregarUsuarioAlRepositorio(
                    invitacion.getRepositorio(),
                    invitacion.getUsuarioInvitado(),
                    invitacion.getPermiso(),
                    equipoIds
            );
            logger.info("✅ Usuario agregado exitosamente al repositorio");

            // Actualizar estado de la invitación
            logger.info("🔍 Paso 6: Actualizando estado de invitación a ACEPTADA...");
            invitacion.setEstado(RepositorioInvitacion.EstadoInvitacion.ACEPTADA);
            invitacion.setFechaRespuesta(LocalDateTime.now());
            invitacionRepository.save(invitacion);
            logger.info("✅ Estado actualizado a ACEPTADA");

            logger.info("╔════════════════════════════════════════════════════════╗");
            logger.info("║     ✅ INVITACIÓN ACEPTADA EXITOSAMENTE ✅             ║");
            logger.info("╚════════════════════════════════════════════════════════╝");

            return invitacion;

        } catch (Exception e) {
            logger.error("❌❌❌ ERROR CRÍTICO al aceptar invitación ❌❌❌");
            logger.error("❌ Mensaje: {}", e.getMessage());
            logger.error("❌ Tipo: {}", e.getClass().getName());
            logger.error("❌ Stack trace:", e);
            throw new RuntimeException("Error al procesar la invitación: " + e.getMessage(), e);
        }
    }

    /**
     * Rechazar invitación (NO agrega al usuario)
     */
    @Transactional
    public RepositorioInvitacion rechazarInvitacion(String token) {
        logger.info("❌ Rechazando invitación con token: {}", token);

        RepositorioInvitacion invitacion = invitacionRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invitación no encontrada o token inválido"));

        if (invitacion.getEstado() != RepositorioInvitacion.EstadoInvitacion.PENDIENTE) {
            throw new IllegalArgumentException("Esta invitación ya fue procesada");
        }

        invitacion.setEstado(RepositorioInvitacion.EstadoInvitacion.RECHAZADA);
        invitacion.setFechaRespuesta(LocalDateTime.now());
        invitacionRepository.save(invitacion);

        logger.info("✅ Invitación rechazada");

        return invitacion;
    }

    /**
     * Agregar usuario al repositorio (ejecutado al aceptar invitación)
     */
    private void agregarUsuarioAlRepositorio(
            Repositorio repositorio,
            Usuario usuario,
            String permiso,
            List<Long> equipoIds) {

        logger.info("╔════════════════════════════════════════════════════════╗");
        logger.info("║     AGREGANDO USUARIO AL REPOSITORIO                   ║");
        logger.info("╚════════════════════════════════════════════════════════╝");
        logger.info("👤 Usuario: {} (ID: {})", usuario.getCorreo(), usuario.getUsuarioId());
        logger.info("📦 Repositorio: {} (ID: {})", repositorio.getNombreRepositorio(), repositorio.getRepositorioId());
        logger.info("🔐 Permiso: {}", permiso);
        logger.info("👥 Equipos: {}", equipoIds);

        // Convertir permiso a ENUM
        logger.info("🔍 Paso 1: Convirtiendo permiso '{}' a ENUM...", permiso);
        UsuarioHasRepositorio.PrivilegioUsuarioRepositorio privilegio;
        try {
            privilegio = UsuarioHasRepositorio.PrivilegioUsuarioRepositorio.valueOf(permiso);
            logger.info("✅ Permiso convertido a: {}", privilegio);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Permiso inválido: {}", permiso);
            throw new IllegalArgumentException("Permiso inválido: " + permiso);
        }

        // 1. Agregar usuario al repositorio
        logger.info("🔍 Paso 2: Creando relación usuario_has_repositorio...");
        UsuarioHasRepositorioId uhrId = new UsuarioHasRepositorioId(usuario.getUsuarioId(), repositorio.getRepositorioId());
        logger.info("   - ID compuesto: usuario={}, repositorio={}", uhrId.getUsuarioId(), uhrId.getRepositorioId());
        
        UsuarioHasRepositorio usuarioHasRepositorio = new UsuarioHasRepositorio();
        usuarioHasRepositorio.setId(uhrId);
        usuarioHasRepositorio.setUsuario(usuario);
        usuarioHasRepositorio.setRepositorio(repositorio);
        usuarioHasRepositorio.setPrivilegio(privilegio);
        usuarioHasRepositorio.setFechaUsuarioRepositorio(LocalDateTime.now());
        
        logger.info("   - Guardando en base de datos...");
        usuarioHasRepositorioRepository.save(usuarioHasRepositorio);
        logger.info("✅ usuario_has_repositorio creado con privilegio: {}", privilegio);

        // 2. Agregar usuario a los equipos
        logger.info("🔍 Paso 3: Agregando usuario a equipos...");
        if (equipoIds != null && !equipoIds.isEmpty()) {
            logger.info("   - Total de equipos a asignar: {}", equipoIds.size());
            int equiposAsignados = 0;
            
            for (Long equipoId : equipoIds) {
                try {
                    logger.info("   - Procesando equipo ID: {}", equipoId);
                    Equipo equipo = equipoRepository.findById(equipoId).orElse(null);
                    
                    if (equipo != null) {
                        logger.info("     ✓ Equipo encontrado: {}", equipo.getNombreEquipo());
                        
                        UsuarioHasEquipoId uheId = new UsuarioHasEquipoId(usuario.getUsuarioId(), equipo.getEquipoId());
                        UsuarioHasEquipo usuarioHasEquipo = new UsuarioHasEquipo();
                        usuarioHasEquipo.setId(uheId);
                        usuarioHasEquipo.setUsuario(usuario);
                        usuarioHasEquipo.setEquipo(equipo);
                        usuarioHasEquipoRepository.save(usuarioHasEquipo);
                        
                        equiposAsignados++;
                        logger.info("     ✅ Usuario asignado al equipo {} (ID: {})", equipo.getNombreEquipo(), equipoId);
                    } else {
                        logger.warn("     ⚠️ Equipo con ID {} no encontrado", equipoId);
                    }
                } catch (Exception e) {
                    logger.warn("     ⚠️ Error asignando usuario a equipo {}: {}", equipoId, e.getMessage());
                    logger.debug("Stack trace:", e);
                }
            }
            
            logger.info("✅ Usuario asignado a {}/{} equipos", equiposAsignados, equipoIds.size());
        } else {
            logger.info("ℹ️ No hay equipos para asignar");
        }

        logger.info("╔════════════════════════════════════════════════════════╗");
        logger.info("║     ✅ USUARIO AGREGADO AL REPOSITORIO EXITOSAMENTE    ║");
        logger.info("╚════════════════════════════════════════════════════════╝");
    }
}
