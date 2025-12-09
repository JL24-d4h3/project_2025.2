package org.project.project.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.project.project.model.entity.API;
import org.project.project.repository.*;
import org.project.project.service.APIService;
import org.project.project.service.ApiContractStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Slf4j
@Controller
@RequestMapping("/devportal/{role}/{username:.+}")
public class CatalogController {

    final CategoriaRepository categoriaRepository;
    final EtiquetaRepository etiquetaRepository;
    final APIRepository apiRepository;
    final UsuarioRepository usuarioRepository;

    public CatalogController(CategoriaRepository categoriaRepository, EtiquetaRepository etiquetaRepository,
                         APIRepository apiRepository, UsuarioRepository usuarioRepository) {
        this.categoriaRepository = categoriaRepository;
        this.etiquetaRepository = etiquetaRepository;
        this.apiRepository = apiRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Autowired
    private APIService apiService;

    @Autowired
    private ApiContractStorageService apiContractStorageService;

    @GetMapping(value = "/catalog")
    public String showApiCatalog(@PathVariable String role,
                                 @PathVariable(value = "username") String username,
                                 @RequestParam(name = "categorias", required = false) List<String> categorias,
                                 @RequestParam(name = "etiquetas",  required = false) List<String> etiquetas,
                                 @RequestParam(name = "estados",    required = false) List<String> estadosRaw,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 Model model,
                                 HttpServletResponse response) {

        // Añadir cabeceras para prevenir el caché del navegador
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/signin";
        }

        String authUsername = authentication.getName();
        String authRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("dev")
                .toLowerCase();

        if (!authUsername.equals(username) || !authRole.equals(role)) {
            return "redirect:/devportal/" + authRole + "/" + authUsername + "/portal";
        }

        // Obtener el usuario actual para el chatbot widget
        var currentUser = usuarioRepository.findByUsername(authUsername).orElse(null);
        model.addAttribute("Usuario", currentUser);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("etiquetas", etiquetaRepository.findAll());
        model.addAttribute("role", role);
        model.addAttribute("rol", role);
        model.addAttribute("username", username);
        model.addAttribute("currentNavSection", "catalog");
        List<String> cats = categorias == null ? List.of() : categorias;
        List<String> tags = etiquetas  == null ? List.of() : etiquetas;
        
        // ✅ FIX: Si no se especifica estado, mostrar solo APIs en PRODUCCION por defecto
        List<API.EstadoApi> estados;
        if (estadosRaw == null || estadosRaw.isEmpty()) {
            estados = List.of(API.EstadoApi.PRODUCCION); // Solo publicadas por defecto
        } else {
            estados = estadosRaw.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(API.EstadoApi::valueOf) // PRODUCCION | QA | DEPRECATED | BORRADOR
                    .toList();
        }

        boolean hasCats = !cats.isEmpty();
        boolean hasTags = !tags.isEmpty();
        boolean hasStats = !estados.isEmpty(); // Siempre será true ahora

        // Configuración de paginación: 9 APIs por página
        int pageSize = 9;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("apiId").descending());

        Page<API> apiPage;
        if (hasCats && hasTags && hasStats) {
            apiPage = apiRepository.findDistinctByCategories_CategoryNameInAndTags_TagNameInAndApiStatusIn(cats, tags, estados, pageable);
        } else if (hasCats && hasTags) {
            apiPage = apiRepository.findDistinctByCategories_CategoryNameInAndTags_TagNameIn(cats, tags, pageable);
        } else if (hasCats && hasStats) {
            apiPage = apiRepository.findDistinctByCategories_CategoryNameInAndApiStatusIn(cats, estados, pageable);
        } else if (hasTags && hasStats) {
            apiPage = apiRepository.findDistinctByTags_TagNameInAndApiStatusIn(tags, estados, pageable);
        } else if (hasCats) {
            apiPage = apiRepository.findDistinctByCategories_CategoryNameIn(cats, pageable);
        } else if (hasTags) {
            apiPage = apiRepository.findDistinctByTags_TagNameIn(tags, pageable);
        } else if (hasStats) {
            apiPage = apiRepository.findDistinctByApiStatusIn(estados, pageable);
        } else {
            apiPage = apiRepository.findAll(pageable);
        }

        model.addAttribute("apis", apiPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", apiPage.getTotalPages());
        model.addAttribute("totalElements", apiPage.getTotalElements());
        model.addAttribute("hasNext", apiPage.hasNext());
        model.addAttribute("hasPrevious", apiPage.hasPrevious());

        return "catalog/catalog";
    }
}