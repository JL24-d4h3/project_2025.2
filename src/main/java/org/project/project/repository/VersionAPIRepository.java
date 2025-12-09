package org.project.project.repository;

import org.project.project.model.entity.VersionAPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionAPIRepository extends JpaRepository<VersionAPI, Long> {
    
    /**
     * Obtiene los IDs únicos de APIs donde el usuario ha creado al menos una versión
     * Usado para determinar qué feedbacks debe recibir un usuario
     */
    @Query("SELECT DISTINCT v.api.apiId FROM VersionAPI v WHERE v.creadoPorUsuarioId = :userId")
    List<Long> findDistinctApiIdsByCreatorId(@Param("userId") Long userId);
}
