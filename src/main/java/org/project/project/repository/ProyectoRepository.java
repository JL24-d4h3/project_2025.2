package org.project.project.repository;

import org.project.project.model.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository principal para operaciones básicas CRUD de Proyecto.
 * 
 * Para consultas complejas específicas del dominio que involucran
 * múltiples entidades y lógica de negocio, usar ProyectoQueryService.
 */
@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    // =================== CONSULTAS BÁSICAS DE ENTIDAD ===================
    
    // Buscar por nombre exacto
    Optional<Proyecto> findByProjectName(String nombreProyecto);
    
    // Buscar por nombre con LIKE
    List<Proyecto> findByProjectNameContainingIgnoreCase(String nombre);
    
    // Buscar por estado
    List<Proyecto> findByProjectStatus(String estado);
    
    // Buscar por propietario
    List<Proyecto> findByProjectOwner(String propietario);
    
    // Buscar por estado y propietario
    List<Proyecto> findByProjectStatusAndProjectOwner(String estado, String propietario);
    
    // Buscar activos
    @Query("SELECT p FROM Proyecto p WHERE p.estadoProyecto != 'CERRADO'")
    List<Proyecto> findActiveProjects();
    
    // Buscar por fecha de inicio
    List<Proyecto> findByProjectStartDate(LocalDate fechaInicio);
    
    // Buscar proyectos por rango de fechas
    List<Proyecto> findByProjectStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Verificar existencia por nombre
    boolean existsByProjectName(String nombreProyecto);
    
    // Contar por estado
    long countByProjectStatus(String estado);
    
    // Contar por propietario
    long countByProjectOwner(String propietario);
    
    // Buscar proyectos creados por un usuario específico (owner)
    // Usando el campo createdBy que es de tipo Usuario (relación ManyToOne)
    // Se usa el patrón Property_Field para acceder al ID del Usuario relacionado
    List<Proyecto> findByCreatedBy_UsuarioId(Long usuarioId);
}
