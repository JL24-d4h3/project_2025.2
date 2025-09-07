package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id", nullable = false)
    private Integer ticketId;

    @Column(name = "asunto_ticket", nullable = false, length = 255)
    private String asuntoTicket;

    @Lob
    @Column(name = "cuerpo_ticket", nullable = false)
    private String cuerpoTicket;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_ticket", nullable = false)
    private EstadoTicket estadoTicket;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ticket", nullable = false)
    private TipoTicket tipoTicket;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad_ticket", nullable = false)
    private PrioridadTicket prioridadTicket;

    @ManyToOne
    @JoinColumn(name = "reportado_por_usuario_id", nullable = false)
    private Usuario reportadoPor;

    @ManyToOne
    @JoinColumn(name = "asignado_a_usuario_id")
    private Usuario asignadoA;

    @ManyToMany(mappedBy = "tickets")
    private Set<Usuario> usuarios;

    public enum EstadoTicket {
        ENVIADO, RECIBIDO, EN_PROGRESO, RESUELTO, CERRADO, RECHAZADO
    }

    public enum TipoTicket {
        INCIDENCIA, CONSULTA, REQUERIMIENTO
    }

    public enum PrioridadTicket {
        BAJA, MEDIA, ALTA
    }
}
