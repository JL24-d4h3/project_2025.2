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
public class EtiquetaHasApiId implements Serializable {

    @Column(name = "etiqueta_tag_id")
    private Long etiquetaId;

    @Column(name = "api_api_id")
    private Long apiId;
    
    // Alias getters para repository methods en inglés
    public Long getTagId() {
        return etiquetaId;
    }
    
    public Long getApiId() {
        return apiId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EtiquetaHasApiId that = (EtiquetaHasApiId) o;
        return Objects.equals(etiquetaId, that.etiquetaId) &&
                Objects.equals(apiId, that.apiId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(etiquetaId, apiId);
    }
}