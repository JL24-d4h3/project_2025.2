package org.project.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para transferir secciones de documentación CMS al frontend.
 * 
 * Contiene el contenido Markdown listo para renderizar, sin importar
 * si proviene de BD (markdown_content) o de GCS (descarga).
 * 
 * FASE 0.5: Creado para separar lógica de lectura de entidades JPA.
 * FASE 0.5.1: Agregado soporte para múltiples archivos adjuntos por sección.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionDocumentacionDTO {
    
    /**
     * ID del contenido original (para tracking/debugging)
     */
    private Long contenidoId;
    
    /**
     * Título de la sección (ej: "Guía de Inicio Rápido")
     */
    private String titulo;
    
    /**
     * Orden de la sección en la documentación
     */
    private Integer orden;
    
    /**
     * Contenido Markdown completo listo para renderizar.
     * 
     * Este campo contiene el texto Markdown sin importar el origen:
     * - Si viene de BD: contenido de recurso.markdown_content
     * - Si viene de GCS: contenido descargado desde enlace.direccion_almacenamiento
     * 
     * El frontend lo recibe en el atributo data-markdown para renderizar con marked.js
     */
    private String contenidoMarkdown;
    
    /**
     * Tipo de contenido (de Clasificacion.tipo_contenido)
     * Ej: "GUIA", "TUTORIAL", "REFERENCIA", etc.
     */
    private String tipoContenido;
    
    /**
     * Indica el origen del contenido (para debugging/logs)
     * Valores: "BD" o "GCS"
     */
    private String origenContenido;
    
    /**
     * Lista de archivos adjuntos (PDFs, imágenes, etc.) asociados a esta sección.
     * 
     * FASE 0.5.1: Soporta múltiples recursos por sección además del Markdown principal.
     * Estos archivos se muestran como descargas o visualizaciones en el frontend.
     */
    @Builder.Default
    private List<ArchivoAdjuntoDTO> archivosAdjuntos = new ArrayList<>();
}
