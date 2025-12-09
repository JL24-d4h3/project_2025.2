package org.project.project.model.dto;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de nodos (archivos/carpetas)
 * entre capas de la aplicación
 */
public class NodoDTO {
    
    private Long nodoId;
    private String nombre;
    private String tipo; // "ARCHIVO" o "CARPETA"
    private Long tamanio; // Tamaño en bytes (para archivos)
    private String mimeType;
    private String rutaGcs;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private Boolean esEliminado;
    
    // Información adicional calculada
    private Long childrenCount; // Cantidad de hijos (para carpetas)
    private Long tamanioRecursivo; // Tamaño total incluyendo subcarpetas
    private String fullPath; // Ruta completa desde la raíz
    
    // Constructores
    public NodoDTO() {}
    
    public NodoDTO(Long nodoId, String nombre, String tipo) {
        this.nodoId = nodoId;
        this.nombre = nombre;
        this.tipo = tipo;
    }
    
    // Getters y Setters
    public Long getNodoId() {
        return nodoId;
    }
    
    public void setNodoId(Long nodoId) {
        this.nodoId = nodoId;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public Long getTamanio() {
        return tamanio;
    }
    
    public void setTamanio(Long tamanio) {
        this.tamanio = tamanio;
    }
    
    // Alias para compatibilidad
    public void setSize(Long size) {
        this.tamanio = size;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public String getRutaGcs() {
        return rutaGcs;
    }
    
    public void setRutaGcs(String rutaGcs) {
        this.rutaGcs = rutaGcs;
    }
    
    // Alias para compatibilidad
    public void setGcsPath(String gcsPath) {
        this.rutaGcs = gcsPath;
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
    
    public Boolean getEsEliminado() {
        return esEliminado;
    }
    
    public void setEsEliminado(Boolean esEliminado) {
        this.esEliminado = esEliminado;
    }
    
    // Alias para compatibilidad
    public void setIsDeleted(Boolean isDeleted) {
        this.esEliminado = isDeleted;
    }
    
    public Long getChildrenCount() {
        return childrenCount;
    }
    
    public void setChildrenCount(Long childrenCount) {
        this.childrenCount = childrenCount;
    }
    
    public Long getTamanioRecursivo() {
        return tamanioRecursivo;
    }
    
    public void setTamanioRecursivo(Long tamanioRecursivo) {
        this.tamanioRecursivo = tamanioRecursivo;
    }
    
    public String getFullPath() {
        return fullPath;
    }
    
    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
    
    @Override
    public String toString() {
        return "NodoDTO{" +
                "nodoId=" + nodoId +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", tamanio=" + tamanio +
                ", fullPath='" + fullPath + '\'' +
                '}';
    }
}
