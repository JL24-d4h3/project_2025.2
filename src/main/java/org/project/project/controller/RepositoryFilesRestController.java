package org.project.project.controller;

import org.project.project.model.dto.NodoDTO;
import org.project.project.model.entity.ClipboardOperation;
import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.Repositorio;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.RepositorioRepository;
import org.project.project.service.ClipboardService;
import org.project.project.service.NodoService;
import org.project.project.service.RepositoryService;
import org.project.project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller para gestión de archivos y carpetas dentro de REPOSITORIOS
 * Endpoints REST que devuelven JSON (no vistas HTML)
 * Rutas base: /api/repositories/{repoId}/...
 */
@RestController
@RequestMapping("/api/repositories")
public class RepositoryFilesRestController {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryFilesRestController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RepositorioRepository repositorioRepository;

    @Autowired
    private NodoService nodoService;

    @Autowired
    private ClipboardService clipboardService;
    
    @Autowired
    private org.project.project.service.FolderCompressionService folderCompressionService;

    /**
     * GET /api/repositories/{repoId}/files
     * Obtiene la lista de archivos/carpetas raíz del repositorio
     */
    @GetMapping("/{repoId}/files")
    public ResponseEntity<?> getRepositoryFiles(@PathVariable Long repoId, Principal principal) {
        
        logger.info("=".repeat(80));
        logger.info("🔌 [REST-API] GET /api/repositories/{}/files", repoId);
        logger.info("   👤 Usuario autenticado: {}", principal != null ? principal.getName() : "ANÓNIMO");
        
        try {
            // Verificar autenticación
            if (principal == null) {
                logger.warn("   ⛔ Usuario NO autenticado - Retornando 401 Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "No autenticado", "message", "Debe iniciar sesión")
                );
            }

            // Obtener usuario actual
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            logger.info("   ✅ Usuario encontrado - ID: {}, Username: {}", 
                currentUser.getUsuarioId(), currentUser.getUsername());

            // Verificar que el repositorio existe
            Repositorio repositorio = repositorioRepository.findById(repoId)
                    .orElseThrow(() -> {
                        logger.error("   ❌ Repositorio R-{} NO ENCONTRADO", repoId);
                        return new RuntimeException("Repositorio no encontrado");
                    });
            logger.info("   ✅ Repositorio encontrado - Nombre: {}", repositorio.getNombreRepositorio());

            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            logger.info("   🔐 Permiso del usuario: {}", userPermission);

            if ("SIN_ACCESO".equals(userPermission)) {
                logger.warn("   ⛔ ACCESO DENEGADO - Retornando 403 Forbidden");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado", "message", "No tiene permisos para ver este repositorio")
                );
            }

            // Obtener nodos raíz
            logger.info("   📁 Obteniendo nodos raíz del repositorio...");
            List<NodoDTO> nodosRaiz = nodoService.obtenerNodosRaizDTO(
                Nodo.ContainerType.REPOSITORIO, repoId);
            
            logger.info("   ✅ Nodos obtenidos exitosamente - Cantidad: {}", nodosRaiz.size());
            
            // Log detallado de cada nodo
            nodosRaiz.forEach(nodo -> {
                logger.debug("      📄 Nodo #{}: {} ({})", nodo.getNodoId(), nodo.getNombre(), nodo.getTipo());
            });

            // Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("repositorioId", repoId);
            response.put("repositorioNombre", repositorio.getNombreRepositorio());
            response.put("parentId", null);
            response.put("files", nodosRaiz);
            response.put("totalFiles", nodosRaiz.size());

            logger.info("   ✅ Respuesta JSON preparada - {} archivos/carpetas", nodosRaiz.size());
            logger.info("=".repeat(80));
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("=".repeat(80));
            logger.error("💥 [ERROR CRÍTICO] Error al obtener archivos del repositorio R-{}", repoId);
            logger.error("   ❌ Tipo de error: {}", e.getClass().getSimpleName());
            logger.error("   ❌ Mensaje: {}", e.getMessage());
            logger.error("   ❌ Stack trace:", e);
            logger.error("=".repeat(80));
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getClass().getSimpleName());
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/repositories/{repoId}/files/{parentId}
     * Obtiene los archivos/carpetas hijos de una carpeta específica
     */
    @GetMapping("/{repoId}/files/{parentId}")
    public ResponseEntity<?> getFolderContents(@PathVariable Long repoId,
                                               @PathVariable Long parentId,
                                               Principal principal) {
        
        logger.info("=".repeat(80));
        logger.info("🔌 [REST-API] GET /api/repositories/{}/files/{}", repoId, parentId);
        logger.info("   👤 Usuario: {}", principal != null ? principal.getName() : "ANÓNIMO");
        
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "No autenticado")
                );
            }

            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            
            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");

            if ("SIN_ACCESO".equals(userPermission)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado")
                );
            }

            // Obtener hijos del nodo padre
            logger.info("   📁 Obteniendo hijos del nodo N-{}...", parentId);
            List<NodoDTO> hijos = nodoService.obtenerHijosDTO(parentId);
            
            logger.info("   ✅ Hijos obtenidos - Cantidad: {}", hijos.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("repositorioId", repoId);
            response.put("parentId", parentId);
            response.put("files", hijos);
            response.put("totalFiles", hijos.size());

            logger.info("=".repeat(80));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("💥 Error al obtener contenido de carpeta N-{}: {}", parentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * POST /api/repositories/{repoId}/folders
     * Crear una nueva carpeta
     */
    @PostMapping("/{repoId}/folders")
    public ResponseEntity<?> createFolder(@PathVariable Long repoId,
                                         @RequestBody Map<String, Object> request,
                                         Principal principal) {
        
        logger.info("=".repeat(80));
        logger.info("🔌 [REST-API] POST /api/repositories/{}/folders", repoId);
        logger.info("   📝 Request body: {}", request);
        
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "No autenticado")
                );
            }

            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            
            String nombre = (String) request.get("nombre");
            Long parentId = request.get("parentId") != null ? 
                Long.valueOf(request.get("parentId").toString()) : null;

            logger.info("   📁 Creando carpeta '{}' en parentId: {}", nombre, parentId);

            Nodo carpeta = nodoService.crearCarpeta(
                nombre,
                Nodo.ContainerType.REPOSITORIO,
                repoId,
                parentId,
                currentUser.getUsuarioId()
            );

            logger.info("   ✅ Carpeta creada exitosamente - ID: {}", carpeta.getNodoId());
            logger.info("=".repeat(80));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "nodoId", carpeta.getNodoId(),
                "message", "Carpeta creada exitosamente"
            ));

        } catch (Exception e) {
            logger.error("💥 Error creando carpeta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * DELETE /api/repositories/{repoId}/files/{nodoId}
     * Eliminar un archivo o carpeta
     */
    @DeleteMapping("/{repoId}/files/{nodoId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long repoId, @PathVariable Long nodoId, Principal principal) {
        
        logger.info("🔌 [REST-API] DELETE /api/repositories/{}/files/{}", repoId, nodoId);
        
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "No autenticado")
                );
            }

            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            
            // Verificar permisos en el repositorio
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
            logger.info("   👤 Usuario: {} | Permiso: {}", currentUser.getUsername(), userPermission);

            if ("SIN_ACCESO".equals(userPermission) || "LECTOR".equals(userPermission)) {
                logger.warn("   ⛔ ACCESO DENEGADO - Se requiere permiso de ESCRITOR o superior");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado", "message", "No tiene permisos para eliminar en este repositorio")
                );
            }
            
            logger.info("   🗑️ Eliminando nodo N-{}", nodoId);
            boolean deleted = nodoService.eliminarNodo(nodoId, currentUser.getUsuarioId());

            if (deleted) {
                logger.info("   ✅ Nodo eliminado exitosamente");
                return ResponseEntity.ok(Map.of("success", true, "message", "Eliminado exitosamente"));
            } else {
                logger.warn("   ⚠️ No se pudo eliminar el nodo");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", "No se pudo eliminar")
                );
            }

        } catch (Exception e) {
            logger.error("💥 Error eliminando nodo N-{}: {}", nodoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * PUT /api/repositories/{repoId}/files/{nodoId}
     * Renombrar un archivo o carpeta
     */
    @PutMapping("/{repoId}/files/{nodoId}")
    public ResponseEntity<?> renameFile(@PathVariable Long repoId, @PathVariable Long nodoId,
                                       @RequestBody Map<String, String> request,
                                       Principal principal) {
        
        logger.info("🔌 [REST-API] PUT /api/repositories/{}/files/{}", repoId, nodoId);
        
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "No autenticado")
                );
            }

            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            
            // Verificar permisos en el repositorio
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
            logger.info("   👤 Usuario: {} | Permiso: {}", currentUser.getUsername(), userPermission);

            if ("SIN_ACCESO".equals(userPermission) || "LECTOR".equals(userPermission)) {
                logger.warn("   ⛔ ACCESO DENEGADO - Se requiere permiso de ESCRITOR o superior");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado", "message", "No tiene permisos para renombrar en este repositorio")
                );
            }

            String nuevoNombre = request.get("nombre");
            
            logger.info("   ✏️ Renombrando nodo N-{} a '{}'", nodoId, nuevoNombre);
            Nodo nodo = nodoService.renombrarNodo(nodoId, nuevoNombre);

            logger.info("   ✅ Nodo renombrado exitosamente");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "nodoId", nodo.getNodoId(),
                "nuevoNombre", nodo.getNombre()
            ));

        } catch (Exception e) {
            logger.error("💥 Error renombrando nodo N-{}: {}", nodoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * POST /api/repositories/{repoId}/files/upload
     * Subir uno o más archivos al repositorio
     */
    @PostMapping("/{repoId}/files/upload")
    public ResponseEntity<?> uploadFiles(@PathVariable Long repoId,
                                        @RequestParam("files") org.springframework.web.multipart.MultipartFile[] files,
                                        @RequestParam(value = "parentId", required = false) Long parentId,
                                        Principal principal) {
        
        logger.info("🔌 [REST-API] POST /api/repositories/{}/files/upload", repoId);
        logger.info("   📎 Archivos a subir: {}", files.length);
        logger.info("   📂 Parent ID: {}", parentId);
        
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "No autenticado")
                );
            }

            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            
            // Verificar permisos en el repositorio
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
            logger.info("   👤 Usuario: {} | Permiso: {}", currentUser.getUsername(), userPermission);

            if ("SIN_ACCESO".equals(userPermission) || "LECTOR".equals(userPermission)) {
                logger.warn("   ⛔ ACCESO DENEGADO - Se requiere permiso de ESCRITOR o superior");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado", "message", "No tiene permisos para subir archivos a este repositorio")
                );
            }

            // Subir cada archivo
            java.util.List<Map<String, Object>> uploadedFiles = new java.util.ArrayList<>();
            for (org.springframework.web.multipart.MultipartFile file : files) {
                logger.info("   📤 Subiendo archivo: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
                
                // 🔄 Usar método con sincronización dual automática
                Nodo nodo = nodoService.subirArchivoConSincronizacionDual(
                    file,
                    Nodo.ContainerType.REPOSITORIO,
                    repoId,
                    parentId,
                    currentUser.getUsuarioId()
                );
                
                uploadedFiles.add(Map.of(
                    "nodoId", nodo.getNodoId(),
                    "nombre", nodo.getNombre(),
                    "tamanio", nodo.getSize() != null ? nodo.getSize() : 0L
                ));
                
                logger.info("   ✅ Archivo subido - Nodo ID: {}", nodo.getNodoId());
            }

            logger.info("   ✅ Todos los archivos subidos exitosamente - Total: {}", uploadedFiles.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Archivos subidos exitosamente",
                "files", uploadedFiles,
                "totalUploaded", uploadedFiles.size()
            ));

        } catch (Exception e) {
            logger.error("💥 Error subiendo archivos al repositorio R-{}: {}", repoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * GET /api/repositories/{repoId}/files/{nodoId}/download
     * Descargar un archivo
     */
    @GetMapping("/{repoId}/files/{nodoId}/download")
    public ResponseEntity<?> downloadFile(@PathVariable Long repoId,
                                         @PathVariable Long nodoId,
                                         @RequestParam(required = false, defaultValue = "false") Boolean inline,
                                         Principal principal) {
        
        logger.info("🔌 [REST-API] GET /api/repositories/{}/files/{}/download (inline={})", repoId, nodoId, inline);
        
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "No autenticado")
                );
            }

            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            
            // Verificar permisos en el repositorio
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");

            if ("SIN_ACCESO".equals(userPermission)) {
                logger.warn("   ⛔ ACCESO DENEGADO");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado")
                );
            }

            // Obtener el nodo
            Nodo nodo = nodoService.obtenerPorId(nodoId)
                    .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));

            if ("CARPETA".equals(nodo.getTipo())) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "No se pueden descargar carpetas")
                );
            }

            logger.info("   📥 Descargando archivo: {} | Ruta GCS: {}", nodo.getNombre(), nodo.getGcsPath());

            // Descargar el archivo desde GCS
            org.springframework.core.io.Resource resource = nodoService.descargarArchivo(nodoId);

            // Preparar headers para descarga o preview
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            String contentDisposition = inline 
                ? "inline; filename=\"" + nodo.getNombre() + "\""
                : "attachment; filename=\"" + nodo.getNombre() + "\"";
            headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            
            if (nodo.getMimeType() != null) {
                headers.setContentType(org.springframework.http.MediaType.parseMediaType(nodo.getMimeType()));
            } else {
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            }

            logger.info("   ✅ Descarga iniciada - Archivo: {}", nodo.getNombre());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            logger.error("💥 Error descargando archivo N-{}: {}", nodoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * GET /api/repositories/{repoId}/files/{nodoId}/content
     * Obtener contenido de archivo como texto (para vista de código)
     */
    @GetMapping("/{repoId}/files/{nodoId}/content")
    public ResponseEntity<?> getFileContent(@PathVariable Long repoId,
                                           @PathVariable Long nodoId,
                                           Principal principal) {
        
        logger.info("🔌 [REST-API] GET /api/repositories/{}/files/{}/content", repoId, nodoId);
        
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "No autenticado")
                );
            }

            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            
            // Verificar permisos en el repositorio
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");

            if ("SIN_ACCESO".equals(userPermission)) {
                logger.warn("   ⛔ ACCESO DENEGADO");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado")
                );
            }

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
            logger.error("💥 Error obteniendo contenido del archivo N-{}: {}", nodoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Error del servidor",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/repositories/{repoId}/files/{nodoId}/path
     * Obtiene la ruta completa (breadcrumb) de un nodo
     */
    @GetMapping("/{repoId}/files/{nodoId}/path")
    public ResponseEntity<?> getNodePath(@PathVariable Long repoId,
                                        @PathVariable Long nodoId,
                                        Principal principal) {
        
        logger.info("🔌 [REST-API] GET /api/repositories/{}/files/{}/path", repoId, nodoId);
        
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "No autenticado")
                );
            }

            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            
            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");

            if ("SIN_ACCESO".equals(userPermission)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado")
                );
            }

            // Obtener jerarquía de nodos desde la raíz hasta el nodo actual
            List<Map<String, Object>> breadcrumbPath = nodoService.obtenerJerarquiaNodo(nodoId);
            
            logger.info("   ✅ Ruta obtenida - {} niveles", breadcrumbPath.size());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "path", breadcrumbPath
            ));

        } catch (Exception e) {
            logger.error("💥 Error obteniendo ruta del nodo N-{}: {}", nodoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", e.getMessage())
            );
        }
    }

    // ==================== CLIPBOARD ENDPOINTS ====================

    /**
     * 📋 POST /api/repositories/{repoId}/clipboard/copy
     * Copiar archivos/carpetas al portapapeles
     */
    @PostMapping("/{repoId}/clipboard/copy")
    public ResponseEntity<?> copiarAlPortapapeles(
            @PathVariable Long repoId,
            @RequestBody Map<String, Object> request,
            Principal principal) {
        try {
            logger.info("📋 [CLIPBOARD] POST /api/repositories/{}/clipboard/copy", repoId);
            
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

            // Verificar permisos del repositorio
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                usuario.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");

            if ("SIN_ACCESO".equals(userPermission)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado")
                );
            }

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

    /**
     * ✂️ POST /api/repositories/{repoId}/clipboard/cut
     * Cortar archivos/carpetas al portapapeles
     */
    @PostMapping("/{repoId}/clipboard/cut")
    public ResponseEntity<?> cortarAlPortapapeles(
            @PathVariable Long repoId,
            @RequestBody Map<String, Object> request,
            Principal principal) {
        try {
            logger.info("✂️ [CLIPBOARD] POST /api/repositories/{}/clipboard/cut", repoId);
            
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

            // Verificar permisos del repositorio
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                usuario.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");

            if ("SIN_ACCESO".equals(userPermission)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado")
                );
            }

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
     * 📌 POST /api/repositories/{repoId}/clipboard/paste
     * Pegar archivos/carpetas desde el portapapeles
     */
    @PostMapping("/{repoId}/clipboard/paste")
    public ResponseEntity<?> pegarDesdePortapapeles(
            @PathVariable Long repoId,
            @RequestBody(required = false) Map<String, Object> request,
            Principal principal) {
        try {
            logger.info("📌 [CLIPBOARD] POST /api/repositories/{}/clipboard/paste", repoId);
            
            // Obtener parentId (puede ser null si se pega en la raíz)
            Long parentId = null;
            if (request != null && request.containsKey("parentId") && request.get("parentId") != null) {
                parentId = ((Number) request.get("parentId")).longValue();
            }

            Usuario usuario = userService.buscarPorUsername(principal.getName());

            // Verificar permisos del repositorio
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                usuario.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");

            if ("SIN_ACCESO".equals(userPermission)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado")
                );
            }

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
     * ℹ️ GET /api/repositories/{repoId}/clipboard/status
     * Obtener estado del portapapeles
     */
    @GetMapping("/{repoId}/clipboard/status")
    public ResponseEntity<?> obtenerEstadoPortapapeles(
            @PathVariable Long repoId,
            Principal principal) {
        try {
            Usuario usuario = userService.buscarPorUsername(principal.getName());

            // Verificar permisos del repositorio
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                usuario.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");

            if ("SIN_ACCESO".equals(userPermission)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado")
                );
            }

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
     * ❌ DELETE /api/repositories/{repoId}/clipboard
     * Cancelar operación de portapapeles
     */
    @DeleteMapping("/{repoId}/clipboard")
    public ResponseEntity<?> cancelarPortapapeles(
            @PathVariable Long repoId,
            Principal principal) {
        try {
            Usuario usuario = userService.buscarPorUsername(principal.getName());

            // Verificar permisos del repositorio
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                usuario.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");

            if ("SIN_ACCESO".equals(userPermission)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado")
                );
            }

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
    
    // ==================== COMPRESIÓN Y DESCARGA DE CARPETAS ====================
    
    /**
     * 📦 GET /api/repositories/{repoId}/folders/{folderId}/download
     * Descargar carpeta como ZIP
     */
    @GetMapping("/{repoId}/folders/{folderId}/download")
    public ResponseEntity<byte[]> descargarCarpeta(
            @PathVariable Long repoId,
            @PathVariable Long folderId,
            Principal principal) {
        try {
            Usuario usuario = userService.buscarPorUsername(principal.getName());
            
            logger.info("📦 [DOWNLOAD FOLDER] Usuario: {}, Repositorio: {}, Carpeta: {}", 
                       usuario.getUsername(), repoId, folderId);

            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                usuario.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");

            if ("SIN_ACCESO".equals(userPermission)) {
                logger.warn("   ⛔ ACCESO DENEGADO");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Obtener información de la carpeta
            Nodo carpeta = nodoService.obtenerPorId(folderId)
                    .orElseThrow(() -> new IllegalArgumentException("Carpeta no encontrada"));
            
            if (carpeta.getTipo() != Nodo.TipoNodo.CARPETA) {
                return ResponseEntity.badRequest().build();
            }

            // Comprimir carpeta
            java.io.ByteArrayOutputStream baos = folderCompressionService.comprimirCarpeta(folderId);
            byte[] zipBytes = baos.toByteArray();

            // Preparar headers de respuesta
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", carpeta.getNombre() + ".zip");
            headers.setContentLength(zipBytes.length);

            logger.info("   ✅ Carpeta comprimida - Tamaño: {} bytes", zipBytes.length);
            
            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("❌ Error al descargar carpeta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 📤 POST /api/repositories/{repoId}/upload-zip
     * Subir y descomprimir archivo ZIP
     */
    @PostMapping("/{repoId}/upload-zip")
    public ResponseEntity<?> subirYDescomprimirZip(
            @PathVariable Long repoId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "parentId", required = false) Long parentId,
            Principal principal) {
        try {
            Usuario usuario = userService.buscarPorUsername(principal.getName());
            
            logger.info("📤 [UPLOAD ZIP] Usuario: {}, Repositorio: {}, Archivo: {}", 
                       usuario.getUsername(), repoId, file.getOriginalFilename());

            // Verificar permisos (escritura o superior)
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                usuario.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");

            if ("SIN_ACCESO".equals(userPermission) || "LECTOR".equals(userPermission)) {
                logger.warn("   ⛔ ACCESO DENEGADO - Se requiere permiso de ESCRITOR o superior");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado", "message", "No tiene permisos para subir archivos")
                );
            }

            // Verificar que sea un archivo ZIP
            if (!file.getOriginalFilename().toLowerCase().endsWith(".zip")) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Tipo de archivo inválido", "message", "Solo se permiten archivos ZIP")
                );
            }

            // Descomprimir y crear estructura
            List<Nodo> nodosCreados = folderCompressionService.descomprimirZip(
                file.getInputStream(),
                Nodo.ContainerType.REPOSITORIO,
                repoId,
                parentId,
                usuario.getUsuarioId()
            );

            logger.info("   ✅ ZIP descomprimido - {} nodos creados", nodosCreados.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "ZIP descomprimido exitosamente",
                "nodesCreated", nodosCreados.size()
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error al descomprimir ZIP: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 📁 POST /api/repositories/{repoId}/upload-folder
     * Subir carpeta completa (múltiples archivos con estructura)
     */
    @PostMapping("/{repoId}/upload-folder")
    public ResponseEntity<?> subirCarpeta(
            @PathVariable Long repoId,
            @RequestParam("files") org.springframework.web.multipart.MultipartFile[] files,
            @RequestParam("paths") String[] paths,
            @RequestParam(value = "parentId", required = false) Long parentId,
            Principal principal) {
        try {
            Usuario usuario = userService.buscarPorUsername(principal.getName());
            
            logger.info("📁 [UPLOAD FOLDER] Usuario: {}, Repositorio: {}, Archivos: {}", 
                       usuario.getUsername(), repoId, files.length);

            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(
                usuario.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");

            if ("SIN_ACCESO".equals(userPermission) || "LECTOR".equals(userPermission)) {
                logger.warn("   ⛔ ACCESO DENEGADO");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado")
                );
            }

            // Procesar cada archivo con su ruta
            List<Map<String, Object>> archivosSubidos = new java.util.ArrayList<>();
            Map<String, Long> carpetasCreadas = new HashMap<>();
            
            for (int i = 0; i < files.length; i++) {
                org.springframework.web.multipart.MultipartFile file = files[i];
                String path = paths[i];
                
                logger.debug("   📄 Procesando: {}", path);
                
                // Separar path en carpetas y nombre de archivo
                String[] partes = path.split("/");
                Long parentActual = parentId;
                
                // Crear carpetas intermedias
                if (partes.length > 1) {
                    StringBuilder rutaAcumulada = new StringBuilder();
                    for (int j = 0; j < partes.length - 1; j++) {
                        rutaAcumulada.append(partes[j]);
                        String rutaCarpeta = rutaAcumulada.toString();
                        
                        if (!carpetasCreadas.containsKey(rutaCarpeta)) {
                            // Crear carpeta
                            Nodo carpeta = nodoService.crearCarpeta(
                                partes[j],
                                Nodo.ContainerType.REPOSITORIO,
                                repoId,
                                parentActual,
                                usuario.getUsuarioId()
                            );
                            carpetasCreadas.put(rutaCarpeta, carpeta.getNodoId());
                            parentActual = carpeta.getNodoId();
                        } else {
                            parentActual = carpetasCreadas.get(rutaCarpeta);
                        }
                        
                        rutaAcumulada.append("/");
                    }
                }
                
                // Subir archivo con sincronización dual
                Nodo nodo = nodoService.subirArchivoConSincronizacionDual(
                    file,
                    Nodo.ContainerType.REPOSITORIO,
                    repoId,
                    parentActual,
                    usuario.getUsuarioId()
                );
                
                archivosSubidos.add(Map.of(
                    "nodoId", nodo.getNodoId(),
                    "nombre", nodo.getNombre(),
                    "path", path
                ));
            }

            logger.info("   ✅ Carpeta subida - {} archivos, {} carpetas creadas", 
                       archivosSubidos.size(), carpetasCreadas.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "filesUploaded", archivosSubidos.size(),
                "foldersCreated", carpetasCreadas.size(),
                "files", archivosSubidos
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error al subir carpeta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
