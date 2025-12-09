package org.project.project.repository;

import org.project.project.model.entity.CategoriaHasApi;
import org.project.project.model.entity.CategoriaHasApiId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaHasApiRepository extends JpaRepository<CategoriaHasApi, CategoriaHasApiId> {

    long countByCategory_CategoryId(Long idCategoria);
}
