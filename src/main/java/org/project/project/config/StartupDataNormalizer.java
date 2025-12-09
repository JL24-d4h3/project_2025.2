package org.project.project.config;

import org.project.project.repository.TicketRepository;
import org.project.project.model.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class StartupDataNormalizer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupDataNormalizer.class);
    private final TicketRepository ticketRepository;

    public StartupDataNormalizer(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            // Verificar que todos los tickets tengan estados válidos
            List<Ticket> tickets = ticketRepository.findAll();
            int normalized = 0;

            for (Ticket ticket : tickets) {
                boolean needsUpdate = false;

                // Normalizar estado_ticket si es null
                if (ticket.getEstadoTicket() == null) {
                    ticket.setEstadoTicket(Ticket.EstadoTicket.ENVIADO);
                    needsUpdate = true;
                }

                // Normalizar etapa_ticket si es null
                if (ticket.getEtapaTicket() == null) {
                    ticket.setEtapaTicket(Ticket.EtapaTicket.PENDIENTE);
                    needsUpdate = true;
                }

                // Normalizar prioridad_ticket si es null
                if (ticket.getPrioridadTicket() == null) {
                    ticket.setPrioridadTicket(Ticket.PrioridadTicket.MEDIA);
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    ticketRepository.save(ticket);
                    normalized++;
                }
            }

            if (normalized > 0) {
                logger.info("✅ Tickets normalizados en startup: {} registros actualizados", normalized);
            } else {
                logger.info("✅ No se encontraron tickets con datos inválidos para normalizar");
            }
        } catch (Exception e) {
            logger.warn("⚠️ No se pudo normalizar tickets en startup: {}", e.getMessage());
        }
    }
}
