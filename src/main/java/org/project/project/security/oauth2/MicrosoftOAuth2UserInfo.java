package org.project.project.security.oauth2;

import java.util.Map;

public class MicrosoftOAuth2UserInfo extends OAuth2UserInfo {

    public MicrosoftOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        System.out.println("🔍 [Microsoft] Buscando ID del usuario...");
        System.out.println("🔍 [Microsoft] Atributos disponibles: " + attributes.keySet());
        
        // Primero intentar con 'sub' (OIDC standard)
        String id = (String) attributes.get("sub");
        if (id != null && !id.isBlank()) {
            System.out.println("✅ [Microsoft] ID obtenido de 'sub': " + id);
            return id;
        }
        
        // Fallback a 'id' (Microsoft Graph endpoint)
        id = (String) attributes.get("id");
        if (id != null && !id.isBlank()) {
            System.out.println("✅ [Microsoft] ID obtenido de 'id': " + id);
            return id;
        }
        
        // Fallback final a 'oid'
        id = (String) attributes.get("oid");
        if (id != null && !id.isBlank()) {
            System.out.println("✅ [Microsoft] ID obtenido de 'oid': " + id);
            return id;
        }
        
        System.out.println("❌ [Microsoft] No se pudo obtener ID del usuario");
        return null;
    }

    @Override
    public String getName() {
        System.out.println("🔍 [Microsoft] Buscando nombre del usuario...");
        
        String name = (String) attributes.get("displayName");
        if (name != null && !name.isBlank()) {
            System.out.println("✅ [Microsoft] Nombre obtenido de 'displayName': " + name);
            return name;
        }
        
        name = (String) attributes.get("userPrincipalName");
        if (name != null && !name.isBlank()) {
            System.out.println("✅ [Microsoft] Nombre obtenido de 'userPrincipalName': " + name);
            return name;
        }
        
        // También probar con 'name'
        name = (String) attributes.get("name");
        if (name != null && !name.isBlank()) {
            System.out.println("✅ [Microsoft] Nombre obtenido de 'name': " + name);
            return name;
        }
        
        System.out.println("❌ [Microsoft] No se pudo obtener nombre del usuario");
        return null;
    }

    @Override
    public String getEmail() {
        System.out.println("🔍 [Microsoft] Buscando email del usuario...");
        System.out.println("🔍 [Microsoft] Todos los atributos: " + attributes);
        
        if (attributes == null) {
            System.out.println("❌ [Microsoft] Atributos son null");
            return null;
        }
        
        // Microsoft Graph API puede devolver el email en diferentes campos
        Object emailObj = attributes.get("mail");
        if (emailObj != null && !emailObj.toString().isBlank()) {
            System.out.println("✅ [Microsoft] Email obtenido de 'mail': " + emailObj.toString());
            return emailObj.toString();
        }
        
        emailObj = attributes.get("userPrincipalName");
        if (emailObj != null && !emailObj.toString().isBlank()) {
            System.out.println("✅ [Microsoft] Email obtenido de 'userPrincipalName': " + emailObj.toString());
            return emailObj.toString();
        }
        
        // También verificar el campo preferred_username
        emailObj = attributes.get("preferred_username");
        if (emailObj != null && !emailObj.toString().isBlank()) {
            System.out.println("✅ [Microsoft] Email obtenido de 'preferred_username': " + emailObj.toString());
            return emailObj.toString();
        }
        
        // También verificar el campo email
        emailObj = attributes.get("email");
        if (emailObj != null && !emailObj.toString().isBlank()) {
            System.out.println("✅ [Microsoft] Email obtenido de 'email': " + emailObj.toString());
            return emailObj.toString();
        }
        
        System.out.println("❌ [Microsoft] No se pudo obtener email del usuario");
        System.out.println("🔍 [Microsoft] Atributos disponibles: " + attributes.keySet());
        return null;
    }

    @Override
    public String getImageUrl() {
        // Microsoft Graph API necesita una llamada adicional para obtener la foto
        // Por ahora retornamos null
        return null;
    }
}