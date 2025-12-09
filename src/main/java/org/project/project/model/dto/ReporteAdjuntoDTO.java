package org.project.project.model.dto;

import java.time.LocalDateTime;

/**
 * DTO para archivos adjuntos de reportes
 * Incluye información de GCS y versionado
 */
public class ReporteAdjuntoDTO {

    private Long adjuntoId;
    private Long reporteId;
    
    // Información del archivo
    private String nombreArchivo;
    private String tipoMime;
    private Long tamanioBytes;
    private String descripcionAdjunto;
    private Integer ordenVisualizacion;

    // Información original de subida
    private LocalDateTime subidoEn;
    private Long subidoPorUsuarioId;
    private String subidoPorNombre;

    // Información GCS
    private String gcsFileId;  // "reportes/2025/10/reporte-123/v1-inicial/archivo.pdf"
    private String gcsBucketName;  // "devportal-storage"
    private String gcsFileePath;  // "gs://devportal-storage/reportes/..."
    private String gcsPublicUrl;  // URL con expiración de 7 días
    private Long gcsFileSizeBytes;

    // Versionado simple
    private Integer versionNumero;  // Siempre 1
    private Boolean esVersionActual;  // true si es el actual
    
    // Auditoría
    private LocalDateTime actualizadoEn;
    private Long actualizadoPorUsuarioId;
    private String actualizadoPorNombre;
    
    // Flag
    private Boolean gcsMigrado;

    // Getters y Setters
    public Long getAdjuntoId() {
        return adjuntoId;
    }

    public void setAdjuntoId(Long adjuntoId) {
        this.adjuntoId = adjuntoId;
    }

    public Long getReporteId() {
        return reporteId;
    }

    public void setReporteId(Long reporteId) {
        this.reporteId = reporteId;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getTipoMime() {
        return tipoMime;
    }

    public void setTipoMime(String tipoMime) {
        this.tipoMime = tipoMime;
    }

    public Long getTamanioBytes() {
        return tamanioBytes;
    }

    public void setTamanioBytes(Long tamanioBytes) {
        this.tamanioBytes = tamanioBytes;
    }

    public String getDescripcionAdjunto() {
        return descripcionAdjunto;
    }

    public void setDescripcionAdjunto(String descripcionAdjunto) {
        this.descripcionAdjunto = descripcionAdjunto;
    }

    public Integer getOrdenVisualizacion() {
        return ordenVisualizacion;
    }

    public void setOrdenVisualizacion(Integer ordenVisualizacion) {
        this.ordenVisualizacion = ordenVisualizacion;
    }

    public LocalDateTime getSubidoEn() {
        return subidoEn;
    }

    public void setSubidoEn(LocalDateTime subidoEn) {
        this.subidoEn = subidoEn;
    }

    public Long getSubidoPorUsuarioId() {
        return subidoPorUsuarioId;
    }

    public void setSubidoPorUsuarioId(Long subidoPorUsuarioId) {
        this.subidoPorUsuarioId = subidoPorUsuarioId;
    }

    public String getSubidoPorNombre() {
        return subidoPorNombre;
    }

    public void setSubidoPorNombre(String subidoPorNombre) {
        this.subidoPorNombre = subidoPorNombre;
    }

    public String getGcsFileId() {
        return gcsFileId;
    }

    public void setGcsFileId(String gcsFileId) {
        this.gcsFileId = gcsFileId;
    }

    public String getGcsBucketName() {
        return gcsBucketName;
    }

    public void setGcsBucketName(String gcsBucketName) {
        this.gcsBucketName = gcsBucketName;
    }

    public String getGcsFileePath() {
        return gcsFileePath;
    }

    public void setGcsFileePath(String gcsFileePath) {
        this.gcsFileePath = gcsFileePath;
    }

    public String getGcsPublicUrl() {
        return gcsPublicUrl;
    }

    public void setGcsPublicUrl(String gcsPublicUrl) {
        this.gcsPublicUrl = gcsPublicUrl;
    }

    public Long getGcsFileSizeBytes() {
        return gcsFileSizeBytes;
    }

    public void setGcsFileSizeBytes(Long gcsFileSizeBytes) {
        this.gcsFileSizeBytes = gcsFileSizeBytes;
    }

    public Integer getVersionNumero() {
        return versionNumero;
    }

    public void setVersionNumero(Integer versionNumero) {
        this.versionNumero = versionNumero;
    }

    public Boolean getEsVersionActual() {
        return esVersionActual;
    }

    public void setEsVersionActual(Boolean esVersionActual) {
        this.esVersionActual = esVersionActual;
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

    public Boolean getGcsMigrado() {
        return gcsMigrado;
    }

    public void setGcsMigrado(Boolean gcsMigrado) {
        this.gcsMigrado = gcsMigrado;
    }
}
