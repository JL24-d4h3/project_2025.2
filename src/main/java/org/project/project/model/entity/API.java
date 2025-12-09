package org.project.project.model.entity;

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
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "api")
public class API {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "api_id", nullable = false)
    private Long apiId;

    @Column(name = "nombre_api", nullable = false, length = 45)
    private String nombreApi;
    
    // Alias en inglés para repository methods
    @Column(name = "nombre_api", insertable = false, updatable = false)
    private String apiName;

    @Lob
    @Column(name = "descripcion_api", nullable = false)
    private String descripcionApi;
    
    // Alias en inglés para repository methods
    @Lob
    @Column(name = "descripcion_api", insertable = false, updatable = false)
    private String apiDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_api", nullable = false)
    private EstadoApi estadoApi;
    
    // Alias en inglés para repository methods
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_api", nullable = false, insertable = false, updatable = false)
    private EstadoApi apiStatus;

    @ManyToOne
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    
    // Alias en inglés para repository methods
    @ManyToOne
    @JoinColumn(name = "creado_por", insertable = false, updatable = false)
    private Usuario createdBy;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
    
    // Alias en inglés para repository methods
    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;
    
    // Alias en inglés para repository methods
    @ManyToOne
    @JoinColumn(name = "actualizado_por", insertable = false, updatable = false)
    private Usuario updatedBy;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
    
    // Alias en inglés para repository methods
    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "api")
    private Set<SolicitudAccesoAPI> solicitudesAccesoAPI;

    @OneToMany(mappedBy = "api")
    private Set<CredencialAPI> credenciales;

    @OneToMany(mappedBy = "api")
    private Set<VersionAPI> versiones;

    @OneToOne(mappedBy = "api")
    private Documentacion documentacion;

    @ManyToMany
    @JoinTable(
        name = "categoria_has_api",
        joinColumns = @JoinColumn(name = "api_api_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id_categoria")
    )
    private Set<Categoria> categorias;
    
    // Alias en inglés para repository methods - apunta a la misma relación
    @ManyToMany
    @JoinTable(
        name = "categoria_has_api",
        joinColumns = @JoinColumn(name = "api_api_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id_categoria")
    )
    private Set<Categoria> categories;

    @ManyToMany
    @JoinTable(
        name = "etiqueta_has_api",
        joinColumns = @JoinColumn(name = "api_api_id"),
        inverseJoinColumns = @JoinColumn(name = "etiqueta_tag_id")
    )
    private Set<Etiqueta> etiquetas;
    
    // Alias en inglés para repository methods - apunta a la misma relación
    @ManyToMany
    @JoinTable(
        name = "etiqueta_has_api",
        joinColumns = @JoinColumn(name = "api_api_id"),
        inverseJoinColumns = @JoinColumn(name = "etiqueta_tag_id")
    )
    private Set<Etiqueta> tags;

    public enum EstadoApi {
        BORRADOR,    // API en construcción, solo visible para el creador
        QA,          // API en revisión por QA, no editable
        PRODUCCION,  // API aprobada y publicada en el catálogo
        DEPRECATED   // API obsoleta, marcada para eliminación
    }
}