-- =====================================================
-- Script de Validación de Correcciones del File System
-- =====================================================
-- Este script verifica que todas las correcciones críticas
-- estén correctamente aplicadas en la base de datos

USE `database`;

-- =====================================================
-- 1. Verificar FK de root_node_id en proyecto
-- =====================================================
SELECT 
    '=== VALIDACIÓN FK: proyecto.root_node_id ===' AS 'Reporte';

SELECT 
    CONSTRAINT_NAME AS 'Constraint',
    TABLE_NAME AS 'Tabla',
    COLUMN_NAME AS 'Columna',
    REFERENCED_TABLE_NAME AS 'Tabla Referenciada',
    REFERENCED_COLUMN_NAME AS 'Columna Referenciada',
    DELETE_RULE AS 'ON DELETE',
    UPDATE_RULE AS 'ON UPDATE',
    CASE 
        WHEN CONSTRAINT_NAME = 'fk_proyecto_root_node' THEN '✅ CORRECTO'
        ELSE '❌ INCORRECTO'
    END AS 'Estado'
FROM 
    INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc
INNER JOIN
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu
    ON rc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME
    AND rc.CONSTRAINT_SCHEMA = kcu.CONSTRAINT_SCHEMA
WHERE 
    rc.CONSTRAINT_SCHEMA = 'database'
    AND kcu.TABLE_NAME = 'proyecto'
    AND kcu.COLUMN_NAME = 'root_node_id';

-- =====================================================
-- 2. Verificar FK de root_node_id en repositorio
-- =====================================================
SELECT 
    '=== VALIDACIÓN FK: repositorio.root_node_id ===' AS 'Reporte';

SELECT 
    CONSTRAINT_NAME AS 'Constraint',
    TABLE_NAME AS 'Tabla',
    COLUMN_NAME AS 'Columna',
    REFERENCED_TABLE_NAME AS 'Tabla Referenciada',
    REFERENCED_COLUMN_NAME AS 'Columna Referenciada',
    DELETE_RULE AS 'ON DELETE',
    UPDATE_RULE AS 'ON UPDATE',
    CASE 
        WHEN CONSTRAINT_NAME = 'fk_repositorio_root_node' THEN '✅ CORRECTO'
        ELSE '❌ INCORRECTO'
    END AS 'Estado'
FROM 
    INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc
INNER JOIN
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu
    ON rc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME
    AND rc.CONSTRAINT_SCHEMA = kcu.CONSTRAINT_SCHEMA
WHERE 
    rc.CONSTRAINT_SCHEMA = 'database'
    AND kcu.TABLE_NAME = 'repositorio'
    AND kcu.COLUMN_NAME = 'root_node_id';

-- =====================================================
-- 3. Verificar índice en nodo.is_deleted
-- =====================================================
SELECT 
    '=== VALIDACIÓN ÍNDICE: nodo.is_deleted ===' AS 'Reporte';

SELECT 
    TABLE_NAME AS 'Tabla',
    INDEX_NAME AS 'Nombre Índice',
    COLUMN_NAME AS 'Columna',
    SEQ_IN_INDEX AS 'Posición',
    INDEX_TYPE AS 'Tipo',
    CASE 
        WHEN INDEX_NAME = 'idx_is_deleted' THEN '✅ CORRECTO'
        ELSE '⚠️ REVISAR'
    END AS 'Estado'
FROM 
    INFORMATION_SCHEMA.STATISTICS
WHERE 
    TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'nodo'
    AND INDEX_NAME = 'idx_is_deleted'
ORDER BY 
    SEQ_IN_INDEX;

-- =====================================================
-- 4. Verificar índice UNIQUE en nodo.path
-- =====================================================
SELECT 
    '=== VALIDACIÓN ÍNDICE ÚNICO: nodo.path ===' AS 'Reporte';

SELECT 
    TABLE_NAME AS 'Tabla',
    INDEX_NAME AS 'Nombre Índice',
    COLUMN_NAME AS 'Columna',
    SEQ_IN_INDEX AS 'Posición',
    NON_UNIQUE AS 'No Único (0=UNIQUE)',
    CASE 
        WHEN INDEX_NAME = 'ux_container_path' AND NON_UNIQUE = 0 THEN '✅ CORRECTO'
        ELSE '❌ INCORRECTO'
    END AS 'Estado'
FROM 
    INFORMATION_SCHEMA.STATISTICS
WHERE 
    TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'nodo'
    AND INDEX_NAME = 'ux_container_path'
ORDER BY 
    SEQ_IN_INDEX;

-- =====================================================
-- 5. Verificar CHECK constraint en permiso_nodo
-- =====================================================
SELECT 
    '=== VALIDACIÓN CHECK: permiso_nodo (usuario O equipo) ===' AS 'Reporte';

SELECT 
    CONSTRAINT_NAME AS 'Constraint',
    TABLE_NAME AS 'Tabla',
    CONSTRAINT_TYPE AS 'Tipo',
    CASE 
        WHEN CONSTRAINT_NAME = 'chk_permiso_nodo_target' THEN '✅ CORRECTO'
        ELSE '⚠️ REVISAR'
    END AS 'Estado'
FROM 
    INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE 
    CONSTRAINT_SCHEMA = 'database'
    AND TABLE_NAME = 'permiso_nodo'
    AND CONSTRAINT_TYPE = 'CHECK'
    AND CONSTRAINT_NAME LIKE '%target%';

-- =====================================================
-- 6. Verificar eliminación de campo redundante en enlace
-- =====================================================
SELECT 
    '=== VALIDACIÓN: enlace SIN campo repositorio_repositorio_id ===' AS 'Reporte';

SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✅ CAMPO ELIMINADO CORRECTAMENTE'
        ELSE '❌ CAMPO AÚN EXISTE'
    END AS 'Estado',
    COUNT(*) AS 'Cantidad Encontrada'
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'enlace'
    AND COLUMN_NAME = 'repositorio_repositorio_id';

-- =====================================================
-- 7. Verificar que enlace tiene campos polimórficos
-- =====================================================
SELECT 
    '=== VALIDACIÓN: enlace.contexto_type y contexto_id ===' AS 'Reporte';

SELECT 
    COLUMN_NAME AS 'Columna',
    DATA_TYPE AS 'Tipo',
    COLUMN_TYPE AS 'Definición Completa',
    CASE 
        WHEN COLUMN_NAME IN ('contexto_type', 'contexto_id') THEN '✅ CORRECTO'
        ELSE '⚠️ REVISAR'
    END AS 'Estado'
FROM 
    INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = 'database'
    AND TABLE_NAME = 'enlace'
    AND COLUMN_NAME IN ('contexto_type', 'contexto_id');

-- =====================================================
-- 8. RESUMEN GENERAL DE CORRECCIONES
-- =====================================================
SELECT 
    '=== RESUMEN GENERAL ===' AS 'Reporte';

SELECT 
    'Foreign Keys root_node_id' AS 'Corrección',
    (SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
     WHERE TABLE_SCHEMA = 'database'
       AND COLUMN_NAME = 'root_node_id'
       AND REFERENCED_TABLE_NAME = 'nodo') AS 'Encontrados',
    2 AS 'Esperados',
    CASE 
        WHEN (SELECT COUNT(*) 
              FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
              WHERE TABLE_SCHEMA = 'database'
                AND COLUMN_NAME = 'root_node_id'
                AND REFERENCED_TABLE_NAME = 'nodo') = 2 
        THEN '✅ CORRECTO'
        ELSE '❌ FALTANTE'
    END AS 'Estado'

UNION ALL

SELECT 
    'Índice nodo.is_deleted',
    (SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = 'database'
       AND TABLE_NAME = 'nodo'
       AND INDEX_NAME = 'idx_is_deleted'),
    1,
    CASE 
        WHEN (SELECT COUNT(*) 
              FROM INFORMATION_SCHEMA.STATISTICS
              WHERE TABLE_SCHEMA = 'database'
                AND TABLE_NAME = 'nodo'
                AND INDEX_NAME = 'idx_is_deleted') >= 1
        THEN '✅ CORRECTO'
        ELSE '❌ FALTANTE'
    END

UNION ALL

SELECT 
    'Índice UNIQUE nodo.path',
    (SELECT COUNT(DISTINCT INDEX_NAME) 
     FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = 'database'
       AND TABLE_NAME = 'nodo'
       AND INDEX_NAME = 'ux_container_path'
       AND NON_UNIQUE = 0),
    1,
    CASE 
        WHEN (SELECT COUNT(DISTINCT INDEX_NAME) 
              FROM INFORMATION_SCHEMA.STATISTICS
              WHERE TABLE_SCHEMA = 'database'
                AND TABLE_NAME = 'nodo'
                AND INDEX_NAME = 'ux_container_path'
                AND NON_UNIQUE = 0) = 1
        THEN '✅ CORRECTO'
        ELSE '❌ FALTANTE'
    END

UNION ALL

SELECT 
    'CHECK constraint permiso_nodo',
    (SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = 'database'
       AND TABLE_NAME = 'permiso_nodo'
       AND CONSTRAINT_TYPE = 'CHECK'
       AND CONSTRAINT_NAME LIKE '%target%'),
    1,
    CASE 
        WHEN (SELECT COUNT(*) 
              FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
              WHERE CONSTRAINT_SCHEMA = 'database'
                AND TABLE_NAME = 'permiso_nodo'
                AND CONSTRAINT_TYPE = 'CHECK'
                AND CONSTRAINT_NAME LIKE '%target%') >= 1
        THEN '✅ CORRECTO'
        ELSE '❌ FALTANTE'
    END

UNION ALL

SELECT 
    'Campo redundante eliminado (enlace)',
    (SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = 'database'
       AND TABLE_NAME = 'enlace'
       AND COLUMN_NAME = 'repositorio_repositorio_id'),
    0,
    CASE 
        WHEN (SELECT COUNT(*) 
              FROM INFORMATION_SCHEMA.COLUMNS
              WHERE TABLE_SCHEMA = 'database'
                AND TABLE_NAME = 'enlace'
                AND COLUMN_NAME = 'repositorio_repositorio_id') = 0
        THEN '✅ CORRECTO'
        ELSE '❌ AÚN EXISTE'
    END;

-- =====================================================
-- 9. Prueba de integridad: Insertar proyecto con root_node_id inválido
-- =====================================================
SELECT 
    '=== PRUEBA DE INTEGRIDAD ===' AS 'Reporte';

-- Esta query fallará si intentamos insertar un root_node_id inválido
-- (descomentar para probar - causará error intencional)
/*
INSERT INTO proyecto (nombre_proyecto, descripcion_proyecto, estado_proyecto, fecha_inicio_proyecto, root_node_id)
VALUES ('Test Proyecto', 'Testing FK root_node', 'PLANEADO', CURDATE(), 99999999);
-- ERROR esperado: Cannot add or update a child row: a foreign key constraint fails
*/

SELECT '✅ Para probar FK root_node_id, descomentar el INSERT en el script' AS 'Nota';

-- =====================================================
-- FIN DEL SCRIPT DE VALIDACIÓN
-- =====================================================

SELECT 
    '=== ✅ VALIDACIÓN DE FILE SYSTEM COMPLETADA ===' AS 'Reporte',
    NOW() AS 'Fecha/Hora';
