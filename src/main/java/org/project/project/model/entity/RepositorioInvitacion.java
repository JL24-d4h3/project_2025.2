package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "repositorio_invitaciones")
@Data
@NoArgsConstructor
public class RepositorioInvitacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitacion_id")
    private Long invitacionId;
    
    @ManyToOne
    @JoinColumn(name = "repositorio_id", nullable = false)
    private Repositorio repositorio;
    
    @ManyToOne
    @JoinColumn(name = "usuario_invitado_id", nullable = false)
    private Usuario usuarioInvitado;
    
    @ManyToOne
    @JoinColumn(name = "invitado_por_usuario_id", nullable = false)
    private Usuario invitadoPor;
    
    @Column(name = "permiso", length = 50, nullable = false)
    private String permiso; // LECTOR o EDITOR
    
    @Column(name = "equipos_json", columnDefinition = "TEXT")
    private String equiposJson; // JSON array de equipo IDs
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoInvitacion estado = EstadoInvitacion.PENDIENTE;
    
    @Column(name = "fecha_invitacion", nullable = false)
    private LocalDateTime fechaInvitacion;
    
    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
    
    @Column(name = "token", length = 255, unique = true)
    private String token;
    
    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;
    
    public enum EstadoInvitacion {
        PENDIENTE,
        ACEPTADA,
        RECHAZADA,
        EXPIRADA
    }
    
    @PrePersist
    public void prePersist() {
        if (fechaInvitacion == null) {
            fechaInvitacion = LocalDateTime.now();
        }
        if (fechaExpiracion == null) {
            fechaExpiracion = LocalDateTime.now().plusDays(7);
        }
    }
}
