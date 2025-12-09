package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categoria_has_proyecto", indexes = {
    @Index(name = "fk_categoria_has_proyecto_proyecto1_idx", columnList = "proyecto_proyecto_id"),
    @Index(name = "fk_categoria_has_proyecto_categoria1_idx", columnList = "categoria_id_categoria")
})
public class CategoriaHasProyecto {

    @EmbeddedId
    private CategoriaHasProyectoId id;

    @MapsId("categoriaId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id_categoria", nullable = false)
    private Categoria categoria;

    @MapsId("proyectoId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_proyecto_id", nullable = false)
    private Proyecto proyecto;

    public CategoriaHasProyecto() {}

    public CategoriaHasProyecto(Categoria categoria, Proyecto proyecto) {
        this.categoria = categoria;
        this.proyecto = proyecto;
        this.id = new CategoriaHasProyectoId(categoria.getIdCategoria(), proyecto.getProyectoId());
    }
    
    // Alias getters para repository methods en inglés
    public Categoria getCategory() {
        return categoria;
    }
    
    public Proyecto getProject() {
        return proyecto;
    }
}