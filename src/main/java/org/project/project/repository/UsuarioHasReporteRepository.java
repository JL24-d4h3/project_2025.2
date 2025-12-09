package org.project.project.repository;

import org.project.project.model.entity.UsuarioHasReporte;
import org.project.project.model.entity.UsuarioHasReporteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioHasReporteRepository extends JpaRepository<UsuarioHasReporte, UsuarioHasReporteId> {
    
    /**
     * Encuentra todos los destinatarios de un reporte
     */
    List<UsuarioHasReporte> findByReporte_ReporteId(Long reporteId);
    
    /**
     * Encuentra todos los reportes de un usuario
     */
    List<UsuarioHasReporte> findByUsuario_UsuarioId(Long usuarioId);
}
