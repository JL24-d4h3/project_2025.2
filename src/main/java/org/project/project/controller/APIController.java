package org.project.project.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.project.project.model.entity.API;
import org.project.project.model.entity.VersionAPI;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.SolicitudPublicacionVersionApi;
import org.project.project.model.entity.SolicitudPublicacionVersionApi.EstadoSolicitud;
import org.project.project.model.dto.api.CrearApiDTO;
import org.project.project.model.dto.api.ApiResponseDTO;
import org.project.project.repository.APIRepository;
import org.project.project.repository.CategoriaRepository;
import org.project.project.repository.EtiquetaRepository;
import org.project.project.repository.UsuarioRepository;
import org.project.project.service.APIService;
import org.project.project.service.ApiContractStorageService;
import org.project.project.service.SolicitudPublicacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

// Imports para manejo de archivos multipart
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

// Imports para manejo de JSON (secciones CMS)
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

// Import del DTO de secciones CMS
import org.project.project.model.dto.api.SeccionCmsDTO;

@Slf4j
@Controller
@RequestMapping("/devportal/{role}/{username}/")
public class APIController {

    final CategoriaRepository categoriaRepository;
    final EtiquetaRepository etiquetaRepository;
    final APIRepository apiRepository;
    final UsuarioRepository usuarioRepository;
    
    public APIController(CategoriaRepository categoriaRepository, EtiquetaRepository etiquetaRepository, 
                        APIRepository apiRepository, UsuarioRepository usuarioRepository) {
        this.categoriaRepository = categoriaRepository;
        this.etiquetaRepository = etiquetaRepository;
        this.apiRepository = apiRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Autowired
    private APIService apiService;
    
    @Autowired
    private ApiContractStorageService apiContractStorageService;
    
    @Autowired
    private SolicitudPublicacionService solicitudPublicacionService;


    @GetMapping("apis/{id}")
    public ResponseEntity<API> getApiById(@PathVariable Long id) {
        API api = apiService.buscarApiPorId(id);
        return ResponseEntity.ok(api);
    }


    @PutMapping("apis/{id}")
    public ResponseEntity<API> updateApi(@PathVariable Long id, @RequestBody API apiDetails) {
        API updatedApi = apiService.actualizarApi(id, apiDetails);
        return ResponseEntity.ok(updatedApi);
    }

    @DeleteMapping("apis/{id}")
    public ResponseEntity<Void> deleteApi(@PathVariable Long id) {
        apiService.eliminarApi(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("apis")
    public String showAPIs(@PathVariable String role,
                          @PathVariable String username,
                          Model model,
                          HttpServletResponse response) {
        
        // Añadir cabeceras para prevenir el caché del navegador
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // Validar autenticación
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/signin";
        }

        // Obtener datos del usuario autenticado
        String authUsername = authentication.getName();
        String authRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("dev")
                .toLowerCase();

        // Verificar autorización - que el usuario acceda a su propia URL
        if (!authUsername.equals(username) || !authRole.equals(role)) {
            return "redirect:/devportal/" + authRole + "/" + authUsername + "/apis";
        }

        // Añadir atributos al modelo
        // Obtener el usuario actual para el chatbot widget
        var currentUser = usuarioRepository.findByUsername(authUsername).orElse(null);
        model.addAttribute("Usuario", currentUser);

        model.addAttribute("userRole", role);
        model.addAttribute("username", username);
        model.addAttribute("rol", role);  // Para compatibilidad con navbar
        model.addAttribute("currentNavSection", "apis");
        
        // Obtener el ID del usuario actual para contar sus APIs
        Long createdApisCount = usuarioRepository.findByUsername(authUsername)
            .map(usuario -> {
                Long count = apiRepository.countDistinctApisByCreatedByUserId(usuario.getUsuarioId());
                log.info("📊 Total APIs creadas por usuario {} (ID: {}): {}", authUsername, usuario.getUsuarioId(), count);
                return count;
            })
            .orElse(0L);
        
        // Obtener el conteo de APIs en estado BORRADOR creadas por el usuario
        Long apisInDraftCount = usuarioRepository.findByUsername(authUsername)
            .map(usuario -> {
                Long count = apiRepository.countApisByCreatedByUserIdAndEstado(usuario.getUsuarioId(), API.EstadoApi.BORRADOR);
                log.info("📊 APIs en BORRADOR de usuario {} (ID: {}): {}", authUsername, usuario.getUsuarioId(), count);
                return count;
            })
            .orElse(0L);
        
        // Obtener el conteo de APIs en revisión (estado QA) donde el usuario es creador
        Long apisInReviewCount = usuarioRepository.findByUsername(authUsername)
            .map(usuario -> {
                Long count = apiRepository.countApisByCreatedByUserIdAndEstado(usuario.getUsuarioId(), API.EstadoApi.QA);
                log.info("📊 APIs en QA de usuario {} (ID: {}): {}", authUsername, usuario.getUsuarioId(), count);
                return count;
            })
            .orElse(0L);
        
        // Obtener el conteo de APIs PUBLICADAS (estado PRODUCCION) creadas por el usuario
        Long apisPublishedCount = usuarioRepository.findByUsername(authUsername)
            .map(usuario -> {
                Long count = apiRepository.countApisByCreatedByUserIdAndEstado(usuario.getUsuarioId(), API.EstadoApi.PRODUCCION);
                log.info("📊 APIs en PRODUCCION de usuario {} (ID: {}): {}", authUsername, usuario.getUsuarioId(), count);
                return count;
            })
            .orElse(0L);
        
        // Obtener las APIs creadas por el usuario para mostrar en las cartas
        List<API> userCreatedApis = usuarioRepository.findByUsername(authUsername)
            .map(usuario -> {
                List<API> apis = apiRepository.findDistinctApisByCreatedByUserId(usuario.getUsuarioId());
                log.info("📊 Usuario: {} (ID: {}) tiene {} APIs creadas", 
                    authUsername, usuario.getUsuarioId(), apis.size());
                // Forzar inicialización de relaciones lazy
                for (API api : apis) {
                    log.info("  - API: {} (ID: {})", api.getNombreApi(), api.getApiId());
                    // Inicializar versiones
                    if (api.getVersiones() != null) {
                        api.getVersiones().size();
                    }
                    // Inicializar categorías
                    if (api.getCategorias() != null) {
                        api.getCategorias().size();
                    }
                    // Inicializar etiquetas
                    if (api.getEtiquetas() != null) {
                        api.getEtiquetas().size();
                    }
                }
                return apis;
            })
            .orElse(List.of());
        
        // Obtener algunas APIs de otros usuarios para mostrar en la sección "Otras APIs"
        List<API> otherUsersApis = usuarioRepository.findByUsername(authUsername)
            .map(usuario -> {
                List<API> apis = apiRepository.findDistinctApisByOtherUsers(usuario.getUsuarioId());
                log.info("🔍 APIs de otros usuarios para {}: {} encontradas", authUsername, apis.size());
                // Forzar inicialización de relaciones lazy
                for (API api : apis) {
                    if (api.getVersiones() != null) {
                        api.getVersiones().size();
                    }
                    if (api.getCategorias() != null) {
                        api.getCategorias().size();
                    }
                    if (api.getEtiquetas() != null) {
                        api.getEtiquetas().size();
                    }
                }
                // Limitar a 6 APIs para no sobrecargar la vista
                return apis.size() > 6 ? apis.subList(0, 6) : apis;
            })
            .orElse(List.of());

        // ==================== LÓGICA PARA VISTA QA (FASE 3.4) ====================
        
        // Detectar rol del usuario
        boolean isQA = currentUser != null && currentUser.getRoles().stream()
            .anyMatch(r -> "QA".equalsIgnoreCase(r.getNombreRolString()));
        boolean isProvider = currentUser != null && currentUser.getRoles().stream()
            .anyMatch(r -> {
                String roleName = r.getNombreRolString();
                return "PROVIDER".equalsIgnoreCase(roleName) || 
                       "DEV".equalsIgnoreCase(roleName) || 
                       "PO".equalsIgnoreCase(roleName);
            });
        
        model.addAttribute("isQA", isQA);
        model.addAttribute("isProvider", isProvider);
        
        // Para QA: Cargar solicitudes pendientes de revisión
        if (isQA) {
            List<SolicitudPublicacionVersionApi> solicitudesPendientes = 
                solicitudPublicacionService.obtenerSolicitudesParaQA();
            model.addAttribute("solicitudesPendientes", solicitudesPendientes);
            model.addAttribute("solicitudesPendientesCount", solicitudesPendientes.size());
            log.info("📋 Usuario QA {} tiene {} solicitudes pendientes de revisión", 
                authUsername, solicitudesPendientes.size());
        }
        
        // Para PROVIDER: Cargar mis solicitudes en revisión (PENDIENTE o EN_REVISION)
        if (isProvider) {
            List<SolicitudPublicacionVersionApi> misSolicitudes = 
                solicitudPublicacionService.obtenerSolicitudesDeUsuario(authUsername);
            // Filtrar solo PENDIENTE y EN_REVISION
            List<SolicitudPublicacionVersionApi> solicitudesEnRevision = 
                misSolicitudes.stream()
                    .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE || 
                                s.getEstado() == EstadoSolicitud.EN_REVISION)
                    .collect(Collectors.toList());
            model.addAttribute("misSolicitudesEnRevision", solicitudesEnRevision);
            model.addAttribute("solicitudesEnRevisionCount", solicitudesEnRevision.size());
            log.info("⏳ Usuario PROVIDER {} tiene {} solicitudes en revisión", 
                authUsername, solicitudesEnRevision.size());
        }

        model.addAttribute("Usuario", currentUser);
        model.addAttribute("createdApisCount", createdApisCount);
        model.addAttribute("apisInDraftCount", apisInDraftCount);
        model.addAttribute("apisInReviewCount", apisInReviewCount);
        model.addAttribute("apisPublishedCount", apisPublishedCount);
        model.addAttribute("userCreatedApis", userCreatedApis);
        model.addAttribute("otherUsersApis", otherUsersApis);
        
        return "api/overview";
    }

    @GetMapping("apis/create")
    public String showCreateApiForm(@PathVariable String role,
                                   @PathVariable String username,
                                   Model model,
                                   HttpServletResponse response) {
        
        // Añadir cabeceras para prevenir el caché del navegador
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // Validar autenticación
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/signin";
        }

        // Obtener datos del usuario autenticado
        String authUsername = authentication.getName();
        String authRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("dev")
                .toLowerCase();

        // Verificar autorización - que el usuario acceda a su propia URL
        if (!authUsername.equals(username) || !authRole.equals(role)) {
            return "redirect:/devportal/" + authRole + "/" + authUsername + "/apis/create";
        }

        // Añadir atributos al modelo
        // Obtener el usuario actual para el chatbot widget
        var currentUser = usuarioRepository.findByUsername(authUsername).orElse(null);
        model.addAttribute("Usuario", currentUser);
        model.addAttribute("userRole", role);
        model.addAttribute("username", username);
        model.addAttribute("rol", role);  // Para compatibilidad con navbar
        model.addAttribute("currentNavSection", "apis");
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("etiquetas", etiquetaRepository.findAll());
        
        return "api/create-api";
    }

    /**
     * Endpoint unificado para crear una nueva API.
     * 
     * Soporta dos escenarios:
     * 1. **API Básica**: Solo información general + contrato OpenAPI (sin secciones CMS)
     *    → Llama a `crearApiBasica()`
     * 
     * 2. **API con Documentación CMS**: Información general + contrato + secciones de documentación con archivos
     *    → Llama a `crearApiConDocumentacion()`
     * 
     * El frontend envía un FormData con:
     * - Campos individuales: apiName, apiVersion, apiDescription, etc.
     * - JSON de secciones CMS: seccionesCmsJson (si hay secciones)
     * - Archivos: cmsFiles_{sectionIndex}_{fileIndex}
     * 
     * @param role Rol del usuario (dev, qa, po, admin)
     * @param username Username del usuario
     * @param request HttpServletRequest para extraer parámetros y archivos
     * @return ResponseEntity con el resultado de la operación
     */
    @PostMapping("apis/create")
    public ResponseEntity<?> createApi(@PathVariable String role,
                                      @PathVariable String username,
                                      HttpServletRequest request,
                                      @RequestParam(value = "seccionesCmsJson", required = false) String seccionesCmsJson,
                                      @RequestParam("apiName") String apiName,
                                      @RequestParam("apiVersion") String apiVersion,
                                      @RequestParam("apiDescription") String apiDescription,
                                      @RequestParam(value = "categories", required = false) List<Long> categoryIds,
                                      @RequestParam(value = "tags", required = false) List<Long> tagIds,
                                      @RequestParam("contractContent") String contractContent,
                                      @RequestParam(value = "baseUrl", required = false) String baseUrl) {
        
        try {
            // ===== VALIDACIÓN DE AUTENTICACIÓN =====
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "No autorizado"
                ));
            }

            String authUsername = authentication.getName();
            String authRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .orElse("dev")
                    .toLowerCase();

            // Verificar que el usuario acceda a su propia ruta
            if (!authUsername.equals(username) || !authRole.equals(role)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Sin permisos para esta operación"
                ));
            }

            // ===== VALIDACIÓN DE NOMBRE ÚNICO =====
            boolean nameExists = apiRepository.existsByNombreApiIgnoreCase(apiName.trim());
            if (nameExists) {
                return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "El nombre de la API ya existe. Por favor elige otro nombre."
                ));
            }

            // ===== CONSTRUCCIÓN DEL DTO =====
            CrearApiDTO dto = CrearApiDTO.builder()
                    .nombre(apiName.trim())
                    .version(apiVersion.trim())
                    .descripcion(apiDescription.trim())
                    .categoriaIds(categoryIds)
                    .tagIds(tagIds)
                    .contratoYaml(contractContent)
                    .baseUrl(baseUrl)
                    .creadoPorUsername(authUsername)
                    .estadoApi("BORRADOR")
                    .build();

            // ===== ESCENARIO 1: API BÁSICA (SIN SECCIONES CMS) =====
            if (seccionesCmsJson == null || seccionesCmsJson.trim().isEmpty() || "[]".equals(seccionesCmsJson.trim())) {
                log.info("📝 Creando API básica sin secciones CMS: '{}'", apiName);
                
                ApiResponseDTO response = apiService.crearApiBasica(dto);
                
                if (response.getSuccess() != null && response.getSuccess()) {
                    log.info("✅ API básica '{}' creada exitosamente por usuario '{}'", apiName, authUsername);
                    
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "✅ API '" + apiName + "' creada exitosamente. Redirigiendo...",
                        "redirectUrl", "/devportal/" + role + "/" + username + "/apis",
                        "apiId", response.getApiId(),
                        "showPopup", true
                    ));
                } else {
                    return ResponseEntity.status(500).body(Map.of(
                        "success", false,
                        "message", "❌ Error al crear la API: " + response.getMessage(),
                        "showPopup", true
                    ));
                }
            }

            // ===== ESCENARIO 2: API CON DOCUMENTACIÓN CMS =====
            log.info("📚 Creando API con documentación CMS: '{}'", apiName);
            
            // Parsear JSON de secciones CMS
            ObjectMapper objectMapper = new ObjectMapper();
            List<SeccionCmsDTO> secciones = objectMapper.readValue(
                seccionesCmsJson, 
                new TypeReference<List<SeccionCmsDTO>>() {}
            );
            
            // Extraer archivos del request con naming pattern: cmsFiles_{sectionIndex}_{fileIndex}
            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
                
                for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                    String fileParamName = entry.getKey();
                    MultipartFile file = entry.getValue();
                    
                    // Pattern: cmsFiles_{sectionIndex}_{fileIndex}
                    if (fileParamName.startsWith("cmsFiles_") && !file.isEmpty()) {
                        String[] parts = fileParamName.replace("cmsFiles_", "").split("_");
                        if (parts.length == 2) {
                            int sectionIndex = Integer.parseInt(parts[0]);
                            
                            // Asignar archivo a la sección correspondiente
                            if (sectionIndex < secciones.size()) {
                                SeccionCmsDTO seccion = secciones.get(sectionIndex);
                                if (seccion.getArchivos() == null) {
                                    seccion.setArchivos(new ArrayList<>());
                                }
                                seccion.getArchivos().add(file);
                            }
                        }
                    }
                }
            }
            
            dto.setSeccionesCms(secciones);
            
            int totalArchivos = secciones.stream()
                    .mapToInt(s -> s.getArchivos() != null ? s.getArchivos().size() : 0)
                    .sum();
            log.info("📎 Total archivos detectados: {} en {} secciones", totalArchivos, secciones.size());
            
            // Crear API con documentación CMS
            ApiResponseDTO response = apiService.crearApiConDocumentacion(dto);
            
            if (response.getSuccess() != null && response.getSuccess()) {
                log.info("✅ API '{}' con documentación CMS creada exitosamente por usuario '{}'", apiName, authUsername);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "✅ API '" + apiName + "' creada exitosamente con documentación. Redirigiendo...",
                    "redirectUrl", "/devportal/" + role + "/" + username + "/apis",
                    "apiId", response.getApiId(),
                    "showPopup", true
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "❌ Error al crear la API: " + response.getMessage(),
                    "showPopup", true
                ));
            }

        } catch (Exception e) {
            log.error("❌ Error creando API: {}", e.getMessage(), e);
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "❌ Error interno del servidor: " + e.getMessage(),
                "showPopup", true
            ));
        }
    }

    @GetMapping("apis/check-name")
    @ResponseBody
    public ResponseEntity<?> checkApiNameAvailability(@RequestParam("name") String name) {
        try {
            boolean exists = apiRepository.existsByNombreApiIgnoreCase(name.trim());
            return ResponseEntity.ok().body(Map.of(
                "available", !exists,
                "message", exists ? "El nombre ya está en uso" : "El nombre está disponible"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error verificando disponibilidad del nombre"
            ));
        }
    }

    @GetMapping("apis/update")
    public String showUpdateApiForm(@PathVariable String role,
                                   @PathVariable String username,
                                   @RequestParam(value = "preselectedApiId", required = false) Long preselectedApiId,
                                   Model model,
                                   HttpServletResponse response) {
        
        // Añadir cabeceras para prevenir el caché del navegador
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // Validar autenticación
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/signin";
        }

        // Obtener datos del usuario autenticado
        String authUsername = authentication.getName();
        String authRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("dev")
                .toLowerCase();

        // Verificar autorización - que el usuario acceda a su propia URL
        if (!authUsername.equals(username) || !authRole.equals(role)) {
            return "redirect:/devportal/" + authRole + "/" + authUsername + "/apis/update";
        }

        // Obtener TODAS las APIs del sistema, no solo las del usuario
        List<API> allApis = apiRepository.findAllByOrderByApiIdDesc();
        log.info("📋 Cargadas {} APIs totales para selección", allApis.size());

        // Añadir atributos al modelo
        model.addAttribute("userRole", role);
        model.addAttribute("username", username);
        model.addAttribute("rol", role);  // Para compatibilidad con navbar
        model.addAttribute("currentNavSection", "apis");
        model.addAttribute("allApis", allApis); // Cambiar de userApis a allApis
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("etiquetas", etiquetaRepository.findAll());
        model.addAttribute("preselectedApiId", preselectedApiId); // Nueva línea para pre-selección
        
        return "api/update"; // Cambiar de edit a update
    }

    @GetMapping("apis/{id}/details")
    @ResponseBody
    public ResponseEntity<?> getApiDetails(@PathVariable String role,
                                          @PathVariable String username,
                                          @PathVariable Long id) {
        try {
            log.info("� ===== INICIANDO getApiDetails =====");
            log.info("🔍 Role: {}, Username: {}, API ID: {}", role, username, id);
            log.info("🔍 Request URL path: apis/{}/details", id);
            
            // Validar autenticación
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("🔐 Authentication present: {}", authentication != null);
            log.info("🔐 Is authenticated: {}", authentication != null && authentication.isAuthenticated());
            log.info("🔐 Principal: {}", authentication != null ? authentication.getPrincipal() : "null");
            
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                log.warn("❌ Usuario no autenticado intentando acceder a API {}", id);
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "No autorizado"
                ));
            }

            String authUsername = authentication.getName();
            String authRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .orElse("dev")
                    .toLowerCase();

            // Verificar autorización
            if (!authUsername.equals(username) || !authRole.equals(role)) {
                log.warn("❌ Usuario {} sin permisos para acceder como {}/{}", authUsername, role, username);
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Sin permisos"
                ));
            }

            // Buscar la API y verificar que existe
            API api;
            try {
                api = apiService.buscarApiPorId(id);
                log.info("✅ API encontrada: {} (ID: {})", api.getNombreApi(), api.getApiId());
            } catch (Exception e) {
                log.error("❌ API con ID {} no encontrada: {}", id, e.getMessage());
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "API no encontrada"
                ));
            }
            
            // Verificar que el usuario está autenticado (cualquier usuario puede ver detalles de APIs)
            // Nota: Removemos la verificación de ownership ya que ahora cualquier usuario puede actualizar cualquier API
            log.info("✅ Usuario autenticado {} puede ver detalles de cualquier API", authUsername);

            // Obtener detalles adicionales de la API
            Map<String, Object> apiDetails = new HashMap<>();
            apiDetails.put("id", api.getApiId());
            apiDetails.put("name", api.getNombreApi());
            apiDetails.put("description", api.getDescripcionApi());
            apiDetails.put("status", api.getEstadoApi().toString());
            
            log.info("📝 Preparando detalles de API: {}", api.getNombreApi());
            
            // Obtener versiones de la API (con null check)
            List<VersionAPI> versions = new ArrayList<>();
            if (api.getVersiones() != null && !api.getVersiones().isEmpty()) {
                versions = api.getVersiones().stream()
                    .sorted((v1, v2) -> {
                        // Primero por comparación semántica de versiones
                        int versionComparison = compareVersions(v2.getNumeroVersion(), v1.getNumeroVersion());
                        if (versionComparison != 0) {
                            return versionComparison;
                        }
                        // Si las versiones son iguales, por fecha de lanzamiento (más reciente primero)
                        int dateComparison = v2.getFechaLanzamiento().compareTo(v1.getFechaLanzamiento());
                        if (dateComparison != 0) {
                            return dateComparison;
                        }
                        // Si las fechas son iguales, ordenar por ID de versión (mayor primero)
                        return v2.getVersionId().compareTo(v1.getVersionId());
                    })
                    .toList();
                log.info("📋 Encontradas {} versiones para la API", versions.size());
                
                // Logging de debug para ver todas las versiones
                for (int i = 0; i < Math.min(versions.size(), 3); i++) {
                    VersionAPI v = versions.get(i);
                    log.info("📋 Versión {}: {} (ID: {}, Fecha: {})", 
                        i + 1, v.getNumeroVersion(), v.getVersionId(), v.getFechaLanzamiento());
                }
            } else {
                log.info("📋 No se encontraron versiones para la API");
            }
            
            if (!versions.isEmpty()) {
                VersionAPI latestVersion = versions.get(0);
                apiDetails.put("latestVersion", latestVersion.getNumeroVersion());
                apiDetails.put("latestVersionDescription", latestVersion.getDescripcionVersion());
                
                log.info("🔢 Última versión: {}", latestVersion.getNumeroVersion());
                
                // Obtener el contrato actual desde el storage
                try {
                    String contractUrl = latestVersion.getContratoApiUrl();
                    log.info("🔍 DEBUG - Contract URL en base de datos: '{}'", contractUrl);
                    log.info("🔍 DEBUG - Contract URL es null? {}", contractUrl == null);
                    log.info("🔍 DEBUG - Contract URL está vacío? {}", contractUrl != null && contractUrl.isEmpty());
                    
                    if (contractUrl != null && !contractUrl.isEmpty()) {
                        log.info("📄 Intentando cargar contrato desde GCS: {}", contractUrl);
                        try {
                            String contractContent = apiContractStorageService.getApiContract(contractUrl);
                            apiDetails.put("currentContract", contractContent);
                            log.info("✅ Contrato cargado exitosamente desde GCS, longitud: {}", contractContent.length());
                            log.info("🔍 DEBUG - Primeros 100 caracteres del contrato: {}", 
                                contractContent.length() > 100 ? contractContent.substring(0, 100) + "..." : contractContent);
                        } catch (Exception storageException) {
                            log.error("❌ Error cargando contrato desde GCS: {}", storageException.getMessage(), storageException);
                            // Generar contrato de fallback
                            String fallbackContract = createFallbackContract(api.getNombreApi(), latestVersion.getNumeroVersion());
                            apiDetails.put("currentContract", fallbackContract);
                            log.info("📄 Usando contrato de fallback, longitud: {}", fallbackContract.length());
                        }
                    } else {
                        log.warn("⚠️ No hay URL de contrato para la API - Generando fallback");
                        String fallbackContract = createFallbackContract(api.getNombreApi(), latestVersion.getNumeroVersion());
                        apiDetails.put("currentContract", fallbackContract);
                        log.info("📄 Usando contrato de fallback por falta de URL");
                    }
                } catch (Exception e) {
                    log.warn("⚠️ No se pudo cargar el contrato actual para la API {}: {}", id, e.getMessage());
                    apiDetails.put("currentContract", null);
                }
            } else {
                apiDetails.put("latestVersion", "1.0.0");
                apiDetails.put("latestVersionDescription", api.getDescripcionApi());
                apiDetails.put("currentContract", null);
                log.info("📝 Usando valores por defecto para API sin versiones");
            }
            
            // Obtener categorías de la API (con null check)
            List<Long> categoryIds = new ArrayList<>();
            if (api.getCategorias() != null && !api.getCategorias().isEmpty()) {
                categoryIds = api.getCategorias().stream()
                    .map(cat -> (long) cat.getIdCategoria())
                    .toList();
                log.info("🏷️ Categorías encontradas: {}", categoryIds.size());
            }
            apiDetails.put("categories", categoryIds);
            
            // Obtener etiquetas de la API (con null check)
            List<Long> tagIds = new ArrayList<>();
            if (api.getEtiquetas() != null && !api.getEtiquetas().isEmpty()) {
                tagIds = api.getEtiquetas().stream()
                    .map(tag -> (long) tag.getTagId())
                    .toList();
                log.info("🏷️ Etiquetas encontradas: {}", tagIds.size());
            }
            apiDetails.put("tags", tagIds);
            
            // Añadir información de todas las versiones para debug en el frontend
            List<Map<String, Object>> versionList = new ArrayList<>();
            if (api.getVersiones() != null && !api.getVersiones().isEmpty()) {
                List<VersionAPI> allVersions = api.getVersiones().stream()
                    .sorted((v1, v2) -> {
                        int versionComparison = compareVersions(v2.getNumeroVersion(), v1.getNumeroVersion());
                        if (versionComparison != 0) {
                            return versionComparison;
                        }
                        if (v2.getFechaLanzamiento() != null && v1.getFechaLanzamiento() != null) {
                            return v2.getFechaLanzamiento().compareTo(v1.getFechaLanzamiento());
                        }
                        return Long.compare(v2.getVersionId(), v1.getVersionId());
                    })
                    .toList();
                
                for (VersionAPI version : allVersions) {
                    Map<String, Object> versionInfo = new HashMap<>();
                    versionInfo.put("id", version.getVersionId());
                    versionInfo.put("numero", version.getNumeroVersion());
                    versionInfo.put("descripcion", version.getDescripcionVersion());
                    versionInfo.put("fechaLanzamiento", version.getFechaLanzamiento());
                    versionList.add(versionInfo);
                }
                log.info("📋 Array de versiones creado con {} elementos", versionList.size());
            }
            apiDetails.put("versions", versionList);
            
            log.info("✅ Detalles de API preparados exitosamente para API ID: {}", id);
            
            // Envolver en estructura de respuesta estándar
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.putAll(apiDetails);
            log.info("✅ ===== getApiDetails COMPLETADO EXITOSAMENTE =====");
            log.info("✅ Respuesta preparada para API ID: {}", id);
            log.info("✅ Total de keys en respuesta: {}", response.keySet().size());
            log.info("✅ Keys en respuesta: {}", response.keySet());
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("💥 Error inesperado obteniendo detalles de API {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    @PostMapping("apis/update")
    public ResponseEntity<?> updateApi(@PathVariable String role,
                                    @PathVariable String username,
                                    @RequestParam("apiId") Long apiId,
                                    @RequestParam("apiVersion") String apiVersion,
                                    @RequestParam("apiStatus") String apiStatus,
                                    @RequestParam("versionDescription") String versionDescription,
                                    @RequestParam(value = "categories", required = false) List<Long> categoryIds,
                                    @RequestParam(value = "tags", required = false) List<Long> tagIds,
                                    @RequestParam("contractContent") String contractContent,
                                    @RequestParam(value = "baseUrl", required = false) String baseUrl,
                                    @RequestParam(value = "documentationUrl", required = false) String documentationUrl) {
        
        try {
            // Validar autenticación
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(401).body("No autorizado");
            }

            String authUsername = authentication.getName();
            String authRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .orElse("dev")
                    .toLowerCase();

            // Verificar autorización
            if (!authUsername.equals(username) || !authRole.equals(role)) {
                return ResponseEntity.status(403).body("Sin permisos");
            }

            // Verificar que la API existe (cualquier usuario autenticado puede actualizar cualquier API)
            try {
                apiService.buscarApiPorId(apiId);
                log.info("✅ API {} encontrada, usuario {} puede actualizarla", apiId, authUsername);
            } catch (Exception e) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "API no encontrada"
                ));
            }

            // Actualizar la API usando el servicio
            apiService.actualizarApiConNuevaVersion(
                authUsername,
                apiId,
                apiVersion.trim(),
                versionDescription.trim(),
                apiStatus,
                categoryIds,
                tagIds,
                contractContent,
                baseUrl,
                documentationUrl
            );

            log.info("API ID {} actualizada exitosamente con nueva versión {} por usuario '{}'", 
                apiId, apiVersion, authUsername);

            // Retornar respuesta exitosa con la URL de redirección
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "API actualizada exitosamente con nueva versión " + apiVersion,
                "redirectUrl", "/devportal/" + role + "/" + username + "/apis"
            ));

        } catch (Exception e) {
            // Log del error
            log.error("Error updating API: {}", e.getMessage(), e);
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Genera un contrato de fallback cuando no se puede cargar el real
     */
    private String createFallbackContract(String apiName, String version) {
        return String.format("""
            {
              "openapi": "3.0.0",
              "info": {
                "title": "%s",
                "version": "%s",
                "description": "Contrato de API - No se pudo cargar el contrato original"
              },
              "paths": {
                "/example": {
                  "get": {
                    "summary": "Endpoint de ejemplo",
                    "responses": {
                      "200": {
                        "description": "Respuesta exitosa",
                        "content": {
                          "application/json": {
                            "schema": {
                              "type": "object",
                              "properties": {
                                "message": {
                                  "type": "string",
                                  "example": "Hola mundo"
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            """, apiName, version);
    }
    
    /**
     * Compara dos versiones usando semántica de versiones
     * @param version1 Primera versión (ej: "1.0.1")
     * @param version2 Segunda versión (ej: "1.0.0")
     * @return Positivo si version1 > version2, negativo si version1 < version2, 0 si son iguales
     */
    private int compareVersions(String version1, String version2) {
        try {
            String[] parts1 = version1.split("\\.");
            String[] parts2 = version2.split("\\.");
            
            int maxLength = Math.max(parts1.length, parts2.length);
            
            for (int i = 0; i < maxLength; i++) {
                int v1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
                int v2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
                
                if (v1 != v2) {
                    return Integer.compare(v1, v2);
                }
            }
            
            return 0; // Las versiones son iguales
        } catch (NumberFormatException e) {
            log.warn("⚠️ Error comparando versiones '{}' y '{}': {}", version1, version2, e.getMessage());
            // Fallback: comparación alfabética
            return version1.compareTo(version2);
        }
    }
    
    /**
     * Vista de detalles de una API específica
     */
    @GetMapping("apis/api-{apiId}")
    public String showApiDetails(@PathVariable String role,
                                @PathVariable String username,
                                @PathVariable Long apiId,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                Model model,
                                HttpServletResponse response) {
        
        // Añadir cabeceras para prevenir el caché del navegador
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // Validar autenticación
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/signin";
        }

        // Obtener datos del usuario autenticado
        String authUsername = authentication.getName();
        String authRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("dev")
                .toLowerCase();

        // Verificar autorización - que el usuario acceda a su propia URL
        if (!authUsername.equals(username) || !authRole.equals(role)) {
            return "redirect:/devportal/" + authRole + "/" + authUsername + "/apis/A-" + apiId;
        }

        try {
            // Buscar la API con todas sus versiones
            Optional<API> apiOpt = apiRepository.findByIdWithVersions(apiId);
            if (!apiOpt.isPresent()) {
                log.warn("API con ID {} no encontrada", apiId);
                return "redirect:/devportal/" + role + "/" + username + "/apis?error=api-not-found";
            }

            API api = apiOpt.get();
            log.info("📋 Mostrando detalles de API: {} (ID: {})", api.getNombreApi(), apiId);

            // Obtener y ordenar las versiones
            List<VersionAPI> allVersions = new ArrayList<>();
            if (api.getVersiones() != null && !api.getVersiones().isEmpty()) {
                allVersions = new ArrayList<>(api.getVersiones());
                
                // Ordenar versiones (más reciente primero)
                allVersions.sort((v1, v2) -> {
                    // Primero comparar por versión semántica
                    int versionComparison = compareVersions(v2.getNumeroVersion(), v1.getNumeroVersion());
                    if (versionComparison != 0) {
                        return versionComparison;
                    }
                    
                    // Si las versiones son iguales, comparar por fecha
                    if (v1.getFechaLanzamiento() != null && v2.getFechaLanzamiento() != null) {
                        return v2.getFechaLanzamiento().compareTo(v1.getFechaLanzamiento());
                    }
                    
                    // Si las fechas son nulas, comparar por ID (más reciente primero)
                    return Long.compare(v2.getVersionId(), v1.getVersionId());
                });
            }

            // Implementar paginación simple
            int totalVersions = allVersions.size();
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalVersions);
            
            List<VersionAPI> paginatedVersions = startIndex < totalVersions ? 
                allVersions.subList(startIndex, endIndex) : new ArrayList<>();

            // Calcular información de paginación
            int totalPages = (int) Math.ceil((double) totalVersions / size);
            boolean hasNext = page < totalPages - 1;
            boolean hasPrevious = page > 0;

            // Encontrar el creador de la API (creador de la primera versión)
            String apiCreator = "No especificado";
            Long apiCreatorId = null;
            if (!allVersions.isEmpty()) {
                // Ordenar por fecha de creación o por ID para encontrar la primera versión
                VersionAPI firstVersion = allVersions.stream()
                    .min((v1, v2) -> {
                        if (v1.getFechaLanzamiento() != null && v2.getFechaLanzamiento() != null) {
                            return v1.getFechaLanzamiento().compareTo(v2.getFechaLanzamiento());
                        }
                        return Long.compare(v1.getVersionId(), v2.getVersionId());
                    })
                    .orElse(allVersions.get(0));
                
                if (firstVersion.getCreadoPor() != null) {
                    apiCreator = firstVersion.getCreadoPor().getUsername();
                    apiCreatorId = firstVersion.getCreadoPor().getUsuarioId();
                }
            }

            // ✅ Obtener usuario autenticado y calcular permisos
            boolean canEdit = false; // Editar API en BORRADOR
            boolean canCreateNewVersion = false; // Crear nueva versión (solo si está en PRODUCCION)
            boolean hasActiveDeployment = false; // Si hay al menos una versión con deployment ACTIVE
            String activeVersionNumber = null; // Número de la versión con deployment ACTIVE
            
            // Verificar si hay deployment ACTIVE y obtener su número de versión
            if (!allVersions.isEmpty()) {
                Optional<VersionAPI> activeVersion = allVersions.stream()
                    .filter(v -> v.getDeploymentStatus() != null && 
                                v.getDeploymentStatus() == VersionAPI.DeploymentStatus.ACTIVE)
                    .findFirst();
                
                if (activeVersion.isPresent()) {
                    hasActiveDeployment = true;
                    activeVersionNumber = activeVersion.get().getNumeroVersion();
                }
            }
            
            Optional<Usuario> currentUserOpt = usuarioRepository.findByUsername(authUsername);
            if (currentUserOpt.isPresent()) {
                Usuario currentUser = currentUserOpt.get();
                Long currentUserId = currentUser.getUsuarioId();
                
                // CASO 1: API en BORRADOR → Solo el creador puede editar
                if (api.getEstadoApi() == API.EstadoApi.BORRADOR) {
                    canEdit = currentUserId.equals(apiCreatorId);
                }
                
                // CASO 2: API en PRODUCCION → Pueden crear nueva versión:
                //   - El creador original de la API
                //   - Cualquier usuario que ya haya publicado alguna versión de esta API
                if (api.getEstadoApi() == API.EstadoApi.PRODUCCION) {
                    boolean isOriginalCreator = currentUserId.equals(apiCreatorId);
                    
                    boolean hasPublishedVersion = allVersions.stream()
                        .anyMatch(v -> v.getCreadoPor() != null && 
                                      v.getCreadoPor().getUsuarioId().equals(currentUserId));
                    
                    canCreateNewVersion = isOriginalCreator || hasPublishedVersion;
                }
                
                log.info("🔐 Permisos para usuario {} (ID:{}): canEdit={}, canCreateNewVersion={}, hasActiveDeployment={}, estadoAPI={}", 
                        authUsername, currentUserId, canEdit, canCreateNewVersion, hasActiveDeployment, api.getEstadoApi());
            }

            // Añadir atributos al modelo
            model.addAttribute("userRole", role);
            model.addAttribute("username", username);
            model.addAttribute("rol", role);  // Para compatibilidad con navbar
            model.addAttribute("currentNavSection", "apis");
            model.addAttribute("api", api);
            model.addAttribute("apiCreator", apiCreator);
            model.addAttribute("versions", paginatedVersions);
            model.addAttribute("canEdit", canEdit);
            model.addAttribute("canCreateNewVersion", canCreateNewVersion);
            model.addAttribute("hasActiveDeployment", hasActiveDeployment);
            model.addAttribute("activeVersionNumber", activeVersionNumber);
            
            // Atributos de paginación
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalVersions", totalVersions);
            model.addAttribute("hasNext", hasNext);
            model.addAttribute("hasPrevious", hasPrevious);
            model.addAttribute("pageSize", size);
            
            // URLs para paginación
            String baseUrl = "/devportal/" + role + "/" + username + "/apis/A-" + apiId;
            model.addAttribute("baseUrl", baseUrl);

            log.info("✅ Detalles de API cargados: {} versiones (página {}/{})", 
                totalVersions, page + 1, totalPages);

            return "api/api-details";

        } catch (Exception e) {
            log.error("Error cargando detalles de API {}: {}", apiId, e.getMessage(), e);
            return "redirect:/devportal/" + role + "/" + username + "/apis?error=load-error";
        }
    }
    
    /**
     * Endpoint para obtener el contrato de una versión específica (usado por el modal)
     */
    @GetMapping("apis/version/{versionId}/contract")
    @ResponseBody
    public ResponseEntity<?> getVersionContract(@PathVariable String role,
                                              @PathVariable String username,
                                              @PathVariable Long versionId) {
        try {
            // Validar autenticación
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Usuario no autenticado"
                ));
            }

            String authUsername = authentication.getName();
            String authRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .orElse("dev")
                    .toLowerCase();

            // Logging para debug
            log.debug("🔐 Usuario autenticado: {} (rol: {}), Solicitando acceso a: {} (rol: {})", 
                authUsername, authRole, username, role);

            // Verificar autorización - permitir acceso si el usuario está autenticado
            // (Los contratos deberían ser accesibles para usuarios autenticados)
            // Solo verificamos que el usuario esté autenticado, no que sea exactamente el mismo
            // if (!authUsername.equals(username) || !authRole.equals(role)) {
            //     return ResponseEntity.status(403).body(Map.of(
            //         "success", false,
            //         "message", "Acceso no autorizado"
            //     ));
            // }

            // Buscar la versión
            Optional<VersionAPI> versionOpt = apiRepository.findVersionById(versionId);
            if (!versionOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Versión no encontrada"
                ));
            }

            VersionAPI version = versionOpt.get();
            log.info("📄 Cargando contrato para versión {} de API {}", 
                version.getNumeroVersion(), version.getApi().getNombreApi());

            // Intentar cargar el contrato desde GCS
            try {
                if (version.getContratoApiUrl() != null && !version.getContratoApiUrl().trim().isEmpty()) {
                    String contract = apiContractStorageService.getApiContract(version.getContratoApiUrl());
                    log.info("✅ Contrato cargado exitosamente desde GCS para versión {}", version.getNumeroVersion());
                    
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "contract", contract,
                        "version", version.getNumeroVersion()
                    ));
                } else {
                    // Generar contrato de fallback
                    String fallbackContract = createFallbackContract(
                        version.getApi().getNombreApi(), 
                        version.getNumeroVersion()
                    );
                    log.warn("⚠️ URL de contrato vacía para versión {}, usando contrato de fallback", 
                        version.getNumeroVersion());
                    
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "contract", fallbackContract,
                        "version", version.getNumeroVersion(),
                        "isFallback", true
                    ));
                }
            } catch (Exception e) {
                log.error("❌ Error cargando contrato desde GCS para versión {}: {}", 
                    version.getNumeroVersion(), e.getMessage());
                
                // Generar contrato de fallback en caso de error
                String fallbackContract = createFallbackContract(
                    version.getApi().getNombreApi(), 
                    version.getNumeroVersion()
                );
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "contract", fallbackContract,
                    "version", version.getNumeroVersion(),
                    "isFallback", true,
                    "error", "Error cargando contrato original: " + e.getMessage()
                ));
            }

        } catch (Exception e) {
            log.error("❌ Error obteniendo contrato de versión {}: {}", versionId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Solicitar revisión de API (DEV → QA)
     * Crea una solicitud de publicación y cambia el estado de la API a QA
     */
    @PostMapping("apis/api-{id}/request-review")
    @ResponseBody
    public ResponseEntity<?> requestReview(@PathVariable String role,
                                          @PathVariable String username,
                                          @PathVariable Long id) {
        try {
            log.info("📤 Solicitud de revisión recibida para API {} por usuario {}", id, username);
            
            // Validar autenticación
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Usuario no autenticado"
                ));
            }

            String authUsername = authentication.getName();
            String authRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .orElse("dev")
                    .toLowerCase();

            // Verificar autorización
            if (!authUsername.equals(username) || !authRole.equals(role)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Sin permisos"
                ));
            }

            // Buscar la API y obtener la última versión
            API api = apiService.buscarApiPorId(id);
            if (api.getVersiones() == null || api.getVersiones().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "La API no tiene versiones para solicitar revisión"
                ));
            }

            // Obtener la última versión (ordenar por fecha de lanzamiento)
            VersionAPI latestVersion = api.getVersiones().stream()
                .sorted((v1, v2) -> {
                    int versionComparison = compareVersions(v2.getNumeroVersion(), v1.getNumeroVersion());
                    if (versionComparison != 0) return versionComparison;
                    if (v2.getFechaLanzamiento() != null && v1.getFechaLanzamiento() != null) {
                        return v2.getFechaLanzamiento().compareTo(v1.getFechaLanzamiento());
                    }
                    return Long.compare(v2.getVersionId(), v1.getVersionId());
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se pudo determinar la última versión"));

            // Crear la solicitud de publicación
            solicitudPublicacionService.crearSolicitud(id, latestVersion.getVersionId(), authUsername);

            log.info("✅ Solicitud de revisión creada exitosamente para API {} versión {}", 
                id, latestVersion.getNumeroVersion());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Solicitud de revisión enviada exitosamente",
                "version", latestVersion.getNumeroVersion()
            ));

        } catch (IllegalStateException e) {
            log.warn("⚠️ Error de validación al solicitar revisión de API {}: {}", id, e.getMessage());
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ Error solicitando revisión de API {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    /**
     * Cancelar solicitud de publicación activa
     * Cancela la solicitud y regresa la API a estado BORRADOR
     */
    @PostMapping("apis/api-{id}/cancel-request")
    @ResponseBody
    public ResponseEntity<?> cancelRequest(@PathVariable String role,
                                          @PathVariable String username,
                                          @PathVariable Long id) {
        try {
            log.info("🚫 Solicitud de cancelación recibida para API {} por usuario {}", id, username);
            
            // Validar autenticación
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Usuario no autenticado"
                ));
            }

            String authUsername = authentication.getName();
            String authRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .orElse("dev")
                    .toLowerCase();

            // Verificar autorización
            if (!authUsername.equals(username) || !authRole.equals(role)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Sin permisos"
                ));
            }

            // Buscar la solicitud activa para esta API
            var solicitudOpt = solicitudPublicacionService.buscarSolicitudActivaPorApi(id);
            if (solicitudOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "No hay solicitud activa para cancelar"
                ));
            }

            // Cancelar la solicitud
            solicitudPublicacionService.cancelarSolicitud(
                solicitudOpt.get().getSolicitudPublicacionId(), 
                authUsername
            );

            log.info("✅ Solicitud de publicación cancelada exitosamente para API {}", id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Solicitud de publicación cancelada exitosamente"
            ));

        } catch (IllegalStateException e) {
            log.warn("⚠️ Error de validación al cancelar solicitud de API {}: {}", id, e.getMessage());
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ Error cancelando solicitud de API {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    /**
     * Verificar si la API puede ser editada (AJAX)
     * Retorna true solo si está en BORRADOR y no tiene solicitud activa
     */
    @GetMapping("apis/api-{id}/can-edit")
    @ResponseBody
    public ResponseEntity<?> canEditApi(@PathVariable String role,
                                       @PathVariable String username,
                                       @PathVariable Long id) {
        try {
            // Validar autenticación
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "canEdit", false,
                    "message", "Usuario no autenticado"
                ));
            }

            String authUsername = authentication.getName();
            String authRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .orElse("dev")
                    .toLowerCase();

            // Verificar autorización
            if (!authUsername.equals(username) || !authRole.equals(role)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "canEdit", false,
                    "message", "Sin permisos"
                ));
            }

            // Verificar si puede editar
            boolean canEdit = solicitudPublicacionService.puedeEditarAPI(id);
            
            // Obtener información adicional
            API api = apiService.buscarApiPorId(id);
            String estado = api.getEstadoApi().toString();
            boolean tieneRequestActivo = solicitudPublicacionService.existeSolicitudActiva(id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "canEdit", canEdit,
                "estado", estado,
                "tieneRequestActivo", tieneRequestActivo,
                "message", canEdit ? "La API puede ser editada" : 
                          "La API no puede ser editada (estado: " + estado + ", solicitud activa: " + tieneRequestActivo + ")"
            ));

        } catch (Exception e) {
            log.error("❌ Error verificando si API {} puede ser editada: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "canEdit", false,
                "message", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint temporal para debugging de contratos
     */
    @GetMapping("/apis/debug/{apiId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugApiContract(@PathVariable Long apiId) {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            Optional<API> apiOpt = apiRepository.findById(apiId);
            if (!apiOpt.isPresent()) {
                debug.put("error", "API no encontrada");
                return ResponseEntity.notFound().build();
            }
            
            API api = apiOpt.get();
            debug.put("apiId", apiId);
            debug.put("apiName", api.getNombreApi());
            
            // Obtener versiones
            List<VersionAPI> versions = api.getVersiones().stream()
                    .sorted((v1, v2) -> v2.getFechaLanzamiento().compareTo(v1.getFechaLanzamiento()))
                    .toList();
            
            debug.put("totalVersions", versions.size());
            
            if (!versions.isEmpty()) {
                VersionAPI latestVersion = versions.get(0);
                debug.put("latestVersion", latestVersion.getNumeroVersion());
                debug.put("contractUrl", latestVersion.getContratoApiUrl());
                debug.put("isUrlNull", latestVersion.getContratoApiUrl() == null);
                debug.put("isUrlEmpty", latestVersion.getContratoApiUrl() != null && latestVersion.getContratoApiUrl().isEmpty());
                
                // Intentar cargar el contrato
                String contractUrl = latestVersion.getContratoApiUrl();
                if (contractUrl != null && !contractUrl.isEmpty()) {
                    try {
                        String content = apiContractStorageService.getApiContract(contractUrl);
                        debug.put("contractLoadSuccess", true);
                        debug.put("contractLength", content.length());
                        debug.put("contractPreview", content.length() > 200 ? content.substring(0, 200) + "..." : content);
                    } catch (Exception e) {
                        debug.put("contractLoadSuccess", false);
                        debug.put("contractError", e.getMessage());
                        debug.put("contractErrorType", e.getClass().getSimpleName());
                    }
                } else {
                    debug.put("contractLoadSuccess", false);
                    debug.put("contractError", "URL de contrato es null o vacía");
                }
            } else {
                debug.put("error", "No hay versiones para esta API");
            }
            
            return ResponseEntity.ok(debug);
            
        } catch (Exception e) {
            debug.put("error", "Error inesperado: " + e.getMessage());
            return ResponseEntity.status(500).body(debug);
        }
    }

}