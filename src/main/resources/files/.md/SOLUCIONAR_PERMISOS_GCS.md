# 🔧 Solución: Permisos del Bucket GCS

## ❌ Problema Detectado

```
Service Account: id-dev-portal-storage-manager@dev-portal-gtics.iam.gserviceaccount.com
Bucket: dev-portal-storage
Error: "does not have storage.buckets.get access"
```

El bucket **existe** pero el Service Account **NO tiene permisos**.

---

## ✅ Solución 1: Usando gsutil (Recomendado)

**Ejecuta en PowerShell como ADMINISTRADOR:**

```powershell
gsutil iam ch serviceAccount:id-dev-portal-storage-manager@dev-portal-gtics.iam.gserviceaccount.com:roles/storage.objectAdmin gs://dev-portal-storage
```

**Verificar permisos aplicados:**

```powershell
gsutil iam get gs://dev-portal-storage
```

---

## ✅ Solución 2: Usando gcloud (Alternativa)

```powershell
gcloud storage buckets add-iam-policy-binding gs://dev-portal-storage `
    --member="serviceAccount:id-dev-portal-storage-manager@dev-portal-gtics.iam.gserviceaccount.com" `
    --role="roles/storage.objectAdmin" `
    --project=dev-portal-gtics
```

---

## ✅ Solución 3: Console (Manual)

1. Ve a: https://console.cloud.google.com/storage/browser/dev-portal-storage?project=dev-portal-gtics

2. Haz clic en **PERMISSIONS** (o "Permisos")

3. Haz clic en **GRANT ACCESS** (o "Conceder acceso")

4. En "New principals" (Nuevos principales):
   ```
   id-dev-portal-storage-manager@dev-portal-gtics.iam.gserviceaccount.com
   ```

5. En "Select a role" (Seleccionar un rol):
   ```
   Storage Object Admin
   ```

6. Haz clic en **SAVE** (Guardar)

---

## 🔍 Verificar que funcionó

Después de agregar los permisos, ejecuta:

```powershell
gsutil iam get gs://dev-portal-storage
```

**Deberías ver algo como:**

```json
{
  "bindings": [
    {
      "members": [
        "serviceAccount:id-dev-portal-storage-manager@dev-portal-gtics.iam.gserviceaccount.com"
      ],
      "role": "roles/storage.objectAdmin"
    }
  ]
}
```

---

## 🚀 Luego ejecutar aplicación

Una vez agregados los permisos:

```powershell
mvn spring-boot:run
```

**Deberías ver:**

```
✅ GCS Bucket validado: dev-portal-storage
✅ Inicializado FileSystemService con bucket: dev-portal-storage
```

---

## 📊 Resumen de tu configuración actual

**Cloud Run:**
- ✅ Servicio: `project-app`
- ✅ Service Account: `488502999710-compute@developer.gserviceaccount.com`
- ✅ Variables de entorno configuradas:
  - `GCP_PROJECT_ID=dev-portal-gtics`
  - `GCS_FILESYSTEM_BUCKET=dev-portal-storage`
  - `GCP_CREDENTIALS_PATH=/secrets/gcs-key/dev-portal-storage-manager-key.json`

**GCS:**
- ✅ Bucket existe: `dev-portal-storage`
- ❌ Permisos faltantes (lo que vamos a arreglar)

**Local:**
- ✅ Service Account: `id-dev-portal-storage-manager@dev-portal-gtics.iam.gserviceaccount.com`
- ✅ JSON key: `src/main/resources/devportal-storage-key.json`
- ❌ Bucket sin permisos para este SA (lo que vamos a arreglar)

---

## ⚠️ Nota Importante

Tienes **DOS Service Accounts diferentes**:

1. **Local (desarrollo)**: `id-dev-portal-storage-manager@...`
   - Usa JSON key para autenticación
   - **NECESITA permisos sobre el bucket** ← Esto es lo que falta

2. **Cloud Run (producción)**: `488502999710-compute@...`
   - Usa ADC (automático)
   - Ya tiene `roles/storage.objectAdmin` a nivel de proyecto
   - Debería funcionar automáticamente en Cloud Run

**Por eso la aplicación falla localmente pero debería funcionar en Cloud Run.**

Si quieres que funcione localmente, **DEBES agregar permisos al Service Account `id-dev-portal-storage-manager`** usando una de las 3 soluciones arriba.
