package org.project.project.controller;

import org.project.project.model.dto.*;
import org.project.project.model.entity.Reporte;
import org.project.project.model.entity.ReporteAdjunto;
import org.project.project.model.entity.Rol;
import org.project.project.model.entity.Usuario;
import org.project.project.service.GoogleCloudStorageService;
import org.project.project.service.MetricsConversionService;
import org.project.project.service.ReporteService;
import org.project.project.service.ReportePdfExportService;
import org.project.project.service.ReportExportService;
import org.project.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * ReportController - Controlador para gestión de reportes
 * 
 * ESTRUCTURA DE RUTAS:
 * - /devportal/{role}/{username}/reports → Vista principal (overview con tabs)
 * - /devportal/{role}/{username}/reports/sent → Tab de reportes enviados
 * - /devportal/{role}/{username}/reports/received → Tab de reportes recibidos
 * - /devportal/{role}/{username}/reports/create → Wizard de creación
 * - /devportal/{role}/{username}/reports/{id} → Detalle de reporte
 * - /devportal/{role}/{username}/reports/{id}/edit → Editar reporte
 * 
 * @author DevPortal Team
 * @since 2025-11-06
 */
@Slf4j
@Controller
@RequestMapping("/devportal/{role}/{username}")
public class ReportController {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private UserService userService;

    @Autowired
    private GoogleCloudStorageService gcsService;

    @Autowired
    private ReportExportService reportExportService;

    @Autowired
    private MetricsConversionService metricsConversionService;

    @Autowired
    private ReportePdfExportService reportePdfExportService;

    // ========== UTILITY METHODS ==========

    /**
     * Obtiene el usuario desde username y valida rol
     */
    private Usuario obtenerUsuario(String username) throws Exception {
        Usuario usuario = userService.buscarPorUsername(username);
        if (usuario == null) {
            throw new Exception("Usuario no encontrado: " + username);
        }
        return usuario;
    }

    // ========== OVERVIEW (VISTA PRINCIPAL) ==========

    /**
     * GET /devportal/{role}/{username}/reports
     * Redirige a la vista de reportes enviados por defecto
     */
    @GetMapping("/reports")
    public String overview(
            @PathVariable String role,
            @PathVariable String username) {
        log.info("Redirigiendo a reportes enviados para usuario: {}", username);
        return "redirect:/devportal/" + role + "/" + username + "/reports/sent";
    }

    /**
     * GET /devportal/{role}/{username}/reports/sent
     * Tab de reportes enviados (activeTab = "sent")
     */
    @GetMapping("/reports/sent")
    public String sentReports(
            @PathVariable String role,
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String search,
            Model model) {
        try {
            log.info("Accediendo a reportes enviados para usuario: {} (page: {})", username, page);
            
            Usuario usuario = obtenerUsuario(username);
            
            // Convertir filtros
            Reporte.TipoReporte tipoFiltro = null;
            Reporte.EstadoReporte estadoFiltro = null;
            
            if (tipo != null && !tipo.isEmpty()) {
                try { tipoFiltro = Reporte.TipoReporte.valueOf(tipo); } catch (Exception e) {}
            }
            if (estado != null && !estado.isEmpty()) {
                try { estadoFiltro = Reporte.EstadoReporte.valueOf(estado); } catch (Exception e) {}
            }
            
            // Obtener reportes enviados paginados (4 por página)
            List<ReportCardDTO> reports = reporteService.obtenerReportesEnviados(
                    usuario.getUsuarioId(), page, tipoFiltro, estadoFiltro, search, null, null);
            
            long totalElements = reporteService.contarReportesEnviados(usuario.getUsuarioId());
            int totalPages = (int) Math.ceil((double) totalElements / 4.0);
            
            // Contar reportes para las métricas en tabs
            long sentCount = reporteService.contarReportesEnviados(usuario.getUsuarioId());
            long receivedCount = reporteService.contarReportesRecibidos(usuario.getUsuarioId());
            
            // Agregar datos al modelo
            model.addAttribute("Usuario", usuario);
            model.addAttribute("rol", role);
            model.addAttribute("username", username);
            model.addAttribute("activeTab", "sent");
            model.addAttribute("reports", reports);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("sentCount", sentCount);
            model.addAttribute("receivedCount", receivedCount);
            model.addAttribute("tipoFiltro", tipoFiltro);
            model.addAttribute("estadoFiltro", estadoFiltro);
            model.addAttribute("searchTerm", search);
            model.addAttribute("currentNavSection", "reports");
            
            return "report/overview";
            
        } catch (Exception e) {
            log.error("Error al cargar reportes enviados: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar reportes enviados");
            return "error/500";
        }
    }

    /**
     * GET /devportal/{role}/{username}/reports/received
     * Tab de reportes recibidos (activeTab = "received")
     */
    @GetMapping("/reports/received")
    public String receivedReports(
            @PathVariable String role,
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String search,
            Model model) {
        try {
            log.info("Accediendo a reportes recibidos para usuario: {} (page: {})", username, page);
            
            Usuario usuario = obtenerUsuario(username);
            
            // Convertir filtros
            Reporte.TipoReporte tipoFiltro = null;
            Reporte.EstadoReporte estadoFiltro = null;
            
            if (tipo != null && !tipo.isEmpty()) {
                try { tipoFiltro = Reporte.TipoReporte.valueOf(tipo); } catch (Exception e) {}
            }
            if (estado != null && !estado.isEmpty()) {
                try { estadoFiltro = Reporte.EstadoReporte.valueOf(estado); } catch (Exception e) {}
            }
            
            // Obtener reportes recibidos paginados (4 por página)
            List<ReportCardDTO> reports = reporteService.obtenerReportesRecibidos(
                    usuario.getUsuarioId(), page, tipoFiltro, estadoFiltro, search, null, null);
            
            long totalElements = reporteService.contarReportesRecibidos(usuario.getUsuarioId());
            int totalPages = (int) Math.ceil((double) totalElements / 4.0);
            
            // Contar reportes para las métricas en tabs
            long sentCount = reporteService.contarReportesEnviados(usuario.getUsuarioId());
            long receivedCount = reporteService.contarReportesRecibidos(usuario.getUsuarioId());
            
            // Agregar datos al modelo
            model.addAttribute("Usuario", usuario);
            model.addAttribute("rol", role);
            model.addAttribute("username", username);
            model.addAttribute("activeTab", "received");
            model.addAttribute("reports", reports);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("sentCount", sentCount);
            model.addAttribute("receivedCount", receivedCount);
            model.addAttribute("tipoFiltro", tipoFiltro);
            model.addAttribute("estadoFiltro", estadoFiltro);
            model.addAttribute("searchTerm", search);
            model.addAttribute("currentNavSection", "reports");
            
            return "report/overview";
            
        } catch (Exception e) {
            log.error("Error al cargar reportes recibidos: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar reportes recibidos");
            return "error/500";
        }
    }

    // ========== API ENDPOINTS (JSON) ==========
    // Endpoints API eliminados - ahora se usa la vista con tabs directamente

    // ========== CREATE WIZARD (VISTA) ==========

    /**
     * GET /devportal/{role}/{username}/reports/create
     * Mostrar formulario wizard de creación de reporte (4 pasos)
     */
    @GetMapping("/reports/create")
    public String showCreateForm(
            @PathVariable String role,
            @PathVariable String username,
            @RequestParam(required = false) String tipo,
            Model model) {
        try {
            log.info("Mostrando wizard de creación para usuario: {}", username);
            
            Usuario usuario = obtenerUsuario(username);

            model.addAttribute("Usuario", usuario);
            model.addAttribute("rol", role);
            model.addAttribute("username", username);
            model.addAttribute("currentUser", usuario);
            model.addAttribute("currentNavSection", "reports");
            
            // Si hay tipo preseleccionado, agregarlo
            if (tipo != null && !tipo.isEmpty()) {
                try {
                    Reporte.TipoReporte.valueOf(tipo.toUpperCase());
                    model.addAttribute("tipoPreseleccionado", tipo.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Tipo de reporte inválido: {}", tipo);
                }
            }
            
            return "report/create";
            
        } catch (Exception e) {
            log.error("Error al mostrar wizard de creación: {}", e.getMessage(), e);
            return "redirect:/error";
        }
    }

    /**
     * POST /devportal/{role}/{username}/reports/create
     * Guardar nuevo reporte (borrador o publicado)
     */
    @PostMapping("/reports/create")
    @ResponseBody
    public ResponseEntity<?> createReport(
            @PathVariable String role,
            @PathVariable String username,
            @RequestParam String tipoReporte,
            @RequestParam String tituloReporte,
            @RequestParam String descripcionReporte,
            @RequestParam String contenidoReporte,
            @RequestParam String estadoReporte,
            @RequestParam(required = false) Long entidadId, // Frontend envía "entidadId"
            @RequestParam(required = false) String descripcionAdjuntos,
            @RequestParam(required = false) List<MultipartFile> adjuntos) {
        try {
            Usuario usuario = obtenerUsuario(username);
            
            log.info("📝 [CREATE REPORT] Tipo: {}, EntidadId recibido: {}", tipoReporte, entidadId);
            
            // Crear reporte
            ReporteCreateDTO createDTO = new ReporteCreateDTO();
            createDTO.setTipoReporte(Reporte.TipoReporte.valueOf(tipoReporte));
            createDTO.setTituloReporte(tituloReporte);
            createDTO.setDescripcionReporte(descripcionReporte);
            createDTO.setContenidoReporte(contenidoReporte);
            createDTO.setEstadoReporte(Reporte.EstadoReporte.valueOf(estadoReporte));
            createDTO.setAutorUsuarioId(usuario.getUsuarioId());
            createDTO.setEntidadRelacionadaId(entidadId); // Mapear a entidadRelacionadaId del DTO
            createDTO.setDescripcionAdjuntos(descripcionAdjuntos);
            
            // Guardar reporte
            Long reporteId = reporteService.crearReporte(createDTO, adjuntos);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Reporte creado exitosamente",
                    "reporteId", reporteId
            ));
            
        } catch (Exception e) {
            log.error("Error al crear reporte: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * GET /devportal/{role}/{username}/reports/entities/{tipo}
     * Obtener entidades según tipo de reporte
     */
    @GetMapping("/reports/entities/{tipo}")
    @ResponseBody
    public ResponseEntity<?> getEntitiesByType(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable String tipo) {
        try {
            Usuario usuario = obtenerUsuario(username);
            
            List<Map<String, Object>> entities = new ArrayList<>();
            
            switch (tipo.toUpperCase()) {
                case "API":
                    // Obtener APIs del usuario
                    entities = reporteService.obtenerAPIsDisponibles(usuario.getUsuarioId());
                    break;
                case "DOCUMENTACION":
                    entities = reporteService.obtenerDocumentacionesDisponibles(usuario.getUsuarioId());
                    break;
                case "FORO":
                    entities = reporteService.obtenerForosDisponibles(usuario.getUsuarioId());
                    break;
                case "PROYECTO":
                    entities = reporteService.obtenerProyectosDisponibles(usuario.getUsuarioId());
                    break;
                case "REPOSITORIO":
                    entities = reporteService.obtenerRepositoriosDisponibles(usuario.getUsuarioId());
                    break;
                case "TICKET":
                    entities = reporteService.obtenerTicketsDisponibles(usuario.getUsuarioId());
                    break;
                default:
                    entities = new ArrayList<>();
            }
            
            return ResponseEntity.ok(entities);
            
        } catch (Exception e) {
            log.error("Error al obtener entidades: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ArrayList<>());
        }
    }

    // ========== DETAIL (VISTA) ==========

    /**
     * GET /devportal/{role}/{username}/reports/detail/{reporteId}
     * Muestra la vista de detalle de un reporte
     */
    @GetMapping("/reports/detail/{reporteId:\\d+}")
    public String showDetailView(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable Long reporteId,
            Model model) {
        try {
            log.info("Mostrando detalle de reporte {} para usuario: {}", reporteId, username);
            
            Usuario usuario = obtenerUsuario(username);
            
            // Obtener detalle del reporte
            ReporteDetailDTO detalle = reporteService.obtenerReporteDetalle(reporteId, usuario.getUsuarioId());
            
            if (detalle == null) {
                return "redirect:/error?msg=Reporte no encontrado";
            }
            
            // Calcular permisos
            boolean esAutor = detalle.getAutorUsuarioId().equals(usuario.getUsuarioId());
            boolean esPO = usuario.getRoles().stream()
                    .anyMatch(rol -> rol.getNombreRol() == Rol.NombreRol.PO);
            boolean esBorrador = "BORRADOR".equals(detalle.getEstadoReporte());
            boolean esPublicado = "PUBLICADO".equals(detalle.getEstadoReporte());
            
            boolean puedeEditar = esAutor && esBorrador;
            boolean puedePublicar = esAutor && esBorrador;
            boolean puedeMarcarRevisado = esPO && esPublicado;
            boolean puedeRechazar = esPO && esPublicado;
            boolean puedeEliminar = esAutor && esBorrador;

            model.addAttribute("Usuario", usuario);
            model.addAttribute("rol", role);
            model.addAttribute("username", username);
            model.addAttribute("currentUser", usuario);
            model.addAttribute("currentNavSection", "reports");
            model.addAttribute("reporte", detalle);
            model.addAttribute("puedeEditar", puedeEditar);
            model.addAttribute("puedePublicar", puedePublicar);
            model.addAttribute("puedeMarcarRevisado", puedeMarcarRevisado);
            model.addAttribute("puedeRechazar", puedeRechazar);
            model.addAttribute("puedeEliminar", puedeEliminar);
            model.addAttribute("adjuntos", detalle.getAdjuntos());
            
            // Agregar nombre de entidad relacionada
            String entidadNombre = reporteService.obtenerNombreEntidadRelacionada(
                    detalle.getTipoReporte(), 
                    detalle.getReporteId()
            );
            model.addAttribute("entidadRelacionada", entidadNombre);
            
            return "report/detail";
            
        } catch (Exception e) {
            log.error("Error al mostrar detalle de reporte: {}", e.getMessage(), e);
            return "redirect:/error";
        }
    }

    // ========== EDIT (VISTA) ==========

    /**
     * GET /devportal/{role}/{username}/reports/edit/{reporteId}
     * Muestra la vista de edición de un reporte
     */
    @GetMapping("/reports/edit/{reporteId:\\d+}")
    public String showEditView(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable Long reporteId,
            Model model) {
        try {
            log.info("Mostrando edición de reporte {} para usuario: {}", reporteId, username);
            
            Usuario usuario = obtenerUsuario(username);
            
            // Obtener detalle del reporte
            ReporteDetailDTO detalle = reporteService.obtenerReporteDetalle(reporteId, usuario.getUsuarioId());
            
            if (detalle == null) {
                return "redirect:/error?msg=Reporte no encontrado";
            }

            model.addAttribute("Usuario", usuario);
            model.addAttribute("rol", role);
            model.addAttribute("username", username);
            model.addAttribute("currentUser", usuario);
            model.addAttribute("currentNavSection", "reports");
            model.addAttribute("reporte", detalle);
            
            // Agregar nombre de entidad relacionada
            String entidadNombre = reporteService.obtenerNombreEntidadRelacionada(
                    detalle.getTipoReporte(), 
                    detalle.getReporteId()
            );
            model.addAttribute("entidadRelacionada", entidadNombre);
            
            return "report/edit";
            
        } catch (Exception e) {
            log.error("Error al mostrar edición de reporte: {}", e.getMessage(), e);
            return "redirect:/error";
        }
    }

    // ========== REST API ENDPOINTS (CRUD) ==========

    /**
     * POST /devportal/{role}/{username}/reports
     * Crear nuevo reporte con adjuntos
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> crearReporte(
            @PathVariable String username,
            @RequestBody CreateReporteDTO createDTO) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Usuario {} creando reporte de tipo {}", username, createDTO.getTipoReporte());

            // Crear reporte
            ReporteDetailDTO nuevoReporte = reporteService.crearReporte(createDTO, usuario.getUsuarioId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "id", nuevoReporte.getReporteId(),
                            "mensaje", "Reporte creado exitosamente",
                            "reporte", nuevoReporte
                    ));
        } catch (Exception e) {
            log.error("Error creando reporte", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /devportal/{role}/{username}/reports/{reporteId}
     * Obtener detalles de un reporte específico (REST API)
     */
    @GetMapping("/reports/{reporteId:\\d+}")
    @ResponseBody
    public ResponseEntity<?> obtenerDetalleReporte(
            @PathVariable String username,
            @PathVariable Long reporteId) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Usuario {} solicitando detalle de reporte {}", username, reporteId);

            ReporteDetailDTO detalle = reporteService.obtenerReporteDetalle(reporteId, usuario.getUsuarioId());
            if (detalle == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(detalle);
        } catch (Exception e) {
            log.error("Error obteniendo detalle de reporte", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /devportal/{role}/{username}/reports/{reporteId}
     * Actualizar un reporte existente (solo JSON sin archivos)
     */
    @PutMapping(value = "/reports/{reporteId:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> actualizarReporteJSON(
            @PathVariable String username,
            @PathVariable Long reporteId,
            @RequestBody UpdateReporteDTO updateDTO) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Usuario {} actualizando reporte {} (JSON)", username, reporteId);

            ReporteDetailDTO reporteActualizado = reporteService.actualizarReporte(
                    reporteId, updateDTO, usuario.getUsuarioId());

            if (reporteActualizado == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(Map.of(
                    "id", reporteActualizado.getReporteId(),
                    "mensaje", "Reporte actualizado exitosamente",
                    "reporte", reporteActualizado
            ));
        } catch (Exception e) {
            log.error("Error actualizando reporte", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /devportal/{role}/{username}/reports/{reporteId}
     * Actualizar un reporte existente (FormData con archivos)
     */
    @PutMapping(value = "/reports/{reporteId:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> actualizarReporteConArchivos(
            @PathVariable String username,
            @PathVariable Long reporteId,
            @RequestParam String tituloReporte,
            @RequestParam String descripcionReporte,
            @RequestParam String contenidoReporte,
            @RequestPart(required = false) List<MultipartFile> nuevosAdjuntos) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Usuario {} actualizando reporte {} (FormData con archivos)", username, reporteId);

            // Construir DTO
            UpdateReporteDTO dto = new UpdateReporteDTO();
            dto.setTituloReporte(tituloReporte);
            dto.setDescripcionReporte(descripcionReporte);
            dto.setContenidoReporte(contenidoReporte);

            // Actualizar reporte
            ReporteDetailDTO reporteActualizado = reporteService.actualizarReporte(
                    reporteId, dto, usuario.getUsuarioId());

            if (reporteActualizado == null) {
                return ResponseEntity.notFound().build();
            }

            // Guardar nuevos adjuntos si existen
            if (nuevosAdjuntos != null && !nuevosAdjuntos.isEmpty()) {
                log.info("📎 Agregando {} nuevos adjuntos al reporte {}", nuevosAdjuntos.size(), reporteId);
                Reporte reporte = reporteService.obtenerReportePorId(reporteId);
                reporteService.guardarAdjuntos(reporte, nuevosAdjuntos, usuario);
            }

            return ResponseEntity.ok(Map.of(
                    "id", reporteActualizado.getReporteId(),
                    "mensaje", "Reporte actualizado exitosamente",
                    "reporte", reporteActualizado
            ));
        } catch (Exception e) {
            log.error("Error actualizando reporte con archivos", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /devportal/{role}/{username}/reports/{reporteId}
     * Eliminar un reporte (DEPRECATED - usar la de abajo con /reports/)
     */
    @DeleteMapping("/reports/delete/{reporteId:\\d+}")
    @ResponseBody
    public ResponseEntity<?> eliminarReporteDeprecated(
            @PathVariable String username,
            @PathVariable Long reporteId) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Usuario {} eliminando reporte {}", username, reporteId);

            reporteService.eliminarReporte(reporteId, usuario.getUsuarioId());

            return ResponseEntity.ok(Map.of(
                    "id", reporteId,
                    "mensaje", "Reporte eliminado exitosamente"
            ));
        } catch (Exception e) {
            log.error("Error eliminando reporte", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== STATE CHANGES ==========

    /**
     * POST /devportal/{role}/{username}/reports/{reporteId}/publish
     * Cambiar estado a PUBLICADO (y guardar recipients)
     */
    @PostMapping("/reports/{reporteId:\\d+}/publish")
    @ResponseBody
    public ResponseEntity<?> publicarReporte(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable Long reporteId) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Usuario {} publicando reporte {}", username, reporteId);

            ReporteDetailDTO reporte = reporteService.publicarReporte(reporteId, usuario.getUsuarioId());

            return ResponseEntity.ok(Map.of(
                    "id", reporte.getReporteId(),
                    "estado", reporte.getEstadoReporte(),
                    "mensaje", "Reporte publicado exitosamente"
            ));
        } catch (Exception e) {
            log.error("Error publicando reporte", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /devportal/{role}/{username}/reports/{reporteId}
     * Eliminar un reporte
     */
    @DeleteMapping("/reports/{reporteId:\\d+}")
    @ResponseBody
    public ResponseEntity<?> eliminarReporte(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable Long reporteId) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Usuario {} eliminando reporte {}", username, reporteId);

            reporteService.eliminarReporte(reporteId, usuario.getUsuarioId());

            return ResponseEntity.ok(Map.of(
                    "id", reporteId,
                    "mensaje", "Reporte eliminado exitosamente"
            ));
        } catch (Exception e) {
            log.error("Error eliminando reporte", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /devportal/{role}/{username}/reports/{reporteId}/review
     * Marcar reporte como REVISADO
     */
    @PostMapping("/reports/{reporteId:\\d+}/review")
    @ResponseBody
    public ResponseEntity<?> marcarComoRevisado(
            @PathVariable String username,
            @PathVariable Long reporteId,
            @RequestParam(required = false) String comentarios) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Usuario {} marcando como revisado reporte {}", username, reporteId);

            ReporteDetailDTO reporte = reporteService.marcarComoRevisado(reporteId, usuario.getUsuarioId(), comentarios);

            return ResponseEntity.ok(Map.of(
                    "id", reporte.getReporteId(),
                    "estado", reporte.getEstadoReporte(),
                    "mensaje", "Reporte marcado como revisado"
            ));
        } catch (Exception e) {
            log.error("Error marcando reporte como revisado", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /devportal/{role}/{username}/reports/{reporteId}/reject
     * Rechazar reporte (vuelve a BORRADOR)
     */
    @PostMapping("/reports/{reporteId:\\d+}/reject")
    @ResponseBody
    public ResponseEntity<?> rechazarReporte(
            @PathVariable String username,
            @PathVariable Long reporteId,
            @RequestParam(required = false) String motivo) {
        try {
            Usuario usuario = obtenerUsuario(username);
            log.info("Usuario {} rechazando reporte {}", username, reporteId);

            ReporteDetailDTO reporte = reporteService.rechazarReporte(reporteId, usuario.getUsuarioId(), motivo);

            return ResponseEntity.ok(Map.of(
                    "id", reporte.getReporteId(),
                    "estado", reporte.getEstadoReporte(),
                    "mensaje", "Reporte rechazado"
            ));
        } catch (Exception e) {
            log.error("Error rechazando reporte", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== ENTITY SELECT ENDPOINTS (Select2) ==========

    /**
     * GET /devportal/{role}/{username}/reports/entities/apis
     * Devuelve lista de APIs disponibles para Select2
     */
    @GetMapping("/entities/apis")
    @ResponseBody
    public ResponseEntity<?> obtenerApisDisponibles(
            @PathVariable String username) {
        try {
            Usuario usuario = obtenerUsuario(username);
            List<EntitySelectDTO> apis = reporteService.obtenerApisDelUsuario(usuario.getUsuarioId());
            return ResponseEntity.ok(apis);
        } catch (Exception e) {
            log.error("Error obteniendo APIs", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /devportal/{role}/{username}/reports/entities/tickets
     * Devuelve lista de Tickets disponibles para Select2
     */
    @GetMapping("/entities/tickets")
    @ResponseBody
    public ResponseEntity<?> obtenerTicketsDisponibles(
            @PathVariable String username) {
        try {
            Usuario usuario = obtenerUsuario(username);
            List<EntitySelectDTO> tickets = reporteService.obtenerTicketsDelUsuario(usuario.getUsuarioId());
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            log.error("Error obteniendo Tickets", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /devportal/{role}/{username}/reports/entities/proyectos
     * Devuelve lista de Proyectos disponibles para Select2
     */
    @GetMapping("/entities/proyectos")
    @ResponseBody
    public ResponseEntity<?> obtenerProyectosDisponibles(
            @PathVariable String username) {
        try {
            Usuario usuario = obtenerUsuario(username);
            List<EntitySelectDTO> proyectos = reporteService.obtenerProyectosDelUsuario(usuario.getUsuarioId());
            return ResponseEntity.ok(proyectos);
        } catch (Exception e) {
            log.error("Error obteniendo Proyectos", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /devportal/{role}/{username}/reports/entities/repositorios
     * Devuelve lista de Repositorios disponibles para Select2
     */
    @GetMapping("/entities/repositorios")
    @ResponseBody
    public ResponseEntity<?> obtenerRepositoriosDisponibles(
            @PathVariable String username) {
        try {
            Usuario usuario = obtenerUsuario(username);
            List<EntitySelectDTO> repositorios = reporteService.obtenerRepositoriosDelUsuario(usuario.getUsuarioId());
            return ResponseEntity.ok(repositorios);
        } catch (Exception e) {
            log.error("Error obteniendo Repositorios", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /devportal/{role}/{username}/reports/entities/documentacion
     * Devuelve lista de Documentación disponible para Select2
     */
    @GetMapping("/entities/documentacion")
    @ResponseBody
    public ResponseEntity<?> obtenerDocumentacionDisponible(
            @PathVariable String username) {
        try {
            Usuario usuario = obtenerUsuario(username);
            List<EntitySelectDTO> documentacion = reporteService.obtenerDocumentacionDelUsuario(usuario.getUsuarioId());
            return ResponseEntity.ok(documentacion);
        } catch (Exception e) {
            log.error("Error obteniendo Documentación", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /devportal/{role}/{username}/reports/entities/foro-temas
     * Devuelve lista de Temas del Foro disponibles para Select2
     */
    @GetMapping("/entities/foro-temas")
    @ResponseBody
    public ResponseEntity<?> obtenerForoTemasDisponibles(
            @PathVariable String username) {
        try {
            Usuario usuario = obtenerUsuario(username);
            List<EntitySelectDTO> temas = reporteService.obtenerForoTemasDelUsuario(usuario.getUsuarioId());
            return ResponseEntity.ok(temas);
        } catch (Exception e) {
            log.error("Error obteniendo Temas del Foro", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== DASHBOARD METRICS ENDPOINTS (NUEVOS) ====================

    /**
     * GET /devportal/{role}/{username}/reports/metrics/preview
     * Muestra vista previa de las métricas del dashboard antes de descargar
     * 
     * Este endpoint:
     * 1. Obtiene las estadísticas del usuario desde DashboardService
     * 2. Convierte a formato de exportación usando MetricsConversionService
     * 3. Renderiza vista HTML con tabs para revisar datos antes de descargar
     */
    @GetMapping("/reports/metrics/preview")
    public String showMetricsPreview(
            @PathVariable String username,
            @PathVariable String role,
            Model model) {
        try {
            log.info("📊 Mostrando vista previa de métricas para usuario: {}", username);
            
            // Obtener usuario autenticado
            Usuario usuario = obtenerUsuario(username);
            
            // Convertir estadísticas del dashboard a métricas de reporte
            ReportMetricsDTO metrics = metricsConversionService.convertirEstadisticasAMetricas(
                usuario.getId(), 
                username, 
                role
            );
            
            // Pasar métricas al modelo para la vista
            model.addAttribute("metrics", metrics);
            model.addAttribute("username", username);
            model.addAttribute("role", role);
            model.addAttribute("rol", role);  // Para navbar
            
            log.info("✅ Vista previa generada - Total proyectos: {}, repos: {}, tickets: {}", 
                    metrics.getTotalProyectos(), metrics.getTotalRepositorios(), metrics.getTotalTickets());
            
            return "report/metrics-preview";
            
        } catch (Exception e) {
            log.error("❌ Error mostrando vista previa de métricas: {}", e.getMessage(), e);
            model.addAttribute("error", "No se pudo cargar la vista previa de métricas: " + e.getMessage());
            return "error/500";
        }
    }

    /**
     * GET /devportal/{role}/{username}/reports/metrics/download/excel
     * Descarga métricas completas del dashboard en formato Excel (.xlsx)
     * 
     * Este endpoint:
     * 1. Obtiene estadísticas completas usando MetricsConversionService
     * 2. Genera archivo Excel con 4 hojas (Resumen, Proyectos, Repositorios, Tickets)
     * 3. Retorna archivo para descarga automática
     */
    @GetMapping("/reports/metrics/download/excel")
    public ResponseEntity<?> downloadMetricsExcel(
            @PathVariable String username,
            @PathVariable String role) {
        try {
            log.info("📥 Descargando métricas Excel para usuario: {}", username);
            
            // Obtener usuario
            Usuario usuario = obtenerUsuario(username);
            
            // Obtener métricas completas del dashboard
            ReportMetricsDTO metrics = metricsConversionService.convertirEstadisticasAMetricas(
                usuario.getId(), 
                username, 
                role
            );
            
            // Generar Excel con Apache POI
            java.io.ByteArrayOutputStream excelBytes = reportExportService.exportToExcel(metrics);
            
            // Configurar headers de descarga
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            
            String fileName = String.format(
                "dashboard-metrics-%s-%s.xlsx",
                username,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            );
            headers.setContentDispositionFormData("attachment", fileName);
            
            log.info("✅ Excel generado exitosamente - Tamaño: {} bytes", excelBytes.size());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes.toByteArray());
                    
        } catch (Exception e) {
            log.error("❌ Error descargando métricas Excel: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error generando Excel: " + e.getMessage()));
        }
    }

    /**
     * GET /devportal/{role}/{username}/reports/metrics/download/csv
     * Descarga métricas completas del dashboard en formato CSV
     * 
     * Similar al endpoint de Excel pero genera archivo CSV plano
     */
    @GetMapping("/reports/metrics/download/csv")
    public ResponseEntity<?> downloadMetricsCSV(
            @PathVariable String username,
            @PathVariable String role) {
        try {
            log.info("📥 Descargando métricas CSV para usuario: {}", username);
            
            Usuario usuario = obtenerUsuario(username);
            
            ReportMetricsDTO metrics = metricsConversionService.convertirEstadisticasAMetricas(
                usuario.getId(), 
                username, 
                role
            );
            
            String csvContent = reportExportService.exportToCSV(metrics);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(new org.springframework.http.MediaType("text", "csv"));
            
            String fileName = String.format(
                "dashboard-metrics-%s-%s.csv",
                username,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            );
            headers.setContentDispositionFormData("attachment", fileName);
            
            log.info("✅ CSV generado exitosamente");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    
        } catch (Exception e) {
            log.error("❌ Error descargando métricas CSV: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error generando CSV: " + e.getMessage()));
        }
    }

    // ========== DASHBOARD EXPORT ENDPOINTS (DEPRECATED) ====================

    /**
     * GET /devportal/{role}/{username}/reports/export/dashboard/excel
     * Exporta métricas del dashboard a Excel (.xlsx)
     * 
     * Requiere que el usuario tenga estadísticas disponibles
     */
    @GetMapping("/export/dashboard/excel")
    public ResponseEntity<?> exportDashboardToExcel(
            @PathVariable String username,
            @RequestParam String usuarioSolicitante,
            @RequestParam String rolUsuario,
            @RequestParam int totalProyectos,
            @RequestParam int totalRepositorios,
            @RequestParam int totalTickets) {
        try {
            log.info("Exportando dashboard a Excel para usuario: {}", username);

            // Construir DTO con métricas (se debe obtener del dashboard real)
            // Por ahora usamos los parámetros recibidos
            ReportMetricsDTO metrics = new ReportMetricsDTO();
            metrics.setUsuarioSolicitante(usuarioSolicitante);
            metrics.setRolUsuario(rolUsuario);
            metrics.setFechaGeneracion(java.time.LocalDateTime.now().toString());
            metrics.setTotalProyectos((long) totalProyectos);
            metrics.setTotalRepositorios((long) totalRepositorios);
            metrics.setTotalTickets((long) totalTickets);

            // TODO: Obtener métricas detalladas del servicio correspondiente
            // Por ahora inicializamos mapas vacíos
            metrics.setProyectosPorPropietario(new java.util.HashMap<>());
            metrics.setProyectosPorEstado(new java.util.HashMap<>());
            metrics.setRepositoriosPorTipo(new java.util.HashMap<>());
            metrics.setRepositoriosPorEstado(new java.util.HashMap<>());
            metrics.setTicketsPorEstado(new java.util.HashMap<>());
            metrics.setTicketsPorPrioridad(new java.util.HashMap<>());

            // Generar Excel
            java.io.ByteArrayOutputStream excelBytes = reportExportService.exportToExcel(metrics);

            // Configurar headers de descarga
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    "dashboard-metrics-" + username + "-" + 
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + 
                    ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes.toByteArray());

        } catch (Exception e) {
            log.error("Error exportando dashboard a Excel", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /devportal/{role}/{username}/reports/export/dashboard/csv
     * Exporta métricas del dashboard a CSV
     */
    @GetMapping("/export/dashboard/csv")
    public ResponseEntity<?> exportDashboardToCSV(
            @PathVariable String username,
            @RequestParam String usuarioSolicitante,
            @RequestParam String rolUsuario,
            @RequestParam int totalProyectos,
            @RequestParam int totalRepositorios,
            @RequestParam int totalTickets) {
        try {
            log.info("Exportando dashboard a CSV para usuario: {}", username);

            // Construir DTO con métricas
            ReportMetricsDTO metrics = new ReportMetricsDTO();
            metrics.setUsuarioSolicitante(usuarioSolicitante);
            metrics.setRolUsuario(rolUsuario);
            metrics.setFechaGeneracion(java.time.LocalDateTime.now().toString());
            metrics.setTotalProyectos((long) totalProyectos);
            metrics.setTotalRepositorios((long) totalRepositorios);
            metrics.setTotalTickets((long) totalTickets);

            // TODO: Obtener métricas detalladas del servicio correspondiente
            metrics.setProyectosPorPropietario(new java.util.HashMap<>());
            metrics.setProyectosPorEstado(new java.util.HashMap<>());
            metrics.setRepositoriosPorTipo(new java.util.HashMap<>());
            metrics.setRepositoriosPorEstado(new java.util.HashMap<>());
            metrics.setTicketsPorEstado(new java.util.HashMap<>());
            metrics.setTicketsPorPrioridad(new java.util.HashMap<>());

            // Generar CSV
            String csvContent = reportExportService.exportToCSV(metrics);

            // Configurar headers de descarga
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(new org.springframework.http.MediaType("text", "csv"));
            headers.setContentDispositionFormData("attachment", 
                    "dashboard-metrics-" + username + "-" + 
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + 
                    ".csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("Error exportando dashboard a CSV", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== ADJUNTOS ENDPOINTS ==========

    /**
     * GET /devportal/{role}/{username}/reports/adjuntos/{adjuntoId}/download
     * Descargar archivo adjunto de un reporte
     * 
     * Retorna el contenido binario del archivo con headers apropiados para descarga
     */
    @GetMapping("/reports/adjuntos/{adjuntoId:\\d+}/download")
    public ResponseEntity<byte[]> downloadAdjunto(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable Long adjuntoId) {
        try {
            log.info("📥 Descargando adjunto ID: {} por usuario: {}", adjuntoId, username);
            
            // Obtener adjunto completo (con binario)
            ReporteAdjunto adjunto = reporteService.obtenerAdjuntoCompleto(adjuntoId);
            
            if (adjunto == null) {
                log.warn("⚠️ Adjunto no encontrado: {}", adjuntoId);
                return ResponseEntity.notFound().build();
            }
            
            if (adjunto.getContenidoArchivo() == null || adjunto.getContenidoArchivo().length == 0) {
                log.warn("⚠️ Adjunto sin contenido binario: {}", adjuntoId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            
            // Preparar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            
            // Content-Type
            if (adjunto.getTipoMime() != null && !adjunto.getTipoMime().isEmpty()) {
                headers.setContentType(MediaType.parseMediaType(adjunto.getTipoMime()));
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            
            // Content-Disposition (attachment para forzar descarga)
            headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                    .filename(adjunto.getNombreArchivo(), StandardCharsets.UTF_8)
                    .build()
            );
            
            // Content-Length
            headers.setContentLength(adjunto.getTamanoBytes());
            
            log.info("✅ Descargando: {} ({} bytes, {})", 
                     adjunto.getNombreArchivo(), 
                     adjunto.getTamanoBytes(), 
                     adjunto.getTipoMime());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(adjunto.getContenidoArchivo());
                    
        } catch (Exception e) {
            log.error("❌ Error descargando adjunto {}: {}", adjuntoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /devportal/{role}/{username}/reports/adjuntos/{adjuntoId}
     * Elimina un adjunto de un reporte (solo BORRADOR, solo autor)
     */
    @DeleteMapping("/reports/adjuntos/{adjuntoId:\\d+}")
    public ResponseEntity<?> eliminarAdjunto(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable Long adjuntoId) {
        
        try {
            log.info("🗑️ Solicitud DELETE adjunto ID={} por usuario={}", adjuntoId, username);
            
            // Validar que el adjunto existe
            ReporteAdjunto adjunto = reporteService.obtenerAdjuntoCompleto(adjuntoId);
            if (adjunto == null) {
                log.warn("⚠️ Adjunto {} no encontrado", adjuntoId);
                return ResponseEntity.notFound().build();
            }
            
            // Obtener el reporte al que pertenece
            Reporte reporte = adjunto.getReporte();
            if (reporte == null) {
                log.error("❌ Adjunto {} no tiene reporte asociado", adjuntoId);
                return ResponseEntity.badRequest().body("Adjunto sin reporte asociado");
            }
            
            // VALIDAR: Solo puede eliminar el autor
            if (!reporte.getAutor().getUsername().equals(username)) {
                log.warn("⚠️ Usuario {} NO es autor del reporte {}", username, reporte.getReporteId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Solo el autor puede eliminar adjuntos");
            }
            
            // VALIDAR: Solo se puede eliminar en estado BORRADOR
            if (reporte.getEstadoReporte() != Reporte.EstadoReporte.BORRADOR) {
                log.warn("⚠️ Reporte {} no está en BORRADOR (estado: {})", 
                         reporte.getReporteId(), reporte.getEstadoReporte());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Solo se pueden eliminar adjuntos de reportes en BORRADOR");
            }
            
            // Eliminar adjunto
            reporteService.eliminarAdjunto(adjuntoId);
            
            log.info("✅ Adjunto {} eliminado exitosamente", adjuntoId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("❌ Error eliminando adjunto {}: {}", adjuntoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el adjunto");
        }
    }

    /**
     * GET /devportal/{role}/{username}/reports/{id}/export/pdf
     * Exporta un reporte a PDF
     */
    @GetMapping("/reports/{id:\\d+}/export/pdf")
    public ResponseEntity<byte[]> exportarReportePDF(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable Long id) {
        
        try {
            log.info("📄 Solicitud de exportación PDF para reporte ID={} por usuario={}", id, username);
            
            // Generar PDF
            java.io.ByteArrayOutputStream pdfStream = reportePdfExportService.exportarReporteAPdf(id);
            byte[] pdfBytes = pdfStream.toByteArray();
            
            // Configurar headers HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                ContentDisposition.attachment()
                    .filename("reporte-" + id + ".pdf")
                    .build()
            );
            headers.setContentLength(pdfBytes.length);
            
            log.info("✅ PDF generado exitosamente para reporte ID={} ({} bytes)", id, pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Reporte {} no encontrado", id);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("❌ Error exportando reporte {} a PDF: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


