package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ticket_has_usuario")
public class TicketHasUsuario {

    @EmbeddedId
    private TicketHasUsuarioId id;

    @MapsId("ticketId")
    @ManyToOne
    @JoinColumn(name = "ticket_ticket_id", nullable = false)
    private Ticket ticket;

    @MapsId("usuarioId")
    @ManyToOne
    @JoinColumn(name = "usuario_usuario_id", nullable = false)
    private Usuario usuario;

    public TicketHasUsuario() {}

    public TicketHasUsuario(Ticket ticket, Usuario usuario) {
        this.ticket = ticket;
        this.usuario = usuario;
        this.id = new TicketHasUsuarioId(ticket.getTicketId(), usuario.getUsuarioId());
    }
    
    // Alias getters para repository methods en inglés
    public Ticket getTicket() {
        return ticket;
    }
    
    public Usuario getUser() {
        return usuario;
    }
}