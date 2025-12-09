package org.project.project.service;

import lombok.extern.slf4j.Slf4j;
import org.project.project.exception.ResourceNotFoundException;
import org.project.project.model.entity.API;
import org.project.project.repository.APIRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CatalogService {

    @Autowired
    private APIRepository apiRepository;

    public List<API> listarApis() {
        return apiRepository.findAll();
    }

    public API buscarApiPorId(Long id) {
        return apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API no encontrada con id: " + id));
    }

    public API guardarApi(API api) {
        // Usar creadoEn en lugar de fechaCreacionApi (que no existe en la BD)
        if (api.getCreadoEn() == null) {
            api.setCreadoEn(LocalDateTime.now());
        }
        return apiRepository.save(api);
    }

    public API actualizarApi(Long id, API apiDetalles) {
        API api = buscarApiPorId(id);
        api.setNombreApi(apiDetalles.getNombreApi());
        api.setDescripcionApi(apiDetalles.getDescripcionApi());
        api.setEstadoApi(apiDetalles.getEstadoApi());
        // La fecha de creación no se debería actualizar
        return apiRepository.save(api);
    }

    public void eliminarApi(Long id) {
        API api = buscarApiPorId(id);
        apiRepository.delete(api);
    }
}
