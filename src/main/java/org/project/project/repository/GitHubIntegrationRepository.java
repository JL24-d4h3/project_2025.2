package org.project.project.repository;

import org.project.project.model.entity.GitHubIntegration;
import org.project.project.model.entity.Repositorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GitHubIntegrationRepository extends JpaRepository<GitHubIntegration, Long> {

    // Find by repository
    Optional<GitHubIntegration> findByRepositorio(Repositorio repositorio);

    // Find by repository ID
    @Query("SELECT g FROM GitHubIntegration g WHERE g.repositorio.repositorioId = :repositoryId")
    Optional<GitHubIntegration> findByRepositoryId(@Param("repositoryId") Long repositoryId);

    // Find by GitHub repository fullname
    Optional<GitHubIntegration> findByGithubRepositoryFullname(String githubRepositoryFullname);

    // Find by GitHub repo ID
    Optional<GitHubIntegration> findByGithubRepoId(Long githubRepoId);

    // Find active integrations
    List<GitHubIntegration> findByIsActiveTrueOrderByCreatedAtDesc();

    // Find by sync mode
    List<GitHubIntegration> findBySyncModeAndIsActiveTrue(GitHubIntegration.SyncMode syncMode);

    // Find integrations with auto-sync enabled
    @Query("SELECT g FROM GitHubIntegration g WHERE g.isActive = true " +
           "AND g.autoSyncEnabled = true ORDER BY g.lastSyncAt ASC NULLS FIRST")
    List<GitHubIntegration> findActiveWithAutoSyncEnabled();

    // Find integrations that need sync
    @Query("SELECT g FROM GitHubIntegration g WHERE g.isActive = true " +
           "AND g.autoSyncEnabled = true " +
           "AND (g.lastSyncAt IS NULL OR g.lastSyncAt < :beforeTime)")
    List<GitHubIntegration> findNeedingSync(@Param("beforeTime") LocalDateTime beforeTime);

    // Find by last sync status
    List<GitHubIntegration> findByLastSyncStatusAndIsActiveTrueOrderByLastSyncAtDesc(
            GitHubIntegration.SyncStatus lastSyncStatus);

    // Find by created by user
    @Query("SELECT g FROM GitHubIntegration g WHERE g.createdBy.usuarioId = :userId " +
           "ORDER BY g.createdAt DESC")
    List<GitHubIntegration> findByCreatedByUserId(@Param("userId") Long userId);

    // Check if repository has integration
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GitHubIntegration g " +
           "WHERE g.repositorio.repositorioId = :repositoryId AND g.isActive = true")
    boolean hasActiveIntegration(@Param("repositoryId") Long repositoryId);

    // Count active integrations
    long countByIsActiveTrue();

    // Find by webhook ID
    Optional<GitHubIntegration> findByWebhookId(String webhookId);
}
