package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categoria_has_api")
public class CategoriaHasApi {

    @EmbeddedId
    private CategoriaHasApiId id;

    @MapsId("categoriaId")
    @ManyToOne
    @JoinColumn(name = "categoria_id_categoria", nullable = false)
    private Categoria categoria;
    
    // Alias para repository methods en inglés
    @ManyToOne
    @JoinColumn(name = "categoria_id_categoria", insertable = false, updatable = false)
    private Categoria category;

    @MapsId("apiId")
    @ManyToOne
    @JoinColumn(name = "api_api_id", nullable = false)
    private API api;

    // Constructors for convenience
    public CategoriaHasApi() {}

    public CategoriaHasApi(Categoria categoria, API api) {
        this.categoria = categoria;
        this.api = api;
        this.id = new CategoriaHasApiId(categoria.getIdCategoria(), api.getApiId());
    }
}
