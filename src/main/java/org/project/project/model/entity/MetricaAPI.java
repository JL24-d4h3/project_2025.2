package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Metrica_API")
public class MetricaAPI {

    @Id
    @Column(name = "metrica_id", nullable = false)
    private Integer metricaId;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "cantidad_llamadas", nullable = false)
    private Integer cantidadLlamadas;

    @Column(name = "cantidad_errores", nullable = false)
    private Integer cantidadErrores;

    @Column(name = "latencia_ms", nullable = false)
    private Float latenciaMs;

    @Column(name = "costo_estimado", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoEstimado;

    @Enumerated(EnumType.STRING)
    @Column(name = "entorno")
    private Entorno entorno;

    @OneToOne(mappedBy = "metricaApi")
    private VersionAPI versionApi;

    public enum Entorno {
        SANDBOX, QA, PROD
    }
}