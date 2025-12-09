package org.project.project.controller;

import org.project.project.model.dto.SandboxRequestDTO;
import org.project.project.model.dto.SandboxResponseDTO;
import org.project.project.service.SandboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class SandboxController {

    @Autowired
    private SandboxService sandboxService;

    @GetMapping("/devportal/{rol}/{username}/test-environment")
    public String showSandboxPage(@PathVariable String rol, @PathVariable String username, Model model) {
        model.addAttribute("userRole", rol);
        model.addAttribute("username", username);
        model.addAttribute("currentNavSection", "test-environment");
        return "test-environment/sandbox";
    }

    @PostMapping("/devportal/{rol}/{username}/test-environment")
    @ResponseBody
    public String executeSandboxRequest(
            @PathVariable String rol,
            @PathVariable String username,
            @RequestBody Map<String, Object> formData) {
        // Construir el DTO desde el frontend
        SandboxRequestDTO request = new SandboxRequestDTO();
        request.setMethod((String) formData.get("method"));
        request.setUrl((String) formData.get("url"));

        // Convertir headers de manera segura
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) formData.get("headers");
        request.setHeaders(headers);

        request.setBody(formData.get("body"));

        // Ejecutar la petición
        long startTime = System.currentTimeMillis();
        SandboxResponseDTO response = sandboxService.executeRequest(request);
        long totalTime = System.currentTimeMillis() - startTime;

        // Generar el HTML de respuesta
        return sandboxService.generateResponseHtml(response, totalTime);
    }
}
