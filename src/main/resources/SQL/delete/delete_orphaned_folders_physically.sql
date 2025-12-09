-- ================================================================================
-- SCRIPT: ELIMINACIÓN FÍSICA de carpetas huérfanas
-- ================================================================================
-- PROBLEMA CRÍTICO:
--   El constraint UNIQUE 'ux_container_path' valida TODOS los registros,
--   incluyendo los que tienen is_deleted=1
--
--   Por eso, aunque hayamos hecho UPDATE SET is_deleted=1,
--   el constraint sigue bloqueando nuevas inserciones con el mismo path.
--
-- SOLUCIÓN:
--   ELIMINAR FÍSICAMENTE (DELETE) los registros huérfanos que ya fueron
--   marcados como is_deleted=1
-- ================================================================================

-- PASO 1: Verificar qué carpetas serán eliminadas
SELECT 
    nodo_id,
    nombre,
    tipo,
    parent_id,
    path,
    is_deleted,
    deleted_at,
    creado_en
FROM nodo
WHERE container_type = 'REPOSITORIO'
  AND container_id = 32
  AND parent_id IS NULL
  AND path LIKE '/src/%'
  AND is_deleted = 1;

-- PASO 2: ELIMINAR FÍSICAMENTE los registros huérfanos
-- ⚠️ ESTA ACCIÓN ES IRREVERSIBLE ⚠️
DELETE FROM nodo
WHERE container_type = 'REPOSITORIO'
  AND container_id = 32
  AND parent_id IS NULL
  AND path LIKE '/src/%'
  AND is_deleted = 1;

-- PASO 3: Verificar que ya no existen registros duplicados
SELECT 
    nodo_id,
    nombre,
    tipo,
    parent_id,
    path,
    is_deleted,
    creado_en
FROM nodo
WHERE container_type = 'REPOSITORIO'
  AND container_id = 32
  AND path LIKE '/src/%'
ORDER BY path, nodo_id;

-- ================================================================================
-- RESULTADO ESPERADO:
-- - Registros eliminados: main, test (con is_deleted=1 y parent_id=NULL)
-- - Registros conservados: dockerfile, gitignore, java (con parent_id=58)
-- - Ahora podrás crear nuevas carpetas 'main' y 'test' sin errores
-- ================================================================================
