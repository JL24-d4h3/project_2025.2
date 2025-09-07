package org.project.project.model.entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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

    @ManyToMany
    @JoinTable(
        name = "Categoria_has_Repositorio",
        joinColumns = @JoinColumn(name = "Repositorio_repositorio_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id_categoria")
    )
    private Set<Categoria> categorias;

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
