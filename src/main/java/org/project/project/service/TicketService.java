package org.project.project.service;

import org.project.project.model.entity.Ticket;
import org.project.project.repository.TicketRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    public List<Ticket> listarTickets() {
        return ticketRepository.findAll();
    }

    public Ticket buscarTicketPorId(Integer id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con id: " + id));
    }

    public Ticket guardarTicket(Ticket ticket) {
        ticket.setFechaCreacion(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    public Ticket actualizarTicket(Integer id, Ticket ticketDetails) {
        Ticket ticket = buscarTicketPorId(id);
        ticket.setAsuntoTicket(ticketDetails.getAsuntoTicket());
        ticket.setCuerpoTicket(ticketDetails.getCuerpoTicket());
        ticket.setFechaCierre(ticketDetails.getFechaCierre());
        ticket.setEstadoTicket(ticketDetails.getEstadoTicket());
        ticket.setTipoTicket(ticketDetails.getTipoTicket());
        ticket.setPrioridadTicket(ticketDetails.getPrioridadTicket());
        ticket.setAsignadoA(ticketDetails.getAsignadoA());
        return ticketRepository.save(ticket);
    }

    public void eliminarTicket(Integer id) {
        Ticket ticket = buscarTicketPorId(id);
        ticketRepository.delete(ticket);
    }
}
