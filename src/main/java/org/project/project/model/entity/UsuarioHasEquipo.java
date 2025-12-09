package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuario_has_equipo")
public class UsuarioHasEquipo {

    @EmbeddedId
    private UsuarioHasEquipoId id;

    @MapsId("usuarioId")
    @ManyToOne
    @JoinColumn(name = "usuario_usuario_id", nullable = false)
    private Usuario usuario;

    @MapsId("equipoId")
    @ManyToOne
    @JoinColumn(name = "equipo_equipo_id", nullable = false)
    private Equipo equipo;

    public UsuarioHasEquipo() {}

    public UsuarioHasEquipo(Usuario usuario, Equipo equipo) {
        this.usuario = usuario;
        this.equipo = equipo;
        this.id = new UsuarioHasEquipoId(usuario.getUsuarioId(), equipo.getEquipoId());
    }
    
    // Alias getters para repository methods en inglés
    public Usuario getUser() {
        return usuario;
    }
    
    public Equipo getTeam() {
        return equipo;
    }
}