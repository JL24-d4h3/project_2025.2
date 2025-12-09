package org.project.project.model.dto;

import java.time.LocalDateTime;

/**
 * Vista proyectada para seguimiento de tickets
 * Adaptado a la nueva BD official_dev_portal
 */
public interface FollowUpTicketView {
    Long getTicketId();
    String getAsuntoTicket(); // Cambiado de getAsunto() para coincidir con la entidad
    String getCreadorUsuario();
    LocalDateTime getFechaCreacion();
    String getEtapaTicket(); // Cambiado de getEtapa() para coincidir con la entidad
}
