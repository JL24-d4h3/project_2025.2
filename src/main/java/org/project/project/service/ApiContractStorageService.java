package org.project.project.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ApiContractStorageService {

    private final Storage storage;
    private final String bucketName;

    public ApiContractStorageService(
            @Value("${devportal.storage.bucket-name}") String bucketName) {
        try {
            log.info("Initializing Google Cloud Storage with explicit credentials...");
            
            // Load credentials directly from classpath
            InputStream credentialsStream = new ClassPathResource("devportal-storage-key.json").getInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/devstorage.read_write"));
            
            // Create storage instance with explicit credentials
            this.storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId("devportal-storage")
                    .build()
                    .getService();
            
            this.bucketName = bucketName;
            log.info("✅ Initialized ApiContractStorageService with bucket: {}", bucketName);
            
            // Test basic connectivity
            try {
                Bucket bucket = storage.get(bucketName);
                if (bucket != null) {
                    log.info("✅ Successfully connected to bucket: {}", bucketName);
                } else {
                    log.warn("⚠️ Bucket {} not found or not accessible", bucketName);
                }
            } catch (Exception e) {
                log.error("❌ Error accessing bucket {}: {}", bucketName, e.getMessage());
            }
        } catch (Exception e) {
            log.error("❌ Failed to initialize Google Cloud Storage: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize GCS service", e);
        }
    }

    /**
     * Guarda el contenido del contrato de API como texto
     */
    public String saveApiContract(Long apiId, String version, String contractContent) {
        log.info("Starting to save API contract for API {} version {}", apiId, version);
        
        try {
            String fileName = generateFileName(apiId, version);
            log.info("Generated filename: {}", fileName);
            
            BlobId blobId = BlobId.of(bucketName, fileName);
            log.info("Created BlobId for bucket: {} file: {}", bucketName, fileName);
            
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("text/plain")
                    .setMetadata(java.util.Map.of(
                            "api_id", apiId.toString(),
                            "version", version,
                            "upload_time", LocalDateTime.now().toString()
                    ))
                    .build();

            log.info("Attempting to create blob in GCS...");
            Blob blob = storage.create(blobInfo, contractContent.getBytes(StandardCharsets.UTF_8));
            log.info("✅ Blob created successfully with name: {}", blob.getName());
            
            String publicUrl = String.format("gs://%s/%s", bucketName, fileName);
            log.info("✅ Successfully saved API contract: {}", publicUrl);
            
            return publicUrl;
            
        } catch (Exception e) {
            log.error("❌ Error saving API contract for API {} version {}: {}", apiId, version, e.getMessage(), e);
            throw new RuntimeException("Failed to save API contract", e);
        }
    }

    /**
     * Guarda un archivo subido (para futuras funcionalidades)
     */
    public String saveApiContractFile(Long apiId, String version, MultipartFile file) {
        try {
            String fileName = generateFileName(apiId, version, file.getOriginalFilename());
            
            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .setMetadata(java.util.Map.of(
                            "api_id", apiId.toString(),
                            "version", version,
                            "original_name", file.getOriginalFilename(),
                            "upload_time", LocalDateTime.now().toString()
                    ))
                    .build();

            Blob blob = storage.create(blobInfo, file.getBytes());
            
            String publicUrl = String.format("gs://%s/%s", bucketName, fileName);
            log.info("Successfully saved API contract file: {}", publicUrl);
            
            return publicUrl;
            
        } catch (IOException e) {
            log.error("Error saving API contract file for API {} version {}: {}", apiId, version, e.getMessage());
            throw new RuntimeException("Failed to save API contract file", e);
        }
    }

    /**
     * Recupera el contenido del contrato
     */
    public String getApiContract(String gcsUrl) {
        try {
            String fileName = extractFileNameFromUrl(gcsUrl);
            BlobId blobId = BlobId.of(bucketName, fileName);
            Blob blob = storage.get(blobId);
            
            if (blob == null) {
                throw new RuntimeException("Contract file not found: " + gcsUrl);
            }
            
            return new String(blob.getContent(), StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Error retrieving API contract from {}: {}", gcsUrl, e.getMessage());
            throw new RuntimeException("Failed to retrieve API contract", e);
        }
    }

    /**
     * Genera una URL firmada para acceso temporal
     */
    public String generateSignedUrl(String gcsUrl, long durationMinutes) {
        try {
            String fileName = extractFileNameFromUrl(gcsUrl);
            BlobId blobId = BlobId.of(bucketName, fileName);
            
            URL signedUrl = storage.signUrl(
                    BlobInfo.newBuilder(blobId).build(),
                    durationMinutes,
                    TimeUnit.MINUTES
            );
            
            return signedUrl.toString();
            
        } catch (Exception e) {
            log.error("Error generating signed URL for {}: {}", gcsUrl, e.getMessage());
            throw new RuntimeException("Failed to generate signed URL", e);
        }
    }

    /**
     * Elimina un contrato
     */
    public boolean deleteApiContract(String gcsUrl) {
        try {
            String fileName = extractFileNameFromUrl(gcsUrl);
            BlobId blobId = BlobId.of(bucketName, fileName);
            
            boolean deleted = storage.delete(blobId);
            log.info("Contract deletion result for {}: {}", gcsUrl, deleted);
            
            return deleted;
            
        } catch (Exception e) {
            log.error("Error deleting API contract {}: {}", gcsUrl, e.getMessage());
            return false;
        }
    }

    // Métodos auxiliares
    private String generateFileName(Long apiId, String version) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("apis/api_%d/v_%s/contract_%s.txt", apiId, version, timestamp);
    }

    private String generateFileName(Long apiId, String version, String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        return String.format("apis/api_%d/v_%s/contract_%s%s", apiId, version, timestamp, extension);
    }

    private String extractFileNameFromUrl(String gcsUrl) {
        // Extrae el nombre del archivo de URLs como "gs://bucket/path/file.txt"
        // o acepta paths relativos como "apis/7/versions/1.0.0/contract.yaml"
        if (gcsUrl.startsWith("gs://")) {
            int bucketEndIndex = gcsUrl.indexOf('/', 5); // Después de "gs://"
            return gcsUrl.substring(bucketEndIndex + 1);
        }
        
        // Si no tiene el prefijo gs://, asumimos que es un path relativo
        // y lo usamos directamente (ya está en el formato correcto para GCS)
        log.debug("Using relative GCS path: {}", gcsUrl);
        return gcsUrl;
    }
}