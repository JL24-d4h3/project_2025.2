package org.project.project.repository;

import org.project.project.model.entity.Clasificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClasificacionRepository extends JpaRepository<Clasificacion, Long> {
    
    /**
     * Busca una clasificación por su tipo de contenido.
     * Usado para asignar clasificación a secciones CMS.
     * 
     * @param tipoContenidoTexto Tipo de contenido (GUIA, TUTORIAL, VIDEO, SNIPPET, OTRO)
     * @return Optional con la clasificación encontrada
     */
    Optional<Clasificacion> findByTipoContenidoTexto(String tipoContenidoTexto);
}
