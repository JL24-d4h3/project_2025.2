package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Impersonacion")
public class Impersonacion {

    @Id
    @Column(name = "idImpersonacion", nullable = false)
    private Integer idImpersonacion;

    @Column(name = "fecha_inicio_impersonacion", nullable = false)
    private LocalDateTime fechaInicioImpersonacion;

    @Column(name = "fecha_fin_impersonacion")
    private LocalDateTime fechaFinImpersonacion;

    @ManyToOne
    @JoinColumn(name = "usuario_usuario_id", nullable = false)
    private Usuario usuario;

}
