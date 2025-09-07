package org.project.project.service;

import org.project.project.model.entity.Repositorio;
import org.project.project.repository.RepositorioRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RepositoryService {

    @Autowired
    private RepositorioRepository repositorioRepository;

    public List<Repositorio> listarRepositorios() {
        return repositorioRepository.findAll();
    }

    public Repositorio buscarRepositorioPorId(Integer id) {
        return repositorioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repositorio no encontrado con id: " + id));
    }

    public Repositorio guardarRepositorio(Repositorio repositorio) {
        repositorio.setFechaCreacion(LocalDateTime.now());
        return repositorioRepository.save(repositorio);
    }

    public Repositorio actualizarRepositorio(Integer id, Repositorio repositorioDetails) {
        Repositorio repositorio = buscarRepositorioPorId(id);
        repositorio.setNombreRepositorio(repositorioDetails.getNombreRepositorio());
        repositorio.setDescripcionRepositorio(repositorioDetails.getDescripcionRepositorio());
        repositorio.setVisibilidadRepositorio(repositorioDetails.getVisibilidadRepositorio());
        repositorio.setAccesoRepositorio(repositorioDetails.getAccesoRepositorio());
        repositorio.setEstadoRepositorio(repositorioDetails.getEstadoRepositorio());
        repositorio.setRamaPrincipalRepositorio(repositorioDetails.getRamaPrincipalRepositorio());
        return repositorioRepository.save(repositorio);
    }

    public void eliminarRepositorio(Integer id) {
        Repositorio repositorio = buscarRepositorioPorId(id);
        repositorioRepository.delete(repositorio);
    }
}
