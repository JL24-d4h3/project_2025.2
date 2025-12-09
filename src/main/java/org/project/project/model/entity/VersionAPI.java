package org.project.project.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "version_api", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"api_api_id", "numero_version"})
       })
public class VersionAPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id", nullable = false)
    private Long versionId;
    
    // Alias para repository methods en inglés
    @Column(name = "version_id", insertable = false, updatable = false)
    private Long apiVersionId;

    @Column(name = "numero_version", nullable = false, length = 45)
    private String numeroVersion;
    
    // Alias para repository methods en inglés
    @Column(name = "numero_version", insertable = false, updatable = false)
    private String versionNumber;

    @Lob
    @Column(name = "descripcion_version")
    private String descripcionVersion;
    
    // Alias para repository methods en inglés
    @Lob
    @Column(name = "descripcion_version", insertable = false, updatable = false)
    private String versionDescription;

    @Lob
    @Column(name = "contrato_api_url", nullable = false)
    private String contratoApiUrl;
    
    // Alias para repository methods en inglés
    @Lob
    @Column(name = "contrato_api_url", insertable = false, updatable = false)
    private String apiContractUrl;

    @Column(name = "fecha_lanzamiento", nullable = false)
    private LocalDate fechaLanzamiento;
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_lanzamiento", insertable = false, updatable = false)
    private LocalDate releaseDate;

    @ManyToOne
    @JoinColumn(name = "api_api_id", nullable = false)
    private API api;

    @ManyToOne
    @JoinColumn(name = "documentacion_documentacion_id", nullable = false)
    private Documentacion documentacion;

    // Auditoría
    @ManyToOne
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    
    // Alias para repository methods en inglés
    @ManyToOne
    @JoinColumn(name = "creado_por", insertable = false, updatable = false)
    private Usuario createdBy;
    
    // Alias legacy compatibility
    @ManyToOne
    @JoinColumn(name = "creado_por", insertable = false, updatable = false)
    private Usuario creador;
    
    // Alias para repository methods en inglés
    @Column(name = "creado_por", insertable = false, updatable = false)
    private Long creadoPorUsuarioId;

    @Column(name = "creado_en", nullable = false)
    private java.time.LocalDateTime creadoEn;
    
    // Alias para repository methods en inglés
    @Column(name = "creado_en", insertable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;
    
    // Alias para repository methods en inglés
    @ManyToOne
    @JoinColumn(name = "actualizado_por", insertable = false, updatable = false)
    private Usuario updatedBy;

    @Column(name = "actualizado_en")
    private java.time.LocalDateTime actualizadoEn;
    
    // Alias para repository methods en inglés
    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private java.time.LocalDateTime updatedAt;

    // ==================== CAMPOS DE DEPLOYMENT Y AUTENTICACIÓN ====================
    
    @Column(name = "docker_image_url", length = 500)
    private String dockerImageUrl;

    @Column(name = "cloud_run_url", length = 500)
    private String cloudRunUrl;

    @Column(name = "deployment_id")
    private Long deploymentId;

    @Column(name = "fecha_ultimo_deployment")
    private LocalDateTime fechaUltimoDeployment;

    @Enumerated(EnumType.STRING)
    @Column(name = "deployment_status", length = 20)
    private DeploymentStatus deploymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_version", nullable = false, length = 20)
    private EstadoVersion estadoVersion = EstadoVersion.DRAFT;

    @Column(name = "requiere_autenticacion", nullable = false)
    private Boolean requiereAutenticacion = true;

    // ==================== ENUMS ====================
    
    public enum DeploymentStatus {
        PENDIENTE,
        DEPLOYING,
        ACTIVE,
        ERROR,
        INACTIVE
    }

    public enum EstadoVersion {
        DRAFT,
        PUBLISHED,
        DEPRECATED
    }

    // ==================== RELACIONES ====================

    @OneToMany(mappedBy = "versionApi")
    private Set<Contenido> contenidos;

}