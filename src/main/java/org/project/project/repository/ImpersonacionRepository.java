package org.project.project.repository;

import org.project.project.model.entity.Impersonacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImpersonacionRepository extends JpaRepository<Impersonacion, Integer> {
}
