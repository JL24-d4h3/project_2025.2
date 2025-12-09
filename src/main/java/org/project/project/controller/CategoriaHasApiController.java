package org.project.project.controller;

import org.project.project.repository.CategoriaHasApiRepository;
import org.springframework.stereotype.Controller;

@Controller
public class CategoriaHasApiController {
    final CategoriaHasApiRepository categoryHasApiRepository;

    public CategoriaHasApiController(CategoriaHasApiRepository categoryHasApiRepository) {
        this.categoryHasApiRepository = categoryHasApiRepository;
    }

}
