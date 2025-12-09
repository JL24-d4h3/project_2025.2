-- =====================================================
-- TRIGGERS PARA AUTO-GENERAR NODO RAÍZ
-- =====================================================
-- Propósito: Crear automáticamente un nodo raíz cuando 
--            se crea un proyecto o repositorio
-- Fecha: 3 de Noviembre, 2025
-- =====================================================

DELIMITER $$

-- =====================================================
-- TRIGGER 1: Auto-crear nodo raíz al insertar PROYECTO
-- =====================================================
DROP TRIGGER IF EXISTS after_proyecto_insert_create_root_node$$

CREATE TRIGGER after_proyecto_insert_create_root_node
AFTER INSERT ON proyecto
FOR EACH ROW
BEGIN
    DECLARE new_nodo_id BIGINT;
    
    -- Crear el nodo raíz del proyecto
    INSERT INTO nodo (
        container_type,
        container_id,
        parent_id,
        nombre,
        tipo,
        path,
        descripcion,
        size_bytes,
        mime_type,
        created_by,
        created_at,
        updated_by,
        updated_at,
        is_deleted,
        deleted_at
    ) VALUES (
        'PROYECTO',                    -- container_type
        NEW.proyecto_id,               -- container_id
        NULL,                          -- parent_id (NULL = nodo raíz)
        '/',                           -- nombre (carpeta raíz)
        'CARPETA',                     -- tipo
        '/',                           -- path (raíz)
        'Carpeta raíz del proyecto',   -- descripcion
        0,                             -- size_bytes
        NULL,                          -- mime_type
        NEW.created_by,                -- created_by
        NOW(),                         -- created_at
        NEW.created_by,                -- updated_by
        NOW(),                         -- updated_at
        0,                             -- is_deleted
        NULL                           -- deleted_at
    );
    
    -- Obtener el ID del nodo recién creado
    SET new_nodo_id = LAST_INSERT_ID();
    
    -- Actualizar el proyecto con el root_node_id
    UPDATE proyecto 
    SET root_node_id = new_nodo_id,
        updated_at = NOW()
    WHERE proyecto_id = NEW.proyecto_id;
END$$


-- =====================================================
-- TRIGGER 2: Auto-crear nodo raíz al insertar REPOSITORIO
-- =====================================================
DROP TRIGGER IF EXISTS after_repositorio_insert_create_root_node$$

CREATE TRIGGER after_repositorio_insert_create_root_node
AFTER INSERT ON repositorio
FOR EACH ROW
BEGIN
    DECLARE new_nodo_id BIGINT;
    
    -- Crear el nodo raíz del repositorio
    INSERT INTO nodo (
        container_type,
        container_id,
        parent_id,
        nombre,
        tipo,
        path,
        descripcion,
        size_bytes,
        mime_type,
        created_by,
        created_at,
        updated_by,
        updated_at,
        is_deleted,
        deleted_at
    ) VALUES (
        'REPOSITORIO',                 -- container_type
        NEW.repositorio_id,            -- container_id
        NULL,                          -- parent_id (NULL = nodo raíz)
        '/',                           -- nombre (carpeta raíz)
        'CARPETA',                     -- tipo
        '/',                           -- path (raíz)
        'Carpeta raíz del repositorio',-- descripcion
        0,                             -- size_bytes
        NULL,                          -- mime_type
        NEW.creado_por,                -- created_by
        NOW(),                         -- created_at
        NEW.creado_por,                -- updated_by
        NOW(),                         -- updated_at
        0,                             -- is_deleted
        NULL                           -- deleted_at
    );
    
    -- Obtener el ID del nodo recién creado
    SET new_nodo_id = LAST_INSERT_ID();
    
    -- Actualizar el repositorio con el root_node_id
    UPDATE repositorio 
    SET root_node_id = new_nodo_id,
        actualizado_en = NOW()
    WHERE repositorio_id = NEW.repositorio_id;
END$$


-- =====================================================
-- TRIGGER 3: Actualizar tamaño de carpeta padre al modificar hijo
-- =====================================================
DROP TRIGGER IF EXISTS after_nodo_update_parent_size$$

CREATE TRIGGER after_nodo_update_parent_size
AFTER UPDATE ON nodo
FOR EACH ROW
BEGIN
    DECLARE parent_total_size BIGINT DEFAULT 0;
    
    -- Solo actualizar si cambió el tamaño y tiene padre
    IF (NEW.size_bytes != OLD.size_bytes) AND (NEW.parent_id IS NOT NULL) THEN
        -- Calcular tamaño total de todos los hijos del padre
        SELECT COALESCE(SUM(size_bytes), 0) INTO parent_total_size
        FROM nodo
        WHERE parent_id = NEW.parent_id
          AND is_deleted = 0;
        
        -- Actualizar tamaño de la carpeta padre
        UPDATE nodo
        SET size_bytes = parent_total_size,
            updated_at = NOW()
        WHERE nodo_id = NEW.parent_id;
    END IF;
END$$


-- =====================================================
-- TRIGGER 4: Soft delete en cascada (marcar hijos como eliminados)
-- =====================================================
DROP TRIGGER IF EXISTS after_nodo_soft_delete_cascade$$

CREATE TRIGGER after_nodo_soft_delete_cascade
AFTER UPDATE ON nodo
FOR EACH ROW
BEGIN
    -- Si se marcó como eliminado, marcar también los hijos
    IF (NEW.is_deleted = 1) AND (OLD.is_deleted = 0) THEN
        UPDATE nodo
        SET is_deleted = 1,
            deleted_at = NEW.deleted_at,
            updated_at = NOW()
        WHERE parent_id = NEW.nodo_id
          AND is_deleted = 0;
    END IF;
    
    -- Si se restauró, restaurar también los hijos
    IF (NEW.is_deleted = 0) AND (OLD.is_deleted = 1) THEN
        UPDATE nodo
        SET is_deleted = 0,
            deleted_at = NULL,
            updated_at = NOW()
        WHERE parent_id = NEW.nodo_id
          AND is_deleted = 1;
    END IF;
END$$

DELIMITER ;

-- =====================================================
-- VERIFICACIÓN DE TRIGGERS
-- =====================================================
-- Para verificar que los triggers se crearon correctamente:
-- SHOW TRIGGERS WHERE `Table` IN ('proyecto', 'repositorio', 'nodo');

-- =====================================================
-- TESTING DE TRIGGERS
-- =====================================================
-- 1. Crear un proyecto y verificar que se crea el nodo raíz:
--    INSERT INTO proyecto (nombre_proyecto, created_by) VALUES ('Test Project', 1);
--    SELECT * FROM nodo WHERE container_type='PROYECTO' ORDER BY nodo_id DESC LIMIT 1;

-- 2. Crear un repositorio y verificar que se crea el nodo raíz:
--    INSERT INTO repositorio (nombre_repositorio, creado_por) VALUES ('Test Repo', 1);
--    SELECT * FROM nodo WHERE container_type='REPOSITORIO' ORDER BY nodo_id DESC LIMIT 1;

-- 3. Verificar soft delete en cascada:
--    UPDATE nodo SET is_deleted=1, deleted_at=NOW() WHERE nodo_id=XXX;
--    SELECT * FROM nodo WHERE parent_id=XXX; -- Todos deben tener is_deleted=1
