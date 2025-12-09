package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "enlace")
public class Enlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enlace_id", nullable = false)
    private Long enlaceId;
    
    // Alias para repository methods en inglés
    @Column(name = "enlace_id", insertable = false, updatable = false)
    private Long linkId;

    @Column(name = "direccion_almacenamiento", nullable = false, columnDefinition = "TEXT")
    private String direccionAlmacenamiento;
    
    // Alias para repository methods en inglés
    @Column(name = "direccion_almacenamiento", insertable = false, updatable = false, columnDefinition = "TEXT")
    private String storageAddress;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "fecha_creacion_enlace", nullable = false)
    private LocalDateTime fechaCreacionEnlace = LocalDateTime.now();

    @Column(name = "fecha_modificacion_enlace")
    private LocalDateTime fechaModificacionEnlace;

    @Enumerated(EnumType.STRING)
    @Column(name = "contexto_type", nullable = false)
    private ContextoType contextoType = ContextoType.REPOSITORIO;
    
    // Alias para repository methods en inglés
    @Column(name = "contexto_type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ContextoType contextType;

    @Column(name = "contexto_id", nullable = false)
    private Long contextoId;
    
    // Alias para repository methods en inglés
    @Column(name = "contexto_id", insertable = false, updatable = false)
    private Long contextId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_enlace", nullable = false)
    private TipoEnlace tipoEnlace = TipoEnlace.STORAGE;
    
    // Alias para repository methods en inglés
    @Column(name = "tipo_enlace", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TipoEnlace linkType;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_enlace", nullable = false)
    private EstadoEnlace estadoEnlace = EstadoEnlace.ACTIVO;

    @Column(name = "nosql_metadata_id", length = 24)
    private String nosqlMetadataId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    
    // Alias para repository methods en inglés
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", insertable = false, updatable = false)
    private Usuario createdBy;

    @OneToMany(mappedBy = "enlace", fetch = FetchType.LAZY)
    private Set<Recurso> recursos;

    public enum ContextoType {
        REPOSITORIO, PROYECTO, NODO, FILE_VERSION, CONTENIDO, RECURSO, DOCUMENTACION, API, TICKET, VERSION_API
    }

    public enum TipoEnlace {
        STORAGE, METADATA, THUMBNAIL, BACKUP, TEMPORAL, TEXTO_CONTENIDO, ENLACE_EXTERNO
    }

    public enum EstadoEnlace {
        ACTIVO, ARCHIVADO, ELIMINADO, PROCESANDO
    }
}