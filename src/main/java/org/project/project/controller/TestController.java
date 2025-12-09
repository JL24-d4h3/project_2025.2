package org.project.project.controller;

import org.project.project.model.entity.Usuario;
import org.project.project.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public TestController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/test/password")
    public String testPassword(@RequestParam String username, @RequestParam String password) {
        System.out.println("🧪 [TestController] Verificando contraseña para: " + username);
        
        try {
            Usuario usuario = userService.buscarPorUsernameOEmail(username);
            if (usuario == null) {
                return "❌ Usuario no encontrado: " + username;
            }
            
            String storedHash = usuario.getHashedPassword();
            System.out.println("🔑 [TestController] Hash almacenado: " + storedHash);
            System.out.println("🔑 [TestController] Password ingresado: " + password);
            
            boolean matches = passwordEncoder.matches(password, storedHash);
            System.out.println("✅ [TestController] Password matches: " + matches);
            
            // Test directo con el hash conocido
            String knownHash = "$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6";
            boolean directMatch = passwordEncoder.matches("DevPortal123", knownHash);
            System.out.println("🔍 [TestController] Test directo 'DevPortal123' vs hash conocido: " + directMatch);
            
            return String.format(
                "Usuario: %s%n" +
                "Hash DB: %s%n" +
                "Password ingresado: %s%n" +
                "Matches: %s%n" +
                "Test directo DevPortal123: %s%n" +
                "Estado usuario: %s%n" +
                "Roles: %s",
                username, storedHash, password, matches, directMatch, 
                usuario.getEstadoUsuario(), usuario.getRoles()
            );
            
        } catch (Exception e) {
            System.err.println("❌ [TestController] Error: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}