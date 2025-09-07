package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Conversacion")
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversacion_id", nullable = false)
    private Integer conversacionId;

    @Column(name = "titulo_conversacion", length = 255)
    private String tituloConversacion;

    @Column(name = "fecha_inicio_conversacion", nullable = false)
    private LocalDateTime fechaInicioConversacion;

    @Column(name = "fecha_fin_conversacion")
    private LocalDateTime fechaFinConversacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_conversacion", nullable = false)
    private EstadoConversacion estadoConversacion;

    @ManyToOne
    @JoinColumn(name = "Usuario_usuario_id", nullable = false)
    private Usuario usuario;

    public enum EstadoConversacion {
        ACTIVA, CERRADA
    }
}
