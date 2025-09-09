package org.project.project.controller;

import org.project.project.model.entity.Usuario;
import org.project.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/devportal/users")
public class UserViewController {

    @Autowired
    private UserService userService;

    @GetMapping("/crear")
    public String showCreateForm(Model model) {
        // Asegurarse de que el objeto "usuario" esté en el modelo para el binding del formulario
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        return "crear-usuario";
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
        return "redirect:/devportal/users/crear";
    }

    @GetMapping("/listar")
    public String listUsers(Model model) {
        List<Usuario> usuarios = userService.listarUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "lista-usuarios";
    }
}
