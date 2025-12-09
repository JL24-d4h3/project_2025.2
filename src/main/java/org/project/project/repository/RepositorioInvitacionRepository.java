package org.project.project.repository;

import org.project.project.model.entity.RepositorioInvitacion;
import org.project.project.model.entity.Repositorio;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioInvitacionRepository extends JpaRepository<RepositorioInvitacion, Long> {
    
    Optional<RepositorioInvitacion> findByToken(String token);
    
    List<RepositorioInvitacion> findByUsuarioInvitadoAndEstado(Usuario usuario, RepositorioInvitacion.EstadoInvitacion estado);
    
    List<RepositorioInvitacion> findByRepositorio(Repositorio repositorio);
    
    boolean existsByUsuarioInvitadoAndRepositorioAndEstado(Usuario usuario, Repositorio repositorio, RepositorioInvitacion.EstadoInvitacion estado);
}
