package org.project.project.model.dto;

import java.time.LocalDateTime;

/**
 * DTO simplificado para listar reportes
 * Se usa en GET /reportes (listados)
 */
public class ReporteListDTO {

    private Long reporteId;
    private String tituloReporte;
    private String descripcionReporte;
    private String tipoReporte;
    private String estadoReporte;

    // Autor
    private Long autorUsuarioId;
    private String autorNombre;
    private String autorUsername;

    // Fechas (sin hora completa para listado)
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    // Contador de adjuntos
    private Integer cantidadAdjuntos;

    // Preview de contenido (primeras 200 caracteres)
    private String previewContenido;

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

    public Integer getCantidadAdjuntos() {
        return cantidadAdjuntos;
    }

    public void setCantidadAdjuntos(Integer cantidadAdjuntos) {
        this.cantidadAdjuntos = cantidadAdjuntos;
    }

    public String getPreviewContenido() {
        return previewContenido;
    }

    public void setPreviewContenido(String previewContenido) {
        this.previewContenido = previewContenido;
    }
}
