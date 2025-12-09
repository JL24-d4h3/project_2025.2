-- ================================================================================
-- Script para limpiar carpetas huérfanas con parent_id incorrecto
-- ================================================================================
-- PROBLEMA: Carpetas creadas antes del fix de parent_id tienen:
--   - path correcto (ej: /src/main)
--   - parent_id = NULL (incorrecto)
-- Esto causa:
--   1. No aparecen en listados de carpetas hijas
--   2. Bloquean la creación de nuevas carpetas por constraint ux_container_path
-- ================================================================================

-- OPCIÓN 1: Eliminar lógicamente las carpetas huérfanas (RECOMENDADO)
-- Solo marca como borradas, no elimina físicamente
UPDATE nodo
SET is_deleted = 1,
    deleted_at = NOW()
WHERE container_type = 'REPOSITORIO'
  AND container_id = 32
  AND parent_id IS NULL
  AND path LIKE '/src/%'
  AND is_deleted = 0;

-- OPCIÓN 2: Corregir el parent_id de las carpetas huérfanas (ALTERNATIVA)
-- Solo si quieres mantener las carpetas y corregir su parent_id
-- DESCOMENTA ESTO SI PREFIERES CORREGIR EN VEZ DE ELIMINAR:

-- -- Corregir main
-- UPDATE nodo
-- SET parent_id = 58,
--     actualizado_en = NOW(),
--     actualizado_por = 38
-- WHERE nombre = 'main'
--   AND container_type = 'REPOSITORIO'
--   AND container_id = 32
--   AND path = '/src/main'
--   AND parent_id IS NULL
--   AND is_deleted = 0;

-- -- Corregir test
-- UPDATE nodo
-- SET parent_id = 58,
--     actualizado_en = NOW(),
--     actualizado_por = 38
-- WHERE nombre = 'test'
--   AND container_type = 'REPOSITORIO'
--   AND container_id = 32
--   AND path = '/src/test'
--   AND parent_id IS NULL
--   AND is_deleted = 0;

-- ================================================================================
-- VERIFICACIÓN: Listar carpetas afectadas ANTES de ejecutar
-- ================================================================================
SELECT 
    nodo_id,
    nombre,
    tipo,
    parent_id,
    path,
    creado_en,
    is_deleted
FROM nodo
WHERE container_type = 'REPOSITORIO'
  AND container_id = 32
  AND parent_id IS NULL
  AND path LIKE '/src/%'
  AND is_deleted = 0;

-- ================================================================================
-- DESPUÉS DE EJECUTAR: Verificar que ya no hay huérfanas
-- ================================================================================
-- SELECT 
--     nodo_id,
--     nombre,
--     tipo,
--     parent_id,
--     path,
--     is_deleted,
--     deleted_at
-- FROM nodo
-- WHERE container_type = 'REPOSITORIO'
--   AND container_id = 32
--   AND path LIKE '/src/%'
-- ORDER BY path;
