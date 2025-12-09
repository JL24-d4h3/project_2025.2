package org.project.project.model.entity;

import java.time.LocalDateTime;
import java.time.LocalDate;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    // Alias para repository methods en inglés
    @Column(name = "usuario_id", insertable = false, updatable = false)
    private Long id;

    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombreUsuario;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 100)
    private String apellidoMaterno;

    @Column(name = "dni", nullable = false, length = 8, unique = true)
    private String dni;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo_usuario")
    private SexoUsuario sexoUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil")
    private EstadoCivil estadoCivil;

    @Column(name = "codigo_pais", length = 5)
    private String codigoPais;

    // Alias en inglés para repository methods
    @Column(name = "codigo_pais", length = 5, insertable = false, updatable = false)
    private String countryCode;

    @Column(name = "telefono", length = 45)
    private String telefono;

    @Column(name = "correo", nullable = false, length = 255, unique = true)
    private String correo;

    // Alias en inglés para repository methods
    @Column(name = "correo", nullable = false, length = 255, unique = true, insertable = false, updatable = false)
    private String email;

    @Column(name = "hashed_password", nullable = false, length = 512)
    private String hashedPassword;

    @Column(name="proveedor", length = 20)
    private String proveedor;

    // Alias en inglés para repository methods
    @Column(name="proveedor", length = 20, insertable = false, updatable = false)
    private String provider;

    @Column(name="id_proveedor", length = 255)
    private String idProveedor;

    // Alias en inglés para repository methods
    @Column(name="id_proveedor", length = 255, insertable = false, updatable = false)
    private String providerId;

    @Column(name = "direccion_usuario", nullable = false, length = 255)
    private String direccionUsuario;

    @Column(name = "username", nullable = false, length = 100, unique = true)
    private String username;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    // Alias en inglés para repository methods
    @Column(name = "fecha_creacion", nullable = false, insertable = false, updatable = false)
    private LocalDateTime creationDate;

    @Column(name = "ultima_conexion")
    private LocalDateTime ultimaConexion;

    // Alias en inglés para repository methods
    @Column(name = "ultima_conexion", insertable = false, updatable = false)
    private LocalDateTime lastConnection;

    @Column(name = "foto_perfil", length = 255)
    private String fotoPerfil;

    @Lob
    @Column(name = "foto_perfil_data", columnDefinition = "LONGBLOB")
    private byte[] fotoPerfilData;

    @Column(name = "foto_perfil_mime_type", length = 50)
    private String fotoPerfilMimeType;

    @Column(name = "foto_perfil_size_bytes")
    private Long fotoPerfilSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_usuario", nullable = false)
    private EstadoUsuario estadoUsuario = EstadoUsuario.INHABILITADO;

    // Alias en inglés para repository methods
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_usuario", nullable = false, insertable = false, updatable = false)
    private EstadoUsuario userStatus = EstadoUsuario.INHABILITADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "actividad_usuario", nullable = false)
    private ActividadUsuario actividadUsuario = ActividadUsuario.ACTIVO;

    @Column(name = "codigo_usuario", length = 45, unique = true)
    private String codigoUsuario;

    // Alias en inglés para repository methods
    @Column(name = "codigo_usuario", length = 45, unique = true, insertable = false, updatable = false)
    private String userCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "acceso_usuario", nullable = false)
    private AccesoUsuario accesoUsuario = AccesoUsuario.SI;

    @OneToMany(mappedBy = "usuario")
    private Set<ChatbotConversacion> conversaciones;

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
            name = "ticket_has_usuario",
            joinColumns = @JoinColumn(name = "usuario_usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "ticket_ticket_id")
    )
    private Set<Ticket> tickets;

    @OneToMany(mappedBy = "usuario")
    private Set<Token> tokens;

    @ManyToMany
    @JoinTable(
            name = "usuario_has_equipo",
            joinColumns = @JoinColumn(name = "usuario_usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "equipo_equipo_id")
    )
    private Set<Equipo> equipos;

    @ManyToMany
    @JoinTable(
            name = "usuario_has_proyecto",
            joinColumns = @JoinColumn(name = "usuario_usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "proyecto_proyecto_id")
    )
    private Set<Proyecto> proyectos;

    @ManyToMany
    @JoinTable(
            name = "usuario_has_repositorio",
            joinColumns = @JoinColumn(name = "usuario_usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "repositorio_repositorio_id")
    )
    private Set<Repositorio> repositorios;

    @ManyToMany
    @JoinTable(
            name = "usuario_has_rol",
            joinColumns = @JoinColumn(name = "usuario_usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_rol_id")
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