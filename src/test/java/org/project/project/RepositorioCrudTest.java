//package org.project.project;
//
//import org.junit.jupiter.api.Test;
//import org.project.project.model.entity.Repositorio;
//import org.project.project.service.RepositoryService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class RepositorioCrudTest {
//
//    @Autowired
//    private RepositoryService repositoryService;
//
//    private Repositorio crearRepositorioDePrueba() {
//        Repositorio newRepo = new Repositorio();
//        newRepo.setNombreRepositorio("Repositorio de Prueba");
//        newRepo.setDescripcionRepositorio("Descripción de prueba.");
//        newRepo.setVisibilidadRepositorio(Repositorio.VisibilidadRepositorio.PUBLICO);
//        newRepo.setAccesoRepositorio(Repositorio.AccesoRepositorio.CUALQUIER_PERSONA_CON_EL_ENLACE);
//        newRepo.setEstadoRepositorio(Repositorio.EstadoRepositorio.ACTIVO);
//        newRepo.setRamaPrincipalRepositorio("main");
//        return newRepo;
//    }
//
//    @Test
//    public void testCrearYLeerRepositorio() {
//        // Arrange
//        Repositorio newRepo = crearRepositorioDePrueba();
//
//        // Act
//        Repositorio savedRepo = repositoryService.guardarRepositorio(newRepo);
//
//        // Assert
//        assertNotNull(savedRepo);
//        assertNotNull(savedRepo.getRepositorioId());
//        assertEquals("Repositorio de Prueba", savedRepo.getNombreRepositorio());
//
//        Repositorio readRepo = repositoryService.buscarRepositorioPorId(savedRepo.getRepositorioId());
//        assertNotNull(readRepo);
//        assertEquals(savedRepo.getRepositorioId(), readRepo.getRepositorioId());
//    }
//
//    @Test
//    public void testActualizarRepositorio() {
//        // Arrange
//        Repositorio repo = crearRepositorioDePrueba();
//        Repositorio savedRepo = repositoryService.guardarRepositorio(repo);
//        Integer repoId = savedRepo.getRepositorioId();
//
//        // Act
//        Repositorio repoParaActualizar = crearRepositorioDePrueba();
//        repoParaActualizar.setNombreRepositorio("Repo Actualizado");
//        repoParaActualizar.setEstadoRepositorio(Repositorio.EstadoRepositorio.ARCHIVADO);
//
//        Repositorio updatedRepo = repositoryService.actualizarRepositorio(repoId, repoParaActualizar);
//
//        // Assert
//        assertNotNull(updatedRepo);
//        assertEquals(repoId, updatedRepo.getRepositorioId());
//        assertEquals("Repo Actualizado", updatedRepo.getNombreRepositorio());
//        assertEquals(Repositorio.EstadoRepositorio.ARCHIVADO, updatedRepo.getEstadoRepositorio());
//    }
//
//    @Test
//    public void testEliminarRepositorio() {
//        // Arrange
//        Repositorio repo = crearRepositorioDePrueba();
//        Repositorio savedRepo = repositoryService.guardarRepositorio(repo);
//        Integer repoId = savedRepo.getRepositorioId();
//
//        // Act
//        repositoryService.eliminarRepositorio(repoId);
//
//        // Assert
//        assertThrows(RuntimeException.class, () -> {
//            repositoryService.buscarRepositorioPorId(repoId);
//        });
//    }
//}
