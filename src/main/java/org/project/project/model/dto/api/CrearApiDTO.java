package org.project.project.model.dto.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * DTO para crear una nueva API completa con documentación CMS.
 * 
 * Este DTO consolida todos los datos del wizard de 4 pestañas:
 * - Tab 1: Información General (nombre, descripción, categorías, tags)
 * - Tab 2: Contrato OpenAPI (YAML/JSON)
 * - Tab 3: Secciones CMS (documentación con archivos)
 * - Tab 4: Revisión (no genera datos adicionales)
 * 
 * Los archivos (MultipartFile[]) se manejan por separado en el Controller.
 * 
 * @author GitHub Copilot
 * @since 2025-10-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearApiDTO {
    
    // ===== TAB 1: INFORMACIÓN GENERAL =====
    
    /**
     * Nombre único de la API (requerido)
     * Ejemplo: "E-commerce API"
     */
    private String nombre;
    
    /**
     * Número de versión inicial (requerido)
     * Formato recomendado: Semantic Versioning (1.0.0)
     * Ejemplo: "1.0.0"
     */
    private String version;
    
    /**
     * Descripción de la API (requerido)
     * Ejemplo: "API para gestión de productos y órdenes"
     */
    private String descripcion;
    
    /**
     * IDs de las categorías seleccionadas (opcional)
     * Ejemplo: [1, 3, 5] → Backend, E-commerce, REST
     */
    private List<Long> categoriaIds;
    
    /**
     * IDs de las etiquetas/tags seleccionados (opcional)
     * Ejemplo: [10, 15] → Java, Spring Boot
     */
    private List<Long> tagIds;
    
    // ===== TAB 2: CONTRATO OPENAPI =====
    
    /**
     * Contenido del contrato OpenAPI como String (requerido)
     * Puede ser JSON o YAML
     * 
     * Opciones de origen:
     * - Modo 1: Editor de código (CodeMirror)
     * - Modo 2: Formulario generado (convertido a YAML)
     * - Modo 3: Archivo subido (leído como String)
     * 
     * Ejemplo (YAML):
     * """
     * openapi: 3.0.0
     * info:
     *   title: E-commerce API
     *   version: 1.0.0
     * paths:
     *   /products:
     *     get:
     *       summary: Listar productos
     * """
     */
    private String contratoYaml;
    
    /**
     * URL base de la API (opcional, puede venir del contrato)
     * Ejemplo: "https://api.example.com/v1"
     */
    private String baseUrl;
    
    /**
     * URL de documentación externa (opcional)
     * Ejemplo: "https://docs.example.com"
     */
    private String documentationUrl;
    
    // ===== TAB 3: SECCIONES CMS =====
    
    /**
     * Lista de secciones de documentación CMS (opcional)
     * Cada sección puede contener texto Markdown, enlaces y archivos
     * 
     * Los archivos adjuntos se envían por separado como MultipartFile[]
     * con naming convention: cmsFiles_{sectionIndex}_{fileIndex}
     */
    private List<SeccionCmsDTO> seccionesCms;
    
    // ===== AL ACTUALIZAR API (NUEVA VERSIÓN) =====
    
    /**
     * Flag para copiar documentación de la versión anterior (solo al actualizar)
     * - true: Copia estructura de docs y reutiliza archivos
     * - false: Crea docs desde cero
     * - null: Creación inicial (no aplica)
     */
    private Boolean copiarDocumentacion;
    
    // ===== METADATOS (INYECTADOS POR EL CONTROLLER) =====
    
    /**
     * Username del usuario que crea la API
     * NO viene del frontend, se inyecta desde Authentication
     */
    private String creadoPorUsername;
    
    /**
     * Estado inicial de la API (siempre BORRADOR al crear)
     * NO viene del frontend, se setea automáticamente
     */
    private String estadoApi;  // "BORRADOR"
}
