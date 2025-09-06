package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "API")
public class API {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "api_id", nullable = false)
    private Integer apiId;

    @Column(name = "nombre_api", nullable = false, length = 45)
    private String nombreApi;

    @Lob
    @Column(name = "descripcion_api", nullable = false)
    private String descripcionApi;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_api", nullable = false)
    private EstadoApi estadoApi;

    @Column(name = "fecha_creacion_api", nullable = false)
    private LocalDateTime fechaCreacionApi;

    @OneToMany(mappedBy = "api")
    private Set<SolicitudAccesoAPI> solicitudesAccesoAPI;

    @OneToMany(mappedBy = "api")
    private Set<CredencialAPI> credenciales;

    @OneToMany(mappedBy = "api")
    private Set<VersionAPI> versiones;

    @OneToMany(mappedBy = "api")
    private Set<Documentacion> documentaciones;

    @ManyToMany
    @JoinTable(
        name = "Categoria_has_api",
        joinColumns = @JoinColumn(name = "API_api_id"),
        inverseJoinColumns = @JoinColumn(name = "Categoria_idCategoria")
    )
    private Set<Categoria> categorias;

    @ManyToMany
    @JoinTable(
        name = "Etiqueta_has_api",
        joinColumns = @JoinColumn(name = "API_api_id"),
        inverseJoinColumns = @JoinColumn(name = "Etiqueta_tag_id")
    )
    private Set<Etiqueta> etiquetas;

    public enum EstadoApi {
        PRODUCCION, QA, DEPRECATED
    }
}