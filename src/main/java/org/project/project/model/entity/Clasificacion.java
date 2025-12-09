package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "clasificacion")
public class Clasificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clasificacion_id", nullable = false)
    private Long clasificacionId;
    
    // Alias para repository methods en inglés
    @Column(name = "clasificacion_id", insertable = false, updatable = false)
    private Long classificationId;

    @Column(name = "tipo_contenido_texto", length = 45)
    private String tipoContenidoTexto;
    
    // Alias para repository methods en inglés
    @Column(name = "tipo_contenido_texto", insertable = false, updatable = false)
    private String contentTypeText;

    @OneToMany(mappedBy = "clasificacion")
    private Set<Contenido> contenidos;

}