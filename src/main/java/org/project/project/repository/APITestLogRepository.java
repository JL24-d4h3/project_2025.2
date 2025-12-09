package org.project.project.repository;

import org.project.project.model.entity.APITestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface APITestLogRepository extends JpaRepository<APITestLog, Long> {

    // =================== BÚSQUEDAS BÁSICAS ===================

    List<APITestLog> findByTestCase_TestCaseId(Long testCaseId);

    @Query("SELECT l FROM APITestLog l WHERE l.testCase.testCaseId = :testCaseId ORDER BY l.fechaEjecucion DESC")
    Page<APITestLog> findByTestCaseIdOrdered(@Param("testCaseId") Long testCaseId, Pageable pageable);

    List<APITestLog> findByEjecutadoPor_UsuarioId(Long usuarioId);

    List<APITestLog> findByResultado(APITestLog.Resultado resultado);

    // =================== ÚLTIMAS EJECUCIONES ===================

    @Query("SELECT l FROM APITestLog l WHERE l.testCase.testCaseId = :testCaseId ORDER BY l.fechaEjecucion DESC")
    List<APITestLog> findLatestExecutionsByTestCaseId(@Param("testCaseId") Long testCaseId, Pageable pageable);

    @Query("SELECT l FROM APITestLog l WHERE l.ejecutadoPor.usuarioId = :usuarioId ORDER BY l.fechaEjecucion DESC")
    Page<APITestLog> findLatestExecutionsByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    // =================== EJECUCIONES POR FECHA ===================

    List<APITestLog> findByFechaEjecucionBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT l FROM APITestLog l WHERE l.testCase.api.apiId = :apiId AND l.fechaEjecucion BETWEEN :startDate AND :endDate ORDER BY l.fechaEjecucion DESC")
    List<APITestLog> findExecutionsByApiIdAndDateRange(@Param("apiId") Long apiId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    // =================== EJECUCIONES POR ENTORNO ===================

    List<APITestLog> findByEntorno_EntornoId(Long entornoId);

    @Query("SELECT l FROM APITestLog l WHERE l.entorno.entornoId = :entornoId AND l.resultado = :resultado")
    List<APITestLog> findByEntornoIdAndResultado(@Param("entornoId") Long entornoId,
                                                   @Param("resultado") APITestLog.Resultado resultado);

    // =================== ESTADÍSTICAS ===================

    long countByTestCase_TestCaseId(Long testCaseId);

    @Query("SELECT COUNT(l) FROM APITestLog l WHERE l.testCase.testCaseId = :testCaseId AND l.resultado = :resultado")
    long countByTestCaseIdAndResultado(@Param("testCaseId") Long testCaseId,
                                        @Param("resultado") APITestLog.Resultado resultado);

    @Query("SELECT l.resultado, COUNT(l) FROM APITestLog l WHERE l.testCase.testCaseId = :testCaseId GROUP BY l.resultado")
    List<Object[]> countResultsByTestCaseId(@Param("testCaseId") Long testCaseId);

    // =================== TASA DE ÉXITO ===================

    @Query("SELECT COUNT(l) FROM APITestLog l WHERE l.testCase.api.apiId = :apiId AND l.resultado = 'EXITOSO'")
    long countSuccessfulExecutionsByApiId(@Param("apiId") Long apiId);

    @Query("SELECT COUNT(l) FROM APITestLog l WHERE l.testCase.api.apiId = :apiId")
    long countTotalExecutionsByApiId(@Param("apiId") Long apiId);

    // =================== DURACIÓN PROMEDIO ===================

    @Query("SELECT AVG(l.duracionMs) FROM APITestLog l WHERE l.testCase.testCaseId = :testCaseId")
    Double calculateAverageDuration(@Param("testCaseId") Long testCaseId);

    @Query("SELECT AVG(l.duracionMs) FROM APITestLog l WHERE l.testCase.api.apiId = :apiId")
    Double calculateAverageDurationByApiId(@Param("apiId") Long apiId);

    // =================== EJECUCIONES FALLIDAS ===================

    @Query("SELECT l FROM APITestLog l WHERE l.resultado IN ('FALLIDO', 'ERROR', 'TIMEOUT') ORDER BY l.fechaEjecucion DESC")
    Page<APITestLog> findFailedExecutions(Pageable pageable);

    @Query("SELECT l FROM APITestLog l WHERE l.testCase.api.apiId = :apiId AND l.resultado IN ('FALLIDO', 'ERROR', 'TIMEOUT') ORDER BY l.fechaEjecucion DESC")
    List<APITestLog> findFailedExecutionsByApiId(@Param("apiId") Long apiId);
}

