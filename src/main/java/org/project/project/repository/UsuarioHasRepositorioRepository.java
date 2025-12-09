package org.project.project.repository;

import org.project.project.model.entity.Repositorio;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.UsuarioHasRepositorio;
import org.project.project.model.entity.UsuarioHasRepositorioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gestionar las relaciones N:M entre Usuario y Repositorio.
 * 
 * Este repository maneja específicamente las operaciones CRUD
 * de la tabla intermedia usuario_has_repositorio con sus privilegios.
 */
@Repository
public interface UsuarioHasRepositorioRepository extends JpaRepository<UsuarioHasRepositorio, UsuarioHasRepositorioId> {
    
    // =================== CONSULTAS BÁSICAS POR USUARIO ===================
    
    // Buscar todas las relaciones de un usuario
    @Query("SELECT uhr FROM UsuarioHasRepositorio uhr WHERE uhr.id.usuarioId = :usuarioId")
    List<UsuarioHasRepositorio> findById_UserId(@Param("usuarioId") Long usuarioId);
    
    // Buscar relaciones de un usuario con privilegio específico
    @Query("SELECT uhr FROM UsuarioHasRepositorio uhr WHERE uhr.id.usuarioId = :usuarioId AND uhr.privilegio = :privilegio")
    List<UsuarioHasRepositorio> findById_UserIdAndPrivilege(@Param("usuarioId") Long usuarioId, @Param("privilegio") UsuarioHasRepositorio.PrivilegioUsuarioRepositorio privilegio);
    
    // Contar repositorios de un usuario
    @Query("SELECT COUNT(uhr) FROM UsuarioHasRepositorio uhr WHERE uhr.id.usuarioId = :usuarioId")
    long countByUserId(@Param("usuarioId") Long usuarioId);
    
    // Contar repositorios de un usuario con privilegio específico
    @Query("SELECT COUNT(uhr) FROM UsuarioHasRepositorio uhr WHERE uhr.id.usuarioId = :usuarioId AND uhr.privilegio = :privilegio")
    long countByUserIdAndPrivilege(@Param("usuarioId") Long usuarioId, @Param("privilegio") UsuarioHasRepositorio.PrivilegioUsuarioRepositorio privilegio);
    
    // =================== CONSULTAS BÁSICAS POR REPOSITORIO ===================
    
    // Buscar todas las relaciones de un repositorio
    @Query("SELECT uhr FROM UsuarioHasRepositorio uhr WHERE uhr.id.repositorioId = :repositorioId")
    List<UsuarioHasRepositorio> findById_RepositoryId(@Param("repositorioId") Long repositorioId);
    
    // Buscar relaciones de un repositorio con privilegio específico
    @Query("SELECT uhr FROM UsuarioHasRepositorio uhr WHERE uhr.id.repositorioId = :repositorioId AND uhr.privilegio = :privilegio")
    List<UsuarioHasRepositorio> findById_RepositoryIdAndPrivilege(@Param("repositorioId") Long repositorioId, @Param("privilegio") UsuarioHasRepositorio.PrivilegioUsuarioRepositorio privilegio);
    
    // Contar usuarios de un repositorio
    @Query("SELECT COUNT(uhr) FROM UsuarioHasRepositorio uhr WHERE uhr.id.repositorioId = :repositorioId")
    long countByRepositoryId(@Param("repositorioId") Long repositorioId);
    
    // Contar usuarios de un repositorio con privilegio específico
    @Query("SELECT COUNT(uhr) FROM UsuarioHasRepositorio uhr WHERE uhr.id.repositorioId = :repositorioId AND uhr.privilegio = :privilegio")
    long countByRepositoryIdAndPrivilege(@Param("repositorioId") Long repositorioId, @Param("privilegio") UsuarioHasRepositorio.PrivilegioUsuarioRepositorio privilegio);
    
    // =================== CONSULTAS POR PRIVILEGIO ===================
    
    // Buscar por privilegio
    List<UsuarioHasRepositorio> findByPrivilegio(UsuarioHasRepositorio.PrivilegioUsuarioRepositorio privilegio);
    
    // Contar por privilegio
    @Query("SELECT COUNT(uhr) FROM UsuarioHasRepositorio uhr WHERE uhr.privilegio = :privilegio")
    long countByPrivilegio(@Param("privilegio") UsuarioHasRepositorio.PrivilegioUsuarioRepositorio privilegio);
    
    // =================== VERIFICACIONES DE EXISTENCIA Y RELACIONES ===================
    
    // Verificar existencia de relación específica
    @Query("SELECT COUNT(uhr) > 0 FROM UsuarioHasRepositorio uhr WHERE uhr.id.usuarioId = :usuarioId AND uhr.id.repositorioId = :repositorioId")
    boolean existsById_UserIdAndId_RepositoryId(@Param("usuarioId") Long usuarioId, @Param("repositorioId") Long repositorioId);
    
    // Verificar si usuario tiene privilegio específico en repositorio
    @Query("SELECT COUNT(uhr) > 0 FROM UsuarioHasRepositorio uhr WHERE uhr.id.usuarioId = :usuarioId AND uhr.id.repositorioId = :repositorioId AND uhr.privilegio = :privilegio")
    boolean existsById_UserIdAndId_RepositoryIdAndPrivilege(@Param("usuarioId") Long usuarioId, @Param("repositorioId") Long repositorioId, @Param("privilegio") UsuarioHasRepositorio.PrivilegioUsuarioRepositorio privilegio);
    
    // Obtener relación específica
    @Query("SELECT uhr FROM UsuarioHasRepositorio uhr WHERE uhr.id.usuarioId = :usuarioId AND uhr.id.repositorioId = :repositorioId")
    Optional<UsuarioHasRepositorio> findById_UserIdAndId_RepositoryId(@Param("usuarioId") Long usuarioId, @Param("repositorioId") Long repositorioId);
    
    // =================== CONSULTAS BATCH (OPTIMIZACIÓN DE RENDIMIENTO) ===================
    
    /**
     * Obtiene permisos de un usuario en MÚLTIPLES repositorios en UNA SOLA query
     * 
     * CRÍTICO para eliminar problema N+1:
     * - Antes: 20 repositorios = 20 queries individuales
     * - Ahora: 20 repositorios = 1 query batch
     * 
     * MEJORA: 20x menos queries, ~40x más rápido
     */
    @Query("SELECT uhr FROM UsuarioHasRepositorio uhr WHERE uhr.id.usuarioId = :usuarioId AND uhr.id.repositorioId IN :repositorioIds")
    List<UsuarioHasRepositorio> findByUsuarioIdAndRepositorioIdIn(@Param("usuarioId") Long usuarioId, @Param("repositorioIds") List<Long> repositorioIds);
    
    // =================== CONSULTAS QUE RETORNAN ENTIDADES RELACIONADAS ===================
    
    // Obtener repositorios de un usuario
    @Query("SELECT uhr.repositorio FROM UsuarioHasRepositorio uhr WHERE uhr.id.usuarioId = :usuarioId")
    List<Repositorio> findRepositoriesByUserId(@Param("usuarioId") Long usuarioId);
    
    // Obtener repositorios de un usuario con privilegio específico
    @Query("SELECT uhr.repositorio FROM UsuarioHasRepositorio uhr WHERE uhr.id.usuarioId = :usuarioId AND uhr.privilegio = :privilegio")
    List<Repositorio> findRepositoriesByUserIdAndPrivilege(@Param("usuarioId") Long usuarioId, @Param("privilegio") UsuarioHasRepositorio.PrivilegioUsuarioRepositorio privilegio);
    
    // Obtener repositorios donde el usuario es editor
    @Query("SELECT uhr.repositorio FROM UsuarioHasRepositorio uhr WHERE uhr.id.usuarioId = :usuarioId AND uhr.privilegio = 'EDITOR'")
    List<Repositorio> findEditableRepositoriesByUser(@Param("usuarioId") Long usuarioId);
    
    // Obtener usuarios de un repositorio
    @Query("SELECT uhr.usuario FROM UsuarioHasRepositorio uhr WHERE uhr.id.repositorioId = :repositorioId")
    List<Usuario> findUsersByRepositoryId(@Param("repositorioId") Long repositorioId);
    
    // Obtener usuarios de un repositorio con privilegio específico
    @Query("SELECT uhr.usuario FROM UsuarioHasRepositorio uhr WHERE uhr.id.repositorioId = :repositorioId AND uhr.privilegio = :privilegio")
    List<Usuario> findUsersByRepositoryIdAndPrivilege(@Param("repositorioId") Long repositorioId, @Param("privilegio") UsuarioHasRepositorio.PrivilegioUsuarioRepositorio privilegio);
    
    // Obtener colaboradores de un repositorio (alias para compatibilidad)
    @Query("SELECT uhr.usuario FROM UsuarioHasRepositorio uhr WHERE uhr.id.repositorioId = :repositorioId")
    List<Usuario> findCollaboratorsByRepository(@Param("repositorioId") Long repositorioId);
    
    // =================== CONSULTAS DE ANÁLISIS Y REPORTES ===================
    
    // Obtener estadísticas de privilegios por repositorio
    @Query("SELECT uhr.privilegio, COUNT(uhr) FROM UsuarioHasRepositorio uhr WHERE uhr.id.repositorioId = :repositorioId GROUP BY uhr.privilegio")
    List<Object[]> getPrivilegeStatsForRepository(@Param("repositorioId") Long repositorioId);
    
    // Obtener estadísticas de privilegios por usuario
    @Query("SELECT uhr.privilegio, COUNT(uhr) FROM UsuarioHasRepositorio uhr WHERE uhr.id.usuarioId = :usuarioId GROUP BY uhr.privilegio")
    List<Object[]> getPrivilegeStatsForUser(@Param("usuarioId") Long usuarioId);
    
    // Encontrar usuarios con múltiples privilegios EDITOR
    @Query("SELECT u FROM Usuario u WHERE u.usuarioId IN (SELECT uhr.id.usuarioId FROM UsuarioHasRepositorio uhr WHERE uhr.privilegio = 'EDITOR' GROUP BY uhr.id.usuarioId HAVING COUNT(uhr) > 1)")
    List<Usuario> findUsersWithMultipleEditorPrivileges();
    
    // Encontrar repositorios sin editores
    @Query("SELECT r FROM Repositorio r WHERE r.repositorioId NOT IN (SELECT uhr.id.repositorioId FROM UsuarioHasRepositorio uhr WHERE uhr.privilegio = 'EDITOR')")
    List<Repositorio> findRepositoriesWithoutEditors();
}