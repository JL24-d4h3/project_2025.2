package org.project.project.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class CategoriaHasProyectoId implements Serializable {

    @Column(name = "categoria_id_categoria")
    private Long categoriaId;

    @Column(name = "proyecto_proyecto_id")
    private Long proyectoId;
    
    // Alias getters para repository methods en inglés
    public Long getCategoryId() {
        return categoriaId;
    }
    
    public Long getProjectId() {
        return proyectoId;
    }
}