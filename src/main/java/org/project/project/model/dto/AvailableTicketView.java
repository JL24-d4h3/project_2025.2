package org.project.project.model.dto;

/**
 * Vista proyectada para tickets disponibles
 * Adaptado a la nueva BD official_dev_portal
 */
public interface AvailableTicketView {
    Long getTicketId();
    String getAsuntoTicket(); // Cambiado de getAsunto() para coincidir con la entidad
    String getCreadorUsuario();
}
