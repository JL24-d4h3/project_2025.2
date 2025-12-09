package org.project.project.controller;

import lombok.extern.slf4j.Slf4j;
import org.project.project.model.dto.SeccionDocumentacionDTO;
import org.project.project.model.entity.API;
import org.project.project.model.entity.Documentacion;
import org.project.project.model.entity.VersionAPI;
import org.project.project.model.entity.Enlace;
import org.project.project.model.entity.Contenido;
import org.project.project.model.entity.Recurso;
import org.project.project.repository.APIRepository;
import org.project.project.repository.UsuarioRepository;
import org.project.project.repository.VersionAPIRepository;
import org.project.project.repository.EnlaceRepository;
import org.project.project.service.DocumentationService;
import org.project.project.service.ApiContractStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/devportal/{role}/{username:.+}")
public class DocumentationController {

    @Autowired
    private DocumentationService documentationService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private APIRepository apiRepository;
    
    @Autowired
    private VersionAPIRepository versionAPIRepository;
    
    @Autowired
    private EnlaceRepository enlaceRepository;
    
    @Autowired
    private ApiContractStorageService apiContractStorageService;

    @GetMapping
    public List<Documentacion> getAllDocumentations() {
        return documentationService.listarDocumentaciones();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Documentacion> getDocumentationById(@PathVariable Long id) {
        Documentacion documentacion = documentationService.buscarDocumentacionPorId(id);
        return ResponseEntity.ok(documentacion);
    }

    @PostMapping
    public Documentacion createDocumentation(@RequestBody Documentacion documentacion) {
        return documentationService.guardarDocumentacion(documentacion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Documentacion> updateDocumentation(@PathVariable Long id, @RequestBody Documentacion documentationDetails) {
        Documentacion updatedDocumentation = documentationService.actualizarDocumentacion(id, documentationDetails);
        return ResponseEntity.ok(updatedDocumentation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocumentation(@PathVariable Long id) {
        documentationService.eliminarDocumentacion(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Mostrar documentación de una versión específica de API (modo lectura)
     */
    @GetMapping("/catalog/api-{apiId}/v-{numeroVersion}")
    public String showApiDocumentation(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable Long apiId,
            @PathVariable String numeroVersion,
            Model model) {
        
        log.info("📖 Mostrando documentación: API={}, Número de Versión={}", apiId, numeroVersion);
        
        try {
            // Validar autenticación
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return "redirect:/signin";
            }

            String authUsername = authentication.getName();
            String authRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .orElse("dev")
                    .toLowerCase();

            // Validar que el usuario autenticado coincida con la URL
            if (!authUsername.equals(username) || !authRole.equals(role)) {
                return "redirect:/devportal/" + authRole + "/" + authUsername + "/catalog";
            }

            // 1. Cargar API
            Optional<API> apiOpt = apiRepository.findById(apiId);
            if (!apiOpt.isPresent()) {
                log.warn("❌ API con ID {} no encontrada", apiId);
                return "redirect:/devportal/" + role + "/" + username + "/catalog?error=api-not-found";
            }
            API api = apiOpt.get();

            // 2. Buscar versión específica por número de versión
            Optional<VersionAPI> versionOpt = api.getVersiones().stream()
                    .filter(v -> v.getNumeroVersion().equals(numeroVersion))
                    .findFirst();
            
            if (!versionOpt.isPresent()) {
                log.warn("❌ Versión {} no encontrada para API {}", numeroVersion, apiId);
                return "redirect:/devportal/" + role + "/" + username + "/catalog?error=version-not-found";
            }
            VersionAPI version = versionOpt.get();

            // 3. Cargar documentación
            Documentacion documentacion = version.getDocumentacion();
            if (documentacion == null) {
                log.warn("⚠️ No hay documentación asociada a la versión {}", numeroVersion);
            } else {
                log.info("📄 Documentación encontrada con ID: {}", documentacion.getDocumentacionId());
            }

            // 4. Cargar secciones CMS con estrategia híbrida (BD/GCS según tipo_enlace)
            List<SeccionDocumentacionDTO> seccionesCMS = new ArrayList<>();
            List<Enlace> enlacesExternos = new ArrayList<>();
            
            if (documentacion != null) {
                // 4.1 Cargar secciones con contenido Markdown listo (BD o GCS)
                seccionesCMS = documentationService.cargarSeccionesCMS(documentacion);
                log.info(" Secciones CMS cargadas desde servicio: {}", seccionesCMS.size());
                
                // 4.2 Buscar enlaces externos asociados a los contenidos de esta documentación
                Set<Contenido> contenidosSet = documentacion.getContenidos();
                if (contenidosSet != null) {
                    for (Contenido contenido : contenidosSet) {
                        // Buscar enlaces externos asociados a este contenido específico
                        List<Enlace> enlacesDelContenido = enlaceRepository.findByContextoTypeAndContextoId(
                                Enlace.ContextoType.CONTENIDO, 
                                contenido.getContenidoId()
                        );
                        
                        for (Enlace enlace : enlacesDelContenido) {
                            if (enlace.getTipoEnlace() == Enlace.TipoEnlace.ENLACE_EXTERNO) {
                                enlacesExternos.add(enlace);
                                log.info(" Enlace externo detectado: '{}' → {}", 
                                        enlace.getNombreArchivo(), 
                                        enlace.getDireccionAlmacenamiento());
                            }
                        }
                    }
                }
            } else {
                log.warn(" No hay documentación, por lo tanto no hay secciones CMS");
            }

            // 6. Obtener URL del contrato OpenAPI
            String contratoUrl = version.getContratoApiUrl();
            String contratoContent = null;
            try {
                if (contratoUrl != null && !contratoUrl.isEmpty()) {
                    contratoContent = apiContractStorageService.getApiContract(contratoUrl);
                }
            } catch (Exception e) {
                log.error("❌ Error cargando contrato OpenAPI: {}", e.getMessage());
            }

            // 7. Preparar datos para la vista
            // Obtener el usuario actual para el chatbot widget
            var currentUser = usuarioRepository.findByUsername(authUsername).orElse(null);
            model.addAttribute("Usuario", currentUser);
            model.addAttribute("userRole", role);
            model.addAttribute("username", username);
            model.addAttribute("rol", role);
            model.addAttribute("currentNavSection", "catalog");
            model.addAttribute("api", api);
            model.addAttribute("version", version);
            model.addAttribute("documentacion", documentacion);
            model.addAttribute("seccionesCMS", seccionesCMS);
            model.addAttribute("enlacesExternos", enlacesExternos);
            model.addAttribute("contratoContent", contratoContent);
            model.addAttribute("readOnly", true); // Modo lectura

            log.info("✅ Documentación cargada: {} secciones CMS, {} enlaces externos", 
                    seccionesCMS.size(), enlacesExternos.size());

            return "documentation/api-documentation-redesign";

        } catch (Exception e) {
            log.error("❌ Error cargando documentación: {}", e.getMessage(), e);
            return "redirect:/devportal/" + role + "/" + username + "/catalog?error=load-error";
        }
    }
}
