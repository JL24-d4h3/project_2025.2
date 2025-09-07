//package org.project.project;
//
//import org.junit.jupiter.api.Test;
//import org.project.project.model.entity.Proyecto;
//import org.project.project.service.ProjectService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class ProyectoCrudTest {
//
//    @Autowired
//    private ProjectService projectService;
//
//    private Proyecto crearProyectoDePrueba() {
//        Proyecto newProject = new Proyecto();
//        newProject.setNombreProyecto("Proyecto de Prueba");
//        newProject.setDescripcionProyecto("Descripción de prueba.");
//        newProject.setVisibilidadProyecto(Proyecto.VisibilidadProyecto.PRIVADO);
//        newProject.setAccesoProyecto(Proyecto.AccesoProyecto.RESTRINGIDO);
//        newProject.setPropietarioProyecto(Proyecto.PropietarioProyecto.USUARIO);
//        newProject.setEstadoProyecto(Proyecto.EstadoProyecto.PLANEADO);
//        return newProject;
//    }
//
//    @Test
//    public void testCrearYLeerProyecto() {
//        // Arrange
//        Proyecto newProject = crearProyectoDePrueba();
//
//        // Act
//        Proyecto savedProject = projectService.guardarProyecto(newProject);
//
//        // Assert
//        assertNotNull(savedProject);
//        assertNotNull(savedProject.getProyectoId());
//        assertEquals("Proyecto de Prueba", savedProject.getNombreProyecto());
//
//        Proyecto readProject = projectService.buscarProyectoPorId(savedProject.getProyectoId());
//        assertNotNull(readProject);
//        assertEquals(savedProject.getProyectoId(), readProject.getProyectoId());
//    }
//
//    @Test
//    public void testActualizarProyecto() {
//        // Arrange
//        Proyecto project = crearProyectoDePrueba();
//        Proyecto savedProject = projectService.guardarProyecto(project);
//        Integer projectId = savedProject.getProyectoId();
//
//        // Act
//        Proyecto proyectoParaActualizar = crearProyectoDePrueba(); // Re-usamos el helper
//        proyectoParaActualizar.setNombreProyecto("Proyecto Actualizado");
//        proyectoParaActualizar.setEstadoProyecto(Proyecto.EstadoProyecto.EN_DESARROLLO);
//
//        Proyecto updatedProject = projectService.actualizarProyecto(projectId, proyectoParaActualizar);
//
//        // Assert
//        assertNotNull(updatedProject);
//        assertEquals(projectId, updatedProject.getProyectoId());
//        assertEquals("Proyecto Actualizado", updatedProject.getNombreProyecto());
//        assertEquals(Proyecto.EstadoProyecto.EN_DESARROLLO, updatedProject.getEstadoProyecto());
//    }
//
//    @Test
//    public void testEliminarProyecto() {
//        // Arrange
//        Proyecto project = crearProyectoDePrueba();
//        Proyecto savedProject = projectService.guardarProyecto(project);
//        Integer projectId = savedProject.getProyectoId();
//
//        // Act
//        projectService.eliminarProyecto(projectId);
//
//        // Assert
//        assertThrows(RuntimeException.class, () -> {
//            projectService.buscarProyectoPorId(projectId);
//        });
//    }
//}
