package org.project.project.controller;

import org.project.project.model.dto.NodoDTO;
import org.project.project.model.entity.Nodo;
import org.project.project.model.entity.Repositorio;
import org.project.project.model.entity.RepositorioRama;
import org.project.project.model.entity.Usuario;
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

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para gestión de archivos y carpetas dentro de REPOSITORIOS
 * Rutas base: /devportal/{rol}/{username}/repositories/R-{repoId}
 * 
 * Este controller maneja la navegación de archivos tipo explorador dentro del contexto de un repositorio.
 * Devuelve vistas HTML (Thymeleaf), NO endpoints REST.
 */
@Controller
@RequestMapping("/devportal/{rol}/{username}/repositories/R-{repoId}")
public class RepositoryFilesController {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryFilesController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RepositoryService repositoryService;

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
     * Vista raíz del explorador de archivos del repositorio - REDIRIGE A RAMA PRINCIPAL
     * Ruta: GET /devportal/{rol}/{username}/repositories/R-{repoId}/files
     * DEPRECADO: Ahora redirige automáticamente al sistema de ramas con la rama principal
     */
    @GetMapping("/files")
    public String showRepositoryRoot(@PathVariable String rol,
                                     @PathVariable String username,
                                     @PathVariable Long repoId) {

        logger.info("=".repeat(80));
        logger.info("🔀 [REDIRECT] /files → Redirigiendo a sistema de ramas");
        logger.info("   📂 Repositorio: R-{}", repoId);
        
        try {
            // Obtener rama principal del repositorio
            RepositorioRama ramaPrincipal = repositorioRamaService.obtenerRamaPrincipal(repoId);
            String nombreRama = ramaPrincipal.getNombreRama();
            
            logger.info("   ✅ Rama principal encontrada: {}", nombreRama);
            logger.info("   🎯 Redirigiendo a: tree/{}", nombreRama);
            logger.info("=".repeat(80));
            
            return "redirect:tree/" + nombreRama;
            
        } catch (Exception e) {
            logger.error("   ❌ Error al obtener rama principal: {}", e.getMessage());
            logger.error("   ⚠️ Fallback: Redirigiendo a tree/main");
            logger.info("=".repeat(80));
            
            // Fallback: redirigir a "main" si no se encuentra rama principal
            return "redirect:tree/main";
        }
    }

    /**
     * ============================================================================================
     * MÉTODOS OBSOLETOS COMENTADOS (pre-sistema de ramas)
     * El método antiguo showRepositoryRoot completo se movió arriba como redirect
     * ============================================================================================
     */
    
    /* CÓDIGO ORIGINAL DEPRECADO - CONSERVADO PARA REFERENCIA
    public String showRepositoryRootOLD(...) {
        logger.info("=".repeat(80));
        logger.info("📂 [FILES-VIEW] Mostrando vista de archivos del repositorio R-{}", repoId);
        logger.info("   👤 Usuario: {} | Rol: {}", username, rol);
        
        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            logger.info("   ✅ Usuario autenticado - ID: {}, Username: {}", currentUser.getUsuarioId(), currentUser.getUsername());
            
            // Obtener repositorio
            Repositorio repositorio = repositorioRepository.findById(repoId)
                    .orElseThrow(() -> {
                        logger.error("   ❌ Repositorio R-{} NO ENCONTRADO", repoId);
                        return new RuntimeException("Repositorio no encontrado");
                    });
            logger.info("   ✅ Repositorio encontrado - Nombre: {}", repositorio.getNombreRepositorio());

            // Verificar permisos del usuario en el repositorio
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            logger.info("   🔐 Permiso del usuario: {}", userPermission);
            
            if ("SIN_ACCESO".equals(userPermission)) {
                logger.warn("   ⛔ ACCESO DENEGADO - Redirigiendo a lista de repositorios");
                return "redirect:/devportal/" + rol + "/" + username + "/repositories?error=access-denied";
            }

            // Obtener nodos raíz del repositorio
            logger.info("   📁 Obteniendo nodos raíz del repositorio...");
            List<NodoDTO> nodosRaiz = nodoService.obtenerNodosRaizDTO(Nodo.ContainerType.REPOSITORIO, repoId);
            logger.info("   ✅ Nodos raíz obtenidos - Cantidad: {}", nodosRaiz.size());

        // Estadísticas del repositorio
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_archivos", nodoService.contarArchivos(Nodo.ContainerType.REPOSITORIO, repoId));
        stats.put("total_carpetas", nodoService.contarCarpetas(Nodo.ContainerType.REPOSITORIO, repoId));
        stats.put("espacio_usado", nodoService.calcularEspacioUsado(Nodo.ContainerType.REPOSITORIO, repoId));

        // Breadcrumb (solo repositorio)
        List<Map<String, Object>> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(Map.of(
            "nombre", repositorio.getNombreRepositorio(),
            "url", "/devportal/" + rol + "/" + username + "/repositories/R-" + repoId,
            "isActive", false
        ));
        breadcrumbs.add(Map.of(
            "nombre", "Archivos",
            "url", "",
            "isActive", true
        ));

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("rol", rol);
        model.addAttribute("username", username);
        model.addAttribute("repositorio", repositorio);
        model.addAttribute("nodos", nodosRaiz);
        model.addAttribute("currentNode", null); // Estamos en la raíz
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("stats", stats);
        model.addAttribute("userPermission", userPermission);
        model.addAttribute("canEdit", true); // Todos pueden editar

        logger.info("   ✅ Atributos agregados al modelo exitosamente");
        logger.info("   🎯 Retornando vista: repository/files");
        logger.info("=".repeat(80));
        return "repository/files";
        
    } catch (Exception e) {
        logger.error("=".repeat(80));
        logger.error("💥 [ERROR CRÍTICO] Error al cargar vista de archivos del repositorio R-{}", repoId);
        logger.error("   ❌ Tipo de error: {}", e.getClass().getSimpleName());
        logger.error("   ❌ Mensaje: {}", e.getMessage());
        logger.error("   ❌ Stack trace:", e);
        logger.error("=".repeat(80));
        throw e;
    }
    }
    FIN CÓDIGO DEPRECADO */

    /**
     * Vista de una carpeta específica dentro del repositorio - DEPRECADO
     * Ruta: GET /devportal/{rol}/{username}/repositories/R-{repoId}/files/N-{nodeId}
     * DEPRECADO: Navegación por nodos obsoleta, usar /tree/{branch}/{path} en su lugar
     */
    @GetMapping("/N-{nodeId}")
    public String showFolder(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long repoId,
                            @PathVariable Long nodeId) {

        logger.warn("⚠️ [DEPRECATED] Acceso obsoleto /N-{} - Redirigiendo a sistema de ramas", nodeId);
        
        try {
            // Obtener rama principal
            RepositorioRama ramaPrincipal = repositorioRamaService.obtenerRamaPrincipal(repoId);
            String nombreRama = ramaPrincipal.getNombreRama();
            
            // NOTA: No podemos determinar el path exacto del nodo sin recorrer la jerarquía
            // Por simplicidad, redirigimos a la raíz de la rama principal
            logger.info("   🎯 Redirigiendo a /tree/{} (raíz)", nombreRama);
            return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repoId + "/tree/" + nombreRama;
            
        } catch (Exception e) {
            logger.error("   ❌ Error en redirect: {}", e.getMessage());
            // Fallback al árbol principal
            return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repoId + "/tree/main";
        }
    }

    // =================== OPERACIONES DE ARCHIVOS ===================

    /**
     * Subir archivo(s) al repositorio
     * Ruta: POST /devportal/{rol}/{username}/repositories/R-{repoId}/files/upload
     */
    @PostMapping("/upload")
    public String uploadFile(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long repoId,
                            @RequestParam("files") MultipartFile[] files,
                            @RequestParam(value = "parentNodeId", required = false) Long parentNodeId,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {

        logger.info("📤 Subiendo {} archivo(s) al repositorio R-{}", files.length, repoId);

        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

            // Verificar permisos de escritura
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
            if ("LECTOR".equals(userPermission) || "SIN_ACCESO".equals(userPermission)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para subir archivos");
                return redirectToCurrentLocation(rol, username, repoId, parentNodeId);
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

        return redirectToCurrentLocation(rol, username, repoId, parentNodeId);
    }

    /**
     * Descargar un archivo
     * Ruta: GET /devportal/{rol}/{username}/repositories/R-{repoId}/files/N-{nodeId}/download
     */
    /**
     * Ver contenido de un archivo (para texto/código)
     * Ruta: GET /devportal/{rol}/{username}/repositories/R-{repoId}/files/N-{nodeId}/view
     */
    @GetMapping("/N-{nodeId}/view")
    public String viewFile(@PathVariable String rol,
                          @PathVariable String username,
                          @PathVariable Long repoId,
                          @PathVariable Long nodeId,
                          Model model,
                          Principal principal) {

        logger.info("👁️ Visualizando archivo N-{} del repositorio R-{}", nodeId, repoId);

        Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
        
        // Obtener detalles del repositorio
        Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
        String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
        
        if ("SIN_ACCESO".equals(userPermission)) {
            return "redirect:/devportal/" + rol + "/" + username + "/repositories?error=access-denied";
        }

        // Obtener el nodo actual (debe ser un archivo)
        Nodo nodo = nodoService.obtenerPorId(nodeId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));

        if (nodo.getTipo() != Nodo.TipoNodo.ARCHIVO) {
            // Si es carpeta, redirigir a la vista de carpeta
            return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repoId + "/files/N-" + nodeId;
        }

        // Obtener jerarquía de carpetas (breadcrumbs)
        List<Map<String, Object>> breadcrumbPath = nodoService.obtenerJerarquiaNodo(nodeId);
        logger.info("   📂 Ruta de carpetas: {} niveles", breadcrumbPath.size());

        model.addAttribute("user", currentUser);
        model.addAttribute("Usuario", currentUser);  // Para chatbot widget
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("rol", rol);
        model.addAttribute("username", username);
        model.addAttribute("repositorio", repoDetails);
        model.addAttribute("nodo", nodo);
        model.addAttribute("breadcrumbPath", breadcrumbPath);
        model.addAttribute("userPermission", userPermission);
        model.addAttribute("canEdit", true); // Todos pueden editar

        return "repository/file-view";
    }

    /**
     * Descargar archivo
     * Ruta: GET /devportal/{rol}/{username}/repositories/R-{repoId}/files/N-{nodeId}/download
     */
    @GetMapping("/N-{nodeId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String rol,
                                                 @PathVariable String username,
                                                 @PathVariable Long repoId,
                                                 @PathVariable Long nodeId,
                                                 Principal principal) {

        logger.info("📥 Descargando archivo N-{} del repositorio R-{}", nodeId, repoId);

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
     * Ruta: POST /devportal/{rol}/{username}/repositories/R-{repoId}/files/create-folder
     */
    @PostMapping("/create-folder")
    public String createFolder(@PathVariable String rol,
                              @PathVariable String username,
                              @PathVariable Long repoId,
                              @RequestParam("folderName") String folderName,
                              @RequestParam(value = "parentNodeId", required = false) Long parentNodeId,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {

        logger.info("📁 Creando carpeta '{}' en repositorio R-{}", folderName, repoId);

        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
            if ("LECTOR".equals(userPermission) || "SIN_ACCESO".equals(userPermission)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para crear carpetas");
                return redirectToCurrentLocation(rol, username, repoId, parentNodeId);
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

        return redirectToCurrentLocation(rol, username, repoId, parentNodeId);
    }

    /**
     * Renombrar archivo o carpeta
     * Ruta: POST /devportal/{rol}/{username}/repositories/R-{repoId}/files/N-{nodeId}/rename
     */
    @PostMapping("/N-{nodeId}/rename")
    public String renameNode(@PathVariable String rol,
                            @PathVariable String username,
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
                return "redirect:" + getRefererOrDefault(rol, username, repoId, null);
            }

            Nodo nodo = nodoService.obtenerPorId(nodeId)
                    .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));

            nodoService.renombrarNodo(nodeId, newName, currentUser);

            redirectAttributes.addFlashAttribute("success", "Renombrado exitosamente");

            // Volver a la carpeta padre
            return redirectToCurrentLocation(rol, username, repoId, nodo.getParentId());

        } catch (Exception e) {
            logger.error("Error renombrando nodo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al renombrar: " + e.getMessage());
            return "redirect:" + getRefererOrDefault(rol, username, repoId, null);
        }
    }

    /**
     * Eliminar archivo o carpeta (soft delete)
     * Ruta: POST /devportal/{rol}/{username}/repositories/R-{repoId}/files/N-{nodeId}/delete
     */
    @PostMapping("/N-{nodeId}/delete")
    public String deleteNode(@PathVariable String rol,
                            @PathVariable String username,
                            @PathVariable Long repoId,
                            @PathVariable Long nodeId,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {

        logger.info("🗑️ Eliminando nodo N-{} del repositorio R-{}", nodeId, repoId);

        try {
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);

            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
            if ("LECTOR".equals(userPermission) || "SIN_ACCESO".equals(userPermission)) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar");
                return "redirect:" + getRefererOrDefault(rol, username, repoId, null);
            }

            Nodo nodo = nodoService.obtenerPorId(nodeId)
                    .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));

            nodoService.eliminarNodo(nodeId, currentUser.getUsuarioId());

            redirectAttributes.addFlashAttribute("success", "Eliminado exitosamente");

            // Volver a la carpeta padre
            return redirectToCurrentLocation(rol, username, repoId, nodo.getParentId());

        } catch (Exception e) {
            logger.error("Error eliminando nodo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
            return "redirect:" + getRefererOrDefault(rol, username, repoId, null);
        }
    }

    /**
     * Mover nodo(s) a otra carpeta
     * Ruta: POST /devportal/{rol}/{username}/repositories/R-{repoId}/files/move
     */
    @PostMapping("/move")
    public String moveNodes(@PathVariable String rol,
                           @PathVariable String username,
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
                return redirectToCurrentLocation(rol, username, repoId, targetNodeId);
            }

            for (Long nodeId : nodeIds) {
                nodoService.moverNodo(nodeId, targetNodeId);
            }

            redirectAttributes.addFlashAttribute("success", nodeIds.size() + " elemento(s) movido(s) exitosamente");

        } catch (Exception e) {
            logger.error("Error moviendo nodos: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al mover: " + e.getMessage());
        }

        return redirectToCurrentLocation(rol, username, repoId, targetNodeId);
    }

    // =================== UTILIDADES ===================

    private String redirectToCurrentLocation(String rol, String username, Long repoId, Long parentNodeId) {
        String baseUrl = "/devportal/" + rol + "/" + username + "/repositories/R-" + repoId + "/files";
        if (parentNodeId != null) {
            return "redirect:" + baseUrl + "/N-" + parentNodeId;
        }
        return "redirect:" + baseUrl;
    }

    private String getRefererOrDefault(String rol, String username, Long repoId, Long nodeId) {
        String baseUrl = "/devportal/" + rol + "/" + username + "/repositories/R-" + repoId + "/files";
        if (nodeId != null) {
            return baseUrl + "/N-" + nodeId;
        }
        return baseUrl;
    }

    // =================== NAVEGACIÓN CON RAMAS (GitHub-style) ===================

    /**
     * Vista de archivos del repositorio con selección de rama
     * Ruta: GET /devportal/{rol}/{username}/repositories/R-{repoId}/tree/{branch}/**
     * Ejemplo: /repositories/R-32/tree/main/src/controllers
     */
    @GetMapping(value = {"/tree/{branch}", "/tree/{branch}/**"}, produces = "text/html")
    public String showRepositoryTreeWithBranch(@PathVariable String rol,
                                               @PathVariable String username,
                                               @PathVariable Long repoId,
                                               @PathVariable String branch,
                                               HttpServletRequest request,
                                               Model model,
                                               Principal principal) {

        logger.info("=".repeat(80));
        logger.info("🌿 [TREE-VIEW] Mostrando archivos del repositorio R-{} en rama '{}'", repoId, branch);
        
        try {
            // 🔍 LOG DETALLADO: Request info
            String fullPath = request.getRequestURI();
            logger.info("   📥 Request URI completo: {}", fullPath);
            logger.info("   📍 Branch parameter: {}", branch);
            
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            
            // Obtener repositorio
            Repositorio repositorio = repositorioRepository.findById(repoId)
                    .orElseThrow(() -> new RuntimeException("Repositorio no encontrado"));
            
            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
            if ("SIN_ACCESO".equals(userPermission)) {
                return "redirect:/devportal/" + rol + "/" + username + "/repositories?error=access-denied";
            }

            // Obtener o validar la rama
            RepositorioRama rama = repositorioRamaService.obtenerRamaOPrincipal(repoId, branch);
            logger.info("   🌿 Rama encontrada: {} (ID: {})", rama.getNombreRama(), rama.getRamaId());

            // Extraer path después de /tree/{branch}/
            String basePath = "/devportal/" + rol + "/" + username + "/repositories/R-" + repoId + "/tree/" + branch;
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
                    return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repoId + "/tree/" + branch;
                }
                
                currentNode = nodoOpt.get();
                
                if (currentNode.getTipo() == Nodo.TipoNodo.ARCHIVO) {
                    // Si es archivo, redirigir a vista de archivo
                    return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repoId + "/blob/" + branch + "/" + path;
                }
                
                nodos = nodoService.obtenerHijosDTO(currentNode.getNodoId());
                logger.info("   ✅ Carpeta '{}' - {} nodos", currentNode.getNombre(), nodos.size());
            }

            // Construir breadcrumbs con rama
            List<Map<String, Object>> breadcrumbs = nodoService.construirBreadcrumbsConRama(
                currentNode, repositorio, rama.getNombreRama(), rol, username
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
            return "repository/files";
            
        } catch (Exception e) {
            logger.error("💥 Error al cargar vista con rama: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Vista de archivo individual con información de rama
     * Ruta: GET /devportal/{rol}/{username}/repositories/R-{repoId}/blob/{branch}/**
     * Ejemplo: /repositories/R-32/blob/main/src/Main.java
     */
    @GetMapping(value = "/blob/{branch}/**", produces = "text/html")
    public String viewFileWithBranch(@PathVariable String rol,
                                     @PathVariable String username,
                                     @PathVariable Long repoId,
                                     @PathVariable String branch,
                                     HttpServletRequest request,
                                     Model model,
                                     Principal principal) {

        logger.info("=".repeat(80));
        logger.info("📄 [BLOB-VIEW] Visualizando archivo del repositorio R-{} en rama '{}'", repoId, branch);
        
        try {
            // 🔍 LOG DETALLADO: Request info
            String fullPath = request.getRequestURI();
            logger.info("   📥 Request URI completo: {}", fullPath);
            logger.info("   📍 Branch parameter: {}", branch);
            
            Usuario currentUser = userService.obtenerUsuarioActualSinUsername(principal);
            
            // Obtener repositorio
            Repositorio repositorio = repositorioRepository.findById(repoId)
                    .orElseThrow(() -> new RuntimeException("Repositorio no encontrado"));
            
            // Verificar permisos
            Map<String, Object> repoDetails = repositoryService.obtenerDetallesRepositorio(currentUser.getUsuarioId(), repoId);
            String userPermission = (String) repoDetails.get("privilegio_usuario_actual");
            
            if ("SIN_ACCESO".equals(userPermission)) {
                return "redirect:/devportal/" + rol + "/" + username + "/repositories?error=access-denied";
            }

            // Obtener rama
            RepositorioRama rama = repositorioRamaService.obtenerRamaOPrincipal(repoId, branch);
            logger.info("   🌿 Rama encontrada: {} (ID: {})", rama.getNombreRama(), rama.getRamaId());

            // Extraer path del archivo
            String basePath = "/devportal/" + rol + "/" + username + "/repositories/R-" + repoId + "/blob/" + branch + "/";
            String path = fullPath.startsWith(basePath) ? fullPath.substring(basePath.length()) : "";
            
            logger.info("   📂 Base path esperado: {}", basePath);
            logger.info("   📂 Path extraído: '{}' (length: {})", path, path.length());
            logger.info("   📂 Path está vacío: {}", path.isEmpty());
            
            if (path.isEmpty()) {
                logger.warn("   ❌ Path vacío - Redirigiendo a tree");
                return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repoId + "/tree/" + branch;
            }

            // Resolver nodo del archivo
            Nodo archivo = nodoService.resolverPathANodoConRama(path, Nodo.ContainerType.REPOSITORIO, repoId, rama.getRamaId())
                    .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));

            if (archivo.getTipo() != Nodo.TipoNodo.ARCHIVO) {
                // Si es carpeta, redirigir a tree
                return "redirect:/devportal/" + rol + "/" + username + "/repositories/R-" + repoId + "/tree/" + branch + "/" + path;
            }

            // Construir breadcrumbs (incluye la carpeta padre del archivo)
            List<Map<String, Object>> breadcrumbs;
            if (archivo.getParentId() != null) {
                Nodo nodoPadre = nodoService.obtenerPorId(archivo.getParentId()).orElse(null);
                breadcrumbs = nodoService.construirBreadcrumbsConRama(nodoPadre, repositorio, rama.getNombreRama(), rol, username);
            } else {
                breadcrumbs = nodoService.construirBreadcrumbsConRama(null, repositorio, rama.getNombreRama(), rol, username);
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
            return "repository/file-view";
            
        } catch (Exception e) {
            logger.error("💥 Error al visualizar archivo: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Método auxiliar para obtener nodos raíz con filtro de rama
     * NOTA: Este método será implementado en NodoService
     */
    private List<NodoDTO> obtenerNodosRaizConRama(Long repositorioId, Long ramaId) {
        // Temporal: usar método sin rama
        return nodoService.obtenerNodosRaizDTO(Nodo.ContainerType.REPOSITORIO, repositorioId);
    }
}
