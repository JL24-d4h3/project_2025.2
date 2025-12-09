package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reporte_has_ticket")
public class ReporteHasTicket {

    @EmbeddedId
    private ReporteHasTicketId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reporteId")
    @JoinColumn(name = "reporte_id")
    private Reporte reporte;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ticketId")
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Lob
    @Column(name = "nota_relacion")
    private String notaRelacion;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "nota_relacion", insertable = false, updatable = false)
    private String relationNote;

    @Column(name = "vinculado_en", nullable = false)
    private LocalDateTime vinculadoEn = LocalDateTime.now();

    // Alias para repository methods en inglés
    @Column(name = "vinculado_en", insertable = false, updatable = false)
    private LocalDateTime linkedAt;

    public ReporteHasTicket() {
    }

    public ReporteHasTicket(Reporte reporte, Ticket ticket) {
        this.reporte = reporte;
        this.ticket = ticket;
        this.id = new ReporteHasTicketId(reporte.getReporteId(), ticket.getTicketId());
        this.vinculadoEn = LocalDateTime.now();
    }

    public ReporteHasTicket(Reporte reporte, Ticket ticket, String notaRelacion) {
        this(reporte, ticket);
        this.notaRelacion = notaRelacion;
    }
}

