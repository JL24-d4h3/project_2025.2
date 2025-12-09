package org.project.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado para Select2 en formularios
 * Usado para dropdowns de entidades (API, Proyecto, Ticket, etc.)
 * 
 * @author jleon
 * @since 2025-01-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntitySelectDTO {
    private Long id;
    private String nombre;          // Nombre principal de la entidad
    private String descripcion;     // Descripción adicional (opcional)
    private String tipo;            // Tipo de entidad (API, PROYECTO, etc.) - opcional
}
