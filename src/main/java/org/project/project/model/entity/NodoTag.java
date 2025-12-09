package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "nodo_tag")
public class NodoTag {

    @EmbeddedId
    private NodoTagId id;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("nodoId")
    @JoinColumn(name = "nodo_id")
    private Nodo nodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private NodoTagMaster tagMaster;

    // Constructors
    public NodoTag() {}

    public NodoTag(Nodo nodo, NodoTagMaster tagMaster) {
        this.nodo = nodo;
        this.tagMaster = tagMaster;
        this.id = new NodoTagId(nodo.getNodoId(), tagMaster.getTagId());
    }

    // Getters and Setters
    public NodoTagId getId() {
        return id;
    }

    public void setId(NodoTagId id) {
        this.id = id;
    }

    public Nodo getNodo() {
        return nodo;
    }

    public void setNodo(Nodo nodo) {
        this.nodo = nodo;
    }

    public NodoTagMaster getTagMaster() {
        return tagMaster;
    }

    public void setTagMaster(NodoTagMaster tagMaster) {
        this.tagMaster = tagMaster;
    }

    // Alias getters para repository methods en inglés
    public Nodo getNode() {
        return nodo;
    }
    
    public NodoTagMaster getTag() {
        return tagMaster;
    }

    @Override
    public String toString() {
        return "NodoTag{" +
                "nodoId=" + (id != null ? id.getNodoId() : null) +
                ", tagId=" + (id != null ? id.getTagId() : null) +
                '}';
    }
}