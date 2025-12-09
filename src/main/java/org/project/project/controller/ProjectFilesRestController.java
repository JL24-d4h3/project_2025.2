package org.project.project.controller;

import lombok.RequiredArgsConstructor;
import org.project.project.model.dto.NodoDTO;
import org.project.project.model.entity.ClipboardOperation;
import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.Usuario;
import org.project.project.service.ClipboardService;
import org.project.project.service.FileStorageService;
import org.project.project.service.NodoService;
import org.project.project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST API Controller para operaciones de archivos en PROYECTOS.
 * Endpoints consumidos por JavaScript en project/files.html
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectFilesRestController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectFilesRestController.class);
    
    private final NodoService nodoService;
    private final FileStorageService fileStorageService;
    private final ClipboardService clipboardService;
    private final UserService userService;
    private final org.project.project.service.FolderCompressionService folderCompressionService;

    /**
     * 📂 GET /api/projects/{projectId}/files
     * Obtener archivos raíz del proyecto (sin parentId)
     */
    @GetMapping("/{projectId}/files")
    public ResponseEntity<?> listarArchivosRaiz(@PathVariable Long projectId) {
        try {
            logger.info("================================================================================");
            logger.info("📂 [REST-API] GET /api/projects/{}/files", projectId);
            logger.info("   🎯 Acción: Listar archivos raíz del proyecto");
            
            List<NodoDTO> archivos = nodoService.obtenerNodosRaizDTO(Nodo.ContainerType.PROYECTO, projectId);
            
            logger.info("   ✅ Archivos obtenidos: {}", archivos.size());
            logger.info("   📋 Detalles de archivos:");
            archivos.forEach(archivo -> 
                logger.info("      - ID: {} | Nombre: '{}' | Tipo: {} | Tamaño: {}", 
                    archivo.getNodoId(), archivo.getNombre(), archivo.getTipo(), archivo.getTamanio())
            );
            logger.info("================================================================================");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("files", archivos);
            response.put("total", archivos.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("💥 [REST-API ERROR] Error al listar archivos raíz del proyecto {}", projectId, e);
            logger.error("   🔴 Tipo de excepción: {}", e.getClass().getName());
            logger.error("   📝 Mensaje: {}", e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error al cargar archivos");
            error.put("message", e.getMessage());
            error.put("type", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 📂 GET /api/projects/{projectId}/files/{parentId}
     * Obtener archivos dentro de una carpeta específica
     */
    @GetMapping("/{projectId}/files/{parentId}")
    public ResponseEntity<?> listarArchivosPorCarpeta(
            @PathVariable Long projectId,
            @PathVariable Long parentId) {
        try {
            logger.info("================================================================================");
            logger.info("📂 [REST-API] GET /api/projects/{}/files/{}", projectId, parentId);
            logger.info("   🎯 Acción: Listar archivos de carpeta padre ID={}", parentId);
            
            List<NodoDTO> archivos = nodoService.obtenerHijosDTO(parentId);
            
            logger.info("   ✅ Archivos obtenidos: {}", archivos.size());
            logger.info("   📋 Detalles de archivos en carpeta ID={}:", parentId);
            archivos.forEach(archivo -> 
                logger.info("      - ID: {} | Nombre: '{}' | Tipo: {} | Tamaño: {}", 
                    archivo.getNodoId(), archivo.getNombre(), archivo.getTipo(), archivo.getTamanio())
            );
            logger.info("================================================================================");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("files", archivos);
            response.put("parentId", parentId);
            response.put("total", archivos.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("💥 [REST-API ERROR] Error al listar archivos de carpeta {} en proyecto {}", parentId, projectId, e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error al cargar contenido de carpeta");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * ➕ POST /api/projects/{projectId}/folders
     * Crear nueva carpeta
     */
    @PostMapping("/{projectId}/folders")
    public ResponseEntity<?> crearCarpeta(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> request) {
        try {
            logger.info("================================================================================");
            logger.info("➕ [REST-API] POST /api/projects/{}/folders", projectId);
            logger.info("   📦 Request Body: {}", request);
            
            String nombre = (String) request.get("nombre");
            Long parentId = request.get("parentId") != null ? 
                Long.valueOf(request.get("parentId").toString()) : null;
            
            logger.info("   📝 Nombre carpeta: '{}'", nombre);
            logger.info("   📂 Parent ID: {}", parentId);
            
            if (nombre == null || nombre.trim().isEmpty()) {
                logger.warn("   ⚠️ Nombre de carpeta vacío");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Nombre de carpeta requerido");
                return ResponseEntity.badRequest().body(error);
            }
            
            Nodo carpeta = nodoService.crearCarpeta(
                nombre.trim(),
                Nodo.ContainerType.PROYECTO,
                projectId,
                parentId,
                1L // TODO: Obtener usuario actual del SecurityContext
            );
            
            logger.info("   ✅ Carpeta creada exitosamente - ID: {}", carpeta.getNodoId());
            logger.info("================================================================================");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Carpeta creada exitosamente");
            response.put("folder", convertirAMapaSimple(carpeta));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("💥 [REST-API ERROR] Error al crear carpeta en proyecto {}", projectId, e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error al crear carpeta");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 📤 POST /api/projects/{projectId}/files/upload
     * Subir uno o más archivos al proyecto
     */
    @PostMapping("/{projectId}/files/upload")
    public ResponseEntity<?> subirArchivo(
            @PathVariable Long projectId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "parentId", required = false) Long parentId) {
        try {
            logger.info("================================================================================");
            logger.info("📤 [REST-API] POST /api/projects/{}/files/upload", projectId);
            logger.info("   � Archivos a subir: {}", files.length);
            logger.info("   📂 Parent ID: {}", parentId);
            
            // Validar que hay archivos
            if (files == null || files.length == 0) {
                logger.warn("   ⚠️ No se recibieron archivos");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "No se recibieron archivos");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Subir cada archivo
            java.util.List<Map<String, Object>> uploadedFiles = new java.util.ArrayList<>();
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    logger.warn("   ⚠️ Archivo vacío saltado: {}", file.getOriginalFilename());
                    continue;
                }
                
                logger.info("   📤 Subiendo archivo: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
                
                Nodo archivo = nodoService.subirArchivo(
                    file,
                    Nodo.ContainerType.PROYECTO,
                    projectId,
                    parentId,
                    1L // TODO: Obtener usuario actual del SecurityContext
                );
                
                uploadedFiles.add(Map.of(
                    "nodoId", archivo.getNodoId(),
                    "nombre", archivo.getNombre(),
                    "tamanio", archivo.getSize() != null ? archivo.getSize() : 0L
                ));
                
                logger.info("   ✅ Archivo subido - Nodo ID: {}", archivo.getNodoId());
            }
            
            logger.info("   ✅ Todos los archivos subidos exitosamente - Total: {}", uploadedFiles.size());
            logger.info("================================================================================");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Archivos subidos exitosamente",
                "files", uploadedFiles,
                "totalUploaded", uploadedFiles.size()
            ));
            
        } catch (Exception e) {
            logger.error("💥 [REST-API ERROR] Error al subir archivo en proyecto {}", projectId, e);
            logger.info("================================================================================");
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error al subir archivo");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 🗑️ DELETE /api/files/{nodoId}
     * Eliminar archivo o carpeta
     */
    @DeleteMapping("/files/{nodoId}")
    public ResponseEntity<?> eliminarNodo(@PathVariable Long nodoId) {
        try {
            logger.info("================================================================================");
            logger.info("🗑️ [REST-API] DELETE /api/files/{}", nodoId);
            
            Nodo nodo = nodoService.obtenerPorId(nodoId)
                    .orElseThrow(() -> new IllegalArgumentException("Nodo no encontrado"));
            String nombre = nodo.getNombre();
            Nodo.TipoNodo tipo = nodo.getTipo();
            
            logger.info("   📝 Eliminando: '{}' (Tipo: {})", nombre, tipo);
            
            nodoService.eliminarNodo(nodoId, 1L); // TODO: Obtener usuario actual
            
            logger.info("   ✅ Eliminado exitosamente");
            logger.info("================================================================================");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Elemento eliminado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("💥 [REST-API ERROR] Error al eliminar nodo {}", nodoId, e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error al eliminar");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * ✏️ PUT /api/files/{nodoId}
     * Renombrar archivo o carpeta
     */
    @PutMapping("/files/{nodoId}")
    public ResponseEntity<?> renombrarNodo(
            @PathVariable Long nodoId,
            @RequestBody Map<String, String> request) {
        try {
            logger.info("================================================================================");
            logger.info("✏️ [REST-API] PUT /api/files/{}", nodoId);
            logger.info("   📦 Request: {}", request);
            
            String nuevoNombre = request.get("nombre");
            
            if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
                logger.warn("   ⚠️ Nuevo nombre vacío");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Nuevo nombre requerido");
                return ResponseEntity.badRequest().body(error);
            }
            
            Nodo nodo = nodoService.renombrarNodo(nodoId, nuevoNombre.trim());
            
            logger.info("   ✅ Renombrado a: '{}'", nuevoNombre);
            logger.info("================================================================================");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Elemento renombrado exitosamente");
            response.put("file", convertirAMapaSimple(nodo));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("💥 [REST-API ERROR] Error al renombrar nodo {}", nodoId, e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error al renombrar");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 🗑️ DELETE /api/projects/{projectId}/files/{nodoId}
     * Eliminar un archivo o carpeta del proyecto
     */
    @DeleteMapping("/{projectId}/files/{nodoId}")
    public ResponseEntity<?> eliminarArchivo(@PathVariable Long projectId, @PathVariable Long nodoId) {
        try {
            logger.info("================================================================================");
            logger.info("🗑️ [REST-API] DELETE /api/projects/{}/files/{}", projectId, nodoId);
            logger.info("   🎯 Acción: Eliminar nodo del proyecto");
            
            boolean eliminado = nodoService.eliminarNodo(nodoId, null);
            
            if (eliminado) {
                logger.info("   ✅ Nodo eliminado exitosamente");
                logger.info("================================================================================");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Archivo/carpeta eliminado exitosamente"
                ));
            } else {
                logger.warn("   ⚠️ No se pudo eliminar el nodo");
                logger.info("================================================================================");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "No se pudo eliminar",
                    "message", "El nodo puede contener archivos o no existe"
                ));
            }
            
        } catch (Exception e) {
            logger.error("💥 [REST-API ERROR] Error al eliminar nodo {} del proyecto {}", nodoId, projectId, e);
            logger.info("================================================================================");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Error del servidor",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * ✏️ PUT /api/projects/{projectId}/files/{nodoId}
     * Renombrar un archivo o carpeta del proyecto
     */
    @PutMapping("/{projectId}/files/{nodoId}")
    public ResponseEntity<?> renombrarArchivo(
            @PathVariable Long projectId,
            @PathVariable Long nodoId,
            @RequestBody Map<String, String> request) {
        try {
            logger.info("================================================================================");
            logger.info("✏️ [REST-API] PUT /api/projects/{}/files/{}", projectId, nodoId);
            logger.info("   📦 Request Body: {}", request);
            
            String nuevoNombre = request.get("nombre");
            
            if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
                logger.warn("   ⚠️ Nombre vacío o nulo");
                logger.info("================================================================================");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Nombre inválido",
                    "message", "El nombre no puede estar vacío"
                ));
            }
            
            logger.info("   📝 Renombrando a: '{}'", nuevoNombre);
            Nodo nodoRenombrado = nodoService.renombrarNodo(nodoId, nuevoNombre);
            
            logger.info("   ✅ Nodo renombrado exitosamente");
            logger.info("================================================================================");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "nodoId", nodoRenombrado.getNodoId(),
                "nuevoNombre", nodoRenombrado.getNombre(),
                "message", "Renombrado exitosamente"
            ));
            
        } catch (Exception e) {
            logger.error("💥 [REST-API ERROR] Error al renombrar nodo {} del proyecto {}", nodoId, projectId, e);
            logger.info("================================================================================");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Error del servidor",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 📍 GET /api/projects/{projectId}/files/{nodoId}/path
     * Obtener la ruta completa (breadcrumb) de un nodo
     */
    @GetMapping("/{projectId}/files/{nodoId}/path")
    public ResponseEntity<?> obtenerRutaNodo(@PathVariable Long projectId, @PathVariable Long nodoId) {
        try {
            logger.info("================================================================================");
            logger.info("📍 [REST-API] GET /api/projects/{}/files/{}/path", projectId, nodoId);
            logger.info("   🎯 Acción: Obtener ruta breadcrumb del nodo");
            
            // El servicio ya retorna List<Map<String, Object>>
            List<Map<String, Object>> breadcrumbPath = nodoService.obtenerJerarquiaNodo(nodoId);
            
            logger.info("   ✅ Ruta obtenida - {} niveles", breadcrumbPath.size());
            logger.info("   📋 Breadcrumb: {}", 
                breadcrumbPath.stream()
                    .map(item -> String.valueOf(item.get("nombre")))
                    .collect(Collectors.joining(" > ")));
            logger.info("================================================================================");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "path", breadcrumbPath
            ));
            
        } catch (Exception e) {
            logger.error("💥 [REST-API ERROR] Error al obtener ruta del nodo {} del proyecto {}", nodoId, projectId, e);
            logger.info("================================================================================");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Error del servidor",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 📥 GET /api/projects/{projectId}/files/{nodoId}/download
     * Descargar archivo
     */
    @GetMapping("/{projectId}/files/{nodoId}/download")
    public ResponseEntity<org.springframework.core.io.Resource> descargarArchivo(
            @PathVariable Long projectId,
            @PathVariable Long nodoId,
            @RequestParam(required = false, defaultValue = "false") Boolean inline) {
        try {
            logger.info("================================================================================");
            logger.info("📥 [REST-API] GET /api/projects/{}/files/{}/download (inline={})", projectId, nodoId, inline);
            
            org.springframework.core.io.Resource resource = nodoService.descargarArchivo(nodoId);
            
            // Obtener nombre del archivo
            Nodo nodo = nodoService.obtenerPorId(nodoId)
                    .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado"));
            
            logger.info("   ✅ Descargando: '{}'", nodo.getNombre());
            logger.info("================================================================================");
            
            // Usar 'inline' para preview (PDF/imágenes en iframe) o 'attachment' para descarga directa
            String contentDisposition = inline 
                ? "inline; filename=\"" + nodo.getNombre() + "\""
                : "attachment; filename=\"" + nodo.getNombre() + "\"";
            
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, 
                            nodo.getMimeType() != null ? nodo.getMimeType() : "application/octet-stream")
                    .body(resource);
            
        } catch (Exception e) {
            logger.error("💥 [REST-API ERROR] Error al descargar archivo {} del proyecto {}", nodoId, projectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 📄 GET /api/projects/{projectId}/files/{nodoId}/content
     * Obtener contenido de archivo como texto (para vista de código)
     */
    @GetMapping("/{projectId}/files/{nodoId}/content")
    public ResponseEntity<?> obtenerContenidoArchivo(
            @PathVariable Long projectId,
            @PathVariable Long nodoId) {
        try {
            logger.info("================================================================================");
            logger.info("📄 [REST-API] GET /api/projects/{}/files/{}/content", projectId, nodoId);
            
            // Obtener nodo
            Nodo nodo = nodoService.obtenerPorId(nodoId)
                    .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado"));
            
            // Validar que sea un archivo, no carpeta
            if (nodo.getTipo() == Nodo.TipoNodo.CARPETA) {
                logger.warn("   ⚠️ Se intentó obtener contenido de una carpeta");
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "No se puede obtener contenido de una carpeta"
                ));
            }
            
            // Detectar extensión
            String extension = nodo.getNombre().contains(".") 
                ? nodo.getNombre().substring(nodo.getNombre().lastIndexOf(".") + 1).toLowerCase()
                : "";
            
            // Detectar archivos binarios conocidos ANTES de intentar leerlos
            java.util.Set<String> binaryExtensions = java.util.Set.of(
                "png", "jpg", "jpeg", "gif", "bmp", "ico", "svg", "webp",  // Imágenes
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",        // Documentos
                "zip", "rar", "7z", "tar", "gz",                           // Comprimidos
                "exe", "dll", "so", "class", "jar", "war",                 // Ejecutables
                "mp3", "mp4", "avi", "mov", "wav",                         // Multimedia
                "ttf", "otf", "woff", "woff2"                              // Fuentes
            );
            
            // Detectar binarios por extensión O por mimeType
            String mimeType = nodo.getMimeType() != null ? nodo.getMimeType().toLowerCase() : "";
            boolean isBinaryByExtension = binaryExtensions.contains(extension);
            
            // Solo considerar binario por MIME si es un tipo explícitamente binario
            // NO incluir application/octet-stream aquí porque es el tipo genérico por defecto
            boolean isBinaryByMime = mimeType.startsWith("image/") || 
                                     mimeType.contains("pdf") || 
                                     mimeType.startsWith("video/") ||
                                     mimeType.startsWith("audio/") ||
                                     mimeType.equals("application/zip") ||
                                     mimeType.equals("application/x-rar-compressed") ||
                                     mimeType.equals("application/x-7z-compressed");
            
            if (isBinaryByExtension || isBinaryByMime) {
                logger.info("   📦 Archivo binario detectado: '{}' (ext: '{}', mime: '{}')", 
                    nodo.getNombre(), extension, mimeType);
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Map.of(
                    "error", "Archivo binario",
                    "message", "Este archivo es binario y se mostrará en modo preview",
                    "extension", extension,
                    "nombre", nodo.getNombre(),
                    "mimeType", nodo.getMimeType() != null ? nodo.getMimeType() : "application/octet-stream",
                    "size", nodo.getSize() != null ? nodo.getSize() : 0L
                ));
            }
            
            // Verificar si el archivo tiene GCS path (archivos nuevos)
            if (nodo.getGcsPath() == null || nodo.getGcsPath().isEmpty()) {
                logger.warn("   ⚠️ Archivo sin ruta GCS (archivo legacy): '{}'", nodo.getNombre());
                logger.info("================================================================================");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Archivo no disponible",
                    "message", "Este archivo fue creado antes de la integración con almacenamiento en la nube y no está disponible para visualización. Por favor, vuelve a subir el archivo.",
                    "nombre", nodo.getNombre(),
                    "extension", extension
                ));
            }
            
            // Descargar contenido desde GCS
            org.springframework.core.io.Resource resource = nodoService.descargarArchivo(nodoId);
            
            // Leer contenido como String
            String contenido = new String(resource.getInputStream().readAllBytes(), 
                java.nio.charset.StandardCharsets.UTF_8);
            
            logger.info("   ✅ Contenido leído: '{}' | {} bytes | Extensión: '{}'", 
                nodo.getNombre(), contenido.length(), extension);
            logger.info("================================================================================");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "content", contenido,
                "nombre", nodo.getNombre(),
                "extension", extension,
                "mimeType", nodo.getMimeType() != null ? nodo.getMimeType() : "text/plain",
                "size", nodo.getSize() != null ? nodo.getSize() : 0L
            ));
            
        } catch (java.nio.charset.MalformedInputException e) {
            logger.warn("   ⚠️ Archivo no es texto plano (binario)");
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Map.of(
                "error", "Este archivo no se puede mostrar como texto",
                "message", "El archivo parece ser binario. Usa el botón de descarga."
            ));
        } catch (Exception e) {
            logger.error("💥 [REST-API ERROR] Error al obtener contenido del archivo {} del proyecto {}", 
                nodoId, projectId, e);
            logger.info("================================================================================");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Error del servidor",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Convertir entidad Nodo a Map simple para respuesta JSON
     */
    private Map<String, Object> convertirAMapaSimple(Nodo nodo) {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("nodoId", nodo.getNodoId());
        mapa.put("nombre", nodo.getNombre());
        mapa.put("tipo", nodo.getTipo().toString());
        mapa.put("tamanio", nodo.getSize());  // ← Corregido: getSize() en lugar de getTamanio()
        mapa.put("gcsPath", nodo.getGcsPath());
        mapa.put("creadoEn", nodo.getCreadoEn());
        mapa.put("actualizadoEn", nodo.getActualizadoEn());
        return mapa;
    }

    // ============================================================================
    // 📋 CLIPBOARD OPERATIONS (Copy/Cut/Paste)
    // ============================================================================

    /**
     * 📋 POST /api/projects/{projectId}/clipboard/copy
     * Copiar archivos/carpetas al portapapeles
     */
    @PostMapping("/{projectId}/clipboard/copy")
    public ResponseEntity<?> copiarAlPortapapeles(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> request,
            Principal principal) {
        try {
            logger.info("📋 [CLIPBOARD] POST /api/projects/{}/clipboard/copy", projectId);
            
            // Convertir Integer a Long (JavaScript envía números como Integer)
            @SuppressWarnings("unchecked")
            List<Number> nodoIdsRaw = (List<Number>) request.get("nodoIds");
            List<Long> nodoIds = nodoIdsRaw.stream()
                    .map(Number::longValue)
                    .collect(Collectors.toList());
            
            if (nodoIds == null || nodoIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se especificaron archivos"));
            }

            Usuario usuario = userService.buscarPorUsername(principal.getName());

            ClipboardOperation operation = clipboardService.copiarNodos(nodoIds, usuario);
            
            logger.info("   ✅ {} nodos copiados al portapapeles", nodoIds.size());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", nodoIds.size() + " archivo(s) copiado(s)",
                    "operation", operation.getOperationType().name(),
                    "count", nodoIds.size()
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error al copiar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{projectId}/clipboard/cut")
    public ResponseEntity<?> cortarAlPortapapeles(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> request,
            Principal principal) {
        try {
            logger.info("✂️ [CLIPBOARD] POST /api/projects/{}/clipboard/cut", projectId);
            
            // Convertir Integer a Long (JavaScript envía números como Integer)
            @SuppressWarnings("unchecked")
            List<Number> nodoIdsRaw = (List<Number>) request.get("nodoIds");
            List<Long> nodoIds = nodoIdsRaw.stream()
                    .map(Number::longValue)
                    .collect(Collectors.toList());
            
            if (nodoIds == null || nodoIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se especificaron archivos"));
            }

            Usuario usuario = userService.buscarPorUsername(principal.getName());

            ClipboardOperation operation = clipboardService.cortarNodos(nodoIds, usuario);
            
            logger.info("   ✅ {} nodos cortados al portapapeles", nodoIds.size());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", nodoIds.size() + " archivo(s) cortado(s)",
                    "operation", operation.getOperationType().name(),
                    "count", nodoIds.size()
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error al cortar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 📌 POST /api/projects/{projectId}/clipboard/paste
     * Pegar archivos/carpetas desde el portapapeles
     */
    @PostMapping("/{projectId}/clipboard/paste")
    public ResponseEntity<?> pegarDesdePortapapeles(
            @PathVariable Long projectId,
            @RequestBody(required = false) Map<String, Object> request,
            Principal principal) {
        try {
            logger.info("📌 [CLIPBOARD] POST /api/projects/{}/clipboard/paste", projectId);
            
            // Obtener parentId (puede ser null si se pega en la raíz)
            Long parentId = null;
            if (request != null && request.containsKey("parentId") && request.get("parentId") != null) {
                parentId = ((Number) request.get("parentId")).longValue();
            }

            Usuario usuario = userService.buscarPorUsername(principal.getName());

            List<Nodo> nodosPegados = clipboardService.pegarNodos(parentId, usuario);
            
            logger.info("   ✅ {} nodos pegados", nodosPegados.size());
            
            // Convertir nodos a DTOs
            List<NodoDTO> nodosDTO = nodosPegados.stream()
                    .map(nodo -> {
                        NodoDTO dto = new NodoDTO();
                        dto.setNodoId(nodo.getNodoId());
                        dto.setNombre(nodo.getNombre());
                        dto.setTipo(nodo.getTipo().name());
                        dto.setTamanio(nodo.getSize());
                        dto.setMimeType(nodo.getMimeType());
                        dto.setCreadoEn(nodo.getCreadoEn());
                        dto.setActualizadoEn(nodo.getActualizadoEn());
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", nodosPegados.size() + " archivo(s) pegado(s)",
                    "nodos", nodosDTO
            ));
            
        } catch (IllegalStateException e) {
            logger.warn("⚠️ No hay portapapeles activo");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No hay archivos en el portapapeles"));
        } catch (Exception e) {
            logger.error("❌ Error al pegar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ℹ️ GET /api/projects/{projectId}/clipboard/status
     * Obtener estado del portapapeles
     */
    @GetMapping("/{projectId}/clipboard/status")
    public ResponseEntity<?> obtenerEstadoPortapapeles(
            @PathVariable Long projectId,
            Principal principal) {
        try {
            Usuario usuario = userService.buscarPorUsername(principal.getName());

            Optional<ClipboardOperation> operationOpt = 
                    clipboardService.obtenerOperacionActiva(usuario.getId());
            
            if (operationOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "hasClipboard", false
                ));
            }

            ClipboardOperation operation = operationOpt.get();
            
            return ResponseEntity.ok(Map.of(
                    "hasClipboard", true,
                    "operation", operation.getOperationType().name(),
                    "count", operation.getNodoIds().size(),
                    "expiresAt", operation.getExpiresAt()
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error al obtener estado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ❌ DELETE /api/projects/{projectId}/clipboard
     * Cancelar operación de portapapeles
     */
    @DeleteMapping("/{projectId}/clipboard")
    public ResponseEntity<?> cancelarPortapapeles(
            @PathVariable Long projectId,
            Principal principal) {
        try {
            Usuario usuario = userService.buscarPorUsername(principal.getName());

            clipboardService.cancelarOperacion(usuario.getId());
            
            logger.info("   ✅ Portapapeles cancelado");
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Portapapeles cancelado"
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error al cancelar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 📦 GET /api/projects/{projectId}/folders/{folderId}/download
     * Descargar carpeta como ZIP
     */
    @GetMapping("/{projectId}/folders/{folderId}/download")
    public ResponseEntity<byte[]> descargarCarpeta(
            @PathVariable Long projectId,
            @PathVariable Long folderId) {
        try {
            logger.info("📦 [FOLDER-DOWNLOAD] Iniciando descarga de carpeta ID: {} del proyecto {}", folderId, projectId);
            
            // Verificar que el nodo es una carpeta
            Nodo carpeta = nodoService.obtenerPorId(folderId)
                    .orElseThrow(() -> new IllegalArgumentException("Carpeta no encontrada"));
            
            if (carpeta.getTipo() != Nodo.TipoNodo.CARPETA) {
                throw new IllegalArgumentException("El nodo especificado no es una carpeta");
            }
            
            // Comprimir carpeta
            java.io.ByteArrayOutputStream zipStream = folderCompressionService.comprimirCarpeta(folderId);
            byte[] zipBytes = zipStream.toByteArray();
            
            logger.info("✅ [FOLDER-DOWNLOAD] Carpeta comprimida exitosamente. Tamaño: {} bytes", zipBytes.length);
            
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + carpeta.getNombre() + ".zip\"")
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/zip")
                    .header(org.springframework.http.HttpHeaders.CONTENT_LENGTH, String.valueOf(zipBytes.length))
                    .body(zipBytes);
                    
        } catch (Exception e) {
            logger.error("💥 [FOLDER-DOWNLOAD] Error al descargar carpeta {}: {}", folderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 📦 POST /api/projects/{projectId}/upload-zip
     * Subir y descomprimir archivo ZIP
     */
    @PostMapping("/{projectId}/upload-zip")
    public ResponseEntity<?> subirYDescomprimirZip(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentId", required = false) Long parentId,
            Principal principal) {
        try {
            logger.info("📦 [ZIP-UPLOAD] Subiendo archivo ZIP al proyecto {}", projectId);
            
            // Validar extensión .zip
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".zip")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Solo se permiten archivos .zip"));
            }
            
            // Obtener usuario
            Usuario usuario = userService.buscarPorUsername(principal.getName());
            
            // Descomprimir y crear nodos
            List<Nodo> nodosCreados = folderCompressionService.descomprimirZip(
                    file.getInputStream(),
                    Nodo.ContainerType.PROYECTO,
                    projectId,
                    parentId,
                    usuario.getUsuarioId()
            );
            
            logger.info("✅ [ZIP-UPLOAD] ZIP descomprimido exitosamente. Nodos creados: {}", nodosCreados.size());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Archivo ZIP descomprimido exitosamente",
                    "nodesCreated", nodosCreados.size()
            ));
            
        } catch (Exception e) {
            logger.error("💥 [ZIP-UPLOAD] Error al subir ZIP: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * 📁 POST /api/projects/{projectId}/upload-folder
     * Subir estructura completa de carpeta
     */
    @PostMapping("/{projectId}/upload-folder")
    public ResponseEntity<?> subirCarpeta(
            @PathVariable Long projectId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("paths") String[] paths,
            @RequestParam(value = "parentId", required = false) Long parentId,
            Principal principal) {
        try {
            logger.info("📁 [FOLDER-UPLOAD] Subiendo {} archivos al proyecto {}", files.length, projectId);
            
            if (files.length != paths.length) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "La cantidad de archivos y rutas no coincide"));
            }
            
            // Obtener usuario
            Usuario usuario = userService.buscarPorUsername(principal.getName());
            
            // Mapa para cachear carpetas creadas (ruta -> nodoId)
            Map<String, Long> carpetasCreadas = new HashMap<>();
            int archivosSubidos = 0;
            
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                String rutaRelativa = paths[i];
                
                logger.info("   📄 Procesando: {}", rutaRelativa);
                
                // Dividir la ruta en partes
                String[] partes = rutaRelativa.split("/");
                Long parentActual = parentId;
                
                // Crear carpetas intermedias si no existen
                for (int j = 0; j < partes.length - 1; j++) {
                    final int jFinal = j;
                    String rutaCarpeta = String.join("/", java.util.Arrays.copyOfRange(partes, 0, j + 1));
                    
                    if (!carpetasCreadas.containsKey(rutaCarpeta)) {
                        // Verificar si la carpeta ya existe
                        List<Nodo> hijos = nodoService.obtenerHijos(parentActual, Nodo.ContainerType.PROYECTO, projectId);
                        Optional<Nodo> carpetaExistente = hijos.stream()
                                .filter(n -> n.getTipo() == Nodo.TipoNodo.CARPETA && n.getNombre().equals(partes[jFinal]))
                                .findFirst();
                        
                        if (carpetaExistente.isPresent()) {
                            carpetasCreadas.put(rutaCarpeta, carpetaExistente.get().getNodoId());
                        } else {
                            Nodo nuevaCarpeta = nodoService.crearCarpeta(
                                    partes[jFinal],
                                    Nodo.ContainerType.PROYECTO,
                                    projectId,
                                    parentActual,
                                    usuario.getUsuarioId()
                            );
                            carpetasCreadas.put(rutaCarpeta, nuevaCarpeta.getNodoId());
                        }
                    }
                    
                    parentActual = carpetasCreadas.get(rutaCarpeta);
                }
                
                // Subir el archivo
                String nombreArchivo = partes[partes.length - 1];
                nodoService.subirArchivoConSincronizacionDual(
                        file,
                        Nodo.ContainerType.PROYECTO,
                        projectId,
                        parentActual,
                        usuario.getUsuarioId()
                );
                
                archivosSubidos++;
            }
            
            logger.info("✅ [FOLDER-UPLOAD] Carpeta subida exitosamente. Archivos: {}, Carpetas: {}", 
                    archivosSubidos, carpetasCreadas.size());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Carpeta subida exitosamente",
                    "filesUploaded", archivosSubidos,
                    "foldersCreated", carpetasCreadas.size()
            ));
            
        } catch (Exception e) {
            logger.error("💥 [FOLDER-UPLOAD] Error al subir carpeta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}

