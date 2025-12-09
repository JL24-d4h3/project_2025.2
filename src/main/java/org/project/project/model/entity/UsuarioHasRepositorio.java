package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "usuario_has_repositorio")
public class UsuarioHasRepositorio {

    @EmbeddedId
    private UsuarioHasRepositorioId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "privilegio_usuario_repositorio", nullable = false)
    private PrivilegioUsuarioRepositorio privilegio = PrivilegioUsuarioRepositorio.LECTOR;
    
    // Alias para repository methods en inglés
    @Enumerated(EnumType.STRING)
    @Column(name = "privilegio_usuario_repositorio", insertable = false, updatable = false)
    private PrivilegioUsuarioRepositorio privilege;

    @Column(name = "fecha_usuario_repositorio", nullable = false)
    private LocalDateTime fechaUsuarioRepositorio = LocalDateTime.now();
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_usuario_repositorio", insertable = false, updatable = false)
    private LocalDateTime userRepositoryDate;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("repositorioId")
    @JoinColumn(name = "repositorio_repositorio_id")
    private Repositorio repositorio;

    // Enums
    public enum PrivilegioUsuarioRepositorio {
        LECTOR, EDITOR, ADMINISTRADOR
    }

    // Constructors
    public UsuarioHasRepositorio() {}

    public UsuarioHasRepositorio(Usuario usuario, Repositorio repositorio, PrivilegioUsuarioRepositorio privilegio) {
        this.usuario = usuario;
        this.repositorio = repositorio;
        this.privilegio = privilegio;
        this.id = new UsuarioHasRepositorioId(usuario.getUsuarioId(), repositorio.getRepositorioId());
        this.fechaUsuarioRepositorio = LocalDateTime.now();
    }

    // Utility methods
    public boolean canEdit() {
        return privilegio == PrivilegioUsuarioRepositorio.EDITOR;
    }

    public boolean canRead() {
        return privilegio == PrivilegioUsuarioRepositorio.LECTOR || 
               privilegio == PrivilegioUsuarioRepositorio.EDITOR;
    }

    public boolean canComment() {
        return privilegio == PrivilegioUsuarioRepositorio.EDITOR;
    }

    // Alias getters para repository methods en inglés
    public Usuario getUser() {
        return usuario;
    }
    
    public Repositorio getRepository() {
        return repositorio;
    }

    @Override
    public String toString() {
        return "UsuarioHasRepositorio{" +
                "usuarioId=" + (id != null ? id.getUsuarioId() : null) +
                ", repositorioId=" + (id != null ? id.getRepositorioId() : null) +
                ", privilegio=" + privilegio +
                ", fechaUsuarioRepositorio=" + fechaUsuarioRepositorio +
                '}';
    }
}