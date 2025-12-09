package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id", nullable = false)
    private Long feedbackId;

    @Lob
    @Column(name = "comentario", nullable = false)
    private String comentario;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "comentario", insertable = false, updatable = false)
    private String comment;

    @Column(name = "puntuacion", nullable = false, precision = 2, scale = 1)
    private BigDecimal puntuacion;

    // Alias para repository methods en inglés
    @Column(name = "puntuacion", insertable = false, updatable = false)
    private BigDecimal rating;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "tipo_feedback", nullable = false)
//    private TipoFeedback tipoFeedback = TipoFeedback.OTRO;

    @Column(name = "fecha_feedback", nullable = false)
    private LocalDateTime fechaFeedback = LocalDateTime.now();

//    @Enumerated(EnumType.STRING)
//    @Column(name = "estado_feedback", nullable = false)
//    private EstadoFeedback estadoFeedback = EstadoFeedback.PENDIENTE;

    @ManyToOne
    @JoinColumn(name = "usuario_usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "documentacion_documentacion_id", nullable = false)
    private Documentacion documentacion;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "revisado_por")
//    private Usuario revisadoPor;
//
//    @Column(name = "fecha_revision")
//    private LocalDateTime fechaRevision;
//
//    @Column(name = "nosql_detalle_id", length = 24)
//    private String nosqlDetalleId;
//
//    public enum TipoFeedback {
//        BUG, SUGERENCIA, ELOGIO, PREGUNTA, OTRO
//    }
//
//    public enum EstadoFeedback {
//        PENDIENTE, REVISADO, RESUELTO, ARCHIVADO
//    }
}
