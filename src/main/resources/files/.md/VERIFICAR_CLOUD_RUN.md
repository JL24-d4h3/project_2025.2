# 🔍 Verificar Configuración Real de Cloud Run

## 1️⃣ Listar tus servicios reales en Cloud Run

```bash
gcloud run services list --project=dev-portal-gtics --region=us-central1
```

**Busca:**
- El nombre REAL de tu servicio (probablemente diferente a "teldev-service")
- La URL del servicio
- El estado (READY/NOT READY)

---

## 2️⃣ Verificar bucket GCS

```bash
# Listar buckets existentes
gsutil ls -p dev-portal-gtics

# Verificar si dev-portal-storage existe
gsutil ls gs://dev-portal-storage
```

**Si el bucket NO existe:**
```bash
# Crear bucket (elige la región más cercana)
gsutil mb -p dev-portal-gtics -c STANDARD -l us-east1 gs://dev-portal-storage

# Verificar creación
gsutil ls -L gs://dev-portal-storage
```

---

## 3️⃣ Ver variables de entorno actuales en Cloud Run

```bash
# Reemplaza NOMBRE_REAL por el nombre obtenido en paso 1
gcloud run services describe NOMBRE_REAL \
    --project=dev-portal-gtics \
    --region=us-central1 \
    --format="yaml(spec.template.spec.containers[0].env)"
```

**Busca estas variables:**
- ✅ `GCP_PROJECT_ID` = `dev-portal-gtics`
- ✅ `GCS_FILESYSTEM_BUCKET` = `dev-portal-storage`
- ✅ `SPRING_DATASOURCE_URL` (conexión Cloud SQL)

---

## 4️⃣ Verificar permisos del Service Account de Cloud Run

```bash
# Ver qué SA usa tu servicio
gcloud run services describe NOMBRE_REAL \
    --project=dev-portal-gtics \
    --region=us-central1 \
    --format="value(spec.template.spec.serviceAccountName)"

# Verificar roles del SA (reemplaza el email obtenido arriba)
gcloud projects get-iam-policy dev-portal-gtics \
    --flatten="bindings[].members" \
    --filter="bindings.members:serviceAccount:EMAIL_DEL_SA" \
    --format="table(bindings.role)"
```

**Roles necesarios:**
- ✅ `roles/cloudsql.client` (para Cloud SQL)
- ✅ `roles/storage.objectAdmin` (para GCS)

---

## 5️⃣ Ver logs recientes de Cloud Run

```bash
# Reemplaza NOMBRE_REAL
gcloud run services logs tail NOMBRE_REAL \
    --project=dev-portal-gtics \
    --region=us-central1 \
    --limit=50
```

**Busca errores relacionados con:**
- Conexión a Cloud SQL
- Bucket GCS no encontrado
- Variables de entorno faltantes

---

## 🎯 Resumen de Configuración Necesaria

### Base de Datos (Cloud SQL)
- ✅ **Tienes:** Base de datos `dev_portal_sql` creada
- ⏳ **Falta:** Ejecutar `fase0_file_system_enhancements.sql`
  - Crea tablas: `nodo_share_link`, `clipboard_operation`, etc.
  - Crea triggers automáticos para nodos raíz
  - Crea stored procedures para operaciones avanzadas

### Bucket GCS
- ⏳ **Verificar:** Si `dev-portal-storage` existe (paso 2)
- ⏳ **Crear:** Si no existe (comando en paso 2)
- ⏳ **Configuración recomendada:**
  - Región: `us-east1` o `southamerica-east1` (cerca de Cloud SQL)
  - Storage class: `STANDARD`
  - Versioning: `ENABLED` (opcional pero recomendado)
  - Lifecycle: Borrado automático de versiones antiguas después de 30 días

### Variables de Entorno en Cloud Run
- ⏳ **Verificar:** Qué variables ya tienes (paso 3)
- ⏳ **Agregar faltantes:**
  ```
  GCP_PROJECT_ID=dev-portal-gtics
  GCS_FILESYSTEM_BUCKET=dev-portal-storage
  ```

### Permisos Service Account
- ✅ **Ya tienes:** `roles/storage.objectAdmin` (agregado hoy)
- ✅ **Ya tienes:** `roles/cloudsql.client`

---

## 📝 Próximos Pasos

1. **Ejecuta paso 1** → Obtén el nombre REAL de tu servicio
2. **Ejecuta paso 2** → Verifica/crea el bucket
3. **Ejecuta paso 3** → Ve qué variables ya tienes configuradas
4. **Ejecuta el SQL** → `fase0_file_system_enhancements.sql` en MySQL Workbench
5. **Despliega nueva revisión** → Con las variables de entorno correctas

---

## ⚠️ Nota sobre "teldev-service"

Este nombre fue un **ejemplo genérico** usado en la documentación. Tu servicio real puede tener un nombre diferente como:
- `dev-portal`
- `devportal-backend`
- `backend-service`
- Cualquier otro nombre que hayas elegido al crear el servicio

Ejecuta el paso 1 para descubrir el nombre correcto.
