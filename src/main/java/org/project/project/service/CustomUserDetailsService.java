package org.project.project.service;

import org.project.project.model.entity.Usuario;
import org.project.project.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Buscar por username o email
        Usuario usuario = usuarioRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> usuarioRepository.findByCorreo(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + usernameOrEmail)));

        // Verificar si el usuario está habilitado
        if (usuario.getEstadoUsuario() != Usuario.EstadoUsuario.HABILITADO) {
            throw new UsernameNotFoundException("Usuario deshabilitado: " + usernameOrEmail);
        }

        // Actualizar última conexión
        // Esta operación de escritura requiere que la transacción no sea readOnly.
        usuario.setUltimaConexion(LocalDateTime.now());
        usuario.setActividadUsuario(Usuario.ActividadUsuario.ACTIVO);
        usuarioRepository.save(usuario);

        // Convertir roles a autoridades de Spring Security
        // La anotación @Transactional asegura que la sesión sigue abierta aquí.
        List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombreRol()))
                .collect(Collectors.toList());

        // Crear y retornar UserDetails
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getHashedPassword())
                .disabled(false)
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(false)
                .authorities(authorities)
                .build();
    }
}