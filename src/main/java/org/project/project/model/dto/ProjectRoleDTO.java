package org.project.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para listar y mostrar roles de proyecto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRoleDTO {
    private Long rolProyectoId;
    private String nombreRolProyecto;
    private String descripcionRolProyecto;
    private String creadoPorNombre;
    private Long creadoPorId;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private Boolean activo;
    private Long usuariosAsignados;

    @Override
    public String toString() {
        return "ProjectRoleDTO{" +
                "rolProyectoId=" + rolProyectoId +
                ", nombreRolProyecto='" + nombreRolProyecto + '\'' +
                ", activo=" + activo +
                ", usuariosAsignados=" + usuariosAsignados +
                '}';
    }
}
