package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Adjunto")
public class Adjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adjunto_id", nullable = false)
    private Integer adjuntoId;

    @Column(name = "nombre_archivo_adjunto", length = 128)
    private String nombreArchivoAdjunto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_archivo_adjunto", nullable = false)
    private TipoArchivoAdjunto tipoArchivoAdjunto;

    @Lob
    @Column(name = "url_archivo_adjunto", nullable = false)
    private String urlArchivoAdjunto;

    @ManyToOne
    @JoinColumn(name = "Mensaje_mensaje_id", nullable = false)
    private Mensaje mensaje;

    public enum TipoArchivoAdjunto {
        PDF, TXT, IMAGEN, DOC, OTRO
    }
}