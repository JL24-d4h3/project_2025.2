package org.project.project.repository;

import org.project.project.model.entity.Payload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayloadRepository extends JpaRepository<Payload, Integer> {
}
