//package org.project.project;
//
//import org.junit.jupiter.api.Test;
//import org.project.project.model.entity.API;
//import org.project.project.service.APIService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class APICrudTest {
//
//    @Autowired
//    private APIService apiService;
//
//    private API crearApiDePrueba() {
//        API newApi = new API();
//        newApi.setNombreApi("API de Prueba");
//        newApi.setDescripcionApi("Esta es una descripción de prueba.");
//        newApi.setEstadoApi(API.EstadoApi.PRODUCCION);
//        return newApi;
//    }
//
//    @Test
//    public void testCrearYLeerApi() {
//        // Arrange
//        API newApi = crearApiDePrueba();
//
//        // Act
//        API savedApi = apiService.guardarApi(newApi);
//
//        // Assert
//        assertNotNull(savedApi, "La API guardada no debería ser nula.");
//        assertNotNull(savedApi.getApiId(), "La API guardada debe tener un ID asignado.");
//        assertEquals("API de Prueba", savedApi.getNombreApi());
//
//        API readApi = apiService.buscarApiPorId(savedApi.getApiId());
//        assertNotNull(readApi, "La API leída no debería ser nula.");
//        assertEquals(savedApi.getApiId(), readApi.getApiId());
//    }
//
//    @Test
//    public void testActualizarApi() {
//        // Arrange
//        API api = crearApiDePrueba();
//        API savedApi = apiService.guardarApi(api);
//        Long apiId = savedApi.getApiId();
//
//        // Act
//        API apiParaActualizar = new API();
//        apiParaActualizar.setNombreApi("API Actualizada");
//        apiParaActualizar.setDescripcionApi("Descripción actualizada.");
//        apiParaActualizar.setEstadoApi(API.EstadoApi.QA);
//
//        API updatedApi = apiService.actualizarApi(apiId, apiParaActualizar);
//
//        // Assert
//        assertNotNull(updatedApi, "La API actualizada no debería ser nula.");
//        assertEquals(apiId, updatedApi.getApiId());
//        assertEquals("API Actualizada", updatedApi.getNombreApi());
//        assertEquals(API.EstadoApi.QA, updatedApi.getEstadoApi());
//    }
//
//    @Test
//    public void testEliminarApi() {
//        // Arrange
//        API api = crearApiDePrueba();
//        API savedApi = apiService.guardarApi(api);
//        Long apiId = savedApi.getApiId();
//
//        // Act
//        apiService.eliminarApi(apiId);
//
//        // Assert
//        assertThrows(RuntimeException.class, () -> {
//            apiService.buscarApiPorId(apiId);
//        }, "Se esperaba que se lanzara una excepción al buscar una API eliminada.");
//    }
//}
