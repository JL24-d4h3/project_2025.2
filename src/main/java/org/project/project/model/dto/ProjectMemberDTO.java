package org.project.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para mostrar miembros del proyecto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberDTO {
    private Long usuarioId;
    private String nombreCompleto;
    private String correo;
    private String fotoPerfil;
    private List<String> rolesAsignados;
    private String estadoInvitacion;  // PENDIENTE, ACTIVO, RECHAZADO, EXPIRADO
    private LocalDateTime fechaAceptacion;
    private Boolean esCreador;

    @Override
    public String toString() {
        return "ProjectMemberDTO{" +
                "usuarioId=" + usuarioId +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", correo='" + correo + '\'' +
                ", estadoInvitacion='" + estadoInvitacion + '\'' +
                ", esCreador=" + esCreador +
                '}';
    }
}
