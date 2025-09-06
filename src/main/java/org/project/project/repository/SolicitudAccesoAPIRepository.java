package org.project.project.repository;

import org.project.project.model.entity.SolicitudAccesoAPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudAccesoAPIRepository extends JpaRepository<SolicitudAccesoAPI, Integer> {
}
