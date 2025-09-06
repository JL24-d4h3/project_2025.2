package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Categoria")
public class Categoria {

    @Id
    @Column(name = "idCategoria", nullable = false)
    private Integer idCategoria;

    @Column(name = "nombre_categoria", nullable = false, length = 45)
    private String nombreCategoria;

    @ManyToMany(mappedBy = "categorias")
    private Set<API> apis;

    @ManyToMany(mappedBy = "categorias")
    private Set<Proyecto> proyectos;

    @ManyToMany(mappedBy = "categorias")
    private Set<Repositorio> repositorios;

    @ManyToMany
    @JoinTable(
        name = "Categoria_has_Notificacion",
        joinColumns = @JoinColumn(name = "Categoria_idCategoria"),
        inverseJoinColumns = @JoinColumn(name = "Notificacion_notificacion_id")
    )
    private Set<Notificacion> notificaciones;

}