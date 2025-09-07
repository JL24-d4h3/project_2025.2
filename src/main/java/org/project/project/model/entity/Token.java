package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id", nullable = false)
    private Integer tokenId;

    @Column(name = "valor_token", length = 45)
    private String valorToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_token")
    private EstadoToken estadoToken;

    @Column(name = "fecha_creacion_token")
    private LocalDateTime fechaCreacionToken;

    @Column(name = "fecha_expiracion_token")
    private LocalDateTime fechaExpiracionToken;

    @ManyToOne
    @JoinColumn(name = "Usuario_usuario_id", nullable = false)
    private Usuario usuario;

    public enum EstadoToken {
        ACTIVO, REVOCADO
    }
}
