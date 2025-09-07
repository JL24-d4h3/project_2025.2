package org.project.project.repository;

import org.project.project.model.entity.Adjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdjuntoRepository extends JpaRepository<Adjunto, Integer> {
}
