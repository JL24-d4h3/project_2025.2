package org.project.project.repository;

import org.project.project.model.dto.DocumentationSimpleDTO;
import org.project.project.model.entity.Documentacion;
import org.project.project.model.entity.Contenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentacionRepository extends JpaRepository<Documentacion, Long> {
    
    /**
     * Busca documentaciones para Select2 AJAX
     * Retorna DTOs ligeros con: documentacionId, nombreApi, seccionDocumentacion
     * Búsqueda case-insensitive en nombre de API y sección
     * Limitado a 50 resultados para performance
     */
    @Query("SELECT new org.project.project.model.dto.DocumentationSimpleDTO(" +
           "d.documentacionId, a.nombreApi, d.seccionDocumentacion) " +
           "FROM Documentacion d JOIN d.api a " +
           "WHERE LOWER(a.nombreApi) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(d.seccionDocumentacion) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY a.nombreApi, d.seccionDocumentacion")
    List<DocumentationSimpleDTO> searchDocumentationsForSelect2(@Param("searchTerm") String searchTerm);
    
    /**
     * Busca todos los Contenidos de una Documentacion específica.
     * Ordenados por campo orden ASC.
     * 
     * @param documentacionId ID de la Documentacion
     * @return Lista de Contenidos ordenados
     */
    @Query("SELECT c FROM Contenido c WHERE c.documentacion.documentacionId = :documentacionId ORDER BY c.orden ASC")
    List<Contenido> findContenidosByDocumentacionId(@Param("documentacionId") Long documentacionId);
}
