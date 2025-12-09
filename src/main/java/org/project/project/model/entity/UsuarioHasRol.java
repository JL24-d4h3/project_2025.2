package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuario_has_rol")
public class UsuarioHasRol {

    @EmbeddedId
    private UsuarioHasRolId id;

    @MapsId("usuarioId")
    @ManyToOne
    @JoinColumn(name = "usuario_usuario_id", nullable = false)
    private Usuario usuario;

    @MapsId("rolId")
    @ManyToOne
    @JoinColumn(name = "rol_rol_id", nullable = false)
    private Rol rol;

    public UsuarioHasRol() {}

    public UsuarioHasRol(Usuario usuario, Rol rol) {
        this.usuario = usuario;
        this.rol = rol;
        this.id = new UsuarioHasRolId(usuario.getUsuarioId(), rol.getRolId().longValue());
    }
    
    // Alias getters para repository methods en inglés
    public Usuario getUser() {
        return usuario;
    }
    
    public Rol getRole() {
        return rol;
    }
}