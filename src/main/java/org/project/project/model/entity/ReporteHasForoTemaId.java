package org.project.project.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Composite Primary Key para la relación N:M entre Reporte y ForoTema
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReporteHasForoTemaId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "reporte_id", nullable = false)
    private Long reporteId;

    @Column(name = "foro_tema_id", nullable = false)
    private Long foroTemaId;
}
