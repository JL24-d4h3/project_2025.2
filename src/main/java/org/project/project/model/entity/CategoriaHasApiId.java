package org.project.project.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CategoriaHasApiId implements Serializable {

    @Column(name = "categoria_id_categoria", nullable = false)
    private Long categoriaId;

    @Column(name = "api_api_id", nullable = false)
    private Long apiId;
    
    // Alias getters para repository methods en inglés
    public Long getCategoryId() {
        return categoriaId;
    }
    
    public Long getApiId() {
        return apiId;
    }
}
