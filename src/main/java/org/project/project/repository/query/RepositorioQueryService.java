package org.project.project.repository.query;

import org.project.project.model.entity.Repositorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository especializado para consultas complejas de Repositorio
 * que involucran múltiples entidades y lógica de negocio específica.
 * 
 * Separado del RepositorioRepository principal para mantener
 * una clara separación de responsabilidades.
 */
@Repository
public interface RepositorioQueryService extends JpaRepository<Repositorio, Long> {

    // =================== CONSULTAS PARA ESTADÍSTICAS DE USUARIO ===================
    
    @Query(value = """
        SELECT 
            -- 1. REPOSITORIOS PERSONALES (tipo PERSONAL donde participa)
            (SELECT COUNT(DISTINCT r1.repositorio_id) FROM repositorio r1 
             WHERE r1.tipo_repositorio = 'PERSONAL'
             AND (r1.creado_por_usuario_id = :userId OR 
                  EXISTS (SELECT 1 FROM usuario_has_repositorio ur1 
                          WHERE ur1.repositorio_repositorio_id = r1.repositorio_id 
                          AND ur1.usuario_usuario_id = :userId))) as personal_repositories,
            
            -- 2. REPOSITORIOS COLABORATIVOS (tipo COLABORATIVO donde participa)
            (SELECT COUNT(DISTINCT r2.repositorio_id) FROM repositorio r2 
             WHERE r2.tipo_repositorio = 'COLABORATIVO'
             AND (r2.creado_por_usuario_id = :userId OR 
                  EXISTS (SELECT 1 FROM usuario_has_repositorio ur2 
                          WHERE ur2.repositorio_repositorio_id = r2.repositorio_id 
                          AND ur2.usuario_usuario_id = :userId))) as collaborative_repositories,
            
            -- 3. DONDE PARTICIPO (calculado en servicio como suma de 1+2)
            0 as i_am_part_of,
            
            -- 4. OTROS REPOSITORIOS (todos los repositorios donde NO participa)
            (SELECT COUNT(*) FROM repositorio r4 
             WHERE r4.creado_por_usuario_id != :userId
             AND r4.repositorio_id NOT IN (
                 SELECT ur4.repositorio_repositorio_id FROM usuario_has_repositorio ur4 
                 WHERE ur4.usuario_usuario_id = :userId
             )) as other_repositories
        """, nativeQuery = true)
    List<Object[]> getUserRepositoryStatsRaw(@Param("userId") Long userId);
    
    // =================== CONSULTAS PARA REPOSITORIOS PERSONALES ===================
    
    @Query("""
        SELECT DISTINCT r FROM Repositorio r
        WHERE r.tipoRepositorio = 'PERSONAL'
        AND (r.creador.usuarioId = :userId OR 
             EXISTS (SELECT 1 FROM UsuarioHasRepositorio ur 
                     WHERE ur.repositorio.repositorioId = r.repositorioId 
                     AND ur.usuario.usuarioId = :userId))
        AND (:category IS NULL OR EXISTS (SELECT c FROM r.categorias c WHERE c.nombreCategoria = :category))
        AND (:search IS NULL OR r.nombreRepositorio LIKE %:search% OR r.descripcionRepositorio LIKE %:search%)
        ORDER BY 
            CASE WHEN :sort = 'name' THEN r.nombreRepositorio END ASC,
            CASE WHEN :sort = 'oldest' THEN r.fechaCreacion END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN r.fechaCreacion END DESC
    """)
    List<Repositorio> findPersonalRepositories(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort
    );
    
    // =================== CONSULTAS PARA TODOS MIS REPOSITORIOS (PERSONAL + COLABORATIVO) ===================
    
    @Query("""
        SELECT DISTINCT r FROM Repositorio r
        WHERE (r.creador.usuarioId = :userId OR 
             EXISTS (SELECT 1 FROM UsuarioHasRepositorio ur 
                     WHERE ur.repositorio.repositorioId = r.repositorioId 
                     AND ur.usuario.usuarioId = :userId))
        AND (:category IS NULL OR EXISTS (SELECT c FROM r.categorias c WHERE c.nombreCategoria = :category))
        AND (:search IS NULL OR r.nombreRepositorio LIKE %:search% OR r.descripcionRepositorio LIKE %:search%)
        ORDER BY 
            CASE WHEN :sort = 'name' THEN r.nombreRepositorio END ASC,
            CASE WHEN :sort = 'oldest' THEN r.fechaCreacion END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN r.fechaCreacion END DESC
    """)
    List<Repositorio> findAllMyRepositories(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort
    );
    
    // =================== CONSULTAS PARA REPOSITORIOS DE PROYECTOS ===================
    
    @Query("""
        SELECT DISTINCT r FROM Repositorio r
        JOIN r.proyectos p
        JOIN UsuarioHasRepositorio ur ON r.repositorioId = ur.repositorio.repositorioId
        WHERE ur.usuario.usuarioId = :userId
        AND SIZE(r.proyectos) > 0
        AND (:category IS NULL OR EXISTS (SELECT c FROM r.categorias c WHERE c.nombreCategoria = :category))
        AND (:search IS NULL OR r.nombreRepositorio LIKE %:search% OR r.descripcionRepositorio LIKE %:search%)
        ORDER BY 
            CASE WHEN :sort = 'name' THEN r.nombreRepositorio END ASC,
            CASE WHEN :sort = 'oldest' THEN r.fechaCreacion END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN r.fechaCreacion END DESC
    """)
    List<Repositorio> findProjectRepositories(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort
    );
    
    @Query("""
        SELECT r FROM Repositorio r
        JOIN r.proyectos p
        WHERE p.proyectoId = :projectId
        AND (:category IS NULL OR EXISTS (SELECT c FROM r.categorias c WHERE c.nombreCategoria = :category))
        AND (:search IS NULL OR r.nombreRepositorio LIKE %:search% OR r.descripcionRepositorio LIKE %:search%)
        ORDER BY 
            CASE WHEN :sort = 'name' THEN r.nombreRepositorio END ASC,
            CASE WHEN :sort = 'oldest' THEN r.fechaCreacion END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN r.fechaCreacion END DESC
    """)
    List<Repositorio> findSpecificProjectRepositories(
        @Param("projectId") Long projectId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort
    );
    
    // =================== CONSULTAS PARA REPOSITORIOS COLABORATIVOS ===================
    
    @Query("""
        SELECT DISTINCT r FROM Repositorio r
        WHERE r.tipoRepositorio = 'COLABORATIVO'
        AND (r.creador.usuarioId = :userId OR 
             EXISTS (SELECT 1 FROM UsuarioHasRepositorio ur 
                     WHERE ur.repositorio.repositorioId = r.repositorioId 
                     AND ur.usuario.usuarioId = :userId))
        AND (:category IS NULL OR EXISTS (SELECT c FROM r.categorias c WHERE c.nombreCategoria = :category))
        AND (:search IS NULL OR r.nombreRepositorio LIKE %:search% OR r.descripcionRepositorio LIKE %:search%)
        ORDER BY 
            CASE WHEN :sort = 'name' THEN r.nombreRepositorio END ASC,
            CASE WHEN :sort = 'oldest' THEN r.fechaCreacion END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN r.fechaCreacion END DESC
    """)
    List<Repositorio> findCollaborativeRepositories(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort
    );
    
    // =================== CONSULTAS PARA OTROS REPOSITORIOS PÚBLICOS ===================
    
    @Query("""
        SELECT r FROM Repositorio r
        WHERE r.creador.usuarioId != :userId
        AND r.visibilidadRepositorio = 'PUBLICO'
        AND r.repositorioId NOT IN (
            SELECT ur.repositorio.repositorioId FROM UsuarioHasRepositorio ur 
            WHERE ur.usuario.usuarioId = :userId
        )
        AND (:category IS NULL OR EXISTS (SELECT c FROM r.categorias c WHERE c.nombreCategoria = :category))
        AND (:search IS NULL OR r.nombreRepositorio LIKE %:search% OR r.descripcionRepositorio LIKE %:search%)
        ORDER BY 
            CASE WHEN :sort = 'name' THEN r.nombreRepositorio END ASC,
            CASE WHEN :sort = 'oldest' THEN r.fechaCreacion END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN r.fechaCreacion END DESC
    """)
    List<Repositorio> findOtherPublicRepositories(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort
    );

    @Query("""
        SELECT r FROM Repositorio r
        WHERE r.creador.usuarioId != :userId
        AND r.repositorioId NOT IN (
            SELECT ur.repositorio.repositorioId FROM UsuarioHasRepositorio ur 
            WHERE ur.usuario.usuarioId = :userId
        )
        AND (:category IS NULL OR EXISTS (SELECT c FROM r.categorias c WHERE c.nombreCategoria = :category))
        AND (:search IS NULL OR r.nombreRepositorio LIKE %:search% OR r.descripcionRepositorio LIKE %:search%)
        ORDER BY 
            CASE WHEN :sort = 'name' THEN r.nombreRepositorio END ASC,
            CASE WHEN :sort = 'oldest' THEN r.fechaCreacion END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN r.fechaCreacion END DESC
    """)
    List<Repositorio> findOtherRepositories(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort
    );
    
    // =================== CONSULTAS PARA DETALLES Y COLABORADORES ===================
    
    @Query("""
        SELECT r FROM Repositorio r
        LEFT JOIN FETCH r.categorias
        LEFT JOIN FETCH r.usuarios
        LEFT JOIN FETCH r.proyectos
        WHERE r.repositorioId = :repositoryId
    """)
    Repositorio findRepositoryDetailsById(@Param("repositoryId") Long repositoryId);
    
    @Query("""
        SELECT ur FROM UsuarioHasRepositorio ur
        JOIN FETCH ur.usuario u
        WHERE ur.repositorio.repositorioId = :repositoryId
        ORDER BY ur.privilegio DESC, u.nombreUsuario ASC
    """)
    List<Object[]> findRepositoryCollaborators(@Param("repositoryId") Long repositoryId);
    
    // =================== CONSULTAS PARA TODOS LOS REPOSITORIOS ACCESIBLES POR EL USUARIO ===================
    
    @Query("""
        SELECT DISTINCT r FROM Repositorio r
        WHERE (:userId IS NULL OR :userId IS NOT NULL)
        AND (:category IS NULL OR EXISTS (SELECT c FROM r.categorias c WHERE c.nombreCategoria = :category))
        AND (:search IS NULL OR r.nombreRepositorio LIKE %:search% OR r.descripcionRepositorio LIKE %:search%)
        ORDER BY 
            CASE WHEN :sort = 'name' THEN r.nombreRepositorio END ASC,
            CASE WHEN :sort = 'oldest' THEN r.fechaCreacion END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN r.fechaCreacion END DESC
    """)
    List<Repositorio> findAllUserAccessibleRepositories(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort
    );
    
    // =================== CONSULTAS PARA PROYECTOS DEL USUARIO ===================
    
    @Query("""
        SELECT DISTINCT p.proyectoId, p.nombreProyecto, p.descripcionProyecto,
               COUNT(DISTINCT r.repositorioId) as total_repositorios
        FROM Proyecto p
        LEFT JOIN UsuarioHasProyecto up ON p.proyectoId = up.proyecto.proyectoId
        LEFT JOIN p.repositorios r
        WHERE up.usuario.usuarioId = :userId
        GROUP BY p.proyectoId, p.nombreProyecto, p.descripcionProyecto
        ORDER BY p.nombreProyecto
    """)
    List<Object[]> findUserProjects(@Param("userId") Long userId);
    
    @Query("""
        SELECT DISTINCT p.proyectoId, p.nombreProyecto, p.descripcionProyecto
        FROM Proyecto p
        JOIN UsuarioHasProyecto up ON p.proyectoId = up.proyecto.proyectoId
        JOIN p.repositorios r
        JOIN UsuarioHasRepositorio ur ON r.repositorioId = ur.repositorio.repositorioId
        WHERE up.usuario.usuarioId = :userId 
        AND ur.usuario.usuarioId = :userId
        ORDER BY p.nombreProyecto
    """)
    List<Object[]> findUserProjectsWithRepositories(@Param("userId") Long userId);
    
    // =================== CONSULTAS PAGINADAS (12 items por página) ===================
    
    /**
     * ✅ PAGINACIÓN: Repositorios personales - 12 items por página
     */
    @Query(value = """
        SELECT DISTINCT r.* FROM repositorio r
        WHERE r.tipo_repositorio = 'PERSONAL'
        AND (r.creado_por_usuario_id = :userId OR 
             EXISTS (SELECT 1 FROM usuario_has_repositorio ur 
                     WHERE ur.repositorio_repositorio_id = r.repositorio_id 
                     AND ur.usuario_usuario_id = :userId))
        AND (:category IS NULL OR :category = '' OR 
             EXISTS (SELECT 1 FROM categoria_has_repositorio cr 
                     INNER JOIN categoria c ON cr.categoria_id_categoria = c.id_categoria 
                     WHERE cr.repositorio_repositorio_id = r.repositorio_id 
                     AND c.nombre_categoria = :category))
        AND (:search IS NULL OR :search = '' 
             OR LOWER(r.nombre_repositorio) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(r.descripcion_repositorio) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN r.nombre_repositorio END ASC,
            CASE WHEN :sort = 'oldest' THEN r.fecha_creacion END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN r.fecha_creacion END DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Repositorio> findPersonalRepositoriesPaginated(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
    
    /**
     * ✅ PAGINACIÓN: Repositorios colaborativos - 12 items por página
     */
    @Query(value = """
        SELECT DISTINCT r.* FROM repositorio r
        WHERE r.tipo_repositorio = 'COLABORATIVO'
        AND (r.creado_por_usuario_id = :userId OR 
             EXISTS (SELECT 1 FROM usuario_has_repositorio ur 
                     WHERE ur.repositorio_repositorio_id = r.repositorio_id 
                     AND ur.usuario_usuario_id = :userId))
        AND (:category IS NULL OR :category = '' OR 
             EXISTS (SELECT 1 FROM categoria_has_repositorio cr 
                     INNER JOIN categoria c ON cr.categoria_id_categoria = c.id_categoria 
                     WHERE cr.repositorio_repositorio_id = r.repositorio_id 
                     AND c.nombre_categoria = :category))
        AND (:search IS NULL OR :search = '' 
             OR LOWER(r.nombre_repositorio) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(r.descripcion_repositorio) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN r.nombre_repositorio END ASC,
            CASE WHEN :sort = 'oldest' THEN r.fecha_creacion END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN r.fecha_creacion END DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Repositorio> findCollaborativeRepositoriesPaginated(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
    
    /**
     * ✅ PAGINACIÓN: Otros repositorios públicos - 12 items por página
     */
    @Query(value = """
        SELECT r.* FROM repositorio r
        WHERE r.creado_por_usuario_id != :userId
        AND r.visibilidad_repositorio = 'PUBLICO'
        AND r.repositorio_id NOT IN (
            SELECT ur.repositorio_repositorio_id FROM usuario_has_repositorio ur 
            WHERE ur.usuario_usuario_id = :userId
        )
        AND (:category IS NULL OR :category = '' OR 
             EXISTS (SELECT 1 FROM categoria_has_repositorio cr 
                     INNER JOIN categoria c ON cr.categoria_id_categoria = c.id_categoria 
                     WHERE cr.repositorio_repositorio_id = r.repositorio_id 
                     AND c.nombre_categoria = :category))
        AND (:search IS NULL OR :search = '' 
             OR LOWER(r.nombre_repositorio) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(r.descripcion_repositorio) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN r.nombre_repositorio END ASC,
            CASE WHEN :sort = 'oldest' THEN r.fecha_creacion END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN r.fecha_creacion END DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Repositorio> findOtherPublicRepositoriesPaginated(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
    
    /**
     * ✅ PAGINACIÓN: Otros repositorios (TODOS, públicos y privados) - 12 items por página
     * Repositorios donde el usuario NO participa, sin filtrar por visibilidad
     */
    @Query(value = """
        SELECT r.* FROM repositorio r
        WHERE r.creado_por_usuario_id != :userId
        AND r.repositorio_id NOT IN (
            SELECT ur.repositorio_repositorio_id FROM usuario_has_repositorio ur 
            WHERE ur.usuario_usuario_id = :userId
        )
        AND (:category IS NULL OR :category = '' OR 
             EXISTS (SELECT 1 FROM categoria_has_repositorio cr 
                     INNER JOIN categoria c ON cr.categoria_id_categoria = c.id_categoria 
                     WHERE cr.repositorio_repositorio_id = r.repositorio_id 
                     AND c.nombre_categoria = :category))
        AND (:search IS NULL OR :search = '' 
             OR LOWER(r.nombre_repositorio) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(r.descripcion_repositorio) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN r.nombre_repositorio END ASC,
            CASE WHEN :sort = 'oldest' THEN r.fecha_creacion END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN r.fecha_creacion END DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Repositorio> findOtherRepositoriesPaginated(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
    
    /**
     * ✅ PAGINACIÓN: Todos mis repositorios (personal + colaborativo) - 12 items por página
     */
    @Query(value = """
        SELECT DISTINCT r.* FROM repositorio r
        WHERE (r.creado_por_usuario_id = :userId OR 
             EXISTS (SELECT 1 FROM usuario_has_repositorio ur 
                     WHERE ur.repositorio_repositorio_id = r.repositorio_id 
                     AND ur.usuario_usuario_id = :userId))
        AND (:category IS NULL OR :category = '' OR 
             EXISTS (SELECT 1 FROM categoria_has_repositorio cr 
                     INNER JOIN categoria c ON cr.categoria_id_categoria = c.id_categoria 
                     WHERE cr.repositorio_repositorio_id = r.repositorio_id 
                     AND c.nombre_categoria = :category))
        AND (:search IS NULL OR :search = '' 
             OR LOWER(r.nombre_repositorio) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(r.descripcion_repositorio) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN r.nombre_repositorio END ASC,
            CASE WHEN :sort = 'oldest' THEN r.fecha_creacion END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN r.fecha_creacion END DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Repositorio> findAllMyRepositoriesPaginated(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
    
    /**
     * ✅ OPTIMIZADO: Query auxiliar con JOIN FETCH para cargar relaciones eagerly
     * Se usa después de obtener los IDs con la query paginada para eliminar N+1 queries
     */
    @Query("""
        SELECT DISTINCT r FROM Repositorio r
        LEFT JOIN FETCH r.creador
        LEFT JOIN FETCH r.actualizador
        LEFT JOIN FETCH r.categorias
        WHERE r.repositorioId IN :repositoryIds
    """)
    List<Repositorio> findByIdsWithRelations(@Param("repositoryIds") List<Long> repositoryIds);
    

}