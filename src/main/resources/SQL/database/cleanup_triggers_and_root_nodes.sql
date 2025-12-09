-- =====================================================
-- SCRIPT DE LIMPIEZA: Eliminar triggers y nodos raíz
-- =====================================================
-- Ejecutar SOLO UNA VEZ en MySQL
-- =====================================================

USE `dev_portal_sql`;

-- 1. Eliminar triggers que crean nodos raíz automáticamente
DROP TRIGGER IF EXISTS `trg_proyecto_create_root_node`;
DROP TRIGGER IF EXISTS `trg_repositorio_create_root_node`;
DROP TRIGGER IF EXISTS `after_proyecto_insert_create_root_node`;
DROP TRIGGER IF EXISTS `after_repositorio_insert_create_root_node`;

-- 2. Ver cuántos nodos "/" existen
SELECT 
    '========== NODOS RAÍZ EXISTENTES ==========' AS info;

SELECT 
    nodo_id, 
    nombre, 
    container_type, 
    container_id,
    (SELECT COUNT(*) FROM nodo n2 WHERE n2.parent_id = nodo.nodo_id AND n2.is_deleted = 0) as cantidad_hijos
FROM nodo 
WHERE nombre = '/' 
  AND parent_id IS NULL 
  AND is_deleted = 0
ORDER BY container_type, container_id;

-- 3. Eliminar SOLO los nodos "/" que NO tienen hijos
-- Usar tabla temporal para evitar error "can't specify target table for update in FROM clause"
CREATE TEMPORARY TABLE nodos_a_eliminar AS
SELECT nodo_id 
FROM nodo 
WHERE nombre = '/' 
  AND parent_id IS NULL 
  AND is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM nodo n2 
      WHERE n2.parent_id = nodo.nodo_id 
        AND n2.is_deleted = 0
  );

DELETE FROM nodo 
WHERE nodo_id IN (SELECT nodo_id FROM nodos_a_eliminar);

DROP TEMPORARY TABLE nodos_a_eliminar;

SELECT 
    '========== NODOS "/" ELIMINADOS ==========' AS info,
    ROW_COUNT() AS cantidad_eliminada;

-- 4. Limpiar referencias en proyectos
-- Usar tabla temporal para evitar error de MySQL
CREATE TEMPORARY TABLE proyectos_a_actualizar AS
SELECT proyecto_id 
FROM proyecto p
WHERE root_node_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM nodo n 
      WHERE n.nodo_id = p.root_node_id 
        AND n.is_deleted = 0
  );

UPDATE proyecto 
SET root_node_id = NULL
WHERE proyecto_id IN (SELECT proyecto_id FROM proyectos_a_actualizar);

DROP TEMPORARY TABLE proyectos_a_actualizar;

SELECT 
    '========== PROYECTOS ACTUALIZADOS ==========' AS info,
    ROW_COUNT() AS cantidad_actualizada;

-- 5. Limpiar referencias en repositorios
-- Usar tabla temporal para evitar error de MySQL
CREATE TEMPORARY TABLE repositorios_a_actualizar AS
SELECT repositorio_id 
FROM repositorio r
WHERE root_node_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM nodo n 
      WHERE n.nodo_id = r.root_node_id 
        AND n.is_deleted = 0
  );

UPDATE repositorio 
SET root_node_id = NULL
WHERE repositorio_id IN (SELECT repositorio_id FROM repositorios_a_actualizar);

DROP TEMPORARY TABLE repositorios_a_actualizar;

SELECT 
    '========== REPOSITORIOS ACTUALIZADOS ==========' AS info,
    ROW_COUNT() AS cantidad_actualizada;

-- 6. Verificar que se eliminaron los triggers
SELECT 
    '========== VERIFICACIÓN FINAL ==========' AS info;

SHOW TRIGGERS WHERE `Table` IN ('proyecto', 'repositorio');

SELECT 
    '✅ LIMPIEZA COMPLETADA' AS resultado;
