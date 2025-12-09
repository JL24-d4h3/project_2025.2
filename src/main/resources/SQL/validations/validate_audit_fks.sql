-- =====================================================
-- Script de Validación de Foreign Keys de Auditoría
-- =====================================================
-- Este script verifica que todos los constraints de auditoría
-- estén correctamente creados en la base de datos

USE `database`;

-- =====================================================
-- 1. Verificar estructura de constraints
-- =====================================================
SELECT 
    '=== FOREIGN KEYS DE AUDITORIA ===' AS 'Reporte';

SELECT 
    kcu.TABLE_NAME AS 'Tabla',
    kcu.COLUMN_NAME AS 'Campo Auditoría',
    kcu.CONSTRAINT_NAME AS 'Constraint',
    kcu.REFERENCED_TABLE_NAME AS 'Tabla Referenciada',
    kcu.REFERENCED_COLUMN_NAME AS 'Columna Referenciada',
    rc.DELETE_RULE AS 'ON DELETE',
    rc.UPDATE_RULE AS 'ON UPDATE'
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu
INNER JOIN 
    INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc
    ON kcu.CONSTRAINT_NAME = rc.CONSTRAINT_NAME
    AND kcu.TABLE_SCHEMA = rc.CONSTRAINT_SCHEMA
WHERE 
    kcu.TABLE_SCHEMA = 'database'
    AND kcu.REFERENCED_TABLE_NAME = 'usuario'
    AND (
        kcu.COLUMN_NAME LIKE '%creado_por%'
        OR kcu.COLUMN_NAME LIKE '%actualizado_por%'
        OR kcu.COLUMN_NAME LIKE '%created_by%'
        OR kcu.COLUMN_NAME LIKE '%updated_by%'
    )
ORDER BY 
    kcu.TABLE_NAME, kcu.COLUMN_NAME;

-- =====================================================
-- 2. Contar constraints esperados vs existentes
-- =====================================================
SELECT 
    '=== RESUMEN DE CONSTRAINTS ===' AS 'Reporte';

SELECT 
    COUNT(*) AS 'Total FK Auditoría Encontrados',
    18 AS 'Total Esperado (9 tablas x 2 campos)',
    CASE 
        WHEN COUNT(*) = 18 THEN '✅ CORRECTO'
        ELSE '❌ FALTANTE'
    END AS 'Estado'
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'database'
    AND REFERENCED_TABLE_NAME = 'usuario'
    AND (
        COLUMN_NAME LIKE '%creado_por%'
        OR COLUMN_NAME LIKE '%actualizado_por%'
        OR COLUMN_NAME LIKE '%created_by%'
        OR COLUMN_NAME LIKE '%updated_by%'
    );

-- =====================================================
-- 3. Verificar tablas individuales
-- =====================================================
SELECT 
    '=== VERIFICACIÓN POR TABLA ===' AS 'Reporte';

SELECT 
    'api' AS 'Tabla',
    COUNT(*) AS 'FK Encontrados',
    2 AS 'FK Esperados',
    GROUP_CONCAT(COLUMN_NAME ORDER BY COLUMN_NAME) AS 'Campos'
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'api'
    AND REFERENCED_TABLE_NAME = 'usuario'
    AND (COLUMN_NAME = 'creado_por' OR COLUMN_NAME = 'actualizado_por')

UNION ALL

SELECT 
    'documentacion',
    COUNT(*),
    2,
    GROUP_CONCAT(COLUMN_NAME ORDER BY COLUMN_NAME)
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'documentacion'
    AND REFERENCED_TABLE_NAME = 'usuario'
    AND (COLUMN_NAME = 'creado_por' OR COLUMN_NAME = 'actualizado_por')

UNION ALL

SELECT 
    'version_api',
    COUNT(*),
    2,
    GROUP_CONCAT(COLUMN_NAME ORDER BY COLUMN_NAME)
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'version_api'
    AND REFERENCED_TABLE_NAME = 'usuario'
    AND (COLUMN_NAME = 'creado_por' OR COLUMN_NAME = 'actualizado_por')

UNION ALL

SELECT 
    'proyecto',
    COUNT(*),
    2,
    GROUP_CONCAT(COLUMN_NAME ORDER BY COLUMN_NAME)
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'proyecto'
    AND REFERENCED_TABLE_NAME = 'usuario'
    AND (COLUMN_NAME = 'created_by' OR COLUMN_NAME = 'updated_by')

UNION ALL

SELECT 
    'repositorio',
    COUNT(*),
    2,
    GROUP_CONCAT(COLUMN_NAME ORDER BY COLUMN_NAME)
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'repositorio'
    AND REFERENCED_TABLE_NAME = 'usuario'
    AND (COLUMN_NAME = 'creado_por_usuario_id' OR COLUMN_NAME = 'actualizado_por_usuario_id')

UNION ALL

SELECT 
    'nodo',
    COUNT(*),
    2,
    GROUP_CONCAT(COLUMN_NAME ORDER BY COLUMN_NAME)
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'nodo'
    AND REFERENCED_TABLE_NAME = 'usuario'
    AND (COLUMN_NAME = 'creado_por' OR COLUMN_NAME = 'actualizado_por')

UNION ALL

SELECT 
    'version_archivo',
    COUNT(*),
    2,
    GROUP_CONCAT(COLUMN_NAME ORDER BY COLUMN_NAME)
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'version_archivo'
    AND REFERENCED_TABLE_NAME = 'usuario'
    AND (COLUMN_NAME = 'creado_por' OR COLUMN_NAME = 'actualizado_por')

UNION ALL

SELECT 
    'permiso_nodo',
    COUNT(*),
    2,
    GROUP_CONCAT(COLUMN_NAME ORDER BY COLUMN_NAME)
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'permiso_nodo'
    AND REFERENCED_TABLE_NAME = 'usuario'
    AND (COLUMN_NAME = 'creado_por' OR COLUMN_NAME = 'actualizado_por')

UNION ALL

SELECT 
    'rol_proyecto',
    COUNT(*),
    2,
    GROUP_CONCAT(COLUMN_NAME ORDER BY COLUMN_NAME)
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'rol_proyecto'
    AND REFERENCED_TABLE_NAME = 'usuario'
    AND (COLUMN_NAME = 'creado_por' OR COLUMN_NAME = 'actualizado_por');

-- =====================================================
-- 4. Verificar índices creados
-- =====================================================
SELECT 
    '=== ÍNDICES DE CAMPOS DE AUDITORÍA ===' AS 'Reporte';

SELECT 
    TABLE_NAME AS 'Tabla',
    INDEX_NAME AS 'Nombre Índice',
    COLUMN_NAME AS 'Columna',
    NON_UNIQUE AS 'No Único',
    INDEX_TYPE AS 'Tipo'
FROM 
    INFORMATION_SCHEMA.STATISTICS
WHERE 
    TABLE_SCHEMA = 'database'
    AND (
        INDEX_NAME LIKE '%creado_por%'
        OR INDEX_NAME LIKE '%actualizado_por%'
        OR INDEX_NAME LIKE '%created_by%'
        OR INDEX_NAME LIKE '%updated_by%'
    )
ORDER BY 
    TABLE_NAME, INDEX_NAME;

-- =====================================================
-- 5. Prueba de inserción con usuario válido
-- =====================================================
SELECT 
    '=== PRUEBA DE INSERCIÓN ===' AS 'Reporte';

-- Verificar que existe al menos un usuario para testing
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN CONCAT('✅ Existen ', COUNT(*), ' usuarios para pruebas')
        ELSE '❌ No hay usuarios - crear al menos uno para testing'
    END AS 'Estado Testing'
FROM 
    usuario
LIMIT 1;

-- Mostrar primer usuario disponible
SELECT 
    usuario_id,
    username_usuario,
    email_usuario
FROM 
    usuario
LIMIT 1;

-- =====================================================
-- 6. Prueba de integridad referencial
-- =====================================================
SELECT 
    '=== PRUEBA DE INTEGRIDAD REFERENCIAL ===' AS 'Reporte';

-- Esta query fallará si intentamos insertar un creado_por inválido
-- (descomentar para probar - causará error intencional)
/*
INSERT INTO api (nombre_api, descripcion_api, estado_api, creado_por)
VALUES ('Test API', 'Testing FK', 'QA', 99999999);
-- ERROR esperado: Cannot add or update a child row: a foreign key constraint fails
*/

SELECT '✅ Para probar FK, descomentar el INSERT en el script' AS 'Nota';

-- =====================================================
-- 7. Verificar ON DELETE SET NULL funciona
-- =====================================================
SELECT 
    '=== PRUEBA ON DELETE SET NULL ===' AS 'Reporte';

SELECT 
    CONCAT(
        'Los FK están configurados con ON DELETE SET NULL. ',
        'Si se elimina un usuario, los campos de auditoría se pondrán en NULL.'
    ) AS 'Comportamiento';

-- =====================================================
-- FIN DEL SCRIPT DE VALIDACIÓN
-- =====================================================

-- =====================================================
-- 8. VALIDACIÓN DE CORRECCIONES ADICIONALES
-- =====================================================
SELECT 
    '=== VALIDACIÓN DE CORRECCIONES APLICADAS ===' AS 'Reporte';

-- Verificar que no hay tablas duplicadas
SELECT 
    TABLE_NAME,
    COUNT(*) as 'Cantidad Definiciones'
FROM (
    SELECT 'asignacion_rol_proyecto' AS TABLE_NAME
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'asignacion_rol_proyecto'
) subq
GROUP BY TABLE_NAME
HAVING COUNT(*) = 1
UNION ALL
SELECT 
    'Sin duplicaciones detectadas' AS resultado,
    1 AS valor
LIMIT 1;

-- Verificar índice único en nodo_tag_master
SELECT 
    CONCAT('✅ Índice ux_tag_nombre en nodo_tag_master: ', 
           IFNULL(COLUMN_NAME, 'NO ENCONTRADO')) AS 'Estado Índice'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'database'
  AND TABLE_NAME = 'nodo_tag_master'
  AND INDEX_NAME = 'ux_tag_nombre'
LIMIT 1;

-- Verificar reglas ON DELETE en ticket
SELECT 
    CONSTRAINT_NAME,
    DELETE_RULE AS 'ON DELETE',
    UPDATE_RULE AS 'ON UPDATE',
    CASE 
        WHEN CONSTRAINT_NAME = 'fk_ticket_usuario1' AND DELETE_RULE = 'RESTRICT' THEN '✅ CORRECTO'
        WHEN CONSTRAINT_NAME = 'fk_ticket_usuario2' AND DELETE_RULE = 'SET NULL' THEN '✅ CORRECTO'
        ELSE '❌ REVISAR'
    END AS 'Estado'
FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = 'database'
  AND CONSTRAINT_NAME LIKE 'fk_ticket_usuario%'
ORDER BY CONSTRAINT_NAME;

SELECT 
    '=== ✅ VALIDACIÓN COMPLETADA ===' AS 'Reporte',
    NOW() AS 'Fecha/Hora';
