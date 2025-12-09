package org.project.project.repository;

import org.project.project.model.entity.EtiquetaHasApi;
import org.project.project.model.entity.EtiquetaHasApiId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtiquetaHasApiRepository extends JpaRepository<EtiquetaHasApi, EtiquetaHasApiId> {
    
    // Find by tag
    @Query("SELECT eha FROM EtiquetaHasApi eha WHERE eha.id.etiquetaId = :etiquetaId")
    List<EtiquetaHasApi> findById_TagId(@Param("etiquetaId") Long etiquetaId);
    
    // Find by API
    @Query("SELECT eha FROM EtiquetaHasApi eha WHERE eha.id.apiId = :apiId")
    List<EtiquetaHasApi> findById_ApiId(@Param("apiId") Long apiId);
    
    // Check if relationship exists
    @Query("SELECT COUNT(eha) > 0 FROM EtiquetaHasApi eha WHERE eha.id.etiquetaId = :etiquetaId AND eha.id.apiId = :apiId")
    boolean existsById_TagIdAndId_ApiId(@Param("etiquetaId") Long etiquetaId, @Param("apiId") Long apiId);
    
    // Count APIs by tag
    @Query("SELECT COUNT(eha) FROM EtiquetaHasApi eha WHERE eha.id.etiquetaId = :etiquetaId")
    long countByTagId(@Param("etiquetaId") Long etiquetaId);
    
    // Count tags by API
    @Query("SELECT COUNT(eha) FROM EtiquetaHasApi eha WHERE eha.id.apiId = :apiId")
    long countByApiId(@Param("apiId") Long apiId);
    
    // Find APIs by multiple tags
    @Query("SELECT DISTINCT eha.api FROM EtiquetaHasApi eha WHERE eha.etiqueta.tagId IN :etiquetaIds")
    List<Object> findApisByTagIds(@Param("etiquetaIds") List<Long> etiquetaIds);
    
    // Find most popular tags (with more APIs)
    @Query("SELECT eha.etiqueta, COUNT(eha.api) as apiCount FROM EtiquetaHasApi eha GROUP BY eha.etiqueta ORDER BY apiCount DESC")
    List<Object[]> findMostPopularTags();
}