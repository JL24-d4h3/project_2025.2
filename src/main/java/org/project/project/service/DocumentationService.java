package org.project.project.service;

import lombok.extern.slf4j.Slf4j;
import org.project.project.model.dto.ArchivoAdjuntoDTO;
import org.project.project.model.dto.SeccionDocumentacionDTO;
import org.project.project.model.entity.Contenido;
import org.project.project.model.entity.Documentacion;
import org.project.project.model.entity.Enlace;
import org.project.project.model.entity.Recurso;
import org.project.project.repository.DocumentationRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class DocumentationService {

    @Autowired
    private DocumentationRepository documentationRepository;
    
    @Autowired
    private GoogleCloudStorageService googleCloudStorageService;

    public List<Documentacion> listarDocumentaciones() {
        return documentationRepository.findAll();
    }

    public Documentacion buscarDocumentacionPorId(Long id) {
        return documentationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documentacion no encontrada con id: " + id));
    }

    public Documentacion guardarDocumentacion(Documentacion documentacion) {
        return documentationRepository.save(documentacion);
    }

    public Documentacion actualizarDocumentacion(Long id, Documentacion documentacionDetails) {
        Documentacion documentacion = buscarDocumentacionPorId(id);
        documentacion.setSeccionDocumentacion(documentacionDetails.getSeccionDocumentacion());
        documentacion.setApi(documentacionDetails.getApi());
        return documentationRepository.save(documentacion);
    }

    public void eliminarDocumentacion(Long id) {
        Documentacion documentacion = buscarDocumentacionPorId(id);
        documentationRepository.delete(documentacion);
    }
    
    // =====================================================================
    // FASE 0.5: Lógica de lectura de contenido Markdown híbrido (BD/GCS)
    // =====================================================================
    
    /**
     * Carga las secciones de documentación CMS con el contenido Markdown listo.
     * 
     * Flujo:
     * 1. Obtener todos los Contenidos de la Documentación
     * 2. Para cada Contenido, buscar sus Recursos
     * 3. Para cada Recurso, analizar su Enlace:
     *    - Si tipo_enlace = TEXTO_CONTENIDO → Leer recurso.markdown_content (BD)
     *    - Si tipo_enlace = STORAGE → Descargar desde GCS usando enlace.direccion_almacenamiento
     * 4. Crear DTO con contenido listo para el frontend
     * 
     * @param documentacion La documentación de la cual cargar secciones
     * @return Lista de DTOs con contenido Markdown listo para renderizar
     */
    public List<SeccionDocumentacionDTO> cargarSeccionesCMS(Documentacion documentacion) {
        List<SeccionDocumentacionDTO> seccionesDTO = new ArrayList<>();
        
        if (documentacion == null) {
            log.warn("⚠️ Documentación es NULL, retornando lista vacía");
            return seccionesDTO;
        }
        
        Set<Contenido> contenidosSet = documentacion.getContenidos();
        if (contenidosSet == null || contenidosSet.isEmpty()) {
            log.warn("⚠️ No hay contenidos en documentación ID={}", documentacion.getDocumentacionId());
            return seccionesDTO;
        }
        
        // Convertir a lista y ordenar
        List<Contenido> contenidos = new ArrayList<>(contenidosSet);
        contenidos.sort(Comparator.comparing(Contenido::getOrden));
        
        log.info("📚 Procesando {} contenidos de documentación ID={}", 
                contenidos.size(), documentacion.getDocumentacionId());
        
        for (Contenido contenido : contenidos) {
            try {
                SeccionDocumentacionDTO seccionDTO = procesarContenido(contenido);
                if (seccionDTO != null) {
                    seccionesDTO.add(seccionDTO);
                    log.info("✅ Sección cargada: '{}' (orden: {}, origen: {}, {} bytes)",
                            seccionDTO.getTitulo(), seccionDTO.getOrden(),
                            seccionDTO.getOrigenContenido(),
                            seccionDTO.getContenidoMarkdown() != null ? seccionDTO.getContenidoMarkdown().length() : 0);
                }
            } catch (Exception e) {
                log.error("❌ Error procesando contenido ID={}: {}", 
                        contenido.getContenidoId(), e.getMessage(), e);
                // Continuar con otros contenidos
            }
        }
        
        log.info("✅ Total secciones CMS cargadas: {}", seccionesDTO.size());
        return seccionesDTO;
    }
    
    /**
     * Procesa un Contenido individual y extrae su contenido Markdown + archivos adjuntos.
     * 
     * Lógica:
     * 1. Buscar Recurso con tipo Markdown (mime_type = text/markdown o extension .md)
     * 2. Para el recurso Markdown:
     *    - Según tipo_enlace: TEXTO_CONTENIDO → BD, STORAGE → GCS
     * 3. Procesar recursos restantes como archivos adjuntos (PDFs, imágenes, etc.)
     * 
     * @param contenido El contenido a procesar
     * @return DTO con contenido listo + archivos adjuntos, o null si no tiene recursos
     */
    private SeccionDocumentacionDTO procesarContenido(Contenido contenido) {
        Set<Recurso> recursos = contenido.getRecursos();
        
        if (recursos == null || recursos.isEmpty()) {
            log.warn("⚠️ Contenido '{}' (ID={}) no tiene recursos asociados",
                    contenido.getTituloContenido(), contenido.getContenidoId());
            return null;
        }
        
        // Crear DTO base
        SeccionDocumentacionDTO dto = SeccionDocumentacionDTO.builder()
                .contenidoId(contenido.getContenidoId())
                .titulo(contenido.getTituloContenido())
                .orden(contenido.getOrden())
                .tipoContenido(contenido.getClasificacion() != null ? 
                        contenido.getClasificacion().getTipoContenidoTexto() : "OTRO")
                .archivosAdjuntos(new ArrayList<>())
                .build();
        
        // 1. Buscar y procesar recurso Markdown
        boolean markdownEncontrado = false;
        for (Recurso recurso : recursos) {
            Enlace enlace = recurso.getEnlace();
            
            if (enlace == null) {
                log.warn("⚠️ Recurso ID={} no tiene enlace asociado", recurso.getRecursoId());
                continue;
            }
            
            // Identificar si es recurso Markdown
            boolean esMarkdown = "text/markdown".equalsIgnoreCase(recurso.getMimeType()) ||
                                 "md".equalsIgnoreCase(recurso.getFormatoRecurso()) ||
                                 "contenido_markdown.md".equals(recurso.getNombreArchivo());
            
            if (esMarkdown && !markdownEncontrado) {
                // Procesar Markdown principal
                if (enlace.getTipoEnlace() == Enlace.TipoEnlace.TEXTO_CONTENIDO || 
                    enlace.getTipoEnlace() == Enlace.TipoEnlace.STORAGE) {
                    
                    String markdown = leerContenidoMarkdown(recurso, enlace);
                    dto.setContenidoMarkdown(markdown);
                    
                    // Determinar origen
                    dto.setOrigenContenido(
                            enlace.getTipoEnlace() == Enlace.TipoEnlace.TEXTO_CONTENIDO ? "BD" : "GCS"
                    );
                    markdownEncontrado = true;
                }
            } else if (!esMarkdown && enlace.getTipoEnlace() == Enlace.TipoEnlace.STORAGE) {
                // 2. Procesar como archivo adjunto (PDF, imagen, etc.)
                String gcsPath = enlace.getDireccionAlmacenamiento();
                
                // Generar URL firmada para acceso público temporal
                String signedUrl = null;
                try {
                    signedUrl = googleCloudStorageService.generateSignedUrlForApi(gcsPath);
                    log.debug("🔗 URL firmada generada para archivo: {}", recurso.getNombreArchivo());
                } catch (Exception e) {
                    log.error("❌ Error generando URL firmada para {}: {}", gcsPath, e.getMessage());
                    signedUrl = gcsPath; // Fallback al path original
                }
                
                ArchivoAdjuntoDTO archivoDto = ArchivoAdjuntoDTO.builder()
                        .recursoId(recurso.getRecursoId())
                        .nombreArchivo(recurso.getNombreArchivo())
                        .url(signedUrl) // ✅ URL firmada lista para el navegador
                        .mimeType(recurso.getMimeType())
                        .tipoEnlace(enlace.getTipoEnlace().name())
                        .build();
                
                dto.getArchivosAdjuntos().add(archivoDto);
                log.info("📎 Archivo adjunto agregado: '{}' (ID={}) a sección '{}'",
                        recurso.getNombreArchivo(), recurso.getRecursoId(), contenido.getTituloContenido());
            }
        }
        
        if (!markdownEncontrado) {
            log.warn("⚠️ Contenido '{}' (ID={}) no tiene recurso Markdown",
                    contenido.getTituloContenido(), contenido.getContenidoId());
            // Aún así retornamos el DTO con los archivos adjuntos
        }
        
        log.info("✅ Sección procesada: '{}' | Markdown: {} | Archivos adjuntos: {}",
                dto.getTitulo(), 
                markdownEncontrado ? "✓" : "✗",
                dto.getArchivosAdjuntos().size());
        
        return dto;
    }
    
    /**
     * Lee el contenido Markdown de un Recurso según su estrategia de almacenamiento.
     * 
     * Estrategia 1: tipo_enlace = TEXTO_CONTENIDO → Leer recurso.markdown_content (BD)
     * Estrategia 2: tipo_enlace = STORAGE → Descargar desde GCS
     * 
     * @param recurso El recurso que contiene el Markdown
     * @param enlace El enlace que determina la estrategia
     * @return Contenido Markdown completo, o null si hay error
     */
    private String leerContenidoMarkdown(Recurso recurso, Enlace enlace) {
        if (enlace.getTipoEnlace() == Enlace.TipoEnlace.TEXTO_CONTENIDO) {
            // ✅ Estrategia 1: Leer de BD (markdown_content)
            String markdown = recurso.getMarkdownContent();
            
            if (markdown == null || markdown.trim().isEmpty()) {
                log.error("❌ Recurso ID={} tipo TEXTO_CONTENIDO pero markdown_content es NULL/vacío",
                        recurso.getRecursoId());
                return null;
            }
            
            log.debug("📖 Contenido leído de BD: {} caracteres", markdown.length());
            return markdown;
            
        } else if (enlace.getTipoEnlace() == Enlace.TipoEnlace.STORAGE) {
            // ✅ Estrategia 2: Descargar de GCS
            String gcsUrl = enlace.getDireccionAlmacenamiento();
            
            if (gcsUrl == null || gcsUrl.trim().isEmpty()) {
                log.error("❌ Enlace ID={} tipo STORAGE pero direccion_almacenamiento es NULL/vacío",
                        enlace.getEnlaceId());
                return null;
            }
            
            try {
                log.debug("☁️ Descargando de GCS: {}", gcsUrl);
                byte[] bytes = googleCloudStorageService.downloadFile(gcsUrl);
                
                if (bytes == null || bytes.length == 0) {
                    log.error("❌ Archivo descargado de GCS está vacío: {}", gcsUrl);
                    return null;
                }
                
                String markdown = new String(bytes, StandardCharsets.UTF_8);
                log.debug("☁️ Contenido descargado de GCS: {} bytes → {} caracteres", 
                        bytes.length, markdown.length());
                return markdown;
                
            } catch (Exception e) {
                log.error("❌ Error descargando de GCS ({}): {}", gcsUrl, e.getMessage(), e);
                return null;
            }
        }
        
        log.warn("⚠️ Tipo de enlace no soportado: {}", enlace.getTipoEnlace());
        return null;
    }
}