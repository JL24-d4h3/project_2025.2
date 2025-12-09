package org.project.project.model.validator;

import org.springframework.stereotype.Component;
import org.project.project.model.entity.Reporte;
import org.project.project.model.entity.Usuario;

/**
 * Validador de permisos para reportes
 * Verifica qué usuario puede hacer qué acción
 */
@Component
public class ReportePermissionValidator {

    /**
     * Verifica si un usuario puede editar un reporte
     * Solo el autor puede editar
     */
    public boolean canEditReporte(Reporte reporte, Usuario usuarioActual) {
        if (reporte == null || usuarioActual == null) {
            return false;
        }
        // Solo el autor original puede editar
        return reporte.getAutor().getUsuarioId().equals(usuarioActual.getUsuarioId());
    }

    /**
     * Verifica si un usuario puede revisar/aprobar un reporte
     * Solo PO puede revisar (esto se determina por rol)
     */
    public boolean canReviewReporte(Usuario usuarioActual) {
        if (usuarioActual == null) {
            return false;
        }
        // Aquí se verificaría si el usuario tiene rol de PO
        // Por ahora retorna true si está autenticado
        // TODO: Implementar verificación de rol PO
        return true;
    }

    /**
     * Verifica si un usuario puede eliminar un reporte
     * Solo el autor o admin pueden eliminar
     */
    public boolean canDeleteReporte(Reporte reporte, Usuario usuarioActual) {
        if (reporte == null || usuarioActual == null) {
            return false;
        }
        // Solo el autor o admin (verific rol)
        return reporte.getAutor().getUsuarioId().equals(usuarioActual.getUsuarioId());
    }

    /**
     * Verifica si un usuario puede ver un reporte
     * Cualquiera puede ver si está PUBLICADO
     * Solo autor/colaboradores pueden ver BORRADORES/REVISADOS
     */
    public boolean canViewReporte(Reporte reporte, Usuario usuarioActual) {
        if (reporte == null) {
            return false;
        }

        // Si es PUBLICADO, todos pueden ver
        if (Reporte.EstadoReporte.PUBLICADO.equals(reporte.getEstadoReporte())) {
            return true;
        }

        // Si no es PUBLICADO, solo autor/colaboradores
        if (usuarioActual == null) {
            return false;
        }

        return reporte.getAutor().getUsuarioId().equals(usuarioActual.getUsuarioId()) ||
               reporte.getColaboradores().stream()
                   .anyMatch(c -> c.getUsuario().getUsuarioId().equals(usuarioActual.getUsuarioId()));
    }

    /**
     * Valida que el reporte esté en estado adecuado para editar
     * Solo se puede editar si está en BORRADOR o RECHAZADO
     */
    public boolean isEditableState(Reporte reporte) {
        return Reporte.EstadoReporte.BORRADOR.equals(reporte.getEstadoReporte()) ||
               "RECHAZADO".equals(reporte.getEstadoReporte());
    }

    /**
     * Valida límites de archivos
     */
    public boolean validateAttachmentLimits(int fileCount, long fileSizeBytes) {
        final int MAX_FILES = 10;
        final long MAX_FILE_SIZE = 50 * 1024 * 1024;  // 50 MB
        final long MAX_TOTAL_SIZE = 500 * 1024 * 1024;  // 500 MB por reporte

        return fileCount <= MAX_FILES &&
               fileSizeBytes <= MAX_FILE_SIZE &&
               fileSizeBytes * fileCount <= MAX_TOTAL_SIZE;
    }
}
