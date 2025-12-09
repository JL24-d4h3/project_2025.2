package org.project.project.repository;

import org.project.project.model.entity.APITestResultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface APITestResultadoRepository extends JpaRepository<APITestResultado, Long> {

    // =================== BÚSQUEDAS BÁSICAS ===================

    List<APITestResultado> findByTestLog_TestLogId(Long testLogId);

    List<APITestResultado> findByResultado(APITestResultado.Resultado resultado);

    List<APITestResultado> findByTipoValidacion(APITestResultado.TipoValidacion tipo);

    // =================== RESULTADOS FALLIDOS ===================

    @Query("SELECT r FROM APITestResultado r WHERE r.testLog.testLogId = :testLogId AND r.resultado = 'FAIL'")
    List<APITestResultado> findFailedValidationsByTestLogId(@Param("testLogId") Long testLogId);

    @Query("SELECT r FROM APITestResultado r WHERE r.testLog.testLogId = :testLogId AND r.resultado = 'PASS'")
    List<APITestResultado> findPassedValidationsByTestLogId(@Param("testLogId") Long testLogId);

    // =================== ESTADÍSTICAS ===================

    long countByTestLog_TestLogId(Long testLogId);

    @Query("SELECT COUNT(r) FROM APITestResultado r WHERE r.testLog.testLogId = :testLogId AND r.resultado = 'PASS'")
    long countPassedByTestLogId(@Param("testLogId") Long testLogId);

    @Query("SELECT COUNT(r) FROM APITestResultado r WHERE r.testLog.testLogId = :testLogId AND r.resultado = 'FAIL'")
    long countFailedByTestLogId(@Param("testLogId") Long testLogId);

    @Query("SELECT r.tipoValidacion, COUNT(r) FROM APITestResultado r WHERE r.testLog.testLogId = :testLogId GROUP BY r.tipoValidacion")
    List<Object[]> countByValidationType(@Param("testLogId") Long testLogId);

    // =================== BÚSQUEDA POR TIPO DE VALIDACIÓN ===================

    @Query("SELECT r FROM APITestResultado r WHERE r.testLog.testLogId = :testLogId AND r.tipoValidacion = :tipo")
    List<APITestResultado> findByTestLogIdAndTipoValidacion(@Param("testLogId") Long testLogId,
                                                              @Param("tipo") APITestResultado.TipoValidacion tipo);

    // =================== VALIDACIONES PROBLEMÁTICAS ===================

    @Query("SELECT r.nombreValidacion, COUNT(r) FROM APITestResultado r " +
           "WHERE r.testLog.testCase.testCaseId = :testCaseId AND r.resultado = 'FAIL' " +
           "GROUP BY r.nombreValidacion ORDER BY COUNT(r) DESC")
    List<Object[]> findMostFailedValidationsByTestCaseId(@Param("testCaseId") Long testCaseId);
}

