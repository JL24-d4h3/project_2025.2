package org.project.project.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * DTO para la información detallada de un miembro del equipo
 * Adaptado a la nueva BD official_dev_portal con campos de la entidad Usuario
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberDTO {
    
    private Long userId;
    private String userName; // nombreUsuario de la entidad
    private String username; // username de la entidad
    private String lastName; // apellidoPaterno de la entidad
    private String dni;
    private String email; // correo de la entidad
    private String role;
    private String userCode; // codigoUsuario de la entidad

    // Constructor sin userCode para compatibilidad
    public TeamMemberDTO(Long userId, String userName, String username, String lastName, 
                        String dni, String email, String role) {
        this.userId = userId;
        this.userName = userName;
        this.username = username;
        this.lastName = lastName;
        this.dni = dni;
        this.email = email;
        this.role = role;
    }
}