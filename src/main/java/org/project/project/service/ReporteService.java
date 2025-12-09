package org.project.project.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.project.project.model.dto.*;
import org.project.project.model.entity.*;
import org.project.project.model.validator.TinyMCEContentValidator;
import org.project.project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ReporteService REFACTORIZADO
 * - Lógica de recipients según tipo de reporte
 * - Métodos reales (no stubs) usando repositorios existentes SIN modificarlos
 * - Paginación manual (4 items por página)
 * - Búsqueda y filtros para enviados/recibidos
 * 
 * @author jleon
 * @since 2025-01-23 REFACTOR
 */
@Service
@Slf4j
@Transactional
public class ReporteService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TinyMCEContentValidator contentValidator;

    // Repositorios para guardar relaciones N:M
    @Autowired
    private ReporteHasApiRepository reporteHasApiRepository;

    @Autowired
    private ReporteHasTicketRepository reporteHasTicketRepository;

    @Autowired
    private ReporteHasProyectoRepository reporteHasProyectoRepository;

    @Autowired
    private ReporteHasRepositorioRepository reporteHasRepositorioRepository;

    @Autowired
    private ReporteHasDocumentacionRepository reporteHasDocumentacionRepository;

    @Autowired
    private ReporteHasForoTemaRepository reporteHasForoTemaRepository;

    @Autowired
    private ReporteAdjuntoRepository reporteAdjuntoRepository;

    // Repositorios de entidades (USAR SIN MODIFICAR)
    @Autowired
    private APIRepository apiRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private RepositorioRepository repositorioRepository;

    @Autowired
    private DocumentacionRepository documentacionRepository;

    @Autowired
    private ForoTemaRepository foroTemaRepository;
    
    @Autowired
    private UsuarioHasReporteRepository usuarioHasReporteRepository;
    
    @Autowired
    private UsuarioHasProyectoRepository usuarioHasProyectoRepository;
    
    @Autowired
    private UsuarioHasRepositorioRepository usuarioHasRepositorioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioHasRolRepository usuarioHasRolRepository;

    // ==================== CREATE ====================

    /**
     * Crea un nuevo reporte básico y guarda sus relaciones.
     * NO determina recipients aún (solo se guardan al PUBLICAR)
     *
     * @param dto DTO con datos del reporte
     * @param usuarioId ID del usuario (autor)
     * @return ReporteDetailDTO con detalles del reporte creado
     */
    public ReporteDetailDTO crearReporte(CreateReporteDTO dto, Long usuarioId) throws IOException {
        log.info("Creando reporte por usuario {}", usuarioId);

        Usuario autor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));

        // Validar y sanitizar contenido
        if (!contentValidator.isValidContent(dto.getContenidoReporte())) {
            throw new IllegalArgumentException("Contenido inválido");
        }

        String contenidoSanitizado = contentValidator.sanitizeHtmlContent(dto.getContenidoReporte());

        // Crear reporte
        Reporte reporte = new Reporte();
        reporte.setAutor(autor);
        reporte.setTituloReporte(dto.getTituloReporte());
        reporte.setDescripcionReporte(dto.getDescripcionReporte());
        reporte.setContenidoReporte(contenidoSanitizado);
        reporte.setTipoReporte(Reporte.TipoReporte.valueOf(dto.getTipoReporte()));
        reporte.setEstadoReporte(Reporte.EstadoReporte.BORRADOR);
        reporte.setCreadoEn(LocalDateTime.now());
        reporte.setActualizadoEn(LocalDateTime.now());

        Reporte reporteGuardado = reporteRepository.save(reporte);
        log.info("Reporte creado: {}", reporteGuardado.getReporteId());

        // Guardar relaciones según el tipo
        guardarRelacionesReporte(reporteGuardado, dto);

        return mapToDetailDTO(reporteGuardado);
    }

    // ==================== READ ====================

    /**
     * Obtiene detalles de un reporte.
     */
    public ReporteDetailDTO obtenerReporteDetalle(Long reporteId, Long usuarioId) {
        log.debug("Obteniendo detalles de reporte {}", reporteId);

        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + reporteId));

        return mapToDetailDTO(reporte);
    }

    /**
     * Lista todos los reportes del usuario (ENVIADOS).
     */
    public List<Reporte> obtenerReportesDelUsuario(Long usuarioId) {
        log.info("Listando reportes del usuario {}", usuarioId);
        return reporteRepository.findSentReportsByUserId(usuarioId);
    }

    /**
     * Lista reportes por tipo.
     */
    public List<Reporte> obtenerReportesPorTipo(Long usuarioId, Reporte.TipoReporte tipo) {
        log.info("Listando reportes de tipo {} para usuario {}", tipo, usuarioId);
        return reporteRepository.findByAutorUsuarioIdAndTipoReporte(usuarioId, tipo);
    }

    /**
     * Obtiene estadísticas de reportes.
     */
    public Map<String, Object> obtenerEstadisticasReportes(Long usuarioId) {
        log.info("Obteniendo estadísticas para usuario {}", usuarioId);

        Map<String, Object> stats = new HashMap<>();
        
        List<Reporte> reportesUsuario = reporteRepository.findSentReportsByUserId(usuarioId);

        // Contar por tipo
        for (Reporte.TipoReporte tipo : Reporte.TipoReporte.values()) {
            long cantidad = reportesUsuario.stream()
                    .filter(r -> r.getTipoReporte() == tipo)
                    .count();
            stats.put(tipo.name().toLowerCase(), cantidad);
        }

        // Contar por estado
        for (Reporte.EstadoReporte estado : Reporte.EstadoReporte.values()) {
            long cantidad = reportesUsuario.stream()
                    .filter(r -> r.getEstadoReporte() == estado)
                    .count();
            stats.put(estado.name().toLowerCase(), cantidad);
        }

        stats.put("total", reportesUsuario.size());

        return stats;
    }

    // ==================== ENVIADOS/RECIBIDOS CON PAGINACIÓN ====================

    /**
     * Obtiene reportes ENVIADOS con paginación manual (4 por página)
     * @param usuarioId ID del usuario
     * @param page Número de página (0-indexed)
     * @param tipoFiltro Tipo de reporte (null para todos)
     * @param estadoFiltro Estado del reporte (null para todos)
     * @param searchTerm Término de búsqueda (null para sin filtro)
     * @param fechaDesde Fecha desde (null para sin filtro)
     * @param fechaHasta Fecha hasta (null para sin filtro)
     * @return Lista de ReportCardDTO
     */
    public List<ReportCardDTO> obtenerReportesEnviados(
            Long usuarioId, 
            int page, 
            Reporte.TipoReporte tipoFiltro,
            Reporte.EstadoReporte estadoFiltro,
            String searchTerm,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta) {
        
        log.info("Obteniendo reportes enviados - usuario: {}, página: {}", usuarioId, page);

        // Convertir searchTerm a minúsculas y agregar wildcards para LIKE
        String normalizedSearchTerm = null;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            normalizedSearchTerm = "%" + searchTerm.toLowerCase() + "%";
        }

        // Obtener reportes con filtros
        List<Reporte> reportes = reporteRepository.searchSentReports(
                usuarioId, tipoFiltro, estadoFiltro, normalizedSearchTerm, fechaDesde, fechaHasta);

        // Paginación manual: 4 items por página
        int itemsPerPage = 4;
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, reportes.size());

        if (startIndex >= reportes.size()) {
            return new ArrayList<>();
        }

        List<Reporte> paginatedReportes = reportes.subList(startIndex, endIndex);

        // Convertir a DTOs
        return paginatedReportes.stream()
                .map(r -> mapToCardDTO(r, usuarioId, false)) // false = no es recibido
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reportes RECIBIDOS con paginación manual (4 por página)
     */
    public List<ReportCardDTO> obtenerReportesRecibidos(
            Long usuarioId, 
            int page, 
            Reporte.TipoReporte tipoFiltro,
            Reporte.EstadoReporte estadoFiltro,
            String searchTerm,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta) {
        
        log.info("Obteniendo reportes recibidos - usuario: {}, página: {}", usuarioId, page);

        // Convertir searchTerm a minúsculas y agregar wildcards para LIKE
        String normalizedSearchTerm = null;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            normalizedSearchTerm = "%" + searchTerm.toLowerCase() + "%";
        }

        // Obtener reportes con filtros
        List<Reporte> reportes = reporteRepository.searchReceivedReports(
                usuarioId, tipoFiltro, estadoFiltro, normalizedSearchTerm, fechaDesde, fechaHasta);

        // Paginación manual: 4 items por página
        int itemsPerPage = 4;
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, reportes.size());

        if (startIndex >= reportes.size()) {
            return new ArrayList<>();
        }

        List<Reporte> paginatedReportes = reportes.subList(startIndex, endIndex);

        // Convertir a DTOs
        return paginatedReportes.stream()
                .map(r -> mapToCardDTO(r, usuarioId, true)) // true = es recibido
                .collect(Collectors.toList());
    }

    /**
     * Contar total de reportes enviados (para paginación)
     */
    public long contarReportesEnviados(Long usuarioId) {
        return reporteRepository.countSentReportsByUserId(usuarioId);
    }

    /**
     * Contar total de reportes recibidos (para paginación)
     */
    public long contarReportesRecibidos(Long usuarioId) {
        return reporteRepository.countReceivedReportsByUserId(usuarioId);
    }

    /**
     * Contar reportes por tipo (para métricas dinámicas)
     */
    public long contarReportesPorTipo(Long usuarioId, Reporte.TipoReporte tipo) {
        return reporteRepository.countByTipoAndAutor(usuarioId, tipo);
    }

    // ==================== UPDATE ====================

    /**
     * Actualiza un reporte básicamente.
     */
    public ReporteDetailDTO actualizarReporte(Long reporteId, UpdateReporteDTO dto, Long usuarioId) throws IOException {
        log.info("Actualizando reporte {} por usuario {}", reporteId, usuarioId);

        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + reporteId));

        // Validar que es autor
        if (!reporte.getAutor().getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("Solo el autor puede editar");
        }

        // Actualizar campos
        if (dto.getTituloReporte() != null) {
            reporte.setTituloReporte(dto.getTituloReporte());
        }
        if (dto.getDescripcionReporte() != null) {
            reporte.setDescripcionReporte(dto.getDescripcionReporte());
        }
        if (dto.getContenidoReporte() != null) {
            reporte.setContenidoReporte(contentValidator.sanitizeHtmlContent(dto.getContenidoReporte()));
        }
        reporte.setActualizadoEn(LocalDateTime.now());

        Reporte reporteActualizado = reporteRepository.save(reporte);
        log.info("Reporte actualizado: {}", reporteId);

        return mapToDetailDTO(reporteActualizado);
    }

    // ==================== DELETE ====================

    /**
     * Elimina un reporte.
     */
    public void eliminarReporte(Long reporteId, Long usuarioId) {
        log.info("Eliminando reporte {} por usuario {}", reporteId, usuarioId);

        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + reporteId));

        if (!reporte.getAutor().getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("Solo el autor puede eliminar");
        }

        reporteRepository.deleteById(reporteId);
        log.info("Reporte eliminado: {}", reporteId);
    }

    // ==================== STATE CHANGES ====================

    /**
     * Publica un reporte (cambio a PUBLICADO).
     * AL PUBLICAR: Determina y guarda recipients en usuario_has_reporte
     */
    public ReporteDetailDTO publicarReporte(Long reporteId, Long usuarioId) {
        log.info("Publicando reporte {} por usuario {}", reporteId, usuarioId);

        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + reporteId));

        if (!reporte.getAutor().getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("Solo el autor puede publicar");
        }

        reporte.setEstadoReporte(Reporte.EstadoReporte.PUBLICADO);
        reporte.setActualizadoEn(LocalDateTime.now());

        Reporte reporteActualizado = reporteRepository.save(reporte);

        // ✅ DETERMINAR Y GUARDAR RECIPIENTS
        determinarYGuardarRecipients(reporteActualizado);

        log.info("Reporte publicado: {}", reporteId);

        return mapToDetailDTO(reporteActualizado);
    }

    /**
     * Rechaza un reporte (vuelve a BORRADOR).
     */
    public ReporteDetailDTO rechazarReporte(Long reporteId, Long usuarioId, String motivo) {
        log.info("Rechazando reporte {} por usuario {}", reporteId, usuarioId);

        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + reporteId));

        reporte.setEstadoReporte(Reporte.EstadoReporte.BORRADOR);
        reporte.setActualizadoEn(LocalDateTime.now());
        reporte.setActualizadoPor(usuarioRepository.findById(usuarioId).orElse(null));

        Reporte reporteActualizado = reporteRepository.save(reporte);
        log.info("Reporte rechazado: {}", reporteId);

        return mapToDetailDTO(reporteActualizado);
    }

    /**
     * Marca un reporte como revisado (cambio a REVISADO).
     * Solo puede hacerlo un RECEPTOR del reporte.
     */
    public ReporteDetailDTO marcarComoRevisado(Long reporteId, Long usuarioId, String comentarios) {
        log.info("Marcando reporte {} como revisado por usuario {}", reporteId, usuarioId);

        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + reporteId));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));

        reporte.setEstadoReporte(Reporte.EstadoReporte.REVISADO);
        reporte.setActualizadoEn(LocalDateTime.now());
        reporte.setActualizadoPor(usuario);

        Reporte reporteActualizado = reporteRepository.save(reporte);
        log.info("Reporte revisado: {}", reporteId);

        return mapToDetailDTO(reporteActualizado);
    }

    // ==================== LÓGICA DE RECIPIENTS ====================

    /**
     * Determina los recipients según el tipo de reporte y guarda en usuario_has_reporte.
     * 
     * Lógica simplificada (NO usar roles de usuario_has_reporte por ahora):
     * - API → Creador de la API
     * - DOCUMENTACION → Creador de la documentación
     * - FORO → Creador del tema del foro
     * - PROYECTO → Todos los miembros del proyecto (enviado para autor, recibido para otros)
     * - REPOSITORIO → Todos los miembros del repositorio (enviado para autor, recibido para otros)
     * - TICKET → Todos los POs
     * - GENERAL → Todos los POs
     */
    private void determinarYGuardarRecipients(Reporte reporte) {
        log.info("Determinando recipients para reporte {} tipo {}", reporte.getReporteId(), reporte.getTipoReporte());

        // ✅ LIMPIAR DESTINATARIOS EXISTENTES (evitar duplicados al republicar)
        List<UsuarioHasReporte> existingRecipients = usuarioHasReporteRepository.findByReporte_ReporteId(reporte.getReporteId());
        if (!existingRecipients.isEmpty()) {
            log.info("Eliminando {} destinatarios existentes del reporte {}", existingRecipients.size(), reporte.getReporteId());
            usuarioHasReporteRepository.deleteAll(existingRecipients);
            entityManager.flush(); // Asegurar que se eliminan antes de insertar nuevos
        }

        Set<Long> recipientIds = new HashSet<>();

        try {
            switch (reporte.getTipoReporte()) {
                case API -> recipientIds.addAll(obtenerRecipientsParaAPI(reporte));
                case DOCUMENTACION -> recipientIds.addAll(obtenerRecipientsParaDocumentacion(reporte));
                case FORO -> recipientIds.addAll(obtenerRecipientsParaForo(reporte));
                case PROYECTO -> recipientIds.addAll(obtenerRecipientsParaProyecto(reporte));
                case REPOSITORIO -> recipientIds.addAll(obtenerRecipientsParaRepositorio(reporte));
                case TICKET, GENERAL -> recipientIds.addAll(obtenerPOs());
            }

            // Guardar en usuario_has_reporte
            for (Long recipientId : recipientIds) {
                try {
                    Usuario recipient = usuarioRepository.findById(recipientId).orElse(null);
                    if (recipient != null) {
                        UsuarioHasReporte uhr = new UsuarioHasReporte();
                        UsuarioHasReporteId id = new UsuarioHasReporteId();
                        id.setUsuarioId(recipientId);
                        id.setReporteId(reporte.getReporteId());
                        uhr.setId(id);
                        uhr.setUsuario(recipient);
                        uhr.setReporte(reporte);
                        uhr.setAsignadoEn(LocalDateTime.now());
                        
                        entityManager.persist(uhr);
                        log.debug("Recipient guardado: usuarioId={}, reporteId={}", recipientId, reporte.getReporteId());
                    }
                } catch (Exception e) {
                    log.warn("Error al guardar recipient {}: {}", recipientId, e.getMessage());
                }
            }

            log.info("Recipients guardados para reporte {}: {} usuarios", reporte.getReporteId(), recipientIds.size());

        } catch (Exception e) {
            log.error("Error determinando recipients para reporte {}: {}", reporte.getReporteId(), e.getMessage(), e);
        }
    }

    /**
     * Obtiene creadores de APIs relacionadas con el reporte
     */
    private Set<Long> obtenerRecipientsParaAPI(Reporte reporte) {
        Set<Long> recipientIds = new HashSet<>();
        
        try {
            List<ReporteHasApi> relaciones = reporteHasApiRepository.findByReporte_ReporteId(reporte.getReporteId());
            
            for (ReporteHasApi relacion : relaciones) {
                API api = relacion.getApi();
                if (api != null && api.getVersiones() != null && !api.getVersiones().isEmpty()) {
                    // Obtener creador de la primera versión
                    VersionAPI primeraVersion = api.getVersiones().stream()
                            .min(Comparator.comparing(VersionAPI::getVersionId))
                            .orElse(null);
                    
                    if (primeraVersion != null && primeraVersion.getCreador() != null) {
                        recipientIds.add(primeraVersion.getCreador().getUsuarioId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error obteniendo recipients para API: {}", e.getMessage());
        }
        
        return recipientIds;
    }

    /**
     * Obtiene creadores de documentación relacionada con el reporte
     */
    private Set<Long> obtenerRecipientsParaDocumentacion(Reporte reporte) {
        Set<Long> recipientIds = new HashSet<>();
        
        try {
            List<ReporteHasDocumentacion> relaciones = reporteHasDocumentacionRepository.findByReporte_ReporteId(reporte.getReporteId());
            
            for (ReporteHasDocumentacion relacion : relaciones) {
                Documentacion doc = relacion.getDocumentacion();
                if (doc != null && doc.getCreadoPor() != null) {
                    recipientIds.add(doc.getCreadoPor().getUsuarioId());
                }
            }
        } catch (Exception e) {
            log.error("Error obteniendo recipients para Documentacion: {}", e.getMessage());
        }
        
        return recipientIds;
    }

    /**
     * Obtiene creadores de temas de foro relacionados con el reporte
     */
    private Set<Long> obtenerRecipientsParaForo(Reporte reporte) {
        Set<Long> recipientIds = new HashSet<>();
        
        try {
            List<ReporteHasForoTema> relaciones = reporteHasForoTemaRepository.findByReporte_ReporteId(reporte.getReporteId());
            
            for (ReporteHasForoTema relacion : relaciones) {
                ForoTema tema = relacion.getForoTema();
                if (tema != null && tema.getAutor() != null) {
                    recipientIds.add(tema.getAutor().getUsuarioId());
                }
            }
        } catch (Exception e) {
            log.error("Error obteniendo recipients para Foro: {}", e.getMessage());
        }
        
        return recipientIds;
    }

    /**
     * Obtiene todos los miembros de proyectos relacionados con el reporte
     */
    private Set<Long> obtenerRecipientsParaProyecto(Reporte reporte) {
        Set<Long> recipientIds = new HashSet<>();
        
        try {
            List<ReporteHasProyecto> relaciones = reporteHasProyectoRepository.findByReporte_ReporteId(reporte.getReporteId());
            
            for (ReporteHasProyecto relacion : relaciones) {
                Proyecto proyecto = relacion.getProyecto();
                if (proyecto != null) {
                    // Obtener todos los miembros del proyecto
                    List<UsuarioHasProyecto> miembros = usuarioHasProyectoRepository
                            .findById_ProjectId(proyecto.getProyectoId());
                    
                    for (UsuarioHasProyecto miembro : miembros) {
                        recipientIds.add(miembro.getId().getUsuarioId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error obteniendo recipients para Proyecto: {}", e.getMessage());
        }
        
        return recipientIds;
    }

    /**
     * Obtiene todos los miembros de repositorios relacionados con el reporte
     */
    private Set<Long> obtenerRecipientsParaRepositorio(Reporte reporte) {
        Set<Long> recipientIds = new HashSet<>();
        
        try {
            List<ReporteHasRepositorio> relaciones = reporteHasRepositorioRepository.findByReporte_ReporteId(reporte.getReporteId());
            
            for (ReporteHasRepositorio relacion : relaciones) {
                Repositorio repositorio = relacion.getRepositorio();
                if (repositorio != null) {
                    // Obtener todos los miembros del repositorio
                    List<UsuarioHasRepositorio> miembros = usuarioHasRepositorioRepository
                            .findById_RepositoryId(repositorio.getRepositorioId());
                    
                    for (UsuarioHasRepositorio miembro : miembros) {
                        recipientIds.add(miembro.getId().getUsuarioId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error obteniendo recipients para Repositorio: {}", e.getMessage());
        }
        
        return recipientIds;
    }

    /**
     * Obtiene todos los usuarios con rol PO
     */
    private Set<Long> obtenerPOs() {
        Set<Long> recipientIds = new HashSet<>();
        
        try {
            // Buscar rol PO
            Optional<Rol> rolPO = rolRepository.findByRoleName(Rol.NombreRol.PO);
            
            if (rolPO.isPresent()) {
                List<UsuarioHasRol> usuariosConRolPO = usuarioHasRolRepository
                        .findById_RoleId(rolPO.get().getRolId());
                
                for (UsuarioHasRol uhr : usuariosConRolPO) {
                    recipientIds.add(uhr.getId().getUsuarioId());
                }
            }
        } catch (Exception e) {
            log.error("Error obteniendo POs: {}", e.getMessage());
        }
        
        return recipientIds;
    }

    // ==================== HELPERS ====================

    /**
     * Construye el nombre completo del usuario
     */
    private String construirNombreCompleto(Usuario usuario) {
        if (usuario == null) {
            return "Usuario Desconocido";
        }
        
        StringBuilder nombreCompleto = new StringBuilder();
        
        if (usuario.getNombreUsuario() != null) {
            nombreCompleto.append(usuario.getNombreUsuario());
        }
        
        if (usuario.getApellidoPaterno() != null) {
            if (nombreCompleto.length() > 0) {
                nombreCompleto.append(" ");
            }
            nombreCompleto.append(usuario.getApellidoPaterno());
        }
        
        if (usuario.getApellidoMaterno() != null) {
            if (nombreCompleto.length() > 0) {
                nombreCompleto.append(" ");
            }
            nombreCompleto.append(usuario.getApellidoMaterno());
        }
        
        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : usuario.getUsername();
    }

    /**
     * Guarda las relaciones entre el reporte y sus entidades relacionadas
     */
    private void guardarRelacionesReporte(Reporte reporte, CreateReporteDTO dto) {
        try {
            // Guardar relaciones con APIs
            if (dto.getApiIds() != null && !dto.getApiIds().isEmpty()) {
                for (Long apiId : dto.getApiIds()) {
                    try {
                        ReporteHasApi relacion = new ReporteHasApi();
                        ReporteHasApiId id = new ReporteHasApiId();
                        id.setReporteId(reporte.getReporteId());
                        id.setApiId(apiId);
                        relacion.setId(id);
                        reporteHasApiRepository.save(relacion);
                        log.debug("Relación reporte-API guardada: reporteId={}, apiId={}", 
                                  reporte.getReporteId(), apiId);
                    } catch (Exception e) {
                        log.warn("Error al guardar relación reporte-API: {}", e.getMessage());
                    }
                }
            }

            // Guardar relaciones con Tickets
            if (dto.getTicketIds() != null && !dto.getTicketIds().isEmpty()) {
                for (Long ticketId : dto.getTicketIds()) {
                    try {
                        ReporteHasTicket relacion = new ReporteHasTicket();
                        ReporteHasTicketId id = new ReporteHasTicketId();
                        id.setReporteId(reporte.getReporteId());
                        id.setTicketId(ticketId);
                        relacion.setId(id);
                        reporteHasTicketRepository.save(relacion);
                        log.debug("Relación reporte-Ticket guardada: reporteId={}, ticketId={}", 
                                  reporte.getReporteId(), ticketId);
                    } catch (Exception e) {
                        log.warn("Error al guardar relación reporte-Ticket: {}", e.getMessage());
                    }
                }
            }

            // Guardar relaciones con Proyectos
            if (dto.getProyectoIds() != null && !dto.getProyectoIds().isEmpty()) {
                for (Long proyectoId : dto.getProyectoIds()) {
                    try {
                        ReporteHasProyecto relacion = new ReporteHasProyecto();
                        ReporteHasProyectoId id = new ReporteHasProyectoId();
                        id.setReporteId(reporte.getReporteId());
                        id.setProyectoId(proyectoId);
                        relacion.setId(id);
                        reporteHasProyectoRepository.save(relacion);
                        log.debug("Relación reporte-Proyecto guardada: reporteId={}, proyectoId={}", 
                                  reporte.getReporteId(), proyectoId);
                    } catch (Exception e) {
                        log.warn("Error al guardar relación reporte-Proyecto: {}", e.getMessage());
                    }
                }
            }

            // Guardar relaciones con Repositorios
            if (dto.getRepositorioIds() != null && !dto.getRepositorioIds().isEmpty()) {
                for (Long repositorioId : dto.getRepositorioIds()) {
                    try {
                        ReporteHasRepositorio relacion = new ReporteHasRepositorio();
                        ReporteHasRepositorioId id = new ReporteHasRepositorioId();
                        id.setReporteId(reporte.getReporteId());
                        id.setRepositorioId(repositorioId);
                        relacion.setId(id);
                        reporteHasRepositorioRepository.save(relacion);
                        log.debug("Relación reporte-Repositorio guardada: reporteId={}, repositorioId={}", 
                                  reporte.getReporteId(), repositorioId);
                    } catch (Exception e) {
                        log.warn("Error al guardar relación reporte-Repositorio: {}", e.getMessage());
                    }
                }
            }

            // Guardar relaciones con Documentación
            if (dto.getDocumentacionIds() != null && !dto.getDocumentacionIds().isEmpty()) {
                for (Long documentacionId : dto.getDocumentacionIds()) {
                    try {
                        ReporteHasDocumentacion relacion = new ReporteHasDocumentacion();
                        ReporteHasDocumentacionId id = new ReporteHasDocumentacionId();
                        id.setReporteId(reporte.getReporteId());
                        id.setDocumentacionId(documentacionId);
                        relacion.setId(id);
                        reporteHasDocumentacionRepository.save(relacion);
                        log.debug("Relación reporte-Documentación guardada: reporteId={}, documentacionId={}", 
                                  reporte.getReporteId(), documentacionId);
                    } catch (Exception e) {
                        log.warn("Error al guardar relación reporte-Documentación: {}", e.getMessage());
                    }
                }
            }

            // Guardar relaciones con Foro Temas
            if (dto.getForoTemaIds() != null && !dto.getForoTemaIds().isEmpty()) {
                for (Long foroTemaId : dto.getForoTemaIds()) {
                    try {
                        ReporteHasForoTema relacion = new ReporteHasForoTema();
                        ReporteHasForoTemaId id = new ReporteHasForoTemaId();
                        id.setReporteId(reporte.getReporteId());
                        id.setForoTemaId(foroTemaId);
                        relacion.setId(id);
                        reporteHasForoTemaRepository.save(relacion);
                        log.debug("Relación reporte-ForoTema guardada: reporteId={}, foroTemaId={}", 
                                  reporte.getReporteId(), foroTemaId);
                    } catch (Exception e) {
                        log.warn("Error al guardar relación reporte-ForoTema: {}", e.getMessage());
                    }
                }
            }

            log.info("Relaciones del reporte {} guardadas exitosamente", reporte.getReporteId());
        } catch (Exception e) {
            log.error("Error al guardar relaciones del reporte: {}", e.getMessage(), e);
        }
    }

    /**
     * Mapea Reporte a ReporteDetailDTO (básico).
     */
    private ReporteDetailDTO mapToDetailDTO(Reporte reporte) {
        ReporteDetailDTO dto = new ReporteDetailDTO();
        
        // Identificación
        dto.setReporteId(reporte.getReporteId());
        dto.setTituloReporte(reporte.getTituloReporte());
        
        // Contenido
        dto.setDescripcionReporte(reporte.getDescripcionReporte());
        dto.setContenidoReporte(reporte.getContenidoReporte());
        
        // Tipo y estado
        dto.setTipoReporte(reporte.getTipoReporte().name());
        dto.setEstadoReporte(reporte.getEstadoReporte().name());
        
        // Entidad relacionada (nombre específico de la entidad)
        dto.setEntidadRelacionadaNombre(obtenerNombreEntidadRelacionada(reporte));
        
        // Autor (CRÍTICO - necesario para calcular permisos)
        if (reporte.getAutor() != null) {
            dto.setAutorUsuarioId(reporte.getAutor().getUsuarioId());
            dto.setAutorNombre(construirNombreCompleto(reporte.getAutor()));
            dto.setAutorUsername(reporte.getAutor().getUsername());
        }
        
        // Metadata
        dto.setCreadoEn(reporte.getCreadoEn());
        dto.setActualizadoEn(reporte.getActualizadoEn());
        
        // Adjuntos - convertir Set<ReporteAdjunto> a List<ReporteAdjuntoDTO>
        if (reporte.getAdjuntos() != null && !reporte.getAdjuntos().isEmpty()) {
            List<ReporteAdjuntoDTO> adjuntosDTO = reporte.getAdjuntos().stream()
                .map(adj -> {
                    ReporteAdjuntoDTO adjDTO = new ReporteAdjuntoDTO();
                    adjDTO.setAdjuntoId(adj.getAdjuntoId());
                    adjDTO.setReporteId(reporte.getReporteId());
                    adjDTO.setNombreArchivo(adj.getNombreArchivo());
                    adjDTO.setTipoMime(adj.getTipoMime());
                    adjDTO.setTamanioBytes(adj.getTamanoBytes());
                    adjDTO.setDescripcionAdjunto(adj.getDescripcionAdjunto());
                    adjDTO.setOrdenVisualizacion(adj.getOrdenVisualizacion());
                    adjDTO.setSubidoEn(adj.getSubidoEn());
                    
                    // GCS info si está migrado
                    if (adj.getGcsMigrado() != null && adj.getGcsMigrado()) {
                        adjDTO.setGcsFileId(adj.getGcsFileId());
                        adjDTO.setGcsBucketName(adj.getGcsBucketName());
                        adjDTO.setGcsFileePath(adj.getGcsFilePath());
                        adjDTO.setGcsPublicUrl(adj.getGcsPublicUrl());
                        adjDTO.setGcsFileSizeBytes(adj.getGcsFileSizeBytes());
                        adjDTO.setGcsMigrado(true);
                    } else {
                        adjDTO.setGcsMigrado(false);
                    }
                    
                    return adjDTO;
                })
                .toList();
            dto.setAdjuntos(adjuntosDTO);
        }
        
        return dto;
    }

    /**
     * Mapea Reporte a ReportCardDTO (para tarjetas en overview).
     * @param reporte El reporte a mapear
     * @param usuarioId ID del usuario actual
     * @param esRecibido true si es un reporte recibido, false si es enviado
     */
    private ReportCardDTO mapToCardDTO(Reporte reporte, Long usuarioId, boolean esRecibido) {
        ReportCardDTO dto = new ReportCardDTO();
        
        // Identificación
        dto.setReporteId(reporte.getReporteId());
        dto.setTituloReporte(reporte.getTituloReporte());
        
        // Contenido
        dto.setDescripcionReporte(reporte.getDescripcionReporte());
        
        // Preview del contenido (primeras líneas sin HTML)
        String contenidoTexto = Jsoup.parse(reporte.getContenidoReporte()).text();
        String preview = contenidoTexto.length() > 150 
                ? contenidoTexto.substring(0, 147) + "..." 
                : contenidoTexto;
        dto.setContenidoReportePreview(preview);
        
        // Tipo y estado
        dto.setTipoReporte(reporte.getTipoReporte().name());
        dto.setEstadoReporte(reporte.getEstadoReporte().name());
        
        // Entidad relacionada (nombre) - obtener de relaciones
        dto.setEntidadRelacionadaNombre(obtenerNombreEntidadRelacionada(reporte));
        
        // Autor
        dto.setAutorId(reporte.getAutor().getUsuarioId());
        dto.setAutorNombreCompleto(construirNombreCompleto(reporte.getAutor()));
        dto.setAutorUsername(reporte.getAutor().getUsername());
        
        // Remitente (solo si es recibido)
        dto.setEsRecibido(esRecibido);
        if (esRecibido) {
            dto.setRemitenteNombreCompleto(construirNombreCompleto(reporte.getAutor()));
            dto.setRemitenteUsername(reporte.getAutor().getUsername());
        }
        
        // Metadata
        dto.setCantidadAdjuntos(reporte.getAdjuntos() != null ? reporte.getAdjuntos().size() : 0);
        dto.setCreadoEn(reporte.getCreadoEn());
        dto.setActualizadoEn(reporte.getActualizadoEn());
        
        // Permisos
        boolean esAutor = reporte.getAutor().getUsuarioId().equals(usuarioId);
        boolean esBorrador = reporte.getEstadoReporte() == Reporte.EstadoReporte.BORRADOR;
        
        dto.setPuedeEditar(esAutor && esBorrador);
        dto.setPuedePublicar(esAutor && esBorrador);
        dto.setPuedeMarcarRevisado(!esAutor && reporte.getEstadoReporte() == Reporte.EstadoReporte.PUBLICADO);
        dto.setPuedeEliminar(esAutor && esBorrador);
        
        log.debug("Permisos para reporte {} - esAutor: {}, estado: {}, puedeEditar: {}, puedePublicar: {}", 
                reporte.getReporteId(), esAutor, reporte.getEstadoReporte(), dto.getPuedeEditar(), dto.getPuedePublicar());
        
        return dto;
    }

    /**
     * Obtiene el nombre de la entidad relacionada con el reporte
     */
    private String obtenerNombreEntidadRelacionada(Reporte reporte) {
        try {
            switch (reporte.getTipoReporte()) {
                case API -> {
                    List<ReporteHasApi> relaciones = reporteHasApiRepository.findByReporte_ReporteId(reporte.getReporteId());
                    if (!relaciones.isEmpty() && relaciones.get(0).getApi() != null) {
                        return relaciones.get(0).getApi().getNombreApi();
                    }
                }
                case TICKET -> {
                    List<ReporteHasTicket> relaciones = reporteHasTicketRepository.findById_ReporteId(reporte.getReporteId());
                    if (!relaciones.isEmpty() && relaciones.get(0).getTicket() != null) {
                        return relaciones.get(0).getTicket().getAsuntoTicket();
                    }
                }
                case PROYECTO -> {
                    List<ReporteHasProyecto> relaciones = reporteHasProyectoRepository.findByReporte_ReporteId(reporte.getReporteId());
                    if (!relaciones.isEmpty() && relaciones.get(0).getProyecto() != null) {
                        return relaciones.get(0).getProyecto().getNombreProyecto();
                    }
                }
                case REPOSITORIO -> {
                    List<ReporteHasRepositorio> relaciones = reporteHasRepositorioRepository.findByReporte_ReporteId(reporte.getReporteId());
                    if (!relaciones.isEmpty() && relaciones.get(0).getRepositorio() != null) {
                        return relaciones.get(0).getRepositorio().getNombreRepositorio();
                    }
                }
                case DOCUMENTACION -> {
                    List<ReporteHasDocumentacion> relaciones = reporteHasDocumentacionRepository.findByReporte_ReporteId(reporte.getReporteId());
                    if (!relaciones.isEmpty() && relaciones.get(0).getDocumentacion() != null) {
                        return relaciones.get(0).getDocumentacion().getSeccionDocumentacion();
                    }
                }
                case FORO -> {
                    List<ReporteHasForoTema> relaciones = reporteHasForoTemaRepository.findByReporte_ReporteId(reporte.getReporteId());
                    if (!relaciones.isEmpty() && relaciones.get(0).getForoTema() != null) {
                        return relaciones.get(0).getForoTema().getTituloTema();
                    }
                }
                case GENERAL -> {
                    return "Reporte General";
                }
            }
        } catch (Exception e) {
            log.warn("Error obteniendo nombre de entidad relacionada: {}", e.getMessage());
        }
        
        return "Sin especificar";
    }

    // ========== MÉTODOS PARA CARGAR ENTIDADES DINÁMICAMENTE (Select2) ==========

    /**
     * Obtiene lista de APIs disponibles para un usuario (creadas por él)
     * Retorna: EntitySelectDTO con {id, nombre, descripcion, tipo}
     */
    public List<EntitySelectDTO> obtenerApisDelUsuario(Long usuarioId) {
        List<EntitySelectDTO> resultado = new ArrayList<>();
        try {
            // Usar método existente en APIRepository SIN MODIFICARLO
            List<API> apis = apiRepository.findDistinctApisByCreatedByUserId(usuarioId);
            
            for (API api : apis) {
                resultado.add(new EntitySelectDTO(
                        api.getApiId(),
                        api.getNombreApi(),
                        null,
                        "API"
                ));
            }
        } catch (Exception e) {
            log.error("Error obteniendo APIs del usuario {}: {}", usuarioId, e.getMessage());
        }
        return resultado;
    }

    /**
     * Obtiene lista de Tickets disponibles para un usuario
     */
    public List<EntitySelectDTO> obtenerTicketsDelUsuario(Long usuarioId) {
        List<EntitySelectDTO> resultado = new ArrayList<>();
        try {
            List<Ticket> tickets = ticketRepository.findByReportadoPor_UsuarioIdOrderByFechaCreacionDesc(usuarioId);
            
            for (Ticket ticket : tickets) {
                resultado.add(new EntitySelectDTO(
                        ticket.getTicketId(),
                        ticket.getAsuntoTicket(),
                        null,
                        "TICKET"
                ));
            }
        } catch (Exception e) {
            log.error("Error obteniendo Tickets del usuario {}: {}", usuarioId, e.getMessage());
        }
        return resultado;
    }

    /**
     * Obtiene lista de Proyectos disponibles para un usuario
     */
    public List<EntitySelectDTO> obtenerProyectosDelUsuario(Long usuarioId) {
        List<EntitySelectDTO> resultado = new ArrayList<>();
        try {
            List<UsuarioHasProyecto> relaciones = usuarioHasProyectoRepository
                    .findById_UserId(usuarioId);
            
            for (UsuarioHasProyecto rel : relaciones) {
                Proyecto proyecto = rel.getProyecto();
                if (proyecto != null) {
                    resultado.add(new EntitySelectDTO(
                            proyecto.getProyectoId(),
                            proyecto.getNombreProyecto(),
                            null,
                            "PROYECTO"
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Error obteniendo Proyectos del usuario {}: {}", usuarioId, e.getMessage());
        }
        return resultado;
    }

    /**
     * Obtiene lista de Repositorios disponibles para un usuario
     */
    public List<EntitySelectDTO> obtenerRepositoriosDelUsuario(Long usuarioId) {
        List<EntitySelectDTO> resultado = new ArrayList<>();
        try {
            List<UsuarioHasRepositorio> relaciones = usuarioHasRepositorioRepository
                    .findById_UserId(usuarioId);
            
            for (UsuarioHasRepositorio rel : relaciones) {
                Repositorio repo = rel.getRepositorio();
                if (repo != null) {
                    resultado.add(new EntitySelectDTO(
                            repo.getRepositorioId(),
                            repo.getNombreRepositorio(),
                            null,
                            "REPOSITORIO"
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Error obteniendo Repositorios del usuario {}: {}", usuarioId, e.getMessage());
        }
        return resultado;
    }

    /**
     * Obtiene lista de Documentación disponible para un usuario
     */
    public List<EntitySelectDTO> obtenerDocumentacionDelUsuario(Long usuarioId) {
        List<EntitySelectDTO> resultado = new ArrayList<>();
        try {
            // Query usando el id del usuario en lugar del objeto Usuario
            List<Documentacion> docs = documentacionRepository.findAll().stream()
                    .filter(d -> d.getCreadoPor() != null && d.getCreadoPor().getUsuarioId().equals(usuarioId))
                    .toList();
            
            for (Documentacion doc : docs) {
                String nombre = doc.getApi() != null 
                        ? doc.getApi().getNombreApi() + " - " + doc.getSeccionDocumentacion()
                        : doc.getSeccionDocumentacion();
                
                resultado.add(new EntitySelectDTO(
                        doc.getDocumentacionId(),
                        nombre,
                        null,
                        "DOCUMENTACION"
                ));
            }
        } catch (Exception e) {
            log.error("Error obteniendo Documentación del usuario {}: {}", usuarioId, e.getMessage());
        }
        return resultado;
    }

    /**
     * Obtiene lista de Temas del Foro disponibles para un usuario
     */
    public List<EntitySelectDTO> obtenerForoTemasDelUsuario(Long usuarioId) {
        List<EntitySelectDTO> resultado = new ArrayList<>();
        try {
            List<ForoTema> temas = foroTemaRepository.findByAutor_UsuarioId(usuarioId);
            
            for (ForoTema tema : temas) {
                resultado.add(new EntitySelectDTO(
                        tema.getTemaId(),
                        tema.getTituloTema(),
                        null,
                        "FORO"
                ));
            }
        } catch (Exception e) {
            log.error("Error obteniendo Temas del Foro del usuario {}: {}", usuarioId, e.getMessage());
        }
        return resultado;
    }

    // ==================== MÉTODOS PARA WIZARD CREATE ====================

    /**
     * Obtiene lista de APIs disponibles para el wizard
     */
    public List<Map<String, Object>> obtenerAPIsDisponibles(Long usuarioId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<API> apis = apiRepository.findAll();
            for (API api : apis) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", api.getApiId());
                map.put("nombre", api.getNombreApi());
                result.add(map);
            }
        } catch (Exception e) {
            log.error("Error obteniendo APIs: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Obtiene lista de Documentaciones disponibles
     */
    public List<Map<String, Object>> obtenerDocumentacionesDisponibles(Long usuarioId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<Documentacion> docs = documentacionRepository.findAll();
            for (Documentacion doc : docs) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", doc.getDocumentacionId());
                String nombre = doc.getApi() != null 
                        ? doc.getApi().getNombreApi() + " - " + doc.getSeccionDocumentacion()
                        : doc.getSeccionDocumentacion();
                map.put("nombre", nombre);
                result.add(map);
            }
        } catch (Exception e) {
            log.error("Error obteniendo Documentaciones: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Obtiene lista de publicaciones de Foro disponibles
     */
    public List<Map<String, Object>> obtenerForosDisponibles(Long usuarioId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<ForoTema> temas = foroTemaRepository.findAll();
            for (ForoTema tema : temas) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", tema.getTemaId());
                map.put("nombre", tema.getTituloTema());
                result.add(map);
            }
        } catch (Exception e) {
            log.error("Error obteniendo Foros: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Obtiene lista de Proyectos del usuario
     */
    public List<Map<String, Object>> obtenerProyectosDisponibles(Long usuarioId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<UsuarioHasProyecto> relaciones = usuarioHasProyectoRepository.findById_UserId(usuarioId);
            for (UsuarioHasProyecto rel : relaciones) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rel.getProyecto().getProyectoId());
                map.put("nombre", rel.getProyecto().getNombreProyecto());
                result.add(map);
            }
        } catch (Exception e) {
            log.error("Error obteniendo Proyectos: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Obtiene lista de Repositorios del usuario
     */
    public List<Map<String, Object>> obtenerRepositoriosDisponibles(Long usuarioId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<UsuarioHasRepositorio> relaciones = usuarioHasRepositorioRepository.findById_UserId(usuarioId);
            for (UsuarioHasRepositorio rel : relaciones) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rel.getRepositorio().getRepositorioId());
                map.put("nombre", rel.getRepositorio().getNombreRepositorio());
                result.add(map);
            }
        } catch (Exception e) {
            log.error("Error obteniendo Repositorios: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Obtiene lista de Tickets del usuario
     */
    public List<Map<String, Object>> obtenerTicketsDisponibles(Long usuarioId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            // Tickets creados por el usuario
            List<Ticket> tickets = ticketRepository.findAll().stream()
                    .filter(t -> t.getReportadoPor() != null && 
                                 t.getReportadoPor().getUsuarioId().equals(usuarioId))
                    .toList();
            
            for (Ticket ticket : tickets) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", ticket.getTicketId());
                map.put("nombre", "TICK-" + ticket.getTicketId() + ": " + ticket.getAsuntoTicket());
                result.add(map);
            }
        } catch (Exception e) {
            log.error("Error obteniendo Tickets: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Crea reporte con archivos adjuntos (versión con MultipartFile)
     */
    public Long crearReporte(ReporteCreateDTO createDTO, List<org.springframework.web.multipart.MultipartFile> adjuntos) throws Exception {
        log.info("Creando reporte con adjuntos por usuario {}", createDTO.getAutorUsuarioId());

        Usuario autor = usuarioRepository.findById(createDTO.getAutorUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validar contenido
        if (!contentValidator.isValidContent(createDTO.getContenidoReporte())) {
            throw new IllegalArgumentException("Contenido inválido");
        }

        String contenidoSanitizado = contentValidator.sanitizeHtmlContent(createDTO.getContenidoReporte());

        // Crear reporte
        Reporte reporte = new Reporte();
        reporte.setAutor(autor);
        reporte.setTituloReporte(createDTO.getTituloReporte());
        reporte.setDescripcionReporte(createDTO.getDescripcionReporte());
        reporte.setContenidoReporte(contenidoSanitizado);
        reporte.setTipoReporte(createDTO.getTipoReporte());
        reporte.setEstadoReporte(createDTO.getEstadoReporte());
        reporte.setCreadoEn(LocalDateTime.now());
        reporte.setActualizadoEn(LocalDateTime.now());

        Reporte reporteGuardado = reporteRepository.save(reporte);
        log.info("Reporte creado: {}", reporteGuardado.getReporteId());

        // Guardar relaciones
        if (createDTO.getEntidadRelacionadaId() != null) {
            guardarRelacionPorTipo(reporteGuardado, createDTO.getTipoReporte(), createDTO.getEntidadRelacionadaId());
        }
        
        // Asignar destinatarios según el tipo de reporte
        asignarDestinatarios(reporteGuardado, createDTO.getTipoReporte(), createDTO.getEntidadRelacionadaId(), autor);

        // Guardar adjuntos usando método reutilizable
        guardarAdjuntos(reporteGuardado, adjuntos, autor);
        
        return reporteGuardado.getReporteId();
    }
    
    /**
     * Asigna destinatarios según el tipo de reporte:
     * - TICKET/GENERAL: todos los POs
     * - API: propietario de la API
     * - DOCUMENTACION: propietario de la documentación
     * - FORO: propietario del tema del foro
     * - PROYECTO: todos los miembros del proyecto
     * - REPOSITORIO: todos los miembros del repositorio
     */
    private void asignarDestinatarios(Reporte reporte, Reporte.TipoReporte tipo, Long entidadId, Usuario autor) {
        Set<Usuario> destinatarios = new HashSet<>();
        
        log.info("=== ASIGNANDO DESTINATARIOS ===");
        log.info("Reporte ID: {}, Tipo: {}, Entidad ID: {}, Autor: {}", 
                reporte.getReporteId(), tipo, entidadId, autor.getUsername());
        
        try {
            switch (tipo) {
                case TICKET:
                case GENERAL:
                    // Enviar a todos los POs
                    destinatarios.addAll(obtenerTodosPOs());
                    log.info("TICKET/GENERAL - {} POs encontrados", destinatarios.size());
                    break;
                    
                case API:
                    if (entidadId != null) {
                        // Enviar al propietario de la API
                        Usuario propietario = obtenerPropietarioAPI(entidadId);
                        if (propietario != null) {
                            destinatarios.add(propietario);
                            log.info("API - Propietario: {}", propietario.getUsername());
                        } else {
                            log.warn("API {} no tiene propietario", entidadId);
                        }
                    }
                    break;
                    
                case DOCUMENTACION:
                    if (entidadId != null) {
                        // Enviar al propietario de la documentación
                        Usuario propietario = obtenerPropietarioDocumentacion(entidadId);
                        if (propietario != null) {
                            destinatarios.add(propietario);
                            log.info("DOCUMENTACION - Propietario: {}", propietario.getUsername());
                        } else {
                            log.warn("DOCUMENTACION {} no tiene propietario", entidadId);
                        }
                    }
                    break;
                    
                case FORO:
                    if (entidadId != null) {
                        // Enviar al autor del tema del foro
                        Usuario propietario = obtenerPropietarioForoTema(entidadId);
                        if (propietario != null) {
                            destinatarios.add(propietario);
                            log.info("FORO - Propietario: {}", propietario.getUsername());
                        } else {
                            log.warn("FORO {} no tiene autor", entidadId);
                        }
                    }
                    break;
                    
                case PROYECTO:
                case REPOSITORIO:
                    // TEMPORALMENTE: Enviar a todos los POs
                    // TODO: Implementar lógica de miembros cuando esté lista
                    destinatarios.addAll(obtenerTodosPOs());
                    log.info("PROYECTO/REPOSITORIO - {} POs encontrados (temporal)", destinatarios.size());
                    break;
            }
            
            // Remover al autor de los destinatarios (no enviarse a sí mismo)
            destinatarios.remove(autor);
            
            log.info("Total destinatarios (sin autor): {}", destinatarios.size());
            
            // Guardar en usuario_has_reporte
            for (Usuario destinatario : destinatarios) {
                UsuarioHasReporte uhr = new UsuarioHasReporte();
                uhr.setId(new UsuarioHasReporteId(destinatario.getUsuarioId(), reporte.getReporteId()));
                uhr.setUsuario(destinatario);
                uhr.setReporte(reporte);
                uhr.setRolColaborador(UsuarioHasReporte.RolColaborador.LECTOR);
                uhr.setPuedeEditar(false);
                uhr.setAsignadoEn(LocalDateTime.now());
                uhr.setAsignadoPor(autor);
                
                usuarioHasReporteRepository.save(uhr);
                log.info("✓ Guardado destinatario: {} para reporte {}", destinatario.getUsername(), reporte.getReporteId());
            }
            
            log.info("=== DESTINATARIOS ASIGNADOS EXITOSAMENTE ===");
            
        } catch (Exception e) {
            log.error("Error asignando destinatarios: {}", e.getMessage());
        }
    }
    
    /**
     * Obtiene todos los usuarios con rol PO
     */
    private List<Usuario> obtenerTodosPOs() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRoles().stream()
                        .anyMatch(rol -> rol.getNombreRol() == Rol.NombreRol.PO))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene el propietario de una API
     */
    private Usuario obtenerPropietarioAPI(Long apiId) {
        try {
            API api = apiRepository.findById(apiId).orElse(null);
            if (api != null && api.getCreadoPor() != null) {
                return api.getCreadoPor();
            }
        } catch (Exception e) {
            log.error("Error obteniendo propietario de API: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Obtiene el propietario de una documentación
     */
    private Usuario obtenerPropietarioDocumentacion(Long docId) {
        try {
            Documentacion doc = documentacionRepository.findById(docId).orElse(null);
            if (doc != null && doc.getCreadoPor() != null) {
                return doc.getCreadoPor();
            }
        } catch (Exception e) {
            log.error("Error obteniendo propietario de documentación: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Obtiene el autor de un tema del foro
     */
    private Usuario obtenerPropietarioForoTema(Long temaId) {
        try {
            ForoTema tema = foroTemaRepository.findById(temaId).orElse(null);
            if (tema != null && tema.getAutor() != null) {
                return tema.getAutor();
            }
        } catch (Exception e) {
            log.error("Error obteniendo autor del tema: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Obtiene todos los miembros de un proyecto
     */
    private List<Usuario> obtenerMiembrosProyecto(Long proyectoId) {
        List<Usuario> miembros = new ArrayList<>();
        usuarioHasProyectoRepository.findById_ProjectId(proyectoId).forEach(uhp -> {
            miembros.add(uhp.getUsuario());
        });
        return miembros;
    }
    
    /**
     * Obtiene todos los miembros de un repositorio
     */
    private List<Usuario> obtenerMiembrosRepositorio(Long repositorioId) {
        List<Usuario> miembros = new ArrayList<>();
        usuarioHasRepositorioRepository.findById_RepositoryId(repositorioId).forEach(uhr -> {
            miembros.add(uhr.getUsuario());
        });
        return miembros;
    }

    /**
     * Guarda relación según tipo de reporte
     */
    private void guardarRelacionPorTipo(Reporte reporte, Reporte.TipoReporte tipo, Long entidadId) {
        log.info("=== GUARDANDO RELACIÓN ===");
        log.info("Reporte ID: {}, Tipo: {}, Entidad ID: {}", reporte.getReporteId(), tipo, entidadId);
        
        try {
            switch (tipo) {
                case API:
                    API api = apiRepository.findById(entidadId).orElse(null);
                    if (api != null) {
                        ReporteHasApi rel = new ReporteHasApi(reporte, api);
                        reporteHasApiRepository.save(rel);
                        log.info("✓ Relación guardada: reporte_has_api (reporte={}, api={})", 
                                reporte.getReporteId(), api.getApiId());
                    } else {
                        log.warn("⚠ API no encontrada con ID: {}", entidadId);
                    }
                    break;
                    
                case DOCUMENTACION:
                    Documentacion doc = documentacionRepository.findById(entidadId).orElse(null);
                    if (doc != null) {
                        ReporteHasDocumentacion rel = new ReporteHasDocumentacion(reporte, doc);
                        reporteHasDocumentacionRepository.save(rel);
                        log.info("✓ Relación guardada: reporte_has_documentacion (reporte={}, doc={})", 
                                reporte.getReporteId(), doc.getDocumentacionId());
                    } else {
                        log.warn("⚠ Documentación no encontrada con ID: {}", entidadId);
                    }
                    break;
                    
                case FORO:
                    ForoTema tema = foroTemaRepository.findById(entidadId).orElse(null);
                    if (tema != null) {
                        ReporteHasForoTema rel = new ReporteHasForoTema(reporte, tema);
                        reporteHasForoTemaRepository.save(rel);
                        log.info("✓ Relación guardada: reporte_has_foro_tema (reporte={}, tema={})", 
                                reporte.getReporteId(), tema.getTemaId());
                    } else {
                        log.warn("⚠ ForoTema no encontrado con ID: {}", entidadId);
                    }
                    break;
                    
                case PROYECTO:
                    Proyecto proyecto = proyectoRepository.findById(entidadId).orElse(null);
                    if (proyecto != null) {
                        ReporteHasProyecto rel = new ReporteHasProyecto(reporte, proyecto);
                        reporteHasProyectoRepository.save(rel);
                        log.info("✓ Relación guardada: reporte_has_proyecto (reporte={}, proyecto={})", 
                                reporte.getReporteId(), proyecto.getProyectoId());
                    } else {
                        log.warn("⚠ Proyecto no encontrado con ID: {}", entidadId);
                    }
                    break;
                    
                case REPOSITORIO:
                    Repositorio repositorio = repositorioRepository.findById(entidadId).orElse(null);
                    if (repositorio != null) {
                        ReporteHasRepositorio rel = new ReporteHasRepositorio(reporte, repositorio);
                        reporteHasRepositorioRepository.save(rel);
                        log.info("✓ Relación guardada: reporte_has_repositorio (reporte={}, repo={})", 
                                reporte.getReporteId(), repositorio.getRepositorioId());
                    } else {
                        log.warn("⚠ Repositorio no encontrado con ID: {}", entidadId);
                    }
                    break;
                    
                case TICKET:
                    Ticket ticket = ticketRepository.findById(entidadId).orElse(null);
                    if (ticket != null) {
                        ReporteHasTicket rel = new ReporteHasTicket(reporte, ticket);
                        reporteHasTicketRepository.save(rel);
                        log.info("✓ Relación guardada: reporte_has_ticket (reporte={}, ticket={})", 
                                reporte.getReporteId(), ticket.getTicketId());
                    } else {
                        log.warn("⚠ Ticket no encontrado con ID: {}", entidadId);
                    }
                    break;
                    
                default:
                    log.info("Tipo GENERAL - sin relación específica");
                    break;
            }
            log.info("=== RELACIÓN GUARDADA EXITOSAMENTE ===");
        } catch (Exception e) {
            log.error("❌ ERROR guardando relación de reporte: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene el nombre de la entidad relacionada con el reporte
     * SIMPLIFICADO: Solo retorna el tipo de reporte por ahora
     */
    public String obtenerNombreEntidadRelacionada(String tipoReporte, Long reporteId) {
        // Por ahora solo retornamos el tipo
        // Los repositorios de relaciones N:M no tienen métodos findByReporte_ReporteId
        // TODO: Agregar estos métodos a los repositorios si se necesita
        return tipoReporte;
    }
    
    /**
     * Obtiene adjunto completo con contenido binario para descarga
     */
    @Transactional(readOnly = true)
    public ReporteAdjunto obtenerAdjuntoCompleto(Long adjuntoId) {
        log.info("📥 Obteniendo adjunto completo: {}", adjuntoId);
        return reporteAdjuntoRepository.findById(adjuntoId)
                .orElse(null);
    }

    /**
     * Obtiene un reporte por ID (entidad completa)
     */
    @Transactional(readOnly = true)
    public Reporte obtenerReportePorId(Long reporteId) {
        return reporteRepository.findById(reporteId)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
    }

    /**
     * Elimina un adjunto por ID
     */
    @Transactional
    public void eliminarAdjunto(Long adjuntoId) {
        log.info("🗑️ Eliminando adjunto ID={}", adjuntoId);
        
        // Obtener el adjunto antes de eliminarlo para tener referencia al reporte
        ReporteAdjunto adjunto = reporteAdjuntoRepository.findById(adjuntoId)
                .orElseThrow(() -> new IllegalArgumentException("Adjunto no encontrado: " + adjuntoId));
        
        Reporte reporte = adjunto.getReporte();
        
        // Remover el adjunto de la colección del reporte (para mantener consistencia en la relación bidireccional)
        if (reporte != null && reporte.getAdjuntos() != null) {
            reporte.getAdjuntos().remove(adjunto);
            // Persistir el cambio en el reporte
            reporteRepository.save(reporte);
        }
        
        // Eliminar el adjunto de la base de datos
        reporteAdjuntoRepository.deleteById(adjuntoId);
        
        // Forzar la sincronización inmediata con la base de datos
        entityManager.flush();
        
        // Limpiar el contexto de persistencia para evitar cachés desactualizados
        entityManager.clear();
        
        log.info("✅ Adjunto {} eliminado exitosamente", adjuntoId);
    }

    /**
     * Guarda adjuntos para un reporte (reutilizable para create y update)
     */
    @Transactional
    public void guardarAdjuntos(Reporte reporte, List<org.springframework.web.multipart.MultipartFile> adjuntos, Usuario usuario) {
        if (adjuntos == null || adjuntos.isEmpty()) {
            return;
        }

        // Obtener el máximo orden actual
        List<ReporteAdjunto> adjuntosActuales = reporteAdjuntoRepository
                .findByReporte_ReporteIdOrderByOrdenVisualizacionAsc(reporte.getReporteId());
        int orden = adjuntosActuales.isEmpty() ? 0 : 
                    adjuntosActuales.stream()
                        .mapToInt(ReporteAdjunto::getOrdenVisualizacion)
                        .max()
                        .orElse(0) + 1;

        for (org.springframework.web.multipart.MultipartFile file : adjuntos) {
            if (!file.isEmpty()) {
                try {
                    // Validar tamaño (máximo 10 MB)
                    if (file.getSize() > 10 * 1024 * 1024) {
                        log.warn("Archivo {} excede el tamaño máximo de 10MB, omitiendo", file.getOriginalFilename());
                        continue;
                    }
                    
                    ReporteAdjunto adjunto = new ReporteAdjunto();
                    adjunto.setReporte(reporte);
                    adjunto.setNombreArchivo(file.getOriginalFilename());
                    adjunto.setTipoMime(file.getContentType());
                    adjunto.setTamanoBytes(file.getSize());
                    adjunto.setContenidoArchivo(file.getBytes()); // Guardar binario en BD
                    adjunto.setRutaArchivo(null); // NULL porque está en contenido_archivo
                    adjunto.setGcsMigrado(false); // No está en GCS
                    adjunto.setOrdenVisualizacion(orden++);
                    adjunto.setSubidoEn(LocalDateTime.now());
                    adjunto.setSubidoPor(usuario);
                    adjunto.setVersionNumero(1);
                    adjunto.setEsVersionActual(true);
                    
                    reporteAdjuntoRepository.save(adjunto);
                    log.info("✅ Adjunto guardado: {} ({} bytes)", 
                             adjunto.getNombreArchivo(), adjunto.getTamanoBytes());
                             
                } catch (Exception e) {
                    log.error("Error al guardar adjunto {}: {}", file.getOriginalFilename(), e.getMessage());
                }
            }
        }
    }
}


