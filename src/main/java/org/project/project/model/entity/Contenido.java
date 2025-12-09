package org.project.project.model.entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "contenido")
public class Contenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contenido_id", nullable = false)
    private Long contenidoId;
    
    // Alias para repository methods en inglés
    @Column(name = "contenido_id", insertable = false, updatable = false)
    private Long contentId;

    @Column(name = "titulo_contenido", nullable = false, length = 255)
    private String tituloContenido;
    
    // Alias para repository methods en inglés
    @Column(name = "titulo_contenido", insertable = false, updatable = false)
    private String contentTitle;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;
    
    // Alias para repository methods en inglés
    @Column(name = "orden", insertable = false, updatable = false)
    private Integer order;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "clasificacion_clasificacion_id", nullable = false)
    private Clasificacion clasificacion;
    
    // Alias para repository methods en inglés
    @ManyToOne
    @JoinColumn(name = "clasificacion_clasificacion_id", nullable = false, insertable = false, updatable = false)
    private Clasificacion classification;

    @ManyToOne
    @JoinColumn(name = "documentacion_documentacion_id", nullable = false)
    private Documentacion documentacion;
    
    // Alias para repository methods en inglés
    @ManyToOne
    @JoinColumn(name = "documentacion_documentacion_id", nullable = false, insertable = false, updatable = false)
    private Documentacion documentation;

    @ManyToOne
    @JoinColumn(name = "version_api_version_id", nullable = false)
    private VersionAPI versionApi;
    
    // Alias para repository methods en inglés
    @ManyToOne
    @JoinColumn(name = "version_api_version_id", nullable = false, insertable = false, updatable = false)
    private VersionAPI apiVersion;

    @OneToMany(mappedBy = "contenido")
    private Set<Recurso> recursos;

}