package org.project.project.repository;

import org.project.project.model.entity.RolProyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolProyectoRepository extends JpaRepository<RolProyecto, Long> {

    // =================== BÚSQUEDAS BÁSICAS ===================

    List<RolProyecto> findByProyecto_ProyectoId(Long proyectoId);

    @Query("SELECT r FROM RolProyecto r WHERE r.proyecto.proyectoId = :proyectoId AND r.activo = true")
    List<RolProyecto> findActiveRolesByProyectoId(@Param("proyectoId") Long proyectoId);

    Optional<RolProyecto> findByProyecto_ProyectoIdAndNombreRolProyecto(Long proyectoId, String nombreRol);

    // =================== BÚSQUEDA POR NOMBRE ===================

    List<RolProyecto> findByNombreRolProyectoContainingIgnoreCase(String nombre);

    @Query("SELECT r FROM RolProyecto r WHERE r.proyecto.proyectoId = :proyectoId AND LOWER(r.nombreRolProyecto) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<RolProyecto> findByProyectoIdAndNombreContaining(@Param("proyectoId") Long proyectoId, @Param("nombre") String nombre);

    // =================== VERIFICAR EXISTENCIA ===================

    boolean existsByProyecto_ProyectoIdAndNombreRolProyecto(Long proyectoId, String nombreRol);

    // =================== ROLES POR CREADOR ===================

    List<RolProyecto> findByCreadoPor_UsuarioId(Long usuarioId);

    @Query("SELECT COUNT(r) FROM RolProyecto r WHERE r.creadoPor.usuarioId = :usuarioId")
    long countRolesByCreador(@Param("usuarioId") Long usuarioId);

    // =================== ESTADÍSTICAS ===================

    long countByProyecto_ProyectoId(Long proyectoId);

    @Query("SELECT COUNT(r) FROM RolProyecto r WHERE r.proyecto.proyectoId = :proyectoId AND r.activo = true")
    long countActiveRolesByProyectoId(@Param("proyectoId") Long proyectoId);

    // =================== ROLES INACTIVOS ===================

    @Query("SELECT r FROM RolProyecto r WHERE r.proyecto.proyectoId = :proyectoId AND r.activo = false")
    List<RolProyecto> findInactiveRolesByProyectoId(@Param("proyectoId") Long proyectoId);
}

