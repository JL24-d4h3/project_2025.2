package org.project.project.service;

import org.project.project.model.entity.*;
import org.project.project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NodeService {

    @Autowired
    private NodoRepository nodoRepository;

    @Autowired
    private VersionArchivoRepository fileVersionRepository;

    @Autowired
    private PermisoNodoRepository PermisoNodoRepository;

    @Autowired
    private NodoTagRepository nodoTagRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ==================== CRUD OPERATIONS ====================

    /**
     * Crear un nuevo nodo
     */
    public Nodo crearNodo(Nodo nodo) {
        if (nodo.getCreadoEn() == null) {
            nodo.setCreadoEn(LocalDateTime.now());
        }
        return nodoRepository.save(nodo);
    }

    /**
     * Obtener nodo por ID
     */
    @Transactional(readOnly = true)
    public Optional<Nodo> obtenerPorId(Long id) {
        return nodoRepository.findById(id);
    }

    /**
     * Obtener todos los nodos
     */
    @Transactional(readOnly = true)
    public List<Nodo> obtenerTodos() {
        return nodoRepository.findAll();
    }

    /**
     * Actualizar nodo
     */
    public Nodo actualizarNodo(Long id, Nodo nodoActualizado) {
        return nodoRepository.findById(id)
                .map(nodo -> {
                    nodo.setNombre(nodoActualizado.getNombre());
                    nodo.setDescripcion(nodoActualizado.getDescripcion());
                    nodo.setMimeType(nodoActualizado.getMimeType());
                    nodo.setActualizadoEn(LocalDateTime.now());
                    return nodoRepository.save(nodo);
                })
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado con id: " + id));
    }

    /**
     * Eliminar nodo (soft delete)
     */
    public void eliminarNodo(Long id) {
        nodoRepository.findById(id)
                .ifPresent(nodo -> {
                    nodo.markAsDeleted();
                    nodoRepository.save(nodo);
                });
    }

    /**
     * Eliminar nodo permanentemente
     */
    public void eliminarNodoPermanente(Long id) {
        nodoRepository.deleteById(id);
    }

    // ==================== BUSINESS OPERATIONS ====================

    /**
     * Obtener nodos por contenedor
     */
    @Transactional(readOnly = true)
    public List<Nodo> obtenerNodosPorContenedor(Nodo.ContainerType tipo, Long contenedorId) {
        return nodoRepository.findByContainerTypeAndContainerIdAndIsDeletedFalse(tipo, contenedorId);
    }

    /**
     * Obtener nodos raíz de un contenedor
     */
    @Transactional(readOnly = true)
    public List<Nodo> obtenerNodosRaiz(Nodo.ContainerType tipo, Long contenedorId) {
        return nodoRepository.findByContainerTypeAndContainerIdAndParentIdIsNullAndIsDeletedFalse(tipo, contenedorId);
    }

    /**
     * Obtener hijos de un nodo
     */
    @Transactional(readOnly = true)
    public List<Nodo> obtenerHijos(Long parentId) {
        return nodoRepository.findByParentIdAndIsDeletedFalse(parentId);
    }

    /**
     * Crear carpeta
     */
    public Nodo crearCarpeta(String nombre, String path, Nodo.ContainerType containerType, 
                           Long containerId, Long parentId, Long creadoPorId) {
        Nodo carpeta = new Nodo();
        carpeta.setNombre(nombre);
        carpeta.setPath(path);
        carpeta.setTipo(Nodo.TipoNodo.CARPETA);
        carpeta.setContainerType(containerType);
        carpeta.setContainerId(containerId);
        carpeta.setParentId(parentId);
        
        // Buscar el usuario creador
        if (creadoPorId != null) {
            Usuario usuario = usuarioRepository.findById(creadoPorId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + creadoPorId));
            carpeta.setCreadoPor(usuario);
        }
        carpeta.setCreadoEn(LocalDateTime.now());
        
        return nodoRepository.save(carpeta);
    }

    /**
     * Crear archivo
     */
    public Nodo crearArchivo(String nombre, String path, String mimeType, Long size,
                           Nodo.ContainerType containerType, Long containerId, 
                           Long parentId, Long creadoPorId) {
        Nodo archivo = new Nodo();
        archivo.setNombre(nombre);
        archivo.setPath(path);
        archivo.setTipo(Nodo.TipoNodo.ARCHIVO);
        archivo.setMimeType(mimeType);
        archivo.setSize(size);
        archivo.setContainerType(containerType);
        archivo.setContainerId(containerId);
        archivo.setParentId(parentId);
        
        // Buscar el usuario creador
        if (creadoPorId != null) {
            Usuario usuario = usuarioRepository.findById(creadoPorId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + creadoPorId));
            archivo.setCreadoPor(usuario);
        }
        archivo.setCreadoEn(LocalDateTime.now());
        
        return nodoRepository.save(archivo);
    }

    /**
     * Mover nodo a nueva ubicación
     */
    public Nodo moverNodo(Long nodoId, Long nuevoParentId, String nuevaRuta) {
        return nodoRepository.findById(nodoId)
                .map(nodo -> {
                    nodo.setParentId(nuevoParentId);
                    nodo.setPath(nuevaRuta);
                    nodo.setActualizadoEn(LocalDateTime.now());
                    return nodoRepository.save(nodo);
                })
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado con id: " + nodoId));
    }

    /**
     * Buscar nodos por nombre
     */
    @Transactional(readOnly = true)
    public List<Nodo> buscarPorNombre(Nodo.ContainerType containerType, Long containerId, String nombre) {
        return nodoRepository.findByNombres(containerType, containerId, List.of(nombre));
    }

    /**
     * Obtener archivos de un contenedor
     */
    @Transactional(readOnly = true)
    public List<Nodo> obtenerArchivos(Nodo.ContainerType containerType, Long containerId) {
        return nodoRepository.findFilesByContainer(containerType, containerId);
    }

    /**
     * Obtener carpetas de un contenedor
     */
    @Transactional(readOnly = true)
    public List<Nodo> obtenerCarpetas(Nodo.ContainerType containerType, Long containerId) {
        return nodoRepository.findFoldersByContainer(containerType, containerId);
    }

    /**
     * Verificar si existe ruta
     */
    @Transactional(readOnly = true)
    public boolean existeRuta(Nodo.ContainerType containerType, Long containerId, String path) {
        return nodoRepository.existsByContainerTypeAndContainerIdAndPathAndIsDeletedFalse(
                containerType, containerId, path);
    }

    /**
     * Obtener tamaño total del contenedor
     */
    @Transactional(readOnly = true)
    public Long obtenerTamañoTotal(Nodo.ContainerType containerType, Long containerId) {
        return nodoRepository.getTotalSizeByContainer(containerType, containerId);
    }

    /**
     * Contar hijos de un nodo
     */
    @Transactional(readOnly = true)
    public long contarHijos(Long parentId) {
        return nodoRepository.countChildrenByParentId(parentId);
    }

    // ==================== PERMISSION OPERATIONS ====================

    /**
     * Asignar permiso de usuario a nodo
     */
    public PermisoNodo asignarPermisoUsuario(Long nodoId, Long usuarioId, 
                                              PermisoNodo.TipoPermiso permiso) {
        Nodo nodo = nodoRepository.findById(nodoId)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        PermisoNodo permission = new PermisoNodo();
        permission.setNodo(nodo);
        permission.setUsuario(usuario);
        permission.setPermiso(permiso);
        permission.setCreadoEn(LocalDateTime.now());
        
        return PermisoNodoRepository.save(permission);
    }

    /**
     * Verificar si usuario tiene permiso
     */
    @Transactional(readOnly = true)
    public boolean usuarioTienePermiso(Long nodoId, Long usuarioId, 
                                      PermisoNodo.TipoPermiso permiso) {
        return PermisoNodoRepository.hasUserPermission(nodoId, usuarioId, permiso);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Generar ruta completa
     */
    public String generarRutaCompleta(String rutaParent, String nombre) {
        if (rutaParent == null || rutaParent.equals("/")) {
            return "/" + nombre;
        }
        return rutaParent.endsWith("/") ? rutaParent + nombre : rutaParent + "/" + nombre;
    }
}