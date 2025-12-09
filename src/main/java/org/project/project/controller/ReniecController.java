package org.project.project.controller;

import org.project.project.model.dto.ReniecResponseDTO;
import org.project.project.service.ReniecService;
import org.project.project.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller para validación de DNI con RENIEC
 * Endpoint público usado durante el registro de usuarios
 */
@RestController
@RequestMapping("/api/reniec")
public class ReniecController {
    
    private final ReniecService reniecService;
    private final UserService userService;
    
    public ReniecController(ReniecService reniecService, UserService userService) {
        this.reniecService = reniecService;
        this.userService = userService;
    }
    
    /**
     * Valida un DNI verificando:
     * 1. Si ya está registrado en la base de datos
     * 2. Si existe en RENIEC y retorna datos oficiales
     * 
     * @param dni - DNI de 8 dígitos a validar
     * @return JSON con el resultado de la validación
     */
    @GetMapping("/validate-dni")
    public ResponseEntity<Map<String, Object>> validateDni(@RequestParam String dni) {
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("🔍 [ReniecController] Validando DNI: " + dni);
        
        // 1. Validar formato del DNI
        if (dni == null || !dni.matches("\\d{8}")) {
            System.out.println("❌ [ReniecController] Formato inválido: " + dni);
            response.put("valid", false);
            response.put("message", "El DNI debe tener exactamente 8 dígitos numéricos");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 2. Verificar si el DNI ya está registrado en la base de datos
        if (!userService.isDniAvailable(dni)) {
            System.out.println("❌ [ReniecController] DNI ya registrado en BD: " + dni);
            response.put("valid", false);
            response.put("registered", true);
            response.put("message", "Este DNI ya está registrado en el sistema");
            return ResponseEntity.ok(response);
        }
        
        // 3. Consultar datos en RENIEC
        ReniecResponseDTO reniecData = reniecService.consultarDni(dni);
        
        if (reniecData == null) {
            // DNI NO EXISTE EN RENIEC o error de API
            System.out.println("❌ [ReniecController] DNI NO ENCONTRADO en RENIEC: " + dni);
            response.put("valid", false);
            response.put("registered", false);
            response.put("inexistente", true);
            response.put("message", "DNI INEXISTENTE - Este DNI no está registrado en RENIEC");
            return ResponseEntity.ok(response);
        }
        
        // 4. DNI válido - Retornar datos oficiales de RENIEC
        System.out.println("✅ [ReniecController] DNI válido: " + dni + " - " + reniecData.getFullName());
        response.put("valid", true);
        response.put("registered", false);
        response.put("inexistente", false);
        response.put("data", reniecData);
        response.put("message", "Datos oficiales de RENIEC cargados correctamente");
        
        return ResponseEntity.ok(response);
    }
}
