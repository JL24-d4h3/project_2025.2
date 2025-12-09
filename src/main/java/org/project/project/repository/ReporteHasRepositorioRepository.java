package org.project.project.repository;

import org.project.project.model.entity.ReporteHasRepositorio;
import org.project.project.model.entity.ReporteHasRepositorioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository principal para operaciones básicas CRUD de ReporteHasRepositorio.
 */
@Repository
public interface ReporteHasRepositorioRepository extends JpaRepository<ReporteHasRepositorio, ReporteHasRepositorioId> {

    // Buscar por reporte ID
    List<ReporteHasRepositorio> findByReporte_ReporteId(Long reporteId);

    // Buscar por repositorio ID
    List<ReporteHasRepositorio> findByRepositorio_RepositorioId(Long repositorioId);

    // Verificar existencia por reporte y repositorio
    boolean existsByReporte_ReporteIdAndRepositorio_RepositorioId(Long reporteId, Long repositorioId);

    // Contar repositorios por reporte
    long countByReporte_ReporteId(Long reporteId);

    // Contar reportes por repositorio
    long countByRepositorio_RepositorioId(Long repositorioId);

    // Obtener todos los repositorios de un reporte con fetch
    @Query("SELECT rhr FROM ReporteHasRepositorio rhr JOIN FETCH rhr.repositorio WHERE rhr.reporte.reporteId = :reporteId")
    List<ReporteHasRepositorio> findByReporteIdWithRepositorio(@Param("reporteId") Long reporteId);
}

