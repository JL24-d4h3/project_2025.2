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
        System.out.println("🔐 [CustomUserDetailsService] Intentando cargar usuario: " + usernameOrEmail);

        // Buscar por username o email
        Usuario usuario = usuarioRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> {
                    System.out.println("🔍 [CustomUserDetailsService] No encontrado por username, buscando por email: " + usernameOrEmail);
                    return usuarioRepository.findByCorreo(usernameOrEmail)
                            .orElseThrow(() -> {
                                System.out.println("❌ [CustomUserDetailsService] Usuario no encontrado: " + usernameOrEmail);
                                return new UsernameNotFoundException("Usuario no encontrado: " + usernameOrEmail);
                            });
                });

        System.out.println("✅ [CustomUserDetailsService] Usuario encontrado: " + usuario.getUsername() +
                ", Estado: " + usuario.getEstadoUsuario() +
                ", Hash: " + usuario.getHashedPassword().substring(0, 10) + "...");

        // Verificar si el usuario está habilitado
        if (usuario.getEstadoUsuario() != Usuario.EstadoUsuario.HABILITADO) {
            System.out.println("❌ [CustomUserDetailsService] Usuario deshabilitado: " + usernameOrEmail + ", Estado: " + usuario.getEstadoUsuario());
            throw new UsernameNotFoundException("Usuario deshabilitado: " + usernameOrEmail);
        }

        // REMOVIDO: Actualización de última conexión para evitar problemas de solo lectura
        // Esta actualización se hará en el LoginSuccessHandler en su lugar

        // Convertir roles a autoridades de Spring Security
        List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> {
                    String authority = "ROLE_" + rol.getNombreRol().toString();
                    System.out.println("🔑 [CustomUserDetailsService] Asignando autoridad: " + authority);
                    return new SimpleGrantedAuthority(authority);
                })
                .collect(Collectors.toList());

        System.out.println("🏁 [CustomUserDetailsService] Autoridades asignadas: " + authorities);

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