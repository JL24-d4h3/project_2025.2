package org.project.project.controller;

import org.project.project.model.entity.API;
import org.project.project.model.entity.SolicitudPublicacionVersionApi;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.VersionAPI;
import org.project.project.service.SolicitudPublicacionService;
import org.project.project.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para gestionar el workflow de revisión QA de APIs.
 * 
 * Endpoints:
 * - POST /devportal/{role}/{username}/apis/api-{apiId}/v-{version}/request-review
 * - POST /devportal/{role}/{username}/apis/api-{apiId}/v-{version}/request-review/approve
 * - POST /devportal/{role}/{username}/apis/api-{apiId}/v-{version}/request-review/reject
 * - POST /devportal/{role}/{username}/apis/api-{apiId}/v-{version}/request-review/cancel
 * 
 * FASE 3.3 y FASE 3.4
 */
@RestController
@RequestMapping("/devportal/{role}/{username}/apis/api-{apiId}/v-{version}/request-review")
public class ApiReviewController {

    private static final Logger log = LoggerFactory.getLogger(ApiReviewController.class);

    @Autowired
    private SolicitudPublicacionService solicitudPublicacionService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * FASE 3.3: Endpoint para crear una solicitud de revisión QA
     * POST /devportal/{role}/{username}/apis/api-{apiId}/v-{version}/request-review
     * 
     * @param role Rol del usuario (dev, po, etc.)
     * @param username Username del usuario solicitante
     * @param apiId ID de la API
     * @param version Versión de la API (ej: "1.0.0")
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping
    public ResponseEntity<?> requestReview(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable Long apiId,
            @PathVariable String version) {
        
        try {
            log.info("📝 Solicitud de revisión QA recibida - API ID: {}, Versión: {}, Usuario: {}", 
                apiId, version, username);
            
            // Validar autenticación
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String authUsername = auth.getName();
            
            if (!username.equals(authUsername)) {
                log.warn("⚠️ Intento de solicitud no autorizado: {} intentó solicitar como {}", 
                    authUsername, username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "success", false,
                        "message", "No autorizado para realizar esta acción"
                    ));
            }
            
            // Obtener usuario
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(authUsername);
            if (usuarioOpt.isEmpty()) {
                log.error("❌ Usuario no encontrado: {}", authUsername);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "Usuario no encontrado"
                    ));
            }
            
            // Crear solicitud usando el servicio
            SolicitudPublicacionVersionApi solicitud = solicitudPublicacionService.crearSolicitud(
                apiId, 
                null, // versionId se obtendrá internamente por número de versión
                authUsername
            );
            
            log.info("✅ Solicitud de revisión creada exitosamente - ID: {}", 
                solicitud.getSolicitudPublicacionId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Solicitud de revisión creada exitosamente",
                "solicitudId", solicitud.getSolicitudPublicacionId(),
                "apiId", apiId,
                "version", version
            ));
            
        } catch (IllegalStateException e) {
            log.warn("⚠️ Error de validación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
                
        } catch (Exception e) {
            log.error("❌ Error al crear solicitud de revisión", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor: " + e.getMessage()
                ));
        }
    }

    /**
     * FASE 3.4: Endpoint para que QA apruebe una solicitud de revisión
     * POST /devportal/{role}/{username}/apis/api-{apiId}/v-{version}/request-review/approve
     * 
     * @param role Rol del usuario (debe ser 'qa')
     * @param username Username del usuario QA
     * @param apiId ID de la API
     * @param version Versión de la API
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/approve")
    public ResponseEntity<?> approveReview(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable Long apiId,
            @PathVariable String version) {
        
        try {
            log.info("✅ Solicitud de APROBACIÓN recibida - API ID: {}, Versión: {}, QA: {}", 
                apiId, version, username);
            
            // Validar autenticación y rol QA
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String authUsername = auth.getName();
            
            if (!username.equals(authUsername)) {
                log.warn("⚠️ Intento de aprobación no autorizado: {} intentó aprobar como {}", 
                    authUsername, username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "success", false,
                        "message", "No autorizado para realizar esta acción"
                    ));
            }
            
            // Validar que el usuario tenga rol QA
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(authUsername);
            if (usuarioOpt.isEmpty()) {
                log.error("❌ Usuario no encontrado: {}", authUsername);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "Usuario no encontrado"
                    ));
            }
            
            Usuario qaUser = usuarioOpt.get();
            boolean isQA = qaUser.getRoles().stream()
                .anyMatch(r -> "QA".equalsIgnoreCase(r.getNombreRolString()));
            
            if (!isQA) {
                log.warn("⚠️ Usuario {} no tiene rol QA", authUsername);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "success", false,
                        "message", "Solo usuarios con rol QA pueden aprobar solicitudes"
                    ));
            }
            
            // Buscar solicitud activa para esta API
            Optional<SolicitudPublicacionVersionApi> solicitudOpt = 
                solicitudPublicacionService.buscarSolicitudActivaPorApi(apiId);
            
            if (solicitudOpt.isEmpty()) {
                log.warn("⚠️ No se encontró solicitud activa para API ID: {}", apiId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "No se encontró una solicitud activa para esta API"
                    ));
            }
            
            Long solicitudId = solicitudOpt.get().getSolicitudPublicacionId();
            
            // Aprobar solicitud usando el servicio
            solicitudPublicacionService.aprobarSolicitud(solicitudId, authUsername);
            
            log.info("✅ Solicitud aprobada exitosamente - API ID: {}, Versión: {}", apiId, version);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Solicitud aprobada exitosamente. La API ahora está en PRODUCCION.",
                "apiId", apiId,
                "version", version
            ));
            
        } catch (IllegalStateException e) {
            log.warn("⚠️ Error de validación al aprobar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
                
        } catch (Exception e) {
            log.error("❌ Error al aprobar solicitud", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor: " + e.getMessage()
                ));
        }
    }

    /**
     * FASE 3.4: Endpoint para que QA rechace una solicitud de revisión
     * POST /devportal/{role}/{username}/apis/api-{apiId}/v-{version}/request-review/reject
     * 
     * @param role Rol del usuario (debe ser 'qa')
     * @param username Username del usuario QA
     * @param apiId ID de la API
     * @param version Versión de la API
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/reject")
    public ResponseEntity<?> rejectReview(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable Long apiId,
            @PathVariable String version) {
        
        try {
            log.info("❌ Solicitud de RECHAZO recibida - API ID: {}, Versión: {}, QA: {}", 
                apiId, version, username);
            
            // Validar autenticación y rol QA
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String authUsername = auth.getName();
            
            if (!username.equals(authUsername)) {
                log.warn("⚠️ Intento de rechazo no autorizado: {} intentó rechazar como {}", 
                    authUsername, username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "success", false,
                        "message", "No autorizado para realizar esta acción"
                    ));
            }
            
            // Validar que el usuario tenga rol QA
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(authUsername);
            if (usuarioOpt.isEmpty()) {
                log.error("❌ Usuario no encontrado: {}", authUsername);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "Usuario no encontrado"
                    ));
            }
            
            Usuario qaUser = usuarioOpt.get();
            boolean isQA = qaUser.getRoles().stream()
                .anyMatch(r -> "QA".equalsIgnoreCase(r.getNombreRolString()));
            
            if (!isQA) {
                log.warn("⚠️ Usuario {} no tiene rol QA", authUsername);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "success", false,
                        "message", "Solo usuarios con rol QA pueden rechazar solicitudes"
                    ));
            }
            
            // Buscar solicitud activa para esta API
            Optional<SolicitudPublicacionVersionApi> solicitudOpt = 
                solicitudPublicacionService.buscarSolicitudActivaPorApi(apiId);
            
            if (solicitudOpt.isEmpty()) {
                log.warn("⚠️ No se encontró solicitud activa para API ID: {}", apiId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "No se encontró una solicitud activa para esta API"
                    ));
            }
            
            Long solicitudId = solicitudOpt.get().getSolicitudPublicacionId();
            
            // Rechazar solicitud usando el servicio (nota: el servicio actual no acepta motivo)
            solicitudPublicacionService.rechazarSolicitud(solicitudId, authUsername);
            
            log.info("❌ Solicitud rechazada exitosamente - API ID: {}, Versión: {}", apiId, version);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Solicitud rechazada. La API vuelve a estado BORRADOR.",
                "apiId", apiId,
                "version", version
            ));
            
        } catch (IllegalStateException e) {
            log.warn("⚠️ Error de validación al rechazar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
                
        } catch (Exception e) {
            log.error("❌ Error al rechazar solicitud", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor: " + e.getMessage()
                ));
        }
    }

    /**
     * FASE 3.4: Endpoint para que el DEV/PROVIDER cancele su propia solicitud
     * POST /devportal/{role}/{username}/apis/api-{apiId}/v-{version}/request-review/cancel
     * 
     * @param role Rol del usuario (dev, po, provider)
     * @param username Username del usuario solicitante
     * @param apiId ID de la API
     * @param version Versión de la API
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelReview(
            @PathVariable String role,
            @PathVariable String username,
            @PathVariable Long apiId,
            @PathVariable String version) {
        
        try {
            log.info("⚠️ Solicitud de CANCELACIÓN recibida - API ID: {}, Versión: {}, Usuario: {}", 
                apiId, version, username);
            
            // Validar autenticación
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String authUsername = auth.getName();
            
            if (!username.equals(authUsername)) {
                log.warn("⚠️ Intento de cancelación no autorizado: {} intentó cancelar como {}", 
                    authUsername, username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "success", false,
                        "message", "No autorizado para realizar esta acción"
                    ));
            }
            
            // Buscar solicitud activa para esta API
            Optional<SolicitudPublicacionVersionApi> solicitudOpt = 
                solicitudPublicacionService.buscarSolicitudActivaPorApi(apiId);
            
            if (solicitudOpt.isEmpty()) {
                log.warn("⚠️ No se encontró solicitud activa para API ID: {}", apiId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "No se encontró una solicitud activa para esta API"
                    ));
            }
            
            Long solicitudId = solicitudOpt.get().getSolicitudPublicacionId();
            
            // Cancelar solicitud usando el servicio
            solicitudPublicacionService.cancelarSolicitud(solicitudId, authUsername);
            
            log.info("⚠️ Solicitud cancelada exitosamente - API ID: {}, Versión: {}", apiId, version);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Solicitud cancelada exitosamente. La API vuelve a estado BORRADOR.",
                "apiId", apiId,
                "version", version
            ));
            
        } catch (IllegalStateException e) {
            log.warn("⚠️ Error de validación al cancelar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
                
        } catch (Exception e) {
            log.error("❌ Error al cancelar solicitud", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor: " + e.getMessage()
                ));
        }
    }
}
