package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;
import java.util.HashSet;

@Getter
@Setter
@Entity
@Table(name = "nodo_tag_master")
public class NodoTagMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "nombre_tag_master", nullable = false, unique = true, length = 100)
    private String nombre;
    
    // Alias para repository methods en inglés
    @Column(name = "nombre_tag_master", insertable = false, updatable = false)
    private String name;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    // Alias en inglés para repository methods
    @Column(name = "descripcion", insertable = false, updatable = false)
    private String description;

    @Column(name = "color", length = 7)
    private String color;

    // Relationships
    @OneToMany(mappedBy = "tagMaster", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<NodoTag> nodoTags = new HashSet<>();

    // Constructors
    public NodoTagMaster() {}

    public NodoTagMaster(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "NodoTagMaster{" +
                "tagId=" + tagId +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}