package org.project.project.model.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SandboxResponseDTO {
    private boolean success;
    private ResponseData response;
    private String error;
    private Metadata metadata;

    @Data
    public static class ResponseData {
        private int status;
        private String statusText;
        private Map<String, String> headers;
        private Object data;
    }

    @Data
    public static class Metadata {
        private long latency;
        private String timestamp;
    }
}

