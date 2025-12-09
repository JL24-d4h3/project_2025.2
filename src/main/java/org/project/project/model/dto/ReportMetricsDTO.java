package org.project.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para métricas del dashboard principal (DEV/QA/PO)
 * Usado para exportar a Excel/CSV las estadísticas del dashboard
 * 
 * @author jleon
 * @since 2025-01-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportMetricsDTO {
    
    // === MÉTRICAS DE PROYECTOS ===
    private Map<String, Long> proyectosPorPropietario;  // Ej: {"jdoe": 5, "msmith": 3}
    private Map<String, Long> proyectosPorEstado;       // Ej: {"ACTIVO": 10, "PAUSADO": 2}
    
    // === MÉTRICAS DE REPOSITORIOS ===
    private Map<String, Long> repositoriosPorTipo;      // Ej: {"PRIVADO": 8, "PUBLICO": 4}
    private Map<String, Long> repositoriosPorEstado;    // Ej: {"ACTIVO": 9, "INACTIVO": 3}
    
    // === MÉTRICAS DE TICKETS ===
    private Map<String, Long> ticketsPorEstado;         // Ej: {"ABIERTO": 15, "EN_PROCESO": 8, "CERRADO": 20}
    private Map<String, Long> ticketsPorPrioridad;      // Ej: {"ALTA": 5, "MEDIA": 10, "BAJA": 8}
    
    // === METADATA ===
    private String usuarioSolicitante;                  // Username del que solicitó el export
    private String rolUsuario;                          // dev, qa, po
    private String fechaGeneracion;                     // Timestamp del export
    
    // === TOTALES ===
    private Long totalProyectos;
    private Long totalRepositorios;
    private Long totalTickets;
}
