# 📁 PLAN COMPLETO: SISTEMA DE ARCHIVOS (File System)

**Proyecto**: TelDev - Developer Portal  
**Fecha**: 2 de Noviembre, 2025  
**Objetivo**: Implementar sistema de archivos tipo Google Drive/GitHub para Proyectos y Repositorios

---

## 📊 ANÁLISIS DE TABLAS EXISTENTES

### **1. TABLAS FUNDAMENTALES (Core)**

#### `nodo` ⭐⭐⭐ **[TABLA CENTRAL DEL FILE SYSTEM]**
**Función**: Representa CUALQUIER elemento del sistema de archivos (carpetas y archivos)

**Estructura Actual**:
```sql
- nodo_id: ID único del nodo
- container_type: ENUM('PROYECTO', 'REPOSITORIO') → Indica si pertenece a un proyecto o repositorio
- container_id: ID del proyecto o repositorio padre
- parent_id: ID del nodo padre (NULL = raíz, carpeta jerárquica si tiene valor)
- nombre: Nombre de la carpeta/archivo (ej: "Authentication", "README.md")
- tipo: ENUM('CARPETA', 'ARCHIVO')
- path: Ruta completa (ej: "/Developer tools/SDK" o "/R-31/Authentication/OAuth2")
- descripcion: Descripción opcional
- size_bytes: Tamaño en bytes (para archivos)
- mime_type: Tipo MIME (ej: "application/pdf", "text/plain")
- creado_por, creado_en, actualizado_por, actualizado_en
- is_deleted, deleted_at: Soft delete
```

**✅ Evaluación**: **PERFECTA** para el sistema de archivos. Soporta:
- Jerarquía de carpetas ilimitada (parent_id autorreferencial)
- Distinción entre proyectos y repositorios
- Rutas completas para navegación
- Soft delete para papelera de reciclaje

**⚠️ Problema Identificado**: 
- Campo `path` tiene limite de 2000 caracteres, pero puede ser insuficiente para rutas muy profundas
- **SOLUCIÓN**: Está bien para 99% de casos, pero agregar validación en backend

---

#### `proyecto` ⭐⭐
**Función**: Contenedor principal que agrupa repositorios y carpetas propias

**Campos Clave**:
```sql
- proyecto_id: ID único
- nombre_proyecto, descripcion_proyecto
- visibilidad_proyecto: ENUM('PUBLICO', 'PRIVADO')
- acceso_proyecto: ENUM('RESTRINGIDO', 'ORGANIZACION', 'CUALQUIER_PERSONA_CON_EL_ENLACE')
- propietario_proyecto: ENUM('USUARIO', 'GRUPO', 'EMPRESA')
- estado_proyecto: ENUM('PLANEADO', 'EN_DESARROLLO', 'MANTENIMIENTO', 'CERRADO')
- root_node_id: ⭐ ID del nodo raíz (carpeta principal del proyecto)
- created_by, updated_by
```

**✅ Evaluación**: Bien diseñado
**🔧 Ajuste Necesario**: El campo `root_node_id` es CLAVE pero puede ser NULL. Debemos:
1. Crear automáticamente un nodo raíz al crear un proyecto
2. Este nodo raíz será la carpeta "/" del proyecto

---

#### `repositorio` ⭐⭐
**Función**: Repositorio de código (similar a GitHub repo)

**Campos Clave**:
```sql
- repositorio_id: ID único
- nombre_repositorio, descripcion_repositorio
- visibilidad_repositorio: ENUM('PUBLICO', 'PRIVADO')
- tipo_repositorio: ENUM('PERSONAL', 'COLABORATIVO')
- propietario_id: Usuario o equipo propietario
- root_node_id: ⭐ ID del nodo raíz (carpeta principal del repositorio)
- rama_principal_repositorio: 'main' (para GitHub integration)
- ultimo_commit_hash: Para sincronización con GitHub
- is_fork, forked_from_repo_id: Soporte para forks
- size_bytes: Tamaño total del repositorio
```

**✅ Evaluación**: Excelente para integración con GitHub
**🔧 Ajuste Necesario**: Igual que proyecto, `root_node_id` debe crearse automáticamente

---

### **2. TABLAS DE ALMACENAMIENTO**

#### `enlace` ⭐⭐⭐ **[ALMACENAMIENTO EN CLOUD]**
**Función**: Vincula archivos físicos en Google Cloud Storage (GCS)

**Estructura**:
```sql
- enlace_id: ID único
- direccion_almacenamiento: URL completa del archivo en GCS
- nombre_archivo: Nombre original del archivo
- contexto_type: ENUM(..., 'NODO', 'FILE_VERSION', ...)
- contexto_id: ID del nodo al que pertenece
- tipo_enlace: ENUM('STORAGE', 'METADATA', 'THUMBNAIL', 'BACKUP', 'TEMPORAL')
- estado_enlace: ENUM('ACTIVO', 'ARCHIVADO', 'ELIMINADO', 'PROCESANDO')
- creado_por
```

**✅ Evaluación**: Perfecta para almacenamiento en GCS
**Uso en File System**:
- Cuando subes un archivo, se crea un `nodo` (tipo='ARCHIVO') y un `enlace` apuntando al archivo en GCS
- Soporta thumbnails (vista previa de imágenes/PDFs)
- Soporta versionamiento con FILE_VERSION

---

#### `version_archivo` ⭐⭐ **[VERSIONAMIENTO]**
**Función**: Historial de versiones de un archivo (tipo Git)

**Estructura**:
```sql
- version_archivo_id: ID único
- nodo_id: Archivo al que pertenece
- enlace_id: Enlace al archivo físico de esta versión
- version_label: Ej: "v1.0", "v2.3"
- storage_key, storage_bucket: Datos de GCS
- checksum: Hash MD5/SHA256 para verificar integridad
- size_bytes: Tamaño de esta versión
- vigente: TINYINT(1) → La versión actual/activa
```

**✅ Evaluación**: Excelente para control de versiones
**Uso**: Cada vez que actualizas un archivo, se crea una nueva `version_archivo`

---

### **3. TABLAS DE PERMISOS Y ACCESO**

#### `permiso_nodo` ⭐⭐⭐ **[CONTROL DE ACCESO GRANULAR]**
**Función**: Define quién puede leer/escribir/administrar carpetas y archivos

**Estructura**:
```sql
- permiso_nodo_id: ID único
- nodo_id: Carpeta/archivo al que aplica
- permiso: ENUM('READ', 'WRITE', 'ADMIN')
- inheritable: Si los permisos se heredan a sub-carpetas/archivos
- usuario_usuario_id: Usuario específico (NULL si es para equipo)
- equipo_equipo_id: Equipo específico (NULL si es para usuario)
- CHECK: Uno de los dos (usuario o equipo) DEBE existir
```

**✅ Evaluación**: PERFECTA para permisos tipo Google Drive
**Uso**:
- Puedes dar permiso a una carpeta completa a un equipo (ej: "QA Team" puede leer carpeta "Tests")
- Puedes dar permiso individual a un usuario (ej: "mlopez" puede editar "config.json")
- Los permisos se heredan (si `inheritable=1`)

---

#### `usuario_has_proyecto` / `usuario_has_repositorio`
**Función**: Relación directa usuario-proyecto/repositorio

**Privilegios**: ENUM('PROPIETARIO', 'EDITOR', 'LECTOR')
- **PROPIETARIO**: Control total (borrar, configurar, invitar)
- **EDITOR**: Puede crear/modificar/eliminar archivos
- **LECTOR**: Solo lectura

**✅ Evaluación**: Necesario para acceso a nivel de contenedor (proyecto/repo completo)

---

#### `equipo_has_proyecto` / `equipo_has_repositorio`
**Función**: Igual que arriba pero para equipos

**✅ Evaluación**: Permite dar acceso a todo un equipo (ej: "Frontend Team" acceso a proyecto "Dashboard")

---

### **4. TABLAS DE RELACIONES**

#### `proyecto_has_repositorio`
**Función**: Un proyecto puede contener múltiples repositorios

**✅ Evaluación**: Necesario para la jerarquía:
```
Proyecto P-23
  ├── Carpeta "Developer tools"
  │     └── SDK.pdf
  ├── Repositorio R-31 (Desarrollo de sistemas de teledetección)
  │     ├── Carpeta "Authentication"
  │     │     └── OAuth2.md
  │     └── Carpeta "Arduino UNO para radares"
  └── Carpeta "Documentation"
```

---

#### `nodo_tag`, `nodo_tag_master`
**Función**: Etiquetar carpetas/archivos (ej: "Important", "Work in Progress")

**✅ Evaluación**: Útil para organización, pero no crítico para MVP

---

### **5. TABLAS DE AUDITORÍA**

#### `historial` (Híbrida SQL + MongoDB)
**Función**: Registrar TODAS las acciones en el file system

**Eventos Relevantes**:
- `CREACION`: Usuario creó carpeta "Authentication"
- `MODIFICACION`: Usuario renombró archivo "config.json" → "settings.json"
- `ELIMINACION`: Usuario eliminó carpeta "old_code"
- `RESTAURACION`: Usuario restauró archivo de la papelera

**✅ Evaluación**: Crítico para trazabilidad

---

### **6. TABLAS DE CONTEXTO**

#### `categoria_has_proyecto`, `categoria_has_repositorio`
**Función**: Clasificar proyectos/repos (ej: "Machine Learning", "Web Development")

**✅ Evaluación**: Útil para búsqueda/filtrado, no afecta file system directamente

---

#### `equipo`, `usuario_has_equipo`
**Función**: Gestión de equipos

**✅ Evaluación**: Necesario para permisos grupales

---

#### `rol`, `rol_proyecto`, `asignacion_rol_proyecto`
**Función**: Roles dentro de proyectos (ej: "Developer", "QA Tester")

**✅ Evaluación**: Complementario a permisos, útil pero no crítico para MVP

---

## 🔧 AJUSTES NECESARIOS EN LA BASE DE DATOS

### **CRÍTICOS (Hacer ANTES de implementar)**

#### 1. **Agregar índice compuesto en `nodo`** ✅ (YA EXISTE)
```sql
-- Ya está: idx_container_parent
INDEX `idx_container_parent` (`container_type`, `container_id`, `parent_id`)
```
✅ Permite buscar rápido todos los hijos de una carpeta

---

#### 2. **Crear trigger para auto-generar `root_node_id`**
```sql
-- Trigger para proyecto
DELIMITER $$
CREATE TRIGGER trg_proyecto_create_root_node
AFTER INSERT ON proyecto
FOR EACH ROW
BEGIN
    INSERT INTO nodo (
        container_type, container_id, parent_id, nombre, tipo, path, creado_por, creado_en
    ) VALUES (
        'PROYECTO', NEW.proyecto_id, NULL, '/', 'CARPETA', '/', NEW.created_by, NOW()
    );
    
    UPDATE proyecto 
    SET root_node_id = LAST_INSERT_ID() 
    WHERE proyecto_id = NEW.proyecto_id;
END$$

-- Trigger para repositorio
CREATE TRIGGER trg_repositorio_create_root_node
AFTER INSERT ON repositorio
FOR EACH ROW
BEGIN
    INSERT INTO nodo (
        container_type, container_id, parent_id, nombre, tipo, path, creado_por, creado_en
    ) VALUES (
        'REPOSITORIO', NEW.repositorio_id, NULL, '/', 'CARPETA', '/', NEW.creado_por_usuario_id, NOW()
    );
    
    UPDATE repositorio 
    SET root_node_id = LAST_INSERT_ID() 
    WHERE repositorio_id = NEW.repositorio_id;
END$$
DELIMITER ;
```

**Beneficio**: Cada proyecto/repositorio tendrá automáticamente su carpeta raíz

---

#### 3. **Agregar columna `web_url_path` en `nodo`** (OPCIONAL pero recomendado)
```sql
ALTER TABLE nodo ADD COLUMN web_url_path VARCHAR(2048) NULL 
COMMENT 'Ruta URL-safe para navegación (ej: /Developer%20tools/SDK)';
```

**Beneficio**: Facilita construcción de URLs en el navegador

---

### **RECOMENDADOS (Hacer para mejorar experiencia)**

#### 4. **Tabla para operaciones en clipboard (Copiar/Cortar/Pegar)**
```sql
CREATE TABLE IF NOT EXISTS `clipboard_operation` (
    `clipboard_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `operation_type` ENUM('COPY', 'CUT') NOT NULL,
    `nodo_ids` JSON NOT NULL COMMENT 'Array de IDs de nodos seleccionados',
    `source_container_type` ENUM('PROYECTO', 'REPOSITORIO') NOT NULL,
    `source_container_id` BIGINT UNSIGNED NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expires_at` DATETIME NOT NULL COMMENT 'Expira en 24 horas',
    PRIMARY KEY (`clipboard_id`),
    INDEX `idx_user_clipboard` (`usuario_id`, `expires_at`),
    FOREIGN KEY (`usuario_id`) REFERENCES `usuario`(`usuario_id`) ON DELETE CASCADE
) ENGINE=InnoDB;
```

**Beneficio**: Permite copiar archivos y pegarlos en otra carpeta (como Google Drive)

---

#### 5. **Tabla para operaciones asíncronas (Comprimir/Descargar múltiples)**
```sql
CREATE TABLE IF NOT EXISTS `file_operation_job` (
    `job_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `operation_type` ENUM('COMPRESS', 'BULK_DOWNLOAD', 'BULK_UPLOAD', 'MOVE', 'COPY') NOT NULL,
    `status` ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    `nodo_ids` JSON NOT NULL COMMENT 'Nodos a procesar',
    `result_enlace_id` BIGINT UNSIGNED NULL COMMENT 'Enlace al archivo resultante (ej: ZIP)',
    `error_message` TEXT NULL,
    `progress_percent` TINYINT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `completed_at` DATETIME NULL,
    PRIMARY KEY (`job_id`),
    INDEX `idx_user_jobs` (`usuario_id`, `status`),
    FOREIGN KEY (`usuario_id`) REFERENCES `usuario`(`usuario_id`) ON DELETE CASCADE,
    FOREIGN KEY (`result_enlace_id`) REFERENCES `enlace`(`enlace_id`) ON DELETE SET NULL
) ENGINE=InnoDB;
```

**Beneficio**: Procesar operaciones pesadas en background (comprimir 100 archivos)

---

## 🔗 INTEGRACIÓN CON GITHUB

### **TABLAS NECESARIAS**

#### 1. **`github_integration`** (Conexión Repo <--> GitHub Repo)
```sql
CREATE TABLE IF NOT EXISTS `github_integration` (
    `github_integration_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `repositorio_id` BIGINT UNSIGNED NOT NULL,
    `github_repository_fullname` VARCHAR(255) NOT NULL COMMENT 'owner/repo (ej: octocat/Hello-World)',
    `github_repository_url` VARCHAR(512) NOT NULL COMMENT 'https://github.com/owner/repo',
    `github_repo_id` BIGINT NULL COMMENT 'ID numérico del repo en GitHub API',
    `default_branch` VARCHAR(100) DEFAULT 'main',
    `sync_mode` ENUM('API_ONLY', 'WEBHOOK', 'CLONE_LOCAL') DEFAULT 'API_ONLY' COMMENT 'Modo de sincronización',
    `last_sync_at` DATETIME NULL,
    `last_sync_commit_hash` VARCHAR(40) NULL COMMENT 'Último commit sincronizado',
    `webhook_secret` VARCHAR(255) NULL COMMENT 'Secret para validar webhooks de GitHub',
    `is_active` TINYINT(1) DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_by` BIGINT UNSIGNED NOT NULL,
    UNIQUE INDEX `uk_repo_github` (`repositorio_id`),
    INDEX `idx_github_fullname` (`github_repository_fullname`),
    FOREIGN KEY (`repositorio_id`) REFERENCES `repositorio`(`repositorio_id`) ON DELETE CASCADE,
    FOREIGN KEY (`created_by`) REFERENCES `usuario`(`usuario_id`) ON DELETE SET NULL
) ENGINE=InnoDB;
```

**Función**: Conecta un repositorio de TelDev con un repositorio de GitHub

---

#### 2. **`github_user_token`** (OAuth Tokens de usuarios)
```sql
CREATE TABLE IF NOT EXISTS `github_user_token` (
    `github_token_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `access_token` VARCHAR(512) NOT NULL COMMENT 'Encrypted OAuth token',
    `token_type` VARCHAR(50) DEFAULT 'bearer',
    `scope` VARCHAR(500) NULL COMMENT 'Permisos del token (repo, read:user, etc)',
    `github_user_id` BIGINT NULL COMMENT 'ID del usuario en GitHub',
    `github_username` VARCHAR(255) NULL,
    `expires_at` DATETIME NULL COMMENT 'NULL = no expira (classic tokens)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_used_at` DATETIME NULL,
    `is_valid` TINYINT(1) DEFAULT 1,
    INDEX `idx_usuario_token` (`usuario_id`, `is_valid`),
    FOREIGN KEY (`usuario_id`) REFERENCES `usuario`(`usuario_id`) ON DELETE CASCADE
) ENGINE=InnoDB;
```

**Función**: Almacena tokens OAuth de GitHub de cada usuario para hacer operaciones

---

#### 3. **`github_sync_log`** (Historial de sincronizaciones)
```sql
CREATE TABLE IF NOT EXISTS `github_sync_log` (
    `sync_log_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `github_integration_id` BIGINT UNSIGNED NOT NULL,
    `sync_type` ENUM('MANUAL', 'WEBHOOK', 'SCHEDULED') NOT NULL,
    `sync_direction` ENUM('GITHUB_TO_TELDEV', 'TELDEV_TO_GITHUB', 'BIDIRECTIONAL') NOT NULL,
    `status` ENUM('SUCCESS', 'PARTIAL', 'FAILED') NOT NULL,
    `commits_synced` INT DEFAULT 0,
    `files_added` INT DEFAULT 0,
    `files_modified` INT DEFAULT 0,
    `files_deleted` INT DEFAULT 0,
    `error_message` TEXT NULL,
    `started_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `completed_at` DATETIME NULL,
    `triggered_by_usuario_id` BIGINT UNSIGNED NULL,
    INDEX `idx_integration_sync` (`github_integration_id`, `started_at`),
    FOREIGN KEY (`github_integration_id`) REFERENCES `github_integration`(`github_integration_id`) ON DELETE CASCADE,
    FOREIGN KEY (`triggered_by_usuario_id`) REFERENCES `usuario`(`usuario_id`) ON DELETE SET NULL
) ENGINE=InnoDB;
```

**Función**: Auditoría de cada sincronización con GitHub

---

### **MODOS DE SINCRONIZACIÓN PROPUESTOS**

#### **Modo 1: API_ONLY** (Recomendado para MVP)
- **Cómo funciona**: Usar GitHub REST API para leer archivos
- **Pros**: 
  - No requiere clonar repo completo
  - Ahorra espacio en GCS
  - Rápido para repos pequeños/medianos
- **Contras**: 
  - Límites de rate (5000 requests/hora con token)
  - Lento para repos gigantes (>1000 archivos)
- **Implementación**:
  1. Usuario conecta su cuenta GitHub (OAuth)
  2. Selecciona repo de GitHub a vincular
  3. TelDev lee estructura de archivos vía API
  4. Crea nodos en `nodo` para cada archivo/carpeta
  5. NO descarga archivos, solo guarda URLs de GitHub
  6. Al abrir un archivo, se muestra desde GitHub raw content

#### **Modo 2: WEBHOOK** (Para sincronización automática)
- **Cómo funciona**: GitHub envía webhook cuando hay push/commit
- **Pros**: Actualización en tiempo real
- **Contras**: Requiere endpoint público
- **Implementación**:
  1. Registrar webhook en GitHub repo settings
  2. Endpoint: `https://teldev.com/api/github/webhook/{github_integration_id}`
  3. Al recibir webhook, comparar commits y actualizar nodos

#### **Modo 3: CLONE_LOCAL** (Para repos grandes o trabajo offline)
- **Cómo funciona**: Clonar repo completo con git y subir a GCS
- **Pros**: Control total, trabajo offline
- **Contras**: 
  - Consume mucho espacio en GCS
  - Costo de storage
  - Complejidad de sincronización
- **Recomendación**: POSTERGAR para v2.0

---

### **FLUJO RECOMENDADO PARA CONECTAR GITHUB**

```
1. Usuario va a Repositorio R-31
2. Click en "Conectar con GitHub"
3. OAuth login GitHub → obtiene access_token
4. Guardar token en `github_user_token`
5. Usuario selecciona repo de GitHub (ej: "mlopez/telemetry-system")
6. Crear registro en `github_integration`:
   - repositorio_id = 31
   - github_repository_fullname = "mlopez/telemetry-system"
   - sync_mode = 'API_ONLY'
7. Background job sincroniza:
   - Lee árbol de archivos con GitHub API
   - Crea nodos en `nodo` para cada archivo/carpeta
   - Guarda URLs de archivos (no descarga contenido)
8. Usuario navega por archivos en TelDev
9. Al abrir archivo, se carga desde GitHub (vía API)
10. (Opcional) Configurar webhook para auto-sync
```

---

## ☁️ CONFIGURACIÓN DE GOOGLE CLOUD STORAGE (GCS)

### **BUCKET EXISTENTE** ✅
- **Nombre**: `dev-portal-storage` (YA CREADO)
- **Proyecto**: `dev-portal-gtics`
- **Ubicación**: `us-east1` (Carolina del Sur)
- **Clase**: Standard
- **Control de versiones**: ✅ Habilitado
- **Eliminación no definitiva**: ✅ 7 días
- **Acceso público**: ✅ Deshabilitado
- **Encriptación**: Administrada por Google
- **Etiquetas**:
  - `environment: production`
  - `owner: dev-portal-gtics`
  - `service: file-system`

### **CONFIGURAR LIFECYCLE RULES** (OPCIONAL pero recomendado)
```bash
# Lifecycle rule 1: Auto-eliminar trash después de 30 días
cat > lifecycle-trash.json << 'EOF'
{
  "lifecycle": {
    "rule": [
      {
        "action": {"type": "Delete"},
        "condition": {
          "age": 30,
          "matchesPrefix": ["trash/"]
        }
      }
    ]
  }
}
EOF

gsutil lifecycle set lifecycle-trash.json gs://dev-portal-storage/

# Lifecycle rule 2: Mover a Nearline archivos antiguos (ahorro de costos)
cat > lifecycle-nearline.json << 'EOF'
{
  "lifecycle": {
    "rule": [
      {
        "action": {"type": "SetStorageClass", "storageClass": "NEARLINE"},
        "condition": {
          "daysSinceCustomTime": 90,
          "matchesPrefix": ["proyectos/", "repositorios/"]
        }
      }
    ]
  }
}
EOF

gsutil lifecycle set lifecycle-nearline.json gs://dev-portal-storage/
```

### **ESTRUCTURA DE CARPETAS EN GCS**
```
gs://dev-portal-storage/
├── proyectos/
│   ├── P-1/
│   │   ├── nodo-123/archivo.pdf
│   │   └── nodo-124/imagen.png
│   └── P-23/
│       └── nodo-456/documento.docx
├── repositorios/
│   ├── R-1/
│   └── R-31/
│       ├── nodo-789/README.md
│       └── nodo-790/config.json
├── trash/  # Papelera (se auto-elimina después de 30 días)
│   └── deleted-nodo-999/archivo-eliminado.pdf
└── temp/  # Archivos temporales (subidas en progreso)
```

### **INTEGRACIÓN EN SPRING BOOT**

#### 1. **Actualizar `application.properties`**
```properties
# Archivo: src/main/resources/application.properties

# GCS Configuration
gcs.bucket.name=dev-portal-storage
gcp.project-id=dev-portal-gtics
gcp.credentials-path=classpath:devportal-storage-key.json

# File System Bucket
gcs.filesystem.bucket-name=teldev-filesystems
gcs.filesystem.base-url=https://storage.googleapis.com/teldev-filesystems

# Upload limits
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Temporary upload directory
file.upload.temp-dir=${java.io.tmpdir}/teldev-uploads
```

#### 2. **Crear servicio de almacenamiento**
```java
// Archivo: FileStorageService.java

@Service
public class FileStorageService {
    
    @Value("${gcs.filesystem.bucket-name}")
    private String bucketName;
    
    private final Storage storage;
    
    // Inyectar Storage (ya configurado en ApiContractStorageService)
    
    /**
     * Sube archivo a GCS y retorna la URL
     */
    public String uploadFile(MultipartFile file, String container Type, Long containerId, Long nodoId) {
        String objectName = buildObjectPath(containerType, containerId, nodoId, file.getOriginalFilename());
        
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(file.getContentType())
            .build();
        
        storage.create(blobInfo, file.getBytes());
        
        return String.format("gs://%s/%s", bucketName, objectName);
    }
    
    private String buildObjectPath(String containerType, Long containerId, Long nodoId, String filename) {
        String prefix = containerType.equals("PROYECTO") ? "proyectos" : "repositorios";
        return String.format("%s/%s-%d/nodo-%d/%s", 
            prefix, 
            containerType.charAt(0), 
            containerId, 
            nodoId, 
            filename
        );
    }
}
```

---

## 🎯 RESPUESTA A TUS PREGUNTAS

### **1. ¿Rutas con nombres o con IDs?**

**Opción A: Rutas con nombres**
```
/devportal/po/mlopez/projects/P-23/Developer tools/SDK
/devportal/po/mlopez/repositories/R-31/Authentication/OAuth2
```

**Opción B: Rutas con IDs de nodos**
```
/devportal/po/mlopez/projects/P-23/N-456/N-789
/devportal/po/mlopez/repositories/R-31/N-123/N-456
```

**✅ RECOMENDACIÓN: OPCIÓN A (rutas con nombres)**

**Razones**:
1. ✅ **SEO-friendly**: URLs legibles para Google
2. ✅ **User-friendly**: Usuario sabe dónde está sin mirar la UI
3. ✅ **GitHub compatibility**: GitHub usa rutas con nombres
4. ✅ **Compartir enlaces**: Más fácil compartir "…/Authentication/OAuth2.md"

**Implementación**:
- Codificar nombres con espacios: `Developer tools` → `Developer%20tools`
- Mantener `path` en tabla `nodo` para queries rápidas
- Validar nombres únicos dentro de la misma carpeta padre

**Opción B solo útil para**:
- Evitar problemas de encoding (caracteres raros)
- Cambiar nombres sin romper enlaces

**Solución híbrida** (LO MEJOR):
```
/devportal/po/mlopez/projects/P-23/Developer-tools-N456/SDK-N789

Formato: {nombre-legible}-N{nodo_id}
```
✅ Legible Y único Y permite renombrar sin romper enlaces

---

### **2. ¿CLI necesario?**

**Respuesta: NO para MVP, SÍ para v2.0**

**Alternativa Mejor**: GitHub Desktop/CLI para repos conectados
- Si usuario tiene repo conectado a GitHub, puede usar `git` normalmente
- Cambios se sincronizan vía webhook

**Si quieres CLI propio de TelDev**:
```bash
# Futuro v2.0
teldev upload myfile.pdf /projects/P-23/Documentation/
teldev download /projects/P-23/SDK.pdf
teldev sync --repo R-31  # Sincronizar con GitHub
```

**Esfuerzo**: Alto (3-4 semanas de desarrollo)
**Beneficio**: Medio (mayoría de usuarios prefieren UI web)

---

## 📋 PLAN DE IMPLEMENTACIÓN (FASES)

### **FASE 1: FUNDAMENTOS (2-3 semanas)**
✅ **Semana 1-2**: Backend básico
- [ ] Crear triggers para `root_node_id`
- [ ] Servicio Java para CRUD de nodos
- [ ] Endpoints REST:
  - `GET /api/projects/{id}/files` → Listar archivos raíz
  - `GET /api/projects/{id}/files/{path}` → Navegar carpeta
  - `POST /api/projects/{id}/files/upload` → Subir archivo
  - `POST /api/projects/{id}/folders` → Crear carpeta
  - `PUT /api/files/{nodoId}` → Renombrar
  - `DELETE /api/files/{nodoId}` → Soft delete

✅ **Semana 2-3**: Frontend básico
- [ ] Vista de archivos en Proyecto (tab "Contenido")
- [ ] Vista de archivos en Repositorio (tab "Contenido")
- [ ] Componente de árbol de carpetas (recursivo)
- [ ] Botones: Crear carpeta, Subir archivo, Renombrar, Eliminar
- [ ] Navegación por doble click

### **FASE 2: OPERACIONES AVANZADAS (2 semanas)**
- [ ] Copiar/Cortar/Pegar (tabla `clipboard_operation`)
- [ ] Descargar múltiples archivos (ZIP)
- [ ] Click derecho contextual (menú)
- [ ] Drag & drop para mover archivos
- [ ] Vista previa de archivos (imágenes, PDFs, código)

### **FASE 3: GITHUB INTEGRATION (3-4 semanas)**
- [ ] Tablas de GitHub (crear migrations)
- [ ] OAuth GitHub flow
- [ ] Sincronización API_ONLY
- [ ] Vista de commits en UI
- [ ] Botón "Sincronizar con GitHub"
- [ ] (Opcional) Webhooks para auto-sync

### **FASE 4: PERMISOS Y SEGURIDAD (1-2 semanas)**
- [ ] Implementar `permiso_nodo`
- [ ] Validación de permisos en backend
- [ ] UI para gestionar permisos (compartir carpeta)
- [ ] Permisos heredables

### **FASE 5: PULIDO Y OPTIMIZACIÓN (1 semana)**
- [ ] Búsqueda de archivos (Ctrl+F)
- [ ] Filtros (por tipo, fecha, tamaño)
- [ ] Papelera de reciclaje (recuperar archivos)
- [ ] Versionamiento de archivos (mostrar historial)
- [ ] Indicadores de progreso (subidas)

---

## 🚀 PRÓXIMOS PASOS INMEDIATOS

### **AHORA MISMO (Hoy)**:
1. ✅ **Revisar este documento** y confirmar arquitectura
2. ⚠️ **Decidir**: ¿Rutas con nombres, IDs o híbrido?
3. ⚠️ **Decidir**: ¿Empezamos sin GitHub o con GitHub desde inicio?

### **PRÓXIMA SESIÓN**:
1. 📝 **Crear migrations** para:
   - Triggers de `root_node_id`
   - Tablas GitHub (si decides integrar desde inicio)
   - Tabla `clipboard_operation`
   - Tabla `file_operation_job`
2. 🔧 **Configurar GCS bucket** para file system
3. ⚙️ **Crear entidades JPA** para `Nodo`, `VersionArchivo`, `PermisoNodo`

### **ESTA SEMANA**:
1. 💻 **Backend**: Endpoints básicos de file system
2. 🎨 **Frontend**: Componente de navegación de carpetas
3. 📤 **Upload**: Sistema de subida de archivos a GCS

---

## 📊 RESUMEN DE DECISIONES PENDIENTES

| Decisión | Opción A | Opción B | Recomendación |
|----------|----------|----------|---------------|
| **Rutas URLs** | Nombres (`/Developer tools/SDK`) | IDs (`/N-456/N-789`) | **Híbrido** (`/Developer-tools-N456/SDK-N789`) |
| **GitHub desde inicio** | Sí (Fase 1) | No (Fase 3) | **No** (enfocarnos en file system básico primero) |
| **CLI propia** | Sí | No | **No** (dejar para v2.0) |
| **Modo sync GitHub** | API_ONLY | CLONE_LOCAL | **API_ONLY** (más simple) |
| **Tab en UI** | "Contenido" | "Carpetas" | **"Contenido"** ✅ |

---

## 🎯 MÉTRICAS DE ÉXITO

**MVP Exitoso si**:
- ✅ Usuario puede crear/subir/eliminar archivos y carpetas
- ✅ Navegación fluida (doble click para abrir carpetas)
- ✅ Rutas en navegador reflejan jerarquía
- ✅ Permisos básicos funcionan (propietario vs colaborador)
- ✅ Archivos se almacenan en GCS sin errores

**v1.0 Exitoso si**:
- ✅ Todo lo anterior +
- ✅ Copiar/pegar archivos entre carpetas
- ✅ Integración GitHub funcional (leer archivos de GitHub)
- ✅ Vista previa de archivos comunes (imágenes, PDFs, Markdown)
- ✅ Búsqueda de archivos por nombre

---

**¿Listo para empezar? Dime qué decisiones tomas y continuamos con la implementación! 🚀**
