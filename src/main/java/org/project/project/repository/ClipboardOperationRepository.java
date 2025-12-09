package org.project.project.repository;

import org.project.project.model.entity.ClipboardOperation;
import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClipboardOperationRepository extends JpaRepository<ClipboardOperation, Long> {

    // Find active clipboard by user
    @Query("SELECT c FROM ClipboardOperation c WHERE c.usuario.usuarioId = :userId " +
           "AND c.isExpired = false AND c.expiresAt > :now ORDER BY c.createdAt DESC")
    List<ClipboardOperation> findActiveByUserId(@Param("userId") Long userId, 
                                                @Param("now") LocalDateTime now);

    // Find latest clipboard by user
    Optional<ClipboardOperation> findFirstByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    // Find by user and operation type
    List<ClipboardOperation> findByUsuarioAndOperationTypeAndIsExpiredFalse(
            Usuario usuario, ClipboardOperation.OperationType operationType);

    // Find by user and not expired
    List<ClipboardOperation> findByUsuarioAndIsExpiredFalseOrderByCreatedAtDesc(Usuario usuario);

    // Find expired operations
    @Query("SELECT c FROM ClipboardOperation c WHERE c.expiresAt < :now AND c.isExpired = false")
    List<ClipboardOperation> findExpiredOperations(@Param("now") LocalDateTime now);

    // Mark expired operations
    @Modifying
    @Query("UPDATE ClipboardOperation c SET c.isExpired = true WHERE c.expiresAt < :now AND c.isExpired = false")
    int markExpiredOperations(@Param("now") LocalDateTime now);

    // Delete by user
    void deleteByUsuario(Usuario usuario);

    // Delete expired operations
    @Modifying
    @Query("DELETE FROM ClipboardOperation c WHERE c.isExpired = true AND c.createdAt < :before")
    int deleteExpiredOperationsBefore(@Param("before") LocalDateTime before);

    // Count active by user
    @Query("SELECT COUNT(c) FROM ClipboardOperation c WHERE c.usuario.usuarioId = :userId " +
           "AND c.isExpired = false AND c.expiresAt > :now")
    long countActiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Check if user has active clipboard
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ClipboardOperation c " +
           "WHERE c.usuario.usuarioId = :userId AND c.isExpired = false AND c.expiresAt > :now")
    boolean hasActiveClipboard(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
