package org.project.project.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ReporteHasProyectoId implements Serializable {

    @Column(name = "reporte_id")
    private Long reporteId;

    @Column(name = "proyecto_id")
    private Long proyectoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReporteHasProyectoId that = (ReporteHasProyectoId) o;
        return Objects.equals(reporteId, that.reporteId) &&
               Objects.equals(proyectoId, that.proyectoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reporteId, proyectoId);
    }
}

