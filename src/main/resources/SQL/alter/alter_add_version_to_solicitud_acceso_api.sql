-- ============================================================================
-- Script: alter_add_version_to_solicitud_acceso_api.sql
-- Fecha: 2025-11-10
-- Autor: GitHub Copilot
-- Descripción: Agrega campo version_id a solicitud_acceso_api para soportar
--              solicitudes a nivel de versión específica de API
-- 
-- Modificaciones:
--   1. Agregar columna version_id (BIGINT UNSIGNED NULL)
--   2. Agregar índice fk_solicitud_acceso_version_idx
--   3. Agregar FK fk_solicitud_acceso_version -> version_api.version_id
--
-- Precondiciones:
--   - Tabla version_api debe existir
--   - Usuario debe tener permisos ALTER TABLE
--   - Recomendado: Backup de dev_portal_sql antes de ejecutar
--
-- Rollback:
--   ALTER TABLE solicitud_acceso_api DROP FOREIGN KEY fk_solicitud_acceso_version;
--   ALTER TABLE solicitud_acceso_api DROP COLUMN version_id;
-- ============================================================================

USE `dev_portal_sql`;

-- ============================================================================
-- PASO 1: Verificar si la columna ya existe
-- ============================================================================
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' 
  AND TABLE_NAME = 'solicitud_acceso_api' 
  AND COLUMN_NAME = 'version_id';

SELECT CONCAT('✓ Estado: Campo version_id ', 
              IF(@col_exists > 0, 'YA EXISTE', 'NO EXISTE (se agregará)')) AS status;

-- ============================================================================
-- PASO 2: Agregar columna version_id con FK si no existe
-- ============================================================================
SET @sql = IF(@col_exists = 0,
    "ALTER TABLE `solicitud_acceso_api` 
        ADD COLUMN `version_id` BIGINT UNSIGNED NULL 
            COMMENT 'FK a version_api: versión específica solicitada' 
            AFTER `api_api_id`,
        ADD INDEX `fk_solicitud_acceso_version_idx` (`version_id` ASC),
        ADD CONSTRAINT `fk_solicitud_acceso_version`
            FOREIGN KEY (`version_id`)
            REFERENCES `version_api` (`version_id`)
            ON DELETE CASCADE
            ON UPDATE CASCADE;",
    'SELECT "✓ Campo version_id ya existe. No se requiere acción." AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================================
-- PASO 3: Verificar resultado de la operación
-- ============================================================================
SELECT 
    COLUMN_NAME AS 'Campo', 
    COLUMN_TYPE AS 'Tipo', 
    IS_NULLABLE AS 'Nullable',
    COLUMN_KEY AS 'Key',
    COLUMN_DEFAULT AS 'Default',
    COLUMN_COMMENT AS 'Comentario'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' 
  AND TABLE_NAME = 'solicitud_acceso_api'
  AND COLUMN_NAME = 'version_id';

-- ============================================================================
-- PASO 4: Verificar Foreign Key creada
-- ============================================================================
SELECT 
    kcu.CONSTRAINT_NAME AS 'FK Name',
    kcu.COLUMN_NAME AS 'Columna',
    kcu.REFERENCED_TABLE_NAME AS 'Tabla Referenciada',
    kcu.REFERENCED_COLUMN_NAME AS 'Columna Referenciada',
    rc.DELETE_RULE AS 'On Delete',
    rc.UPDATE_RULE AS 'On Update'
FROM information_schema.KEY_COLUMN_USAGE kcu
LEFT JOIN information_schema.REFERENTIAL_CONSTRAINTS rc 
    ON rc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME
    AND rc.CONSTRAINT_SCHEMA = kcu.CONSTRAINT_SCHEMA
WHERE kcu.TABLE_SCHEMA = 'dev_portal_sql'
  AND kcu.TABLE_NAME = 'solicitud_acceso_api'
  AND kcu.CONSTRAINT_NAME = 'fk_solicitud_acceso_version';

-- ============================================================================
-- PASO 5: Mostrar estructura completa de la tabla
-- ============================================================================
SELECT '============================================' AS '';
SELECT '  ESTRUCTURA FINAL: solicitud_acceso_api  ' AS '';
SELECT '============================================' AS '';

SELECT 
    ORDINAL_POSITION AS '#',
    COLUMN_NAME AS 'Campo', 
    COLUMN_TYPE AS 'Tipo',
    IS_NULLABLE AS 'NULL',
    COLUMN_KEY AS 'Key',
    COLUMN_DEFAULT AS 'Default'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' 
  AND TABLE_NAME = 'solicitud_acceso_api'
ORDER BY ORDINAL_POSITION;

-- ============================================================================
-- PASO 6: Mostrar todas las Foreign Keys de la tabla
-- ============================================================================
SELECT '================================================' AS '';
SELECT '  FOREIGN KEYS: solicitud_acceso_api           ' AS '';
SELECT '================================================' AS '';

SELECT 
    kcu.CONSTRAINT_NAME AS 'FK Constraint',
    kcu.COLUMN_NAME AS 'Columna',
    kcu.REFERENCED_TABLE_NAME AS 'Ref. Tabla',
    kcu.REFERENCED_COLUMN_NAME AS 'Ref. Columna',
    rc.DELETE_RULE AS 'On Delete',
    rc.UPDATE_RULE AS 'On Update'
FROM information_schema.KEY_COLUMN_USAGE kcu
LEFT JOIN information_schema.REFERENTIAL_CONSTRAINTS rc 
    ON rc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME
    AND rc.CONSTRAINT_SCHEMA = kcu.CONSTRAINT_SCHEMA
WHERE kcu.TABLE_SCHEMA = 'dev_portal_sql'
  AND kcu.TABLE_NAME = 'solicitud_acceso_api'
  AND kcu.REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY kcu.CONSTRAINT_NAME;

-- ============================================================================
-- PASO 7: Estadísticas de datos existentes
-- ============================================================================
SELECT '============================================' AS '';
SELECT '  ESTADÍSTICAS DE DATOS                    ' AS '';
SELECT '============================================' AS '';

SELECT 
    COUNT(*) AS 'Total de Solicitudes',
    SUM(CASE WHEN version_id IS NULL THEN 1 ELSE 0 END) AS 'Sin version_id',
    SUM(CASE WHEN version_id IS NOT NULL THEN 1 ELSE 0 END) AS 'Con version_id'
FROM solicitud_acceso_api;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
SELECT '============================================' AS '';
SELECT '  ✓ MIGRACIÓN COMPLETADA EXITOSAMENTE      ' AS '';
SELECT '============================================' AS '';
SELECT CONCAT('Fecha: ', NOW()) AS 'Timestamp';

-- Nota: Si hay solicitudes existentes con version_id NULL, considera ejecutar:
-- UPDATE solicitud_acceso_api sa
-- INNER JOIN version_api va ON va.api_api_id = sa.api_api_id
-- SET sa.version_id = va.version_id
-- WHERE sa.version_id IS NULL
--   AND va.estado_version = 'ACTIVO'
-- LIMIT 1000;
