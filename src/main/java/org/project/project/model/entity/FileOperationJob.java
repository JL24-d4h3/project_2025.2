package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "file_operation_job")
public class FileOperationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;
    
    // Alias para repository methods en inglés
    @Column(name = "job_id", insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    // Alias para repository methods en inglés
    @Column(name = "usuario_id", insertable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status = JobStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "nodo_ids", nullable = false, columnDefinition = "JSON")
    private List<Long> nodoIds;
    
    // Alias para repository methods en inglés
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "nodo_ids", insertable = false, updatable = false, columnDefinition = "JSON")
    private List<Long> nodeIds;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_container_type")
    private Nodo.ContainerType targetContainerType;

    @Column(name = "target_container_id")
    private Long targetContainerId;

    @Column(name = "target_parent_id")
    private Long targetParentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_enlace_id")
    private Enlace resultEnlace;

    @Column(name = "result_url", length = 2048)
    private String resultUrl;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "progress_percent")
    private Integer progressPercent = 0;

    @Column(name = "total_files")
    private Integer totalFiles = 0;

    @Column(name = "processed_files")
    private Integer processedFiles = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Alias para repository methods en inglés
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "JSON")
    private Map<String, Object> metadata;

    public enum OperationType {
        COMPRESS,        // Comprimir archivos en ZIP
        BULK_DOWNLOAD,   // Descargar múltiples archivos
        BULK_UPLOAD,     // Subir múltiples archivos
        MOVE,            // Mover archivos
        COPY,            // Copiar archivos
        DELETE_BULK      // Eliminar múltiples archivos
    }

    public enum JobStatus {
        PENDING,      // Esperando ser procesado
        PROCESSING,   // En proceso
        COMPLETED,    // Completado exitosamente
        FAILED,       // Falló
        CANCELLED     // Cancelado por usuario
    }

    public FileOperationJob() {}

    public FileOperationJob(Usuario usuario, OperationType operationType, List<Long> nodoIds) {
        this.usuario = usuario;
        this.operationType = operationType;
        this.nodoIds = nodoIds;
        this.status = JobStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.progressPercent = 0;
    }

    public void start() {
        this.status = JobStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
    }

    public void complete(String resultUrl) {
        this.status = JobStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.resultUrl = resultUrl;
        this.progressPercent = 100;
    }

    public void fail(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public void cancel() {
        this.status = JobStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }

    public void updateProgress(int processed, int total) {
        this.processedFiles = processed;
        this.totalFiles = total;
        this.progressPercent = total > 0 ? (processed * 100 / total) : 0;
    }

    public boolean isInProgress() {
        return status == JobStatus.PENDING || status == JobStatus.PROCESSING;
    }

    public boolean isCompleted() {
        return status == JobStatus.COMPLETED || status == JobStatus.FAILED || status == JobStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return "FileOperationJob{" +
                "jobId=" + jobId +
                ", operationType=" + operationType +
                ", status=" + status +
                ", progressPercent=" + progressPercent +
                ", totalFiles=" + totalFiles +
                ", processedFiles=" + processedFiles +
                '}';
    }
}
