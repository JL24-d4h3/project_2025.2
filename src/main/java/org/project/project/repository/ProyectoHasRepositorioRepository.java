package org.project.project.repository;

import org.project.project.model.entity.ProyectoHasRepositorio;
import org.project.project.model.entity.ProyectoHasRepositorioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProyectoHasRepositorioRepository extends JpaRepository<ProyectoHasRepositorio, ProyectoHasRepositorioId> {
    
    // Find by project
    @Query("SELECT phr FROM ProyectoHasRepositorio phr WHERE phr.id.proyectoId = :proyectoId")
    List<ProyectoHasRepositorio> findById_ProjectId(@Param("proyectoId") Long proyectoId);
    
    // Find by repository
    @Query("SELECT phr FROM ProyectoHasRepositorio phr WHERE phr.id.repositorioId = :repositorioId")
    List<ProyectoHasRepositorio> findById_RepositoryId(@Param("repositorioId") Long repositorioId);
    
    // Check if relationship exists
    @Query("SELECT COUNT(phr) > 0 FROM ProyectoHasRepositorio phr WHERE phr.id.proyectoId = :proyectoId AND phr.id.repositorioId = :repositorioId")
    boolean existsById_ProjectIdAndId_RepositoryId(@Param("proyectoId") Long proyectoId, @Param("repositorioId") Long repositorioId);
    
    // Count repositories by project
    @Query("SELECT COUNT(phr) FROM ProyectoHasRepositorio phr WHERE phr.id.proyectoId = :proyectoId")
    long countByProjectId(@Param("proyectoId") Long proyectoId);
    
    // Count projects by repository
    @Query("SELECT COUNT(phr) FROM ProyectoHasRepositorio phr WHERE phr.id.repositorioId = :repositorioId")
    long countByRepositoryId(@Param("repositorioId") Long repositorioId);
    
    // Find repositories by multiple projects
    @Query("SELECT phr FROM ProyectoHasRepositorio phr WHERE phr.id.proyectoId IN :proyectoIds")
    List<ProyectoHasRepositorio> findByProjectIds(@Param("proyectoIds") List<Long> proyectoIds);
    
    // Find projects by multiple repositories
    @Query("SELECT phr FROM ProyectoHasRepositorio phr WHERE phr.id.repositorioId IN :repositorioIds")
    List<ProyectoHasRepositorio> findByRepositoryIds(@Param("repositorioIds") List<Long> repositorioIds);
    
    // Get project-repository relationship statistics
    @Query("SELECT p.nombreProyecto, COUNT(phr) as repoCount FROM ProyectoHasRepositorio phr JOIN phr.proyecto p GROUP BY p ORDER BY repoCount DESC")
    List<Object[]> getProjectRepositoryStats();
}