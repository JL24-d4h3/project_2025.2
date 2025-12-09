package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificacion_id", nullable = false)
    private Long notificacionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_notificacion", nullable = false)
    private TipoNotificacion tipoNotificacion;

    @Column(name = "leida", nullable = false)
    private Boolean leida = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false)
    private Prioridad prioridad = Prioridad.NORMAL;

    @Column(name = "nosql_payload_id", length = 24)
    private String nosqlPayloadId;

    public enum TipoNotificacion {
        SOLICITUD_VERSION, FEEDBACK, TICKET_RESUELTO, TICKET_COMENTARIO, FORO_RESPUESTA,
        FORO_MENCION, API_ACTUALIZADA, PROYECTO_INVITACION, SISTEMA, OTRO
    }

    public enum Prioridad {
        BAJA, NORMAL, ALTA, URGENTE
    }
}
