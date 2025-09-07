package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Enlace")
public class Enlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enlace_id", nullable = false)
    private Integer enlaceId;

    @Lob
    @Column(name = "direccion_almacenamiento", nullable = false)
    private String direccionAlmacenamiento;

    @Column(name = "fecha_creacion_enlace", nullable = false)
    private LocalDateTime fechaCreacionEnlace;

    @ManyToOne
    @JoinColumn(name = "Repositorio_repositorio_id", nullable = false)
    private Repositorio repositorio;

    @OneToOne(mappedBy = "enlace")
    private Recurso recurso;

    @ManyToMany(mappedBy = "enlaces")
    private Set<VersionAPI> versionesApi;

}