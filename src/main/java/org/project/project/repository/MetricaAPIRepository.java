package org.project.project.repository;

import org.project.project.model.entity.MetricaAPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricaAPIRepository extends JpaRepository<MetricaAPI, Integer> {
}
