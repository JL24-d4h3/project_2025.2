package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categoria_has_repositorio", indexes = {
    @Index(name = "fk_categoria_has_repositorio_repositorio1_idx", columnList = "repositorio_repositorio_id"),
    @Index(name = "fk_categoria_has_repositorio_categoria1_idx", columnList = "categoria_id_categoria")
})
public class CategoriaHasRepositorio {

    @EmbeddedId
    private CategoriaHasRepositorioId id;

    @MapsId("categoriaId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id_categoria", nullable = false)
    private Categoria categoria;

    @MapsId("repositorioId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repositorio_repositorio_id", nullable = false)
    private Repositorio repositorio;

    public CategoriaHasRepositorio() {}

    public CategoriaHasRepositorio(Categoria categoria, Repositorio repositorio) {
        this.categoria = categoria;
        this.repositorio = repositorio;
        this.id = new CategoriaHasRepositorioId(categoria.getIdCategoria(), repositorio.getRepositorioId());
    }
    
    // Alias getters para repository methods en inglés
    public Categoria getCategory() {
        return categoria;
    }
    
    public Repositorio getRepository() {
        return repositorio;
    }
}