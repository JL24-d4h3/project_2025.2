package org.project.project.repository;

import org.project.project.model.entity.VersionAPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VersionAPIRepository extends JpaRepository<VersionAPI, Integer> {
}
