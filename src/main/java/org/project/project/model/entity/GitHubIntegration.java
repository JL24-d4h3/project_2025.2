package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "github_integration")
public class GitHubIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "github_integration_id")
    private Long githubIntegrationId;
    
    // Alias para repository methods en inglés
    @Column(name = "github_integration_id", insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repositorio_id", nullable = false)
    private Repositorio repositorio;
    
    // Alias para repository methods en inglés
    @Column(name = "repositorio_id", insertable = false, updatable = false)
    private Long repositoryId;

    @Column(name = "github_repository_fullname", nullable = false)
    private String githubRepositoryFullname; // ej: "octocat/Hello-World"

    @Column(name = "github_repository_url", nullable = false, length = 512)
    private String githubRepositoryUrl;

    @Column(name = "github_repo_id")
    private Long githubRepoId; // ID numérico del repo en GitHub API

    @Column(name = "default_branch", length = 100)
    private String defaultBranch = "main";

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_mode")
    private SyncMode syncMode = SyncMode.API_ONLY;

    @Column(name = "auto_sync_enabled")
    private Boolean autoSyncEnabled = false;

    @Column(name = "sync_interval_minutes")
    private Integer syncIntervalMinutes = 60;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "last_sync_commit_hash", length = 40)
    private String lastSyncCommitHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_sync_status")
    private SyncStatus lastSyncStatus;

    @Column(name = "webhook_id", length = 100)
    private String webhookId;

    @Column(name = "webhook_secret")
    private String webhookSecret;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Usuario createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SyncMode {
        API_ONLY,      // Solo leer vía API (no clonar repo)
        WEBHOOK,       // Sincronización automática con webhooks
        CLONE_LOCAL    // Clonar repo completo y subir a GCS
    }

    public enum SyncStatus {
        SUCCESS,   // Sincronización exitosa
        FAILED,    // Falló
        PARTIAL    // Parcialmente exitosa
    }

    public GitHubIntegration() {}

    public GitHubIntegration(Repositorio repositorio, String githubRepositoryFullname, 
                           String githubRepositoryUrl, Usuario createdBy) {
        this.repositorio = repositorio;
        this.githubRepositoryFullname = githubRepositoryFullname;
        this.githubRepositoryUrl = githubRepositoryUrl;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.syncMode = SyncMode.API_ONLY;
        this.autoSyncEnabled = false;
    }

    public void updateLastSync(String commitHash, SyncStatus status) {
        this.lastSyncAt = LocalDateTime.now();
        this.lastSyncCommitHash = commitHash;
        this.lastSyncStatus = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void enableAutoSync(int intervalMinutes) {
        this.autoSyncEnabled = true;
        this.syncIntervalMinutes = intervalMinutes;
        this.updatedAt = LocalDateTime.now();
    }

    public void disableAutoSync() {
        this.autoSyncEnabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean needsSync() {
        if (!isActive || !autoSyncEnabled) {
            return false;
        }
        if (lastSyncAt == null) {
            return true;
        }
        LocalDateTime nextSync = lastSyncAt.plusMinutes(syncIntervalMinutes);
        return LocalDateTime.now().isAfter(nextSync);
    }

    @Override
    public String toString() {
        return "GitHubIntegration{" +
                "githubIntegrationId=" + githubIntegrationId +
                ", githubRepositoryFullname='" + githubRepositoryFullname + '\'' +
                ", syncMode=" + syncMode +
                ", isActive=" + isActive +
                ", lastSyncStatus=" + lastSyncStatus +
                '}';
    }
}
