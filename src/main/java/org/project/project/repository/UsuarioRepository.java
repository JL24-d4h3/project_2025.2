package org.project.project.repository;

import org.project.project.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByProveedorAndIdProveedor(String proveedor, String idProveedor);
    boolean existsByUsername(String username);
    boolean existsByCodigoUsuario(String codigoUsuario);
}