package org.project.project.model.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DTO para crear un nuevo reporte
 * Incluye validaciones necesarias
 */
public class CreateReporteDTO {

    @NotBlank(message = "El título del reporte no puede estar vacío")
    @Size(min = 3, max = 255, message = "El título debe tener entre 3 y 255 caracteres")
    private String tituloReporte;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcionReporte;

    @NotBlank(message = "El contenido del reporte no puede estar vacío")
    @Size(min = 10, max = 50000, message = "El contenido debe tener entre 10 y 50000 caracteres")
    private String contenidoReporte;  // HTML desde TinyMCE

    @NotNull(message = "El tipo de reporte es requerido")
    @Pattern(regexp = "TICKET|API|PROYECTO|REPOSITORIO|DOCUMENTACION|FORO|GENERAL", 
             message = "Tipo de reporte inválido")
    private String tipoReporte;

    // Archivos adjuntos
    private List<MultipartFile> adjuntos;

    @Size(max = 10, message = "No se pueden subir más de 10 archivos por reporte")
    private List<String> descripcionesAdjuntos;

    @Size(max = 10, message = "Máximo 10 órdenes de visualización")
    private List<Integer> ordenesVisualizacion;

    // Relaciones opcionales
    private List<Long> apiIds;
    private List<Long> proyectoIds;
    private List<Long> repositorioIds;
    private List<Long> ticketIds;
    private List<Long> documentacionIds;
    private List<Long> foroTemaIds;

    // Getters y Setters
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

    public List<MultipartFile> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(List<MultipartFile> adjuntos) {
        this.adjuntos = adjuntos;
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
}
