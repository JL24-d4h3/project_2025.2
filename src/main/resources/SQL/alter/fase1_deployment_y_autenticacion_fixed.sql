-- =====================================================================
-- FASE 1.3: Deployment por Versión + Sistema de Autenticación
-- =====================================================================
-- Autor: Jesús León
-- Fecha: 11 de Noviembre, 2025
-- Descripción: Agregar 7 campos a version_api para deployment en Cloud Run
--              y sistema de autenticación con API Keys
-- Versión: CORREGIDA (sin IF NOT EXISTS en ALTER TABLE ADD COLUMN)
-- =====================================================================

USE `dev_portal_sql`;

-- =====================================================================
-- VERIFICAR COLUMNAS EXISTENTES ANTES DE AGREGAR
-- =====================================================================

-- ---------------------------------------------------------------------
-- Campo 1: URL de la imagen Docker de esta versión
-- ---------------------------------------------------------------------
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'version_api' AND COLUMN_NAME = 'docker_image_url';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `version_api` ADD COLUMN `docker_image_url` VARCHAR(500) NULL COMMENT ''URL de la imagen Docker en GCR para esta versión (ej: gcr.io/project/api:1.0.0)''',
    'SELECT ''✓ Campo docker_image_url ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- Campo 2: URL pública del servicio de esta versión en Cloud Run
-- ---------------------------------------------------------------------
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'version_api' AND COLUMN_NAME = 'cloud_run_url';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `version_api` ADD COLUMN `cloud_run_url` VARCHAR(500) NULL COMMENT ''URL del servicio en Cloud Run para esta versión (ej: https://user-api-v1-xyz.run.app)''',
    'SELECT ''✓ Campo cloud_run_url ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- Campo 3: ID del deployment en el microservicio
-- ---------------------------------------------------------------------
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'version_api' AND COLUMN_NAME = 'deployment_id';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `version_api` ADD COLUMN `deployment_id` BIGINT UNSIGNED NULL COMMENT ''ID del deployment en el microservicio de hosting''',
    'SELECT ''✓ Campo deployment_id ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- Campo 4: Fecha del último deployment
-- ---------------------------------------------------------------------
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'version_api' AND COLUMN_NAME = 'fecha_ultimo_deployment';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `version_api` ADD COLUMN `fecha_ultimo_deployment` DATETIME NULL COMMENT ''Timestamp del último despliegue exitoso de esta versión''',
    'SELECT ''✓ Campo fecha_ultimo_deployment ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- Campo 5: Estado del deployment
-- ---------------------------------------------------------------------
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'version_api' AND COLUMN_NAME = 'deployment_status';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `version_api` ADD COLUMN `deployment_status` ENUM(''PENDIENTE'', ''DEPLOYING'', ''ACTIVE'', ''ERROR'', ''INACTIVE'') NULL COMMENT ''Estado actual del deployment en Cloud Run''',
    'SELECT ''✓ Campo deployment_status ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- Campo 6: Estado de la versión (ciclo de vida)
-- ---------------------------------------------------------------------
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'version_api' AND COLUMN_NAME = 'estado_version';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `version_api` ADD COLUMN `estado_version` ENUM(''DRAFT'', ''PUBLISHED'', ''DEPRECATED'') NOT NULL DEFAULT ''DRAFT'' COMMENT ''Estado de publicación de la versión en el portal''',
    'SELECT ''✓ Campo estado_version ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- Campo 7: Requiere autenticación con API Key
-- ---------------------------------------------------------------------
SELECT COUNT(*) INTO @col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'version_api' AND COLUMN_NAME = 'requiere_autenticacion';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `version_api` ADD COLUMN `requiere_autenticacion` BOOLEAN NOT NULL DEFAULT TRUE COMMENT ''Si TRUE, los usuarios deben solicitar API Key para consumir. Si FALSE, es pública sin autenticación.''',
    'SELECT ''✓ Campo requiere_autenticacion ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =====================================================================
-- SECCIÓN 2: CREAR ÍNDICES DE OPTIMIZACIÓN (IDEMPOTENTE)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Índice 1: Optimizar consultas por estado de deployment
-- ---------------------------------------------------------------------
SELECT COUNT(*) INTO @idx_exists FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'version_api' AND INDEX_NAME = 'idx_version_deployment_status';

SET @sql = IF(@idx_exists = 0,
    'CREATE INDEX `idx_version_deployment_status` ON `version_api` (`deployment_status`)',
    'SELECT ''✓ Índice idx_version_deployment_status ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- Índice 2: Optimizar consultas por estado de versión
-- ---------------------------------------------------------------------
SELECT COUNT(*) INTO @idx_exists FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'version_api' AND INDEX_NAME = 'idx_version_estado';

SET @sql = IF(@idx_exists = 0,
    'CREATE INDEX `idx_version_estado` ON `version_api` (`estado_version`)',
    'SELECT ''✓ Índice idx_version_estado ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- Índice 3: Optimizar consultas de APIs públicas vs privadas
-- ---------------------------------------------------------------------
SELECT COUNT(*) INTO @idx_exists FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dev_portal_sql' AND TABLE_NAME = 'version_api' AND INDEX_NAME = 'idx_version_requiere_auth';

SET @sql = IF(@idx_exists = 0,
    'CREATE INDEX `idx_version_requiere_auth` ON `version_api` (`requiere_autenticacion`)',
    'SELECT ''✓ Índice idx_version_requiere_auth ya existe'' AS mensaje');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =====================================================================
-- SECCIÓN 3: VERIFICACIÓN POST-EJECUCIÓN
-- =====================================================================

-- Query 1: Contar cuántos campos nuevos se agregaron (debe retornar 7)
SELECT 
    COUNT(*) AS total_columnas_nuevas
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'dev_portal_sql' 
  AND TABLE_NAME = 'version_api'
  AND COLUMN_NAME IN (
      'docker_image_url', 
      'cloud_run_url', 
      'deployment_id', 
      'fecha_ultimo_deployment', 
      'deployment_status', 
      'estado_version', 
      'requiere_autenticacion'
  );
-- Resultado esperado: 7

-- Query 2: Listar todos los índices creados
SELECT 
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'dev_portal_sql'
  AND TABLE_NAME = 'version_api'
  AND INDEX_NAME LIKE 'idx_version_%'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;
-- Resultado esperado: 3 índices

-- Query 3: Ver estructura completa de la tabla
DESCRIBE `version_api`;
-- Verificar que los 7 campos nuevos aparecen al final

-- =====================================================================
-- SECCIÓN 4: TESTING (OPCIONAL)
-- =====================================================================

-- Test 1: Insertar una versión de prueba con valores default
-- (Solo si existe al menos una API y una documentación en la BD)
/*
INSERT INTO version_api (
    api_api_id, 
    numero_version, 
    descripcion, 
    documentacion_documentacion_id
) VALUES (
    1,  -- Asumir que existe API con ID 1
    '1.0.0-test',
    'Versión de prueba para validar campos nuevos',
    1   -- Asumir que existe documentación con ID 1
);

-- Test 2: Verificar valores default
SELECT 
    version_id,
    numero_version,
    estado_version,            -- Debe ser 'DRAFT' (default)
    requiere_autenticacion,    -- Debe ser TRUE (default)
    deployment_status,         -- Debe ser NULL
    docker_image_url,          -- Debe ser NULL
    cloud_run_url,             -- Debe ser NULL
    deployment_id,             -- Debe ser NULL
    fecha_ultimo_deployment    -- Debe ser NULL
FROM version_api 
WHERE numero_version = '1.0.0-test';

-- Test 3: Limpiar (eliminar la versión de prueba)
DELETE FROM version_api WHERE numero_version = '1.0.0-test';
*/

-- =====================================================================
-- FIN DEL SCRIPT
-- =====================================================================
-- Resumen:
-- ✅ 7 campos agregados a version_api
-- ✅ 3 índices creados para optimización
-- ✅ Queries de verificación incluidas
-- ✅ Script listo para FASE 1.4 (actualizar entidad Java)
-- =====================================================================
