package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for chatbot_conversacion table
 * Manages AI chatbot conversations with users
 */
@Getter
@Setter
@Entity
@Table(name = "chatbot_conversacion")
public class ChatbotConversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversacion_id", nullable = false)
    private Long conversacionId;

    // English alias for repositories
    @Column(name = "conversacion_id", insertable = false, updatable = false)
    private Long conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "titulo_conversacion", length = 255)
    private String tituloConversacion;

    // English alias for repositories
    @Column(name = "titulo_conversacion", length = 255, insertable = false, updatable = false)
    private String conversationTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_conversacion", nullable = false)
    private EstadoConversacion estadoConversacion = EstadoConversacion.ACTIVA;

    // English alias for repositories
    @Column(name = "estado_conversacion", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EstadoConversacion conversationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "tema_conversacion")
    private TemaConversacion temaConversacion = TemaConversacion.GENERAL;

    // English alias for repositories
    @Column(name = "tema_conversacion", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TemaConversacion conversationTopic;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio = LocalDateTime.now();

    // English alias for repositories
    @Column(name = "fecha_inicio", insertable = false, updatable = false)
    private LocalDateTime startDate;

    @Column(name = "fecha_ultimo_mensaje")
    private LocalDateTime fechaUltimoMensaje;

    // English alias for repositories
    @Column(name = "fecha_ultimo_mensaje", insertable = false, updatable = false)
    private LocalDateTime lastMessageDate;

    @Column(name = "mensajes_count", nullable = false)
    private Integer mensajesCount = 0;

    // English alias for repositories
    @Column(name = "mensajes_count", insertable = false, updatable = false)
    private Integer messagesCount;

    @Column(name = "tokens_totales_usados", nullable = false)
    private Integer tokensTotalesUsados = 0;

    // English alias for repositories
    @Column(name = "tokens_totales_usados", insertable = false, updatable = false)
    private Integer totalTokensUsed;

    @Column(name = "modelo_ia_usado", length = 50)
    private String modeloIaUsado;

    // English alias for repositories
    @Column(name = "modelo_ia_usado", length = 50, insertable = false, updatable = false)
    private String aiModelUsed;

    @Column(name = "nosql_mensajes_id", length = 24)
    private String nosqlMensajesId;

    // English alias for repositories
    @Column(name = "nosql_mensajes_id", length = 24, insertable = false, updatable = false)
    private String nosqlMessagesId;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    // English alias for repositories
    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "mensaje_usuario", nullable = false, columnDefinition = "TEXT")
    private String mensajeUsuario;

    @Column(name = "respuesta_bot", nullable = false, columnDefinition = "TEXT")
    private String respuestaBot;

    @Column(name = "fecha_conversacion", nullable = false)
    private LocalDateTime fechaConversacion = LocalDateTime.now();

    @Column(name = "contexto", columnDefinition = "TEXT")
    private String contexto;

    @Column(name = "intento_resuelto", nullable = false)
    private Boolean intentoResuelto = false;

    @ManyToOne
    @JoinColumn(name = "ticket_generado_id")
    private Ticket ticketGenerado;

    public enum EstadoConversacion { ACTIVA, CERRADA, ARCHIVADA }
    public enum TemaConversacion { API, PROYECTO, REPOSITORIO, AUTENTICACION, ERROR, GENERAL }
}
