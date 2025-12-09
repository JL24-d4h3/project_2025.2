package org.project.project.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad para gestionar el workflow de QA de publicación de APIs.
 * 
 * Flujo:
 * 1. DEV crea API en estado BORRADOR (editable)
 * 2. DEV solicita revisión → estado QA, crea solicitud PENDIENTE
 * 3. QA revisa → APROBADO (API pasa a PRODUCCION) o RECHAZADO (vuelve a BORRADOR)
 * 4. DEV puede CANCELAR solicitud mientras esté PENDIENTE/EN_REVISION
 * 
 * Estados posibles:
 * - PENDIENTE: Solicitud creada, esperando revisión QA
 * - EN_REVISION: QA está revisando (opcional, para indicar que alguien está trabajando)
 * - APROBADO: QA aprobó, API publicada en catálogo
 * - RECHAZADO: QA rechazó, API vuelve a BORRADOR para correcciones
 * - CANCELADO: DEV canceló la solicitud, API vuelve a BORRADOR
 */
@Getter
@Setter
@Entity
@Table(name = "solicitud_publicacion_version_api")
public class SolicitudPublicacionVersionApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solicitud_publicacion_id", nullable = false)
    private Long solicitudPublicacionId;

    // ========== RELACIONES ==========
    
    /**
     * API asociada a esta solicitud
     */
    @ManyToOne
    @JoinColumn(name = "api_id", nullable = false)
    private API api;
    
    /**
     * Versión específica de la API que se solicita publicar
     */
    @ManyToOne
    @JoinColumn(name = "version_api_id", nullable = false)
    private VersionAPI versionApi;

    // ========== ACTORES ==========
    
    /**
     * Usuario DEV que generó la solicitud de publicación
     */
    @ManyToOne
    @JoinColumn(name = "generado_por", nullable = false)
    private Usuario generadoPor;
    
    /**
     * Usuario QA que aprobó/rechazó la solicitud (null si aún está pendiente)
     */
    @ManyToOne
    @JoinColumn(name = "aprobado_por")
    private Usuario aprobadoPor;

    // ========== ESTADO ==========
    
    /**
     * Estado actual de la solicitud
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    // ========== FECHAS ==========
    
    /**
     * Fecha en que QA resolvió la solicitud (aprobó o rechazó)
     */
    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;
    
    /**
     * Fecha de creación de la solicitud (timestamp inmutable)
     */
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    // ========== ENUM DE ESTADOS ==========
    
    /**
     * Estados posibles de una solicitud de publicación
     */
    public enum EstadoSolicitud {
        /**
         * Solicitud creada, esperando que QA la revise
         */
        PENDIENTE,
        
        /**
         * QA está activamente revisando la solicitud
         * (Opcional: puede ir directamente de PENDIENTE a APROBADO/RECHAZADO)
         */
        EN_REVISION,
        
        /**
         * QA aprobó la solicitud, API publicada en catálogo
         * Estado final (no puede volver atrás)
         */
        APROBADO,
        
        /**
         * QA rechazó la solicitud, API vuelve a BORRADOR
         * DEV debe corregir y crear nueva solicitud
         */
        RECHAZADO,
        
        /**
         * DEV canceló la solicitud mientras estaba pendiente
         * API vuelve a BORRADOR
         */
        CANCELADO
    }

    // ========== MÉTODOS DE NEGOCIO ==========
    
    /**
     * Verifica si la solicitud está activa (pendiente o en revisión)
     * Usado para bloquear ediciones de la API
     */
    public boolean isActiva() {
        return estado == EstadoSolicitud.PENDIENTE || 
               estado == EstadoSolicitud.EN_REVISION;
    }
    
    /**
     * Verifica si la solicitud está finalizada (aprobada, rechazada o cancelada)
     */
    public boolean isFinalizada() {
        return estado == EstadoSolicitud.APROBADO || 
               estado == EstadoSolicitud.RECHAZADO || 
               estado == EstadoSolicitud.CANCELADO;
    }
    
    /**
     * Verifica si la solicitud puede ser cancelada por el DEV
     * Solo se puede cancelar si está pendiente o en revisión
     */
    public boolean puedeCancelar() {
        return isActiva();
    }
    
    /**
     * Verifica si la solicitud puede ser aprobada/rechazada por QA
     */
    public boolean puedeResolver() {
        return isActiva();
    }

    // ========== MÉTODOS DE AUDITORIA ==========
    
    /**
     * Marca la solicitud como aprobada por un usuario QA
     */
    public void aprobar(Usuario qa) {
        this.estado = EstadoSolicitud.APROBADO;
        this.aprobadoPor = qa;
        this.fechaResolucion = LocalDateTime.now();
    }
    
    /**
     * Marca la solicitud como rechazada por un usuario QA
     */
    public void rechazar(Usuario qa) {
        this.estado = EstadoSolicitud.RECHAZADO;
        this.aprobadoPor = qa;
        this.fechaResolucion = LocalDateTime.now();
    }
    
    /**
     * Marca la solicitud como cancelada por el DEV
     */
    public void cancelar() {
        this.estado = EstadoSolicitud.CANCELADO;
        this.fechaResolucion = LocalDateTime.now();
    }
    
    /**
     * Marca la solicitud como "en revisión" cuando QA comienza a trabajar
     */
    public void marcarEnRevision() {
        if (this.estado == EstadoSolicitud.PENDIENTE) {
            this.estado = EstadoSolicitud.EN_REVISION;
        }
    }

    // ========== toString para debugging ==========
    
    @Override
    public String toString() {
        return String.format(
            "SolicitudPublicacionVersionApi[id=%d, api=%s, version=%s, estado=%s, generadoPor=%s, creadoEn=%s]",
            solicitudPublicacionId,
            api != null ? api.getNombreApi() : "null",
            versionApi != null ? versionApi.getNumeroVersion() : "null",
            estado,
            generadoPor != null ? generadoPor.getUsername() : "null",
            creadoEn
        );
    }
}
