package org.project.project.repository;

import org.project.project.model.entity.Enlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnlaceRepository extends JpaRepository<Enlace, Long> {
    
    /**
     * Busca todos los enlaces vinculados a un contexto específico.
     * Usado para obtener enlaces de un Contenido o Documentacion.
     * 
     * @param contextoType Tipo de contexto (CONTENIDO, DOCUMENTACION, etc.)
     * @param contextoId ID del contexto
     * @return Lista de enlaces vinculados
     */
    List<Enlace> findByContextoTypeAndContextoId(Enlace.ContextoType contextoType, Long contextoId);
}
