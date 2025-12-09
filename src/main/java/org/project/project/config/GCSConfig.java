package org.project.project.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Configuración de Google Cloud Storage (GCS) para el file system
 */
@Configuration
public class GCSConfig {

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.credentials-path}")
    private Resource credentialsPath;

    @Value("${gcs.filesystem.bucket-name}")
    private String bucketName;

    /**
     * Bean de Storage de GCS
     * Se usa para todas las operaciones de archivos en Google Cloud Storage
     */
    @Bean
    public Storage gcsStorage() throws IOException {
        GoogleCredentials credentials;
        
        try {
            // Intentar cargar credenciales desde archivo
            credentials = GoogleCredentials.fromStream(credentialsPath.getInputStream());
        } catch (Exception e) {
            // Fallback: usar credenciales por defecto (útil en Cloud Run)
            credentials = GoogleCredentials.getApplicationDefault();
        }

        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }

    /**
     * Validar que el bucket existe al iniciar la aplicación
     * NOTA: Comentado temporalmente por problemas de conectividad
     */
    @Bean
    public String validateBucket(Storage storage) {
        try {
            if (storage.get(bucketName) == null) {
                System.err.println("⚠️ ADVERTENCIA: GCS Bucket '" + bucketName + "' no existe o no se pudo validar.");
                return bucketName; // Continuar de todos modos
            }
            System.out.println("✅ GCS Bucket validado: " + bucketName);
            return bucketName;
        } catch (Exception e) {
            System.err.println("⚠️ ADVERTENCIA: No se pudo validar GCS Bucket (probablemente sin internet): " + e.getMessage());
            // NO lanzar excepción - permitir que la app inicie
            return bucketName;
        }
    }
}
