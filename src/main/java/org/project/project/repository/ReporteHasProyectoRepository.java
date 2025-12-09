package org.project.project.repository;

import org.project.project.model.entity.ReporteHasProyecto;
import org.project.project.model.entity.ReporteHasProyectoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository principal para operaciones básicas CRUD de ReporteHasProyecto.
 */
@Repository
public interface ReporteHasProyectoRepository extends JpaRepository<ReporteHasProyecto, ReporteHasProyectoId> {

    // Buscar por reporte ID
    List<ReporteHasProyecto> findByReporte_ReporteId(Long reporteId);

    // Buscar por proyecto ID
    List<ReporteHasProyecto> findByProyecto_ProyectoId(Long proyectoId);

    // Verificar existencia por reporte y proyecto
    boolean existsByReporte_ReporteIdAndProyecto_ProyectoId(Long reporteId, Long proyectoId);

    // Contar proyectos por reporte
    long countByReporte_ReporteId(Long reporteId);

    // Contar reportes por proyecto
    long countByProyecto_ProyectoId(Long proyectoId);

    // Obtener todos los proyectos de un reporte con fetch
    @Query("SELECT rhp FROM ReporteHasProyecto rhp JOIN FETCH rhp.proyecto WHERE rhp.reporte.reporteId = :reporteId")
    List<ReporteHasProyecto> findByReporteIdWithProyecto(@Param("reporteId") Long reporteId);
}

