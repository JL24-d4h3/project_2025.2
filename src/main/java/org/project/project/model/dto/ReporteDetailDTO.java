package org.project.project.model.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para retornar detalles completos de un reporte
 * Se usa en GET /reportes/{id}
 */
public class ReporteDetailDTO {

    private Long reporteId;
    private String tituloReporte;
    private String descripcionReporte;
    private String contenidoReporte;
    private String tipoReporte;
    private String estadoReporte;
    
    // Nombre de la entidad relacionada (ej: "Payment Gateway API", "Proyecto DevPortal")
    private String entidadRelacionadaNombre;

    // Auditoría
    private Long autorUsuarioId;
    private String autorNombre;
    private String autorUsername;
    
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    
    private Long actualizadoPorUsuarioId;
    private String actualizadoPorNombre;

    // Adjuntos
    private List<ReporteAdjuntoDTO> adjuntos;

    // Relaciones
    private List<Long> apiIds;
    private List<Long> proyectoIds;
    private List<Long> repositorioIds;
    private List<Long> ticketIds;
    private List<Long> documentacionIds;
    private List<Long> foroTemaIds;

    // Colaboradores
    private List<UsuarioDTO> colaboradores;

    // Permisos del usuario actual
    private boolean puedeEditar;  // true si es el autor
    private boolean puedeRevisar;  // true si es PO

    // Getters y Setters
    public Long getReporteId() {
        return reporteId;
    }

    public void setReporteId(Long reporteId) {
        this.reporteId = reporteId;
    }

    public String getTituloReporte() {
        return tituloReporte;
    }

    public void setTituloReporte(String tituloReporte) {
        this.tituloReporte = tituloReporte;
    }

    public String getDescripcionReporte() {
        return descripcionReporte;
    }

    public void setDescripcionReporte(String descripcionReporte) {
        this.descripcionReporte = descripcionReporte;
    }

    public String getContenidoReporte() {
        return contenidoReporte;
    }

    public void setContenidoReporte(String contenidoReporte) {
        this.contenidoReporte = contenidoReporte;
    }

    public String getTipoReporte() {
        return tipoReporte;
    }

    public void setTipoReporte(String tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public String getEstadoReporte() {
        return estadoReporte;
    }

    public void setEstadoReporte(String estadoReporte) {
        this.estadoReporte = estadoReporte;
    }

    public String getEntidadRelacionadaNombre() {
        return entidadRelacionadaNombre;
    }

    public void setEntidadRelacionadaNombre(String entidadRelacionadaNombre) {
        this.entidadRelacionadaNombre = entidadRelacionadaNombre;
    }

    public Long getAutorUsuarioId() {
        return autorUsuarioId;
    }

    public void setAutorUsuarioId(Long autorUsuarioId) {
        this.autorUsuarioId = autorUsuarioId;
    }

    public String getAutorNombre() {
        return autorNombre;
    }

    public void setAutorNombre(String autorNombre) {
        this.autorNombre = autorNombre;
    }

    public String getAutorUsername() {
        return autorUsername;
    }

    public void setAutorUsername(String autorUsername) {
        this.autorUsername = autorUsername;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public Long getActualizadoPorUsuarioId() {
        return actualizadoPorUsuarioId;
    }

    public void setActualizadoPorUsuarioId(Long actualizadoPorUsuarioId) {
        this.actualizadoPorUsuarioId = actualizadoPorUsuarioId;
    }

    public String getActualizadoPorNombre() {
        return actualizadoPorNombre;
    }

    public void setActualizadoPorNombre(String actualizadoPorNombre) {
        this.actualizadoPorNombre = actualizadoPorNombre;
    }

    public List<ReporteAdjuntoDTO> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(List<ReporteAdjuntoDTO> adjuntos) {
        this.adjuntos = adjuntos;
    }

    public List<Long> getApiIds() {
        return apiIds;
    }

    public void setApiIds(List<Long> apiIds) {
        this.apiIds = apiIds;
    }

    public List<Long> getProyectoIds() {
        return proyectoIds;
    }

    public void setProyectoIds(List<Long> proyectoIds) {
        this.proyectoIds = proyectoIds;
    }

    public List<Long> getRepositorioIds() {
        return repositorioIds;
    }

    public void setRepositorioIds(List<Long> repositorioIds) {
        this.repositorioIds = repositorioIds;
    }

    public List<Long> getTicketIds() {
        return ticketIds;
    }

    public void setTicketIds(List<Long> ticketIds) {
        this.ticketIds = ticketIds;
    }

    public List<Long> getDocumentacionIds() {
        return documentacionIds;
    }

    public void setDocumentacionIds(List<Long> documentacionIds) {
        this.documentacionIds = documentacionIds;
    }

    public List<Long> getForoTemaIds() {
        return foroTemaIds;
    }

    public void setForoTemaIds(List<Long> foroTemaIds) {
        this.foroTemaIds = foroTemaIds;
    }

    public List<UsuarioDTO> getColaboradores() {
        return colaboradores;
    }

    public void setColaboradores(List<UsuarioDTO> colaboradores) {
        this.colaboradores = colaboradores;
    }

    public boolean isPuedeEditar() {
        return puedeEditar;
    }

    public void setPuedeEditar(boolean puedeEditar) {
        this.puedeEditar = puedeEditar;
    }

    public boolean isPuedeRevisar() {
        return puedeRevisar;
    }

    public void setPuedeRevisar(boolean puedeRevisar) {
        this.puedeRevisar = puedeRevisar;
    }
}
