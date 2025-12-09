package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa una solicitud de acceso a una versión específica de una API.
 * 
 * Flujo:
 * 1. DEV solicita acceso a una versión de API (estado = PENDIENTE)
 * 2. PROVIDER (creador de la versión) aprueba/rechaza
 * 3. Si está APROBADO, DEV puede generar su API Key
 * 
 * Control a nivel de VERSIÓN (no API completa)
 */

@Getter
@Setter
@Entity
@Table(name = "solicitud_acceso_api")
public class SolicitudAccesoAPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accesibilidad_id", nullable = false)
    private Long accesibilidadId;
    
    // Alias para repository methods en inglés
    @Column(name = "accesibilidad_id", insertable = false, updatable = false)
    private Long apiAccessRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entorno", nullable = false)
    private TipoEntorno tipoEntorno;
    
    // Alias para repository methods en inglés
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entorno", insertable = false, updatable = false)
    private TipoEntorno environmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud", nullable = false)
    private EstadoSolicitud estadoSolicitud;
    
    // Alias para repository methods en inglés
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud", insertable = false, updatable = false)
    private EstadoSolicitud requestStatus;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_solicitud", insertable = false, updatable = false)
    private LocalDateTime requestDate;

    @Column(name = "comentario_solicitud", length = 255)
    private String comentarioSolicitud;
    
    // Alias para repository methods en inglés
    @Column(name = "comentario_solicitud", insertable = false, updatable = false)
    private String requestComment;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    // Alias en inglés para repository methods
    @Column(name = "fecha_revision", insertable = false, updatable = false)
    private LocalDateTime reviewDate;

    @Column(name = "aprobador_usuario_id")
    private Long aprobadorUsuarioId;

    // Alias en inglés para repository methods
    @Column(name = "aprobador_usuario_id", insertable = false, updatable = false)
    private Long approverUserId;

    @Column(name = "comentario_revision", columnDefinition = "TEXT")
    private String comentarioRevision;

    // Alias en inglés para repository methods
    @Column(name = "comentario_revision", insertable = false, updatable = false)
    private String reviewComment;

    @ManyToOne
    @JoinColumn(name = "usuario_usuario_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne
    @JoinColumn(name = "aprobador_usuario_id", nullable = true, insertable = false, updatable = false)
    private Usuario aprobador;

    @ManyToOne
    @JoinColumn(name = "api_api_id", nullable = false)
    private API api;

    // ✅ NUEVO: Versión específica solicitada (control granular)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id")
    private VersionAPI versionApi;

    public enum TipoEntorno {
        SANDBOX, QA, PROD
    }

    public enum EstadoSolicitud {
        PENDIENTE, APROBADO, RECHAZADO
    }
}
