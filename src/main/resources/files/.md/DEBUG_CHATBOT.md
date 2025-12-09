# 🐛 DEBUG CHATBOT - Error 500

## 📊 Problema Identificado

### Error en Consola:
```
Usuario: usuario          ❌ 
Email: email@example.com  ❌ 
Rol: Usuario             ❌ 
UserID: 0                ❌ 
HTTP 500 del servidor
```

---

## 🔍 Causa Raíz

**Problema 1: Variables Thymeleaf no se evalúan**
- Las variables están mostrando valores por defecto
- Esto significa que `userId = 0` se envía al servidor Python

**Problema 2: Python rechaza `userId = 0`**
- En `tools.py` línea 54: `if not userId: return {"success": False, "error": "Usuario no autenticado"}`
- Cuando `userId = 0`, Python lo trata como `False` → retorna error
- Esto puede causar el error 500

---

## ✅ Soluciones

### Solución 1: Verificar logs del servidor (URGENTE)

**En PowerShell:**
```powershell
gcloud auth login
gcloud config set project api-sandbox-476603
gcloud run services logs read chatbot-vertex --region us-central1 --limit 30 --format="table(timestamp,severity,textPayload)"
```

**Buscar:**
- Errores de Python
- Traceback completo
- Mensaje específico del error 500

---

### Solución 2: Agregar logs al HTML (TEMPORAL)

Agregar después de la línea 345 en `chatbot-test.html`:

```javascript
// DEBUG: Verificar si las variables se evaluaron
if (userId === 0 || email === 'email@example.com') {
    console.error('❌ THYMELEAF NO EVALUÓ LAS VARIABLES');
    console.error('Esto significa que hay un problema con el Model del controlador');
    alert('ERROR: Las variables del usuario no se cargaron. Verifica que estés autenticado.');
}
```

---

### Solución 3: Verificar autenticación del usuario

**En la URL del navegador:**
- ¿Estás realmente logueado?
- ¿La URL es `/devportal/po/mlopez/chatbot-test`?
- ¿O estás accediendo sin autenticación?

**Probar:**
1. Cierra sesión: `https://teldev.pro/signout`
2. Inicia sesión: `https://teldev.pro/signin`
   - Username: `mlopez`
   - Password: (la contraseña que hayas configurado)
3. Navega a: `https://teldev.pro/devportal/po/mlopez/chatbot-test`

---

### Solución 4: Modificar Python para aceptar userId = 0 (TEMPORAL)

**Cambiar en `tools.py` línea 54:**

```python
# ANTES:
if not userId:
    return {"success": False, "error": "Usuario no autenticado"}

# DESPUÉS:
if userId is None or userId < 0:
    return {"success": False, "error": "Usuario no autenticado"}
```

Esto permitirá que `userId = 0` pase, aunque técnicamente no es válido.

---

### Solución 5: Hardcodear userId para testing (TEMPORAL)

**En `chatbot-test.html` línea 345:**

```javascript
// TEMPORAL: Hardcodear userId para testing
const userId = /*[[${usuario.usuarioId}]]*/ 38; // ← Cambiar 0 por 38 (ID de mlopez)
```

Esto al menos permitirá probar el chatbot mientras arreglamos Thymeleaf.

---

## 🎯 Plan de Acción

### Paso 1: Verificar autenticación
```
1. Abre incógnito
2. Ve a https://teldev.pro/signin
3. Login con mlopez
4. Ve a https://teldev.pro/devportal/po/mlopez/chatbot-test
5. Abre consola → ¿userId sigue siendo 0?
```

### Paso 2: Si userId sigue siendo 0
```
→ El problema es el controlador Java no está pasando el objeto 'usuario' correctamente
→ Revisar logs de Spring Boot
→ Verificar que userService.buscarPorUsername() retorna un objeto válido
```

### Paso 3: Ver logs de Python
```bash
gcloud run services logs read chatbot-vertex --region us-central1 --limit 50
```

**Buscar líneas con:**
- `❌ Error`
- `Traceback`
- `Exception`

---

## 🚨 Verificación Rápida

**Ejecuta esto en la consola del navegador (en la página del chatbot):**

```javascript
console.log('=== DEBUG VARIABLES ===');
console.log('username:', username);
console.log('email:', email);
console.log('userId:', userId);
console.log('nombreCompleto:', nombreCompleto);
console.log('rol:', rol);
console.log('======================');

// Verificar si Thymeleaf funcionó
if (userId === 0) {
    console.error('🔴 PROBLEMA: userId es 0 - Thymeleaf no evaluó las variables');
    console.error('Posibles causas:');
    console.error('1. No estás autenticado');
    console.error('2. El controlador no pasó el objeto usuario');
    console.error('3. Hay un error en la sintaxis de Thymeleaf');
} else {
    console.log('✅ userId válido:', userId);
}
```

---

## 📝 Próximos Pasos

1. ✅ Actualizar `chatbot-test.html` con sintaxis correcta de Thymeleaf (YA HECHO)
2. 🔄 Reiniciar servidor Spring Boot
3. 🔄 Recargar página (Ctrl+Shift+R para hard refresh)
4. 📊 Verificar logs de Python
5. 🐛 Agregar más logs si es necesario

---

**Fecha:** 6 de Noviembre, 2025  
**Estado:** 🔴 Error 500 - Investigando
