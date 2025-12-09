package org.project.project.repository;

import org.project.project.model.entity.GitHubUserToken;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GitHubUserTokenRepository extends JpaRepository<GitHubUserToken, Long> {

    // Find by user
    List<GitHubUserToken> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    // Find by user ID
    @Query("SELECT t FROM GitHubUserToken t WHERE t.usuario.usuarioId = :userId " +
           "ORDER BY t.createdAt DESC")
    List<GitHubUserToken> findByUserId(@Param("userId") Long userId);

    // Find valid token by user
    @Query("SELECT t FROM GitHubUserToken t WHERE t.usuario.usuarioId = :userId " +
           "AND t.isValid = true AND t.revokedAt IS NULL " +
           "ORDER BY t.createdAt DESC")
    List<GitHubUserToken> findValidByUserId(@Param("userId") Long userId);

    // Find active (valid and not expired) token by user
    @Query("SELECT t FROM GitHubUserToken t WHERE t.usuario.usuarioId = :userId " +
           "AND t.isValid = true AND t.revokedAt IS NULL " +
           "AND (t.expiresAt IS NULL OR t.expiresAt > CURRENT_TIMESTAMP) " +
           "ORDER BY t.createdAt DESC")
    Optional<GitHubUserToken> findActiveByUserId(@Param("userId") Long userId);

    // Find by user and is valid
    List<GitHubUserToken> findByUsuarioAndIsValidTrueOrderByCreatedAtDesc(Usuario usuario);

    // Find by GitHub username
    Optional<GitHubUserToken> findByGithubUsernameAndIsValidTrue(String githubUsername);

    // Find by GitHub user ID
    Optional<GitHubUserToken> findByGithubUserIdAndIsValidTrue(Long githubUserId);

    // Find all valid tokens
    @Query("SELECT t FROM GitHubUserToken t WHERE t.isValid = true AND t.revokedAt IS NULL " +
           "ORDER BY t.createdAt DESC")
    List<GitHubUserToken> findAllValid();

    // Find expired tokens
    @Query("SELECT t FROM GitHubUserToken t WHERE t.isValid = true " +
           "AND t.expiresAt IS NOT NULL AND t.expiresAt < CURRENT_TIMESTAMP")
    List<GitHubUserToken> findExpiredTokens();

    // Check if user has valid token
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM GitHubUserToken t " +
           "WHERE t.usuario.usuarioId = :userId AND t.isValid = true AND t.revokedAt IS NULL " +
           "AND (t.expiresAt IS NULL OR t.expiresAt > CURRENT_TIMESTAMP)")
    boolean hasValidToken(@Param("userId") Long userId);

    // Count valid tokens by user
    @Query("SELECT COUNT(t) FROM GitHubUserToken t WHERE t.usuario.usuarioId = :userId " +
           "AND t.isValid = true AND t.revokedAt IS NULL")
    long countValidByUserId(@Param("userId") Long userId);

    // Delete by user
    void deleteByUsuario(Usuario usuario);
}
