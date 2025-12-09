package org.project.project.repository;

import org.project.project.model.entity.ReporteHasApi;
import org.project.project.model.entity.ReporteHasApiId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository principal para operaciones básicas CRUD de ReporteHasApi.
 */
@Repository
public interface ReporteHasApiRepository extends JpaRepository<ReporteHasApi, ReporteHasApiId> {

    // Buscar por reporte ID
    List<ReporteHasApi> findByReporte_ReporteId(Long reporteId);

    // Buscar por API ID
    List<ReporteHasApi> findByApi_ApiId(Long apiId);

    // Verificar existencia por reporte y API
    boolean existsByReporte_ReporteIdAndApi_ApiId(Long reporteId, Long apiId);

    // Contar APIs por reporte
    long countByReporte_ReporteId(Long reporteId);

    // Contar reportes por API
    long countByApi_ApiId(Long apiId);

    // Obtener todas las APIs de un reporte con fetch
    @Query("SELECT rha FROM ReporteHasApi rha JOIN FETCH rha.api WHERE rha.reporte.reporteId = :reporteId")
    List<ReporteHasApi> findByReporteIdWithApi(@Param("reporteId") Long reporteId);
}

