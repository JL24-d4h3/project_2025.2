package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Repositorio")
public class Repositorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repositorio_id", nullable = false)
    private Integer repositorioId;

    @Column(name = "nombre_repositorio", nullable = false, length = 128)
    private String nombreRepositorio;

    @Lob
    @Column(name = "descripcion_repositorio")
    private String descripcionRepositorio;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibilidad_repositorio", nullable = false)
    private VisibilidadRepositorio visibilidadRepositorio;

    @Enumerated(EnumType.STRING)
    @Column(name = "acceso_repositorio", nullable = false)
    private AccesoRepositorio accesoRepositorio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_repositorio")
    private EstadoRepositorio estadoRepositorio;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "rama_principal_repositorio", nullable = false, length = 45)
    private String ramaPrincipalRepositorio;

    @ManyToMany(mappedBy = "repositorios")
    private Set<Usuario> usuarios;

    public enum VisibilidadRepositorio {
        PUBLICO, PRIVADO
    }

    public enum AccesoRepositorio {
        RESTRINGIDO, ORGANIZACION, CUALQUIER_PERSONA_CON_EL_ENLACE
    }

    public enum EstadoRepositorio {
        ACTIVO, ARCHIVADO
    }
}
