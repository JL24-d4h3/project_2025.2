package org.project.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.project.project.model.dto.NodoDTO;
import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.Proyecto;
import org.project.project.model.entity.Repositorio;
import org.project.project.model.entity.RepositorioRama;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.ProyectoRepository;
import org.project.project.repository.RepositorioRepository;
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
 * Controlador para gestión de archivos y carpetas dentro de REPOSITORIOS que pertenecen a PROYECTOS
 * Rutas base: /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}
 * 
 * Este controller maneja la navegación de archivos tipo explorador dentro del contexto de un repositorio de un proyecto.
 * Devuelve vistas HTML (Thymeleaf), NO endpoints REST.
 * 
 * NUEVA FUNCIONALIDAD: URLs dinámicas estilo GitHub con ramas
 * - /tree/main (raíz de la rama main)
 * - /tree/main/src (carpeta src en rama main)
 * - /tree/main/src/main/java (path completo en rama main)
 * - /blob/main/README.md (archivo en rama main)
 */
@Controller
@RequestMapping("/devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}")
public class ProjectRepositoryFilesController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectRepositoryFilesController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private RepositorioRepository repositorioRepository;

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

    @Autowired
    private RepositorioRamaService repositorioRamaService;

    // =================== NAVEGACIÓN DE ARCHIVOS ===================

    /**
     * Vista raíz del explorador de archivos del repositorio dentro del proyecto - REDIRIGE A RAMA PRINCIPAL
     * Ruta: GET /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}/files
     * DEPRECADO: Redirige automáticamente a /tree/{branch} con la rama principal
     */
    @GetMapping("/files")
    public String showRepositoryRoot(@PathVariable String rol,
                                     @PathVariable String username,
                                     @PathVariable Long projectId,
                                     @PathVariable Long repoId) {

        logger.info("🔀 [REDIRECT] /files → Redirigiendo a sistema de ramas (Proyecto P-{})", projectId);
        logger.info("   📂 Repositorio: R-{}", repoId);
        
        try {
            // Obtener rama principal del repositorio
            RepositorioRama ramaPrincipal = repositorioRamaService.obtenerRamaPrincipal(repoId);
            String nombreRama = ramaPrincipal.getNombreRama();
            
            logger.info("   ✅ Rama principal encontrada: {}", nombreRama);
            logger.info("   🎯 Redirigiendo a: tree/{}", nombreRama);
            
            return "redirect:tree/" + nombreRama;
            
        } catch (Exception e) {
            logger.error("   ❌ Error al obtener rama principal: {}", e.getMessage());
            logger.error("   ⚠️ Fallback: Redirigiendo a tree/main");
            
            // Fallback: redirigir a "main" si no se encuentra rama principal
            return "redirect:tree/main";
        }
    }

    /**
     * Vista de una carpeta específica dentro del repositorio - DEPRECADO
     * Ruta: GET /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}/files/N-{nodeId}
     */
    @GetMapping("/N-{nodeId}")
    public String showFolder(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long projectId,
                            @PathVariable Long repoId,
                            @PathVariable Long nodeId) {

        logger.warn("⚠️ [DEPRECATED] Acceso obsoleto /N-{} (Proyecto) - Redirigiendo a sistema de ramas", nodeId);
        
        try {
            // Obtener rama principal
            RepositorioRama ramaPrincipal = repositorioRamaService.obtenerRamaPrincipal(repoId);
            String nombreRama = ramaPrincipal.getNombreRama();
            
            logger.info("   🎯 Redirigiendo a /tree/{} (raíz)", nombreRama);
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/repositories/R-" + repoId + "/tree/" + nombreRama;
            
        } catch (Exception e) {
            logger.error("   ❌ Error en redirect: {}", e.getMessage());
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/repositories/R-" + repoId + "/tree/main";
        }
    }

    // =================== OPERACIONES DE ARCHIVOS (PROYECTO) ===================

    /** CÓDIGO DEPRECADO - Conservado para referencia
    public String showFolderOLD(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long projectId,
                            @PathVariable Long repoId,
                            @PathVariable Long nodeId,
                            Model model,
                            Principal principal) {

        logger.info("📁 Mostrando carpeta N-{} del repositorio R-{} en proyecto P-{}", nodeId, repoId, projectId);

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
        
        Proyecto proyecto = proyectoRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        Repositorio repositorio = repositorioRepository.findById(repoId)
                .orElseThrow(() -> new RuntimeException("Repositorio no encontrado"));

        // Verificar permisos
        Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
        String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
        
        if ("SIN_ACCESO".equals(userPermission)) {
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "?error=access-denied";
        }

        // Obtener el nodo actual (debe ser una carpeta)
        Nodo nodoActual = nodoService.obtenerPorId(nodeId)
                .orElseThrow(() -> new RuntimeException("Carpeta no encontrada"));

        if (nodoActual.getTipo() != Nodo.TipoNodo.CARPETA) {
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + 
                   "/repositories/R-" + repoId + "/files?error=not-folder";
        }

        // Obtener hijos del nodo actual
        List<NodoDTO> hijosNodo = nodoService.obtenerHijosDTO(nodeId);

        // Construir breadcrumbs (proyecto > repositorio > carpeta1 > ... > carpeta actual)
        List<Map<String, Object>> breadcrumbs = construirBreadcrumbs(
            proyecto, repositorio, nodoActual, rol, username, projectId, repoId);

        // Estadísticas
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_archivos", nodoService.contarArchivos(Nodo.ContainerType.REPOSITORIO, repoId));
        stats.put("total_carpetas", nodoService.contarCarpetas(Nodo.ContainerType.REPOSITORIO, repoId));
        stats.put("espacio_usado", nodoService.calcularEspacioUsado(Nodo.ContainerType.REPOSITORIO, repoId));

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("rol", rol);
        model.addAttribute("username", username);
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("repositorio", repositorio);
        model.addAttribute("nodos", hijosNodo);
        model.addAttribute("currentNode", nodoActual);
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("stats", stats);
        model.addAttribute("userPermission", userPermission);
        model.addAttribute("canEdit", !"LECTOR".equals(userPermission) && !"SIN_ACCESO".equals(userPermission));

        return "project/repository/files";
    }
    FIN CÓDIGO DEPRECADO */

    // =================== OPERACIONES DE ARCHIVOS (PROYECTO) ===================

    /**
     * Subir archivo(s) al repositorio
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}/files/upload
     */
    @PostMapping("/upload")
    public String uploadFile(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long projectId,
                            @PathVariable Long repoId,
                            @RequestParam("files") MultipartFile[] files,
                            @RequestParam(value = "parentNodeId", required = false) Long parentNodeId,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {

        logger.info("📤 Subiendo {} archivo(s) al repositorio R-{} en proyecto P-{}", files.length, repoId, projectId);

        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

            // Verificar permisos de escritura
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
            if ("LECTOR".equals(userPermission) || "SIN_ACCESO".equals(userPermission)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para subir archivos");
                return redirectToCurrentLocation(rol, username, projectId, repoId, parentNodeId);
            }

            int uploadedCount = 0;
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    nodoService.subirArchivo(
                        file,
                        Nodo.ContainerType.REPOSITORIO,
                        repoId,
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

        return redirectToCurrentLocation(rol, username, projectId, repoId, parentNodeId);
    }

    /**
     * Descargar un archivo
     * Ruta: GET /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}/files/N-{nodeId}/download
     */
    /**
     * Ver contenido de un archivo (para texto/código)
     * Ruta: GET /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}/files/N-{nodeId}/view
     */
    @GetMapping("/N-{nodeId}/view")
    public String viewFile(@PathVariable String rol,
                          @PathVariable String username,
                          @PathVariable Long projectId,
                          @PathVariable Long repoId,
                          @PathVariable Long nodeId,
                          Model model,
                          Principal principal) {

        logger.info("👁️ Visualizando archivo N-{} del repositorio R-{} en proyecto P-{}", nodeId, repoId, projectId);

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
        
        // Obtener proyecto
        Proyecto proyecto = proyectoRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        
        // Obtener detalles del repositorio
        Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
        String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
        
        if ("SIN_ACCESO".equals(userPermission)) {
            return "redirect:/devportal/" + rol + "/" + username + "/projects?error=access-denied";
        }

        // Obtener el nodo actual (debe ser un archivo)
        Nodo nodo = nodoService.obtenerPorId(nodeId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));

        if (nodo.getTipo() != Nodo.TipoNodo.ARCHIVO) {
            // Si es carpeta, redirigir a la vista de carpeta
            return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/repositories/R-" + repoId + "/files/N-" + nodeId;
        }

        // Obtener jerarquía de carpetas (breadcrumbs)
        List<Map<String, Object>> breadcrumbPath = nodoService.obtenerJerarquiaNodo(nodeId);
        logger.info("   📂 Ruta de carpetas: {} niveles", breadcrumbPath.size());

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("rol", rol);
        model.addAttribute("username", username);
        model.addAttribute("proyecto", proyecto);
        model.addAttribute("repositorio", repoDetails);
        model.addAttribute("nodo", nodo);
        model.addAttribute("breadcrumbPath", breadcrumbPath);
        model.addAttribute("userPermission", userPermission);
        model.addAttribute("canEdit", !"LECTOR".equals(userPermission));

        return "project/repository/file-view";
    }

    /**
     * Descargar archivo
     * Ruta: GET /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}/files/N-{nodeId}/download
     */
    @GetMapping("/N-{nodeId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String rol,
                                                 @PathVariable String username,
                                                 @PathVariable Long projectId,
                                                 @PathVariable Long repoId,
                                                 @PathVariable Long nodeId,
                                                 Principal principal) {

        logger.info("📥 Descargando archivo N-{} del repositorio R-{} en proyecto P-{}", nodeId, repoId, projectId);

        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
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
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}/files/create-folder
     */
    @PostMapping("/create-folder")
    public String createFolder(@PathVariable String rol,
                              @PathVariable String username,
                              @PathVariable Long projectId,
                              @PathVariable Long repoId,
                              @RequestParam("folderName") String folderName,
                              @RequestParam(value = "parentNodeId", required = false) Long parentNodeId,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {

        logger.info("📁 Creando carpeta '{}' en repositorio R-{} del proyecto P-{}", folderName, repoId, projectId);

        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
            if ("LECTOR".equals(userPermission) || "SIN_ACCESO".equals(userPermission)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para crear carpetas");
                return redirectToCurrentLocation(rol, username, projectId, repoId, parentNodeId);
            }

            nodoService.crearCarpeta(
                folderName,
                Nodo.ContainerType.REPOSITORIO,
                repoId,
                parentNodeId,
                currentUser.getUsuarioId()
            );

            redirectAttributes.addFlashAttribute("success", "Carpeta creada exitosamente");

        } catch (Exception e) {
            logger.error("Error creando carpeta: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al crear carpeta: " + e.getMessage());
        }

        return redirectToCurrentLocation(rol, username, projectId, repoId, parentNodeId);
    }

    /**
     * Renombrar archivo o carpeta
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}/files/N-{nodeId}/rename
     */
    @PostMapping("/N-{nodeId}/rename")
    public String renameNode(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long projectId,
                            @PathVariable Long repoId,
                            @PathVariable Long nodeId,
                            @RequestParam("newName") String newName,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {

        logger.info("✏️ Renombrando nodo N-{} a '{}'", nodeId, newName);

        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
            if ("LECTOR".equals(userPermission) || "SIN_ACCESO".equals(userPermission)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para renombrar");
                return redirectToCurrentLocation(rol, username, projectId, repoId, null);
            }

            Nodo nodo = nodoService.obtenerPorId(nodeId)
                    .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));

            nodoService.renombrarNodo(nodeId, newName, currentUser);

            redirectAttributes.addFlashAttribute("success", "Renombrado exitosamente");

            // Volver a la carpeta padre
            return redirectToCurrentLocation(rol, username, projectId, repoId, nodo.getParentId());

        } catch (Exception e) {
            logger.error("Error renombrando nodo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al renombrar: " + e.getMessage());
            return redirectToCurrentLocation(rol, username, projectId, repoId, null);
        }
    }

    /**
     * Eliminar archivo o carpeta (soft delete)
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}/files/N-{nodeId}/delete
     */
    @PostMapping("/N-{nodeId}/delete")
    public String deleteNode(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long projectId,
                            @PathVariable Long repoId,
                            @PathVariable Long nodeId,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {

        logger.info("🗑️ Eliminando nodo N-{} del repositorio R-{} en proyecto P-{}", nodeId, repoId, projectId);

        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
            if ("LECTOR".equals(userPermission) || "SIN_ACCESO".equals(userPermission)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar");
                return redirectToCurrentLocation(rol, username, projectId, repoId, null);
            }

            Nodo nodo = nodoService.obtenerPorId(nodeId)
                    .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));

            nodoService.eliminarNodo(nodeId, currentUser.getUsuarioId());

            redirectAttributes.addFlashAttribute("success", "Eliminado exitosamente");

            // Volver a la carpeta padre
            return redirectToCurrentLocation(rol, username, projectId, repoId, nodo.getParentId());

        } catch (Exception e) {
            logger.error("Error eliminando nodo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
            return redirectToCurrentLocation(rol, username, projectId, repoId, null);
        }
    }

    /**
     * Mover nodo(s) a otra carpeta
     * Ruta: POST /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}/files/move
     */
    @PostMapping("/move")
    public String moveNodes(@PathVariable String rol,
                           @PathVariable String username,
                           @PathVariable Long projectId,
                           @PathVariable Long repoId,
                           @RequestParam("nodeIds") List<Long> nodeIds,
                           @RequestParam("targetNodeId") Long targetNodeId,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {

        logger.info("📦 Moviendo {} nodo(s) a carpeta N-{}", nodeIds.size(), targetNodeId);

        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
            if ("LECTOR".equals(userPermission) || "SIN_ACCESO".equals(userPermission)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para mover archivos");
                return redirectToCurrentLocation(rol, username, projectId, repoId, targetNodeId);
            }

            for (Long nodeId : nodeIds) {
                nodoService.moverNodo(nodeId, targetNodeId);
            }

            redirectAttributes.addFlashAttribute("success", nodeIds.size() + " elemento(s) movido(s) exitosamente");

        } catch (Exception e) {
            logger.error("Error moviendo nodos: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al mover: " + e.getMessage());
        }

        return redirectToCurrentLocation(rol, username, projectId, repoId, targetNodeId);
    }

    // =================== UTILIDADES ===================

    private String redirectToCurrentLocation(String rol, String username, Long projectId, Long repoId, Long parentNodeId) {
        String baseUrl = "/devportal/" + rol + "/" + username + "/projects/P-" + projectId + 
                         "/repositories/R-" + repoId + "/files";
        if (parentNodeId != null) {
            return "redirect:" + baseUrl + "/N-" + parentNodeId;
        }
        return "redirect:" + baseUrl;
    }

    private List<Map<String, Object>> construirBreadcrumbs(Proyecto proyecto, Repositorio repositorio, 
                                                           Nodo nodoActual, String rol, String username,
                                                           Long projectId, Long repoId) {
        List<Map<String, Object>> breadcrumbs = new ArrayList<>();
        
        // Proyecto
        breadcrumbs.add(Map.of(
            "nombre", proyecto.getNombreProyecto(),
            "url", "/devportal/" + rol + "/" + username + "/projects/P-" + projectId,
            "isActive", false
        ));
        
        // Repositorio
        breadcrumbs.add(Map.of(
            "nombre", repositorio.getNombreRepositorio(),
            "url", "/devportal/" + rol + "/" + username + "/projects/P-" + projectId + "/repositories/R-" + repoId,
            "isActive", false
        ));
        
        // Archivos raíz
        breadcrumbs.add(Map.of(
            "nombre", "Archivos",
            "url", "/devportal/" + rol + "/" + username + "/projects/P-" + projectId + 
                   "/repositories/R-" + repoId + "/files",
            "isActive", false
        ));
        
        // Jerarquía de carpetas hasta la actual
        List<Nodo> jerarquia = new ArrayList<>();
        Nodo temp = nodoActual;
        while (temp != null) {
            jerarquia.add(0, temp);
            temp = temp.getParent();
        }
        
        for (int i = 0; i < jerarquia.size(); i++) {
            Nodo nodo = jerarquia.get(i);
            boolean isLast = (i == jerarquia.size() - 1);
            breadcrumbs.add(Map.of(
                "nombre", nodo.getNombre(),
                "url", isLast ? "" : "/devportal/" + rol + "/" + username + "/projects/P-" + projectId + 
                                      "/repositories/R-" + repoId + "/files/N-" + nodo.getNodoId(),
                "isActive", isLast
            ));
        }
        
        return breadcrumbs;
    }

    // =================== NAVEGACIÓN CON RAMAS (GitHub-style) ===================

    /**
     * Vista de archivos del repositorio con selección de rama (DENTRO DE PROYECTO)
     * Ruta: GET /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}/tree/{branch}/**
     * Ejemplo: /projects/P-23/repositories/R-32/tree/main/src/controllers
     */
    @GetMapping(value = {"/tree/{branch}", "/tree/{branch}/**"}, produces = "text/html")
    public String showRepositoryTreeWithBranch(@PathVariable String rol,
                                               @PathVariable String username,
                                               @PathVariable Long projectId,
                                               @PathVariable Long repoId,
                                               @PathVariable String branch,
                                               HttpServletRequest request,
                                               Model model,
                                               Principal principal) {

        logger.info("=".repeat(80));
        logger.info("🌿 [TREE-VIEW-PROJECT] Mostrando archivos del repositorio R-{} en proyecto P-{}, rama '{}'", repoId, projectId, branch);
        
        try {
            // 🔍 LOG DETALLADO: Request info
            String fullPath = request.getRequestURI();
            logger.info("   📥 Request URI completo: {}", fullPath);
            logger.info("   📍 Branch parameter: {}", branch);
            
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            
            // Obtener proyecto
            Proyecto proyecto = proyectoRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
            
            // Obtener repositorio
            Repositorio repositorio = repositorioRepository.findById(repoId)
                    .orElseThrow(() -> new RuntimeException("Repositorio no encontrado"));
            
            // Verificar permisos
            Map<String, Object> projectDetails = projectService.obtenerDetallesProyecto(currentUser.getUsuarioId(), projectId);
            String userPermission = (String) projectDetails.get("privilegio_usuario_actual");
            
            if ("SIN_ACCESO".equals(userPermission)) {
                return "redirect:/devportal/" + rol + "/" + username + "/projects?error=access-denied";
            }

            // Obtener o validar la rama
            RepositorioRama rama = repositorioRamaService.obtenerRamaOPrincipal(repoId, branch);
            logger.info("   🌿 Rama encontrada: {} (ID: {})", rama.getNombreRama(), rama.getRamaId());

            // Extraer path después de /tree/{branch}/
            String basePath = "/devportal/" + rol + "/" + username + "/projects/P-" + projectId + 
                             "/repositories/R-" + repoId + "/tree/" + branch;
            String path = fullPath.startsWith(basePath + "/") ? fullPath.substring(basePath.length() + 1) : "";
            
            logger.info("   📂 Base path esperado: {}", basePath);
            logger.info("   📂 Path extraído: '{}' (length: {})", path, path.length());
            logger.info("   📂 Path está vacío: {}", path.isEmpty());

            // Resolver nodo (si hay path) o mostrar raíz
            List<NodoDTO> nodos;
            Nodo currentNode = null;
            
            if (path.isEmpty()) {
                // Raíz de la rama
                nodos = nodoService.obtenerNodosRaizDTOConRama(Nodo.ContainerType.REPOSITORIO, repoId, rama.getRamaId());
                logger.info("   ✅ Mostrando raíz de la rama - {} nodos", nodos.size());
            } else {
                // Carpeta específica
                Optional<Nodo> nodoOpt = nodoService.resolverPathANodoConRama(path, Nodo.ContainerType.REPOSITORIO, repoId, rama.getRamaId());
                
                if (nodoOpt.isEmpty()) {
                    logger.warn("   ❌ Path '{}' no encontrado en rama '{}'", path, branch);
                    return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + 
                           "/repositories/R-" + repoId + "/tree/" + branch;
                }
                
                currentNode = nodoOpt.get();
                
                if (currentNode.getTipo() == Nodo.TipoNodo.ARCHIVO) {
                    // Si es archivo, redirigir a vista de archivo
                    return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + 
                           "/repositories/R-" + repoId + "/blob/" + branch + "/" + path;
                }
                
                nodos = nodoService.obtenerHijosDTO(currentNode.getNodoId());
                logger.info("   ✅ Carpeta '{}' - {} nodos", currentNode.getNombre(), nodos.size());
            }

            // Construir breadcrumbs con rama
            List<Map<String, Object>> breadcrumbs = nodoService.construirBreadcrumbsProyectoRepositorioConRama(
                currentNode, proyecto, repositorio, rama.getNombreRama(), rol, username
            );

            // Obtener todas las ramas para el selector
            List<RepositorioRama> todasLasRamas = repositorioRamaService.findByRepositorioId(repoId);

            // Estadísticas
            Map<String, Object> stats = new HashMap<>();
            stats.put("total_archivos", nodoService.contarArchivos(Nodo.ContainerType.REPOSITORIO, repoId));
            stats.put("total_carpetas", nodoService.contarCarpetas(Nodo.ContainerType.REPOSITORIO, repoId));

            // Agregar atributos al modelo
            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("rol", rol);
            model.addAttribute("username", username);
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("repositorio", repositorio);
            model.addAttribute("ramaActual", rama);
            model.addAttribute("ramas", todasLasRamas);
            model.addAttribute("nodos", nodos);
            model.addAttribute("currentNode", currentNode);
            model.addAttribute("breadcrumbs", breadcrumbs);
            model.addAttribute("stats", stats);
            model.addAttribute("userPermission", userPermission);
            model.addAttribute("canEdit", true);

            logger.info("   ✅ Vista con ramas cargada exitosamente");
            logger.info("=".repeat(80));
            return "project/repository/files";
            
        } catch (Exception e) {
            logger.error("💥 Error al cargar vista con rama en proyecto: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Vista de archivo individual con información de rama (DENTRO DE PROYECTO)
     * Ruta: GET /devportal/{rol}/{username}/projects/P-{projectId}/repositories/R-{repoId}/blob/{branch}/**
     * Ejemplo: /projects/P-23/repositories/R-32/blob/main/src/Main.java
     */
    @GetMapping(value = "/blob/{branch}/**", produces = "text/html")
    public String viewFileWithBranch(@PathVariable String rol,
                                     @PathVariable String username,
                                     @PathVariable Long projectId,
                                     @PathVariable Long repoId,
                                     @PathVariable String branch,
                                     HttpServletRequest request,
                                     Model model,
                                     Principal principal) {

        logger.info("=".repeat(80));
        logger.info("📄 [BLOB-VIEW-PROJECT] Visualizando archivo del repositorio R-{} en proyecto P-{}, rama '{}'", repoId, projectId, branch);
        
        try {
            // 🔍 LOG DETALLADO: Request info
            String fullPath = request.getRequestURI();
            logger.info("   📥 Request URI completo: {}", fullPath);
            logger.info("   📍 Branch parameter: {}", branch);
            
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            
            // Obtener proyecto
            Proyecto proyecto = proyectoRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
            
            // Obtener repositorio
            Repositorio repositorio = repositorioRepository.findById(repoId)
                    .orElseThrow(() -> new RuntimeException("Repositorio no encontrado"));
            
            // Verificar permisos
            Map<String, Object> projectDetails = projectService.obtenerDetallesProyecto(currentUser.getUsuarioId(), projectId);
            String userPermission = (String) projectDetails.get("privilegio_usuario_actual");
            
            if ("SIN_ACCESO".equals(userPermission)) {
                return "redirect:/devportal/" + rol + "/" + username + "/projects?error=access-denied";
            }

            // Obtener rama
            RepositorioRama rama = repositorioRamaService.obtenerRamaOPrincipal(repoId, branch);
            logger.info("   🌿 Rama encontrada: {} (ID: {})", rama.getNombreRama(), rama.getRamaId());

            // Extraer path del archivo
            String basePath = "/devportal/" + rol + "/" + username + "/projects/P-" + projectId + 
                             "/repositories/R-" + repoId + "/blob/" + branch + "/";
            String path = fullPath.startsWith(basePath) ? fullPath.substring(basePath.length()) : "";
            
            logger.info("   📂 Base path esperado: {}", basePath);
            logger.info("   📂 Path extraído: '{}' (length: {})", path, path.length());
            logger.info("   📂 Path está vacío: {}", path.isEmpty());
            
            if (path.isEmpty()) {
                logger.warn("   ❌ Path vacío - Redirigiendo a tree");
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + 
                       "/repositories/R-" + repoId + "/tree/" + branch;
            }

            // Resolver nodo del archivo
            logger.info("   🔍 Resolviendo path a archivo: '{}'", path);
            Nodo archivo = nodoService.resolverPathANodoConRama(path, Nodo.ContainerType.REPOSITORIO, repoId, rama.getRamaId())
                    .orElseThrow(() -> new RuntimeException("Archivo no encontrado: " + path));

            if (archivo.getTipo() != Nodo.TipoNodo.ARCHIVO) {
                // Si es carpeta, redirigir a tree
                return "redirect:/devportal/" + rol + "/" + username + "/projects/P-" + projectId + 
                       "/repositories/R-" + repoId + "/tree/" + branch + "/" + path;
            }

            // Construir breadcrumbs (incluye la carpeta padre del archivo)
            List<Map<String, Object>> breadcrumbs;
            if (archivo.getParentId() != null) {
                Nodo nodoPadre = nodoService.obtenerPorId(archivo.getParentId()).orElse(null);
                breadcrumbs = nodoService.construirBreadcrumbsProyectoRepositorioConRama(
                    nodoPadre, proyecto, repositorio, rama.getNombreRama(), rol, username
                );
            } else {
                breadcrumbs = nodoService.construirBreadcrumbsProyectoRepositorioConRama(
                    null, proyecto, repositorio, rama.getNombreRama(), rol, username
                );
            }
            
            // Agregar el archivo actual como último breadcrumb (activo)
            breadcrumbs.add(Map.of(
                "nombre", archivo.getNombre(),
                "url", "",
                "isActive", true
            ));

            // Obtener todas las ramas para el selector
            List<RepositorioRama> todasLasRamas = repositorioRamaService.findByRepositorioId(repoId);

            // Obtener contenido del archivo (si es de texto)
            String contenidoArchivo = null;
            boolean esTexto = archivo.getMimeType() != null && archivo.getMimeType().startsWith("text/");
            
            if (esTexto) {
                try {
                    byte[] contenidoBytes = fileStorageService.descargarArchivo(archivo.getGcsPath());
                    contenidoArchivo = new String(contenidoBytes);
                } catch (Exception e) {
                    logger.warn("No se pudo cargar contenido del archivo: {}", e.getMessage());
                }
            }

            // Agregar atributos al modelo
            model.addAttribute("user", currentUser);
            model.addAttribute("Usuario", currentUser);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("rol", rol);
            model.addAttribute("username", username);
            model.addAttribute("proyecto", proyecto);
            model.addAttribute("repositorio", repositorio);
            model.addAttribute("ramaActual", rama);
            model.addAttribute("ramas", todasLasRamas);
            model.addAttribute("nodo", archivo);
            model.addAttribute("breadcrumbs", breadcrumbs);
            model.addAttribute("contenidoArchivo", contenidoArchivo);
            model.addAttribute("esTexto", esTexto);
            model.addAttribute("userPermission", userPermission);

            logger.info("   ✅ Archivo visualizado exitosamente");
            logger.info("=".repeat(80));
            return "project/repository/file-view";
            
        } catch (Exception e) {
            logger.error("💥 Error al visualizar archivo en proyecto: {}", e.getMessage(), e);
            throw e;
        }
    }
}
