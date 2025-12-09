USE `dev_portal_sql`;

-- =====================================================================
-- Agregar columnas para foto de perfil (IDEMPOTENTE)
-- =====================================================================

-- Columna 1: Datos binarios de la foto de perfil
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'foto_perfil_data';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE usuario ADD COLUMN foto_perfil_data LONGBLOB NULL COMMENT ''Datos binarios de la foto de perfil subida por el usuario (JPEG/PNG)'' AFTER foto_perfil',
    'SELECT ''✓ Campo foto_perfil_data ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Columna 2: Tipo MIME de la imagen
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'foto_perfil_mime_type';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE usuario ADD COLUMN foto_perfil_mime_type VARCHAR(50) NULL COMMENT ''Tipo MIME de la foto (image/jpeg, image/png, etc.)'' AFTER foto_perfil_data',
    'SELECT ''✓ Campo foto_perfil_mime_type ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Índice: Optimizar consultas por tipo de foto
SELECT COUNT(*) INTO @idx_exists FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'usuario' AND INDEX_NAME = 'idx_foto_perfil_type';

SET @sql = IF(@idx_exists = 0,
    'CREATE INDEX idx_foto_perfil_type ON usuario(foto_perfil_mime_type)',
    'SELECT ''✓ Índice idx_foto_perfil_type ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Columna 3: Tamaño en bytes de la foto
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'foto_perfil_size_bytes';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE usuario ADD COLUMN foto_perfil_size_bytes BIGINT UNSIGNED NULL COMMENT ''Tamaño en bytes de la foto de perfil (foto_perfil_data)'' AFTER foto_perfil_mime_type',
    'SELECT ''✓ Campo foto_perfil_size_bytes ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =====================================================================
-- Verificación: Mostrar estructura de las columnas agregadas
-- =====================================================================
SELECT '✅ Script completado. Columnas agregadas:' AS estado;

SELECT 
    COLUMN_NAME AS Campo,
    COLUMN_TYPE AS Tipo,
    IS_NULLABLE AS 'NULL',
    COLUMN_COMMENT AS Comentario
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' 
  AND TABLE_NAME = 'usuario'
  AND COLUMN_NAME IN ('foto_perfil_data', 'foto_perfil_mime_type', 'foto_perfil_size_bytes')
ORDER BY ORDINAL_POSITION;