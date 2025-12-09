package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "proyecto_has_repositorio", indexes = {
    @Index(name = "fk_proyecto_has_repositorio_repositorio1_idx", columnList = "repositorio_repositorio_id"),
    @Index(name = "fk_proyecto_has_repositorio_proyecto1_idx", columnList = "proyecto_proyecto_id")
})
public class ProyectoHasRepositorio {

    @EmbeddedId
    private ProyectoHasRepositorioId id;

    @MapsId("proyectoId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_proyecto_id", nullable = false)
    private Proyecto proyecto;

    @MapsId("repositorioId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repositorio_repositorio_id", nullable = false)
    private Repositorio repositorio;

    public ProyectoHasRepositorio() {}

    public ProyectoHasRepositorio(Proyecto proyecto, Repositorio repositorio) {
        this.proyecto = proyecto;
        this.repositorio = repositorio;
        this.id = new ProyectoHasRepositorioId(proyecto.getProyectoId(), repositorio.getRepositorioId());
    }
    
    // Alias getters para repository methods en inglés
    public Proyecto getProject() {
        return proyecto;
    }
    
    public Repositorio getRepository() {
        return repositorio;
    }
}