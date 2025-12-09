package org.project.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.project.model.entity.Reporte;

/**
 * DTO para creación de reporte desde wizard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteCreateDTO {
    private Reporte.TipoReporte tipoReporte;
    private String tituloReporte;
    private String descripcionReporte;
    private String contenidoReporte;
    private Reporte.EstadoReporte estadoReporte;
    private Long autorUsuarioId;
    private Long entidadRelacionadaId;
    private String descripcionAdjuntos;
}
