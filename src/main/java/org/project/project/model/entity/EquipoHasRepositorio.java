package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "equipo_has_repositorio")
public class EquipoHasRepositorio {

    @EmbeddedId
    private EquipoHasRepositorioId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "privilegio_equipo_repositorio", nullable = false)
    private PrivilegioEquipoRepositorio privilegio = PrivilegioEquipoRepositorio.LECTOR;
    
    // Alias para repository methods en inglés
    @Enumerated(EnumType.STRING)
    @Column(name = "privilegio_equipo_repositorio", insertable = false, updatable = false)
    private PrivilegioEquipoRepositorio teamRepositoryPrivilege;

    @Column(name = "fecha_equipo_repositorio", nullable = false)
    private LocalDateTime fechaEquipoRepositorio = LocalDateTime.now();
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_equipo_repositorio", insertable = false, updatable = false)
    private LocalDateTime teamRepositoryDate;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("equipoId")
    @JoinColumn(name = "equipo_equipo_id")
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("repositorioId")
    @JoinColumn(name = "repositorio_repositorio_id")
    private Repositorio repositorio;

    // Enums
    public enum PrivilegioEquipoRepositorio {
        LECTOR, EDITOR, ADMINISTRADOR, PERSONALIZADO
    }

    // Constructors
    public EquipoHasRepositorio() {}

    public EquipoHasRepositorio(Equipo equipo, Repositorio repositorio, PrivilegioEquipoRepositorio privilegio) {
        this.equipo = equipo;
        this.repositorio = repositorio;
        this.privilegio = privilegio;
        this.id = new EquipoHasRepositorioId(equipo.getEquipoId(), repositorio.getRepositorioId());
        this.fechaEquipoRepositorio = LocalDateTime.now();
    }

    // Getters and Setters
    public EquipoHasRepositorioId getId() {
        return id;
    }

    public void setId(EquipoHasRepositorioId id) {
        this.id = id;
    }

    public PrivilegioEquipoRepositorio getPrivilegio() {
        return privilegio;
    }

    public void setPrivilegio(PrivilegioEquipoRepositorio privilegio) {
        this.privilegio = privilegio;
    }

    public LocalDateTime getFechaEquipoRepositorio() {
        return fechaEquipoRepositorio;
    }

    public void setFechaEquipoRepositorio(LocalDateTime fechaEquipoRepositorio) {
        this.fechaEquipoRepositorio = fechaEquipoRepositorio;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public Repositorio getRepositorio() {
        return repositorio;
    }

    public void setRepositorio(Repositorio repositorio) {
        this.repositorio = repositorio;
    }

    // Utility methods
    public boolean canEdit() {
        return privilegio == PrivilegioEquipoRepositorio.EDITOR;
    }

    public boolean canRead() {
        return privilegio == PrivilegioEquipoRepositorio.LECTOR || 
               privilegio == PrivilegioEquipoRepositorio.EDITOR;
    }

    public boolean canComment() {
        return privilegio == PrivilegioEquipoRepositorio.EDITOR;
    }

    // Alias getters para repository methods en inglés  
    public Equipo getTeam() {
        return equipo;
    }
    
    public Repositorio getRepository() {
        return repositorio;
    }
    
    public PrivilegioEquipoRepositorio getPrivilege() {
        return privilegio;
    }
    
    public LocalDateTime getTeamRepositoryDate() {
        return fechaEquipoRepositorio;
    }

    @Override
    public String toString() {
        return "EquipoHasRepositorio{" +
                "equipoId=" + (id != null ? id.getEquipoId() : null) +
                ", repositorioId=" + (id != null ? id.getRepositorioId() : null) +
                ", privilegio=" + privilegio +
                ", fechaEquipoRepositorio=" + fechaEquipoRepositorio +
                '}';
    }
}