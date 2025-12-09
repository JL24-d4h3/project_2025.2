package org.project.project.model.dto.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO para enlaces de referencia externos en secciones CMS.
 * 
 * Representa un enlace externo asociado a una sección de documentación:
 * - Repositorio de código (GitHub, GitLab)
 * - Proyecto/ticket (JIRA, Trello)
 * - Documentación externa
 * - Nodo de conocimiento relacionado
 * 
 * Estos enlaces NO son archivos almacenados, son URLs externas.
 * 
 * @author GitHub Copilot
 * @since 2025-10-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnlaceReferenciaDTO {
    
    /**
     * Tipo de enlace (requerido)
     * 
     * Valores sugeridos:
     * - "repositorio": Repositorio de código (GitHub, GitLab, Bitbucket)
     * - "proyecto": Proyecto o ticket (JIRA, Trello, Asana)
     * - "documentacion": Documentación externa (Swagger, Confluence)
     * - "nodo": Nodo de conocimiento relacionado (wiki interna)
     * - "referencia": Otra referencia externa
     * 
     * Ejemplo: "repositorio"
     */
    private String tipo;
    
    /**
     * URL completa del enlace (requerido)
     * 
     * Debe ser una URL válida y accesible
     * 
     * Ejemplos:
     * - "https://github.com/org/repo"
     * - "https://jira.company.com/browse/PROJ-123"
     * - "https://confluence.company.com/display/DOC/API+Guide"
     * - "https://swagger.io/docs/"
     */
    private String url;
    
    /**
     * Descripción/Título del enlace (opcional)
     * 
     * Texto corto que explica qué contiene el enlace.
     * El frontend envía este campo como "titulo" en JSON.
     * 
     * Ejemplos:
     * - "Código fuente del cliente Java"
     * - "Ticket con historial de cambios"
     * - "Guía de migración oficial"
     * - "Wiki de arquitectura"
     */
    @JsonProperty("titulo") // El frontend envía "titulo" en lugar de "descripcion"
    private String descripcion;
}
