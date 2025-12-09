package org.project.project.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TicketHasUsuarioId implements Serializable {

    @Column(name = "ticket_ticket_id")
    private Long ticketId;

    @Column(name = "usuario_usuario_id")
    private Long usuarioId;
    
    // Alias getters para repository methods en inglés
    public Long getUserId() {
        return usuarioId;
    }
    
    public Long getTicketId() {
        return ticketId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketHasUsuarioId that = (TicketHasUsuarioId) o;
        return Objects.equals(ticketId, that.ticketId) &&
                Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId, usuarioId);
    }
}