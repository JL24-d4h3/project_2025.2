package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "equipo_has_proyecto")
public class EquipoHasProyecto {

    @EmbeddedId
    private EquipoHasProyectoId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "privilegio_equipo_proyecto", nullable = false)
    private PrivilegioEquipoProyecto privilegio = PrivilegioEquipoProyecto.LECTOR;
    
    // Alias para repository methods en inglés
    @Enumerated(EnumType.STRING)
    @Column(name = "privilegio_equipo_proyecto", insertable = false, updatable = false)
    private PrivilegioEquipoProyecto teamProjectPrivilege;

    @Column(name = "fecha_equipo_proyecto", nullable = false)
    private LocalDateTime fechaEquipoProyecto = LocalDateTime.now();
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_equipo_proyecto", insertable = false, updatable = false)
    private LocalDateTime teamProjectDate;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("equipoId")
    @JoinColumn(name = "equipo_equipo_id")
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("proyectoId")
    @JoinColumn(name = "proyecto_proyecto_id")
    private Proyecto proyecto;

    // Enums
    public enum PrivilegioEquipoProyecto {
        LECTOR, EDITOR, COMENTADOR, ADMINISTRADOR, PERSONALIZADO
    }

    // Constructors
    public EquipoHasProyecto() {}

    public EquipoHasProyecto(Equipo equipo, Proyecto proyecto, PrivilegioEquipoProyecto privilegio) {
        this.equipo = equipo;
        this.proyecto = proyecto;
        this.privilegio = privilegio;
        this.id = new EquipoHasProyectoId(equipo.getEquipoId(), proyecto.getProyectoId());
        this.fechaEquipoProyecto = LocalDateTime.now();
    }

    // Getters and Setters
    public EquipoHasProyectoId getId() {
        return id;
    }

    public void setId(EquipoHasProyectoId id) {
        this.id = id;
    }

    public PrivilegioEquipoProyecto getPrivilegio() {
        return privilegio;
    }

    public void setPrivilegio(PrivilegioEquipoProyecto privilegio) {
        this.privilegio = privilegio;
    }

    public LocalDateTime getFechaEquipoProyecto() {
        return fechaEquipoProyecto;
    }

    public void setFechaEquipoProyecto(LocalDateTime fechaEquipoProyecto) {
        this.fechaEquipoProyecto = fechaEquipoProyecto;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    // Utility methods
    public boolean canEdit() {
        return privilegio == PrivilegioEquipoProyecto.EDITOR;
    }

    public boolean canRead() {
        return privilegio == PrivilegioEquipoProyecto.LECTOR || 
               privilegio == PrivilegioEquipoProyecto.EDITOR || 
               privilegio == PrivilegioEquipoProyecto.COMENTADOR;
    }

    public boolean canComment() {
        return privilegio == PrivilegioEquipoProyecto.COMENTADOR || 
               privilegio == PrivilegioEquipoProyecto.EDITOR;
    }

    // Alias getters para repository methods en inglés
    public Equipo getTeam() {
        return equipo;
    }
    
    public Proyecto getProject() {
        return proyecto;
    }
    
    public PrivilegioEquipoProyecto getPrivilege() {
        return privilegio;
    }

    @Override
    public String toString() {
        return "EquipoHasProyecto{" +
                "equipoId=" + (id != null ? id.getEquipoId() : null) +
                ", proyectoId=" + (id != null ? id.getProyectoId() : null) +
                ", privilegio=" + privilegio +
                ", fechaEquipoProyecto=" + fechaEquipoProyecto +
                '}';
    }
}