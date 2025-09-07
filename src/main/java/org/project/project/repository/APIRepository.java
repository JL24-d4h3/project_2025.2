package org.project.project.repository;

import org.project.project.model.entity.API;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface APIRepository extends JpaRepository<API, Integer> {
}
