package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private Long rolId;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre_rol", nullable = false)
    private NombreRol nombreRol;
    
    // Alias en inglés para repository methods
    @Enumerated(EnumType.STRING)
    @Column(name = "nombre_rol", nullable = false, insertable = false, updatable = false)
    private NombreRol roleName;

    @Column(name = "descripcion_rol", columnDefinition = "TEXT")
    private String descripcionRol;

    // Alias en inglés para repository methods
    @Column(name = "descripcion_rol", insertable = false, updatable = false)
    private String roleDescription;

//    @Column(name = "nivel_acceso")
//    private Integer nivelAcceso;
//
//    // Alias en inglés para repository methods
//    @Column(name = "nivel_acceso", insertable = false, updatable = false)
//    private Integer accessLevel;
    
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // Alias en inglés para repository methods
    @Column(name = "activo", insertable = false, updatable = false)
    private Boolean active;

    @ManyToMany(mappedBy = "roles")
    private Set<Usuario> usuarios;
    
    // Enum para los roles del sistema
    @Getter
    public enum NombreRol {
        DEV("Desarrollador"),
        QA("Quality Assurance"), 
        PO("Product Owner"),
        SA("Super Administrador");
        
        private final String descripcion;
        
        NombreRol(String descripcion) {
            this.descripcion = descripcion;
        }

    }
    
    // Método helper para compatibilidad con código existente
    public String getNombreRolString() {
        return nombreRol != null ? nombreRol.name() : null;
    }
    
    // Método helper para obtener el NombreRol por ID
    public static NombreRol getNombreRolPorId(Integer id) {
        return switch (id) {
            case 1 -> NombreRol.DEV;
            case 2 -> NombreRol.QA;
            case 3 -> NombreRol.PO;
            case 4 -> NombreRol.SA;
            default -> NombreRol.DEV;
        };
    }
    
    // Método helper para obtener ID por NombreRol
    public static Integer getIdPorNombreRol(NombreRol nombreRol) {
        return switch (nombreRol) {
            case DEV -> 1;
            case QA -> 2;
            case PO -> 3;
            case SA -> 4;
            default -> 1;
        };
    }

}
