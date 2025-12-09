package org.project.project.repository;

import org.project.project.model.entity.PermisoNodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermisoNodoRepository extends JpaRepository<PermisoNodo, Long> {

    // Find permissions by node
    List<PermisoNodo> findByNodo_NodoId(Long nodoId);

    // Find permissions by user
    List<PermisoNodo> findByUserId(Long usuarioId);

    // Find permissions by team
    List<PermisoNodo> findByTeamId(Long equipoId);

    // Find specific user permission for node
    Optional<PermisoNodo> findByNodo_NodoIdAndUserId(Long nodoId, Long usuarioId);

    // Find specific team permission for node
    Optional<PermisoNodo> findByNodo_NodoIdAndTeamId(Long nodoId, Long equipoId);

    // Check if user has specific permission on node
    @Query("SELECT COUNT(np) > 0 FROM PermisoNodo np WHERE np.nodo.nodoId = :nodoId AND np.userId = :usuarioId " +
           "AND np.permiso = :permiso")
    boolean hasUserPermission(@Param("nodoId") Long nodoId, @Param("usuarioId") Long usuarioId, 
                             @Param("permiso") PermisoNodo.TipoPermiso permiso);

    // Check if team has specific permission on node
    @Query("SELECT COUNT(np) > 0 FROM PermisoNodo np WHERE np.nodo.nodoId = :nodoId AND np.teamId = :equipoId " +
           "AND np.permiso = :permiso")
    boolean hasTeamPermission(@Param("nodoId") Long nodoId, @Param("equipoId") Long equipoId, 
                             @Param("permiso") PermisoNodo.TipoPermiso permiso);

    // Find admin permissions
    List<PermisoNodo> findByPermisoAndNodo_NodoId(PermisoNodo.TipoPermiso permiso, Long nodoId);

    // Find inheritable permissions
    List<PermisoNodo> findByNodo_NodoIdAndInheritableTrue(Long nodoId);

    // Find user permissions with minimum level
    @Query("SELECT np FROM PermisoNodo np WHERE np.userId = :usuarioId " +
           "AND (np.permiso = 'ADMIN' OR (:minPermiso = 'WRITE' AND np.permiso IN ('ADMIN', 'WRITE')) " +
           "OR (:minPermiso = 'read' AND np.permiso IN ('ADMIN', 'WRITE', 'read')))")
    List<PermisoNodo> findUserPermissionsWithMinLevel(@Param("usuarioId") Long usuarioId, 
                                                         @Param("minPermiso") PermisoNodo.TipoPermiso minPermiso);

    // Count permissions by nodo
    long countByNodo_NodoId(Long nodoId);

    // Delete permissions by nodo
    void deleteByNodo_NodoId(Long nodoId);

    // Delete permissions by usuario
    void deleteByUserId(Long usuarioId);

    // Delete permissions by equipo
    void deleteByTeamId(Long equipoId);

    // Find nodes where user has admin permission
    @Query("SELECT np.nodo.nodoId FROM PermisoNodo np WHERE np.userId = :usuarioId AND np.permiso = 'ADMIN'")
    List<Long> findNodosWhereUserIsAdmin(@Param("usuarioId") Long usuarioId);

    // Find effective permissions (user + team permissions)
    @Query("SELECT np FROM PermisoNodo np WHERE np.nodo.nodoId = :nodoId AND " +
           "(np.userId = :usuarioId OR np.teamId IN :equipoIds)")
    List<PermisoNodo> findEffectivePermissions(@Param("nodoId") Long nodoId, 
                                                  @Param("usuarioId") Long usuarioId, 
                                                  @Param("equipoIds") List<Long> equipoIds);
}