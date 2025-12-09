package org.project.project.service;

import lombok.extern.slf4j.Slf4j;
import org.project.project.model.dto.DashboardStatsDTO;
import org.project.project.model.dto.ReportMetricsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * MetricsConversionService - Servicio especializado en conversión de métricas
 * 
 * Responsabilidad:
 * - Convertir DashboardStatsDTO (datos del dashboard) → ReportMetricsDTO (formato de exportación)
 * 
 * Este servicio actúa como ADAPTADOR entre:
 * - DashboardService (calcula estadísticas)
 * - ReportExportService (genera archivos Excel/CSV)
 * 
 * Patrón de diseño: Adapter Pattern
 * 
 * @author jleon
 * @since 2025-11-12
 */
@Service
@Slf4j
public class MetricsConversionService {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Convierte estadísticas del dashboard a métricas exportables.
     * 
     * FLUJO:
     * 1. Obtiene DashboardStatsDTO del usuario (con 40+ campos individuales)
     * 2. Transforma a ReportMetricsDTO (con Maps agrupados)
     * 3. Agrega metadata (usuario, rol, fecha)
     * 
     * TRANSFORMACIÓN:
     * - DashboardStatsDTO.personalProjects → ReportMetricsDTO.proyectosPorPropietario.get("USUARIO")
     * - DashboardStatsDTO.groupProjects → ReportMetricsDTO.proyectosPorPropietario.get("GRUPO")
     * - Y así para todos los campos...
     * 
     * @param usuarioId ID del usuario (para filtrar estadísticas)
     * @param username Username del usuario (para metadata)
     * @param role Rol del usuario: dev, qa, po (para metadata)
     * @return ReportMetricsDTO listo para exportar a Excel/CSV
     */
    public ReportMetricsDTO convertirEstadisticasAMetricas(Long usuarioId, String username, String role) {
        log.info("🔄 Convirtiendo estadísticas del dashboard a métricas para usuario: {}", username);
        
        // ===== PASO 1: Obtener estadísticas completas del dashboard =====
        DashboardStatsDTO stats = dashboardService.obtenerEstadisticasCompletas(usuarioId);
        log.debug("📊 Estadísticas obtenidas - Proyectos: {}, Repositorios: {}, Tickets: {}", 
                 stats.getTotalProjects(), stats.getTotalRepositories(), stats.getTotalTickets());
        
        // ===== PASO 2: Crear DTO de métricas =====
        ReportMetricsDTO metrics = new ReportMetricsDTO();
        
        // ===== PASO 3: Metadata =====
        metrics.setUsuarioSolicitante(username);
        metrics.setRolUsuario(role.toUpperCase());
        metrics.setFechaGeneracion(LocalDateTime.now().toString());
        log.debug("📝 Metadata configurada - Usuario: {}, Rol: {}", username, role);
        
        // ===== PASO 4: Totales =====
        metrics.setTotalProyectos(stats.getTotalProjects());
        metrics.setTotalRepositorios(stats.getTotalRepositories());
        metrics.setTotalTickets(stats.getTotalTickets());
        log.debug("📈 Totales configurados - Proyectos: {}, Repos: {}, Tickets: {}", 
                 metrics.getTotalProyectos(), metrics.getTotalRepositorios(), metrics.getTotalTickets());
        
        // ===== PASO 5: Proyectos por propietario =====
        Map<String, Long> proyectosPorPropietario = new HashMap<>();
        proyectosPorPropietario.put("USUARIO", stats.getPersonalProjects());
        proyectosPorPropietario.put("GRUPO", stats.getGroupProjects());
        proyectosPorPropietario.put("EMPRESA", stats.getCompanyProjects());
        metrics.setProyectosPorPropietario(proyectosPorPropietario);
        log.debug("👤 Proyectos por propietario - USUARIO: {}, GRUPO: {}, EMPRESA: {}", 
                 stats.getPersonalProjects(), stats.getGroupProjects(), stats.getCompanyProjects());
        
        // ===== PASO 6: Proyectos por estado =====
        Map<String, Long> proyectosPorEstado = new HashMap<>();
        proyectosPorEstado.put("PLANEADO", stats.getPlannedProjects());
        proyectosPorEstado.put("EN_DESARROLLO", stats.getDevelopmentProjects());
        proyectosPorEstado.put("MANTENIMIENTO", stats.getMaintenanceProjects());
        proyectosPorEstado.put("CERRADO", stats.getClosedProjects());
        metrics.setProyectosPorEstado(proyectosPorEstado);
        log.debug("📊 Proyectos por estado - PLANEADO: {}, EN_DESARROLLO: {}, MANTENIMIENTO: {}, CERRADO: {}", 
                 stats.getPlannedProjects(), stats.getDevelopmentProjects(), 
                 stats.getMaintenanceProjects(), stats.getClosedProjects());
        
        // ===== PASO 7: Repositorios por tipo =====
        Map<String, Long> repositoriosPorTipo = new HashMap<>();
        repositoriosPorTipo.put("PERSONAL", stats.getPersonalRepositories());
        repositoriosPorTipo.put("COLABORATIVO", stats.getCollaborativeRepositories());
        metrics.setRepositoriosPorTipo(repositoriosPorTipo);
        log.debug("📦 Repositorios por tipo - PERSONAL: {}, COLABORATIVO: {}", 
                 stats.getPersonalRepositories(), stats.getCollaborativeRepositories());
        
        // ===== PASO 8: Repositorios por visibilidad =====
        Map<String, Long> repositoriosPorEstado = new HashMap<>();
        repositoriosPorEstado.put("PUBLICO", stats.getPublicRepositories());
        repositoriosPorEstado.put("PRIVADO", stats.getPrivateRepositories());
        metrics.setRepositoriosPorEstado(repositoriosPorEstado);
        log.debug("👁️ Repositorios por visibilidad - PUBLICO: {}, PRIVADO: {}", 
                 stats.getPublicRepositories(), stats.getPrivateRepositories());
        
        // ===== PASO 9: Tickets por estado =====
        Map<String, Long> ticketsPorEstado = new HashMap<>();
        ticketsPorEstado.put("EN_PROGRESO", stats.getInProgressTickets());
        ticketsPorEstado.put("RESUELTO", stats.getResolvedTickets());
        ticketsPorEstado.put("CERRADO", stats.getClosedTickets());
        ticketsPorEstado.put("RECHAZADO", stats.getRejectedTickets());
        metrics.setTicketsPorEstado(ticketsPorEstado);
        log.debug("🎫 Tickets por estado - EN_PROGRESO: {}, RESUELTO: {}, CERRADO: {}, RECHAZADO: {}", 
                 stats.getInProgressTickets(), stats.getResolvedTickets(), 
                 stats.getClosedTickets(), stats.getRejectedTickets());
        
        // ===== PASO 10: Tickets por prioridad =====
        Map<String, Long> ticketsPorPrioridad = new HashMap<>();
        ticketsPorPrioridad.put("BAJA", stats.getLowPriorityTickets());
        ticketsPorPrioridad.put("MEDIA", stats.getMediumPriorityTickets());
        ticketsPorPrioridad.put("ALTA", stats.getHighPriorityTickets());
        metrics.setTicketsPorPrioridad(ticketsPorPrioridad);
        log.debug("⚠️ Tickets por prioridad - BAJA: {}, MEDIA: {}, ALTA: {}", 
                 stats.getLowPriorityTickets(), stats.getMediumPriorityTickets(), stats.getHighPriorityTickets());
        
        log.info("✅ Métricas convertidas exitosamente para usuario: {}", username);
        return metrics;
    }
}
