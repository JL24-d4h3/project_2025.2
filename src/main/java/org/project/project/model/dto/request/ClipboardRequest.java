package org.project.project.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClipboardRequest {
    private List<Long> nodeIds;
    private String operation; // "COPY" o "CUT"
}
