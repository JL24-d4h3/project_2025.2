package org.project.project.repository;

import org.project.project.model.entity.ForoRespuesta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ForoRespuestaRepository extends JpaRepository<ForoRespuesta, Long> {

    // =================== BÚSQUEDAS BÁSICAS ===================

    List<ForoRespuesta> findByTema_TemaId(Long temaId);

    @Query("SELECT r FROM ForoRespuesta r WHERE r.tema.temaId = :temaId ORDER BY r.fechaCreacion ASC")
    Page<ForoRespuesta> findByTemaIdOrdered(@Param("temaId") Long temaId, Pageable pageable);

    List<ForoRespuesta> findByAutor_UsuarioId(Long autorId);

    // =================== RESPUESTAS RAÍZ (SIN PADRE) ===================

    @Query("SELECT r FROM ForoRespuesta r WHERE r.tema.temaId = :temaId AND r.parentRespuesta IS NULL ORDER BY r.fechaCreacion ASC")
    List<ForoRespuesta> findRootAnswersByTemaId(@Param("temaId") Long temaId);

    @Query("SELECT r FROM ForoRespuesta r WHERE r.tema.temaId = :temaId AND r.parentRespuesta IS NULL ORDER BY r.puntuacionTotal DESC, r.fechaCreacion ASC")
    List<ForoRespuesta> findRootAnswersByTemaIdOrderedByScore(@Param("temaId") Long temaId);

    // =================== RESPUESTAS HIJAS ===================

    @Query("SELECT r FROM ForoRespuesta r WHERE r.parentRespuesta.respuestaId = :parentId ORDER BY r.fechaCreacion ASC")
    List<ForoRespuesta> findRepliesByParentId(@Param("parentId") Long parentId);

    // =================== SOLUCIÓN ACEPTADA ===================

    @Query("SELECT r FROM ForoRespuesta r WHERE r.tema.temaId = :temaId AND r.esSolucion = true")
    List<ForoRespuesta> findSolutionByTemaId(@Param("temaId") Long temaId);

    boolean existsByTema_TemaIdAndEsSolucionTrue(Long temaId);

    // =================== ESTADÍSTICAS ===================

    long countByTema_TemaId(Long temaId);

    long countByAutor_UsuarioId(Long autorId);

    @Query("SELECT COUNT(r) FROM ForoRespuesta r WHERE r.autor.usuarioId = :autorId AND r.esSolucion = true")
    long countSolutionsByAutorId(@Param("autorId") Long autorId);

    @Query("SELECT SUM(r.votosPositivos) FROM ForoRespuesta r WHERE r.autor.usuarioId = :autorId")
    Long sumUpvotesByAutorId(@Param("autorId") Long autorId);

    // =================== MEJORES RESPUESTAS ===================

    @Query("SELECT r FROM ForoRespuesta r WHERE r.tema.temaId = :temaId ORDER BY r.puntuacionTotal DESC")
    List<ForoRespuesta> findTopAnswersByTemaId(@Param("temaId") Long temaId, Pageable pageable);

    // =================== ACTUALIZACIÓN DE VOTOS ===================

    @Modifying
    @Transactional
    @Query("UPDATE ForoRespuesta r SET r.votosPositivos = r.votosPositivos + 1, r.puntuacionTotal = r.puntuacionTotal + 1 WHERE r.respuestaId = :respuestaId")
    void incrementUpvotes(@Param("respuestaId") Long respuestaId);

    @Modifying
    @Transactional
    @Query("UPDATE ForoRespuesta r SET r.votosPositivos = r.votosPositivos - 1, r.puntuacionTotal = r.puntuacionTotal - 1 WHERE r.respuestaId = :respuestaId AND r.votosPositivos > 0")
    void decrementUpvotes(@Param("respuestaId") Long respuestaId);

    @Modifying
    @Transactional
    @Query("UPDATE ForoRespuesta r SET r.votosNegativos = r.votosNegativos + 1, r.puntuacionTotal = r.puntuacionTotal - 1 WHERE r.respuestaId = :respuestaId")
    void incrementDownvotes(@Param("respuestaId") Long respuestaId);

    @Modifying
    @Transactional
    @Query("UPDATE ForoRespuesta r SET r.votosNegativos = r.votosNegativos - 1, r.puntuacionTotal = r.puntuacionTotal + 1 WHERE r.respuestaId = :respuestaId AND r.votosNegativos > 0")
    void decrementDownvotes(@Param("respuestaId") Long respuestaId);

    // =================== MARCAR COMO SOLUCIÓN ===================

    @Modifying
    @Transactional
    @Query("UPDATE ForoRespuesta r SET r.esSolucion = false WHERE r.tema.temaId = :temaId")
    void unmarkAllSolutionsForTema(@Param("temaId") Long temaId);

    @Modifying
    @Transactional
    @Query("UPDATE ForoRespuesta r SET r.esSolucion = true WHERE r.respuestaId = :respuestaId")
    void markAsSolution(@Param("respuestaId") Long respuestaId);
}

