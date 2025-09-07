package org.project.project.controller;

import org.project.project.model.entity.Proyecto;
import org.project.project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devportal/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public List<Proyecto> getAllProjects() {
        return projectService.listarProyectos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proyecto> getProjectById(@PathVariable Integer id) {
        Proyecto proyecto = projectService.buscarProyectoPorId(id);
        return ResponseEntity.ok(proyecto);
    }

    @PostMapping
    public Proyecto createProject(@RequestBody Proyecto proyecto) {
        return projectService.guardarProyecto(proyecto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proyecto> updateProject(@PathVariable Integer id, @RequestBody Proyecto projectDetails) {
        Proyecto updatedProject = projectService.actualizarProyecto(id, projectDetails);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Integer id) {
        projectService.eliminarProyecto(id);
        return ResponseEntity.noContent().build();
    }
}