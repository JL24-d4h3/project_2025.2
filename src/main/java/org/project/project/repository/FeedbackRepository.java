package org.project.project.repository;

import org.project.project.model.entity.Feedback;
import org.project.project.model.entity.Documentacion;
import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // ========== EXISTING METHODS ==========
    
    /**
     * Elimina todos los feedbacks asociados a un usuario específico
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Feedback f WHERE f.usuario.usuarioId = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);
    
    /**
     * Elimina todos los feedbacks asociados a un usuario
     */
    @Modifying
    @Transactional
    void deleteByUsuario(Usuario usuario);
    
    // ========== NEW QUERIES ==========
    
    /**
     * Busca todos los feedbacks creados POR un usuario específico
     * Ordenados por fecha descendente (más reciente primero)
     */
    @Query("SELECT f FROM Feedback f WHERE f.usuario.usuarioId = :userId ORDER BY f.fechaFeedback DESC")
    List<Feedback> findByUserIdOrderByDateDesc(@Param("userId") Long userId);
    
    /**
     * Busca todos los feedbacks PARA documentaciones creadas por el usuario
     * (Feedbacks recibidos: usuarios dejaron feedback en MIS documentaciones)
     */
    @Query("SELECT f FROM Feedback f " +
           "WHERE f.documentacion.creadoPor.usuarioId = :userId " +
           "AND f.usuario.usuarioId != :userId " +
           "ORDER BY f.fechaFeedback DESC")
    List<Feedback> findReceivedFeedbacksByUserId(@Param("userId") Long userId);
    
    /**
     * Busca feedbacks por ID de documentación
     */
    List<Feedback> findByDocumentacion_DocumentacionId(Long documentacionId);
    
    /**
     * Busca feedbacks por documentación (entidad completa)
     */
    List<Feedback> findByDocumentacionIn(List<Documentacion> documentaciones);
    
    /**
     * Busca feedbacks por estado
     */
//    List<Feedback> findByEstadoFeedback(Feedback.EstadoFeedback estado);
//
//    /**
//     * Busca feedbacks por tipo
//     */
//    List<Feedback> findByTipoFeedback(Feedback.TipoFeedback tipo);
    
    /**
     * Cuenta el total de feedbacks creados por un usuario
     */
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.usuario.usuarioId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    /**
     * Cuenta feedbacks por estado para un usuario específico
     */
//    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.usuario.usuarioId = :userId AND f.estadoFeedback = :status")
//    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Feedback.EstadoFeedback status);
//
    /**
     * Obtiene la calificación promedio para una documentación específica
     */
    @Query("SELECT AVG(f.puntuacion) FROM Feedback f WHERE f.documentacion.documentacionId = :docId")
    BigDecimal getAverageRatingByDocumentationId(@Param("docId") Long docId);
    
    /**
     * Obtiene la calificación promedio de todos los feedbacks creados por un usuario
     */
    @Query("SELECT AVG(f.puntuacion) FROM Feedback f WHERE f.usuario.usuarioId = :userId")
    BigDecimal getAverageRatingByUserId(@Param("userId") Long userId);
    
    // ========== DOCUMENTATION SEARCH FOR SELECT2 ==========
    
    /**
     * Busca documentaciones para Select2 AJAX
     * Retorna DTOs ligeros con: documentacionId, nombreApi, seccionDocumentacion
     * Búsqueda case-insensitive en nombre de API y sección
     */
    @Query("SELECT new org.project.project.model.dto.DocumentationSimpleDTO(" +
           "d.documentacionId, a.nombreApi, d.seccionDocumentacion) " +
           "FROM Documentacion d JOIN d.api a " +
           "WHERE LOWER(a.nombreApi) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(d.seccionDocumentacion) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY a.nombreApi, d.seccionDocumentacion")
    List<org.project.project.model.dto.DocumentationSimpleDTO> searchDocumentationsForSelect2(@Param("searchTerm") String searchTerm);
    
    /**
     * Obtiene las primeras documentaciones ordenadas alfabéticamente
     * Usado para mostrar listado inicial en Select2 cuando no hay término de búsqueda
     */
    @Query(value = "SELECT new org.project.project.model.dto.DocumentationSimpleDTO(" +
           "d.documentacionId, a.nombreApi, d.seccionDocumentacion) " +
           "FROM Documentacion d JOIN d.api a " +
           "ORDER BY a.nombreApi, d.seccionDocumentacion")
    List<org.project.project.model.dto.DocumentationSimpleDTO> findTop10DocumentationsForSelect2();
    
    /**
     * Cuenta feedbacks recibidos pendientes para un usuario
     * (Para documentaciones de sus APIs)
     */
//    @Query("SELECT COUNT(f) FROM Feedback f " +
//           "WHERE f.documentacion.api.apiId IN " +
//           "(SELECT DISTINCT v.api.apiId FROM VersionAPI v WHERE v.creadoPorUsuarioId = :userId) " +
//           "AND f.estadoFeedback = :status")
//    Long countReceivedFeedbacksByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Feedback.EstadoFeedback status);
}
