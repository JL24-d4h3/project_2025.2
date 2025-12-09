package org.project.project.repository;

import org.project.project.model.entity.CategoriaHasProyecto;
import org.project.project.model.entity.CategoriaHasProyectoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaHasProyectoRepository extends JpaRepository<CategoriaHasProyecto, CategoriaHasProyectoId> {
    
    // Buscar por categoria
    @Query("SELECT chp FROM CategoriaHasProyecto chp WHERE chp.id.categoriaId = :categoriaId")
    List<CategoriaHasProyecto> findById_CategoryId(@Param("categoriaId") Long categoriaId);
    
    // Buscar por proyecto
    @Query("SELECT chp FROM CategoriaHasProyecto chp WHERE chp.id.proyectoId = :proyectoId")
    List<CategoriaHasProyecto> findById_ProjectId(@Param("proyectoId") Long proyectoId);
    
    // Verificar existencia de relación
    @Query("SELECT COUNT(chp) > 0 FROM CategoriaHasProyecto chp WHERE chp.id.categoriaId = :categoriaId AND chp.id.proyectoId = :proyectoId")
    boolean existsById_CategoryIdAndId_ProjectId(@Param("categoriaId") Long categoriaId, @Param("proyectoId") Long proyectoId);
    
    // Contar proyectos por categoría
    @Query("SELECT COUNT(chp) FROM CategoriaHasProyecto chp WHERE chp.id.categoriaId = :categoriaId")
    long countByCategoryId(@Param("categoriaId") Long categoriaId);
    
    // Contar categorías por proyecto
    @Query("SELECT COUNT(chp) FROM CategoriaHasProyecto chp WHERE chp.id.proyectoId = :proyectoId")
    long countByProjectId(@Param("proyectoId") Long proyectoId);
    
    // Obtener proyectos de múltiples categorías
    @Query("SELECT DISTINCT chp.proyecto FROM CategoriaHasProyecto chp WHERE chp.categoria.idCategoria IN :categoriaIds")
    List<Object> findProjectsByCategoryIds(@Param("categoriaIds") List<Long> categoriaIds);
}