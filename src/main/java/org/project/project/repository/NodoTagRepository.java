package org.project.project.repository;

import org.project.project.model.entity.NodoTag;
import org.project.project.model.entity.NodoTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NodoTagRepository extends JpaRepository<NodoTag, NodoTagId> {

    // Find tags by nodo
    List<NodoTag> findByIdNodoId(Long nodoId);

    // Find nodes by tag
    List<NodoTag> findByIdTagId(Long tagId);

    // Find nodes by tag name
    @Query("SELECT nt FROM NodoTag nt JOIN nt.tagMaster tm WHERE tm.nombre = :tagName")
    List<NodoTag> findByTagName(@Param("tagName") String tagName);

    // Count tags by nodo
    long countByIdNodoId(Long nodoId);

    // Count nodes by tag
    long countByIdTagId(Long tagId);

    // Check if nodo has specific tag
    boolean existsByIdNodoIdAndIdTagId(Long nodoId, Long tagId);

    // Find tags by multiple nodos
    @Query("SELECT nt FROM NodoTag nt WHERE nt.id.nodoId IN :nodoIds")
    List<NodoTag> findByNodoIds(@Param("nodoIds") List<Long> nodoIds);

    // Find common tags between nodos
    @Query("SELECT nt.tagMaster, COUNT(nt) FROM NodoTag nt WHERE nt.id.nodoId IN :nodoIds " +
           "GROUP BY nt.tagMaster HAVING COUNT(nt) = :nodoCount")
    List<Object[]> findCommonTagsByNodos(@Param("nodoIds") List<Long> nodoIds, @Param("nodoCount") long nodoCount);

    // Delete tags by nodo
    void deleteByIdNodoId(Long nodoId);

    // Delete tags by tag master
    void deleteByIdTagId(Long tagId);

    // Find popular tag combinations
    @Query("SELECT nt1.tagMaster, nt2.tagMaster, COUNT(*) FROM NodoTag nt1 " +
           "JOIN NodoTag nt2 ON nt1.id.nodoId = nt2.id.nodoId " +
           "WHERE nt1.id.tagId < nt2.id.tagId " +
           "GROUP BY nt1.tagMaster, nt2.tagMaster " +
           "ORDER BY COUNT(*) DESC")
    List<Object[]> findPopularTagCombinations();
}