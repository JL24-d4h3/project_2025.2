package org.project.project.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "etiqueta_has_api")
public class EtiquetaHasApi {

    @EmbeddedId
    private EtiquetaHasApiId id;

    @MapsId("etiquetaId")
    @ManyToOne
    @JoinColumn(name = "etiqueta_tag_id", nullable = false)
    private Etiqueta etiqueta;

    @MapsId("apiId")
    @ManyToOne
    @JoinColumn(name = "api_api_id", nullable = false)
    private API api;

    public EtiquetaHasApi() {}

    public EtiquetaHasApi(Etiqueta etiqueta, API api) {
        this.etiqueta = etiqueta;
        this.api = api;
        this.id = new EtiquetaHasApiId(etiqueta.getTagId(), api.getApiId());
    }
    
    // Alias getters para repository methods en inglés
    public Etiqueta getTag() {
        return etiqueta;
    }
    
    public API getApi() {
        return api;
    }
}