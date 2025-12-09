package org.project.project.repository;

import org.project.project.model.entity.TicketComentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketComentarioRepository extends JpaRepository<TicketComentario, Long> {

    // =================== BÚSQUEDAS BÁSICAS ===================

    List<TicketComentario> findByTicket_TicketId(Long ticketId);

    @Query("SELECT c FROM TicketComentario c WHERE c.ticket.ticketId = :ticketId ORDER BY c.fechaCreacion ASC")
    Page<TicketComentario> findByTicketIdOrdered(@Param("ticketId") Long ticketId, Pageable pageable);

    List<TicketComentario> findByAutor_UsuarioId(Long autorId);

    List<TicketComentario> findByTipoComentario(TicketComentario.TipoComentario tipo);

    // =================== COMENTARIOS RAÍZ (SIN PADRE) ===================

    @Query("SELECT c FROM TicketComentario c WHERE c.ticket.ticketId = :ticketId AND c.parentComentario IS NULL ORDER BY c.fechaCreacion ASC")
    List<TicketComentario> findRootCommentsByTicketId(@Param("ticketId") Long ticketId);

    // =================== RESPUESTAS A UN COMENTARIO ===================

    @Query("SELECT c FROM TicketComentario c WHERE c.parentComentario.comentarioId = :parentId ORDER BY c.fechaCreacion ASC")
    List<TicketComentario> findRepliesByParentId(@Param("parentId") Long parentId);

    // =================== SOLUCIONES ===================

    @Query("SELECT c FROM TicketComentario c WHERE c.ticket.ticketId = :ticketId AND c.tipoComentario = 'SOLUCION'")
    List<TicketComentario> findSolutionsByTicketId(@Param("ticketId") Long ticketId);

    boolean existsByTicket_TicketIdAndTipoComentario(Long ticketId, TicketComentario.TipoComentario tipo);

    // =================== NOTAS INTERNAS ===================

    @Query("SELECT c FROM TicketComentario c WHERE c.ticket.ticketId = :ticketId AND c.tipoComentario = 'NOTA_INTERNA' ORDER BY c.fechaCreacion DESC")
    List<TicketComentario> findInternalNotesByTicketId(@Param("ticketId") Long ticketId);

    // =================== ESTADÍSTICAS ===================

    long countByTicket_TicketId(Long ticketId);

    @Query("SELECT COUNT(c) FROM TicketComentario c WHERE c.ticket.ticketId = :ticketId AND c.tipoComentario = 'COMENTARIO'")
    long countPublicCommentsByTicketId(@Param("ticketId") Long ticketId);

    long countByAutor_UsuarioId(Long autorId);

    @Query("SELECT COUNT(c) FROM TicketComentario c WHERE c.autor.usuarioId = :autorId AND c.tipoComentario = 'SOLUCION'")
    long countSolutionsByAutorId(@Param("autorId") Long autorId);

    // =================== ÚLTIMOS COMENTARIOS ===================

    @Query("SELECT c FROM TicketComentario c WHERE c.ticket.ticketId = :ticketId ORDER BY c.fechaCreacion DESC")
    List<TicketComentario> findLatestCommentsByTicketId(@Param("ticketId") Long ticketId, Pageable pageable);

    @Query("SELECT c FROM TicketComentario c WHERE c.autor.usuarioId = :autorId ORDER BY c.fechaCreacion DESC")
    Page<TicketComentario> findLatestCommentsByAutorId(@Param("autorId") Long autorId, Pageable pageable);
}

