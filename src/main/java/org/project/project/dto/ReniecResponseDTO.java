package org.project.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO para mapear la respuesta de la API de RENIEC (Decolecta)
 * Estructura de respuesta esperada:
 * {
 *   "first_name": "ROXANA KARINA",
 *   "first_last_name": "DELGADO",
 *   "second_last_name": "CUELLAR",
 *   "full_name": "DELGADO CUELLAR ROXANA KARINA",
 *   "document_number": "46027896"
 * }
 */
@Data
public class ReniecResponseDTO {
    
    @JsonProperty("first_name")
    private String firstName;        // Ambos nombres (ej: "ROXANA KARINA")
    
    @JsonProperty("first_last_name")
    private String firstLastName;    // Apellido paterno (ej: "DELGADO")
    
    @JsonProperty("second_last_name")
    private String secondLastName;   // Apellido materno (ej: "CUELLAR")
    
    @JsonProperty("full_name")
    private String fullName;         // Nombre completo oficial
    
    @JsonProperty("document_number")
    private String documentNumber;   // DNI de 8 dígitos
}
