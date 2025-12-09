package org.project.project.repository;

import org.project.project.model.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository principal para operaciones básicas CRUD de Usuario.
 * 
 * Para consultas complejas específicas del dominio que involucran
 * múltiples entidades y lógica de negocio, usar UsuarioQueryService (cuando se cree).
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // =================== CONSULTAS BÁSICAS DE AUTENTICACIÓN ===================
    
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByProviderAndProviderId(String proveedor, String idProveedor);
    Optional<Usuario> findByCodigoUsuario(String codigoUsuario);
    
    // =================== EXISTENCE VERIFICATIONS ===================
    
    boolean existsByUsername(String username);
    boolean existsByCodigoUsuario(String codigoUsuario);
    boolean existsByCorreo(String correo);
    boolean existsByDni(String dni);
    boolean existsByNombreUsuario(String username);

    
    // =================== BASIC ATTRIBUTE QUERIES ===================
    
    // Find by user name (partial) - maps to nombreUsuario
    List<Usuario> findByNombreUsuarioContainingIgnoreCase(String nombre);
    
    // Find by paternal last name
    List<Usuario> findByApellidoPaternoContainingIgnoreCase(String apellido);
    
    // Find by full user name
    @Query("SELECT u FROM Usuario u WHERE LOWER(CONCAT(u.nombreUsuario, ' ', u.apellidoPaterno)) LIKE LOWER(CONCAT('%', :nombreCompleto, '%'))")
    List<Usuario> findByFullUserName(@Param("nombreCompleto") String nombreCompleto);
    
    // Find by status
    List<Usuario> findByEstadoUsuario(Usuario.EstadoUsuario estado);
    
    // Buscar usuarios activos
    @Query("SELECT u FROM Usuario u WHERE u.estadoUsuario = 'HABILITADO'")
    List<Usuario> findActiveUsers();
    
    // Buscar usuarios inactivos
    @Query("SELECT u FROM Usuario u WHERE u.estadoUsuario = 'DESHABILITADO'")
    List<Usuario> findInactiveUsers();
    
    // =================== DATE QUERIES ===================
    
    // Find by creation date
    List<Usuario> findByFechaCreacion(LocalDateTime fechaCreacion);
    
    // Find users created in date range
    List<Usuario> findByFechaCreacionBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find users created after date
    List<Usuario> findByFechaCreacionAfter(LocalDateTime fecha);
    
    // Find users with recent last connection
    List<Usuario> findByUltimaConexionAfter(LocalDateTime fecha);
    
    // =================== PROVIDER QUERIES ===================
    
    // Find by provider
    List<Usuario> findByProveedor(String proveedor);
    
    // Find local users (without external provider)
    @Query("SELECT u FROM Usuario u WHERE u.proveedor IS NULL OR u.proveedor = 'LOCAL'")
    List<Usuario> findLocalUsers();
    
    // Find external users (with provider)
    @Query("SELECT u FROM Usuario u WHERE u.proveedor IS NOT NULL AND u.proveedor != 'LOCAL'")
    List<Usuario> findExternalUsers();
    
    // =================== ROLE RELATED QUERIES ===================
    
    @Query("SELECT u FROM Usuario u WHERE :rolId NOT IN " +
            "(SELECT r.rolId FROM u.roles r)")
    List<Usuario> findUsersExceptRole(@Param("rolId") Integer rolId);
    
    @Query("SELECT u FROM Usuario u WHERE u NOT IN " +
           "(SELECT DISTINCT ur FROM Usuario ur JOIN ur.roles r WHERE r.rolId = :rolId)")
    List<Usuario> findUsersExceptRoleById(@Param("rolId") Integer rolId);
    
    // Find users with specific role
    @Query("SELECT DISTINCT u FROM Usuario u JOIN u.roles r WHERE r.rolId = :rolId")
    List<Usuario> findUsersByRoleId(@Param("rolId") Integer rolId);
    
    // Find users with multiple roles
    @Query("SELECT u FROM Usuario u WHERE SIZE(u.roles) > 1")
    List<Usuario> findUsersWithMultipleRoles();
    
    // Find users without roles
    @Query("SELECT u FROM Usuario u WHERE SIZE(u.roles) = 0")
    List<Usuario> findUsersWithoutRoles();
    
    // =================== STATISTICS QUERIES ===================
    
    // Count by status
    long countByEstadoUsuario(Usuario.EstadoUsuario estado);
    
    // Count by provider
    long countByProveedor(String proveedor);
    
    // Count users created in date range
    long countByFechaCreacionBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // =================== UTILITY QUERIES ===================
    
    // Find users by multiple criteria
    @Query("SELECT u FROM Usuario u WHERE " +
           "(:nombre IS NULL OR LOWER(u.nombreUsuario) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:correo IS NULL OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :correo, '%'))) AND " +
           "(:estado IS NULL OR u.estadoUsuario = :estado)")
    List<Usuario> findByCriteria(@Param("nombre") String nombre, 
                                @Param("correo") String correo, 
                                @Param("estado") String estado);
    
    // =================== ENGLISH NAMED METHODS (map to Spanish fields) ===================
    
    // Find by last name (lastName maps to apellidoPaterno)
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.apellidoPaterno) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<Usuario> findByLastNameContainingIgnoreCase(@Param("lastName") String lastName);
    

    

    
    // Count by status (additional method for enum parameter)
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.estadoUsuario = :userStatus")
    long countByUserStatusEnum(@Param("userStatus") Usuario.EstadoUsuario userStatus);
    
    // =================== PLATFORM USER MANAGEMENT QUERIES ===================
    
    // Find platform users with pagination (excluding SA role) 
    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN u.roles r " +
           "WHERE r.nombreRol != 'SA' OR u.roles IS EMPTY " +
           "ORDER BY u.fechaCreacion DESC")
    Page<Usuario> findPlatformUsers(Pageable pageable);
    
    // Find platform users with search and pagination
    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN u.roles r " +
           "WHERE (r.nombreRol != 'SA' OR u.roles IS EMPTY) " +
           "AND (LOWER(u.nombreUsuario) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.apellidoPaterno) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.apellidoMaterno) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR u.dni LIKE CONCAT('%', :search, '%')) " +
           "ORDER BY u.fechaCreacion DESC")
    Page<Usuario> findPlatformUsersWithSearch(@Param("search") String search, Pageable pageable);
    
    // =================== METRICS QUERIES ===================
    
    // Count all DEV users (regardless of status)
    @Query("SELECT COUNT(DISTINCT u) FROM Usuario u JOIN u.roles r " +
           "WHERE r.nombreRol = 'DEV'")
    Long countByRolNombreRol_DEV();
    
    // Count all QA users (regardless of status) 
    @Query("SELECT COUNT(DISTINCT u) FROM Usuario u JOIN u.roles r " +
           "WHERE r.nombreRol = 'QA'")
    Long countByRolNombreRol_QA();
    
    // Count all PO users (regardless of status)
    @Query("SELECT COUNT(DISTINCT u) FROM Usuario u JOIN u.roles r " +
           "WHERE r.nombreRol = 'PO'")
    Long countByRolNombreRol_PO();
    
    // Count all platform users (excluding SA role)
    @Query("SELECT COUNT(DISTINCT u) FROM Usuario u LEFT JOIN u.roles r " +
           "WHERE r.nombreRol != 'SA' OR u.roles IS EMPTY")
    Long countPlatformUsers();
    
    // Count enabled platform users
    @Query("SELECT COUNT(DISTINCT u) FROM Usuario u LEFT JOIN u.roles r " + 
           "WHERE (r.nombreRol != 'SA' OR u.roles IS EMPTY) " +
           "AND u.estadoUsuario = 'HABILITADO'")
    Long countEnabledPlatformUsers();
    
    // Count disabled platform users
    @Query("SELECT COUNT(DISTINCT u) FROM Usuario u LEFT JOIN u.roles r " +
           "WHERE (r.nombreRol != 'SA' OR u.roles IS EMPTY) " +
           "AND u.estadoUsuario = 'INHABILITADO'")
    Long countDisabledPlatformUsers();
    
    // =================== FORCED DELETION METHODS ===================
    
    @Modifying
    @Transactional
    @Query(value = "SET FOREIGN_KEY_CHECKS = 0", nativeQuery = true)
    void disableForeignKeyChecks();
    
    @Modifying
    @Transactional
    @Query(value = "SET FOREIGN_KEY_CHECKS = 1", nativeQuery = true)
    void enableForeignKeyChecks();
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ticket_has_usuario WHERE usuario_usuario_id = :userId", nativeQuery = true)
    void deleteTicketHasUsuarioByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM usuario_has_equipo WHERE usuario_usuario_id = :userId", nativeQuery = true)
    void deleteUsuarioHasEquipoByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM usuario_has_proyecto WHERE usuario_usuario_id = :userId", nativeQuery = true)
    void deleteUsuarioHasProyectoByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM usuario_has_repositorio WHERE usuario_usuario_id = :userId", nativeQuery = true)
    void deleteUsuarioHasRepositorioByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM usuario_has_rol WHERE usuario_usuario_id = :userId", nativeQuery = true)
    void deleteUsuarioHasRolByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ticket WHERE reportado_por_usuario_id = :userId OR asignado_a_usuario_id = :userId", nativeQuery = true)
    void deleteTicketsByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM feedback WHERE usuario_usuario_id = :userId", nativeQuery = true)
    void deleteFeedbackByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM notificacion WHERE usuario_id = :userId", nativeQuery = true)
    void deleteNotificacionByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM chatbot_conversacion WHERE usuario_id = :userId", nativeQuery = true)
    void deleteConversacionByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM token WHERE usuario_usuario_id = :userId", nativeQuery = true)
    void deleteTokenByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM usuario WHERE usuario_id = :userId", nativeQuery = true)
    void deleteUsuarioByUserId(@Param("userId") Long userId);
}