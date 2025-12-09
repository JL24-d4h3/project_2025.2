-- ===== DIAGNÓSTICO DE EQUIPOS =====
-- Este script revisa la integridad de los datos de equipos

-- 1. Verificar equipos sin creador
SELECT 
    e.equipo_id,
    e.nombre_equipo,
    e.creado_por_usuario_id,
    e.fecha_creacion
FROM equipo e
WHERE e.creado_por_usuario_id IS NULL
ORDER BY e.equipo_id;

-- 2. Verificar equipos sin asociaciones (ni proyectos ni repositorios)
SELECT 
    e.equipo_id,
    e.nombre_equipo,
    e.creado_por_usuario_id,
    COUNT(DISTINCT ehp.proyecto_proyecto_id) as num_proyectos,
    COUNT(DISTINCT ehr.repositorio_repositorio_id) as num_repositorios
FROM equipo e
LEFT JOIN equipo_has_proyecto ehp ON e.equipo_id = ehp.equipo_equipo_id
LEFT JOIN equipo_has_repositorio ehr ON e.equipo_id = ehr.equipo_equipo_id
GROUP BY e.equipo_id, e.nombre_equipo, e.creado_por_usuario_id
HAVING COUNT(DISTINCT ehp.proyecto_proyecto_id) = 0 AND COUNT(DISTINCT ehr.repositorio_repositorio_id) = 0
ORDER BY e.equipo_id;

-- 3. Verificar asociaciones de equipos a proyectos
SELECT 
    e.equipo_id,
    e.nombre_equipo,
    u.username as creador,
    p.proyecto_id,
    p.nombre_proyecto,
    p.propietario_proyecto,
    ehp.privilegio_equipo_proyecto
FROM equipo e
LEFT JOIN usuario u ON e.creado_por_usuario_id = u.usuario_id
LEFT JOIN equipo_has_proyecto ehp ON e.equipo_id = ehp.equipo_equipo_id
LEFT JOIN proyecto p ON ehp.proyecto_proyecto_id = p.proyecto_id
ORDER BY e.equipo_id, p.proyecto_id;

-- 4. Verificar asociaciones de equipos a repositorios
SELECT 
    e.equipo_id,
    e.nombre_equipo,
    u.username as creador,
    r.repositorio_id,
    r.nombre_repositorio,
    r.tipo_repositorio,
    ehr.privilegio_equipo_repositorio
FROM equipo e
LEFT JOIN usuario u ON e.creado_por_usuario_id = u.usuario_id
LEFT JOIN equipo_has_repositorio ehr ON e.equipo_id = ehr.equipo_equipo_id
LEFT JOIN repositorio r ON ehr.repositorio_repositorio_id = r.repositorio_id
ORDER BY e.equipo_id, r.repositorio_id;

-- 5. Contar por categoría
SELECT 
    COUNT(DISTINCT CASE WHEN ehp.proyecto_proyecto_id IS NOT NULL THEN e.equipo_id ELSE NULL END) as equipos_con_proyectos,
    COUNT(DISTINCT CASE WHEN ehr.repositorio_repositorio_id IS NOT NULL THEN e.equipo_id ELSE NULL END) as equipos_con_repositorios,
    COUNT(DISTINCT e.equipo_id) as total_equipos
FROM equipo e
LEFT JOIN equipo_has_proyecto ehp ON e.equipo_id = ehp.equipo_equipo_id
LEFT JOIN equipo_has_repositorio ehr ON e.equipo_id = ehr.equipo_equipo_id;

-- 6. Verificar qué equipos tiene rbeltran (usuario_id = 32)
SELECT 
    e.equipo_id,
    e.nombre_equipo,
    e.creado_por_usuario_id,
    COUNT(DISTINCT ehp.proyecto_proyecto_id) as num_proyectos,
    COUNT(DISTINCT ehr.repositorio_repositorio_id) as num_repositorios
FROM equipo e
LEFT JOIN equipo_has_proyecto ehp ON e.equipo_id = ehp.equipo_equipo_id
LEFT JOIN equipo_has_repositorio ehr ON e.equipo_id = ehr.equipo_equipo_id
WHERE e.creado_por_usuario_id = 32
GROUP BY e.equipo_id, e.nombre_equipo, e.creado_por_usuario_id
ORDER BY e.equipo_id;
