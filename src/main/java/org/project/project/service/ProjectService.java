package org.project.project.service;

import org.project.project.model.entity.Proyecto;
import org.project.project.repository.ProyectoRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProyectoRepository proyectoRepository;

    public List<Proyecto> listarProyectos() {
        return proyectoRepository.findAll();
    }

    public Proyecto buscarProyectoPorId(Integer id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + id));
    }

    public Proyecto guardarProyecto(Proyecto proyecto) {
        proyecto.setFechaInicioProyecto(LocalDate.now());
        return proyectoRepository.save(proyecto);
    }

    public Proyecto actualizarProyecto(Integer id, Proyecto proyectoDetails) {
        Proyecto proyecto = buscarProyectoPorId(id);
        proyecto.setNombreProyecto(proyectoDetails.getNombreProyecto());
        proyecto.setDescripcionProyecto(proyectoDetails.getDescripcionProyecto());
        proyecto.setVisibilidadProyecto(proyectoDetails.getVisibilidadProyecto());
        proyecto.setAccesoProyecto(proyectoDetails.getAccesoProyecto());
        proyecto.setPropietarioProyecto(proyectoDetails.getPropietarioProyecto());
        proyecto.setEstadoProyecto(proyectoDetails.getEstadoProyecto());
        proyecto.setFechaInicioProyecto(proyectoDetails.getFechaInicioProyecto());
        proyecto.setFechaFinProyecto(proyectoDetails.getFechaFinProyecto());
        return proyectoRepository.save(proyecto);
    }

    public void eliminarProyecto(Integer id) {
        Proyecto proyecto = buscarProyectoPorId(id);
        proyectoRepository.delete(proyecto);
    }
}
