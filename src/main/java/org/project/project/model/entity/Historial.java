package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Historial")
public class Historial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "historial_id", nullable = false)
    private Integer historialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false)
    private TipoEvento tipoEvento;

    @Column(name = "entidad_afectada", length = 45)
    private String entidadAfectada;

    @Column(name = "id_entidad_afectada")
    private Integer idEntidadAfectada;

    @Lob
    @Column(name = "descripcion_evento", nullable = false)
    private String descripcionEvento;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    @Column(name = "ip_origen", length = 128)
    private String ipOrigen;

    @ManyToOne
    @JoinColumn(name = "Usuario_usuario_id", nullable = false)
    private Usuario usuario;

    public enum TipoEvento {
        CREACION, MODIFICACION, ELIMINACION, LOGIN, LOGOUT
    }
}
