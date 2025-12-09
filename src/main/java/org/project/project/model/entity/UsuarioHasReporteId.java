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
public class UsuarioHasReporteId implements Serializable {

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "reporte_id")
    private Long reporteId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioHasReporteId that = (UsuarioHasReporteId) o;
        return Objects.equals(usuarioId, that.usuarioId) &&
               Objects.equals(reporteId, that.reporteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, reporteId);
    }
}

