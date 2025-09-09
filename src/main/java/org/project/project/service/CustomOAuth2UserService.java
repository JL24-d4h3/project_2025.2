//package org.project.project.service;
//
//import org.project.project.model.entity.Rol;
//import org.project.project.model.entity.Usuario;
//import org.project.project.repository.RolRepository;
//import org.project.project.repository.UsuarioRepository;
//import org.project.project.security.oauth2.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.InternalAuthenticationServiceException;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.util.Map;
//
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.Optional;
//import java.util.Set;
//import java.util.UUID;
//
//@Service
//public class CustomOAuth2UserService extends DefaultOAuth2UserService {
//
//    @Autowired
//    private UsuarioRepository usuarioRepository;
//
//    @Autowired
//    private RolRepository rolRepository;
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        OAuth2User oauth2User = super.loadUser(userRequest);
//
//        try {
//            return processOAuth2User(userRequest, oauth2User);
//        } catch (AuthenticationException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
//        }
//    }
//
//    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
//        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
//
//        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
//            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
//        }
//
//        Optional<Usuario> userOptional = usuarioRepository.findByCorreo(oAuth2UserInfo.getEmail());
//        Usuario usuario;
//
//        if (userOptional.isPresent()) {
//            usuario = userOptional.get();
//            if (!usuario.getProveedor().equals(oAuth2UserRequest.getClientRegistration().getRegistrationId())) {
//                throw new OAuth2AuthenticationException(
//                    "Looks like you're signed up with " + usuario.getProveedor() +
//                    " account. Please use your " + usuario.getProveedor() + " account to login."
//                );
//            }
//            usuario = updateExistingUser(usuario, oAuth2UserInfo);
//        } else {
//            usuario = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
//        }
//
//        usuarioRepository.save(usuario);
//        return oAuth2User;
//    }
//
//    private Usuario registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
//        Usuario usuario = new Usuario();
//
//        usuario.setProveedor(oAuth2UserRequest.getClientRegistration().getRegistrationId());
//        usuario.setIdProveedor(oAuth2UserInfo.getId());
//        usuario.setNombreUsuario(oAuth2UserInfo.getName());
//        usuario.setCorreo(oAuth2UserInfo.getEmail());
//        usuario.setFotoPerfil(oAuth2UserInfo.getImageUrl());
//        usuario.setUsername(generateUniqueUsername(oAuth2UserInfo.getEmail()));
//        usuario.setFechaCreacion(LocalDateTime.now());
//        usuario.setUltimaConexion(LocalDateTime.now());
//        usuario.setEstadoUsuario(Usuario.EstadoUsuario.HABILITADO);
//        usuario.setActividadUsuario(Usuario.ActividadUsuario.ACTIVO);
//        usuario.setAccesoUsuario(Usuario.AccesoUsuario.SI);
//        usuario.setHashedPassword(UUID.randomUUID().toString());
//        usuario.setCodigoUsuario(generateUniqueUserCode());
//        usuario.setApellidoPaterno(""); // Placeholder
//        usuario.setApellidoMaterno(""); // Placeholder
//        usuario.setDireccionUsuario(""); // Placeholder
//        usuario.setDni("00000000"); // Placeholder
//
//        // Asignar rol por defecto
//        Rol rolDev = rolRepository.findByNombreRol("DEV")
//                .orElseThrow(() -> new RuntimeException("Error: Rol 'DEV' no encontrado."));
//        Set<Rol> roles = new HashSet<>();
//        roles.add(rolDev);
//        usuario.setRoles(roles);
//
//        return usuario;
//    }
//
//    private Usuario updateExistingUser(Usuario usuario, OAuth2UserInfo oAuth2UserInfo) {
//        usuario.setNombreUsuario(oAuth2UserInfo.getName());
//        usuario.setFotoPerfil(oAuth2UserInfo.getImageUrl());
//        usuario.setUltimaConexion(LocalDateTime.now());
//        return usuario;
//    }
//
//    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
//        switch(registrationId.toLowerCase()) {
//            case "google":
//                return new GoogleOAuth2UserInfo(attributes);
//            case "github":
//                return new GithubOAuth2UserInfo(attributes);
//            case "microsoft":
//                return new MicrosoftOAuth2UserInfo(attributes);
//            case "facebook":
//                return new FacebookOAuth2UserInfo(attributes);
//            default:
//                throw new OAuth2AuthenticationException("Sorry! Login with " + registrationId + " is not supported yet.");
//        }
//    }
//
//    private String generateUniqueUsername(String email) {
//        String baseUsername = email.split("@")[0];
//        String username = baseUsername;
//        int counter = 1;
//
//        while (usuarioRepository.existsByUsername(username)) {
//            username = baseUsername + counter++;
//        }
//
//        return username;
//    }
//
//    private String generateUniqueUserCode() {
//        String code;
//        do {
//            code = "USR" + String.format("%06d", (int)(Math.random() * 1000000));
//        } while (usuarioRepository.existsByCodigoUsuario(code));
//        return code;
//    }
//}
//
