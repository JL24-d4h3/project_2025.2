package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad híbrida SQL + MongoDB
 * SQL almacena metadatos de ejecución (quién, cuándo, resultado, duración)
 * MongoDB almacena request/response completos (pueden ser muy grandes)
 * en la colección: test_log_detalle
 */
@Getter
@Setter
@Entity
@Table(name = "api_test_log")
public class APITestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_log_id", nullable = false)
    private Long testLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_test_case_id", nullable = false)
    private APITestCase testCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ejecutado_por", nullable = false)
    private Usuario ejecutadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entorno_entorno_id")
    private EntornoPrueba entorno;

    @Column(name = "fecha_ejecucion", nullable = false)
    private LocalDateTime fechaEjecucion = LocalDateTime.now();

    // Alias para repository methods en inglés
    @Column(name = "fecha_ejecucion", insertable = false, updatable = false)
    private LocalDateTime executionDate;

    @Column(name = "duracion_ms")
    private Integer duracionMs;

    // Alias para repository methods en inglés
    @Column(name = "duracion_ms", insertable = false, updatable = false)
    private Integer durationMs;

    @Column(name = "status_code")
    private Integer statusCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false)
    private Resultado resultado;

    // Alias para repository methods en inglés
    @Column(name = "resultado", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Resultado result;

    /**
     * ObjectId de MongoDB que apunta a la colección test_log_detalle
     * Contiene request/response completos, headers, body, etc.
     */
    @Column(name = "nosql_detalle_id", length = 24)
    private String nosqlDetalleId;

    // Relaciones
    @OneToMany(mappedBy = "testLog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<APITestResultado> resultados = new HashSet<>();

    public enum Resultado {
        EXITOSO, FALLIDO, ERROR, TIMEOUT
    }

    // Métodos helper
    public boolean fueExitoso() {
        return Resultado.EXITOSO.equals(this.resultado);
    }

    public boolean fallo() {
        return Resultado.FALLIDO.equals(this.resultado) || Resultado.ERROR.equals(this.resultado);
    }

    public boolean huboCommunicationTimeout() {
        return Resultado.TIMEOUT.equals(this.resultado);
    }
}

