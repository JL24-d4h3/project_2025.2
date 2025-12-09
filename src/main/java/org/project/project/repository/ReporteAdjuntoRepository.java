package org.project.project.repository;

import org.project.project.model.entity.ReporteAdjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para ReporteAdjunto
 * Incluye queries optimizadas para evitar cargar contenido binario innecesariamente
 */
@Repository
public interface ReporteAdjuntoRepository extends JpaRepository<ReporteAdjunto, Long> {
    
    /**
     * Obtener adjuntos de un reporte ordenados por orden de visualización
     * NOTA: Esta query NO incluye contenido_archivo para optimizar rendimiento
     */
    List<ReporteAdjunto> findByReporte_ReporteIdOrderByOrdenVisualizacionAsc(Long reporteId);
    
    /**
     * Obtener un adjunto completo incluyendo contenido binario (para descarga)
     */
    @Query("SELECT a FROM ReporteAdjunto a WHERE a.adjuntoId = :adjuntoId")
    Optional<ReporteAdjunto> findByIdWithContent(@Param("adjuntoId") Long adjuntoId);
}
