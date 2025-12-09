package org.project.project.service;

import org.project.project.exception.ResourceNotFoundException;
import org.project.project.model.entity.RepositorioRama;
import org.project.project.repository.RepositorioRamaRepository;
import org.project.project.repository.RepositorioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar las ramas (branches) de repositorios.
 * Implementa lógica de negocio para creación, actualización y consulta de ramas.
 */
@Service
@Transactional
public class RepositorioRamaService {

    private static final Logger logger = LoggerFactory.getLogger(RepositorioRamaService.class);

    @Autowired
    private RepositorioRamaRepository repositorioRamaRepository;

    @Autowired
    private RepositorioRepository repositorioRepository;

    // =================== CONSULTAS ===================

    /**
     * Obtiene todas las ramas de un repositorio
     */
    public List<RepositorioRama> findByRepositorioId(Long repositorioId) {
        return repositorioRamaRepository.findByRepositorioId(repositorioId);
    }

    /**
     * Busca una rama específica por repositorio y nombre
     */
    public Optional<RepositorioRama> findByRepositorioIdAndNombre(Long repositorioId, String nombreRama) {
        return repositorioRamaRepository.findByRepositorioIdAndNombreRama(repositorioId, nombreRama);
    }

    /**
     * Obtiene la rama especificada o la rama principal si no se especifica
     * @param repositorioId ID del repositorio
     * @param nombreRama Nombre de la rama (puede ser null)
     * @return La rama encontrada
     * @throws ResourceNotFoundException si no se encuentra la rama
     */
    public RepositorioRama obtenerRamaOPrincipal(Long repositorioId, String nombreRama) {
        if (nombreRama != null && !nombreRama.isEmpty()) {
            return findByRepositorioIdAndNombre(repositorioId, nombreRama)
                    .orElseThrow(() -> new ResourceNotFoundException("Rama '" + nombreRama + "' no encontrada en repositorio R-" + repositorioId));
        } else {
            return obtenerRamaPrincipal(repositorioId);
        }
    }

    /**
     * Obtiene la rama principal de un repositorio
     */
    public RepositorioRama obtenerRamaPrincipal(Long repositorioId) {
        return repositorioRamaRepository.findPrincipalByRepositorioId(repositorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Rama principal no encontrada para repositorio R-" + repositorioId));
    }

    /**
     * Verifica si existe una rama con ese nombre en el repositorio
     */
    public boolean existeRama(Long repositorioId, String nombreRama) {
        return repositorioRamaRepository.existsByRepositorioIdAndNombreRama(repositorioId, nombreRama);
    }

    /**
     * Cuenta cuántas ramas tiene un repositorio
     */
    public long contarRamas(Long repositorioId) {
        return repositorioRamaRepository.countByRepositorioId(repositorioId);
    }

    // =================== CREACIÓN Y MODIFICACIÓN ===================

    /**
     * Crea una nueva rama en un repositorio
     * @param repositorioId ID del repositorio
     * @param nombreRama Nombre de la nueva rama
     * @param descripcion Descripción opcional
     * @param usuarioId ID del usuario que crea la rama
     * @param marcarComoPrincipal Si debe ser la nueva rama principal
     * @return La rama creada
     */
    public RepositorioRama crearRama(Long repositorioId, String nombreRama, String descripcion, 
                                     Long usuarioId, boolean marcarComoPrincipal) {
        
        logger.info("🌿 Creando rama '{}' en repositorio R-{}", nombreRama, repositorioId);

        // Validar que el repositorio existe
        if (!repositorioRepository.existsById(repositorioId)) {
            throw new ResourceNotFoundException("Repositorio R-" + repositorioId + " no encontrado");
        }

        // Validar que no existe una rama con ese nombre
        if (existeRama(repositorioId, nombreRama)) {
            throw new IllegalArgumentException("Ya existe una rama con el nombre '" + nombreRama + "' en este repositorio");
        }

        // Validar nombre de rama (solo alfanuméricos, guiones y guiones bajos)
        if (!nombreRama.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("El nombre de la rama solo puede contener letras, números, guiones y guiones bajos");
        }

        // Crear la nueva rama
        RepositorioRama nuevaRama = new RepositorioRama();
        nuevaRama.setRepositorioId(repositorioId);
        nuevaRama.setNombreRama(nombreRama);
        nuevaRama.setDescripcionRama(descripcion);
        nuevaRama.setIsPrincipal(marcarComoPrincipal);
        nuevaRama.setIsProtegida(marcarComoPrincipal); // La rama principal siempre es protegida
        nuevaRama.setCreadaPorUsuarioId(usuarioId);
        nuevaRama.setFechaCreacion(LocalDateTime.now());

        RepositorioRama ramaGuardada = repositorioRamaRepository.save(nuevaRama);
        
        logger.info("✅ Rama '{}' creada exitosamente con ID {}", nombreRama, ramaGuardada.getRamaId());
        
        return ramaGuardada;
    }

    /**
     * Cambia la rama principal de un repositorio
     */
    public void cambiarRamaPrincipal(Long repositorioId, String nombreRama, Long usuarioId) {
        
        logger.info("🔄 Cambiando rama principal de R-{} a '{}'", repositorioId, nombreRama);

        RepositorioRama nuevaRamaPrincipal = findByRepositorioIdAndNombre(repositorioId, nombreRama)
                .orElseThrow(() -> new ResourceNotFoundException("Rama '" + nombreRama + "' no encontrada"));

        if (nuevaRamaPrincipal.esPrincipal()) {
            throw new IllegalArgumentException("La rama '" + nombreRama + "' ya es la rama principal");
        }

        // Marcar como principal (el trigger se encarga de desmarcar las demás)
        nuevaRamaPrincipal.setIsPrincipal(true);
        nuevaRamaPrincipal.setIsProtegida(true); // La rama principal siempre está protegida
        nuevaRamaPrincipal.setActualizadaEn(LocalDateTime.now());

        repositorioRamaRepository.save(nuevaRamaPrincipal);
        
        logger.info("✅ Rama principal cambiada a '{}'", nombreRama);
    }

    /**
     * Actualiza información del último commit de una rama
     */
    public void actualizarUltimoCommit(Long ramaId, String commitHash, String mensaje, String autor) {
        
        RepositorioRama rama = repositorioRamaRepository.findById(ramaId)
                .orElseThrow(() -> new ResourceNotFoundException("Rama con ID " + ramaId + " no encontrada"));

        rama.setUltimoCommitHash(commitHash);
        rama.setUltimoCommitFecha(LocalDateTime.now());
        rama.setUltimoCommitMensaje(mensaje);
        rama.setUltimoCommitAutor(autor);
        rama.setActualizadaEn(LocalDateTime.now());

        repositorioRamaRepository.save(rama);
        
        logger.info("📝 Commit actualizado en rama '{}': {}", rama.getNombreRama(), commitHash);
    }

    /**
     * Marca/desmarca una rama como protegida
     */
    public void cambiarProteccion(Long ramaId, boolean protegida, Long usuarioId) {
        
        RepositorioRama rama = repositorioRamaRepository.findById(ramaId)
                .orElseThrow(() -> new ResourceNotFoundException("Rama con ID " + ramaId + " no encontrada"));

        if (rama.esPrincipal() && !protegida) {
            throw new IllegalArgumentException("La rama principal no puede dejar de estar protegida");
        }

        rama.setIsProtegida(protegida);
        rama.setActualizadaEn(LocalDateTime.now());

        repositorioRamaRepository.save(rama);
        
        logger.info("🔒 Rama '{}' {} protegida", rama.getNombreRama(), protegida ? "marcada como" : "desmarcada como");
    }

    /**
     * Renombra una rama
     */
    public void renombrarRama(Long ramaId, String nuevoNombre, Long usuarioId) {
        
        RepositorioRama rama = repositorioRamaRepository.findById(ramaId)
                .orElseThrow(() -> new ResourceNotFoundException("Rama con ID " + ramaId + " no encontrada"));

        String nombreAntiguo = rama.getNombreRama();

        // Validar que no existe otra rama con ese nombre
        if (existeRama(rama.getRepositorioId(), nuevoNombre)) {
            throw new IllegalArgumentException("Ya existe una rama con el nombre '" + nuevoNombre + "' en este repositorio");
        }

        // Validar nombre
        if (!nuevoNombre.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("El nombre de la rama solo puede contener letras, números, guiones y guiones bajos");
        }

        rama.setNombreRama(nuevoNombre);
        rama.setActualizadaEn(LocalDateTime.now());

        repositorioRamaRepository.save(rama);
        
        logger.info("📝 Rama '{}' renombrada a '{}'", nombreAntiguo, nuevoNombre);
    }

    // =================== ELIMINACIÓN ===================

    /**
     * Elimina una rama (solo si no es principal ni protegida)
     */
    public void eliminarRama(Long ramaId, Long usuarioId) {
        
        RepositorioRama rama = repositorioRamaRepository.findById(ramaId)
                .orElseThrow(() -> new ResourceNotFoundException("Rama con ID " + ramaId + " no encontrada"));

        if (rama.esPrincipal()) {
            throw new IllegalArgumentException("No se puede eliminar la rama principal. Primero cambia la rama principal a otra rama.");
        }

        if (rama.estaProtegida()) {
            throw new IllegalArgumentException("No se puede eliminar una rama protegida. Primero desprotégela.");
        }

        String nombreRama = rama.getNombreRama();
        Long repositorioId = rama.getRepositorioId();

        repositorioRamaRepository.delete(rama);
        
        logger.warn("🗑️ Rama '{}' eliminada de repositorio R-{} por usuario {}", nombreRama, repositorioId, usuarioId);
    }

    /**
     * Elimina todas las ramas de un repositorio (usado al eliminar el repositorio)
     */
    @Transactional
    public void eliminarTodasLasRamas(Long repositorioId) {
        
        long cantidadEliminada = contarRamas(repositorioId);
        repositorioRamaRepository.deleteByRepositorioId(repositorioId);
        
        logger.warn("🗑️ {} ramas eliminadas de repositorio R-{}", cantidadEliminada, repositorioId);
    }

    // =================== BÚSQUEDA ===================

    /**
     * Busca ramas por nombre parcial
     */
    public List<RepositorioRama> buscarPorNombre(Long repositorioId, String nombreParcial) {
        return repositorioRamaRepository.searchByNombreRama(repositorioId, nombreParcial);
    }

    /**
     * Obtiene todas las ramas protegidas de un repositorio
     */
    public List<RepositorioRama> obtenerRamasProtegidas(Long repositorioId) {
        return repositorioRamaRepository.findByRepositorioIdAndIsProtegidaTrue(repositorioId);
    }
}
