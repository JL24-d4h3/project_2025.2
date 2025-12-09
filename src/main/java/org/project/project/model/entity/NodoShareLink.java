package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "nodo_share_link")
public class NodoShareLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_link_id")
    private Long shareLinkId;
    
    // Alias para repository methods en inglés
    @Column(name = "share_link_id", insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodo_id", nullable = false)
    private Nodo nodo;
    
    // Alias para repository methods en inglés
    @Column(name = "nodo_id", insertable = false, updatable = false)
    private Long nodeId;

    @Column(name = "share_token", nullable = false, unique = true, length = 64)
    private String shareToken; // UUID o hash aleatorio

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Usuario createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // NULL = no expira

    @Column(name = "password_hash")
    private String passwordHash; // Opcional: proteger con contraseña

    @Column(name = "max_downloads")
    private Integer maxDownloads; // NULL = ilimitado

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "allow_download")
    private Boolean allowDownload = true;

    @Column(name = "allow_preview")
    private Boolean allowPreview = true;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    public NodoShareLink() {}

    public NodoShareLink(Nodo nodo, Usuario createdBy, String shareToken) {
        this.nodo = nodo;
        this.createdBy = createdBy;
        this.shareToken = shareToken;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.allowDownload = true;
        this.allowPreview = true;
        this.downloadCount = 0;
    }

    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    public void deactivate() {
        this.isActive = false;
        this.deactivatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.deactivatedAt = null;
    }

    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasReachedMaxDownloads() {
        if (maxDownloads == null) {
            return false;
        }
        return downloadCount >= maxDownloads;
    }

    public boolean isAccessible() {
        return isActive && !isExpired() && !hasReachedMaxDownloads();
    }

    public boolean requiresPassword() {
        return passwordHash != null && !passwordHash.isEmpty();
    }

    public int getRemainingDownloads() {
        if (maxDownloads == null) {
            return -1; // Ilimitado
        }
        return Math.max(0, maxDownloads - downloadCount);
    }

    @Override
    public String toString() {
        return "NodoShareLink{" +
                "shareLinkId=" + shareLinkId +
                ", shareToken='" + shareToken + '\'' +
                ", nodoId=" + (nodo != null ? nodo.getNodoId() : null) +
                ", isActive=" + isActive +
                ", downloadCount=" + downloadCount +
                ", maxDownloads=" + maxDownloads +
                ", isExpired=" + isExpired() +
                '}';
    }
}
