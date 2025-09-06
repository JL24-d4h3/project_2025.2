package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Documentacion")
public class Documentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "documentacion_id", nullable = false)
    private Integer documentacionId;

    @Column(name = "seccion_documentacion", length = 128)
    private String seccionDocumentacion;

    @ManyToOne
    @JoinColumn(name = "API_api_id", nullable = false)
    private API api;

    @OneToMany(mappedBy = "documentacion")
    private Set<Feedback> feedbacks;

    @OneToMany(mappedBy = "documentacion")
    private Set<Contenido> contenidos;

}