package org.project.project.repository;

import org.project.project.model.entity.ChatbotConversacion;
import org.project.project.model.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatbotConversacionRepository extends JpaRepository<ChatbotConversacion, Long> {

    // =================== BÚSQUEDAS BÁSICAS ===================

    // Find conversations by user
    List<ChatbotConversacion> findByUsuarioOrderByFechaConversacionDesc(Usuario usuario);

    Page<ChatbotConversacion> findByUsuarioOrderByFechaConversacionDesc(Usuario usuario, Pageable pageable);

    // Find by user ID
    List<ChatbotConversacion> findByUsuario_UsuarioIdOrderByFechaConversacionDesc(Long usuarioId);

    Page<ChatbotConversacion> findByUsuario_UsuarioIdOrderByFechaConversacionDesc(Long usuarioId, Pageable pageable);

    // Find conversations by status
    List<ChatbotConversacion> findByEstadoConversacion(ChatbotConversacion.EstadoConversacion estado);

    // Find conversations by topic
    List<ChatbotConversacion> findByTemaConversacion(ChatbotConversacion.TemaConversacion tema);

    // Find by user and status
    List<ChatbotConversacion> findByUsuarioAndEstadoConversacion(Usuario usuario, ChatbotConversacion.EstadoConversacion estado);

    // Find by user and topic
    List<ChatbotConversacion> findByUsuarioAndTemaConversacion(Usuario usuario, ChatbotConversacion.TemaConversacion tema);

    // Find conversations in date range
    List<ChatbotConversacion> findByFechaConversacionBetween(LocalDateTime start, LocalDateTime end);

    // Find by resolved status
    List<ChatbotConversacion> findByIntentoResuelto(Boolean resuelto);

    // Find by user and resolved status
    List<ChatbotConversacion> findByUsuarioAndIntentoResuelto(Usuario usuario, Boolean resuelto);

    // Find conversations that generated tickets
    List<ChatbotConversacion> findByTicketGeneradoIsNotNull();

    // Find by specific ticket
    Optional<ChatbotConversacion> findByTicketGenerado_TicketId(Long ticketId);

    // =================== ESTADÍSTICAS ===================

    // Count conversations by user
    long countByUsuario(Usuario usuario);

    long countByUsuario_UsuarioId(Long usuarioId);

    // Count unresolved conversations
    long countByIntentoResueltoFalse();

    long countByUsuarioAndIntentoResueltoFalse(Usuario usuario);

    // Count conversations by status
    long countByEstadoConversacion(ChatbotConversacion.EstadoConversacion estado);

    // Custom queries for analytics
    @Query("SELECT COUNT(c) FROM ChatbotConversacion c WHERE c.usuario.usuarioId = :userId AND c.fechaConversacion >= :since")
    long countRecentConversationsByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT c FROM ChatbotConversacion c WHERE c.usuario.usuarioId = :userId ORDER BY c.fechaConversacion DESC")
    List<ChatbotConversacion> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT c FROM ChatbotConversacion c WHERE c.contexto LIKE %:keyword% OR c.mensajeUsuario LIKE %:keyword% OR c.respuestaBot LIKE %:keyword%")
    List<ChatbotConversacion> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT c FROM ChatbotConversacion c WHERE c.usuario.usuarioId = :userId AND (c.contexto LIKE %:keyword% OR c.mensajeUsuario LIKE %:keyword% OR c.respuestaBot LIKE %:keyword%)")
    List<ChatbotConversacion> searchByUserAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
}
