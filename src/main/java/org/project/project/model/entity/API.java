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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "API")
public class API {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "api_id", nullable = false)
    private Long apiId;

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

    @OneToOne(mappedBy = "api")
    private Documentacion documentacion;

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