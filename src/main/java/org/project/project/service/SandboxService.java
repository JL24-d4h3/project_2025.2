package org.project.project.service;

import org.project.project.model.dto.SandboxRequestDTO;
import org.project.project.model.dto.SandboxResponseDTO;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class SandboxService {

    private static final String EXTERNAL_PROXY_URL = "https://api-proxy-backend-4epq24vhwa-uc.a.run.app/api/request";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SandboxService() {
        // Configurar RestTemplate con timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000); // 30 segundos para conectar
        factory.setReadTimeout(30000);    // 30 segundos para leer

        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public SandboxResponseDTO executeRequest(SandboxRequestDTO request) {
        SandboxResponseDTO response = new SandboxResponseDTO();
        SandboxResponseDTO.Metadata metadata = new SandboxResponseDTO.Metadata();

        long startTime = System.currentTimeMillis();

        try {
            // Construir el JSON para enviar al servicio externo
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("method", request.getMethod());
            requestBody.put("url", request.getUrl());

            if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
                requestBody.put("headers", request.getHeaders());
            }

            if (request.getBody() != null &&
                ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod()))) {
                requestBody.put("body", request.getBody());
            }

            // Configurar headers para la petición al servicio externo
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Enviar petición al servicio externo
            ResponseEntity<String> httpResponse = restTemplate.exchange(
                EXTERNAL_PROXY_URL,
                HttpMethod.POST,
                entity,
                String.class
            );

            long endTime = System.currentTimeMillis();
            metadata.setLatency(endTime - startTime);
            metadata.setTimestamp(LocalDateTime.now().toString());

            // Parsear la respuesta JSON del servicio externo
            Map<String, Object> externalResponse = objectMapper.readValue(
                httpResponse.getBody(),
                Map.class
            );

            // Extraer datos de la respuesta
            response.setSuccess((Boolean) externalResponse.getOrDefault("success", false));

            if (externalResponse.containsKey("response")) {
                Map<String, Object> responseData = (Map<String, Object>) externalResponse.get("response");

                SandboxResponseDTO.ResponseData data = new SandboxResponseDTO.ResponseData();
                data.setStatus((Integer) responseData.get("status"));
                data.setStatusText((String) responseData.get("statusText"));
                data.setHeaders((Map<String, String>) responseData.get("headers"));
                data.setData(responseData.get("data"));

                response.setResponse(data);
            }

            if (externalResponse.containsKey("error")) {
                response.setError((String) externalResponse.get("error"));
            }

            if (externalResponse.containsKey("metadata")) {
                Map<String, Object> externalMetadata = (Map<String, Object>) externalResponse.get("metadata");
                SandboxResponseDTO.Metadata responseMeta = new SandboxResponseDTO.Metadata();

                Object latencyObj = externalMetadata.get("latency");
                if (latencyObj instanceof Integer) {
                    responseMeta.setLatency(((Integer) latencyObj).longValue());
                } else if (latencyObj instanceof Long) {
                    responseMeta.setLatency((Long) latencyObj);
                }

                responseMeta.setTimestamp((String) externalMetadata.get("timestamp"));
                response.setMetadata(responseMeta);
            } else {
                response.setMetadata(metadata);
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Error HTTP del servicio externo
            long endTime = System.currentTimeMillis();
            metadata.setLatency(endTime - startTime);
            metadata.setTimestamp(LocalDateTime.now().toString());

            response.setSuccess(false);
            response.setError("Error al comunicarse con el servicio externo: " + e.getMessage());
            response.setMetadata(metadata);

        } catch (Exception e) {
            // Error general
            long endTime = System.currentTimeMillis();
            metadata.setLatency(endTime - startTime);
            metadata.setTimestamp(LocalDateTime.now().toString());

            response.setSuccess(false);
            response.setError("Error al ejecutar la petición: " + e.getMessage());
            response.setMetadata(metadata);
        }

        return response;
    }

    public String generateResponseHtml(SandboxResponseDTO response, long totalTime) {
        StringBuilder html = new StringBuilder();

        html.append("<div class='response-panel'>");
        html.append("<div class='response-header'>");

        // Status Badge
        if (response.isSuccess() && response.getResponse() != null) {
            html.append("<div class='status success'>")
                .append(response.getResponse().getStatus())
                .append(" ")
                .append(response.getResponse().getStatusText())
                .append("</div>");
        } else {
            html.append("<div class='status error'>");
            if (response.getError() != null) {
                html.append("Error");
            } else if (response.getResponse() != null) {
                html.append(response.getResponse().getStatus())
                    .append(" ")
                    .append(response.getResponse().getStatusText());
            } else {
                html.append("Error Desconocido");
            }
            html.append("</div>");
        }

        // Latency
        long backendLatency = response.getMetadata() != null ? response.getMetadata().getLatency() : 0;
        html.append("<div class='latency'>")
            .append("Latencia Backend: ").append(backendLatency).append("ms | ")
            .append("Total: ").append(totalTime).append("ms")
            .append("</div>");

        html.append("</div>"); // close response-header

        // Tabs para cambiar entre vistas
        html.append("<div class='response-tabs'>")
            .append("<button class='tab-btn active' onclick='switchTab(event, \"pretty\")'>Pretty</button>")
            .append("<button class='tab-btn' onclick='switchTab(event, \"raw\")'>Raw</button>")
            .append("</div>");

        // Response Data - Extraer datos
        Object dataToShow = response;

        if (response.isSuccess() && response.getResponse() != null && response.getResponse().getData() != null) {
            dataToShow = response.getResponse().getData();
        } else if (response.getError() != null) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", response.getError());
            dataToShow = errorMap;
        }

        try {
            String jsonResponse = objectMapper.writeValueAsString(dataToShow);

            // Vista Pretty (procesada)
            html.append("<div id='pretty' class='tab-content active'>")
                .append("<div class='pretty-view'>");

            html.append(generatePrettyView(dataToShow, 0));

            html.append("</div></div>");

            // Vista Raw (JSON crudo)
            html.append("<div id='raw' class='tab-content' style='display:none;'>")
                .append("<pre>").append(escapeHtml(jsonResponse)).append("</pre>")
                .append("</div>");

        } catch (Exception e) {
            html.append("<div class='tab-content active'>")
                .append("<pre>Error al formatear respuesta: ").append(escapeHtml(e.getMessage())).append("</pre>")
                .append("</div>");
        }

        html.append("</div>"); // close response-panel

        return html.toString();
    }

    /**
     * Genera una vista "Pretty" del JSON con formato tabular y colapsable
     */
    private String generatePrettyView(Object data, int level) {
        StringBuilder html = new StringBuilder();
        String indent = "  ".repeat(level);

        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            html.append("<div class='json-object' style='margin-left: ").append(level * 20).append("px;'>");

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object value = entry.getValue();

                html.append("<div class='json-entry'>");
                html.append("<span class='json-key'>").append(escapeHtml(key)).append(":</span> ");

                if (value instanceof Map || value instanceof java.util.List) {
                    html.append("<button class='collapse-btn' onclick='toggleCollapse(this)'>▼</button>");
                    html.append("<div class='json-value collapsible'>");
                    html.append(generatePrettyView(value, level + 1));
                    html.append("</div>");
                } else {
                    html.append("<span class='json-value-simple'>");
                    html.append(formatSimpleValue(value));
                    html.append("</span>");
                }

                html.append("</div>");
            }

            html.append("</div>");

        } else if (data instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) data;
            html.append("<div class='json-array' style='margin-left: ").append(level * 20).append("px;'>");

            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                html.append("<div class='json-array-item'>");
                html.append("<span class='json-key'>[").append(i).append("]:</span> ");

                if (item instanceof Map || item instanceof java.util.List) {
                    html.append("<button class='collapse-btn' onclick='toggleCollapse(this)'>▼</button>");
                    html.append("<div class='json-value collapsible'>");
                    html.append(generatePrettyView(item, level + 1));
                    html.append("</div>");
                } else {
                    html.append("<span class='json-value-simple'>");
                    html.append(formatSimpleValue(item));
                    html.append("</span>");
                }

                html.append("</div>");
            }

            html.append("</div>");
        } else {
            html.append(formatSimpleValue(data));
        }

        return html.toString();
    }

    /**
     * Formatea valores simples (strings, números, booleanos, null)
     */
    private String formatSimpleValue(Object value) {
        if (value == null) {
            return "<span class='json-null'>null</span>";
        } else if (value instanceof String) {
            return "<span class='json-string'>\"" + escapeHtml(String.valueOf(value)) + "\"</span>";
        } else if (value instanceof Number) {
            return "<span class='json-number'>" + value + "</span>";
        } else if (value instanceof Boolean) {
            return "<span class='json-boolean'>" + value + "</span>";
        } else {
            return "<span class='json-string'>\"" + escapeHtml(String.valueOf(value)) + "\"</span>";
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
}
