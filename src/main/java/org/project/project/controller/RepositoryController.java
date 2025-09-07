package org.project.project.controller;

import org.project.project.model.entity.Repositorio;
import org.project.project.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devportal/repositories")
public class RepositoryController {

    @Autowired
    private RepositoryService repositoryService;

    @GetMapping
    public List<Repositorio> getAllRepositories() {
        return repositoryService.listarRepositorios();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Repositorio> getRepositoryById(@PathVariable Integer id) {
        Repositorio repositorio = repositoryService.buscarRepositorioPorId(id);
        return ResponseEntity.ok(repositorio);
    }

    @PostMapping
    public Repositorio createRepository(@RequestBody Repositorio repositorio) {
        return repositoryService.guardarRepositorio(repositorio);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Repositorio> updateRepository(@PathVariable Integer id, @RequestBody Repositorio repositoryDetails) {
        Repositorio updatedRepositorio = repositoryService.actualizarRepositorio(id, repositoryDetails);
        return ResponseEntity.ok(updatedRepositorio);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepository(@PathVariable Integer id) {
        repositoryService.eliminarRepositorio(id);
        return ResponseEntity.noContent().build();
    }
}