-- =====================================================================================================================
-- FIX: Agregar columnas faltantes a tabla nodo (con verificación previa)
-- =====================================================================================================================
-- Propósito: Agregar gcs_path, deleted_at, descripcion a la tabla nodo solo si no existen
-- Fecha: 3 de Noviembre, 2025
-- =====================================================================================================================

USE `dev_portal_sql`;

-- Agregar columna gcs_path solo si no existe
SET @col_exists_gcs = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                       WHERE TABLE_SCHEMA = 'dev_portal_sql' 
                       AND TABLE_NAME = 'nodo' 
                       AND COLUMN_NAME = 'gcs_path');

SET @sql_gcs = IF(@col_exists_gcs = 0,
    "ALTER TABLE `nodo` ADD COLUMN `gcs_path` VARCHAR(2048) NULL COMMENT 'Ruta completa en GCS'",
    "SELECT 'gcs_path ya existe' AS info");

PREPARE stmt_gcs FROM @sql_gcs;
EXECUTE stmt_gcs;
DEALLOCATE PREPARE stmt_gcs;

-- Agregar columna deleted_at solo si no existe
SET @col_exists_deleted = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                           WHERE TABLE_SCHEMA = 'dev_portal_sql' 
                           AND TABLE_NAME = 'nodo' 
                           AND COLUMN_NAME = 'deleted_at');

SET @sql_deleted = IF(@col_exists_deleted = 0,
    "ALTER TABLE `nodo` ADD COLUMN `deleted_at` DATETIME NULL COMMENT 'Fecha de eliminación'",
    "SELECT 'deleted_at ya existe' AS info");

PREPARE stmt_deleted FROM @sql_deleted;
EXECUTE stmt_deleted;
DEALLOCATE PREPARE stmt_deleted;

-- Agregar columna descripcion solo si no existe
SET @col_exists_desc = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                        WHERE TABLE_SCHEMA = 'dev_portal_sql' 
                        AND TABLE_NAME = 'nodo' 
                        AND COLUMN_NAME = 'descripcion');

SET @sql_desc = IF(@col_exists_desc = 0,
    "ALTER TABLE `nodo` ADD COLUMN `descripcion` TEXT NULL COMMENT 'Descripción del nodo'",
    "SELECT 'descripcion ya existe' AS info");

PREPARE stmt_desc FROM @sql_desc;
EXECUTE stmt_desc;
DEALLOCATE PREPARE stmt_desc;

-- Verificar estado final de todas las columnas
SELECT 
    COLUMN_NAME, 
    COLUMN_TYPE, 
    IS_NULLABLE, 
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' 
  AND TABLE_NAME = 'nodo'
  AND COLUMN_NAME IN ('gcs_path', 'deleted_at', 'descripcion')
ORDER BY ORDINAL_POSITION;

SELECT '✅ Proceso completado - Verificar resultado arriba' AS resultado;
