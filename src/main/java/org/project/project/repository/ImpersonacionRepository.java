package org.project.project.repository;

import org.project.project.model.entity.Impersonacion;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImpersonacionRepository extends JpaRepository<Impersonacion, Long> {
    
    @Query("SELECT i FROM Impersonacion i WHERE i.usuario.usuarioId = :usuarioId AND i.fechaFinImpersonacion IS NULL")
    Optional<Impersonacion> findActiveByUserId(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT i FROM Impersonacion i WHERE i.fechaFinImpersonacion IS NULL")
    List<Impersonacion> findAllActive();
    
    @Query("SELECT i FROM Impersonacion i WHERE i.usuario = :usuario ORDER BY i.fechaInicioImpersonacion DESC")
    List<Impersonacion> findByUser(@Param("usuario") Usuario usuario);
}
