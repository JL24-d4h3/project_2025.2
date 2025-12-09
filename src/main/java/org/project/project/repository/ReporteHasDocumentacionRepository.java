package org.project.project.repository;

import org.project.project.model.entity.ReporteHasDocumentacion;
import org.project.project.model.entity.ReporteHasDocumentacionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository principal para operaciones básicas CRUD de ReporteHasDocumentacion.
 */
@Repository
public interface ReporteHasDocumentacionRepository extends JpaRepository<ReporteHasDocumentacion, ReporteHasDocumentacionId> {

    // Buscar por reporte ID
    List<ReporteHasDocumentacion> findByReporte_ReporteId(Long reporteId);

    // Buscar por documentación ID
    List<ReporteHasDocumentacion> findByDocumentacion_DocumentacionId(Long documentacionId);

    // Verificar existencia por reporte y documentación
    boolean existsByReporte_ReporteIdAndDocumentacion_DocumentacionId(Long reporteId, Long documentacionId);

    // Contar documentaciones por reporte
    long countByReporte_ReporteId(Long reporteId);

    // Contar reportes por documentación
    long countByDocumentacion_DocumentacionId(Long documentacionId);

    // Obtener todas las documentaciones de un reporte con fetch
    @Query("SELECT rhd FROM ReporteHasDocumentacion rhd JOIN FETCH rhd.documentacion WHERE rhd.reporte.reporteId = :reporteId")
    List<ReporteHasDocumentacion> findByReporteIdWithDocumentacion(@Param("reporteId") Long reporteId);
}

