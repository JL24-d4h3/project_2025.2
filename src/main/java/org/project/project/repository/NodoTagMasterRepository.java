package org.project.project.repository;

import org.project.project.model.entity.NodoTagMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodoTagMasterRepository extends JpaRepository<NodoTagMaster, Long> {

    // Find by exact name
    Optional<NodoTagMaster> findByName(String nombre);

    // Find by name containing (case insensitive)
    @Query("SELECT ntm FROM NodoTagMaster ntm WHERE LOWER(ntm.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<NodoTagMaster> findByNameContainingIgnoreCase(@Param("nombre") String nombre);

    // Check if tag exists
    boolean existsByName(String nombre);

    // Find most used tags
    @Query("SELECT ntm, COUNT(nt) as usageCount FROM NodoTagMaster ntm " +
           "LEFT JOIN ntm.nodoTags nt " +
           "GROUP BY ntm " +
           "ORDER BY usageCount DESC")
    List<Object[]> findMostUsedTags();

    // Find unused tags
    @Query("SELECT ntm FROM NodoTagMaster ntm WHERE ntm.nodoTags IS EMPTY")
    List<NodoTagMaster> findUnusedTags();

    // Get tags by usage count
    @Query("SELECT ntm FROM NodoTagMaster ntm WHERE SIZE(ntm.nodoTags) >= :minUsage ORDER BY SIZE(ntm.nodoTags) DESC")
    List<NodoTagMaster> findTagsWithMinimumUsage(@Param("minUsage") int minUsage);

    // Find tags by name pattern
    @Query("SELECT ntm FROM NodoTagMaster ntm WHERE ntm.nombre LIKE :pattern ORDER BY ntm.nombre")
    List<NodoTagMaster> findByNamePattern(@Param("pattern") String pattern);
}