package org.project.project.service;

import org.project.project.model.entity.Usuario;
import org.project.project.repository.UsuarioRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    public Usuario guardarUsuario(Usuario usuario) {
        usuario.setFechaCreacion(LocalDateTime.now());
        // En una aplicación real, aquí se debería codificar la contraseña antes de guardarla
        return usuarioRepository.save(usuario);
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

    public void eliminarUsuario(Long id) {
        Usuario usuario = buscarUsuarioPorId(id);
        usuarioRepository.delete(usuario);
    }
}
