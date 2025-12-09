package org.project.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para tarjetas de reporte en overview (más grande que feedback)
 * Usado para mostrar reportes enviados/recibidos en vista principal
 * 
 * @author jleon
 * @since 2025-01-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportCardDTO {
    
    // === IDENTIFICACIÓN ===
    private Long reporteId;
    private String tituloReporte;
    
    // === CONTENIDO (limitado para card) ===
    private String descripcionReporte;              // Texto completo
    private String contenidoReportePreview;         // Primeras líneas del contenido HTML (sin tags)
    
    // === TIPO Y ESTADO ===
    private String tipoReporte;                     // API, TICKET, PROYECTO, etc.
    private String estadoReporte;                   // BORRADOR, PUBLICADO, REVISADO
    
    // === ENTIDAD RELACIONADA ===
    private String entidadRelacionadaNombre;        // Ej: "Payment Gateway API", "Proyecto DevPortal"
    private Long entidadRelacionadaId;              // ID de la API, proyecto, etc.
    
    // === AUTOR (siempre visible) ===
    private Long autorId;
    private String autorNombreCompleto;
    private String autorUsername;
    
    // === REMITENTE (solo si es reporte recibido) ===
    private Boolean esRecibido;                     // true si el usuario actual es receptor
    private String remitenteNombreCompleto;         // Solo si esRecibido=true
    private String remitenteUsername;               // Solo si esRecibido=true
    
    // === METADATA ===
    private Integer cantidadAdjuntos;               // Número de archivos adjuntos
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    
    // === PERMISOS (para mostrar/ocultar botones) ===
    private Boolean puedeEditar;                    // true si autor o tiene permiso
    private Boolean puedePublicar;                  // true si es BORRADOR y es autor
    private Boolean puedeMarcarRevisado;            // true si es PUBLICADO y es receptor
    private Boolean puedeEliminar;                  // true si es BORRADOR y es autor
}
