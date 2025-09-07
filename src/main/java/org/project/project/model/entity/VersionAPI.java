package org.project.project.model.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Version_API")
public class VersionAPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id", nullable = false)
    private Long versionId;

    @Column(name = "numero_version", nullable = false, length = 45, unique = true)
    private String numeroVersion;

    @Lob
    @Column(name = "descripcion_version")
    private String descripcionVersion;

    @Lob
    @Column(name = "contrato_api_url", nullable = false)
    private String contratoApiUrl;

    @Column(name = "fecha_lanzamiento", nullable = false)
    private LocalDate fechaLanzamiento;

    @ManyToOne
    @JoinColumn(name = "API_api_id", nullable = false)
    private API api;

    @OneToOne
    @JoinColumn(name = "metrica_api_metrica_id", nullable = false)
    private MetricaAPI metricaApi;

    @OneToOne
    @JoinColumn(name = "Documentacion_documentacion_id", nullable = false)
    private Documentacion documentacion;

    @OneToMany(mappedBy = "versionApi")
    private Set<Contenido> contenidos;

    @ManyToMany
    @JoinTable(
        name = "Version_API_has_Enlace",
        joinColumns = @JoinColumn(name = "Version_API_version_id"),
        inverseJoinColumns = @JoinColumn(name = "Enlace_enlace_id")
    )
    private Set<Enlace> enlaces;

}