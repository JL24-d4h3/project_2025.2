//package org.project.project;
//
//import org.junit.jupiter.api.Test;
//import org.project.project.model.entity.API;
//import org.project.project.model.entity.Documentacion;
//import org.project.project.service.APIService;
//import org.project.project.service.DocumentationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class DocumentacionCrudTest {
//
//    @Autowired
//    private DocumentationService documentationService;
//
//    @Autowired
//    private APIService apiService; // Dependencia para crear una API de prueba
//
//    private Documentacion crearDocumentacionDePrueba() {
//        // Se necesita una API existente para asociarla a la documentación
//        API newApi = new API();
//        newApi.setNombreApi("API para Doc Test");
//        newApi.setDescripcionApi("Descripción para API de Doc");
//        newApi.setEstadoApi(API.EstadoApi.PRODUCCION);
//        API savedApi = apiService.guardarApi(newApi);
//
//        Documentacion newDoc = new Documentacion();
//        newDoc.setSeccionDocumentacion("Sección de Introducción");
//        newDoc.setApi(savedApi);
//        return newDoc;
//    }
//
//    @Test
//    public void testCrearYLeerDocumentacion() {
//        // Arrange
//        Documentacion newDoc = crearDocumentacionDePrueba();
//
//        // Act
//        Documentacion savedDoc = documentationService.guardarDocumentacion(newDoc);
//
//        // Assert
//        assertNotNull(savedDoc, "La documentación guardada no debería ser nula.");
//        assertNotNull(savedDoc.getDocumentacionId(), "La documentación guardada debe tener un ID.");
//        assertEquals("Sección de Introducción", savedDoc.getSeccionDocumentacion());
//
//        Documentacion readDoc = documentationService.buscarDocumentacionPorId(savedDoc.getDocumentacionId());
//        assertNotNull(readDoc, "La documentación leída no debería ser nula.");
//        assertEquals(savedDoc.getDocumentacionId(), readDoc.getDocumentacionId());
//        assertNotNull(readDoc.getApi(), "La documentación debe tener una API asociada.");
//    }
//
//    @Test
//    public void testActualizarDocumentacion() {
//        // Arrange
//        Documentacion doc = crearDocumentacionDePrueba();
//        Documentacion savedDoc = documentationService.guardarDocumentacion(doc);
//        Long docId = savedDoc.getDocumentacionId();
//
//        // Act
//        Documentacion docParaActualizar = new Documentacion();
//        docParaActualizar.setSeccionDocumentacion("Sección de Conclusión Actualizada");
//        docParaActualizar.setApi(savedDoc.getApi()); // Mantenemos la misma API
//
//        Documentacion updatedDoc = documentationService.actualizarDocumentacion(docId, docParaActualizar);
//
//        // Assert
//        assertNotNull(updatedDoc);
//        assertEquals(docId, updatedDoc.getDocumentacionId());
//        assertEquals("Sección de Conclusión Actualizada", updatedDoc.getSeccionDocumentacion());
//    }
//
//    @Test
//    public void testEliminarDocumentacion() {
//        // Arrange
//        Documentacion doc = crearDocumentacionDePrueba();
//        Documentacion savedDoc = documentationService.guardarDocumentacion(doc);
//        Long docId = savedDoc.getDocumentacionId();
//
//        // Act
//        documentationService.eliminarDocumentacion(docId);
//
//        // Assert
//        assertThrows(RuntimeException.class, () -> {
//            documentationService.buscarDocumentacionPorId(docId);
//        });
//    }
//}
