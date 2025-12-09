package org.project.project.model.dto.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DTO para una sección de documentación CMS en Tab 3.
 * 
 * Cada sección representa un bloque de contenido en la documentación de la API:
 * - Título (ej: "Guía de Inicio", "Tutorial Avanzado")
 * - Tipo de contenido (GUIA, TUTORIAL, VIDEO, SNIPPET, OTRO)
 * - Contenido en formato Markdown
 * - Enlaces de referencia externos
 * - Archivos adjuntos (manejados por separado en el controller)
 * 
 * @author GitHub Copilot
 * @since 2025-10-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeccionCmsDTO {
    
    /**
     * Título de la sección (requerido)
     * Ejemplo: "Guía de Inicio Rápido"
     */
    private String titulo;
    
    /**
     * Tipo de contenido (requerido)
     * Debe corresponder a un valor en la tabla clasificacion
     * 
     * Valores válidos:
     * - "GUIA": Guía paso a paso
     * - "TUTORIAL": Tutorial con código
     * - "VIDEO": Video tutorial
     * - "SNIPPET": Fragmento de código
     * - "OTRO": Otro tipo de contenido
     */
    private String tipoContenido;
    
    /**
     * Contenido en formato Markdown (opcional)
     * 
     * Este texto se almacena en la tabla 'enlace' con tipo_enlace='TEXTO_CONTENIDO'
     * utilizando el campo direccion_almacenamiento (TEXT) para el contenido Markdown.
     * 
     * Ejemplo:
     * ```markdown
     * # Guía de Inicio
     * Esta API permite...
     * 
     * ## Autenticación
     * Use API Key en el header...
     * ```
     */
    private String contenido;
    
    /**
     * Orden de visualización de la sección (requerido)
     * Las secciones se muestran ordenadas por este campo
     * Ejemplo: 1, 2, 3, ...
     */
    private Integer orden;
    
    /**
     * Lista de enlaces de referencia externos (opcional)
     * 
     * Estos enlaces se almacenan en la tabla 'enlace' con tipo_enlace='ENLACE_EXTERNO'
     * usando el campo direccion_almacenamiento para la URL externa.
     * 
     * El frontend envía un array de objetos con estructura:
     * ```json
     * [
     *   { "titulo": "GitHub Repo", "url": "https://github.com/..." },
     *   { "titulo": "Documentación", "url": "https://docs.example.com" }
     * ]
     * ```
     * 
     * O un array simple de URLs (strings):
     * ```json
     * ["https://github.com/...", "https://docs.example.com"]
     * ```
     */
    private List<EnlaceReferenciaDTO> enlaces;
    
    /**
     * Lista de archivos adjuntos a esta sección (opcional)
     * PDFs, imágenes, código fuente, videos, etc.
     * 
     * Estos archivos se suben a GCS y se crean:
     * - Entidades Recurso (metadata)
     * - Enlaces tipo STORAGE (apuntan a GCS)
     * 
     * El frontend puede enviarlos de dos formas:
     * 1. Directamente en este campo (si usa @ModelAttribute)
     * 2. Como campos separados: cmsFiles_0_0, cmsFiles_0_1, etc. (FormData)
     * 
     * PHASE 4+5: Ahora soportamos subida de archivos
     */
    private List<MultipartFile> archivos;
}
