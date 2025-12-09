package org.project.project.controller;

import org.project.project.model.entity.Token;
import org.project.project.model.entity.Usuario;
import org.project.project.repository.TokenRepository;
import org.project.project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/devportal/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    public List<Usuario> getAllUsers() {
        return userService.listarUsuarios();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUserById(@PathVariable Long id) {
        Usuario usuario = userService.buscarUsuarioPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    public Usuario createUser(@RequestBody Usuario usuario) {
        return userService.guardarUsuario(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUser(@PathVariable Long id, @RequestBody Usuario userDetails) {
        Usuario updatedUser = userService.actualizarUsuario(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate-field")
    public ResponseEntity<Boolean> validateField(@RequestParam String field, @RequestParam String value) {
        boolean available = false;
        switch (field) {
            case "username":
                available = userService.isUsernameAvailable(value);
                break;
            case "dni":
                available = userService.isDniAvailable(value);
                break;
            case "email":
                available = userService.isEmailAvailable(value);
                break;
            default:
                return ResponseEntity.badRequest().body(false);
        }
        return ResponseEntity.ok(available);
    }
    
    @GetMapping("/check-exists")
    public ResponseEntity<java.util.Map<String, Boolean>> checkUserExists(@RequestParam String email) {
        logger.info("🔍 [UserController] Verificando existencia de usuario: {}", email);
        boolean exists = userService.existsByEmail(email);
        logger.info("✅ [UserController] Usuario {} existe: {}", email, exists);
        return ResponseEntity.ok(java.util.Map.of("exists", exists));
    }
}