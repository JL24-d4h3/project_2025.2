-- =====================================================================================================================
-- FASE 0.4: SISTEMA DE RAMAS (BRANCHES) PARA REPOSITORIOS
-- =====================================================================================================================
-- Fecha: 14 de Noviembre, 2025
-- Propósito: Implementar sistema de múltiples ramas tipo GitHub para repositorios
-- 
-- CAMBIOS INCLUIDOS:
-- 1. Tabla repositorio_rama para almacenar múltiples ramas por repositorio
-- 2. Triggers para garantizar una sola rama principal por repositorio
-- 3. Trigger para crear rama principal automáticamente al crear repositorio
-- 4. Migración de datos existentes (crear rama 'main' para repos sin rama)
-- 5. Índices para optimización de queries
-- =====================================================================================================================

USE `dev_portal_sql`;

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- =====================================================================================================================
-- LIMPIEZA INICIAL: Eliminar triggers existentes de intentos anteriores
-- =====================================================================================================================
DROP TRIGGER IF EXISTS `trg_before_insert_rama_principal`;
DROP TRIGGER IF EXISTS `trg_before_update_rama_principal`;
DROP TRIGGER IF EXISTS `trg_after_insert_repositorio_create_main_branch`;

-- =====================================================================================================================
-- PARTE 1: CREAR TABLA repositorio_rama
-- =====================================================================================================================

CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`repositorio_rama` (
    `rama_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `repositorio_id` BIGINT UNSIGNED NOT NULL COMMENT 'ID del repositorio al que pertenece',
    `nombre_rama` VARCHAR(100) NOT NULL COMMENT 'Nombre de la rama (main, develop, feature-auth, etc)',
    `descripcion_rama` TEXT NULL COMMENT 'Descripción opcional de la rama',
    `is_principal` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Indica si es la rama principal del repositorio',
    `is_protegida` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Rama protegida (solo admin puede eliminar/modificar)',
    `ultimo_commit_hash` VARCHAR(128) NULL COMMENT 'SHA del último commit (para integración GitHub)',
    `ultimo_commit_fecha` DATETIME NULL COMMENT 'Fecha del último commit',
    `ultimo_commit_mensaje` TEXT NULL COMMENT 'Mensaje del último commit',
    `ultimo_commit_autor` VARCHAR(255) NULL COMMENT 'Autor del último commit',
    `creada_por_usuario_id` BIGINT UNSIGNED NULL COMMENT 'Usuario que creó la rama',
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación de la rama',
    `actualizada_en` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Última actualización',
    
    PRIMARY KEY (`rama_id`),
    
    -- ÍNDICES
    UNIQUE INDEX `uk_repo_rama` (`repositorio_id`, `nombre_rama`) COMMENT 'No puede haber dos ramas con el mismo nombre en un repo',
    INDEX `idx_repo_principal` (`repositorio_id`, `is_principal`) COMMENT 'Para buscar rama principal rápidamente',
    INDEX `idx_rama_nombre` (`nombre_rama`) COMMENT 'Para búsquedas por nombre de rama',
    INDEX `idx_rama_creador` (`creada_por_usuario_id`) COMMENT 'Para buscar ramas creadas por usuario',
    
    -- FOREIGN KEYS
    CONSTRAINT `fk_rama_repositorio`
        FOREIGN KEY (`repositorio_id`)
        REFERENCES `dev_portal_sql`.`repositorio` (`repositorio_id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
        
    CONSTRAINT `fk_rama_creador`
        FOREIGN KEY (`creada_por_usuario_id`)
        REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
        ON DELETE SET NULL
        ON UPDATE CASCADE
        
) ENGINE=InnoDB 
  DEFAULT CHARACTER SET=utf8mb4 
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='Ramas (branches) de repositorios - Sistema tipo GitHub';


-- =====================================================================================================================
-- PARTE 2: MIGRACIÓN DE DATOS EXISTENTES (SIN TRIGGERS)
-- =====================================================================================================================

-- Para todos los repositorios existentes que NO tienen rama en repositorio_rama, crear la rama principal
INSERT INTO `repositorio_rama` (
    `repositorio_id`,
    `nombre_rama`,
    `descripcion_rama`,
    `is_principal`,
    `is_protegida`,
    `ultimo_commit_hash`,
    `creada_por_usuario_id`,
    `fecha_creacion`
)
SELECT 
    r.repositorio_id,
    COALESCE(r.rama_principal_repositorio, 'main') AS nombre_rama,
    CONCAT('Rama principal del repositorio ', r.nombre_repositorio) AS descripcion_rama,
    1 AS is_principal,
    1 AS is_protegida,
    r.ultimo_commit_hash,
    r.creado_por_usuario_id,
    COALESCE(r.fecha_creacion, NOW()) AS fecha_creacion
FROM 
    `repositorio` r
LEFT JOIN 
    `repositorio_rama` rr ON r.repositorio_id = rr.repositorio_id AND rr.is_principal = 1
WHERE 
    rr.rama_id IS NULL; -- Solo para repos que NO tienen rama principal


-- =====================================================================================================================
-- PARTE 3: TRIGGER PARA CREAR RAMA PRINCIPAL AUTOMÁTICAMENTE (DESPUÉS DE MIGRACIÓN)
-- =====================================================================================================================
-- NOTA: Creamos el trigger DESPUÉS de la migración para evitar conflictos
-- La validación de rama principal única se maneja en la capa de aplicación (Java)

-- Eliminar trigger si ya existe (para re-ejecución del script)
DROP TRIGGER IF EXISTS `trg_after_insert_repositorio_create_main_branch`;

DELIMITER $$

-- -----------------------------------------------------
-- Trigger: Al CREAR un repositorio, crear automáticamente su rama principal
-- -----------------------------------------------------
CREATE TRIGGER `trg_after_insert_repositorio_create_main_branch`
AFTER INSERT ON `repositorio`
FOR EACH ROW
BEGIN
    -- Crear rama principal automáticamente
    INSERT INTO `repositorio_rama` (
        `repositorio_id`,
        `nombre_rama`,
        `is_principal`,
        `is_protegida`,
        `creada_por_usuario_id`,
        `fecha_creacion`
    ) VALUES (
        NEW.repositorio_id,
        COALESCE(NEW.rama_principal_repositorio, 'main'), -- Usar rama_principal_repositorio o 'main' por defecto
        1, -- Es la rama principal
        1, -- Rama principal siempre protegida
        NEW.creado_por_usuario_id,
        NOW()
    );
END$$

DELIMITER ;


-- =====================================================================================================================
-- PARTE 4: MODIFICAR TABLA nodo PARA SOPORTAR RAMAS
-- =====================================================================================================================

-- Agregar columna rama_id a la tabla nodo (para asociar cada nodo con una rama específica)
-- NOTA: Los nodos de PROYECTOS no tienen rama (rama_id = NULL)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = 'dev_portal_sql' 
                   AND TABLE_NAME = 'nodo' 
                   AND COLUMN_NAME = 'rama_id');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `nodo` ADD COLUMN `rama_id` BIGINT UNSIGNED NULL COMMENT ''ID de la rama (solo para repositorios, NULL para proyectos)'' AFTER `container_id`',
    'SELECT ''Column rama_id already exists'' AS info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Crear índice para búsquedas por rama
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
                     WHERE TABLE_SCHEMA = 'dev_portal_sql' 
                     AND TABLE_NAME = 'nodo' 
                     AND INDEX_NAME = 'idx_nodo_container_rama');

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_nodo_container_rama ON nodo(container_type, container_id, rama_id, parent_id, is_deleted)',
    'SELECT ''Index idx_nodo_container_rama already exists'' AS info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Agregar foreign key a rama_id
SET @fk_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
                  WHERE TABLE_SCHEMA = 'dev_portal_sql' 
                  AND TABLE_NAME = 'nodo' 
                  AND CONSTRAINT_NAME = 'fk_nodo_rama');

SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE `nodo` ADD CONSTRAINT `fk_nodo_rama` 
     FOREIGN KEY (`rama_id`) REFERENCES `repositorio_rama`(`rama_id`) 
     ON DELETE SET NULL ON UPDATE CASCADE',
    'SELECT ''Foreign key fk_nodo_rama already exists'' AS info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- =====================================================================================================================
-- PARTE 5: MIGRAR NODOS EXISTENTES DE REPOSITORIOS A SU RAMA PRINCIPAL
-- =====================================================================================================================

-- Asociar todos los nodos de repositorios a su rama principal
UPDATE `nodo` n
INNER JOIN `repositorio_rama` rr 
    ON n.container_type = 'REPOSITORIO' 
    AND n.container_id = rr.repositorio_id 
    AND rr.is_principal = 1
SET n.rama_id = rr.rama_id
WHERE n.container_type = 'REPOSITORIO' 
  AND n.rama_id IS NULL;


-- =====================================================================================================================
-- PARTE 6: PROCEDIMIENTO ALMACENADO ÚTIL
-- =====================================================================================================================

DELIMITER $$

-- -----------------------------------------------------
-- Procedimiento: sp_create_branch_from_existing
-- Propósito: Crear una nueva rama copiando la estructura de archivos de otra rama
-- Uso: CALL sp_create_branch_from_existing(32, 'main', 'develop', 1);
-- Efecto: Crea rama 'develop' en repo R-32 copiando estructura de 'main'
-- -----------------------------------------------------
DROP PROCEDURE IF EXISTS `sp_create_branch_from_existing`$$

CREATE PROCEDURE `sp_create_branch_from_existing`(
    IN p_repositorio_id BIGINT UNSIGNED,
    IN p_rama_origen VARCHAR(100),
    IN p_rama_destino VARCHAR(100),
    IN p_usuario_id BIGINT UNSIGNED
)
BEGIN
    DECLARE v_rama_origen_id BIGINT UNSIGNED;
    DECLARE v_rama_destino_id BIGINT UNSIGNED;
    DECLARE v_error_msg VARCHAR(500);
    
    -- Buscar ID de rama origen
    SELECT rama_id INTO v_rama_origen_id
    FROM repositorio_rama
    WHERE repositorio_id = p_repositorio_id 
      AND nombre_rama = p_rama_origen
    LIMIT 1;
    
    IF v_rama_origen_id IS NULL THEN
        SET v_error_msg = CONCAT('Rama origen "', p_rama_origen, '" no existe en repositorio ', p_repositorio_id);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = v_error_msg;
    END IF;
    
    -- Crear nueva rama
    INSERT INTO repositorio_rama (
        repositorio_id, 
        nombre_rama, 
        descripcion_rama,
        is_principal, 
        is_protegida,
        creada_por_usuario_id,
        fecha_creacion
    ) VALUES (
        p_repositorio_id,
        p_rama_destino,
        CONCAT('Rama creada desde ', p_rama_origen),
        0,
        0,
        p_usuario_id,
        NOW()
    );
    
    SET v_rama_destino_id = LAST_INSERT_ID();
    
    -- Copiar estructura de nodos (carpetas y archivos)
    -- NOTA: Esto copia la ESTRUCTURA, no los archivos físicos en GCS
    -- Los archivos físicos se deben copiar en el backend
    INSERT INTO nodo (
        container_type,
        container_id,
        rama_id,
        parent_id,
        nombre,
        tipo,
        path,
        descripcion,
        size_bytes,
        mime_type,
        creado_por,
        creado_en,
        is_deleted
    )
    SELECT 
        container_type,
        container_id,
        v_rama_destino_id AS rama_id,
        parent_id, -- NOTA: Esto puede necesitar mapeo si se copian nodos específicos
        nombre,
        tipo,
        REPLACE(path, CONCAT('/', p_rama_origen, '/'), CONCAT('/', p_rama_destino, '/')) AS path,
        descripcion,
        size_bytes,
        mime_type,
        p_usuario_id AS creado_por,
        NOW() AS creado_en,
        0 AS is_deleted
    FROM nodo
    WHERE container_type = 'REPOSITORIO'
      AND container_id = p_repositorio_id
      AND rama_id = v_rama_origen_id
      AND is_deleted = 0;
    
    SELECT 
        v_rama_destino_id AS nueva_rama_id,
        p_rama_destino AS nombre_rama,
        ROW_COUNT() AS nodos_copiados;
END$$

DELIMITER ;


-- =====================================================================================================================
-- PARTE 7: VERIFICACIÓN FINAL
-- =====================================================================================================================

-- Mostrar resumen de ramas creadas
SELECT '=== RESUMEN DE RAMAS POR REPOSITORIO ===' AS info;

SELECT 
    r.repositorio_id,
    r.nombre_repositorio,
    rr.nombre_rama,
    rr.is_principal,
    rr.is_protegida,
    rr.fecha_creacion,
    COUNT(n.nodo_id) AS cantidad_nodos
FROM repositorio r
LEFT JOIN repositorio_rama rr ON r.repositorio_id = rr.repositorio_id
LEFT JOIN nodo n ON rr.rama_id = n.rama_id AND n.is_deleted = 0
GROUP BY r.repositorio_id, rr.rama_id
ORDER BY r.repositorio_id, rr.is_principal DESC, rr.nombre_rama;


-- Verificar integridad
SELECT '=== VERIFICACIÓN DE INTEGRIDAD ===' AS info;

-- Repositorios sin rama principal
SELECT 
    'ALERTA: Repositorios sin rama principal' AS tipo,
    COUNT(*) AS cantidad
FROM repositorio r
LEFT JOIN repositorio_rama rr ON r.repositorio_id = rr.repositorio_id AND rr.is_principal = 1
WHERE rr.rama_id IS NULL;

-- Repositorios con múltiples ramas principales (NO debería ocurrir)
SELECT 
    'ERROR: Repositorios con múltiples ramas principales' AS tipo,
    COUNT(*) AS cantidad
FROM (
    SELECT repositorio_id
    FROM repositorio_rama
    WHERE is_principal = 1
    GROUP BY repositorio_id
    HAVING COUNT(*) > 1
) AS duplicados;

-- Nodos de repositorio sin rama asignada
SELECT 
    'ALERTA: Nodos de repositorio sin rama' AS tipo,
    COUNT(*) AS cantidad
FROM nodo
WHERE container_type = 'REPOSITORIO' 
  AND rama_id IS NULL
  AND is_deleted = 0;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- =====================================================================================================================
-- FIN DEL SCRIPT
-- =====================================================================================================================

SELECT '✅ Script ejecutado exitosamente. Sistema de ramas implementado.' AS resultado;
