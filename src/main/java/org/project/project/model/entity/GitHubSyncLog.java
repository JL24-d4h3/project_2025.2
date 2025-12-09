package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "github_sync_log")
public class GitHubSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sync_log_id")
    private Long syncLogId;
    
    // Alias para repository methods en inglés
    @Column(name = "sync_log_id", insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "github_integration_id", nullable = false)
    private GitHubIntegration githubIntegration;
    
    // Alias para repository methods en inglés
    @Column(name = "github_integration_id", insertable = false, updatable = false)
    private Long integrationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_type", nullable = false)
    private SyncType syncType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_direction", nullable = false)
    private SyncDirection syncDirection;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SyncStatus status;

    @Column(name = "commits_synced")
    private Integer commitsSynced = 0;

    @Column(name = "files_added")
    private Integer filesAdded = 0;

    @Column(name = "files_modified")
    private Integer filesModified = 0;

    @Column(name = "files_deleted")
    private Integer filesDeleted = 0;

    @Column(name = "total_size_bytes")
    private Long totalSizeBytes;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "error_details", columnDefinition = "JSON")
    private Map<String, Object> errorDetails;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_usuario_id")
    private Usuario triggeredBy;

    @Column(name = "commit_range", length = 200)
    private String commitRange; // ej: "abc123..def456"

    public enum SyncType {
        MANUAL,     // Iniciada manualmente por usuario
        WEBHOOK,    // Disparada por webhook de GitHub
        SCHEDULED,  // Programada automáticamente
        AUTO        // Auto-sincronización
    }

    public enum SyncDirection {
        GITHUB_TO_TELDEV,  // GitHub → TelDev
        TELDEV_TO_GITHUB,  // TelDev → GitHub
        BIDIRECTIONAL      // Bidireccional
    }

    public enum SyncStatus {
        SUCCESS,   // Exitosa
        PARTIAL,   // Parcialmente exitosa
        FAILED     // Falló
    }

    public GitHubSyncLog() {}

    public GitHubSyncLog(GitHubIntegration githubIntegration, SyncType syncType, 
                        SyncDirection syncDirection, Usuario triggeredBy) {
        this.githubIntegration = githubIntegration;
        this.syncType = syncType;
        this.syncDirection = syncDirection;
        this.triggeredBy = triggeredBy;
        this.startedAt = LocalDateTime.now();
        this.status = SyncStatus.SUCCESS; // Se actualiza al finalizar
    }

    public void complete(SyncStatus status) {
        this.completedAt = LocalDateTime.now();
        this.status = status;
        this.durationSeconds = (int) java.time.Duration.between(startedAt, completedAt).getSeconds();
    }

    public void completeWithError(String errorMessage, Map<String, Object> errorDetails) {
        this.completedAt = LocalDateTime.now();
        this.status = SyncStatus.FAILED;
        this.errorMessage = errorMessage;
        this.errorDetails = errorDetails;
        this.durationSeconds = (int) java.time.Duration.between(startedAt, completedAt).getSeconds();
    }

    public void updateProgress(int commits, int added, int modified, int deleted, long totalBytes) {
        this.commitsSynced = commits;
        this.filesAdded = added;
        this.filesModified = modified;
        this.filesDeleted = deleted;
        this.totalSizeBytes = totalBytes;
    }

    public int getTotalFilesAffected() {
        return filesAdded + filesModified + filesDeleted;
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    @Override
    public String toString() {
        return "GitHubSyncLog{" +
                "syncLogId=" + syncLogId +
                ", syncType=" + syncType +
                ", syncDirection=" + syncDirection +
                ", status=" + status +
                ", commitsSynced=" + commitsSynced +
                ", filesAffected=" + getTotalFilesAffected() +
                ", durationSeconds=" + durationSeconds +
                '}';
    }
}
