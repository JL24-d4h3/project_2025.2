package org.project.project.model.dto;

import java.time.LocalDateTime;

/**
 * Vista resumida de tickets
 * Adaptado a la nueva BD official_dev_portal
 */
public interface TicketSummaryView {
    Long getTicketId();
    String getAsuntoTicket(); // Cambiado de getAsunto() para coincidir con la entidad
    LocalDateTime getFechaCreacion();
    LocalDateTime getFechaCierre();
    String getEtapaTicket(); // Cambiado de getEtapa() para coincidir con la entidad
}
