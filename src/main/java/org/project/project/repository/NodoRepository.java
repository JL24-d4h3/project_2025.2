package org.project.project.repository;

import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodoRepository extends JpaRepository<Nodo, Long> {

    // Find by container (proyecto o repositorio)
    List<Nodo> findByContainerTypeAndContainerIdAndIsDeletedFalse(
            Nodo.ContainerType containerType, Long containerId);

    // Find by parent
    List<Nodo> findByParentIdAndIsDeletedFalse(Long parentId);

    // Find root nodes (without parent)
    List<Nodo> findByContainerTypeAndContainerIdAndParentIdIsNullAndIsDeletedFalse(
            Nodo.ContainerType containerType, Long containerId);

    // Find by path
    Optional<Nodo> findByContainerTypeAndContainerIdAndPathAndIsDeletedFalse(
            Nodo.ContainerType containerType, Long containerId, String path);

    // Find by name in specific container and parent
    List<Nodo> findByContainerTypeAndContainerIdAndParentIdAndNombreAndIsDeletedFalse(
            Nodo.ContainerType containerType, Long containerId, Long parentId, String nombre);

    // Find files only
    @Query("SELECT n FROM Nodo n WHERE n.containerType = :containerType AND n.containerId = :containerId " +
           "AND n.tipo = 'ARCHIVO' AND n.isDeleted = false")
    List<Nodo> findFilesByContainer(@Param("containerType") Nodo.ContainerType containerType, 
                                   @Param("containerId") Long containerId);

    // Find folders only
    @Query("SELECT n FROM Nodo n WHERE n.containerType = :containerType AND n.containerId = :containerId " +
           "AND n.tipo = 'CARPETA' AND n.isDeleted = false")
    List<Nodo> findFoldersByContainer(@Param("containerType") Nodo.ContainerType containerType, 
                                     @Param("containerId") Long containerId);

    // Find nodes by path pattern
    @Query("SELECT n FROM Nodo n WHERE n.containerType = :containerType AND n.containerId = :containerId " +
           "AND n.path LIKE :pathPattern AND n.isDeleted = false")
    List<Nodo> findByPathPattern(@Param("containerType") Nodo.ContainerType containerType,
                                @Param("containerId") Long containerId,
                                @Param("pathPattern") String pathPattern);

    // Count children of a node
    @Query("SELECT COUNT(n) FROM Nodo n WHERE n.parentId = :parentId AND n.isDeleted = false")
    long countChildrenByParentId(@Param("parentId") Long parentId);

    // Find nodes created by user
    List<Nodo> findByCreatedByAndIsDeletedFalse(Usuario creadoPor);

    // Check if path exists in container
    boolean existsByContainerTypeAndContainerIdAndPathAndIsDeletedFalse(
            Nodo.ContainerType containerType, Long containerId, String path);

    // Find nodes by mime type
    List<Nodo> findByContainerTypeAndContainerIdAndMimeTypeAndIsDeletedFalse(
            Nodo.ContainerType containerType, Long containerId, String mimeType);

    // Get total size by container
    @Query("SELECT COALESCE(SUM(n.size), 0) FROM Nodo n WHERE n.containerType = :containerType " +
           "AND n.containerId = :containerId AND n.isDeleted = false")
    Long getTotalSizeByContainer(@Param("containerType") Nodo.ContainerType containerType,
                                @Param("containerId") Long containerId);

    // Find nodes with specific names
    @Query("SELECT n FROM Nodo n WHERE n.containerType = :containerType AND n.containerId = :containerId " +
           "AND n.nombre IN :nombres AND n.isDeleted = false")
    List<Nodo> findByNombres(@Param("containerType") Nodo.ContainerType containerType,
                            @Param("containerId") Long containerId,
                            @Param("nombres") List<String> nombres);

    // Métodos adicionales para NodoService
    boolean existsByNombreAndParentIdAndIsDeletedFalse(String nombre, Long parentId);
    
    boolean existsByNombreAndParentIdAndIsDeletedFalseAndNodoIdNot(String nombre, Long parentId, Long nodoId);
    
    List<Nodo> findByParentIdIsNullAndContainerTypeAndContainerIdAndIsDeletedFalseOrderByTipoDescNombreAsc(
            Nodo.ContainerType containerType, Long containerId);

    List<Nodo> findByParentIdAndIsDeletedFalseOrderByTipoDescNombreAsc(Long parentId);

    // ===================================================================
    // OPTIMIZED QUERIES WITH JOIN FETCH (Elimina N+1 lazy loading)
    // ===================================================================
    
    /**
     * Carga nodos raíz con usuarios en UNA SOLA QUERY (JOIN FETCH)
     * Evita N+1 problem al acceder a creadoPor/actualizadoPor
     */
    @Query("SELECT DISTINCT n FROM Nodo n " +
           "LEFT JOIN FETCH n.creadoPor " +
           "LEFT JOIN FETCH n.actualizadoPor " +
           "WHERE n.parentId IS NULL " +
           "AND n.containerType = :containerType " +
           "AND n.containerId = :containerId " +
           "AND n.isDeleted = false " +
           "ORDER BY n.tipo DESC, n.nombre ASC")
    List<Nodo> findRootNodesWithUsers(@Param("containerType") Nodo.ContainerType containerType,
                                      @Param("containerId") Long containerId);
    
    /**
     * Carga nodos hijos con usuarios en UNA SOLA QUERY (JOIN FETCH)
     * Evita N+1 problem al acceder a creadoPor/actualizadoPor
     */
    @Query("SELECT DISTINCT n FROM Nodo n " +
           "LEFT JOIN FETCH n.creadoPor " +
           "LEFT JOIN FETCH n.actualizadoPor " +
           "WHERE n.parentId = :parentId " +
           "AND n.isDeleted = false " +
           "ORDER BY n.tipo DESC, n.nombre ASC")
    List<Nodo> findChildrenWithUsers(@Param("parentId") Long parentId);

    List<Nodo> findByNombreContainingAndContainerTypeAndContainerIdAndIsDeletedFalse(
            String nombre, Nodo.ContainerType containerType, Long containerId);

    List<Nodo> findByContainerTypeAndContainerIdAndIsDeletedTrueOrderByDeletedAtDesc(
            Nodo.ContainerType containerType, Long containerId);
    
    long countByParentIdAndIsDeletedFalse(Long parentId);
    
    long countByContainerTypeAndContainerIdAndTipoAndIsDeletedFalse(
            Nodo.ContainerType containerType, Long containerId, Nodo.TipoNodo tipo);
    
    List<Nodo> findByContainerTypeAndContainerIdAndTipoAndIsDeletedFalse(
            Nodo.ContainerType containerType, Long containerId, Nodo.TipoNodo tipo);

    // ===================================================================
    // MÉTODOS CON SOPORTE PARA RAMAS (BRANCHES)
    // ===================================================================
    
    /**
     * Busca nodos raíz filtrados por rama (para repositorios con branches)
     * Para PROYECTOS, rama_id será NULL
     */
    @Query("SELECT n FROM Nodo n WHERE n.parentId IS NULL " +
           "AND n.containerType = :containerType " +
           "AND n.containerId = :containerId " +
           "AND n.ramaId = :ramaId " +
           "AND n.isDeleted = false " +
           "ORDER BY n.tipo DESC, n.nombre ASC")
    List<Nodo> findByParentIdIsNullAndContainerTypeAndContainerIdAndRamaIdAndIsDeletedFalse(
            @Param("containerType") Nodo.ContainerType containerType,
            @Param("containerId") Long containerId,
            @Param("ramaId") Long ramaId);
    
    /**
     * Busca nodos por contenedor y rama (todos los nodos de una rama específica)
     */
    List<Nodo> findByContainerTypeAndContainerIdAndRamaIdAndIsDeletedFalse(
            Nodo.ContainerType containerType, Long containerId, Long ramaId);
    
    /**
     * Cuenta nodos en una rama específica
     */
    long countByContainerTypeAndContainerIdAndRamaIdAndIsDeletedFalse(
            Nodo.ContainerType containerType, Long containerId, Long ramaId);
}
