package org.project.project.service;

import org.project.project.model.dto.DashboardStatsDTO;
import org.project.project.model.dto.TeamInfoDTO;
import org.project.project.model.dto.TeamMemberDTO;

import org.project.project.repository.*;
import org.project.project.repository.query.RepositorioQueryService;
import org.project.project.model.entity.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio para el Dashboard - Completamente nuevo
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    
    // Repositorios principales
    private final UsuarioRepository usuarioRepository;
    private final EquipoRepository equipoRepository;
    private final TicketRepository ticketRepository;
    private final APIRepository apiRepository;
    private final DocumentacionRepository documentacionRepository;
    
    // Repositorios de relaciones
    private final UsuarioHasProyectoRepository usuarioHasProyectoRepository;
    private final UsuarioHasRepositorioRepository usuarioHasRepositorioRepository;
    
    // Query services
    private final RepositorioQueryService repositorioQueryService;
    
    public DashboardService(
            UsuarioRepository usuarioRepository,
            EquipoRepository equipoRepository,
            TicketRepository ticketRepository,
            APIRepository apiRepository,
            DocumentacionRepository documentacionRepository,
            UsuarioHasProyectoRepository usuarioHasProyectoRepository,
            UsuarioHasRepositorioRepository usuarioHasRepositorioRepository,
            RepositorioQueryService repositorioQueryService) {
        
        this.usuarioRepository = usuarioRepository;
        this.equipoRepository = equipoRepository;
        this.ticketRepository = ticketRepository;
        this.apiRepository = apiRepository;
        this.documentacionRepository = documentacionRepository;
        this.usuarioHasProyectoRepository = usuarioHasProyectoRepository;
        this.usuarioHasRepositorioRepository = usuarioHasRepositorioRepository;
        this.repositorioQueryService = repositorioQueryService;
    }
    
    /**
     * Obtiene las estadísticas completas del dashboard para un usuario
     */
    public DashboardStatsDTO obtenerEstadisticasCompletas(Long usuarioId) {
        logger.info("=== GETTING DASHBOARD STATS FOR USER ID: {} ===", usuarioId);
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        // Estadísticas principales
        stats.setTotalTeams(contarEquiposDelUsuario(usuarioId));
        stats.setTotalProjects(contarProyectosDelUsuario(usuarioId));
        stats.setTotalRepositories(contarRepositoriosDelUsuario(usuarioId));
        stats.setTotalTickets(contarTicketsDelUsuario(usuarioId));
        stats.setTotalApis(contarApisDelUsuario(usuarioId));
        stats.setTotalDocumentations(contarDocumentacionesDelUsuario(usuarioId));
        
        // Estadísticas de proyectos por propietario
        stats.setPersonalProjects(contarProyectosPersonales(usuarioId));
        stats.setGroupProjects(contarProyectosDeEquipo(usuarioId));
        stats.setCompanyProjects(contarProyectosEmpresariales(usuarioId));
        
        // Estadísticas de proyectos por estado
        stats.setPlannedProjects(contarProyectosPorEstado(usuarioId, "PLANEADO"));
        stats.setDevelopmentProjects(contarProyectosPorEstado(usuarioId, "EN_DESARROLLO"));
        stats.setMaintenanceProjects(contarProyectosPorEstado(usuarioId, "MANTENIMIENTO"));
        stats.setClosedProjects(contarProyectosPorEstado(usuarioId, "CERRADO"));
        
        // Estadísticas de repositorios
        stats.setPersonalRepositories(contarRepositoriosPersonales(usuarioId));
        stats.setCollaborativeRepositories(contarRepositoriosColaborativos(usuarioId));
        stats.setPublicRepositories(contarRepositoriosPorVisibilidad(usuarioId, "PUBLICO"));
        stats.setPrivateRepositories(contarRepositoriosPorVisibilidad(usuarioId, "PRIVADO"));
        
        // Estadísticas de tickets por estado
        stats.setSentTickets(contarTicketsPorEstado(usuarioId, "ENVIADO"));
        stats.setReceivedTickets(contarTicketsPorEstado(usuarioId, "RECIBIDO"));
        stats.setInProgressTickets(contarTicketsPorEstado(usuarioId, "EN_PROGRESO"));
        stats.setResolvedTickets(contarTicketsPorEstado(usuarioId, "RESUELTO"));
        stats.setClosedTickets(contarTicketsPorEstado(usuarioId, "CERRADO"));
        stats.setRejectedTickets(contarTicketsPorEstado(usuarioId, "RECHAZADO"));
        
        // Estadísticas de tickets por prioridad
        stats.setLowPriorityTickets(contarTicketsPorPrioridad(usuarioId, "BAJA"));
        stats.setMediumPriorityTickets(contarTicketsPorPrioridad(usuarioId, "MEDIA"));
        stats.setHighPriorityTickets(contarTicketsPorPrioridad(usuarioId, "ALTA"));
        
        logger.info("Stats completed for user {}: Projects={}, Teams={}, Repositories={}", 
                   usuarioId, stats.getTotalProjects(), stats.getTotalTeams(), stats.getTotalRepositories());
        
        return stats;
    }
    
    /**
     * Obtiene la información de todos los equipos de un usuario
     */
    public List<TeamInfoDTO> obtenerEquiposDelUsuario(Long usuarioId) {
        logger.info("Getting teams for user ID: {}", usuarioId);
        try {
            List<Object[]> equiposRaw = equipoRepository.findTeamsByUserId(usuarioId);
            List<TeamInfoDTO> equipos = new ArrayList<>();
            
            logger.info("Found {} raw team records", equiposRaw.size());
            
            for (Object[] row : equiposRaw) {
                try {
                    // FIX: equipo_id es BIGINT (Long), no Integer
                    Long equipoId = ((Number) row[0]).longValue();
                    String nombreEquipo = (String) row[1];
                    
                    // Contar miembros del equipo
                    List<Object[]> miembros = equipoRepository.findUsersByTeamId(equipoId);
                    int cantidadMiembros = miembros.size();
                    
                    equipos.add(new TeamInfoDTO(equipoId, nombreEquipo, cantidadMiembros));
                    logger.debug("Added team: {} (ID: {}, Members: {})", nombreEquipo, equipoId, cantidadMiembros);
                } catch (Exception e) {
                    logger.error("Error processing team row: {}", e.getMessage());
                }
            }
            
            logger.info("Returning {} teams for user {}", equipos.size(), usuarioId);
            return equipos;
        } catch (Exception e) {
            logger.error("Error getting teams for user {}: {}", usuarioId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene la información detallada de los miembros de un equipo
     */
    public List<TeamMemberDTO> obtenerMiembrosDelEquipo(Integer equipoId) {
        List<Object[]> miembrosRaw = equipoRepository.findUsersByTeamId(equipoId.longValue());
        List<TeamMemberDTO> miembros = new ArrayList<>();
        
        for (Object[] row : miembrosRaw) {
            TeamMemberDTO miembro = new TeamMemberDTO();
            
            // Mapear los datos según la estructura del resultado
            if (row.length >= 3) {
                miembro.setUserName((String) row[0]);
                miembro.setLastName((String) row[1]);
                miembro.setEmail((String) row[2]);
                
                // Buscar información adicional del usuario si es necesario
                Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo((String) row[2]);
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    miembro.setUserId(usuario.getUsuarioId());
                    miembro.setDni(usuario.getDni());
                    miembro.setUserCode(usuario.getCodigoUsuario());
                    miembro.setRole("Developer"); // Por defecto, puede mejorarse
                }
            }
            
            miembros.add(miembro);
        }
        
        return miembros;
    }
    
    // Métodos auxiliares para conteos específicos
    
    private long contarEquiposDelUsuario(Long usuarioId) {
        try {
            long count = equipoRepository.countTeamsByUserId(usuarioId);
            logger.debug("Teams count for user {}: {}", usuarioId, count);
            return count;
        } catch (Exception e) {
            logger.error("Error counting teams for user {}: {}", usuarioId, e.getMessage());
            return 0;
        }
    }
    
    private long contarProyectosDelUsuario(Long usuarioId) {
        try {
            return usuarioHasProyectoRepository.countByUserId(usuarioId);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long contarRepositoriosDelUsuario(Long usuarioId) {
        try {
            // SOLUCIÓN: Usar la misma consulta exacta que la sección "I-am-part-of"
            System.out.println("🔍 DASHBOARD - Contando repositorios para usuario ID: " + usuarioId);
            
            // Usar repositorioQueryService.findAllMyRepositories como en I-am-part-of
            // Esto incluye TANTO repositorios donde es colaborador COMO donde es creador
            List<Repositorio> repositoriosCorrectos = repositorioQueryService.findAllMyRepositories(usuarioId, null, null, null);
            System.out.println("📊 DASHBOARD - CONSULTA CORRECTA (usando repositorioQueryService) - Repositorios encontrados: " + repositoriosCorrectos.size());
            
            // Log detallado de cada repositorio
            System.out.println("📋 DASHBOARD - Repositorios detallados (Consulta correcta):");
            for (Repositorio repo : repositoriosCorrectos) {
                System.out.println("  - " + repo.getNombreRepositorio() + " (ID: " + repo.getRepositorioId() + ")");
            }
            
            // Para comparación: mantener la consulta anterior para ver la diferencia
            List<Repositorio> repositoriosIncorrectos = usuarioHasRepositorioRepository.findRepositoriesByUserId(usuarioId);
            System.out.println("📊 DASHBOARD - CONSULTA ANTERIOR (solo tabla intermedia) - Repositorios encontrados: " + repositoriosIncorrectos.size());
            
            if (repositoriosCorrectos.size() != repositoriosIncorrectos.size()) {
                System.out.println("✅ DASHBOARD - ¡PROBLEMA IDENTIFICADO! La consulta anterior no incluía todos los repositorios");
                System.out.println("� DASHBOARD - Repositorios que faltaban en consulta anterior:");
                for (Repositorio repo : repositoriosCorrectos) {
                    boolean faltaba = repositoriosIncorrectos.stream()
                        .noneMatch(r -> r.getRepositorioId().equals(repo.getRepositorioId()));
                    if (faltaba) {
                        System.out.println("  ❌ FALTABA: " + repo.getNombreRepositorio() + " (ID: " + repo.getRepositorioId() + ")");
                    }
                }
            }
            
            return repositoriosCorrectos.size();
        } catch (Exception e) {
            System.err.println("🔥 DASHBOARD - Error contando repositorios: " + e.getMessage());
            logger.error("Error counting repositories for user {}: {}", usuarioId, e.getMessage());
            return 0;
        }
    }
    
    private long contarTicketsDelUsuario(Long usuarioId) {
        try {
            // Contar tickets reportados + tickets asignados
            long ticketsReportados = ticketRepository.countByReportadoPor_UsuarioId(usuarioId);
            long ticketsAsignados = ticketRepository.countByAsignadoA_UsuarioId(usuarioId);
            return ticketsReportados + ticketsAsignados;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long contarApisDelUsuario(Long usuarioId) {
        try {
            // Contamos las APIs donde el usuario tiene acceso
            return apiRepository.count();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long contarDocumentacionesDelUsuario(Long usuarioId) {
        try {
            // Contamos las documentaciones disponibles
            return documentacionRepository.count();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long contarProyectosPersonales(Long usuarioId) {
        try {
            List<Proyecto> proyectos = usuarioHasProyectoRepository.findProjectsByUserId(usuarioId);
            return proyectos.stream()
                .filter(p -> p.getPropietarioProyecto() == Proyecto.PropietarioProyecto.USUARIO)
                .count();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long contarProyectosDeEquipo(Long usuarioId) {
        try {
            List<Proyecto> proyectos = usuarioHasProyectoRepository.findProjectsByUserId(usuarioId);
            return proyectos.stream()
                .filter(p -> p.getPropietarioProyecto() == Proyecto.PropietarioProyecto.GRUPO)
                .count();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long contarProyectosEmpresariales(Long usuarioId) {
        try {
            List<Proyecto> proyectos = usuarioHasProyectoRepository.findProjectsByUserId(usuarioId);
            return proyectos.stream()
                .filter(p -> p.getPropietarioProyecto() == Proyecto.PropietarioProyecto.EMPRESA)
                .count();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long contarProyectosPorEstado(Long usuarioId, String estado) {
        try {
            Proyecto.EstadoProyecto estadoEnum = Proyecto.EstadoProyecto.valueOf(estado);
            List<Proyecto> proyectos = usuarioHasProyectoRepository.findProjectsByUserId(usuarioId);
            return proyectos.stream()
                .filter(p -> p.getEstadoProyecto() == estadoEnum)
                .count();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long contarRepositoriosPersonales(Long usuarioId) {
        try {
            return usuarioHasRepositorioRepository.countByUserIdAndPrivilege(
                usuarioId, UsuarioHasRepositorio.PrivilegioUsuarioRepositorio.EDITOR);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long contarRepositoriosColaborativos(Long usuarioId) {
        try {
            return usuarioHasRepositorioRepository.countByUserIdAndPrivilege(
                usuarioId, UsuarioHasRepositorio.PrivilegioUsuarioRepositorio.LECTOR);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long contarRepositoriosPorVisibilidad(Long usuarioId, String visibilidad) {
        try {
            Repositorio.VisibilidadRepositorio visibilidadEnum = Repositorio.VisibilidadRepositorio.valueOf(visibilidad);
            List<Repositorio> repositorios = usuarioHasRepositorioRepository.findRepositoriesByUserId(usuarioId);
            return repositorios.stream()
                .filter(r -> r.getVisibilidadRepositorio() == visibilidadEnum)
                .count();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long contarTicketsPorEstado(Long usuarioId, String estado) {
        try {
            Ticket.EstadoTicket estadoEnum = Ticket.EstadoTicket.valueOf(estado);
            // Obtener tickets del usuario (reportados + asignados)
            List<Ticket> ticketsReportados = ticketRepository.findByReportadoPor_UsuarioIdOrderByFechaCreacionDesc(usuarioId);
            List<Ticket> ticketsAsignados = ticketRepository.findByAsignadoA_UsuarioIdOrderByFechaCreacionDesc(usuarioId);

            long countReportados = ticketsReportados.stream()
                .filter(t -> t.getEstadoTicket() == estadoEnum)
                .count();
            long countAsignados = ticketsAsignados.stream()
                .filter(t -> t.getEstadoTicket() == estadoEnum)
                .count();

            return countReportados + countAsignados;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long contarTicketsPorPrioridad(Long usuarioId, String prioridad) {
        try {
            Ticket.PrioridadTicket prioridadEnum = Ticket.PrioridadTicket.valueOf(prioridad);
            // Obtener tickets del usuario (reportados + asignados)
            List<Ticket> ticketsReportados = ticketRepository.findByReportadoPor_UsuarioIdOrderByFechaCreacionDesc(usuarioId);
            List<Ticket> ticketsAsignados = ticketRepository.findByAsignadoA_UsuarioIdOrderByFechaCreacionDesc(usuarioId);

            long countReportados = ticketsReportados.stream()
                .filter(t -> t.getPrioridadTicket() == prioridadEnum)
                .count();
            long countAsignados = ticketsAsignados.stream()
                .filter(t -> t.getPrioridadTicket() == prioridadEnum)
                .count();

            return countReportados + countAsignados;
        } catch (Exception e) {
            return 0;
        }
    }
}
