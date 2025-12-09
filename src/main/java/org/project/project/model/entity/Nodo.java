package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Getter
@Setter
@Entity
@Table(name = "nodo")
public class Nodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nodo_id")
    private Long nodoId;
    
    // Alias para repository methods en inglés
    @Column(name = "nodo_id", insertable = false, updatable = false)
    private Long nodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "container_type", nullable = false)
    private ContainerType containerType;

    @Column(name = "container_id", nullable = false)
    private Long containerId;

    @Column(name = "rama_id")
    private Long ramaId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoNodo tipo;

    @Column(name = "path", nullable = false, length = 2000)
    private String path;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "size_bytes")
    private Long size = 0L;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "gcs_path", length = 2048)
    private String gcsPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    
    // Alias para repository methods en inglés
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", insertable = false, updatable = false)
    private Usuario createdBy;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
    
    // Alias para repository methods en inglés
    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;
    
    // Alias para repository methods en inglés
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por", insertable = false, updatable = false)
    private Usuario updatedBy;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
    
    // Alias para repository methods en inglés
    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Self-referencing relationship for parent-child structure
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Nodo parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Nodo> children = new HashSet<>();

    // Relación con la rama (solo para nodos de repositorios)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rama_id", insertable = false, updatable = false)
    private RepositorioRama rama;

    // Relationships
    @OneToMany(mappedBy = "nodo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<VersionArchivo> fileVersions = new HashSet<>();

    @OneToMany(mappedBy = "nodo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PermisoNodo> permissions = new HashSet<>();

    @OneToMany(mappedBy = "nodo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<NodoTag> tags = new HashSet<>();

    public enum ContainerType {
        PROYECTO, REPOSITORIO
    }

    public enum TipoNodo {
        CARPETA, ARCHIVO
    }

    public Nodo() {}

    public Nodo(String nombre, TipoNodo tipo, String path, ContainerType containerType, Long containerId) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.path = path;
        this.containerType = containerType;
        this.containerId = containerId;
        this.creadoEn = LocalDateTime.now();
    }

    public boolean isDirectory() {
        return tipo == TipoNodo.CARPETA;
    }

    public boolean isFile() {
        return tipo == TipoNodo.ARCHIVO;
    }

    public void markAsDeleted() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Nodo{" +
                "nodoId=" + nodoId +
                ", containerType=" + containerType +
                ", containerId=" + containerId +
                ", nombre='" + nombre + '\'' +
                ", tipo=" + tipo +
                ", path='" + path + '\'' +
                '}';
    }
}