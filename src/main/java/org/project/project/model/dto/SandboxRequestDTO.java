package org.project.project.model.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SandboxRequestDTO {
    private String method;
    private String url;
    private Map<String, String> headers;
    private Object body;
}

