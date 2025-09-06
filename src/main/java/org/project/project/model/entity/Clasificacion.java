package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Clasificacion")
public class Clasificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clasificacion_id", nullable = false)
    private Integer clasificacionId;

    @Column(name = "tipo_contenido_texto", length = 45)
    private String tipoContenidoTexto;

    @OneToMany(mappedBy = "clasificacion")
    private Set<Contenido> contenidos;

}