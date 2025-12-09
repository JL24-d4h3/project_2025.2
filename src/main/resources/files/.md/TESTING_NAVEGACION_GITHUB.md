# 🧪 Guía de Pruebas - Navegación GitHub-Style

## 📝 Pasos para Probar la Nueva Funcionalidad

### Prerequisito
```bash
# Asegurarse de que la aplicación está corriendo
cd c:\Users\jesus\Desktop\SOLO_TRABAJO\jesusleon
./mvnw.cmd spring-boot:run
```

---

## ✅ Test 1: Navegación Básica por Click en Nombres

### Pasos:
1. Ir a: `http://localhost:8080/devportal/po/mlopez/projects/P-23/files`
2. **Observar:** Lista de carpetas/archivos
3. **Hacer click en el NOMBRE** de una carpeta (ej: "src")
4. **Verificar:** 
   - URL cambia a: `/projects/P-23/files/src`
   - Se muestra el contenido de la carpeta "src"
   - El nombre NO tiene subrayado
   - El nombre NO cambia de color hasta hacer hover

### Resultado Esperado:
```
✅ URL actualizada correctamente
✅ Contenido de carpeta mostrado
✅ Estilo sin subrayado
✅ Hover cambia color a azul
```

---

## ✅ Test 2: Navegación Profunda (Múltiples Niveles)

### Pasos:
1. Desde raíz: `/projects/P-23/files`
2. Click en "src"
3. Click en "main"
4. Click en "java"
5. **Verificar cada nivel:**
   - URL refleja el path completo
   - Contenido correcto en cada nivel

### URLs esperadas:
```
Nivel 1: /projects/P-23/files/src
Nivel 2: /projects/P-23/files/src/main
Nivel 3: /projects/P-23/files/src/main/java
Nivel 4: /projects/P-23/files/src/main/java/com
```

### Resultado Esperado:
```
✅ Cada click actualiza URL correctamente
✅ Navegación fluida entre niveles
✅ Contenido correcto en cada nivel
```

---

## ✅ Test 3: Breadcrumbs Clickeables

### Pasos:
1. Navegar a: `/projects/P-23/files/src/main/java`
2. **Observar breadcrumbs:** `Proyecto > Archivos > src > main > java`
3. Click en breadcrumb "main"
4. **Verificar:** 
   - URL cambia a: `/projects/P-23/files/src/main`
   - Se muestra contenido de "main"

### Resultado Esperado:
```
✅ Breadcrumbs muestran path completo
✅ Cada nivel es clickeable
✅ Click en nivel navega correctamente
```

---

## ✅ Test 4: Botón "Atrás" del Navegador

### Pasos:
1. Navegar: `/files` → `/files/src` → `/files/src/main`
2. Presionar botón "Atrás" del navegador (←)
3. **Verificar:**
   - URL regresa a: `/files/src`
   - Contenido de "src" se muestra
4. Presionar "Atrás" nuevamente
5. **Verificar:**
   - URL regresa a: `/files`
   - Raíz del proyecto se muestra

### Resultado Esperado:
```
✅ Botón "Atrás" funciona correctamente
✅ Historial del navegador preservado
✅ Contenido correcto en cada paso
```

---

## ✅ Test 5: URL Directa (Bookmark / Compartir)

### Pasos:
1. Copiar URL: `http://localhost:8080/devportal/po/mlopez/projects/P-23/files/src/main/java`
2. Abrir nueva pestaña del navegador
3. Pegar URL y presionar Enter
4. **Verificar:**
   - Se carga directamente la carpeta "java"
   - Breadcrumbs correctos
   - Contenido correcto

### Resultado Esperado:
```
✅ URL se puede compartir/guardar
✅ Acceso directo funciona
✅ No requiere navegación secuencial
```

---

## ✅ Test 6: Path No Existente (Error Handling)

### Pasos:
1. Ir a: `http://localhost:8080/devportal/po/mlopez/projects/P-23/files/carpeta_inexistente`
2. **Verificar:**
   - Redirige a: `/projects/P-23/files?error=path-not-found`
   - Muestra mensaje de error (si está configurado)

### Resultado Esperado:
```
✅ Path inválido manejado correctamente
✅ Redirección a raíz con mensaje de error
✅ No se produce crash/500 error
```

---

## ✅ Test 7: Compatibilidad con Navegación Antigua (API REST)

### Pasos:
1. Estar en: `/projects/P-23/files`
2. **Hacer DOBLE CLICK en la FILA** (no el nombre) de una carpeta
3. **Verificar:**
   - Se navega a la carpeta
   - JavaScript `loadFiles(nodoId)` se ejecuta
   - API REST funciona correctamente

### Resultado Esperado:
```
✅ Doble click en fila funciona
✅ Navegación por API REST preservada
✅ Ambos métodos coexisten sin conflicto
```

---

## ✅ Test 8: Click en Archivo (No Carpeta)

### Pasos:
1. Estar en cualquier carpeta con archivos
2. Click en el NOMBRE de un archivo (ej: "Main.java")
3. **Verificar:**
   - Redirige a vista de código: `/files/N-{id}/view`
   - Se muestra el contenido del archivo
   - Editor de código funciona

### Resultado Esperado:
```
✅ Click en archivo abre editor
✅ Redirección correcta
✅ Contenido mostrado correctamente
```

---

## ✅ Test 9: Estilo Visual (GitHub-like)

### Aspectos a Verificar:

#### ✅ Nombres de archivos/carpetas:
- [ ] Sin subrayado en estado normal
- [ ] Color normal (negro/gris oscuro)
- [ ] Cursor cambia a "pointer" al hover
- [ ] Color cambia a azul al hover
- [ ] Sin subrayado incluso al hover

#### ✅ Links visitados:
- [ ] No cambian de color (permanecen igual)
- [ ] No se ven "púrpuras" como links tradicionales

#### ✅ Breadcrumbs:
- [ ] Separador ">" entre niveles
- [ ] Color azul en links
- [ ] Hover con subrayado en breadcrumbs (esto sí)
- [ ] Último nivel (activo) sin link

---

## 🐛 Problemas Comunes y Soluciones

### Problema 1: "Path not found" en todos los paths
**Causa:** La tabla `nodo` no tiene datos o el `fullPath` no se está calculando  
**Solución:**
```sql
-- Verificar que hay nodos en el proyecto
SELECT * FROM nodo WHERE container_type = 'PROYECTO' AND container_id = 23;

-- Verificar que hay una carpeta raíz
SELECT * FROM nodo WHERE parent_id IS NULL AND container_type = 'PROYECTO';
```

### Problema 2: URL no cambia al hacer click
**Causa:** JavaScript está interceptando el click  
**Solución:** Verificar que el `onclick="event.stopPropagation()"` está en el `<a>` tag

### Problema 3: Doble navegación (se navega dos veces)
**Causa:** El click del link Y el click de la fila se están ejecutando  
**Solución:** Ya implementado con `event.stopPropagation()` en el enlace

### Problema 4: Nombres con subrayado
**Causa:** CSS no se aplicó correctamente  
**Solución:** Verificar que `.file-name-link` tiene `text-decoration: none`

---

## 📊 Checklist de Validación Final

Marcar cada test después de ejecutarlo:

- [ ] Test 1: Navegación básica por click ✅
- [ ] Test 2: Navegación profunda ✅
- [ ] Test 3: Breadcrumbs clickeables ✅
- [ ] Test 4: Botón "Atrás" ✅
- [ ] Test 5: URL directa ✅
- [ ] Test 6: Path no existente ✅
- [ ] Test 7: Compatibilidad API REST ✅
- [ ] Test 8: Click en archivo ✅
- [ ] Test 9: Estilo visual ✅

---

## 🔍 Debugging Tips

### Ver logs del servidor:
```bash
# Buscar logs de navegación
grep "PATH-RESOLVER" logs/spring.log

# Buscar logs del controller
grep "Mostrando archivos del proyecto" logs/spring.log
```

### Ver Network en DevTools:
1. Abrir DevTools (F12)
2. Pestaña "Network"
3. Hacer click en carpeta
4. **Verificar:**
   - Request a `/projects/P-23/files/src`
   - Status: 200 OK
   - Response: HTML de la página

### Consola JavaScript:
```javascript
// Ver datos de archivos cargados
console.log(filesData);

// Ver ROL y USERNAME
console.log(ROL, USERNAME);

// Ver PROYECTO_ID
console.log(PROYECTO_ID);
```

---

## 📸 Screenshots de Referencia

### Antes (URLs con IDs):
```
/devportal/po/mlopez/projects/P-23/files/N-145
```

### Ahora (URLs con paths):
```
/devportal/po/mlopez/projects/P-23/files/src/main/java
```

### Comparación Visual:

**GitHub:**
```
github.com/user/repo/tree/main/src/main/java
          │         │         │    └─ path navegable
          │         │         └─── branch
          │         └─────────── repo
          └────────────────────── user
```

**Dev Portal (Ahora):**
```
localhost:8080/devportal/po/mlopez/projects/P-23/files/src/main/java
               │         │  │      │        │    │     └─ path navegable
               │         │  │      │        │    └─── files section
               │         │  │      │        └────── project ID
               │         │  │      └───────────── projects
               │         │  └──────────────────── username
               │         └──────────────────────── rol
               └─────────────────────────────────── portal
```

---

## ✅ Criterios de Aceptación

La funcionalidad se considera **COMPLETA** cuando:

1. ✅ URLs reflejan la estructura de carpetas (legibles)
2. ✅ Nombres de archivos/carpetas son enlaces clickeables
3. ✅ Sin subrayado, sin cambio de color (excepto hover)
4. ✅ Breadcrumbs con URLs dinámicos y clickeables
5. ✅ Botón "Atrás" del navegador funciona
6. ✅ URLs directas/compartibles funcionan
7. ✅ Navegación antigua (API REST) sigue funcionando
8. ✅ Manejo de errores (paths inválidos)
9. ✅ Estilo visual similar a GitHub
10. ✅ Sin errores de compilación ni runtime

---

**Última actualización:** Enero 2025  
**Tester:** Usuario / QA Team  
**Status:** ⏳ Pendiente de pruebas
