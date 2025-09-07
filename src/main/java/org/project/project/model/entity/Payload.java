package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Payload")
public class Payload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payload_id", nullable = false)
    private Integer payloadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_payload", nullable = false)
    private TipoPayload tipoPayload;

    @Lob
    @Column(name = "contenido_payload", nullable = false)
    private String contenidoPayload;

    @ManyToOne
    @JoinColumn(name = "Mensaje_mensaje_id", nullable = false)
    private Mensaje mensaje;

    public enum TipoPayload {
        BOTON, ENLACE, JSON, OTRO
    }
}