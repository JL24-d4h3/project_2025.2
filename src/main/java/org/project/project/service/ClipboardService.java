package org.project.project.service;

import org.project.project.model.entity.ClipboardOperation;
import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.ClipboardOperationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service para operaciones de clipboard (copiar/cortar/pegar)
 * Métodos en español siguiendo convención
 */
@Service
public class ClipboardService {

    @Autowired
    private ClipboardOperationRepository clipboardRepository;

    @Autowired
    private NodoService nodoService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private GCSConfigService gcsConfigService;

    /**
     * Copia nodos al clipboard
     * @param nodoIds Lista de IDs de nodos a copiar
     * @param usuario Usuario que realiza la operación
     * @return Operación de clipboard creada
     */
    @Transactional
    public ClipboardOperation copiarNodos(List<Long> nodoIds, Usuario usuario) {
        // Cancelar operación activa anterior si existe
        cancelarOperacionActiva(usuario.getId());

        // Validar que los nodos existen y obtener información del contenedor
        Nodo primerNodo = null;
        for (Long nodoId : nodoIds) {
            Nodo nodo = nodoService.obtenerPorId(nodoId)
                    .orElseThrow(() -> new IllegalArgumentException("Nodo no encontrado: " + nodoId));
            if (primerNodo == null) {
                primerNodo = nodo;
            }
        }

        ClipboardOperation operation = new ClipboardOperation();
        operation.setUsuario(usuario);
        operation.setNodoIds(nodoIds);
        operation.setOperationType(ClipboardOperation.OperationType.COPY);
        operation.setSourceContainerType(primerNodo.getContainerType());
        operation.setSourceContainerId(primerNodo.getContainerId());
        operation.setSourceParentId(primerNodo.getParentId());
        operation.setCreatedAt(LocalDateTime.now());
        operation.setExpiresAt(LocalDateTime.now().plusHours(24));
        operation.setIsExpired(false);

        return clipboardRepository.save(operation);
    }

    /**
     * Corta nodos al clipboard (para mover)
     * @param nodoIds Lista de IDs de nodos a cortar
     * @param usuario Usuario que realiza la operación
     * @return Operación de clipboard creada
     */
    @Transactional
    public ClipboardOperation cortarNodos(List<Long> nodoIds, Usuario usuario) {
        // Cancelar operación activa anterior si existe
        cancelarOperacionActiva(usuario.getId());

        // Validar que los nodos existen y no están eliminados, obtener información del contenedor
        Nodo primerNodo = null;
        for (Long nodoId : nodoIds) {
            Nodo nodo = nodoService.obtenerPorId(nodoId)
                    .orElseThrow(() -> new IllegalArgumentException("Nodo no encontrado: " + nodoId));
            
            if (nodo.getIsDeleted()) {
                throw new IllegalArgumentException("No se puede cortar un nodo eliminado");
            }
            
            if (primerNodo == null) {
                primerNodo = nodo;
            }
        }

        ClipboardOperation operation = new ClipboardOperation();
        operation.setUsuario(usuario);
        operation.setNodoIds(nodoIds);
        operation.setOperationType(ClipboardOperation.OperationType.CUT);
        operation.setSourceContainerType(primerNodo.getContainerType());
        operation.setSourceContainerId(primerNodo.getContainerId());
        operation.setSourceParentId(primerNodo.getParentId());
        operation.setCreatedAt(LocalDateTime.now());
        operation.setExpiresAt(LocalDateTime.now().plusHours(24));
        operation.setIsExpired(false);

        return clipboardRepository.save(operation);
    }

    /**
     * Pega los nodos del clipboard en una nueva ubicación
     * @param nodoPadreDestinoId ID del nodo padre destino (null para raíz)
     * @param usuario Usuario que realiza la operación
     * @return Lista de nodos creados/movidos
     */
    @Transactional
    public List<Nodo> pegarNodos(Long nodoPadreDestinoId, Usuario usuario) {
        // Obtener operación activa
        ClipboardOperation operation = obtenerOperacionActiva(usuario.getId())
                .orElseThrow(() -> new IllegalStateException("No hay operación de clipboard activa"));

        List<Nodo> resultados = new ArrayList<>();

        if (operation.getOperationType() == ClipboardOperation.OperationType.COPY) {
            // COPIAR: Duplicar nodos en nueva ubicación
            for (Long nodoId : operation.getNodoIds()) {
                Nodo nodoOriginal = nodoService.obtenerPorId(nodoId)
                        .orElseThrow(() -> new IllegalArgumentException("Nodo no encontrado: " + nodoId));
                
                Nodo nodoCopia = copiarNodoRecursivo(nodoOriginal, nodoPadreDestinoId, usuario);
                resultados.add(nodoCopia);
            }
            
            // La operación de COPY no se marca como expirada (se puede pegar múltiples veces)
            
        } else if (operation.getOperationType() == ClipboardOperation.OperationType.CUT) {
            // CORTAR: Mover nodos a nueva ubicación
            for (Long nodoId : operation.getNodoIds()) {
                nodoService.moverNodo(nodoId, nodoPadreDestinoId);
                
                Nodo nodoMovido = nodoService.obtenerPorId(nodoId)
                        .orElseThrow(() -> new IllegalArgumentException("Nodo no encontrado: " + nodoId));
                resultados.add(nodoMovido);
            }
            
            // Marcar operación como expirada después de pegar
            operation.markAsExpired();
            clipboardRepository.save(operation);
        }

        return resultados;
    }

    /**
     * Copia un nodo recursivamente (incluyendo archivos en GCS)
     * @param nodoOriginal Nodo a copiar
     * @param nodoPadreDestinoId ID del nodo padre destino
     * @param usuario Usuario que realiza la copia
     * @return Nodo copiado
     */
    private Nodo copiarNodoRecursivo(Nodo nodoOriginal, Long nodoPadreDestinoId, Usuario usuario) {
        if (nodoOriginal.getTipo() == Nodo.TipoNodo.CARPETA) {
            // Copiar carpeta
            Nodo carpetaCopia = nodoService.crearCarpeta(
                    nodoOriginal.getNombre() + " (copia)",
                    nodoOriginal.getContainerType(),
                    nodoOriginal.getContainerId(),
                    nodoPadreDestinoId,
                    usuario
            );

            // Copiar hijos recursivamente
            List<Nodo> hijos = nodoService.obtenerHijos(
                    nodoOriginal.getNodoId(),
                    nodoOriginal.getContainerType(),
                    nodoOriginal.getContainerId()
            );

            for (Nodo hijo : hijos) {
                copiarNodoRecursivo(hijo, carpetaCopia.getNodoId(), usuario);
            }

            return carpetaCopia;

        } else {
            // Copiar archivo en GCS
            String rutaOrigenGCS = nodoOriginal.getGcsPath();
            
            String rutaDestino = "/";
            if (nodoPadreDestinoId != null) {
                rutaDestino = nodoService.obtenerRutaCompleta(nodoPadreDestinoId);
            }

            Nodo nodoTemporal = new Nodo();
            nodoTemporal.setNombre(nodoOriginal.getNombre());
            nodoTemporal.setContainerType(nodoOriginal.getContainerType());
            nodoTemporal.setContainerId(nodoOriginal.getContainerId());
            
            String rutaDestinoGCS = gcsConfigService.obtenerRutaCompleta(nodoTemporal, rutaDestino);

            // Copiar archivo en GCS
            fileStorageService.copiarArchivo(rutaOrigenGCS, rutaDestinoGCS);

            // Crear nodo en BD
            return nodoService.crearNodoArchivo(
                    nodoOriginal.getNombre(),
                    nodoOriginal.getContainerType(),
                    nodoOriginal.getContainerId(),
                    nodoPadreDestinoId,
                    rutaDestinoGCS,
                    nodoOriginal.getSize(),
                    nodoOriginal.getMimeType(),
                    usuario
            );
        }
    }

    /**
     * Cancela la operación activa de un usuario
     * @param usuarioId ID del usuario
     */
    @Transactional
    public void cancelarOperacion(Long usuarioId) {
        cancelarOperacionActiva(usuarioId);
    }

    /**
     * Cancela la operación activa de clipboard de un usuario
     * @param usuarioId ID del usuario
     */
    private void cancelarOperacionActiva(Long usuarioId) {
        List<ClipboardOperation> activeOps = clipboardRepository.findActiveByUserId(usuarioId, LocalDateTime.now());
        if (!activeOps.isEmpty()) {
            ClipboardOperation operation = activeOps.get(0);
            operation.markAsExpired();
            clipboardRepository.save(operation);
        }
    }

    /**
     * Obtiene la operación activa de clipboard de un usuario
     * @param usuarioId ID del usuario
     * @return Optional con la operación activa si existe
     */
    public Optional<ClipboardOperation> obtenerOperacionActiva(Long usuarioId) {
        List<ClipboardOperation> activeOps = clipboardRepository.findActiveByUserId(usuarioId, LocalDateTime.now());
        return activeOps.isEmpty() ? Optional.empty() : Optional.of(activeOps.get(0));
    }

    /**
     * Verifica si un usuario tiene una operación activa de clipboard
     * @param usuarioId ID del usuario
     * @return true si tiene operación activa
     */
    public boolean tieneOperacionActiva(Long usuarioId) {
        return clipboardRepository.hasActiveClipboard(usuarioId, LocalDateTime.now());
    }

    /**
     * Limpia las operaciones expiradas del sistema (ejecutado por evento programado)
     */
    @Transactional
    public void limpiarOperacionesExpiradas() {
        clipboardRepository.markExpiredOperations(LocalDateTime.now());
    }
}



