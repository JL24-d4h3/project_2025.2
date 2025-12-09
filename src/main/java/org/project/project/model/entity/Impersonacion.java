package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "impersonacion")
public class Impersonacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "impersonacion_id", nullable = false)
    private Long impersonacionId;
    
    // Alias para repository methods en inglés
    @Column(name = "impersonacion_id", insertable = false, updatable = false)
    private Long impersonationId;

    @Column(name = "fecha_inicio_impersonacion", nullable = false)
    private LocalDateTime fechaInicioImpersonacion = LocalDateTime.now();
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_inicio_impersonacion", insertable = false, updatable = false)
    private LocalDateTime impersonationStartDate;

    @Column(name = "fecha_fin_impersonacion")
    private LocalDateTime fechaFinImpersonacion;
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_fin_impersonacion", insertable = false, updatable = false)
    private LocalDateTime impersonationEndDate;

//    @Column(name = "motivo", columnDefinition = "TEXT")
//    private String motivo;
//
//    // Alias en inglés para repository methods
//    @Column(name = "motivo", insertable = false, updatable = false)
//    private String reason;

    @ManyToOne
    @JoinColumn(name = "usuario_usuario_id", nullable = false)
    private Usuario usuario;

    // Constructores
    public Impersonacion() {}

    public Impersonacion(Usuario usuario) {
        this.usuario = usuario;
        this.fechaInicioImpersonacion = LocalDateTime.now();
    }

    public void finalizarImpersonacion() {
        this.fechaFinImpersonacion = LocalDateTime.now();
    }

    public boolean estaActiva() {
        return fechaFinImpersonacion == null;
    }
}
