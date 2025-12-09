package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reporte_has_documentacion")
public class ReporteHasDocumentacion {

    @EmbeddedId
    private ReporteHasDocumentacionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reporteId")
    @JoinColumn(name = "reporte_id")
    private Reporte reporte;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("documentacionId")
    @JoinColumn(name = "documentacion_id")
    private Documentacion documentacion;

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

    public ReporteHasDocumentacion() {
    }

    public ReporteHasDocumentacion(Reporte reporte, Documentacion documentacion) {
        this.reporte = reporte;
        this.documentacion = documentacion;
        this.id = new ReporteHasDocumentacionId(reporte.getReporteId(), documentacion.getDocumentacionId());
        this.vinculadoEn = LocalDateTime.now();
    }

    public ReporteHasDocumentacion(Reporte reporte, Documentacion documentacion, String notaRelacion) {
        this(reporte, documentacion);
        this.notaRelacion = notaRelacion;
    }
}

