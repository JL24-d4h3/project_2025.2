-- Cleaned ALTER script for dev_portal_sql
-- This script was generated from alter_noha_paul_mark.sql by removing markdown/text and adding safe existence checks.

USE `dev_portal_sql`;

-- 1) CHATBOT: add 'EN_REVISION' to estado_conversacion enum (safe approach: rebuild column)
-- Verify current enum contains the target or not
SELECT
    COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='dev_portal_sql'
  AND TABLE_NAME='chatbot_conversacion'
  AND COLUMN_NAME='estado_conversacion'
INTO @tipo_enum;

-- If enum already contains 'EN_REVISION' this will be a no-op; otherwise modify column
SET @need_alter = (LOCATE('EN_REVISION', @tipo_enum) = 0);

SET @sql = IF(@need_alter,
    "ALTER TABLE `chatbot_conversacion` MODIFY COLUMN `estado_conversacion` ENUM('ACTIVA','CERRADA','ARCHIVADA','EN_REVISION') NOT NULL DEFAULT 'ACTIVA';",
    'SELECT "estado_conversacion already contains EN_REVISION"');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) CHATBOT: add column contexto_inicial_ia if not exists
SELECT COUNT(*) INTO @cnt_col FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA='dev_portal_sql' AND TABLE_NAME='chatbot_conversacion' AND COLUMN_NAME='contexto_inicial_ia';

SET @sql = IF(@cnt_col=0,
    "ALTER TABLE `chatbot_conversacion` ADD COLUMN `contexto_inicial_ia` VARCHAR(24) NULL DEFAULT NULL COMMENT 'ObjectId MongoDB -> Collection: context_ia';",
    'SELECT "column context already exists"');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) CHATBOT: add index for contexto_inicial_ia if not exists
SELECT COUNT(*) INTO @idx_cnt FROM information_schema.STATISTICS
 WHERE TABLE_SCHEMA='dev_portal_sql' AND TABLE_NAME='chatbot_conversacion' AND INDEX_NAME='idx_contexto_inicial';

SET @sql = IF(@idx_cnt=0,
    "CREATE INDEX `idx_contexto_inicial` ON `chatbot_conversacion` (`contexto_inicial_ia`(24));",
    'SELECT "index already exists"');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) FEEDBACK: drop foreign key only if it exists
SELECT COUNT(*) INTO @fk_cnt FROM information_schema.TABLE_CONSTRAINTS
 WHERE TABLE_SCHEMA='dev_portal_sql' AND TABLE_NAME='feedback' AND CONSTRAINT_TYPE='FOREIGN KEY' AND CONSTRAINT_NAME='fk_feedback_revisado_por';

SET @sql = IF(@fk_cnt>0,
    "ALTER TABLE `feedback` DROP FOREIGN KEY `fk_feedback_revisado_por`;",
    'SELECT "foreign key not present"');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5) (Optional) Add CHECK constraint for puntuacion range if needed and not present
-- MySQL supports CHECK but it may be accepted silently depending on sql_mode/version; we add it only if not exists.
SELECT COUNT(*) INTO @chk_cnt FROM information_schema.TABLE_CONSTRAINTS
 WHERE TABLE_SCHEMA='dev_portal_sql' AND TABLE_NAME='feedback' AND CONSTRAINT_TYPE='CHECK' AND CONSTRAINT_NAME='chk_feedback_puntuacion_range';

SET @sql = IF(@chk_cnt=0,
    "ALTER TABLE `feedback` ADD CONSTRAINT `chk_feedback_puntuacion_range` CHECK (`puntuacion` >= 1.0 AND `puntuacion` <= 5.0);",
    'SELECT "check already exists or not supported"');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- NOTES:
-- 1) This script avoids non-SQL text and uses information_schema checks + prepared statements to skip operations
--    that would fail if objects already exist.
-- 2) If your MySQL user does not have privileges to read information_schema or to create / alter tables, the
--    script may still fail. Run as a user with adequate privileges.
-- 3) Always backup the DB before running ALTER scripts. Example:
--    mysqldump -u root -p dev_portal_sql > backup_dev_portal_sql.sql

-- Usage (example):
-- mysql -u <user> -p < c:\path\to\alter_noha_paul_mark.cleaned.sql

-- End of script
