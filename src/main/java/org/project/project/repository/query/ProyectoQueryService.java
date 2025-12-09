package org.project.project.repository.query;

import org.project.project.model.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository especializado para consultas complejas de Proyecto
 * que involucran múltiples entidades y lógica de negocio específica.
 * 
 * Separado del ProyectoRepository principal para mantener
 * una clara separación de responsabilidades.
 */
@Repository
public interface ProyectoQueryService extends JpaRepository<Proyecto, Long> {

    // =================== CONSULTAS PARA ESTADÍSTICAS DE USUARIO ===================
    
    @Query(value = """
        SELECT 
            -- 1. PROYECTOS PERSONALES (propietario_proyecto = 'USUARIO' Y participa)
            (SELECT COUNT(*) FROM proyecto p 
             INNER JOIN usuario_has_proyecto up ON p.proyecto_id = up.proyecto_proyecto_id 
             WHERE up.usuario_usuario_id = :userId 
             AND p.propietario_proyecto = 'USUARIO'),
            
            -- 2. PROYECTOS DE EQUIPO (propietario_proyecto != 'USUARIO' Y participa)
            (SELECT COUNT(*) FROM proyecto p 
             INNER JOIN usuario_has_proyecto up ON p.proyecto_id = up.proyecto_proyecto_id 
             WHERE up.usuario_usuario_id = :userId 
             AND p.propietario_proyecto != 'USUARIO'),
            
            -- 3. DONDE PARTICIPO (calculado en servicio como suma de 1+2)
            0,
            
            -- 4. OTROS PROYECTOS (total sistema menos donde participa)
            ((SELECT COUNT(*) FROM proyecto) - 
             (SELECT COUNT(DISTINCT p.proyecto_id) FROM proyecto p 
              INNER JOIN usuario_has_proyecto up ON p.proyecto_id = up.proyecto_proyecto_id 
              WHERE up.usuario_usuario_id = :userId))
    """, nativeQuery = true)
    List<Object[]> getUserProjectStatsRaw(@Param("userId") Long userId);
    
    // =================== CONSULTAS PARA PROYECTOS PERSONALES ===================
    
    @Query(value = """
        SELECT DISTINCT p.* FROM proyecto p
        INNER JOIN usuario_has_proyecto up ON p.proyecto_id = up.proyecto_proyecto_id
        LEFT JOIN categoria_has_proyecto chp ON p.proyecto_id = chp.proyecto_proyecto_id
        LEFT JOIN categoria c ON chp.categoria_id_categoria = c.id_categoria
        WHERE up.usuario_usuario_id = :userId
        AND p.propietario_proyecto = 'USUARIO'
        AND (:category IS NULL OR :category = '' OR c.nombre_categoria = :category)
        AND (:search IS NULL OR :search = '' 
             OR LOWER(p.nombre_proyecto) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(p.descripcion_proyecto) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN p.nombre_proyecto END ASC,
            CASE WHEN :sort = 'oldest' THEN p.fecha_inicio_proyecto END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN p.fecha_inicio_proyecto END DESC
    """, nativeQuery = true)
    List<Proyecto> findPersonalProjects(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort
    );
    
    // =================== CONSULTAS PARA PROYECTOS DE EQUIPOS ===================
    
    @Query(value = """
        SELECT DISTINCT p.* FROM proyecto p
        INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
        LEFT JOIN categoria_has_proyecto chp ON p.proyecto_id = chp.proyecto_proyecto_id
        LEFT JOIN categoria c ON chp.categoria_id_categoria = c.id_categoria
        WHERE uhp.usuario_usuario_id = :userId
        AND p.propietario_proyecto != 'USUARIO'
        AND (:category IS NULL OR :category = '' OR c.nombre_categoria = :category)
        AND (:search IS NULL OR :search = '' 
             OR LOWER(p.nombre_proyecto) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(p.descripcion_proyecto) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN p.nombre_proyecto END ASC,
            CASE WHEN :sort = 'oldest' THEN p.fecha_inicio_proyecto END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN p.fecha_inicio_proyecto END DESC
    """, nativeQuery = true)
    List<Proyecto> findTeamProjects(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort
    );
    
    @Query(value = """
        SELECT DISTINCT p.* FROM proyecto p
        INNER JOIN equipo_has_proyecto ep ON p.proyecto_id = ep.proyecto_proyecto_id
        INNER JOIN equipo e ON ep.equipo_equipo_id = e.equipo_id
        INNER JOIN usuario_has_equipo ue ON e.equipo_id = ue.equipo_equipo_id
        LEFT JOIN categoria_has_proyecto cp ON p.proyecto_id = cp.proyecto_proyecto_id
        LEFT JOIN categoria c ON cp.categoria_id_categoria = c.id_categoria
        WHERE ue.usuario_usuario_id = :userId 
        AND e.equipo_id = :teamId
        AND p.estado_proyecto != 'CERRADO'
        AND (:category IS NULL OR c.nombre_categoria = :category)
        AND (:search IS NULL OR p.nombre_proyecto LIKE CONCAT('%', :search, '%') OR p.descripcion_proyecto LIKE CONCAT('%', :search, '%'))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN p.nombre_proyecto END ASC,
            CASE WHEN :sort = 'oldest' THEN p.fecha_inicio_proyecto END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN p.fecha_inicio_proyecto END DESC
    """, nativeQuery = true)
    List<Proyecto> findSpecificTeamProjects(
        @Param("userId") Long userId,
        @Param("teamId") Long teamId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort
    );
    
    // =================== CONSULTAS PARA PROYECTOS DONDE PARTICIPO ===================
    
    @Query(value = """
        SELECT DISTINCT p.* FROM proyecto p
        INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
        LEFT JOIN categoria_has_proyecto chp ON p.proyecto_id = chp.proyecto_proyecto_id
        LEFT JOIN categoria c ON chp.categoria_id_categoria = c.id_categoria
        WHERE uhp.usuario_usuario_id = :userId
        AND (:category IS NULL OR :category = '' OR c.nombre_categoria = :category)
        AND (:search IS NULL OR :search = '' 
             OR LOWER(p.nombre_proyecto) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(p.descripcion_proyecto) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN p.nombre_proyecto END ASC,
            CASE WHEN :sort = 'oldest' THEN p.fecha_inicio_proyecto END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN p.fecha_inicio_proyecto END DESC
    """, nativeQuery = true)
    List<Proyecto> findParticipatingProjects(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort
    );
    
    // =================== CONSULTAS PARA OTROS PROYECTOS ===================
    
    @Query(value = """
        SELECT DISTINCT p.* FROM proyecto p
        LEFT JOIN categoria_has_proyecto chp ON p.proyecto_id = chp.proyecto_proyecto_id
        LEFT JOIN categoria c ON chp.categoria_id_categoria = c.id_categoria
        WHERE NOT EXISTS (
            SELECT 1 FROM usuario_has_proyecto uhp 
            WHERE uhp.proyecto_proyecto_id = p.proyecto_id 
            AND uhp.usuario_usuario_id = :userId
        )
        AND (:category IS NULL OR :category = '' OR c.nombre_categoria = :category)
        AND (:search IS NULL OR :search = '' 
             OR LOWER(p.nombre_proyecto) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(p.descripcion_proyecto) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN p.nombre_proyecto END ASC,
            CASE WHEN :sort = 'oldest' THEN p.fecha_inicio_proyecto END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN p.fecha_inicio_proyecto END DESC
    """, nativeQuery = true)
    List<Proyecto> findOtherProjects(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort
    );
    
    // =================== CONSULTAS PARA DETALLES Y COLABORADORES ===================
    
    @Query("SELECT p FROM Proyecto p WHERE p.proyectoId = :projectId")
    Proyecto findProjectDetailsById(@Param("projectId") Long projectId);
    
    @Query(value = """
        SELECT u.usuario_id, u.nombre_usuario, u.correo, up.privilegio_usuario_proyecto, up.fecha_usuario_proyecto
        FROM usuario u
        INNER JOIN usuario_has_proyecto up ON u.usuario_id = up.usuario_usuario_id
        WHERE up.proyecto_proyecto_id = :projectId
        ORDER BY up.privilegio_usuario_proyecto DESC, u.nombre_usuario ASC
    """, nativeQuery = true)
    List<Object[]> findProjectCollaborators(@Param("projectId") Long projectId);
    
    // =================== CONSULTAS PARA EQUIPOS ===================
    
    @Query(value = """
        SELECT DISTINCT e.equipo_id, e.nombre_equipo
        FROM equipo e
        INNER JOIN usuario_has_equipo ue ON e.equipo_id = ue.equipo_equipo_id
        WHERE ue.usuario_usuario_id = :userId
        ORDER BY e.nombre_equipo
    """, nativeQuery = true)
    List<Object[]> findUserTeams(@Param("userId") Long userId);
    
    @Query(value = """
        SELECT e.equipo_id, e.nombre_equipo, 
               (SELECT COUNT(*) FROM usuario_has_equipo ue WHERE ue.equipo_equipo_id = e.equipo_id) as total_members,
               (SELECT COUNT(*) FROM equipo_has_proyecto ep WHERE ep.equipo_equipo_id = e.equipo_id) as total_projects
        FROM equipo e
        WHERE e.equipo_id = :teamId
    """, nativeQuery = true)
    Object findTeamInfo(@Param("teamId") Long teamId);
    
    // =================== CONSULTAS PAGINADAS (12 items por página) ===================
    
    /**
     * ✅ PAGINACIÓN: Proyectos donde participo (personales + equipos) - 12 items por página
     * Native query mantenida para compatibilidad con LIMIT/OFFSET
     */
    @Query(value = """
        SELECT DISTINCT p.* FROM proyecto p
        INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
        LEFT JOIN categoria_has_proyecto chp ON p.proyecto_id = chp.proyecto_proyecto_id
        LEFT JOIN categoria c ON chp.categoria_id_categoria = c.id_categoria
        WHERE uhp.usuario_usuario_id = :userId
        AND (:category IS NULL OR :category = '' OR c.nombre_categoria = :category)
        AND (:search IS NULL OR :search = '' 
             OR LOWER(p.nombre_proyecto) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(p.descripcion_proyecto) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN p.nombre_proyecto END ASC,
            CASE WHEN :sort = 'oldest' THEN p.fecha_inicio_proyecto END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN p.fecha_inicio_proyecto END DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Proyecto> findParticipatingProjectsPaginated(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
    
    /**
     * ✅ PAGINACIÓN: Otros proyectos públicos - 12 items por página
     * Native query mantenida para compatibilidad con LIMIT/OFFSET
     */
    @Query(value = """
        SELECT DISTINCT p.* FROM proyecto p
        LEFT JOIN categoria_has_proyecto chp ON p.proyecto_id = chp.proyecto_proyecto_id
        LEFT JOIN categoria c ON chp.categoria_id_categoria = c.id_categoria
        WHERE NOT EXISTS (
            SELECT 1 FROM usuario_has_proyecto uhp 
            WHERE uhp.proyecto_proyecto_id = p.proyecto_id 
            AND uhp.usuario_usuario_id = :userId
        )
        AND (:category IS NULL OR :category = '' OR c.nombre_categoria = :category)
        AND (:search IS NULL OR :search = '' 
             OR LOWER(p.nombre_proyecto) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(p.descripcion_proyecto) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN p.nombre_proyecto END ASC,
            CASE WHEN :sort = 'oldest' THEN p.fecha_inicio_proyecto END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN p.fecha_inicio_proyecto END DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Proyecto> findOtherProjectsPaginated(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
    
    /**
     * ✅ PAGINACIÓN: Proyectos personales - 12 items por página
     * Native query mantenida para compatibilidad con LIMIT/OFFSET
     */
    @Query(value = """
        SELECT DISTINCT p.* FROM proyecto p
        INNER JOIN usuario_has_proyecto up ON p.proyecto_id = up.proyecto_proyecto_id
        LEFT JOIN categoria_has_proyecto chp ON p.proyecto_id = chp.proyecto_proyecto_id
        LEFT JOIN categoria c ON chp.categoria_id_categoria = c.id_categoria
        WHERE up.usuario_usuario_id = :userId
        AND p.propietario_proyecto = 'USUARIO'
        AND (:category IS NULL OR :category = '' OR c.nombre_categoria = :category)
        AND (:search IS NULL OR :search = '' 
             OR LOWER(p.nombre_proyecto) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(p.descripcion_proyecto) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN p.nombre_proyecto END ASC,
            CASE WHEN :sort = 'oldest' THEN p.fecha_inicio_proyecto END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN p.fecha_inicio_proyecto END DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Proyecto> findPersonalProjectsPaginated(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
    
    /**
     * ✅ OPTIMIZADO: Query auxiliar con JOIN FETCH para cargar relaciones eagerly
     * Se usa después de obtener los IDs con la query paginada
     */
    @Query("""
        SELECT DISTINCT p FROM Proyecto p
        LEFT JOIN FETCH p.creadoPor
        LEFT JOIN FETCH p.actualizadoPor
        LEFT JOIN FETCH p.categorias
        WHERE p.proyectoId IN :projectIds
    """)
    List<Proyecto> findByIdsWithRelations(@Param("projectIds") List<Long> projectIds);
    
    /**
     * ✅ PAGINACIÓN: Proyectos de equipos - 12 items por página
     * Native query mantenida para compatibilidad con LIMIT/OFFSET
     */
    @Query(value = """
        SELECT DISTINCT p.* FROM proyecto p
        INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
        LEFT JOIN categoria_has_proyecto chp ON p.proyecto_id = chp.proyecto_proyecto_id
        LEFT JOIN categoria c ON chp.categoria_id_categoria = c.id_categoria
        WHERE uhp.usuario_usuario_id = :userId
        AND p.propietario_proyecto != 'USUARIO'
        AND (:category IS NULL OR :category = '' OR c.nombre_categoria = :category)
        AND (:search IS NULL OR :search = '' 
             OR LOWER(p.nombre_proyecto) LIKE LOWER(CONCAT('%', :search, '%')) 
             OR LOWER(p.descripcion_proyecto) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY 
            CASE WHEN :sort = 'name' THEN p.nombre_proyecto END ASC,
            CASE WHEN :sort = 'oldest' THEN p.fecha_inicio_proyecto END ASC,
            CASE WHEN :sort IS NULL OR :sort = 'recent' THEN p.fecha_inicio_proyecto END DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Proyecto> findTeamProjectsPaginated(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("search") String search,
        @Param("sort") String sort,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
}