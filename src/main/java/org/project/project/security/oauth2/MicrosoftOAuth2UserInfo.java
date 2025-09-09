package org.project.project.security.oauth2;

import java.util.Map;

public class MicrosoftOAuth2UserInfo extends OAuth2UserInfo {

    public MicrosoftOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getName() {
        return (String) attributes.get("displayName");
    }

    @Override
    public String getEmail() {
        // Microsoft Graph API puede devolver el email en diferentes campos
        String email = (String) attributes.get("mail");
        if (email == null) {
            email = (String) attributes.get("userPrincipalName");
        }
        return email;
    }

    @Override
    public String getImageUrl() {
        // Microsoft Graph API necesita una llamada adicional para obtener la foto
        // Por ahora retornamos null
        return null;
    }
}
