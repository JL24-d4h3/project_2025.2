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
public class UsuarioHasRepositorioId implements java.io.Serializable {

    @Column(name = "usuario_usuario_id")
    private Long usuarioId;

    @Column(name = "repositorio_repositorio_id")
    private Long repositorioId;
    
    // Alias getters para repository methods en inglés
    public Long getUserId() {
        return usuarioId;
    }
    
    public Long getRepositoryId() {
        return repositorioId;
    }
}