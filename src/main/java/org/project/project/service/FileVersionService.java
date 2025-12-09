package org.project.project.service;

import org.project.project.model.entity.Enlace;
import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.VersionArchivo;
import org.project.project.repository.EnlaceRepository;
import org.project.project.repository.NodoRepository;
import org.project.project.repository.UsuarioRepository;
import org.project.project.repository.VersionArchivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FileVersionService {

    @Autowired
    private VersionArchivoRepository fileVersionRepository;
    
    @Autowired
    private NodoRepository nodoRepository;
    
    @Autowired
    private EnlaceRepository enlaceRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    // ==================== CRUD OPERATIONS ====================

    /**
     * Crear nueva versión de archivo
     */
    public VersionArchivo crearVersion(VersionArchivo versionArchivo) {
        if (versionArchivo.getCreadoEn() == null) {
            versionArchivo.setCreadoEn(LocalDateTime.now());
        }
        return fileVersionRepository.save(versionArchivo);
    }

    /**
     * Obtener versión por ID
     */
    @Transactional(readOnly = true)
    public Optional<VersionArchivo> obtenerPorId(Long id) {
        return fileVersionRepository.findById(id);
    }

    /**
     * Obtener todas las versiones
     */
    @Transactional(readOnly = true)
    public List<VersionArchivo> obtenerTodas() {
        return fileVersionRepository.findAll();
    }

    /**
     * Actualizar versión
     */
    public VersionArchivo actualizarVersion(Long id, VersionArchivo versionActualizada) {
        return fileVersionRepository.findById(id)
                .map(version -> {
                    version.setVersionLabel(versionActualizada.getVersionLabel());
                    version.setChecksum(versionActualizada.getChecksum());
                    version.setVigente(versionActualizada.getVigente());
                    return fileVersionRepository.save(version);
                })
                .orElseThrow(() -> new RuntimeException("Versión no encontrada con id: " + id));
    }

    /**
     * Eliminar versión
     */
    public void eliminarVersion(Long id) {
        fileVersionRepository.deleteById(id);
    }

    // ==================== BUSINESS OPERATIONS ====================

    /**
     * Obtener versiones de un nodo
     */
    @Transactional(readOnly = true)
    public List<VersionArchivo> obtenerVersionesPorNodo(Long nodoId) {
        return fileVersionRepository.findByNodo_NodoIdOrderByCreatedAtDesc(nodoId);
    }

    /**
     * Obtener versión actual (vigente) de un nodo
     */
    @Transactional(readOnly = true)
    public Optional<VersionArchivo> obtenerVersionActual(Long nodoId) {
        return fileVersionRepository.findActiveVersionByNodeId(nodoId);
    }

    /**
     * Crear nueva versión y marcar anterior como obsoleta
     */
    public VersionArchivo crearNuevaVersion(Long nodoId, Long enlaceId, String storageKey,
                                       Long tamaño, String versionLabel, Long creadoPor) {
        // Obtener entidades necesarias
        Nodo nodo = obtenerNodo(nodoId);
        Enlace enlace = obtenerEnlace(enlaceId);
        Usuario usuario = obtenerUsuario(creadoPor);
        
        // Marcar versión actual como obsoleta
        fileVersionRepository.findActiveVersionByNodeId(nodoId)
                .ifPresent(versionActual -> {
                    versionActual.markAsObsolete();
                    versionActual.setActualizadoPor(usuario);
                    fileVersionRepository.save(versionActual);
                });

        // Crear nueva versión vigente
        VersionArchivo nuevaVersion = new VersionArchivo();
        nuevaVersion.setNodo(nodo);
        nuevaVersion.setEnlace(enlace);
        nuevaVersion.setStorageKey(storageKey);
        nuevaVersion.setSizeBytes(tamaño);
        nuevaVersion.setVersionLabel(versionLabel);
        nuevaVersion.setCreadoPor(usuario);
        nuevaVersion.setVigente(true);
        nuevaVersion.setCreadoEn(LocalDateTime.now());

        return fileVersionRepository.save(nuevaVersion);
    }
    
    // Métodos auxiliares privados
    private Nodo obtenerNodo(Long nodoId) {
        return nodoRepository.findById(nodoId)
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado con id: " + nodoId));
    }
    
    private Enlace obtenerEnlace(Long enlaceId) {
        return enlaceRepository.findById(enlaceId)
                .orElseThrow(() -> new RuntimeException("Enlace no encontrado con id: " + enlaceId));
    }
    
    private Usuario obtenerUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + usuarioId));
    }

    /**
     * Obtener última versión de un nodo
     */
    @Transactional(readOnly = true)
    public Optional<VersionArchivo> obtenerUltimaVersion(Long nodoId) {
        return fileVersionRepository.findLatestByNodeId(nodoId);
    }

    /**
     * Contar versiones de un nodo
     */
    @Transactional(readOnly = true)
    public long contarVersiones(Long nodoId) {
        return fileVersionRepository.countByNodo_NodoId(nodoId);
    }

    /**
     * Obtener tamaño total de versiones de un nodo
     */
    @Transactional(readOnly = true)
    public Long obtenerTamañoTotal(Long nodoId) {
        return fileVersionRepository.getTotalSizeByNodeId(nodoId);
    }

    /**
     * Buscar versiones por checksum
     */
    @Transactional(readOnly = true)
    public List<VersionArchivo> buscarPorChecksum(String checksum) {
        return fileVersionRepository.findByChecksum(checksum);
    }

    /**
     * Verificar si storage key existe
     */
    @Transactional(readOnly = true)
    public boolean existeStorageKey(String storageKey) {
        return fileVersionRepository.existsByStorageKey(storageKey);
    }

    /**
     * Obtener versiones obsoletas de un nodo
     */
    @Transactional(readOnly = true)
    public List<VersionArchivo> obtenerVersionesObsoletas(Long nodoId) {
        return fileVersionRepository.findObsoleteVersionsByNodeId(nodoId);
    }

    /**
     * Obtener uso total de almacenamiento
     */
    @Transactional(readOnly = true)
    public Long obtenerUsoTotalAlmacenamiento() {
        return fileVersionRepository.getTotalStorageUsage();
    }

    /**
     * Limpiar versiones obsoletas de un nodo (mantener solo las últimas N)
     */
    public void limpiarVersionesObsoletas(Long nodoId, int mantenerUltimas) {
        List<VersionArchivo> versiones = fileVersionRepository.findByNodo_NodoIdOrderByCreatedAtDesc(nodoId);
        
        if (versiones.size() > mantenerUltimas) {
            List<VersionArchivo> aEliminar = versiones.subList(mantenerUltimas, versiones.size());
            fileVersionRepository.deleteAll(aEliminar);
        }
    }
}