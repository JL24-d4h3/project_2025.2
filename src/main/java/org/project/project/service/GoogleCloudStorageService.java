package org.project.project.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para gestionar archivos en Google Cloud Storage.
 * 
 * Características:
 * - Subida automática con estructura de carpetas por fecha y reporte
 * - URLs públicas con expiración de 7 días
 * - Eliminación de archivos
 * - Gestión de versiones (v1-inicial sin subdirectorios de versión)
 * 
 * Estructura de almacenamiento:
 * gs://bucket-name/reportes/YYYY/MM/reporte-{id}/v1-inicial/{archivo}
 * 
 * @author jleon
 * @since 2025-10-23
 */
@Service
@Slf4j
public class GoogleCloudStorageService {

    private static final String REPORTES_PREFIX = "reportes";
    private static final String VERSION_INICIAL = "v1-inicial";
    private static final long URL_EXPIRY_DAYS = 7L;

    private final Storage storage;
    private final String bucketName;

    /**
     * Constructor que inicializa Google Cloud Storage con credenciales explícitas.
     */
    public GoogleCloudStorageService(@Value("${devportal.storage.bucket-name}") String bucketName) {
        try {
            log.info("Inicializando Google Cloud Storage con credenciales explícitas...");
            
            // Cargar credenciales desde classpath
            InputStream credentialsStream = new ClassPathResource("devportal-storage-key.json").getInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/devstorage.read_write"));
            
            // Crear instancia de Storage con credenciales explícitas
            this.storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId("devportal-storage")
                    .build()
                    .getService();
            
            this.bucketName = bucketName;
            log.info("✅ GoogleCloudStorageService inicializado con bucket: {}", bucketName);
            
            // Probar conectividad básica
            try {
                Bucket bucket = storage.get(bucketName);
                if (bucket != null) {
                    log.info("✅ Conectado exitosamente al bucket: {}", bucketName);
                } else {
                    log.warn("⚠️ Bucket {} no encontrado o no accesible", bucketName);
                }
            } catch (Exception e) {
                log.error("❌ Error al acceder al bucket {}: {}", bucketName, e.getMessage());
            }
        } catch (Exception e) {
            log.error("❌ Error al inicializar Google Cloud Storage: {}", e.getMessage(), e);
            throw new RuntimeException("Error al inicializar el servicio de GCS", e);
        }
    }

    /**
     * Sube un archivo a GCS con estructura de carpetas automática.
     * 
     * Estructura generada:
     * reportes/2025/10/reporte-123/v1-inicial/nombreArchivo.pdf
     *
     * @param file El archivo a subir
     * @param reporteId ID del reporte propietario
     * @param subidoPorUsuarioId ID del usuario que sube el archivo
     * @return GcsFileInfo con metadatos del archivo subido
     * @throws IOException si hay error en la subida
     */
    public GcsFileInfo uploadFile(MultipartFile file, Long reporteId, Long subidoPorUsuarioId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        try {
            // Generar ruta GCS automáticamente
            String gcsPath = generateGcsPath(reporteId, file.getOriginalFilename());
            log.info("Iniciando subida de archivo a GCS: {} (tamaño: {} bytes)", gcsPath, file.getSize());

            // Preparar metadatos del blob
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, gcsPath))
                    .setContentType(file.getContentType())
                    .setMetadata(Map.of(
                            "uploaded-by", String.valueOf(subidoPorUsuarioId),
                            "reporte-id", String.valueOf(reporteId),
                            "uploaded-at", LocalDateTime.now().toString()
                    ))
                    .build();

            // Subir a GCS
            byte[] fileContent = ByteStreams.toByteArray(file.getInputStream());
            Blob blob = storage.create(blobInfo, fileContent);
            log.info("Archivo subido exitosamente: {} (GCS ID: {})", gcsPath, blob.getName());

            // Generar URL pública con expiración
            String publicUrl = generatePublicUrl(gcsPath);

            // Construir objeto de respuesta
            return GcsFileInfo.builder()
                    .gcsFileId(blob.getName())
                    .gcsBucketName(bucketName)
                    .gcsFilePath(gcsPath)
                    .gcsPublicUrl(publicUrl)
                    .gcsFileSizeBytes(file.getSize())
                    .versionNumero(1)
                    .esVersionActual(true)
                    .gcsMigrado(true)
                    .uploadedAt(LocalDateTime.now())
                    .uploadedBy(subidoPorUsuarioId)
                    .build();

        } catch (IOException e) {
            log.error("Error al subir archivo a GCS: {}", e.getMessage(), e);
            throw new IOException("Error al subir archivo a Google Cloud Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Genera URL pública con expiración para descargar un archivo.
     * La URL expira en 7 días.
     *
     * @param gcsPath Ruta del archivo en GCS
     * @return URL pública válida por 7 días
     */
    public String generatePublicUrl(String gcsPath) {
        try {
            BlobId blobId = BlobId.of(bucketName, gcsPath);
            Blob blob = storage.get(blobId);

            if (blob == null) {
                log.warn("Blob no encontrado para generar URL: {}", gcsPath);
                return null;
            }

            // Generar URL con firma (signed URL) que expira en 7 días
            URL signedUrl = blob.signUrl(
                    URL_EXPIRY_DAYS,
                    TimeUnit.DAYS,
                    Storage.SignUrlOption.withV4Signature()
            );

            log.debug("URL pública generada para: {} (expira en {} días)", gcsPath, URL_EXPIRY_DAYS);
            return signedUrl.toString();

        } catch (Exception e) {
            log.error("Error al generar URL pública para: {}", gcsPath, e);
            return null;
        }
    }

    /**
     * Elimina un archivo de GCS.
     *
     * @param gcsPath Ruta del archivo a eliminar
     * @return true si se eliminó, false si no existe
     */
    public boolean deleteFile(String gcsPath) {
        try {
            BlobId blobId = BlobId.of(bucketName, gcsPath);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("Archivo eliminado de GCS: {}", gcsPath);
            } else {
                log.warn("Archivo no encontrado al intentar eliminar: {}", gcsPath);
            }

            return deleted;

        } catch (Exception e) {
            log.error("Error al eliminar archivo de GCS: {}", gcsPath, e);
            return false;
        }
    }

    /**
     * Descarga un archivo de GCS como bytes.
     *
     * @param gcsPath Ruta del archivo
     * @return Array de bytes del archivo
     * @throws IOException si hay error en la descarga
     */
    public byte[] downloadFile(String gcsPath) throws IOException {
        try {
            BlobId blobId = BlobId.of(bucketName, gcsPath);
            Blob blob = storage.get(blobId);

            if (blob == null) {
                throw new IOException("Archivo no encontrado en GCS: " + gcsPath);
            }

            log.info("Descargando archivo de GCS: {} (tamaño: {} bytes)", gcsPath, blob.getSize());
            return blob.getContent();

        } catch (IOException e) {
            log.error("Error al descargar archivo de GCS: {}", gcsPath, e);
            throw new IOException("Error al descargar archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene información metadata de un archivo en GCS.
     *
     * @param gcsPath Ruta del archivo
     * @return BlobInfo con metadatos o null si no existe
     */
    public BlobInfo getFileInfo(String gcsPath) {
        try {
            BlobId blobId = BlobId.of(bucketName, gcsPath);
            Blob blob = storage.get(blobId);

            if (blob == null) {
                return null;
            }

            log.debug("Obteniendo info de archivo GCS: {}", gcsPath);
            return blob;

        } catch (Exception e) {
            log.error("Error al obtener información de archivo GCS: {}", gcsPath, e);
            return null;
        }
    }

    /**
     * Verifica si un archivo existe en GCS.
     *
     * @param gcsPath Ruta del archivo
     * @return true si existe, false si no
     */
    public boolean fileExists(String gcsPath) {
        try {
            BlobId blobId = BlobId.of(bucketName, gcsPath);
            Blob blob = storage.get(blobId);
            return blob != null && blob.exists();

        } catch (Exception e) {
            log.error("Error al verificar existencia de archivo: {}", gcsPath, e);
            return false;
        }
    }

    /**
     * Lista todos los archivos de un reporte específico en GCS.
     *
     * @param reporteId ID del reporte
     * @return Lista de rutas GCS de archivos del reporte
     */
    public List<String> listReporteFiles(Long reporteId) {
        try {
            String prefix = String.format("%s/%s/reporte-%d/", REPORTES_PREFIX, getCurrentYearMonth(), reporteId);
            List<String> files = new ArrayList<>();

            Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll();

            blobs.forEach(blob -> {
                if (!blob.getName().endsWith("/")) { // Excluir "carpetas"
                    files.add(blob.getName());
                }
            });

            log.debug("Se encontraron {} archivos para reporte {}", files.size(), reporteId);
            return files;

        } catch (Exception e) {
            log.error("Error al listar archivos del reporte {}: {}", reporteId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Genera la ruta GCS automáticamente basada en fecha y ID del reporte.
     * 
     * Formato: reportes/YYYY/MM/reporte-{id}/v1-inicial/{filename}
     *
     * @param reporteId ID del reporte
     * @param filename Nombre del archivo original
     * @return Ruta completa en GCS
     */
    private String generateGcsPath(Long reporteId, String filename) {
        // Obtener YYYY/MM actual
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = String.format("%04d/%02d",
                now.getYear(),
                now.getMonthValue());

        // Generar path: reportes/YYYY/MM/reporte-{id}/v1-inicial/{filename}
        return String.format("%s/%s/reporte-%d/%s/%s",
                REPORTES_PREFIX,
                yearMonth,
                reporteId,
                VERSION_INICIAL,
                sanitizeFilename(filename));
    }

    /**
     * Obtiene el año y mes actual en formato YYYY/MM.
     *
     * @return Formato: 2025/10
     */
    private String getCurrentYearMonth() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%04d/%02d", now.getYear(), now.getMonthValue());
    }

    /**
     * Sanitiza el nombre del archivo para evitar caracteres problemáticos.
     *
     * @param filename Nombre original del archivo
     * @return Nombre sanitizado
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "archivo_sin_nombre_" + System.currentTimeMillis();
        }

        // Eliminar espacios y caracteres especiales problemáticos
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");

        // Limitar longitud
        if (filename.length() > 200) {
            String extension = filename.substring(filename.lastIndexOf("."));
            filename = filename.substring(0, 195) + extension;
        }

        return filename;
    }

    /**
     * Obtiene información del bucket de GCS.
     *
     * @return BucketInfo con metadatos del bucket
     */
    public BucketInfo getBucketInfo() {
        try {
            Bucket bucket = storage.get(bucketName);
            if (bucket != null) {
                log.debug("Bucket info obtenido: {}", bucketName);
                return bucket;
            }
            return null;

        } catch (Exception e) {
            log.error("Error al obtener información del bucket: {}", bucketName, e);
            return null;
        }
    }

    /**
     * Verifica la conexión con GCS (test de conectividad).
     *
     * @return true si la conexión es exitosa
     */
    public boolean testConnection() {
        try {
            BucketInfo bucketInfo = getBucketInfo();
            if (bucketInfo != null) {
                log.info("Conexión a GCS verificada exitosamente (bucket: {})", bucketName);
                return true;
            }
            return false;

        } catch (Exception e) {
            log.error("Error al verificar conexión con GCS: {}", e.getMessage());
            return false;
        }
    }

    // =====================================================================
    // NUEVOS MÉTODOS PARA APIS - PHASE 2
    // Estructura: apis/{apiId}/versions/{versionId}/...
    // FASE 1.1: Prefijo para separar local/producción
    // =====================================================================

    private static final String APIS_PREFIX = "apis";
    
    /**
     * Prefijo de entorno para separar archivos locales de producción.
     * - Local (desarrollo): "local/" → archivos en local/apis/...
     * - Producción (Cloud Run): "" → archivos en apis/...
     * 
     * Detecta automáticamente usando CLOUD_SQL_CONNECTION_NAME:
     * - En Cloud Run: Variable definida en app.yaml → Modo producción
     * - En Local: Variable no existe → Modo desarrollo
     */
    private static final String ENVIRONMENT_PREFIX;
    
    static {
        // CLOUD_SQL_CONNECTION_NAME solo existe en Cloud Run (definida en app.yaml)
        // En desarrollo local esta variable NO existe
        String cloudSqlConnection = System.getenv("CLOUD_SQL_CONNECTION_NAME");
        
        if (cloudSqlConnection != null) {
            // Estamos en Cloud Run (producción)
            ENVIRONMENT_PREFIX = "";
            log.info("☁️ Cloud Run detectado (Cloud SQL: {}) - archivos en: apis/...", cloudSqlConnection);
        } else {
            // Estamos en desarrollo local
            ENVIRONMENT_PREFIX = "local/";
            log.info("🏠 Desarrollo LOCAL detectado - archivos en: local/apis/...");
        }
    }

    /**
     * Sube un contrato OpenAPI (YAML/JSON) a GCS.
     * 
     * Estructura generada:
     * apis/{apiId}/versions/{numeroVersion}/contract.yaml
     *
     * @param apiId ID de la API
     * @param numeroVersion Número de versión semántica (ej: "1.0.0")
     * @param contratoContent Contenido del contrato en formato YAML o JSON
     * @param contentType MIME type ("application/yaml" o "application/json")
     * @param creadoPorUsuarioId ID del usuario que sube el contrato
     * @return URL de GCS donde se almacenó el contrato
     * @throws IOException si hay error en la subida
     */
    public String uploadContract(Long apiId, String numeroVersion, String contratoContent, String contentType, Long creadoPorUsuarioId) throws IOException {
        if (contratoContent == null || contratoContent.trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido del contrato no puede estar vacío");
        }

        try {
            // Generar ruta GCS con prefijo de entorno
            // Local: local/apis/{apiId}/versions/{numeroVersion}/contract.yaml
            // Producción: apis/{apiId}/versions/{numeroVersion}/contract.yaml
            String extension = contentType != null && contentType.contains("json") ? "json" : "yaml";
            String gcsPath = String.format("%s%s/%d/versions/%s/contract.%s",
                    ENVIRONMENT_PREFIX, APIS_PREFIX, apiId, numeroVersion, extension);

            log.info("📄 Subiendo contrato API a GCS: {} (tamaño: {} bytes)", gcsPath, contratoContent.length());

            // Preparar metadatos del blob
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, gcsPath))
                    .setContentType(contentType)
                    .setMetadata(Map.of(
                            "uploaded-by", String.valueOf(creadoPorUsuarioId),
                            "api-id", String.valueOf(apiId),
                            "version", numeroVersion,
                            "uploaded-at", LocalDateTime.now().toString(),
                            "file-type", "contract"
                    ))
                    .build();

            // Subir a GCS
            byte[] content = contratoContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            Blob blob = storage.create(blobInfo, content);
            log.info("Contrato API subido exitosamente: {} (GCS ID: {})", gcsPath, blob.getName());

            // Retornar la ruta GCS (se guardará en VersionAPI.contratoApiUrl)
            return gcsPath;

        } catch (Exception e) {
            log.error("Error al subir contrato API a GCS: {}", e.getMessage(), e);
            throw new IOException("Error al subir contrato a Google Cloud Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Sube un recurso (archivo) de documentación CMS a GCS.
     * 
     * Estructura generada:
     * apis/{apiId}/versions/{numeroVersion}/documentation/resources/{recursoId}/{filename}
     *
     * @param apiId ID de la API
     * @param numeroVersion Número de versión semántica (ej: "1.0.0")
     * @param recursoId ID del recurso (generado antes de subir)
     * @param file Archivo a subir (PDF, imagen, código, etc.)
     * @param creadoPorUsuarioId ID del usuario que sube el recurso
     * @return URL de GCS donde se almacenó el recurso
     * @throws IOException si hay error en la subida
     */
    public String uploadRecurso(Long apiId, String numeroVersion, Long recursoId, MultipartFile file, Long creadoPorUsuarioId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        try {
            // Generar ruta GCS con prefijo de entorno
            // Local: local/apis/{apiId}/versions/{numeroVersion}/documentation/resources/{recursoId}/{filename}
            // Producción: apis/{apiId}/versions/{numeroVersion}/documentation/resources/{recursoId}/{filename}
            String sanitizedFilename = sanitizeFilename(file.getOriginalFilename());
            String gcsPath = String.format("%s%s/%d/versions/%s/documentation/resources/%d/%s",
                    ENVIRONMENT_PREFIX, APIS_PREFIX, apiId, numeroVersion, recursoId, sanitizedFilename);

            log.info("📎 Subiendo recurso a GCS: {} (tamaño: {} bytes)", gcsPath, file.getSize());

            // Preparar metadatos del blob
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, gcsPath))
                    .setContentType(file.getContentType())
                    .setMetadata(Map.of(
                            "uploaded-by", String.valueOf(creadoPorUsuarioId),
                            "api-id", String.valueOf(apiId),
                            "version", numeroVersion,
                            "recurso-id", String.valueOf(recursoId),
                            "uploaded-at", LocalDateTime.now().toString(),
                            "file-type", "recurso",
                            "original-filename", file.getOriginalFilename()
                    ))
                    .build();

            // Subir a GCS
            byte[] fileContent = ByteStreams.toByteArray(file.getInputStream());
            Blob blob = storage.create(blobInfo, fileContent);
            log.info("Recurso subido exitosamente: {} (GCS ID: {})", gcsPath, blob.getName());

            // Retornar la ruta GCS (se guardará en Enlace.direccionAlmacenamiento)
            return gcsPath;

        } catch (IOException e) {
            log.error("Error al subir recurso a GCS: {}", e.getMessage(), e);
            throw new IOException("Error al subir recurso a Google Cloud Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina todos los archivos de una versión específica de API.
     * Útil para operaciones de limpieza o rollback.
     * 
     * Elimina:
     * - apis/{apiId}/versions/{numeroVersion}/contract.yaml
     * - apis/{apiId}/versions/{numeroVersion}/documentation/resources/**
     *
     * @param apiId ID de la API
     * @param numeroVersion Número de versión semántica (ej: "1.0.0")
     * @return Número de archivos eliminados
     */
    public int deleteVersionFiles(Long apiId, String numeroVersion) {
        try {
            String prefix = String.format("%s%s/%d/versions/%s/", 
                    ENVIRONMENT_PREFIX, APIS_PREFIX, apiId, numeroVersion);
            log.info("🗑️ Eliminando archivos de versión: {}", prefix);

            int deletedCount = 0;
            Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll();

            for (Blob blob : blobs) {
                if (!blob.getName().endsWith("/")) { // Excluir "carpetas"
                    boolean deleted = storage.delete(blob.getBlobId());
                    if (deleted) {
                        deletedCount++;
                        log.debug("Archivo eliminado: {}", blob.getName());
                    }
                }
            }

            log.info("Se eliminaron {} archivos de la versión {} de API {}", deletedCount, numeroVersion, apiId);
            return deletedCount;

        } catch (Exception e) {
            log.error("Error al eliminar archivos de versión {}: {}", numeroVersion, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Elimina todos los archivos de una API completa (todas sus versiones).
     * Útil cuando se elimina una API completa del sistema.
     * 
     * Elimina:
     * - apis/{apiId}/versions/**
     *
     * @param apiId ID de la API a eliminar
     * @return Número de archivos eliminados
     */
    public int deleteApiFiles(Long apiId) {
        try {
            String prefix = String.format("%s%s/%d/", 
                    ENVIRONMENT_PREFIX, APIS_PREFIX, apiId);
            log.info("🗑️ Eliminando archivos de API completa: {}", prefix);

            int deletedCount = 0;
            Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll();

            for (Blob blob : blobs) {
                if (!blob.getName().endsWith("/")) { // Excluir "carpetas"
                    boolean deleted = storage.delete(blob.getBlobId());
                    if (deleted) {
                        deletedCount++;
                        log.debug("Archivo eliminado: {}", blob.getName());
                    }
                }
            }

            log.info("Se eliminaron {} archivos de la API {}", deletedCount, apiId);
            return deletedCount;

        } catch (Exception e) {
            log.error("Error al eliminar archivos de API {}: {}", apiId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Copia los archivos de recursos de una versión a otra.
     * Usado cuando se crea una nueva versión con opción "copiar documentación".
     * 
     * IMPORTANTE: Solo copia los archivos físicos en GCS.
     * La duplicación de entidades (Documentacion, Contenido, Recurso, Enlace)
     * se hace en el servicio de negocio.
     * 
     * @param apiId ID de la API
     * @param sourceVersionId ID de la versión origen
     * @param targetVersionId ID de la versión destino
     * @param recursoIdMapping Map<oldRecursoId, newRecursoId> para mapear IDs
     * @return Número de archivos copiados
     */
    public int copyRecursosBetweenVersions(Long apiId, Long sourceVersionId, Long targetVersionId, Map<Long, Long> recursoIdMapping) {
        try {
            String sourcePrefix = String.format("%s%s/%d/versions/%d/documentacion/recursos/",
                    ENVIRONMENT_PREFIX, APIS_PREFIX, apiId, sourceVersionId);
            
            log.info("📋 Copiando recursos de versión {} a versión {} de API {}", sourceVersionId, targetVersionId, apiId);

            int copiedCount = 0;
            Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(sourcePrefix)).iterateAll();

            for (Blob sourceBlob : blobs) {
                if (!sourceBlob.getName().endsWith("/")) { // Excluir "carpetas"
                    // Extraer recursoId del path origen
                    String[] parts = sourceBlob.getName().split("/");
                    if (parts.length >= 7) { // apis/X/versions/Y/documentacion/recursos/Z/file
                        Long oldRecursoId = Long.parseLong(parts[6]);
                        Long newRecursoId = recursoIdMapping.get(oldRecursoId);

                        if (newRecursoId != null) {
                            String filename = parts[7];
                            String targetPath = String.format("%s%s/%d/versions/%d/documentacion/recursos/%d/%s",
                                    ENVIRONMENT_PREFIX, APIS_PREFIX, apiId, targetVersionId, newRecursoId, filename);

                            // Copiar blob
                            Storage.CopyRequest copyRequest = Storage.CopyRequest.newBuilder()
                                    .setSource(sourceBlob.getBlobId())
                                    .setTarget(BlobId.of(bucketName, targetPath))
                                    .build();
                            
                            storage.copy(copyRequest);
                            copiedCount++;
                            log.debug("Recurso copiado: {} -> {}", sourceBlob.getName(), targetPath);
                        }
                    }
                }
            }

            log.info("Se copiaron {} recursos de versión {} a versión {} de API {}", 
                    copiedCount, sourceVersionId, targetVersionId, apiId);
            return copiedCount;

        } catch (Exception e) {
            log.error("Error al copiar recursos entre versiones: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Lista todos los archivos de una versión específica de API.
     *
     * @param apiId ID de la API
     * @param versionId ID de la versión
     * @return Lista de rutas GCS de archivos de la versión
     */
    public List<String> listVersionFiles(Long apiId, Long versionId) {
        try {
            String prefix = String.format("%s%s/%d/versions/%d/", 
                    ENVIRONMENT_PREFIX, APIS_PREFIX, apiId, versionId);
            List<String> files = new ArrayList<>();

            Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll();

            blobs.forEach(blob -> {
                if (!blob.getName().endsWith("/")) { // Excluir "carpetas"
                    files.add(blob.getName());
                }
            });

            log.debug("Se encontraron {} archivos para versión {} de API {}", files.size(), versionId, apiId);
            return files;

        } catch (Exception e) {
            log.error("Error al listar archivos de versión {} de API {}: {}", versionId, apiId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Genera URL pública con firma para un archivo de API (contrato o recurso).
     * Sobrecarga del método existente con mismo comportamiento (7 días de expiración).
     * 
     * @param gcsPath Ruta del archivo en GCS (completa)
     * @return URL pública con firma válida por 7 días, o null si hay error
     */
    public String generateSignedUrlForApi(String gcsPath) {
        // Reutiliza el método existente generatePublicUrl()
        return generatePublicUrl(gcsPath);
    }

    // =====================================================================
    // FIN MÉTODOS PARA APIS
    // =====================================================================

    /**
     * DTO para retornar información sobre archivo subido a GCS.
     * Contiene todos los datos necesarios para guardar en la BD.
     */
    @lombok.Data
    @lombok.Builder
    public static class GcsFileInfo {
        private String gcsFileId;           // Path completo del archivo en GCS
        private String gcsBucketName;       // Nombre del bucket
        private String gcsFilePath;         // Ruta relativa en el bucket
        private String gcsPublicUrl;        // URL pública con firma (7 días)
        private Long gcsFileSizeBytes;      // Tamaño en bytes
        private Integer versionNumero;      // Siempre 1 (v1-inicial)
        private Boolean esVersionActual;    // Siempre true (initial)
        private Boolean gcsMigrado;         // Siempre true (nativo)
        private LocalDateTime uploadedAt;   // Timestamp de subida
        private Long uploadedBy;            // ID del usuario que subió
    }
}
