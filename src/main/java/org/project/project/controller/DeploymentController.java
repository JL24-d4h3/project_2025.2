package org.project.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.project.exception.ResourceNotFoundException;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.VersionAPI;
import org.project.project.repository.VersionAPIRepository;
import org.project.project.service.CloudRunDeploymentService;
import org.project.project.service.CloudRunDeploymentService.DeploymentException;
import org.project.project.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 🚀 REST Controller para gestión de Deployments en Cloud Run.
 * 
 * <p>Endpoints REST para que usuarios PROVIDER desplieguen sus APIs en Cloud Run
 * desde imágenes Docker pre-construidas.
 * 
 * <p><strong>Endpoints disponibles:</strong>
 * <ul>
 *   <li>{@code POST   /api/deployments/deploy} - Desplegar versión de API</li>
 *   <li>{@code GET    /api/deployments/{versionId}/status} - Consultar estado deployment</li>
 *   <li>{@code PUT    /api/deployments/{versionId}/stop} - Detener servicio Cloud Run</li>
 *   <li>{@code PUT    /api/deployments/{versionId}/restart} - Reiniciar servicio Cloud Run</li>
 *   <li>{@code DELETE /api/deployments/{versionId}} - Eliminar deployment (futuro)</li>
 * </ul>
 * 
 * <p><strong>Seguridad:</strong>
 * <ul>
 *   <li>Solo usuarios con rol {@code PROVIDER} pueden desplegar APIs</li>
 *   <li>Autenticación requerida mediante Spring Security</li>
 *   <li>Validación de pertenencia: solo el propietario puede gestionar su API</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong>
 * <ul>
 *   <li>Versión debe existir en BD</li>
 *   <li>Versión debe estar en estado {@code BORRADOR} (para deploy)</li>
 *   <li>URL Docker debe ser válida: {@code gcr.io/PROJECT/image:tag}</li>
 *   <li>Usuario debe ser propietario de la API</li>
 * </ul>
 * 
 * @author Jesús León
 * @version 1.0
 * @since 2025-11-12
 * @see CloudRunDeploymentService
 */
@Slf4j
@RestController
@RequestMapping("/api/deployments")
@RequiredArgsConstructor
public class DeploymentController {

    private final CloudRunDeploymentService deploymentService;
    private final VersionAPIRepository versionAPIRepository;
    private final UserService userService;

    /**
     * 🚀 POST /api/deployments/deploy
     * 
     * <p>Despliega una versión de API en Cloud Run desde imagen Docker.
     * 
     * <p><strong>Request Body:</strong>
     * <pre>{@code
     * {
     *   "versionId": 123,
     *   "dockerImageUrl": "gcr.io/my-project/api-usuarios:2.0.0"
     * }
     * }</pre>
     * 
     * <p><strong>Response exitoso (200):</strong>
     * <pre>{@code
     * {
     *   "success": true,
     *   "message": "Deployment iniciado exitosamente",
     *   "data": {
     *     "versionId": 123,
     *     "deploymentStatus": "DEPLOYING",
     *     "cloudRunUrl": null,
     *     "deploymentId": "abc123",
     *     "timestamp": "2025-11-12T10:30:00"
     *   }
     * }
     * }</pre>
     * 
     * <p><strong>Response error (400/403/404/500):</strong>
     * <pre>{@code
     * {
     *   "success": false,
     *   "error": "Descripción del error",
     *   "timestamp": "2025-11-12T10:30:00"
     * }
     * }</pre>
     * 
     * @param request Objeto con {@code versionId} y {@code dockerImageUrl}
     * @param principal Usuario autenticado
     * @return ResponseEntity con resultado del deployment
     */
    @PostMapping("/deploy")
    @PreAuthorize("hasAuthority('PROVIDER')")
    public ResponseEntity<?> deployAPI(@Valid @RequestBody DeployRequest request, Principal principal) {
        log.info("🚀 [DEPLOYMENT] POST /api/deployments/deploy - versionId: {}, dockerImageUrl: {}", 
                 request.getVersionId(), request.getDockerImageUrl());

        try {
            // 1. Validar usuario autenticado
            if (principal == null) {
                log.warn("⚠️ [DEPLOYMENT] Usuario no autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    createErrorResponse("Usuario no autenticado")
                );
            }

            Usuario usuario = userService.obtenerUsuarioActualSinUsername(principal);
            log.info("👤 [DEPLOYMENT] Usuario autenticado: {} (ID: {})", usuario.getUsername(), usuario.getId());

            // 2. Validar que la versión existe y pertenece al usuario
            VersionAPI version = versionAPIRepository.findById(request.getVersionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Versión con ID " + request.getVersionId() + " no encontrada"
                ));

            if (!version.getApi().getCreadoPor().getId().equals(usuario.getId())) {
                log.warn("⚠️ [DEPLOYMENT] Usuario {} intentó desplegar versión {} que no le pertenece", 
                         usuario.getId(), version.getVersionId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    createErrorResponse("No tienes permisos para desplegar esta API")
                );
            }

            // 3. Llamar al servicio de deployment
            Map<String, Object> result = deploymentService.deploy(
                request.getVersionId(), 
                request.getDockerImageUrl()
            );

            log.info("✅ [DEPLOYMENT] Deployment iniciado exitosamente para versión {}", request.getVersionId());

            // 4. Construir respuesta exitosa
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Deployment iniciado exitosamente");
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            log.error("❌ [DEPLOYMENT] Versión no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                createErrorResponse(e.getMessage())
            );

        } catch (DeploymentException e) {
            log.error("❌ [DEPLOYMENT] Error de deployment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                createErrorResponse(e.getMessage())
            );

        } catch (Exception e) {
            log.error("❌ [DEPLOYMENT] Error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                createErrorResponse("Error interno del servidor: " + e.getMessage())
            );
        }
    }

    /**
     * 📊 GET /api/deployments/{versionId}/status
     * 
     * <p>Consulta el estado actual del deployment de una versión.
     * 
     * <p><strong>Response exitoso (200):</strong>
     * <pre>{@code
     * {
     *   "success": true,
     *   "data": {
     *     "deploymentStatus": "ACTIVE",
     *     "cloudRunUrl": "https://api-usuarios-v1-abc123.run.app",
     *     "deploymentId": "abc123",
     *     "deployedAt": "2025-11-12T10:05:00",
     *     "lastChecked": "2025-11-12T10:35:00"
     *   }
     * }
     * }</pre>
     * 
     * @param versionId ID de la versión
     * @param principal Usuario autenticado
     * @return ResponseEntity con estado del deployment
     */
    @GetMapping("/{versionId}/status")
    @PreAuthorize("hasAuthority('PROVIDER')")
    public ResponseEntity<?> checkStatus(@PathVariable Long versionId, Principal principal) {
        log.info("📊 [DEPLOYMENT] GET /api/deployments/{}/status", versionId);

        try {
            // 1. Validar usuario autenticado
            if (principal == null) {
                log.warn("⚠️ [DEPLOYMENT] Usuario no autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    createErrorResponse("Usuario no autenticado")
                );
            }

            Usuario usuario = userService.obtenerUsuarioActualSinUsername(principal);

            // 2. Validar que la versión existe y pertenece al usuario
            VersionAPI version = versionAPIRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Versión con ID " + versionId + " no encontrada"
                ));

            if (!version.getApi().getCreadoPor().getId().equals(usuario.getId())) {
                log.warn("⚠️ [DEPLOYMENT] Usuario {} intentó consultar versión {} que no le pertenece", 
                         usuario.getId(), versionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    createErrorResponse("No tienes permisos para consultar esta API")
                );
            }

            // 3. Llamar al servicio para obtener estado
            Map<String, Object> status = deploymentService.checkStatus(versionId);

            log.info("✅ [DEPLOYMENT] Estado consultado para versión {}: {}", 
                     versionId, status.get("deploymentStatus"));

            // 4. Construir respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", status);

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            log.error("❌ [DEPLOYMENT] Versión no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                createErrorResponse(e.getMessage())
            );

        } catch (Exception e) {
            log.error("❌ [DEPLOYMENT] Error al consultar estado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                createErrorResponse("Error interno del servidor: " + e.getMessage())
            );
        }
    }

    /**
     * 📋 GET /api/deployments/{versionId}/logs
     * 
     * <p>Obtiene los logs de deployment de una versión en estado ERROR.
     * 
     * <p><strong>Response exitoso (200):</strong>
     * <pre>{@code
     * {
     *   "success": true,
     *   "logs": "2025-11-12 10:30:00 INFO Starting deployment...\n2025-11-12 10:30:05 ERROR Failed to pull image\n...",
     *   "timestamp": "2025-11-12T10:35:00"
     * }
     * }</pre>
     * 
     * <p><strong>Response error (400/403/404/500):</strong>
     * <pre>{@code
     * {
     *   "success": false,
     *   "error": "Descripción del error",
     *   "timestamp": "2025-11-12T10:35:00"
     * }
     * }</pre>
     * 
     * @param versionId ID de la versión
     * @param principal Usuario autenticado
     * @return ResponseEntity con logs del deployment
     */
    @GetMapping("/{versionId}/logs")
    @PreAuthorize("hasAuthority('PROVIDER')")
    public ResponseEntity<?> getDeploymentLogs(@PathVariable Long versionId, Principal principal) {
        log.info("📋 [DEPLOYMENT] GET /api/deployments/{}/logs", versionId);

        try {
            // 1. Validar usuario autenticado
            if (principal == null) {
                log.warn("⚠️ [DEPLOYMENT] Usuario no autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    createErrorResponse("Usuario no autenticado")
                );
            }

            Usuario usuario = userService.obtenerUsuarioActualSinUsername(principal);

            // 2. Validar que la versión existe y pertenece al usuario
            VersionAPI version = versionAPIRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Versión con ID " + versionId + " no encontrada"
                ));

            if (!version.getApi().getCreadoPor().getId().equals(usuario.getId())) {
                log.warn("⚠️ [DEPLOYMENT] Usuario {} intentó obtener logs de versión {} que no le pertenece", 
                         usuario.getId(), versionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    createErrorResponse("No tienes permisos para ver los logs de esta API")
                );
            }

            // 3. Llamar al servicio para obtener logs
            String logs = deploymentService.getDeploymentLogs(versionId);

            log.info("✅ [DEPLOYMENT] Logs obtenidos para versión {}", versionId);

            // 4. Construir respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("logs", logs);
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            log.error("❌ [DEPLOYMENT] Versión no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                createErrorResponse(e.getMessage())
            );

        } catch (DeploymentException e) {
            log.error("❌ [DEPLOYMENT] Error al obtener logs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                createErrorResponse(e.getMessage())
            );

        } catch (Exception e) {
            log.error("❌ [DEPLOYMENT] Error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                createErrorResponse("Error interno del servidor: " + e.getMessage())
            );
        }
    }

    /**
     * ⏹️ PUT /api/deployments/{versionId}/stop
     * 
     * <p>Detiene el servicio Cloud Run de una versión desplegada.
     * 
     * <p><strong>Prerequisitos:</strong>
     * <ul>
     *   <li>La versión debe estar en estado {@code ACTIVE}</li>
     * </ul>
     * 
     * <p><strong>Response exitoso (200):</strong>
     * <pre>{@code
     * {
     *   "success": true,
     *   "message": "Servicio detenido exitosamente",
     *   "data": {
     *     "versionId": 123,
     *     "deploymentStatus": "INACTIVE",
     *     "stoppedAt": "2025-11-12T10:40:00"
     *   }
     * }
     * }</pre>
     * 
     * @param versionId ID de la versión
     * @param principal Usuario autenticado
     * @return ResponseEntity con resultado de la operación
     */
    @PutMapping("/{versionId}/stop")
    @PreAuthorize("hasAuthority('PROVIDER')")
    public ResponseEntity<?> stopDeployment(@PathVariable Long versionId, Principal principal) {
        log.info("⏹️ [DEPLOYMENT] PUT /api/deployments/{}/stop", versionId);

        try {
            // 1. Validar usuario autenticado
            if (principal == null) {
                log.warn("⚠️ [DEPLOYMENT] Usuario no autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    createErrorResponse("Usuario no autenticado")
                );
            }

            Usuario usuario = userService.obtenerUsuarioActualSinUsername(principal);

            // 2. Validar que la versión existe y pertenece al usuario
            VersionAPI version = versionAPIRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Versión con ID " + versionId + " no encontrada"
                ));

            if (!version.getApi().getCreadoPor().getId().equals(usuario.getId())) {
                log.warn("⚠️ [DEPLOYMENT] Usuario {} intentó detener versión {} que no le pertenece", 
                         usuario.getId(), versionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    createErrorResponse("No tienes permisos para detener esta API")
                );
            }

            // 3. Llamar al servicio para detener
            Map<String, Object> result = deploymentService.stop(versionId);

            log.info("✅ [DEPLOYMENT] Servicio detenido para versión {}", versionId);

            // 4. Construir respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Servicio detenido exitosamente");
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            log.error("❌ [DEPLOYMENT] Versión no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                createErrorResponse(e.getMessage())
            );

        } catch (DeploymentException e) {
            log.error("❌ [DEPLOYMENT] Error al detener deployment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                createErrorResponse(e.getMessage())
            );

        } catch (Exception e) {
            log.error("❌ [DEPLOYMENT] Error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                createErrorResponse("Error interno del servidor: " + e.getMessage())
            );
        }
    }

    /**
     * ▶️ PUT /api/deployments/{versionId}/restart
     * 
     * <p>Reinicia el servicio Cloud Run de una versión detenida.
     * 
     * <p><strong>Prerequisitos:</strong>
     * <ul>
     *   <li>La versión debe estar en estado {@code INACTIVE}</li>
     * </ul>
     * 
     * <p><strong>Response exitoso (200):</strong>
     * <pre>{@code
     * {
     *   "success": true,
     *   "message": "Servicio reiniciado exitosamente",
     *   "data": {
     *     "versionId": 123,
     *     "deploymentStatus": "ACTIVE",
     *     "restartedAt": "2025-11-12T10:45:00"
     *   }
     * }
     * }</pre>
     * 
     * @param versionId ID de la versión
     * @param principal Usuario autenticado
     * @return ResponseEntity con resultado de la operación
     */
    @PutMapping("/{versionId}/restart")
    @PreAuthorize("hasAuthority('PROVIDER')")
    public ResponseEntity<?> restartDeployment(@PathVariable Long versionId, Principal principal) {
        log.info("▶️ [DEPLOYMENT] PUT /api/deployments/{}/restart", versionId);

        try {
            // 1. Validar usuario autenticado
            if (principal == null) {
                log.warn("⚠️ [DEPLOYMENT] Usuario no autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    createErrorResponse("Usuario no autenticado")
                );
            }

            Usuario usuario = userService.obtenerUsuarioActualSinUsername(principal);

            // 2. Validar que la versión existe y pertenece al usuario
            VersionAPI version = versionAPIRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Versión con ID " + versionId + " no encontrada"
                ));

            if (!version.getApi().getCreadoPor().getId().equals(usuario.getId())) {
                log.warn("⚠️ [DEPLOYMENT] Usuario {} intentó reiniciar versión {} que no le pertenece", 
                         usuario.getId(), versionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    createErrorResponse("No tienes permisos para reiniciar esta API")
                );
            }

            // 3. Llamar al servicio para reiniciar
            Map<String, Object> result = deploymentService.restart(versionId);

            log.info("✅ [DEPLOYMENT] Servicio reiniciado para versión {}", versionId);

            // 4. Construir respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Servicio reiniciado exitosamente");
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            log.error("❌ [DEPLOYMENT] Versión no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                createErrorResponse(e.getMessage())
            );

        } catch (DeploymentException e) {
            log.error("❌ [DEPLOYMENT] Error al reiniciar deployment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                createErrorResponse(e.getMessage())
            );

        } catch (Exception e) {
            log.error("❌ [DEPLOYMENT] Error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                createErrorResponse("Error interno del servidor: " + e.getMessage())
            );
        }
    }

    /**
     * 🗑️ DELETE /api/deployments/{versionId}
     * 
     * <p>Elimina un deployment de Cloud Run (FUTURO - no implementado aún).
     * 
     * <p><strong>Response futuro (200):</strong>
     * <pre>{@code
     * {
     *   "success": true,
     *   "message": "Deployment eliminado exitosamente",
     *   "data": {
     *     "versionId": 123,
     *     "deploymentStatus": null,
     *     "deletedAt": "2025-11-12T10:50:00"
     *   }
     * }
     * }</pre>
     * 
     * @param versionId ID de la versión
     * @param principal Usuario autenticado
     * @return ResponseEntity con resultado de la operación
     */
    @DeleteMapping("/{versionId}")
    @PreAuthorize("hasAuthority('PROVIDER')")
    public ResponseEntity<?> deleteDeployment(@PathVariable Long versionId, Principal principal) {
        log.info("🗑️ [DEPLOYMENT] DELETE /api/deployments/{} (NO IMPLEMENTADO)", versionId);

        // TODO: Implementar en futuro si es necesario
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
            createErrorResponse("Endpoint no implementado. Usa /stop en su lugar.")
        );
    }

    /**
     * Helper para crear respuestas de error consistentes.
     * 
     * @param errorMessage Mensaje de error
     * @return Map con estructura de error
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", errorMessage);
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }

    /**
     * DTO para request de deployment.
     */
    public static class DeployRequest {
        @NotNull(message = "versionId es requerido")
        private Long versionId;

        @NotBlank(message = "dockerImageUrl es requerida")
        private String dockerImageUrl;

        // Getters y Setters
        public Long getVersionId() {
            return versionId;
        }

        public void setVersionId(Long versionId) {
            this.versionId = versionId;
        }

        public String getDockerImageUrl() {
            return dockerImageUrl;
        }

        public void setDockerImageUrl(String dockerImageUrl) {
            this.dockerImageUrl = dockerImageUrl;
        }
    }
}
