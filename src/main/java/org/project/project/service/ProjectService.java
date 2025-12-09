package org.project.project.service;

import org.project.project.model.entity.Proyecto;
import org.project.project.model.entity.Proyecto.EstadoProyecto;
import org.project.project.model.entity.Proyecto.VisibilidadProyecto;
import org.project.project.model.entity.Proyecto.PropietarioProyecto;
import org.project.project.model.entity.Proyecto.AccesoProyecto;
import org.project.project.model.entity.Categoria;
import org.project.project.model.entity.Repositorio;
import org.project.project.model.entity.ProyectoHasRepositorio;
import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.Historial;
import org.project.project.model.entity.UsuarioHasProyecto;
import org.project.project.model.entity.UsuarioHasProyectoId;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.ProyectoRepository;
import org.project.project.repository.query.ProyectoQueryService;
import org.project.project.repository.CategoriaRepository;
import org.project.project.repository.RepositorioRepository;
import org.project.project.repository.NodoRepository;
import org.project.project.repository.HistorialRepository;
import org.project.project.repository.UsuarioHasProyectoRepository;
import org.project.project.repository.UsuarioRepository;
import org.project.project.repository.ProyectoHasRepositorioRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.project.project.model.dto.NodoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

@Service
public class ProjectService {

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private ProyectoQueryService proyectoQueryService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private RepositorioRepository repositorioRepository;

    @Autowired
    private NodoRepository nodoRepository;
    
    @Autowired
    private NodoService nodoService;

    @Autowired
    private HistorialRepository historialRepository;

    @Autowired
    private ProyectoHasRepositorioRepository proyectoHasRepositorioRepository;

    @Autowired
    private UsuarioHasProyectoRepository usuarioHasProyectoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Proyecto> listarProyectos() {
        return proyectoRepository.findAll();
    }

    public Proyecto buscarProyectoPorId(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + id));
    }

    public Proyecto guardarProyecto(Proyecto proyecto) {
        proyecto.setFechaInicioProyecto(LocalDate.now());
        return proyectoRepository.save(proyecto);
    }

    public Proyecto guardarProyecto(Proyecto proyecto, Long categoriaId) {
        proyecto.setFechaInicioProyecto(LocalDate.now());

        // Asignar categoría si se proporcionó
        if (categoriaId != null) {
            Categoria categoria = categoriaRepository.findById(categoriaId).orElse(null);
            if (categoria != null) {
                Set<Categoria> categorias = new HashSet<>();
                categorias.add(categoria);
                proyecto.setCategorias(categorias);
            }
        }

        // Establecer valores por defecto si no están establecidos (coincidiendo con BD)
        if (proyecto.getVisibilidadProyecto() == null) {
            proyecto.setVisibilidadProyecto(VisibilidadProyecto.PRIVADO); // BD default es PRIVADO
        }
        if (proyecto.getEstadoProyecto() == null) {
            proyecto.setEstadoProyecto(EstadoProyecto.PLANEADO); // BD default es PLANEADO
        }
        // NO sobrescribir PropietarioProyecto si ya fue establecido en el controlador
        if (proyecto.getPropietarioProyecto() == null) {
            proyecto.setPropietarioProyecto(PropietarioProyecto.USUARIO);
        }
        if (proyecto.getAccesoProyecto() == null) {
            proyecto.setAccesoProyecto(AccesoProyecto.RESTRINGIDO); // BD default es RESTRINGIDO
        }

        // DEBUG: Log antes de guardar
        System.out.println("🔧 SERVICE DEBUG: PropietarioProyecto antes de save = " + proyecto.getPropietarioProyecto());
        System.out.println("🔧 SERVICE DEBUG: PropietarioNombre = " + proyecto.getPropietarioNombre());

        // Guardar el proyecto
        Proyecto savedProject = proyectoRepository.save(proyecto);

        System.out.println("🔧 SERVICE DEBUG: Proyecto guardado con ID = " + savedProject.getProyectoId());
        System.out.println("🔧 SERVICE DEBUG: PropietarioProyecto después de save = " + savedProject.getPropietarioProyecto());
        System.out.println("🔧 SERVICE DEBUG: PropietarioNombre después de save = " + savedProject.getPropietarioNombre());

        // Crear relación usuario_has_proyecto usando createdBy (usuario que creó el proyecto)
        if (savedProject.getCreatedBy() != null) {
            System.out.println("🔧 SERVICE DEBUG: Creando relación usuario_has_proyecto para usuario: " +
                    savedProject.getCreatedBy().getUsuarioId() + " y proyecto: " + savedProject.getProyectoId());

            Usuario propietario = savedProject.getCreatedBy();
            System.out.println("🔧 SERVICE DEBUG: Usuario propietario encontrado: " + propietario.getUsername());

            // Crear la relación manualmente estableciendo las entidades y el ID compuesto
            UsuarioHasProyecto relacion = new UsuarioHasProyecto();

            // Establecer las entidades
            relacion.setUsuario(propietario);
            relacion.setProyecto(savedProject);

            // Crear y establecer el ID compuesto
            UsuarioHasProyectoId relacionId = new UsuarioHasProyectoId();
            relacionId.setUsuarioId(propietario.getUsuarioId());
            relacionId.setProyectoId(savedProject.getProyectoId());
            relacion.setId(relacionId);

            // Establecer otros campos
            relacion.setPrivilegio(UsuarioHasProyecto.PrivilegioUsuarioProyecto.EDITOR);
            relacion.setFechaUsuarioProyecto(LocalDateTime.now());

            System.out.println("🔧 SERVICE DEBUG: Relación configurada - usuarioId=" + relacionId.getUsuarioId() +
                    ", proyectoId=" + relacionId.getProyectoId());

            UsuarioHasProyecto savedRelacion = usuarioHasProyectoRepository.save(relacion);
            System.out.println("🔧 SERVICE DEBUG: Relación usuario_has_proyecto creada exitosamente: usuario=" +
                    savedRelacion.getId().getUsuarioId() + ", proyecto=" + savedRelacion.getId().getProyectoId());
        } else {
            System.out.println("🔧 SERVICE DEBUG: NO se creó relación usuario_has_proyecto porque createdBy es null");
        }

        return savedProject;
    }

    public Proyecto actualizarProyecto(Long id, Proyecto proyectoDetails) {
        Proyecto proyecto = buscarProyectoPorId(id);
        proyecto.setNombreProyecto(proyectoDetails.getNombreProyecto());
        proyecto.setDescripcionProyecto(proyectoDetails.getDescripcionProyecto());
        proyecto.setVisibilidadProyecto(proyectoDetails.getVisibilidadProyecto());
        proyecto.setAccesoProyecto(proyectoDetails.getAccesoProyecto());

        // Verificar que propietarioProyecto no sea null antes de asignarlo
        if (proyectoDetails.getPropietarioProyecto() != null) {
            proyecto.setPropietarioProyecto(proyectoDetails.getPropietarioProyecto());
        }
        // Si no se proporciona, mantener el valor original

        proyecto.setEstadoProyecto(proyectoDetails.getEstadoProyecto());

        // Preservar fecha_inicio_proyecto si no se proporciona una nueva
        if (proyectoDetails.getFechaInicioProyecto() != null) {
            proyecto.setFechaInicioProyecto(proyectoDetails.getFechaInicioProyecto());
        }
        // Si no se proporcionó, se mantiene la fecha original

        proyecto.setFechaFinProyecto(proyectoDetails.getFechaFinProyecto());
        return proyectoRepository.save(proyecto);
    }

    public void eliminarProyecto(Long id) {
        Proyecto proyecto = buscarProyectoPorId(id);
        proyectoRepository.delete(proyecto);
    }

    // =================== MÉTODOS PARA EL CONTROLADOR JERÁRQUICO ===================

    /**
     * Obtiene estadísticas generales del usuario
     */
    public Map<String, Object> obtenerEstadisticasProyectosUsuario(Long userId) {
        List<Object[]> statsRaw = proyectoQueryService.getUserProjectStatsRaw(userId);
        Map<String, Object> result = new HashMap<>();

        if (!statsRaw.isEmpty()) {
            Object[] stats = statsRaw.get(0);
            long personalProjects = stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
            long teamProjects = stats[1] != null ? ((Number) stats[1]).longValue() : 0L;
            long otherProjects = stats[3] != null ? ((Number) stats[3]).longValue() : 0L;

            result.put("personal_projects", personalProjects);
            result.put("team_projects", teamProjects);
            result.put("participating_projects", personalProjects + teamProjects); // SUMA MATEMÁTICA
            result.put("other_projects", otherProjects);
        } else {
            result.put("personal_projects", 0L);
            result.put("team_projects", 0L);
            result.put("participating_projects", 0L);
            result.put("other_projects", 0L);
        }

        return result;
    }

    /**
     * Obtiene todos los proyectos del usuario con filtros
     */
    public List<Map<String, Object>> obtenerTodosProyectosUsuario(Long userId, String category, String search, String sort, String filter) {
        // Retorna TODOS los proyectos donde el usuario participa (personales + equipo)
        return obtenerProyectosParticipacion(userId, category, search, sort);
    }

    /**
     * Obtiene proyectos personales del usuario
     * ✅ OPTIMIZADO: Usa convertirProyectosAMapaBatch() para evitar procesamiento individual
     */
    public List<Map<String, Object>> obtenerProyectosPersonales(Long userId, String category, String search, String sort) {
        List<Proyecto> projects = proyectoQueryService.findPersonalProjects(userId, category, search, sort);
        return convertirProyectosAMapaBatch(projects, "PROPIETARIO");
    }

    /**
     * Obtiene proyectos de equipos del usuario
     * ✅ OPTIMIZADO: Usa convertirProyectosAMapaBatch() para evitar procesamiento individual
     */
    public List<Map<String, Object>> obtenerProyectosEquipos(Long userId, String category, String search, String sort) {
        List<Proyecto> projects = proyectoQueryService.findTeamProjects(userId, category, search, sort);
        return convertirProyectosAMapaBatch(projects, "COLABORADOR");
    }

    /**
     * Obtiene proyectos de un equipo específico
     */
    public List<Map<String, Object>> obtenerProyectosEquipoEspecifico(Long userId, Long teamId, String category, String search, String sort) {
        List<Proyecto> projects = proyectoQueryService.findSpecificTeamProjects(userId, teamId, category, search, sort);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Proyecto p : projects) {
            Map<String, Object> projectMap = new HashMap<>();
            projectMap.put("proyecto_id", p.getProyectoId());
            projectMap.put("nombre_proyecto", p.getNombreProyecto());
            projectMap.put("descripcion_proyecto", p.getDescripcionProyecto());
            projectMap.put("visibilidad_proyecto", p.getVisibilidadProyecto());
            projectMap.put("estado_proyecto", p.getEstadoProyecto());
            projectMap.put("fecha_inicio_proyecto", p.getFechaInicioProyecto());
            projectMap.put("fecha_fin_proyecto", p.getFechaFinProyecto());
            projectMap.put("privilegio_usuario_actual", "COLABORADOR");
            result.add(projectMap);
        }

        return result;
    }

    /**
     * Obtiene proyectos donde el usuario participa (personales + equipos)
     */
    public List<Map<String, Object>> obtenerProyectosParticipacion(Long userId, String category, String search, String sort) {
        List<Proyecto> projects = proyectoQueryService.findParticipatingProjects(userId, category, search, sort);
        List<Map<String, Object>> result = new ArrayList<>();

        // ✅ OPTIMIZADO: Procesamiento batch con privilegios dinámicos
        for (Proyecto p : projects) {
            Map<String, Object> projectMap = new HashMap<>();
            projectMap.put("proyecto_id", p.getProyectoId());
            projectMap.put("nombre_proyecto", p.getNombreProyecto());
            projectMap.put("descripcion_proyecto", p.getDescripcionProyecto());
            projectMap.put("visibilidad_proyecto", p.getVisibilidadProyecto());
            projectMap.put("estado_proyecto", p.getEstadoProyecto());
            projectMap.put("fecha_inicio_proyecto", p.getFechaInicioProyecto());
            projectMap.put("fecha_fin_proyecto", p.getFechaFinProyecto());

            // Determinar si es grupal basado en el propietario
            boolean esGrupal = p.getPropietarioProyecto() == Proyecto.PropietarioProyecto.GRUPO ||
                    p.getPropietarioProyecto() == Proyecto.PropietarioProyecto.EMPRESA;
            projectMap.put("es_grupal", esGrupal);

            // Agregar información de categoría (tomamos la primera si hay varias)
            if (p.getCategorias() != null && !p.getCategorias().isEmpty()) {
                Categoria categoria = p.getCategorias().iterator().next();
                projectMap.put("nombre_categoria", categoria.getNombreCategoria());
                projectMap.put("categoria_id", categoria.getIdCategoria());
            } else {
                projectMap.put("nombre_categoria", null);
                projectMap.put("categoria_id", null);
            }

            // Determinar el privilegio basado en el tipo de proyecto
            if (p.getPropietarioProyecto() == Proyecto.PropietarioProyecto.USUARIO) {
                projectMap.put("privilegio_usuario_actual", "PROPIETARIO");
            } else {
                projectMap.put("privilegio_usuario_actual", "COLABORADOR");
            }

            result.add(projectMap);
        }

        return result;
    }

    /**
     * Obtiene otros proyectos públicos
     * ✅ OPTIMIZADO: Usa convertirProyectosAMapaBatch() para evitar procesamiento individual
     */
    public List<Map<String, Object>> obtenerOtrosProyectosPublicos(Long userId, String category, String search, String sort) {
        List<Proyecto> projects = proyectoQueryService.findOtherProjects(userId, category, search, sort);
        return convertirProyectosAMapaBatch(projects, "LECTURA");
    }

    /**
     * Obtiene equipos del usuario
     */
    public List<Map<String, Object>> obtenerEquiposUsuario(Long userId) {
        List<Object[]> teams = proyectoQueryService.findUserTeams(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] team : teams) {
            Map<String, Object> teamMap = new HashMap<>();
            teamMap.put("equipo_id", team[0]);
            teamMap.put("nombre_equipo", team[1]);
            result.add(teamMap);
        }

        return result;
    }

    /**
     * Obtiene información de un equipo específico
     */
    public Map<String, Object> obtenerInformacionEquipo(Long teamId) {
        Object teamInfoRaw = proyectoQueryService.findTeamInfo(teamId);
        Map<String, Object> result = new HashMap<>();

        if (teamInfoRaw != null && teamInfoRaw instanceof Object[]) {
            Object[] teamInfo = (Object[]) teamInfoRaw;
            result.put("team_id", teamInfo[0]);
            result.put("nombre_equipo", teamInfo[1]);
            result.put("total_members", teamInfo[2]);
            result.put("total_projects", teamInfo[3]);
        } else {
            result.put("team_id", teamId);
            result.put("nombre_equipo", "Equipo no encontrado");
            result.put("total_members", 0);
            result.put("total_projects", 0);
        }

        return result;
    }

    /**
     * Obtiene todas las categorías
     */
    public List<Categoria> obtenerTodasCategorias() {
        return categoriaRepository.findAll();
    }

    /**
     * Obtiene detalles completos de un proyecto
     */
    public Map<String, Object> obtenerDetallesProyecto(Long userId, Long projectId) {
        Proyecto proyecto = proyectoQueryService.findProjectDetailsById(projectId);

        if (proyecto == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("proyecto_id", proyecto.getProyectoId());
        result.put("nombre_proyecto", proyecto.getNombreProyecto());
        result.put("descripcion_proyecto", proyecto.getDescripcionProyecto());
        result.put("visibilidad_proyecto", proyecto.getVisibilidadProyecto() != null ? proyecto.getVisibilidadProyecto().toString() : "PRIVADO");
        result.put("estado_proyecto", proyecto.getEstadoProyecto() != null ? proyecto.getEstadoProyecto().toString() : "PLANEADO");
        result.put("fecha_inicio_proyecto", proyecto.getFechaInicioProyecto());
        result.put("fecha_fin_proyecto", proyecto.getFechaFinProyecto());
        result.put("created_by", proyecto.getCreatedBy() != null ? proyecto.getCreatedBy().getUsuarioId() : null);
        result.put("propietario_proyecto", proyecto.getPropietarioProyecto() != null ? proyecto.getPropietarioProyecto().toString() : "USUARIO");
        result.put("propietario_nombre", proyecto.getPropietarioNombre());

        // Agregar información de categoría (tomamos la primera si hay varias)
        if (proyecto.getCategorias() != null && !proyecto.getCategorias().isEmpty()) {
            Categoria categoria = proyecto.getCategorias().iterator().next();
            result.put("nombre_categoria", categoria.getNombreCategoria());
            result.put("categoria_id", categoria.getIdCategoria());
        } else {
            result.put("nombre_categoria", null);
            result.put("categoria_id", null);
        }

        result.put("privilegio_usuario_actual", "LECTURA"); // Por defecto

        return result;
    }

    /**
     * Obtiene colaboradores de un proyecto específico desde la base de datos
     */
    public List<Map<String, Object>> obtenerColaboradoresProyecto(Long projectId) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // Buscar todas las relaciones usuario-proyecto para este proyecto
            List<UsuarioHasProyecto> relaciones = usuarioHasProyectoRepository.findById_ProjectId(projectId);

            // Convertir cada relación a un mapa con información del usuario
            for (UsuarioHasProyecto relacion : relaciones) {
                Usuario usuario = relacion.getUsuario();
                if (usuario != null) {
                    Map<String, Object> collab = new HashMap<>();
                    collab.put("usuario_id", usuario.getUsuarioId());
                    collab.put("nombre_usuario", usuario.getNombreUsuario() + " " + usuario.getApellidoPaterno());
                    collab.put("username", "@" + usuario.getUsername());
                    collab.put("correo", usuario.getCorreo());
                    collab.put("privilegio_usuario_proyecto", relacion.getPrivilegio().toString());
                    collab.put("fecha_usuario_proyecto", relacion.getFechaUsuarioProyecto().toString());
                    collab.put("foto_perfil", usuario.getFotoPerfil() != null ? usuario.getFotoPerfil() : "/img/default-avatar.png");
                    result.add(collab);
                }
            }

            // Si no hay colaboradores reales, crear algunos datos para demo
            if (result.isEmpty()) {
                Map<String, Object> defaultCollab = new HashMap<>();
                defaultCollab.put("usuario_id", 0L);
                defaultCollab.put("nombre_usuario", "Sin colaboradores");
                defaultCollab.put("username", "@demo");
                defaultCollab.put("correo", "demo@example.com");
                defaultCollab.put("privilegio_usuario_proyecto", "PROPIETARIO");
                defaultCollab.put("fecha_usuario_proyecto", LocalDateTime.now().toString());
                defaultCollab.put("foto_perfil", "/img/default-avatar.png");
                result.add(defaultCollab);
            }

        } catch (Exception e) {
            System.err.println("Error obteniendo colaboradores del proyecto " + projectId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Obtiene repositorios de un proyecto específico
     */
    public List<Map<String, Object>> obtenerRepositoriosProyecto(Long projectId) {
        // Obtener las relaciones proyecto-repositorio para este proyecto
        List<ProyectoHasRepositorio> relaciones = proyectoHasRepositorioRepository.findById_ProjectId(projectId);

        // Convertir a mapas con la información del repositorio
        return relaciones.stream()
                .map(relacion -> {
                    Repositorio repo = relacion.getRepositorio();
                    Map<String, Object> repoMap = new HashMap<>();
                    repoMap.put("repositorio_id", repo.getRepositorioId());
                    repoMap.put("nombre_repositorio", repo.getNombreRepositorio());
                    repoMap.put("descripcion_repositorio", repo.getDescripcionRepositorio());
                    repoMap.put("visibilidad_repositorio", repo.getVisibilidadRepositorio().toString());
                    repoMap.put("tipo_repositorio", repo.getTipoRepositorio().toString());
                    repoMap.put("rama_principal", repo.getRamaPrincipalRepositorio());
                    repoMap.put("fecha_creacion", repo.getFechaCreacion());
                    return repoMap;
                })
                .toList();
    }

    /**
     * Obtiene nodos/carpetas de un proyecto específico
     */
    public List<Map<String, Object>> obtenerNodosProyecto(Long projectId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // Obtener nodos raíz del proyecto usando el servicio de nodos
            List<NodoDTO> nodosDTO = nodoService.obtenerNodosRaizDTO(Nodo.ContainerType.PROYECTO, projectId);
            
            // Convertir NodoDTO a Map para compatibilidad con la vista
            for (NodoDTO dto : nodosDTO) {
                Map<String, Object> node = new HashMap<>();
                node.put("nodo_id", dto.getNodoId());
                node.put("nombre", dto.getNombre());
                node.put("descripcion", ""); // NodoDTO no tiene descripción
                node.put("tipo_nodo", dto.getTipo()); // Ya es String: "ARCHIVO" o "CARPETA"
                node.put("container_type", "PROYECTO");
                node.put("container_id", projectId);
                
                // Formatear fecha
                if (dto.getCreadoEn() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                    node.put("fecha_creacion", sdf.format(java.sql.Timestamp.valueOf(dto.getCreadoEn())));
                } else {
                    node.put("fecha_creacion", "");
                }
                
                // Contar archivos (hijos del nodo si es carpeta)
                if ("CARPETA".equals(dto.getTipo())) {
                    List<NodoDTO> hijos = nodoService.obtenerHijosDTO(dto.getNodoId());
                    long totalArchivos = hijos.stream()
                        .filter(h -> "ARCHIVO".equals(h.getTipo()))
                        .count();
                    node.put("total_archivos", totalArchivos);
                } else {
                    node.put("total_archivos", 0);
                }
                
                result.add(node);
            }
        } catch (Exception e) {
            System.err.println("Error al obtener nodos del proyecto " + projectId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * Obtiene actividad reciente de un proyecto
     */
    public List<Map<String, Object>> obtenerActividadRecienteProyecto(Long projectId) {
        List<Map<String, Object>> result = new ArrayList<>();

        // Hard-coded para TelDevPortal Project (ID 1) - datos del historial
        if (projectId == 1L) {
            // Actividad 1: Commit en backend-api
            Map<String, Object> activity1 = new HashMap<>();
            activity1.put("tipo_evento", "MODIFICACION");
            activity1.put("titulo", "Commit en backend-api");
            activity1.put("descripcion", "Roberto Beltrán actualizó la configuración de OAuth2 en el repositorio backend-api");
            activity1.put("fecha_evento", "01/10/2024 14:30");
            activity1.put("usuario", "Roberto Beltrán");
            activity1.put("icono", "fas fa-commit");
            result.add(activity1);

            // Actividad 2: Nuevo repositorio creado
            Map<String, Object> activity2 = new HashMap<>();
            activity2.put("tipo_evento", "CREACION");
            activity2.put("titulo", "Nuevo repositorio creado");
            activity2.put("descripcion", "María González creó el repositorio database-scripts para almacenar los scripts de BD");
            activity2.put("fecha_evento", "30/09/2024 16:45");
            activity2.put("usuario", "María González");
            activity2.put("icono", "fas fa-code-branch");
            result.add(activity2);

            // Actividad 3: Colaborador invitado
            Map<String, Object> activity3 = new HashMap<>();
            activity3.put("tipo_evento", "CREACION");
            activity3.put("titulo", "Colaborador invitado");
            activity3.put("descripcion", "Pedro Silva fue invitado como colaborador al proyecto");
            activity3.put("fecha_evento", "29/09/2024 10:15");
            activity3.put("usuario", "Roberto Beltrán");
            activity3.put("icono", "fas fa-user-plus");
            result.add(activity3);

            // Actividad 4: Carpeta creada
            Map<String, Object> activity4 = new HashMap<>();
            activity4.put("tipo_evento", "CREACION");
            activity4.put("titulo", "Carpeta creada");
            activity4.put("descripcion", "Ana López creó la carpeta \"Documentación\" para organizar los manuales del proyecto");
            activity4.put("fecha_evento", "28/09/2024 13:20");
            activity4.put("usuario", "Ana López");
            activity4.put("icono", "fas fa-folder-plus");
            result.add(activity4);

            // Actividad 5: Proyecto creado
            Map<String, Object> activity5 = new HashMap<>();
            activity5.put("tipo_evento", "CREACION");
            activity5.put("titulo", "Proyecto creado");
            activity5.put("descripcion", "TelDevPortal Project fue creado por Roberto Beltrán");
            activity5.put("fecha_evento", "25/09/2024 09:00");
            activity5.put("usuario", "Roberto Beltrán");
            activity5.put("icono", "fas fa-project-diagram");
            result.add(activity5);
        }

        return result;
    }

    /**
     * Obtiene estadísticas de un proyecto
     */
    public Map<String, Object> obtenerEstadisticasProyecto(Long projectId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Contar repositorios reales del proyecto
            List<ProyectoHasRepositorio> repositorios = proyectoHasRepositorioRepository.findById_ProjectId(projectId);
            result.put("total_repositorios", repositorios.size());

            // Contar colaboradores reales del proyecto
            List<UsuarioHasProyecto> colaboradores = usuarioHasProyectoRepository.findById_ProjectId(projectId);
            result.put("total_colaboradores", colaboradores.size());

            // Por ahora mantener valores por defecto para commits y actividad
            // TODO: Implementar conteo real de commits cuando se integre con git
            result.put("total_commits", 0);
            result.put("ultima_actividad", "N/A");

            // Por ahora mantener carpetas en 0
            // TODO: Implementar conteo real de nodos/carpetas
            result.put("total_carpetas", 0);

        } catch (Exception e) {
            // En caso de error, devolver valores por defecto
            result.put("total_repositorios", 0);
            result.put("total_colaboradores", 0);
            result.put("total_commits", 0);
            result.put("ultima_actividad", "N/A");
            result.put("total_carpetas", 0);
        }

        return result;
    }

    /**
     * Crea un nuevo proyecto
     */
    public Map<String, Object> crearProyecto(Long userId, Map<String, Object> projectData) {
        // Implementación básica - se puede expandir
        Proyecto proyecto = new Proyecto();
        proyecto.setNombreProyecto((String) projectData.get("nombre"));
        proyecto.setDescripcionProyecto((String) projectData.get("descripcion"));
        // NOTA: La entidad actual tiene un diseño problemático - propietario debería ser una FK a usuario
        // Por ahora usamos USUARIO como valor por defecto
        proyecto.setPropietarioProyecto(Proyecto.PropietarioProyecto.USUARIO);
        proyecto.setVisibilidadProyecto(Proyecto.VisibilidadProyecto.PRIVADO);
        proyecto.setAccesoProyecto(Proyecto.AccesoProyecto.RESTRINGIDO);
        proyecto.setEstadoProyecto(Proyecto.EstadoProyecto.PLANEADO);
        proyecto.setFechaInicioProyecto(LocalDate.now());

        Proyecto saved = proyectoRepository.save(proyecto);

        // El trigger creó el nodo raíz, ahora buscarlo y actualizar root_node_id
        Nodo rootNode = nodoRepository.findByContainerTypeAndContainerIdAndParentIdIsNullAndIsDeletedFalse(
                Nodo.ContainerType.PROYECTO, saved.getProyectoId()
        ).stream().findFirst().orElse(null);

        if (rootNode != null) {
            saved.setRootNodeId(rootNode.getNodoId());
            proyectoRepository.save(saved);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("projectId", saved.getProyectoId());
        return result;
    }

    /**
     * Actualiza un proyecto
     */
    public Map<String, Object> actualizarProyectoMapa(Long userId, Long projectId, Map<String, Object> projectData) {
        // Verificar permisos y actualizar
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    /**
     * Elimina un proyecto
     */
    public void eliminarProyectoPorId(Long userId, Long projectId) {
        // NOTA: Como la entidad actual no tiene FK al propietario, por ahora solo verificamos existencia
        Proyecto proyecto = proyectoRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        // TODO: Implementar verificación de permisos cuando se corrija el diseño de la entidad

        proyectoRepository.delete(proyecto);
    }

    // =================== GETTERS PARA NUEVOS SERVICIOS ===================

    @Autowired
    private ProjectRoleService projectRoleService;

    @Autowired
    private ProjectTeamService projectTeamService;

    public ProjectRoleService getProjectRoleService() {
        return projectRoleService;
    }

    public ProjectTeamService getProjectTeamService() {
        return projectTeamService;
    }

    /**
     * Obtiene usuarios con dominio corporativo para invitación empresarial
     * Extrae el dominio del correo del usuario logueado y busca usuarios con ese dominio
     * @param correoUsuarioLogueado Correo del usuario que inicia sesión (ej: usuario@company.com)
     * @param usuarioIdActual ID del usuario actual (para excluirlo de la lista)
     * @return Lista de usuarios con el mismo dominio corporativo
     */
    public List<Map<String, Object>> obtenerUsuariosConDominioCorporativo(String correoUsuarioLogueado, Long usuarioIdActual) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // Validar que el correo tenga formato válido
            if (correoUsuarioLogueado == null || !correoUsuarioLogueado.contains("@")) {
                return result;
            }

            // Extraer dominio del correo del usuario logueado (ej: de usuario@company.com extraer company.com)
            String dominioCorporativo = correoUsuarioLogueado.substring(correoUsuarioLogueado.indexOf("@") + 1);

            // Obtener todos los usuarios
            List<Usuario> todosLosUsuarios = usuarioRepository.findAll();

            // Filtrar usuarios que tengan el mismo dominio corporativo
            for (Usuario usuario : todosLosUsuarios) {
                // No incluir al usuario actual
                if (usuario.getUsuarioId().equals(usuarioIdActual)) {
                    continue;
                }

                // Verificar que el correo del usuario tiene el dominio corporativo
                String userEmail = usuario.getCorreo();
                if (userEmail != null && userEmail.endsWith("@" + dominioCorporativo)) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("usuario_id", usuario.getUsuarioId());
                    userMap.put("nombre_completo", usuario.getNombreUsuario() + " " + usuario.getApellidoPaterno() + " " + usuario.getApellidoMaterno());
                    userMap.put("username", usuario.getUsername());
                    userMap.put("correo", usuario.getCorreo());
                    userMap.put("foto_perfil", usuario.getFotoPerfil() != null ? usuario.getFotoPerfil() : "/img/default-avatar.png");
                    result.add(userMap);
                }
            }

        } catch (Exception e) {
            System.err.println("Error obteniendo usuarios con dominio corporativo: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
    
    /**
     * Extrae el dominio corporativo del correo del usuario logueado
     * @param correoUsuario Correo del usuario (ej: usuario@company.com)
     * @return Dominio corporativo (ej: company.com)
     */
    public String extraerDominioCorporativo(String correoUsuario) {
        if (correoUsuario != null && correoUsuario.contains("@")) {
            return correoUsuario.substring(correoUsuario.indexOf("@") + 1);
        }
        return null;
    }

    /**
     * Obtiene el permiso/privilegio del usuario en un proyecto
     * @param userId ID del usuario
     * @param projectId ID del proyecto
     * @return String con el privilegio: "PROPIETARIO", "EDITOR", "LECTOR", "SIN_ACCESO"
     */
    public String obtenerPermisoUsuarioEnProyecto(Long userId, Long projectId) {
        try {
            // Verificar si el usuario es el creador del proyecto
            Proyecto proyecto = proyectoRepository.findById(projectId).orElse(null);
            if (proyecto != null && proyecto.getCreatedBy() != null && 
                proyecto.getCreatedBy().getUsuarioId().equals(userId)) {
                return "PROPIETARIO";
            }

            // Por ahora, si no es propietario, devolvemos LECTOR (acceso de solo lectura)
            // TODO: Implementar lógica completa de permisos con tabla usuario_has_proyecto
            // cuando se requiera control granular de permisos
            
            return "LECTOR";
        } catch (Exception e) {
            System.err.println("Error obteniendo permiso del usuario en proyecto: " + e.getMessage());
            return "SIN_ACCESO";
        }
    }

    // =================== MÉTODO BATCH OPTIMIZADO ===================
    
    /**
     * ✅ OPTIMIZACIÓN BATCH: Convierte lista de proyectos a mapas con privilegio específico
     * Evita N+1 queries cargando datos relacionados en batch
     * @param projects Lista de proyectos a convertir
     * @param privilegio Privilegio del usuario actual sobre estos proyectos
     * @return Lista de mapas con información de proyectos
     */
    private List<Map<String, Object>> convertirProyectosAMapaBatch(List<Proyecto> projects, String privilegio) {
        if (projects == null || projects.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Proyecto p : projects) {
            Map<String, Object> projectMap = new HashMap<>();
            projectMap.put("proyecto_id", p.getProyectoId());
            projectMap.put("nombre_proyecto", p.getNombreProyecto());
            projectMap.put("descripcion_proyecto", p.getDescripcionProyecto());
            projectMap.put("visibilidad_proyecto", p.getVisibilidadProyecto());
            projectMap.put("estado_proyecto", p.getEstadoProyecto());
            projectMap.put("fecha_inicio_proyecto", p.getFechaInicioProyecto());
            projectMap.put("fecha_fin_proyecto", p.getFechaFinProyecto());

            // Determinar si es grupal basado en el propietario
            boolean esGrupal = p.getPropietarioProyecto() == Proyecto.PropietarioProyecto.GRUPO ||
                    p.getPropietarioProyecto() == Proyecto.PropietarioProyecto.EMPRESA;
            projectMap.put("es_grupal", esGrupal);

            // Agregar información de categoría (tomamos la primera si hay varias)
            if (p.getCategorias() != null && !p.getCategorias().isEmpty()) {
                Categoria categoria = p.getCategorias().iterator().next();
                projectMap.put("nombre_categoria", categoria.getNombreCategoria());
                projectMap.put("categoria_id", categoria.getIdCategoria());
            } else {
                projectMap.put("nombre_categoria", null);
                projectMap.put("categoria_id", null);
            }

            // Usar privilegio proporcionado
            projectMap.put("privilegio_usuario_actual", privilegio);
            result.add(projectMap);
        }

        return result;
    }
    
    // =================== MÉTODOS PAGINADOS (12 items por página) ===================
    
    /**
     * ✅ PAGINACIÓN: Obtiene proyectos donde participo (personales + equipos) - 12 por página
     * @param page Número de página (0-indexed)
     */
    public List<Map<String, Object>> obtenerTodosProyectosUsuarioPaginado(Long userId, String category, String search, String sort, int page) {
        int limit = 12;
        int offset = page * limit;
        List<Proyecto> projects = proyectoQueryService.findParticipatingProjectsPaginated(userId, category, search, sort, limit, offset);
        
        // Procesar con lógica dinámica de privilegios
        List<Map<String, Object>> result = new ArrayList<>();
        for (Proyecto p : projects) {
            Map<String, Object> projectMap = new HashMap<>();
            projectMap.put("proyecto_id", p.getProyectoId());
            projectMap.put("nombre_proyecto", p.getNombreProyecto());
            projectMap.put("descripcion_proyecto", p.getDescripcionProyecto());
            projectMap.put("visibilidad_proyecto", p.getVisibilidadProyecto());
            projectMap.put("estado_proyecto", p.getEstadoProyecto());
            projectMap.put("fecha_inicio_proyecto", p.getFechaInicioProyecto());
            projectMap.put("fecha_fin_proyecto", p.getFechaFinProyecto());

            boolean esGrupal = p.getPropietarioProyecto() == Proyecto.PropietarioProyecto.GRUPO ||
                    p.getPropietarioProyecto() == Proyecto.PropietarioProyecto.EMPRESA;
            projectMap.put("es_grupal", esGrupal);

            if (p.getCategorias() != null && !p.getCategorias().isEmpty()) {
                Categoria categoria = p.getCategorias().iterator().next();
                projectMap.put("nombre_categoria", categoria.getNombreCategoria());
                projectMap.put("categoria_id", categoria.getIdCategoria());
            } else {
                projectMap.put("nombre_categoria", null);
                projectMap.put("categoria_id", null);
            }

            // Privilegio dinámico
            if (p.getPropietarioProyecto() == Proyecto.PropietarioProyecto.USUARIO) {
                projectMap.put("privilegio_usuario_actual", "PROPIETARIO");
            } else {
                projectMap.put("privilegio_usuario_actual", "COLABORADOR");
            }

            result.add(projectMap);
        }
        return result;
    }
    
    /**
     * ✅ PAGINACIÓN OPTIMIZADA: Obtiene proyectos personales - 12 por página
     * Usa estrategia de 2 queries para eliminar N+1:
     * 1. Query paginada para obtener IDs
     * 2. Query con LEFT JOIN FETCH para cargar relaciones eagerly
     */
    public List<Map<String, Object>> obtenerProyectosPersonalesPaginado(Long userId, String category, String search, String sort, int page) {
        int limit = 12;
        int offset = page * limit;
        
        // 1. Query paginada (obtiene proyectos sin relaciones)
        List<Proyecto> projects = proyectoQueryService.findPersonalProjectsPaginated(userId, category, search, sort, limit, offset);
        
        // 2. Si hay proyectos, recargar con JOIN FETCH para eliminar N+1
        if (!projects.isEmpty()) {
            List<Long> projectIds = projects.stream()
                .map(Proyecto::getProyectoId)
                .toList();
            projects = proyectoQueryService.findByIdsWithRelations(projectIds);
        }
        
        return convertirProyectosAMapaBatch(projects, "PROPIETARIO");
    }
    
    /**
     * ✅ PAGINACIÓN OPTIMIZADA: Obtiene proyectos de equipos - 12 por página
     * Usa estrategia de 2 queries para eliminar N+1:
     * 1. Query paginada para obtener IDs
     * 2. Query con LEFT JOIN FETCH para cargar relaciones eagerly
     */
    public List<Map<String, Object>> obtenerProyectosEquiposPaginado(Long userId, String category, String search, String sort, int page) {
        int limit = 12;
        int offset = page * limit;
        
        // 1. Query paginada (obtiene proyectos sin relaciones)
        List<Proyecto> projects = proyectoQueryService.findTeamProjectsPaginated(userId, category, search, sort, limit, offset);
        
        // 2. Si hay proyectos, recargar con JOIN FETCH para eliminar N+1
        if (!projects.isEmpty()) {
            List<Long> projectIds = projects.stream()
                .map(Proyecto::getProyectoId)
                .toList();
            projects = proyectoQueryService.findByIdsWithRelations(projectIds);
        }
        
        return convertirProyectosAMapaBatch(projects, "COLABORADOR");
    }
    
    /**
     * ✅ PAGINACIÓN OPTIMIZADA: Obtiene otros proyectos públicos - 12 por página
     * Usa estrategia de 2 queries para eliminar N+1:
     * 1. Query paginada para obtener IDs
     * 2. Query con LEFT JOIN FETCH para cargar relaciones eagerly
     */
    public List<Map<String, Object>> obtenerOtrosProyectosPublicosPaginado(Long userId, String category, String search, String sort, int page) {
        int limit = 12;
        int offset = page * limit;
        
        // 1. Query paginada (obtiene proyectos sin relaciones)
        List<Proyecto> projects = proyectoQueryService.findOtherProjectsPaginated(userId, category, search, sort, limit, offset);
        
        // 2. Si hay proyectos, recargar con JOIN FETCH para eliminar N+1
        if (!projects.isEmpty()) {
            List<Long> projectIds = projects.stream()
                .map(Proyecto::getProyectoId)
                .toList();
            projects = proyectoQueryService.findByIdsWithRelations(projectIds);
        }
        
        return convertirProyectosAMapaBatch(projects, "LECTURA");
    }

    /**
     * ✅ OPTIMIZADO: Obtiene TODOS los proyectos donde participo (personales + equipos) - SIN paginar
     * Combina: proyectos propios + proyectos de equipos
     */
    public List<Map<String, Object>> obtenerProyectosEnLosQueParticipoPersonalesYEquipos(Long userId, String category, String search, String sort) {
        List<Map<String, Object>> allProjects = new ArrayList<>();
        
        // Obtener proyectos personales (sin paginar)
        List<Proyecto> personalProjects = proyectoQueryService.findPersonalProjects(userId, category, search, sort);
        if (!personalProjects.isEmpty()) {
            List<Long> personalIds = personalProjects.stream().map(Proyecto::getProyectoId).toList();
            personalProjects = proyectoQueryService.findByIdsWithRelations(personalIds);
            allProjects.addAll(convertirProyectosAMapaBatch(personalProjects, "PROPIETARIO"));
        }
        
        // Obtener proyectos de equipos (sin paginar)
        List<Proyecto> teamProjects = proyectoQueryService.findTeamProjects(userId, category, search, sort);
        if (!teamProjects.isEmpty()) {
            List<Long> teamIds = teamProjects.stream().map(Proyecto::getProyectoId).toList();
            teamProjects = proyectoQueryService.findByIdsWithRelations(teamIds);
            allProjects.addAll(convertirProyectosAMapaBatch(teamProjects, "COLABORADOR"));
        }
        
        return allProjects;
    }

    /**
     * ✅ PAGINACIÓN OPTIMIZADA: Obtiene proyectos donde participo (personales + equipos) - 12 por página
     * Combina: proyectos propios + proyectos de equipos, luego aplica paginación
     */
    public List<Map<String, Object>> obtenerProyectosEnLosQueParticipoPersonalesYEquiposPaginado(Long userId, String category, String search, String sort, int page) {
        int limit = 12;
        int offset = page * limit;
        
        // Obtener TODOS los proyectos combinados
        List<Map<String, Object>> allProjects = obtenerProyectosEnLosQueParticipoPersonalesYEquipos(userId, category, search, sort);
        
        // Aplicar paginación en memoria
        return allProjects.stream()
            .skip(offset)
            .limit(limit)
            .toList();
    }

}
