package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "api_test_resultado")
public class APITestResultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resultado_id", nullable = false)
    private Long resultadoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_log_test_log_id", nullable = false)
    private APITestLog testLog;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_validacion", nullable = false)
    private TipoValidacion tipoValidacion;

    // Alias para repository methods en inglés
    @Column(name = "tipo_validacion", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TipoValidacion validationType;

    @Column(name = "nombre_validacion", nullable = false, length = 255)
    private String nombreValidacion;

    // Alias para repository methods en inglés
    @Column(name = "nombre_validacion", insertable = false, updatable = false)
    private String validationName;

    @Lob
    @Column(name = "esperado")
    private String esperado;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "esperado", insertable = false, updatable = false)
    private String expected;

    @Lob
    @Column(name = "obtenido")
    private String obtenido;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "obtenido", insertable = false, updatable = false)
    private String actual;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false)
    private Resultado resultado;

    // Alias para repository methods en inglés
    @Column(name = "resultado", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Resultado result;

    @Lob
    @Column(name = "mensaje_error")
    private String mensajeError;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "mensaje_error", insertable = false, updatable = false)
    private String errorMessage;

    public enum TipoValidacion {
        STATUS_CODE,
        RESPONSE_TIME,
        SCHEMA,
        CONTENT,
        HEADER,
        CUSTOM
    }

    public enum Resultado {
        PASS, FAIL
    }

    // Métodos helper
    public boolean paso() {
        return Resultado.PASS.equals(this.resultado);
    }

    public boolean fallo() {
        return Resultado.FAIL.equals(this.resultado);
    }
}

