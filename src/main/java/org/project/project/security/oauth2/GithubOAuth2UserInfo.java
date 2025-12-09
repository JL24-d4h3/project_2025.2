package org.project.project.security.oauth2;

import java.util.Map;
import java.util.Objects;

public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        if (attributes == null) return null;
        Object idObj = attributes.get("id");
        if (idObj == null) return null;
        // Si es un Number (Integer, Long, ...), convertir de forma segura
        if (idObj instanceof Number) {
            return Long.toString(((Number) idObj).longValue());
        }
        // Fallback: usar toString()
        return idObj.toString();
    }

    @Override
    public String getName() {
        if (attributes == null) return null;
        Object nameObj = attributes.get("name");
        if (nameObj != null) return nameObj.toString();
        Object loginObj = attributes.get("login");
        return loginObj != null ? loginObj.toString() : null;
    }

    @Override
    public String getEmail() {
        if (attributes == null) return null;
        Object emailObj = attributes.get("email");
        if (emailObj != null && !Objects.toString(emailObj).isBlank()) {
            return emailObj.toString();
        }
        // GitHub puede no proporcionar email público
        // En lugar de generar un fallback, devolver null para indicar que no hay email
        System.out.println("⚠️ GitHub no proporcionó email público para el usuario");
        return null;
    }

    @Override
    public String getImageUrl() {
        if (attributes == null) return null;
        Object avatar = attributes.get("avatar_url");
        return avatar != null ? avatar.toString() : null;
    }
}
