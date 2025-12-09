package org.project.project.service;

import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.Rol;
import org.project.project.model.entity.Token;
import org.project.project.repository.UsuarioRepository;
import org.project.project.repository.RolRepository;
import org.project.project.repository.FeedbackRepository;
import org.project.project.repository.NotificacionRepository;
import org.project.project.repository.TicketRepository;
import org.project.project.repository.TicketHasUsuarioRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de usuarios de la plataforma
 * Maneja operaciones CRUD para usuarios PO, QA y DEV (excluyendo SA)
 */
@Service
@Transactional
public class PlatformUserManagementService {

    private static final Logger logger = LoggerFactory.getLogger(PlatformUserManagementService.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final FeedbackRepository feedbackRepository;
    private final NotificacionRepository notificacionRepository;
    private final TicketRepository ticketRepository;
    private final TicketHasUsuarioRepository ticketHasUsuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenService tokenService;

    public PlatformUserManagementService(UsuarioRepository usuarioRepository,
                                       RolRepository rolRepository,
                                       FeedbackRepository feedbackRepository,
                                       NotificacionRepository notificacionRepository,
                                       TicketRepository ticketRepository,
                                       TicketHasUsuarioRepository ticketHasUsuarioRepository,
                                       PasswordEncoder passwordEncoder,
                                       EmailService emailService,
                                       TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.feedbackRepository = feedbackRepository;
        this.notificacionRepository = notificacionRepository;
        this.ticketRepository = ticketRepository;
        this.ticketHasUsuarioRepository = ticketHasUsuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenService = tokenService;
    }

    /**
     * Obtiene métricas de usuarios de la plataforma
     */
    public Map<String, Object> obtenerMetricasPlataforma() {
        Map<String, Object> metricas = new HashMap<>();

        // Conteo por roles usando queries del repository (todos los usuarios, sin importar estado)
        long totalDEV = usuarioRepository.countByRolNombreRol_DEV();
        long totalQA = usuarioRepository.countByRolNombreRol_QA();
        long totalPO = usuarioRepository.countByRolNombreRol_PO();
        long totalUsuarios = usuarioRepository.countPlatformUsers();

        metricas.put("totalDEV", totalDEV);
        metricas.put("totalQA", totalQA);
        metricas.put("totalPO", totalPO);
        metricas.put("totalUsuarios", totalUsuarios);

        // Conteo por estado usando queries del repository
        long usuariosHabilitados = usuarioRepository.countEnabledPlatformUsers();
        long usuariosInhabilitados = usuarioRepository.countDisabledPlatformUsers();

        metricas.put("usuariosHabilitados", usuariosHabilitados);
        metricas.put("usuariosInhabilitados", usuariosInhabilitados);

        // Usuarios en los últimos 30 días
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);

        // Usuarios que se unieron en los últimos 30 días (usando query del repository)
        long usuariosNuevosMes = usuarioRepository.countByFechaCreacionBetween(hace30Dias, LocalDateTime.now());

        // Para usuarios que abandonaron, necesitamos obtener la lista para filtrar
        // Nota: "Abandonaron" significa usuarios eliminados de la plataforma, no inhabilitados
        // Por ahora ponemos 0 hasta que se implemente la lógica de eliminación de usuarios
        long usuariosAbandonaronMes = 0;

        metricas.put("usuariosNuevosMes", usuariosNuevosMes);
        metricas.put("usuariosAbandonaronMes", usuariosAbandonaronMes);

        return metricas;
    }

    /**
     * Obtiene usuarios de la plataforma con paginación y filtros
     */
    public Page<Usuario> obtenerUsuariosPlataforma(int page, int size, String sortBy, String sortDir, String search) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (search != null && !search.trim().isEmpty()) {
            return usuarioRepository.findPlatformUsersWithSearch(search.trim(), pageable);
        } else {
            return usuarioRepository.findPlatformUsers(pageable);
        }
    }

    /**
     * Obtiene un usuario por ID (solo si no es SA)
     */
    public Usuario obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        // Verificar que no sea SA
        boolean esSA = usuario.getRoles().stream()
            .anyMatch(rol -> rol.getNombreRol() == Rol.NombreRol.SA);

        if (esSA) {
            throw new ResourceNotFoundException("No se puede acceder a usuarios Super Administradores");
        }

        return usuario;
    }

    /**
     * Crea un nuevo usuario de la plataforma
     */
    public Usuario crearUsuarioPlataforma(Usuario usuario, List<Long> roleIds) {
        // Configurar campos automáticos
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setEstadoUsuario(Usuario.EstadoUsuario.INHABILITADO); // Debe activar cuenta
        usuario.setActividadUsuario(Usuario.ActividadUsuario.ACTIVO);
        usuario.setAccesoUsuario(Usuario.AccesoUsuario.NO); // NO hasta que configure contraseña

        // Encriptar contraseña o asignar temporal
        if (usuario.getHashedPassword() != null && !usuario.getHashedPassword().isEmpty()) {
            usuario.setHashedPassword(passwordEncoder.encode(usuario.getHashedPassword()));
        } else {
            // Contraseña temporal que se debe cambiar
            String tempPassword = "TempPass123!";
            usuario.setHashedPassword(passwordEncoder.encode(tempPassword));
        }

        // Guardar usuario
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Asignar roles (excluir SA)
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Rol> roles = obtenerRolesValidosParaPlataforma(roleIds);
            usuarioGuardado.setRoles(roles);
            usuarioGuardado = usuarioRepository.save(usuarioGuardado);
        }

        // Generar token de recuperación de contraseña y enviar email
        Token token = tokenService.generarTokenRecuperacionContrasena(usuarioGuardado);

        // Enviar email con enlace para cambiar contraseña (similar a "forgot password")
        emailService.enviarCorreoRestablecimientoContrasena(usuarioGuardado, token);

        return usuarioGuardado;
    }

    /**
     * Crea invitación para usuario (solo email y rol)
     */
    public Usuario crearInvitacionUsuario(String email, Long rolId) {
        logger.info("=== INICIANDO crearInvitacionUsuario ===");
        logger.info("Email: {}, RolId: {}", email, rolId);

        try {
            // Verificar que el email no exista
            logger.info("Verificando si el email ya existe...");
            if (usuarioRepository.findByCorreo(email).isPresent()) {
                logger.warn("Ya existe un usuario con el email: {}", email);
                throw new IllegalArgumentException("Ya existe un usuario con este correo electrónico");
            }

            // Obtener el rol válido
            logger.info("Obteniendo rol válido para ID: {}", rolId);
            Rol rol = obtenerRolValidoParaPlataforma(rolId);
            logger.info("Rol obtenido: {} ({})", rol.getNombreRol(), rol.getRolId());

            // Crear usuario temporal con datos mínimos
            logger.info("Creando usuario temporal...");
            Usuario usuario = new Usuario();
            usuario.setCorreo(email);
            usuario.setUsername(email); // temporal, será cambiado al completar perfil
            usuario.setNombreUsuario("Pendiente"); // temporal
            usuario.setApellidoPaterno("Pendiente"); // temporal
            usuario.setApellidoMaterno("Pendiente"); // temporal
            usuario.setDni("00000000"); // temporal
            usuario.setDireccionUsuario("Pendiente"); // temporal
            usuario.setHashedPassword(passwordEncoder.encode("temporal123")); // temporal
            usuario.setFechaCreacion(LocalDateTime.now());
            usuario.setEstadoUsuario(Usuario.EstadoUsuario.INHABILITADO);
            usuario.setActividadUsuario(Usuario.ActividadUsuario.ACTIVO);
            usuario.setAccesoUsuario(Usuario.AccesoUsuario.NO); // NO hasta completar perfil

            // Guardar usuario temporal
            logger.info("Guardando usuario temporal...");
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            logger.info("Usuario guardado con ID: {}", usuarioGuardado.getUsuarioId());

            // Asignar rol
            logger.info("Asignando rol al usuario...");
            Set<Rol> roles = new HashSet<>();
            roles.add(rol);
            usuarioGuardado.setRoles(roles);
            usuarioGuardado = usuarioRepository.save(usuarioGuardado);
            logger.info("Rol asignado correctamente");

            // Generar token de invitación y enviar email
            logger.info("Generando token de invitación...");
            tokenService.generarTokenInvitacion(usuarioGuardado);
            logger.info("Token de invitación generado y email enviado");

            logger.info("=== crearInvitacionUsuario COMPLETADO EXITOSAMENTE ===");
            return usuarioGuardado;

        } catch (Exception e) {
            logger.error("Error en crearInvitacionUsuario: ", e);
            throw e;
        }
    }

    /**
     * Habilita un usuario
     */
    public Usuario habilitarUsuario(Long id) {
        logger.info("=== INICIANDO habilitarUsuario ===");
        logger.info("ID del usuario a habilitar: {}", id);

        Usuario usuario = obtenerUsuarioPorId(id);
        logger.info("Usuario encontrado: {} - Estado actual: {}", usuario.getUsername(), usuario.getEstadoUsuario());

        usuario.setEstadoUsuario(Usuario.EstadoUsuario.HABILITADO);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        logger.info("Usuario habilitado exitosamente: {} - Nuevo estado: {}",
            usuarioActualizado.getUsername(), usuarioActualizado.getEstadoUsuario());
        logger.info("=== FIN habilitarUsuario ===");

        return usuarioActualizado;
    }

    /**
     * Inhabilita un usuario
     */
    public Usuario inhabilitarUsuario(Long id) {
        logger.info("=== INICIANDO inhabilitarUsuario ===");
        logger.info("ID del usuario a inhabilitar: {}", id);

        Usuario usuario = obtenerUsuarioPorId(id);
        logger.info("Usuario encontrado: {} - Estado actual: {}", usuario.getUsername(), usuario.getEstadoUsuario());

        usuario.setEstadoUsuario(Usuario.EstadoUsuario.INHABILITADO);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        logger.info("Usuario inhabilitado exitosamente: {} - Nuevo estado: {}",
            usuarioActualizado.getUsername(), usuarioActualizado.getEstadoUsuario());
        logger.info("=== FIN inhabilitarUsuario ===");

        return usuarioActualizado;
    }

    /**
     * Elimina un usuario de la plataforma con eliminación forzada en cascada
     */
    @Transactional
    public void eliminarUsuario(Long id) {
        eliminarUsuario(id, false);
    }

    /**
     * Elimina un usuario de la plataforma
     * @param id ID del usuario a eliminar
     * @param forzarEliminacion Si es true, usa eliminación SQL nativa más agresiva
     */
    @Transactional(rollbackFor = Exception.class)
    public void eliminarUsuario(Long id, boolean forzarEliminacion) {
        logger.info("╔════════════════════════════════════════════════════════════════╗");
        logger.info("║       INICIO ELIMINACIÓN USUARIO - SERVICE                    ║");
        logger.info("╚════════════════════════════════════════════════════════════════╝");
        logger.info("📋 Parámetros:");
        logger.info("   - Usuario ID: {}", id);
        logger.info("   - Modo forzado: {}", forzarEliminacion);

        try {
            logger.info("🔍 PASO 1: Obteniendo información del usuario...");
            Usuario usuario = obtenerUsuarioPorId(id);
            logger.info("   ✅ Usuario encontrado:");
            logger.info("      - ID: {}", usuario.getUsuarioId());
            logger.info("      - Username: {}", usuario.getUsername());
            logger.info("      - Email: {}", usuario.getCorreo());
            logger.info("      - Estado: {}", usuario.getEstadoUsuario());
            logger.info("      - Roles: {}", usuario.getRoles().stream()
                .map(r -> r.getNombreRol().toString())
                .toList());

            logger.info("🗑️ PASO 2: Iniciando proceso de eliminación...");
            if (forzarEliminacion) {
                logger.info("   ⚡ Modo: FORZADO (SQL Nativo)");
                eliminarUsuarioForzado(id, usuario);
            } else {
                logger.info("   📦 Modo: NORMAL (JPA)");
                eliminarUsuarioNormal(id, usuario);
            }

            logger.info("╔════════════════════════════════════════════════════════════════╗");
            logger.info("║       ELIMINACIÓN COMPLETADA EXITOSAMENTE                     ║");
            logger.info("╚════════════════════════════════════════════════════════════════╝");
            
        } catch (Exception e) {
            logger.error("╔════════════════════════════════════════════════════════════════╗");
            logger.error("║       ERROR CRÍTICO EN ELIMINACIÓN - SERVICE                  ║");
            logger.error("╚════════════════════════════════════════════════════════════════╝");
            logger.error("❌ Error eliminando usuario ID: {}", id);
            logger.error("❌ Tipo de excepción: {}", e.getClass().getName());
            logger.error("❌ Mensaje: {}", e.getMessage());
            logger.error("❌ Stack trace:", e);
            throw new RuntimeException("Error al eliminar usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Eliminación normal usando repositorios JPA
     */
    private void eliminarUsuarioNormal(Long id, Usuario usuario) {
        try {
            logger.info("=== ELIMINACIÓN NORMAL EN CASCADA ===");
            logger.info("Eliminando todas las relaciones del usuario ID: {} - {}", id, usuario.getUsername());

            // 1. Eliminar relaciones many-to-many primero (más específicas)
            logger.info("1️⃣ Eliminando relaciones ticket_has_usuario para usuario ID: {}", id);
            ticketHasUsuarioRepository.deleteByUsuarioId(id);
            ticketHasUsuarioRepository.flush();
            logger.info("✅ Relaciones ticket_has_usuario eliminadas");

            // 2. Eliminar tickets donde el usuario es reportador O asignado
            logger.info("2️⃣ Eliminando tickets reportados por usuario ID: {}", id);
            ticketRepository.deleteByReportadoPorUsuarioId(id);
            ticketRepository.flush();
            logger.info("✅ Tickets reportados eliminados");

            logger.info("3️⃣ Eliminando tickets asignados a usuario ID: {}", id);
            ticketRepository.deleteByAsignadoAUsuarioId(id);
            ticketRepository.flush();
            logger.info("✅ Tickets asignados eliminados");

            // 3. Eliminar feedbacks del usuario
            logger.info("4️⃣ Eliminando feedbacks del usuario ID: {}", id);
            feedbackRepository.deleteByUsuarioId(id);
            feedbackRepository.flush();
            logger.info("✅ Feedbacks eliminados");

            // 4. Eliminar notificaciones del usuario
            logger.info("5️⃣ Eliminando notificaciones del usuario ID: {}", id);
            notificacionRepository.deleteByUsuarioId(id);
            notificacionRepository.flush();
            logger.info("✅ Notificaciones eliminadas");

            // 5. Limpiar las asociaciones de roles antes de eliminar
            logger.info("6️⃣ Limpiando roles del usuario...");
            usuario.getRoles().clear();
            usuarioRepository.saveAndFlush(usuario);
            logger.info("✅ Roles limpiados");

            // 6. Finalmente eliminar el usuario
            logger.info("7️⃣ Eliminando usuario: {} (ID: {})", usuario.getUsername(), id);
            usuarioRepository.delete(usuario);
            usuarioRepository.flush();
            logger.info("✅ Usuario eliminado exitosamente: {}", usuario.getUsername());

        } catch (Exception e) {
            logger.error("❌ Error en eliminación normal del usuario {}: {}", usuario.getUsername(), e.getMessage(), e);
            logger.info("🔄 Intentando eliminación forzada...");
            eliminarUsuarioForzado(id, usuario);
        }
    }

    /**
     * Eliminación forzada usando SQL nativo (más agresiva)
     */
    @Transactional(rollbackFor = Exception.class)
    private void eliminarUsuarioForzado(Long id, Usuario usuario) {
        logger.info("╔════════════════════════════════════════════════════════════════╗");
        logger.info("║       ELIMINACIÓN FORZADA CON SQL NATIVO                      ║");
        logger.info("╚════════════════════════════════════════════════════════════════╝");
        logger.warn("⚠️ Usando eliminación forzada para usuario: {} (ID: {})", usuario.getUsername(), id);

        try {
            // Deshabilitar verificación de foreign keys temporalmente
            logger.info("🔓 PASO 1: Deshabilitando verificación de foreign keys...");
            usuarioRepository.disableForeignKeyChecks();
            usuarioRepository.flush();
            logger.info("   ✅ Foreign key checks deshabilitadas");

            // Eliminar todas las relaciones una por una con SQL nativo
            logger.info("🗑️ PASO 2: Eliminando relaciones many-to-many...");
            
            logger.info("   2.1 - Eliminando ticket_has_usuario...");
            usuarioRepository.deleteTicketHasUsuarioByUserId(id);
            usuarioRepository.flush();
            logger.info("      ✅ ticket_has_usuario eliminados");
            
            logger.info("   2.2 - Eliminando usuario_has_equipo...");
            usuarioRepository.deleteUsuarioHasEquipoByUserId(id);
            usuarioRepository.flush();
            logger.info("      ✅ usuario_has_equipo eliminados");
            
            logger.info("   2.3 - Eliminando usuario_has_proyecto...");
            usuarioRepository.deleteUsuarioHasProyectoByUserId(id);
            usuarioRepository.flush();
            logger.info("      ✅ usuario_has_proyecto eliminados");
            
            logger.info("   2.4 - Eliminando usuario_has_repositorio...");
            usuarioRepository.deleteUsuarioHasRepositorioByUserId(id);
            usuarioRepository.flush();
            logger.info("      ✅ usuario_has_repositorio eliminados");
            
            logger.info("   2.5 - Eliminando usuario_has_rol...");
            usuarioRepository.deleteUsuarioHasRolByUserId(id);
            usuarioRepository.flush();
            logger.info("      ✅ usuario_has_rol eliminados");

            logger.info("🗑️ PASO 3: Eliminando entidades dependientes...");
            
            logger.info("   3.1 - Eliminando tickets...");
            usuarioRepository.deleteTicketsByUserId(id);
            usuarioRepository.flush();
            logger.info("      ✅ Tickets eliminados");
            
            logger.info("   3.2 - Eliminando feedback...");
            usuarioRepository.deleteFeedbackByUserId(id);
            usuarioRepository.flush();
            logger.info("      ✅ Feedback eliminado");
            
            logger.info("   3.3 - Eliminando notificaciones...");
            usuarioRepository.deleteNotificacionByUserId(id);
            usuarioRepository.flush();
            logger.info("      ✅ Notificaciones eliminadas");
            
            logger.info("   3.4 - Eliminando conversaciones...");
            usuarioRepository.deleteConversacionByUserId(id);
            usuarioRepository.flush();
            logger.info("      ✅ Conversaciones eliminadas");
            
            logger.info("   3.5 - Eliminando tokens...");
            usuarioRepository.deleteTokenByUserId(id);
            usuarioRepository.flush();
            logger.info("      ✅ Tokens eliminados");

            logger.info("🗑️ PASO 4: Eliminando usuario final...");
            usuarioRepository.deleteUsuarioByUserId(id);
            usuarioRepository.flush();
            logger.info("   ✅ Usuario eliminado de la base de datos");

            // Rehabilitar verificación de foreign keys
            logger.info("🔒 PASO 5: Rehabilitando verificación de foreign keys...");
            usuarioRepository.enableForeignKeyChecks();
            usuarioRepository.flush();
            logger.info("   ✅ Foreign key checks rehabilitadas");

            logger.info("╔════════════════════════════════════════════════════════════════╗");
            logger.info("║       ELIMINACIÓN FORZADA COMPLETADA                          ║");
            logger.info("╚════════════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            logger.error("╔════════════════════════════════════════════════════════════════╗");
            logger.error("║       ERROR EN ELIMINACIÓN FORZADA                            ║");
            logger.error("╚════════════════════════════════════════════════════════════════╝");
            logger.error("❌ Error en eliminación forzada del usuario {} (ID: {})", usuario.getUsername(), id);
            logger.error("❌ Tipo de excepción: {}", e.getClass().getName());
            logger.error("❌ Mensaje: {}", e.getMessage());
            logger.error("❌ Stack trace:", e);
            
            // Como último recurso, rehabilitar foreign keys y lanzar excepción
            try {
                logger.warn("⚠️ Intentando rehabilitar foreign keys después del error...");
                usuarioRepository.enableForeignKeyChecks();
                usuarioRepository.flush();
                logger.info("✅ Foreign keys rehabilitadas");
            } catch (Exception fkError) {
                logger.error("❌ Error crítico rehabilitando foreign keys: {}", fkError.getMessage(), fkError);
            }
            
            throw new RuntimeException("No se pudo eliminar el usuario incluso con eliminación forzada: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza información de un usuario
     */
    public Usuario actualizarUsuario(Long id, Usuario datosActualizados) {
        Usuario usuario = obtenerUsuarioPorId(id);

        // Actualizar campos permitidos
        if (datosActualizados.getNombreUsuario() != null) {
            usuario.setNombreUsuario(datosActualizados.getNombreUsuario());
        }
        if (datosActualizados.getApellidoPaterno() != null) {
            usuario.setApellidoPaterno(datosActualizados.getApellidoPaterno());
        }
        if (datosActualizados.getApellidoMaterno() != null) {
            usuario.setApellidoMaterno(datosActualizados.getApellidoMaterno());
        }
        if (datosActualizados.getDni() != null) {
            usuario.setDni(datosActualizados.getDni());
        }
        if (datosActualizados.getTelefono() != null) {
            usuario.setTelefono(datosActualizados.getTelefono());
        }
        if (datosActualizados.getDireccionUsuario() != null) {
            usuario.setDireccionUsuario(datosActualizados.getDireccionUsuario());
        }
        if (datosActualizados.getFotoPerfil() != null) {
            usuario.setFotoPerfil(datosActualizados.getFotoPerfil());
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Obtiene todos los roles válidos para la plataforma (excluyendo SA)
     */
    public List<Rol> obtenerRolesPlataforma() {
        logger.info("=== obtenerRolesPlataforma ===");
        try {
            List<Rol> todosLosRoles = rolRepository.findAll();
            logger.info("Total de roles en BD: {}", todosLosRoles.size());

            List<Rol> rolesPlataforma = todosLosRoles.stream()
                .filter(rol -> rol.getNombreRol() != Rol.NombreRol.SA)
                .collect(Collectors.toList());

            logger.info("Roles válidos para plataforma: {}", rolesPlataforma.size());
            for (Rol rol : rolesPlataforma) {
                logger.info("- Rol: {} (ID: {})", rol.getNombreRol(), rol.getRolId());
            }

            return rolesPlataforma;
        } catch (Exception e) {
            logger.error("Error al obtener roles de plataforma: ", e);
            throw e;
        }
    }

    /**
     * Obtiene todos los roles disponibles para SuperAdmin (incluyendo SA)
     * A diferencia de obtenerRolesPlataforma(), este método NO excluye el rol SA
     * para permitir que SuperAdmins puedan crear otros SuperAdmins
     */
    public List<Rol> obtenerTodosLosRolesSuperAdmin() {
        logger.info("=== obtenerTodosLosRolesSuperAdmin ===");
        try {
            List<Rol> todosLosRoles = rolRepository.findAll();
            logger.info("Todos los roles para SuperAdmin: {}", todosLosRoles.size());

            for (Rol rol : todosLosRoles) {
                logger.info("- Rol: {} (ID: {})", rol.getNombreRol(), rol.getRolId());
            }

            return todosLosRoles; // Incluye TODOS los roles: SA, PO, DEV, QA
        } catch (Exception e) {
            logger.error("Error al obtener roles para SuperAdmin: ", e);
            return new ArrayList<>(); // Retornar lista vacía en caso de error
        }
    }

    // ============= MÉTODOS AUXILIARES =============

    private Set<Rol> obtenerRolesValidosParaPlataforma(List<Long> roleIds) {
        Set<Rol> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Rol rol = obtenerRolValidoParaPlataforma(roleId);
            roles.add(rol);
        }
        return roles;
    }

    private Rol obtenerRolValidoParaPlataforma(Long roleId) {
        logger.info("=== obtenerRolValidoParaPlataforma ===");
        logger.info("Buscando rol con ID: {}", roleId);

        try {
            Rol rol = rolRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + roleId));

            logger.info("Rol encontrado: {} (ID: {})", rol.getNombreRol(), rol.getRolId());

            // Verificar que no sea SA
            if (rol.getNombreRol() == Rol.NombreRol.SA) {
                logger.warn("Intento de asignar rol SA, bloqueado");
                throw new IllegalArgumentException("No se puede asignar el rol de Super Administrador");
            }

            logger.info("Rol válido para plataforma: {}", rol.getNombreRol());
            return rol;
        } catch (ResourceNotFoundException e) {
            logger.error("Rol no encontrado: {}", roleId);
            throw e;
        }
    }

    /**
     * Crear usuario en plataforma (específico para SuperAdmin - permite TODOS los roles incluyendo SA)
     * @param usuario Usuario a crear
     * @param roleIds Lista de IDs de roles (puede incluir SA)
     * @return Usuario creado
     */
    public Usuario crearUsuarioPlataformaSuperAdmin(Usuario usuario, List<Long> roleIds) {
        logger.info("=== INICIANDO crearUsuarioPlataformaSuperAdmin ===");
        logger.info("Email: {}, RoleIds: {}", usuario.getCorreo(), roleIds);

        try {
            // Configurar campos automáticos (igual que método original)
            usuario.setFechaCreacion(LocalDateTime.now());
            usuario.setEstadoUsuario(Usuario.EstadoUsuario.INHABILITADO);
            usuario.setActividadUsuario(Usuario.ActividadUsuario.ACTIVO);
            usuario.setAccesoUsuario(Usuario.AccesoUsuario.NO);

            // Encriptar contraseña o asignar temporal
            if (usuario.getHashedPassword() != null && !usuario.getHashedPassword().isEmpty()) {
                usuario.setHashedPassword(passwordEncoder.encode(usuario.getHashedPassword()));
            } else {
                String tempPassword = "TempPass123!";
                usuario.setHashedPassword(passwordEncoder.encode(tempPassword));
            }

            // Guardar usuario
            Usuario usuarioGuardado = usuarioRepository.save(usuario);

            // Asignar roles (SIN restricción de SA - usando método SA)
            if (roleIds != null && !roleIds.isEmpty()) {
                Set<Rol> roles = obtenerRolesValidosParaSuperAdmin(roleIds);
                usuarioGuardado.setRoles(roles);
                usuarioGuardado = usuarioRepository.save(usuarioGuardado);
            }

            // Generar token y enviar email
            Token token = tokenService.generarTokenRecuperacionContrasena(usuarioGuardado);
            emailService.enviarCorreoRestablecimientoContrasena(usuarioGuardado, token);

            logger.info("=== crearUsuarioPlataformaSuperAdmin COMPLETADO ===");
            return usuarioGuardado;

        } catch (Exception e) {
            logger.error("Error al crear usuario para SuperAdmin: ", e);
            throw e;
        }
    }

    /**
     * Crear invitación de usuario (específico para SuperAdmin - permite TODOS los roles incluyendo SA)
     * @param email Email del usuario a invitar
     * @param rolId ID del rol a asignar (puede incluir SA)
     * @return Usuario invitado
     */
    public Usuario crearInvitacionUsuarioSuperAdmin(String email, Long rolId) {
        logger.info("=== INICIANDO crearInvitacionUsuarioSuperAdmin ===");
        logger.info("Email: {}, RolId: {}", email, rolId);

        try {
            // Verificar que el email no exista (usando el método correcto)
            if (usuarioRepository.findByCorreo(email).isPresent()) {
                logger.warn("Ya existe un usuario con el email: {}", email);
                throw new IllegalArgumentException("Ya existe un usuario con este correo electrónico");
            }

            // Obtener rol válido (SIN restricción de SA - usando método SA)
            Rol rol = obtenerRolValidoParaSuperAdmin(rolId);
            logger.info("Rol obtenido: {} ({})", rol.getNombreRol(), rol.getRolId());

            // Crear usuario temporal (igual que método original)
            Usuario usuario = new Usuario();
            usuario.setCorreo(email);
            usuario.setUsername(email);
            usuario.setNombreUsuario("Pendiente");
            usuario.setApellidoPaterno("Pendiente");
            usuario.setApellidoMaterno("Pendiente");
            usuario.setDni("00000000");
            usuario.setDireccionUsuario("Pendiente");
            usuario.setHashedPassword(passwordEncoder.encode("temporal123"));
            usuario.setFechaCreacion(LocalDateTime.now());
            usuario.setEstadoUsuario(Usuario.EstadoUsuario.INHABILITADO);
            usuario.setActividadUsuario(Usuario.ActividadUsuario.ACTIVO);
            usuario.setAccesoUsuario(Usuario.AccesoUsuario.NO);

            // Guardar usuario temporal
            Usuario usuarioGuardado = usuarioRepository.save(usuario);

            // Asignar rol (SIN restricción de SA)
            Set<Rol> roles = new HashSet<>();
            roles.add(rol);
            usuarioGuardado.setRoles(roles);
            usuarioGuardado = usuarioRepository.save(usuarioGuardado);

            // Generar token de invitación y enviar email
            tokenService.generarTokenInvitacion(usuarioGuardado);

            logger.info("=== crearInvitacionUsuarioSuperAdmin COMPLETADO ===");
            return usuarioGuardado;

        } catch (Exception e) {
            logger.error("Error al crear invitación para SuperAdmin: ", e);
            throw e;
        }
    }

    /**
     * Obtiene un rol válido para asignación por SuperAdmin (permite TODOS los roles incluyendo SA)
     * @param idRol ID del rol
     * @return Rol válido o lanza excepción si no existe
     */
    private Rol obtenerRolValidoParaSuperAdmin(Long idRol) {
        try {
            Rol rol = rolRepository.findById(idRol)
                    .orElseThrow(() -> new IllegalArgumentException("El rol especificado no existe: " + idRol));

            logger.info("Rol válido para SuperAdmin: {}", rol.getNombreRol());
            return rol;

        } catch (Exception e) {
            logger.error("Error al obtener rol válido para SuperAdmin: ", e);
            throw e;
        }
    }

    /**
     * Obtiene roles válidos para asignación por SuperAdmin (permite TODOS los roles incluyendo SA)
     * @param roleIds Lista de IDs de roles
     * @return Set de roles válidos
     */
    private Set<Rol> obtenerRolesValidosParaSuperAdmin(List<Long> roleIds) {
        Set<Rol> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            roles.add(obtenerRolValidoParaSuperAdmin(roleId));
        }
        return roles;
    }
}
