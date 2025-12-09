package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for version_archivo table
 * Manages file versions with storage metadata
 */
@Getter
@Setter
@Entity
@Table(name = "version_archivo")
public class VersionArchivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_archivo_id", nullable = false)
    private Long versionArchivoId;

    // English alias for repositories
    @Column(name = "version_archivo_id", insertable = false, updatable = false)
    private Long fileVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodo_id", nullable = false)
    private Nodo nodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enlace_id", nullable = false)
    private Enlace enlace;

    @Column(name = "version_label", length = 100)
    private String versionLabel;

    @Column(name = "storage_key", nullable = false, length = 2000)
    private String storageKey;

    @Column(name = "storage_bucket", length = 255)
    private String storageBucket;

    @Column(name = "checksum", length = 128)
    private String checksum;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    // English alias for repositories
    @Column(name = "creado_por", insertable = false, updatable = false)
    private Long createdBy;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    // English alias for repositories
    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;

    // English alias for repositories
    @Column(name = "actualizado_por", insertable = false, updatable = false)
    private Long updatedBy;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    // English alias for repositories
    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "vigente", nullable = false)
    private Boolean vigente = true;

    // English alias for repositories
    @Column(name = "vigente", insertable = false, updatable = false)
    private Boolean active;

    // Helper method
    public void markAsObsolete() {
        this.vigente = false;
        this.actualizadoEn = LocalDateTime.now();
    }
}
