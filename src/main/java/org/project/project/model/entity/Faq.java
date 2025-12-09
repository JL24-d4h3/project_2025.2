package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "faq")
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faq_id", nullable = false)
    private Long faqId;

    @Lob
    @Column(name = "pregunta", nullable = false)
    private String pregunta;

    // Alias en inglés para repository methods
    @Lob
    @Column(name = "pregunta", insertable = false, updatable = false)
    private String question;

    @Lob
    @Column(name = "respuesta", nullable = false)
    private String respuesta;

    // Alias en inglés para repository methods
    @Lob
    @Column(name = "respuesta", insertable = false, updatable = false)
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_faq", nullable = false)
    private CategoriaFaq categoriaFaq = CategoriaFaq.GENERAL;

    // Alias en inglés para repository methods
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_faq", insertable = false, updatable = false)
    private CategoriaFaq faqCategory;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    // Alias en inglés para repository methods
    @Column(name = "orden", insertable = false, updatable = false)
    private Integer order;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // Alias en inglés para repository methods
    @Column(name = "activo", insertable = false, updatable = false)
    private Boolean active;

    @Column(name = "vistas", nullable = false)
    private Integer vistas = 0;

    // Alias en inglés para repository methods
    @Column(name = "vistas", insertable = false, updatable = false)
    private Integer views;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    // Alias en inglés para repository methods
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", insertable = false, updatable = false)
    private Usuario author;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Alias en inglés para repository methods
    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime creationDate;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Alias en inglés para repository methods
    @Column(name = "fecha_modificacion", insertable = false, updatable = false)
    private LocalDateTime modificationDate;

    public enum CategoriaFaq {
        GENERAL, API, PROYECTO, REPOSITORIO, AUTENTICACION, TICKET, FORO, OTRO
    }
}