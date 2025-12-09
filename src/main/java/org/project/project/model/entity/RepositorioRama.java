package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad para representar las ramas (branches) de un repositorio.
 * Similar al concepto de branches en Git/GitHub.
 * 
 * Cada repositorio puede tener múltiples ramas (main, develop, feature-*, etc.)
 * pero solo una puede ser la rama principal (is_principal = true).
 * 
 * Los nodos (archivos/carpetas) están asociados a una rama específica,
 * permitiendo navegación independiente por rama como en GitHub.
 */
@Getter
@Setter
@Entity
@Table(name = "repositorio_rama")
public class RepositorioRama {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rama_id", nullable = false)
    private Long ramaId;

    @Column(name = "repositorio_id", nullable = false)
    private Long repositorioId;

    @Column(name = "nombre_rama", nullable = false, length = 100)
    private String nombreRama;

    @Lob
    @Column(name = "descripcion_rama")
    private String descripcionRama;

    @Column(name = "is_principal", nullable = false)
    private Boolean isPrincipal = false;

    @Column(name = "is_protegida", nullable = false)
    private Boolean isProtegida = false;

    @Column(name = "ultimo_commit_hash", length = 128)
    private String ultimoCommitHash;

    @Column(name = "ultimo_commit_fecha")
    private LocalDateTime ultimoCommitFecha;

    @Lob
    @Column(name = "ultimo_commit_mensaje")
    private String ultimoCommitMensaje;

    @Column(name = "ultimo_commit_autor", length = 255)
    private String ultimoCommitAutor;

    @Column(name = "creada_por_usuario_id")
    private Long creadaPorUsuarioId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "actualizada_en")
    private LocalDateTime actualizadaEn;

    // =================== RELACIONES ===================

    /**
     * Relación con el repositorio al que pertenece esta rama
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repositorio_id", insertable = false, updatable = false)
    private Repositorio repositorio;

    /**
     * Relación con el usuario que creó la rama
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creada_por_usuario_id", insertable = false, updatable = false)
    private Usuario creador;

    // =================== MÉTODOS DE UTILIDAD ===================

    /**
     * Verifica si esta es la rama principal del repositorio
     */
    public boolean esPrincipal() {
        return isPrincipal != null && isPrincipal;
    }

    /**
     * Verifica si esta rama está protegida (no se puede eliminar fácilmente)
     */
    public boolean estaProtegida() {
        return isProtegida != null && isProtegida;
    }

    /**
     * Obtiene el nombre corto del último commit (primeros 7 caracteres del hash)
     */
    public String getUltimoCommitHashCorto() {
        if (ultimoCommitHash != null && ultimoCommitHash.length() >= 7) {
            return ultimoCommitHash.substring(0, 7);
        }
        return ultimoCommitHash;
    }

    /**
     * Obtiene el primer línea del mensaje del último commit
     */
    public String getUltimoCommitMensajeCorto() {
        if (ultimoCommitMensaje != null) {
            int newlineIndex = ultimoCommitMensaje.indexOf('\n');
            if (newlineIndex > 0) {
                return ultimoCommitMensaje.substring(0, newlineIndex);
            }
            return ultimoCommitMensaje.length() > 100 
                ? ultimoCommitMensaje.substring(0, 100) + "..." 
                : ultimoCommitMensaje;
        }
        return null;
    }

    @Override
    public String toString() {
        return "RepositorioRama{" +
                "ramaId=" + ramaId +
                ", repositorioId=" + repositorioId +
                ", nombreRama='" + nombreRama + '\'' +
                ", isPrincipal=" + isPrincipal +
                ", isProtegida=" + isProtegida +
                '}';
    }
}
