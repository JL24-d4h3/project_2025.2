package org.project.project.repository;

import org.project.project.model.entity.ForoVoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForoVotoRepository extends JpaRepository<ForoVoto, Long> {

    // =================== VERIFICAR VOTO EXISTENTE ===================

    Optional<ForoVoto> findByUsuario_UsuarioIdAndRespuesta_RespuestaId(Long usuarioId, Long respuestaId);

    boolean existsByUsuario_UsuarioIdAndRespuesta_RespuestaId(Long usuarioId, Long respuestaId);

    // =================== OBTENER TIPO DE VOTO ===================

    @Query("SELECT v.tipoVoto FROM ForoVoto v WHERE v.usuario.usuarioId = :usuarioId AND v.respuesta.respuestaId = :respuestaId")
    Optional<ForoVoto.TipoVoto> findVoteType(@Param("usuarioId") Long usuarioId, @Param("respuestaId") Long respuestaId);

    // =================== ESTADÍSTICAS DE VOTOS ===================

    @Query("SELECT COUNT(v) FROM ForoVoto v WHERE v.respuesta.respuestaId = :respuestaId AND v.tipoVoto = 'POSITIVO'")
    long countPositiveVotesByRespuestaId(@Param("respuestaId") Long respuestaId);

    @Query("SELECT COUNT(v) FROM ForoVoto v WHERE v.respuesta.respuestaId = :respuestaId AND v.tipoVoto = 'NEGATIVO'")
    long countNegativeVotesByRespuestaId(@Param("respuestaId") Long respuestaId);

    // =================== ELIMINAR VOTO ===================

    void deleteByUsuario_UsuarioIdAndRespuesta_RespuestaId(Long usuarioId, Long respuestaId);
}

