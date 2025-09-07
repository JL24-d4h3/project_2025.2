//package org.project.project;
//
//import org.junit.jupiter.api.Test;
//import org.project.project.model.entity.Usuario;
//import org.project.project.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class UsuarioCrudTest {
//
//    @Autowired
//    private UserService userService;
//
//    private Usuario crearUsuarioDePrueba() {
//        Usuario newUser = new Usuario();
//        // Usamos System.currentTimeMillis() para asegurar valores únicos para campos `unique`
//        long uniqueId = System.currentTimeMillis();
//        newUser.setUsername("testuser_" + uniqueId);
//        newUser.setNombreUsuario("Test");
//        newUser.setApellidoPaterno("Usuario");
//        newUser.setApellidoMaterno("Prueba");
//        newUser.setDni("" + uniqueId % 100000000);
//        newUser.setCorreo("test_" + uniqueId + "@example.com");
//        newUser.setHashedPassword("password_hashed"); // En un caso real, esto debería ser un hash real
//        newUser.setDireccionUsuario("Calle Falsa 123");
//        newUser.setEstadoUsuario(Usuario.EstadoUsuario.HABILITADO);
//        newUser.setActividadUsuario(Usuario.ActividadUsuario.ACTIVO);
//        newUser.setCodigoUsuario("COD_" + uniqueId);
//        newUser.setAccesoUsuario(Usuario.AccesoUsuario.SI);
//        return newUser;
//    }
//
//    @Test
//    public void testCrearYLeerUsuario() {
//        // Arrange
//        Usuario newUser = crearUsuarioDePrueba();
//
//        // Act
//        Usuario savedUser = userService.guardarUsuario(newUser);
//
//        // Assert
//        assertNotNull(savedUser, "El usuario guardado no debería ser nulo.");
//        assertNotNull(savedUser.getUsuarioId(), "El usuario guardado debe tener un ID asignado.");
//        assertEquals(newUser.getUsername(), savedUser.getUsername());
//
//        Usuario readUser = userService.buscarUsuarioPorId(savedUser.getUsuarioId());
//        assertNotNull(readUser, "El usuario leído no debería ser nulo.");
//        assertEquals(savedUser.getUsuarioId(), readUser.getUsuarioId());
//    }
//
//    @Test
//    public void testActualizarUsuario() {
//        // Arrange
//        Usuario user = crearUsuarioDePrueba();
//        Usuario savedUser = userService.guardarUsuario(user);
//        Long userId = savedUser.getUsuarioId();
//
//        // Act
//        Usuario usuarioParaActualizar = new Usuario();
//        usuarioParaActualizar.setNombreUsuario("Nombre Actualizado");
//        // Copiamos los demás campos para no fallar las validaciones de "not null"
//        usuarioParaActualizar.setApellidoPaterno(savedUser.getApellidoPaterno());
//        usuarioParaActualizar.setApellidoMaterno(savedUser.getApellidoMaterno());
//        usuarioParaActualizar.setDni(savedUser.getDni());
//        usuarioParaActualizar.setCorreo(savedUser.getCorreo());
//        usuarioParaActualizar.setDireccionUsuario("Dirección Actualizada 123");
//        usuarioParaActualizar.setUsername(savedUser.getUsername());
//        usuarioParaActualizar.setEstadoUsuario(Usuario.EstadoUsuario.INHABILITADO);
//        usuarioParaActualizar.setActividadUsuario(Usuario.ActividadUsuario.INACTIVO);
//        usuarioParaActualizar.setCodigoUsuario(savedUser.getCodigoUsuario());
//        usuarioParaActualizar.setAccesoUsuario(Usuario.AccesoUsuario.NO);
//
//        Usuario updatedUser = userService.actualizarUsuario(userId, usuarioParaActualizar);
//
//        // Assert
//        assertNotNull(updatedUser, "El usuario actualizado no debería ser nulo.");
//        assertEquals(userId, updatedUser.getUsuarioId());
//        assertEquals("Nombre Actualizado", updatedUser.getNombreUsuario());
//        assertEquals(Usuario.EstadoUsuario.INHABILITADO, updatedUser.getEstadoUsuario());
//    }
//
//    @Test
//    public void testEliminarUsuario() {
//        // Arrange
//        Usuario user = crearUsuarioDePrueba();
//        Usuario savedUser = userService.guardarUsuario(user);
//        Long userId = savedUser.getUsuarioId();
//
//        // Act
//        userService.eliminarUsuario(userId);
//
//        // Assert
//        assertThrows(RuntimeException.class, () -> {
//            userService.buscarUsuarioPorId(userId);
//        }, "Se esperaba que se lanzara una excepción al buscar un usuario eliminado.");
//    }
//}
