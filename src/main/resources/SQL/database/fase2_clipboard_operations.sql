-- ============================================================================
-- FASE 2: Sistema de Clipboard (Copiar/Cortar/Pegar)
-- ============================================================================
-- Fecha: 2025-11-04
-- Descripción: Tabla para trackear operaciones de portapapeles por usuario
-- Permite implementar copy/cut/paste de archivos y carpetas (múltiples a la vez)
-- ============================================================================

-- Tabla de operaciones de portapapeles
CREATE TABLE IF NOT EXISTS clipboard_operation (
    clipboard_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    operation_type ENUM('COPY', 'CUT') NOT NULL,
    nodo_ids JSON NOT NULL COMMENT 'Array de IDs de nodos en el portapapeles',
    source_container_type ENUM('PROYECTO', 'REPOSITORIO', 'PROYECTO_REPOSITORIO') NOT NULL,
    source_container_id BIGINT NOT NULL COMMENT 'ID del proyecto o repositorio de origen',
    source_parent_id BIGINT NULL COMMENT 'ID del nodo padre de origen',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL COMMENT 'Fecha de expiración (24 horas)',
    is_expired BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Claves foráneas
    CONSTRAINT fk_clipboard_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    
    -- Índices para optimizar búsquedas
    INDEX idx_clipboard_usuario (usuario_id),
    INDEX idx_clipboard_created (created_at),
    INDEX idx_clipboard_expired (is_expired, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Operaciones de portapapeles (copy/cut/paste) por usuario - Soporta selección múltiple';

-- ============================================================================
-- Notas de implementación:
-- ============================================================================
-- 1. Solo puede haber UNA operación activa por usuario
-- 2. Al hacer COPY/CUT nueva, se marca como expirada la anterior del mismo usuario
-- 3. Al hacer PASTE con CUT, se marca como expirada (mover solo una vez)
-- 4. Al hacer PASTE con COPY, NO se marca expirada (se puede pegar múltiples veces)
-- 5. operation_type indica si es copia (duplicar) o corte (mover)
-- 6. nodo_ids es un array JSON de IDs para soportar selección múltiple
-- 7. source_parent_id se usa para saber de dónde vienen los archivos
-- 8. expires_at es 24 horas después de created_at
-- 9. is_expired se marca true cuando ya se usó o expiró
-- ============================================================================
