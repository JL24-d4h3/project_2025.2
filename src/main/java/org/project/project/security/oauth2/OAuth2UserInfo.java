package org.project.project.security.oauth2;

import lombok.Getter;

import java.util.Map;

@Getter
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();

    // Se añadió un método setter para poder actualizar el email
    public void setEmail(String email) {
        this.attributes.put("email", email);
    }
}
