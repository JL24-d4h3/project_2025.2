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
@Table(name = "foro_tema")
public class ForoTema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tema_id", nullable = false)
    private Long temaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_usuario_id", nullable = false)
    private Usuario autor;

    @Column(name = "titulo_tema", nullable = false, length = 500)
    private String tituloTema;

    // Alias para repository methods en inglés
    @Column(name = "titulo_tema", insertable = false, updatable = false)
    private String topicTitle;

    @Lob
    @Column(name = "contenido_tema", nullable = false)
    private String contenidoTema;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "contenido_tema", insertable = false, updatable = false)
    private String topicContent;

    @Column(name = "slug", nullable = false, unique = true, length = 600)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_tema", nullable = false)
    private EstadoTema estadoTema = EstadoTema.ABIERTO;

    // Alias para repository methods en inglés
    @Column(name = "estado_tema", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EstadoTema topicStatus;

    @Column(name = "es_anclado", nullable = false)
    private Boolean esAnclado = false;

    // Alias para repository methods en inglés
    @Column(name = "es_anclado", insertable = false, updatable = false)
    private Boolean isPinned;

    @Column(name = "es_bloqueado", nullable = false)
    private Boolean esBloqueado = false;

    // Alias para repository methods en inglés
    @Column(name = "es_bloqueado", insertable = false, updatable = false)
    private Boolean isLocked;

    @Column(name = "vistas", nullable = false)
    private Integer vistas = 0;

    // Alias para repository methods en inglés
    @Column(name = "vistas", insertable = false, updatable = false)
    private Integer views;

    @Column(name = "respuestas_count", nullable = false)
    private Integer respuestasCount = 0;

    // Alias para repository methods en inglés
    @Column(name = "respuestas_count", insertable = false, updatable = false)
    private Integer answersCount;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Alias para repository methods en inglés
    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Alias para repository methods en inglés
    @Column(name = "fecha_actualizacion", insertable = false, updatable = false)
    private LocalDateTime updatedDate;

    @Column(name = "ultima_respuesta_fecha")
    private LocalDateTime ultimaRespuestaFecha;

    // Alias para repository methods en inglés
    @Column(name = "ultima_respuesta_fecha", insertable = false, updatable = false)
    private LocalDateTime lastAnswerDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ultima_respuesta_usuario_id")
    private Usuario ultimaRespuestaUsuario;

    // Relaciones
    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ForoRespuesta> respuestas = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "categoria_has_foro_tema",
        joinColumns = @JoinColumn(name = "foro_tema_tema_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id_categoria")
    )
    private Set<Categoria> categorias = new HashSet<>();

    public enum EstadoTema {
        ABIERTO, CERRADO, RESUELTO, ARCHIVADO
    }

    // Métodos helper
    public void incrementarVistas() {
        this.vistas++;
    }

    public void incrementarRespuestasCount() {
        this.respuestasCount++;
    }

    public void decrementarRespuestasCount() {
        if (this.respuestasCount > 0) {
            this.respuestasCount--;
        }
    }

    public void marcarComoResuelto() {
        this.estadoTema = EstadoTema.RESUELTO;
    }

    public void cerrar() {
        this.estadoTema = EstadoTema.CERRADO;
    }

    public void anclar() {
        this.esAnclado = true;
    }

    public void desanclar() {
        this.esAnclado = false;
    }

    public void bloquear() {
        this.esBloqueado = true;
    }

    public void desbloquear() {
        this.esBloqueado = false;
    }
}

