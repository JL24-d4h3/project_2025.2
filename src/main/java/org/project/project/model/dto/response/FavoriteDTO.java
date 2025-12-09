package org.project.project.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteDTO {
    private Long id;
    private Long nodeId;
    private Long userId;
    private String label;
    private LocalDateTime createdAt;
    
    // Campos adicionales del nodo
    private String nodeName;
    private String nodeType;
}
