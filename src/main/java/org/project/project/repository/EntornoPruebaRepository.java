package org.project.project.repository;

import org.project.project.model.entity.EntornoPrueba;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EntornoPruebaRepository extends JpaRepository<EntornoPrueba, Long> {

    // =================== BÚSQUEDAS BÁSICAS ===================

    List<EntornoPrueba> findByUsuario_UsuarioId(Long usuarioId);

    List<EntornoPrueba> findByApi_ApiId(Long apiId);

    List<EntornoPrueba> findByUsuario_UsuarioIdAndApi_ApiId(Long usuarioId, Long apiId);

    List<EntornoPrueba> findByEstadoEntorno(EntornoPrueba.EstadoEntorno estado);

    // =================== ENTORNOS ACTIVOS ===================

    @Query("SELECT e FROM EntornoPrueba e WHERE e.usuario.usuarioId = :usuarioId AND e.estadoEntorno = 'ACTIVO' ORDER BY e.fechaCreacion DESC")
    List<EntornoPrueba> findActiveEnvironmentsByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT e FROM EntornoPrueba e WHERE e.api.apiId = :apiId AND e.estadoEntorno = 'ACTIVO'")
    List<EntornoPrueba> findActiveEnvironmentsByApiId(@Param("apiId") Long apiId);

    // =================== VERIFICAR LÍMITES ===================

    @Query("SELECT COUNT(e) FROM EntornoPrueba e WHERE e.usuario.usuarioId = :usuarioId AND e.estadoEntorno = 'ACTIVO'")
    long countActiveEnvironmentsByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT e FROM EntornoPrueba e WHERE e.llamadasRealizadas >= e.limiteLlamadasDia AND e.estadoEntorno = 'ACTIVO'")
    List<EntornoPrueba> findEnvironmentsAtLimit();

    // =================== ENTORNOS EXPIRADOS ===================

    @Query("SELECT e FROM EntornoPrueba e WHERE e.fechaExpiracion < :fecha AND e.estadoEntorno = 'ACTIVO'")
    List<EntornoPrueba> findExpiredEnvironments(@Param("fecha") LocalDateTime fecha);

    // =================== ACTUALIZAR CONTADORES ===================

    @Modifying
    @Transactional
    @Query("UPDATE EntornoPrueba e SET e.llamadasRealizadas = e.llamadasRealizadas + 1, e.ultimaLlamada = CURRENT_TIMESTAMP WHERE e.entornoId = :entornoId")
    void incrementCallCount(@Param("entornoId") Long entornoId);

    @Modifying
    @Transactional
    @Query("UPDATE EntornoPrueba e SET e.llamadasRealizadas = 0 WHERE e.entornoId = :entornoId")
    void resetCallCount(@Param("entornoId") Long entornoId);

    // =================== CAMBIAR ESTADO ===================

    @Modifying
    @Transactional
    @Query("UPDATE EntornoPrueba e SET e.estadoEntorno = :estado WHERE e.entornoId = :entornoId")
    void updateEstado(@Param("entornoId") Long entornoId, @Param("estado") EntornoPrueba.EstadoEntorno estado);

    @Modifying
    @Transactional
    @Query("UPDATE EntornoPrueba e SET e.estadoEntorno = 'EXPIRADO' WHERE e.fechaExpiracion < :fecha AND e.estadoEntorno = 'ACTIVO'")
    int expireOldEnvironments(@Param("fecha") LocalDateTime fecha);
}

