package org.project.project.repository;

import org.project.project.model.entity.APISuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface APISuscripcionRepository extends JpaRepository<APISuscripcion, Long> {

    // =================== BÚSQUEDAS BÁSICAS ===================

    Optional<APISuscripcion> findByUsuario_UsuarioIdAndApi_ApiId(Long usuarioId, Long apiId);

    List<APISuscripcion> findByUsuario_UsuarioId(Long usuarioId);

    List<APISuscripcion> findByApi_ApiId(Long apiId);

    List<APISuscripcion> findByEstadoSuscripcion(APISuscripcion.EstadoSuscripcion estado);

    List<APISuscripcion> findByPlanSuscripcion(APISuscripcion.PlanSuscripcion plan);

    // =================== SUSCRIPCIONES ACTIVAS ===================

    @Query("SELECT s FROM APISuscripcion s WHERE s.usuario.usuarioId = :usuarioId AND s.estadoSuscripcion = 'ACTIVA' ORDER BY s.fechaInicio DESC")
    List<APISuscripcion> findActiveSuscriptionsByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT s FROM APISuscripcion s WHERE s.api.apiId = :apiId AND s.estadoSuscripcion = 'ACTIVA'")
    List<APISuscripcion> findActiveSuscriptionsByApiId(@Param("apiId") Long apiId);

    // =================== VERIFICAR EXISTENCIA ===================

    boolean existsByUsuario_UsuarioIdAndApi_ApiIdAndEstadoSuscripcion(Long usuarioId, Long apiId, APISuscripcion.EstadoSuscripcion estado);

    // =================== VERIFICAR LÍMITES ===================

    @Query("SELECT s FROM APISuscripcion s WHERE s.llamadasMesActual >= s.limiteLlamadasMes AND s.estadoSuscripcion = 'ACTIVA'")
    List<APISuscripcion> findSuscriptionsAtLimit();

    @Query("SELECT s FROM APISuscripcion s WHERE s.usuario.usuarioId = :usuarioId AND s.api.apiId = :apiId AND s.estadoSuscripcion = 'ACTIVA' AND s.llamadasMesActual < s.limiteLlamadasMes")
    Optional<APISuscripcion> findValidSuscriptionForCall(@Param("usuarioId") Long usuarioId, @Param("apiId") Long apiId);

    // =================== ACTUALIZAR CONTADORES ===================

    @Modifying
    @Transactional
    @Query("UPDATE APISuscripcion s SET s.llamadasMesActual = s.llamadasMesActual + 1 WHERE s.suscripcionId = :suscripcionId")
    void incrementCallCount(@Param("suscripcionId") Long suscripcionId);

    @Modifying
    @Transactional
    @Query("UPDATE APISuscripcion s SET s.llamadasMesActual = 0, s.mesPeriodo = :nuevoMes WHERE s.suscripcionId = :suscripcionId")
    void resetMonthlyCounter(@Param("suscripcionId") Long suscripcionId, @Param("nuevoMes") LocalDate nuevoMes);

    // =================== RENOVACIÓN MENSUAL ===================

    @Query("SELECT s FROM APISuscripcion s WHERE s.mesPeriodo < :mesActual AND s.estadoSuscripcion = 'ACTIVA'")
    List<APISuscripcion> findSuscriptionsToRenew(@Param("mesActual") LocalDate mesActual);

    // =================== ESTADÍSTICAS ===================

    @Query("SELECT COUNT(s) FROM APISuscripcion s WHERE s.api.apiId = :apiId AND s.estadoSuscripcion = 'ACTIVA'")
    long countActiveSuscriptionsByApiId(@Param("apiId") Long apiId);

    @Query("SELECT s.planSuscripcion, COUNT(s) FROM APISuscripcion s WHERE s.api.apiId = :apiId AND s.estadoSuscripcion = 'ACTIVA' GROUP BY s.planSuscripcion")
    List<Object[]> countSuscriptionsByPlanForApi(@Param("apiId") Long apiId);

    @Query("SELECT SUM(s.costoMensual) FROM APISuscripcion s WHERE s.estadoSuscripcion = 'ACTIVA'")
    Double calculateTotalMonthlyRevenue();
}

