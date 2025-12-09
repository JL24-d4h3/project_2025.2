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
@Table(name = "reporte")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reporte_id", nullable = false)
    private Long reporteId;

    @Column(name = "titulo_reporte", nullable = false, length = 255)
    private String tituloReporte;

    // Alias para repository methods en inglés
    @Column(name = "titulo_reporte", insertable = false, updatable = false)
    private String reportTitle;

    @Lob
    @Column(name = "descripcion_reporte")
    private String descripcionReporte;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "descripcion_reporte", insertable = false, updatable = false)
    private String reportDescription;

    @Lob
    @Column(name = "contenido_reporte", nullable = false)
    private String contenidoReporte;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "contenido_reporte", insertable = false, updatable = false)
    private String reportContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_reporte", nullable = false)
    private TipoReporte tipoReporte;

    // Alias para repository methods en inglés
    @Column(name = "tipo_reporte", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TipoReporte reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_reporte", nullable = false)
    private EstadoReporte estadoReporte = EstadoReporte.BORRADOR;

    // Alias para repository methods en inglés
    @Column(name = "estado_reporte", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EstadoReporte reportStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_usuario_id", nullable = false)
    private Usuario autor;
    
    // Alias para repository methods en inglés
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_usuario_id", insertable = false, updatable = false)
    private Usuario author;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
    
    // Alias para repository methods en inglés
    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn = LocalDateTime.now();
    
    // Alias para repository methods en inglés
    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;
    
    // Alias para repository methods en inglés
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por", insertable = false, updatable = false)
    private Usuario updatedBy;

    // Relaciones
    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ReporteAdjunto> adjuntos = new HashSet<>();

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UsuarioHasReporte> colaboradores = new HashSet<>();

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ReporteHasTicket> ticketsRelacionados = new HashSet<>();

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ReporteHasApi> apisRelacionadas = new HashSet<>();

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ReporteHasProyecto> proyectosRelacionados = new HashSet<>();

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ReporteHasRepositorio> repositoriosRelacionados = new HashSet<>();

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ReporteHasDocumentacion> documentacionRelacionada = new HashSet<>();

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ReporteHasForoTema> forumRelacionado = new HashSet<>();

    public enum TipoReporte {
        TICKET, API, PROYECTO, REPOSITORIO, DOCUMENTACION, FORO, GENERAL
    }

    public enum EstadoReporte {
        BORRADOR, PUBLICADO, REVISADO, ARCHIVADO
    }

    // Métodos helper
    public void publicar() {
        this.estadoReporte = EstadoReporte.PUBLICADO;
        this.actualizadoEn = LocalDateTime.now();
    }

    public void marcarComoRevisado() {
        this.estadoReporte = EstadoReporte.REVISADO;
        this.actualizadoEn = LocalDateTime.now();
    }

    public void archivar() {
        this.estadoReporte = EstadoReporte.ARCHIVADO;
        this.actualizadoEn = LocalDateTime.now();
    }

    public void volverABorrador() {
        this.estadoReporte = EstadoReporte.BORRADOR;
        this.actualizadoEn = LocalDateTime.now();
    }

    public boolean esBorrador() {
        return EstadoReporte.BORRADOR.equals(this.estadoReporte);
    }

    public boolean estaPublicado() {
        return EstadoReporte.PUBLICADO.equals(this.estadoReporte);
    }

    public void actualizar() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
