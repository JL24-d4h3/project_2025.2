package org.project.project.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CustomMicrosoftOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("🔍 [CustomMicrosoftOAuth2UserService] === INICIANDO MICROSOFT USER LOAD ===");

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        System.out.println("🔍 [CustomMicrosoftOAuth2UserService] Registration ID: " + registrationId);

        if (!"microsoft".equals(registrationId)) {
            // Si no es Microsoft, usar el servicio por defecto
            return super.loadUser(userRequest);
        }

        try {
            // Hacer la llamada directa a Microsoft Graph API para debug
            String accessToken = userRequest.getAccessToken().getTokenValue();
            System.out.println("🔍 [CustomMicrosoftOAuth2UserService] Access Token disponible: " + (accessToken != null ? "SÍ" : "NO"));

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            System.out.println("🔍 [CustomMicrosoftOAuth2UserService] Llamando a Microsoft Graph API...");
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://graph.microsoft.com/v1.0/me",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            System.out.println("🔍 [CustomMicrosoftOAuth2UserService] Response Status: " + response.getStatusCode());
            System.out.println("🔍 [CustomMicrosoftOAuth2UserService] Response Headers: " + response.getHeaders());
            System.out.println("🔍 [CustomMicrosoftOAuth2UserService] Response Body: " + response.getBody());

            Map<String, Object> userAttributes = response.getBody();
            if (userAttributes != null) {
                System.out.println("🔍 [CustomMicrosoftOAuth2UserService] === ANÁLISIS DE ATRIBUTOS RECIBIDOS ===");
                for (Map.Entry<String, Object> entry : userAttributes.entrySet()) {
                    System.out.println("🔍   " + entry.getKey() + " = " + entry.getValue() + " (" + (entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null") + ")");
                }
            }

            // Ahora intentar el método por defecto para ver dónde falla
            System.out.println("🔍 [CustomMicrosoftOAuth2UserService] Intentando super.loadUser()...");
            return super.loadUser(userRequest);

        } catch (Exception e) {
            System.out.println("❌ [CustomMicrosoftOAuth2UserService] ERROR COMPLETO:");
            System.out.println("❌ [CustomMicrosoftOAuth2UserService] Exception Type: " + e.getClass().getName());
            System.out.println("❌ [CustomMicrosoftOAuth2UserService] Exception Message: " + e.getMessage());

            if (e instanceof OAuth2AuthenticationException) {
                OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) e;
                System.out.println("❌ [CustomMicrosoftOAuth2UserService] OAuth2 Error Code: " + oauth2Exception.getError().getErrorCode());
                System.out.println("❌ [CustomMicrosoftOAuth2UserService] OAuth2 Error Description: " + oauth2Exception.getError().getDescription());
            }

            e.printStackTrace();

            // Re-throw the exception
            if (e instanceof OAuth2AuthenticationException) {
                throw (OAuth2AuthenticationException) e;
            } else {
                throw new OAuth2AuthenticationException(new OAuth2Error("microsoft_user_load_error"), e.getMessage(), e);
            }
        }
    }
}