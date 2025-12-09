package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "nodo_favorite")
public class NodoFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long favoriteId;
    
    // Alias para repository methods en inglés
    @Column(name = "favorite_id", insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    // Alias para repository methods en inglés
    @Column(name = "usuario_id", insertable = false, updatable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodo_id", nullable = false)
    private Nodo nodo;
    
    // Alias para repository methods en inglés
    @Column(name = "nodo_id", insertable = false, updatable = false)
    private Long nodeId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "custom_label")
    private String customLabel; // Alias personalizado opcional

    public NodoFavorite() {}

    public NodoFavorite(Usuario usuario, Nodo nodo) {
        this.usuario = usuario;
        this.nodo = nodo;
        this.createdAt = LocalDateTime.now();
    }

    public NodoFavorite(Usuario usuario, Nodo nodo, String customLabel) {
        this.usuario = usuario;
        this.nodo = nodo;
        this.customLabel = customLabel;
        this.createdAt = LocalDateTime.now();
    }

    public String getDisplayName() {
        if (customLabel != null && !customLabel.isEmpty()) {
            return customLabel;
        }
        return nodo != null ? nodo.getNombre() : "";
    }

    @Override
    public String toString() {
        return "NodoFavorite{" +
                "favoriteId=" + favoriteId +
                ", userId=" + (usuario != null ? usuario.getUsuarioId() : null) +
                ", nodoId=" + (nodo != null ? nodo.getNodoId() : null) +
                ", customLabel='" + customLabel + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
