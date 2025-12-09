package org.project.project.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class EquipoHasRepositorioId implements java.io.Serializable {

    @Column(name = "equipo_equipo_id")
    private Long equipoId;

    @Column(name = "repositorio_repositorio_id")
    private Long repositorioId;

    // Alias getters para repository methods en inglés
    public Long getTeamId() {
        return equipoId;
    }
    
    public Long getRepositoryId() {
        return repositorioId;
    }
}