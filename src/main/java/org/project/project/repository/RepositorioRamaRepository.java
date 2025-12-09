package org.project.project.repository;

import org.project.project.model.entity.RepositorioRama;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad RepositorioRama.
 * Gestiona las operaciones CRUD y consultas para las ramas de repositorios.
 */
@Repository
public interface RepositorioRamaRepository extends JpaRepository<RepositorioRama, Long> {

    /**
     * Busca todas las ramas de un repositorio específico
     * @param repositorioId ID del repositorio
     * @return Lista de ramas ordenadas por: principal primero, luego por nombre
     */
    @Query("SELECT rr FROM RepositorioRama rr WHERE rr.repositorioId = :repositorioId ORDER BY rr.isPrincipal DESC, rr.nombreRama ASC")
    List<RepositorioRama> findByRepositorioId(@Param("repositorioId") Long repositorioId);

    /**
     * Busca una rama específica por repositorio y nombre
     * @param repositorioId ID del repositorio
     * @param nombreRama Nombre de la rama (ej: "main", "develop")
     * @return Optional con la rama si existe
     */
    Optional<RepositorioRama> findByRepositorioIdAndNombreRama(Long repositorioId, String nombreRama);

    /**
     * Busca la rama principal de un repositorio
     * @param repositorioId ID del repositorio
     * @return Optional con la rama principal
     */
    @Query("SELECT rr FROM RepositorioRama rr WHERE rr.repositorioId = :repositorioId AND rr.isPrincipal = true")
    Optional<RepositorioRama> findPrincipalByRepositorioId(@Param("repositorioId") Long repositorioId);

    /**
     * Verifica si existe una rama con un nombre específico en un repositorio
     * @param repositorioId ID del repositorio
     * @param nombreRama Nombre de la rama
     * @return true si existe, false si no
     */
    boolean existsByRepositorioIdAndNombreRama(Long repositorioId, String nombreRama);

    /**
     * Cuenta cuántas ramas tiene un repositorio
     * @param repositorioId ID del repositorio
     * @return Número de ramas
     */
    long countByRepositorioId(Long repositorioId);

    /**
     * Busca todas las ramas protegidas de un repositorio
     * @param repositorioId ID del repositorio
     * @return Lista de ramas protegidas
     */
    List<RepositorioRama> findByRepositorioIdAndIsProtegidaTrue(Long repositorioId);

    /**
     * Busca todas las ramas creadas por un usuario específico
     * @param usuarioId ID del usuario creador
     * @return Lista de ramas creadas por el usuario
     */
    List<RepositorioRama> findByCreadaPorUsuarioId(Long usuarioId);

    /**
     * Busca ramas por nombre parcial (búsqueda con LIKE)
     * @param repositorioId ID del repositorio
     * @param nombreParcial Parte del nombre de la rama
     * @return Lista de ramas que coinciden
     */
    @Query("SELECT rr FROM RepositorioRama rr WHERE rr.repositorioId = :repositorioId AND rr.nombreRama LIKE %:nombreParcial%")
    List<RepositorioRama> searchByNombreRama(@Param("repositorioId") Long repositorioId, @Param("nombreParcial") String nombreParcial);

    /**
     * Elimina todas las ramas de un repositorio (usado cuando se elimina el repositorio)
     * @param repositorioId ID del repositorio
     */
    void deleteByRepositorioId(Long repositorioId);

    /**
     * Busca todas las ramas que contienen commits de un autor específico
     * @param autor Nombre del autor del commit
     * @return Lista de ramas
     */
    List<RepositorioRama> findByUltimoCommitAutorContainingIgnoreCase(String autor);
}
