package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "clipboard_operation")
public class ClipboardOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clipboard_id")
    private Long clipboardId;
    
    // Alias para repository methods en inglés
    @Column(name = "clipboard_id", insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    // Alias para repository methods en inglés
    @Column(name = "usuario_id", insertable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "nodo_ids", nullable = false, columnDefinition = "JSON")
    private List<Long> nodoIds;
    
    // Alias para repository methods en inglés
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "nodo_ids", insertable = false, updatable = false, columnDefinition = "JSON")
    private List<Long> nodeIds;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_container_type", nullable = false)
    private Nodo.ContainerType sourceContainerType;

    @Column(name = "source_container_id", nullable = false)
    private Long sourceContainerId;

    @Column(name = "source_parent_id")
    private Long sourceParentId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired = false;

    public enum OperationType {
        COPY,  // Duplicar archivos
        CUT    // Mover archivos
    }

    public ClipboardOperation() {}

    public ClipboardOperation(Usuario usuario, OperationType operationType, List<Long> nodoIds,
                            Nodo.ContainerType sourceContainerType, Long sourceContainerId, Long sourceParentId) {
        this.usuario = usuario;
        this.operationType = operationType;
        this.nodoIds = nodoIds;
        this.sourceContainerType = sourceContainerType;
        this.sourceContainerId = sourceContainerId;
        this.sourceParentId = sourceParentId;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24); // Expira en 24 horas
        this.isExpired = false;
    }

    public void markAsExpired() {
        this.isExpired = true;
    }

    public boolean isActive() {
        return !isExpired && LocalDateTime.now().isBefore(expiresAt);
    }

    @Override
    public String toString() {
        return "ClipboardOperation{" +
                "clipboardId=" + clipboardId +
                ", userId=" + (usuario != null ? usuario.getUsuarioId() : null) +
                ", operationType=" + operationType +
                ", nodoIdsCount=" + (nodoIds != null ? nodoIds.size() : 0) +
                ", isExpired=" + isExpired +
                '}';
    }
}
