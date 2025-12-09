package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "historial")
public class Historial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "historial_id", nullable = false)
    private Long historialId;
    
    // Alias para repository methods en inglés
    @Column(name = "historial_id", insertable = false, updatable = false)
    private Long historyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false)
    private TipoEvento tipoEvento;
    
    // Alias para repository methods en inglés
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", insertable = false, updatable = false)
    private TipoEvento eventType;

    @Column(name = "entidad_afectada", length = 45)
    private String entidadAfectada;
    
    // Alias para repository methods en inglés
    @Column(name = "entidad_afectada", insertable = false, updatable = false)
    private String affectedEntity;

    @Column(name = "id_entidad_afectada")
    private Integer idEntidadAfectada;
    
    // Alias para repository methods en inglés
    @Column(name = "id_entidad_afectada", insertable = false, updatable = false)
    private Integer affectedEntityId;

    @Lob
    @Column(name = "descripcion_evento", nullable = false)
    private String descripcionEvento;
    
    // Alias para repository methods en inglés
    @Lob
    @Column(name = "descripcion_evento", insertable = false, updatable = false)
    private String eventDescription;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_evento", insertable = false, updatable = false)
    private LocalDateTime eventDate;

    @Column(name = "ip_origen", length = 128)
    private String ipOrigen;
    
    // Alias para repository methods en inglés
    @Column(name = "ip_origen", insertable = false, updatable = false)
    private String originIp;

    @ManyToOne
    @JoinColumn(name = "usuario_usuario_id", nullable = false)
    private Usuario usuario;

    public enum TipoEvento {
        CREACION, MODIFICACION, ELIMINACION, LOGIN, LOGOUT
    }
}
