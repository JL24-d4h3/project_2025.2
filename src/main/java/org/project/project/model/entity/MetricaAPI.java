package org.project.project.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Metrica_API")
public class MetricaAPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metrica_id", nullable = false)
    private Long metricaId;

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