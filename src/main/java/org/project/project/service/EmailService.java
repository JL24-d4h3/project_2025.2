package org.project.project.service;

import jakarta.annotation.PostConstruct;
import org.project.project.model.entity.Token;
import org.project.project.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void verificarConexionSMTP() {
        try {
            System.out.println("🔧 Verificando configuración de correo...");
            System.out.println("📧 Mail sender configurado: " + (mailSender != null ? "SÍ" : "NO"));
            System.out.println("🌐 Base URL configurada: " + baseUrl);

            // Intentar obtener la sesión SMTP
            if (mailSender != null) {
                System.out.println("✅ JavaMailSender está disponible");
            }
        } catch (Exception e) {
            System.err.println("❌ Error en configuración de correo: " + e.getMessage());
        }
    }

    public void enviarTokenPorCorreo(Usuario usuario, Token token) {
        try {
            System.out.println("🚀 INICIANDO envío de token de verificación para usuario: " + usuario.getCorreo());

            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(usuario.getCorreo());
            mensaje.setSubject("Verificación de correo");
            mensaje.setText("Hola " + usuario.getNombreUsuario()
                    + ",\n\nHaz clic en el siguiente enlace para verificar tu cuenta:\n"
                    + baseUrl + "/verify?token=" + token.getValorToken()
                    + "\n\nEste enlace expira en 24 horas.");

            System.out.println("📧 Enviando token de verificación a: " + usuario.getCorreo());
            mailSender.send(mensaje);
            System.out.println("✅ Token de verificación enviado exitosamente");
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar token de verificación: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al enviar token de verificación por correo", e);
        }
    }

    public void enviarCorreoRestablecimientoContrasena(Usuario usuario, Token token) {
        try {
            System.out.println("🚀 INICIANDO envío de restablecimiento de contraseña para usuario: " + usuario.getCorreo());

            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(usuario.getCorreo());
            mensaje.setSubject("Establecer contraseña - TelDev Portal");
            mensaje.setText("Hola " + usuario.getNombreUsuario()
                    + ",\n\nHaz clic en el siguiente enlace para establecer tu contraseña:\n"
                    + baseUrl + "/reset-password?token=" + token.getValorToken()
                    + "\n\nEste enlace expira en 1 hora y solo puede usarse una vez.");

            System.out.println("📧 Enviando enlace de restablecimiento a: " + usuario.getCorreo());
            mailSender.send(mensaje);
            System.out.println("✅ Enlace de restablecimiento enviado exitosamente");
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar enlace de restablecimiento: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al enviar enlace de restablecimiento por correo", e);
        }
    }

    public void enviarCodigoRestablecimientoContrasena(Usuario usuario, Token token) {
        try {
            System.out.println("🚀 INICIANDO envío de código de restablecimiento para usuario: " + usuario.getCorreo());

            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(usuario.getCorreo());
            mensaje.setSubject("Código de verificación - TelDev Portal");

            String messageText = "Hola " + usuario.getNombreUsuario() + ",\n\n" +
                    "Has solicitado restablecer tu contraseña en TelDev Portal.\n\n" +
                    "Tu código de verificación es: " + token.getValorToken() + "\n\n" +
                    "Este código expira en 5 minutos.\n" +
                    "Por tu seguridad, no compartas este código con nadie.\n\n" +
                    "Si no solicitaste este cambio, ignora este correo.\n\n" +
                    "Saludos,\n" +
                    "Equipo TelDev Portal";

            mensaje.setText(messageText);

            System.out.println("📧 Enviando código de verificación a: " + usuario.getCorreo());
            mailSender.send(mensaje);
            System.out.println("✅ Código enviado exitosamente");
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar código de restablecimiento: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al enviar código de restablecimiento por correo", e);
        }
    }

    public void enviarCodigoVerificacion(Usuario usuario, Token token) {
        try {
            System.out.println("🚀 INICIANDO envío de código de verificación de registro para usuario: " + usuario.getCorreo());

            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(usuario.getCorreo());
            mensaje.setSubject("Código de verificación de registro - TelDev Portal");

            String messageText = "¡Bienvenido a TelDev Portal, " + usuario.getNombreUsuario() + "!\n\n" +
                    "Para completar tu registro y activar tu cuenta, ingresa el siguiente código de verificación:\n\n" +
                    "Tu código de verificación es: " + token.getValorToken() + "\n\n" +
                    "Este código expira en 5 minutos.\n" +
                    "Por tu seguridad, no compartas este código con nadie.\n\n" +
                    "Una vez verificado tu código, podrás acceder a todas las funcionalidades del portal.\n\n" +
                    "¡Gracias por unirte a nuestra comunidad de desarrolladores!\n\n" +
                    "Saludos,\n" +
                    "Equipo TelDev Portal";

            mensaje.setText(messageText);

            System.out.println("📧 Enviando código de verificación de registro a: " + usuario.getCorreo());
            mailSender.send(mensaje);
            System.out.println("✅ Código de verificación de registro enviado exitosamente");
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar código de verificación de registro: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al enviar código de verificación de registro por correo", e);
        }
    }

    public void enviarCorreoTokenSA(Usuario usuario, Token token) {
        try {
            System.out.println("🚀 INICIANDO envío de token SA para usuario: " + usuario.getCorreo());

            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(usuario.getCorreo());
            mensaje.setSubject("Token de Super Administrador - TelDev Portal");

            String messageText = "Hola " + usuario.getNombreUsuario() + ",\n\n" +
                    "Se ha solicitado acceso de Super Administrador para tu cuenta.\n\n" +
                    "Tu código de verificación es: " + token.getValorToken() + "\n\n" +
                    "Este código expira en 5 minutos.\n" +
                    "Por tu seguridad, no compartas este código con nadie.\n" +
                    "Si no fuiste tú quien solicitó este acceso, ignora este correo e informa al equipo de seguridad.\n\n" +
                    "Saludos,\n" +
                    "Sistema de Seguridad TelDev Portal";

            mensaje.setText(messageText);

            System.out.println("📧 Enviando token SA a: " + usuario.getCorreo());
            mailSender.send(mensaje);
            System.out.println("✅ Token SA enviado exitosamente");
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar token SA: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al enviar token SA por correo", e);
        }
    }

    public void enviarInvitacionUsuario(Usuario usuario, Token token) {
        try {
            System.out.println("🚀 INICIANDO envío de invitación para usuario: " + usuario.getCorreo());

            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(usuario.getCorreo());
            mensaje.setSubject("Invitación a TelDev Portal - Completa tu registro");

            // Obtener rol principal de forma segura (usuarios temporales pueden no tener roles asignados aún)
            String rolPrincipal = "Usuario";
            if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
                rolPrincipal = usuario.getRoles().stream()
                        .findFirst()
                        .map(r -> r.getNombreRol().getDescripcion())
                        .orElse("Usuario");
            }

            String messageText = "¡Hola!\n\n" +
                    "Has sido invitado a formar parte de TelDev Portal como " + rolPrincipal + ".\n\n" +
                    "Para completar tu registro y activar tu cuenta, haz clic en el siguiente enlace:\n\n" +
                    baseUrl + "/complete-profile?token=" + token.getValorToken() + "\n\n" +
                    "En el formulario deberás completar tu información personal:\n" +
                    "• Nombre de usuario único\n" +
                    "• Nombres y apellidos\n" +
                    "• DNI y dirección\n" +
                    "• Contraseña segura\n\n" +
                    "Tu correo electrónico y rol ya están preconfigurados.\n\n" +
                    "Este enlace expira en 7 días.\n" +
                    "Una vez completado el registro, recibirás un código de verificación para activar tu cuenta.\n\n" +
                    "¡Bienvenido al equipo de TelDev Portal!\n\n" +
                    "Saludos,\n" +
                    "Equipo TelDev Portal";

            mensaje.setText(messageText);

            System.out.println("📧 Enviando invitación de usuario a: " + usuario.getCorreo());
            System.out.println("🔗 Token: " + token.getValorToken());

            mailSender.send(mensaje);

            System.out.println("✅ Invitación enviada exitosamente");
        } catch (Exception e) {
            System.err.println("❌ ERROR al enviar invitación: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al enviar invitación por correo", e);
        }
    }

    /**
     * Enviar invitación a un proyecto específico
     */
    public void enviarInvitacionProyecto(Usuario usuario, String nombreProyecto, String tipoInvitacion, Token token) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════");
        System.out.println("║  � EmailService.enviarInvitacionProyecto() - INICIANDO");
        System.out.println("╚════════════════════════════════════════════════════════════════");
        
        try {
            System.out.println("📋 Validando parámetros...");
            System.out.println("   ✓ Usuario: " + (usuario != null ? usuario.getCorreo() : "NULL"));
            System.out.println("   ✓ Usuario.nombreUsuario: " + (usuario != null ? usuario.getNombreUsuario() : "NULL"));
            System.out.println("   ✓ nombreProyecto: " + (nombreProyecto != null ? nombreProyecto : "NULL"));
            System.out.println("   ✓ tipoInvitacion: " + (tipoInvitacion != null ? tipoInvitacion : "NULL"));
            System.out.println("   ✓ token: " + (token != null ? token.getValorToken() : "NULL"));
            System.out.println("   ✓ mailSender: " + (mailSender != null ? "CONFIGURADO" : "NULL"));
            System.out.println("   ✓ baseUrl: " + baseUrl);

            if (usuario == null || token == null) {
                throw new IllegalArgumentException("Usuario o token son null");
            }

            System.out.println("📧 Creando mensaje de correo...");
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(usuario.getCorreo());
            mensaje.setSubject("¡Has sido invitado al proyecto " + nombreProyecto + "!");

            String tipoTexto = tipoInvitacion.equals("ENTERPRISE") ? "Invitación Empresarial" : "Invitación Grupal";
            
            // Construir URL de aceptación segura (ruta pública sin autenticación)
            String aceptarUrl = baseUrl + "/invitations/accept?token=" + token.getValorToken();
            String rechazarUrl = baseUrl + "/invitations/decline?token=" + token.getValorToken();
            
            System.out.println("🔗 URLs generadas:");
            System.out.println("   - Aceptar: " + aceptarUrl);
            System.out.println("   - Rechazar: " + rechazarUrl);
            
            String messageText = "¡Hola " + usuario.getNombreUsuario() + "!\n\n" +
                    "Has sido invitado a colaborar en el proyecto: " + nombreProyecto + "\n" +
                    "Tipo de invitación: " + tipoTexto + "\n\n" +
                    "Para aceptar esta invitación, ingresa en el siguiente enlace:\n" +
                    aceptarUrl + "\n\n" +
                    "O recházala aquí:\n" +
                    rechazarUrl + "\n\n" +
                    "Si deseas rechazar esta invitación, simplemente ignora este correo.\n\n" +
                    "Detalles de la invitación:\n" +
                    "• Proyecto: " + nombreProyecto + "\n" +
                    "• Tipo: " + tipoTexto + "\n" +
                    "• Válido por: 7 días\n\n" +
                    "Una vez aceptada, tendrás acceso al proyecto y a toda su documentación.\n\n" +
                    "Si tienes preguntas, contacta al administrador del proyecto.\n\n" +
                    "Saludos,\n" +
                    "Equipo TelDev Portal";

            mensaje.setText(messageText);

            System.out.println("✉️  Mensaje preparado. Enviando vía mailSender.send()...");
            System.out.println("   - Destinatario: " + usuario.getCorreo());
            System.out.println("   - Asunto: ¡Has sido invitado al proyecto " + nombreProyecto + "!");
            System.out.println("   - Longitud del texto: " + messageText.length() + " caracteres");

            mailSender.send(mensaje);

            System.out.println("✅✅✅ CORREO ENVIADO EXITOSAMENTE ✅✅✅");
            System.out.println("╔════════════════════════════════════════════════════════════════");
            System.out.println("║  📧 EmailService.enviarInvitacionProyecto() - FIN EXITOSO");
            System.out.println("╚════════════════════════════════════════════════════════════════\n");
        } catch (Exception e) {
            System.err.println("\n❌❌❌ ERROR EN EmailService.enviarInvitacionProyecto() ❌❌❌");
            System.err.println("Tipo de error: " + e.getClass().getName());
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("════════════════════════════════════════════════════════════════\n");
            throw new RuntimeException("Error al enviar invitación de proyecto por correo: " + e.getMessage(), e);
        }
    }

    public void enviarInvitacionRepositorio(Usuario usuario, String nombreRepositorio, Token token) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════");
        System.out.println("║  📧 EmailService.enviarInvitacionRepositorio() - INICIANDO");
        System.out.println("╚════════════════════════════════════════════════════════════════");
        
        try {
            System.out.println("📋 Validando parámetros...");
            System.out.println("   ✓ Usuario: " + (usuario != null ? usuario.getCorreo() : "NULL"));
            System.out.println("   ✓ Usuario.nombreUsuario: " + (usuario != null ? usuario.getNombreUsuario() : "NULL"));
            System.out.println("   ✓ nombreRepositorio: " + (nombreRepositorio != null ? nombreRepositorio : "NULL"));
            System.out.println("   ✓ token: " + (token != null ? token.getValorToken() : "NULL"));
            System.out.println("   ✓ mailSender: " + (mailSender != null ? "CONFIGURADO" : "NULL"));
            System.out.println("   ✓ baseUrl: " + baseUrl);

            if (usuario == null || token == null) {
                throw new IllegalArgumentException("Usuario o token son null");
            }

            System.out.println("📧 Creando mensaje de correo...");
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(usuario.getCorreo());
            mensaje.setSubject("¡Has sido invitado al repositorio " + nombreRepositorio + "!");

            // Construir URL de aceptación segura (ruta pública sin autenticación)
            String aceptarUrl = baseUrl + "/invitations/repository/accept?token=" + token.getValorToken();
            String rechazarUrl = baseUrl + "/invitations/repository/decline?token=" + token.getValorToken();
            
            System.out.println("🔗 URLs generadas:");
            System.out.println("   - Aceptar: " + aceptarUrl);
            System.out.println("   - Rechazar: " + rechazarUrl);
            
            String messageText = "¡Hola " + usuario.getNombreUsuario() + "!\n\n" +
                    "Has sido invitado a colaborar en el repositorio: " + nombreRepositorio + "\n\n" +
                    "Para aceptar esta invitación, ingresa en el siguiente enlace:\n" +
                    aceptarUrl + "\n\n" +
                    "O recházala aquí:\n" +
                    rechazarUrl + "\n\n" +
                    "Si deseas rechazar esta invitación, simplemente ignora este correo.\n\n" +
                    "Detalles de la invitación:\n" +
                    "• Repositorio: " + nombreRepositorio + "\n" +
                    "• Válido por: 7 días\n\n" +
                    "Una vez aceptada, tendrás acceso al repositorio y podrás colaborar con el equipo.\n\n" +
                    "Si tienes preguntas, contacta al administrador del repositorio.\n\n" +
                    "Saludos,\n" +
                    "Equipo TelDev Portal";

            mensaje.setText(messageText);

            System.out.println("✉️  Mensaje preparado. Enviando vía mailSender.send()...");
            System.out.println("   - Destinatario: " + usuario.getCorreo());
            System.out.println("   - Asunto: ¡Has sido invitado al repositorio " + nombreRepositorio + "!");
            System.out.println("   - Longitud del texto: " + messageText.length() + " caracteres");

            mailSender.send(mensaje);

            System.out.println("✅✅✅ CORREO ENVIADO EXITOSAMENTE ✅✅✅");
            System.out.println("╔════════════════════════════════════════════════════════════════");
            System.out.println("║  📧 EmailService.enviarInvitacionRepositorio() - FIN EXITOSO");
            System.out.println("╚════════════════════════════════════════════════════════════════\n");
        } catch (Exception e) {
            System.err.println("\n❌❌❌ ERROR EN EmailService.enviarInvitacionRepositorio() ❌❌❌");
            System.err.println("Tipo de error: " + e.getClass().getName());
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("════════════════════════════════════════════════════════════════\n");
            throw new RuntimeException("Error al enviar invitación de repositorio por correo: " + e.getMessage(), e);
        }
    }
}
