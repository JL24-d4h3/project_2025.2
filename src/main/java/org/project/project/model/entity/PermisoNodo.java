package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for permiso_nodo table
 * Manages permissions for nodes (users or teams)
 */
@Getter
@Setter
@Entity
@Table(name = "permiso_nodo")
public class PermisoNodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permiso_nodo_id", nullable = false)
    private Long permisoNodoId;

    // English alias for repositories
    @Column(name = "permiso_nodo_id", insertable = false, updatable = false)
    private Long nodePermissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodo_id", nullable = false)
    private Nodo nodo;

    @Enumerated(EnumType.STRING)
    @Column(name = "permiso", nullable = false)
    private TipoPermiso permiso = TipoPermiso.READ;

    // English alias for repositories
    @Column(name = "permiso", insertable = false, updatable = false)
    private String permission;

    @Column(name = "inheritable", nullable = false)
    private Boolean inheritable = true;

    // English alias for repositories
    @Column(name = "inheritable", insertable = false, updatable = false)
    private Boolean isInheritable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    // English alias for repositories
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", insertable = false, updatable = false)
    private Usuario createdBy;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    // English alias for repositories
    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;

    // English alias for repositories
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por", insertable = false, updatable = false)
    private Usuario updatedBy;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    // English alias for repositories
    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_usuario_id")
    private Usuario usuario;

    // English alias for repositories
    @Column(name = "usuario_usuario_id", insertable = false, updatable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_equipo_id")
    private Equipo equipo;

    // English alias for repositories
    @Column(name = "equipo_equipo_id", insertable = false, updatable = false)
    private Long teamId;

    public enum TipoPermiso {
        READ, WRITE, ADMIN
    }

    // Helper methods
    public boolean isUserPermission() {
        return usuario != null;
    }

    public boolean isTeamPermission() {
        return equipo != null;
    }
}
