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
public class ReporteHasRepositorioId implements Serializable {

    @Column(name = "reporte_id")
    private Long reporteId;

    @Column(name = "repositorio_id")
    private Long repositorioId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReporteHasRepositorioId that = (ReporteHasRepositorioId) o;
        return Objects.equals(reporteId, that.reporteId) &&
               Objects.equals(repositorioId, that.repositorioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reporteId, repositorioId);
    }
}

