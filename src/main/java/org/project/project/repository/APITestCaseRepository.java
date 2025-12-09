package org.project.project.repository;

import org.project.project.model.entity.APITestCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface APITestCaseRepository extends JpaRepository<APITestCase, Long> {

    // =================== BÚSQUEDAS BÁSICAS ===================

    List<APITestCase> findByApi_ApiId(Long apiId);

    List<APITestCase> findByVersion_VersionId(Long versionId);

    List<APITestCase> findByApi_ApiIdAndVersion_VersionId(Long apiId, Long versionId);

    List<APITestCase> findByEstadoTest(APITestCase.EstadoTest estado);

    List<APITestCase> findByPrioridadTest(APITestCase.PrioridadTest prioridad);

    // =================== TESTS ACTIVOS ===================

    @Query("SELECT t FROM APITestCase t WHERE t.api.apiId = :apiId AND t.estadoTest = 'ACTIVO' ORDER BY t.prioridadTest DESC, t.creadoEn DESC")
    List<APITestCase> findActiveTestsByApiId(@Param("apiId") Long apiId);

    @Query("SELECT t FROM APITestCase t WHERE t.version.versionId = :versionId AND t.estadoTest = 'ACTIVO'")
    List<APITestCase> findActiveTestsByVersionId(@Param("versionId") Long versionId);

    // =================== BÚSQUEDA POR MÉTODO HTTP ===================

    List<APITestCase> findByMetodoHTTP(APITestCase.MetodoHTTP metodo);

    @Query("SELECT t FROM APITestCase t WHERE t.api.apiId = :apiId AND t.metodoHTTP = :metodo AND t.estadoTest = 'ACTIVO'")
    List<APITestCase> findByApiIdAndMetodo(@Param("apiId") Long apiId, @Param("metodo") APITestCase.MetodoHTTP metodo);

    // =================== BÚSQUEDA POR ENDPOINT ===================

    List<APITestCase> findByEndpointPathContaining(String path);

    @Query("SELECT t FROM APITestCase t WHERE t.api.apiId = :apiId AND t.endpointPath = :path")
    List<APITestCase> findByApiIdAndEndpoint(@Param("apiId") Long apiId, @Param("path") String path);

    // =================== BÚSQUEDA AVANZADA ===================

    @Query("SELECT t FROM APITestCase t WHERE " +
           "t.api.apiId = :apiId " +
           "AND (:versionId IS NULL OR t.version.versionId = :versionId) " +
           "AND (:estado IS NULL OR t.estadoTest = :estado) " +
           "AND (:prioridad IS NULL OR t.prioridadTest = :prioridad) " +
           "ORDER BY t.prioridadTest DESC, t.creadoEn DESC")
    Page<APITestCase> findTestsWithFilters(@Param("apiId") Long apiId,
                                           @Param("versionId") Long versionId,
                                           @Param("estado") APITestCase.EstadoTest estado,
                                           @Param("prioridad") APITestCase.PrioridadTest prioridad,
                                           Pageable pageable);

    // =================== ESTADÍSTICAS ===================

    long countByApi_ApiId(Long apiId);

    long countByVersion_VersionId(Long versionId);

    @Query("SELECT COUNT(t) FROM APITestCase t WHERE t.api.apiId = :apiId AND t.estadoTest = 'ACTIVO'")
    long countActiveTestsByApiId(@Param("apiId") Long apiId);

    @Query("SELECT t.prioridadTest, COUNT(t) FROM APITestCase t WHERE t.api.apiId = :apiId GROUP BY t.prioridadTest")
    List<Object[]> countTestsByPriorityForApi(@Param("apiId") Long apiId);

    // =================== TESTS POR CREADOR ===================

    List<APITestCase> findByCreadoPor_UsuarioId(Long usuarioId);

    @Query("SELECT COUNT(t) FROM APITestCase t WHERE t.creadoPor.usuarioId = :usuarioId")
    long countTestsByCreador(@Param("usuarioId") Long usuarioId);
}

