package org.project.project.service;

import org.project.project.model.entity.Usuario;
import org.project.project.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("🔍 [CustomOidcUserService] === INICIANDO CARGA DE USUARIO OIDC ===");
        System.out.println("🔍 [CustomOidcUserService] Client Registration ID: " + userRequest.getClientRegistration().getRegistrationId());

        OidcUser oidcUser = super.loadUser(userRequest);

        System.out.println("🔍 [CustomOidcUserService] OIDC User obtenido exitosamente:");
        System.out.println("🔍 [CustomOidcUserService] Attributes: " + oidcUser.getAttributes());
        System.out.println("🔍 [CustomOidcUserService] Authorities: " + oidcUser.getAuthorities());
        System.out.println("🔍 [CustomOidcUserService] Name: " + oidcUser.getName());

        return processOidcUser(userRequest, oidcUser);
    }

    private OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) {
        System.out.println("🔄 [CustomOidcUserService] Procesando usuario OIDC...");

        String email = oidcUser.getAttribute("email");
        System.out.println("📧 [CustomOidcUserService] Email obtenido: " + email);

        if (email == null || email.trim().isEmpty()) {
            System.out.println("❌ [CustomOidcUserService] Email no encontrado en OIDC user");
            throw new OAuth2AuthenticationException("Email no encontrado en Google OIDC");
        }

        // Crear atributos modificados que incluyan el email
        Map<String, Object> attributes = new HashMap<>(oidcUser.getAttributes());

        Optional<Usuario> userOptional = usuarioRepository.findByCorreo(email);

        if (userOptional.isPresent()) {
            System.out.println("👤 [CustomOidcUserService] Usuario existente encontrado: " + email);
            Usuario usuario = userOptional.get();
            // Actualizar última conexión
            usuario.setUltimaConexion(LocalDateTime.now());
            usuarioRepository.save(usuario);
            System.out.println("📝 [CustomOidcUserService] Usuario existente actualizado");
        } else {
            System.out.println("🆕 [CustomOidcUserService] PRIMERA VEZ - Usuario nuevo: " + email);
            System.out.println("🚫 [CustomOidcUserService] NO creando usuario automáticamente - se manejará en handler");
            // 🔥 AGREGAMOS BANDERA DE PRIMERA VEZ
            attributes.put("first_login", "true");
            System.out.println("🏷️ [CustomOidcUserService] Bandera first_login=true agregada a attributes");
        }

        // ✅ ASEGURAR QUE EMAIL ESTÉ EN ATTRIBUTES
        attributes.put("email", email);
        System.out.println("📧 [CustomOidcUserService] Email agregado a attributes: " + email);

        // Verificar que el email esté en los atributos antes de crear el OIDC User
        System.out.println("🔍 [CustomOidcUserService] Email en attributes antes de crear DefaultOidcUser: " + attributes.get("email"));
        System.out.println("🔍 [CustomOidcUserService] Atributos completos antes de crear DefaultOidcUser: " + attributes.keySet());

        // Crear nuevo OidcUser con los atributos modificados
        // Necesitamos usar reflection o crear un wrapper ya que DefaultOidcUser no permite modificar atributos fácilmente
        // Por ahora, retornamos el OIDC user original, pero agregamos el email y first_login a sus atributos
        Map<String, Object> modifiedAttributes = new HashMap<>(oidcUser.getAttributes());
        modifiedAttributes.putAll(attributes); // Incluir email y first_login

        // Como DefaultOidcUser no permite modificar atributos fácilmente, creamos un wrapper
        DefaultOidcUser modifiedOidcUser = new DefaultOidcUser(
                oidcUser.getAuthorities(),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        ) {
            @Override
            public Map<String, Object> getAttributes() {
                return modifiedAttributes; // Devolver nuestros atributos modificados
            }
        };

        System.out.println("✅ [CustomOidcUserService] OIDC User modificado creado con email: " + modifiedOidcUser.getAttribute("email"));
        return modifiedOidcUser;
    }
}