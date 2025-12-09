package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "credencial_api")
public class CredencialAPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credencial_id", nullable = false)
    private Long credencialId;
    
    // Alias para repository methods en inglés
    @Column(name = "credencial_id", insertable = false, updatable = false)
    private Long credentialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entorno_credencial", nullable = false)
    private EntornoCredencial entornoCredencial;
    
    // Alias para repository methods en inglés
    @Enumerated(EnumType.STRING)
    @Column(name = "entorno_credencial", insertable = false, updatable = false)
    private EntornoCredencial credentialEnvironment;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_credencial", nullable = false)
    private TipoCredencial tipoCredencial;
    
    // Alias para repository methods en inglés
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_credencial", insertable = false, updatable = false)
    private TipoCredencial credentialType;

    @Column(name = "valor_publico", nullable = false, length = 255)
    private String valorPublico;
    
    // Alias para repository methods en inglés
    @Column(name = "valor_publico", insertable = false, updatable = false)
    private String publicValue;

    @Column(name = "valor_secreto_hash", nullable = false, length = 255)
    private String valorSecretoHash;
    
    // Alias para repository methods en inglés
    @Column(name = "valor_secreto_hash", insertable = false, updatable = false)
    private String secretValueHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_credencial", nullable = false)
    private EstadoCredencial estadoCredencial;
    
    // Alias para repository methods en inglés
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_credencial", insertable = false, updatable = false)
    private EstadoCredencial credentialStatus;

    @Column(name = "fecha_creacion_credencial", nullable = false)
    private LocalDateTime fechaCreacionCredencial = LocalDateTime.now();

    // Alias para repository methods en inglés
    @Column(name = "fecha_creacion_credencial", insertable = false, updatable = false)
    private LocalDateTime credentialCreationDate;

    @Column(name = "fecha_vencimiento_credencial")
    private LocalDateTime fechaVencimientoCredencial;
    
    // Alias para repository methods en inglés
    @Column(name = "fecha_vencimiento_credencial", insertable = false, updatable = false)
    private LocalDateTime credentialExpirationDate;

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