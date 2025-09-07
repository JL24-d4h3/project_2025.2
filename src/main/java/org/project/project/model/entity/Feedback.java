package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "Feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id", nullable = false)
    private Integer feedbackId;

    @Lob
    @Column(name = "comentario", nullable = false)
    private String comentario;

    @Column(name = "puntuacion", nullable = false, precision = 2, scale = 1)
    private BigDecimal puntuacion;

    @ManyToOne
    @JoinColumn(name = "Usuario_usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "Documentacion_documentacion_id", nullable = false)
    private Documentacion documentacion;

}
