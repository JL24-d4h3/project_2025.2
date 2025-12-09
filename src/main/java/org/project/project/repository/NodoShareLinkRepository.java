package org.project.project.repository;

import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.NodoShareLink;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NodoShareLinkRepository extends JpaRepository<NodoShareLink, Long> {

    // Find by share token
    Optional<NodoShareLink> findByShareToken(String shareToken);

    // Find by share token and is active
    Optional<NodoShareLink> findByShareTokenAndIsActiveTrue(String shareToken);

    // Find by node
    List<NodoShareLink> findByNodoOrderByCreatedAtDesc(Nodo nodo);

    // Find by node ID
    @Query("SELECT s FROM NodoShareLink s WHERE s.nodo.nodoId = :nodeId ORDER BY s.createdAt DESC")
    List<NodoShareLink> findByNodeId(@Param("nodeId") Long nodeId);

    // Find by node and is active
    @Query("SELECT s FROM NodoShareLink s WHERE s.nodo.nodoId = :nodeId AND s.isActive = true " +
           "ORDER BY s.createdAt DESC")
    List<NodoShareLink> findByNodeIdAndIsActiveTrue(@Param("nodeId") Long nodeId);

    // Find by created by user
    List<NodoShareLink> findByCreatedByOrderByCreatedAtDesc(Usuario createdBy);

    // Find by created by user ID
    @Query("SELECT s FROM NodoShareLink s WHERE s.createdBy.usuarioId = :userId " +
           "ORDER BY s.createdAt DESC")
    List<NodoShareLink> findByCreatedByUserId(@Param("userId") Long userId);

    // Find active links
    @Query("SELECT s FROM NodoShareLink s WHERE s.isActive = true " +
           "AND (s.expiresAt IS NULL OR s.expiresAt > CURRENT_TIMESTAMP) " +
           "AND (s.maxDownloads IS NULL OR s.downloadCount < s.maxDownloads) " +
           "ORDER BY s.createdAt DESC")
    List<NodoShareLink> findActiveLinks();

    // Find active links by user
    @Query("SELECT s FROM NodoShareLink s WHERE s.createdBy.usuarioId = :userId " +
           "AND s.isActive = true " +
           "AND (s.expiresAt IS NULL OR s.expiresAt > CURRENT_TIMESTAMP) " +
           "ORDER BY s.createdAt DESC")
    List<NodoShareLink> findActiveLinksByUserId(@Param("userId") Long userId);

    // Find expired links
    @Query("SELECT s FROM NodoShareLink s WHERE s.isActive = true " +
           "AND s.expiresAt IS NOT NULL AND s.expiresAt < CURRENT_TIMESTAMP")
    List<NodoShareLink> findExpiredLinks();

    // Find links that reached max downloads
    @Query("SELECT s FROM NodoShareLink s WHERE s.isActive = true " +
           "AND s.maxDownloads IS NOT NULL AND s.downloadCount >= s.maxDownloads")
    List<NodoShareLink> findLinksWithMaxDownloadsReached();

    // Check if node has active share link
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM NodoShareLink s " +
           "WHERE s.nodo.nodoId = :nodeId AND s.isActive = true " +
           "AND (s.expiresAt IS NULL OR s.expiresAt > CURRENT_TIMESTAMP)")
    boolean hasActiveShareLink(@Param("nodeId") Long nodeId);

    // Count active links by node
    @Query("SELECT COUNT(s) FROM NodoShareLink s WHERE s.nodo.nodoId = :nodeId " +
           "AND s.isActive = true")
    long countActiveLinksByNodeId(@Param("nodeId") Long nodeId);

    // Delete by node
    void deleteByNodo(Nodo nodo);

    // Find by created at after
    @Query("SELECT s FROM NodoShareLink s WHERE s.createdAt > :after ORDER BY s.createdAt DESC")
    List<NodoShareLink> findRecentLinks(@Param("after") LocalDateTime after);

    // Métodos adicionales para NodoShareLinkService
    @Query("SELECT s FROM NodoShareLink s WHERE s.nodo.nodoId = :nodoId AND s.isActive = true " +
           "ORDER BY s.createdAt DESC")
    List<NodoShareLink> findByNodoNodoIdAndIsActiveTrue(@Param("nodoId") Long nodoId);
    
    @Query("SELECT s FROM NodoShareLink s WHERE s.createdBy.usuarioId = :createdById " +
           "ORDER BY s.createdAt DESC")
    List<NodoShareLink> findByCreatedByUsuarioIdOrderByCreatedAtDesc(@Param("createdById") Long createdById);
    
    boolean existsByShareToken(String shareToken);
}
