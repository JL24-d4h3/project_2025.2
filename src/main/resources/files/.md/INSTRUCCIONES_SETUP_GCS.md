# 🔧 INSTRUCCIONES: Configurar Google Cloud Storage para File System

**Fecha**: 3 de Noviembre, 2025  
**Objetivo**: Crear bucket y service account propios para el sistema de archivos

---

## 📋 CONTEXTO

Actualmente hay **DOS configuraciones de GCS**:

### 1️⃣ Configuración de APIs (tu compañero)
- **Bucket**: `devportal-api-contracts-noha`
- **Service Account**: `devportal-storage-service@devportal-storage.iam.gserviceaccount.com`
- **Clave JSON**: `devportal-storage-key.json` (ya existe en `src/main/resources/`)
- **Propósito**: Almacenar contratos de APIs

### 2️⃣ Configuración File System (TÚ)
- **Bucket**: `dev-portal-storage` ✅ **YA EXISTE** (creado previamente)
- **Service Account**: ✅ **YA EXISTE** (`dev-portal-storage-manager`)
- **Email SA**: `id-dev-portal-storage-manager@dev-portal-gtics.iam.gserviceaccount.com`
- **Rol**: Administrador de objetos de Storage ✅
- **Clave JSON**: ⚠️ **DEBES DESCARGARLA** (la clave existente no se puede recuperar)
- **Propósito**: Sistema de archivos (proyectos y repositorios)

---

## ✅ TU SITUACIÓN ACTUAL (Service Account ya existe)

**¡Buenas noticias!** Ya tienes configurado:
- ✅ Service Account: `dev-portal-storage-manager`
- ✅ Email: `id-dev-portal-storage-manager@dev-portal-gtics.iam.gserviceaccount.com`
- ✅ Rol: Administrador de objetos de Storage
- ✅ Clave activa: `a97ec4f7a26beb01cd180e18b8d1b2569d22583c` (30 sept 2025)

**Problema**: La clave JSON no fue descargada cuando se creó.

**Solución**: Crear una nueva clave JSON para el mismo service account.

---

## 🚀 PASOS SIMPLIFICADOS (Para ti)

### Paso 1: Descargar clave JSON del Service Account existente

1. **Ir a Service Accounts**:
   - https://console.cloud.google.com/iam-admin/serviceaccounts?project=dev-portal-gtics

2. **Click en** el service account: `dev-portal-storage-manager`

3. **Tab "CLAVES" (KEYS)**

4. **AGREGAR CLAVE** → **Crear clave nueva**

5. **Tipo**: JSON

6. **CREAR**

7. Se descargará: `dev-portal-gtics-XXXXXXXXXXXXXX.json`

8. **Renombrar** el archivo a:
   ```
   dev-portal-storage-manager-key.json
   ```

9. **Mover** a:
   ```
   c:\Users\jesus\Desktop\SOLO_TRABAJO\teldev\src\main\resources\dev-portal-storage-manager-key.json
   ```

### Paso 2: Verificar configuración

El archivo `application.properties` ya fue actualizado para usar:
```properties
gcp.credentials-path=classpath:dev-portal-storage-manager-key.json
```

### Paso 3: Compilar proyecto

```bash
cd c:\Users\jesus\Desktop\SOLO_TRABAJO\teldev
mvn clean compile
```

Si compila sin errores, ¡estás listo! ✅

---

## ⚠️ NOTA IMPORTANTE: No necesitas crear nada nuevo

**NO HAGAS** las siguientes secciones (ya las tienes configuradas):
- ❌ ~~OPCIÓN 1: Reutilizar Service Account~~
- ❌ ~~OPCIÓN 2: Crear Service Account NUEVO~~

**Solo necesitas**:
1. Descargar la clave JSON (Paso 1)
2. Colocarla en `src/main/resources/`
3. Compilar

---

## 🎯 OPCIÓN 1: Reutilizar Service Account existente (Rápido pero NO recomendado)

Si quieres **probar rápidamente** sin crear nada nuevo:

### Paso 1: Crear solo el bucket
```bash
# En Google Cloud Console:
# 1. Ir a: https://console.cloud.google.com/storage
# 2. Cambiar a proyecto: devportal-storage
# 3. Click "CREATE BUCKET"
# 4. Nombre: dev-portal-storage
# 5. Location: us-east1 (Carolina del Sur)
# 6. Storage class: Standard
# 7. Access control: Uniform
# 8. Versioning: Enable object versioning ✅
# 9. Soft delete: 7 días
# 10. CREATE
```

### Paso 2: Dar permisos al service account existente
```bash
# En Cloud Shell o terminal local con gcloud:
gsutil iam ch serviceAccount:devportal-storage-service@devportal-storage.iam.gserviceaccount.com:objectAdmin gs://dev-portal-storage
```

### Paso 3: Listo
El código ya está configurado para usar `devportal-storage-key.json`.

**⚠️ DESVENTAJA**: Mezclas APIs y File System en el mismo service account (menos seguro).

---

## ✅ OPCIÓN 2: Crear Service Account NUEVO (RECOMENDADO)

### Paso 1: Crear el bucket

Igual que en Opción 1 (arriba).

### Paso 2: Crear Service Account

1. **Ir a IAM & Admin** → Service Accounts
   - URL: https://console.cloud.google.com/iam-admin/serviceaccounts?project=devportal-storage

2. **Click "CREATE SERVICE ACCOUNT"**

3. **Rellenar formulario**:
   - **Service account name**: `teldev-filesystem-sa`
   - **Service account ID**: `teldev-filesystem-sa` (se auto-genera)
   - **Description**: `Service account para sistema de archivos de TelDev (proyectos y repositorios)`
   - Click **CREATE AND CONTINUE**

4. **Grant permissions** (Step 2):
   - **Role**: `Storage Object Admin`
   - Click **CONTINUE**

5. **Grant users access** (Step 3):
   - Déjalo vacío (opcional)
   - Click **DONE**

### Paso 3: Generar clave JSON

1. **En la lista de service accounts**, click en `teldev-filesystem-sa`

2. **Tab "KEYS"** → **ADD KEY** → **Create new key**

3. **Key type**: JSON

4. **CREATE**

5. Se descargará un archivo: `devportal-storage-XXXXXXXXXXXX.json`

6. **Renombra** el archivo a: `teldev-filesystem-key.json`

7. **Mueve** el archivo a:
   ```
   c:\Users\jesus\Desktop\SOLO_TRABAJO\teldev\src\main\resources\teldev-filesystem-key.json
   ```

### Paso 4: Actualizar application.properties

Abre `src/main/resources/application.properties` y cambia esta línea:

```properties
# ANTES (usa clave de APIs):
gcp.credentials-path=${GCP_CREDENTIALS_PATH:classpath:devportal-storage-key.json}

# DESPUÉS (usa TU clave nueva):
gcp.credentials-path=${GCP_CREDENTIALS_PATH:classpath:teldev-filesystem-key.json}
```

### Paso 5: Verificar

```bash
# Compilar proyecto
mvn clean compile

# Debe compilar sin errores relacionados con GCS
```

---

## 🔐 SEGURIDAD: .gitignore

**IMPORTANTE**: Verifica que las claves JSON NO se suban a Git.

Abre `.gitignore` y asegúrate que tiene:

```gitignore
# Google Cloud credentials
*.json
!src/main/resources/static/**/*.json
devportal-storage-key.json
teldev-filesystem-key.json
```

---

## 🗄️ ESTRUCTURA DEL BUCKET

Una vez creado el bucket `dev-portal-storage`, tendrá esta estructura:

```
gs://dev-portal-storage/
├── proyectos/           # Archivos de proyectos
│   ├── P-1/
│   │   ├── nodo-123/archivo.pdf
│   │   └── nodo-124/imagen.png
│   └── P-23/
│       └── nodo-456/documento.docx
│
├── repositorios/        # Archivos de repositorios
│   ├── R-1/
│   └── R-31/
│       ├── nodo-789/README.md
│       └── nodo-790/config.json
│
├── trash/               # Papelera (archivos eliminados)
│   └── deleted-nodo-999/archivo-eliminado.pdf
│
└── temp/                # Archivos temporales (uploads en progreso)
    └── upload-session-abc/temp-file.tmp
```

---

## 📊 RESUMEN DE CONFIGURACIÓN FINAL

### application.properties
```properties
# GCP Project
gcp.project-id=dev-portal-gtics  # O devportal-storage si usas el mismo

# Clave del Service Account
gcp.credentials-path=classpath:teldev-filesystem-key.json  # TU clave nueva

# Bucket para file system
gcs.filesystem.bucket-name=dev-portal-storage

# Prefijos de carpetas
gcs.filesystem.prefix.proyectos=proyectos/
gcs.filesystem.prefix.repositorios=repositorios/
gcs.filesystem.prefix.trash=trash/
gcs.filesystem.prefix.temp=temp/

# Límites de upload
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

---

## ✅ CHECKLIST

Marca cuando completes cada paso:

- [ ] Bucket `dev-portal-storage` creado en GCS
- [ ] Service Account `teldev-filesystem-sa` creado (OPCIÓN 2)
- [ ] Clave JSON descargada como `teldev-filesystem-key.json`
- [ ] Archivo movido a `src/main/resources/`
- [ ] `application.properties` actualizado
- [ ] `.gitignore` configurado correctamente
- [ ] Proyecto compila sin errores: `mvn clean compile`

---

## 🆘 TROUBLESHOOTING

### Error: "Could not load credentials from classpath:teldev-filesystem-key.json"

**Solución**: Verifica que el archivo esté en la ruta correcta:
```
src/main/resources/teldev-filesystem-key.json
```

### Error: "Access denied to bucket"

**Solución**: Verifica que el service account tenga rol `Storage Object Admin`:
```bash
gsutil iam get gs://dev-portal-storage
```

### Error: "Bucket not found"

**Solución**: Verifica que el bucket exista y el nombre sea correcto:
```bash
gsutil ls gs://dev-portal-storage
```

---

## 📞 SIGUIENTE PASO

Después de completar este setup, continúa con:

**Ejecutar SQL**: `fase0_file_system_enhancements.sql` en la base de datos para crear triggers y tablas.
