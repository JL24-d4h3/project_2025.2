package org.project.project.repository;

import org.project.project.model.entity.GitHubIntegration;
import org.project.project.model.entity.GitHubSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GitHubSyncLogRepository extends JpaRepository<GitHubSyncLog, Long> {

    // Find by integration
    List<GitHubSyncLog> findByGithubIntegrationOrderByStartedAtDesc(GitHubIntegration githubIntegration);

    // Find by integration ID
    @Query("SELECT s FROM GitHubSyncLog s WHERE s.githubIntegration.githubIntegrationId = :integrationId " +
           "ORDER BY s.startedAt DESC")
    List<GitHubSyncLog> findByIntegrationIdOrderByStartedAtDesc(@Param("integrationId") Long integrationId);

    // Find recent logs by integration
    @Query("SELECT s FROM GitHubSyncLog s WHERE s.githubIntegration.githubIntegrationId = :integrationId " +
           "AND s.startedAt > :since ORDER BY s.startedAt DESC")
    List<GitHubSyncLog> findRecentByIntegrationId(@Param("integrationId") Long integrationId, 
                                                  @Param("since") LocalDateTime since);

    // Find by status
    List<GitHubSyncLog> findByStatusOrderByStartedAtDesc(GitHubSyncLog.SyncStatus status);

    // Find by integration and status
    @Query("SELECT s FROM GitHubSyncLog s WHERE s.githubIntegration.githubIntegrationId = :integrationId " +
           "AND s.status = :status ORDER BY s.startedAt DESC")
    List<GitHubSyncLog> findByIntegrationIdAndStatus(@Param("integrationId") Long integrationId,
                                                     @Param("status") GitHubSyncLog.SyncStatus status);

    // Find by sync type
    List<GitHubSyncLog> findBySyncTypeOrderByStartedAtDesc(GitHubSyncLog.SyncType syncType);

    // Find failed syncs
    @Query("SELECT s FROM GitHubSyncLog s WHERE s.status = 'FAILED' ORDER BY s.startedAt DESC")
    List<GitHubSyncLog> findFailedSyncs();

    // Find latest by integration
    @Query("SELECT s FROM GitHubSyncLog s WHERE s.githubIntegration.githubIntegrationId = :integrationId " +
           "ORDER BY s.startedAt DESC LIMIT 1")
    GitHubSyncLog findLatestByIntegrationId(@Param("integrationId") Long integrationId);

    // Find by triggered by user
    @Query("SELECT s FROM GitHubSyncLog s WHERE s.triggeredBy.usuarioId = :userId " +
           "ORDER BY s.startedAt DESC")
    List<GitHubSyncLog> findByTriggeredByUserId(@Param("userId") Long userId);

    // Find old logs
    @Query("SELECT s FROM GitHubSyncLog s WHERE s.startedAt < :before ORDER BY s.startedAt ASC")
    List<GitHubSyncLog> findOldLogs(@Param("before") LocalDateTime before);

    // Delete old logs
    void deleteByStartedAtBefore(LocalDateTime before);

    // Find by date range
    @Query("SELECT s FROM GitHubSyncLog s WHERE s.startedAt BETWEEN :start AND :end " +
           "ORDER BY s.startedAt DESC")
    List<GitHubSyncLog> findByDateRange(@Param("start") LocalDateTime start, 
                                       @Param("end") LocalDateTime end);

    // Get sync statistics by integration
    @Query("SELECT s.status, COUNT(s) FROM GitHubSyncLog s " +
           "WHERE s.githubIntegration.githubIntegrationId = :integrationId " +
           "GROUP BY s.status")
    List<Object[]> getSyncStatisticsByIntegrationId(@Param("integrationId") Long integrationId);

    // Count by integration and status
    @Query("SELECT COUNT(s) FROM GitHubSyncLog s " +
           "WHERE s.githubIntegration.githubIntegrationId = :integrationId AND s.status = :status")
    long countByIntegrationIdAndStatus(@Param("integrationId") Long integrationId,
                                      @Param("status") GitHubSyncLog.SyncStatus status);
}
