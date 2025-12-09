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
public class AsignacionRolProyectoId implements Serializable {

    @Column(name = "rol_proyecto_rol_proyecto_id")
    private Long rolProyectoRolProyectoId;

    @Column(name = "usuario_has_proyecto_usuario_usuario_id")
    private Long usuarioHasProyectoUsuarioUsuarioId;

    @Column(name = "usuario_has_proyecto_proyecto_proyecto_id")
    private Long usuarioHasProyectoProyectoProyectoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsignacionRolProyectoId that = (AsignacionRolProyectoId) o;
        return Objects.equals(rolProyectoRolProyectoId, that.rolProyectoRolProyectoId) &&
               Objects.equals(usuarioHasProyectoUsuarioUsuarioId, that.usuarioHasProyectoUsuarioUsuarioId) &&
               Objects.equals(usuarioHasProyectoProyectoProyectoId, that.usuarioHasProyectoProyectoProyectoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rolProyectoRolProyectoId, usuarioHasProyectoUsuarioUsuarioId, usuarioHasProyectoProyectoProyectoId);
    }
}

