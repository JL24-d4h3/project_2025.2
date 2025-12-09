package org.project.project.service;

import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.NodoFavorite;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.NodoFavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service para gestión de favoritos de nodos
 * Métodos en español siguiendo convención
 */
@Service
public class NodoFavoriteService {

    @Autowired
    private NodoFavoriteRepository favoriteRepository;

    @Autowired
    private NodoService nodoService;

    /**
     * Agrega un nodo a favoritos
     * @param nodoId ID del nodo
     * @param usuario Usuario que agrega el favorito
     * @param customLabel Etiqueta personalizada opcional
     * @return Favorito creado
     */
    @Transactional
    public NodoFavorite agregarFavorito(Long nodoId, Usuario usuario, String customLabel) {
        // Verificar que el nodo existe
        Nodo nodo = nodoService.obtenerPorId(nodoId)
                .orElseThrow(() -> new IllegalArgumentException("Nodo no encontrado"));

        // Verificar que no esté eliminado
        if (nodo.getIsDeleted()) {
            throw new IllegalArgumentException("No se puede marcar como favorito un nodo eliminado");
        }

        // Verificar que no exista ya como favorito
        if (favoriteRepository.existsByUserIdAndNodeId(usuario.getId(), nodoId)) {
            throw new IllegalArgumentException("El nodo ya está en favoritos");
        }

        NodoFavorite favorite = new NodoFavorite();
        favorite.setUsuario(usuario);
        favorite.setNodo(nodo);
        favorite.setCustomLabel(customLabel);
        favorite.setCreatedAt(LocalDateTime.now());

        return favoriteRepository.save(favorite);
    }

    /**
     * Agrega un nodo a favoritos sin etiqueta personalizada
     * @param nodoId ID del nodo
     * @param usuario Usuario que agrega el favorito
     * @return Favorito creado
     */
    @Transactional
    public NodoFavorite agregarFavorito(Long nodoId, Usuario usuario) {
        return agregarFavorito(nodoId, usuario, null);
    }

    /**
     * Quita un nodo de favoritos
     * @param nodoId ID del nodo
     * @param usuarioId ID del usuario
     */
    @Transactional
    public void quitarFavorito(Long nodoId, Long usuarioId) {
        favoriteRepository.deleteByUserIdAndNodeId(usuarioId, nodoId);
    }

    /**
     * Quita un favorito por su ID
     * @param favoriteId ID del favorito
     */
    @Transactional
    public void quitarFavoritoPorId(Long favoriteId) {
        favoriteRepository.deleteById(favoriteId);
    }

    /**
     * Obtiene todos los favoritos de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de favoritos ordenados por fecha de creación descendente
     */
    public List<NodoFavorite> obtenerFavoritos(Long usuarioId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(usuarioId);
    }

    /**
     * Obtiene un favorito específico
     * @param favoriteId ID del favorito
     * @return Optional con el favorito si existe
     */
    public Optional<NodoFavorite> obtenerFavoritoPorId(Long favoriteId) {
        return favoriteRepository.findById(favoriteId);
    }

    /**
     * Verifica si un nodo es favorito de un usuario
     * @param nodoId ID del nodo
     * @param usuarioId ID del usuario
     * @return true si el nodo está en favoritos
     */
    public boolean esFavorito(Long nodoId, Long usuarioId) {
        return favoriteRepository.existsByUserIdAndNodeId(usuarioId, nodoId);
    }

    /**
     * Cuenta cuántos favoritos tiene un usuario
     * @param usuarioId ID del usuario
     * @return Número de favoritos
     */
    public long contarFavoritos(Long usuarioId) {
        return favoriteRepository.countByUserId(usuarioId);
    }

    /**
     * Actualiza la etiqueta personalizada de un favorito
     * @param favoriteId ID del favorito
     * @param nuevoLabel Nueva etiqueta
     * @return Favorito actualizado
     */
    @Transactional
    public NodoFavorite actualizarEtiqueta(Long favoriteId, String nuevoLabel) {
        NodoFavorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new IllegalArgumentException("Favorito no encontrado"));

        favorite.setCustomLabel(nuevoLabel);
        return favoriteRepository.save(favorite);
    }

    /**
     * Busca favoritos por etiqueta personalizada
     * @param usuarioId ID del usuario
     * @param label Etiqueta a buscar (búsqueda parcial)
     * @return Lista de favoritos que coinciden
     */
    public List<NodoFavorite> buscarPorEtiqueta(Long usuarioId, String label) {
        return favoriteRepository.findByUserIdAndCustomLabelContaining(usuarioId, label);
    }

    /**
     * Obtiene todos los usuarios que tienen un nodo como favorito
     * @param nodoId ID del nodo
     * @return Lista de favoritos
     */
    public List<NodoFavorite> obtenerUsuariosQueFavorecieron(Long nodoId) {
        return favoriteRepository.findByNodeId(nodoId);
    }

    /**
     * Cuenta cuántos usuarios tienen un nodo como favorito
     * @param nodoId ID del nodo
     * @return Número de usuarios
     */
    public long contarUsuariosQueFavorecieron(Long nodoId) {
        return favoriteRepository.countByNodeId(nodoId);
    }

    /**
     * Limpia favoritos de nodos que fueron eliminados permanentemente
     * Esta operación se debe ejecutar periódicamente
     */
    @Transactional
    public void limpiarFavoritosDeNodosEliminados() {
        // Obtener todos los favoritos
        List<NodoFavorite> todosFavoritos = favoriteRepository.findAll();
        
        // Filtrar favoritos cuyos nodos están eliminados
        List<NodoFavorite> favoritosAEliminar = todosFavoritos.stream()
                .filter(fav -> fav.getNodo().getIsDeleted())
                .toList();
        
        // Eliminar favoritos de nodos eliminados
        favoriteRepository.deleteAll(favoritosAEliminar);
    }

    /**
     * Alterna el estado de favorito (si existe lo quita, si no existe lo agrega)
     * @param nodoId ID del nodo
     * @param usuario Usuario
     * @return true si se agregó, false si se quitó
     */
    @Transactional
    public boolean alternarFavorito(Long nodoId, Usuario usuario) {
        if (esFavorito(nodoId, usuario.getId())) {
            quitarFavorito(nodoId, usuario.getId());
            return false;
        } else {
            agregarFavorito(nodoId, usuario);
            return true;
        }
    }
}

