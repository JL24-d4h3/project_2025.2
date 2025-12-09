package org.project.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.project.model.entity.API;
import org.project.project.model.entity.VersionAPI;
import org.project.project.model.entity.Documentacion;
import org.project.project.service.APIService;
import org.project.project.service.UserService;
import org.project.project.model.entity.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API Controller for API management
 * Provides JSON endpoints for API operations (used by chatbot and external services)
 */
@Slf4j
@RestController
@RequestMapping("/api/apis")
@RequiredArgsConstructor
public class APIRestController {

    private final APIService apiService;
    private final UserService userService;

    /**
     * GET /api/apis
     * List all APIs accessible by current user
     */
    @GetMapping
    public ResponseEntity<?> listApis(Principal principal) {
        log.info("🔌 [API-REST] GET /api/apis");

        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("success", false, "error", "No autenticado")
                );
            }

            Usuario usuario = userService.obtenerUsuarioActualSinUsername(principal);

            // Get all APIs
            List<API> allApis = apiService.listarApis();

            // Filter: created by user OR public (PRODUCCION state)
            List<API> apisCreadas = allApis.stream()
                .filter(api -> api.getCreadoPor() != null &&
                              api.getCreadoPor().getUsuarioId().equals(usuario.getUsuarioId()))
                .collect(Collectors.toList());

            List<API> apisPublicas = allApis.stream()
                .filter(api -> api.getEstadoApi() == API.EstadoApi.PRODUCCION)
                .collect(Collectors.toList());

            // Combine and remove duplicates
            Set<Long> idsVistos = new HashSet<>();
            List<Map<String, Object>> todasApis = new ArrayList<>();

            // Add user's APIs
            apisCreadas.forEach(api -> {
                if (idsVistos.add(api.getApiId())) {
                    todasApis.add(mapApiToJson(api, true));
                }
            });

            // Add public APIs
            apisPublicas.forEach(api -> {
                if (idsVistos.add(api.getApiId())) {
                    todasApis.add(mapApiToJson(api, false));
                }
            });

            log.info("✅ APIs retrieved: {} total ({} created, {} public)",
                todasApis.size(), apisCreadas.size(), apisPublicas.size());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "totalApis", todasApis.size(),
                "apisCreadas", apisCreadas.size(),
                "apisPublicas", apisPublicas.size() - apisCreadas.size(),
                "apis", todasApis
            ));

        } catch (Exception e) {
            log.error("❌ Error listing APIs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "error", "Error al listar APIs",
                       "message", e.getMessage())
            );
        }
    }

    /**
     * GET /api/apis/{apiId}
     * Get complete details of a specific API
     */
    @GetMapping("/{apiId}")
    public ResponseEntity<?> getApiById(@PathVariable Long apiId, Principal principal) {
        log.info("🔌 [API-REST] GET /api/apis/{}", apiId);

        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("success", false, "error", "No autenticado")
                );
            }

            API api = apiService.buscarApiPorId(apiId);
            Usuario usuario = userService.obtenerUsuarioActualSinUsername(principal);

            boolean isCreator = api.getCreadoPor() != null &&
                                api.getCreadoPor().getUsuarioId().equals(usuario.getUsuarioId());

            Map<String, Object> apiDetail = mapApiToJsonComplete(api, isCreator);

            log.info("✅ API retrieved: {}", api.getNombreApi());

            return ResponseEntity.ok(Map.of("success", true, "api", apiDetail));

        } catch (Exception e) {
            log.error("❌ Error getting API {}: {}", apiId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "error", "Error al obtener API",
                       "message", e.getMessage())
            );
        }
    }

    /**
     * GET /api/apis/{apiId}/versions
     * Get all versions of an API
     */
    @GetMapping("/{apiId}/versions")
    public ResponseEntity<?> getVersions(@PathVariable Long apiId, Principal principal) {
        log.info("🔌 [API-REST] GET /api/apis/{}/versions", apiId);

        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("success", false, "error", "No autenticado")
                );
            }

            API api = apiService.buscarApiPorId(apiId);

            // Get versions from the API entity
            Set<VersionAPI> versionesSet = api.getVersiones();
            List<VersionAPI> versiones = versionesSet != null ?
                new ArrayList<>(versionesSet) : new ArrayList<>();

            List<Map<String, Object>> versionesData = versiones.stream()
                .map(this::mapVersionToJson)
                .collect(Collectors.toList());

            log.info("✅ Versions retrieved: {} versions for API '{}'",
                versiones.size(), api.getNombreApi());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "apiId", apiId,
                "nombreApi", api.getNombreApi(),
                "totalVersiones", versiones.size(),
                "versiones", versionesData
            ));

        } catch (Exception e) {
            log.error("❌ Error getting versions of API {}: {}", apiId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "error", "Error al obtener versiones",
                       "message", e.getMessage())
            );
        }
    }

    /**
     * GET /api/apis/{apiId}/versions/{versionId}/documentation
     * Get complete documentation of a specific API version
     */
    @GetMapping("/{apiId}/versions/{versionId}/documentation")
    public ResponseEntity<?> getDocumentation(
            @PathVariable Long apiId,
            @PathVariable Long versionId,
            Principal principal) {

        log.info("🔌 [API-REST] GET /api/apis/{}/versions/{}/documentation", apiId, versionId);

        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("success", false, "error", "No autenticado")
                );
            }

            API api = apiService.buscarApiPorId(apiId);

            // Find the version in the API's versions set
            VersionAPI version = api.getVersiones().stream()
                .filter(v -> v.getVersionId().equals(versionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Version not found with id: " + versionId));

            Documentacion documentacion = version.getDocumentacion();

            Map<String, Object> docData = new HashMap<>();
            docData.put("documentacionId", documentacion.getDocumentacionId());
            docData.put("seccionDocumentacion", documentacion.getSeccionDocumentacion());
            docData.put("apiId", apiId);
            docData.put("versionId", versionId);
            docData.put("numeroVersion", version.getNumeroVersion());
            docData.put("contratoApiUrl", version.getContratoApiUrl());

            log.info("✅ Documentation retrieved for version {}", version.getNumeroVersion());

            return ResponseEntity.ok(Map.of("success", true, "documentacion", docData));

        } catch (Exception e) {
            log.error("❌ Error getting documentation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "error", "Error al obtener documentación",
                       "message", e.getMessage())
            );
        }
    }

    /**
     * GET /api/apis/search
     * Search APIs by name or description
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchApis(
            @RequestParam String query,
            Principal principal) {

        log.info("🔌 [API-REST] GET /api/apis/search?query={}", query);

        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("success", false, "error", "No autenticado")
                );
            }

            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "error", "El parámetro 'query' es requerido")
                );
            }

            // Search in all APIs
            String queryLower = query.trim().toLowerCase();
            List<API> apis = apiService.listarApis().stream()
                .filter(api -> api.getNombreApi().toLowerCase().contains(queryLower) ||
                              (api.getDescripcionApi() != null &&
                               api.getDescripcionApi().toLowerCase().contains(queryLower)))
                .collect(Collectors.toList());

            List<Map<String, Object>> resultados = apis.stream()
                .map(api -> mapApiToJson(api, false))
                .collect(Collectors.toList());

            log.info("✅ Search completed: {} results", resultados.size());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "query", query,
                "totalResultados", resultados.size(),
                "apis", resultados
            ));

        } catch (Exception e) {
            log.error("❌ Error in search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "error", "Error en búsqueda",
                       "message", e.getMessage())
            );
        }
    }

    // =================== HELPER METHODS ===================

    /**
     * Map API entity to simple JSON Map
     */
    private Map<String, Object> mapApiToJson(API api, boolean isCreator) {
        Map<String, Object> map = new HashMap<>();
        map.put("apiId", api.getApiId());
        map.put("nombreApi", api.getNombreApi());
        map.put("descripcionApi", api.getDescripcionApi());
        map.put("estadoApi", api.getEstadoApi().toString());
        map.put("creadoEn", api.getCreadoEn());
        map.put("esCreador", isCreator);

        if (api.getCreadoPor() != null) {
            Usuario creador = api.getCreadoPor();
            String nombreCompleto = creador.getNombreUsuario() + " " +
                                   creador.getApellidoPaterno() + " " +
                                   creador.getApellidoMaterno();
            map.put("creadoPor", Map.of(
                "usuarioId", creador.getUsuarioId(),
                "username", creador.getUsername(),
                "nombreCompleto", nombreCompleto
            ));
        }

        return map;
    }

    /**
     * Map API entity to complete JSON Map (with versions)
     */
    private Map<String, Object> mapApiToJsonComplete(API api, boolean isCreator) {
        Map<String, Object> map = mapApiToJson(api, isCreator);

        // Add versions
        Set<VersionAPI> versionesSet = api.getVersiones();
        List<VersionAPI> versiones = versionesSet != null ?
            new ArrayList<>(versionesSet) : new ArrayList<>();

        map.put("totalVersiones", versiones.size());
        map.put("versiones", versiones.stream()
            .map(this::mapVersionToJson)
            .collect(Collectors.toList())
        );

        return map;
    }

    /**
     * Map VersionAPI entity to JSON Map
     */
    private Map<String, Object> mapVersionToJson(VersionAPI version) {
        Map<String, Object> map = new HashMap<>();
        map.put("versionId", version.getVersionId());
        map.put("numeroVersion", version.getNumeroVersion());
        map.put("descripcionVersion", version.getDescripcionVersion());
        map.put("contratoApiUrl", version.getContratoApiUrl());
        map.put("fechaLanzamiento", version.getFechaLanzamiento());
        map.put("creadoEn", version.getCreadoEn());

        if (version.getCreador() != null) {
            map.put("creadoPor", Map.of(
                "usuarioId", version.getCreador().getUsuarioId(),
                "username", version.getCreador().getUsername()
            ));
        }

        return map;
    }
}
