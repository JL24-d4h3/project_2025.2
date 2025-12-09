package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "equipo")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipo_id", nullable = false)
    private Long equipoId;
    
    // Alias para repository methods en inglés
    @Column(name = "equipo_id", insertable = false, updatable = false)
    private Long teamId;

    @Column(name = "nombre_equipo", nullable = false, length = 45)
    private String nombreEquipo;
    
    // Alias para repository methods en inglés
    @Column(name = "nombre_equipo", insertable = false, updatable = false)
    private String teamName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_usuario_id", nullable = true)
    private Usuario creadoPor;

    @Column(name = "fecha_creacion", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;

    @ManyToMany(mappedBy = "equipos")
    private Set<Usuario> usuarios;

}
