package org.project.project.service;

import org.project.project.model.entity.Nodo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Servicio para compresión y descompresión de carpetas
 */
@Service
public class FolderCompressionService {

    private static final Logger logger = LoggerFactory.getLogger(FolderCompressionService.class);

    @Autowired
    private NodoService nodoService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Comprime una carpeta completa (incluyendo subcarpetas) en un archivo ZIP
     * @param carpetaId ID de la carpeta raíz a comprimir
     * @return ByteArrayOutputStream con el contenido del ZIP
     */
    public ByteArrayOutputStream comprimirCarpeta(Long carpetaId) throws IOException {
        logger.info("📦 Comprimiendo carpeta ID: {}", carpetaId);
        
        Nodo carpeta = nodoService.obtenerPorId(carpetaId)
                .orElseThrow(() -> new IllegalArgumentException("Carpeta no encontrada"));
        
        if (carpeta.getTipo() != Nodo.TipoNodo.CARPETA) {
            throw new IllegalArgumentException("El nodo no es una carpeta");
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Comprimir recursivamente todos los archivos
            comprimirCarpetaRecursivo(carpeta, "", zos);
            zos.finish();
        }
        
        logger.info("✅ Carpeta comprimida exitosamente - Tamaño: {} bytes", baos.size());
        return baos;
    }

    /**
     * Comprime recursivamente archivos y subcarpetas
     */
    private void comprimirCarpetaRecursivo(Nodo carpeta, String rutaRelativa, ZipOutputStream zos) throws IOException {
        List<Nodo> hijos = nodoService.obtenerHijos(
            carpeta.getNodoId(),
            carpeta.getContainerType(), 
            carpeta.getContainerId()
        );
        
        for (Nodo hijo : hijos) {
            String rutaHijo = rutaRelativa.isEmpty() ? hijo.getNombre() : rutaRelativa + "/" + hijo.getNombre();
            
            if (hijo.getTipo() == Nodo.TipoNodo.ARCHIVO) {
                // Agregar archivo al ZIP
                logger.debug("   📄 Agregando archivo: {}", rutaHijo);
                
                try {
                    byte[] contenido = fileStorageService.descargarArchivo(hijo.getGcsPath());
                    ZipEntry entry = new ZipEntry(rutaHijo);
                    entry.setSize(contenido.length);
                    zos.putNextEntry(entry);
                    zos.write(contenido);
                    zos.closeEntry();
                } catch (Exception e) {
                    logger.warn("   ⚠️ No se pudo agregar archivo {}: {}", rutaHijo, e.getMessage());
                    // Continuar con otros archivos
                }
            } else if (hijo.getTipo() == Nodo.TipoNodo.CARPETA) {
                // Crear entrada de carpeta en ZIP
                logger.debug("   📁 Agregando carpeta: {}/", rutaHijo);
                ZipEntry entry = new ZipEntry(rutaHijo + "/");
                zos.putNextEntry(entry);
                zos.closeEntry();
                
                // Recursión para subcarpetas
                comprimirCarpetaRecursivo(hijo, rutaHijo, zos);
            }
        }
    }

    /**
     * Descomprime un archivo ZIP y crea la estructura de carpetas/archivos
     * @param zipInputStream Stream del archivo ZIP
     * @param containerType Tipo de contenedor (PROYECTO o REPOSITORIO)
     * @param containerId ID del contenedor
     * @param parentId ID del nodo padre donde descomprimir
     * @param usuarioId ID del usuario que realiza la operación
     * @return Lista de nodos creados
     */
    public List<Nodo> descomprimirZip(InputStream zipInputStream, 
                                       Nodo.ContainerType containerType,
                                       Long containerId,
                                       Long parentId,
                                       Long usuarioId) throws Exception {
        logger.info("📦 Descomprimiendo ZIP en contenedor {} ID: {}", containerType, containerId);
        
        List<Nodo> nodosCreados = new ArrayList<>();
        
        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;
            
            while ((entry = zis.getNextEntry()) != null) {
                String nombreEntry = entry.getName();
                logger.debug("   📄 Procesando: {}", nombreEntry);
                
                if (entry.isDirectory()) {
                    // Crear carpeta
                    Nodo carpeta = crearCarpetaDesdeZip(nombreEntry, containerType, containerId, parentId, usuarioId);
                    if (carpeta != null) {
                        nodosCreados.add(carpeta);
                    }
                } else {
                    // Crear archivo
                    Nodo archivo = crearArchivoDesdeZip(nombreEntry, zis, containerType, containerId, parentId, usuarioId);
                    if (archivo != null) {
                        nodosCreados.add(archivo);
                    }
                }
                
                zis.closeEntry();
            }
        }
        
        logger.info("✅ ZIP descomprimido - {} nodos creados", nodosCreados.size());
        return nodosCreados;
    }

    /**
     * Crea una carpeta desde una entrada del ZIP
     */
    private Nodo crearCarpetaDesdeZip(String rutaCompleta, Nodo.ContainerType containerType,
                                       Long containerId, Long parentId, Long usuarioId) {
        try {
            // Remover trailing slash
            String ruta = rutaCompleta.endsWith("/") ? rutaCompleta.substring(0, rutaCompleta.length() - 1) : rutaCompleta;
            
            // Separar path en partes
            String[] partes = ruta.split("/");
            Long parentActual = parentId;
            
            // Crear carpetas intermedias si no existen
            for (String nombreCarpeta : partes) {
                // Verificar si ya existe
                List<Nodo> hijosExistentes = nodoService.obtenerHijos(parentActual, containerType, containerId);
                Nodo existente = hijosExistentes.stream()
                    .filter(n -> n.getNombre().equals(nombreCarpeta) && n.getTipo() == Nodo.TipoNodo.CARPETA)
                    .findFirst()
                    .orElse(null);
                
                if (existente != null) {
                    parentActual = existente.getNodoId();
                } else {
                    // Crear carpeta
                    Nodo nuevaCarpeta = nodoService.crearCarpeta(
                        nombreCarpeta,
                        containerType,
                        containerId,
                        parentActual,
                        usuarioId
                    );
                    parentActual = nuevaCarpeta.getNodoId();
                    logger.debug("   ✅ Carpeta creada: {}", nombreCarpeta);
                    return nuevaCarpeta;
                }
            }
        } catch (Exception e) {
            logger.error("   ❌ Error creando carpeta {}: {}", rutaCompleta, e.getMessage());
        }
        return null;
    }

    /**
     * Crea un archivo desde una entrada del ZIP
     */
    private Nodo crearArchivoDesdeZip(String rutaCompleta, InputStream contenido,
                                       Nodo.ContainerType containerType, Long containerId,
                                       Long parentId, Long usuarioId) {
        try {
            // Separar path en carpetas y nombre de archivo
            String[] partes = rutaCompleta.split("/");
            String nombreArchivo = partes[partes.length - 1];
            
            // Crear carpetas intermedias si es necesario
            Long parentActual = parentId;
            if (partes.length > 1) {
                for (int i = 0; i < partes.length - 1; i++) {
                    String nombreCarpeta = partes[i];
                    
                    // Verificar si ya existe
                    List<Nodo> hijosExistentes = nodoService.obtenerHijos(parentActual, containerType, containerId);
                    Nodo existente = hijosExistentes.stream()
                        .filter(n -> n.getNombre().equals(nombreCarpeta) && n.getTipo() == Nodo.TipoNodo.CARPETA)
                        .findFirst()
                        .orElse(null);
                    
                    if (existente != null) {
                        parentActual = existente.getNodoId();
                    } else {
                        Nodo nuevaCarpeta = nodoService.crearCarpeta(
                            nombreCarpeta,
                            containerType,
                            containerId,
                            parentActual,
                            usuarioId
                        );
                        parentActual = nuevaCarpeta.getNodoId();
                    }
                }
            }
            
            // Leer contenido del archivo
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = contenido.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            
            byte[] bytes = buffer.toByteArray();
            
            // Crear archivo temporal para subir
            CustomMultipartFile file = new CustomMultipartFile(
                nombreArchivo,
                nombreArchivo,
                detectarMimeType(nombreArchivo),
                bytes
            );
            
            // Subir archivo con sincronización dual
            Nodo nodo = nodoService.subirArchivoConSincronizacionDual(
                file,
                containerType,
                containerId,
                parentActual,
                usuarioId
            );
            
            logger.debug("   ✅ Archivo creado: {} ({} bytes)", nombreArchivo, bytes.length);
            return nodo;
        } catch (Exception e) {
            logger.error("   ❌ Error creando archivo {}: {}", rutaCompleta, e.getMessage());
            return null;
        }
    }

    /**
     * Detecta el MIME type basado en la extensión del archivo
     */
    private String detectarMimeType(String nombreArchivo) {
        String ext = nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1).toLowerCase();
        
        return switch (ext) {
            case "txt" -> "text/plain";
            case "html", "htm" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            case "pdf" -> "application/pdf";
            case "zip" -> "application/zip";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "mp4" -> "video/mp4";
            case "mp3" -> "audio/mpeg";
            default -> "application/octet-stream";
        };
    }
    
    /**
     * Implementación simple de MultipartFile para archivos en memoria
     */
    private static class CustomMultipartFile implements org.springframework.web.multipart.MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;
        
        public CustomMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }
        
        @Override
        public String getContentType() {
            return contentType;
        }
        
        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }
        
        @Override
        public long getSize() {
            return content.length;
        }
        
        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }
        
        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }
}
