# 🔧 CORRECCIONES CRÍTICAS: NodoService.java

## Resumen de Problemas

1. **eliminarNodo()**: Solo hace soft delete en BD, NO elimina de GCS
2. **renombrarNodo()**: Solo cambia nombre en BD, NO mueve archivo en GCS  
3. **moverNodo()**: Solo mueve en BD, NO actualiza gcsPath ni mueve en GCS

---

## ✅ CORRECCIÓN 1: eliminarNodo()

**Reemplazar desde línea ~293 hasta ~305**

```java
/**
 * Elimina un nodo de forma lógica usando el stored procedure sp_delete_nodo_soft
 * 🔧 FASE 7.1: SINCRONIZACIÓN CON GCS
 * - Elimina archivo físico de GCS ANTES de marcar como eliminado en BD
 * - Si falla eliminación de GCS, no procede con eliminación en BD
 * - Para carpetas, elimina recursivamente todos los archivos hijos
 * 
 * @param nodoId ID del nodo a eliminar
 * @param usuarioId ID del usuario que realiza la eliminación
 * @return true si la eliminación fue exitosa
 */
@Transactional
@CacheEvict(value = {"nodosRaiz", "nodosHijos", "jerarquiasNodos"}, allEntries = true)
public boolean eliminarNodo(Long nodoId, Long usuarioId) {
    logger.info("🗑️ [DELETE] Iniciando eliminación de nodo ID: {}", nodoId);
    
    // 1. Obtener nodo ANTES de eliminarlo para acceder a gcsPath
    Nodo nodo = obtenerPorId(nodoId)
            .orElseThrow(() -> new IllegalArgumentException("Nodo no encontrado: " + nodoId));
    
    logger.info("   📄 Nodo a eliminar: '{}' (Tipo: {}, GCS: {})", 
        nodo.getNombre(), nodo.getTipo(), nodo.getGcsPath());
    
    // 2. Si es archivo, eliminar de GCS PRIMERO
    if (nodo.getTipo() == Nodo.TipoNodo.ARCHIVO && nodo.getGcsPath() != null) {
        try {
            logger.info("   🔥 Eliminando archivo de GCS: {}", nodo.getGcsPath());
            boolean eliminadoGCS = fileStorageService.eliminarArchivoDeGCS(nodo.getGcsPath());
            
            if (eliminadoGCS) {
                logger.info("   ✅ Archivo eliminado de GCS exitosamente");
            } else {
                logger.warn("   ⚠️ Archivo no encontrado en GCS (puede haber sido eliminado previamente): {}", 
                    nodo.getGcsPath());
                // Continuar con eliminación de BD de todos modos
            }
        } catch (Exception e) {
            logger.error("   ❌ Error al eliminar archivo de GCS: {}", e.getMessage(), e);
            throw new RuntimeException("Error al eliminar archivo de almacenamiento: " + e.getMessage(), e);
        }
    } else if (nodo.getTipo() == Nodo.TipoNodo.CARPETA) {
        logger.info("   📁 Es carpeta, eliminando archivos hijos recursivamente de GCS...");
        eliminarArchivosHijosDeGCS(nodoId);
    }
    
    // 3. Hacer soft delete en BD usando stored procedure
    logger.info("   💾 Marcando nodo como eliminado en BD...");
    StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_delete_nodo_soft")
            .registerStoredProcedureParameter("p_nodo_id", Long.class, jakarta.persistence.ParameterMode.IN)
            .registerStoredProcedureParameter("p_usuario_id", Long.class, jakarta.persistence.ParameterMode.IN)
            .setParameter("p_nodo_id", nodoId)
            .setParameter("p_usuario_id", usuarioId);

    try {
        query.execute();
        entityManager.clear();
        logger.info("   ✅ Nodo eliminado exitosamente de BD");
        logger.info("🗑️ [DELETE] Proceso completado para nodo ID: {}", nodoId);
        return true;
    } catch (Exception e) {
        logger.error("   ❌ Error al eliminar nodo de BD: {}", e.getMessage(), e);
        throw new RuntimeException("Error al eliminar el nodo: " + e.getMessage(), e);
    }
}

/**
 * Elimina recursivamente todos los archivos hijos de una carpeta desde GCS
 * @param carpetaId ID de la carpeta padre
 */
private void eliminarArchivosHijosDeGCS(Long carpetaId) {
    List<Nodo> hijos = nodoRepository.findByParentIdAndIsDeletedFalseOrderByTipoDescNombreAsc(carpetaId);
    
    for (Nodo hijo : hijos) {
        if (hijo.getTipo() == Nodo.TipoNodo.ARCHIVO && hijo.getGcsPath() != null) {
            try {
                logger.info("      🔥 Eliminando archivo hijo de GCS: {}", hijo.getGcsPath());
                fileStorageService.eliminarArchivoDeGCS(hijo.getGcsPath());
            } catch (Exception e) {
                logger.warn("      ⚠️ Error al eliminar archivo hijo de GCS (continuando): {}", e.getMessage());
            }
        } else if (hijo.getTipo() == Nodo.TipoNodo.CARPETA) {
            // Recursión para subcarpetas
            eliminarArchivosHijosDeGCS(hijo.getNodoId());
        }
    }
}
```

---

## PASOS PARA APLICAR LAS CORRECCIONES

Como las correcciones son extensas y pueden causar problemas si se hacen manualmente, te recomiendo:

### Opción 1: Aplicar correcciones manualmente (RECOMENDADO)

1. Abre `NodoService.java`
2. Busca el método `eliminarNodo` (línea ~293)
3. Reemplaza TODO el método con el código de CORRECCIÓN 1 de arriba
4. Agrega el método helper `eliminarArchivosHijosDeGCS` después del método `eliminarNodo`
5. Repite para los otros dos métodos (`renombrarNodo` y `moverNodo`)

### Opción 2: Usar git apply (si tienes un archivo patch)

Puedo generar archivos separados con cada corrección que puedes aplicar una por una.

---

## TESTING REQUERIDO

Después de aplicar las correcciones, debes probar:

1. **Eliminar un archivo** → Verificar que desaparece tanto de BD como de GCS
2. **Eliminar una carpeta con archivos** → Verificar que todos los archivos se eliminan de GCS
3. **Renombrar un archivo** → Verificar que el archivo se mueve en GCS con el nuevo nombre
4. **Renombrar una carpeta** → Verificar que todos los archivos hijos se mueven en GCS
5. **Mover un archivo a otra carpeta** → Verificar que el archivo cambia de ubicación en GCS
6. **Mover una carpeta** → Verificar que todos los archivos hijos se mueven en GCS

---

## PRÓXIMO PASO

¿Quieres que:
A) Te genere los 3 archivos de corrección completos (para que hagas copy-paste)?
B) Intente hacer las correcciones automáticamente una por una (con más cuidado)?
C) Te ayude a hacerlo manualmente paso a paso?

