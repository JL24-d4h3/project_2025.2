package org.project.project.service;

import org.project.project.model.entity.*;
import org.project.project.model.dto.InviteUserRequest;
import org.project.project.model.dto.ProjectMemberDTO;
import org.project.project.repository.*;
import org.project.project.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectTeamService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectTeamService.class);
    private static final int MAX_ROLES_POR_USUARIO = 3;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioHasProyectoRepository usuarioHasProyectoRepository;

    @Autowired
    private UsuarioHasEquipoRepository usuarioHasEquipoRepository;

    @Autowired
    private RolProyectoRepository rolProyectoRepository;

    @Autowired
    private AsignacionRolProyectoRepository asignacionRolProyectoRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ProyectoInvitacionService invitacionService;

    @Autowired
    private ProyectoInvitacionRepository invitacionRepository;

    @Autowired
    private ProjectRoleService projectRoleService;

    @Autowired
    private TokenRepository tokenRepository;

    // =================== INVITAR USUARIOS (GRUPAL) ===================

    @Transactional
    public Map<String, Object> invitarUsuariosGrupal(Long proyectoId, InviteUserRequest request, Long usuarioIdActual) {
        logger.info("╔════════════════════════════════════════════════════════╗");
        logger.info("║         INVITAR USUARIOS - PROYECTO GRUPAL            ║");
        logger.info("╚════════════════════════════════════════════════════════╝");
        logger.info("📋 Proyecto: {}, Usuarios a invitar: {}, Roles: {}", proyectoId, request.getEmails().size(), request.getRolIds().size());

        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, String>> invitados = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        try {
            // Validar proyecto existe
            Proyecto proyecto = proyectoRepository.findById(proyectoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));
            logger.info("✅ Proyecto encontrado: {}", proyecto.getNombreProyecto());

            // Validar que usuario sea creador
            if (!proyecto.getCreatedBy().getUsuarioId().equals(usuarioIdActual)) {
                throw new IllegalArgumentException("Solo el creador puede invitar usuarios");
            }
            logger.info("✅ Usuario es creador del proyecto");

            // Validar que existan roles
            if (request.getRolIds() == null || request.getRolIds().isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar al menos un rol");
            }
            logger.info("✅ Se seleccionaron {} rol(es)", request.getRolIds().size());

            // Obtener usuario actual para invitación
            Usuario usuarioActual = usuarioRepository.findById(usuarioIdActual)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            // Procesar cada email
            for (String email : request.getEmails()) {
                logger.info("📧 Procesando email: {}", email);

                try {
                    // Validar que usuario exista
                    Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(email);
                    if (usuarioOpt.isEmpty()) {
                        String error = "Usuario con email " + email + " no encontrado";
                        logger.warn("❌ {}", error);
                        errores.add(error);
                        continue;
                    }

                    Usuario usuarioAInvitar = usuarioOpt.get();
                    logger.info("✅ Usuario encontrado: {}", usuarioAInvitar.getUsername());

                    // ✅ FIX: Permitir re-invitar si es a EQUIPOS DIFERENTES
                    // Verificar si el usuario ya está en TODOS los equipos especificados
                    boolean yaEsMiembro = usuarioHasProyectoRepository.existsById_UserIdAndId_ProjectId(
                            usuarioAInvitar.getUsuarioId(), proyectoId);
                    
                    if (yaEsMiembro && request.getEquipoIds() != null && !request.getEquipoIds().isEmpty()) {
                        // Usuario ya es miembro - verificar si ya está en todos los equipos
                        boolean yaEnTodosEquipos = true;
                        List<Long> equiposNuevos = new java.util.ArrayList<>();
                        
                        for (Long equipoId : request.getEquipoIds()) {
                            // Saltar IDs negativos (temporales) - se asume que son nuevos
                            if (equipoId < 0) {
                                equiposNuevos.add(equipoId);
                                yaEnTodosEquipos = false;
                                continue;
                            }
                            
                            boolean yaEnEquipo = usuarioHasEquipoRepository.existsById_UserIdAndId_TeamId(
                                    usuarioAInvitar.getUsuarioId(), equipoId);
                            
                            if (!yaEnEquipo) {
                                yaEnTodosEquipos = false;
                                equiposNuevos.add(equipoId);
                            }
                        }
                        
                        if (yaEnTodosEquipos) {
                            logger.warn("⚠️ Usuario {} ya es miembro de todos los equipos especificados", email);
                            errores.add("Usuario " + email + " ya es miembro de todos los equipos especificados. Seleccione equipos diferentes.");
                            continue;
                        }
                        
                        // Actualizar la lista de equipos solo con los nuevos
                        request.setEquipoIds(equiposNuevos);
                        logger.info("✅ Usuario ya es miembro pero se agregará a {} equipo(s) nuevo(s)", equiposNuevos.size());
                    }

                    // Invitar usuario
                    String permiso = request.getPermission() != null ? request.getPermission() : "LECTOR";
                    _invitarUsuario(proyecto, usuarioAInvitar, usuarioActual, request.getRolIds(), email, permiso, request.getEquipoIds(), request.getTeamNamesMap(), "GRUPAL");

                    Map<String, String> invitado = new HashMap<>();
                    invitado.put("email", email);
                    invitado.put("status", "INVITADO");
                    invitado.put("roles", String.join(", ", request.getRolIds().stream().map(String::valueOf).collect(Collectors.toList())));
                    invitados.add(invitado);
                    logger.info("✅ Usuario {} invitado exitosamente", email);

                } catch (Exception e) {
                    logger.error("❌ Error invitando a {}: {}", email, e.getMessage());
                    errores.add("Error invitando a " + email + ": " + e.getMessage());
                }
            }

            resultado.put("success", errores.isEmpty());
            resultado.put("invitados", invitados);
            resultado.put("errores", errores);
            resultado.put("mensaje", invitados.size() + " invitaciones enviadas");

            logger.info("╔════════════════════════════════════════════════════════╗");
            logger.info("║         INVITACIONES PROCESADAS                       ║");
            logger.info("╚════════════════════════════════════════════════════════╝");
            return resultado;

        } catch (Exception e) {
            logger.error("❌ Error crítico en invitación grupal: {}", e.getMessage(), e);
            resultado.put("success", false);
            resultado.put("mensaje", "Error: " + e.getMessage());
            return resultado;
        }
    }

    // =================== INVITAR USUARIOS (EMPRESARIAL) ===================

    @Transactional
    public Map<String, Object> invitarUsuariosEmpresarial(Long proyectoId, InviteUserRequest request, Long usuarioIdActual) {
        logger.info("╔════════════════════════════════════════════════════════╗");
        logger.info("║       INVITAR USUARIOS - PROYECTO EMPRESARIAL         ║");
        logger.info("╚════════════════════════════════════════════════════════╝");

        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, String>> invitados = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        try {
            // Validar proyecto
            Proyecto proyecto = proyectoRepository.findById(proyectoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));
            logger.info("✅ Proyecto empresarial: {}", proyecto.getNombreProyecto());

            // Validar creador
            if (!proyecto.getCreatedBy().getUsuarioId().equals(usuarioIdActual)) {
                throw new IllegalArgumentException("Solo el creador puede invitar usuarios");
            }

            // Obtener usuario actual para invitación
            Usuario usuarioActual = usuarioRepository.findById(usuarioIdActual)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            logger.info("✅ Usuario invitador: {}", usuarioActual.getUsername());

            // Procesar cada email
            for (String email : request.getEmails()) {
                logger.info("📧 Procesando invitación para: {}", email);

                try {
                    // Para proyectos EMPRESARIALES: el usuario DEBE existir en la BD
                    // (la validación en frontend ya debería prevenir emails no registrados)
                    Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(email);
                    
                    if (usuarioOpt.isEmpty()) {
                        String error = "Usuario con email " + email + " no encontrado en la plataforma";
                        logger.warn("❌ {}", error);
                        errores.add(error);
                        continue;
                    }

                    Usuario usuarioAInvitar = usuarioOpt.get();
                    logger.info("✅ Usuario encontrado: {} (ID: {})", usuarioAInvitar.getUsername(), usuarioAInvitar.getUsuarioId());

                    // ✅ FIX: Permitir re-invitar si es a EQUIPOS DIFERENTES
                    // Verificar si el usuario ya está en TODOS los equipos especificados
                    boolean yaEsMiembro = usuarioHasProyectoRepository.existsById_UserIdAndId_ProjectId(
                            usuarioAInvitar.getUsuarioId(), proyectoId);
                    
                    if (yaEsMiembro && request.getEquipoIds() != null && !request.getEquipoIds().isEmpty()) {
                        // Usuario ya es miembro - verificar si ya está en todos los equipos
                        boolean yaEnTodosEquipos = true;
                        List<Long> equiposNuevos = new java.util.ArrayList<>();
                        
                        for (Long equipoId : request.getEquipoIds()) {
                            // Saltar IDs negativos (temporales) - se asume que son nuevos
                            if (equipoId < 0) {
                                equiposNuevos.add(equipoId);
                                yaEnTodosEquipos = false;
                                continue;
                            }
                            
                            boolean yaEnEquipo = usuarioHasEquipoRepository.existsById_UserIdAndId_TeamId(
                                    usuarioAInvitar.getUsuarioId(), equipoId);
                            
                            if (!yaEnEquipo) {
                                yaEnTodosEquipos = false;
                                equiposNuevos.add(equipoId);
                            }
                        }
                        
                        if (yaEnTodosEquipos) {
                            logger.warn("⚠️ Usuario {} ya es miembro de todos los equipos especificados", email);
                            errores.add("Usuario " + email + " ya es miembro de todos los equipos especificados. Seleccione equipos diferentes.");
                            continue;
                        }
                        
                        // Actualizar la lista de equipos solo con los nuevos
                        request.setEquipoIds(equiposNuevos);
                        logger.info("✅ Usuario ya es miembro pero se agregará a {} equipo(s) nuevo(s)", equiposNuevos.size());
                    }

                    // Invitar usuario al proyecto
                    String permiso = request.getPermission() != null ? request.getPermission() : "LECTOR";
                    _invitarUsuario(proyecto, usuarioAInvitar, usuarioActual, request.getRolIds(), email, permiso, request.getEquipoIds(), request.getTeamNamesMap(), "EMPRESARIAL");

                    Map<String, String> invitado = new HashMap<>();
                    invitado.put("email", email);
                    invitado.put("status", "INVITADO");
                    invitados.add(invitado);
                    logger.info("✅ Usuario {} invitado exitosamente al proyecto {}", email, proyecto.getNombreProyecto());

                } catch (Exception e) {
                    logger.error("❌ Error invitando a {}: {}", email, e.getMessage());
                    errores.add("Error invitando a " + email + ": " + e.getMessage());
                }
            }

            resultado.put("success", errores.isEmpty());
            resultado.put("invitados", invitados);
            resultado.put("errores", errores);
            resultado.put("mensaje", invitados.size() + " invitaciones enviadas");

            return resultado;

        } catch (Exception e) {
            logger.error("❌ Error crítico en invitación empresarial: {}", e.getMessage(), e);
            resultado.put("success", false);
            resultado.put("mensaje", "Error: " + e.getMessage());
            return resultado;
        }
    }

    // =================== HELPERS INTERNOS ===================

    @Transactional
    private void _invitarUsuario(Proyecto proyecto, Usuario usuario, Usuario invitadoPor, List<Long> rolIds, String correoInvitado) {
        _invitarUsuario(proyecto, usuario, invitadoPor, rolIds, correoInvitado, "LECTOR", new ArrayList<>(), null, "GRUPAL");
    }

    @Transactional
    private void _invitarUsuario(Proyecto proyecto, Usuario usuario, Usuario invitadoPor, List<Long> rolIds, String correoInvitado, String permiso, List<Long> equipoIds, String tipoInvitacion) {
        _invitarUsuario(proyecto, usuario, invitadoPor, rolIds, correoInvitado, permiso, equipoIds, null, tipoInvitacion);
    }

    @Transactional
    private void _invitarUsuario(Proyecto proyecto, Usuario usuario, Usuario invitadoPor, List<Long> rolIds, String correoInvitado, String permiso, List<Long> equipoIds, Map<Long, String> teamNamesMap, String tipoInvitacion) {
        logger.info("🔗 _invitarUsuario: usuario={}, proyecto={}, roles={}, permiso={}, equipos={}, tipo={}", usuario.getUsuarioId(), proyecto.getProyectoId(), rolIds.size(), permiso, equipoIds != null ? equipoIds.size() : 0, tipoInvitacion);

        // **NUEVO FLUJO**: Crear invitación PENDIENTE en vez de agregar directamente al proyecto
        String tokenValue = null;
        try {
            logger.info("📧 ========== INICIANDO PROCESO DE INVITACIÓN PENDIENTE ==========");
            logger.info("📧 Usuario destinatario: {}", usuario.getCorreo());
            logger.info("📧 Proyecto: {}", proyecto.getNombreProyecto());
            
            // 1. Generar token único
            tokenValue = java.util.UUID.randomUUID().toString();
            logger.info("🔑 Token generado: {}", tokenValue);
            
            // 2. Crear invitación PENDIENTE (NO agrega al usuario aún)
            invitacionService.crearInvitacion(
                    proyecto,
                    usuario,
                    invitadoPor,
                    rolIds,
                    permiso,
                    equipoIds,
                    teamNamesMap, // Pass team names map
                    tipoInvitacion,
                    tokenValue
            );
            
            logger.info("✅ Invitación pendiente creada");
            
            // 3. Crear token en tabla token (para compatibilidad)
            List<Token> tokensAnteriores = tokenRepository.findByUsuarioAndTokenStatus(usuario, Token.EstadoToken.ACTIVO);
            logger.info("🔄 Revocando {} tokens anteriores", tokensAnteriores.size());
            for (Token t : tokensAnteriores) {
                if (t.getValorToken().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                    logger.info("   🔒 Revocando token UUID: {}", t.getValorToken());
                    t.setEstadoToken(Token.EstadoToken.REVOCADO);
                    tokenRepository.save(t);
                }
            }

            Token token = new Token();
            token.setValorToken(tokenValue);
            token.setEstadoToken(Token.EstadoToken.ACTIVO);
            token.setFechaCreacionToken(LocalDateTime.now());
            token.setFechaExpiracionToken(LocalDateTime.now().plusDays(7));
            token.setUsuario(usuario);
            
            logger.info("💾 Guardando token en base de datos...");
            Token tokenGuardado = tokenRepository.save(token);
            logger.info("✅ Token guardado exitosamente: {} (ID: {})", tokenValue, tokenGuardado.getTokenId());

            // 4. Enviar email con botones de Aceptar/Rechazar
            String tipoEmail = tipoInvitacion.equals("EMPRESARIAL") ? "ENTERPRISE" : "GROUP";
            logger.info("📧 ========== LLAMANDO A emailService.enviarInvitacionProyecto ==========");
            logger.info("📧 Parámetros:");
            logger.info("   - Usuario: {} ({})", usuario.getNombreUsuario(), usuario.getCorreo());
            logger.info("   - NombreProyecto: {}", proyecto.getNombreProyecto());
            logger.info("   - TipoInvitacion: {} (original: {})", tipoEmail, tipoInvitacion);
            logger.info("   - Token.valorToken: {}", tokenGuardado.getValorToken());
            logger.info("   - Token.usuario.correo: {}", tokenGuardado.getUsuario().getCorreo());
            
            emailService.enviarInvitacionProyecto(usuario, proyecto.getNombreProyecto(), tipoEmail, tokenGuardado);
            
            logger.info("✅✅✅ Email de invitación PENDIENTE enviado exitosamente a {} [{}]", correoInvitado, tipoEmail);
            logger.info("📧 ========== FIN PROCESO DE INVITACIÓN PENDIENTE ==========");
        } catch (Exception e) {
            logger.error("❌❌❌ ERROR CRÍTICO enviando email de invitación ❌❌❌");
            logger.error("❌ Mensaje: {}", e.getMessage());
            logger.error("❌ Tipo: {}", e.getClass().getName());
            logger.error("❌ Correo destinatario: {}", correoInvitado);
            logger.error("❌ Proyecto: {}", proyecto.getNombreProyecto());
            logger.error("❌ Token generado: {}", tokenValue);
            logger.error("❌ Usuario: {} (ID: {})", usuario.getCorreo(), usuario.getUsuarioId());
            logger.error("❌ Stack trace completo:", e);
            
            // IMPORTANTE: Lanzar excepción para que el usuario sepa que el email falló
            throw new RuntimeException("Error al enviar email de invitación a " + correoInvitado + ": " + e.getMessage(), e);
        }
    }

    @Transactional
    // =================== MÉTODOS AUXILIARES ===================

    private String extraerDominio(String email) {
        if (email == null || !email.contains("@")) {
            return "";
        }
        return "@" + email.split("@")[1];
    }

    // =================== LISTAR MIEMBROS ===================

    @Transactional(readOnly = true)
    public List<ProjectMemberDTO> listarMiembros(Long proyectoId, Long usuarioIdActual) {
        logger.info("📋 Listando miembros del proyecto {}", proyectoId);

        try {
            Proyecto proyecto = proyectoRepository.findById(proyectoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

            List<Usuario> usuarios = usuarioHasProyectoRepository.findUsersByProjectId(proyectoId);
            logger.info("✅ Se encontraron {} miembros", usuarios.size());

            return usuarios.stream()
                    .map(u -> _convertToMemberDTO(u, proyectoId, usuarioIdActual, proyecto.getCreatedBy().getUsuarioId()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("❌ Error al listar miembros: {}", e.getMessage(), e);
            throw new RuntimeException("Error al listar miembros: " + e.getMessage(), e);
        }
    }

    private ProjectMemberDTO _convertToMemberDTO(Usuario usuario, Long proyectoId, Long usuarioIdActual, Long creadoPorId) {
        ProjectMemberDTO dto = new ProjectMemberDTO();
        dto.setUsuarioId(usuario.getUsuarioId());
        dto.setNombreCompleto(usuario.getNombreUsuario() + " " + usuario.getApellidoPaterno());
        dto.setCorreo(usuario.getCorreo());
        dto.setFotoPerfil(usuario.getFotoPerfil());
        dto.setEsCreador(usuario.getUsuarioId().equals(creadoPorId));

        // Obtener roles
        List<AsignacionRolProyecto> asignaciones = asignacionRolProyectoRepository.findByUsuarioIdAndProyectoId(usuario.getUsuarioId(), proyectoId);
        dto.setRolesAsignados(asignaciones.stream()
                .map(a -> a.getRolProyecto().getNombreRolProyecto())
                .collect(Collectors.toList()));

        // El estado de invitación se obtiene del token (si existe token INVITACION_PROYECTO para este usuario)
        // Por defecto es ACTIVO (miembro del proyecto)
        dto.setEstadoInvitacion("ACTIVO");

        return dto;
    }

    // =================== REMOVER MIEMBRO ===================

    @Transactional
    public void removerMiembro(Long proyectoId, Long usuarioId, Long usuarioIdActual) {
        logger.info("🗑️  Removiendo usuario {} del proyecto {}", usuarioId, proyectoId);

        try {
            Proyecto proyecto = proyectoRepository.findById(proyectoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

            // Validar que quien lo hace es creador
            if (!proyecto.getCreatedBy().getUsuarioId().equals(usuarioIdActual)) {
                throw new IllegalArgumentException("Solo el creador puede remover miembros");
            }

            // No permitir remover al creador
            if (usuarioId.equals(proyecto.getCreatedBy().getUsuarioId())) {
                throw new IllegalArgumentException("No se puede remover al creador del proyecto");
            }

            // Eliminar: usuario_has_proyecto, asignaciones, usuario_has_equipo
            UsuarioHasProyectoId uhpId = new UsuarioHasProyectoId(usuarioId, proyectoId);
            usuarioHasProyectoRepository.deleteById(uhpId);
            logger.info("✅ usuario_has_proyecto eliminado");

            List<AsignacionRolProyecto> asignaciones = asignacionRolProyectoRepository.findByUsuarioIdAndProyectoId(usuarioId, proyectoId);
            asignacionRolProyectoRepository.deleteAll(asignaciones);
            logger.info("✅ {} asignaciones de rol eliminadas", asignaciones.size());

        } catch (Exception e) {
            logger.error("❌ Error al remover miembro: {}", e.getMessage(), e);
            throw new RuntimeException("Error al remover miembro: " + e.getMessage(), e);
        }
    }
}
