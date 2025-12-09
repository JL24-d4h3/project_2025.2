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
public class ProyectoHasRepositorioId implements Serializable {

    @Column(name = "proyecto_proyecto_id")
    private Long proyectoId;

    @Column(name = "repositorio_repositorio_id")
    private Long repositorioId;
    
    // Alias getters para repository methods en inglés
    public Long getProjectId() {
        return proyectoId;
    }
    
    public Long getRepositoryId() {
        return repositorioId;
    }
}