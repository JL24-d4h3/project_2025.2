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
public class ReporteHasApiId implements Serializable {

    @Column(name = "reporte_id")
    private Long reporteId;

    @Column(name = "api_id")
    private Long apiId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReporteHasApiId that = (ReporteHasApiId) o;
        return Objects.equals(reporteId, that.reporteId) &&
               Objects.equals(apiId, that.apiId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reporteId, apiId);
    }
}

