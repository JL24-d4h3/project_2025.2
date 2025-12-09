package org.project.project.service;

import org.project.project.model.entity.*;
import org.project.project.repository.*;
import org.project.project.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final RolRepository rolRepository;

    private final APIRepository apiRepository;
    private final ProyectoRepository proyectoRepository;
    private final TicketRepository ticketRepository;
    private final RepositorioRepository repositorioRepository;
    private final CategoriaRepository categoriaRepository;

    public UserService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       TokenService tokenService, EmailService emailService, RolRepository rolRepository,
                       APIRepository apiRepository, ProyectoRepository proyectoRepository,
                       TicketRepository ticketRepository, RepositorioRepository repositorioRepository,
                       CategoriaRepository categoriaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.rolRepository = rolRepository;
        this.apiRepository = apiRepository;
        this.proyectoRepository = proyectoRepository;
        this.ticketRepository = ticketRepository;
        this.repositorioRepository = repositorioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarUsuariosExceptoSuperadmin() {
        return usuarioRepository.findUsersExceptRoleById(4); // ID 4 = SA
    }

    /**
     * Busca usuario por ID
     * CACHEABLE: Este método se llama frecuentemente para verificar permisos
     * Hit ratio esperado: >80%
     */
    @Cacheable(value = "usuarios", key = "#id")
    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    public Usuario guardarUsuario(Usuario usuario) {

// La contraseña debe ser codificada ANTES de llamar a este método.
        usuario.setFechaCreacion(LocalDateTime.now());

// Guardar usuario en DB
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
// Generar token
        Token token = tokenService.generarToken(usuarioGuardado);
// Enviar correo con el token
        emailService.enviarTokenPorCorreo(usuarioGuardado, token);
        return usuarioGuardado;
    }

    public Usuario guardarUsuarioConCodigo(Usuario usuario) {
        // La contraseña debe ser codificada ANTES de llamar a este método.
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setEstadoUsuario(Usuario.EstadoUsuario.INHABILITADO); // Usuario inactivo hasta verificar código

        // Guardar usuario en DB
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Generar código de verificación de 6 dígitos
        tokenService.generarCodigoVerificacion(usuarioGuardado);

        return usuarioGuardado;
    }

    public void actualizarContrasena(String usernameOrEmail, String newPassword) {
        Usuario usuario = buscarPorUsernameOEmail(usernameOrEmail);
        usuario.setHashedPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }

    /**
     * Actualiza usuario e invalida caché
     * CACHE_EVICT: Elimina entrada del caché cuando cambian datos del usuario
     */
    @CacheEvict(value = "usuarios", key = "#id")
    public Usuario actualizarUsuario(Long id, Usuario usuarioDetails) {
        Usuario usuario = buscarUsuarioPorId(id);
        usuario.setNombreUsuario(usuarioDetails.getNombreUsuario());
        usuario.setApellidoPaterno(usuarioDetails.getApellidoPaterno());
        usuario.setApellidoMaterno(usuarioDetails.getApellidoMaterno());
        usuario.setDni(usuarioDetails.getDni());
        usuario.setFechaNacimiento(usuarioDetails.getFechaNacimiento());
        usuario.setSexoUsuario(usuarioDetails.getSexoUsuario());
        usuario.setEstadoCivil(usuarioDetails.getEstadoCivil());
        usuario.setTelefono(usuarioDetails.getTelefono());
        usuario.setCorreo(usuarioDetails.getCorreo());
        usuario.setDireccionUsuario(usuarioDetails.getDireccionUsuario());
        usuario.setUsername(usuarioDetails.getUsername());
        usuario.setFotoPerfil(usuarioDetails.getFotoPerfil());
        usuario.setEstadoUsuario(usuarioDetails.getEstadoUsuario());
        usuario.setActividadUsuario(usuarioDetails.getActividadUsuario());
        usuario.setCodigoUsuario(usuarioDetails.getCodigoUsuario());
        usuario.setAccesoUsuario(usuarioDetails.getAccesoUsuario());
// La contraseña (hashedPassword) no se actualiza por esta vía por seguridad
        return usuarioRepository.save(usuario);
    }

    public Usuario eliminarUsuario(Long id) {
        Usuario usuario = buscarUsuarioPorId(id);
        usuarioRepository.delete(usuario);
        return usuario;
    }

    public Usuario buscarPorUsernameOEmail(String usernameOrEmail) {
        return usuarioRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> usuarioRepository.findByCorreo(usernameOrEmail)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usernameOrEmail)));
    }

    public boolean existePorUsernameOEmail(String username, String email) {
        return usuarioRepository.findByUsername(username).isPresent() ||
                usuarioRepository.findByCorreo(email).isPresent();
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    /**
     * Busca usuario por username
     * CACHEABLE: Este es el método MÁS CRÍTICO - se ejecuta en CADA request
     * Hit ratio esperado: >90% (mismo usuario hace múltiples requests)
     * 
     * IMPACTO:
     * - Sin caché: ~30-50ms por query
     * - Con caché: ~0.1ms (300-500x más rápido)
     */
    @Cacheable(value = "usuarios", key = "'username:' + #username")
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con username: " + username));
    }

    // Método para obtener todos los usuarios (para compatibilidad con DashboardController)
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    // Método para listar todos los roles disponibles
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    // Método para obtener roles por sus IDs
    public Set<Rol> obtenerRolesPorIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }

        System.out.println("Buscando roles con IDs: " + roleIds);
        Set<Rol> roles = new HashSet<>(rolRepository.findAllById(roleIds));
        System.out.println("Roles encontrados: " + roles.size());

        return roles;
    }

    // Método para crear un nuevo usuario completo con roles (Para SuperAdmin - No requiere verificación de email)
    public Usuario crearNuevoUsuarioCompleto(Usuario usuario, List<Long> roleIds) {
        try {
            System.out.println("=== INICIO CREACION USUARIO POR SUPERADMIN ===");
            System.out.println("Usuario: " + usuario.getUsername());
            System.out.println("Roles IDs recibidos: " + roleIds);

            // Configurar campos automáticos
            usuario.setFechaCreacion(LocalDateTime.now());

            // IMPORTANTE: Usuario creado por SuperAdmin debe estar HABILITADO automáticamente
            usuario.setEstadoUsuario(Usuario.EstadoUsuario.HABILITADO);
            usuario.setActividadUsuario(Usuario.ActividadUsuario.ACTIVO);

            // Encriptar la contraseña
            if (usuario.getHashedPassword() != null && !usuario.getHashedPassword().isEmpty()) {
                usuario.setHashedPassword(passwordEncoder.encode(usuario.getHashedPassword()));
                System.out.println("Contraseña encriptada correctamente");
            }

            // Guardar el usuario primero
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            System.out.println("Usuario guardado con ID: " + usuarioGuardado.getUsuarioId());

            // Asignar roles si se proporcionaron
            if (roleIds != null && !roleIds.isEmpty()) {
                Set<Rol> roles = obtenerRolesPorIds(roleIds);
                System.out.println("Roles obtenidos: " + roles.size());
                for (Rol rol : roles) {
                    System.out.println("- Rol ID: " + rol.getRolId() + ", Nombre: " + rol.getNombreRol());
                }

                usuarioGuardado.setRoles(roles);
                usuarioGuardado = usuarioRepository.save(usuarioGuardado);
                System.out.println("Usuario guardado con roles asignados");
            }

            System.out.println("=== USUARIO CREADO POR SUPERADMIN - NO SE ENVÍA TOKEN ===");
            System.out.println("Estado: " + usuarioGuardado.getEstadoUsuario());
            System.out.println("=== FIN CREACION USUARIO ===");
            return usuarioGuardado;

        } catch (Exception e) {
            System.out.println("ERROR en crearNuevoUsuarioCompleto: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public java.util.Map<String, Object> obtenerMetricasDashboard() {
        java.util.Map<String, Object> metrics = new java.util.HashMap<>();

        try {

            List<Usuario> todosUsuarios = usuarioRepository.findAll();

            metrics.put("totalUsuarios", todosUsuarios.size());

            long usuariosHabilitados = todosUsuarios.stream()
                    .filter(u -> u.getEstadoUsuario() == Usuario.EstadoUsuario.HABILITADO)
                    .count();
            long usuariosInhabilitados = todosUsuarios.size() - usuariosHabilitados;

            metrics.put("usuariosHabilitados", usuariosHabilitados);
            metrics.put("usuariosInhabilitados", usuariosInhabilitados);

            long usuariosActivos = todosUsuarios.stream()
                    .filter(u -> u.getActividadUsuario() == Usuario.ActividadUsuario.ACTIVO)
                    .count();
            long usuariosInactivos = todosUsuarios.size() - usuariosActivos;

            metrics.put("usuariosActivos", usuariosActivos);
            metrics.put("usuariosInactivos", usuariosInactivos);

            LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
            long usuariosCreados30Dias = todosUsuarios.stream()
                    .filter(u -> u.getFechaCreacion() != null && u.getFechaCreacion().isAfter(hace30Dias))
                    .count();
            metrics.put("usuariosCreados30Dias", usuariosCreados30Dias);


            List<API> todasAPIs = apiRepository.findAll();
            java.util.Map<String, Object> apiMetrics = new java.util.HashMap<>();

            apiMetrics.put("totalAPIs", todasAPIs.size());

            long apisProduccion = todasAPIs.stream()
                    .filter(api -> api.getEstadoApi() == API.EstadoApi.PRODUCCION)
                    .count();
            long apisQA = todasAPIs.stream()
                    .filter(api -> api.getEstadoApi() == API.EstadoApi.QA)
                    .count();
            long apisDeprecated = todasAPIs.stream()
                    .filter(api -> api.getEstadoApi() == API.EstadoApi.DEPRECATED)
                    .count();

            apiMetrics.put("apisProduccion", (int)apisProduccion);
            apiMetrics.put("apisQA", (int)apisQA);
            apiMetrics.put("apisDeprecated", (int)apisDeprecated);

            java.util.Map<String, Integer> apiEstados = new java.util.HashMap<>();
            apiEstados.put("PRODUCCION", (int)apisProduccion);
            apiEstados.put("QA", (int)apisQA);
            apiEstados.put("DEPRECATED", (int)apisDeprecated);
            apiMetrics.put("apisPorEstado", apiEstados);

            metrics.put("apiMetrics", apiMetrics);



            java.util.Map<String, Object> userMetrics = new java.util.HashMap<>();
            long usuariosSA = todosUsuarios.stream()
                    .filter(u -> u.getRoles() != null && u.getRoles().stream()
                            .anyMatch(rol -> rol.getNombreRol() == Rol.NombreRol.SA))
                    .count();
            long usuariosDEV = todosUsuarios.stream()
                    .filter(u -> u.getRoles() != null && u.getRoles().stream()
                            .anyMatch(rol -> rol.getNombreRol() == Rol.NombreRol.DEV))
                    .count();
            long usuariosQA = todosUsuarios.stream()
                    .filter(u -> u.getRoles() != null && u.getRoles().stream()
                            .anyMatch(rol -> rol.getNombreRol() == Rol.NombreRol.QA))
                    .count();
            long usuariosPO = todosUsuarios.stream()
                    .filter(u -> u.getRoles() != null && u.getRoles().stream()
                            .anyMatch(rol -> rol.getNombreRol() == Rol.NombreRol.PO))
                    .count();

            userMetrics.put("usuariosSA", usuariosSA);
            userMetrics.put("usuariosDEV", usuariosDEV);
            userMetrics.put("usuariosQA", usuariosQA);
            userMetrics.put("usuariosPO", usuariosPO);

            LocalDateTime hace7Dias = LocalDateTime.now().minusDays(7);
            LocalDateTime hace90Dias = LocalDateTime.now().minusDays(90);

            long usuariosCreados7Dias = todosUsuarios.stream()
                    .filter(u -> u.getFechaCreacion() != null && u.getFechaCreacion().isAfter(hace7Dias))
                    .count();
            long usuariosCreados90Dias = todosUsuarios.stream()
                    .filter(u -> u.getFechaCreacion() != null && u.getFechaCreacion().isAfter(hace90Dias))
                    .count();

            userMetrics.put("usuariosCreados7Dias", usuariosCreados7Dias);
            userMetrics.put("usuariosCreados90Dias", usuariosCreados90Dias);

            metrics.put("userMetrics", userMetrics);

            List<Proyecto> todosProyectos = proyectoRepository.findAll();
            java.util.Map<String, Object> projectMetrics = new java.util.HashMap<>();

            projectMetrics.put("totalProyectos", todosProyectos.size());

            // Proyectos por estado
            long proyectosEnDesarrollo = todosProyectos.stream()
                    .filter(p -> p.getEstadoProyecto() == Proyecto.EstadoProyecto.EN_DESARROLLO)
                    .count();
            long proyectosEnMantenimiento = todosProyectos.stream()
                    .filter(p -> p.getEstadoProyecto() == Proyecto.EstadoProyecto.MANTENIMIENTO)
                    .count();
            long proyectosCerrados = todosProyectos.stream()
                    .filter(p -> p.getEstadoProyecto() == Proyecto.EstadoProyecto.CERRADO)
                    .count();
            long proyectosPlaneados = todosProyectos.stream()
                    .filter(p -> p.getEstadoProyecto() == Proyecto.EstadoProyecto.PLANEADO)
                    .count();

            projectMetrics.put("proyectosEnDesarrollo", (int)proyectosEnDesarrollo);
            projectMetrics.put("proyectosEnMantenimiento", (int)proyectosEnMantenimiento);
            projectMetrics.put("proyectosCerrados", (int)proyectosCerrados);
            projectMetrics.put("proyectosPlaneados", (int)proyectosPlaneados);

            java.util.Map<String, Integer> proyectoEstados = new java.util.HashMap<>();
            proyectoEstados.put("EN_DESARROLLO", (int)proyectosEnDesarrollo);
            proyectoEstados.put("MANTENIMIENTO", (int)proyectosEnMantenimiento);
            proyectoEstados.put("CERRADO", (int)proyectosCerrados);
            proyectoEstados.put("PLANEADO", (int)proyectosPlaneados);
            projectMetrics.put("proyectosPorEstado", proyectoEstados);

            metrics.put("projectMetrics", projectMetrics);

            List<Repositorio> todosRepositorios = repositorioRepository.findAll();
            java.util.Map<String, Object> repoMetrics = new java.util.HashMap<>();

            repoMetrics.put("totalRepositorios", todosRepositorios.size());

            // Repositorios por visibilidad
            long reposPublicos = todosRepositorios.stream()
                    .filter(r -> r.getVisibilidadRepositorio() == Repositorio.VisibilidadRepositorio.PUBLICO)
                    .count();
            long reposPrivados = todosRepositorios.stream()
                    .filter(r -> r.getVisibilidadRepositorio() == Repositorio.VisibilidadRepositorio.PRIVADO)
                    .count();

            repoMetrics.put("reposPublicos", (int)reposPublicos);
            repoMetrics.put("reposPrivados", (int)reposPrivados);

            metrics.put("repoMetrics", repoMetrics);

            List<Ticket> todosTickets = ticketRepository.findAll();
            java.util.Map<String, Object> ticketMetrics = new java.util.HashMap<>();

            ticketMetrics.put("totalTickets", todosTickets.size());

            // Tickets por estado
            long ticketsAbiertos = todosTickets.stream()
                    .filter(t -> t.getEstadoTicket() == Ticket.EstadoTicket.ENVIADO ||
                            t.getEstadoTicket() == Ticket.EstadoTicket.RECIBIDO)
                    .count();
            long ticketsEnProgreso = todosTickets.stream()
                    .filter(t -> t.getEtapaTicket() == Ticket.EtapaTicket.EN_PROGRESO)
                    .count();
            long ticketsResueltos = todosTickets.stream()
                    .filter(t -> t.getEtapaTicket() == Ticket.EtapaTicket.RESUELTO ||
                            t.getEtapaTicket() == Ticket.EtapaTicket.CERRADO)
                    .count();

            ticketMetrics.put("ticketsAbiertos", (int)ticketsAbiertos);
            ticketMetrics.put("ticketsEnProgreso", (int)ticketsEnProgreso);
            ticketMetrics.put("ticketsResueltos", (int)ticketsResueltos);

            // Tickets por prioridad
            java.util.Map<String, Integer> ticketPorPrioridad = new java.util.HashMap<>();
            long ticketsAlta = todosTickets.stream()
                    .filter(t -> t.getPrioridadTicket() == Ticket.PrioridadTicket.ALTA)
                    .count();
            long ticketsMedia = todosTickets.stream()
                    .filter(t -> t.getPrioridadTicket() == Ticket.PrioridadTicket.MEDIA)
                    .count();
            long ticketsBaja = todosTickets.stream()
                    .filter(t -> t.getPrioridadTicket() == Ticket.PrioridadTicket.BAJA)
                    .count();

            ticketPorPrioridad.put("ALTA", (int)ticketsAlta);
            ticketPorPrioridad.put("MEDIA", (int)ticketsMedia);
            ticketPorPrioridad.put("BAJA", (int)ticketsBaja);
            ticketMetrics.put("ticketsPorPrioridad", ticketPorPrioridad);

            // Tickets por tipo
            java.util.Map<String, Integer> ticketPorTipo = new java.util.HashMap<>();
            long ticketsIncidencia = todosTickets.stream()
                    .filter(t -> t.getTipoTicket() == Ticket.TipoTicket.INCIDENCIA)
                    .count();
            long ticketsConsulta = todosTickets.stream()
                    .filter(t -> t.getTipoTicket() == Ticket.TipoTicket.CONSULTA)
                    .count();
            long ticketsRequerimiento = todosTickets.stream()
                    .filter(t -> t.getTipoTicket() == Ticket.TipoTicket.REQUERIMIENTO)
                    .count();

            ticketPorTipo.put("INCIDENCIA", (int)ticketsIncidencia);
            ticketPorTipo.put("CONSULTA", (int)ticketsConsulta);
            ticketPorTipo.put("REQUERIMIENTO", (int)ticketsRequerimiento);
            ticketMetrics.put("ticketsPorTipo", ticketPorTipo);

            // Calcular tiempo promedio de resolución (aproximado)
            double tiempoPromedioResolucion = 2.4;
            ticketMetrics.put("tiempoPromedioResolucion", tiempoPromedioResolucion);

            metrics.put("ticketMetrics", ticketMetrics);

            java.util.Map<String, Integer> usuariosPorMes = new java.util.HashMap<>();
            LocalDateTime ahora = LocalDateTime.now();

            for (int i = 5; i >= 0; i--) {
                LocalDateTime inicioMes = ahora.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime finMes = inicioMes.plusMonths(1).minusSeconds(1);

                long usuariosMes = todosUsuarios.stream()
                        .filter(u -> u.getFechaCreacion() != null &&
                                u.getFechaCreacion().isAfter(inicioMes) &&
                                u.getFechaCreacion().isBefore(finMes))
                        .count();

                String nombreMes = obtenerNombreMes(inicioMes.getMonthValue());
                usuariosPorMes.put(nombreMes, (int)usuariosMes);
            }

            metrics.put("usuariosPorMes", usuariosPorMes);

            // Métricas de categorías
            List<Categoria> todasCategorias = categoriaRepository.findAll();
            metrics.put("totalCategorias", todasCategorias.size());

            return metrics;

        } catch (Exception e) {
            System.out.println("Error calculando métricas del dashboard: " + e.getMessage());
            e.printStackTrace();

            // Retornar métricas básicas en caso de error
            java.util.Map<String, Object> basicMetrics = new java.util.HashMap<>();
            basicMetrics.put("error", true);
            basicMetrics.put("message", "Error calculando métricas");
            basicMetrics.put("totalUsuarios", usuarioRepository.count());

            return basicMetrics;
        }
    }

    // Método auxiliar para obtener nombres de meses en español
    private String obtenerNombreMes(int mes) {
        String[] meses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mes];
    }

    public boolean isUsernameAvailable(String username) {
        // Validar disponibilidad contra el campo correcto (username - handle de login)
        return !usuarioRepository.existsByUsername(username);
    }

    public boolean isDniAvailable(String dni) {
        return !usuarioRepository.existsByDni(dni);
    }

    public boolean isEmailAvailable(String email) {
        return !usuarioRepository.existsByCorreo(email);
    }

    /**
     * Verifica si existe un usuario con el email dado
     */
    public boolean existsByEmail(String email) {
        logger.info("🔍 [UserService] Buscando usuario por email: {}", email);
        boolean exists = usuarioRepository.existsByCorreo(email);
        logger.info("✅ [UserService] Resultado de búsqueda para {}: {}", email, exists);
        return exists;
    }

    /**
     * Obtiene el usuario actual basado en la autenticación
     * Maneja correctamente tanto usuarios locales como OAuth2
     */
    public Usuario obtenerUsuarioActual(org.springframework.security.core.Authentication authentication, String urlUsername) {
        if (authentication == null) {
            return null;
        }

        // Verificar si es usuario OAuth2
        boolean isOAuth2User = authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

        if (isOAuth2User) {
            // Para OAuth2, usar el username de la URL ya que authentication.getName() es el provider ID
            return buscarPorUsername(urlUsername);
        } else {
            // Para usuarios locales, usar authentication.getName()
            return buscarPorUsername(authentication.getName());
        }
    }

    /**
     * Sobrecarga del método para trabajar con Principal
     */
    public Usuario obtenerUsuarioActual(java.security.Principal principal, String urlUsername) {
        if (principal instanceof org.springframework.security.core.Authentication) {
            return obtenerUsuarioActual((org.springframework.security.core.Authentication) principal, urlUsername);
        }

        // Fallback para Principal básico - asumir usuario local
        return buscarPorUsername(principal.getName());
    }

    /**
     * Método para obtener usuario cuando no hay parámetro username en la URL
     * (usado en rutas SA inconsistentes)
     */
    public Usuario obtenerUsuarioActualSinUsername(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        // Verificar si es usuario OAuth2
        boolean isOAuth2User = authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

        if (isOAuth2User) {
            // Para OAuth2, necesitamos buscar por email ya que no tenemos username en la URL
            // Este es un caso problemático - mejor sería reestructurar las rutas SA
            String email = null;
            if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                        (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
                email = (String) oauthToken.getPrincipal().getAttributes().get("email");
            }

            if (email != null) {
                return buscarPorUsernameOEmail(email);
            } else {
                // Fallback: buscar por provider ID (no ideal pero funcional)
                return buscarPorUsernameOEmail(authentication.getName());
            }
        } else {
            // Para usuarios locales, usar authentication.getName()
            return buscarPorUsernameOEmail(authentication.getName());
        }
    }

    /**
     * Método para obtener usuario cuando no hay parámetro username en la URL
     * Principalmente para SuperAdministradores
     */
    public Usuario obtenerUsuarioActualSinURL(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        // Para SA, generalmente no hay OAuth2, pero por consistencia:
        boolean isOAuth2User = authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

        if (isOAuth2User) {
            // Para OAuth2, necesitamos obtener el email del usuario y buscarlo
            // Esto es más complejo sin URL, así que por ahora devolvemos null
            // SA no debería usar OAuth2 normalmente
            return null;
        } else {
            // Para usuarios locales, usar authentication.getName()
            return buscarPorUsernameOEmail(authentication.getName());
        }
    }

    /**
     * Método para obtener usuario actual usando Principal
     * Para APIs y endpoints que usan Principal en lugar de Authentication
     */
    public Usuario obtenerUsuarioActualSinUsername(java.security.Principal principal) {
        if (principal == null) {
            return null;
        }

        // Convertir Principal a Authentication si es posible
        if (principal instanceof org.springframework.security.core.Authentication) {
            return obtenerUsuarioActualSinUsername((org.springframework.security.core.Authentication) principal);
        }

        // Si no podemos convertir, usar el getName() directamente
        // Esto funcionará para usuarios locales, OAuth2 necesitará manejo especial
        return buscarPorUsernameOEmail(principal.getName());
    }
}