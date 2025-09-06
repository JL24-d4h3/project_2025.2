package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificacion_id", nullable = false)
    private Integer notificacionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_notificacion", nullable = false)
    private TipoNotificacion tipoNotificacion;

    @Column(name = "asunto_notificacion", nullable = false, length = 255)
    private String asuntoNotificacion;

    @Lob
    @Column(name = "mensaje_notificacion", nullable = false)
    private String mensajeNotificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_notificacion", nullable = false)
    private EstadoNotificacion estadoNotificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspeccion_notificacion", nullable = false)
    private InspeccionNotificacion inspeccionNotificacion;

    @ManyToOne
    @JoinColumn(name = "Usuario_usuario_id", nullable = false)
    private Usuario usuario;

    public enum TipoNotificacion {
        SISTEMA, TICKET, ALERTA, METRICA
    }

    public enum EstadoNotificacion {
        ENVIADA, RECIBIDA
    }

    public enum InspeccionNotificacion {
        LEIDA, NO_LEIDA
    }
}
