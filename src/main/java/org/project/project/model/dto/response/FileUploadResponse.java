package org.project.project.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private Boolean success;
    private String message;
    private Long nodeId;
    private String fileName;
    private Long fileSize;
    private String gcsPath;
}
