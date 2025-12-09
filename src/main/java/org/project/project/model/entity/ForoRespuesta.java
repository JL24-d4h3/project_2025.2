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
@Table(name = "foro_respuesta")
public class ForoRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "respuesta_id", nullable = false)
    private Long respuestaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id", nullable = false)
    private ForoTema tema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_respuesta_id")
    private ForoRespuesta parentRespuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_usuario_id", nullable = false)
    private Usuario autor;

    @Lob
    @Column(name = "contenido_respuesta", nullable = false)
    private String contenidoRespuesta;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "contenido_respuesta", insertable = false, updatable = false)
    private String answerContent;

    @Column(name = "es_solucion", nullable = false)
    private Boolean esSolucion = false;

    // Alias para repository methods en inglés
    @Column(name = "es_solucion", insertable = false, updatable = false)
    private Boolean isSolution;

    @Column(name = "votos_positivos", nullable = false)
    private Integer votosPositivos = 0;

    // Alias para repository methods en inglés
    @Column(name = "votos_positivos", insertable = false, updatable = false)
    private Integer upvotes;

    @Column(name = "votos_negativos", nullable = false)
    private Integer votosNegativos = 0;

    // Alias para repository methods en inglés
    @Column(name = "votos_negativos", insertable = false, updatable = false)
    private Integer downvotes;

    @Column(name = "puntuacion_total", nullable = false)
    private Integer puntuacionTotal = 0;

    // Alias para repository methods en inglés
    @Column(name = "puntuacion_total", insertable = false, updatable = false)
    private Integer totalScore;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Alias para repository methods en inglés
    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Alias para repository methods en inglés
    @Column(name = "fecha_modificacion", insertable = false, updatable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "editado", nullable = false)
    private Boolean editado = false;

    // Alias para repository methods en inglés
    @Column(name = "editado", insertable = false, updatable = false)
    private Boolean isEdited;

    // Relaciones
    @OneToMany(mappedBy = "parentRespuesta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ForoRespuesta> respuestasHijas = new HashSet<>();

    @OneToMany(mappedBy = "respuesta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ForoVoto> votos = new HashSet<>();

    // Métodos helper
    public void marcarComoSolucion() {
        this.esSolucion = true;
    }

    public void desmarcarComoSolucion() {
        this.esSolucion = false;
    }

    public void incrementarVotosPositivos() {
        this.votosPositivos++;
        actualizarPuntuacionTotal();
    }

    public void decrementarVotosPositivos() {
        if (this.votosPositivos > 0) {
            this.votosPositivos--;
            actualizarPuntuacionTotal();
        }
    }

    public void incrementarVotosNegativos() {
        this.votosNegativos++;
        actualizarPuntuacionTotal();
    }

    public void decrementarVotosNegativos() {
        if (this.votosNegativos > 0) {
            this.votosNegativos--;
            actualizarPuntuacionTotal();
        }
    }

    private void actualizarPuntuacionTotal() {
        this.puntuacionTotal = this.votosPositivos - this.votosNegativos;
    }

    public void marcarComoEditado() {
        this.editado = true;
        this.fechaModificacion = LocalDateTime.now();
    }

    public boolean esRespuestaRaiz() {
        return this.parentRespuesta == null;
    }

    public boolean tieneRespuestasHijas() {
        return this.respuestasHijas != null && !this.respuestasHijas.isEmpty();
    }
}

