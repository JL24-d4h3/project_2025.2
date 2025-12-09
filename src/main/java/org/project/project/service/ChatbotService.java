package org.project.project.service;

import lombok.RequiredArgsConstructor;
import org.project.project.model.entity.*;
import org.project.project.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatbotConversacionRepository conversacionRepository;
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioHasProyectoRepository usuarioHasProyectoRepository;
    private final UsuarioHasRepositorioRepository usuarioHasRepositorioRepository;
    private final ProyectoRepository proyectoRepository;
    private final RepositorioRepository repositorioRepository;
    private final APIRepository apiRepository;

    // =================== SESSION METHODS FOR CHATBOT ===================

    /**
     * Obtiene la sesión completa del usuario con todos sus datos relevantes
     * para el chatbot (proyectos, repositorios, APIs, tickets, estadísticas)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserCompleteSession(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Map<String, Object> session = new HashMap<>();

        // Datos básicos del usuario
        Map<String, Object> userData = new HashMap<>();
        userData.put("usuarioId", usuario.getUsuarioId());
        userData.put("username", usuario.getUsername());
        userData.put("nombreCompleto", usuario.getNombreUsuario() + " " +
                                      (usuario.getApellidoPaterno() != null ? usuario.getApellidoPaterno() : "") + " " +
                                      (usuario.getApellidoMaterno() != null ? usuario.getApellidoMaterno() : ""));
        userData.put("correo", usuario.getCorreo());
        userData.put("estadoUsuario", usuario.getEstadoUsuario());

        session.put("success", true);
        session.put("usuario", userData);

        // Obtener proyectos del usuario
        List<Map<String, Object>> proyectos = getUserProjects(userId);
        session.put("proyectos", proyectos);

        // Obtener repositorios del usuario
        List<Map<String, Object>> repositorios = getUserRepositories(userId);
        session.put("repositorios", repositorios);

        // Obtener APIs del usuario
        List<Map<String, Object>> apis = getUserApis(userId);
        session.put("apis", apis);

        // Obtener tickets del usuario
        List<Map<String, Object>> tickets = getUserTickets(userId);
        session.put("tickets", tickets);

        // Estadísticas
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalProyectos", proyectos.size());
        estadisticas.put("totalRepositorios", repositorios.size());
        estadisticas.put("totalApis", apis.size());
        estadisticas.put("ticketsPendientes", tickets.stream()
                .filter(t -> "ABIERTO".equals(String.valueOf(t.get("estado"))) || "EN_PROGRESO".equals(String.valueOf(t.get("estado"))))
                .count());
        estadisticas.put("totalTickets", tickets.size());

        session.put("estadisticas", estadisticas);

        return session;
    }

    /**
     * Obtiene la lista de proyectos del usuario con información detallada
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserProjects(Long userId) {
        List<UsuarioHasProyecto> userProjects = usuarioHasProyectoRepository.findById_UserId(userId);

        return userProjects.stream().map(uhp -> {
            Proyecto proyecto = uhp.getProyecto();
            Map<String, Object> projectData = new HashMap<>();
            projectData.put("proyectoId", proyecto.getProyectoId());
            projectData.put("nombreProyecto", proyecto.getNombreProyecto());
            projectData.put("descripcionProyecto", proyecto.getDescripcionProyecto());
            projectData.put("estadoProyecto", proyecto.getEstadoProyecto());
            projectData.put("visibilidadProyecto", proyecto.getVisibilidadProyecto());
            projectData.put("propietarioProyecto", proyecto.getPropietarioProyecto());
            projectData.put("privilegio", uhp.getPrivilegio());
            projectData.put("fechaInicio", proyecto.getFechaInicioProyecto());
            return projectData;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene la lista de repositorios del usuario con información detallada
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserRepositories(Long userId) {
        List<UsuarioHasRepositorio> userRepositories = usuarioHasRepositorioRepository.findById_UserId(userId);

        return userRepositories.stream().map(uhr -> {
            Repositorio repositorio = uhr.getRepositorio();
            Map<String, Object> repoData = new HashMap<>();
            repoData.put("repositorioId", repositorio.getRepositorioId());
            repoData.put("nombreRepositorio", repositorio.getNombreRepositorio());
            repoData.put("descripcionRepositorio", repositorio.getDescripcionRepositorio());
            repoData.put("visibilidadRepositorio", repositorio.getVisibilidadRepositorio());
            repoData.put("privilegio", uhr.getPrivilegio());
            repoData.put("fechaCreacion", repositorio.getFechaCreacion());
            return repoData;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene la lista de APIs del usuario con información detallada
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserApis(Long userId) {
        List<API> userApis = apiRepository.findDistinctApisByCreatedByUserId(userId);

        return userApis.stream().map(api -> {
            Map<String, Object> apiData = new HashMap<>();
            apiData.put("apiId", api.getApiId());
            apiData.put("nombreApi", api.getNombreApi());
            apiData.put("descripcionApi", api.getDescripcionApi());
            apiData.put("estadoApi", api.getEstadoApi());
            apiData.put("fechaCreacion", api.getCreadoEn());

            // Agregar información de versiones si existen
            if (api.getVersiones() != null) {
                apiData.put("totalVersiones", api.getVersiones().size());
            }

            return apiData;
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene la lista de tickets del usuario (reportados y asignados)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserTickets(Long userId) {
        List<Ticket> reportedTickets = ticketRepository.findByReportadoPor_UsuarioIdOrderByFechaCreacionDesc(userId);
        List<Ticket> assignedTickets = ticketRepository.findByAsignadoA_UsuarioIdOrderByFechaCreacionDesc(userId);

        // Combinar y eliminar duplicados
        Set<Ticket> allTickets = new HashSet<>();
        allTickets.addAll(reportedTickets);
        allTickets.addAll(assignedTickets);

        return allTickets.stream().map(ticket -> {
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("ticketId", ticket.getTicketId());
            ticketData.put("asunto", ticket.getAsuntoTicket());
            ticketData.put("estado", ticket.getEstadoTicket());
            ticketData.put("prioridad", ticket.getPrioridadTicket());
            ticketData.put("tipo", ticket.getTipoTicket());
            ticketData.put("fechaCreacion", ticket.getFechaCreacion());
            ticketData.put("reportadoPor", ticket.getReportadoPor() != null ? ticket.getReportadoPor().getUsername() : null);
            ticketData.put("asignadoA", ticket.getAsignadoA() != null ? ticket.getAsignadoA().getUsername() : null);

            if (ticket.getProyecto() != null) {
                ticketData.put("proyectoId", ticket.getProyecto().getProyectoId());
                ticketData.put("proyectoNombre", ticket.getProyecto().getNombreProyecto());
            }

            return ticketData;
        }).collect(Collectors.toList());
    }

    // =================== CONVERSATION MANAGEMENT ===================

    @Transactional
    public ChatbotConversacion createConversation(Long userId, String userMessage, String botResponse) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        ChatbotConversacion conversacion = new ChatbotConversacion();
        conversacion.setUsuario(usuario);
        conversacion.setMensajeUsuario(userMessage);
        conversacion.setRespuestaBot(botResponse);
        conversacion.setFechaConversacion(LocalDateTime.now());
        conversacion.setIntentoResuelto(false);
        conversacion.setEstadoConversacion(ChatbotConversacion.EstadoConversacion.ACTIVA);
        conversacion.setTemaConversacion(ChatbotConversacion.TemaConversacion.GENERAL);
        conversacion.setFechaInicio(LocalDateTime.now());
        conversacion.setFechaUltimoMensaje(LocalDateTime.now());
        conversacion.setMensajesCount(1);
        conversacion.setTokensTotalesUsados(0);
        conversacion.setCreadoEn(LocalDateTime.now());

        return conversacionRepository.save(conversacion);
    }

    @Transactional
    public ChatbotConversacion createConversationWithContext(Long userId, String userMessage, String botResponse, String context) {
        ChatbotConversacion conversacion = createConversation(userId, userMessage, botResponse);
        conversacion.setContexto(context);
        return conversacionRepository.save(conversacion);
    }

    @Transactional
    public ChatbotConversacion updateConversation(Long conversationId, String userMessage, String botResponse) {
        ChatbotConversacion conversacion = conversacionRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        conversacion.setMensajeUsuario(userMessage);
        conversacion.setRespuestaBot(botResponse);
        conversacion.setFechaUltimoMensaje(LocalDateTime.now());
        conversacion.setMensajesCount(conversacion.getMensajesCount() + 1);

        return conversacionRepository.save(conversacion);
    }

    @Transactional
    public ChatbotConversacion markAsResolved(Long conversationId) {
        ChatbotConversacion conversacion = conversacionRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        conversacion.setIntentoResuelto(true);
        return conversacionRepository.save(conversacion);
    }

    @Transactional
    public ChatbotConversacion closeConversation(Long conversationId) {
        ChatbotConversacion conversacion = conversacionRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        conversacion.setEstadoConversacion(ChatbotConversacion.EstadoConversacion.CERRADA);
        return conversacionRepository.save(conversacion);
    }

    @Transactional
    public ChatbotConversacion linkTicketToConversation(Long conversationId, Long ticketId) {
        ChatbotConversacion conversacion = conversacionRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        conversacion.setTicketGenerado(ticket);
        return conversacionRepository.save(conversacion);
    }

    // =================== QUERY METHODS ===================

    public Optional<ChatbotConversacion> findById(Long conversationId) {
        return conversacionRepository.findById(conversationId);
    }

    public List<ChatbotConversacion> findByUserId(Long userId) {
        return conversacionRepository.findByUsuario_UsuarioIdOrderByFechaConversacionDesc(userId);
    }

    public Page<ChatbotConversacion> findByUserId(Long userId, Pageable pageable) {
        return conversacionRepository.findByUsuario_UsuarioIdOrderByFechaConversacionDesc(userId, pageable);
    }

    public List<ChatbotConversacion> findByUser(Usuario usuario) {
        return conversacionRepository.findByUsuarioOrderByFechaConversacionDesc(usuario);
    }

    public Page<ChatbotConversacion> findByUser(Usuario usuario, Pageable pageable) {
        return conversacionRepository.findByUsuarioOrderByFechaConversacionDesc(usuario, pageable);
    }

    public List<ChatbotConversacion> findByStatus(ChatbotConversacion.EstadoConversacion status) {
        return conversacionRepository.findByEstadoConversacion(status);
    }

    public List<ChatbotConversacion> findByTopic(ChatbotConversacion.TemaConversacion topic) {
        return conversacionRepository.findByTemaConversacion(topic);
    }

    public List<ChatbotConversacion> findByUserAndStatus(Usuario usuario, ChatbotConversacion.EstadoConversacion status) {
        return conversacionRepository.findByUsuarioAndEstadoConversacion(usuario, status);
    }

    public List<ChatbotConversacion> findByUserAndTopic(Usuario usuario, ChatbotConversacion.TemaConversacion topic) {
        return conversacionRepository.findByUsuarioAndTemaConversacion(usuario, topic);
    }

    public List<ChatbotConversacion> findUnresolvedByUser(Usuario usuario) {
        return conversacionRepository.findByUsuarioAndIntentoResuelto(usuario, false);
    }

    public List<ChatbotConversacion> findResolvedByUser(Usuario usuario) {
        return conversacionRepository.findByUsuarioAndIntentoResuelto(usuario, true);
    }

    public List<ChatbotConversacion> findConversationsWithTickets() {
        return conversacionRepository.findByTicketGeneradoIsNotNull();
    }

    public Optional<ChatbotConversacion> findByTicketId(Long ticketId) {
        return conversacionRepository.findByTicketGenerado_TicketId(ticketId);
    }

    public List<ChatbotConversacion> searchByKeyword(String keyword) {
        return conversacionRepository.searchByKeyword(keyword);
    }

    public List<ChatbotConversacion> searchByUserAndKeyword(Long userId, String keyword) {
        return conversacionRepository.searchByUserAndKeyword(userId, keyword);
    }

    public List<ChatbotConversacion> findRecentByUserId(Long userId, Pageable pageable) {
        return conversacionRepository.findRecentByUserId(userId, pageable);
    }

    // =================== STATISTICS METHODS ===================

    public long countByUser(Usuario usuario) {
        return conversacionRepository.countByUsuario(usuario);
    }

    public long countByUserId(Long userId) {
        return conversacionRepository.countByUsuario_UsuarioId(userId);
    }

    public long countUnresolvedConversations() {
        return conversacionRepository.countByIntentoResueltoFalse();
    }

    public long countUnresolvedByUser(Usuario usuario) {
        return conversacionRepository.countByUsuarioAndIntentoResueltoFalse(usuario);
    }

    public long countByStatus(ChatbotConversacion.EstadoConversacion status) {
        return conversacionRepository.countByEstadoConversacion(status);
    }

    public long countRecentConversationsByUser(Long userId, LocalDateTime since) {
        return conversacionRepository.countRecentConversationsByUser(userId, since);
    }

    // =================== UTILITY METHODS ===================

    public List<ChatbotConversacion> findAll() {
        return conversacionRepository.findAll();
    }

    public Page<ChatbotConversacion> findAll(Pageable pageable) {
        return conversacionRepository.findAll(pageable);
    }

    @Transactional
    public void deleteConversation(Long conversationId) {
        conversacionRepository.deleteById(conversationId);
    }

    @Transactional
    public ChatbotConversacion updateTopic(Long conversationId, ChatbotConversacion.TemaConversacion topic) {
        ChatbotConversacion conversacion = conversacionRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        conversacion.setTemaConversacion(topic);
        return conversacionRepository.save(conversacion);
    }

    @Transactional
    public ChatbotConversacion updateTitle(Long conversationId, String title) {
        ChatbotConversacion conversacion = conversacionRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        conversacion.setTituloConversacion(title);
        return conversacionRepository.save(conversacion);
    }

    @Transactional
    public ChatbotConversacion archiveConversation(Long conversationId) {
        ChatbotConversacion conversacion = conversacionRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        conversacion.setEstadoConversacion(ChatbotConversacion.EstadoConversacion.ARCHIVADA);
        return conversacionRepository.save(conversacion);
    }

    // =================== AI-SPECIFIC METHODS ===================

    @Transactional
    public ChatbotConversacion updateTokenUsage(Long conversationId, int tokensUsed) {
        ChatbotConversacion conversacion = conversacionRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        conversacion.setTokensTotalesUsados(conversacion.getTokensTotalesUsados() + tokensUsed);
        return conversacionRepository.save(conversacion);
    }

    @Transactional
    public ChatbotConversacion setAiModel(Long conversationId, String modelName) {
        ChatbotConversacion conversacion = conversacionRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        conversacion.setModeloIaUsado(modelName);
        return conversacionRepository.save(conversacion);
    }

    @Transactional
    public ChatbotConversacion setNoSqlMessagesId(Long conversationId, String nosqlId) {
        ChatbotConversacion conversacion = conversacionRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        conversacion.setNosqlMensajesId(nosqlId);
        return conversacionRepository.save(conversacion);
    }
}
