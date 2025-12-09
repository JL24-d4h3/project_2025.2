package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa la relación N:M entre Reportes y Temas del Foro
 * Un reporte puede estar vinculado a múltiples temas del foro
 * Un tema del foro puede tener múltiples reportes asociados
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reporte_has_foro")
public class ReporteHasForoTema {

    @EmbeddedId
    private ReporteHasForoTemaId id = new ReporteHasForoTemaId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reporteId")
    @JoinColumn(name = "reporte_id", nullable = false)
    private Reporte reporte;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("foroTemaId")
    @JoinColumn(name = "foro_tema_id", nullable = false, referencedColumnName = "tema_id")
    private ForoTema foroTema;

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

    /**
     * Constructor conveniente para crear la relación
     */
    public ReporteHasForoTema(Reporte reporte, ForoTema foroTema) {
        this.reporte = reporte;
        this.foroTema = foroTema;
        this.id = new ReporteHasForoTemaId(reporte.getReporteId(), foroTema.getTemaId());
        this.vinculadoEn = LocalDateTime.now();
    }

    /**
     * Constructor con nota de relación
     */
    public ReporteHasForoTema(Reporte reporte, ForoTema foroTema, String notaRelacion) {
        this(reporte, foroTema);
        this.notaRelacion = notaRelacion;
    }
}
