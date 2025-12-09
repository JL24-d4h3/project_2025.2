package org.project.project.repository;

import org.project.project.model.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca categorías por nombre (case insensitive)
     */
    List<Categoria> findByNombreCategoriaContainingIgnoreCase(String nombre);

    /**
     * Busca una categoría por nombre exacto
     */
    Optional<Categoria> findByNombreCategoria(String nombreCategoria);

    /**
     * Verifica si existe una categoría con el nombre dado
     */
    boolean existsByNombreCategoria(String nombreCategoria);

    /**
     * Obtiene las categorías ordenadas por nombre
     */
    List<Categoria> findAllByOrderByNombreCategoriaAsc();

    /**
     * Cuenta categorías por descripción que contenga el texto
     */
    @Query("SELECT COUNT(c) FROM Categoria c WHERE c.descripcionCategoria LIKE %:texto%")
    long countByDescripcionContaining(String texto);

    /**
     * Busca categorías por sección
     */
    List<Categoria> findBySeccionCategoria(Categoria.SeccionCategoria seccionCategoria);

    /**
     * Obtiene todas las categorías de una sección específica ordenadas por nombre
     */
    List<Categoria> findBySeccionCategoriaOrderByNombreCategoriaAsc(Categoria.SeccionCategoria seccionCategoria);

    /**
     * Cuenta categorías por sección
     */
    long countBySeccionCategoria(Categoria.SeccionCategoria seccionCategoria);

    /**
     * Obtiene categorías sin sección asignada
     */
    List<Categoria> findBySeccionCategoriaIsNull();

    /**
     * Cuenta categorías sin sección asignada
     */
    long countBySeccionCategoriaIsNull();
}