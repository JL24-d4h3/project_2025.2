package org.project.project.model.dto;

/**
 * Vista proyectada para tickets disponibles de proyecto
 */
public interface AvailableProjectTicketView {
    Long getTicketId();
    String getAsunto();
    String getCreadorUsuario();
    String getNombreProyecto();
}

