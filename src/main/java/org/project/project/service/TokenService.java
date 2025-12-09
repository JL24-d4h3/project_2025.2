package org.project.project.service;

import org.project.project.model.entity.Token;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.TokenRepository;
import org.project.project.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenService {
    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    @Lazy
    private EmailService emailService;

    // Generar token para verificación de correo
    public Token generarToken(Usuario usuario) {
        Token token = new Token();
        token.setValorToken(UUID.randomUUID().toString());
        token.setEstadoToken(Token.EstadoToken.ACTIVO);
        token.setFechaCreacionToken(LocalDateTime.now());
        token.setFechaExpiracionToken(LocalDateTime.now().plusHours(24));
        token.setUsuario(usuario);

        return tokenRepository.save(token);
    }

    // Método combinado para generar token Y enviar correo (para romper dependencia circular)
    public Token generarTokenYEnviarCorreo(Usuario usuario) {
        Token token = generarToken(usuario);
        emailService.enviarTokenPorCorreo(usuario, token);
        return token;
    }

    public Token verificarToken(String valorToken) {
        System.out.println("LOG: 1. Iniciando verificación para el token: " + valorToken);

        try {
            Token token = tokenRepository.findByTokenValue(valorToken)
                    .orElseThrow(() -> new RuntimeException("Token no encontrado"));

            System.out.println("LOG: 2. Token encontrado. Estado: " + token.getEstadoToken());

            if (token.getEstadoToken() == Token.EstadoToken.REVOCADO ||
                    token.getFechaExpiracionToken().isBefore(LocalDateTime.now())) {

                System.out.println("LOG: 3. El token es inválido o expirado. No se puede continuar.");
                throw new RuntimeException("Token inválido o expirado");
            }

            Usuario usuario = token.getUsuario();
            System.out.println("LOG: 4. Usuario asociado al token: " + usuario.getCorreo());
            System.out.println("LOG: 5. Estado actual del usuario: " + usuario.getEstadoUsuario());

            usuario.setEstadoUsuario(Usuario.EstadoUsuario.HABILITADO);
            System.out.println("LOG: 6. Cambiando el estado del usuario a HABILITADO.");

            usuarioRepository.save(usuario);
            System.out.println("LOG: 7. Usuario guardado en la base de datos. El cambio debería ser visible ahora.");

            token.setEstadoToken(Token.EstadoToken.REVOCADO);
            tokenRepository.save(token);
            System.out.println("LOG: 8. Token revocado y guardado.");

            return token;

        } catch (RuntimeException e) {
            System.out.println("LOG: 9. Error durante la verificación: " + e.getMessage());
            throw e;
        }
    }

    // Generar token para recuperación de contraseña
    public Token generarTokenRecuperacionContrasena(Usuario usuario) {
        Token token = new Token();
        token.setValorToken(UUID.randomUUID().toString());
        token.setEstadoToken(Token.EstadoToken.ACTIVO);
        token.setFechaCreacionToken(LocalDateTime.now());
        token.setFechaExpiracionToken(LocalDateTime.now().plusHours(1));
        token.setUsuario(usuario);
        return tokenRepository.save(token);
    }

    // Generar código de verificación de 6 dígitos para recuperación de contraseña
    public Token generarCodigoRecuperacionContrasena(Usuario usuario) {
        // Revocar cualquier token anterior del usuario para evitar múltiples códigos activos
        revocarTokensRecuperacionContrasenaUsuario(usuario);

        Token token = new Token();

        // Generar código de 6 dígitos
        String code = generarCodigoSeisDigitos();
        token.setValorToken(code);

        token.setEstadoToken(Token.EstadoToken.ACTIVO);
        token.setFechaCreacionToken(LocalDateTime.now());
        // Código válido por 5 minutos
        token.setFechaExpiracionToken(LocalDateTime.now().plusMinutes(5));
        token.setUsuario(usuario);

        System.out.println("📧 Código de verificación generado: " + code + " para usuario: " + usuario.getCorreo());

        return tokenRepository.save(token);
    }

    // Generar código aleatorio de 6 dígitos
    private String generarCodigoSeisDigitos() {
        java.util.Random random = new java.util.Random();
        int code = 100000 + random.nextInt(900000); // Garantiza 6 dígitos
        return String.valueOf(code);
    }

    // Revocar todos los tokens de recuperación de contraseña activos de un usuario
    private void revocarTokensRecuperacionContrasenaUsuario(Usuario usuario) {
        java.util.List<Token> activeTokens = tokenRepository.findByUsuarioAndTokenStatus(
                usuario, Token.EstadoToken.ACTIVO);

        for (Token token : activeTokens) {
            // Solo revocar si no es muy viejo (última hora) y parece ser de reset
            if (token.getFechaCreacionToken().isAfter(LocalDateTime.now().minusHours(1)) &&
                    token.getValorToken().matches("\\d{6}")) {
                token.setEstadoToken(Token.EstadoToken.REVOCADO);
                tokenRepository.save(token);
                System.out.println("🔒 Token anterior revocado: " + token.getValorToken());
            }
        }
    }

    // Validar token de recuperación de contraseña
    public Token validarTokenRecuperacionContrasena(String valorToken) {
        Token token = tokenRepository.findByTokenValue(valorToken)
                .orElse(null);
        if (token == null || token.getEstadoToken() != Token.EstadoToken.ACTIVO
                || token.getFechaExpiracionToken().isBefore(LocalDateTime.now())) {
            return null;
        }
        return token;
    }



    // Validar código de verificación de 6 dígitos para recuperación de contraseña
    public Token validarCodigoRecuperacionContrasena(String code, Usuario usuario) {
        System.out.println("🔍 Validando código: " + code + " para usuario: " + usuario.getCorreo());

        Token token = tokenRepository.findByTokenValue(code)
                .orElse(null);

        if (token == null) {
            System.out.println("❌ Token no encontrado");
            return null;
        }

        if (token.getEstadoToken() != Token.EstadoToken.ACTIVO) {
            System.out.println("❌ Token no está activo: " + token.getEstadoToken());
            return null;
        }

        if (token.getFechaExpiracionToken().isBefore(LocalDateTime.now())) {
            System.out.println("❌ Token expirado");
            return null;
        }

        if (!token.getUsuario().getUsuarioId().equals(usuario.getUsuarioId())) {
            System.out.println("❌ Token no pertenece al usuario");
            return null;
        }

        System.out.println("✅ Código válido");
        return token;
    }

    // Revocar token tras uso
    public void revocarToken(Token token) {
        token.setEstadoToken(Token.EstadoToken.REVOCADO);
        tokenRepository.save(token);
    }

    // Generar token para verificación de Super Administrador
    public Token generarTokenSA(Usuario usuario) {
        // Revocar cualquier token SA anterior del usuario
        revocarTokensSA(usuario);

        Token token = new Token();

        // Generar código de 8 dígitos alfanumérico para SA
        String code = generarCodigoOchoCaracteres();
        token.setValorToken(code);

        token.setEstadoToken(Token.EstadoToken.ACTIVO);
        token.setFechaCreacionToken(LocalDateTime.now());
        token.setFechaExpiracionToken(LocalDateTime.now().plusMinutes(5));
        token.setUsuario(usuario);

        System.out.println("🔐 Token SA generado: " + code + " para usuario: " + usuario.getCorreo());

        return tokenRepository.save(token);
    }

    // Generar código alfanumérico de 8 caracteres para SA
    private String generarCodigoOchoCaracteres() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Sin caracteres confusos como 0, O, 1, I
        java.util.Random random = new java.util.Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    // Revocar todos los tokens SA activos de un usuario
    private void revocarTokensSA(Usuario usuario) {
        java.util.List<Token> activeTokens = tokenRepository.findByUsuarioAndTokenStatus(
                usuario, Token.EstadoToken.ACTIVO);

        for (Token token : activeTokens) {
            // Solo revocar tokens SA (últimos 10 minutos y 8 caracteres alfanuméricos)
            if (token.getFechaCreacionToken().isAfter(LocalDateTime.now().minusMinutes(15)) &&
                    token.getValorToken().matches("[A-Z0-9]{8}")) {
                token.setEstadoToken(Token.EstadoToken.REVOCADO);
                tokenRepository.save(token);
                System.out.println("🔒 Token SA anterior revocado: " + token.getValorToken());
            }
        }
    }

    // ==================== CÓDIGOS DE VERIFICACIÓN DE REGISTRO ====================

    // Generar código de verificación de 6 dígitos para registro
    public void generarCodigoVerificacion(Usuario usuario) {
        // Revocar códigos anteriores
        revocarCodigosVerificacion(usuario);

        // Generar código de 6 dígitos
        String codigo = String.format("%06d", new java.util.Random().nextInt(1000000));

        Token token = new Token();
        token.setValorToken(codigo);
        token.setEstadoToken(Token.EstadoToken.ACTIVO);
        token.setFechaCreacionToken(LocalDateTime.now());
        token.setFechaExpiracionToken(LocalDateTime.now().plusMinutes(5)); // 5 minutos como el resto
        token.setUsuario(usuario);

        tokenRepository.save(token);

        System.out.println("🔢 Código de verificación generado: " + codigo + " para usuario: " + usuario.getCorreo());

        // Enviar código por correo
        emailService.enviarCodigoVerificacion(usuario, token);
        System.out.println("📧 Código enviado exitosamente");
    }

    // Generar token de invitación para nuevos usuarios
    public void generarTokenInvitacion(Usuario usuario) {
        // Revocar tokens anteriores
        revocarTokensInvitacion(usuario);

        // Generar token UUID único
        String tokenValue = UUID.randomUUID().toString();

        Token token = new Token();
        token.setValorToken(tokenValue);
        token.setEstadoToken(Token.EstadoToken.ACTIVO);
        token.setFechaCreacionToken(LocalDateTime.now());
        token.setFechaExpiracionToken(LocalDateTime.now().plusDays(7)); // 7 días para completar invitación
        token.setUsuario(usuario);

        tokenRepository.save(token);

        System.out.println("🎟️ Token de invitación generado para usuario: " + usuario.getCorreo());

        // Enviar invitación por correo
        emailService.enviarInvitacionUsuario(usuario, token);
        System.out.println("📧 Invitación enviada exitosamente");
    }

    // Validar token de invitación
    public Token validarTokenInvitacion(String tokenValue) {
        try {
            Token token = tokenRepository.findByTokenValue(tokenValue)
                    .orElse(null);

            if (token == null) {
                System.out.println("❌ Token de invitación no encontrado");
                return null;
            }

            if (token.getEstadoToken() != Token.EstadoToken.ACTIVO) {
                System.out.println("❌ Token de invitación no está activo");
                return null;
            }

            if (token.getFechaExpiracionToken().isBefore(LocalDateTime.now())) {
                System.out.println("❌ Token de invitación ha expirado");
                return null;
            }

            System.out.println("✅ Token de invitación válido");
            return token;

        } catch (Exception e) {
            System.out.println("❌ Error validando token de invitación: " + e.getMessage());
            return null;
        }
    }

    // Validar código de verificación de registro
    public boolean validarCodigoVerificacion(String email, String codigo) {
        try {
            Usuario usuario = usuarioRepository.findByCorreo(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            System.out.println("🔍 Validando código: " + codigo + " para usuario: " + email);

            Token token = tokenRepository.findByTokenValue(codigo)
                    .orElse(null);

            if (token == null) {
                System.out.println("❌ Código no encontrado");
                return false;
            }

            if (token.getEstadoToken() != Token.EstadoToken.ACTIVO) {
                System.out.println("❌ Código no está activo");
                return false;
            }

            if (token.getFechaExpiracionToken().isBefore(LocalDateTime.now())) {
                System.out.println("❌ Código expirado");
                return false;
            }

            if (!token.getUsuario().getUsuarioId().equals(usuario.getUsuarioId())) {
                System.out.println("❌ Código no pertenece al usuario");
                return false;
            }

            // Revocar el código después de uso exitoso
            token.setEstadoToken(Token.EstadoToken.REVOCADO);
            tokenRepository.save(token);

            System.out.println("✅ Código válido");
            return true;

        } catch (Exception e) {
            System.out.println("❌ Error validando código: " + e.getMessage());
            return false;
        }
    }

    // Revocar códigos de verificación de registro activos
    private void revocarCodigosVerificacion(Usuario usuario) {
        java.util.List<Token> activeTokens = tokenRepository.findByUsuarioAndTokenStatus(
                usuario, Token.EstadoToken.ACTIVO);

        for (Token token : activeTokens) {
            // Solo revocar códigos de 6 dígitos (verificación de registro)
            if (token.getValorToken().matches("\\d{6}")) {
                token.setEstadoToken(Token.EstadoToken.REVOCADO);
                tokenRepository.save(token);
                System.out.println("🔒 Código de verificación anterior revocado: " + token.getValorToken());
            }
        }
    }

    // Validar token de verificación de Super Administrador
    public Token validarTokenSA(String valorToken, Usuario usuario) {
        Token token = tokenRepository.findByTokenValue(valorToken)
                .orElse(null);
        if (token == null || token.getEstadoToken() != Token.EstadoToken.ACTIVO
                || token.getFechaExpiracionToken().isBefore(LocalDateTime.now())
                || !token.getUsuario().getUsuarioId().equals(usuario.getUsuarioId())) {
            return null;
        }
        return token;
    }

    // Revocar tokens de invitación activos
    private void revocarTokensInvitacion(Usuario usuario) {
        java.util.List<Token> activeTokens = tokenRepository.findByUsuarioAndTokenStatus(
                usuario, Token.EstadoToken.ACTIVO);

        for (Token token : activeTokens) {
            // Solo revocar tokens UUID de invitación (36 caracteres con guiones)
            if (token.getValorToken().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                token.setEstadoToken(Token.EstadoToken.REVOCADO);
                tokenRepository.save(token);
                System.out.println("🔒 Token de invitación anterior revocado: " + token.getValorToken());
            }
        }
    }
}
