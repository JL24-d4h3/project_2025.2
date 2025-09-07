package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Proyecto")
public class Proyecto {

    @Id
    @Column(name = "proyecto_id", nullable = false)
    private Integer proyectoId;

    @Column(name = "nombre_proyecto", nullable = false, length = 128)
    private String nombreProyecto;

    @Lob
    @Column(name = "descripcion_proyecto")
    private String descripcionProyecto;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibilidad_proyecto", nullable = false)
    private VisibilidadProyecto visibilidadProyecto;

    @Enumerated(EnumType.STRING)
    @Column(name = "acceso_proyecto", nullable = false)
    private AccesoProyecto accesoProyecto;

    @Enumerated(EnumType.STRING)
    @Column(name = "propietario_proyecto", nullable = false)
    private PropietarioProyecto propietarioProyecto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_proyecto", nullable = false)
    private EstadoProyecto estadoProyecto;

    @Column(name = "fecha_inicio_proyecto", nullable = false)
    private LocalDate fechaInicioProyecto;

    @Column(name = "fecha_fin_proyecto")
    private LocalDate fechaFinProyecto;

    @ManyToMany(mappedBy = "proyectos")
    private Set<Usuario> usuarios;

    @ManyToMany
    @JoinTable(
            name = "Categoria_has_Proyecto",
            joinColumns = @JoinColumn(name = "Proyecto_proyecto_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id_categoria")
    )
    private Set<Categoria> categorias;

    public enum VisibilidadProyecto {
        PUBLICO, PRIVADO
    }

    public enum AccesoProyecto {
        RESTRINGIDO, ORGANIZACION, CUALQUIER_PERSONA_CON_EL_ENLACE
    }

    public enum PropietarioProyecto {
        USUARIO, GRUPO, EMPRESA
    }

    public enum EstadoProyecto {
        PLANEADO, EN_DESARROLLO, MANTENIMIENTO, CERRADO
    }
}
