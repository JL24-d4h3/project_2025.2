package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "proyecto")
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proyecto_id", nullable = false)
    private Long proyectoId;

    @Column(name = "nombre_proyecto", nullable = false, length = 128)
    private String nombreProyecto;
    
    // Alias para repository methods en inglés
    @Column(name = "nombre_proyecto", insertable = false, updatable = false)
    private String projectName;

    @Lob
    @Column(name = "descripcion_proyecto")
    private String descripcionProyecto;
    
    // Alias para repository methods en inglés
    @Lob
    @Column(name = "descripcion_proyecto", insertable = false, updatable = false)
    private String projectDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibilidad_proyecto", nullable = false)
    private VisibilidadProyecto visibilidadProyecto;
    
    // Alias para repository methods en inglés
    @Column(name = "visibilidad_proyecto", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private VisibilidadProyecto projectVisibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "acceso_proyecto", nullable = false)
    private AccesoProyecto accesoProyecto;
    
    // Alias para repository methods en inglés
    @Column(name = "acceso_proyecto", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private AccesoProyecto projectAccess;

    @Enumerated(EnumType.STRING)
    @Column(name = "propietario_proyecto", nullable = false)
    private PropietarioProyecto propietarioProyecto;
    
    // Alias para repository methods en inglés
    @Column(name = "propietario_proyecto", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private PropietarioProyecto projectOwner;

    @Column(name = "propietario_nombre", length = 128, nullable = false)
    private String propietarioNombre;
    
    // Alias en inglés para compatibilidad
    @Column(name = "propietario_nombre", insertable = false, updatable = false)
    private String ownerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_proyecto", nullable = false)
    private EstadoProyecto estadoProyecto;
    
    // Alias para repository methods en inglés
    @Column(name = "estado_proyecto", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EstadoProyecto projectStatus;

    @Column(name = "fecha_inicio_proyecto", nullable = false)
    private LocalDate fechaInicioProyecto;
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_inicio_proyecto", insertable = false, updatable = false)
    private LocalDate projectStartDate;

    @Column(name = "fecha_fin_proyecto")
    private LocalDate fechaFinProyecto;
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_fin_proyecto", insertable = false, updatable = false)
    private LocalDate projectEndDate;

    // Campos faltantes según BD
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Alias en español
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private Usuario createdBy;
    
    // Alias en español
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private Usuario creadoPor;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Alias en español
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime actualizadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private Usuario updatedBy;
    
    // Alias en español
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", insertable = false, updatable = false)
    private Usuario actualizadoPor;

    @Column(name = "slug", length = 200)
    private String slug;

    @Column(name = "proyecto_key", length = 64)
    private String proyectoKey;

    @Column(name = "root_node_id")
    private Long rootNodeId;

    // Relación con el nodo raíz del proyecto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_node_id", insertable = false, updatable = false)
    private Nodo rootNode;

    @Column(name = "web_url", length = 2048)
    private String webUrl;

    @ManyToMany(mappedBy = "proyectos", fetch = FetchType.LAZY)
    private Set<Usuario> usuarios;

    @ManyToMany
    @JoinTable(
            name = "categoria_has_proyecto",
            joinColumns = @JoinColumn(name = "proyecto_proyecto_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id_categoria")
    )
    private Set<Categoria> categorias;

    @ManyToMany
    @JoinTable(
            name = "proyecto_has_repositorio",
            joinColumns = @JoinColumn(name = "proyecto_proyecto_id"),
            inverseJoinColumns = @JoinColumn(name = "repositorio_repositorio_id")
    )
    private Set<Repositorio> repositorios;

    //Relacion con tickets del proyecto
    @OneToMany(mappedBy = "proyecto", fetch = FetchType.LAZY)
    private Set<Ticket> tickets;

    public enum VisibilidadProyecto {
        PUBLICO, PRIVADO
    }

    public enum AccesoProyecto {
        RESTRINGIDO, ORGANIZACION, CUALQUIER_PERSONA_CON_EL_ENLACE
    }

    public enum PropietarioProyecto {
        USUARIO, GRUPO, EMPRESA
    }

    public enum EstadoProyecto {
        PLANEADO, EN_DESARROLLO, MANTENIMIENTO, CERRADO
    }
}
