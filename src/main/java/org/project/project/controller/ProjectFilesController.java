package org.project.project.controller;

import org.project.project.model.dto.NodoDTO;
import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.Proyecto;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.ProyectoRepository;
import org.project.project.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para gestión de archivos y carpetas dentro de PROYECTOS
 * Rutas base: /devportal/{rol}/{username}/projects/P-{projectId}/files
 * 
 * Este controller maneja la navegación de archivos tipo explorador dentro del contexto de un proyecto.
 * Devuelve vistas HTML (Thymeleaf), NO endpoints REST.
 * 
 * NUEVA FUNCIONALIDAD: URLs dinámicas estilo GitHub
 * - /devportal/po/mlopez/projects/P-23/files (raíz)
 * - /devportal/po/mlopez/projects/P-23/files/src
 * - /devportal/po/mlopez/projects/P-23/files/src/main/java
 */
@Controller
@RequestMapping("/devportal/{rol}/{username}/projects/P-{projectId}/files")
public class ProjectFilesController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectFilesController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private NodoService nodoService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ClipboardService clipboardService;

    @Autowired
    private NodoShareLinkService nodoShareLinkService;

    @Autowired
    private NodoFavoriteService nodoFavoriteService;

    // =================== NAVEGACIÓN DE ARCHIVOS ===================

    /**
     * Vista raíz del explorador de archivos del proyecto (NUEVA VERSIÓN CON PATHS DINÁMICOS)
     * Ruta: GET /devportal/{rol}/{username}/projects/P-{projectId}/files
     * Ruta: GET /devportal/{rol}/{username}/projects/P-{projectId}/files/{path...}
     * 
     * Ejemplos:
     * - /files -> muestra raíz
     * - /files/src -> muestra carpeta "src"
     * - /files/src/main/java -> muestra carpeta "java" dentro de src/main
     */
    @GetMapping({"", "/**"})
    public String showProjectRoot(@PathVariable String rol,
                                  @PathVariable String username,
                                  @PathVariable Long projectId,
                                  jakarta.servlet.http.HttpServletRequest request,
                                  Model model,
                                  Principal principal) {

        String requestURI = request.getRequestURI();
        logger.info("📂 Request URI completo: {}", requestURI);
        
        // Extraer el path después de /files/
        String basePath = "/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/files";
        String path = "";
        
        if (requestURI.length() > basePath.length()) {
            path = requestURI.substring(basePath.length());
            // Quitar "/" inicial si existe
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
        }
        
        logger.info("📂 Mostrando archivos del proyecto P-{} | Path: '{}'", projectId, path);

        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);
        Proyecto proyecto = proyectoRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        // Verificar permisos del usuario en el proyecto
        String userPermission = projectService.obtenerPermisoUsuarioEnProyecto(currentUser.getUsuarioId(), projectId);
        if ("SIN_ACCESO".equals(userPermission)) {
            return "redirect:/devportal/" + rol + "/" + username + "/projects?error=access-denied";
        }
        
        // TODOS los usuarios con acceso pueden editar
        boolean canEdit = true;
        logger.info("   🔑 Usuario: {} tiene acceso completo al proyecto", currentUser.getUsername());

        // Variables para nodos y nodo actual
        List<NodoDTO> nodos;
        Nodo currentNode = null;
        List<Map<String, Object>> breadcrumbs;
        
        // Si path está vacío, mostrar raíz
        if (path.isEmpty()) {
            logger.info("   📁 Mostrando raíz del proyecto");
            nodos = nodoService.obtenerNodosRaizDTO(Nodo.ContainerType.PROYECTO, projectId);
            
            // Breadcrumb simple para raíz
            breadcrumbs = new ArrayList<>();
            breadcrumbs.add(Map.of(
                "nombre", proyecto.getNombreProyecto(),
                "url", "/devportal/" + rol + "/" + username + "/projects/P-" + projectId,
                "isActive", false
            ));
            breadcrumbs.add(Map.of(
                "nombre", "Archivos",
                "url", "",
                "isActive", true
            ));
        } else {
            // Resolver path a nodo
            logger.info("   🔍 Resolviendo path: '{}'", path);
            Optional<Nodo> nodoOpt = nodoService.resolverPathANodo(path, Nodo.ContainerType.PROYECTO, projectId);
            
            if (nodoOpt.isEmpty()) {
                logger.warn("   ❌ Path no encontrado: '{}'", path);
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/files?error=path-not-found";
            }
            
            currentNode = nodoOpt.get();
            logger.info("   ✅ Path resuelto a nodo: {} (ID: {})", currentNode.getNombre(), currentNode.getNodoId());
            
            // Verificar que sea carpeta
            if (currentNode.getTipo() != Nodo.TipoNodo.CARPETA) {
                logger.warn("   ⚠️ Path apunta a archivo, no carpeta. Redirigiendo a vista de archivo.");
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/files/N-" + currentNode.getNodoId() + "/view";
            }
            
            // Obtener hijos del nodo
            nodos = nodoService.obtenerHijosDTO(currentNode.getNodoId());
            
            // Construir breadcrumbs
            breadcrumbs = nodoService.construirBreadcrumbs(currentNode, proyecto, rol, username);
        }

        // Estadísticas del proyecto
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_archivos", nodoService.contarArchivos(Nodo.ContainerType.PROYECTO, projectId));
        stats.put("total_carpetas", nodoService.contarCarpetas(Nodo.ContainerType.PROYECTO, projectId));
        stats.put("espacio_usado", nodoService.calcularEspacioUsado(Nodo.ContainerType.PROYECTO, projectId));

        model.addAttribute("user", currentUser);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("rol", rol);
        model.addAttribute("username", username);
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("nodos", nodos);
        model.addAttribute("currentNode", currentNode);
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("stats", stats);
        model.addAttribute("userPermission", userPermission);
        model.addAttribute("canEdit", canEdit);

        return "project/files";
    }

    /**
     * Vista de una carpeta específica dentro del proyecto
     * Ruta: GET /devportal/{rol}/{username}/projects/P-{projectId}/files/N-{nodeId}
     */
    @GetMapping("/N-{nodeId}")
    public String showFolder(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long projectId,
                            @PathVariable Long nodeId,
                            Model model,
                            Principal principal) {

        logger.info("📁 Mostrando carpeta N-{} del proyecto P-{}", nodeId, projectId);

        Usuario currentUser = userService.obtenerUsuarioActual(principal, username);
        Proyecto proyecto = proyectoRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        // Verificar permisos
        String userPermission = projectService.obtenerPermisoUsuarioEnProyecto(currentUser.getUsuarioId(), projectId);
        if ("SIN_ACCESO".equals(userPermission)) {
            return "redirect:/devportal/" + rol + "/" + username + "/projects?error=access-denied";
        }
        
        // TODOS los usuarios con acceso pueden editar
        boolean canEdit = true;
        logger.info("   🔑 Usuario: {} tiene acceso completo al proyecto", currentUser.getUsername());

        // Obtener el nodo actual (debe ser una carpeta)
        Nodo nodoActual = nodoService.obtenerPorId(nodeId)
                .orElseThrow(() -> new RuntimeException("Carpeta no encontrada"));

        if (nodoActual.getTipo() != Nodo.TipoNodo.CARPETA) {
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/files?error=not-folder";
        }

        // Obtener hijos del nodo actual
        List<NodoDTO> hijosNodo = nodoService.obtenerHijosDTO(nodeId);

        // Construir breadcrumbs (proyecto > carpeta1 > carpeta2 > ... > carpeta actual)
        List<Map<String, Object>> breadcrumbs = nodoService.construirBreadcrumbs(nodoActual, proyecto, rol, username);

        // Estadísticas
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_archivos", nodoService.contarArchivos(Nodo.ContainerType.PROYECTO, projectId));
        stats.put("total_carpetas", nodoService.contarCarpetas(Nodo.ContainerType.PROYECTO, projectId));
        stats.put("espacio_usado", nodoService.calcularEspacioUsado(Nodo.ContainerType.PROYECTO, projectId));

        model.addAttribute("user", currentUser);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("rol", rol);
        model.addAttribute("username", username);
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("nodos", hijosNodo);
        model.addAttribute("currentNode", nodoActual);
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("stats", stats);
        model.addAttribute("userPermission", userPermission);
        model.addAttribute("canEdit", canEdit);

        return "project/files";
    }

    // =================== OPERACIONES DE ARCHIVOS ===================

    /**
     * Subir archivo(s) al proyecto
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/files/upload
     */
    @PostMapping("/upload")
    public String uploadFile(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long projectId,
                            @RequestParam("files") MultipartFile[] files,
                            @RequestParam(value = "parentNodeId", required = false) Long parentNodeId,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {

        logger.info("📤 Subiendo {} archivo(s) al proyecto P-{}", files.length, projectId);

        try {
            Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

            // Si tiene acceso al proyecto, puede subir archivos
            int uploadedCount = 0;
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    nodoService.subirArchivo(
                        file,
                        Nodo.ContainerType.PROYECTO,
                        projectId,
                        parentNodeId,
                        currentUser.getUsuarioId()
                    );
                    uploadedCount++;
                }
            }

            redirectAttributes.addFlashAttribute("success", 
                uploadedCount + " archivo(s) subido(s) exitosamente");

        } catch (Exception e) {
            logger.error("Error subiendo archivos: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al subir archivos: " + e.getMessage());
        }

        return redirectToCurrentLocation(rol, username, projectId, parentNodeId);
    }

    /**
     * Ver contenido de un archivo (para texto/código)
     * Ruta: GET /devportal/{rol}/{username}/projects/P-{projectId}/files/N-{nodeId}/view
     */
    @GetMapping("/N-{nodeId}/view")
    public String viewFile(@PathVariable String rol,
                          @PathVariable String username,
                          @PathVariable Long projectId,
                          @PathVariable Long nodeId,
                          Model model,
                          Principal principal) {

        logger.info("👁️ Visualizando archivo N-{} del proyecto P-{}", nodeId, projectId);

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
        Proyecto proyecto = proyectoRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        // Verificar permisos
        String userPermission = projectService.obtenerPermisoUsuarioEnProyecto(currentUser.getUsuarioId(), projectId);
        if ("SIN_ACCESO".equals(userPermission)) {
            return "redirect:/devportal/" + rol + "/" + username + "/projects?error=access-denied";
        }

        // Obtener el nodo actual (debe ser un archivo)
        Nodo nodo = nodoService.obtenerPorId(nodeId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));

        if (nodo.getTipo() != Nodo.TipoNodo.ARCHIVO) {
            // Si es carpeta, redirigir a la vista de carpeta
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/files/N-" + nodeId;
        }

        // 🆕 GITHUB-STYLE: Construir breadcrumbs con paths dinámicos (igual que en files.html)
        // Obtener el padre del archivo para generar breadcrumbs hasta la carpeta contenedora
        Nodo nodoPadre = nodo.getParent();
        List<Map<String, Object>> breadcrumbs;
        
        if (nodoPadre != null) {
            // Generar breadcrumbs hasta la carpeta que contiene el archivo
            breadcrumbs = nodoService.construirBreadcrumbs(nodoPadre, proyecto, rol, username);
            
            // Agregar el archivo actual como último elemento (activo)
            breadcrumbs.add(Map.of(
                "nombre", nodo.getNombre(),
                "url", "",
                "isActive", true
            ));
        } else {
            // Archivo en raíz (sin carpeta padre)
            breadcrumbs = new ArrayList<>();
            breadcrumbs.add(Map.of(
                "nombre", proyecto.getNombreProyecto(),
                "url", "/devportal/" + rol + "/" + username + "/projects/P-" + proyecto.getProyectoId(),
                "isActive", false
            ));
            breadcrumbs.add(Map.of(
                "nombre", "Archivos",
                "url", "/devportal/" + rol + "/" + username + "/projects/P-" + proyecto.getProyectoId() + "/files",
                "isActive", false
            ));
            breadcrumbs.add(Map.of(
                "nombre", nodo.getNombre(),
                "url", "",
                "isActive", true
            ));
        }
        
        logger.info("   📂 Breadcrumbs generados: {} niveles", breadcrumbs.size());

        model.addAttribute("user", currentUser);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("rol", rol);
        model.addAttribute("username", username);
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("nodo", nodo);
        model.addAttribute("breadcrumbs", breadcrumbs); // Cambiar nombre de atributo
        model.addAttribute("userPermission", userPermission);
        model.addAttribute("canEdit", true); // Todos pueden editar

        return "project/file-view";
    }

    /**
     * Descargar un archivo
     * Ruta: GET /devportal/{rol}/{username}/projects/P-{projectId}/files/N-{nodeId}/download
     */
    @GetMapping("/N-{nodeId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String rol,
                                                 @PathVariable String username,
                                                 @PathVariable Long projectId,
                                                 @PathVariable Long nodeId,
                                                 Principal principal) {

        logger.info("📥 Descargando archivo N-{} del proyecto P-{}", nodeId, projectId);

        try {
            Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

            // Verificar permisos
            String userPermission = projectService.obtenerPermisoUsuarioEnProyecto(currentUser.getUsuarioId(), projectId);
            if ("SIN_ACCESO".equals(userPermission)) {
                return ResponseEntity.status(403).build();
            }

            Nodo nodo = nodoService.obtenerPorId(nodeId)
                    .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));

            if (nodo.getTipo() != Nodo.TipoNodo.ARCHIVO) {
                return ResponseEntity.badRequest().build();
            }

            byte[] fileBytes = fileStorageService.descargarArchivo(nodo.getGcsPath());
            Resource resource = new ByteArrayResource(fileBytes);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(nodo.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + nodo.getNombre() + "\"")
                    .body(resource);

        } catch (Exception e) {
            logger.error("Error descargando archivo: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Crear nueva carpeta
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/files/create-folder
     */
    @PostMapping("/create-folder")
    public String createFolder(@PathVariable String rol,
                              @PathVariable String username,
                              @PathVariable Long projectId,
                              @RequestParam("folderName") String folderName,
                              @RequestParam(value = "parentNodeId", required = false) Long parentNodeId,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {

        logger.info("📁 Creando carpeta '{}' en proyecto P-{}", folderName, projectId);

        try {
            Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

            // Si tiene acceso al proyecto, puede crear carpetas
            nodoService.crearCarpeta(
                folderName,
                Nodo.ContainerType.PROYECTO,
                projectId,
                parentNodeId,
                currentUser.getUsuarioId()
            );

            redirectAttributes.addFlashAttribute("success", "Carpeta creada exitosamente");

        } catch (Exception e) {
            logger.error("Error creando carpeta: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al crear carpeta: " + e.getMessage());
        }

        return redirectToCurrentLocation(rol, username, projectId, parentNodeId);
    }

    /**
     * Renombrar archivo o carpeta
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/files/N-{nodeId}/rename
     */
    @PostMapping("/N-{nodeId}/rename")
    public String renameNode(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long projectId,
                            @PathVariable Long nodeId,
                            @RequestParam("newName") String newName,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {

        logger.info("✏️ Renombrando nodo N-{} a '{}'", nodeId, newName);

        try {
            Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

            // Si tiene acceso al proyecto, puede renombrar
            Nodo nodo = nodoService.obtenerPorId(nodeId)
                    .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));

            nodoService.renombrarNodo(nodeId, newName, currentUser);

            redirectAttributes.addFlashAttribute("success", "Renombrado exitosamente");

            // Volver a la carpeta padre
            return redirectToCurrentLocation(rol, username, projectId, nodo.getParentId());

        } catch (Exception e) {
            logger.error("Error renombrando nodo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al renombrar: " + e.getMessage());
            return "redirect:" + getRefererOrDefault(rol, username, projectId, null);
        }
    }

    /**
     * Eliminar archivo o carpeta (soft delete)
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/files/N-{nodeId}/delete
     */
    @PostMapping("/N-{nodeId}/delete")
    public String deleteNode(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long projectId,
                            @PathVariable Long nodeId,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {

        logger.info("🗑️ Eliminando nodo N-{} del proyecto P-{}", nodeId, projectId);

        try {
            Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

            // Si tiene acceso al proyecto, puede eliminar
            Nodo nodo = nodoService.obtenerPorId(nodeId)
                    .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));

            nodoService.eliminarNodo(nodeId, currentUser.getUsuarioId());

            redirectAttributes.addFlashAttribute("success", "Eliminado exitosamente");

            // Volver a la carpeta padre
            return redirectToCurrentLocation(rol, username, projectId, nodo.getParentId());

        } catch (Exception e) {
            logger.error("Error eliminando nodo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
            return "redirect:" + getRefererOrDefault(rol, username, projectId, null);
        }
    }

    /**
     * Mover nodo(s) a otra carpeta
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/files/move
     */
    @PostMapping("/move")
    public String moveNodes(@PathVariable String rol,
                           @PathVariable String username,
                           @PathVariable Long projectId,
                           @RequestParam("nodeIds") List<Long> nodeIds,
                           @RequestParam("targetNodeId") Long targetNodeId,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {

        logger.info("📦 Moviendo {} nodo(s) a carpeta N-{}", nodeIds.size(), targetNodeId);

        try {
            Usuario currentUser = userService.obtenerUsuarioActual(principal, username);

            // Si tiene acceso al proyecto, puede mover archivos
            for (Long nodeId : nodeIds) {
                nodoService.moverNodo(nodeId, targetNodeId);
            }

            redirectAttributes.addFlashAttribute("success", nodeIds.size() + " elemento(s) movido(s) exitosamente");

        } catch (Exception e) {
            logger.error("Error moviendo nodos: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al mover: " + e.getMessage());
        }

        return redirectToCurrentLocation(rol, username, projectId, targetNodeId);
    }

    // =================== UTILIDADES ===================

    private String redirectToCurrentLocation(String rol, String username, Long projectId, Long parentNodeId) {
        String baseUrl = "/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/files";
        if (parentNodeId != null) {
            return "redirect:" + baseUrl + "/N-" + parentNodeId;
        }
        return "redirect:" + baseUrl;
    }

    private String getRefererOrDefault(String rol, String username, Long projectId, Long nodeId) {
        String baseUrl = "/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/files";
        if (nodeId != null) {
            return baseUrl + "/N-" + nodeId;
        }
        return baseUrl;
    }
}
