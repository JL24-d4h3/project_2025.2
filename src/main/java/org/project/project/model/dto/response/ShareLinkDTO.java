package org.project.project.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareLinkDTO {
    private Long id;
    private Long nodeId;
    private String shareToken;
    private String shareUrl;
    private Boolean hasPassword;
    private LocalDateTime expiresAt;
    private Integer maxDownloads;
    private Integer currentDownloads;
    private Boolean allowDownload;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
