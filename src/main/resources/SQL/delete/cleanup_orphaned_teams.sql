-- ===== SCRIPT DE LIMPIEZA DE EQUIPOS HUÉRFANOS =====
-- Elimina equipos que:
-- 1. NO tienen creador (creado_por_usuario_id IS NULL) - estos vienen de inserts_dev_portal.sql
-- 2. NO están asociados a ningún proyecto
-- 3. NO están asociados a ningún repositorio
-- 
-- NOTA: Estos son equipos base creados sin contexto (Backend Development, Frontend Development, QA, etc)
--       que violaban la regla de negocio de que TODOS los equipos deben estar asociados a un proyecto o repositorio

START TRANSACTION;

-- 1. VER EQUIPOS A ELIMINAR (para revisar primero)
SELECT 
    e.equipo_id,
    e.nombre_equipo,
    e.creado_por_usuario_id,
    e.fecha_creacion,
    COUNT(DISTINCT ehp.proyecto_proyecto_id) as num_proyectos,
    COUNT(DISTINCT ehr.repositorio_repositorio_id) as num_repositorios
FROM equipo e
LEFT JOIN equipo_has_proyecto ehp ON e.equipo_id = ehp.equipo_equipo_id
LEFT JOIN equipo_has_repositorio ehr ON e.equipo_id = ehr.equipo_equipo_id
WHERE e.creado_por_usuario_id IS NULL
GROUP BY e.equipo_id, e.nombre_equipo, e.creado_por_usuario_id, e.fecha_creacion
HAVING COUNT(DISTINCT ehp.proyecto_proyecto_id) = 0 AND COUNT(DISTINCT ehr.repositorio_repositorio_id) = 0;

-- 2. ELIMINAR RELACIONES EN usuario_has_equipo (si las hay)
DELETE FROM usuario_has_equipo 
WHERE equipo_equipo_id IN (
    SELECT e.equipo_id FROM equipo e
    LEFT JOIN equipo_has_proyecto ehp ON e.equipo_id = ehp.equipo_equipo_id
    LEFT JOIN equipo_has_repositorio ehr ON e.equipo_id = ehr.equipo_equipo_id
    WHERE e.creado_por_usuario_id IS NULL
    GROUP BY e.equipo_id
    HAVING COUNT(DISTINCT ehp.proyecto_proyecto_id) = 0 AND COUNT(DISTINCT ehr.repositorio_repositorio_id) = 0
);

-- 3. ELIMINAR LOS EQUIPOS HUÉRFANOS
DELETE FROM equipo
WHERE creado_por_usuario_id IS NULL
  AND equipo_id NOT IN (
      SELECT DISTINCT equipo_equipo_id FROM equipo_has_proyecto
      UNION
      SELECT DISTINCT equipo_equipo_id FROM equipo_has_repositorio
  );

-- ROLLBACK; -- Descomentar para SOLO VER QUÉ SE VA A ELIMINAR (sin aplicar cambios)
COMMIT; -- Aplica los cambios
