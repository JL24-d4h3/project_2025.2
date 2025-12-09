package org.project.project.model.enums;

/**
 * Estados posibles de una solicitud de acceso a API
 * 
 * - PENDIENTE: Solicitud creada, esperando aprobación del PROVIDER
 * - APROBADO: PROVIDER aprobó la solicitud, DEV puede generar API Key
 * - RECHAZADO: PROVIDER rechazó la solicitud, incluye comentario de rechazo
 */
public enum EstadoSolicitudAcceso {
    
    PENDIENTE,
    APROBADO,
    RECHAZADO
    
}
