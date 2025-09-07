package org.project.project.service;

import org.project.project.model.entity.Documentacion;
import org.project.project.repository.DocumentacionRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentationService {

    @Autowired
    private DocumentacionRepository documentacionRepository;

    public List<Documentacion> listarDocumentaciones() {
        return documentacionRepository.findAll();
    }

    public Documentacion buscarDocumentacionPorId(Long id) {
        return documentacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documentacion no encontrada con id: " + id));
    }

    public Documentacion guardarDocumentacion(Documentacion documentacion) {
        return documentacionRepository.save(documentacion);
    }

    public Documentacion actualizarDocumentacion(Long id, Documentacion documentacionDetails) {
        Documentacion documentacion = buscarDocumentacionPorId(id);
        documentacion.setSeccionDocumentacion(documentacionDetails.getSeccionDocumentacion());
        documentacion.setApi(documentacionDetails.getApi());
        return documentacionRepository.save(documentacion);
    }

    public void eliminarDocumentacion(Long id) {
        Documentacion documentacion = buscarDocumentacionPorId(id);
        documentacionRepository.delete(documentacion);
    }
}