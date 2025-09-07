//package org.project.project;
//
//import org.junit.jupiter.api.Test;
//import org.project.project.model.entity.Ticket;
//import org.project.project.model.entity.Usuario;
//import org.project.project.service.TicketService;
//import org.project.project.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class TicketCrudTest {
//
//    @Autowired
//    private TicketService ticketService;
//
//    @Autowired
//    private UserService userService;
//
//    private Ticket crearTicketDePrueba() {
//        // Un ticket debe ser reportado por un usuario existente
//        Usuario user = new Usuario();
//        long uniqueId = System.currentTimeMillis();
//        user.setUsername("reporter_" + uniqueId);
//        user.setNombreUsuario("Reporter");
//        user.setApellidoPaterno("User");
//        user.setApellidoMaterno("Test");
//        user.setDni("" + uniqueId % 100000000);
//        user.setCorreo("reporter_" + uniqueId + "@example.com");
//        user.setHashedPassword("password");
//        user.setDireccionUsuario("Calle Falsa 123");
//        user.setEstadoUsuario(Usuario.EstadoUsuario.HABILITADO);
//        user.setActividadUsuario(Usuario.ActividadUsuario.ACTIVO);
//        user.setCodigoUsuario("REP_" + uniqueId);
//        user.setAccesoUsuario(Usuario.AccesoUsuario.SI);
//        Usuario savedUser = userService.guardarUsuario(user);
//
//        Ticket newTicket = new Ticket();
//        newTicket.setAsuntoTicket("Asunto de prueba");
//        newTicket.setCuerpoTicket("Este es el cuerpo del ticket de prueba.");
//        newTicket.setEstadoTicket(Ticket.EstadoTicket.ENVIADO);
//        newTicket.setTipoTicket(Ticket.TipoTicket.CONSULTA);
//        newTicket.setPrioridadTicket(Ticket.PrioridadTicket.MEDIA);
//        newTicket.setReportadoPor(savedUser);
//
//        return newTicket;
//    }
//
//    @Test
//    public void testCrearYLeerTicket() {
//        // Arrange
//        Ticket newTicket = crearTicketDePrueba();
//
//        // Act
//        Ticket savedTicket = ticketService.guardarTicket(newTicket);
//
//        // Assert
//        assertNotNull(savedTicket);
//        assertNotNull(savedTicket.getTicketId());
//        assertEquals("Asunto de prueba", savedTicket.getAsuntoTicket());
//        assertNotNull(savedTicket.getReportadoPor());
//
//        Ticket readTicket = ticketService.buscarTicketPorId(savedTicket.getTicketId());
//        assertNotNull(readTicket);
//        assertEquals(savedTicket.getTicketId(), readTicket.getTicketId());
//    }
//
//    @Test
//    public void testActualizarTicket() {
//        // Arrange
//        Ticket ticket = crearTicketDePrueba();
//        Ticket savedTicket = ticketService.guardarTicket(ticket);
//        Integer ticketId = savedTicket.getTicketId();
//
//        // Act
//        Ticket ticketParaActualizar = new Ticket(); // Solo se actualizan algunos campos
//        ticketParaActualizar.setAsuntoTicket("Asunto Actualizado");
//        ticketParaActualizar.setEstadoTicket(Ticket.EstadoTicket.EN_PROGRESO);
//        ticketParaActualizar.setPrioridadTicket(Ticket.PrioridadTicket.ALTA);
//        // Los demás campos no se deberían poder cambiar en una actualización simple
//        ticketParaActualizar.setCuerpoTicket(savedTicket.getCuerpoTicket());
//        ticketParaActualizar.setTipoTicket(savedTicket.getTipoTicket());
//        ticketParaActualizar.setAsignadoA(savedTicket.getAsignadoA());
//
//        Ticket updatedTicket = ticketService.actualizarTicket(ticketId, ticketParaActualizar);
//
//        // Assert
//        assertNotNull(updatedTicket);
//        assertEquals(ticketId, updatedTicket.getTicketId());
//        assertEquals("Asunto Actualizado", updatedTicket.getAsuntoTicket());
//        assertEquals(Ticket.EstadoTicket.EN_PROGRESO, updatedTicket.getEstadoTicket());
//    }
//
//    @Test
//    public void testEliminarTicket() {
//        // Arrange
//        Ticket ticket = crearTicketDePrueba();
//        Ticket savedTicket = ticketService.guardarTicket(ticket);
//        Integer ticketId = savedTicket.getTicketId();
//
//        // Act
//        ticketService.eliminarTicket(ticketId);
//
//        // Assert
//        assertThrows(RuntimeException.class, () -> {
//            ticketService.buscarTicketPorId(ticketId);
//        });
//    }
//}
