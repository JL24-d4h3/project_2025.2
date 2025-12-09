package org.project.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Servicio para la administración del sistema y métricas avanzadas del SuperAdmin
 */
@Service
public class SystemAdministratorService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Obtiene métricas generales del sistema
     */
    public Map<String, Object> getGeneralMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Total de usuarios (todos, habilitados e inhabilitados)
            Integer totalUsuarios = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuario", 
                Integer.class);
            
            // Total de proyectos
            Integer totalProyectos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM proyecto", 
                Integer.class);
            
            // Total de repositorios (la tabla repositorio no tiene estado_repositorio)
            Integer totalRepositorios = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM repositorio", 
                Integer.class);
            
            // Total de tickets
            Integer totalTickets = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ticket", 
                Integer.class);
            
            // Total de APIs
            Integer totalApis = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM api", 
                Integer.class);
            
            // Usuarios activos
            Integer usuariosActivos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuario WHERE actividad_usuario = 'ACTIVO' AND estado_usuario = 'HABILITADO'", 
                Integer.class);
            
            metrics.put("totalUsuarios", totalUsuarios != null ? totalUsuarios : 0);
            metrics.put("totalProyectos", totalProyectos != null ? totalProyectos : 0);
            metrics.put("totalRepositorios", totalRepositorios != null ? totalRepositorios : 0);
            metrics.put("totalTickets", totalTickets != null ? totalTickets : 0);
            metrics.put("totalApis", totalApis != null ? totalApis : 0);
            metrics.put("usuariosActivos", usuariosActivos != null ? usuariosActivos : 0);
            
        } catch (Exception e) {
            // En caso de error, devolver valores por defecto
            metrics.put("totalUsuarios", 0);
            metrics.put("totalProyectos", 0);
            metrics.put("totalRepositorios", 0);
            metrics.put("totalTickets", 0);
            metrics.put("totalApis", 0);
            metrics.put("usuariosActivos", 0);
            metrics.put("error", "Error al obtener métricas generales: " + e.getMessage());
        }
        
        return metrics;
    }

    /**
     * Obtiene métricas específicas de proyectos
     */
    public Map<String, Object> getProyectosMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Distribución de repositorios por proyecto
            List<Map<String, Object>> repositoriosPorProyecto = jdbcTemplate.queryForList(
                """
                SELECT 
                    p.nombre_proyecto,
                    COUNT(phr.repositorio_repositorio_id) as repositorios,
                    p.estado_proyecto
                FROM proyecto p
                LEFT JOIN proyecto_has_repositorio phr ON p.proyecto_id = phr.proyecto_proyecto_id
                GROUP BY p.proyecto_id, p.nombre_proyecto, p.estado_proyecto
                ORDER BY repositorios DESC, p.nombre_proyecto
                LIMIT 10
                """
            );
            
            // Usuarios por proyecto con roles
            List<Map<String, Object>> usuariosPorProyecto = jdbcTemplate.queryForList(
                """
                SELECT 
                    p.nombre_proyecto,
                    COUNT(DISTINCT up.usuario_usuario_id) as total_colaboradores,
                    COUNT(CASE WHEN up.privilegio_usuario_proyecto = 'EDITOR' THEN 1 END) as editores,
                    COUNT(CASE WHEN up.privilegio_usuario_proyecto = 'LECTOR' THEN 1 END) as lectores,
                    COUNT(CASE WHEN up.privilegio_usuario_proyecto = 'COMENTADOR' THEN 1 END) as comentadores
                FROM proyecto p
                LEFT JOIN usuario_has_proyecto up ON p.proyecto_id = up.proyecto_proyecto_id
                GROUP BY p.proyecto_id, p.nombre_proyecto
                HAVING total_colaboradores > 0
                ORDER BY total_colaboradores DESC, p.nombre_proyecto
                LIMIT 10
                """
            );
            
            // Proyectos más complejos (por número de repositorios + colaboradores)
            List<Map<String, Object>> proyectosComplejos = jdbcTemplate.queryForList(
                """
                SELECT 
                    p.nombre_proyecto,
                    COUNT(DISTINCT phr.repositorio_repositorio_id) as repositorios,
                    COUNT(DISTINCT up.usuario_usuario_id) as colaboradores,
                    (COUNT(DISTINCT phr.repositorio_repositorio_id) * COUNT(DISTINCT up.usuario_usuario_id)) as complejidad
                FROM proyecto p
                LEFT JOIN proyecto_has_repositorio phr ON p.proyecto_id = phr.proyecto_proyecto_id
                LEFT JOIN usuario_has_proyecto up ON p.proyecto_id = up.proyecto_proyecto_id
                GROUP BY p.proyecto_id, p.nombre_proyecto
                HAVING repositorios > 0 OR colaboradores > 0
                ORDER BY complejidad DESC, repositorios DESC
                LIMIT 10
                """
            );
            
            metrics.put("repositoriosPorProyecto", repositoriosPorProyecto);
            metrics.put("usuariosPorProyecto", usuariosPorProyecto);
            metrics.put("proyectosComplejos", proyectosComplejos);
            
        } catch (Exception e) {
            metrics.put("error", "Error al obtener métricas de proyectos: " + e.getMessage());
            metrics.put("repositoriosPorProyecto", new ArrayList<>());
            metrics.put("usuariosPorProyecto", new ArrayList<>());
            metrics.put("proyectosComplejos", new ArrayList<>());
        }
        
        return metrics;
    }

    /**
     * Obtiene métricas específicas de usuarios
     */
    public Map<String, Object> getUsuariosMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Usuarios más colaborativos (por número de proyectos)
            List<Map<String, Object>> usuariosColaborativos = jdbcTemplate.queryForList(
                """
                SELECT 
                    CONCAT(u.nombre_usuario, ' ', u.apellido_paterno, ' ', u.apellido_materno) as nombre_completo,
                    u.username,
                    COUNT(DISTINCT up.proyecto_proyecto_id) as proyectos,
                    COUNT(CASE WHEN up.privilegio_usuario_proyecto = 'EDITOR' THEN 1 END) as como_editor,
                    COUNT(CASE WHEN up.privilegio_usuario_proyecto = 'LECTOR' THEN 1 END) as como_lector
                FROM usuario u
                INNER JOIN usuario_has_proyecto up ON u.usuario_id = up.usuario_usuario_id
                WHERE u.estado_usuario = 'HABILITADO'
                GROUP BY u.usuario_id, u.nombre_usuario, u.apellido_paterno, u.apellido_materno, u.username
                ORDER BY proyectos DESC, como_editor DESC
                LIMIT 10
                """
            );
            
            // Usuarios sin proyectos asignados
            List<Map<String, Object>> usuariosSinProyectos = jdbcTemplate.queryForList(
                """
                SELECT 
                    CONCAT(u.nombre_usuario, ' ', u.apellido_paterno, ' ', u.apellido_materno) as nombre_completo,
                    u.username,
                    DATEDIFF(NOW(), u.fecha_creacion) as dias_sin_proyectos
                FROM usuario u
                LEFT JOIN usuario_has_proyecto up ON u.usuario_id = up.usuario_usuario_id
                WHERE u.estado_usuario = 'HABILITADO' 
                  AND u.actividad_usuario = 'ACTIVO'
                  AND up.usuario_usuario_id IS NULL
                ORDER BY dias_sin_proyectos DESC
                """
            );
            
            // Distribución de usuarios por roles
            List<Map<String, Object>> usuariosPorRol = jdbcTemplate.queryForList(
                """
                SELECT 
                    r.nombre_rol,
                    COUNT(ur.usuario_usuario_id) as total_usuarios
                FROM rol r
                LEFT JOIN usuario_has_rol ur ON r.rol_id = ur.rol_rol_id
                LEFT JOIN usuario u ON ur.usuario_usuario_id = u.usuario_id 
                    AND u.estado_usuario = 'HABILITADO'
                GROUP BY r.rol_id, r.nombre_rol
                ORDER BY total_usuarios DESC
                """
            );
            
            metrics.put("usuariosColaborativos", usuariosColaborativos);
            metrics.put("usuariosSinProyectos", usuariosSinProyectos);
            metrics.put("usuariosPorRol", usuariosPorRol);
            
        } catch (Exception e) {
            metrics.put("error", "Error al obtener métricas de usuarios: " + e.getMessage());
            metrics.put("usuariosColaborativos", new ArrayList<>());
            metrics.put("usuariosSinProyectos", new ArrayList<>());
            metrics.put("usuariosPorRol", new ArrayList<>());
        }
        
        return metrics;
    }

    /**
     * Obtiene métricas específicas de repositorios
     */
    public Map<String, Object> getRepositoriosMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Repositorios por visibilidad (la tabla no tiene estado_repositorio)
            List<Map<String, Object>> repositoriosPorVisibilidad = jdbcTemplate.queryForList(
                "SELECT visibilidad_repositorio as label, COUNT(*) as total FROM repositorio GROUP BY visibilidad_repositorio"
            );
            
            // Repositorios por tipo
            List<Map<String, Object>> repositoriosPorTipo = jdbcTemplate.queryForList(
                "SELECT COALESCE(tipo_repositorio, 'SIN_TIPO') as tipo_repositorio, COUNT(*) as total FROM repositorio GROUP BY tipo_repositorio"
            );
            
            // Repositorios más recientes
            List<Map<String, Object>> repositoriosRecientes = jdbcTemplate.queryForList(
                """
                SELECT 
                    r.nombre_repositorio,
                    r.descripcion_repositorio,
                    r.tipo_repositorio,
                    r.visibilidad_repositorio,
                    r.fecha_creacion,
                    CONCAT(u.nombre_usuario, ' ', u.apellido_paterno) as creado_por
                FROM repositorio r
                INNER JOIN usuario u ON r.creado_por_usuario_id = u.usuario_id
                ORDER BY r.fecha_creacion DESC
                LIMIT 10
                """
            );
            
            metrics.put("repositoriosPorVisibilidad", repositoriosPorVisibilidad);
            metrics.put("repositoriosPorTipo", repositoriosPorTipo);
            metrics.put("repositoriosRecientes", repositoriosRecientes);
            
        } catch (Exception e) {
            metrics.put("error", "Error al obtener métricas de repositorios: " + e.getMessage());
            metrics.put("repositoriosPorVisibilidad", new ArrayList<>());
            metrics.put("repositoriosPorTipo", new ArrayList<>());
            metrics.put("repositoriosRecientes", new ArrayList<>());
        }
        
        return metrics;
    }

    /**
     * Obtiene métricas específicas de APIs
     */
    public Map<String, Object> getApisMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // APIs por estado
            List<Map<String, Object>> apisPorEstado = jdbcTemplate.queryForList(
                "SELECT estado_api as label, COUNT(*) as value FROM api GROUP BY estado_api"
            );
            
            // APIs más recientes
            List<Map<String, Object>> apisRecientes = jdbcTemplate.queryForList(
                """
                SELECT 
                    nombre_api,
                    descripcion_api,
                    estado_api,
                    creado_en as fecha_creacion
                FROM api
                ORDER BY creado_en DESC
                LIMIT 10
                """
            );
            
            // APIs por categoría
            List<Map<String, Object>> apisPorCategoria = jdbcTemplate.queryForList(
                """
                SELECT 
                    COALESCE(c.nombre_categoria, 'Sin categoría') as label,
                    COUNT(cha.api_api_id) as value
                FROM categoria c
                LEFT JOIN categoria_has_api cha ON c.id_categoria = cha.categoria_id_categoria
                GROUP BY c.id_categoria, c.nombre_categoria
                ORDER BY value DESC
                """
            );
            
            metrics.put("apisPorEstado", apisPorEstado);
            metrics.put("apisRecientes", apisRecientes);
            metrics.put("apisPorCategoria", apisPorCategoria);
            
        } catch (Exception e) {
            metrics.put("error", "Error al obtener métricas de APIs: " + e.getMessage());
            metrics.put("apisPorEstado", new ArrayList<>());
            metrics.put("apisRecientes", new ArrayList<>());
            metrics.put("apisPorCategoria", new ArrayList<>());
        }
        
        return metrics;
    }

    /**
     * Obtiene métricas específicas de tickets
     */
    public Map<String, Object> getTicketsMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Tickets por estado
            List<Map<String, Object>> ticketsPorEstado = jdbcTemplate.queryForList(
                "SELECT etapa_ticket, COUNT(*) as total FROM ticket GROUP BY etapa_ticket"
            );
            
            // Tickets por prioridad
            List<Map<String, Object>> ticketsPorPrioridad = jdbcTemplate.queryForList(
                "SELECT prioridad_ticket, COUNT(*) as total FROM ticket GROUP BY prioridad_ticket"
            );
            
            // Tickets por tipo
            List<Map<String, Object>> ticketsPorTipo = jdbcTemplate.queryForList(
                "SELECT tipo_ticket, COUNT(*) as total FROM ticket GROUP BY tipo_ticket"
            );
            
            // Tickets pendientes más urgentes
            List<Map<String, Object>> ticketsPendientes = jdbcTemplate.queryForList(
                """
                SELECT 
                    t.asunto_ticket,
                    t.prioridad_ticket,
                    t.tipo_ticket,
                    t.fecha_creacion,
                    CONCAT(u.nombre_usuario, ' ', u.apellido_paterno) as reportado_por,
                    DATEDIFF(NOW(), t.fecha_creacion) as dias_pendiente
                FROM ticket t
                INNER JOIN usuario u ON t.reportado_por_usuario_id = u.usuario_id
                WHERE t.etapa_ticket IN ('PENDIENTE', 'EN_PROGRESO')
                ORDER BY 
                    CASE t.prioridad_ticket 
                        WHEN 'ALTA' THEN 1 
                        WHEN 'MEDIA' THEN 2 
                        ELSE 3 
                    END ASC, 
                    t.fecha_creacion ASC
                LIMIT 15
                """
            );
            
            metrics.put("ticketsPorEstado", ticketsPorEstado);
            metrics.put("ticketsPorPrioridad", ticketsPorPrioridad);
            metrics.put("ticketsPorTipo", ticketsPorTipo);
            metrics.put("ticketsPendientes", ticketsPendientes);
            
        } catch (Exception e) {
            metrics.put("error", "Error al obtener métricas de tickets: " + e.getMessage());
            metrics.put("ticketsPorEstado", new ArrayList<>());
            metrics.put("ticketsPorPrioridad", new ArrayList<>());
            metrics.put("ticketsPorTipo", new ArrayList<>());
            metrics.put("ticketsPendientes", new ArrayList<>());
        }
        
        return metrics;
    }

    /**
     * Obtiene datos paginados para tablas específicas
     */
    public Map<String, Object> getTableData(String category, int page, int size, String search) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String searchCondition = "";
            if (search != null && !search.trim().isEmpty()) {
                search = search.replace("'", "''"); // Escapar comillas simples
                searchCondition = " WHERE LOWER(nombre_busqueda) LIKE LOWER('%" + search + "%')";
            }
            
            String baseQuery = "";
            String countQuery = "";
            
            switch (category.toLowerCase()) {
                case "proyectos":
                    baseQuery = """
                        SELECT 
                            nombre_proyecto as nombre,
                            descripcion_proyecto as descripcion,
                            estado_proyecto as estado,
                            fecha_inicio_proyecto as fecha_inicio,
                            nombre_proyecto as nombre_busqueda
                        FROM proyecto
                        """ + searchCondition + """
                        ORDER BY nombre_proyecto
                        LIMIT ? OFFSET ?
                        """;
                    countQuery = "SELECT COUNT(*) FROM proyecto" + searchCondition;
                    break;
                    
                case "usuarios":
                    baseQuery = """
                        SELECT 
                            CONCAT(nombre_usuario, ' ', apellido_paterno, ' ', apellido_materno) as nombre,
                            username,
                            correo,
                            estado_usuario as estado,
                            actividad_usuario as actividad,
                            CONCAT(nombre_usuario, ' ', apellido_paterno, ' ', apellido_materno, ' ', username) as nombre_busqueda
                        FROM usuario
                        """ + searchCondition + """
                        ORDER BY nombre_usuario, apellido_paterno
                        LIMIT ? OFFSET ?
                        """;
                    countQuery = "SELECT COUNT(*) FROM usuario" + searchCondition;
                    break;
                    
                case "repositorios":
                    baseQuery = """
                        SELECT 
                            nombre_repositorio as nombre,
                            descripcion_repositorio as descripcion,
                            tipo_repositorio as tipo,
                            visibilidad_repositorio as visibilidad,
                            fecha_creacion,
                            nombre_repositorio as nombre_busqueda
                        FROM repositorio
                        """ + searchCondition + """
                        ORDER BY fecha_creacion DESC
                        LIMIT ? OFFSET ?
                        """;
                    countQuery = "SELECT COUNT(*) FROM repositorio" + searchCondition;
                    break;
                    
                default:
                    result.put("data", new ArrayList<>());
                    result.put("totalElements", 0);
                    result.put("totalPages", 0);
                    result.put("currentPage", page);
                    result.put("size", size);
                    return result;
            }
            
            // Ejecutar consultas
            List<Map<String, Object>> data = jdbcTemplate.queryForList(baseQuery, size, page * size);
            Integer totalElements = jdbcTemplate.queryForObject(countQuery, Integer.class);
            
            result.put("data", data);
            result.put("totalElements", totalElements != null ? totalElements : 0);
            result.put("totalPages", totalElements != null ? (int) Math.ceil((double) totalElements / size) : 0);
            result.put("currentPage", page);
            result.put("size", size);
            
        } catch (Exception e) {
            result.put("error", "Error al obtener datos de tabla: " + e.getMessage());
            result.put("data", new ArrayList<>());
            result.put("totalElements", 0);
            result.put("totalPages", 0);
            result.put("currentPage", page);
            result.put("size", size);
        }
        
        return result;
    }

    /**
     * Obtiene datos específicos para gráficos
     */
    public Map<String, Object> getChartData(String category, String chartType) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String key = category.toLowerCase() + "_" + chartType.toLowerCase();
            
            switch (key) {
                case "proyectos_estado":
                    List<Map<String, Object>> proyectosPorEstado = jdbcTemplate.queryForList(
                        "SELECT estado_proyecto as label, COUNT(*) as value FROM proyecto GROUP BY estado_proyecto"
                    );
                    result.put("data", proyectosPorEstado);
                    result.put("type", "pie");
                    break;
                    
                case "usuarios_rol":
                    List<Map<String, Object>> usuariosPorRol = jdbcTemplate.queryForList(
                        """
                        SELECT 
                            r.nombre_rol as label, 
                            COUNT(ur.usuario_usuario_id) as value
                        FROM rol r
                        LEFT JOIN usuario_has_rol ur ON r.rol_id = ur.rol_rol_id
                        LEFT JOIN usuario u ON ur.usuario_usuario_id = u.usuario_id 
                            AND u.estado_usuario = 'HABILITADO'
                        GROUP BY r.rol_id, r.nombre_rol
                        ORDER BY value DESC
                        """
                    );
                    result.put("data", usuariosPorRol);
                    result.put("type", "doughnut");
                    break;
                    
                case "tickets_prioridad":
                    List<Map<String, Object>> ticketsPorPrioridad = jdbcTemplate.queryForList(
                        "SELECT prioridad_ticket as label, COUNT(*) as value FROM ticket GROUP BY prioridad_ticket"
                    );
                    result.put("data", ticketsPorPrioridad);
                    result.put("type", "bar");
                    break;
                    
                default:
                    result.put("data", new ArrayList<>());
                    result.put("type", "bar");
            }
            
        } catch (Exception e) {
            result.put("error", "Error al obtener datos de gráfico: " + e.getMessage());
            result.put("data", new ArrayList<>());
            result.put("type", "bar");
        }
        
        return result;
    }

    /**
     * Obtiene datos específicos para gráfico de tickets
     */
    public Map<String, Object> getTicketsChartData() {
        Map<String, Object> chartData = new HashMap<>();
        
        try {
            // Tickets por estado (etapa)
            List<Map<String, Object>> ticketsPorEstado = jdbcTemplate.queryForList(
                "SELECT COALESCE(NULLIF(etapa_ticket, ''), 'Sin etapa') as label, COUNT(*) as value FROM ticket GROUP BY etapa_ticket"
            );
            
            // Tickets por prioridad
            List<Map<String, Object>> ticketsPorPrioridad = jdbcTemplate.queryForList(
                "SELECT prioridad_ticket as label, COUNT(*) as value FROM ticket GROUP BY prioridad_ticket"
            );
            
            // Tickets por tipo
            List<Map<String, Object>> ticketsPorTipo = jdbcTemplate.queryForList(
                "SELECT tipo_ticket as label, COUNT(*) as value FROM ticket GROUP BY tipo_ticket"
            );
            
            // Tickets pendientes urgentes (alta prioridad y pendientes/en progreso)
            List<Map<String, Object>> ticketsPendientesUrgentes = jdbcTemplate.queryForList(
                """
                SELECT t.asunto_ticket, t.prioridad_ticket, t.fecha_creacion,
                       u.username as reportado_por
                FROM ticket t
                INNER JOIN usuario u ON t.reportado_por_usuario_id = u.usuario_id
                WHERE t.etapa_ticket IN ('PENDIENTE', 'EN_PROGRESO') 
                  AND t.prioridad_ticket = 'ALTA'
                ORDER BY t.fecha_creacion ASC
                LIMIT 10
                """
            );
            
            chartData.put("ticketsPorEstado", ticketsPorEstado);
            chartData.put("ticketsPorPrioridad", ticketsPorPrioridad);
            chartData.put("ticketsPorTipo", ticketsPorTipo);
            chartData.put("ticketsPendientesUrgentes", ticketsPendientesUrgentes);
            
        } catch (Exception e) {
            chartData.put("error", "Error al obtener datos de tickets: " + e.getMessage());
            e.printStackTrace();
        }
        
        return chartData;
    }
    
    /**
     * Obtiene datos específicos para gráfico de usuarios
     */
    public Map<String, Object> getUsersChartData() {
        Map<String, Object> chartData = new HashMap<>();
        
        try {
            // Usuarios por rol - usando tabla correcta usuario_has_rol
            List<Map<String, Object>> usuariosPorRol = jdbcTemplate.queryForList(
                """
                SELECT r.nombre_rol as label, COUNT(u.usuario_id) as value
                FROM rol r 
                LEFT JOIN usuario_has_rol uhr ON r.rol_id = uhr.rol_rol_id 
                LEFT JOIN usuario u ON uhr.usuario_usuario_id = u.usuario_id 
                WHERE u.estado_usuario = 'HABILITADO' OR u.estado_usuario IS NULL 
                GROUP BY r.nombre_rol
                """
            );
            
            // Usuarios por estado
            List<Map<String, Object>> usuariosPorEstado = jdbcTemplate.queryForList(
                "SELECT estado_usuario as label, COUNT(*) as value FROM usuario GROUP BY estado_usuario"
            );
            
            // Usuarios por actividad
            List<Map<String, Object>> usuariosPorActividad = jdbcTemplate.queryForList(
                "SELECT actividad_usuario as label, COUNT(*) as value FROM usuario GROUP BY actividad_usuario"
            );
            
            // Solo métricas simples sin fechas
            chartData.put("usuariosPorRol", usuariosPorRol);
            chartData.put("usuariosPorEstado", usuariosPorEstado);
            chartData.put("usuariosPorActividad", usuariosPorActividad);
            
        } catch (Exception e) {
            chartData.put("error", "Error al obtener datos de usuarios: " + e.getMessage());
            e.printStackTrace();
        }
        
        return chartData;
    }
    
    /**
     * Obtiene datos específicos para gráfico de proyectos
     */
    public Map<String, Object> getProjectsChartData() {
        Map<String, Object> chartData = new HashMap<>();
        
        try {
            // Estados de proyectos - métricas simples y generales
            List<Map<String, Object>> proyectosPorEstado = jdbcTemplate.queryForList(
                "SELECT estado_proyecto as label, COUNT(*) as value FROM proyecto GROUP BY estado_proyecto"
            );
            
            // Visibilidad de proyectos (públicos vs privados)
            List<Map<String, Object>> proyectosPorVisibilidad = jdbcTemplate.queryForList(
                "SELECT visibilidad_proyecto as label, COUNT(*) as value FROM proyecto GROUP BY visibilidad_proyecto"
            );
            
            // Proyectos por tipo de propietario
            List<Map<String, Object>> proyectosPorPropietario = jdbcTemplate.queryForList(
                "SELECT COALESCE(NULLIF(propietario_proyecto, ''), 'Sin propietario') as label, COUNT(*) as value FROM proyecto GROUP BY propietario_proyecto"
            );
            
            // Solo métricas simples sin fechas
            chartData.put("proyectosPorEstado", proyectosPorEstado);
            chartData.put("proyectosPorVisibilidad", proyectosPorVisibilidad);
            chartData.put("proyectosPorPropietario", proyectosPorPropietario);
            
        } catch (Exception e) {
            chartData.put("error", "Error al obtener datos de proyectos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return chartData;
    }
    
    /**
     * Obtiene datos específicos para gráfico de repositorios
     */
    public Map<String, Object> getRepositoriesChartData() {
        Map<String, Object> chartData = new HashMap<>();
        
        try {
            // Repositorios por visibilidad (la tabla no tiene estado_repositorio)
            List<Map<String, Object>> repositoriosPorVisibilidad = jdbcTemplate.queryForList(
                "SELECT visibilidad_repositorio as label, COUNT(*) as value FROM repositorio GROUP BY visibilidad_repositorio"
            );
            
            // Repositorios por tipo
            List<Map<String, Object>> repositoriosPorTipo = jdbcTemplate.queryForList(
                "SELECT tipo_repositorio as label, COUNT(*) as value FROM repositorio GROUP BY tipo_repositorio"
            );
            
            // Repositorios por tipo
            List<Map<String, Object>> repositoriosPorTipo2 = jdbcTemplate.queryForList(
                "SELECT COALESCE(NULLIF(tipo_repositorio, ''), 'Sin tipo') as label, COUNT(*) as value FROM repositorio GROUP BY tipo_repositorio"
            );
            
            // Solo métricas simples sin fechas
            chartData.put("repositoriosPorVisibilidad", repositoriosPorVisibilidad);
            chartData.put("repositoriosPorTipo", repositoriosPorTipo2);
            
        } catch (Exception e) {
            chartData.put("error", "Error al obtener datos de repositorios: " + e.getMessage());
            e.printStackTrace();
        }
        
        return chartData;
    }

    /**
     * Obtiene resumen del dashboard para SuperAdmin
     */
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            // Combinar métricas generales con algunas específicas
            summary.putAll(getGeneralMetrics());
            
            // Agregar KPIs adicionales
            Integer proyectosActivos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM proyecto WHERE estado_proyecto IN ('EN_DESARROLLO', 'MANTENIMIENTO')", 
                Integer.class);
            
            Integer ticketsPendientes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ticket WHERE etapa_ticket IN ('PENDIENTE', 'EN_PROGRESO')", 
                Integer.class);
                
            Integer usuariosNuevos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuario WHERE fecha_creacion >= DATE_SUB(NOW(), INTERVAL 30 DAY)", 
                Integer.class);
            
            summary.put("proyectosActivos", proyectosActivos != null ? proyectosActivos : 0);
            summary.put("ticketsPendientes", ticketsPendientes != null ? ticketsPendientes : 0);
            summary.put("usuariosNuevos", usuariosNuevos != null ? usuariosNuevos : 0);
            
        } catch (Exception e) {
            summary.put("error", "Error al obtener resumen del dashboard: " + e.getMessage());
        }
        
        return summary;
    }


    // ==========================================
    // NUEVAS MÉTRICAS GENERALES PARA DASHBOARD
    // ==========================================

    /**
     * Obtiene métricas generales de repositorios
     */
    public Map<String, Object> getRepositoriosMetricsGeneral() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Repositorios por visibilidad (la tabla no tiene estado_repositorio)
            List<Map<String, Object>> repositoriosPorVisibilidad = jdbcTemplate.queryForList(
                """
                SELECT 
                    CASE 
                        WHEN visibilidad_repositorio = 'PUBLICO' THEN 'Público'
                        WHEN visibilidad_repositorio = 'PRIVADO' THEN 'Privado'
                        ELSE 'No Definido'
                    END as visibilidad,
                    COUNT(*) as total
                FROM repositorio 
                GROUP BY visibilidad_repositorio
                ORDER BY total DESC
                """
            );
            
            // Repositorios por tipo
            List<Map<String, Object>> repositoriosPorTipo = jdbcTemplate.queryForList(
                """
                SELECT 
                    CASE 
                        WHEN tipo_repositorio = 'PERSONAL' THEN 'Personal'
                        WHEN tipo_repositorio = 'COLABORATIVO' THEN 'Colaborativo'
                        ELSE 'No Definido'
                    END as tipo,
                    COUNT(*) as total
                FROM repositorio 
                GROUP BY tipo_repositorio
                ORDER BY total DESC
                """
            );
            
            // Repositorios sin actividad reciente (> 90 días)
            Integer repositoriosSinActividad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) 
                FROM repositorio 
                WHERE fecha_actualizacion < DATE_SUB(NOW(), INTERVAL 90 DAY)
                   OR fecha_actualizacion IS NULL
                """, 
                Integer.class
            );
            
            // Repositorios por tecnología principal (basado en nombre/descripción)
            List<Map<String, Object>> repositoriosPorTecnologia = jdbcTemplate.queryForList(
                """
                SELECT 
                    CASE 
                        WHEN LOWER(nombre_repositorio) LIKE '%java%' OR LOWER(descripcion_repositorio) LIKE '%java%' THEN 'Java'
                        WHEN LOWER(nombre_repositorio) LIKE '%python%' OR LOWER(descripcion_repositorio) LIKE '%python%' THEN 'Python'
                        WHEN LOWER(nombre_repositorio) LIKE '%javascript%' OR LOWER(descripcion_repositorio) LIKE '%javascript%' OR LOWER(nombre_repositorio) LIKE '%js%' THEN 'JavaScript'
                        WHEN LOWER(nombre_repositorio) LIKE '%react%' OR LOWER(descripcion_repositorio) LIKE '%react%' THEN 'React'
                        WHEN LOWER(nombre_repositorio) LIKE '%angular%' OR LOWER(descripcion_repositorio) LIKE '%angular%' THEN 'Angular'
                        WHEN LOWER(nombre_repositorio) LIKE '%spring%' OR LOWER(descripcion_repositorio) LIKE '%spring%' THEN 'Spring'
                        WHEN LOWER(nombre_repositorio) LIKE '%node%' OR LOWER(descripcion_repositorio) LIKE '%node%' THEN 'Node.js'
                        ELSE 'Otro'
                    END as tecnologia,
                    COUNT(*) as total
                FROM repositorio 
                GROUP BY tecnologia
                HAVING total > 0
                ORDER BY total DESC
                """
            );
            
            metrics.put("repositoriosPorVisibilidad", repositoriosPorVisibilidad);
            metrics.put("repositoriosPorTipo", repositoriosPorTipo);
            metrics.put("repositoriosSinActividad", repositoriosSinActividad != null ? repositoriosSinActividad : 0);
            metrics.put("repositoriosPorTecnologia", repositoriosPorTecnologia);
            
        } catch (Exception e) {
            metrics.put("error", "Error al obtener métricas de repositorios: " + e.getMessage());
            metrics.put("repositoriosPorVisibilidad", new ArrayList<>());
            metrics.put("repositoriosPorTipo", new ArrayList<>());
            metrics.put("repositoriosSinActividad", 0);
            metrics.put("repositoriosPorTecnologia", new ArrayList<>());
        }
        
        return metrics;
    }

    /**
     * Obtiene métricas generales de proyectos
     */
    public Map<String, Object> getProyectosMetricsGeneral() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Proyectos por estado
            List<Map<String, Object>> proyectosPorEstado = jdbcTemplate.queryForList(
                """
                SELECT 
                    COALESCE(estado_proyecto, 'SIN_ESTADO') as estado_proyecto,
                    COUNT(*) as total
                FROM proyecto 
                GROUP BY estado_proyecto
                ORDER BY total DESC
                """
            );
            
            // Proyectos por visibilidad
            List<Map<String, Object>> proyectosPorVisibilidad = jdbcTemplate.queryForList(
                """
                SELECT 
                    CASE 
                        WHEN visibilidad_proyecto = 'PUBLICO' THEN 'Público'
                        WHEN visibilidad_proyecto = 'PRIVADO' THEN 'Privado'
                        ELSE 'No Definido'
                    END as visibilidad_proyecto,
                    COUNT(*) as total
                FROM proyecto 
                GROUP BY visibilidad_proyecto
                ORDER BY total DESC
                """
            );
            
            // Proyectos sin actividad reciente (> 30 días)
            Integer proyectosSinActividad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) 
                FROM proyecto 
                WHERE updated_at < DATE_SUB(NOW(), INTERVAL 30 DAY)
                   OR updated_at IS NULL
                """, 
                Integer.class
            );
            
            // Proyectos por tipo de propietario
            List<Map<String, Object>> proyectosPorPropietario = jdbcTemplate.queryForList(
                """
                SELECT 
                    CASE 
                        WHEN propietario_proyecto LIKE '%admin%' OR propietario_proyecto LIKE '%sa%' THEN 'Administrador'
                        WHEN propietario_proyecto LIKE '%po%' OR propietario_proyecto LIKE '%product%' THEN 'Product Owner'
                        WHEN propietario_proyecto LIKE '%dev%' OR propietario_proyecto LIKE '%developer%' THEN 'Developer'
                        ELSE 'Otro'
                    END as tipo_propietario,
                    COUNT(*) as total
                FROM proyecto 
                WHERE propietario_proyecto IS NOT NULL
                GROUP BY tipo_propietario
                HAVING total > 0
                ORDER BY total DESC
                """
            );
            
            metrics.put("proyectosPorEstado", proyectosPorEstado);
            metrics.put("proyectosPorVisibilidad", proyectosPorVisibilidad);
            metrics.put("proyectosSinActividad", proyectosSinActividad != null ? proyectosSinActividad : 0);
            metrics.put("proyectosPorPropietario", proyectosPorPropietario);
            
        } catch (Exception e) {
            metrics.put("error", "Error al obtener métricas de proyectos: " + e.getMessage());
            metrics.put("proyectosPorEstado", new ArrayList<>());
            metrics.put("proyectosPorVisibilidad", new ArrayList<>());
            metrics.put("proyectosSinActividad", 0);
            metrics.put("proyectosPorPropietario", new ArrayList<>());
        }
        
        return metrics;
    }

    /**
     * Obtiene métricas generales de usuarios y roles
     */
    public Map<String, Object> getUsuariosRolesMetricsGeneral() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Usuarios por rol
            List<Map<String, Object>> usuariosPorRol = jdbcTemplate.queryForList(
                """
                SELECT 
                    r.nombre_rol,
                    COUNT(DISTINCT u.usuario_id) as total_usuarios
                FROM rol r
                LEFT JOIN usuario_has_rol ur ON r.rol_id = ur.rol_rol_id
                LEFT JOIN usuario u ON ur.usuario_usuario_id = u.usuario_id 
                    AND u.estado_usuario = 'HABILITADO'
                GROUP BY r.rol_id, r.nombre_rol
                ORDER BY total_usuarios DESC
                """
            );
            
            // Usuarios activos vs inactivos
            List<Map<String, Object>> usuariosPorActividad = jdbcTemplate.queryForList(
                """
                SELECT 
                    CASE 
                        WHEN actividad_usuario = 'ACTIVO' THEN 'Activos'
                        WHEN actividad_usuario = 'INACTIVO' THEN 'Inactivos'
                        ELSE 'No Definido'
                    END as actividad_usuario,
                    COUNT(*) as total
                FROM usuario 
                WHERE estado_usuario = 'HABILITADO'
                GROUP BY actividad_usuario
                ORDER BY total DESC
                """
            );
            
            // Usuarios sin proyectos asignados
            Integer usuariosSinProyectos = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(DISTINCT u.usuario_id)
                FROM usuario u
                LEFT JOIN usuario_has_proyecto up ON u.usuario_id = up.usuario_usuario_id
                WHERE u.estado_usuario = 'HABILITADO' 
                  AND u.actividad_usuario = 'ACTIVO'
                  AND up.usuario_usuario_id IS NULL
                """, 
                Integer.class
            );
            
            // Nuevos usuarios este mes
            Integer nuevosUsuarios = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) 
                FROM usuario 
                WHERE fecha_creacion >= DATE_SUB(NOW(), INTERVAL 1 MONTH)
                  AND estado_usuario = 'HABILITADO'
                """, 
                Integer.class
            );
            
            metrics.put("usuariosPorRol", usuariosPorRol);
            metrics.put("usuariosPorActividad", usuariosPorActividad);
            metrics.put("usuariosSinProyectos", usuariosSinProyectos != null ? usuariosSinProyectos : 0);
            metrics.put("nuevosUsuarios", nuevosUsuarios != null ? nuevosUsuarios : 0);
            
        } catch (Exception e) {
            metrics.put("error", "Error al obtener métricas de usuarios: " + e.getMessage());
            metrics.put("usuariosPorRol", new ArrayList<>());
            metrics.put("usuariosPorActividad", new ArrayList<>());
            metrics.put("usuariosSinProyectos", 0);
            metrics.put("nuevosUsuarios", 0);
        }
        
        return metrics;
    }

    /**
     * Obtiene métricas generales de tickets
     */
    public Map<String, Object> getTicketsMetricsGeneral() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Tickets por estado
            List<Map<String, Object>> ticketsPorEstado = jdbcTemplate.queryForList(
                """
                SELECT 
                    COALESCE(etapa_ticket, 'SIN_ETAPA') as etapa_ticket,
                    COUNT(*) as total
                FROM ticket 
                GROUP BY etapa_ticket
                ORDER BY total DESC
                """
            );
            
            // Tickets por prioridad
            List<Map<String, Object>> ticketsPorPrioridad = jdbcTemplate.queryForList(
                """
                SELECT 
                    COALESCE(prioridad_ticket, 'SIN_PRIORIDAD') as prioridad_ticket,
                    COUNT(*) as total
                FROM ticket 
                GROUP BY prioridad_ticket
                ORDER BY 
                    CASE prioridad_ticket
                        WHEN 'ALTA' THEN 1
                        WHEN 'MEDIA' THEN 2
                        WHEN 'BAJA' THEN 3
                        ELSE 4
                    END
                """
            );
            
            // Tickets sin asignar
            Integer ticketsSinAsignar = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) 
                FROM ticket 
                WHERE asignado_a_usuario_id IS NULL 
                  AND etapa_ticket IN ('PENDIENTE', 'EN_PROGRESO')
                """, 
                Integer.class
            );
            
            metrics.put("ticketsPorEstado", ticketsPorEstado);
            metrics.put("ticketsPorPrioridad", ticketsPorPrioridad);
            metrics.put("ticketsSinAsignar", ticketsSinAsignar != null ? ticketsSinAsignar : 0);
            
        } catch (Exception e) {
            metrics.put("error", "Error al obtener métricas de tickets: " + e.getMessage());
            metrics.put("ticketsPorEstado", new ArrayList<>());
            metrics.put("ticketsPorPrioridad", new ArrayList<>());
            metrics.put("ticketsSinAsignar", 0);
        }
        
        return metrics;
    }

    /**
     * Obtiene métricas generales de APIs
     */
    public Map<String, Object> getApisMetricsGeneral() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // APIs por estado
            List<Map<String, Object>> apisPorEstado = jdbcTemplate.queryForList(
                """
                SELECT 
                    COALESCE(estado_api, 'SIN_ESTADO') as estado_api,
                    COUNT(*) as total
                FROM api 
                GROUP BY estado_api
                ORDER BY total DESC
                """
            );
            
            // APIs por categoría (contando las asociaciones)
            List<Map<String, Object>> apisPorCategoria = jdbcTemplate.queryForList(
                """
                SELECT 
                    c.nombre_categoria,
                    COUNT(cha.api_api_id) as total_apis
                FROM categoria c
                LEFT JOIN categoria_has_api cha ON c.id_categoria = cha.categoria_id_categoria
                GROUP BY c.id_categoria, c.nombre_categoria
                HAVING total_apis > 0
                ORDER BY total_apis DESC
                LIMIT 10
                """
            );
            
            // APIs sin documentar (sin descripción o con descripción muy corta)
            Integer apisSinDocumentar = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) 
                FROM api 
                WHERE descripcion_api IS NULL 
                   OR LENGTH(TRIM(descripcion_api)) < 10
                """, 
                Integer.class
            );
            
            metrics.put("apisPorEstado", apisPorEstado);
            metrics.put("apisPorCategoria", apisPorCategoria);
            metrics.put("apisSinDocumentar", apisSinDocumentar != null ? apisSinDocumentar : 0);
            
        } catch (Exception e) {
            metrics.put("error", "Error al obtener métricas de APIs: " + e.getMessage());
            metrics.put("apisPorEstado", new ArrayList<>());
            metrics.put("apisPorCategoria", new ArrayList<>());
            metrics.put("apisSinDocumentar", 0);
        }
        
        return metrics;
    }

    /**
     * Obtiene todas las métricas generales consolidadas para el dashboard
     */
    public Map<String, Object> getAllGeneralMetrics() {
        Map<String, Object> allMetrics = new HashMap<>();
        
        try {
            // Métricas generales básicas
            Map<String, Object> generalMetrics = getGeneralMetrics();
            allMetrics.putAll(generalMetrics);
            
            // Métricas específicas por categoría
            allMetrics.put("repositoriosMetrics", getRepositoriosMetricsGeneral());
            allMetrics.put("proyectosMetrics", getProyectosMetricsGeneral());
            allMetrics.put("usuariosMetrics", getUsuariosRolesMetricsGeneral());
            allMetrics.put("ticketsMetrics", getTicketsMetricsGeneral());
            allMetrics.put("apisMetrics", getApisMetricsGeneral());
            
            // Total de categorías
            Integer totalCategorias = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM categoria", 
                Integer.class
            );
            allMetrics.put("totalCategorias", totalCategorias != null ? totalCategorias : 0);
            
        } catch (Exception e) {
            allMetrics.put("error", "Error al obtener métricas consolidadas: " + e.getMessage());
        }
        
        return allMetrics;
    }

}
