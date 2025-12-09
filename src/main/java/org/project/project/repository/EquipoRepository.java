package org.project.project.repository;

import org.project.project.model.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    @Query(value = "SELECT COUNT(e.equipo_id) FROM usuario_has_equipo uhe INNER JOIN equipo e ON uhe.equipo_equipo_id = e.equipo_id WHERE uhe.usuario_usuario_id = :usuarioId", nativeQuery = true)
    int countTeamsByUserId(@Param("usuarioId") Long usuarioId);

    @Query(value = "SELECT usuario.nombre_usuario, usuario.apellido_paterno, usuario.correo FROM equipo INNER JOIN usuario_has_equipo ON equipo.equipo_id = usuario_has_equipo.equipo_equipo_id INNER JOIN usuario ON usuario_has_equipo.usuario_usuario_id = usuario.usuario_id WHERE equipo.equipo_id = :equipoId", nativeQuery = true)
    java.util.List<Object[]> findUsersByTeamId(@Param("equipoId") Long equipoId);

    @Query(value = "SELECT u.nombre_usuario, u.apellido_paterno, e.nombre_equipo, u.codigo_usuario, u.correo FROM usuario AS u INNER JOIN usuario_has_equipo AS ue ON u.usuario_id = ue.usuario_usuario_id INNER JOIN equipo AS e ON ue.equipo_equipo_id = e.equipo_id WHERE e.equipo_id IN (SELECT equipo_equipo_id FROM usuario_has_equipo WHERE usuario_usuario_id = :usuarioId)", nativeQuery = true)
    java.util.List<Object[]> findUsersFromUserTeams(@Param("usuarioId") Long usuarioId);

    @Query(value = "SELECT e.equipo_id, e.nombre_equipo FROM equipo AS e INNER JOIN usuario_has_equipo AS ue ON e.equipo_id = ue.equipo_equipo_id WHERE ue.usuario_usuario_id = :usuarioId", nativeQuery = true)
    java.util.List<Object[]> findTeamsByUserId(@Param("usuarioId") Long usuarioId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "INSERT INTO usuario_has_equipo (usuario_usuario_id, equipo_equipo_id) VALUES (:usuarioId, :equipoId)", nativeQuery = true)
    void inviteUserToTeam(@Param("usuarioId") Long usuarioId, @Param("equipoId") Long equipoId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "INSERT INTO equipo (nombre_equipo) VALUES (:nombreEquipo)", nativeQuery = true)
    void createTeam(@Param("nombreEquipo") String nombreEquipo);
}
