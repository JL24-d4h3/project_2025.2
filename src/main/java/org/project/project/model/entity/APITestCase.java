package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad híbrida SQL + MongoDB
 * SQL almacena metadatos básicos del test case
 * MongoDB almacena configuración completa (headers, body, assertions, scripts)
 * en la colección: test_case_config
 */
@Getter
@Setter
@Entity
@Table(name = "api_test_case")
public class APITestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_case_id", nullable = false)
    private Long testCaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_api_id", nullable = false)
    private API api;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_version_id", nullable = false)
    private VersionAPI version;

    @Column(name = "nombre_test", nullable = false, length = 255)
    private String nombreTest;

    // Alias para repository methods en inglés
    @Column(name = "nombre_test", insertable = false, updatable = false)
    private String testName;

    @Lob
    @Column(name = "descripcion_test")
    private String descripcionTest;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "descripcion_test", insertable = false, updatable = false)
    private String testDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_http", nullable = false)
    private MetodoHTTP metodoHTTP;

    // Alias para repository methods en inglés
    @Column(name = "metodo_http", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private MetodoHTTP httpMethod;

    @Column(name = "endpoint_path", nullable = false, length = 500)
    private String endpointPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_test", nullable = false)
    private EstadoTest estadoTest = EstadoTest.ACTIVO;

    // Alias para repository methods en inglés
    @Column(name = "estado_test", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EstadoTest testStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad_test", nullable = false)
    private PrioridadTest prioridadTest = PrioridadTest.MEDIA;

    // Alias para repository methods en inglés
    @Column(name = "prioridad_test", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private PrioridadTest testPriority;

    /**
     * ObjectId de MongoDB que apunta a la colección test_case_config
     * Contiene la configuración completa del test (headers, body, assertions, etc.)
     */
    @Column(name = "nosql_config_id", length = 24)
    private String nosqlConfigId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    
    // Alias para repository methods en inglés
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", insertable = false, updatable = false)
    private Usuario createdBy;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
    
    // Alias para repository methods en inglés
    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;
    
    // Alias para repository methods en inglés
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por", insertable = false, updatable = false)
    private Usuario updatedBy;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
    
    // Alias para repository methods en inglés
    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // Relaciones
    @OneToMany(mappedBy = "testCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<APITestLog> testLogs = new HashSet<>();

    public enum MetodoHTTP {
        GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD
    }

    public enum EstadoTest {
        ACTIVO, INACTIVO, DEPRECADO
    }

    public enum PrioridadTest {
        BAJA, MEDIA, ALTA, CRITICA
    }

    // Métodos helper
    public void activar() {
        this.estadoTest = EstadoTest.ACTIVO;
    }

    public void desactivar() {
        this.estadoTest = EstadoTest.INACTIVO;
    }

    public void marcarComoDeprecado() {
        this.estadoTest = EstadoTest.DEPRECADO;
    }

    public boolean estaActivo() {
        return EstadoTest.ACTIVO.equals(this.estadoTest);
    }
}

