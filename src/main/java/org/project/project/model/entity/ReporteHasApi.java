package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reporte_has_api")
public class ReporteHasApi {

    @EmbeddedId
    private ReporteHasApiId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reporteId")
    @JoinColumn(name = "reporte_id")
    private Reporte reporte;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("apiId")
    @JoinColumn(name = "api_id")
    private API api;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id")
    private VersionAPI version;

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

    public ReporteHasApi() {
    }

    public ReporteHasApi(Reporte reporte, API api) {
        this.reporte = reporte;
        this.api = api;
        this.id = new ReporteHasApiId(reporte.getReporteId(), api.getApiId());
        this.vinculadoEn = LocalDateTime.now();
    }

    public ReporteHasApi(Reporte reporte, API api, VersionAPI version, String notaRelacion) {
        this(reporte, api);
        this.version = version;
        this.notaRelacion = notaRelacion;
    }
}
