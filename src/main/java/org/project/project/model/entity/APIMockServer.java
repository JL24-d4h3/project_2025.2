package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad híbrida SQL + MongoDB
 * SQL almacena metadatos del mock server (nombre, estado, URL base)
 * MongoDB almacena configuración de endpoints y respuestas mock
 * en la colección: mock_server_config
 */
@Getter
@Setter
@Entity
@Table(name = "api_mock_server")
public class APIMockServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mock_server_id", nullable = false)
    private Long mockServerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_api_id", nullable = false)
    private API api;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_version_id")
    private VersionAPI version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;
    
    // Alias para repository methods en inglés
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", insertable = false, updatable = false)
    private Usuario createdBy;

    @Column(name = "nombre_mock", nullable = false, length = 255)
    private String nombreMock;

    // Alias para repository methods en inglés
    @Column(name = "nombre_mock", insertable = false, updatable = false)
    private String mockName;

    @Lob
    @Column(name = "descripcion_mock")
    private String descripcionMock;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "descripcion_mock", insertable = false, updatable = false)
    private String mockDescription;

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_mock", nullable = false)
    private EstadoMock estadoMock = EstadoMock.ACTIVO;

    // Alias para repository methods en inglés
    @Column(name = "estado_mock", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EstadoMock mockStatus;

    /**
     * ObjectId de MongoDB que apunta a la colección mock_server_config
     * Contiene la configuración de endpoints y respuestas mock
     */
    @Column(name = "nosql_config_id", length = 24)
    private String nosqlConfigId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public enum EstadoMock {
        ACTIVO, INACTIVO, MANTENIMIENTO
    }

    // Métodos helper
    public void activar() {
        this.estadoMock = EstadoMock.ACTIVO;
    }

    public void desactivar() {
        this.estadoMock = EstadoMock.INACTIVO;
    }

    public void ponerEnMantenimiento() {
        this.estadoMock = EstadoMock.MANTENIMIENTO;
    }

    public boolean estaActivo() {
        return EstadoMock.ACTIVO.equals(this.estadoMock);
    }

    public void actualizar() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}

