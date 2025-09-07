package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Credencial_API")
public class CredencialAPI {

    @Id
    @Column(name = "credencial_id", nullable = false)
    private Integer credencialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entorno_credencial", nullable = false)
    private EntornoCredencial entornoCredencial;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_credencial", nullable = false)
    private TipoCredencial tipoCredencial;

    @Column(name = "valor_publico", nullable = false, length = 255)
    private String valorPublico;

    @Column(name = "valor_secreto_hash", nullable = false, length = 255)
    private String valorSecretoHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_credencial", nullable = false)
    private EstadoCredencial estadoCredencial;

    @Column(name = "fecha_creacion_credencial", nullable = false)
    private LocalDateTime fechaCreacionCredencial;

    @Column(name = "fecha_vencimiento_credencial")
    private LocalDateTime fechaVencimientoCredencial;

    @ManyToOne
    @JoinColumn(name = "api_api_id", nullable = false)
    private API api;

    public enum EntornoCredencial {
        SANDBOX, QA, PROD
    }

    public enum TipoCredencial {
        API_KEY, OAUTH_CLIENT
    }

    public enum EstadoCredencial {
        ACTIVO, REVOCADO
    }
}