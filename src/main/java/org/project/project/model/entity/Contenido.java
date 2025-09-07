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
@Table(name = "Contenido")
public class Contenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contenido_id", nullable = false)
    private Long contenidoId;

    @Column(name = "titulo_contenido", nullable = false, length = 45)
    private String tituloContenido;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "Clasificacion_clasificacion_id", nullable = false)
    private Clasificacion clasificacion;

    @ManyToOne
    @JoinColumn(name = "Documentacion_documentacion_id", nullable = false)
    private Documentacion documentacion;

    @ManyToOne
    @JoinColumn(name = "Version_API_version_id", nullable = false)
    private VersionAPI versionApi;

    @OneToMany(mappedBy = "contenido")
    private Set<Recurso> recursos;

}