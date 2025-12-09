package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "rol_proyecto")
public class RolProyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_proyecto_id", nullable = false)
    private Long rolProyectoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(name = "nombre_rol_proyecto", nullable = false, length = 100)
    private String nombreRolProyecto;

    // Alias para repository methods en inglés
    @Column(name = "nombre_rol_proyecto", insertable = false, updatable = false)
    private String projectRoleName;

    @Lob
    @Column(name = "descripcion_rol_proyecto")
    private String descripcionRolProyecto;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "descripcion_rol_proyecto", insertable = false, updatable = false)
    private String projectRoleDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // Relaciones
    @OneToMany(mappedBy = "rolProyecto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AsignacionRolProyecto> asignaciones = new HashSet<>();

    // Métodos helper
    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public boolean estaActivo() {
        return this.activo;
    }

    public void actualizar() {
        this.actualizadoEn = LocalDateTime.now();
    }
}

