package org.project.project.controller;

import org.project.project.model.entity.Usuario;
import org.project.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping(value = "/devportal/SA")
public class SystemAdministratorController {

    @Autowired
    private UserService userService;

    @GetMapping("/crear")
    public String showCreateForm(Model model) {
        // Asegurarse de que el objeto "usuario" esté en el modelo para el binding del formulario
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        return "SA/crear-usuario";
    }

    @PostMapping("/crear")
    public String processCreateForm(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            Usuario savedUser = userService.guardarUsuario(usuario);
            // Añadir mensaje de éxito para mostrarlo después de la redirección
            redirectAttributes.addFlashAttribute("successMessage", "¡Usuario " + savedUser.getUsername() + " creado con éxito!");
        } catch (Exception e) {
            // En un caso real, aquí se manejaría el error de forma más elegante
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el usuario: " + e.getMessage());
        }
        // Redirigir para evitar el reenvío del formulario si el usuario recarga la página
        return "redirect:/devportal/SA/crear";
    }

    @GetMapping("/listar")
    public String listUsers(Model model) {
        List<Usuario> usuarios = userService.listarUsuarios();
        model.addAttribute("usuarios", usuarios);
        System.out.println(usuarios);
        return "SA/lista-usuarios";
    }

    @DeleteMapping("/listar/{id}")
    public String eliminarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes, @PathVariable Long id) {

        try {
            // Añadir mensaje de éxito para mostrarlo después de la redirección
            redirectAttributes.addFlashAttribute("successMessage", "¡Usuario " + userService.eliminarUsuario(id).getUsername() + " creado con éxito!");
        } catch (Exception e) {
            // En un caso real, aquí se manejaría el error de forma más elegante
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/devportal/SA/listar";
    }
}
