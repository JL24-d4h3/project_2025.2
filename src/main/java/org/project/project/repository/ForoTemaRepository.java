package org.project.project.repository;

import org.project.project.model.entity.ForoTema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForoTemaRepository extends JpaRepository<ForoTema, Long> {

    // =================== BÚSQUEDAS BÁSICAS ===================

    Optional<ForoTema> findBySlug(String slug);

    List<ForoTema> findByTituloTemaContainingIgnoreCase(String titulo);

    List<ForoTema> findByEstadoTema(ForoTema.EstadoTema estado);

    List<ForoTema> findByAutor_UsuarioId(Long autorId);

    // =================== TEMAS DESTACADOS ===================

    @Query("SELECT t FROM ForoTema t WHERE t.esAnclado = true ORDER BY t.fechaCreacion DESC")
    List<ForoTema> findPinnedTopics();

    @Query("SELECT t FROM ForoTema t WHERE t.esAnclado = true AND t.estadoTema = :estado")
    List<ForoTema> findPinnedTopicsByEstado(@Param("estado") ForoTema.EstadoTema estado);

    // =================== LISTADOS CON PAGINACIÓN ===================

    @Query("SELECT t FROM ForoTema t WHERE t.estadoTema = 'ABIERTO' ORDER BY t.esAnclado DESC, t.ultimaRespuestaFecha DESC")
    Page<ForoTema> findOpenTopicsOrderedByActivity(Pageable pageable);

    @Query("SELECT t FROM ForoTema t ORDER BY t.esAnclado DESC, t.fechaCreacion DESC")
    Page<ForoTema> findAllTopicsOrdered(Pageable pageable);

    // =================== BÚSQUEDA POR CATEGORÍA ===================

    @Query("SELECT t FROM ForoTema t JOIN t.categorias c WHERE c.idCategoria = :categoriaId ORDER BY t.esAnclado DESC, t.ultimaRespuestaFecha DESC")
    Page<ForoTema> findByCategoriaId(@Param("categoriaId") Long categoriaId, Pageable pageable);

    // =================== ESTADÍSTICAS ===================

    long countByEstadoTema(ForoTema.EstadoTema estado);

    @Query("SELECT COUNT(t) FROM ForoTema t WHERE t.autor.usuarioId = :autorId")
    long countByAutorId(@Param("autorId") Long autorId);

    @Query("SELECT SUM(t.vistas) FROM ForoTema t WHERE t.autor.usuarioId = :autorId")
    Long sumVistasByAutorId(@Param("autorId") Long autorId);

    // =================== BÚSQUEDA AVANZADA ===================

    // TEMPORALMENTE DESACTIVADO - Problema de validación de query
    /*
    @Query("SELECT t FROM ForoTema t WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(t.tituloTema) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.contenidoTema) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:estado IS NULL OR t.estadoTema = :estado) " +
           "ORDER BY t.esAnclado DESC, t.fechaCreacion DESC")
    Page<ForoTema> searchTopics(@Param("search") String search,
                                 @Param("estado") ForoTema.EstadoTema estado,
                                 Pageable pageable);
    */

    // =================== TEMAS POPULARES ===================

    @Query("SELECT t FROM ForoTema t WHERE t.estadoTema = 'ABIERTO' ORDER BY t.vistas DESC")
    List<ForoTema> findMostViewedTopics(Pageable pageable);

    @Query("SELECT t FROM ForoTema t WHERE t.estadoTema = 'ABIERTO' ORDER BY t.respuestasCount DESC")
    List<ForoTema> findMostAnsweredTopics(Pageable pageable);

    // =================== ACTUALIZACIÓN DE CONTADORES ===================

    @Modifying
    @Transactional
    @Query("UPDATE ForoTema t SET t.vistas = t.vistas + 1 WHERE t.temaId = :temaId")
    void incrementViewCount(@Param("temaId") Long temaId);

    @Modifying
    @Transactional
    @Query("UPDATE ForoTema t SET t.respuestasCount = t.respuestasCount + 1, t.ultimaRespuestaFecha = CURRENT_TIMESTAMP, t.ultimaRespuestaUsuario.usuarioId = :usuarioId WHERE t.temaId = :temaId")
    void incrementAnswerCount(@Param("temaId") Long temaId, @Param("usuarioId") Long usuarioId);

    @Modifying
    @Transactional
    @Query("UPDATE ForoTema t SET t.respuestasCount = t.respuestasCount - 1 WHERE t.temaId = :temaId AND t.respuestasCount > 0")
    void decrementAnswerCount(@Param("temaId") Long temaId);
}
