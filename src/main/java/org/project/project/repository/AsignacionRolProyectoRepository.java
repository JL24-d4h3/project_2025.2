package org.project.project.repository;

import org.project.project.model.entity.AsignacionRolProyecto;
import org.project.project.model.entity.AsignacionRolProyectoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionRolProyectoRepository extends JpaRepository<AsignacionRolProyecto, AsignacionRolProyectoId> {

    // =================== BÚSQUEDAS POR ROL ===================

    @Query("SELECT a FROM AsignacionRolProyecto a WHERE a.rolProyecto.rolProyectoId = :rolProyectoId")
    List<AsignacionRolProyecto> findByRolProyectoId(@Param("rolProyectoId") Long rolProyectoId);

    // =================== BÚSQUEDAS POR USUARIO Y PROYECTO ===================

    @Query("SELECT a FROM AsignacionRolProyecto a WHERE a.id.usuarioHasProyectoUsuarioUsuarioId = :usuarioId AND a.id.usuarioHasProyectoProyectoProyectoId = :proyectoId")
    List<AsignacionRolProyecto> findByUsuarioIdAndProyectoId(@Param("usuarioId") Long usuarioId, @Param("proyectoId") Long proyectoId);

    // =================== BÚSQUEDAS POR PROYECTO ===================

    @Query("SELECT a FROM AsignacionRolProyecto a WHERE a.id.usuarioHasProyectoProyectoProyectoId = :proyectoId")
    List<AsignacionRolProyecto> findByProyectoId(@Param("proyectoId") Long proyectoId);

    // =================== ESTADÍSTICAS ===================

    @Query("SELECT COUNT(a) FROM AsignacionRolProyecto a WHERE a.id.rolProyectoRolProyectoId = :rolProyectoId")
    long countByRolProyectoId(@Param("rolProyectoId") Long rolProyectoId);

    @Query("SELECT COUNT(a) FROM AsignacionRolProyecto a WHERE a.id.usuarioHasProyectoUsuarioUsuarioId = :usuarioId")
    long countByUsuarioId(@Param("usuarioId") Long usuarioId);
}
