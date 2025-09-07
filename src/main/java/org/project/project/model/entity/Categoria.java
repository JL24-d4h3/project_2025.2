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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria", nullable = false)
    private Long idCategoria;

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
        joinColumns = @JoinColumn(name = "categoria_id_categoria"),
        inverseJoinColumns = @JoinColumn(name = "Notificacion_notificacion_id")
    )
    private Set<Notificacion> notificaciones;

}