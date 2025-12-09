package org.project.project.model.entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "documentacion")
public class Documentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "documentacion_id", nullable = false)
    private Long documentacionId;
    
    // Alias para repository methods en inglés
    @Column(name = "documentacion_id", insertable = false, updatable = false)
    private Long documentationId;

    @Column(name = "seccion_documentacion", length = 128)
    private String seccionDocumentacion;
    
    // Alias para repository methods en inglés
    @Column(name = "seccion_documentacion", insertable = false, updatable = false)
    private String documentationSection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_api_id", nullable = false)
    private API api;

    // Auditoría
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;
    
    // Alias para repository methods en inglés
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", insertable = false, updatable = false)
    private Usuario createdBy;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
    
    // Alias para repository methods en inglés
    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;
    
    // Alias para repository methods en inglés
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por", insertable = false, updatable = false)
    private Usuario updatedBy;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
    
    // Alias para repository methods en inglés
    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "documentacion")
    private Set<Feedback> feedbacks;

    @OneToMany(mappedBy = "documentacion")
    private Set<Contenido> contenidos;

}