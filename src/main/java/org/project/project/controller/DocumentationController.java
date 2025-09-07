package org.project.project.controller;

import org.project.project.model.entity.Documentacion;
import org.project.project.service.DocumentationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devportal/documentations")
public class DocumentationController {

    @Autowired
    private DocumentationService documentationService;

    @GetMapping
    public List<Documentacion> getAllDocumentations() {
        return documentationService.listarDocumentaciones();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Documentacion> getDocumentationById(@PathVariable Long id) {
        Documentacion documentacion = documentationService.buscarDocumentacionPorId(id);
        return ResponseEntity.ok(documentacion);
    }

    @PostMapping
    public Documentacion createDocumentation(@RequestBody Documentacion documentacion) {
        return documentationService.guardarDocumentacion(documentacion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Documentacion> updateDocumentation(@PathVariable Long id, @RequestBody Documentacion documentationDetails) {
        Documentacion updatedDocumentation = documentationService.actualizarDocumentacion(id, documentationDetails);
        return ResponseEntity.ok(updatedDocumentation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocumentation(@PathVariable Long id) {
        documentationService.eliminarDocumentacion(id);
        return ResponseEntity.noContent().build();
    }
}