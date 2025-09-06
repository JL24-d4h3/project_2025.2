package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Etiqueta")
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id", nullable = false)
    private Integer tagId;

    @Column(name = "nombre_tag", nullable = false, length = 45)
    private String nombreTag;

    @ManyToMany(mappedBy = "etiquetas")
    private Set<API> apis;

}