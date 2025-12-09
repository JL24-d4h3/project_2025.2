package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reporte_adjunto")
public class ReporteAdjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adjunto_id", nullable = false)
    private Long adjuntoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id", nullable = false)
    private Reporte reporte;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    // Alias para repository methods en inglés
    @Column(name = "nombre_archivo", insertable = false, updatable = false)
    private String fileName;

    @Column(name = "ruta_archivo", length = 500)
    private String rutaArchivo;

    // Alias para repository methods en inglés
    @Column(name = "ruta_archivo", insertable = false, updatable = false)
    private String filePath;

    @Lob
    @Column(name = "contenido_archivo")
    private byte[] contenidoArchivo;

    @Column(name = "tipo_mime", length = 100)
    private String tipoMime;

    // Alias para repository methods en inglés
    @Column(name = "tipo_mime", insertable = false, updatable = false)
    private String mimeType;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    // Alias para repository methods en inglés
    @Column(name = "tamano_bytes", insertable = false, updatable = false)
    private Long sizeBytes;

    @Lob
    @Column(name = "descripcion_adjunto")
    private String descripcionAdjunto;

    // Alias para repository methods en inglés
    @Lob
    @Column(name = "descripcion_adjunto", insertable = false, updatable = false)
    private String attachmentDescription;

    @Column(name = "orden_visualizacion")
    private Integer ordenVisualizacion = 0;

    // Alias para repository methods en inglés
    @Column(name = "orden_visualizacion", insertable = false, updatable = false)
    private Integer displayOrder;

    @Column(name = "subido_en", nullable = false)
    private LocalDateTime subidoEn = LocalDateTime.now();

    // Alias para repository methods en inglés
    @Column(name = "subido_en", insertable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subido_por")
    private Usuario subidoPor;

    // ✨ NUEVOS CAMPOS PARA GOOGLE CLOUD STORAGE
    @Column(name = "gcs_file_id", length = 500)
    private String gcsFileId;  // "reportes/2025/10/reporte-123/v1-inicial/archivo.pdf"

    @Column(name = "gcs_bucket_name", length = 100)
    private String gcsBucketName;  // "devportal-storage"

    @Column(name = "gcs_file_path", length = 500)
    private String gcsFilePath;  // "gs://devportal-storage/reportes/..."

    @Column(name = "gcs_public_url", length = 1000)
    private String gcsPublicUrl;  // URL pública con expiración

    @Column(name = "gcs_file_size_bytes")
    private Long gcsFileSizeBytes;  // Tamaño en bytes

    // Versionado Simple
    @Column(name = "version_numero")
    private Integer versionNumero;  // Siempre 1 (versionado simple)

    @Column(name = "es_version_actual")
    private Boolean esVersionActual;  // TRUE si es actual, FALSE si fue reemplazada

    // Auditoría
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;  // Quién la actualizó

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;  // Cuándo se actualizó

    @Column(name = "gcs_migrado")
    private Boolean gcsMigrado;  // Flag para migración gradual

    // Métodos helper
    public String getTamanoFormateado() {
        if (tamanoBytes == null) return "0 B";
        long bytes = tamanoBytes;
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}

