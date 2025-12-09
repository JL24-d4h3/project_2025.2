package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "github_user_token")
public class GitHubUserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "github_token_id")
    private Long githubTokenId;
    
    // Alias para repository methods en inglés
    @Column(name = "github_token_id", insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    // Alias para repository methods en inglés
    @Column(name = "usuario_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "access_token", nullable = false, length = 512)
    private String accessToken; // DEBE ESTAR ENCRIPTADO en la aplicación

    @Column(name = "token_type", length = 50)
    private String tokenType = "bearer";

    @Column(name = "scope", length = 500)
    private String scope; // ej: "repo, read:user, workflow"

    @Column(name = "github_user_id")
    private Long githubUserId;

    @Column(name = "github_username")
    private String githubUsername;

    @Column(name = "github_email")
    private String githubEmail;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // NULL = no expira (classic personal access tokens)

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "is_valid")
    private Boolean isValid = true;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public GitHubUserToken() {}

    public GitHubUserToken(Usuario usuario, String accessToken, String scope) {
        this.usuario = usuario;
        this.accessToken = accessToken;
        this.scope = scope;
        this.tokenType = "bearer";
        this.createdAt = LocalDateTime.now();
        this.isValid = true;
    }

    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public void markAsInvalid() {
        this.isValid = false;
    }

    public void revoke() {
        this.isValid = false;
        this.revokedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        if (expiresAt == null) {
            return false; // Classic tokens no expiran
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return isValid && !isExpired() && revokedAt == null;
    }

    @Override
    public String toString() {
        return "GitHubUserToken{" +
                "githubTokenId=" + githubTokenId +
                ", userId=" + (usuario != null ? usuario.getUsuarioId() : null) +
                ", githubUsername='" + githubUsername + '\'' +
                ", isValid=" + isValid +
                ", isExpired=" + isExpired() +
                '}';
    }
}
