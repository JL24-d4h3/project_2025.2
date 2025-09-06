package org.project.project.repository;

import org.project.project.model.entity.Enlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnlaceRepository extends JpaRepository<Enlace, Integer> {
}
