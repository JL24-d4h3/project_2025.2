package org.project.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para archivos adjuntos en secciones de documentación.
 * 
 * Representa archivos adicionales (PDF, images, etc.) asociados a una sección,
 * además del contenido Markdown principal.
 * 
 * FASE 0.5.1: Creado para soportar múltiples recursos por sección.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchivoAdjuntoDTO {
    
    /**
     * ID del recurso original (para tracking)
     */
    private Long recursoId;
    
    /**
     * Nombre del archivo (ej: "manual.pdf", "diagram.png")
     */
    private String nombreArchivo;
    
    /**
     * URL completa del archivo (GCS path o URL externa)
     * Ej: "gs://bucket/local/apis/6/versions/1.0.0/documentation/resources/2/owo.pdf"
     */
    private String url;
    
    /**
     * Tipo MIME del archivo (ej: "application/pdf", "image/png")
     */
    private String mimeType;
    
    /**
     * Tamaño del archivo en bytes (opcional, para mostrar en UI)
     */
    private Long tamanoBytes;
    
    /**
     * Tipo de enlace: "STORAGE", "ENLACE_EXTERNO", etc.
     */
    private String tipoEnlace;
}
