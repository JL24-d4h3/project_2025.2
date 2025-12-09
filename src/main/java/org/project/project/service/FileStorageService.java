package org.project.project.service;

import com.google.cloud.storage.*;
import org.project.project.model.entity.Nodo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service para operaciones de almacenamiento en Google Cloud Storage
 * Maneja subida, descarga, eliminación y gestión de archivos en GCS
 */
@Service
public class FileStorageService {

    @Autowired
    private Storage storage;

    @Autowired
    private GCSConfigService gcsConfigService;

    /**
     * Sube un archivo a Google Cloud Storage
     * @param file Archivo multipart a subir
     * @param rutaGCS Ruta completa en GCS donde se guardará el archivo
     * @param metadata Metadatos adicionales para el archivo (opcional)
     * @return Información del blob creado en GCS
     * @throws IOException Si hay error al leer el archivo o subirlo
     */
    public Blob subirArchivo(MultipartFile file, String rutaGCS, Map<String, String> metadata) throws IOException {
        BlobId blobId = BlobId.of(gcsConfigService.getBucketName(), rutaGCS);
        
        BlobInfo.Builder blobInfoBuilder = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType());
        
        // Agregar metadata personalizada
        if (metadata != null && !metadata.isEmpty()) {
            blobInfoBuilder.setMetadata(metadata);
        }
        
        // Agregar metadata automática
        Map<String, String> autoMetadata = new HashMap<>();
        autoMetadata.put("original-filename", file.getOriginalFilename());
        autoMetadata.put("upload-timestamp", String.valueOf(System.currentTimeMillis()));
        blobInfoBuilder.setMetadata(autoMetadata);
        
        BlobInfo blobInfo = blobInfoBuilder.build();
        
        return storage.create(blobInfo, file.getBytes());
    }

    /**
     * Sube un archivo desde un InputStream a Google Cloud Storage
     * @param inputStream Stream de datos del archivo
     * @param rutaGCS Ruta completa en GCS donde se guardará el archivo
     * @param contentType Tipo MIME del archivo
     * @param tamanio Tamaño del archivo en bytes
     * @return Información del blob creado en GCS
     * @throws IOException Si hay error al leer el stream o subirlo
     */
    public Blob subirArchivoDesdeStream(InputStream inputStream, String rutaGCS, String contentType, long tamanio) throws IOException {
        BlobId blobId = BlobId.of(gcsConfigService.getBucketName(), rutaGCS);
        
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();
        
        // Usar Writer para subida eficiente de streams grandes
        try (var writer = storage.writer(blobInfo)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                writer.write(java.nio.ByteBuffer.wrap(buffer, 0, bytesRead));
            }
        }
        
        return storage.get(blobId);
    }

    /**
     * Descarga un archivo desde Google Cloud Storage
     * @param rutaGCS Ruta completa del archivo en GCS
     * @return Array de bytes con el contenido del archivo
     * @throws IllegalArgumentException Si el archivo no existe
     */
    public byte[] descargarArchivo(String rutaGCS) {
        BlobId blobId = BlobId.of(gcsConfigService.getBucketName(), rutaGCS);
        Blob blob = storage.get(blobId);
        
        if (blob == null || !blob.exists()) {
            throw new IllegalArgumentException("Archivo no encontrado en GCS: " + rutaGCS);
        }
        
        return blob.getContent();
    }

    /**
     * Obtiene un InputStream para leer un archivo desde GCS (eficiente para archivos grandes)
     * @param rutaGCS Ruta completa del archivo en GCS
     * @return InputStream para leer el archivo
     * @throws IllegalArgumentException Si el archivo no existe
     */
    public InputStream obtenerStreamDescarga(String rutaGCS) {
        BlobId blobId = BlobId.of(gcsConfigService.getBucketName(), rutaGCS);
        Blob blob = storage.get(blobId);
        
        if (blob == null || !blob.exists()) {
            throw new IllegalArgumentException("Archivo no encontrado en GCS: " + rutaGCS);
        }
        
        return Channels.newInputStream(blob.reader());
    }

    /**
     * Elimina un archivo de Google Cloud Storage
     * @param rutaGCS Ruta completa del archivo en GCS
     * @return true si el archivo fue eliminado, false si no existía
     */
    public boolean eliminarArchivoDeGCS(String rutaGCS) {
        BlobId blobId = BlobId.of(gcsConfigService.getBucketName(), rutaGCS);
        return storage.delete(blobId);
    }

    /**
     * Copia un archivo dentro de GCS
     * @param rutaOrigenGCS Ruta del archivo origen
     * @param rutaDestinoGCS Ruta del archivo destino
     * @return Información del blob copiado
     * @throws IllegalArgumentException Si el archivo origen no existe
     */
    public Blob copiarArchivo(String rutaOrigenGCS, String rutaDestinoGCS) {
        BlobId origenBlobId = BlobId.of(gcsConfigService.getBucketName(), rutaOrigenGCS);
        BlobId destinoBlobId = BlobId.of(gcsConfigService.getBucketName(), rutaDestinoGCS);
        
        Blob origenBlob = storage.get(origenBlobId);
        if (origenBlob == null || !origenBlob.exists()) {
            throw new IllegalArgumentException("Archivo origen no encontrado en GCS: " + rutaOrigenGCS);
        }
        
        // Copiar blob manteniendo metadatos
        Storage.CopyRequest copyRequest = Storage.CopyRequest.newBuilder()
                .setSource(origenBlobId)
                .setTarget(destinoBlobId)
                .build();
        
        return storage.copy(copyRequest).getResult();
    }

    /**
     * Mueve un archivo dentro de GCS (copia + elimina origen)
     * @param rutaOrigenGCS Ruta del archivo origen
     * @param rutaDestinoGCS Ruta del archivo destino
     * @return Información del blob movido
     * @throws IllegalArgumentException Si el archivo origen no existe
     */
    public Blob moverArchivo(String rutaOrigenGCS, String rutaDestinoGCS) {
        System.out.println("🚚 [FileStorageService] Iniciando movimiento de archivo:");
        System.out.println("   📍 Origen:  " + rutaOrigenGCS);
        System.out.println("   📍 Destino: " + rutaDestinoGCS);
        
        try {
            // Copiar archivo
            System.out.println("   📋 Copiando archivo...");
            Blob destinoBlob = copiarArchivo(rutaOrigenGCS, rutaDestinoGCS);
            System.out.println("   ✅ Archivo copiado exitosamente");
            
            // Eliminar archivo origen
            System.out.println("   🗑️  Eliminando archivo origen...");
            boolean eliminado = eliminarArchivoDeGCS(rutaOrigenGCS);
            System.out.println("   ✅ Archivo origen eliminado: " + eliminado);
            
            System.out.println("🚚 [FileStorageService] Movimiento completado exitosamente");
            return destinoBlob;
        } catch (Exception e) {
            System.err.println("❌ [FileStorageService] ERROR al mover archivo: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Genera una URL firmada para acceso temporal a un archivo privado
     * @param rutaGCS Ruta completa del archivo en GCS
     * @param duracionHoras Duración de validez de la URL en horas
     * @return URL firmada con acceso temporal
     * @throws IllegalArgumentException Si el archivo no existe
     */
    public URL obtenerUrlFirmada(String rutaGCS, int duracionHoras) {
        BlobId blobId = BlobId.of(gcsConfigService.getBucketName(), rutaGCS);
        Blob blob = storage.get(blobId);
        
        if (blob == null || !blob.exists()) {
            throw new IllegalArgumentException("Archivo no encontrado en GCS: " + rutaGCS);
        }
        
        return blob.signUrl(duracionHoras, TimeUnit.HOURS);
    }

    /**
     * Genera una URL firmada con duración por defecto (24 horas)
     * @param rutaGCS Ruta completa del archivo en GCS
     * @return URL firmada con acceso temporal
     */
    public URL obtenerUrlFirmada(String rutaGCS) {
        return obtenerUrlFirmada(rutaGCS, 24);
    }

    /**
     * Obtiene los metadatos de un archivo en GCS
     * @param rutaGCS Ruta completa del archivo en GCS
     * @return Map con los metadatos del archivo
     * @throws IllegalArgumentException Si el archivo no existe
     */
    public Map<String, String> obtenerMetadatos(String rutaGCS) {
        BlobId blobId = BlobId.of(gcsConfigService.getBucketName(), rutaGCS);
        Blob blob = storage.get(blobId);
        
        if (blob == null || !blob.exists()) {
            throw new IllegalArgumentException("Archivo no encontrado en GCS: " + rutaGCS);
        }
        
        Map<String, String> metadatos = new HashMap<>();
        metadatos.put("name", blob.getName());
        metadatos.put("size", String.valueOf(blob.getSize()));
        metadatos.put("contentType", blob.getContentType());
        metadatos.put("created", String.valueOf(blob.getCreateTime()));
        metadatos.put("updated", String.valueOf(blob.getUpdateTime()));
        metadatos.put("generation", String.valueOf(blob.getGeneration()));
        
        // Agregar metadata personalizada si existe
        if (blob.getMetadata() != null) {
            metadatos.putAll(blob.getMetadata());
        }
        
        return metadatos;
    }

    /**
     * Verifica si un archivo existe en GCS
     * @param rutaGCS Ruta completa del archivo en GCS
     * @return true si el archivo existe, false en caso contrario
     */
    public boolean existeArchivo(String rutaGCS) {
        BlobId blobId = BlobId.of(gcsConfigService.getBucketName(), rutaGCS);
        Blob blob = storage.get(blobId);
        return blob != null && blob.exists();
    }

    /**
     * Obtiene el tamaño de un archivo en GCS
     * @param rutaGCS Ruta completa del archivo en GCS
     * @return Tamaño del archivo en bytes
     * @throws IllegalArgumentException Si el archivo no existe
     */
    public long obtenerTamanio(String rutaGCS) {
        BlobId blobId = BlobId.of(gcsConfigService.getBucketName(), rutaGCS);
        Blob blob = storage.get(blobId);
        
        if (blob == null || !blob.exists()) {
            throw new IllegalArgumentException("Archivo no encontrado en GCS: " + rutaGCS);
        }
        
        return blob.getSize();
    }

    /**
     * Actualiza los metadatos de un archivo en GCS
     * @param rutaGCS Ruta completa del archivo en GCS
     * @param nuevosMetadatos Map con los nuevos metadatos
     * @return Información actualizada del blob
     * @throws IllegalArgumentException Si el archivo no existe
     */
    public Blob actualizarMetadatos(String rutaGCS, Map<String, String> nuevosMetadatos) {
        BlobId blobId = BlobId.of(gcsConfigService.getBucketName(), rutaGCS);
        Blob blob = storage.get(blobId);
        
        if (blob == null || !blob.exists()) {
            throw new IllegalArgumentException("Archivo no encontrado en GCS: " + rutaGCS);
        }
        
        return blob.toBuilder()
                .setMetadata(nuevosMetadatos)
                .build()
                .update();
    }

    /**
     * Mueve un archivo a la papelera (trash)
     * @param rutaGCS Ruta actual del archivo en GCS
     * @param tipoContenedor Tipo de contenedor (PROYECTO o REPOSITORIO)
     * @param containerId ID del contenedor
     * @param nodoId ID del nodo
     * @param nombreOriginal Nombre original del archivo
     * @return Ruta del archivo en trash
     */
    public String moverATrash(String rutaGCS, Nodo.ContainerType tipoContenedor, Long containerId, Long nodoId, String nombreOriginal) {
        String rutaTrash = gcsConfigService.construirRutaTrash(tipoContenedor, containerId, nodoId, nombreOriginal);
        moverArchivo(rutaGCS, rutaTrash);
        return rutaTrash;
    }

    /**
     * Restaura un archivo desde la papelera
     * @param rutaTrash Ruta del archivo en trash
     * @param rutaDestino Ruta destino para restaurar el archivo
     * @return Información del blob restaurado
     */
    public Blob restaurarDesdeTrash(String rutaTrash, String rutaDestino) {
        return moverArchivo(rutaTrash, rutaDestino);
    }
}
