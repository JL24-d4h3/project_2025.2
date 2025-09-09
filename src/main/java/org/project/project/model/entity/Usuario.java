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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombreUsuario;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 100)
    private String apellidoMaterno;

    @Column(name = "dni", nullable = false, length = 8, unique = true)
    private String dni;

    @Column(name = "fecha_nacimiento")
    private LocalDateTime fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo_usuario")
    private SexoUsuario sexoUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil")
    private EstadoCivil estadoCivil;

    @Column(name = "telefono", length = 45)
    private String telefono;

    @Column(name = "correo", nullable = false, length = 255, unique = true)
    private String correo;

    @Column(name = "hashed_password", nullable = false, length = 512)
    private String hashedPassword;

    @Column(name="proveedor", length = 20)
    private String proveedor;

    @Column(name="id_proveedor", length = 255)
    private String idProveedor;

    @Column(name = "direccion_usuario", nullable = false, length = 255)
    private String direccionUsuario;

    @Column(name = "username", nullable = false, length = 100, unique = true)
    private String username;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "ultima_conexion")
    private LocalDateTime ultimaConexion;

    @Column(name = "foto_perfil", length = 255)
    private String fotoPerfil;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_usuario", nullable = false)
    private EstadoUsuario estadoUsuario = EstadoUsuario.HABILITADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "actividad_usuario", nullable = false)
    private ActividadUsuario actividadUsuario = ActividadUsuario.ACTIVO;

    @Column(name = "codigo_usuario", length = 45, unique = true)
    private String codigoUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "acceso_usuario", nullable = false)
    private AccesoUsuario accesoUsuario = AccesoUsuario.SI;

    @OneToMany(mappedBy = "usuario")
    private Set<Conversacion> conversaciones;

    @OneToMany(mappedBy = "usuario")
    private Set<Feedback> feedbacks;

    @OneToMany(mappedBy = "usuario")
    private Set<Notificacion> notificaciones;

    @OneToMany(mappedBy = "reportadoPor")
    private Set<Ticket> ticketsReportados;

    @OneToMany(mappedBy = "asignadoA")
    private Set<Ticket> ticketsAsignados;

    @ManyToMany
    @JoinTable(
        name = "Ticket_has_usuario",
        joinColumns = @JoinColumn(name = "Usuario_usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "Ticket_ticket_id")
    )
    private Set<Ticket> tickets;

    @OneToMany(mappedBy = "usuario")
    private Set<Token> tokens;

    @ManyToMany
    @JoinTable(
        name = "Usuario_has_equipo",
        joinColumns = @JoinColumn(name = "Usuario_usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "Equipo_equipo_id")
    )
    private Set<Equipo> equipos;

    @ManyToMany
    @JoinTable(
        name = "Usuario_has_proyecto",
        joinColumns = @JoinColumn(name = "Usuario_usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "Proyecto_proyecto_id")
    )
    private Set<Proyecto> proyectos;

    @ManyToMany
    @JoinTable(
        name = "Usuario_has_repositorio",
        joinColumns = @JoinColumn(name = "Usuario_usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "Repositorio_repositorio_id")
    )
    private Set<Repositorio> repositorios;

    @ManyToMany
    @JoinTable(
        name = "Usuario_has_rol",
        joinColumns = @JoinColumn(name = "Usuario_usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "Rol_rol_id")
    )
    private Set<Rol> roles;

    @OneToMany(mappedBy = "solicitante")
    private Set<SolicitudAccesoAPI> solicitudesAccesoAPI;
    
    @OneToMany(mappedBy = "usuario")
    private Set<Impersonacion> impersonaciones;

    @OneToMany(mappedBy = "usuario")
    private Set<Historial> historiales;

    public enum SexoUsuario {
        HOMBRE, MUJER
    }

    public enum EstadoCivil {
        SOLTERO, CASADO, VIUDO, DIVORCIADO
    }

    public enum EstadoUsuario {
        HABILITADO, INHABILITADO
    }

    public enum ActividadUsuario {
        ACTIVO, INACTIVO
    }

    public enum AccesoUsuario {
        SI, NO
    }
}
