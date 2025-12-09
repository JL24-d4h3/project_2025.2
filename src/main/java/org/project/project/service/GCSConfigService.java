package org.project.project.service;

import com.google.cloud.storage.Storage;
import org.project.project.model.entity.Nodo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service para configuración y helpers de Google Cloud Storage
 * Proporciona métodos para construir rutas, validar bucket y obtener información de configuración
 */
@Service
public class GCSConfigService {

    @Autowired
    private Storage storage;

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcs.filesystem.bucket-name}")
    private String bucketName;

    @Value("${gcs.filesystem.prefix.proyectos}")
    private String proyectosPrefix;

    @Value("${gcs.filesystem.prefix.repositorios}")
    private String repositoriosPrefix;

    @Value("${gcs.filesystem.prefix.trash}")
    private String trashPrefix;

    @Value("${gcs.filesystem.prefix.temp}")
    private String tempPrefix;

    @Value("${gcs.filesystem.base-url}")
    private String baseUrl;

    /**
     * Obtiene la ruta completa en GCS para un nodo
     * @param nodo El nodo para el cual construir la ruta
     * @param rutaCarpetas Ruta de carpetas desde la raíz (ej: "carpeta1/carpeta2/")
     * @return Ruta completa en formato: proyectos/{id}/carpeta1/carpeta2/archivo.txt
     */
    public String obtenerRutaCompleta(Nodo nodo, String rutaCarpetas) {
        String prefix = obtenerPrefijoPorTipo(nodo.getContainerType());
        String containerId = nodo.getContainerId().toString();
        
        StringBuilder ruta = new StringBuilder(prefix);
        ruta.append(containerId).append("/");
        
        if (rutaCarpetas != null && !rutaCarpetas.isEmpty() && !rutaCarpetas.equals("/")) {
            String rutaLimpia = rutaCarpetas.startsWith("/") ? rutaCarpetas.substring(1) : rutaCarpetas;
            if (!rutaLimpia.isEmpty()) {
                ruta.append(rutaLimpia);
                if (!rutaLimpia.endsWith("/")) {
                    ruta.append("/");
                }
            }
        }
        
        ruta.append(nodo.getNombre());
        
        return ruta.toString();
    }

    /**
     * Construye la ruta GCS completa para un nodo
     * @param nodo El nodo para el cual construir la ruta
     * @return Ruta completa en GCS
     */
    public String construirRutaGCS(Nodo nodo) {
        String prefix = obtenerPrefijoPorTipo(nodo.getContainerType());
        String containerId = nodo.getContainerId().toString();
        
        StringBuilder ruta = new StringBuilder(prefix);
        ruta.append(containerId);
        
        // Usar el path del nodo que incluye toda la jerarquía
        // El path ya viene con "/" al inicio (ej: "/src/main/java/Archivo.java")
        if (nodo.getPath() != null && !nodo.getPath().isEmpty()) {
            ruta.append(nodo.getPath());
        } else if (nodo.getNombre() != null) {
            // Fallback: usar solo el nombre si no hay path
            ruta.append("/").append(nodo.getNombre());
        }
        
        return ruta.toString();
    }

    /**
     * Construye la ruta base para un proyecto específico
     * @param proyectoId ID del proyecto
     * @return Ruta en formato: proyectos/{proyectoId}/
     */
    public String construirRutaProyecto(Long proyectoId) {
        return proyectosPrefix + proyectoId + "/";
    }

    /**
     * Construye la ruta base para un repositorio específico
     * @param repositorioId ID del repositorio
     * @return Ruta en formato: repositorios/{repositorioId}/
     */
    public String construirRutaRepositorio(Long repositorioId) {
        return repositoriosPrefix + repositorioId + "/";
    }

    /**
     * Construye la ruta para un repositorio dentro de un proyecto
     * @param proyectoId ID del proyecto padre
     * @param repositorioId ID del repositorio
     * @return Ruta en formato: proyectos/{proyectoId}/repositorios/{repositorioId}/
     */
    public String construirRutaRepositorioEnProyecto(Long proyectoId, Long repositorioId) {
        return proyectosPrefix + proyectoId + "/" + repositoriosPrefix + repositorioId + "/";
    }

    /**
     * Construye la ruta GCS completa para un nodo de repositorio dentro de un proyecto
     * @param nodo El nodo para el cual construir la ruta
     * @param proyectoId ID del proyecto padre (si aplica)
     * @return Ruta completa en GCS
     */
    public String construirRutaGCS(Nodo nodo, Long proyectoId) {
        StringBuilder ruta = new StringBuilder();
        
        // Si el nodo es de un repositorio Y tiene un proyectoId, usar la ruta anidada
        if (nodo.getContainerType() == Nodo.ContainerType.REPOSITORIO && proyectoId != null) {
            ruta.append(proyectosPrefix).append(proyectoId).append("/");
            ruta.append(repositoriosPrefix).append(nodo.getContainerId());
        } else {
            // Ruta normal (standalone)
            String prefix = obtenerPrefijoPorTipo(nodo.getContainerType());
            ruta.append(prefix).append(nodo.getContainerId());
        }
        
        // Agregar el path del nodo
        if (nodo.getPath() != null && !nodo.getPath().isEmpty()) {
            ruta.append(nodo.getPath());
        } else if (nodo.getNombre() != null) {
            ruta.append("/").append(nodo.getNombre());
        }
        
        return ruta.toString();
    }

    /**
     * Construye la ruta en trash para un nodo eliminado
     * @param tipoContenedor Tipo de contenedor (PROYECTO o REPOSITORIO)
     * @param containerId ID del contenedor
     * @param nodoId ID del nodo eliminado
     * @param nombreOriginal Nombre original del archivo/carpeta
     * @return Ruta en formato: trash/{tipo}/{containerId}/{nodoId}_{timestamp}_{nombre}
     */
    public String construirRutaTrash(Nodo.ContainerType tipoContenedor, Long containerId, Long nodoId, String nombreOriginal) {
        long timestamp = System.currentTimeMillis();
        return String.format("%s%s/%d/%d_%d_%s",
                trashPrefix,
                tipoContenedor.name().toLowerCase(),
                containerId,
                nodoId,
                timestamp,
                nombreOriginal);
    }

    /**
     * Construye una ruta temporal para operaciones como compresión o procesamiento
     * @param operationId ID de la operación
     * @param filename Nombre del archivo temporal
     * @return Ruta en formato: temp/{operationId}/{filename}
     */
    public String construirRutaTemporal(String operationId, String filename) {
        return tempPrefix + operationId + "/" + filename;
    }

    /**
     * Obtiene el prefijo correspondiente según el tipo de contenedor
     * @param tipo Tipo de contenedor (PROYECTO o REPOSITORIO)
     * @return Prefijo correspondiente
     */
    private String obtenerPrefijoPorTipo(Nodo.ContainerType tipo) {
        return tipo == Nodo.ContainerType.PROYECTO ? proyectosPrefix : repositoriosPrefix;
    }

    /**
     * Valida que el bucket de GCS esté configurado y accesible
     * @return true si el bucket es válido y accesible
     * @throws IllegalStateException si el bucket no existe o no es accesible
     */
    public boolean validarConfiguracion() {
        try {
            var bucket = storage.get(bucketName);
            if (bucket == null || !bucket.exists()) {
                throw new IllegalStateException("Bucket GCS no existe: " + bucketName);
            }
            return true;
        } catch (Exception e) {
            throw new IllegalStateException("Error validando bucket GCS: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene la URL pública base del bucket
     * @return URL base en formato: https://storage.googleapis.com/{bucket}/
     */
    public String obtenerUrlBase() {
        return baseUrl;
    }

    /**
     * Obtiene la URL pública completa para un objeto en GCS
     * @param rutaGCS Ruta completa del objeto en GCS
     * @return URL pública del objeto
     */
    public String obtenerUrlPublica(String rutaGCS) {
        return baseUrl + rutaGCS;
    }

    /**
     * Obtiene la duración por defecto para URLs firmadas (24 horas)
     * @return Duration de 24 horas
     */
    public Duration obtenerDuracionUrlFirmada() {
        return Duration.ofHours(24);
    }

    /**
     * Obtiene la duración personalizada para URLs firmadas
     * @param horas Número de horas de validez
     * @return Duration con las horas especificadas
     */
    public Duration obtenerDuracionUrlFirmada(int horas) {
        return Duration.ofHours(horas);
    }

    // Getters para acceso a configuración
    
    public String getBucketName() {
        return bucketName;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProyectosPrefix() {
        return proyectosPrefix;
    }

    public String getRepositoriosPrefix() {
        return repositoriosPrefix;
    }

    public String getTrashPrefix() {
        return trashPrefix;
    }

    public String getTempPrefix() {
        return tempPrefix;
    }
}

