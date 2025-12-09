package org.project.project.repository;

import org.project.project.model.entity.CategoriaHasRepositorio;
import org.project.project.model.entity.CategoriaHasRepositorioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaHasRepositorioRepository extends JpaRepository<CategoriaHasRepositorio, CategoriaHasRepositorioId> {
    
    // Find by category
    @Query("SELECT chr FROM CategoriaHasRepositorio chr WHERE chr.id.categoriaId = :categoriaId")
    List<CategoriaHasRepositorio> findById_CategoryId(@Param("categoriaId") Long categoriaId);
    
    // Find by repository
    @Query("SELECT chr FROM CategoriaHasRepositorio chr WHERE chr.id.repositorioId = :repositorioId")
    List<CategoriaHasRepositorio> findById_RepositoryId(@Param("repositorioId") Long repositorioId);
    
    // Check if relationship exists
    @Query("SELECT COUNT(chr) > 0 FROM CategoriaHasRepositorio chr WHERE chr.id.categoriaId = :categoriaId AND chr.id.repositorioId = :repositorioId")
    boolean existsById_CategoryIdAndId_RepositoryId(@Param("categoriaId") Long categoriaId, @Param("repositorioId") Long repositorioId);
    
    // Count repositories by category
    @Query("SELECT COUNT(chr) FROM CategoriaHasRepositorio chr WHERE chr.id.categoriaId = :categoriaId")
    long countByCategoryId(@Param("categoriaId") Long categoriaId);
    
    // Count categories by repository
    @Query("SELECT COUNT(chr) FROM CategoriaHasRepositorio chr WHERE chr.id.repositorioId = :repositorioId")
    long countByRepositoryId(@Param("repositorioId") Long repositorioId);
    
    // Find repositories by multiple categories
    @Query("SELECT DISTINCT chr.repositorio FROM CategoriaHasRepositorio chr WHERE chr.categoria.idCategoria IN :categoriaIds")
    List<Object> findRepositoriesByCategoryIds(@Param("categoriaIds") List<Long> categoriaIds);
}