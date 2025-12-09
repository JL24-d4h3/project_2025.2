package org.project.project.repository;

import org.project.project.model.entity.TicketHasUsuario;
import org.project.project.model.entity.TicketHasUsuarioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TicketHasUsuarioRepository extends JpaRepository<TicketHasUsuario, TicketHasUsuarioId> {
    
    // Find by ticket
    @Query("SELECT thu FROM TicketHasUsuario thu WHERE thu.id.ticketId = :ticketId")
    List<TicketHasUsuario> findById_TicketId(@Param("ticketId") Long ticketId);
    
    // Find by user
    @Query("SELECT thu FROM TicketHasUsuario thu WHERE thu.id.usuarioId = :usuarioId")
    List<TicketHasUsuario> findById_UserId(@Param("usuarioId") Long usuarioId);
    
    // Check if relationship exists
    @Query("SELECT COUNT(thu) > 0 FROM TicketHasUsuario thu WHERE thu.id.ticketId = :ticketId AND thu.id.usuarioId = :usuarioId")
    boolean existsById_TicketIdAndId_UserId(@Param("ticketId") Long ticketId, @Param("usuarioId") Long usuarioId);
    
    // Count users by ticket
    @Query("SELECT COUNT(thu) FROM TicketHasUsuario thu WHERE thu.id.ticketId = :ticketId")
    long countByTicketId(@Param("ticketId") Long ticketId);
    
    // Count tickets by user
    @Query("SELECT COUNT(thu) FROM TicketHasUsuario thu WHERE thu.id.usuarioId = :usuarioId")
    long countByUserId(@Param("usuarioId") Long usuarioId);
    
    // Find users by multiple tickets
    @Query("SELECT thu FROM TicketHasUsuario thu WHERE thu.id.ticketId IN :ticketIds")
    List<TicketHasUsuario> findByTicketIds(@Param("ticketIds") List<Long> ticketIds);
    
    // Find tickets by multiple users
    @Query("SELECT thu FROM TicketHasUsuario thu WHERE thu.id.usuarioId IN :usuarioIds")
    List<TicketHasUsuario> findByUserIds(@Param("usuarioIds") List<Long> usuarioIds);
    
    // Get ticket collaborator statistics
    @Query("SELECT t.asuntoTicket, COUNT(thu) as colaboradorCount FROM TicketHasUsuario thu JOIN thu.ticket t GROUP BY t ORDER BY colaboradorCount DESC")
    List<Object[]> getTicketCollaboratorStats();

    // Método de eliminación para cascada
    @Modifying
    @Transactional
    @Query("DELETE FROM TicketHasUsuario thu WHERE thu.id.usuarioId = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);
}