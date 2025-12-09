package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "usuario_has_reporte")
public class UsuarioHasReporte {

    @EmbeddedId
    private UsuarioHasReporteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reporteId")
    @JoinColumn(name = "reporte_id")
    private Reporte reporte;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_colaborador", nullable = false)
    private RolColaborador rolColaborador = RolColaborador.COLABORADOR;

    // Alias para repository methods en inglés
    @Column(name = "rol_colaborador", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private RolColaborador collaboratorRole;

    @Column(name = "puede_editar", nullable = false)
    private Boolean puedeEditar = false;

    // Alias para repository methods en inglés
    @Column(name = "puede_editar", insertable = false, updatable = false)
    private Boolean canEdit;

    @Column(name = "asignado_en", nullable = false)
    private LocalDateTime asignadoEn = LocalDateTime.now();

    // Alias para repository methods en inglés
    @Column(name = "asignado_en", insertable = false, updatable = false)
    private LocalDateTime assignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_por")
    private Usuario asignadoPor;

    public enum RolColaborador {
        AUTOR, REVISOR, COLABORADOR, LECTOR
    }

    // Constructors
    public UsuarioHasReporte() {
    }

    public UsuarioHasReporte(Usuario usuario, Reporte reporte, RolColaborador rolColaborador) {
        this.usuario = usuario;
        this.reporte = reporte;
        this.rolColaborador = rolColaborador;
        this.id = new UsuarioHasReporteId(usuario.getUsuarioId(), reporte.getReporteId());
        this.asignadoEn = LocalDateTime.now();
        this.puedeEditar = (rolColaborador == RolColaborador.AUTOR || rolColaborador == RolColaborador.COLABORADOR);
    }

    // Métodos helper
    public void concederPermisoEdicion() {
        this.puedeEditar = true;
    }

    public void revocarPermisoEdicion() {
        this.puedeEditar = false;
    }

    public boolean esAutor() {
        return RolColaborador.AUTOR.equals(this.rolColaborador);
    }

    public boolean esRevisor() {
        return RolColaborador.REVISOR.equals(this.rolColaborador);
    }

    public boolean esColaborador() {
        return RolColaborador.COLABORADOR.equals(this.rolColaborador);
    }

    public boolean esLector() {
        return RolColaborador.LECTOR.equals(this.rolColaborador);
    }

    public void cambiarRol(RolColaborador nuevoRol) {
        this.rolColaborador = nuevoRol;
        // Actualizar permisos según el rol
        this.puedeEditar = (nuevoRol == RolColaborador.AUTOR || nuevoRol == RolColaborador.COLABORADOR);
    }
}
