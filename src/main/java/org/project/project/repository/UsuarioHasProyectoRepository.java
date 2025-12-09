package org.project.project.repository;

import org.project.project.model.entity.Proyecto;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.UsuarioHasProyecto;
import org.project.project.model.entity.UsuarioHasProyectoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gestionar las relaciones N:M entre Usuario y Proyecto.
 * 
 * Este repository maneja específicamente las operaciones CRUD
 * de la tabla intermedia usuario_has_proyecto con sus privilegios.
 */
@Repository
public interface UsuarioHasProyectoRepository extends JpaRepository<UsuarioHasProyecto, UsuarioHasProyectoId> {
    
    // =================== CONSULTAS BÁSICAS POR USUARIO ===================
    
    // Buscar todas las relaciones de un usuario
    @Query("SELECT uhp FROM UsuarioHasProyecto uhp WHERE uhp.id.usuarioId = :usuarioId")
    List<UsuarioHasProyecto> findById_UserId(@Param("usuarioId") Long usuarioId);
    
    // Buscar relaciones de un usuario con privilegio específico
    @Query("SELECT uhp FROM UsuarioHasProyecto uhp WHERE uhp.id.usuarioId = :usuarioId AND uhp.privilegio = :privilegio")
    List<UsuarioHasProyecto> findById_UserIdAndPrivilege(@Param("usuarioId") Long usuarioId, @Param("privilegio") UsuarioHasProyecto.PrivilegioUsuarioProyecto privilegio);
    
    // Contar proyectos de un usuario
    @Query("SELECT COUNT(uhp) FROM UsuarioHasProyecto uhp WHERE uhp.id.usuarioId = :usuarioId")
    long countByUserId(@Param("usuarioId") Long usuarioId);
    
    // Contar proyectos de un usuario con privilegio específico
    @Query("SELECT COUNT(uhp) FROM UsuarioHasProyecto uhp WHERE uhp.id.usuarioId = :usuarioId AND uhp.privilegio = :privilegio")
    long countByUserIdAndPrivilege(@Param("usuarioId") Long usuarioId, @Param("privilegio") UsuarioHasProyecto.PrivilegioUsuarioProyecto privilegio);
    
    // =================== CONSULTAS BÁSICAS POR PROYECTO ===================
    
    // Buscar todas las relaciones de un proyecto
    @Query("SELECT uhp FROM UsuarioHasProyecto uhp WHERE uhp.id.proyectoId = :proyectoId")
    List<UsuarioHasProyecto> findById_ProjectId(@Param("proyectoId") Long proyectoId);
    
    // Buscar relaciones de un proyecto con privilegio específico
    @Query("SELECT uhp FROM UsuarioHasProyecto uhp WHERE uhp.id.proyectoId = :proyectoId AND uhp.privilegio = :privilegio")
    List<UsuarioHasProyecto> findById_ProjectIdAndPrivilege(@Param("proyectoId") Long proyectoId, @Param("privilegio") UsuarioHasProyecto.PrivilegioUsuarioProyecto privilegio);
    
    // Contar usuarios de un proyecto
    @Query("SELECT COUNT(uhp) FROM UsuarioHasProyecto uhp WHERE uhp.id.proyectoId = :proyectoId")
    long countByProjectId(@Param("proyectoId") Long proyectoId);
    
    // Contar usuarios de un proyecto con privilegio específico
    @Query("SELECT COUNT(uhp) FROM UsuarioHasProyecto uhp WHERE uhp.id.proyectoId = :proyectoId AND uhp.privilegio = :privilegio")
    long countByProjectIdAndPrivilege(@Param("proyectoId") Long proyectoId, @Param("privilegio") UsuarioHasProyecto.PrivilegioUsuarioProyecto privilegio);
    
    // =================== CONSULTAS POR PRIVILEGIO ===================
    
    // Buscar por privilegio
    List<UsuarioHasProyecto> findByPrivilegio(UsuarioHasProyecto.PrivilegioUsuarioProyecto privilegio);
    
    // Contar por privilegio
    @Query("SELECT COUNT(uhp) FROM UsuarioHasProyecto uhp WHERE uhp.privilegio = :privilegio")
    long countByPrivilegio(@Param("privilegio") UsuarioHasProyecto.PrivilegioUsuarioProyecto privilegio);
    
    // =================== VERIFICACIONES DE EXISTENCIA Y RELACIONES ===================
    
    // Verificar existencia de relación específica
    @Query("SELECT COUNT(uhp) > 0 FROM UsuarioHasProyecto uhp WHERE uhp.id.usuarioId = :usuarioId AND uhp.id.proyectoId = :proyectoId")
    boolean existsById_UserIdAndId_ProjectId(@Param("usuarioId") Long usuarioId, @Param("proyectoId") Long proyectoId);
    
    // Verificar si usuario tiene privilegio específico en proyecto
    @Query("SELECT COUNT(uhp) > 0 FROM UsuarioHasProyecto uhp WHERE uhp.id.usuarioId = :usuarioId AND uhp.id.proyectoId = :proyectoId AND uhp.privilegio = :privilegio")
    boolean existsById_UserIdAndId_ProjectIdAndPrivilege(@Param("usuarioId") Long usuarioId, @Param("proyectoId") Long proyectoId, @Param("privilegio") UsuarioHasProyecto.PrivilegioUsuarioProyecto privilegio);
    
    // Obtener relación específica
    @Query("SELECT uhp FROM UsuarioHasProyecto uhp WHERE uhp.id.usuarioId = :usuarioId AND uhp.id.proyectoId = :proyectoId")
    Optional<UsuarioHasProyecto> findById_UserIdAndId_ProjectId(@Param("usuarioId") Long usuarioId, @Param("proyectoId") Long proyectoId);
    
    // =================== CONSULTAS QUE RETORNAN ENTIDADES RELACIONADAS ===================
    
    // Obtener proyectos de un usuario
    @Query("SELECT uhp.proyecto FROM UsuarioHasProyecto uhp WHERE uhp.id.usuarioId = :usuarioId")
    List<Proyecto> findProjectsByUserId(@Param("usuarioId") Long usuarioId);
    
    // Obtener proyectos de un usuario con privilegio específico
    @Query("SELECT uhp.proyecto FROM UsuarioHasProyecto uhp WHERE uhp.id.usuarioId = :usuarioId AND uhp.privilegio = :privilegio")
    List<Proyecto> findProjectsByUserIdAndPrivilege(@Param("usuarioId") Long usuarioId, @Param("privilegio") UsuarioHasProyecto.PrivilegioUsuarioProyecto privilegio);
    
    // Obtener proyectos donde el usuario es administrador
    @Query("SELECT uhp.proyecto FROM UsuarioHasProyecto uhp WHERE uhp.id.usuarioId = :usuarioId AND uhp.privilegio = 'ADMIN'")
    List<Proyecto> findAdminProjectsByUser(@Param("usuarioId") Long usuarioId);
    
    // Obtener usuarios de un proyecto
    @Query("SELECT uhp.usuario FROM UsuarioHasProyecto uhp WHERE uhp.id.proyectoId = :proyectoId")
    List<Usuario> findUsersByProjectId(@Param("proyectoId") Long proyectoId);
    
    // Obtener usuarios de un proyecto con privilegio específico
    @Query("SELECT uhp.usuario FROM UsuarioHasProyecto uhp WHERE uhp.id.proyectoId = :proyectoId AND uhp.privilegio = :privilegio")
    List<Usuario> findUsersByProjectIdAndPrivilege(@Param("proyectoId") Long proyectoId, @Param("privilegio") UsuarioHasProyecto.PrivilegioUsuarioProyecto privilegio);
    
    // Obtener colaboradores de un proyecto
    @Query("SELECT uhp.usuario FROM UsuarioHasProyecto uhp WHERE uhp.id.proyectoId = :proyectoId")
    List<Usuario> findCollaboratorsByProject(@Param("proyectoId") Long proyectoId);
    
    // =================== CONSULTAS DE ANÁLISIS Y REPORTES ===================
    
    // Obtener estadísticas de privilegios por proyecto
    @Query("SELECT uhp.privilegio, COUNT(uhp) FROM UsuarioHasProyecto uhp WHERE uhp.id.proyectoId = :proyectoId GROUP BY uhp.privilegio")
    List<Object[]> getPrivilegeStatsForProject(@Param("proyectoId") Long proyectoId);
    
    // Obtener estadísticas de privilegios por usuario
    @Query("SELECT uhp.privilegio, COUNT(uhp) FROM UsuarioHasProyecto uhp WHERE uhp.id.usuarioId = :usuarioId GROUP BY uhp.privilegio")
    List<Object[]> getPrivilegeStatsForUser(@Param("usuarioId") Long usuarioId);
    
    // Encontrar usuarios con múltiples privilegios ADMIN
    @Query("SELECT u FROM Usuario u WHERE u.usuarioId IN (SELECT uhp.id.usuarioId FROM UsuarioHasProyecto uhp WHERE uhp.privilegio = 'ADMIN' GROUP BY uhp.id.usuarioId HAVING COUNT(uhp) > 1)")
    List<Usuario> findUsersWithMultipleAdminPrivileges();
    
    // Encontrar proyectos sin administradores
    @Query("SELECT p FROM Proyecto p WHERE p.proyectoId NOT IN (SELECT uhp.id.proyectoId FROM UsuarioHasProyecto uhp WHERE uhp.privilegio = 'ADMIN')")
    List<Proyecto> findProjectsWithoutAdmins();
}