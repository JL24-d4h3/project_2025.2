package org.project.project.service;

import org.project.project.model.entity.FileOperationJob;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.FileOperationJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service para jobs asíncronos de operaciones de archivos
 * Maneja compresión, descarga masiva y otras operaciones pesadas
 * Métodos en español siguiendo convención
 */
@Service
public class FileOperationJobService {

    @Autowired
    private FileOperationJobRepository jobRepository;

    /**
     * Crea un job para comprimir archivos/carpetas
     * @param nodoIds Lista de IDs de nodos a comprimir
     * @param usuario Usuario que solicita la compresión
     * @return Job creado
     */
    @Transactional
    public FileOperationJob crearJobCompresion(List<Long> nodoIds, Usuario usuario) {
        FileOperationJob job = new FileOperationJob();
        job.setUsuario(usuario);
        job.setOperationType(FileOperationJob.OperationType.COMPRESS);
        job.setStatus(FileOperationJob.JobStatus.PENDING);
        job.setProgressPercent(0);
        
        // Metadata con información adicional
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("nodoIds", nodoIds);
        metadata.put("totalFiles", nodoIds.size());
        job.setMetadata(metadata);

        return jobRepository.save(job);
    }

    /**
     * Crea un job para descarga masiva de archivos
     * @param nodoIds Lista de IDs de nodos a descargar
     * @param usuario Usuario que solicita la descarga
     * @return Job creado
     */
    @Transactional
    public FileOperationJob crearJobDescargaMasiva(List<Long> nodoIds, Usuario usuario) {
        FileOperationJob job = new FileOperationJob();
        job.setUsuario(usuario);
        job.setOperationType(FileOperationJob.OperationType.BULK_DOWNLOAD);
        job.setStatus(FileOperationJob.JobStatus.PENDING);
        job.setProgressPercent(0);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("nodoIds", nodoIds);
        metadata.put("totalFiles", nodoIds.size());
        job.setMetadata(metadata);

        return jobRepository.save(job);
    }

    /**
     * Crea un job para conversión de formato
     * @param nodoId ID del nodo a convertir
     * @param formatoDestino Formato de destino
     * @param usuario Usuario que solicita la conversión
     * @return Job creado
     */
    @Transactional
    public FileOperationJob crearJobConversion(Long nodoId, String formatoDestino, Usuario usuario) {
        FileOperationJob job = new FileOperationJob();
        job.setUsuario(usuario);
        job.setOperationType(FileOperationJob.OperationType.COMPRESS);
        job.setStatus(FileOperationJob.JobStatus.PENDING);
        job.setProgressPercent(0);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("nodoId", nodoId);
        metadata.put("formatoDestino", formatoDestino);
        job.setMetadata(metadata);

        return jobRepository.save(job);
    }

    /**
     * Procesa un job de forma asíncrona
     * @param jobId ID del job a procesar
     */
    @Async("fileOperationExecutor")
    @Transactional
    public void procesarJob(Long jobId) {
        Optional<FileOperationJob> optionalJob = jobRepository.findById(jobId);
        if (optionalJob.isEmpty()) {
            return;
        }

        FileOperationJob job = optionalJob.get();
        
        try {
            // Marcar como en progreso
            job.start();
            jobRepository.save(job);

            // Procesar según el tipo de operación
            switch (job.getOperationType()) {
                case COMPRESS:
                    procesarCompresion(job);
                    break;
                case BULK_DOWNLOAD:
                    procesarDescargaMasiva(job);
                    break;
                case MOVE:
                    procesarMovimiento(job);
                    break;
                case DELETE_BULK:
                    procesarEliminacionMasiva(job);
                    break;
                default:
                    throw new IllegalStateException("Tipo de operación no soportado");
            }

            // Marcar como completado
            job.complete(null);
            jobRepository.save(job);

        } catch (Exception e) {
            job.fail("Error procesando job: " + e.getMessage());
            jobRepository.save(job);
        }
    }

    /**
     * Procesa la compresión de archivos (implementación placeholder)
     */
    private void procesarCompresion(FileOperationJob job) {
        // TODO: Implementar lógica de compresión
        // 1. Obtener nodos del metadata
        // 2. Descargar archivos de GCS
        // 3. Crear ZIP
        // 4. Subir ZIP a GCS
        // 5. Crear enlace temporal
        // 6. Actualizar job con resultEnlace
        
        // Simulación de progreso
        for (int i = 0; i <= 100; i += 10) {
            job.updateProgress(i, 100);
            jobRepository.save(job);
            try {
                Thread.sleep(100); // Simular trabajo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Procesa la descarga masiva de archivos (implementación placeholder)
     */
    private void procesarDescargaMasiva(FileOperationJob job) {
        // TODO: Implementar lógica de descarga masiva
        // Similar a compresión pero puede incluir carpetas completas
        
        for (int i = 0; i <= 100; i += 10) {
            job.updateProgress(i, 100);
            jobRepository.save(job);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Procesa la conversión de formato (implementación placeholder)
     */
    private void procesarConversion(FileOperationJob job) {
        // TODO: Implementar lógica de conversión
        // Usar librerías de conversión según el formato
        
        for (int i = 0; i <= 100; i += 10) {
            job.updateProgress(i, 100);
            jobRepository.save(job);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Procesa el movimiento de archivos (implementación placeholder)
     */
    private void procesarMovimiento(FileOperationJob job) {
        // TODO: Implementar lógica de movimiento
        // Mover nodos de una carpeta a otra
        
        for (int i = 0; i <= 100; i += 10) {
            job.updateProgress(i, 100);
            jobRepository.save(job);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Procesa la eliminación masiva (implementación placeholder)
     */
    private void procesarEliminacionMasiva(FileOperationJob job) {
        // TODO: Implementar lógica de eliminación masiva
        // Eliminar múltiples nodos
        
        for (int i = 0; i <= 100; i += 10) {
            job.updateProgress(i, 100);
            jobRepository.save(job);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Procesa la extracción de archivos comprimidos (implementación placeholder)
     */
    private void procesarExtraccion(FileOperationJob job) {
        // TODO: Implementar lógica de extracción
        // Descomprimir ZIP/RAR/TAR y crear nodos
        
        for (int i = 0; i <= 100; i += 10) {
            job.updateProgress(i, 100);
            jobRepository.save(job);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Obtiene el estado de un job
     * @param jobId ID del job
     * @return Optional con el job si existe
     */
    public Optional<FileOperationJob> obtenerEstadoJob(Long jobId) {
        return jobRepository.findById(jobId);
    }

    /**
     * Obtiene todos los jobs de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de jobs
     */
    public List<FileOperationJob> obtenerJobsPorUsuario(Long usuarioId) {
        return jobRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId);
    }

    /**
     * Obtiene los jobs pendientes del sistema
     * @return Lista de jobs pendientes
     */
    public List<FileOperationJob> obtenerJobsPendientes() {
        return jobRepository.findPendingJobs();
    }

    /**
     * Obtiene los jobs en progreso del sistema
     * @return Lista de jobs en progreso
     */
    public List<FileOperationJob> obtenerJobsEnProgreso() {
        return jobRepository.findInProgressJobs();
    }

    /**
     * Cancela un job
     * @param jobId ID del job a cancelar
     * @return true si se canceló exitosamente
     */
    @Transactional
    public boolean cancelarJob(Long jobId) {
        Optional<FileOperationJob> optionalJob = jobRepository.findById(jobId);
        if (optionalJob.isEmpty()) {
            return false;
        }

        FileOperationJob job = optionalJob.get();
        
        if (job.getStatus() == FileOperationJob.JobStatus.COMPLETED ||
            job.getStatus() == FileOperationJob.JobStatus.FAILED ||
            job.getStatus() == FileOperationJob.JobStatus.CANCELLED) {
            return false; // No se puede cancelar un job ya finalizado
        }

        job.cancel();
        jobRepository.save(job);
        return true;
    }

    /**
     * Limpia jobs antiguos completados/fallidos (ejecutado por evento programado)
     * @param diasAntiguedad Días de antigüedad para considerar un job como antiguo
     */
    @Transactional
    public void limpiarJobsAntiguos(int diasAntiguedad) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(diasAntiguedad);
        List<FileOperationJob.JobStatus> statusToClean = List.of(
                FileOperationJob.JobStatus.COMPLETED,
                FileOperationJob.JobStatus.FAILED,
                FileOperationJob.JobStatus.CANCELLED
        );
        jobRepository.deleteByStatusInAndCompletedAtBefore(statusToClean, cutoffDate);
    }

    /**
     * Reinicia un job fallido
     * @param jobId ID del job a reintentar
     * @return Job reiniciado
     */
    @Transactional
    public Optional<FileOperationJob> reintentarJob(Long jobId) {
        Optional<FileOperationJob> optionalJob = jobRepository.findById(jobId);
        if (optionalJob.isEmpty()) {
            return Optional.empty();
        }

        FileOperationJob job = optionalJob.get();
        
        if (job.getStatus() != FileOperationJob.JobStatus.FAILED) {
            return Optional.empty(); // Solo se pueden reintentar jobs fallidos
        }

        job.setStatus(FileOperationJob.JobStatus.PENDING);
        job.setProgressPercent(0);
        job.setErrorMessage(null);
        job.setStartedAt(null);
        job.setCompletedAt(null);
        
        return Optional.of(jobRepository.save(job));
    }
}





