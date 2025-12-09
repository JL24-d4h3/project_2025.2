package org.project.project.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {
    // Getters y Setters
    @NotBlank(message = "El usuario o correo electrónico es requerido")
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$|^[\\w]{3,20}$",
            message = "Ingrese un usuario válido (3-20 caracteres) o un correo electrónico válido")
    private String usernameOrEmail;

    @NotBlank(message = "La contraseña es requerida")
    private String password;

    private boolean rememberMe;

}
