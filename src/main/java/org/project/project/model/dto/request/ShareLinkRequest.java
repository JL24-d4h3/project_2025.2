package org.project.project.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareLinkRequest {
    private Long nodeId;
    private String password;
    private LocalDateTime expiresAt;
    private Integer maxDownloads;
    private Boolean allowDownload;
}
