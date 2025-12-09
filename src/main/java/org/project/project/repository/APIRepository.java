package org.project.project.repository;

import org.project.project.model.entity.API;
import org.project.project.model.entity.VersionAPI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface APIRepository extends JpaRepository<API, Long> {
    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    List<API> findAllByOrderByApiIdDesc();

    // ONLY categories
    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    List<API> findDistinctByCategories_CategoryNameInOrderByApiIdDesc(Collection<String> categorias);

    // ONLY tags
    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    List<API> findDistinctByTags_TagNameInOrderByApiIdDesc(Collection<String> etiquetas);

    // ONLY status
    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    List<API> findDistinctByApiStatusInOrderByApiIdDesc(Collection<API.EstadoApi> estados);

    // categories + tags
    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    List<API> findDistinctByCategories_CategoryNameInAndTags_TagNameInOrderByApiIdDesc(
            Collection<String> categorias, Collection<String> etiquetas);

    // categories + status
    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    List<API> findDistinctByCategories_CategoryNameInAndApiStatusInOrderByApiIdDesc(
            Collection<String> categorias, Collection<API.EstadoApi> estados);

    // tags + status
    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    List<API> findDistinctByTags_TagNameInAndApiStatusInOrderByApiIdDesc(
            Collection<String> etiquetas, Collection<API.EstadoApi> estados);

    // categories + tags + status
    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    List<API> findDistinctByCategories_CategoryNameInAndTags_TagNameInAndApiStatusInOrderByApiIdDesc(
            Collection<String> categorias, Collection<String> etiquetas, Collection<API.EstadoApi> estados);

    // Contar APIs distintas creadas por un usuario (a través de versiones)
    // Cuenta el número de APIs únicas, no el número de versiones
    @Query("SELECT COUNT(DISTINCT a) FROM API a JOIN a.versiones v WHERE v.creadoPorUsuarioId = :usuarioId")
    Long countDistinctApisByCreatedByUserId(@Param("usuarioId") Long usuarioId);

    // Obtener APIs creadas por un usuario
    @Query("SELECT DISTINCT a FROM API a JOIN a.versiones v WHERE v.creador.usuarioId = :usuarioId ORDER BY a.apiId DESC")
    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    List<API> findDistinctApisByCreatedByUserId(@Param("usuarioId") Long usuarioId);

    // Contar APIs distintas creadas por otros usuarios (no por el usuario actual)
    // Incluye APIs creadas por otros usuarios Y APIs sin creador asignado (id_creador IS NULL)
    @Query("SELECT COUNT(DISTINCT a) FROM API a LEFT JOIN a.versiones v WHERE (v.creador.usuarioId != :usuarioId OR v.creador.usuarioId IS NULL) AND a.apiId NOT IN (SELECT DISTINCT a2.apiId FROM API a2 JOIN a2.versiones v2 WHERE v2.creador.usuarioId = :usuarioId)")
    Long countDistinctApisByOtherUsers(@Param("usuarioId") Long usuarioId);

    // Contar el número total de versiones de API creadas por un usuario
    @Query("SELECT COUNT(v) FROM VersionAPI v WHERE v.creador.usuarioId = :usuarioId")
    Long countVersionsByCreatedByUserId(@Param("usuarioId") Long usuarioId);

    // Obtener APIs creadas por otros usuarios (limitado para overview)
    // Incluye APIs creadas por otros usuarios Y APIs sin creador asignado (id_creador IS NULL)
    @Query("SELECT DISTINCT a FROM API a LEFT JOIN a.versiones v WHERE (v.creador.usuarioId != :usuarioId OR v.creador.usuarioId IS NULL) AND a.apiId NOT IN (SELECT DISTINCT a2.apiId FROM API a2 JOIN a2.versiones v2 WHERE v2.creador.usuarioId = :usuarioId) ORDER BY a.apiId DESC")
    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    List<API> findDistinctApisByOtherUsers(@Param("usuarioId") Long usuarioId);

    // Contar APIs en estado QA donde el usuario es creador de la primera versión
    @Query("SELECT COUNT(DISTINCT a) FROM API a WHERE a.estadoApi = 'QA' AND EXISTS (SELECT v FROM VersionAPI v WHERE v.api = a AND v.creador.usuarioId = :usuarioId)")
    Long countApisInReviewByUser(@Param("usuarioId") Long usuarioId);

    // Verificar si existe una API con el nombre dado (case insensitive)
    boolean existsByNombreApiIgnoreCase(String nombreApi);

    // Buscar API por nombre exacto
    Optional<API> findByNombreApi(String nombreApi);

    // Buscar una versión específica por ID
    @Query("SELECT v FROM VersionAPI v JOIN FETCH v.api WHERE v.versionId = :versionId")
    Optional<VersionAPI> findVersionById(@Param("versionId") Long versionId);

    // Buscar API por ID con todas sus versiones cargadas
    @EntityGraph(attributePaths = {"versiones", "versiones.creador"})
    @Query("SELECT a FROM API a WHERE a.apiId = :apiId")
    Optional<API> findByIdWithVersions(@Param("apiId") Long apiId);

    // Métodos de paginación para filtros
    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    Page<API> findDistinctByCategories_CategoryNameIn(
            Collection<String> categorias, Pageable pageable);

    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    Page<API> findDistinctByTags_TagNameIn(
            Collection<String> etiquetas, Pageable pageable);

    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    Page<API> findDistinctByApiStatusIn(
            Collection<API.EstadoApi> estados, Pageable pageable);

    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    Page<API> findDistinctByCategories_CategoryNameInAndTags_TagNameIn(
            Collection<String> categorias, Collection<String> etiquetas, Pageable pageable);

    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    Page<API> findDistinctByCategories_CategoryNameInAndApiStatusIn(
            Collection<String> categorias, Collection<API.EstadoApi> estados, Pageable pageable);

    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    Page<API> findDistinctByTags_TagNameInAndApiStatusIn(
            Collection<String> etiquetas, Collection<API.EstadoApi> estados, Pageable pageable);

    @EntityGraph(attributePaths = { "categorias", "etiquetas", "versiones" })
    Page<API> findDistinctByCategories_CategoryNameInAndTags_TagNameInAndApiStatusIn(
            Collection<String> categorias, Collection<String> etiquetas, Collection<API.EstadoApi> estados, Pageable pageable);

    // Contar APIs por estado específico donde el usuario es creador
    @Query("SELECT COUNT(DISTINCT a) FROM API a JOIN a.versiones v WHERE v.creador.usuarioId = :usuarioId AND a.estadoApi = :estado")
    Long countApisByCreatedByUserIdAndEstado(@Param("usuarioId") Long usuarioId, @Param("estado") API.EstadoApi estado);

}
