package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "usuario_has_proyecto")
public class UsuarioHasProyecto {

    @EmbeddedId
    private UsuarioHasProyectoId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "privilegio_usuario_proyecto", nullable = false)
    private PrivilegioUsuarioProyecto privilegio = PrivilegioUsuarioProyecto.LECTOR;
    
    // Alias para repository methods en inglés
    @Enumerated(EnumType.STRING)
    @Column(name = "privilegio_usuario_proyecto", insertable = false, updatable = false)
    private PrivilegioUsuarioProyecto privilege;

    @Column(name = "fecha_usuario_proyecto", nullable = false)
    private LocalDateTime fechaUsuarioProyecto = LocalDateTime.now();
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_usuario_proyecto", insertable = false, updatable = false)
    private LocalDateTime userProjectDate;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("proyectoId")
    @JoinColumn(name = "proyecto_proyecto_id")
    private Proyecto proyecto;

    // Enums
    public enum PrivilegioUsuarioProyecto {
        LECTOR, EDITOR, COMENTADOR, ADMINISTRADOR
    }

    // Constructors
    public UsuarioHasProyecto() {}

    public UsuarioHasProyecto(Usuario usuario, Proyecto proyecto, PrivilegioUsuarioProyecto privilegio) {
        this.usuario = usuario;
        this.proyecto = proyecto;
        this.privilegio = privilegio;
        this.id = new UsuarioHasProyectoId(usuario.getUsuarioId(), proyecto.getProyectoId());
        this.fechaUsuarioProyecto = LocalDateTime.now();
    }

    // Getters and Setters
    public UsuarioHasProyectoId getId() {
        return id;
    }

    public void setId(UsuarioHasProyectoId id) {
        this.id = id;
    }

    public PrivilegioUsuarioProyecto getPrivilegio() {
        return privilegio;
    }

    public void setPrivilegio(PrivilegioUsuarioProyecto privilegio) {
        this.privilegio = privilegio;
    }

    public LocalDateTime getFechaUsuarioProyecto() {
        return fechaUsuarioProyecto;
    }

    public void setFechaUsuarioProyecto(LocalDateTime fechaUsuarioProyecto) {
        this.fechaUsuarioProyecto = fechaUsuarioProyecto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    // Getter alias para repository methods en inglés
    public PrivilegioUsuarioProyecto getPrivilege() {
        return privilegio;
    }

    // Utility methods
    public boolean canEdit() {
        return privilegio == PrivilegioUsuarioProyecto.EDITOR;
    }

    public boolean canRead() {
        return privilegio == PrivilegioUsuarioProyecto.LECTOR || 
               privilegio == PrivilegioUsuarioProyecto.EDITOR || 
               privilegio == PrivilegioUsuarioProyecto.COMENTADOR;
    }

    public boolean canComment() {
        return privilegio == PrivilegioUsuarioProyecto.COMENTADOR || 
               privilegio == PrivilegioUsuarioProyecto.EDITOR;
    }

    @Override
    public String toString() {
        return "UsuarioHasProyecto{" +
                "usuarioId=" + (id != null ? id.getUsuarioId() : null) +
                ", proyectoId=" + (id != null ? id.getProyectoId() : null) +
                ", privilegio=" + privilegio +
                ", fechaUsuarioProyecto=" + fechaUsuarioProyecto +
                '}';
    }
}