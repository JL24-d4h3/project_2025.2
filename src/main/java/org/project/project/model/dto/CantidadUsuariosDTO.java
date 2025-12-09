package org.project.project.model.dto;

/**
 * DTO para estadísticas de cantidad de usuarios
 * Adaptado a la nueva BD official_dev_portal
 */
public class CantidadUsuariosDTO {
    private long totalUsuarios;
    private long usuariosActivos;
    private long usuariosInactivos;

    // Constructores
    public CantidadUsuariosDTO() {}

    public CantidadUsuariosDTO(long totalUsuarios, long usuariosActivos, long usuariosInactivos) {
        this.totalUsuarios = totalUsuarios;
        this.usuariosActivos = usuariosActivos;
        this.usuariosInactivos = usuariosInactivos;
    }

    // Getters y Setters
    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public long getUsuariosActivos() {
        return usuariosActivos;
    }

    public void setUsuariosActivos(long usuariosActivos) {
        this.usuariosActivos = usuariosActivos;
    }

    public long getUsuariosInactivos() {
        return usuariosInactivos;
    }

    public void setUsuariosInactivos(long usuariosInactivos) {
        this.usuariosInactivos = usuariosInactivos;
    }
}
