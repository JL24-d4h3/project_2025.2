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
@Table(name = "ticket_comentario")
public class TicketComentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comentario_id", nullable = false)
    private Long comentarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comentario_id")
    private TicketComentario parentComentario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_usuario_id", nullable = false)
    private Usuario autor;

    @Lob
    @Column(name = "contenido_comentario", nullable = false)
    private String contenidoComentario;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "contenido_comentario", insertable = false, updatable = false)
    private String commentContent;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comentario", nullable = false)
    private TipoComentario tipoComentario = TipoComentario.COMENTARIO;

    // Alias para repository methods en inglés
    @Column(name = "tipo_comentario", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TipoComentario commentType;

    // Relaciones
    @OneToMany(mappedBy = "parentComentario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TicketComentario> respuestas = new HashSet<>();

    public enum TipoComentario {
        COMENTARIO, SOLUCION, NOTA_INTERNA
    }

    // Métodos helper
    public void marcarComoSolucion() {
        this.tipoComentario = TipoComentario.SOLUCION;
    }

    public void marcarComoNotaInterna() {
        this.tipoComentario = TipoComentario.NOTA_INTERNA;
    }

    public void marcarComoEditado() {
        this.editado = true;
        this.fechaModificacion = LocalDateTime.now();
    }

    public boolean esSolucion() {
        return TipoComentario.SOLUCION.equals(this.tipoComentario);
    }

    public boolean esNotaInterna() {
        return TipoComentario.NOTA_INTERNA.equals(this.tipoComentario);
    }

    public boolean esComentarioRaiz() {
        return this.parentComentario == null;
    }

    public boolean tieneRespuestas() {
        return this.respuestas != null && !this.respuestas.isEmpty();
    }
}

