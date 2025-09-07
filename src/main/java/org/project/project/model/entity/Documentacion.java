package org.project.project.model.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Documentacion")
public class Documentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "documentacion_id", nullable = false)
    private Long documentacionId;

    @Column(name = "seccion_documentacion", length = 128)
    private String seccionDocumentacion;

    @OneToOne
    @JoinColumn(name = "API_api_id", unique = true, nullable = false)
    private API api;

    @OneToMany(mappedBy = "documentacion")
    private Set<Feedback> feedbacks;

    @OneToMany(mappedBy = "documentacion")
    private Set<Contenido> contenidos;

}