package org.project.project.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CopyNodesRequest {
    private List<Long> nodeIds;
    private Long targetNodeId;
}
