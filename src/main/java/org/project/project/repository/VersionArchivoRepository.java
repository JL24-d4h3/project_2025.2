package org.project.project.repository;

import org.project.project.model.entity.VersionArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository principal para operaciones básicas CRUD de VersionArchivo.
 * 
 * Para consultas complejas específicas del dominio que involucran
 * múltiples entidades y lógica de negocio, usar VersionArchivoQueryService.
 */
@Repository
public interface VersionArchivoRepository extends JpaRepository<VersionArchivo, Long> {

    // =================== CONSULTAS BÁSICAS DE ENTIDAD ===================
    
    // Find all versions of a file by node ID ordered by creation date descending
    List<VersionArchivo> findByNodo_NodoIdOrderByCreatedAtDesc(Long nodoId);

    // Find current version by node ID and active status
    List<VersionArchivo> findByNodo_NodoIdAndActive(Long nodoId, Boolean vigente);
    
    // Find active version by node ID
    @Query("SELECT fv FROM VersionArchivo fv WHERE fv.nodo.nodoId = :nodoId AND fv.vigente = true")
    Optional<VersionArchivo> findActiveVersionByNodeId(@Param("nodoId") Long nodoId);

    // Find by storage key
    Optional<VersionArchivo> findByStorageKey(String storageKey);

    // Find by link ID
    List<VersionArchivo> findByEnlace_EnlaceId(Long enlaceId);

    // Find versions created by user ordered by creation date descending
    List<VersionArchivo> findByCreadoPor_UsuarioIdOrderByCreatedAtDesc(Long usuarioId);

    // Find versions created in date range
    @Query("SELECT fv FROM VersionArchivo fv WHERE fv.creadoEn BETWEEN :startDate AND :endDate ORDER BY fv.creadoEn DESC")
    List<VersionArchivo> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    // Get total size by node ID
    @Query("SELECT COALESCE(SUM(fv.sizeBytes), 0) FROM VersionArchivo fv WHERE fv.nodo.nodoId = :nodoId")
    Long getTotalSizeByNodeId(@Param("nodoId") Long nodoId);

    // Count versions by node ID
    long countByNodo_NodoId(Long nodoId);

    // Find versions by checksum
    List<VersionArchivo> findByChecksum(String checksum);

    // Find latest version by node ID
    @Query("SELECT fv FROM VersionArchivo fv WHERE fv.nodo.nodoId = :nodoId ORDER BY fv.creadoEn DESC LIMIT 1")
    Optional<VersionArchivo> findLatestByNodeId(@Param("nodoId") Long nodoId);

    // Find versions by storage bucket
    List<VersionArchivo> findByStorageBucket(String storageBucket);

    // Check if storage key exists
    boolean existsByStorageKey(String storageKey);

    // Find large files (above threshold)
    @Query("SELECT fv FROM VersionArchivo fv WHERE fv.sizeBytes > :threshold ORDER BY fv.sizeBytes DESC")
    List<VersionArchivo> findLargeFiles(@Param("threshold") Long threshold);

    // Find obsolete versions by node ID
    @Query("SELECT fv FROM VersionArchivo fv WHERE fv.nodo.nodoId = :nodoId AND fv.vigente = false ORDER BY fv.creadoEn DESC")
    List<VersionArchivo> findObsoleteVersionsByNodeId(@Param("nodoId") Long nodoId);

    // Get total storage usage
    @Query("SELECT COALESCE(SUM(fv.sizeBytes), 0) FROM VersionArchivo fv WHERE fv.vigente = true")
    Long getTotalStorageUsage();

    // Find duplicated files by checksum
    @Query("SELECT fv.checksum, COUNT(fv) FROM VersionArchivo fv WHERE fv.checksum IS NOT NULL " +
           "GROUP BY fv.checksum HAVING COUNT(fv) > 1")
    List<Object[]> findDuplicatedFilesByChecksum();
}