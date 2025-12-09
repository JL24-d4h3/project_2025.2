package org.project.project.repository;

import org.project.project.model.entity.APIMockServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface APIMockServerRepository extends JpaRepository<APIMockServer, Long> {

    // =================== BÚSQUEDAS BÁSICAS ===================

    List<APIMockServer> findByApi_ApiId(Long apiId);

    List<APIMockServer> findByVersion_VersionId(Long versionId);

    List<APIMockServer> findByApi_ApiIdAndVersion_VersionId(Long apiId, Long versionId);

    List<APIMockServer> findByEstadoMock(APIMockServer.EstadoMock estado);

    List<APIMockServer> findByCreadoPor_UsuarioId(Long usuarioId);

    // =================== MOCKS ACTIVOS ===================

    @Query("SELECT m FROM APIMockServer m WHERE m.api.apiId = :apiId AND m.estadoMock = 'ACTIVO' ORDER BY m.fechaCreacion DESC")
    List<APIMockServer> findActiveMocksByApiId(@Param("apiId") Long apiId);

    @Query("SELECT m FROM APIMockServer m WHERE m.version.versionId = :versionId AND m.estadoMock = 'ACTIVO'")
    List<APIMockServer> findActiveMocksByVersionId(@Param("versionId") Long versionId);

    // =================== BÚSQUEDA POR NOMBRE ===================

    Optional<APIMockServer> findByNombreMock(String nombreMock);

    List<APIMockServer> findByNombreMockContainingIgnoreCase(String nombre);

    // =================== BÚSQUEDA POR BASE URL ===================

    Optional<APIMockServer> findByBaseUrl(String baseUrl);

    List<APIMockServer> findByBaseUrlContaining(String urlPart);

    // =================== ESTADÍSTICAS ===================

    long countByApi_ApiId(Long apiId);

    @Query("SELECT COUNT(m) FROM APIMockServer m WHERE m.api.apiId = :apiId AND m.estadoMock = 'ACTIVO'")
    long countActiveMocksByApiId(@Param("apiId") Long apiId);

    @Query("SELECT m.estadoMock, COUNT(m) FROM APIMockServer m WHERE m.api.apiId = :apiId GROUP BY m.estadoMock")
    List<Object[]> countMocksByEstadoForApi(@Param("apiId") Long apiId);

    // =================== BÚSQUEDA AVANZADA ===================

    @Query("SELECT m FROM APIMockServer m WHERE " +
           "m.api.apiId = :apiId " +
           "AND (:versionId IS NULL OR m.version.versionId = :versionId) " +
           "AND (:estado IS NULL OR m.estadoMock = :estado) " +
           "ORDER BY m.fechaCreacion DESC")
    List<APIMockServer> findMocksWithFilters(@Param("apiId") Long apiId,
                                              @Param("versionId") Long versionId,
                                              @Param("estado") APIMockServer.EstadoMock estado);
}

