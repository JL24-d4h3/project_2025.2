package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Recurso")
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recurso_id", nullable = false)
    private Integer recursoId;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recurso", nullable = false)
    private TipoRecurso tipoRecurso;

    @Column(name = "formato_recurso", length = 10)
    private String formatoRecurso;

    @Column(name = "mime_type", nullable = false, length = 50)
    private String mimeType;

    @ManyToOne
    @JoinColumn(name = "Contenido_contenido_id", nullable = false)
    private Contenido contenido;

    @OneToOne
    @JoinColumn(name = "Enlace_enlace_id", nullable = false)
    private Enlace enlace;

    public enum TipoRecurso {
        TEXTO, IMAGEN, AUDIO, VIDEO, CODIGO, DOCUMENTO, GRAFICA, OTRO
    }
}