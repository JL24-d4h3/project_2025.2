package org.project.project.service;

import org.project.project.model.entity.Rol;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.UsuarioRepository;
import org.project.project.repository.RolRepository;
import org.project.project.security.oauth2.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.HashSet;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("🔍 [CustomOAuth2UserService] === INICIANDO CARGA DE USUARIO ===");
        System.out.println("🔍 [CustomOAuth2UserService] Client Registration ID: " + userRequest.getClientRegistration().getRegistrationId());
        System.out.println("🔍 [CustomOAuth2UserService] Access Token: " + userRequest.getAccessToken().getTokenValue().substring(0, Math.min(20, userRequest.getAccessToken().getTokenValue().length())) + "...");

        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);

            System.out.println("� [CustomOAuth2UserService] OAuth2User obtenido exitosamente:");
            System.out.println("🔍 [CustomOAuth2UserService] Attributes: " + oAuth2User.getAttributes());
            System.out.println("🔍 [CustomOAuth2UserService] Authorities: " + oAuth2User.getAuthorities());
            System.out.println("🔍 [CustomOAuth2UserService] Name: " + oAuth2User.getName());

            return this.processOAuth2User(userRequest, oAuth2User);
        } catch (OAuth2AuthenticationException e) {
            System.out.println("❌ [CustomOAuth2UserService] ERROR en super.loadUser():");
            System.out.println("❌ [CustomOAuth2UserService] Error Code: " + e.getError().getErrorCode());
            System.out.println("❌ [CustomOAuth2UserService] Error Description: " + e.getError().getDescription());
            System.out.println("❌ [CustomOAuth2UserService] Stack trace:");
            e.printStackTrace();
            throw e;
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String provider = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        System.out.println("🔄 Procesando usuario para proveedor: " + provider);

        try {
            // Para GitHub, necesitamos obtener el email de manera especial
            Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
            if ("github".equalsIgnoreCase(provider)) {
                String email = fetchGitHubEmail(oAuth2UserRequest);
                if (StringUtils.hasText(email)) {
                    attributes.put("email", email);
                    System.out.println("📧 Email obtenido de GitHub API: " + email);
                } else {
                    System.out.println("❌ No se pudo obtener email de GitHub");
                    throw new OAuth2AuthenticationException("No se pudo obtener email público de GitHub. Verifica que tengas un email público configurado en tu perfil de GitHub.");
                }
            }

            OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(provider, attributes);
            System.out.println("📧 Email extraído: " + oAuth2UserInfo.getEmail());
            System.out.println("👤 Nombre extraído: " + oAuth2UserInfo.getName());
            System.out.println("🆔 ID extraído: " + oAuth2UserInfo.getId());

            if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
                System.out.println("❌ Email no encontrado o vacío");
                throw new OAuth2AuthenticationException("Email no encontrado en la respuesta de " + provider);
            }

            Optional<Usuario> userOptional = usuarioRepository.findByCorreo(oAuth2UserInfo.getEmail());

            if (userOptional.isPresent()) {
                System.out.println("👤 Usuario existente encontrado: " + oAuth2UserInfo.getEmail());
                Usuario usuario = userOptional.get();
                usuario = updateExistingUser(usuario, oAuth2UserInfo);
                usuarioRepository.save(usuario);
                System.out.println("📝 Usuario existente actualizado");
            } else {
                System.out.println("🆕 PRIMERA VEZ - Usuario nuevo: " + oAuth2UserInfo.getEmail());
                System.out.println("🚫 NO creando usuario automáticamente - se manejará en handler");
                // 🔥 AGREGAMOS BANDERA DE PRIMERA VEZ
                attributes.put("first_login", "true");
                System.out.println("🏷️ Bandera first_login=true agregada a attributes");
            }

            // ✅ AGREGAR EL EMAIL A LOS ATRIBUTOS ANTES DE CREAR EL DefaultOAuth2User
            attributes.put("email", oAuth2UserInfo.getEmail());
            System.out.println("📧 Email agregado a attributes: " + oAuth2UserInfo.getEmail());

            // Crear nuevo OAuth2User con los atributos modificados que incluyen el email
            String userNameAttributeName = oAuth2UserRequest.getClientRegistration()
                    .getProviderDetails()
                    .getUserInfoEndpoint()
                    .getUserNameAttributeName();

            // Verificar que el email esté en los atributos antes de crear el OAuth2User
            System.out.println("🔍 Email en attributes antes de crear DefaultOAuth2User: " + attributes.get("email"));
            System.out.println("🔍 Atributos completos antes de crear DefaultOAuth2User: " + attributes.keySet());

            DefaultOAuth2User modifiedOAuth2User = new DefaultOAuth2User(
                    oAuth2User.getAuthorities(),
                    attributes, // Usar los atributos modificados que incluyen el email
                    userNameAttributeName
            );

            System.out.println("✅ OAuth2User modificado creado con email: " + modifiedOAuth2User.getAttribute("email"));
            return modifiedOAuth2User;

        } catch (Exception e) {
            System.out.println("❌ Error en processOAuth2User: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private String fetchGitHubEmail(OAuth2UserRequest oAuth2UserRequest) {
        System.out.println("🔍 Obteniendo emails de GitHub API...");
        try {
            String accessToken = oAuth2UserRequest.getAccessToken().getTokenValue();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("Accept", "application/vnd.github.v3+json");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            @SuppressWarnings("unchecked")
            ResponseEntity<List> response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> emails = response.getBody();
            System.out.println("📧 Emails recibidos de GitHub: " + emails);

            if (emails != null) {
                // Primary email primero
                for (Map<String, Object> emailInfo : emails) {
                    Boolean primary = (Boolean) emailInfo.get("primary");
                    Boolean verified = (Boolean) emailInfo.get("verified");
                    String email = (String) emailInfo.get("email");

                    if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                        System.out.println("✅ Email primario verificado encontrado: " + email);
                        return email;
                    }
                }

                // Si no hay primary, buscar cualquier verificado
                for (Map<String, Object> emailInfo : emails) {
                    Boolean verified = (Boolean) emailInfo.get("verified");
                    String email = (String) emailInfo.get("email");

                    if (Boolean.TRUE.equals(verified)) {
                        System.out.println("✅ Email verificado encontrado: " + email);
                        return email;
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error obteniendo emails de GitHub: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        System.out.println("🔍 Obteniendo OAuth2UserInfo para: " + registrationId);
        System.out.println("🔍 Atributos recibidos: " + attributes);

        OAuth2UserInfo userInfo = switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "github" -> new GithubOAuth2UserInfo(attributes);
            case "microsoft" -> new MicrosoftOAuth2UserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("Proveedor no soportado: " + registrationId);
        };

        System.out.println("✅ OAuth2UserInfo creado exitosamente");
        return userInfo;
    }

    private Usuario updateExistingUser(Usuario usuario, OAuth2UserInfo oAuth2UserInfo) {
        usuario.setNombreUsuario(oAuth2UserInfo.getName());
        usuario.setFotoPerfil(oAuth2UserInfo.getImageUrl());
        usuario.setUltimaConexion(LocalDateTime.now());
        return usuario;
    }

    private Usuario createNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(oAuth2UserInfo.getEmail());
        usuario.setNombreUsuario(oAuth2UserInfo.getName());
        usuario.setFotoPerfil(oAuth2UserInfo.getImageUrl());
        usuario.setProveedor(oAuth2UserRequest.getClientRegistration().getRegistrationId());
        usuario.setIdProveedor(oAuth2UserInfo.getId());
        usuario.setActividadUsuario(Usuario.ActividadUsuario.ACTIVO);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setUltimaConexion(LocalDateTime.now());
        usuario.setEstadoUsuario(Usuario.EstadoUsuario.HABILITADO);
        usuario.setAccesoUsuario(Usuario.AccesoUsuario.SI);
        usuario.setApellidoPaterno("N/A");
        usuario.setApellidoMaterno("N/A");
        // Generar DNI único basado en el ID del proveedor OAuth2 y timestamp
        String providerId = oAuth2UserInfo.getId();
        String uniqueDni = String.format("%08d", Math.abs((providerId + System.currentTimeMillis()).hashCode()) % 100000000);
        usuario.setDni(uniqueDni);
        usuario.setDireccionUsuario("N/A");
        usuario.setHashedPassword("OAUTH2_USER");
        String baseUsername = oAuth2UserInfo.getEmail().split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        usuario.setUsername(baseUsername);

        // Inicializar el Set de roles
        usuario.setRoles(new HashSet<>());

        Rol rolDev = rolRepository.findByRoleName(Rol.NombreRol.DEV)
                .orElseThrow(() -> new RuntimeException("Rol DEV no encontrado"));
        usuario.getRoles().add(rolDev);

        return usuario;
    }
}
