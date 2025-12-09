package org.project.project.model.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DTO para actualizar un reporte existente
 * Solo el autor puede editar
 * Los cambios sobrescriben la última versión (versionado simple)
 */
public class UpdateReporteDTO {

    @NotNull(message = "El ID del reporte es requerido")
    private Long reporteId;

    @Size(min = 3, max = 255, message = "El título debe tener entre 3 y 255 caracteres")
    private String tituloReporte;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcionReporte;

    @Size(min = 10, max = 50000, message = "El contenido debe tener entre 10 y 50000 caracteres")
    private String contenidoReporte;  // HTML desde TinyMCE

    // Archivos: solo incluir si se quieren cambiar
    private List<MultipartFile> adjuntosNuevos;

    // IDs de archivos a eliminar (marca es_version_actual = FALSE)
    private List<Long> adjetosAEliminar;

    @Size(max = 10, message = "No se pueden subir más de 10 archivos")
    private List<String> descripcionesAdjuntos;

    @Size(max = 10, message = "Máximo 10 órdenes de visualización")
    private List<Integer> ordenesVisualizacion;

    // Relaciones (opcionales)
    private List<Long> apiIds;
    private List<Long> proyectoIds;
    private List<Long> repositorioIds;
    private List<Long> ticketIds;
    private List<Long> documentacionIds;
    private List<Long> foroTemaIds;

    // Estado (solo para PO)
    @Pattern(regexp = "BORRADOR|PUBLICADO|REVISADO|ARCHIVADO|RECHAZADO", 
             message = "Estado inválido")
    private String nuevoEstado;

    // Comentarios del PO (solo si está rechazando)
    @Size(max = 500, message = "Los comentarios no pueden exceder 500 caracteres")
    private String comentariosPO;

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

    public List<MultipartFile> getAdjuntosNuevos() {
        return adjuntosNuevos;
    }

    public void setAdjuntosNuevos(List<MultipartFile> adjuntosNuevos) {
        this.adjuntosNuevos = adjuntosNuevos;
    }

    public List<Long> getAdjetosAEliminar() {
        return adjetosAEliminar;
    }

    public void setAdjetosAEliminar(List<Long> adjetosAEliminar) {
        this.adjetosAEliminar = adjetosAEliminar;
    }

    public List<String> getDescripcionesAdjuntos() {
        return descripcionesAdjuntos;
    }

    public void setDescripcionesAdjuntos(List<String> descripcionesAdjuntos) {
        this.descripcionesAdjuntos = descripcionesAdjuntos;
    }

    public List<Integer> getOrdenesVisualizacion() {
        return ordenesVisualizacion;
    }

    public void setOrdenesVisualizacion(List<Integer> ordenesVisualizacion) {
        this.ordenesVisualizacion = ordenesVisualizacion;
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

    public String getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(String nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    public String getComentariosPO() {
        return comentariosPO;
    }

    public void setComentariosPO(String comentariosPO) {
        this.comentariosPO = comentariosPO;
    }
}
