package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Equipo")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipo_id", nullable = false)
    private Integer equipoId;

    @Column(name = "nombre_equipo", nullable = false, length = 45)
    private String nombreEquipo;

    @ManyToMany(mappedBy = "equipos")
    private Set<Usuario> usuarios;

}
