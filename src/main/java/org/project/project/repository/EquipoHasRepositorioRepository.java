package org.project.project.repository;

import org.project.project.model.entity.EquipoHasRepositorio;
import org.project.project.model.entity.EquipoHasRepositorioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoHasRepositorioRepository extends JpaRepository<EquipoHasRepositorio, EquipoHasRepositorioId> {
    
    // Find by team
    @Query("SELECT ehr FROM EquipoHasRepositorio ehr WHERE ehr.id.equipoId = :equipoId")
    List<EquipoHasRepositorio> findById_TeamId(@Param("equipoId") Long equipoId);
    
    // Find by repository
    @Query("SELECT ehr FROM EquipoHasRepositorio ehr WHERE ehr.id.repositorioId = :repositorioId")
    List<EquipoHasRepositorio> findById_RepositoryId(@Param("repositorioId") Long repositorioId);
    
    // Find by privilege
    @Query("SELECT ehr FROM EquipoHasRepositorio ehr WHERE ehr.privilegio = :privilegio")
    List<EquipoHasRepositorio> findByPrivilege(@Param("privilegio") EquipoHasRepositorio.PrivilegioEquipoRepositorio privilegio);
    
    // Check if relationship exists
    @Query("SELECT COUNT(ehr) > 0 FROM EquipoHasRepositorio ehr WHERE ehr.id.equipoId = :equipoId AND ehr.id.repositorioId = :repositorioId")
    boolean existsById_TeamIdAndId_RepositoryId(@Param("equipoId") Long equipoId, @Param("repositorioId") Long repositorioId);
    
    // Count repositories by team
    @Query("SELECT COUNT(ehr) FROM EquipoHasRepositorio ehr WHERE ehr.id.equipoId = :equipoId")
    long countByTeamId(@Param("equipoId") Long equipoId);
    
    // Count teams by repository
    @Query("SELECT COUNT(ehr) FROM EquipoHasRepositorio ehr WHERE ehr.id.repositorioId = :repositorioId")
    long countByRepositoryId(@Param("repositorioId") Long repositorioId);
    
    // Find by team and specific privilege
    @Query("SELECT ehr FROM EquipoHasRepositorio ehr WHERE ehr.id.equipoId = :equipoId AND ehr.privilegio = :privilegio")
    List<EquipoHasRepositorio> findById_TeamIdAndPrivilege(@Param("equipoId") Long equipoId, @Param("privilegio") EquipoHasRepositorio.PrivilegioEquipoRepositorio privilegio);
    
    // Find by team and repository
    @Query("SELECT ehr FROM EquipoHasRepositorio ehr WHERE ehr.id.equipoId = :equipoId AND ehr.id.repositorioId = :repositorioId")
    EquipoHasRepositorio findByEquipoIdAndRepositorioId(@Param("equipoId") Long equipoId, @Param("repositorioId") Long repositorioId);
    
    // Check if team name exists in repository (case-insensitive)
    @Query("SELECT COUNT(ehr) > 0 FROM EquipoHasRepositorio ehr JOIN ehr.equipo e WHERE ehr.id.repositorioId = :repositorioId AND LOWER(e.nombreEquipo) = LOWER(:teamName)")
    boolean existsByRepositoryIdAndTeamName(@Param("repositorioId") Long repositorioId, @Param("teamName") String teamName);
}