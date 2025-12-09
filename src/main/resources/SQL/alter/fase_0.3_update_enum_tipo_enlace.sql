-- =====================================================================
-- FASE 0.3: Actualizar ENUM tipo_enlace en tabla enlace
-- =====================================================================
-- Fecha: 11 de Noviembre, 2025
-- Objetivo: Asegurar que el ENUM tipo_enlace incluye todos los valores
--           necesarios para la estrategia de almacenamiento híbrido
-- =====================================================================

USE dev_portal_sql;

-- =====================================================================
-- PASO 1: Verificar valores actuales del ENUM
-- =====================================================================
SHOW COLUMNS FROM enlace LIKE 'tipo_enlace';

-- Resultado esperado:
-- Si TEXTO_CONTENIDO y ENLACE_EXTERNO ya existen, el ENUM está correcto
-- Si faltan, se deben agregar

-- =====================================================================
-- PASO 2: Actualizar ENUM con todos los valores necesarios
-- =====================================================================
-- IMPORTANTE: Este ALTER TABLE actualiza el ENUM completo
-- Debe incluir TODOS los valores (existentes + nuevos)

ALTER TABLE enlace 
MODIFY COLUMN tipo_enlace ENUM(
    'STORAGE',           -- Archivos almacenados en GCS
    'METADATA',          -- Metadatos en NoSQL (MongoDB)
    'THUMBNAIL',         -- Miniaturas/previews de archivos
    'BACKUP',            -- Archivos de respaldo
    'TEMPORAL',          -- Archivos temporales (se eliminan después)
    'TEXTO_CONTENIDO',   -- Contenido Markdown en BD (< 64KB) - FASE 0.2
    'ENLACE_EXTERNO'     -- URLs externas (documentación externa, etc.)
) NOT NULL DEFAULT 'STORAGE';

-- =====================================================================
-- PASO 3: Verificar que el cambio se aplicó correctamente
-- =====================================================================
SHOW COLUMNS FROM enlace LIKE 'tipo_enlace';

-- Resultado esperado:
-- +-------------+---------------------------------------------------------------------+
-- | Field       | Type                                                                |
-- +-------------+---------------------------------------------------------------------+
-- | tipo_enlace | enum('STORAGE','METADATA','THUMBNAIL','BACKUP','TEMPORAL',          |
-- |             |      'TEXTO_CONTENIDO','ENLACE_EXTERNO')                            |
-- +-------------+---------------------------------------------------------------------+

-- =====================================================================
-- PASO 4: Verificar que no hay datos incompatibles
-- =====================================================================
-- Si la tabla tiene datos con valores no incluidos en el nuevo ENUM,
-- MySQL rechazará el ALTER TABLE. Verificar primero:

SELECT DISTINCT tipo_enlace 
FROM enlace 
ORDER BY tipo_enlace;

-- Si aparecen valores no listados arriba, agregarlos al ENUM o migrarlos

-- =====================================================================
-- NOTAS TÉCNICAS
-- =====================================================================
-- 1. Estrategia de uso (FASE 0.4 - Guardado):
--    - Contenido < 64KB  → tipo_enlace = 'TEXTO_CONTENIDO'
--                        → recurso.markdown_content = contenido
--                        → enlace.direccion_almacenamiento = NULL
--
--    - Contenido >= 64KB → tipo_enlace = 'STORAGE'
--                        → recurso.markdown_content = NULL
--                        → enlace.direccion_almacenamiento = 'gs://...'
--
-- 2. Lectura (FASE 0.5):
--    - Si tipo_enlace = 'TEXTO_CONTENIDO' → leer recurso.markdown_content
--    - Si tipo_enlace = 'STORAGE'         → descargar de GCS
--    - Si tipo_enlace = 'ENLACE_EXTERNO'  → redirigir a URL externa
--
-- 3. Default value:
--    - DEFAULT 'STORAGE' mantiene compatibilidad con código existente
--    - Archivos nuevos pueden usar 'TEXTO_CONTENIDO' según tamaño
--
-- 4. NOT NULL:
--    - Mantiene restricción NOT NULL
--    - Todos los enlaces DEBEN tener un tipo definido
--
-- 5. Orden de valores:
--    - El orden en el ENUM no afecta funcionalidad
--    - Se mantiene orden lógico: Storage físico → Metadatos → Contenido
-- =====================================================================

-- =====================================================================
-- ROLLBACK (si es necesario deshacer cambios)
-- =====================================================================
-- ADVERTENCIA: Solo usar si el ENUM anterior era diferente
-- Reemplazar con los valores que tenía ANTES del ALTER TABLE

-- ALTER TABLE enlace 
-- MODIFY COLUMN tipo_enlace ENUM(
--     'STORAGE', 'METADATA', 'THUMBNAIL', 'BACKUP', 'TEMPORAL'
-- ) NOT NULL DEFAULT 'STORAGE';
-- =====================================================================
