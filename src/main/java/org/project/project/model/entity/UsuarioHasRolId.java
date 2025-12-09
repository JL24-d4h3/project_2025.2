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
public class UsuarioHasRolId implements Serializable {

    @Column(name = "usuario_usuario_id")
    private Long usuarioId;

    @Column(name = "rol_rol_id")
    private Long rolId;
    
    // Alias getters para repository methods en inglés
    public Long getUserId() {
        return usuarioId;
    }
    
    public Long getRoleId() {
        return rolId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioHasRolId that = (UsuarioHasRolId) o;
        return Objects.equals(usuarioId, that.usuarioId) &&
                Objects.equals(rolId, that.rolId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, rolId);
    }
}