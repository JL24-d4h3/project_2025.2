package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id", nullable = false)
    private Long tokenId;

    @Column(name = "valor_token", length = 512)
    private String valorToken;
    
    // Alias en inglés para repository methods
    @Column(name = "valor_token", length = 512, insertable = false, updatable = false)
    private String tokenValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_token")
    private EstadoToken estadoToken = EstadoToken.ACTIVO;
    
    // Alias en inglés para repository methods
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_token", insertable = false, updatable = false)
    private EstadoToken tokenStatus = EstadoToken.ACTIVO;
    
    @Column(name = "fecha_creacion_token")
    private LocalDateTime fechaCreacionToken;
    
    // Alias en inglés para repository methods
    @Column(name = "fecha_creacion_token", insertable = false, updatable = false)
    private LocalDateTime creationDate;

    @Column(name = "fecha_expiracion_token")
    private LocalDateTime fechaExpiracionToken;
    
    // Alias en inglés para repository methods
    @Column(name = "fecha_expiracion_token", insertable = false, updatable = false)
    private LocalDateTime expirationDate;

    @ManyToOne
    @JoinColumn(name = "usuario_usuario_id", nullable = false)
    private Usuario usuario;

    // Enums
    public enum EstadoToken {
        ACTIVO, REVOCADO
    }
}