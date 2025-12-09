-- ============================================================================
-- QUERIES PARA TESTING DEL SISTEMA DE EQUIPOS
-- ============================================================================
-- Reemplaza los valores de ejemplo con IDs reales de tu base de datos

-- ============================================================================
-- 1. VERIFICAR ESTRUCTURA DE TABLAS
-- ============================================================================

-- Ver estructura de tabla usuario_has_proyecto
DESCRIBE usuario_has_proyecto;

-- Ver estructura de tabla usuario_has_repositorio
DESCRIBE usuario_has_repositorio;

-- Ver estructura de tabla proyecto
DESCRIBE proyecto;

-- Ver estructura de tabla repositorio
DESCRIBE repositorio;

-- Ver estructura de tabla equipo
DESCRIBE equipo;


-- ============================================================================
-- 2. VERIFICAR DATOS BÁSICOS DEL USUARIO
-- ============================================================================

-- Obtener un usuario específico (cambiar ID según necesites)
SELECT usuario_id, nombre_usuario, username, correo 
FROM usuario 
WHERE usuario_id = 1 
LIMIT 1;

-- Listar todos los usuarios (para encontrar el que vas a usar para testing)
SELECT usuario_id, nombre_usuario, username, correo 
FROM usuario 
LIMIT 10;


-- ============================================================================
-- 3. PROYECTOS GRUPALES DE LOS QUE EL USUARIO FORMA PARTE
-- ============================================================================

-- Query principal: Proyectos GRUPO donde el usuario participa
SELECT DISTINCT 
    p.proyecto_id,
    p.nombre_proyecto,
    p.descripcion_proyecto,
    p.propietario_proyecto,
    p.propietario_id,
    p.estado_proyecto,
    uhp.rol_proyecto,
    uhp.privilegio_usuario_proyecto,
    uhp.fecha_asignacion
FROM proyecto p
INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
WHERE uhp.usuario_usuario_id = 1  -- Cambiar 1 por el usuario_id que desees probar
  AND p.propietario_proyecto = 'GRUPO'
ORDER BY p.nombre_proyecto;

-- Verificación: Contar proyectos grupales del usuario
SELECT COUNT(*) as cantidad_proyectos_grupo
FROM proyecto p
INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
WHERE uhp.usuario_usuario_id = 1  -- Cambiar 1 por el usuario_id que desees probar
  AND p.propietario_proyecto = 'GRUPO';


-- ============================================================================
-- 4. PROYECTOS EMPRESARIALES DE LOS QUE EL USUARIO FORMA PARTE
-- ============================================================================

-- Query principal: Proyectos EMPRESA donde el usuario participa
SELECT DISTINCT 
    p.proyecto_id,
    p.nombre_proyecto,
    p.descripcion_proyecto,
    p.propietario_proyecto,
    p.propietario_id,
    p.estado_proyecto,
    uhp.rol_proyecto,
    uhp.privilegio_usuario_proyecto,
    uhp.fecha_asignacion
FROM proyecto p
INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
WHERE uhp.usuario_usuario_id = 1  -- Cambiar 1 por el usuario_id que desees probar
  AND p.propietario_proyecto = 'EMPRESA'
ORDER BY p.nombre_proyecto;

-- Verificación: Contar proyectos empresariales del usuario
SELECT COUNT(*) as cantidad_proyectos_empresa
FROM proyecto p
INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
WHERE uhp.usuario_usuario_id = 1  -- Cambiar 1 por el usuario_id que desees probar
  AND p.propietario_proyecto = 'EMPRESA';


-- ============================================================================
-- 5. REPOSITORIOS COLABORATIVOS DE LOS QUE EL USUARIO FORMA PARTE
-- ============================================================================

-- Query principal: Repositorios COLABORATIVO donde el usuario participa
SELECT DISTINCT 
    r.repositorio_id,
    r.nombre_repositorio,
    r.descripcion_repositorio,
    r.tipo_repositorio,
    r.propietario_id,
    r.estado_repositorio,
    uhr.rol_repositorio,
    uhr.privilegio_usuario_repositorio,
    uhr.fecha_asignacion
FROM repositorio r
INNER JOIN usuario_has_repositorio uhr ON r.repositorio_id = uhr.repositorio_repositorio_id
WHERE uhr.usuario_usuario_id = 1  -- Cambiar 1 por el usuario_id que desees probar
  AND r.tipo_repositorio = 'COLABORATIVO'
ORDER BY r.nombre_repositorio;

-- Verificación: Contar repositorios colaborativos del usuario
SELECT COUNT(*) as cantidad_repositorios_colaborativos
FROM repositorio r
INNER JOIN usuario_has_repositorio uhr ON r.repositorio_id = uhr.repositorio_repositorio_id
WHERE uhr.usuario_usuario_id = 1  -- Cambiar 1 por el usuario_id que desees probar
  AND r.tipo_repositorio = 'COLABORATIVO';


-- ============================================================================
-- 6. RESUMEN COMPLETO: TODOS LOS PROYECTOS DEL USUARIO
-- ============================================================================

-- Ver todos los proyectos de un usuario (sin filtrar por tipo)
SELECT DISTINCT 
    p.proyecto_id,
    p.nombre_proyecto,
    p.propietario_proyecto as tipo_proyecto,
    p.estado_proyecto,
    'PROYECTO' as entidad_tipo
FROM proyecto p
INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
WHERE uhp.usuario_usuario_id = 1  -- Cambiar 1 por el usuario_id que desees probar
UNION ALL
-- Ver todos los repositorios del usuario
SELECT DISTINCT 
    r.repositorio_id,
    r.nombre_repositorio,
    r.tipo_repositorio,
    r.estado_repositorio,
    'REPOSITORIO' as entidad_tipo
FROM repositorio r
INNER JOIN usuario_has_repositorio uhr ON r.repositorio_id = uhr.repositorio_repositorio_id
WHERE uhr.usuario_usuario_id = 1  -- Cambiar 1 por el usuario_id que desees probar
ORDER BY tipo_proyecto, nombre_proyecto;


-- ============================================================================
-- 7. EQUIPOS EN PROYECTOS
-- ============================================================================

-- Listar todos los equipos
SELECT 
    e.equipo_id,
    e.nombre_equipo,
    COUNT(DISTINCT uhe.usuario_usuario_id) as cantidad_miembros
FROM equipo e
LEFT JOIN usuario_has_equipo uhe ON e.equipo_id = uhe.equipo_equipo_id
GROUP BY e.equipo_id, e.nombre_equipo;

-- Detalles de un equipo específico con sus miembros
SELECT 
    e.equipo_id,
    e.nombre_equipo,
    u.usuario_id,
    u.nombre_usuario,
    u.username,
    u.correo,
    uhe.rol_equipo,
    uhe.privilegio_usuario_equipo
FROM equipo e
LEFT JOIN usuario_has_equipo uhe ON e.equipo_id = uhe.equipo_equipo_id
LEFT JOIN usuario u ON uhe.usuario_usuario_id = u.usuario_id
WHERE e.equipo_id = 1  -- Cambiar 1 por el equipo_id que desees ver
ORDER BY e.nombre_equipo, u.nombre_usuario;

-- Equipos asociados a un proyecto específico
SELECT DISTINCT
    e.equipo_id,
    e.nombre_equipo,
    COUNT(DISTINCT uhe.usuario_usuario_id) as cantidad_miembros
FROM equipo e
LEFT JOIN equipo_has_proyecto ehp ON e.equipo_id = ehp.equipo_equipo_id
LEFT JOIN usuario_has_equipo uhe ON e.equipo_id = uhe.equipo_equipo_id
WHERE ehp.proyecto_proyecto_id = 1  -- Cambiar 1 por el proyecto_id
GROUP BY e.equipo_id, e.nombre_equipo;

-- Equipos asociados a un repositorio específico
SELECT DISTINCT
    e.equipo_id,
    e.nombre_equipo,
    COUNT(DISTINCT uhe.usuario_usuario_id) as cantidad_miembros
FROM equipo e
LEFT JOIN equipo_has_repositorio ehr ON e.equipo_id = ehr.equipo_equipo_id
LEFT JOIN usuario_has_equipo uhe ON e.equipo_id = uhe.equipo_equipo_id
WHERE ehr.repositorio_repositorio_id = 1  -- Cambiar 1 por el repositorio_id
GROUP BY e.equipo_id, e.nombre_equipo;


-- ============================================================================
-- 8. PRIVILEGIOS DEL USUARIO EN PROYECTOS Y REPOSITORIOS
-- ============================================================================

-- Ver los privilegios del usuario en cada proyecto
SELECT 
    p.proyecto_id,
    p.nombre_proyecto,
    uhp.privilegio_usuario_proyecto as privilegio,
    CASE 
        WHEN uhp.privilegio_usuario_proyecto = 'EDITOR' THEN 'Puede editar'
        WHEN uhp.privilegio_usuario_proyecto = 'LECTOR' THEN 'Solo lectura'
        WHEN uhp.privilegio_usuario_proyecto = 'COMENTADOR' THEN 'Puede comentar'
        ELSE 'Sin especificar'
    END as descripcion_privilegio
FROM proyecto p
INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
WHERE uhp.usuario_usuario_id = 1  -- Cambiar 1 por el usuario_id
ORDER BY p.nombre_proyecto;

-- Ver los privilegios del usuario en cada repositorio
SELECT 
    r.repositorio_id,
    r.nombre_repositorio,
    uhr.privilegio_usuario_repositorio as privilegio,
    CASE 
        WHEN uhr.privilegio_usuario_repositorio = 'EDITOR' THEN 'Puede editar'
        WHEN uhr.privilegio_usuario_repositorio = 'LECTOR' THEN 'Solo lectura'
        WHEN uhr.privilegio_usuario_repositorio = 'COMENTADOR' THEN 'Puede comentar'
        ELSE 'Sin especificar'
    END as descripcion_privilegio
FROM repositorio r
INNER JOIN usuario_has_repositorio uhr ON r.repositorio_id = uhr.repositorio_repositorio_id
WHERE uhr.usuario_usuario_id = 1  -- Cambiar 1 por el usuario_id
ORDER BY r.nombre_repositorio;


-- ============================================================================
-- 9. VERIFICACIÓN DE INTEGRIDAD: USUARIOS SIN PROYECTOS/REPOSITORIOS
-- ============================================================================

-- Usuarios que no tienen asignado ningún proyecto
SELECT DISTINCT u.usuario_id, u.nombre_usuario, u.username
FROM usuario u
WHERE u.usuario_id NOT IN (
    SELECT DISTINCT usuario_usuario_id FROM usuario_has_proyecto
)
LIMIT 10;

-- Usuarios que no tienen asignado ningún repositorio
SELECT DISTINCT u.usuario_id, u.nombre_usuario, u.username
FROM usuario u
WHERE u.usuario_id NOT IN (
    SELECT DISTINCT usuario_usuario_id FROM usuario_has_repositorio
)
LIMIT 10;

-- Proyectos que no tienen usuarios asignados
SELECT DISTINCT p.proyecto_id, p.nombre_proyecto
FROM proyecto p
WHERE p.proyecto_id NOT IN (
    SELECT DISTINCT proyecto_proyecto_id FROM usuario_has_proyecto
)
LIMIT 10;

-- Repositorios que no tienen usuarios asignados
SELECT DISTINCT r.repositorio_id, r.nombre_repositorio
FROM repositorio r
WHERE r.repositorio_id NOT IN (
    SELECT DISTINCT repositorio_repositorio_id FROM usuario_has_repositorio
)
LIMIT 10;


-- ============================================================================
-- 10. ESTADÍSTICAS Y ANÁLISIS
-- ============================================================================

-- Total de usuarios con al menos un proyecto o repositorio
SELECT 
    (SELECT COUNT(DISTINCT usuario_usuario_id) FROM usuario_has_proyecto) as usuarios_con_proyectos,
    (SELECT COUNT(DISTINCT usuario_usuario_id) FROM usuario_has_repositorio) as usuarios_con_repositorios,
    (SELECT COUNT(DISTINCT usuario_id) FROM usuario) as total_usuarios;

-- Proyectos por tipo (cuántos GRUPO, cuántos EMPRESA)
SELECT 
    propietario_proyecto as tipo_proyecto,
    COUNT(*) as cantidad
FROM proyecto
GROUP BY propietario_proyecto;

-- Repositorios por tipo
SELECT 
    tipo_repositorio as tipo,
    COUNT(*) as cantidad
FROM repositorio
GROUP BY tipo_repositorio;

-- Usuario con más proyectos
SELECT 
    u.usuario_id,
    u.nombre_usuario,
    COUNT(DISTINCT uhp.proyecto_proyecto_id) as cantidad_proyectos
FROM usuario u
LEFT JOIN usuario_has_proyecto uhp ON u.usuario_id = uhp.usuario_usuario_id
GROUP BY u.usuario_id, u.nombre_usuario
ORDER BY cantidad_proyectos DESC
LIMIT 10;

-- Usuario con más repositorios
SELECT 
    u.usuario_id,
    u.nombre_usuario,
    COUNT(DISTINCT uhr.repositorio_repositorio_id) as cantidad_repositorios
FROM usuario u
LEFT JOIN usuario_has_repositorio uhr ON u.usuario_id = uhr.usuario_usuario_id
GROUP BY u.usuario_id, u.nombre_usuario
ORDER BY cantidad_repositorios DESC
LIMIT 10;


-- ============================================================================
-- 11. VERIFICACIÓN DE EQUIPOS (IMPORTANTE PARA TESTING)
-- ============================================================================

-- Contar cuántos equipos hay en total
SELECT COUNT(*) as total_equipos FROM equipo;

-- Verificar si existen equipos asociados a proyectos
SELECT COUNT(*) as equipos_en_proyectos FROM equipo_has_proyecto;

-- Verificar si existen equipos asociados a repositorios
SELECT COUNT(*) as equipos_en_repositorios FROM equipo_has_repositorio;

-- Listar equipos con detalle completo
SELECT 
    e.equipo_id,
    e.nombre_equipo,
    COUNT(DISTINCT uhe.usuario_usuario_id) as cantidad_miembros,
    GROUP_CONCAT(DISTINCT u.nombre_usuario SEPARATOR ', ') as miembros
FROM equipo e
LEFT JOIN usuario_has_equipo uhe ON e.equipo_id = uhe.equipo_equipo_id
LEFT JOIN usuario u ON uhe.usuario_usuario_id = u.usuario_id
GROUP BY e.equipo_id, e.nombre_equipo;


-- ============================================================================
-- 12. TEST FINAL: QUERY COMPLETA QUE USA LA LÓGICA DE TEAMSERVICE
-- ============================================================================

-- Esto simula exactamente lo que hace el TeamService para usuario_id = 1
-- PROYECTOS GRUPO
SELECT 
    'PROYECTO_GRUPO' as tipo,
    p.proyecto_id as id,
    p.nombre_proyecto as nombre,
    p.descripcion_proyecto as descripcion,
    p.propietario_proyecto as propietario
FROM proyecto p
INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
WHERE uhp.usuario_usuario_id = 1
  AND p.propietario_proyecto = 'GRUPO'
UNION ALL
-- PROYECTOS EMPRESA
SELECT 
    'PROYECTO_EMPRESA' as tipo,
    p.proyecto_id as id,
    p.nombre_proyecto as nombre,
    p.descripcion_proyecto as descripcion,
    p.propietario_proyecto as propietario
FROM proyecto p
INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
WHERE uhp.usuario_usuario_id = 1
  AND p.propietario_proyecto = 'EMPRESA'
UNION ALL
-- REPOSITORIOS COLABORATIVO
SELECT 
    'REPOSITORIO_COLABORATIVO' as tipo,
    r.repositorio_id as id,
    r.nombre_repositorio as nombre,
    r.descripcion_repositorio as descripcion,
    r.tipo_repositorio as propietario
FROM repositorio r
INNER JOIN usuario_has_repositorio uhr ON r.repositorio_id = uhr.repositorio_repositorio_id
WHERE uhr.usuario_usuario_id = 1
  AND r.tipo_repositorio = 'COLABORATIVO'
ORDER BY tipo, nombre;


-- ============================================================================
-- INSTRUCCIONES DE USO:
-- ============================================================================
-- 1. Reemplaza "1" por el usuario_id que desees probar en todas las queries
-- 2. Ejecuta primero las queries de VERIFICACIÓN DE ESTRUCTURA DE TABLAS
-- 3. Verifica que el usuario existe con VERIFICAR DATOS BÁSICOS DEL USUARIO
-- 4. Ejecuta las queries de PROYECTOS GRUPALES, EMPRESARIALES y REPOSITORIOS
-- 5. Ejecuta el TEST FINAL para ver el resultado completo
-- 6. Compara los resultados con lo que el TeamService devuelve
-- ============================================================================
