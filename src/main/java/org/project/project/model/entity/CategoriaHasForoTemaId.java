package org.project.project.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CategoriaHasForoTemaId implements Serializable {

    @Column(name = "categoria_id_categoria")
    private Long categoriaIdCategoria;

    @Column(name = "foro_tema_tema_id")
    private Long foroTemaTemaId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoriaHasForoTemaId that = (CategoriaHasForoTemaId) o;
        return Objects.equals(categoriaIdCategoria, that.categoriaIdCategoria) &&
               Objects.equals(foroTemaTemaId, that.foroTemaTemaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoriaIdCategoria, foroTemaTemaId);
    }
}

