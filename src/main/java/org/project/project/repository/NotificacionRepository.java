package org.project.project.repository;

import org.project.project.model.entity.Notificacion;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Notificacion n WHERE n.usuario.usuarioId = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);
    
    @Modifying
    @Transactional
    void deleteByUsuario(Usuario usuario);
}
