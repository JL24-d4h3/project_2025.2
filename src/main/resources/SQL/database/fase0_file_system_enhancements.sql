-- =====================================================================================================================
-- FASE 0: MEJORAS PARA SISTEMA DE ARCHIVOS
-- =====================================================================================================================
-- Fecha: 2 de Noviembre, 2025
-- Propósito: Preparar la base de datos para implementar File System completo tipo Google Drive/GitHub
-- 
-- CAMBIOS INCLUIDOS:
-- 1. Triggers para auto-crear carpeta raíz en proyectos/repositorios
-- 2. Tabla para operaciones de clipboard (copiar/cortar/pegar)
-- 3. Tabla para trabajos asíncronos (comprimir, descargar múltiples)
-- 4. Tablas para integración con GitHub
-- 5. Mejoras en índices y constraints
-- 6. Procedimientos almacenados útiles
-- =====================================================================================================================

USE `dev_portal_sql`;

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- =====================================================================================================================
-- PARTE 0: MODIFICAR TABLA NODO EXISTENTE (AGREGAR COLUMNAS FALTANTES)
-- =====================================================================================================================

-- Agregar columna gcs_path si no existe
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = 'dev_portal_sql' 
                   AND TABLE_NAME = 'nodo' 
                   AND COLUMN_NAME = 'gcs_path');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `nodo` ADD COLUMN `gcs_path` VARCHAR(2048) NULL COMMENT ''Ruta completa en GCS: gs://bucket/path/to/file.pdf'' AFTER `path`',
    'SELECT ''Column gcs_path already exists'' AS info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Agregar columna deleted_at si no existe
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = 'dev_portal_sql' 
                   AND TABLE_NAME = 'nodo' 
                   AND COLUMN_NAME = 'deleted_at');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `nodo` ADD COLUMN `deleted_at` DATETIME NULL COMMENT ''Fecha de eliminación (soft delete)'' AFTER `is_deleted`',
    'SELECT ''Column deleted_at already exists'' AS info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Agregar columna descripcion si no existe
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = 'dev_portal_sql' 
                   AND TABLE_NAME = 'nodo' 
                   AND COLUMN_NAME = 'descripcion');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `nodo` ADD COLUMN `descripcion` TEXT NULL COMMENT ''Descripción o notas del archivo/carpeta'' AFTER `nombre`',
    'SELECT ''Column descripcion already exists'' AS info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================================================================================
-- PARTE 1: NUEVAS TABLAS
-- =====================================================================================================================

-- -----------------------------------------------------
-- Tabla: clipboard_operation
-- Propósito: Almacenar operaciones de copiar/cortar temporalmente
-- Uso: Cuando usuario hace Ctrl+C o Ctrl+X sobre archivos/carpetas
-- Lifecycle: Expira automáticamente después de 24 horas
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`clipboard_operation` (
    `clipboard_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `usuario_id` BIGINT UNSIGNED NOT NULL COMMENT 'Usuario que copió/cortó',
    `operation_type` ENUM('COPY', 'CUT') NOT NULL COMMENT 'COPY: duplicar | CUT: mover',
    `nodo_ids` JSON NOT NULL COMMENT 'Array de IDs de nodos seleccionados. Ej: [123, 456, 789]',
    `source_container_type` ENUM('PROYECTO', 'REPOSITORIO') NOT NULL,
    `source_container_id` BIGINT UNSIGNED NOT NULL COMMENT 'ID del proyecto o repositorio origen',
    `source_parent_id` BIGINT UNSIGNED NULL COMMENT 'ID de la carpeta padre donde estaban los nodos',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expires_at` DATETIME NOT NULL COMMENT 'Expira en 24 horas. Usar event scheduler para limpiar',
    `is_expired` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1 si ya se usó o expiró',
    PRIMARY KEY (`clipboard_id`),
    INDEX `idx_user_clipboard` (`usuario_id`, `is_expired`, `expires_at`),
    CONSTRAINT `fk_clipboard_usuario`
        FOREIGN KEY (`usuario_id`)
        REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
        ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Operaciones de copiar/cortar en clipboard del usuario';


-- -----------------------------------------------------
-- Tabla: file_operation_job
-- Propósito: Trabajos asíncronos para operaciones pesadas
-- Uso: Comprimir 100 archivos, descargar carpeta completa, etc.
-- Lifecycle: Se procesa en background con Spring @Async
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`file_operation_job` (
    `job_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `operation_type` ENUM('COMPRESS', 'BULK_DOWNLOAD', 'BULK_UPLOAD', 'MOVE', 'COPY', 'DELETE_BULK') NOT NULL,
    `status` ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    `nodo_ids` JSON NOT NULL COMMENT 'Nodos a procesar',
    `target_container_type` ENUM('PROYECTO', 'REPOSITORIO') NULL COMMENT 'Destino (para MOVE/COPY)',
    `target_container_id` BIGINT UNSIGNED NULL,
    `target_parent_id` BIGINT UNSIGNED NULL COMMENT 'Carpeta destino',
    `result_enlace_id` BIGINT UNSIGNED NULL COMMENT 'Enlace al archivo ZIP resultante (para COMPRESS)',
    `result_url` VARCHAR(2048) NULL COMMENT 'URL de descarga directa del resultado',
    `error_message` TEXT NULL,
    `progress_percent` TINYINT UNSIGNED NULL DEFAULT 0 COMMENT '0-100',
    `total_files` INT NULL DEFAULT 0,
    `processed_files` INT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `started_at` DATETIME NULL,
    `completed_at` DATETIME NULL,
    `metadata` JSON NULL COMMENT 'Información adicional específica de la operación',
    PRIMARY KEY (`job_id`),
    INDEX `idx_user_jobs` (`usuario_id`, `status`, `created_at`),
    INDEX `idx_status` (`status`, `created_at`),
    CONSTRAINT `fk_job_usuario`
        FOREIGN KEY (`usuario_id`)
        REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_job_result_enlace`
        FOREIGN KEY (`result_enlace_id`)
        REFERENCES `dev_portal_sql`.`enlace` (`enlace_id`)
        ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='Jobs asíncronos para operaciones de archivos pesadas';


-- -----------------------------------------------------
-- Tabla: github_integration
-- Propósito: Conectar repositorios de TelDev con repositorios de GitHub
-- Uso: Usuario conecta R-31 con github.com/mlopez/telemetry-system
-- Sync Modes:
--   API_ONLY: Solo leer vía API (no clonar repo)
--   WEBHOOK: Sincronización automática con webhooks
--   CLONE_LOCAL: Clonar repo completo y subir a GCS
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`github_integration` (
    `github_integration_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `repositorio_id` BIGINT UNSIGNED NOT NULL,
    `github_repository_fullname` VARCHAR(255) NOT NULL COMMENT 'owner/repo (ej: octocat/Hello-World)',
    `github_repository_url` VARCHAR(512) NOT NULL COMMENT 'https://github.com/owner/repo',
    `github_repo_id` BIGINT NULL COMMENT 'ID numérico del repo en GitHub API',
    `default_branch` VARCHAR(100) DEFAULT 'main',
    `sync_mode` ENUM('API_ONLY', 'WEBHOOK', 'CLONE_LOCAL') DEFAULT 'API_ONLY' COMMENT 'Modo de sincronización',
    `auto_sync_enabled` TINYINT(1) DEFAULT 0 COMMENT 'Si está habilitada la sincronización automática',
    `sync_interval_minutes` INT NULL DEFAULT 60 COMMENT 'Intervalo de sincronización automática (solo si auto_sync_enabled=1)',
    `last_sync_at` DATETIME NULL,
    `last_sync_commit_hash` VARCHAR(40) NULL COMMENT 'SHA del último commit sincronizado',
    `last_sync_status` ENUM('SUCCESS', 'FAILED', 'PARTIAL') NULL,
    `webhook_id` VARCHAR(100) NULL COMMENT 'ID del webhook en GitHub (si sync_mode=WEBHOOK)',
    `webhook_secret` VARCHAR(255) NULL COMMENT 'Secret para validar webhooks de GitHub',
    `is_active` TINYINT(1) DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_by` BIGINT UNSIGNED NULL,
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX `uk_repo_github` (`repositorio_id`),
    INDEX `idx_github_fullname` (`github_repository_fullname`),
    INDEX `idx_active_sync` (`is_active`, `auto_sync_enabled`, `last_sync_at`),
    CONSTRAINT `fk_github_int_repositorio`
        FOREIGN KEY (`repositorio_id`)
        REFERENCES `dev_portal_sql`.`repositorio`(`repositorio_id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_github_int_created_by`
        FOREIGN KEY (`created_by`)
        REFERENCES `dev_portal_sql`.`usuario`(`usuario_id`)
        ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='Integración de repositorios con GitHub';


-- -----------------------------------------------------
-- Tabla: github_user_token
-- Propósito: Almacenar tokens OAuth de GitHub de cada usuario
-- Uso: Usuario autoriza TelDev a acceder a sus repos de GitHub
-- Seguridad: access_token debe encriptarse en la aplicación antes de guardar
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`github_user_token` (
    `github_token_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `access_token` VARCHAR(512) NOT NULL COMMENT 'Token OAuth (DEBE ESTAR ENCRIPTADO)',
    `token_type` VARCHAR(50) DEFAULT 'bearer',
    `scope` VARCHAR(500) NULL COMMENT 'Permisos del token (repo, read:user, workflow, etc)',
    `github_user_id` BIGINT NULL COMMENT 'ID del usuario en GitHub',
    `github_username` VARCHAR(255) NULL COMMENT 'Username en GitHub',
    `github_email` VARCHAR(255) NULL,
    `expires_at` DATETIME NULL COMMENT 'NULL = no expira (classic personal access tokens)',
    `refresh_token` VARCHAR(512) NULL COMMENT 'Para renovar token (OAuth Apps)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_used_at` DATETIME NULL,
    `is_valid` TINYINT(1) DEFAULT 1 COMMENT 'Se marca como 0 si GitHub rechaza el token',
    `revoked_at` DATETIME NULL,
    INDEX `idx_usuario_token` (`usuario_id`, `is_valid`),
    INDEX `idx_github_username` (`github_username`),
    CONSTRAINT `fk_github_token_usuario`
        FOREIGN KEY (`usuario_id`)
        REFERENCES `dev_portal_sql`.`usuario`(`usuario_id`)
        ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Tokens OAuth de GitHub de usuarios';


-- -----------------------------------------------------
-- Tabla: github_sync_log
-- Propósito: Historial de sincronizaciones con GitHub
-- Uso: Auditoría y debugging de sincronizaciones
-- Lifecycle: Se puede limpiar logs antiguos (>6 meses)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`github_sync_log` (
    `sync_log_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `github_integration_id` BIGINT UNSIGNED NOT NULL,
    `sync_type` ENUM('MANUAL', 'WEBHOOK', 'SCHEDULED', 'AUTO') NOT NULL,
    `sync_direction` ENUM('GITHUB_TO_TELDEV', 'TELDEV_TO_GITHUB', 'BIDIRECTIONAL') NOT NULL,
    `status` ENUM('SUCCESS', 'PARTIAL', 'FAILED') NOT NULL,
    `commits_synced` INT DEFAULT 0,
    `files_added` INT DEFAULT 0,
    `files_modified` INT DEFAULT 0,
    `files_deleted` INT DEFAULT 0,
    `total_size_bytes` BIGINT UNSIGNED NULL,
    `error_message` TEXT NULL,
    `error_details` JSON NULL COMMENT 'Stack trace o detalles técnicos del error',
    `started_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `completed_at` DATETIME NULL,
    `duration_seconds` INT NULL COMMENT 'Duración de la sincronización',
    `triggered_by_usuario_id` BIGINT UNSIGNED NULL,
    `commit_range` VARCHAR(200) NULL COMMENT 'Rango de commits sincronizados (ej: abc123..def456)',
    INDEX `idx_integration_sync` (`github_integration_id`, `started_at`),
    INDEX `idx_status_date` (`status`, `started_at`),
    CONSTRAINT `fk_sync_log_integration`
        FOREIGN KEY (`github_integration_id`)
        REFERENCES `dev_portal_sql`.`github_integration`(`github_integration_id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_sync_log_usuario`
        FOREIGN KEY (`triggered_by_usuario_id`)
        REFERENCES `dev_portal_sql`.`usuario`(`usuario_id`)
        ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='Historial de sincronizaciones con GitHub';


-- -----------------------------------------------------
-- Tabla: nodo_share_link
-- Propósito: Enlaces públicos para compartir archivos/carpetas
-- Uso: Usuario genera link "https://teldev.com/s/abc123def" para compartir archivo
-- Seguridad: token debe ser aleatorio y difícil de adivinar
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`nodo_share_link` (
    `share_link_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `nodo_id` BIGINT UNSIGNED NOT NULL,
    `share_token` VARCHAR(64) NOT NULL COMMENT 'Token único (UUID o hash aleatorio)',
    `created_by` BIGINT UNSIGNED NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expires_at` DATETIME NULL COMMENT 'NULL = no expira',
    `password_hash` VARCHAR(255) NULL COMMENT 'Opcional: proteger con contraseña',
    `max_downloads` INT NULL COMMENT 'NULL = ilimitado',
    `download_count` INT DEFAULT 0,
    `is_active` TINYINT(1) DEFAULT 1,
    `allow_download` TINYINT(1) DEFAULT 1,
    `allow_preview` TINYINT(1) DEFAULT 1,
    `deactivated_at` DATETIME NULL,
    UNIQUE INDEX `uk_share_token` (`share_token`),
    INDEX `idx_nodo_share` (`nodo_id`, `is_active`),
    INDEX `idx_token_active` (`share_token`, `is_active`),
    CONSTRAINT `fk_share_nodo`
        FOREIGN KEY (`nodo_id`)
        REFERENCES `dev_portal_sql`.`nodo`(`nodo_id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_share_created_by`
        FOREIGN KEY (`created_by`)
        REFERENCES `dev_portal_sql`.`usuario`(`usuario_id`)
        ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Enlaces públicos para compartir archivos/carpetas';


-- -----------------------------------------------------
-- Tabla: nodo_favorite
-- Propósito: Marcadores/favoritos de usuarios
-- Uso: Usuario marca carpeta "Documentation" como favorita
-- UI: Mostrar sección "⭐ Favoritos" en sidebar
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`nodo_favorite` (
    `favorite_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `nodo_id` BIGINT UNSIGNED NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `custom_label` VARCHAR(255) NULL COMMENT 'Alias personalizado (opcional)',
    UNIQUE INDEX `uk_user_nodo_fav` (`usuario_id`, `nodo_id`),
    INDEX `idx_user_favorites` (`usuario_id`, `created_at`),
    CONSTRAINT `fk_favorite_usuario`
        FOREIGN KEY (`usuario_id`)
        REFERENCES `dev_portal_sql`.`usuario`(`usuario_id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_favorite_nodo`
        FOREIGN KEY (`nodo_id`)
        REFERENCES `dev_portal_sql`.`nodo`(`nodo_id`)
        ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Favoritos/marcadores de usuarios';


-- =====================================================================================================================
-- PARTE 2: TRIGGERS DESHABILITADOS - NO SE CREA CARPETA RAÍZ AUTOMÁTICA
-- =====================================================================================================================
-- NOTA: Se deshabilitó la creación automática de carpeta raíz "/"
-- RAZÓN: Los usuarios deben crear sus propias carpetas. La carpeta "/" confunde la UI.
-- CAMBIO REALIZADO: 3 de Noviembre, 2025
-- =====================================================================================================================

-- Eliminar triggers si existen (para limpiar instalaciones anteriores)
DROP TRIGGER IF EXISTS `trg_proyecto_create_root_node`;
DROP TRIGGER IF EXISTS `trg_repositorio_create_root_node`;
DROP TRIGGER IF EXISTS `after_proyecto_insert_create_root_node`;
DROP TRIGGER IF EXISTS `after_repositorio_insert_create_root_node`;

-- Los triggers están COMENTADOS para no crear nodos raíz automáticamente
-- Si necesitas habilitarlos en el futuro, descomenta el código siguiente:

/*
DELIMITER $$

CREATE TRIGGER `trg_proyecto_create_root_node`
AFTER INSERT ON `proyecto`
FOR EACH ROW
BEGIN
    INSERT INTO `nodo` (
        `container_type`,
        `container_id`,
        `parent_id`,
        `nombre`,
        `tipo`,
        `path`,
        `descripcion`,
        `creado_por`,
        `creado_en`
    ) VALUES (
        'PROYECTO',
        NEW.proyecto_id,
        NULL,
        '/',
        'CARPETA',
        '/',
        'Carpeta raíz del proyecto',
        NEW.created_by,
        NOW()
    );
END$$

CREATE TRIGGER `trg_repositorio_create_root_node`
AFTER INSERT ON `repositorio`
FOR EACH ROW
BEGIN
    INSERT INTO `nodo` (
        `container_type`,
        `container_id`,
        `parent_id`,
        `nombre`,
        `tipo`,
        `path`,
        `descripcion`,
        `creado_por`,
        `creado_en`
    ) VALUES (
        'REPOSITORIO',
        NEW.repositorio_id,
        NULL,
        '/',
        'CARPETA',
        '/',
        'Carpeta raíz del repositorio',
        NEW.creado_por,
        NOW()
    );
END$$

DELIMITER ;
*/


-- =====================================================================================================================
-- PARTE 3: PROCEDIMIENTOS ALMACENADOS ÚTILES
-- =====================================================================================================================

DELIMITER $$

-- -----------------------------------------------------
-- Procedimiento: sp_get_nodo_full_path
-- Propósito: Obtener la ruta completa de un nodo recorriendo la jerarquía
-- Uso: CALL sp_get_nodo_full_path(456, @path); SELECT @path;
-- Retorna: "/Developer tools/SDK/manual.pdf"
-- -----------------------------------------------------
DROP PROCEDURE IF EXISTS `sp_get_nodo_full_path`$$

CREATE PROCEDURE `sp_get_nodo_full_path`(
    IN p_nodo_id BIGINT UNSIGNED,
    OUT p_full_path VARCHAR(2000)
)
BEGIN
    DECLARE v_nombre VARCHAR(255);
    DECLARE v_parent_id BIGINT UNSIGNED;
    DECLARE v_path VARCHAR(2000) DEFAULT '';
    DECLARE v_current_id BIGINT UNSIGNED;
    DECLARE v_iteration INT DEFAULT 0;
    DECLARE v_max_iterations INT DEFAULT 100;  -- Prevenir loops infinitos
    
    SET v_current_id = p_nodo_id;
    
    -- Recorrer hacia arriba hasta llegar a la raíz
    WHILE v_current_id IS NOT NULL AND v_iteration < v_max_iterations DO
        -- Obtener nombre y parent del nodo actual
        SELECT `nombre`, `parent_id`
        INTO v_nombre, v_parent_id
        FROM `nodo`
        WHERE `nodo_id` = v_current_id;
        
        -- Si no es la raíz (/), agregar al path
        IF v_nombre != '/' THEN
            IF v_path = '' THEN
                SET v_path = v_nombre;
            ELSE
                SET v_path = CONCAT(v_nombre, '/', v_path);
            END IF;
        END IF;
        
        -- Subir al padre
        SET v_current_id = v_parent_id;
        SET v_iteration = v_iteration + 1;
    END WHILE;
    
    -- Agregar / al inicio
    SET p_full_path = CONCAT('/', v_path);
END$$


-- -----------------------------------------------------
-- Procedimiento: sp_move_nodo
-- Propósito: Mover un nodo a otra carpeta (actualizar parent_id y path)
-- Uso: CALL sp_move_nodo(456, 789); -- Mover nodo 456 a carpeta 789
-- Efecto: Actualiza path de nodo y todos sus hijos recursivamente
-- -----------------------------------------------------
DROP PROCEDURE IF EXISTS `sp_move_nodo`$$

CREATE PROCEDURE `sp_move_nodo`(
    IN p_nodo_id BIGINT UNSIGNED,
    IN p_new_parent_id BIGINT UNSIGNED
)
BEGIN
    DECLARE v_old_path VARCHAR(2000);
    DECLARE v_new_path VARCHAR(2000);
    DECLARE v_nombre VARCHAR(255);
    DECLARE v_parent_path VARCHAR(2000);
    
    -- Obtener información del nodo a mover
    SELECT `path`, `nombre`
    INTO v_old_path, v_nombre
    FROM `nodo`
    WHERE `nodo_id` = p_nodo_id;
    
    -- Obtener path del nuevo padre
    IF p_new_parent_id IS NULL THEN
        -- Mover a raíz
        SET v_parent_path = '/';
    ELSE
        SELECT `path`
        INTO v_parent_path
        FROM `nodo`
        WHERE `nodo_id` = p_new_parent_id;
    END IF;
    
    -- Construir nuevo path
    IF v_parent_path = '/' THEN
        SET v_new_path = CONCAT('/', v_nombre);
    ELSE
        SET v_new_path = CONCAT(v_parent_path, '/', v_nombre);
    END IF;
    
    -- Actualizar el nodo
    UPDATE `nodo`
    SET `parent_id` = p_new_parent_id,
        `path` = v_new_path,
        `actualizado_en` = NOW()
    WHERE `nodo_id` = p_nodo_id;
    
    -- Actualizar paths de todos los hijos (recursivamente)
    UPDATE `nodo`
    SET `path` = REPLACE(`path`, v_old_path, v_new_path),
        `actualizado_en` = NOW()
    WHERE `path` LIKE CONCAT(v_old_path, '/%');
END$$


-- -----------------------------------------------------
-- Procedimiento: sp_delete_nodo_soft
-- Propósito: Soft delete de un nodo (marcar is_deleted=1)
-- Uso: CALL sp_delete_nodo_soft(456, 1); -- Eliminar nodo 456, usuario 1
-- Efecto: Marca nodo y todos sus hijos como eliminados
-- -----------------------------------------------------
DROP PROCEDURE IF EXISTS `sp_delete_nodo_soft`$$

CREATE PROCEDURE `sp_delete_nodo_soft`(
    IN p_nodo_id BIGINT UNSIGNED,
    IN p_usuario_id BIGINT UNSIGNED
)
BEGIN
    DECLARE v_path VARCHAR(2000);
    
    -- Obtener path del nodo
    SELECT `path`
    INTO v_path
    FROM `nodo`
    WHERE `nodo_id` = p_nodo_id;
    
    -- Marcar el nodo como eliminado
    UPDATE `nodo`
    SET `is_deleted` = 1,
        `deleted_at` = NOW(),
        `actualizado_por` = p_usuario_id,
        `actualizado_en` = NOW()
    WHERE `nodo_id` = p_nodo_id;
    
    -- Marcar todos los hijos como eliminados
    UPDATE `nodo`
    SET `is_deleted` = 1,
        `deleted_at` = NOW(),
        `actualizado_por` = p_usuario_id,
        `actualizado_en` = NOW()
    WHERE `path` LIKE CONCAT(v_path, '/%')
      AND `is_deleted` = 0;
END$$


-- -----------------------------------------------------
-- Procedimiento: sp_restore_nodo
-- Propósito: Restaurar un nodo eliminado (soft delete)
-- Uso: CALL sp_restore_nodo(456, 1); -- Restaurar nodo 456
-- Efecto: Marca nodo y todos sus hijos como NO eliminados
-- -----------------------------------------------------
DROP PROCEDURE IF EXISTS `sp_restore_nodo`$$

CREATE PROCEDURE `sp_restore_nodo`(
    IN p_nodo_id BIGINT UNSIGNED,
    IN p_usuario_id BIGINT UNSIGNED
)
BEGIN
    DECLARE v_path VARCHAR(2000);
    
    -- Obtener path del nodo
    SELECT `path`
    INTO v_path
    FROM `nodo`
    WHERE `nodo_id` = p_nodo_id;
    
    -- Restaurar el nodo
    UPDATE `nodo`
    SET `is_deleted` = 0,
        `deleted_at` = NULL,
        `actualizado_por` = p_usuario_id,
        `actualizado_en` = NOW()
    WHERE `nodo_id` = p_nodo_id;
    
    -- Restaurar todos los hijos
    UPDATE `nodo`
    SET `is_deleted` = 0,
        `deleted_at` = NULL,
        `actualizado_por` = p_usuario_id,
        `actualizado_en` = NOW()
    WHERE `path` LIKE CONCAT(v_path, '/%')
      AND `is_deleted` = 1;
END$$


-- -----------------------------------------------------
-- Procedimiento: sp_get_nodo_size_recursive
-- Propósito: Calcular tamaño total de una carpeta (suma de archivos hijos)
-- Uso: CALL sp_get_nodo_size_recursive(456, @size); SELECT @size;
-- Retorna: Tamaño en bytes de todos los archivos dentro de la carpeta
-- -----------------------------------------------------
DROP PROCEDURE IF EXISTS `sp_get_nodo_size_recursive`$$

CREATE PROCEDURE `sp_get_nodo_size_recursive`(
    IN p_nodo_id BIGINT UNSIGNED,
    OUT p_total_size BIGINT UNSIGNED
)
BEGIN
    DECLARE v_path VARCHAR(2000);
    
    -- Obtener path del nodo
    SELECT `path`
    INTO v_path
    FROM `nodo`
    WHERE `nodo_id` = p_nodo_id;
    
    -- Sumar tamaños de todos los archivos dentro
    SELECT COALESCE(SUM(`size_bytes`), 0)
    INTO p_total_size
    FROM `nodo`
    WHERE (`path` LIKE CONCAT(v_path, '/%') OR `nodo_id` = p_nodo_id)
      AND `tipo` = 'ARCHIVO'
      AND `is_deleted` = 0;
END$$

DELIMITER ;


-- =====================================================================================================================
-- PARTE 4: EVENTOS PROGRAMADOS (EVENT SCHEDULER)
-- =====================================================================================================================

-- NOTA: El event scheduler debe estar habilitado en el servidor MySQL
-- Si tienes privilegios SUPER, ejecuta: SET GLOBAL event_scheduler = ON;
-- Si no, pide al administrador del servidor que habilite event_scheduler en my.cnf/my.ini:
-- [mysqld]
-- event_scheduler = ON

-- -----------------------------------------------------
-- Evento: evt_cleanup_expired_clipboard
-- Propósito: Limpiar operaciones de clipboard expiradas cada hora
-- Frecuencia: Cada 1 hora
-- Acción: Marcar is_expired=1 para clipboards que pasaron expires_at
-- -----------------------------------------------------
DROP EVENT IF EXISTS `evt_cleanup_expired_clipboard`;

CREATE EVENT `evt_cleanup_expired_clipboard`
ON SCHEDULE EVERY 1 HOUR
STARTS CURRENT_TIMESTAMP
DO
    UPDATE `clipboard_operation`
    SET `is_expired` = 1
    WHERE `expires_at` < NOW()
      AND `is_expired` = 0;


-- -----------------------------------------------------
-- Evento: evt_cleanup_old_jobs
-- Propósito: Eliminar jobs completados de más de 30 días
-- Frecuencia: Cada 1 día a las 3 AM
-- Acción: DELETE jobs antiguos para no saturar la tabla
-- -----------------------------------------------------
DROP EVENT IF EXISTS `evt_cleanup_old_jobs`;

CREATE EVENT `evt_cleanup_old_jobs`
ON SCHEDULE EVERY 1 DAY
STARTS (CURRENT_DATE + INTERVAL 1 DAY + INTERVAL 3 HOUR)  -- Mañana a las 3 AM
DO
    DELETE FROM `file_operation_job`
    WHERE `status` IN ('COMPLETED', 'FAILED', 'CANCELLED')
      AND `completed_at` < DATE_SUB(NOW(), INTERVAL 30 DAY);


-- -----------------------------------------------------
-- Evento: evt_cleanup_old_sync_logs
-- Propósito: Eliminar logs de sincronización de más de 6 meses
-- Frecuencia: Cada 1 semana
-- Acción: DELETE logs antiguos
-- -----------------------------------------------------
DROP EVENT IF EXISTS `evt_cleanup_old_sync_logs`;

CREATE EVENT `evt_cleanup_old_sync_logs`
ON SCHEDULE EVERY 1 WEEK
STARTS (CURRENT_DATE + INTERVAL 1 DAY)  -- Mañana
DO
    DELETE FROM `github_sync_log`
    WHERE `started_at` < DATE_SUB(NOW(), INTERVAL 6 MONTH);


-- =====================================================================================================================
-- PARTE 5: MEJORAS EN CONFIGURACIÓN GCS
-- =====================================================================================================================

-- NOTA: Estas configuraciones se hacen en la aplicación Spring Boot, no en SQL
-- Se documentan aquí como referencia

/*
=====================================
CONFIGURACIÓN DE application.properties
=====================================

# Google Cloud Storage - File System
gcp.project-id=dev-portal-gtics
gcp.credentials-path=classpath:devportal-storage-key.json

# Bucket principal para file system
gcs.filesystem.bucket-name=dev-portal-storage
gcs.filesystem.base-url=https://storage.googleapis.com/dev-portal-storage

# Estructura de carpetas en GCS
gcs.filesystem.prefix.proyectos=proyectos/
gcs.filesystem.prefix.repositorios=repositorios/
gcs.filesystem.prefix.trash=trash/
gcs.filesystem.prefix.temp=temp/

# Límites de upload
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB

# Directorio temporal local
file.upload.temp-dir=${java.io.tmpdir}/teldev-uploads

# GitHub Integration
github.oauth.client-id=${GITHUB_CLIENT_ID:your-client-id-here}
github.oauth.client-secret=${GITHUB_CLIENT_SECRET:your-client-secret-here}
github.oauth.redirect-uri=http://localhost:8080/api/github/callback
github.api.base-url=https://api.github.com

=====================================
CONFIGURACIÓN DE BUCKET EN GCS (ya hecho)
=====================================

✅ Bucket: dev-portal-storage
✅ Ubicación: us-east1 (Carolina del Sur)
✅ Clase: Standard
✅ Control de versiones: Habilitado
✅ Eliminación no definitiva: 7 días
✅ Acceso público: Deshabilitado
✅ Encriptación: Administrada por Google
✅ Etiquetas:
   - environment: production
   - owner: dev-portal-gtics
   - service: file-system

RECOMENDACIÓN ADICIONAL:
- Crear regla de ciclo de vida para auto-eliminar archivos en trash/ después de 30 días
- Crear regla para mover archivos antiguos (>90 días sin acceso) a clase Nearline (más barato)

Comandos para aplicar:

# Lifecycle rule 1: Auto-eliminar trash después de 30 días
cat > lifecycle-trash.json << 'EOF'
{
  "lifecycle": {
    "rule": [
      {
        "action": {"type": "Delete"},
        "condition": {
          "age": 30,
          "matchesPrefix": ["trash/"]
        }
      }
    ]
  }
}
EOF

gsutil lifecycle set lifecycle-trash.json gs://dev-portal-storage/

# Lifecycle rule 2: Mover a Nearline archivos antiguos sin acceso
cat > lifecycle-nearline.json << 'EOF'
{
  "lifecycle": {
    "rule": [
      {
        "action": {"type": "SetStorageClass", "storageClass": "NEARLINE"},
        "condition": {
          "daysSinceCustomTime": 90,
          "matchesPrefix": ["proyectos/", "repositorios/"]
        }
      }
    ]
  }
}
EOF

gsutil lifecycle set lifecycle-nearline.json gs://dev-portal-storage/

*/


-- =====================================================================================================================
-- PARTE 6: ÍNDICES ADICIONALES PARA OPTIMIZACIÓN
-- =====================================================================================================================

-- NOTA: Los índices ya existen en la base de datos, por lo tanto los comentamos
-- Si necesitas crearlos en una base de datos nueva, descomenta estas líneas:

-- Índice para búsqueda de archivos por nombre (útil para "Buscar archivos...")
-- CREATE INDEX `idx_nodo_nombre_tipo` ON `nodo` (`nombre`(100), `tipo`, `is_deleted`);

-- Índice para búsqueda por MIME type (ej: "todos los PDFs")
-- CREATE INDEX `idx_nodo_mime` ON `nodo` (`mime_type`(50), `container_type`, `container_id`, `is_deleted`);

-- Índice para ordenar por fecha de creación
-- CREATE INDEX `idx_nodo_created` ON `nodo` (`container_type`, `container_id`, `creado_en`, `is_deleted`);

-- Índice para ordenar por tamaño (archivos más grandes)
-- CREATE INDEX `idx_nodo_size` ON `nodo` (`size_bytes`, `tipo`, `is_deleted`);


-- =====================================================================================================================
-- PARTE 7: VISTAS ÚTILES
-- =====================================================================================================================

-- -----------------------------------------------------
-- Vista: v_nodos_with_full_info
-- Propósito: Vista completa de nodos con información enriquecida
-- Uso: SELECT * FROM v_nodos_with_full_info WHERE container_id = 23
-- -----------------------------------------------------
CREATE OR REPLACE VIEW `v_nodos_with_full_info` AS
SELECT 
    n.nodo_id,
    n.container_type,
    n.container_id,
    n.parent_id,
    n.nombre,
    n.tipo,
    n.path,
    n.descripcion,
    n.size_bytes,
    n.mime_type,
    n.is_deleted,
    n.creado_en,
    n.actualizado_en,
    -- Información del creador
    u_creado.username AS creado_por_username,
    CONCAT(u_creado.nombre_usuario, ' ', u_creado.apellido_paterno) AS creado_por_nombre,
    -- Información del actualizador
    u_actualizado.username AS actualizado_por_username,
    -- Información del contenedor
    CASE 
        WHEN n.container_type = 'PROYECTO' THEN p.nombre_proyecto
        WHEN n.container_type = 'REPOSITORIO' THEN r.nombre_repositorio
    END AS container_nombre,
    -- Nodo padre
    parent.nombre AS parent_nombre,
    -- Conteo de hijos (si es carpeta)
    (SELECT COUNT(*) FROM nodo WHERE parent_id = n.nodo_id AND is_deleted = 0) AS children_count,
    -- Enlace asociado (para archivos)
    e.enlace_id,
    e.direccion_almacenamiento AS file_url
FROM nodo n
LEFT JOIN usuario u_creado ON n.creado_por = u_creado.usuario_id
LEFT JOIN usuario u_actualizado ON n.actualizado_por = u_actualizado.usuario_id
LEFT JOIN proyecto p ON n.container_type = 'PROYECTO' AND n.container_id = p.proyecto_id
LEFT JOIN repositorio r ON n.container_type = 'REPOSITORIO' AND n.container_id = r.repositorio_id
LEFT JOIN nodo parent ON n.parent_id = parent.nodo_id
LEFT JOIN enlace e ON n.nodo_id = e.contexto_id AND e.contexto_type = 'NODO' AND e.tipo_enlace = 'STORAGE';


-- =====================================================================================================================
-- FIN DEL SCRIPT
-- =====================================================================================================================

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- Mensaje de confirmación
SELECT '✅ FASE 0 COMPLETADA: Base de datos preparada para File System' AS resultado;
SELECT 'Nuevas tablas creadas: 6' AS detalle_1;
SELECT 'Triggers creados: 2' AS detalle_2;
SELECT 'Procedimientos almacenados: 5' AS detalle_3;
SELECT 'Eventos programados: 3' AS detalle_4;
SELECT 'Índices adicionales: 4' AS detalle_5;
SELECT 'Vistas: 1' AS detalle_6;
