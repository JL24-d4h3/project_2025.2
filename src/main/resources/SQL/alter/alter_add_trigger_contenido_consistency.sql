USE `dev_portal_sql`;

SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';
SET FOREIGN_KEY_CHECKS=1;

-- =====================================================
-- PARTE 1: VERIFICACIONES PREVIAS
-- =====================================================

SELECT '================================================' AS '';
SELECT '  FASE 0.7: TRIGGERS DE CONSISTENCIA          ' AS '';
SELECT '================================================' AS '';

-- Verificar que las tablas existen
SELECT 'Verificando existencia de tablas...' AS paso_1;

SELECT COUNT(*) INTO @contenido_exists
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'dev_portal_sql' 
  AND TABLE_NAME = 'contenido';

SELECT COUNT(*) INTO @version_api_exists
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'dev_portal_sql' 
  AND TABLE_NAME = 'version_api';

SELECT CONCAT(' Tabla contenido: ', IF(@contenido_exists > 0, 'EXISTE', 'NO EXISTE')) AS status;
SELECT CONCAT(' Tabla version_api: ', IF(@version_api_exists > 0, 'EXISTE', 'NO EXISTE')) AS status;

-- =====================================================
-- PARTE 2: ELIMINAR TRIGGERS EXISTENTES (IDEMPOTENTE)
-- =====================================================

SELECT 'Eliminando triggers existentes (si existen)...' AS paso_2;

DROP TRIGGER IF EXISTS `trg_contenido_consistency_check_insert`;
DROP TRIGGER IF EXISTS `trg_contenido_consistency_check_update`;

-- =====================================================
-- PARTE 3: CREAR TRIGGER BEFORE INSERT
-- =====================================================

SELECT 'Creando TRIGGER BEFORE INSERT...' AS paso_3;

DELIMITER $$

CREATE TRIGGER `trg_contenido_consistency_check_insert`
BEFORE INSERT ON `contenido`
FOR EACH ROW
BEGIN
    DECLARE version_doc_id BIGINT;
    DECLARE error_msg VARCHAR(500);
    
    -- Obtener el documentacion_documentacion_id de la version_api asociada
    SELECT documentacion_documentacion_id INTO version_doc_id
    FROM version_api
    WHERE version_id = NEW.version_api_version_id;
    
    -- VALIDACIÓN 1: La versión debe existir
    IF version_doc_id IS NULL THEN
        SET error_msg = CONCAT(
            'Error de consistencia [INSERT contenido]: ',
            'La version_api con version_id=', NEW.version_api_version_id, 
            ' no existe en la tabla version_api'
        );
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    END IF;
    
    -- VALIDACIÓN 2: Los documentacion_id deben coincidir
    IF version_doc_id != NEW.documentacion_documentacion_id THEN
        SET error_msg = CONCAT(
            'Error de consistencia [INSERT contenido]: ',
            'contenido.documentacion_documentacion_id=', NEW.documentacion_documentacion_id, 
            ' NO coincide con version_api.documentacion_documentacion_id=', version_doc_id,
            ' (version_api.version_id=', NEW.version_api_version_id, ')'
        );
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    END IF;
    
    -- Si llegamos aquí, la validación pasó correctamente
END$$

DELIMITER ;

-- =====================================================
-- PARTE 4: CREAR TRIGGER BEFORE UPDATE
-- =====================================================

SELECT 'Creando TRIGGER BEFORE UPDATE...' AS paso_4;

DELIMITER $$

CREATE TRIGGER `trg_contenido_consistency_check_update`
BEFORE UPDATE ON `contenido`
FOR EACH ROW
BEGIN
    DECLARE version_doc_id BIGINT;
    DECLARE error_msg VARCHAR(500);
    
    -- Obtener el documentacion_documentacion_id de la version_api asociada
    SELECT documentacion_documentacion_id INTO version_doc_id
    FROM version_api
    WHERE version_id = NEW.version_api_version_id;
    
    -- VALIDACIÓN 1: La versión debe existir
    IF version_doc_id IS NULL THEN
        SET error_msg = CONCAT(
            'Error de consistencia [UPDATE contenido]: ',
            'La version_api con version_id=', NEW.version_api_version_id, 
            ' no existe en la tabla version_api'
        );
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    END IF;
    
    -- VALIDACIÓN 2: Los documentacion_id deben coincidir
    IF version_doc_id != NEW.documentacion_documentacion_id THEN
        SET error_msg = CONCAT(
            'Error de consistencia [UPDATE contenido]: ',
            'contenido.documentacion_documentacion_id=', NEW.documentacion_documentacion_id, 
            ' NO coincide con version_api.documentacion_documentacion_id=', version_doc_id,
            ' (version_api.version_id=', NEW.version_api_version_id, ')'
        );
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = error_msg;
    END IF;
    
    -- Si llegamos aquí, la validación pasó correctamente
END$$

DELIMITER ;

-- =====================================================
-- PARTE 5: VERIFICACIÓN DE TRIGGERS CREADOS
-- =====================================================

SELECT '================================================' AS '';
SELECT '  TRIGGERS CREADOS EXITOSAMENTE                ' AS '';
SELECT '================================================' AS '';

SHOW TRIGGERS WHERE `Table` = 'contenido';

-- Detalles de los triggers desde information_schema
SELECT 
    TRIGGER_NAME as 'Trigger',
    EVENT_MANIPULATION as 'Evento',
    EVENT_OBJECT_TABLE as 'Tabla',
    ACTION_TIMING as 'Timing',
    'Validación consistencia documentacion_id' as 'Propósito'
FROM information_schema.TRIGGERS
WHERE TRIGGER_SCHEMA = 'dev_portal_sql'
  AND EVENT_OBJECT_TABLE = 'contenido'
ORDER BY TRIGGER_NAME;

-- =====================================================
-- PARTE 6: CASOS DE PRUEBA (COMENTADOS)
-- =====================================================
-- Descomentar las siguientes líneas para probar los triggers

/*
-- =====================================================
-- TEST CASE 1: Intentar INSERT con IDs inconsistentes
-- =====================================================
-- Este test DEBE FALLAR y mostrar error de consistencia

-- Primero, obtener IDs válidos de prueba
SELECT 
    v.version_id,
    v.documentacion_documentacion_id as version_doc_id,
    d.documentacion_id as otro_doc_id
FROM version_api v
CROSS JOIN documentacion d
WHERE v.documentacion_documentacion_id != d.documentacion_id
LIMIT 1;

-- Suponiendo que encontramos:
-- version_id = 1
-- version_doc_id = 10
-- otro_doc_id = 20

-- Intentar insertar con IDs inconsistentes (DEBE FALLAR)
INSERT INTO contenido (
    titulo_contenido,
    orden,
    clasificacion_clasificacion_id,
    documentacion_documentacion_id,  -- 20 (incorrecto)
    version_api_version_id            -- 1 (que pertenece a doc 10)
) VALUES (
    'Test inconsistente',
    1,
    1,
    20,  --  Incorrecto (version_id=1 tiene doc_id=10)
    1
);

-- Resultado esperado:
-- ERROR 1644 (45000): Error de consistencia [INSERT contenido]: 
-- contenido.documentacion_documentacion_id=20 NO coincide con 
-- version_api.documentacion_documentacion_id=10 (version_api.version_id=1)

-- =====================================================
-- TEST CASE 2: INSERT con IDs correctos y consistentes
-- =====================================================
-- Este test DEBE TENER ÉXITO

-- Obtener IDs válidos que sean consistentes
SELECT 
    v.version_id,
    v.documentacion_documentacion_id,
    c.clasificacion_id
FROM version_api v
CROSS JOIN clasificacion c
LIMIT 1;

-- Suponiendo que encontramos:
-- version_id = 1
-- documentacion_documentacion_id = 10
-- clasificacion_id = 1

-- Insertar con IDs consistentes (DEBE TENER ÉXITO)
INSERT INTO contenido (
    titulo_contenido,
    orden,
    clasificacion_clasificacion_id,
    documentacion_documentacion_id,  -- 10 (correcto)
    version_api_version_id            -- 1 (que pertenece a doc 10)
) VALUES (
    'Test consistente',
    1,
    1,
    10,  --  Correcto (coincide con version_api)
    1
);

-- Resultado esperado:
-- Query OK, 1 row affected

-- Limpiar datos de prueba
DELETE FROM contenido WHERE titulo_contenido LIKE 'Test %';

-- =====================================================
-- TEST CASE 3: UPDATE cambiando a IDs inconsistentes
-- =====================================================
-- Este test DEBE FALLAR

-- Obtener un registro existente
SELECT * FROM contenido LIMIT 1;

-- Intentar actualizar con IDs inconsistentes (DEBE FALLAR)
UPDATE contenido 
SET documentacion_documentacion_id = 999  -- ID inexistente o inconsistente
WHERE contenido_id = 1;

-- Resultado esperado:
-- ERROR 1644 (45000): Error de consistencia [UPDATE contenido]...

*/

-- =====================================================
-- PARTE 7: ESTADÍSTICAS FINALES
-- =====================================================

SELECT '================================================' AS '';
SELECT '  ESTADÍSTICAS DE DATOS EXISTENTES            ' AS '';
SELECT '================================================' AS '';

-- Contar registros en contenido
SELECT COUNT(*) as 'Total registros en contenido' 
FROM contenido;

-- Verificar si hay registros potencialmente inconsistentes
-- (esto no debería pasar si los FKs están bien definidos)
SELECT 
    COUNT(*) as 'Registros potencialmente inconsistentes'
FROM contenido c
LEFT JOIN version_api v ON v.version_id = c.version_api_version_id
WHERE v.documentacion_documentacion_id != c.documentacion_documentacion_id;

-- Si el conteo anterior es > 0, investigar:
-- SELECT 
--     c.contenido_id,
--     c.documentacion_documentacion_id as contenido_doc_id,
--     c.version_api_version_id,
--     v.documentacion_documentacion_id as version_doc_id
-- FROM contenido c
-- LEFT JOIN version_api v ON v.version_id = c.version_api_version_id
-- WHERE v.documentacion_documentacion_id != c.documentacion_documentacion_id;

-- =====================================================
-- PARTE 8: ROLLBACK (SI ES NECESARIO)
-- =====================================================

/*
-- Para deshacer los cambios, ejecutar:

USE dev_portal_sql;

DROP TRIGGER IF EXISTS `trg_contenido_consistency_check_insert`;
DROP TRIGGER IF EXISTS `trg_contenido_consistency_check_update`;

-- Verificar eliminación
SHOW TRIGGERS WHERE `Table` = 'contenido';

*/

-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================

SET SQL_MODE=@OLD_SQL_MODE;

SELECT '================================================' AS '';
SELECT '   FASE 0.7 COMPLETADA EXITOSAMENTE           ' AS '';
SELECT '================================================' AS '';
SELECT CONCAT('Fecha: ', NOW()) AS 'Timestamp';
SELECT '2 TRIGGERS creados:' AS '';
SELECT '  1. trg_contenido_consistency_check_insert' AS '';
SELECT '  2. trg_contenido_consistency_check_update' AS '';

-- =====================================================================
-- NOTAS TÉCNICAS IMPORTANTES
-- =====================================================================
-- 1. NOMBRES DE COLUMNAS CORRECTOS (tu esquema real):
--    - contenido.documentacion_documentacion_id (FK a documentacion)
--    - contenido.version_api_version_id (FK a version_api)
--    - version_api.documentacion_documentacion_id (FK a documentacion)
--
-- 2. LÓGICA DE VALIDACIÓN:
--    Cuando se inserta/actualiza un registro en contenido:
--    a) Buscar documentacion_documentacion_id de la version_api
--    b) Comparar con contenido.documentacion_documentacion_id
--    c) Si no coinciden  RECHAZAR con SIGNAL SQLSTATE '45000'
--
-- 3. MYSQL VERSION:
--    Requiere MySQL 5.7+ para SIGNAL SQLSTATE
--
-- 4. PERFORMANCE:
--    Los triggers se ejecutan POR CADA FILA (FOR EACH ROW)
--    El impacto es mínimo (1 SELECT por operación)
--
-- 5. MANTENIMIENTO:
--    - Script es IDEMPOTENTE (puede ejecutarse múltiples veces)
--    - DROP TRIGGER IF EXISTS previene errores
--
-- 6. TESTING:
--    - Descomentar PARTE 6 para ejecutar casos de prueba
--    - Recomendado: Probar en entorno de desarrollo primero
--
-- 7. ROLLBACK:
--    - Descomentar PARTE 8 para eliminar triggers
--    - No afecta datos existentes, solo elimina validación
-- =====================================================================
