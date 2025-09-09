package org.project.project.model.dto;

public class CantidadUsuariosDTO {
    private long totalUsuarios;
    private long usuariosActivos;
    private long usuariosInactivos;

    public CantidadUsuariosDTO(long totalUsuarios, long usuariosActivos, long usuariosInactivos) {
        this.totalUsuarios = totalUsuarios;
        this.usuariosActivos = usuariosActivos;
        this.usuariosInactivos = usuariosInactivos;
    }

    // Getters
    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public long getUsuariosActivos() {
        return usuariosActivos;
    }

    public long getUsuariosInactivos() {
        return usuariosInactivos;
    }
}
