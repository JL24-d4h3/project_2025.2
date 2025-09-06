package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Mensaje")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mensaje_id", nullable = false)
    private Integer mensajeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "remitente", nullable = false)
    private Remitente remitente;

    @Lob
    @Column(name = "contenido_mensaje")
    private String contenidoMensaje;

    @Column(name = "fecha_envio_mensaje", nullable = false)
    private LocalDateTime fechaEnvioMensaje;

    @ManyToOne
    @JoinColumn(name = "Conversacion_conversacion_id", nullable = false)
    private Conversacion conversacion;

    @OneToMany(mappedBy = "mensaje")
    private Set<Adjunto> adjuntos;

    public enum Remitente {
        USUARIO, CHATBOT
    }
}