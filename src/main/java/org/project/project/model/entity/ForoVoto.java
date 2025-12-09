package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "foro_voto")
public class ForoVoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voto_id", nullable = false)
    private Long votoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respuesta_id", nullable = false)
    private ForoRespuesta respuesta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_voto", nullable = false)
    private TipoVoto tipoVoto;

    // Alias para repository methods en inglés
    @Column(name = "tipo_voto", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TipoVoto voteType;

    @Column(name = "fecha_voto", nullable = false)
    private LocalDateTime fechaVoto = LocalDateTime.now();

    // Alias para repository methods en inglés
    @Column(name = "fecha_voto", insertable = false, updatable = false)
    private LocalDateTime voteDate;

    public enum TipoVoto {
        POSITIVO, NEGATIVO
    }

    // Métodos helper
    public boolean esVotoPositivo() {
        return TipoVoto.POSITIVO.equals(this.tipoVoto);
    }

    public boolean esVotoNegativo() {
        return TipoVoto.NEGATIVO.equals(this.tipoVoto);
    }

    public void cambiarTipoVoto(TipoVoto nuevoTipo) {
        this.tipoVoto = nuevoTipo;
        this.fechaVoto = LocalDateTime.now();
    }
}

