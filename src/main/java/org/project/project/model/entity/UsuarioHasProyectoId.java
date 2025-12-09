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
public class UsuarioHasProyectoId implements java.io.Serializable {

    @Column(name = "usuario_usuario_id")
    private Long usuarioId;

    @Column(name = "proyecto_proyecto_id")
    private Long proyectoId;

    // Alias getters para repository methods en inglés
    public Long getUserId() {
        return usuarioId;
    }
    
    public Long getProjectId() {
        return proyectoId;
    }
}