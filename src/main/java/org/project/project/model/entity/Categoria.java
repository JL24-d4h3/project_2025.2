package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria", nullable = false)
    private Long idCategoria;

    // Alias para repository methods en inglés
    @Column(name = "id_categoria", insertable = false, updatable = false)
    private Long categoryId;

    @Column(name = "nombre_categoria", nullable = false, length = 45)
    private String nombreCategoria;

    // Alias en inglés para repository methods
    @Column(name = "nombre_categoria", nullable = false, length = 45, insertable = false, updatable = false)
    private String categoryName;

    @Column(name = "descripcion_categoria", nullable = false, length = 255)
    private String descripcionCategoria;

    // Alias en inglés para repository methods
    @Column(name = "descripcion_categoria", insertable = false, updatable = false)
    private String categoryDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "seccion_categoria", length = 20)
    private SeccionCategoria seccionCategoria;

    // Alias en inglés para repository methods
    @Enumerated(EnumType.STRING)
    @Column(name = "seccion_categoria", insertable = false, updatable = false)
    private SeccionCategoria categorySection;

    // Enum para las secciones de categoría
    public enum SeccionCategoria {
        APIS, PROYECTOS, REPOSITORIOS, FORO, OTRO
    }

    @ManyToMany(mappedBy = "categorias", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<API> apis;

    @ManyToMany(mappedBy = "categorias", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Proyecto> proyectos;

    @ManyToMany(mappedBy = "categorias", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Repositorio> repositorios;
}