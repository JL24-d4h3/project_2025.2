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
public class ReporteHasTicketId implements Serializable {

    @Column(name = "reporte_id")
    private Long reporteId;

    @Column(name = "ticket_id")
    private Long ticketId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReporteHasTicketId that = (ReporteHasTicketId) o;
        return Objects.equals(reporteId, that.reporteId) &&
               Objects.equals(ticketId, that.ticketId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reporteId, ticketId);
    }
}

