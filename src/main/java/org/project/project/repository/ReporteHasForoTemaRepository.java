package org.project.project.repository;

import org.project.project.model.entity.ReporteHasForoTema;
import org.project.project.model.entity.ReporteHasForoTemaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad ReporteHasForoTema
 * Maneja la relación N:M entre Reportes y Temas del Foro
 */
@Repository
public interface ReporteHasForoTemaRepository extends JpaRepository<ReporteHasForoTema, ReporteHasForoTemaId> {

    /**
     * Obtiene todos los temas del foro vinculados a un reporte específico
     * 
     * @param reporteId ID del reporte
     * @return Lista de relaciones entre reporte y temas del foro
     */
    List<ReporteHasForoTema> findByReporte_ReporteId(Long reporteId);

    /**
     * Obtiene todos los reportes vinculados a un tema del foro específico
     * 
     * @param foroTemaId ID del tema del foro
     * @return Lista de relaciones entre reportes y tema del foro
     */
    List<ReporteHasForoTema> findByForoTema_TemaId(Long foroTemaId);

    /**
     * Verifica si existe una relación entre un reporte y un tema del foro
     * 
     * @param reporteId ID del reporte
     * @param foroTemaId ID del tema del foro
     * @return true si la relación existe
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ReporteHasForoTema r " +
           "WHERE r.reporte.reporteId = :reporteId AND r.foroTema.temaId = :foroTemaId")
    boolean existsByReporteAndForoTema(@Param("reporteId") Long reporteId, @Param("foroTemaId") Long foroTemaId);

    /**
     * Obtiene una relación específica entre un reporte y un tema del foro
     * 
     * @param reporteId ID del reporte
     * @param foroTemaId ID del tema del foro
     * @return Optional con la relación si existe
     */
    Optional<ReporteHasForoTema> findByReporte_ReporteIdAndForoTema_TemaId(Long reporteId, Long foroTemaId);

    /**
     * Elimina la relación entre un reporte y un tema del foro
     * 
     * @param reporteId ID del reporte
     * @param foroTemaId ID del tema del foro
     */
    void deleteByReporte_ReporteIdAndForoTema_TemaId(Long reporteId, Long foroTemaId);

    /**
     * Obtiene el count de reportes vinculados a un tema del foro
     * 
     * @param foroTemaId ID del tema del foro
     * @return Cantidad de reportes
     */
    long countByForoTema_TemaId(Long foroTemaId);

    /**
     * Obtiene el count de temas del foro vinculados a un reporte
     * 
     * @param reporteId ID del reporte
     * @return Cantidad de temas del foro
     */
    long countByReporte_ReporteId(Long reporteId);
}
