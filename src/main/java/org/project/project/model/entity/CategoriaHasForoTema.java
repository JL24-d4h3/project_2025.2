package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "categoria_has_foro_tema")
public class CategoriaHasForoTema {

    @EmbeddedId
    private CategoriaHasForoTemaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoriaIdCategoria")
    @JoinColumn(name = "categoria_id_categoria")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("foroTemaTemaId")
    @JoinColumn(name = "foro_tema_tema_id")
    private ForoTema foroTema;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion = LocalDateTime.now();

    // Alias para repository methods en inglés
    @Column(name = "fecha_asignacion", insertable = false, updatable = false)
    private LocalDateTime assignmentDate;

    // Constructors
    public CategoriaHasForoTema() {
    }

    public CategoriaHasForoTema(Categoria categoria, ForoTema foroTema) {
        this.categoria = categoria;
        this.foroTema = foroTema;
        this.id = new CategoriaHasForoTemaId(categoria.getIdCategoria(), foroTema.getTemaId());
        this.fechaAsignacion = LocalDateTime.now();
    }
}

