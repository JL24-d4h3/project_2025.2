package org.project.project.service;

import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.NodoShareLink;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.NodoShareLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service para gestión de enlaces compartidos
 * Métodos en español siguiendo convención
 */
@Service
public class NodoShareLinkService {

    @Autowired
    private NodoShareLinkRepository shareLinkRepository;

    @Autowired
    private NodoService nodoService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Crea un nuevo enlace compartido para un nodo
     * @param nodoId ID del nodo a compartir
     * @param usuario Usuario que crea el enlace
     * @param password Contraseña opcional para proteger el enlace
     * @param expiresAt Fecha de expiración opcional
     * @param maxDownloads Número máximo de descargas opcional
     * @param allowDownload Permitir descarga
     * @param allowPreview Permitir vista previa
     * @return Enlace compartido creado
     */
    @Transactional
    public NodoShareLink crearEnlace(Long nodoId, Usuario usuario, String password,
                                     LocalDateTime expiresAt, Integer maxDownloads,
                                     Boolean allowDownload, Boolean allowPreview) {
        
        Nodo nodo = nodoService.obtenerPorId(nodoId)
                .orElseThrow(() -> new IllegalArgumentException("Nodo no encontrado"));

        if (nodo.getIsDeleted()) {
            throw new IllegalArgumentException("No se puede compartir un nodo eliminado");
        }

        // Generar token único
        String shareToken = generarTokenUnico();

        NodoShareLink shareLink = new NodoShareLink();
        shareLink.setNodo(nodo);
        shareLink.setCreatedBy(usuario);
        shareLink.setShareToken(shareToken);
        shareLink.setCreatedAt(LocalDateTime.now());
        shareLink.setExpiresAt(expiresAt);
        shareLink.setMaxDownloads(maxDownloads);
        shareLink.setDownloadCount(0);
        shareLink.setAllowDownload(allowDownload != null ? allowDownload : true);
        shareLink.setAllowPreview(allowPreview != null ? allowPreview : true);
        shareLink.setIsActive(true);

        // Encriptar contraseña si se proporciona
        if (password != null && !password.isEmpty()) {
            shareLink.setPasswordHash(passwordEncoder.encode(password));
        }

        return shareLinkRepository.save(shareLink);
    }

    /**
     * Crea un enlace compartido simple (sin contraseña ni restricciones)
     * @param nodoId ID del nodo a compartir
     * @param usuario Usuario que crea el enlace
     * @return Enlace compartido creado
     */
    @Transactional
    public NodoShareLink crearEnlaceSimple(Long nodoId, Usuario usuario) {
        return crearEnlace(nodoId, usuario, null, null, null, true, true);
    }

    /**
     * Obtiene un enlace compartido por su token
     * @param shareToken Token del enlace
     * @return Optional con el enlace si existe
     */
    public Optional<NodoShareLink> obtenerPorToken(String shareToken) {
        return shareLinkRepository.findByShareToken(shareToken);
    }

    /**
     * Valida el acceso a un enlace compartido
     * @param shareToken Token del enlace
     * @param password Contraseña proporcionada (opcional)
     * @return true si el acceso es válido
     */
    public boolean validarAcceso(String shareToken, String password) {
        Optional<NodoShareLink> optionalLink = shareLinkRepository.findByShareToken(shareToken);
        
        if (optionalLink.isEmpty()) {
            return false;
        }

        NodoShareLink shareLink = optionalLink.get();

        // Verificar que el enlace sea accesible
        if (!shareLink.isAccessible()) {
            return false;
        }

        // Verificar contraseña si es requerida
        if (shareLink.requiresPassword()) {
            if (password == null || password.isEmpty()) {
                return false;
            }
            return passwordEncoder.matches(password, shareLink.getPasswordHash());
        }

        return true;
    }

    /**
     * Incrementa el contador de descargas de un enlace
     * @param shareToken Token del enlace
     */
    @Transactional
    public void incrementarDescarga(String shareToken) {
        NodoShareLink shareLink = shareLinkRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new IllegalArgumentException("Enlace no encontrado"));

        shareLink.incrementDownloadCount();
        shareLinkRepository.save(shareLink);
    }

    /**
     * Revoca (desactiva) un enlace compartido
     * @param shareLinkId ID del enlace a revocar
     */
    @Transactional
    public void revocarEnlace(Long shareLinkId) {
        NodoShareLink shareLink = shareLinkRepository.findById(shareLinkId)
                .orElseThrow(() -> new IllegalArgumentException("Enlace no encontrado"));

        shareLink.deactivate();
        shareLinkRepository.save(shareLink);
    }

    /**
     * Activa un enlace compartido previamente desactivado
     * @param shareLinkId ID del enlace a activar
     */
    @Transactional
    public void activarEnlace(Long shareLinkId) {
        NodoShareLink shareLink = shareLinkRepository.findById(shareLinkId)
                .orElseThrow(() -> new IllegalArgumentException("Enlace no encontrado"));

        shareLink.activate();
        shareLinkRepository.save(shareLink);
    }

    /**
     * Obtiene todos los enlaces activos de un nodo
     * @param nodoId ID del nodo
     * @return Lista de enlaces activos
     */
    public List<NodoShareLink> obtenerEnlacesPorNodo(Long nodoId) {
        return shareLinkRepository.findByNodoNodoIdAndIsActiveTrue(nodoId);
    }

    /**
     * Obtiene todos los enlaces creados por un usuario
     * @param usuarioId ID del usuario
     * @return Lista de enlaces
     */
    public List<NodoShareLink> obtenerEnlacesPorUsuario(Long usuarioId) {
        return shareLinkRepository.findByCreatedByUsuarioIdOrderByCreatedAtDesc(usuarioId);
    }

    /**
     * Obtiene todos los enlaces activos del sistema
     * @return Lista de enlaces activos
     */
    public List<NodoShareLink> obtenerEnlacesActivos() {
        return shareLinkRepository.findActiveLinks();
    }

    /**
     * Obtiene todos los enlaces expirados
     * @return Lista de enlaces expirados
     */
    public List<NodoShareLink> obtenerEnlacesExpirados() {
        return shareLinkRepository.findExpiredLinks();
    }

    /**
     * Verifica si un nodo tiene enlaces activos
     * @param nodoId ID del nodo
     * @return true si tiene enlaces activos
     */
    public boolean tieneEnlacesActivos(Long nodoId) {
        return shareLinkRepository.hasActiveShareLink(nodoId);
    }

    /**
     * Actualiza la configuración de un enlace compartido
     * @param shareLinkId ID del enlace
     * @param expiresAt Nueva fecha de expiración (opcional)
     * @param maxDownloads Nuevo límite de descargas (opcional)
     * @param allowDownload Permitir descarga
     * @param allowPreview Permitir vista previa
     * @return Enlace actualizado
     */
    @Transactional
    public NodoShareLink actualizarEnlace(Long shareLinkId, LocalDateTime expiresAt,
                                          Integer maxDownloads, Boolean allowDownload,
                                          Boolean allowPreview) {
        
        NodoShareLink shareLink = shareLinkRepository.findById(shareLinkId)
                .orElseThrow(() -> new IllegalArgumentException("Enlace no encontrado"));

        if (expiresAt != null) {
            shareLink.setExpiresAt(expiresAt);
        }
        if (maxDownloads != null) {
            shareLink.setMaxDownloads(maxDownloads);
        }
        if (allowDownload != null) {
            shareLink.setAllowDownload(allowDownload);
        }
        if (allowPreview != null) {
            shareLink.setAllowPreview(allowPreview);
        }

        return shareLinkRepository.save(shareLink);
    }

    /**
     * Cambia la contraseña de un enlace compartido
     * @param shareLinkId ID del enlace
     * @param newPassword Nueva contraseña (null para quitar la contraseña)
     */
    @Transactional
    public void cambiarContrasena(Long shareLinkId, String newPassword) {
        NodoShareLink shareLink = shareLinkRepository.findById(shareLinkId)
                .orElseThrow(() -> new IllegalArgumentException("Enlace no encontrado"));

        if (newPassword == null || newPassword.isEmpty()) {
            shareLink.setPasswordHash(null);
        } else {
            shareLink.setPasswordHash(passwordEncoder.encode(newPassword));
        }

        shareLinkRepository.save(shareLink);
    }

    /**
     * Elimina un enlace compartido permanentemente
     * @param shareLinkId ID del enlace a eliminar
     */
    @Transactional
    public void eliminarEnlace(Long shareLinkId) {
        shareLinkRepository.deleteById(shareLinkId);
    }

    /**
     * Limpia enlaces expirados o que alcanzaron el máximo de descargas
     */
    @Transactional
    public void limpiarEnlacesInvalidos() {
        List<NodoShareLink> enlacesExpirados = shareLinkRepository.findExpiredLinks();
        shareLinkRepository.deleteAll(enlacesExpirados);
    }

    /**
     * Genera un token único para un enlace compartido
     * @return Token UUID único
     */
    private String generarTokenUnico() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "");
        } while (shareLinkRepository.existsByShareToken(token));
        
        return token;
    }
}
