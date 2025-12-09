package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "api_suscripcion")
public class APISuscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suscripcion_id", nullable = false)
    private Long suscripcionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_api_id", nullable = false)
    private API api;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_suscripcion", nullable = false)
    private PlanSuscripcion planSuscripcion = PlanSuscripcion.FREE;

    // Alias para repository methods en inglés
    @Column(name = "plan_suscripcion", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private PlanSuscripcion subscriptionPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_suscripcion", nullable = false)
    private EstadoSuscripcion estadoSuscripcion = EstadoSuscripcion.ACTIVA;

    // Alias para repository methods en inglés
    @Column(name = "estado_suscripcion", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EstadoSuscripcion subscriptionStatus;

    @Column(name = "limite_llamadas_mes", nullable = false)
    private Integer limiteLlamadasMes;

    // Alias para repository methods en inglés
    @Column(name = "limite_llamadas_mes", insertable = false, updatable = false)
    private Integer monthlyCallLimit;

    @Column(name = "llamadas_mes_actual", nullable = false)
    private Integer llamadasMesActual = 0;

    // Alias para repository methods en inglés
    @Column(name = "llamadas_mes_actual", insertable = false, updatable = false)
    private Integer currentMonthCalls;

    @Column(name = "mes_periodo", nullable = false)
    private LocalDate mesPeriodo;

    // Alias para repository methods en inglés
    @Column(name = "mes_periodo", insertable = false, updatable = false)
    private LocalDate periodMonth;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio = LocalDateTime.now();

    // Alias para repository methods en inglés
    @Column(name = "fecha_inicio", insertable = false, updatable = false)
    private LocalDateTime startDate;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    // Alias para repository methods en inglés
    @Column(name = "fecha_fin", insertable = false, updatable = false)
    private LocalDateTime endDate;

    @Column(name = "costo_mensual", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoMensual = BigDecimal.ZERO;

    // Alias para repository methods en inglés
    @Column(name = "costo_mensual", insertable = false, updatable = false)
    private BigDecimal monthlyCost;

    public enum PlanSuscripcion {
        FREE, BASIC, PRO, ENTERPRISE
    }

    public enum EstadoSuscripcion {
        ACTIVA, SUSPENDIDA, CANCELADA, EXPIRADA
    }

    // Métodos helper
    public void incrementarLlamadasMes() {
        this.llamadasMesActual++;
    }

    public void reiniciarContadorMensual() {
        this.llamadasMesActual = 0;
        this.mesPeriodo = LocalDate.now().withDayOfMonth(1);
    }

    public boolean haAlcanzadoLimiteMensual() {
        return this.llamadasMesActual >= this.limiteLlamadasMes;
    }

    public void activar() {
        this.estadoSuscripcion = EstadoSuscripcion.ACTIVA;
    }

    public void suspender() {
        this.estadoSuscripcion = EstadoSuscripcion.SUSPENDIDA;
    }

    public void cancelar() {
        this.estadoSuscripcion = EstadoSuscripcion.CANCELADA;
        this.fechaFin = LocalDateTime.now();
    }

    public void marcarComoExpirada() {
        this.estadoSuscripcion = EstadoSuscripcion.EXPIRADA;
    }

    public boolean estaActiva() {
        return EstadoSuscripcion.ACTIVA.equals(this.estadoSuscripcion);
    }

    public void cambiarPlan(PlanSuscripcion nuevoPlan, Integer nuevoLimite, BigDecimal nuevoCosto) {
        this.planSuscripcion = nuevoPlan;
        this.limiteLlamadasMes = nuevoLimite;
        this.costoMensual = nuevoCosto;
    }
}

