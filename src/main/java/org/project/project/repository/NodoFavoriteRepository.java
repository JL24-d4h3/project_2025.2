package org.project.project.repository;

import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.NodoFavorite;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodoFavoriteRepository extends JpaRepository<NodoFavorite, Long> {

    // Find by user
    List<NodoFavorite> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    // Find by user ID
    @Query("SELECT f FROM NodoFavorite f WHERE f.usuario.usuarioId = :userId " +
           "ORDER BY f.createdAt DESC")
    List<NodoFavorite> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // Find by user and node
    Optional<NodoFavorite> findByUsuarioAndNodo(Usuario usuario, Nodo nodo);

    // Find by user ID and node ID
    @Query("SELECT f FROM NodoFavorite f WHERE f.usuario.usuarioId = :userId " +
           "AND f.nodo.nodoId = :nodeId")
    Optional<NodoFavorite> findByUserIdAndNodeId(@Param("userId") Long userId, 
                                                 @Param("nodeId") Long nodeId);

    // Find by node
    List<NodoFavorite> findByNodoOrderByCreatedAtDesc(Nodo nodo);

    // Find by node ID
    @Query("SELECT f FROM NodoFavorite f WHERE f.nodo.nodoId = :nodeId " +
           "ORDER BY f.createdAt DESC")
    List<NodoFavorite> findByNodeId(@Param("nodeId") Long nodeId);

    // Check if exists by user and node
    boolean existsByUsuarioAndNodo(Usuario usuario, Nodo nodo);

    // Check if exists by user ID and node ID
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM NodoFavorite f " +
           "WHERE f.usuario.usuarioId = :userId AND f.nodo.nodoId = :nodeId")
    boolean existsByUserIdAndNodeId(@Param("userId") Long userId, @Param("nodeId") Long nodeId);

    // Delete by user and node
    void deleteByUsuarioAndNodo(Usuario usuario, Nodo nodo);

    // Delete by user ID and node ID
    @Query("DELETE FROM NodoFavorite f WHERE f.usuario.usuarioId = :userId " +
           "AND f.nodo.nodoId = :nodeId")
    void deleteByUserIdAndNodeId(@Param("userId") Long userId, @Param("nodeId") Long nodeId);

    // Delete by user
    void deleteByUsuario(Usuario usuario);

    // Delete by node
    void deleteByNodo(Nodo nodo);

    // Count favorites by user
    @Query("SELECT COUNT(f) FROM NodoFavorite f WHERE f.usuario.usuarioId = :userId")
    long countByUserId(@Param("userId") Long userId);

    // Count favorites by node
    @Query("SELECT COUNT(f) FROM NodoFavorite f WHERE f.nodo.nodoId = :nodeId")
    long countByNodeId(@Param("nodeId") Long nodeId);

    // Find by custom label
    @Query("SELECT f FROM NodoFavorite f WHERE f.usuario.usuarioId = :userId " +
           "AND f.customLabel LIKE %:label% ORDER BY f.createdAt DESC")
    List<NodoFavorite> findByUserIdAndCustomLabelContaining(@Param("userId") Long userId, 
                                                            @Param("label") String label);
}
