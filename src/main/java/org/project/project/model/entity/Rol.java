package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id", nullable = false)
    private Integer rolId;

    @Column(name = "nombre_rol", nullable = false, length = 30)
    private String nombreRol;

    @ManyToMany(mappedBy = "roles")
    private Set<Usuario> usuarios;

}
