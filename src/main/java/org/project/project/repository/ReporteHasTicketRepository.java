package org.project.project.repository;

import org.project.project.model.entity.ReporteHasTicket;
import org.project.project.model.entity.ReporteHasTicketId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository principal para operaciones básicas CRUD de ReporteHasTicket.
 */
@Repository
public interface ReporteHasTicketRepository extends JpaRepository<ReporteHasTicket, ReporteHasTicketId> {

    // Buscar por reporte ID
    List<ReporteHasTicket> findById_ReporteId(Long reporteId);

    // Buscar por ticket ID
    List<ReporteHasTicket> findById_TicketId(Long ticketId);

    // Verificar existencia por reporte y ticket
    boolean existsById_ReporteIdAndId_TicketId(Long reporteId, Long ticketId);

    // Contar tickets por reporte
    long countById_ReporteId(Long reporteId);

    // Contar reportes por ticket
    long countById_TicketId(Long ticketId);

    // Obtener todos los tickets de un reporte con fetch
    @Query("SELECT rht FROM ReporteHasTicket rht JOIN FETCH rht.ticket WHERE rht.id.reporteId = :reporteId")
    List<ReporteHasTicket> findByReporteIdWithTicket(@Param("reporteId") Long reporteId);
}
