package org.project.project.service;

import org.project.project.model.entity.Impersonacion;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.ImpersonacionRepository;
import org.project.project.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ImpersonacionService {

    @Autowired
    private ImpersonacionRepository impersonacionRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Inicia la impersonación de un usuario
     */
    public Impersonacion iniciarImpersonacion(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuarioId));
        
        // Verificar si ya hay una impersonación activa para este usuario
        Optional<Impersonacion> impersonacionActiva = impersonacionRepository.findActiveByUserId(usuarioId);
        if (impersonacionActiva.isPresent()) {
            throw new RuntimeException("Ya existe una impersonación activa para este usuario");
        }
        
        // Crear nueva impersonación
        Impersonacion impersonacion = new Impersonacion();
        impersonacion.setUsuario(usuario);
        impersonacion.setFechaInicioImpersonacion(LocalDateTime.now());
        
        impersonacion = impersonacionRepository.save(impersonacion);
        
        return impersonacion;
    }

    /**
     * Finaliza la impersonación activa y restaura la sesión original
     */
    public void finalizarImpersonacion(Long usuarioId) {
        Optional<Impersonacion> impersonacionOpt = impersonacionRepository.findActiveByUserId(usuarioId);
        
        if (impersonacionOpt.isPresent()) {
            Impersonacion impersonacion = impersonacionOpt.get();
            impersonacion.setFechaFinImpersonacion(LocalDateTime.now());
            impersonacionRepository.save(impersonacion);
        }
        
        // El contexto de seguridad se restaurará en el controlador
    }

    /**
     * Obtiene todas las impersonaciones activas
     */
    public List<Impersonacion> obtenerImpersonacionesActivas() {
        return impersonacionRepository.findAllActive();
    }

    /**
     * Verifica si hay una impersonación activa para un usuario
     */
    public boolean tieneImpersonacionActiva(Long usuarioId) {
        return impersonacionRepository.findActiveByUserId(usuarioId).isPresent();
    }

    /**
     * Obtiene la impersonación activa para un usuario
     */
    public Optional<Impersonacion> obtenerImpersonacionActiva(Long usuarioId) {
        return impersonacionRepository.findActiveByUserId(usuarioId);
    }

    /**
     * Cambia el contexto de seguridad para impersonar a un usuario
     */
    private void impersonarUsuario(Usuario usuario) {
        // Crear las autoridades del usuario basadas en sus roles
        List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
            .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombreRol().name()))
            .collect(Collectors.toList());

        // Crear UserDetails para el usuario impersonado
        UserDetails userDetails = User.builder()
            .username(usuario.getUsername())
            .password(usuario.getHashedPassword())
            .authorities(authorities)
            .build();

        // Crear nueva autenticación
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
            userDetails, null, authorities);

        // Establecer en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    /**
     * Obtiene el historial de impersonaciones de un usuario
     */
    public List<Impersonacion> obtenerHistorialImpersonaciones(Usuario usuario) {
        return impersonacionRepository.findByUser(usuario);
    }
}