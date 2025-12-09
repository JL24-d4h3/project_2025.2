package org.project.project.repository;

import org.project.project.model.entity.EquipoHasProyecto;
import org.project.project.model.entity.EquipoHasProyectoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoHasProyectoRepository extends JpaRepository<EquipoHasProyecto, EquipoHasProyectoId> {
    
    // Find by team
    @Query("SELECT ehp FROM EquipoHasProyecto ehp WHERE ehp.id.equipoId = :equipoId")
    List<EquipoHasProyecto> findById_TeamId(@Param("equipoId") Long equipoId);
    
    // Find by project
    @Query("SELECT ehp FROM EquipoHasProyecto ehp WHERE ehp.id.proyectoId = :proyectoId")
    List<EquipoHasProyecto> findById_ProjectId(@Param("proyectoId") Long proyectoId);
    
    // Find by privilege
    @Query("SELECT ehp FROM EquipoHasProyecto ehp WHERE ehp.privilegio = :privilegio")
    List<EquipoHasProyecto> findByPrivilege(@Param("privilegio") EquipoHasProyecto.PrivilegioEquipoProyecto privilegio);
    
    // Check if relationship exists
    @Query("SELECT COUNT(ehp) > 0 FROM EquipoHasProyecto ehp WHERE ehp.id.equipoId = :equipoId AND ehp.id.proyectoId = :proyectoId")
    boolean existsById_TeamIdAndId_ProjectId(@Param("equipoId") Long equipoId, @Param("proyectoId") Long proyectoId);
    
    // Count projects by team
    @Query("SELECT COUNT(ehp) FROM EquipoHasProyecto ehp WHERE ehp.id.equipoId = :equipoId")
    long countByTeamId(@Param("equipoId") Long equipoId);
    
    // Count teams by project
    @Query("SELECT COUNT(ehp) FROM EquipoHasProyecto ehp WHERE ehp.id.proyectoId = :proyectoId")
    long countByProjectId(@Param("proyectoId") Long proyectoId);
    
    // Find by team and specific privilege
    @Query("SELECT ehp FROM EquipoHasProyecto ehp WHERE ehp.id.equipoId = :equipoId AND ehp.privilegio = :privilegio")
    List<EquipoHasProyecto> findById_TeamIdAndPrivilege(@Param("equipoId") Long equipoId, @Param("privilegio") EquipoHasProyecto.PrivilegioEquipoProyecto privilegio);
    
    // Find by equipo ID and proyecto ID
    @Query("SELECT ehp FROM EquipoHasProyecto ehp WHERE ehp.id.equipoId = :equipoId AND ehp.id.proyectoId = :proyectoId")
    EquipoHasProyecto findByEquipoIdAndProyectoId(@Param("equipoId") Long equipoId, @Param("proyectoId") Long proyectoId);
    
    // Check if team name exists in project (case-insensitive)
    @Query("SELECT COUNT(ehp) > 0 FROM EquipoHasProyecto ehp JOIN ehp.equipo e WHERE ehp.id.proyectoId = :proyectoId AND LOWER(e.nombreEquipo) = LOWER(:teamName)")
    boolean existsByProjectIdAndTeamName(@Param("proyectoId") Long proyectoId, @Param("teamName") String teamName);
}