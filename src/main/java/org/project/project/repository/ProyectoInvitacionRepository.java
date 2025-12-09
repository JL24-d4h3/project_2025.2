package org.project.project.repository;

import org.project.project.model.entity.ProyectoInvitacion;
import org.project.project.model.entity.Proyecto;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProyectoInvitacionRepository extends JpaRepository<ProyectoInvitacion, Long> {
    
    Optional<ProyectoInvitacion> findByToken(String token);
    
    List<ProyectoInvitacion> findByUsuarioInvitadoAndEstado(Usuario usuario, ProyectoInvitacion.EstadoInvitacion estado);
    
    List<ProyectoInvitacion> findByProyecto(Proyecto proyecto);
    
    boolean existsByUsuarioInvitadoAndProyectoAndEstado(Usuario usuario, Proyecto proyecto, ProyectoInvitacion.EstadoInvitacion estado);
}
