package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "entorno_prueba")
public class EntornoPrueba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entorno_id", nullable = false)
    private Long entornoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_api_id", nullable = false)
    private API api;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_version_id")
    private VersionAPI version;

    @Column(name = "nombre_entorno", nullable = false, length = 255)
    private String nombreEntorno;

    // Alias para repository methods en inglés
    @Column(name = "nombre_entorno", insertable = false, updatable = false)
    private String environmentName;

    @Lob
    @Column(name = "descripcion_entorno")
    private String descripcionEntorno;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "descripcion_entorno", insertable = false, updatable = false)
    private String environmentDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_entorno", nullable = false)
    private EstadoEntorno estadoEntorno = EstadoEntorno.ACTIVO;

    // Alias para repository methods en inglés
    @Column(name = "estado_entorno", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EstadoEntorno environmentStatus;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    @Column(name = "limite_llamadas_dia", nullable = false)
    private Integer limiteLlamadasDia = 1000;

    // Alias para repository methods en inglés
    @Column(name = "limite_llamadas_dia", insertable = false, updatable = false)
    private Integer dailyCallLimit;

    @Column(name = "llamadas_realizadas", nullable = false)
    private Integer llamadasRealizadas = 0;

    // Alias para repository methods en inglés
    @Column(name = "llamadas_realizadas", insertable = false, updatable = false)
    private Integer callsMade;

    @Column(name = "ultima_llamada")
    private LocalDateTime ultimaLlamada;

    // Alias para repository methods en inglés
    @Column(name = "ultima_llamada", insertable = false, updatable = false)
    private LocalDateTime lastCall;

    // Relaciones
    @OneToMany(mappedBy = "entorno", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<APITestLog> testLogs = new HashSet<>();

    public enum EstadoEntorno {
        ACTIVO, PAUSADO, EXPIRADO, ELIMINADO
    }

    // Métodos helper
    public void incrementarLlamadasRealizadas() {
        this.llamadasRealizadas++;
        this.ultimaLlamada = LocalDateTime.now();
    }

    public void reiniciarContadorLlamadas() {
        this.llamadasRealizadas = 0;
    }

    public boolean haAlcanzadoLimite() {
        return this.llamadasRealizadas >= this.limiteLlamadasDia;
    }

    public boolean estaExpirado() {
        return this.fechaExpiracion != null && LocalDateTime.now().isAfter(this.fechaExpiracion);
    }

    public void pausar() {
        this.estadoEntorno = EstadoEntorno.PAUSADO;
    }

    public void activar() {
        this.estadoEntorno = EstadoEntorno.ACTIVO;
    }

    public void marcarComoExpirado() {
        this.estadoEntorno = EstadoEntorno.EXPIRADO;
    }

    public void eliminar() {
        this.estadoEntorno = EstadoEntorno.ELIMINADO;
    }
}

