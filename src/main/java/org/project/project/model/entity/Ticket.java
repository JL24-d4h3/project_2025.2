package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "asunto_ticket", nullable = false, length = 255)
    private String asuntoTicket;
    
    // Alias para repository methods en inglés
    @Column(name = "asunto_ticket", insertable = false, updatable = false)
    private String ticketSubject;

    @Lob
    @Column(name = "cuerpo_ticket", nullable = false)
    private String cuerpoTicket;
    
    // Alias para repository methods en inglés
    @Lob
    @Column(name = "cuerpo_ticket", insertable = false, updatable = false)
    private String ticketBody;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Alias para repository methods en inglés
    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_cierre", insertable = false, updatable = false)
    private LocalDateTime closedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_ticket", nullable = false)
    private EstadoTicket estadoTicket;
    
    // Alias para repository methods en inglés
    @Column(name = "estado_ticket", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EstadoTicket ticketStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_ticket", nullable = false)
    private EtapaTicket etapaTicket = EtapaTicket.PENDIENTE;

    // Alias para repository methods en inglés
    @Column(name = "etapa_ticket", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EtapaTicket ticketStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ticket", nullable = false)
    private TipoTicket tipoTicket;
    
    // Alias para repository methods en inglés
    @Column(name = "tipo_ticket", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TipoTicket ticketType;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad_ticket", nullable = false)
    private PrioridadTicket prioridadTicket = PrioridadTicket.MEDIA;

    // Alias para repository methods en inglés
    @Column(name = "prioridad_ticket", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private PrioridadTicket ticketPriority;

    @ManyToOne
    @JoinColumn(name = "reportado_por_usuario_id", nullable = false)
    private Usuario reportadoPor;
    
    // Alias para repository methods en inglés - especial para findByReportedByUserId
    @ManyToOne
    @JoinColumn(name = "reportado_por_usuario_id", insertable = false, updatable = false)
    private Usuario reportedByUser;

    @ManyToOne
    @JoinColumn(name = "asignado_a_usuario_id")
    private Usuario asignadoA;
    
    // Alias para repository methods en inglés
    @ManyToOne
    @JoinColumn(name = "asignado_a_usuario_id", insertable = false, updatable = false)
    private Usuario assignedToUser;

    // Nuevo campo: Proyecto asociado (si es NULL, es ticket público)
    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    // Relación inversa: Conversaciones del chatbot que generaron este ticket
    @OneToMany(mappedBy = "ticketGenerado")
    private Set<ChatbotConversacion> conversacionesChatbot;

    @ManyToMany(mappedBy = "tickets")
    private Set<Usuario> usuarios;

    public enum EstadoTicket {
        ENVIADO, RECIBIDO
    }

    public enum EtapaTicket {
        PENDIENTE, EN_PROGRESO, RESUELTO, CERRADO, RECHAZADO
    }

    public enum TipoTicket {
        INCIDENCIA, CONSULTA, REQUERIMIENTO
    }

    public enum PrioridadTicket {
        BAJA, MEDIA, ALTA
    }
}
