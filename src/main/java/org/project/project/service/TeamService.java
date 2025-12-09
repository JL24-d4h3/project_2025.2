package org.project.project.service;

import org.project.project.model.entity.Equipo;
import org.project.project.model.entity.EquipoHasProyecto;
import org.project.project.model.entity.EquipoHasRepositorio;
import org.project.project.model.entity.Proyecto;
import org.project.project.model.entity.Repositorio;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.EquipoRepository;
import org.project.project.repository.EquipoHasProyectoRepository;
import org.project.project.repository.EquipoHasRepositorioRepository;
import org.project.project.repository.ProyectoRepository;
import org.project.project.repository.RepositorioRepository;
import org.project.project.repository.UsuarioRepository;
import org.project.project.repository.UsuarioHasProyectoRepository;
import org.project.project.repository.UsuarioHasRepositorioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeamService {

    private static final Logger logger = LoggerFactory.getLogger(TeamService.class);

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private RepositorioRepository repositorioRepository;

    @Autowired
    private UsuarioHasProyectoRepository usuarioHasProyectoRepository;

    @Autowired
    private UsuarioHasRepositorioRepository usuarioHasRepositorioRepository;

    @Autowired
    private EquipoHasProyectoRepository equipoHasProyectoRepository;

    @Autowired
    private EquipoHasRepositorioRepository equipoHasRepositorioRepository;

    // =================== LISTAR EQUIPOS ===================

    /**
     * Obtiene TODOS los equipos creados por el usuario (no solo los asociados a proyectos)
     * Se usa para listar en el formulario de creación de proyectos
     */
    public List<Map<String, Object>> obtenerTodosEquiposDelUsuario(Long usuarioId) {
        logger.info("🔍 [TeamService] Obteniendo TODOS los equipos creados por usuario: {}", usuarioId);
        
        try {
            // Obtener todos los proyectos del usuario
            List<Proyecto> proyectosUsuario = usuarioHasProyectoRepository.findProjectsByUserId(usuarioId);
            logger.debug("  📊 Proyectos encontrados para usuario {}: {}", usuarioId, proyectosUsuario.size());
            
            // Obtener todos los repositorios del usuario
            List<Repositorio> repositoriosUsuario = usuarioHasRepositorioRepository.findRepositoriesByUserId(usuarioId);
            logger.debug("  📦 Repositorios encontrados para usuario {}: {}", usuarioId, repositoriosUsuario.size());
            
            Set<Equipo> equiposUnicos = new HashSet<>();
            
            // Agregar equipos de proyectos
            for (Proyecto proyecto : proyectosUsuario) {
                logger.debug("    🔗 Buscando equipos del proyecto: {}", proyecto.getNombreProyecto());
                List<EquipoHasProyecto> relaciones = equipoHasProyectoRepository.findById_ProjectId(proyecto.getProyectoId());
                for (EquipoHasProyecto relacion : relaciones) {
                    Equipo equipo = equipoRepository.findById(relacion.getId().getEquipoId()).orElse(null);
                    if (equipo != null) {
                        logger.debug("      ✅ Equipo encontrado: {} (creador: {})", 
                            equipo.getNombreEquipo(), 
                            equipo.getCreadoPor() != null ? equipo.getCreadoPor().getUsuarioId() : "NULL");
                        equiposUnicos.add(equipo);
                    }
                }
            }
            
            // Agregar equipos de repositorios
            for (Repositorio repositorio : repositoriosUsuario) {
                logger.debug("    🔗 Buscando equipos del repositorio: {}", repositorio.getNombreRepositorio());
                List<EquipoHasRepositorio> relaciones = equipoHasRepositorioRepository.findById_RepositoryId(repositorio.getRepositorioId());
                for (EquipoHasRepositorio relacion : relaciones) {
                    Equipo equipo = equipoRepository.findById(relacion.getId().getEquipoId()).orElse(null);
                    if (equipo != null) {
                        logger.debug("      ✅ Equipo encontrado: {} (creador: {})", 
                            equipo.getNombreEquipo(), 
                            equipo.getCreadoPor() != null ? equipo.getCreadoPor().getUsuarioId() : "NULL");
                        equiposUnicos.add(equipo);
                    }
                }
            }

            logger.info("✅ [TeamService] Encontrados {} equipos únicos creados por usuario {}", 
                        equiposUnicos.size(), usuarioId);
            
            return equiposUnicos.stream()
                    .map(this::mapearEquipoADTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("⚠️ [TeamService] Error al obtener todos los equipos: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene lista de equipos asociados a un proyecto específico
     * Usado en formularios de invitación (solo el creador del proyecto puede invitar)
     */
    public List<Equipo> obtenerEquiposDelProyecto(Long projectId) {
        logger.info("🔍 [TeamService] Obteniendo equipos del proyecto: {}", projectId);
        
        try {
            List<EquipoHasProyecto> relaciones = equipoHasProyectoRepository.findById_ProjectId(projectId);
            List<Equipo> equipos = new ArrayList<>();
            
            for (EquipoHasProyecto relacion : relaciones) {
                Equipo equipo = equipoRepository.findById(relacion.getId().getEquipoId()).orElse(null);
                if (equipo != null) {
                    equipos.add(equipo);
                }
            }
            
            logger.info("✅ Encontrados {} equipos asociados al proyecto", equipos.size());
            return equipos;
            
        } catch (Exception e) {
            logger.error("⚠️ Error al obtener equipos del proyecto: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene lista de equipos asociados a un repositorio específico
     */
    public List<Equipo> obtenerEquiposDelRepositorio(Long repositoryId) {
        logger.info("🔍 [TeamService] Obteniendo equipos del repositorio: {}", repositoryId);
        
        try {
            List<EquipoHasRepositorio> relaciones = equipoHasRepositorioRepository.findById_RepositoryId(repositoryId);
            List<Equipo> equipos = new ArrayList<>();
            
            for (EquipoHasRepositorio relacion : relaciones) {
                Equipo equipo = equipoRepository.findById(relacion.getId().getEquipoId()).orElse(null);
                if (equipo != null) {
                    equipos.add(equipo);
                }
            }
            
            logger.info("✅ Encontrados {} equipos asociados al repositorio", equipos.size());
            return equipos;
            
        } catch (Exception e) {
            logger.error("⚠️ Error al obtener equipos del repositorio: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene lista de equipos como entidades (sin DTO) para invitaciones
     * Incluye equipos donde el usuario es miembro O creador
     */
    public List<Equipo> obtenerEquiposDelUsuarioParaInvitar(Long usuarioId) {
        logger.info("🔍 [TeamService] Obteniendo equipos del usuario para invitaciones: {}", usuarioId);
        
        try {
            // Obtener todos los proyectos del usuario
            List<Proyecto> proyectosUsuario = usuarioHasProyectoRepository.findProjectsByUserId(usuarioId);
            
            Set<Equipo> equiposUnicos = new HashSet<>();
            
            // Agregar equipos de proyectos
            for (Proyecto proyecto : proyectosUsuario) {
                List<EquipoHasProyecto> relaciones = equipoHasProyectoRepository.findById_ProjectId(proyecto.getProyectoId());
                for (EquipoHasProyecto relacion : relaciones) {
                    Equipo equipo = equipoRepository.findById(relacion.getId().getEquipoId()).orElse(null);
                    if (equipo != null) {
                        equiposUnicos.add(equipo);
                    }
                }
            }
            
            // Agregar equipos de repositorios
            List<Repositorio> repositoriosUsuario = usuarioHasRepositorioRepository.findRepositoriesByUserId(usuarioId);
            for (Repositorio repositorio : repositoriosUsuario) {
                List<EquipoHasRepositorio> relaciones = equipoHasRepositorioRepository.findById_RepositoryId(repositorio.getRepositorioId());
                for (EquipoHasRepositorio relacion : relaciones) {
                    Equipo equipo = equipoRepository.findById(relacion.getId().getEquipoId()).orElse(null);
                    if (equipo != null) {
                        equiposUnicos.add(equipo);
                    }
                }
            }

            logger.info("✅ Encontrados {} equipos para invitaciones", equiposUnicos.size());
            return new ArrayList<>(equiposUnicos);
            
        } catch (Exception e) {
            logger.error("⚠️ Error al obtener equipos para invitaciones: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> obtenerEquiposProyectosUsuario(Long usuarioId) {
        logger.info("🔍 [TeamService] Obteniendo equipos de proyectos para usuario: {}", usuarioId);
        
        try {
            // Obtener todos los proyectos del usuario
            List<Proyecto> proyectosUsuario = usuarioHasProyectoRepository.findProjectsByUserId(usuarioId);
            
            // Obtener equipos que tengan relación con estos proyectos (a través de equipo_has_proyecto)
            Set<Equipo> equiposUnicos = new HashSet<>();
            for (Proyecto proyecto : proyectosUsuario) {
                // Buscar relaciones en equipo_has_proyecto para este proyecto
                List<EquipoHasProyecto> relaciones = equipoHasProyectoRepository.findById_ProjectId(proyecto.getProyectoId());
                for (EquipoHasProyecto relacion : relaciones) {
                    // Obtener el equipo y agregarlo al set
                    Equipo equipo = equipoRepository.findById(relacion.getId().getEquipoId()).orElse(null);
                    if (equipo != null) {
                        equiposUnicos.add(equipo);
                    }
                }
            }

            logger.info("✅ [TeamService] Encontrados {} equipos de proyectos para usuario {}", 
                        equiposUnicos.size(), usuarioId);
            
            return equiposUnicos.stream()
                    .map(this::mapearEquipoADTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("⚠️ [TeamService] Error al obtener equipos de proyectos: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene todos los equipos asociados a repositorios del usuario
     * Solo retorna equipos donde el usuario tiene participación en algún repositorio del equipo
     */
    public List<Map<String, Object>> obtenerEquiposRepositoriosUsuario(Long usuarioId) {
        logger.info("🔍 [TeamService] Obteniendo equipos de repositorios para usuario: {}", usuarioId);
        
        try {
            // Obtener todos los repositorios del usuario
            List<Repositorio> repositoriosUsuario = usuarioHasRepositorioRepository.findRepositoriesByUserId(usuarioId);
            logger.debug("  📦 Repositorios encontrados para usuario {}: {}", usuarioId, repositoriosUsuario.size());
            
            // Obtener equipos que tengan relación con estos repositorios (a través de equipo_has_repositorio)
            Set<Equipo> equiposUnicos = new HashSet<>();
            for (Repositorio repositorio : repositoriosUsuario) {
                logger.debug("    🔗 Buscando equipos del repositorio: {} (ID: {})", 
                    repositorio.getNombreRepositorio(), repositorio.getRepositorioId());
                
                // Buscar relaciones en equipo_has_repositorio para este repositorio
                List<EquipoHasRepositorio> relaciones = equipoHasRepositorioRepository.findById_RepositoryId(repositorio.getRepositorioId());
                logger.debug("      📍 Relaciones encontradas: {}", relaciones.size());
                
                for (EquipoHasRepositorio relacion : relaciones) {
                    // Obtener el equipo y agregarlo al set
                    Equipo equipo = equipoRepository.findById(relacion.getId().getEquipoId()).orElse(null);
                    if (equipo != null) {
                        String creador = equipo.getCreadoPor() != null ? 
                            equipo.getCreadoPor().getUsername() : "NULL";
                        logger.debug("        ✅ Equipo: {} | Creador: {} | Fecha: {}", 
                            equipo.getNombreEquipo(), 
                            creador,
                            equipo.getFechaCreacion());
                        equiposUnicos.add(equipo);
                    } else {
                        logger.warn("        ⚠️ Equipo no encontrado con ID: {}", relacion.getId().getEquipoId());
                    }
                }
            }

            logger.info("✅ [TeamService] Encontrados {} equipos de repositorios para usuario {}", 
                        equiposUnicos.size(), usuarioId);
            
            return equiposUnicos.stream()
                    .map(this::mapearEquipoADTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("❌ [TeamService] Error al obtener equipos de repositorios: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene equipos asociados a proyectos GRUPALES del usuario
     */
    public List<Map<String, Object>> obtenerEquiposProyectosGrupalesUsuario(Long usuarioId) {
        logger.info("🔍 [TeamService] Obteniendo equipos de proyectos GRUPALES para usuario: {}", usuarioId);
        
        try {
            // Obtener todos los proyectos del usuario
            List<Proyecto> todosProyectos = usuarioHasProyectoRepository.findProjectsByUserId(usuarioId);
            logger.info("  📊 Total de proyectos para usuario {}: {}", usuarioId, todosProyectos.size());
            
            // Filtrar solo proyectos GRUPO
            List<Proyecto> proyectosGrupales = todosProyectos.stream()
                    .filter(p -> "GRUPO".equals(p.getPropietarioProyecto().name()))
                    .collect(Collectors.toList());
            
            logger.info("  🔵 Proyectos GRUPO encontrados: {}", proyectosGrupales.size());
            for (Proyecto p : proyectosGrupales) {
                logger.info("      → Proyecto GRUPO: {} (ID: {})", p.getNombreProyecto(), p.getProyectoId());
            }
            
            // Obtener equipos que tengan relación con estos proyectos grupales
            Set<Equipo> equiposUnicos = new HashSet<>();
            for (Proyecto proyecto : proyectosGrupales) {
                logger.info("    🔗 Buscando equipos del proyecto GRUPO: {} (ID: {})", 
                    proyecto.getNombreProyecto(), proyecto.getProyectoId());
                
                List<EquipoHasProyecto> relaciones = equipoHasProyectoRepository.findById_ProjectId(proyecto.getProyectoId());
                logger.info("      📍 Relaciones encontradas: {}", relaciones.size());
                
                for (EquipoHasProyecto relacion : relaciones) {
                    logger.info("        → RelacionID equipo={}, proyecto={}", 
                        relacion.getId().getEquipoId(), relacion.getId().getProyectoId());
                    
                    Equipo equipo = equipoRepository.findById(relacion.getId().getEquipoId()).orElse(null);
                    if (equipo != null) {
                        String creador = equipo.getCreadoPor() != null ? 
                            equipo.getCreadoPor().getUsername() : "NULL";
                        logger.info("        ✅ Equipo: {} | Creador: {} | Fecha: {}", 
                            equipo.getNombreEquipo(), 
                            creador,
                            equipo.getFechaCreacion());
                        equiposUnicos.add(equipo);
                    } else {
                        logger.warn("        ⚠️ Equipo no encontrado con ID: {}", relacion.getId().getEquipoId());
                    }
                }
            }

            logger.info("✅ [TeamService] Encontrados {} equipos ÚNICOS de proyectos GRUPALES para usuario {}", 
                        equiposUnicos.size(), usuarioId);
            
            return equiposUnicos.stream()
                    .map(this::mapearEquipoADTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("❌ [TeamService] Error al obtener equipos de proyectos GRUPALES: {}", e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene equipos asociados a proyectos EMPRESARIALES del usuario
     */
    public List<Map<String, Object>> obtenerEquiposProyectosEmpresarialesUsuario(Long usuarioId) {
        logger.info("🔍 [TeamService] Obteniendo equipos de proyectos EMPRESARIALES para usuario: {}", usuarioId);
        
        try {
            // Obtener todos los proyectos del usuario
            List<Proyecto> todosProyectos = usuarioHasProyectoRepository.findProjectsByUserId(usuarioId);
            logger.info("  📊 Total de proyectos para usuario {}: {}", usuarioId, todosProyectos.size());
            
            // Filtrar solo proyectos EMPRESA
            List<Proyecto> proyectosEmpresariales = todosProyectos.stream()
                    .filter(p -> "EMPRESA".equals(p.getPropietarioProyecto().name()))
                    .collect(Collectors.toList());
            
            logger.info("  🟣 Proyectos EMPRESA encontrados: {}", proyectosEmpresariales.size());
            for (Proyecto p : proyectosEmpresariales) {
                logger.info("      → Proyecto EMPRESA: {} (ID: {})", p.getNombreProyecto(), p.getProyectoId());
            }
            
            // Obtener equipos que tengan relación con estos proyectos empresariales
            Set<Equipo> equiposUnicos = new HashSet<>();
            for (Proyecto proyecto : proyectosEmpresariales) {
                logger.info("    🔗 Buscando equipos del proyecto EMPRESA: {} (ID: {})", 
                    proyecto.getNombreProyecto(), proyecto.getProyectoId());
                
                List<EquipoHasProyecto> relaciones = equipoHasProyectoRepository.findById_ProjectId(proyecto.getProyectoId());
                logger.info("      📍 Relaciones encontradas: {}", relaciones.size());
                
                for (EquipoHasProyecto relacion : relaciones) {
                    logger.info("        → RelacionID equipo={}, proyecto={}", 
                        relacion.getId().getEquipoId(), relacion.getId().getProyectoId());
                    
                    Equipo equipo = equipoRepository.findById(relacion.getId().getEquipoId()).orElse(null);
                    if (equipo != null) {
                        String creador = equipo.getCreadoPor() != null ? 
                            equipo.getCreadoPor().getUsername() : "NULL";
                        logger.info("        ✅ Equipo: {} | Creador: {} | Fecha: {}", 
                            equipo.getNombreEquipo(), 
                            creador,
                            equipo.getFechaCreacion());
                        equiposUnicos.add(equipo);
                    } else {
                        logger.warn("        ⚠️ Equipo no encontrado con ID: {}", relacion.getId().getEquipoId());
                    }
                }
            }

            logger.info("✅ [TeamService] Encontrados {} equipos ÚNICOS de proyectos EMPRESARIALES para usuario {}", 
                        equiposUnicos.size(), usuarioId);
            
            return equiposUnicos.stream()
                    .map(this::mapearEquipoADTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("❌ [TeamService] Error al obtener equipos de proyectos EMPRESARIALES: {}", e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene proyectos grupales del usuario (para crear equipo)
     * SOLO proyectos GRUPO creados por el usuario actual
     * Filtra por: propietarioProyecto = GRUPO AND createdBy = usuarioId
     */
    public List<Map<String, Object>> obtenerProyectosGrupalesUsuario(Long usuarioId) {
        logger.info("🔍 [TeamService] Obteniendo proyectos GRUPO creados por usuario: {}", usuarioId);
        
        try {
            // Proyectos GRUPO donde createdBy = usuario actual
            List<Proyecto> proyectos = proyectoRepository.findAll().stream()
                    .filter(p -> "GRUPO".equals(p.getPropietarioProyecto().name()))
                    .filter(p -> p.getCreatedBy().getUsuarioId().equals(usuarioId))
                    .distinct()
                    .collect(Collectors.toList());

            logger.info("✅ [TeamService] Encontrados {} proyectos GRUPO creados por usuario {}", 
                        proyectos.size(), usuarioId);
            
            return proyectos.stream()
                    .map(this::mapearProyectoADTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("❌ [TeamService] Error al obtener proyectos GRUPO del usuario: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene proyectos empresariales del usuario (para crear equipo)
     * SOLO proyectos EMPRESA creados por el usuario actual
     * Filtra por: propietarioProyecto = EMPRESA AND createdBy = usuarioId
     */
    public List<Map<String, Object>> obtenerProyectosEmpresarialesUsuario(Long usuarioId) {
        logger.info("🔍 [TeamService] Obteniendo proyectos EMPRESA creados por usuario: {}", usuarioId);
        
        try {
            // Proyectos EMPRESA donde createdBy = usuario actual
            List<Proyecto> proyectos = proyectoRepository.findAll().stream()
                    .filter(p -> "EMPRESA".equals(p.getPropietarioProyecto().name()))
                    .filter(p -> p.getCreatedBy().getUsuarioId().equals(usuarioId))
                    .distinct()
                    .collect(Collectors.toList());

            logger.info("✅ [TeamService] Encontrados {} proyectos EMPRESA creados por usuario {}", 
                        proyectos.size(), usuarioId);
            
            return proyectos.stream()
                    .map(this::mapearProyectoADTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("❌ [TeamService] Error al obtener proyectos EMPRESA del usuario: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene repositorios colaborativos del usuario (para crear equipo)
     * SOLO repositorios COLABORATIVO creados por el usuario actual
     * Filtra por: tipoRepositorio = COLABORATIVO AND creador = usuarioId
     */
    public List<Map<String, Object>> obtenerRepositoriosColaborativosUsuario(Long usuarioId) {
        logger.info("🔍 [TeamService] Obteniendo repositorios COLABORATIVO creados por usuario: {}", usuarioId);
        
        try {
            // Obtener TODOS los repositorios para diagnóstico
            List<Repositorio> todosRepositorios = repositorioRepository.findAll();
            logger.info("📊 [TeamService] Total repositorios en BD: {}", todosRepositorios.size());
            
            // Log de TODOS los repositorios para diagnóstico
            todosRepositorios.forEach(r -> {
                logger.info("   🔎 Repo: ID={}, Nombre='{}', Tipo='{}', Creador={}",
                        r.getRepositorioId(), r.getNombreRepositorio(), r.getTipoRepositorio(),
                        r.getCreador() != null ? r.getCreador().getUsuarioId() : "NULL");
            });
            
            // Contar por tipo
            long colaborativos = todosRepositorios.stream()
                    .filter(r -> r.getTipoRepositorio() == Repositorio.TipoRepositorio.COLABORATIVO)
                    .count();
            logger.info("   → Repositorios COLABORATIVO: {}", colaborativos);
            
            // Contar por creador
            long delUsuario = todosRepositorios.stream()
                    .filter(r -> r.getCreador() != null && r.getCreador().getUsuarioId().equals(usuarioId))
                    .count();
            logger.info("   → Repositorios creados por usuario {}: {}", usuarioId, delUsuario);
            
            // Repositorios COLABORATIVO donde creador = usuario actual
            List<Repositorio> repositorios = todosRepositorios.stream()
                    .filter(r -> r.getTipoRepositorio() == Repositorio.TipoRepositorio.COLABORATIVO)
                    .filter(r -> r.getCreador() != null && r.getCreador().getUsuarioId().equals(usuarioId))
                    .distinct()
                    .collect(Collectors.toList());

            logger.info("✅ [TeamService] Encontrados {} repositorios COLABORATIVO creados por usuario {}", 
                        repositorios.size(), usuarioId);
            
            // Log detallado de cada repositorio encontrado
            repositorios.forEach(r -> {
                logger.info("   📦 Repositorio: ID={}, Nombre='{}', Tipo='{}', Creador={}",
                        r.getRepositorioId(), r.getNombreRepositorio(), r.getTipoRepositorio(),
                        r.getCreador() != null ? r.getCreador().getUsuarioId() : "NULL");
            });
            
            return repositorios.stream()
                    .map(this::mapearRepositorioADTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("⚠️ [TeamService] Error al obtener repositorios colaborativos del usuario: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // =================== CREAR EQUIPO ===================

    /**
     * Crea un nuevo equipo asociado a múltiples proyectos
     * Se crean las entradas en la tabla equipo_has_proyecto para cada proyecto
     */
    public Equipo crearEquipoConProyectos(String nombreEquipo, List<Long> proyectosIds, Long usuarioId) {
        logger.info("➕ [TeamService] Creando equipo '{}' con {} proyectos", nombreEquipo, proyectosIds.size());
        System.out.println("\n🔬 [crearEquipoConProyectos] INICIANDO");
        System.out.println("  nombreEquipo: " + nombreEquipo);
        System.out.println("  proyectosIds: " + proyectosIds);
        System.out.println("  usuarioId: " + usuarioId);
        
        // Crear el equipo
        Equipo equipo = new Equipo();
        equipo.setNombreEquipo(nombreEquipo);
        
        // Obtener el usuario creador
        Usuario creador = new Usuario();
        creador.setUsuarioId(usuarioId);
        equipo.setCreadoPor(creador);
        equipo.setFechaCreacion(java.time.LocalDateTime.now());
        
        System.out.println("  📝 Equipo object creado (antes de guardar en BD)");
        
        Equipo equipoGuardado = equipoRepository.save(equipo);
        System.out.println("  ✅ Equipo guardado en BD con ID: " + equipoGuardado.getEquipoId());

        // Asociar el equipo a los proyectos
        System.out.println("  🔗 Iniciando asociación a proyectos...");
        for (Long proyectoId : proyectosIds) {
            System.out.println("    → Procesando proyectoId: " + proyectoId);
            
            Proyecto proyecto = proyectoRepository.findById(proyectoId)
                    .orElseThrow(() -> new RuntimeException("Proyecto " + proyectoId + " no encontrado"));
            System.out.println("      ✓ Proyecto encontrado: " + proyecto.getNombreProyecto());

            // Crear la relación en equipo_has_proyecto usando el constructor
            EquipoHasProyecto relacion = new EquipoHasProyecto(equipoGuardado, proyecto, EquipoHasProyecto.PrivilegioEquipoProyecto.LECTOR);
            EquipoHasProyecto relacionGuardada = equipoHasProyectoRepository.save(relacion);
            System.out.println("      ✓ Relación guardada en BD");

            logger.info("  ✓ Asociado equipo {} al proyecto {}", equipoGuardado.getEquipoId(), proyectoId);
        }

        System.out.println("  ✅ Equipo " + nombreEquipo + " creado exitosamente con ID: " + equipoGuardado.getEquipoId());
        System.out.println("🔬 [crearEquipoConProyectos] FIN\n");
        return equipoGuardado;
    }

    /**
     * Crea un nuevo equipo asociado a UN SOLO proyecto (versión simplificada)
     * Se debe crear la entrada en la tabla equipo_has_proyecto
     */
    public Equipo crearEquipoEnProyecto(String nombreEquipo, Long proyectoId, Long usuarioId) {
        logger.info("➕ [TeamService] Creando equipo '{}' en proyecto: {}", nombreEquipo, proyectoId);
        
        List<Long> proyectosIds = new ArrayList<>();
        proyectosIds.add(proyectoId);
        return crearEquipoConProyectos(nombreEquipo, proyectosIds, usuarioId);
    }

    /**
     * Asocia un equipo existente a un nuevo proyecto
     * Crea una entrada en la tabla equipo_has_proyecto
     */
    public void asociarEquipoAProyecto(Long equipoId, Long proyectoId) {
        logger.info("🔗 [TeamService] Asociando equipo {} al proyecto {}", equipoId, proyectoId);
        
        try {
            Equipo equipo = equipoRepository.findById(equipoId)
                    .orElseThrow(() -> new RuntimeException("Equipo " + equipoId + " no encontrado"));
            
            Proyecto proyecto = proyectoRepository.findById(proyectoId)
                    .orElseThrow(() -> new RuntimeException("Proyecto " + proyectoId + " no encontrado"));

            // Usar el constructor que inicializa correctamente el ID
            EquipoHasProyecto relacion = new EquipoHasProyecto(equipo, proyecto, EquipoHasProyecto.PrivilegioEquipoProyecto.LECTOR);
            equipoHasProyectoRepository.save(relacion);

            logger.info("✅ [TeamService] Equipo {} asociado exitosamente al proyecto {}", equipoId, proyectoId);
        } catch (Exception e) {
            logger.error("❌ [TeamService] Error al asociar equipo a proyecto: {}", e.getMessage());
            throw new RuntimeException("Error al asociar equipo a proyecto: " + e.getMessage());
        }
    }

    /**
     * Crea un nuevo equipo asociado a múltiples repositorios
     * Se crean las entradas en la tabla equipo_has_repositorio para cada repositorio
     */
    public Equipo crearEquipoConRepositorios(String nombreEquipo, List<Long> repositoriosIds, Long usuarioId) {
        logger.info("➕ [TeamService] Creando equipo '{}' con {} repositorios", nombreEquipo, repositoriosIds.size());
        
        // Crear el equipo
        Equipo equipo = new Equipo();
        equipo.setNombreEquipo(nombreEquipo);
        
        // Obtener el usuario creador
        Usuario creador = new Usuario();
        creador.setUsuarioId(usuarioId);
        equipo.setCreadoPor(creador);
        equipo.setFechaCreacion(java.time.LocalDateTime.now());
        
        Equipo equipoGuardado = equipoRepository.save(equipo);

        // Asociar el equipo a los repositorios
        for (Long repositorioId : repositoriosIds) {
            Repositorio repositorio = repositorioRepository.findById(repositorioId)
                    .orElseThrow(() -> new RuntimeException("Repositorio " + repositorioId + " no encontrado"));

            // Crear la relación en equipo_has_repositorio usando el constructor
            EquipoHasRepositorio relacion = new EquipoHasRepositorio(equipoGuardado, repositorio, EquipoHasRepositorio.PrivilegioEquipoRepositorio.LECTOR);
            equipoHasRepositorioRepository.save(relacion);

            logger.info("  ✓ Asociado equipo {} al repositorio {}", equipoGuardado.getEquipoId(), repositorioId);
        }

        logger.info("✅ [TeamService] Equipo '{}' creado exitosamente con ID: {}", nombreEquipo, equipoGuardado.getEquipoId());
        return equipoGuardado;
    }

    /**
     * Crea un nuevo equipo asociado a UN SOLO repositorio (versión simplificada)
     */
    public Equipo crearEquipoEnRepositorio(String nombreEquipo, Long repositorioId, Long usuarioId) {
        logger.info("➕ [TeamService] Creando equipo '{}' en repositorio: {}", nombreEquipo, repositorioId);
        
        List<Long> repositoriosIds = new ArrayList<>();
        repositoriosIds.add(repositorioId);
        return crearEquipoConRepositorios(nombreEquipo, repositoriosIds, usuarioId);
    }

    /**
     * Crea un equipo temporal con descripción para un repositorio
     * NOTA: Por ahora la descripción solo se usa para logs, el entity Equipo no tiene campo descripción
     */
    public Equipo crearEquipoEnRepositorio(String nombreEquipo, String descripcion, Long repositorioId, Long usuarioId) {
        logger.info("➕ [TeamService] Creando equipo '{}' (descripción: '{}') en repositorio: {}", nombreEquipo, descripcion, repositorioId);
        
        List<Long> repositoriosIds = new ArrayList<>();
        repositoriosIds.add(repositorioId);
        return crearEquipoConRepositorios(nombreEquipo, repositoriosIds, usuarioId);
    }

    // =================== VER DETALLE EQUIPO ===================

    /**
     * Obtiene los detalles completos de un equipo con sus miembros
     */
    public Map<String, Object> obtenerDetallesEquipo(Long equipoId) {
        logger.info("🔍 [TeamService] Obteniendo detalles del equipo: {}", equipoId);
        
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        Map<String, Object> detalles = new HashMap<>();
        detalles.put("equipo", equipo);
        detalles.put("nombreEquipo", equipo.getNombreEquipo());
        detalles.put("equipoId", equipo.getEquipoId());
        detalles.put("cantidadMiembros", equipo.getUsuarios() != null ? equipo.getUsuarios().size() : 0);
        detalles.put("miembros", equipo.getUsuarios() != null ? 
                equipo.getUsuarios().stream()
                        .map(this::mapearUsuarioADTO)
                        .collect(Collectors.toList()) : 
                new ArrayList<>());

        logger.info("✅ [TeamService] Detalles del equipo obtenidos: {} miembros", 
                    detalles.get("cantidadMiembros"));
        
        return detalles;
    }

    // =================== ACTUALIZAR EQUIPO ===================

    /**
     * Actualiza la información del equipo (solo nombre)
     */
    public Equipo actualizarEquipo(Long equipoId, String nuevoNombre) {
        logger.info("✏️ [TeamService] Actualizando equipo {}: nuevo nombre = '{}'", equipoId, nuevoNombre);
        
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        equipo.setNombreEquipo(nuevoNombre);
        Equipo equipoActualizado = equipoRepository.save(equipo);

        logger.info("✅ [TeamService] Equipo {} actualizado exitosamente", equipoId);
        return equipoActualizado;
    }

    // =================== MÉTODOS AUXILIARES ===================

    /**
     * Convierte un Equipo a DTO
     */
    private Map<String, Object> mapearEquipoADTO(Equipo equipo) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("equipoId", equipo.getEquipoId());
        dto.put("nombreEquipo", equipo.getNombreEquipo());
        dto.put("cantidadMiembros", equipo.getUsuarios() != null ? equipo.getUsuarios().size() : 0);
        return dto;
    }

    /**
     * Convierte un Proyecto a DTO
     */
    private Map<String, Object> mapearProyectoADTO(Proyecto proyecto) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("proyectoId", proyecto.getProyectoId());
        dto.put("nombreProyecto", proyecto.getNombreProyecto());
        dto.put("descripcion", proyecto.getDescripcionProyecto());
        dto.put("propietario", proyecto.getPropietarioProyecto());
        return dto;
    }

    /**
     * Convierte un Repositorio a DTO
     */
    private Map<String, Object> mapearRepositorioADTO(Repositorio repositorio) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("repositorioId", repositorio.getRepositorioId());
        dto.put("nombreRepositorio", repositorio.getNombreRepositorio());
        dto.put("descripcion", repositorio.getDescripcionRepositorio());
        dto.put("tipoRepositorio", repositorio.getTipoRepositorio());
        return dto;
    }

    /**
     * Convierte un Usuario a DTO
     */
    private Map<String, Object> mapearUsuarioADTO(Usuario usuario) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("usuarioId", usuario.getUsuarioId());
        dto.put("nombreUsuario", usuario.getNombreUsuario());
        dto.put("username", usuario.getUsername());
        dto.put("correo", usuario.getCorreo());
        return dto;
    }

    /**
     * Actualiza los permisos de un equipo en un proyecto específico
     * @param equipoId ID del equipo
     * @param proyectoId ID del proyecto
     * @param privilegio Nuevo privilegio (LECTOR, COMENTADOR, EDITOR)
     */
    public void actualizarPermisosEquipoEnProyecto(Long equipoId, Long proyectoId, String privilegio) {
        logger.info("🔐 [TeamService] Actualizando permisos del equipo {} en proyecto {} a: {}", 
                   equipoId, proyectoId, privilegio);
        
        try {
            // Buscar la relación equipo_has_proyecto
            EquipoHasProyecto relacion = equipoHasProyectoRepository.findByEquipoIdAndProyectoId(equipoId, proyectoId);
            
            if (relacion == null) {
                logger.error("❌ [TeamService] Relación no encontrada para equipo {} y proyecto {}", equipoId, proyectoId);
                throw new RuntimeException("Relación equipo-proyecto no encontrada");
            }
            
            // Convertir el string a enum y actualizar
            EquipoHasProyecto.PrivilegioEquipoProyecto privsEnum = 
                    EquipoHasProyecto.PrivilegioEquipoProyecto.valueOf(privilegio.toUpperCase());
            
            relacion.setPrivilegio(privsEnum);
            equipoHasProyectoRepository.save(relacion);
            
            logger.info("✅ [TeamService] Permisos actualizados exitosamente para equipo {} en proyecto {}", 
                       equipoId, proyectoId);
        } catch (Exception e) {
            logger.error("❌ [TeamService] Error al actualizar permisos: {}", e.getMessage());
            throw new RuntimeException("Error al actualizar permisos: " + e.getMessage());
        }
    }

    /**
     * Asocia un equipo existente a un nuevo repositorio
     * Crea una entrada en la tabla equipo_has_repositorio
     */
    public void asociarEquipoARepositorio(Long equipoId, Long repositorioId) {
        logger.info("🔗 [TeamService] Asociando equipo {} al repositorio {}", equipoId, repositorioId);
        
        try {
            Equipo equipo = equipoRepository.findById(equipoId)
                    .orElseThrow(() -> new RuntimeException("Equipo " + equipoId + " no encontrado"));
            
            Repositorio repositorio = repositorioRepository.findById(repositorioId)
                    .orElseThrow(() -> new RuntimeException("Repositorio " + repositorioId + " no encontrado"));

            // Usar el constructor que inicializa correctamente el ID
            EquipoHasRepositorio relacion = new EquipoHasRepositorio(equipo, repositorio, EquipoHasRepositorio.PrivilegioEquipoRepositorio.LECTOR);
            equipoHasRepositorioRepository.save(relacion);

            logger.info("✅ [TeamService] Equipo {} asociado exitosamente al repositorio {}", equipoId, repositorioId);
        } catch (Exception e) {
            logger.error("❌ [TeamService] Error al asociar equipo a repositorio: {}", e.getMessage());
            throw new RuntimeException("Error al asociar equipo a repositorio: " + e.getMessage());
        }
    }

    /**
     * Asigna un equipo a un repositorio con permisos específicos
     * Crea la relación si no existe, o actualiza los permisos si ya existe
     * @param equipoId ID del equipo
     * @param repositorioId ID del repositorio
     * @param privilegio Privilegio (LECTOR o EDITOR)
     */
    public void asignarEquipoARepositorio(Long equipoId, Long repositorioId, String privilegio) {
        logger.info("🔗 [TeamService] Asignando equipo {} a repositorio {} con privilegio: {}", 
                   equipoId, repositorioId, privilegio);
        
        try {
            // Buscar la relación existente
            EquipoHasRepositorio relacion = equipoHasRepositorioRepository.findByEquipoIdAndRepositorioId(equipoId, repositorioId);
            
            // Convertir el string a enum
            EquipoHasRepositorio.PrivilegioEquipoRepositorio privsEnum = 
                    EquipoHasRepositorio.PrivilegioEquipoRepositorio.valueOf(privilegio.toUpperCase());
            
            if (relacion == null) {
                // No existe la relación, crearla
                logger.info("  ➕ Creando nueva relación equipo-repositorio");
                
                Equipo equipo = equipoRepository.findById(equipoId)
                        .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
                Repositorio repositorio = repositorioRepository.findById(repositorioId)
                        .orElseThrow(() -> new RuntimeException("Repositorio no encontrado"));
                
                relacion = new EquipoHasRepositorio(equipo, repositorio, privsEnum);
                equipoHasRepositorioRepository.save(relacion);
                logger.info("✅ [TeamService] Relación creada exitosamente");
            } else {
                // Ya existe, solo actualizar permisos
                logger.info("  ✏️ Actualizando permisos de relación existente");
                relacion.setPrivilegio(privsEnum);
                equipoHasRepositorioRepository.save(relacion);
                logger.info("✅ [TeamService] Permisos actualizados exitosamente");
            }
        } catch (Exception e) {
            logger.error("❌ [TeamService] Error al asignar equipo a repositorio: {}", e.getMessage());
            throw new RuntimeException("Error al asignar equipo a repositorio: " + e.getMessage());
        }
    }

    /**
     * Actualiza los permisos de un equipo en un repositorio específico
     * @param equipoId ID del equipo
     * @param repositorioId ID del repositorio
     * @param privilegio Nuevo privilegio (LECTOR o EDITOR)
     */
    public void actualizarPermisosEquipoEnRepositorio(Long equipoId, Long repositorioId, String privilegio) {
        logger.info("🔐 [TeamService] Actualizando permisos del equipo {} en repositorio {} a: {}", 
                   equipoId, repositorioId, privilegio);
        
        try {
            // Buscar la relación equipo_has_repositorio
            EquipoHasRepositorio relacion = equipoHasRepositorioRepository.findByEquipoIdAndRepositorioId(equipoId, repositorioId);
            
            if (relacion == null) {
                logger.error("❌ [TeamService] Relación no encontrada para equipo {} y repositorio {}", equipoId, repositorioId);
                throw new RuntimeException("Relación equipo-repositorio no encontrada");
            }
            
            // Convertir el string a enum y actualizar
            EquipoHasRepositorio.PrivilegioEquipoRepositorio privsEnum = 
                    EquipoHasRepositorio.PrivilegioEquipoRepositorio.valueOf(privilegio.toUpperCase());
            
            relacion.setPrivilegio(privsEnum);
            equipoHasRepositorioRepository.save(relacion);
            
            logger.info("✅ [TeamService] Permisos actualizados exitosamente para equipo {} en repositorio {}", 
                       equipoId, repositorioId);
        } catch (Exception e) {
            logger.error("❌ [TeamService] Error al actualizar permisos: {}", e.getMessage());
            throw new RuntimeException("Error al actualizar permisos: " + e.getMessage());
        }
    }
    
    /**
     * Verifica si existe un equipo con el nombre dado en el repositorio
     * @param repositoryId ID del repositorio
     * @param teamName Nombre del equipo a verificar
     * @return true si existe, false si no existe
     */
    public boolean existsTeamByNameInRepository(Long repositoryId, String teamName) {
        logger.info("🔍 [TeamService] Verificando si existe equipo '{}' en repositorio {}", teamName, repositoryId);
        
        try {
            // Obtener todos los equipos del repositorio
            List<Equipo> teams = obtenerEquiposDelRepositorio(repositoryId);
            
            // Verificar si algún equipo tiene ese nombre (case-insensitive)
            boolean exists = teams.stream()
                    .anyMatch(team -> teamName.trim().equalsIgnoreCase(team.getNombreEquipo()));
            
            logger.info("✅ [TeamService] Equipo '{}' {} en repositorio {}", 
                       teamName, exists ? "YA EXISTE" : "NO EXISTE", repositoryId);
            
            return exists;
            
        } catch (Exception e) {
            logger.error("❌ [TeamService] Error al verificar existencia de equipo: {}", e.getMessage());
            return false; // En caso de error, permitir el nombre
        }
    }
}
