package org.project.project.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class NodoTagId implements java.io.Serializable {

    @Column(name = "nodo_id")
    private Long nodoId;

    @Column(name = "tag_id")
    private Long tagId;

    // Alias getters para repository methods en inglés
    public Long getNodeId() {
        return nodoId;
    }
    
    public Long getTagId() {
        return tagId;
    }
}