package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "recurso")
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recurso_id", nullable = false)
    private Long recursoId;

    // Alias en inglés para repository methods
    @Column(name = "recurso_id", insertable = false, updatable = false)
    private Long resourceId;
    
    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    // Alias en inglés para repository methods
    @Column(name = "nombre_archivo", insertable = false, updatable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recurso", nullable = false)
    private TipoRecurso tipoRecurso;

    // Alias en inglés para repository methods
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recurso", insertable = false, updatable = false)
    private TipoRecurso resourceType;

    @Column(name = "formato_recurso", length = 10)
    private String formatoRecurso;

    // Alias en inglés para repository methods
    @Column(name = "formato_recurso", insertable = false, updatable = false)
    private String resourceFormat;

    @Column(name = "mime_type", nullable = false, length = 50)
    private String mimeType;

    // FASE 0.2: Contenido Markdown almacenado en BD (< 64KB)
    // Si este campo es NULL, el contenido está en GCS (enlace.direccionAlmacenamiento)
    // Si tiene valor, el contenido se lee directamente de BD (evita descarga GCS)
    @Column(name = "markdown_content", columnDefinition = "TEXT")
    private String markdownContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenido_contenido_id", nullable = false)
    private Contenido contenido;

    // Alias en inglés para repository methods
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenido_contenido_id", insertable = false, updatable = false)
    private Contenido content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enlace_enlace_id", nullable = false)
    private Enlace enlace;

    // Alias en inglés para repository methods
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enlace_enlace_id", insertable = false, updatable = false)
    private Enlace link;

    public enum TipoRecurso {
        TEXTO, IMAGEN, AUDIO, VIDEO, CODIGO, DOCUMENTO, GRAFICA, OTRO
    }
}