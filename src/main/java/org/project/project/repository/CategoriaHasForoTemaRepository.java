package org.project.project.repository;

import org.project.project.model.entity.CategoriaHasForoTema;
import org.project.project.model.entity.CategoriaHasForoTemaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaHasForoTemaRepository extends JpaRepository<CategoriaHasForoTema, CategoriaHasForoTemaId> {

    // =================== BÚSQUEDAS POR CATEGORÍA ===================

    List<CategoriaHasForoTema> findByCategoria_IdCategoria(Long categoriaId);

    @Query("SELECT c FROM CategoriaHasForoTema c WHERE c.categoria.idCategoria = :categoriaId ORDER BY c.fechaAsignacion DESC")
    List<CategoriaHasForoTema> findByCategoriaIdOrdered(@Param("categoriaId") Long categoriaId);

    // =================== BÚSQUEDAS POR TEMA ===================

    List<CategoriaHasForoTema> findByForoTema_TemaId(Long temaId);

    // =================== VERIFICAR EXISTENCIA ===================

    boolean existsByCategoria_IdCategoriaAndForoTema_TemaId(Long categoriaId, Long temaId);

    // =================== CONTAR ===================

    long countByCategoria_IdCategoria(Long categoriaId);

    long countByForoTema_TemaId(Long temaId);

    // =================== ELIMINAR RELACIÓN ===================

    void deleteByCategoria_IdCategoriaAndForoTema_TemaId(Long categoriaId, Long temaId);
}

