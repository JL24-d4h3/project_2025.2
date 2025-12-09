-- =====================================================
-- Script para agregar Foreign Keys a campos de auditoría
-- Estandariza campos creado_por, actualizado_por para referenciar usuario(usuario_id)
-- =====================================================

USE `database`;

-- =====================================================
-- 1. API - agregar FKs para creado_por y actualizado_por
-- =====================================================
ALTER TABLE `api`
  ADD CONSTRAINT `fk_api_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_api_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- =====================================================
-- 2. DOCUMENTACION - agregar FKs para creado_por y actualizado_por
-- =====================================================
ALTER TABLE `documentacion`
  ADD CONSTRAINT `fk_documentacion_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_documentacion_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- =====================================================
-- 3. VERSION_API - agregar FKs para creado_por y actualizado_por
-- NOTA: actualizado_por es BIGINT (no BIGINT UNSIGNED) - inconsistencia
-- =====================================================
ALTER TABLE `version_api`
  ADD CONSTRAINT `fk_version_api_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_version_api_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- =====================================================
-- 4. PROYECTO - agregar FKs para created_by y updated_by (inglés)
-- =====================================================
ALTER TABLE `proyecto`
  ADD CONSTRAINT `fk_proyecto_created_by`
    FOREIGN KEY (`created_by`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_proyecto_updated_by`
    FOREIGN KEY (`updated_by`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- =====================================================
-- 5. REPOSITORIO - agregar FKs para creado_por_usuario_id y actualizado_por_usuario_id
-- =====================================================
ALTER TABLE `repositorio`
  ADD CONSTRAINT `fk_repositorio_creado_por`
    FOREIGN KEY (`creado_por_usuario_id`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_repositorio_actualizado_por`
    FOREIGN KEY (`actualizado_por_usuario_id`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- =====================================================
-- 6. NODO - agregar FKs para creado_por y actualizado_por
-- NOTA: actualizado_por es BIGINT (no BIGINT UNSIGNED) - inconsistencia
-- =====================================================
ALTER TABLE `nodo`
  ADD CONSTRAINT `fk_nodo_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_nodo_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- =====================================================
-- 7. VERSION_ARCHIVO - agregar FKs para creado_por y actualizado_por
-- =====================================================
ALTER TABLE `version_archivo`
  ADD CONSTRAINT `fk_version_archivo_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_version_archivo_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- =====================================================
-- 8. PERMISO_NODO - agregar FKs para creado_por y actualizado_por
-- =====================================================
ALTER TABLE `permiso_nodo`
  ADD CONSTRAINT `fk_permiso_nodo_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_permiso_nodo_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- =====================================================
-- 9. ROL_PROYECTO - agregar FKs para creado_por y actualizado_por
-- =====================================================
ALTER TABLE `rol_proyecto`
  ADD CONSTRAINT `fk_rol_proyecto_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_rol_proyecto_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- =====================================================
-- NOTAS IMPORTANTES:
-- =====================================================
-- 1. Todas las FKs usan ON DELETE SET NULL porque los campos de auditoría son NULL
-- 2. ON UPDATE CASCADE permite actualizar el usuario_id si cambia
-- 3. Hay inconsistencias de tipo:
--    - version_api.actualizado_por es BIGINT (debería ser BIGINT UNSIGNED)
--    - nodo.actualizado_por es BIGINT (debería ser BIGINT UNSIGNED)
-- 4. Hay inconsistencias de nomenclatura:
--    - proyecto usa inglés (created_by, updated_by)
--    - resto usa español (creado_por, actualizado_por)
--    - repositorio usa nombres largos (creado_por_usuario_id)
-- 
-- RECOMENDACIÓN: Estandarizar todos a español (creado_por, actualizado_por)
-- y todos como BIGINT UNSIGNED para consistencia
-- =====================================================
