package org.project.project.repository;

import org.project.project.model.entity.Repositorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository principal para operaciones básicas CRUD de Repositorio.
 * 
 * Para consultas complejas específicas del dominio que involucran
 * múltiples entidades y lógica de negocio, usar RepositorioQueryService.
 */
@Repository
public interface RepositorioRepository extends JpaRepository<Repositorio, Long> {

    // =================== CONSULTAS BÁSICAS DE ENTIDAD ===================
    
    // Buscar por nombre exacto
    Optional<Repositorio> findByRepositoryName(String nombreRepositorio);
    
    // Buscar por nombre con LIKE
    List<Repositorio> findByRepositoryNameContainingIgnoreCase(String nombre);
    
    // Buscar por visibilidad
    List<Repositorio> findByRepositoryVisibility(String visibilidad);
    
    // Buscar repositorios públicos
    @Query("SELECT r FROM Repositorio r WHERE r.visibilidadRepositorio = 'PUBLICO'")
    List<Repositorio> findPublicRepositories();
    
    // Verificar existencia por nombre
    boolean existsByRepositoryName(String nombreRepositorio);
    
    // Contar por visibilidad
    long countByRepositoryVisibility(String visibilidad);
}
