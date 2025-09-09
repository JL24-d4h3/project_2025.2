package org.project.project.service;

import org.project.project.model.entity.Usuario;
import org.project.project.repository.UsuarioRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    public Usuario guardarUsuario(Usuario usuario) {
        // La contraseña debe ser codificada ANTES de llamar a este método.
        usuario.setFechaCreacion(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    public void updatePassword(String usernameOrEmail, String newPassword) {
        Usuario usuario = findByUsernameOrEmail(usernameOrEmail);
        usuario.setHashedPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }

    public Usuario actualizarUsuario(Long id, Usuario usuarioDetails) {
        Usuario usuario = buscarUsuarioPorId(id);
        usuario.setNombreUsuario(usuarioDetails.getNombreUsuario());
        usuario.setApellidoPaterno(usuarioDetails.getApellidoPaterno());
        usuario.setApellidoMaterno(usuarioDetails.getApellidoMaterno());
        usuario.setDni(usuarioDetails.getDni());
        usuario.setFechaNacimiento(usuarioDetails.getFechaNacimiento());
        usuario.setSexoUsuario(usuarioDetails.getSexoUsuario());
        usuario.setEstadoCivil(usuarioDetails.getEstadoCivil());
        usuario.setTelefono(usuarioDetails.getTelefono());
        usuario.setCorreo(usuarioDetails.getCorreo());
        usuario.setDireccionUsuario(usuarioDetails.getDireccionUsuario());
        usuario.setUsername(usuarioDetails.getUsername());
        usuario.setFotoPerfil(usuarioDetails.getFotoPerfil());
        usuario.setEstadoUsuario(usuarioDetails.getEstadoUsuario());
        usuario.setActividadUsuario(usuarioDetails.getActividadUsuario());
        usuario.setCodigoUsuario(usuarioDetails.getCodigoUsuario());
        usuario.setAccesoUsuario(usuarioDetails.getAccesoUsuario());
        // La contraseña (hashedPassword) no se actualiza por esta vía por seguridad
        return usuarioRepository.save(usuario);
    }

    public Usuario eliminarUsuario(Long id) {
        Usuario usuario = buscarUsuarioPorId(id);
        usuarioRepository.delete(usuario);
        return usuario;
    }

    public Usuario findByUsernameOrEmail(String usernameOrEmail) {
        return usuarioRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> usuarioRepository.findByCorreo(usernameOrEmail)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usernameOrEmail)));
    }

    public boolean existsByUsernameOrEmail(String username, String email) {
        return usuarioRepository.findByUsername(username).isPresent() ||
               usuarioRepository.findByCorreo(email).isPresent();
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }
}
