# ⚖️ COMPARACIÓN: triggers_root_node.sql vs fase0_file_system_enhancements.sql

**Fecha**: 3 de Noviembre, 2025

---

## 📂 ARCHIVO 1: `triggers_root_node.sql` (Creado hoy por el asistente)

**Ubicación**: `src/main/resources/SQL/database/triggers_root_node.sql`

### Contenido:
✅ **4 Triggers**:
1. `after_proyecto_insert_create_root_node` - Crear nodo raíz al insertar proyecto
2. `after_repositorio_insert_create_root_node` - Crear nodo raíz al insertar repositorio
3. `after_nodo_update_parent_size` - Actualizar tamaño de carpeta padre
4. `after_nodo_soft_delete_cascade` - Eliminar en cascada (soft delete)

### Propósito:
Crear automáticamente el nodo raíz "/" cuando se crea un proyecto o repositorio.

### Tamaño:
~200 líneas

---

## 📂 ARCHIVO 2: `fase0_file_system_enhancements.sql` (Creado previamente)

**Ubicación**: `src/main/resources/SQL/database/fase0_file_system_enhancements.sql`

### Contenido COMPLETO:

#### ✅ **6 Tablas nuevas**:
1. `clipboard_operation` - Copiar/cortar/pegar
2. `file_operation_job` - Trabajos asíncronos (comprimir, descargar múltiples)
3. `github_integration` - Integración con GitHub
4. `github_user_token` - Tokens OAuth de GitHub
5. `github_sync_log` - Historial de sincronizaciones
6. `nodo_share_link` - Enlaces públicos para compartir archivos
7. `nodo_favorite` - Favoritos de usuarios

#### ✅ **2 Triggers** (iguales a los míos):
1. `trg_proyecto_create_root_node`
2. `trg_repositorio_create_root_node`

#### ✅ **5 Procedimientos almacenados**:
1. `sp_get_nodo_full_path` - Obtener ruta completa de un nodo
2. `sp_move_nodo` - Mover nodo a otra carpeta (actualiza paths)
3. `sp_delete_nodo_soft` - Soft delete en cascada
4. `sp_restore_nodo` - Restaurar nodo eliminado
5. `sp_get_nodo_size_recursive` - Calcular tamaño de carpeta

#### ✅ **3 Eventos programados**:
1. `evt_cleanup_expired_clipboard` - Limpiar clipboard expirado (cada 1 hora)
2. `evt_cleanup_old_jobs` - Eliminar jobs antiguos (cada 1 día)
3. `evt_cleanup_old_sync_logs` - Eliminar logs viejos (cada 1 semana)

#### ✅ **4 Índices adicionales**:
1. `idx_nodo_nombre_tipo` - Búsqueda por nombre
2. `idx_nodo_mime` - Búsqueda por MIME type
3. `idx_nodo_created` - Ordenar por fecha de creación
4. `idx_nodo_size` - Ordenar por tamaño

#### ✅ **1 Vista**:
1. `v_nodos_with_full_info` - Vista completa de nodos con info enriquecida

### Propósito:
Setup COMPLETO de la base de datos para el sistema de archivos, incluyendo triggers, tablas auxiliares, procedimientos y optimizaciones.

### Tamaño:
~850 líneas

---

## 🎯 VEREDICTO

### ✅ **USA: `fase0_file_system_enhancements.sql`**

**Razones**:
1. ✅ Incluye los mismos triggers que `triggers_root_node.sql` (pero con nombres diferentes)
2. ✅ Crea las 7 tablas que **YA ESTÁN EN TU CÓDIGO JAVA** (entidades JPA)
3. ✅ Procedimientos almacenados muy útiles (mover archivos, obtener rutas, etc.)
4. ✅ Eventos programados para limpieza automática
5. ✅ Índices optimizados para búsquedas rápidas
6. ✅ Vista SQL para consultas complejas

### ❌ **IGNORA: `triggers_root_node.sql`**

**Razones**:
1. ❌ Redundante (los triggers ya están en `fase0`)
2. ❌ Menos completo
3. ❌ No crea las tablas necesarias

---

## 📊 TABLA COMPARATIVA

| Característica | triggers_root_node.sql | fase0_file_system_enhancements.sql |
|----------------|------------------------|-------------------------------------|
| **Triggers para root_node** | ✅ 2 triggers | ✅ 2 triggers (iguales) |
| **Triggers para tamaño** | ✅ 1 trigger | ❌ No |
| **Triggers soft delete** | ✅ 1 trigger | ❌ No |
| **Tablas nuevas** | ❌ 0 | ✅ 7 tablas |
| **Procedimientos almacenados** | ❌ 0 | ✅ 5 procedimientos |
| **Eventos programados** | ❌ 0 | ✅ 3 eventos |
| **Índices adicionales** | ❌ 0 | ✅ 4 índices |
| **Vistas** | ❌ 0 | ✅ 1 vista |
| **Tamaño** | ~200 líneas | ~850 líneas |
| **Completitud** | 40% | 100% ✅ |

---

## 🔄 DIFERENCIAS EN TRIGGERS

### Nombres diferentes pero hacen LO MISMO:

#### `triggers_root_node.sql`:
```sql
CREATE TRIGGER after_proyecto_insert_create_root_node
AFTER INSERT ON proyecto
FOR EACH ROW
BEGIN
    -- Crear nodo raíz...
END;
```

#### `fase0_file_system_enhancements.sql`:
```sql
CREATE TRIGGER trg_proyecto_create_root_node
AFTER INSERT ON proyecto
FOR EACH ROW
BEGIN
    -- Crear nodo raíz... (MISMO CÓDIGO)
END;
```

**Solo cambian los nombres**:
- `after_proyecto_insert_create_root_node` → `trg_proyecto_create_root_node`
- `after_repositorio_insert_create_root_node` → `trg_repositorio_create_root_node`

---

## ⚠️ TRIGGERS EXCLUSIVOS de `triggers_root_node.sql`

Estos 2 triggers SÍ son exclusivos y NO están en `fase0`:

### 1. `after_nodo_update_parent_size`
**Propósito**: Actualizar el tamaño de la carpeta padre cuando cambia el tamaño de un hijo.

**Útil**: ✅ Sí, para mostrar tamaño total de carpetas.

**Solución**: Podrías agregarlo manualmente a `fase0` o implementarlo en Java.

### 2. `after_nodo_soft_delete_cascade`
**Propósito**: Soft delete en cascada (marcar hijos como eliminados).

**Útil**: ✅ Sí, para eliminar carpetas completas.

**Solución**: El `fase0` tiene `sp_delete_nodo_soft` (procedimiento almacenado) que hace lo mismo.

---

## ✅ RECOMENDACIÓN FINAL

### PASO 1: Ejecuta `fase0_file_system_enhancements.sql`
```sql
-- Desde MySQL Workbench:
File → Run SQL Script → Seleccionar fase0_file_system_enhancements.sql
```

### PASO 2 (OPCIONAL): Agregar triggers de tamaño y soft delete

Si quieres los triggers adicionales de `triggers_root_node.sql`, ejecuta solo estas secciones:

```sql
DELIMITER $$

-- Trigger: Actualizar tamaño de carpeta padre
CREATE TRIGGER after_nodo_update_parent_size
AFTER UPDATE ON nodo
FOR EACH ROW
BEGIN
    DECLARE parent_total_size BIGINT DEFAULT 0;
    
    IF (NEW.size_bytes != OLD.size_bytes) AND (NEW.parent_id IS NOT NULL) THEN
        SELECT COALESCE(SUM(size_bytes), 0) INTO parent_total_size
        FROM nodo
        WHERE parent_id = NEW.parent_id AND is_deleted = 0;
        
        UPDATE nodo
        SET size_bytes = parent_total_size, updated_at = NOW()
        WHERE nodo_id = NEW.parent_id;
    END IF;
END$$

-- Trigger: Soft delete en cascada
CREATE TRIGGER after_nodo_soft_delete_cascade
AFTER UPDATE ON nodo
FOR EACH ROW
BEGIN
    IF (NEW.is_deleted = 1) AND (OLD.is_deleted = 0) THEN
        UPDATE nodo
        SET is_deleted = 1, deleted_at = NEW.deleted_at, updated_at = NOW()
        WHERE parent_id = NEW.nodo_id AND is_deleted = 0;
    END IF;
    
    IF (NEW.is_deleted = 0) AND (OLD.is_deleted = 1) THEN
        UPDATE nodo
        SET is_deleted = 0, deleted_at = NULL, updated_at = NOW()
        WHERE parent_id = NEW.nodo_id AND is_deleted = 1;
    END IF;
END$$

DELIMITER ;
```

---

## 🗑️ PUEDES ELIMINAR

El archivo `triggers_root_node.sql` es redundante. Puedes:

```bash
# Eliminar el archivo (opcional):
rm src/main/resources/SQL/database/triggers_root_node.sql
```

O simplemente **ignorarlo** y usar solo `fase0_file_system_enhancements.sql`.

---

## 📝 RESUMEN

- ✅ **USA**: `fase0_file_system_enhancements.sql` (COMPLETO)
- ❌ **IGNORA**: `triggers_root_node.sql` (redundante)
- 🔧 **OPCIONAL**: Agregar los 2 triggers extra de tamaño y soft delete
