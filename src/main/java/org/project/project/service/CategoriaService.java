package org.project.project.service;

import org.project.project.model.entity.Categoria;
import org.project.project.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Obtiene todas las categorías
     */
    public List<Categoria> listarTodasLasCategorias() {
        return categoriaRepository.findAllByOrderByNombreCategoriaAsc();
    }

    /**
     * Busca una categoría por ID
     */
    public Optional<Categoria> buscarCategoriaPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    /**
     * Busca categorías por nombre
     */
    public List<Categoria> buscarCategoriasPorNombre(String nombre) {
        return categoriaRepository.findByNombreCategoriaContainingIgnoreCase(nombre);
    }

    /**
     * Guarda una nueva categoría
     */
    public Categoria guardarCategoria(Categoria categoria) {
        // Verificar que no exista una categoría con el mismo nombre
        if (categoriaRepository.existsByNombreCategoria(categoria.getNombreCategoria())) {
            throw new RuntimeException("Ya existe una categoría con el nombre: " + categoria.getNombreCategoria());
        }
        return categoriaRepository.save(categoria);
    }

    /**
     * Actualiza una categoría existente
     */
    public Categoria actualizarCategoria(Long id, Categoria categoriaActualizada) {
        return categoriaRepository.findById(id)
            .map(categoria -> {
                // Verificar que no exista otra categoría con el mismo nombre (excepto la actual)
                if (!categoria.getNombreCategoria().equals(categoriaActualizada.getNombreCategoria()) &&
                    categoriaRepository.existsByNombreCategoria(categoriaActualizada.getNombreCategoria())) {
                    throw new RuntimeException("Ya existe una categoría con el nombre: " + categoriaActualizada.getNombreCategoria());
                }
                
                categoria.setNombreCategoria(categoriaActualizada.getNombreCategoria());
                categoria.setDescripcionCategoria(categoriaActualizada.getDescripcionCategoria());
                categoria.setSeccionCategoria(categoriaActualizada.getSeccionCategoria());
                return categoriaRepository.save(categoria);
            })
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
    }

    /**
     * Elimina una categoría por ID con manejo de restricciones
     */
    public void eliminarCategoria(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada con ID: " + id);
        }
        
        try {
            // Verificar si la categoría tiene asociaciones
            boolean tieneAsociaciones = verificarAsociacionesCategoria(id);
            
            if (tieneAsociaciones) {
                // Eliminar primero las asociaciones
                eliminarAsociacionesCategoria(id);
            }
            
            categoriaRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("No se puede eliminar la categoría. Puede estar asociada a otras entidades: " + e.getMessage());
        }
    }
    
    /**
     * Verifica si una categoría tiene asociaciones con otras entidades
     */
    private boolean verificarAsociacionesCategoria(Long categoriaId) {
        try {
            // Verificar asociaciones con APIs
            Long apiCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM categoria_has_api WHERE categoria_id_categoria = ?", 
                Long.class, categoriaId);
            
            // Verificar asociaciones con proyectos
            Long proyectoCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM categoria_has_proyecto WHERE categoria_id_categoria = ?", 
                Long.class, categoriaId);
            
            // Verificar asociaciones con repositorios
            Long repositorioCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM categoria_has_repositorio WHERE categoria_id_categoria = ?", 
                Long.class, categoriaId);
            
            // Verificar asociaciones con notificaciones
            Long notificacionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM categoria_has_notificacion WHERE categoria_id_categoria = ?", 
                Long.class, categoriaId);
            
            return (apiCount > 0 || proyectoCount > 0 || repositorioCount > 0 || notificacionCount > 0);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Elimina todas las asociaciones de una categoría antes de eliminarla
     */
    private void eliminarAsociacionesCategoria(Long categoriaId) {
        try {
            // Eliminar asociaciones con APIs
            jdbcTemplate.update("DELETE FROM categoria_has_api WHERE categoria_id_categoria = ?", categoriaId);
            
            // Eliminar asociaciones con proyectos
            jdbcTemplate.update("DELETE FROM categoria_has_proyecto WHERE categoria_id_categoria = ?", categoriaId);
            
            // Eliminar asociaciones con repositorios
            jdbcTemplate.update("DELETE FROM categoria_has_repositorio WHERE categoria_id_categoria = ?", categoriaId);
            
            // Eliminar asociaciones con notificaciones
            jdbcTemplate.update("DELETE FROM categoria_has_notificacion WHERE categoria_id_categoria = ?", categoriaId);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar asociaciones de la categoría: " + e.getMessage());
        }
    }

    /**
     * Verifica si una categoría existe
     */
    public boolean existeCategoria(Long id) {
        return categoriaRepository.existsById(id);
    }

    /**
     * Cuenta el total de categorías
     */
    public long contarCategorias() {
        return categoriaRepository.count();
    }
    
    /**
     * Obtiene categorías por tipo de entidad basado en asociaciones existentes
     */
    public List<Categoria> findCategoriasByTipo(String tipo) {
        switch (tipo.toUpperCase()) {
            case "API":
                return jdbcTemplate.query(
                    "SELECT DISTINCT c.* FROM categoria c " +
                    "INNER JOIN categoria_has_api cha ON c.id_categoria = cha.categoria_id_categoria " +
                    "ORDER BY c.nombre_categoria",
                    (rs, rowNum) -> {
                        Categoria categoria = new Categoria();
                        categoria.setIdCategoria(rs.getLong("id_categoria"));
                        categoria.setNombreCategoria(rs.getString("nombre_categoria"));
                        categoria.setDescripcionCategoria(rs.getString("descripcion_categoria"));
                        try {
                            String seccion = rs.getString("seccion_categoria");
                            if (seccion != null) {
                                categoria.setSeccionCategoria(Categoria.SeccionCategoria.valueOf(seccion));
                            }
                        } catch (Exception e) {
                            // Si hay error al parsear la sección, la dejamos como null
                        }
                        return categoria;
                    }
                );
                
            case "PROYECTO":
                return jdbcTemplate.query(
                    "SELECT DISTINCT c.* FROM categoria c " +
                    "INNER JOIN categoria_has_proyecto chp ON c.id_categoria = chp.categoria_id_categoria " +
                    "ORDER BY c.nombre_categoria",
                    (rs, rowNum) -> {
                        Categoria categoria = new Categoria();
                        categoria.setIdCategoria(rs.getLong("id_categoria"));
                        categoria.setNombreCategoria(rs.getString("nombre_categoria"));
                        categoria.setDescripcionCategoria(rs.getString("descripcion_categoria"));
                        try {
                            String seccion = rs.getString("seccion_categoria");
                            if (seccion != null) {
                                categoria.setSeccionCategoria(Categoria.SeccionCategoria.valueOf(seccion));
                            }
                        } catch (Exception e) {
                            // Si hay error al parsear la sección, la dejamos como null
                        }
                        return categoria;
                    }
                );
                
            case "REPOSITORIO":
                return jdbcTemplate.query(
                    "SELECT DISTINCT c.* FROM categoria c " +
                    "INNER JOIN categoria_has_repositorio chr ON c.id_categoria = chr.categoria_id_categoria " +
                    "ORDER BY c.nombre_categoria",
                    (rs, rowNum) -> {
                        Categoria categoria = new Categoria();
                        categoria.setIdCategoria(rs.getLong("id_categoria"));
                        categoria.setNombreCategoria(rs.getString("nombre_categoria"));
                        categoria.setDescripcionCategoria(rs.getString("descripcion_categoria"));
                        try {
                            String seccion = rs.getString("seccion_categoria");
                            if (seccion != null) {
                                categoria.setSeccionCategoria(Categoria.SeccionCategoria.valueOf(seccion));
                            }
                        } catch (Exception e) {
                            // Si hay error al parsear la sección, la dejamos como null
                        }
                        return categoria;
                    }
                );
                
            case "NOTIFICACION":
                return jdbcTemplate.query(
                    "SELECT DISTINCT c.* FROM categoria c " +
                    "INNER JOIN categoria_has_notificacion chn ON c.id_categoria = chn.categoria_id_categoria " +
                    "ORDER BY c.nombre_categoria",
                    (rs, rowNum) -> {
                        Categoria categoria = new Categoria();
                        categoria.setIdCategoria(rs.getLong("id_categoria"));
                        categoria.setNombreCategoria(rs.getString("nombre_categoria"));
                        categoria.setDescripcionCategoria(rs.getString("descripcion_categoria"));
                        try {
                            String seccion = rs.getString("seccion_categoria");
                            if (seccion != null) {
                                categoria.setSeccionCategoria(Categoria.SeccionCategoria.valueOf(seccion));
                            }
                        } catch (Exception e) {
                            // Si hay error al parsear la sección, la dejamos como null
                        }
                        return categoria;
                    }
                );
                
            case "TODAS":
            default:
                return listarTodasLasCategorias();
        }
    }
    
    /**
     * Obtiene estadísticas por tipo de categoría
     */
    public Map<String, Object> getEstadisticasPorTipo(String tipo) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        switch (tipo.toUpperCase()) {
            case "API":
                estadisticas.put("totalCategorias", jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT c.id_categoria) FROM categoria c " +
                    "INNER JOIN categoria_has_api cha ON c.id_categoria = cha.categoria_id_categoria", 
                    Long.class));
                estadisticas.put("totalEntidades", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM api", Long.class));
                estadisticas.put("entidadesCategorizadas", jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT cha.api_api_id) FROM categoria_has_api cha", Long.class));
                break;
                
            case "PROYECTO":
                estadisticas.put("totalCategorias", jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT c.id_categoria) FROM categoria c " +
                    "INNER JOIN categoria_has_proyecto chp ON c.id_categoria = chp.categoria_id_categoria", 
                    Long.class));
                estadisticas.put("totalEntidades", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM proyecto", Long.class));
                estadisticas.put("entidadesCategorizadas", jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT chp.proyecto_proyecto_id) FROM categoria_has_proyecto chp", Long.class));
                break;
                
            case "REPOSITORIO":
                estadisticas.put("totalCategorias", jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT c.id_categoria) FROM categoria c " +
                    "INNER JOIN categoria_has_repositorio chr ON c.id_categoria = chr.categoria_id_categoria", 
                    Long.class));
                estadisticas.put("totalEntidades", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM repositorio", Long.class));
                estadisticas.put("entidadesCategorizadas", jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT chr.repositorio_repositorio_id) FROM categoria_has_repositorio chr", Long.class));
                break;
                
            case "NOTIFICACION":
                estadisticas.put("totalCategorias", jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT c.id_categoria) FROM categoria c " +
                    "INNER JOIN categoria_has_notificacion chn ON c.id_categoria = chn.categoria_id_categoria", 
                    Long.class));
                estadisticas.put("totalEntidades", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM notificacion", Long.class));
                estadisticas.put("entidadesCategorizadas", jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT chn.notificacion_notificacion_id) FROM categoria_has_notificacion chn", Long.class));
                break;
                
            case "TODAS":
            default:
                estadisticas.put("totalCategorias", contarCategorias());
                estadisticas.put("totalEntidades", 0L);
                estadisticas.put("entidadesCategorizadas", 0L);
                // Agregar estadísticas desglosadas por tipo
                estadisticas.put("categoriasAPI", jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT c.id_categoria) FROM categoria c " +
                    "INNER JOIN categoria_has_api cha ON c.id_categoria = cha.categoria_id_categoria", 
                    Long.class));
                estadisticas.put("categoriasProyecto", jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT c.id_categoria) FROM categoria c " +
                    "INNER JOIN categoria_has_proyecto chp ON c.id_categoria = chp.categoria_id_categoria", 
                    Long.class));
                estadisticas.put("categoriasRepositorio", jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT c.id_categoria) FROM categoria c " +
                    "INNER JOIN categoria_has_repositorio chr ON c.id_categoria = chr.categoria_id_categoria", 
                    Long.class));
                estadisticas.put("categoriasNotificacion", jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT c.id_categoria) FROM categoria c " +
                    "INNER JOIN categoria_has_notificacion chn ON c.id_categoria = chn.categoria_id_categoria", 
                    Long.class));
        }
        
        return estadisticas;
    }
    
    /**
     * Asocia una categoría con un tipo específico (creando registro en tabla de relación)
     */
    public void asociarCategoriaConTipo(Long categoriaId, String tipo) {
        // Este método está pensado para cuando se quiera crear una relación específica
        // con una entidad existente. Por ahora, solo creamos la categoría.
        // Las relaciones se crearán cuando se asignen las categorías a entidades específicas.
    }
    
    /**
     * Guarda una categoría y la asocia con un tipo si se especifica una entidad
     */
    public Categoria guardarCategoriaConTipo(Categoria categoria, String tipo, Long entidadId) {
        // Guardar la categoría primero
        Categoria categoriaSaved = guardarCategoria(categoria);
        
        // Si se especifica un tipo y entidad, crear la asociación
        if (tipo != null && entidadId != null) {
            asociarCategoriaConEntidad(categoriaSaved.getIdCategoria(), tipo, entidadId);
        }
        
        return categoriaSaved;
    }
    
    /**
     * Asocia una categoría existente con una entidad específica
     */
    public void asociarCategoriaConEntidad(Long categoriaId, String tipo, Long entidadId) {
        try {
            switch (tipo.toUpperCase()) {
                case "API":
                    jdbcTemplate.update(
                        "INSERT IGNORE INTO categoria_has_api (categoria_id_categoria, api_api_id) VALUES (?, ?)",
                        categoriaId, entidadId);
                    break;
                case "PROYECTO":
                    jdbcTemplate.update(
                        "INSERT IGNORE INTO categoria_has_proyecto (categoria_id_categoria, proyecto_proyecto_id) VALUES (?, ?)",
                        categoriaId, entidadId);
                    break;
                case "REPOSITORIO":
                    jdbcTemplate.update(
                        "INSERT IGNORE INTO categoria_has_repositorio (categoria_id_categoria, repositorio_repositorio_id) VALUES (?, ?)",
                        categoriaId, entidadId);
                    break;
                case "NOTIFICACION":
                    jdbcTemplate.update(
                        "INSERT IGNORE INTO categoria_has_notificacion (categoria_id_categoria, notificacion_notificacion_id) VALUES (?, ?)",
                        categoriaId, entidadId);
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al asociar categoría con " + tipo + ": " + e.getMessage());
        }
    }
    
    /**
     * Obtiene categorías no asociadas (disponibles para asociar)
     */
    public List<Categoria> findCategoriasNoAsociadas() {
        return jdbcTemplate.query(
            "SELECT c.* FROM categoria c " +
            "WHERE c.id_categoria NOT IN (" +
            "    SELECT DISTINCT categoria_id_categoria FROM categoria_has_api " +
            "    UNION " +
            "    SELECT DISTINCT categoria_id_categoria FROM categoria_has_proyecto " +
            "    UNION " +
            "    SELECT DISTINCT categoria_id_categoria FROM categoria_has_repositorio " +
            "    UNION " +
            "    SELECT DISTINCT categoria_id_categoria FROM categoria_has_notificacion" +
            ") ORDER BY c.nombre_categoria",
            (rs, rowNum) -> {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(rs.getLong("id_categoria"));
                categoria.setNombreCategoria(rs.getString("nombre_categoria"));
                categoria.setDescripcionCategoria(rs.getString("descripcion_categoria"));
                try {
                    String seccion = rs.getString("seccion_categoria");
                    if (seccion != null) {
                        categoria.setSeccionCategoria(Categoria.SeccionCategoria.valueOf(seccion));
                    }
                } catch (Exception e) {
                    // Si hay error al parsear la sección, la dejamos como null
                }
                return categoria;
            }
        );
    }
    
    /**
     * Obtiene todas las categorías ordenadas por nombre (alias para compatibilidad)
     */
    public List<Categoria> findAllOrderByNombre() {
        return listarTodasLasCategorias();
    }

    // ===== NUEVOS MÉTODOS PARA MANEJAR SECCIONES =====

    /**
     * Obtiene categorías por sección
     */
    public List<Categoria> findCategoriasPorSeccion(String seccion) {
        if ("TODAS".equals(seccion)) {
            return listarTodasLasCategorias();
        }
        
        try {
            Categoria.SeccionCategoria seccionEnum = Categoria.SeccionCategoria.valueOf(seccion);
            return categoriaRepository.findBySeccionCategoriaOrderByNombreCategoriaAsc(seccionEnum);
        } catch (IllegalArgumentException e) {
            return List.of(); // Retorna lista vacía si la sección no es válida
        }
    }

    /**
     * Obtiene estadísticas por sección (nueva implementación)
     */
    public Map<String, Object> getEstadisticasPorSeccion(String seccion) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        if ("TODAS".equals(seccion)) {
            estadisticas.put("totalCategorias", contarCategorias());
            estadisticas.put("totalEntidades", 0L);
            estadisticas.put("entidadesCategorizadas", 0L);
        } else {
            try {
                Categoria.SeccionCategoria seccionEnum = Categoria.SeccionCategoria.valueOf(seccion);
                long totalCategorias = categoriaRepository.countBySeccionCategoria(seccionEnum);
                estadisticas.put("totalCategorias", totalCategorias);
                
                // Para las estadísticas de entidades, por ahora devolvemos 0
                // porque necesitaríamos mapear las secciones con las entidades reales
                estadisticas.put("totalEntidades", 0L);
                estadisticas.put("entidadesCategorizadas", 0L);
            } catch (IllegalArgumentException e) {
                estadisticas.put("totalCategorias", 0L);
                estadisticas.put("totalEntidades", 0L);
                estadisticas.put("entidadesCategorizadas", 0L);
            }
        }
        
        return estadisticas;
    }

    /**
     * Cuenta categorías por sección
     */
    public long contarCategoriasPorSeccion(String seccion) {
        if ("TODAS".equals(seccion)) {
            return contarCategorias();
        }
        
        try {
            Categoria.SeccionCategoria seccionEnum = Categoria.SeccionCategoria.valueOf(seccion);
            return categoriaRepository.countBySeccionCategoria(seccionEnum);
        } catch (IllegalArgumentException e) {
            return 0L;
        }
    }

    /**
     * Obtiene categorías sin sección asignada
     */
    public List<Categoria> findCategoriasSinSeccion() {
        return categoriaRepository.findBySeccionCategoriaIsNull();
    }

    /**
     * Cuenta categorías sin sección asignada
     */
    public long contarCategoriasSinSeccion() {
        return categoriaRepository.countBySeccionCategoriaIsNull();
    }
}