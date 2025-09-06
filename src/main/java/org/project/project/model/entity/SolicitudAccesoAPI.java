package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Solicitud_acceso_API")
public class SolicitudAccesoAPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accesibilidad_id", nullable = false)
    private Integer accesibilidadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entorno", nullable = false)
    private TipoEntorno tipoEntorno;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud", nullable = false)
    private EstadoSolicitud estadoSolicitud;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "comentario_solicitud", length = 255)
    private String comentarioSolicitud;

    @ManyToOne
    @JoinColumn(name = "usuario_usuario_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne
    @JoinColumn(name = "usuario_usuario_id1", nullable = false)
    private Usuario aprobador;

    @ManyToOne
    @JoinColumn(name = "api_api_id", nullable = false)
    private API api;

    public enum TipoEntorno {
        SANDBOX, QA, PROD
    }

    public enum EstadoSolicitud {
        PENDIENTE, APROBADO, RECHAZADO
    }
}
