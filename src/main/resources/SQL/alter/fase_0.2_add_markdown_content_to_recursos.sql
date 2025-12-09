-- =====================================================================
-- FASE 0.2: Agregar campo markdown_content a tabla recurso
-- =====================================================================
-- Fecha: 11 de Noviembre, 2025
-- Objetivo: Permitir almacenar contenido Markdown directamente en BD
--           para archivos pequeños (< 64KB), evitando subidas a GCS
-- =====================================================================

USE dev_portal_sql;

-- =====================================================================
-- Verificar si la columna ya existe antes de agregarla
-- =====================================================================
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'dev_portal_sql' 
  AND TABLE_NAME = 'recurso' 
  AND COLUMN_NAME = 'markdown_content';

-- Solo agregar si NO existe
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE recurso ADD COLUMN markdown_content TEXT NULL COMMENT ''Contenido Markdown almacenado en BD (< 64KB). Si NULL, el contenido está en GCS (url_recurso).''',
    'SELECT ''✓ La columna markdown_content ya existe. No se requiere acción.'' AS mensaje');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verificar que la columna existe
DESCRIBE recurso;

-- =====================================================================
-- NOTAS TÉCNICAS
-- =====================================================================
-- 1. Tipo TEXT: Soporta hasta ~65,535 caracteres (suficiente para < 64KB)
-- 2. NULL permitido: El contenido puede estar en GCS o en BD
-- 3. Lógica de decisión (FASE 0.4):
--    - Si contenido < 64KB → guardar en markdown_content (tipo_enlace = TEXTO_CONTENIDO)
--    - Si contenido >= 64KB → subir a GCS (tipo_enlace = STORAGE)
-- 4. Lectura (FASE 0.5):
--    - Si markdown_content NOT NULL → leer de BD
--    - Si markdown_content IS NULL → descargar de url_recurso (GCS)
-- =====================================================================

-- =====================================================================
-- ROLLBACK (si es necesario deshacer cambios)
-- =====================================================================
-- ALTER TABLE recurso DROP COLUMN markdown_content;
-- =====================================================================
