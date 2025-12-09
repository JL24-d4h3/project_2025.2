package org.project.project.service;

import lombok.extern.slf4j.Slf4j;
import org.project.project.exception.ResourceNotFoundException;
import org.project.project.model.entity.VersionAPI;
import org.project.project.repository.VersionAPIRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 🚀 Servicio para gestionar deployments de APIs en Cloud Run.
 * 
 * <p>Integración con microservicio externo del compañero para:
 * <ul>
 *   <li>Desplegar versiones de APIs desde imágenes Docker pre-construidas</li>
 *   <li>Consultar estado de deployments</li>
 *   <li>Detener servicios Cloud Run</li>
 *   <li>Reiniciar servicios Cloud Run</li>
 * </ul>
 * 
 * <p><strong>Flujo de Deployment:</strong>
 * <ol>
 *   <li>Usuario PROVIDER construye imagen Docker localmente</li>
 *   <li>Usuario sube imagen a GCR: {@code gcr.io/PROJECT/api:v2.0}</li>
 *   <li>Usuario ingresa URL de imagen en portal</li>
 *   <li>Portal llama {@link #deploy(Long, String)}</li>
 *   <li>Service actualiza BD: {@code deployment_status = DEPLOYING}</li>
 *   <li>Service llama microservicio: {@code POST /deployments}</li>
 *   <li>Microservicio despliega en Cloud Run</li>
 *   <li>Service actualiza BD: {@code deployment_status = ACTIVE}, guarda {@code cloud_run_url}</li>
 * </ol>
 * 
 * <p><strong>Estados de Deployment:</strong>
 * <ul>
 *   <li>{@code NULL} - No desplegado</li>
 *   <li>{@code PENDIENTE} - En cola de deployment</li>
 *   <li>{@code DEPLOYING} - Desplegando en Cloud Run</li>
 *   <li>{@code ACTIVE} - Desplegado y funcionando</li>
 *   <li>{@code ERROR} - Falló el deployment</li>
 *   <li>{@code INACTIVE} - Detenido manualmente</li>
 * </ul>
 * 
 * @author Jesús León
 * @version 1.0
 * @since 2025-11-12
 */
@Service
@Slf4j
public class CloudRunDeploymentService {

    @Autowired
    private VersionAPIRepository versionAPIRepository;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * URL base del microservicio de hosting (Cloud Run).
     * Microservicio construido por el compañero - Revision 00045-nhh.
     */
    private static final String MICROSERVICE_BASE_URL = "https://api-hosting-backend-532585334983.us-central1.run.app/api/v1";

    /**
     * JWT Token para autenticación con el microservicio.
     * Se obtiene de variables de entorno o application.properties.
     */
    @Value("${cloudrun.deployment.jwt-token:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsImVtYWlsIjoidGVzdEB0ZXN0LmNvbSIsInJvbGUiOiJhZG1pbiJ9.FAKE_TOKEN}")
    private String jwtToken;

    /**
     * 🚀 Despliega una versión de API en Cloud Run.
     * 
     * <p><strong>Prerequisitos:</strong>
     * <ul>
     *   <li>La versión debe existir en BD</li>
     *   <li>La versión debe estar en estado {@code BORRADOR}</li>
     *   <li>La imagen Docker debe existir en GCR</li>
     * </ul>
     * 
     * <p><strong>Proceso:</strong>
     * <ol>
     *   <li>Valida que versión existe y está en BORRADOR</li>
     *   <li>Valida formato de URL de imagen Docker</li>
     *   <li>Actualiza estado a {@code DEPLOYING}</li>
     *   <li>Llama al microservicio {@code POST /deployments}</li>
     *   <li>Si éxito: Actualiza BD con {@code cloud_run_url} y {@code ACTIVE}</li>
     *   <li>Si fallo: Actualiza estado a {@code ERROR}</li>
     * </ol>
     * 
     * @param versionId ID de la versión a desplegar
     * @param dockerImageUrl URL completa de la imagen Docker en GCR
     *                       Formato: {@code gcr.io/PROJECT_ID/api-name:tag}
     *                       Ejemplo: {@code gcr.io/dev-portal-123/user-api:v2.0}
     * @return Mapa con datos del deployment:
     *         <ul>
     *           <li>{@code deploymentId} - ID del deployment en el microservicio</li>
     *           <li>{@code cloudRunUrl} - URL del servicio desplegado</li>
     *           <li>{@code status} - Estado final (ACTIVE o ERROR)</li>
     *           <li>{@code message} - Mensaje descriptivo</li>
     *         </ul>
     * @throws ResourceNotFoundException Si la versión no existe
     * @throws IllegalStateException Si la versión no está en estado válido para deployment
     * @throws DeploymentException Si falla el deployment en Cloud Run
     */
    @Transactional
    public Map<String, Object> deploy(Long versionId, String dockerImageUrl) {
        log.info("🚀 Iniciando deployment de versión ID: {} con imagen: {}", versionId, dockerImageUrl);

        // 1️⃣ Validar que versión existe
        VersionAPI version = versionAPIRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Versión no encontrada con ID: " + versionId
                ));

        // 2️⃣ Validar que versión está en BORRADOR (editable)
        if (version.getEstadoVersion() != VersionAPI.EstadoVersion.DRAFT) {
            throw new IllegalStateException(
                    "Solo se pueden desplegar versiones en estado BORRADOR. " +
                    "Estado actual: " + version.getEstadoVersion()
            );
        }

        // 3️⃣ Validar formato de URL de imagen Docker
        if (!isValidDockerImageUrl(dockerImageUrl)) {
            throw new IllegalArgumentException(
                    "URL de imagen Docker inválida. " +
                    "Formato esperado: gcr.io/PROJECT_ID/imagen:tag"
            );
        }

        // 4️⃣ Actualizar estado a DEPLOYING
        version.setDeploymentStatus(VersionAPI.DeploymentStatus.DEPLOYING);
        version.setDockerImageUrl(dockerImageUrl);
        version.setFechaUltimoDeployment(LocalDateTime.now());
        versionAPIRepository.save(version);
        log.info("✅ Estado actualizado a DEPLOYING para versión ID: {}", versionId);

        try {
            // 5️⃣ Preparar request para microservicio
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("apiName", version.getApi().getNombreApi());
            requestBody.put("version", version.getNumeroVersion());
            requestBody.put("dockerImageUrl", dockerImageUrl);
            requestBody.put("requiresAuth", version.getRequiereAutenticacion());
            requestBody.put("environment", "production");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + jwtToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("📡 Llamando a microservicio: POST {}/deployments", MICROSERVICE_BASE_URL);
            log.debug("📦 Request body: {}", requestBody);

            // 6️⃣ Llamar al microservicio
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.exchange(
                    MICROSERVICE_BASE_URL + "/deployments",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK || 
                response.getStatusCode() == HttpStatus.CREATED) {
                
                Map<String, Object> responseBody = response.getBody();
                
                // 7️⃣ Extraer datos del response
                String cloudRunUrl = (String) responseBody.get("cloudRunUrl");
                Object deploymentIdObj = responseBody.get("deploymentId");
                Long deploymentId = null;
                
                if (deploymentIdObj instanceof Integer) {
                    deploymentId = ((Integer) deploymentIdObj).longValue();
                } else if (deploymentIdObj instanceof Long) {
                    deploymentId = (Long) deploymentIdObj;
                }

                // 8️⃣ Actualizar BD con datos del deployment exitoso
                version.setDeploymentStatus(VersionAPI.DeploymentStatus.ACTIVE);
                version.setCloudRunUrl(cloudRunUrl);
                version.setDeploymentId(deploymentId);
                version.setFechaUltimoDeployment(LocalDateTime.now());
                versionAPIRepository.save(version);

                log.info("✅ Deployment exitoso! Cloud Run URL: {}", cloudRunUrl);

                // 9️⃣ Preparar response
                Map<String, Object> result = new HashMap<>();
                result.put("deploymentId", deploymentId);
                result.put("cloudRunUrl", cloudRunUrl);
                result.put("status", "ACTIVE");
                result.put("message", "API desplegada exitosamente en Cloud Run");
                result.put("versionId", versionId);

                return result;

            } else {
                throw new DeploymentException(
                        "Respuesta inesperada del microservicio: " + response.getStatusCode()
                );
            }

        } catch (HttpClientErrorException e) {
            // Error 4xx (Bad Request, Unauthorized, etc.)
            log.error("❌ Error del cliente al desplegar: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            
            version.setDeploymentStatus(VersionAPI.DeploymentStatus.ERROR);
            versionAPIRepository.save(version);

            throw new DeploymentException(
                    "Error en la solicitud de deployment: " + e.getStatusCode() + 
                    " - " + e.getResponseBodyAsString()
            );

        } catch (HttpServerErrorException e) {
            // Error 5xx (Internal Server Error, Service Unavailable, etc.)
            log.error("❌ Error del servidor al desplegar: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            
            version.setDeploymentStatus(VersionAPI.DeploymentStatus.ERROR);
            versionAPIRepository.save(version);

            throw new DeploymentException(
                    "Error en el servidor de deployment: " + e.getStatusCode() + 
                    " - El microservicio no está disponible"
            );

        } catch (Exception e) {
            // Cualquier otro error
            log.error("❌ Error inesperado al desplegar versión ID: {}", versionId, e);
            
            version.setDeploymentStatus(VersionAPI.DeploymentStatus.ERROR);
            versionAPIRepository.save(version);

            throw new DeploymentException(
                    "Error inesperado durante el deployment: " + e.getMessage()
            );
        }
    }

    /**
     * 🔍 Consulta el estado actual de un deployment.
     * 
     * <p>Útil para:
     * <ul>
     *   <li>Polling del estado mientras se despliega</li>
     *   <li>Verificar si un servicio sigue activo</li>
     *   <li>Obtener métricas básicas del deployment</li>
     * </ul>
     * 
     * @param versionId ID de la versión cuyo deployment se quiere consultar
     * @return Mapa con información del deployment:
     *         <ul>
     *           <li>{@code deploymentStatus} - Estado actual (ACTIVE, ERROR, etc.)</li>
     *           <li>{@code cloudRunUrl} - URL del servicio</li>
     *           <li>{@code deploymentId} - ID del deployment</li>
     *           <li>{@code lastDeployment} - Fecha del último deployment</li>
     *         </ul>
     * @throws ResourceNotFoundException Si la versión no existe
     */
    @Transactional(readOnly = true)
    public Map<String, Object> checkStatus(Long versionId) {
        log.info("🔍 Consultando estado de deployment para versión ID: {}", versionId);

        VersionAPI version = versionAPIRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Versión no encontrada con ID: " + versionId
                ));

        Map<String, Object> status = new HashMap<>();
        status.put("versionId", versionId);
        status.put("deploymentStatus", version.getDeploymentStatus());
        status.put("cloudRunUrl", version.getCloudRunUrl());
        status.put("deploymentId", version.getDeploymentId());
        status.put("dockerImageUrl", version.getDockerImageUrl());
        status.put("lastDeployment", version.getFechaUltimoDeployment());

        log.info("✅ Estado: {} - URL: {}", version.getDeploymentStatus(), version.getCloudRunUrl());

        return status;
    }

    /**
     * 🛑 Detiene un servicio Cloud Run desplegado.
     * 
     * <p><strong>Notas:</strong>
     * <ul>
     *   <li>El servicio sigue existiendo en Cloud Run pero no recibe tráfico</li>
     *   <li>No se cobra por el servicio detenido</li>
     *   <li>Se puede reiniciar con {@link #restart(Long)}</li>
     * </ul>
     * 
     * @param versionId ID de la versión a detener
     * @return Mapa con resultado de la operación
     * @throws ResourceNotFoundException Si la versión no existe
     * @throws IllegalStateException Si la versión no está desplegada
     */
    @Transactional
    public Map<String, Object> stop(Long versionId) {
        log.info("🛑 Deteniendo deployment de versión ID: {}", versionId);

        VersionAPI version = versionAPIRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Versión no encontrada con ID: " + versionId
                ));

        if (version.getDeploymentStatus() != VersionAPI.DeploymentStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Solo se pueden detener versiones con estado ACTIVE. " +
                    "Estado actual: " + version.getDeploymentStatus()
            );
        }

        try {
            // Llamar al microservicio para detener el servicio
            String url = MICROSERVICE_BASE_URL + "/deployments/" + version.getDeploymentId() + "/stop";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + jwtToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            log.info("📡 Llamando a microservicio: PUT {}", url);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // Actualizar estado en BD
                version.setDeploymentStatus(VersionAPI.DeploymentStatus.INACTIVE);
                versionAPIRepository.save(version);

                log.info("✅ Servicio detenido exitosamente");

                Map<String, Object> result = new HashMap<>();
                result.put("versionId", versionId);
                result.put("status", "INACTIVE");
                result.put("message", "Servicio Cloud Run detenido exitosamente");

                return result;
            } else {
                throw new DeploymentException(
                        "Respuesta inesperada al detener servicio: " + response.getStatusCode()
                );
            }

        } catch (Exception e) {
            log.error("❌ Error al detener servicio de versión ID: {}", versionId, e);
            throw new DeploymentException("Error al detener servicio: " + e.getMessage());
        }
    }

    /**
     * 🔄 Reinicia un servicio Cloud Run previamente detenido.
     * 
     * @param versionId ID de la versión a reiniciar
     * @return Mapa con resultado de la operación
     * @throws ResourceNotFoundException Si la versión no existe
     * @throws IllegalStateException Si la versión no está detenida
     */
    @Transactional
    public Map<String, Object> restart(Long versionId) {
        log.info("🔄 Reiniciando deployment de versión ID: {}", versionId);

        VersionAPI version = versionAPIRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Versión no encontrada con ID: " + versionId
                ));

        if (version.getDeploymentStatus() != VersionAPI.DeploymentStatus.INACTIVE) {
            throw new IllegalStateException(
                    "Solo se pueden reiniciar versiones con estado INACTIVE. " +
                    "Estado actual: " + version.getDeploymentStatus()
            );
        }

        try {
            // Llamar al microservicio para reiniciar el servicio
            String url = MICROSERVICE_BASE_URL + "/deployments/" + version.getDeploymentId() + "/restart";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + jwtToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            log.info("📡 Llamando a microservicio: PUT {}", url);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // Actualizar estado en BD
                version.setDeploymentStatus(VersionAPI.DeploymentStatus.ACTIVE);
                versionAPIRepository.save(version);

                log.info("✅ Servicio reiniciado exitosamente");

                Map<String, Object> result = new HashMap<>();
                result.put("versionId", versionId);
                result.put("status", "ACTIVE");
                result.put("cloudRunUrl", version.getCloudRunUrl());
                result.put("message", "Servicio Cloud Run reiniciado exitosamente");

                return result;
            } else {
                throw new DeploymentException(
                        "Respuesta inesperada al reiniciar servicio: " + response.getStatusCode()
                );
            }

        } catch (Exception e) {
            log.error("❌ Error al reiniciar servicio de versión ID: {}", versionId, e);
            throw new DeploymentException("Error al reiniciar servicio: " + e.getMessage());
        }
    }

    /**
     * 📋 Obtiene los logs de deployment de una versión.
     * 
     * <p>Obtiene la información de error del deployment desde la base de datos.
     * Muestra información contextual del deployment con recomendaciones.
     * 
     * <p><strong>Información mostrada:</strong>
     * <ul>
     *   <li>Información de la versión (número, fecha lanzamiento)</li>
     *   <li>Estado del deployment</li>
     *   <li>Deployment ID si existe</li>
     *   <li>URL de Cloud Run si existe</li>
     *   <li>Sugerencias según el estado</li>
     * </ul>
     * 
     * @param versionId ID de la versión de la API
     * @return String formateado con información del deployment
     * @throws ResourceNotFoundException Si la versión no existe
     */
    @Transactional(readOnly = true)
    public String getDeploymentLogs(Long versionId) {
        log.info("📋 [SERVICE] Obteniendo información de deployment para versión ID: {}", versionId);

        // 1. Validar que la versión existe
        VersionAPI version = versionAPIRepository.findById(versionId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Versión con ID " + versionId + " no encontrada"
            ));

        // 2. Construir información detallada del deployment
        StringBuilder logsBuilder = new StringBuilder();
        
        // Header con información de la versión
        logsBuilder.append("=== INFORMACIÓN DEL DEPLOYMENT ===\n\n");
        logsBuilder.append("API: ").append(version.getApi().getNombreApi()).append("\n");
        logsBuilder.append("Versión: ").append(version.getNumeroVersion()).append("\n");
        logsBuilder.append("Estado: ").append(version.getDeploymentStatus() != null ? 
            version.getDeploymentStatus().name() : "N/A").append("\n");
        logsBuilder.append("Fecha Lanzamiento: ").append(version.getFechaLanzamiento() != null ? 
            version.getFechaLanzamiento().toString() : "N/A").append("\n");
        
        if (version.getFechaUltimoDeployment() != null) {
            logsBuilder.append("Último Deployment: ").append(version.getFechaUltimoDeployment().toString()).append("\n");
        }
        
        if (version.getDeploymentId() != null) {
            logsBuilder.append("Deployment ID: ").append(version.getDeploymentId()).append("\n");
        }
        
        if (version.getCloudRunUrl() != null && !version.getCloudRunUrl().trim().isEmpty()) {
            logsBuilder.append("Cloud Run URL: ").append(version.getCloudRunUrl()).append("\n");
        }
        
        logsBuilder.append("\n");
        
        // Información según el estado del deployment
        if (version.getDeploymentStatus() == VersionAPI.DeploymentStatus.ERROR) {
            logsBuilder.append("=== ESTADO: ERROR ===\n\n");
            logsBuilder.append("El deployment ha fallado. A continuación se muestran las causas más comunes y sus soluciones:\n\n");
            
            logsBuilder.append("=== CAUSAS COMUNES DE ERROR ===\n\n");
            
            logsBuilder.append("1. IMAGEN DOCKER NO ENCONTRADA\n");
            logsBuilder.append("   • La imagen no existe en Google Container Registry\n");
            logsBuilder.append("   • La URL de la imagen es incorrecta\n");
            logsBuilder.append("   • No tienes permisos para acceder a la imagen\n\n");
            
            logsBuilder.append("2. TIMEOUT EN EL DEPLOYMENT\n");
            logsBuilder.append("   • La aplicación no responde en el puerto correcto\n");
            logsBuilder.append("   • El contenedor tarda mucho en iniciar\n");
            logsBuilder.append("   • La aplicación no escucha en el puerto $PORT\n\n");
            
            logsBuilder.append("3. ERROR EN LA APLICACIÓN\n");
            logsBuilder.append("   • La aplicación crashea al iniciar\n");
            logsBuilder.append("   • Faltan variables de entorno necesarias\n");
            logsBuilder.append("   • Error en el health check\n\n");
            
            logsBuilder.append("4. PROBLEMAS DE PERMISOS\n");
            logsBuilder.append("   • Service Account sin permisos suficientes\n");
            logsBuilder.append("   • No tienes rol 'Cloud Run Admin' en GCP\n\n");
            
            logsBuilder.append("=== SOLUCIONES RECOMENDADAS ===\n\n");
            
            logsBuilder.append("1. VERIFICAR LA IMAGEN DOCKER\n");
            logsBuilder.append("   Comando: gcloud container images list\n");
            logsBuilder.append("   Verifica que la URL sea: gcr.io/PROJECT_ID/IMAGE:TAG\n\n");
            
            logsBuilder.append("2. RECONSTRUIR Y SUBIR LA IMAGEN\n");
            logsBuilder.append("   docker build -t gcr.io/PROJECT_ID/IMAGE:TAG .\n");
            logsBuilder.append("   docker push gcr.io/PROJECT_ID/IMAGE:TAG\n\n");
            
            logsBuilder.append("3. VERIFICAR EL PUERTO\n");
            logsBuilder.append("   Asegúrate que tu aplicación escuche en: process.env.PORT o System.getenv(\"PORT\")\n");
            logsBuilder.append("   Cloud Run asigna el puerto dinámicamente\n\n");
            
            logsBuilder.append("4. REVISAR LOGS EN GCP\n");
            logsBuilder.append("   Ve a: Cloud Run > [tu-servicio] > Logs\n");
            logsBuilder.append("   Busca errores en los logs de startup\n\n");
            
            logsBuilder.append("5. VERIFICAR PERMISOS\n");
            logsBuilder.append("   IAM > Service Accounts\n");
            logsBuilder.append("   Asegúrate de tener: Cloud Run Admin, Storage Admin\n\n");
            
            logsBuilder.append("6. INTENTAR DEPLOYMENT NUEVAMENTE\n");
            logsBuilder.append("   Después de corregir el problema, despliega nuevamente desde el portal\n");
            
            log.info("✅ Información de error generada para versión {}", versionId);
            
        } else if (version.getDeploymentId() == null) {
            logsBuilder.append("=== ESTADO: SIN DESPLEGAR ===\n\n");
            logsBuilder.append("Esta versión aún no ha sido desplegada.\n\n");
            
            logsBuilder.append("=== PASOS PARA DESPLEGAR ===\n\n");
            logsBuilder.append("1. Construye tu imagen Docker:\n");
            logsBuilder.append("   docker build -t gcr.io/PROJECT_ID/IMAGE:TAG .\n\n");
            
            logsBuilder.append("2. Sube la imagen a GCR:\n");
            logsBuilder.append("   docker push gcr.io/PROJECT_ID/IMAGE:TAG\n\n");
            
            logsBuilder.append("3. En el portal, haz clic en 'Deploy to Cloud Run'\n");
            logsBuilder.append("4. Ingresa la URL de tu imagen Docker\n");
            logsBuilder.append("5. Espera a que el deployment se complete\n");
            
            log.warn("⚠️ Versión {} no tiene deploymentId", versionId);
            
        } else {
            logsBuilder.append("=== ESTADO: ").append(version.getDeploymentStatus().name()).append(" ===\n\n");
            
            if (version.getDeploymentStatus() == VersionAPI.DeploymentStatus.DEPLOYING) {
                logsBuilder.append("El deployment está en progreso...\n");
                logsBuilder.append("Esto puede tomar varios minutos.\n\n");
                logsBuilder.append("La página se actualizará automáticamente cada 30 segundos.\n");
                logsBuilder.append("Si el proceso tarda más de 10 minutos, probablemente haya un error.\n");
                
            } else if (version.getDeploymentStatus() == VersionAPI.DeploymentStatus.ACTIVE) {
                logsBuilder.append("El deployment está activo y funcionando correctamente.\n\n");
                logsBuilder.append("URL del servicio: ").append(version.getCloudRunUrl() != null ? 
                    version.getCloudRunUrl() : "No disponible").append("\n\n");
                logsBuilder.append("Puedes probar tu API accediendo a la URL de arriba.\n");
                
            } else if (version.getDeploymentStatus() == VersionAPI.DeploymentStatus.INACTIVE) {
                logsBuilder.append("El servicio ha sido detenido manualmente.\n\n");
                logsBuilder.append("Para reactivarlo, usa el botón 'Restart' en el portal.\n");
                
            } else if (version.getDeploymentStatus() == VersionAPI.DeploymentStatus.PENDIENTE) {
                logsBuilder.append("El deployment está en cola esperando ser procesado.\n");
            }
            
            log.info("✅ Información de estado obtenida para versión {}", versionId);
        }
        
        return logsBuilder.toString();
    }

    /**
     * ✅ Valida formato de URL de imagen Docker en GCR.
     * 
     * <p><strong>Formatos válidos:</strong>
     * <ul>
     *   <li>{@code gcr.io/project-id/image-name:tag}</li>
     *   <li>{@code gcr.io/project-id/path/image-name:tag}</li>
     * </ul>
     * 
     * @param dockerImageUrl URL a validar
     * @return true si el formato es válido
     */
    private boolean isValidDockerImageUrl(String dockerImageUrl) {
        if (dockerImageUrl == null || dockerImageUrl.trim().isEmpty()) {
            return false;
        }

        // Validar que empiece con gcr.io
        if (!dockerImageUrl.startsWith("gcr.io/")) {
            return false;
        }

        // Validar que tenga al menos: gcr.io/project/image:tag
        String[] parts = dockerImageUrl.split("/");
        if (parts.length < 3) {
            return false;
        }

        // Validar que tenga un tag (contiene ':')
        String lastPart = parts[parts.length - 1];
        if (!lastPart.contains(":")) {
            return false;
        }

        return true;
    }

    /**
     * 🔧 Excepción personalizada para errores de deployment.
     */
    public static class DeploymentException extends RuntimeException {
        public DeploymentException(String message) {
            super(message);
        }

        public DeploymentException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
