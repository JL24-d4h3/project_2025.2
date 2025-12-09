package org.project.project.repository;

import org.project.project.model.entity.UsuarioHasEquipo;
import org.project.project.model.entity.UsuarioHasEquipoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioHasEquipoRepository extends JpaRepository<UsuarioHasEquipo, UsuarioHasEquipoId> {
    
    // Find by user
    @Query("SELECT uhe FROM UsuarioHasEquipo uhe WHERE uhe.id.usuarioId = :usuarioId")
    List<UsuarioHasEquipo> findById_UserId(@Param("usuarioId") Long usuarioId);
    
    // Find by team
    @Query("SELECT uhe FROM UsuarioHasEquipo uhe WHERE uhe.id.equipoId = :equipoId")
    List<UsuarioHasEquipo> findById_TeamId(@Param("equipoId") Long equipoId);
    
    // Check if relationship exists
    @Query("SELECT COUNT(uhe) > 0 FROM UsuarioHasEquipo uhe WHERE uhe.id.usuarioId = :usuarioId AND uhe.id.equipoId = :equipoId")
    boolean existsById_UserIdAndId_TeamId(@Param("usuarioId") Long usuarioId, @Param("equipoId") Long equipoId);
    
    // Count teams by user
    @Query("SELECT COUNT(uhe) FROM UsuarioHasEquipo uhe WHERE uhe.id.usuarioId = :usuarioId")
    long countByUserId(@Param("usuarioId") Long usuarioId);
    
    // Count users by team
    @Query("SELECT COUNT(uhe) FROM UsuarioHasEquipo uhe WHERE uhe.id.equipoId = :equipoId")
    long countByTeamId(@Param("equipoId") Long equipoId);
    
    // Find teams by multiple users
    @Query("SELECT uhe FROM UsuarioHasEquipo uhe WHERE uhe.id.usuarioId IN :usuarioIds")
    List<UsuarioHasEquipo> findByUserIds(@Param("usuarioIds") List<Long> usuarioIds);
    
    // Find users by multiple teams
    @Query("SELECT uhe FROM UsuarioHasEquipo uhe WHERE uhe.id.equipoId IN :equipoIds")
    List<UsuarioHasEquipo> findByTeamIds(@Param("equipoIds") List<Long> equipoIds);
    
    // Get team membership statistics
    @Query("SELECT e.nombreEquipo, COUNT(uhe) as memberCount FROM UsuarioHasEquipo uhe JOIN uhe.equipo e GROUP BY e ORDER BY memberCount DESC")
    List<Object[]> getTeamMembershipStats();
    
    // Check if user belongs to any specific team
    @Query("SELECT CASE WHEN COUNT(uhe) > 0 THEN true ELSE false END FROM UsuarioHasEquipo uhe WHERE uhe.id.usuarioId = :usuarioId AND uhe.id.equipoId IN :equipoIds")
    boolean isUserInAnyTeam(@Param("usuarioId") Long usuarioId, @Param("equipoIds") List<Long> equipoIds);
}