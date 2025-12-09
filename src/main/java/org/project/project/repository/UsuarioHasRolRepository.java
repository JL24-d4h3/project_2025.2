package org.project.project.repository;

import org.project.project.model.entity.UsuarioHasRol;
import org.project.project.model.entity.UsuarioHasRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioHasRolRepository extends JpaRepository<UsuarioHasRol, UsuarioHasRolId> {
    
    // Find by user
    @Query("SELECT uhr FROM UsuarioHasRol uhr WHERE uhr.id.usuarioId = :usuarioId")
    List<UsuarioHasRol> findById_UserId(@Param("usuarioId") Long usuarioId);
    
    // Find by role
    @Query("SELECT uhr FROM UsuarioHasRol uhr WHERE uhr.id.rolId = :rolId")
    List<UsuarioHasRol> findById_RoleId(@Param("rolId") Long rolId);
    
    // Check if relationship exists
    @Query("SELECT COUNT(uhr) > 0 FROM UsuarioHasRol uhr WHERE uhr.id.usuarioId = :usuarioId AND uhr.id.rolId = :rolId")
    boolean existsById_UserIdAndId_RoleId(@Param("usuarioId") Long usuarioId, @Param("rolId") Long rolId);
    
    // Count roles by user
    @Query("SELECT COUNT(uhr) FROM UsuarioHasRol uhr WHERE uhr.id.usuarioId = :usuarioId")
    long countByUserId(@Param("usuarioId") Long usuarioId);
    
    // Count users by role
    @Query("SELECT COUNT(uhr) FROM UsuarioHasRol uhr WHERE uhr.id.rolId = :rolId")
    long countByRoleId(@Param("rolId") Long rolId);
    
    // Find users by multiple roles
    @Query("SELECT uhr FROM UsuarioHasRol uhr WHERE uhr.id.rolId IN :rolIds")
    List<UsuarioHasRol> findByRoleIds(@Param("rolIds") List<Long> rolIds);
    
    // Find roles by multiple users
    @Query("SELECT uhr FROM UsuarioHasRol uhr WHERE uhr.id.usuarioId IN :usuarioIds")
    List<UsuarioHasRol> findByUserIds(@Param("usuarioIds") List<Long> usuarioIds);
    
    // Get role statistics
    @Query("SELECT r.nombreRol, COUNT(uhr) as userCount FROM UsuarioHasRol uhr JOIN uhr.rol r GROUP BY r.nombreRol ORDER BY userCount DESC")
    List<Object[]> getRoleStats();
    
    // Check if user has specific role
    @Query("SELECT CASE WHEN COUNT(uhr) > 0 THEN true ELSE false END FROM UsuarioHasRol uhr WHERE uhr.id.usuarioId = :usuarioId AND uhr.rol.nombreRol = :nombreRol")
    boolean hasUserRole(@Param("usuarioId") Long usuarioId, @Param("nombreRol") String nombreRol);
    
    // Find users by role name
    @Query("SELECT uhr.usuario FROM UsuarioHasRol uhr WHERE uhr.rol.nombreRol = :nombreRol")
    List<Object> findUsersByRoleName(@Param("nombreRol") String nombreRol);
}