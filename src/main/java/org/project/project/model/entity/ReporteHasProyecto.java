package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reporte_has_proyecto")
public class ReporteHasProyecto {

    @EmbeddedId
    private ReporteHasProyectoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reporteId")
    @JoinColumn(name = "reporte_id")
    private Reporte reporte;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("proyectoId")
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @Lob
    @Column(name = "nota_relacion")
    private String notaRelacion;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "nota_relacion", insertable = false, updatable = false)
    private String relationNote;

    @Column(name = "vinculado_en", nullable = false)
    private LocalDateTime vinculadoEn = LocalDateTime.now();

    // Alias para repository methods en inglés
    @Column(name = "vinculado_en", insertable = false, updatable = false)
    private LocalDateTime linkedAt;

    public ReporteHasProyecto() {
    }

    public ReporteHasProyecto(Reporte reporte, Proyecto proyecto) {
        this.reporte = reporte;
        this.proyecto = proyecto;
        this.id = new ReporteHasProyectoId(reporte.getReporteId(), proyecto.getProyectoId());
        this.vinculadoEn = LocalDateTime.now();
    }

    public ReporteHasProyecto(Reporte reporte, Proyecto proyecto, String notaRelacion) {
        this(reporte, proyecto);
        this.notaRelacion = notaRelacion;
    }
}

