package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "asignacion_rol_proyecto")
public class AsignacionRolProyecto {

    @EmbeddedId
    private AsignacionRolProyectoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("rolProyectoRolProyectoId")
    @JoinColumn(name = "rol_proyecto_rol_proyecto_id")
    private RolProyecto rolProyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "usuario_has_proyecto_usuario_usuario_id", referencedColumnName = "usuario_usuario_id", insertable = false, updatable = false),
        @JoinColumn(name = "usuario_has_proyecto_proyecto_proyecto_id", referencedColumnName = "proyecto_proyecto_id", insertable = false, updatable = false)
    })
    private UsuarioHasProyecto usuarioHasProyecto;

    // Constructors
    public AsignacionRolProyecto() {
    }

    public AsignacionRolProyecto(RolProyecto rolProyecto, UsuarioHasProyecto usuarioHasProyecto) {
        this.rolProyecto = rolProyecto;
        this.usuarioHasProyecto = usuarioHasProyecto;
        this.id = new AsignacionRolProyectoId(
            rolProyecto.getRolProyectoId(),
            usuarioHasProyecto.getId().getUsuarioId(),
            usuarioHasProyecto.getId().getProyectoId()
        );
    }
}
