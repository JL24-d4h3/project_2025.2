package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reporte_has_repositorio")
public class ReporteHasRepositorio {

    @EmbeddedId
    private ReporteHasRepositorioId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reporteId")
    @JoinColumn(name = "reporte_id")
    private Reporte reporte;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("repositorioId")
    @JoinColumn(name = "repositorio_id")
    private Repositorio repositorio;

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

    public ReporteHasRepositorio() {
    }

    public ReporteHasRepositorio(Reporte reporte, Repositorio repositorio) {
        this.reporte = reporte;
        this.repositorio = repositorio;
        this.id = new ReporteHasRepositorioId(reporte.getReporteId(), repositorio.getRepositorioId());
        this.vinculadoEn = LocalDateTime.now();
    }

    public ReporteHasRepositorio(Reporte reporte, Repositorio repositorio, String notaRelacion) {
        this(reporte, repositorio);
        this.notaRelacion = notaRelacion;
    }
}

