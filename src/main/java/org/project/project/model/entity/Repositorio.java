package org.project.project.model.entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "repositorio")
public class Repositorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repositorio_id", nullable = false)
    private Long repositorioId;

    @Column(name = "nombre_repositorio", nullable = false, length = 128)
    private String nombreRepositorio;
    
    // Alias para repository methods en inglés
    @Column(name = "nombre_repositorio", insertable = false, updatable = false)
    private String repositoryName;

    @Lob
    @Column(name = "descripcion_repositorio")
    private String descripcionRepositorio;
    
    // Alias para repository methods en inglés
    @Lob
    @Column(name = "descripcion_repositorio", insertable = false, updatable = false)
    private String repositoryDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibilidad_repositorio", nullable = false)
    private VisibilidadRepositorio visibilidadRepositorio;
    
    // Alias para repository methods en inglés
    @Column(name = "visibilidad_repositorio", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private VisibilidadRepositorio repositoryVisibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_repositorio")
    private TipoRepositorio tipoRepositorio;
    
    // Alias para repository methods en inglés
    @Column(name = "tipo_repositorio", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TipoRepositorio repositoryType;

    @Column(name = "propietario_id")
    private Long propietarioId;
    
    // Alias para repository methods en inglés
    @Column(name = "propietario_id", insertable = false, updatable = false)
    private Long ownerId;

    @Column(name = "creado_por_usuario_id")
    private Long creadoPorUsuarioId;
    
    // Alias para repository methods en inglés
    @Column(name = "creado_por_usuario_id", insertable = false, updatable = false)
    private Long createdByUserId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime creationDate;

    @Column(name = "actualizado_por_usuario_id")
    private Long actualizadoPorUsuarioId;

    // Alias para repository methods en inglés
    @Column(name = "actualizado_por_usuario_id", insertable = false, updatable = false)
    private Long updatedByUserId;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    private LocalDateTime updateDate;

    @Column(name = "rama_principal_repositorio", nullable = false, length = 45)
    private String ramaPrincipalRepositorio;
    
    // Alias para repository methods en inglés
    @Column(name = "rama_principal_repositorio", insertable = false, updatable = false)
    private String mainBranch;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "ultimo_commit_hash", length = 128)
    private String ultimoCommitHash;
    
    // Alias para repository methods en inglés
    @Column(name = "ultimo_commit_hash", insertable = false, updatable = false)
    private String lastCommitHash;

    @Column(name = "is_fork", nullable = false)
    private Boolean isFork = false;

    @Column(name = "forked_from_repo_id")
    private Long forkedFromRepoId;

    @Column(name = "license", length = 128)
    private String license;

    @Column(name = "root_node_id")
    private Long rootNodeId;

    // Relación con el nodo raíz del repositorio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_node_id", insertable = false, updatable = false)
    private Nodo rootNode;

    // Relación con el usuario creador (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_usuario_id", insertable = false, updatable = false)
    private Usuario creador;
    
    // Alias para repository methods en inglés
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_usuario_id", insertable = false, updatable = false)
    private Usuario creator;

    // Relación con el usuario actualizador (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por_usuario_id", insertable = false, updatable = false)
    private Usuario actualizador;
    
    // Alias para repository methods en inglés
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por_usuario_id", insertable = false, updatable = false)
    private Usuario updater;

    @ManyToMany(mappedBy = "repositorios")
    private Set<Usuario> usuarios;

    @ManyToMany
    @JoinTable(
        name = "categoria_has_repositorio",
        joinColumns = @JoinColumn(name = "repositorio_repositorio_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id_categoria")
    )
    private Set<Categoria> categorias;

    @ManyToMany(mappedBy = "repositorios")
    private Set<Proyecto> proyectos;

    public enum VisibilidadRepositorio {
        PUBLICO, PRIVADO
    }

    public enum TipoRepositorio {
        PERSONAL, COLABORATIVO
    }
}
