-- Script de INSERTs completo para dev_portal_file.sql
-- Creado: 2025-09-29
-- Descripción: Inserts idempotentes para todas las tablas principales
-- Distribución de usuarios: 3 SA, 6 PO, 12 QA, 20 DEV

SET FOREIGN_KEY_CHECKS = 0;

-- ================================================
-- ROLES (idempotente - no especifica ID)
-- ================================================
INSERT INTO `database`.`rol` (`nombre_rol`, `tipo_rol_proyecto`, `descripcion_rol`, `activo`)
SELECT 'DEV', NULL, 'Desarrollador', TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `database`.`rol` WHERE `nombre_rol` = 'DEV');

INSERT INTO `database`.`rol` (`nombre_rol`, `tipo_rol_proyecto`, `descripcion_rol`, `activo`)
SELECT 'QA', NULL, 'Quality Assurance', TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `database`.`rol` WHERE `nombre_rol` = 'QA');

INSERT INTO `database`.`rol` (`nombre_rol`, `tipo_rol_proyecto`, `descripcion_rol`, `activo`)
SELECT 'PO', NULL, 'Product Owner', TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `database`.`rol` WHERE `nombre_rol` = 'PO');

INSERT INTO `database`.`rol` (`nombre_rol`, `tipo_rol_proyecto`, `descripcion_rol`, `activo`)
SELECT 'SA', NULL, 'Super Administrator', TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `database`.`rol` WHERE `nombre_rol` = 'SA');

-- ================================================
-- CATEGORÍAS
-- ================================================
INSERT INTO `database`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Pagos', 'Funcionalidades relacionadas con pagos'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`categoria` WHERE `nombre_categoria` = 'Pagos');

INSERT INTO `database`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Clientes', 'Gestión de clientes'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`categoria` WHERE `nombre_categoria` = 'Clientes');

INSERT INTO `database`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Logística', 'Logística y envíos'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`categoria` WHERE `nombre_categoria` = 'Logística');

INSERT INTO `database`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Autenticación', 'Autenticación y autorización'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`categoria` WHERE `nombre_categoria` = 'Autenticación');

INSERT INTO `database`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Notificaciones', 'Sistemas de notificaciones'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`categoria` WHERE `nombre_categoria` = 'Notificaciones');

INSERT INTO `database`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Geolocalización', 'APIs de mapas y geo'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`categoria` WHERE `nombre_categoria` = 'Geolocalización');

INSERT INTO `database`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Analítica', 'Métricas y analítica'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`categoria` WHERE `nombre_categoria` = 'Analítica');

INSERT INTO `database`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Almacenamiento', 'Almacenamiento de objetos'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`categoria` WHERE `nombre_categoria` = 'Almacenamiento');

-- ================================================
-- ETIQUETAS
-- ================================================
INSERT INTO `database`.`etiqueta` (`nombre_tag`)
SELECT 'REST' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`etiqueta` WHERE `nombre_tag` = 'REST');

INSERT INTO `database`.`etiqueta` (`nombre_tag`)
SELECT 'GraphQL' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`etiqueta` WHERE `nombre_tag` = 'GraphQL');

INSERT INTO `database`.`etiqueta` (`nombre_tag`)
SELECT 'Eventos' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`etiqueta` WHERE `nombre_tag` = 'Eventos');

INSERT INTO `database`.`etiqueta` (`nombre_tag`)
SELECT 'WebSocket' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`etiqueta` WHERE `nombre_tag` = 'WebSocket');

INSERT INTO `database`.`etiqueta` (`nombre_tag`)
SELECT 'JSON' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`etiqueta` WHERE `nombre_tag` = 'JSON');

INSERT INTO `database`.`etiqueta` (`nombre_tag`)
SELECT 'XML' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`etiqueta` WHERE `nombre_tag` = 'XML');

INSERT INTO `database`.`etiqueta` (`nombre_tag`)
SELECT 'Seguridad' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`etiqueta` WHERE `nombre_tag` = 'Seguridad');

INSERT INTO `database`.`etiqueta` (`nombre_tag`)
SELECT 'Microservicios' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`etiqueta` WHERE `nombre_tag` = 'Microservicios');

INSERT INTO `database`.`etiqueta` (`nombre_tag`)
SELECT 'Cloud' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`etiqueta` WHERE `nombre_tag` = 'Cloud');

INSERT INTO `database`.`etiqueta` (`nombre_tag`)
SELECT 'Mobile' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`etiqueta` WHERE `nombre_tag` = 'Mobile');

-- ================================================
-- CLASIFICACIONES
-- ================================================
INSERT INTO `database`.`clasificacion` (`tipo_contenido_texto`)
SELECT 'Guía de inicio rápido' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`clasificacion` WHERE `tipo_contenido_texto` = 'Guía de inicio rápido');

INSERT INTO `database`.`clasificacion` (`tipo_contenido_texto`)
SELECT 'Tutorial' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`clasificacion` WHERE `tipo_contenido_texto` = 'Tutorial');

INSERT INTO `database`.`clasificacion` (`tipo_contenido_texto`)
SELECT 'Referencia API' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`clasificacion` WHERE `tipo_contenido_texto` = 'Referencia API');

INSERT INTO `database`.`clasificacion` (`tipo_contenido_texto`)
SELECT 'Ejemplos de código' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`clasificacion` WHERE `tipo_contenido_texto` = 'Ejemplos de código');

INSERT INTO `database`.`clasificacion` (`tipo_contenido_texto`)
SELECT 'Mejores prácticas' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`clasificacion` WHERE `tipo_contenido_texto` = 'Mejores prácticas');

INSERT INTO `database`.`clasificacion` (`tipo_contenido_texto`)
SELECT 'Solución de problemas' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`clasificacion` WHERE `tipo_contenido_texto` = 'Solución de problemas');

-- ================================================
-- EQUIPOS
-- ================================================
INSERT INTO `database`.`equipo` (`nombre_equipo`)
SELECT 'Backend Development' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`equipo` WHERE `nombre_equipo` = 'Backend Development');

INSERT INTO `database`.`equipo` (`nombre_equipo`)
SELECT 'Frontend Development' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`equipo` WHERE `nombre_equipo` = 'Frontend Development');

INSERT INTO `database`.`equipo` (`nombre_equipo`)
SELECT 'Quality Assurance' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`equipo` WHERE `nombre_equipo` = 'Quality Assurance');

INSERT INTO `database`.`equipo` (`nombre_equipo`)
SELECT 'DevOps' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`equipo` WHERE `nombre_equipo` = 'DevOps');

INSERT INTO `database`.`equipo` (`nombre_equipo`)
SELECT 'Product Management' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`equipo` WHERE `nombre_equipo` = 'Product Management');

INSERT INTO `database`.`equipo` (`nombre_equipo`)
SELECT 'Security Team' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`equipo` WHERE `nombre_equipo` = 'Security Team');

-- ================================================
-- USUARIOS - 20 DEV
-- ================================================
INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Carlos', 'Gomez', 'Lopez', '71234567', '1990-05-15', 'HOMBRE', 'SOLTERO', '+51987654321', 'carlos.gomez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Siempre Viva 123, Lima', 'cgomez', NOW(), NOW(), 'profile_carlos.jpg', 'HABILITADO', 'ACTIVO', 'DEV001', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '71234567');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Maria', 'Rodriguez', 'Santos', '72345678', '1992-08-22', 'MUJER', 'CASADO', '+51987654322', 'maria.rodriguez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. Union 456, Lima', 'mrodriguez', NOW(), NOW(), 'profile_maria.jpg', 'HABILITADO', 'ACTIVO', 'DEV002', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '72345678');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Juan', 'Perez', 'Garcia', '73456789', '1988-12-10', 'HOMBRE', 'CASADO', '+51987654323', 'juan.perez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Arequipa 789, Lima', 'jperez', NOW(), NOW(), 'profile_juan.jpg', 'HABILITADO', 'ACTIVO', 'DEV003', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '73456789');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Laura', 'Martinez', 'Diaz', '74567890', '1991-03-25', 'MUJER', 'SOLTERO', '+51987654324', 'laura.martinez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle Las Flores 321, Lima', 'lmartinez', NOW(), NOW(), 'profile_laura.jpg', 'HABILITADO', 'ACTIVO', 'DEV004', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '74567890');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Pedro', 'Sanchez', 'Vargas', '75678901', '1989-07-18', 'HOMBRE', 'SOLTERO', '+51987654325', 'pedro.sanchez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Javier Prado 654, Lima', 'psanchez', NOW(), NOW(), 'profile_pedro.jpg', 'HABILITADO', 'ACTIVO', 'DEV005', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '75678901');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Ana', 'Torres', 'Rojas', '76789012', '1993-11-05', 'MUJER', 'CASADO', '+51987654326', 'ana.torres@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Urb. Los Próceres 987, Lima', 'atorres', NOW(), NOW(), 'profile_ana.jpg', 'HABILITADO', 'ACTIVO', 'DEV006', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '76789012');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Miguel', 'Castillo', 'Flores', '77890123', '1990-02-14', 'HOMBRE', 'SOLTERO', '+51987654327', 'miguel.castillo@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. La Marina 147, Lima', 'mcastillo', NOW(), NOW(), 'profile_miguel.jpg', 'HABILITADO', 'ACTIVO', 'DEV007', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '77890123');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Sofia', 'Ramirez', 'Mendoza', '78901234', '1994-06-30', 'MUJER', 'SOLTERO', '+51987654328', 'sofia.ramirez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. de la Unión 258, Lima', 'sramirez', NOW(), NOW(), 'profile_sofia.jpg', 'HABILITADO', 'ACTIVO', 'DEV008', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '78901234');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Diego', 'Herrera', 'Castro', '79012345', '1987-09-12', 'HOMBRE', 'CASADO', '+51987654329', 'diego.herrera@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Brasil 369, Lima', 'dherrera', NOW(), NOW(), 'profile_diego.jpg', 'HABILITADO', 'ACTIVO', 'DEV009', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '79012345');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Elena', 'Morales', 'Silva', '80123456', '1992-04-08', 'MUJER', 'SOLTERO', '+51987654330', 'elena.morales@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle Schell 753, Lima', 'emorales', NOW(), NOW(), 'profile_elena.jpg', 'HABILITADO', 'ACTIVO', 'DEV010', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '80123456');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Ricardo', 'Ortiz', 'Chavez', '81234567', '1986-01-20', 'HOMBRE', 'CASADO', '+51987654331', 'ricardo.ortiz@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Angamos 951, Lima', 'rortiz', NOW(), NOW(), 'profile_ricardo.jpg', 'HABILITADO', 'ACTIVO', 'DEV011', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '81234567');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Carmen', 'Vega', 'Paredes', '82345678', '1995-08-15', 'MUJER', 'SOLTERO', '+51987654332', 'carmen.vega@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. Carabaya 357, Lima', 'cvega', NOW(), NOW(), 'profile_carmen.jpg', 'HABILITADO', 'ACTIVO', 'DEV012', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '82345678');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Javier', 'Rios', 'Cabrera', '83456789', '1989-12-03', 'HOMBRE', 'SOLTERO', '+51987654333', 'javier.rios@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Tacna 159, Lima', 'jrios', NOW(), NOW(), 'profile_javier.jpg', 'HABILITADO', 'ACTIVO', 'DEV013', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '83456789');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Patricia', 'Medina', 'Leon', '84567890', '1991-07-22', 'MUJER', 'CASADO', '+51987654334', 'patricia.medina@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Urb. San Felipe 486, Lima', 'pmedina', NOW(), NOW(), 'profile_patricia.jpg', 'HABILITADO', 'ACTIVO', 'DEV014', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '84567890');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Oscar', 'Diaz', 'Romero', '85678901', '1988-03-17', 'HOMBRE', 'SOLTERO', '+51987654335', 'oscar.diaz@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Petit Thouars 642, Lima', 'odiaz', NOW(), NOW(), 'profile_oscar.jpg', 'HABILITADO', 'ACTIVO', 'DEV015', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '85678901');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Teresa', 'Guerrero', 'Suarez', '86789012', '1993-10-28', 'MUJER', 'SOLTERO', '+51987654336', 'teresa.guerrero@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle Montevideo 825, Lima', 'tguerrero', NOW(), NOW(), 'profile_teresa.jpg', 'HABILITADO', 'ACTIVO', 'DEV016', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '86789012');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Fernando', 'Cordova', 'Aguilar', '87890123', '1990-05-11', 'HOMBRE', 'CASADO', '+51987654337', 'fernando.cordova@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Universitaria 741, Lima', 'fcordova', NOW(), NOW(), 'profile_fernando.jpg', 'HABILITADO', 'ACTIVO', 'DEV017', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '87890123');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Gabriela', 'Paredes', 'Torres', '88901234', '1994-02-09', 'MUJER', 'SOLTERO', '+51987654338', 'gabriela.paredes@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. Huallaga 963, Lima', 'gparedes', NOW(), NOW(), 'profile_gabriela.jpg', 'HABILITADO', 'ACTIVO', 'DEV018', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '88901234');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Hector', 'Salazar', 'Mendez', '89012345', '1987-11-14', 'HOMBRE', 'SOLTERO', '+51987654339', 'hector.salazar@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Canada 528, Lima', 'hsalazar', NOW(), NOW(), 'profile_hector.jpg', 'HABILITADO', 'ACTIVO', 'DEV019', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '89012345');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Lucia', 'Valdivia', 'Reyes', '90123456', '1992-06-25', 'MUJER', 'CASADO', '+51987654340', 'lucia.valdivia@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle Bolívar 417, Lima', 'lvaldivia', NOW(), NOW(), 'profile_lucia.jpg', 'HABILITADO', 'ACTIVO', 'DEV020', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '90123456');

-- ================================================
-- USUARIOS - 12 QA
-- ================================================
INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Roberto', 'Mendoza', 'Castro', '91234567', '1985-04-18', 'HOMBRE', 'CASADO', '+51987654401', 'roberto.mendoza@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Salaverry 123, Lima', 'rmendoza', NOW(), NOW(), 'profile_roberto.jpg', 'HABILITADO', 'ACTIVO', 'QA001', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '91234567');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Silvia', 'Quispe', 'Lopez', '92345678', '1989-09-22', 'MUJER', 'SOLTERO', '+51987654402', 'silvia.quispe@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. Azángaro 456, Lima', 'squispe', NOW(), NOW(), 'profile_silvia.jpg', 'HABILITADO', 'ACTIVO', 'QA002', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '92345678');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Raul', 'Gonzales', 'Vera', '93456789', '1990-12-15', 'HOMBRE', 'SOLTERO', '+51987654403', 'raul.gonzales@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Alfonso Ugarte 789, Lima', 'rgonzales', NOW(), NOW(), 'profile_raul.jpg', 'HABILITADO', 'ACTIVO', 'QA003', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '93456789');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Veronica', 'Castro', 'Rios', '94567890', '1987-07-30', 'MUJER', 'CASADO', '+51987654404', 'veronica.castro@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle Roma 321, Lima', 'vcastro', NOW(), NOW(), 'profile_veronica.jpg', 'HABILITADO', 'ACTIVO', 'QA004', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '94567890');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Mario', 'Torres', 'Diaz', '95678901', '1991-03-08', 'HOMBRE', 'SOLTERO', '+51987654405', 'mario.torres@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Petit Thouars 654, Lima', 'mtorres', NOW(), NOW(), 'profile_mario.jpg', 'HABILITADO', 'ACTIVO', 'QA005', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '95678901');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Fernando', 'Cruz', 'Lopez', '96789011', '1986-10-10', 'HOMBRE', 'CASADO', '+51987654406', 'fernando.cruz@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Arenales 111, Lima', 'fcruz', NOW(), NOW(), 'profile_fernando2.jpg', 'HABILITADO', 'ACTIVO', 'QA006', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '96789011');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Yolanda', 'Sierra', 'Paz', '97890122', '1988-02-20', 'MUJER', 'SOLTERO', '+51987654407', 'yolanda.sierra@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. Lampa 45, Lima', 'ysierra', NOW(), NOW(), 'profile_yolanda.jpg', 'HABILITADO', 'ACTIVO', 'QA007', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '97890122');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Diego', 'Mora', 'Quispe', '98901233', '1992-09-01', 'HOMBRE', 'SOLTERO', '+51987654408', 'diego.mora@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle Dos de Mayo 12, Lima', 'dmora', NOW(), NOW(), 'profile_diego2.jpg', 'HABILITADO', 'ACTIVO', 'QA008', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '98901233');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Lucero', 'Vargas', 'Pinto', '99012344', '1991-12-12', 'MUJER', 'CASADO', '+51987654409', 'lucero.vargas@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Salaverry 222, Lima', 'lvargas', NOW(), NOW(), 'profile_lucero.jpg', 'HABILITADO', 'ACTIVO', 'QA009', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '99012344');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Sergio', 'Alonso', 'Ramos', '00123455', '1984-06-06', 'HOMBRE', 'CASADO', '+51987654410', 'sergio.alonso@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. Puno 50, Lima', 'salonso', NOW(), NOW(), 'profile_sergio.jpg', 'HABILITADO', 'ACTIVO', 'QA010', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '00123455');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Paty', 'Nunez', 'Lopez', '01234566', '1993-05-05', 'MUJER', 'SOLTERO', '+51987654411', 'paty.nunez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle San Martin 66, Lima', 'pnunez', NOW(), NOW(), 'profile_paty.jpg', 'HABILITADO', 'ACTIVO', 'QA011', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '01234566');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Rocio', 'Beltran', 'Cano', '02345677', '1990-11-11', 'MUJER', 'SOLTERO', '+51987654412', 'rocio.beltran@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Javier Prado 77, Lima', 'rbeltran', NOW(), NOW(), 'profile_rocio.jpg', 'HABILITADO', 'ACTIVO', 'QA012', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '02345677');

-- ================================================
-- USUARIOS - 6 PO
-- ================================================
INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Alejandro', 'Vargas', 'Santos', '03456788', '1983-05-20', 'HOMBRE', 'CASADO', '+51987654501', 'alejandro.vargas@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Benavides 123, Lima', 'avargas', NOW(), NOW(), 'profile_alejandro.jpg', 'HABILITADO', 'ACTIVO', 'PO001', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '03456788');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Daniela', 'Rojas', 'Mendoza', '04567899', '1986-08-14', 'MUJER', 'SOLTERO', '+51987654502', 'daniela.rojas@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. de la Unión 456, Lima', 'drojas', NOW(), NOW(), 'profile_daniela.jpg', 'HABILITADO', 'ACTIVO', 'PO002', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '04567899');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Arturo', 'Silva', 'Perez', '05678900', '1980-11-25', 'HOMBRE', 'CASADO', '+51987654503', 'arturo.silva@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Javier Prado 789, Lima', 'asilva', NOW(), NOW(), 'profile_arturo.jpg', 'HABILITADO', 'ACTIVO', 'PO003', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '05678900');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Claudia', 'Morales', 'Garcia', '06789011', '1984-02-17', 'MUJER', 'SOLTERO', '+51987654504', 'claudia.morales@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle Schell 321, Lima', 'cmorales', NOW(), NOW(), 'profile_claudia.jpg', 'HABILITADO', 'ACTIVO', 'PO004', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '06789011');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Pablo', 'Herrera', 'Castillo', '07890122', '1982-07-12', 'HOMBRE', 'CASADO', '+51987654505', 'pablo.herrera@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. La Marina 654, Lima', 'pherrera', NOW(), NOW(), 'profile_pablo.jpg', 'HABILITADO', 'ACTIVO', 'PO005', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '07890122');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Mariana', 'Lopez', 'Sosa', '08901233', '1987-03-03', 'MUJER', 'SOLTERO', '+51987654506', 'mariana.lopez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. Amazonas 10, Lima', 'mlopez', NOW(), NOW(), 'profile_mariana.jpg', 'HABILITADO', 'ACTIVO', 'PO006', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '08901233');

-- ================================================
-- USUARIOS - 3 SA
-- ================================================
INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Super', 'Admin', 'Uno', '09012344', '1978-01-01', 'HOMBRE', 'CASADO', '+51987654601', 'sa.one@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Administrador 1, Lima', 'sa1', NOW(), NOW(), 'profile_sa1.jpg', 'HABILITADO', 'ACTIVO', 'SA001', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '09012344');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Super', 'Admin', 'Dos', '09123455', '1979-02-02', 'MUJER', 'CASADO', '+51987654602', 'sa.two@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Administrador 2, Lima', 'sa2', NOW(), NOW(), 'profile_sa2.jpg', 'HABILITADO', 'ACTIVO', 'SA002', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '09123455');

INSERT INTO `database`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Super', 'Admin', 'Tres', '09234566', '1980-03-03', 'HOMBRE', 'CASADO', '+51987654603', 'sa.three@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Administrador 3, Lima', 'sa3', NOW(), NOW(), 'profile_sa3.jpg', 'HABILITADO', 'ACTIVO', 'SA003', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`usuario` WHERE `dni` = '09234566');

-- ================================================
-- ASIGNAR ROLES A USUARIOS (usando subqueries)
-- ================================================
-- Asignar rol DEV a usuarios DEV (DNI 71234567 a 90123456)
INSERT INTO `database`.`usuario_has_rol` (`usuario_usuario_id`, `rol_rol_id`)
SELECT u.usuario_id, r.rol_id
FROM `database`.`usuario` u, `database`.`rol` r
WHERE u.dni BETWEEN '71234567' AND '90123456'
  AND r.nombre_rol = 'DEV'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`usuario_has_rol` ur 
    WHERE ur.usuario_usuario_id = u.usuario_id AND ur.rol_rol_id = r.rol_id
  );

-- Asignar rol QA a usuarios QA (DNI 91234567 a 02345677)
INSERT INTO `database`.`usuario_has_rol` (`usuario_usuario_id`, `rol_rol_id`)
SELECT u.usuario_id, r.rol_id
FROM `database`.`usuario` u, `database`.`rol` r
WHERE (u.dni BETWEEN '91234567' AND '99999999' OR u.dni BETWEEN '00000000' AND '02345677')
  AND r.nombre_rol = 'QA'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`usuario_has_rol` ur 
    WHERE ur.usuario_usuario_id = u.usuario_id AND ur.rol_rol_id = r.rol_id
  );

-- Asignar rol PO a usuarios PO (DNI 03456788 a 08901233)
INSERT INTO `database`.`usuario_has_rol` (`usuario_usuario_id`, `rol_rol_id`)
SELECT u.usuario_id, r.rol_id
FROM `database`.`usuario` u, `database`.`rol` r
WHERE u.dni BETWEEN '03456788' AND '08901233'
  AND r.nombre_rol = 'PO'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`usuario_has_rol` ur 
    WHERE ur.usuario_usuario_id = u.usuario_id AND ur.rol_rol_id = r.rol_id
  );

-- Asignar rol SA a usuarios SA (DNI 09012344 a 09234566)
INSERT INTO `database`.`usuario_has_rol` (`usuario_usuario_id`, `rol_rol_id`)
SELECT u.usuario_id, r.rol_id
FROM `database`.`usuario` u, `database`.`rol` r
WHERE u.dni BETWEEN '09012344' AND '09234566'
  AND r.nombre_rol = 'SA'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`usuario_has_rol` ur 
    WHERE ur.usuario_usuario_id = u.usuario_id AND ur.rol_rol_id = r.rol_id
  );

-- ================================================
-- MÉTRICAS API (PRIMERO, para resolver dependencias)
-- ================================================
INSERT INTO `database`.`metrica_api` (`fecha_hora`, `cantidad_llamadas`, `cantidad_errores`, `latencia_ms`, `costo_estimado`, `entorno`)
SELECT NOW(), 1500000, 250, 45.2, 1250.75, 'PROD'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`metrica_api` WHERE `cantidad_llamadas` = 1500000 AND `entorno` = 'PROD');

INSERT INTO `database`.`metrica_api` (`fecha_hora`, `cantidad_llamadas`, `cantidad_errores`, `latencia_ms`, `costo_estimado`, `entorno`)
SELECT NOW(), 890000, 120, 32.1, 890.50, 'PROD'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`metrica_api` WHERE `cantidad_llamadas` = 890000 AND `entorno` = 'PROD');

INSERT INTO `database`.`metrica_api` (`fecha_hora`, `cantidad_llamadas`, `cantidad_errores`, `latencia_ms`, `costo_estimado`, `entorno`)
SELECT NOW(), 2300000, 180, 28.5, 2100.25, 'PROD'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`metrica_api` WHERE `cantidad_llamadas` = 2300000 AND `entorno` = 'PROD');

INSERT INTO `database`.`metrica_api` (`fecha_hora`, `cantidad_llamadas`, `cantidad_errores`, `latencia_ms`, `costo_estimado`, `entorno`)
SELECT NOW(), 450000, 45, 65.8, 350.00, 'PROD'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`metrica_api` WHERE `cantidad_llamadas` = 450000 AND `entorno` = 'PROD');

INSERT INTO `database`.`metrica_api` (`fecha_hora`, `cantidad_llamadas`, `cantidad_errores`, `latencia_ms`, `costo_estimado`, `entorno`)
SELECT NOW(), 12000, 8, 89.3, 45.75, 'QA'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`metrica_api` WHERE `cantidad_llamadas` = 12000 AND `entorno` = 'QA');

SET FOREIGN_KEY_CHECKS = 1;

-- ================================================
-- APIS Y DATOS COMPLETOS
-- ================================================
INSERT INTO `database`.`api` (`nombre_api`, `descripcion_api`, `estado_api`, `fecha_creacion_api`)
SELECT 'Stripe Payment API', 'API para procesamiento de pagos en línea', 'PRODUCCION', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`api` WHERE `nombre_api` = 'Stripe Payment API');

INSERT INTO `database`.`api` (`nombre_api`, `descripcion_api`, `estado_api`, `fecha_creacion_api`)
SELECT 'Twilio SMS API', 'API para envío y recepción de mensajes SMS', 'PRODUCCION', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`api` WHERE `nombre_api` = 'Twilio SMS API');

INSERT INTO `database`.`api` (`nombre_api`, `descripcion_api`, `estado_api`, `fecha_creacion_api`)
SELECT 'Google Maps API', 'API de geolocalización y mapas', 'PRODUCCION', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`api` WHERE `nombre_api` = 'Google Maps API');

INSERT INTO `database`.`api` (`nombre_api`, `descripcion_api`, `estado_api`, `fecha_creacion_api`)
SELECT 'AWS S3 API', 'API para almacenamiento de objetos', 'PRODUCCION', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`api` WHERE `nombre_api` = 'AWS S3 API');

INSERT INTO `database`.`api` (`nombre_api`, `descripcion_api`, `estado_api`, `fecha_creacion_api`)
SELECT 'Auth0 Authentication', 'API de autenticación y autorización', 'QA', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`api` WHERE `nombre_api` = 'Auth0 Authentication');

-- ================================================
-- PROYECTOS
-- ================================================
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`)
SELECT 'E-commerce Platform', 'Plataforma completa de comercio electrónico', 'PRIVADO', 'RESTRINGIDO', 'EMPRESA', 'EN_DESARROLLO', '2024-01-15', '2024-06-30', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `nombre_proyecto` = 'E-commerce Platform');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`)
SELECT 'Mobile Banking App', 'Aplicación móvil de banca en línea', 'PRIVADO', 'RESTRINGIDO', 'GRUPO', 'EN_DESARROLLO', '2024-02-01', '2024-08-31', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `nombre_proyecto` = 'Mobile Banking App');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`)
SELECT 'API Management System', 'Sistema de gestión y monitoreo de APIs', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'MANTENIMIENTO', '2023-11-01', NULL, NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `nombre_proyecto` = 'API Management System');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`)
SELECT 'Payment Gateway', 'Pasarela de pagos unificada', 'PRIVADO', 'RESTRINGIDO', 'GRUPO', 'PLANEADO', '2024-03-01', '2024-09-30', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `nombre_proyecto` = 'Payment Gateway');

-- ================================================
-- REPOSITORIOS
-- ================================================
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'stripe-integration', 'Repositorio oficial de integración Stripe', 'PUBLICO', 'ACTIVO', 'PERSONAL', 
       (SELECT usuario_id FROM `database`.`usuario` WHERE username = 'cgomez' LIMIT 1), NOW(), 'main'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'stripe-integration');

INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'payment-microservice', 'Microservicio de procesamiento de pagos', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', 
       (SELECT usuario_id FROM `database`.`usuario` WHERE username = 'mrodriguez' LIMIT 1), NOW(), 'develop'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'payment-microservice');

INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'auth-service', 'Servicio de autenticación', 'PRIVADO', 'ACTIVO', 'PERSONAL', 
       (SELECT usuario_id FROM `database`.`usuario` WHERE username = 'jperez' LIMIT 1), NOW(), 'main'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'auth-service');

INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'notification-system', 'Sistema de notificaciones', 'PUBLICO', 'ACTIVO', 'COLABORATIVO', 
       (SELECT usuario_id FROM `database`.`usuario` WHERE username = 'lmartinez' LIMIT 1), NOW(), 'master'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'notification-system');

INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'api-gateway', 'Gateway principal para todas las APIs', 'PRIVADO', 'ACTIVO', 'PERSONAL', 
       (SELECT usuario_id FROM `database`.`usuario` WHERE username = 'psanchez' LIMIT 1), NOW(), 'main'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'api-gateway');

-- ================================================
-- ENLACES
-- ================================================
INSERT INTO `database`.`enlace` (`direccion_almacenamiento`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`, `repositorio_repositorio_id`)
SELECT 'https://github.com/topics/stripe-integration', NOW(), 'REPOSITORIO', r.repositorio_id, 'STORAGE', r.repositorio_id
FROM `database`.`repositorio` r
WHERE r.nombre_repositorio = 'stripe-integration'
  AND NOT EXISTS (SELECT 1 FROM `database`.`enlace` WHERE `direccion_almacenamiento` = 'https://github.com/topics/stripe-integration');

INSERT INTO `database`.`enlace` (`direccion_almacenamiento`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`, `repositorio_repositorio_id`)
SELECT 'https://github.com/rehan-adi/payment-processing-microservices', NOW(), 'REPOSITORIO', r.repositorio_id, 'STORAGE', r.repositorio_id
FROM `database`.`repositorio` r
WHERE r.nombre_repositorio = 'payment-microservice'
  AND NOT EXISTS (SELECT 1 FROM `database`.`enlace` WHERE `direccion_almacenamiento` = 'https://github.com/rehan-adi/payment-processing-microservices');

INSERT INTO `database`.`enlace` (`direccion_almacenamiento`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`, `repositorio_repositorio_id`)
SELECT 'https://github.com/company/auth-service', NOW(), 'REPOSITORIO', r.repositorio_id, 'STORAGE', r.repositorio_id
FROM `database`.`repositorio` r
WHERE r.nombre_repositorio = 'auth-service'
  AND NOT EXISTS (SELECT 1 FROM `database`.`enlace` WHERE `direccion_almacenamiento` = 'https://github.com/company/auth-service');

INSERT INTO `database`.`enlace` (`direccion_almacenamiento`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`, `repositorio_repositorio_id`)
SELECT 'https://github.com/KeyAuth/KeyAuth-Source-Code', NOW(), 'REPOSITORIO', r.repositorio_id, 'STORAGE', r.repositorio_id
FROM `database`.`repositorio` r
WHERE r.nombre_repositorio = 'notification-system'
  AND NOT EXISTS (SELECT 1 FROM `database`.`enlace` WHERE `direccion_almacenamiento` = 'https://github.com/KeyAuth/KeyAuth-Source-Code');

INSERT INTO `database`.`enlace` (`direccion_almacenamiento`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`, `repositorio_repositorio_id`)
SELECT 'https://github.com/topics/api-gateway', NOW(), 'REPOSITORIO', r.repositorio_id, 'STORAGE', r.repositorio_id
FROM `database`.`repositorio` r
WHERE r.nombre_repositorio = 'api-gateway'
  AND NOT EXISTS (SELECT 1 FROM `database`.`enlace` WHERE `direccion_almacenamiento` = 'https://github.com/topics/api-gateway');

-- Enlaces externos para documentación de APIs
INSERT INTO `database`.`enlace` (`direccion_almacenamiento`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://docs.stripe.com/?locale=es-419', NOW(), 'REPOSITORIO', r.repositorio_id, 'METADATA'
FROM `database`.`repositorio` r
WHERE r.nombre_repositorio = 'stripe-integration'
  AND NOT EXISTS (SELECT 1 FROM `database`.`enlace` WHERE `direccion_almacenamiento` = 'https://docs.stripe.com/?locale=es-419');

INSERT INTO `database`.`enlace` (`direccion_almacenamiento`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://www.twilio.com/docs', NOW(), 'REPOSITORIO', r.repositorio_id, 'METADATA'
FROM `database`.`repositorio` r
WHERE r.nombre_repositorio = 'notification-system'
  AND NOT EXISTS (SELECT 1 FROM `database`.`enlace` WHERE `direccion_almacenamiento` = 'https://www.twilio.com/docs');

INSERT INTO `database`.`enlace` (`direccion_almacenamiento`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://developers.google.com/maps/documentation', NOW(), 'REPOSITORIO', r.repositorio_id, 'METADATA'
FROM `database`.`repositorio` r
WHERE r.nombre_repositorio = 'api-gateway'
  AND NOT EXISTS (SELECT 1 FROM `database`.`enlace` WHERE `direccion_almacenamiento` = 'https://developers.google.com/maps/documentation');

INSERT INTO `database`.`enlace` (`direccion_almacenamiento`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://docs.aws.amazon.com/s3/', NOW(), 'PROYECTO', p.proyecto_id, 'METADATA'
FROM `database`.`proyecto` p
WHERE p.nombre_proyecto = 'API Management System'
  AND NOT EXISTS (SELECT 1 FROM `database`.`enlace` WHERE `direccion_almacenamiento` = 'https://docs.aws.amazon.com/s3/');

INSERT INTO `database`.`enlace` (`direccion_almacenamiento`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://auth0.com/docs', NOW(), 'PROYECTO', p.proyecto_id, 'METADATA'
FROM `database`.`proyecto` p
WHERE p.nombre_proyecto = 'Mobile Banking App'
  AND NOT EXISTS (SELECT 1 FROM `database`.`enlace` WHERE `direccion_almacenamiento` = 'https://auth0.com/docs');

-- ================================================
-- DOCUMENTACIÓN (cada una asociada a su API específica)
-- ================================================
INSERT INTO `database`.`documentacion` (`seccion_documentacion`, `api_api_id`)
SELECT 'Documentación Principal Stripe', a.api_id
FROM `database`.`api` a
WHERE a.nombre_api = 'Stripe Payment API'
  AND NOT EXISTS (SELECT 1 FROM `database`.`documentacion` WHERE `seccion_documentacion` = 'Documentación Principal Stripe');

INSERT INTO `database`.`documentacion` (`seccion_documentacion`, `api_api_id`)
SELECT 'Documentación Principal Twilio', a.api_id
FROM `database`.`api` a
WHERE a.nombre_api = 'Twilio SMS API'
  AND NOT EXISTS (SELECT 1 FROM `database`.`documentacion` WHERE `seccion_documentacion` = 'Documentación Principal Twilio');

INSERT INTO `database`.`documentacion` (`seccion_documentacion`, `api_api_id`)
SELECT 'Documentación Principal Google Maps', a.api_id
FROM `database`.`api` a
WHERE a.nombre_api = 'Google Maps API'
  AND NOT EXISTS (SELECT 1 FROM `database`.`documentacion` WHERE `seccion_documentacion` = 'Documentación Principal Google Maps');

INSERT INTO `database`.`documentacion` (`seccion_documentacion`, `api_api_id`)
SELECT 'Documentación Principal AWS S3', a.api_id
FROM `database`.`api` a
WHERE a.nombre_api = 'AWS S3 API'
  AND NOT EXISTS (SELECT 1 FROM `database`.`documentacion` WHERE `seccion_documentacion` = 'Documentación Principal AWS S3');

INSERT INTO `database`.`documentacion` (`seccion_documentacion`, `api_api_id`)
SELECT 'Documentación Principal Auth0', a.api_id
FROM `database`.`api` a
WHERE a.nombre_api = 'Auth0 Authentication'
  AND NOT EXISTS (SELECT 1 FROM `database`.`documentacion` WHERE `seccion_documentacion` = 'Documentación Principal Auth0');

-- ================================================
-- VERSIONES API (con referencias a API, métricas y documentación)
-- ================================================
INSERT INTO `database`.`version_api` (`numero_version`, `descripcion_version`, `contrato_api_url`, `fecha_lanzamiento`, `api_api_id`, `metrica_api_metrica_id`, `documentacion_documentacion_id`)
SELECT 'v1.0', 'Versión inicial estable Stripe', 'https://docs.stripe.com/?locale=es-419', '2023-01-15', 
       a.api_id, m.metrica_id, d.documentacion_id
FROM `database`.`api` a, `database`.`metrica_api` m, `database`.`documentacion` d
WHERE a.nombre_api = 'Stripe Payment API'
  AND m.cantidad_llamadas = 1500000 AND m.entorno = 'PROD'
  AND d.seccion_documentacion = 'Documentación Principal Stripe'
  AND NOT EXISTS (SELECT 1 FROM `database`.`version_api` WHERE `numero_version` = 'v1.0' AND `descripcion_version` = 'Versión inicial estable Stripe');

INSERT INTO `database`.`version_api` (`numero_version`, `descripcion_version`, `contrato_api_url`, `fecha_lanzamiento`, `api_api_id`, `metrica_api_metrica_id`, `documentacion_documentacion_id`)
SELECT 'v2.1', 'Soporte para nuevos métodos de pago', 'https://docs.stripe.com/payments/payment-methods/payment-method-support', '2023-06-20',
       a.api_id, m.metrica_id, d.documentacion_id
FROM `database`.`api` a, `database`.`metrica_api` m, `database`.`documentacion` d
WHERE a.nombre_api = 'Stripe Payment API'
  AND m.cantidad_llamadas = 890000 AND m.entorno = 'PROD'
  AND d.seccion_documentacion = 'Documentación Principal Stripe'
  AND NOT EXISTS (SELECT 1 FROM `database`.`version_api` WHERE `numero_version` = 'v2.1' AND `descripcion_version` = 'Soporte para nuevos métodos de pago');

INSERT INTO `database`.`version_api` (`numero_version`, `descripcion_version`, `contrato_api_url`, `fecha_lanzamiento`, `api_api_id`, `metrica_api_metrica_id`, `documentacion_documentacion_id`)
SELECT 'v2023-10', 'Documentos de Twilio', 'https://www.twilio.com/docs', '2023-10-01',
       a.api_id, m.metrica_id, d.documentacion_id
FROM `database`.`api` a, `database`.`metrica_api` m, `database`.`documentacion` d
WHERE a.nombre_api = 'Twilio SMS API'
  AND m.cantidad_llamadas = 2300000 AND m.entorno = 'PROD'
  AND d.seccion_documentacion = 'Documentación Principal Twilio'
  AND NOT EXISTS (SELECT 1 FROM `database`.`version_api` WHERE `numero_version` = 'v2023-10' AND `descripcion_version` = 'Documentos de Twilio');

INSERT INTO `database`.`version_api` (`numero_version`, `descripcion_version`, `contrato_api_url`, `fecha_lanzamiento`, `api_api_id`, `metrica_api_metrica_id`, `documentacion_documentacion_id`)
SELECT 'v3.0', 'Google plataforma de mapas', 'https://developers.google.com/maps/documentation', '2023-03-10',
       a.api_id, m.metrica_id, d.documentacion_id
FROM `database`.`api` a, `database`.`metrica_api` m, `database`.`documentacion` d
WHERE a.nombre_api = 'Google Maps API'
  AND m.cantidad_llamadas = 1500000 AND m.entorno = 'PROD'
  AND d.seccion_documentacion = 'Documentación Principal Google Maps'
  AND NOT EXISTS (SELECT 1 FROM `database`.`version_api` WHERE `numero_version` = 'v3.0' AND `descripcion_version` = 'Google plataforma de mapas');

INSERT INTO `database`.`version_api` (`numero_version`, `descripcion_version`, `contrato_api_url`, `fecha_lanzamiento`, `api_api_id`, `metrica_api_metrica_id`, `documentacion_documentacion_id`)
SELECT 'v2.4', 'Documentación AWS S3', 'https://docs.aws.amazon.com/s3/', '2023-08-15',
       a.api_id, m.metrica_id, d.documentacion_id
FROM `database`.`api` a, `database`.`metrica_api` m, `database`.`documentacion` d
WHERE a.nombre_api = 'AWS S3 API'
  AND m.cantidad_llamadas = 890000 AND m.entorno = 'PROD'
  AND d.seccion_documentacion = 'Documentación Principal AWS S3'
  AND NOT EXISTS (SELECT 1 FROM `database`.`version_api` WHERE `numero_version` = 'v2.4' AND `descripcion_version` = 'Documentación AWS S3');

-- ================================================
-- CONTENIDOS (con referencias a clasificación, documentación y versión API)
-- ================================================
INSERT INTO `database`.`contenido` (`titulo_contenido`, `fecha_creacion`, `clasificacion_clasificacion_id`, `documentacion_documentacion_id`, `version_api_version_id`)
SELECT 'Getting Started with Stripe', NOW(), 
       c.clasificacion_id, d.documentacion_id, v.version_id
FROM `database`.`clasificacion` c, `database`.`documentacion` d, `database`.`version_api` v
WHERE c.tipo_contenido_texto = 'Tutorial'
  AND d.seccion_documentacion = 'Documentación Principal Stripe'
  AND v.numero_version = 'v1.0' AND v.descripcion_version = 'Versión inicial estable Stripe'
  AND NOT EXISTS (SELECT 1 FROM `database`.`contenido` WHERE `titulo_contenido` = 'Getting Started with Stripe');

INSERT INTO `database`.`contenido` (`titulo_contenido`, `fecha_creacion`, `clasificacion_clasificacion_id`, `documentacion_documentacion_id`, `version_api_version_id`)
SELECT 'Payment Processing Tutorial', NOW(),
       c.clasificacion_id, d.documentacion_id, v.version_id
FROM `database`.`clasificacion` c, `database`.`documentacion` d, `database`.`version_api` v
WHERE c.tipo_contenido_texto = 'Tutorial'
  AND d.seccion_documentacion = 'Documentación Principal Stripe'
  AND v.numero_version = 'v2.1' AND v.descripcion_version = 'Soporte para nuevos métodos de pago'
  AND NOT EXISTS (SELECT 1 FROM `database`.`contenido` WHERE `titulo_contenido` = 'Payment Processing Tutorial');

INSERT INTO `database`.`contenido` (`titulo_contenido`, `fecha_creacion`, `clasificacion_clasificacion_id`, `documentacion_documentacion_id`, `version_api_version_id`)
SELECT 'API Reference Guide', NOW(),
       c.clasificacion_id, d.documentacion_id, v.version_id
FROM `database`.`clasificacion` c, `database`.`documentacion` d, `database`.`version_api` v
WHERE c.tipo_contenido_texto = 'Referencia API'
  AND d.seccion_documentacion = 'Documentación Principal Stripe'
  AND v.numero_version = 'v1.0' AND v.descripcion_version = 'Versión inicial estable Stripe'
  AND NOT EXISTS (SELECT 1 FROM `database`.`contenido` WHERE `titulo_contenido` = 'API Reference Guide');

INSERT INTO `database`.`contenido` (`titulo_contenido`, `fecha_creacion`, `clasificacion_clasificacion_id`, `documentacion_documentacion_id`, `version_api_version_id`)
SELECT 'Error Handling Best Practices', NOW(),
       c.clasificacion_id, d.documentacion_id, v.version_id
FROM `database`.`clasificacion` c, `database`.`documentacion` d, `database`.`version_api` v
WHERE c.tipo_contenido_texto = 'Mejores prácticas'
  AND d.seccion_documentacion = 'Documentación Principal Stripe'
  AND v.numero_version = 'v2.1' AND v.descripcion_version = 'Soporte para nuevos métodos de pago'
  AND NOT EXISTS (SELECT 1 FROM `database`.`contenido` WHERE `titulo_contenido` = 'Error Handling Best Practices');

INSERT INTO `database`.`contenido` (`titulo_contenido`, `fecha_creacion`, `clasificacion_clasificacion_id`, `documentacion_documentacion_id`, `version_api_version_id`)
SELECT 'Twilio SMS Quick Start', NOW(),
       c.clasificacion_id, d.documentacion_id, v.version_id
FROM `database`.`clasificacion` c, `database`.`documentacion` d, `database`.`version_api` v
WHERE c.tipo_contenido_texto = 'Guía de inicio rápido'
  AND d.seccion_documentacion = 'Documentación Principal Twilio'
  AND v.numero_version = 'v2023-10' AND v.descripcion_version = 'Documentos de Twilio'
  AND NOT EXISTS (SELECT 1 FROM `database`.`contenido` WHERE `titulo_contenido` = 'Twilio SMS Quick Start');

INSERT INTO `database`.`contenido` (`titulo_contenido`, `fecha_creacion`, `clasificacion_clasificacion_id`, `documentacion_documentacion_id`, `version_api_version_id`)
SELECT 'Google Maps Integration Guide', NOW(),
       c.clasificacion_id, d.documentacion_id, v.version_id
FROM `database`.`clasificacion` c, `database`.`documentacion` d, `database`.`version_api` v
WHERE c.tipo_contenido_texto = 'Guía de inicio rápido'
  AND d.seccion_documentacion = 'Documentación Principal Google Maps'
  AND v.numero_version = 'v3.0' AND v.descripcion_version = 'Google plataforma de mapas'
  AND NOT EXISTS (SELECT 1 FROM `database`.`contenido` WHERE `titulo_contenido` = 'Google Maps Integration Guide');

-- ================================================
-- TICKETS (con referencia a usuario reportador)
-- ================================================
INSERT INTO `database`.`ticket` (`asunto_ticket`, `cuerpo_ticket`, `fecha_creacion`, `estado_ticket`, `etapa_ticket`, `tipo_ticket`, `prioridad_ticket`, `reportado_por_usuario_id`)
SELECT 'Error en procesamiento de pagos', 'Al procesar pagos con tarjeta internacional, error 500', NOW(), 'ENVIADO', 'EN_PROGRESO', 'INCIDENCIA', 'ALTA', u.usuario_id
FROM `database`.`usuario` u
WHERE u.dni = '71234567'  -- Primer usuario DEV
  AND NOT EXISTS (SELECT 1 FROM `database`.`ticket` WHERE `asunto_ticket` = 'Error en procesamiento de pagos');

INSERT INTO `database`.`ticket` (`asunto_ticket`, `cuerpo_ticket`, `fecha_creacion`, `fecha_cierre`, `estado_ticket`, `etapa_ticket`, `tipo_ticket`, `prioridad_ticket`, `reportado_por_usuario_id`)
SELECT 'Consulta sobre documentación Stripe', 'Necesito ayuda con webhooks', NOW(), NOW(), 'ENVIADO', 'RESUELTO', 'CONSULTA', 'MEDIA', u.usuario_id
FROM `database`.`usuario` u
WHERE u.dni = '91234567'  -- Primer usuario QA
  AND NOT EXISTS (SELECT 1 FROM `database`.`ticket` WHERE `asunto_ticket` = 'Consulta sobre documentación Stripe');

INSERT INTO `database`.`ticket` (`asunto_ticket`, `cuerpo_ticket`, `fecha_creacion`, `estado_ticket`, `etapa_ticket`, `tipo_ticket`, `prioridad_ticket`, `reportado_por_usuario_id`)
SELECT 'Solicitud de nueva funcionalidad', 'Agregar soporte para Apple Pay', NOW(), 'ENVIADO', 'PENDIENTE', 'REQUERIMIENTO', 'MEDIA', u.usuario_id
FROM `database`.`usuario` u
WHERE u.dni = '03456788'  -- Primer usuario PO
  AND NOT EXISTS (SELECT 1 FROM `database`.`ticket` WHERE `asunto_ticket` = 'Solicitud de nueva funcionalidad');

INSERT INTO `database`.`ticket` (`asunto_ticket`, `cuerpo_ticket`, `fecha_creacion`, `estado_ticket`, `etapa_ticket`, `tipo_ticket`, `prioridad_ticket`, `reportado_por_usuario_id`, `asignado_a_usuario_id`)
SELECT 'Problema de autenticación', 'Tokens JWT expiran antes de tiempo', NOW(), 'RECIBIDO', 'EN_PROGRESO', 'INCIDENCIA', 'ALTA', u.usuario_id, 
       (SELECT usuario_id FROM `database`.`usuario` WHERE dni = '01234567' LIMIT 1) -- Asignar a SA
FROM `database`.`usuario` u
WHERE u.dni = '72345678'  -- Segundo usuario DEV
  AND NOT EXISTS (SELECT 1 FROM `database`.`ticket` WHERE `asunto_ticket` = 'Problema de autenticación');

-- ================================================
-- NOTIFICACIONES (con referencia a usuario destinatario)
-- ================================================
INSERT INTO `database`.`notificacion` (`tipo_notificacion`, `asunto_notificacion`, `mensaje_notificacion`, `estado_notificacion`, `inspeccion_notificacion`, `usuario_usuario_id`)
SELECT 'SISTEMA', 'Nueva actualización disponible', 'Se ha publicado la versión 2.1 de la API de Stripe', 'ENVIADA', 'NO_LEIDA', u.usuario_id
FROM `database`.`usuario` u
WHERE u.dni = '71234567'  -- Primer usuario DEV
  AND NOT EXISTS (SELECT 1 FROM `database`.`notificacion` WHERE `asunto_notificacion` = 'Nueva actualización disponible');

INSERT INTO `database`.`notificacion` (`tipo_notificacion`, `asunto_notificacion`, `mensaje_notificacion`, `estado_notificacion`, `inspeccion_notificacion`, `usuario_usuario_id`)
SELECT 'TICKET', 'Ticket asignado', 'Se te ha asignado el ticket #1 sobre error en pagos', 'ENVIADA', 'LEIDA', u.usuario_id
FROM `database`.`usuario` u
WHERE u.dni = '09012344'  -- Primer usuario SA
  AND NOT EXISTS (SELECT 1 FROM `database`.`notificacion` WHERE `asunto_notificacion` = 'Ticket asignado');

INSERT INTO `database`.`notificacion` (`tipo_notificacion`, `asunto_notificacion`, `mensaje_notificacion`, `estado_notificacion`, `inspeccion_notificacion`, `usuario_usuario_id`)
SELECT 'ALERTA', 'Alerta de rendimiento', 'La API de Google Maps muestra latencia elevada', 'ENVIADA', 'NO_LEIDA', u.usuario_id
FROM `database`.`usuario` u
WHERE u.dni = '09123455'  -- Segundo usuario SA
  AND NOT EXISTS (SELECT 1 FROM `database`.`notificacion` WHERE `asunto_notificacion` = 'Alerta de rendimiento');

INSERT INTO `database`.`notificacion` (`tipo_notificacion`, `asunto_notificacion`, `mensaje_notificacion`, `estado_notificacion`, `inspeccion_notificacion`, `usuario_usuario_id`)
SELECT 'METRICA', 'Reporte mensual', 'Resumen de métricas del mes anterior', 'ENVIADA', 'NO_LEIDA', u.usuario_id
FROM `database`.`usuario` u
WHERE u.dni = '03456788'  -- Primer usuario PO
  AND NOT EXISTS (SELECT 1 FROM `database`.`notificacion` WHERE `asunto_notificacion` = 'Reporte mensual');

-- ================================================
-- TOKENS (con referencia a usuario propietario)
-- ================================================
INSERT INTO `database`.`token` (`valor_token`, `estado_token`, `fecha_creacion_token`, `fecha_expiracion_token`, `usuario_usuario_id`)
SELECT 'abc123def456ghi789', 'ACTIVO', NOW(), DATE_ADD(NOW(), INTERVAL 1 HOUR), u.usuario_id
FROM `database`.`usuario` u
WHERE u.dni = '71234567'  -- Primer usuario DEV
  AND NOT EXISTS (SELECT 1 FROM `database`.`token` WHERE `valor_token` = 'abc123def456ghi789');

INSERT INTO `database`.`token` (`valor_token`, `estado_token`, `fecha_creacion_token`, `fecha_expiracion_token`, `usuario_usuario_id`)
SELECT 'jkl012mno345pqr678', 'ACTIVO', NOW(), DATE_ADD(NOW(), INTERVAL 1 HOUR), u.usuario_id
FROM `database`.`usuario` u
WHERE u.dni = '72345678'  -- Segundo usuario DEV
  AND NOT EXISTS (SELECT 1 FROM `database`.`token` WHERE `valor_token` = 'jkl012mno345pqr678');

INSERT INTO `database`.`token` (`valor_token`, `estado_token`, `fecha_creacion_token`, `fecha_expiracion_token`, `usuario_usuario_id`)
SELECT 'stu901vwx234yz567', 'REVOCADO', NOW(), DATE_ADD(NOW(), INTERVAL 1 HOUR), u.usuario_id
FROM `database`.`usuario` u
WHERE u.dni = '91234567'  -- Primer usuario QA
  AND NOT EXISTS (SELECT 1 FROM `database`.`token` WHERE `valor_token` = 'stu901vwx234yz567');

-- ================================================
-- HISTORIAL (con referencia a usuario que ejecuta la acción)
-- ================================================
INSERT INTO `database`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `ip_origen`, `usuario_usuario_id`)
SELECT 'LOGIN', 'Usuario', u.usuario_id, 'Inicio de sesión exitoso', NOW(), '192.168.1.100', u.usuario_id
FROM `database`.`usuario` u
WHERE u.dni = '71234567'  -- Primer usuario DEV
  AND NOT EXISTS (SELECT 1 FROM `database`.`historial` WHERE `descripcion_evento` = 'Inicio de sesión exitoso' AND `ip_origen` = '192.168.1.100');

INSERT INTO `database`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `ip_origen`, `usuario_usuario_id`)
SELECT 'MODIFICACION', 'API', a.api_id, 'Actualización de documentación de Stripe', NOW(), '10.0.0.50', u.usuario_id
FROM `database`.`usuario` u, `database`.`api` a
WHERE u.dni = '09012344'  -- Primer usuario SA
  AND a.nombre_api = 'Stripe Payment API'
  AND NOT EXISTS (SELECT 1 FROM `database`.`historial` WHERE `descripcion_evento` = 'Actualización de documentación de Stripe' AND `ip_origen` = '10.0.0.50');

INSERT INTO `database`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `ip_origen`, `usuario_usuario_id`)
SELECT 'CREACION', 'Ticket', t.ticket_id, 'Nuevo ticket creado para error en pagos', NOW(), '172.16.0.25', u.usuario_id
FROM `database`.`usuario` u, `database`.`ticket` t
WHERE u.dni = '72345678'  -- Segundo usuario DEV
  AND t.asunto_ticket = 'Error en procesamiento de pagos'
  AND NOT EXISTS (SELECT 1 FROM `database`.`historial` WHERE `descripcion_evento` = 'Nuevo ticket creado para error en pagos' AND `ip_origen` = '172.16.0.25');

-- ================================================
-- FEEDBACK (con referencia a usuario y documentación)
-- ================================================
INSERT INTO `database`.`feedback` (`comentario`, `puntuacion`, `usuario_usuario_id`, `documentacion_documentacion_id`)
SELECT 'Excelente documentación, muy clara y completa', 5.0, u.usuario_id, d.documentacion_id
FROM `database`.`usuario` u, `database`.`documentacion` d
WHERE u.dni = '71234567'  -- Primer usuario DEV
  AND d.seccion_documentacion = 'Documentación Principal Stripe'
  AND NOT EXISTS (SELECT 1 FROM `database`.`feedback` WHERE `comentario` = 'Excelente documentación, muy clara y completa');

INSERT INTO `database`.`feedback` (`comentario`, `puntuacion`, `usuario_usuario_id`, `documentacion_documentacion_id`)
SELECT 'Faltan ejemplos en Python para la versión 2.1', 3.5, u.usuario_id, d.documentacion_id
FROM `database`.`usuario` u, `database`.`documentacion` d
WHERE u.dni = '91234567'  -- Primer usuario QA
  AND d.seccion_documentacion = 'Documentación Principal Stripe'
  AND NOT EXISTS (SELECT 1 FROM `database`.`feedback` WHERE `comentario` = 'Faltan ejemplos en Python para la versión 2.1');

INSERT INTO `database`.`feedback` (`comentario`, `puntuacion`, `usuario_usuario_id`, `documentacion_documentacion_id`)
SELECT 'La guía de solución de problemas es muy útil', 4.5, u.usuario_id, d.documentacion_id
FROM `database`.`usuario` u, `database`.`documentacion` d
WHERE u.dni = '03456788'  -- Primer usuario PO
  AND d.seccion_documentacion = 'Documentación Principal Twilio'
  AND NOT EXISTS (SELECT 1 FROM `database`.`feedback` WHERE `comentario` = 'La guía de solución de problemas es muy útil');

-- ================================================
-- NODOS (ejemplos básicos)
-- ================================================
INSERT INTO `database`.`nodo` (`container_type`, `container_id`, `nombre`, `tipo`, `path`, `descripcion`, `size`, `mime_type`, `creado_en`)
SELECT 'REPOSITORIO', r.repositorio_id, '/', 'CARPETA', '/', 'Raíz del repo', 0, NULL, NOW()
FROM `database`.`repositorio` r
WHERE r.nombre_repositorio = 'stripe-integration'
  AND NOT EXISTS (SELECT 1 FROM `database`.`nodo` WHERE `path` = '/' AND `nombre` = '/' AND `container_type` = 'REPOSITORIO');

INSERT INTO `database`.`nodo` (`container_type`, `container_id`, `parent_id`, `nombre`, `tipo`, `path`, `descripcion`, `size`, `mime_type`, `creado_en`)
SELECT 'REPOSITORIO', r.repositorio_id, n.nodo_id, 'src', 'CARPETA', '/src', 'Código fuente', 0, NULL, NOW()
FROM `database`.`repositorio` r, `database`.`nodo` n
WHERE r.nombre_repositorio = 'stripe-integration'
  AND n.path = '/' AND n.container_type = 'REPOSITORIO' AND n.container_id = r.repositorio_id
  AND NOT EXISTS (SELECT 1 FROM `database`.`nodo` WHERE `path` = '/src' AND `nombre` = 'src');

INSERT INTO `database`.`nodo` (`container_type`, `container_id`, `parent_id`, `nombre`, `tipo`, `path`, `descripcion`, `size`, `mime_type`, `creado_en`)
SELECT 'REPOSITORIO', r.repositorio_id, n.nodo_id, 'README.md', 'ARCHIVO', '/src/README.md', 'Readme', 1024, 'text/markdown', NOW()
FROM `database`.`repositorio` r, `database`.`nodo` n
WHERE r.nombre_repositorio = 'stripe-integration'
  AND n.path = '/src' AND n.container_type = 'REPOSITORIO' AND n.container_id = r.repositorio_id
  AND NOT EXISTS (SELECT 1 FROM `database`.`nodo` WHERE `path` = '/src/README.md' AND `nombre` = 'README.md');

-- ================================================
-- RELACIONES PROYECTO-REPOSITORIO
-- ================================================
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'E-commerce Platform' AND r.nombre_repositorio = 'stripe-integration'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'E-commerce Platform' AND r.nombre_repositorio = 'payment-microservice'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'Mobile Banking App' AND r.nombre_repositorio = 'auth-service'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'Mobile Banking App' AND r.nombre_repositorio = 'notification-system'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'API Management System' AND r.nombre_repositorio = 'api-gateway'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

-- ================================================
-- RELACIONES REPOSITORIO-ENLACE
-- ================================================
INSERT INTO `database`.`repositorio_has_enlace` (`repositorio_repositorio_id`, `enlace_enlace_id`)
SELECT r.repositorio_id, e.enlace_id
FROM `database`.`repositorio` r, `database`.`enlace` e
WHERE r.nombre_repositorio = 'stripe-integration' 
  AND e.direccion_almacenamiento = 'https://github.com/topics/stripe-integration'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio_has_enlace` 
    WHERE `repositorio_repositorio_id` = r.repositorio_id AND `enlace_enlace_id` = e.enlace_id);

INSERT INTO `database`.`repositorio_has_enlace` (`repositorio_repositorio_id`, `enlace_enlace_id`)
SELECT r.repositorio_id, e.enlace_id
FROM `database`.`repositorio` r, `database`.`enlace` e
WHERE r.nombre_repositorio = 'payment-microservice' 
  AND e.direccion_almacenamiento = 'https://github.com/rehan-adi/payment-processing-microservices'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio_has_enlace` 
    WHERE `repositorio_repositorio_id` = r.repositorio_id AND `enlace_enlace_id` = e.enlace_id);

INSERT INTO `database`.`repositorio_has_enlace` (`repositorio_repositorio_id`, `enlace_enlace_id`)
SELECT r.repositorio_id, e.enlace_id
FROM `database`.`repositorio` r, `database`.`enlace` e
WHERE r.nombre_repositorio = 'auth-service' 
  AND e.direccion_almacenamiento = 'https://github.com/company/auth-service'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio_has_enlace` 
    WHERE `repositorio_repositorio_id` = r.repositorio_id AND `enlace_enlace_id` = e.enlace_id);

INSERT INTO `database`.`repositorio_has_enlace` (`repositorio_repositorio_id`, `enlace_enlace_id`)
SELECT r.repositorio_id, e.enlace_id
FROM `database`.`repositorio` r, `database`.`enlace` e
WHERE r.nombre_repositorio = 'notification-system' 
  AND e.direccion_almacenamiento = 'https://github.com/KeyAuth/KeyAuth-Source-Code'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio_has_enlace` 
    WHERE `repositorio_repositorio_id` = r.repositorio_id AND `enlace_enlace_id` = e.enlace_id);

INSERT INTO `database`.`repositorio_has_enlace` (`repositorio_repositorio_id`, `enlace_enlace_id`)
SELECT r.repositorio_id, e.enlace_id
FROM `database`.`repositorio` r, `database`.`enlace` e
WHERE r.nombre_repositorio = 'api-gateway' 
  AND e.direccion_almacenamiento = 'https://github.com/topics/api-gateway'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio_has_enlace` 
    WHERE `repositorio_repositorio_id` = r.repositorio_id AND `enlace_enlace_id` = e.enlace_id);

-- ================================================
-- RELACIONES VERSIÓN API-ENLACE
-- ================================================
INSERT INTO `database`.`version_api_has_enlace` (`version_api_version_id`, `enlace_enlace_id`)
SELECT v.version_id, e.enlace_id
FROM `database`.`version_api` v, `database`.`enlace` e
WHERE v.numero_version = 'v1.0' AND v.descripcion_version = 'Versión inicial estable Stripe'
  AND e.direccion_almacenamiento = 'https://docs.stripe.com/?locale=es-419'
  AND NOT EXISTS (SELECT 1 FROM `database`.`version_api_has_enlace` 
    WHERE `version_api_version_id` = v.version_id AND `enlace_enlace_id` = e.enlace_id);

INSERT INTO `database`.`version_api_has_enlace` (`version_api_version_id`, `enlace_enlace_id`)
SELECT v.version_id, e.enlace_id
FROM `database`.`version_api` v, `database`.`enlace` e
WHERE v.numero_version = 'v2.1' AND v.descripcion_version = 'Soporte para nuevos métodos de pago'
  AND e.direccion_almacenamiento = 'https://docs.stripe.com/?locale=es-419'
  AND NOT EXISTS (SELECT 1 FROM `database`.`version_api_has_enlace` 
    WHERE `version_api_version_id` = v.version_id AND `enlace_enlace_id` = e.enlace_id);

INSERT INTO `database`.`version_api_has_enlace` (`version_api_version_id`, `enlace_enlace_id`)
SELECT v.version_id, e.enlace_id
FROM `database`.`version_api` v, `database`.`enlace` e
WHERE v.numero_version = 'v2023-10' AND v.descripcion_version = 'Documentos de Twilio'
  AND e.direccion_almacenamiento = 'https://www.twilio.com/docs'
  AND NOT EXISTS (SELECT 1 FROM `database`.`version_api_has_enlace` 
    WHERE `version_api_version_id` = v.version_id AND `enlace_enlace_id` = e.enlace_id);

INSERT INTO `database`.`version_api_has_enlace` (`version_api_version_id`, `enlace_enlace_id`)
SELECT v.version_id, e.enlace_id
FROM `database`.`version_api` v, `database`.`enlace` e
WHERE v.numero_version = 'v3.0' AND v.descripcion_version = 'Google plataforma de mapas'
  AND e.direccion_almacenamiento = 'https://developers.google.com/maps/documentation'
  AND NOT EXISTS (SELECT 1 FROM `database`.`version_api_has_enlace` 
    WHERE `version_api_version_id` = v.version_id AND `enlace_enlace_id` = e.enlace_id);

-- ================================================
-- RELACIONES PROYECTO-ENLACE
-- ================================================
INSERT INTO `database`.`proyecto_has_enlace` (`proyecto_id`, `enlace_id`)
SELECT p.proyecto_id, e.enlace_id
FROM `database`.`proyecto` p, `database`.`enlace` e
WHERE p.nombre_proyecto = 'API Management System' 
  AND e.direccion_almacenamiento = 'https://docs.aws.amazon.com/s3/'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_enlace` 
    WHERE `proyecto_id` = p.proyecto_id AND `enlace_id` = e.enlace_id);

INSERT INTO `database`.`proyecto_has_enlace` (`proyecto_id`, `enlace_id`)
SELECT p.proyecto_id, e.enlace_id
FROM `database`.`proyecto` p, `database`.`enlace` e
WHERE p.nombre_proyecto = 'Mobile Banking App' 
  AND e.direccion_almacenamiento = 'https://auth0.com/docs'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_enlace` 
    WHERE `proyecto_id` = p.proyecto_id AND `enlace_id` = e.enlace_id);

-- ================================================
-- ASIGNAR USUARIOS A EQUIPOS (ejemplos)
-- ================================================
-- Backend Team (DEV users 1-5)
INSERT INTO `database`.`usuario_has_equipo` (`usuario_usuario_id`, `equipo_equipo_id`)
SELECT u.usuario_id, eq.equipo_id
FROM `database`.`usuario` u, `database`.`equipo` eq
WHERE u.dni BETWEEN '71234567' AND '75678901' 
  AND eq.nombre_equipo = 'Backend Development'
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_equipo` 
    WHERE `usuario_usuario_id` = u.usuario_id AND `equipo_equipo_id` = eq.equipo_id);

-- Frontend Team (DEV users 6-9)
INSERT INTO `database`.`usuario_has_equipo` (`usuario_usuario_id`, `equipo_equipo_id`)
SELECT u.usuario_id, eq.equipo_id
FROM `database`.`usuario` u, `database`.`equipo` eq
WHERE u.dni BETWEEN '76789012' AND '79012345' 
  AND eq.nombre_equipo = 'Frontend Development'
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_equipo` 
    WHERE `usuario_usuario_id` = u.usuario_id AND `equipo_equipo_id` = eq.equipo_id);

-- QA Team (QA users)
INSERT INTO `database`.`usuario_has_equipo` (`usuario_usuario_id`, `equipo_equipo_id`)
SELECT u.usuario_id, eq.equipo_id
FROM `database`.`usuario` u, `database`.`equipo` eq
WHERE (u.dni BETWEEN '91234567' AND '99999999' OR u.dni BETWEEN '00000000' AND '02345677')
  AND eq.nombre_equipo = 'Quality Assurance'
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_equipo` 
    WHERE `usuario_usuario_id` = u.usuario_id AND `equipo_equipo_id` = eq.equipo_id);

-- DevOps Team (DEV users 10-12)
INSERT INTO `database`.`usuario_has_equipo` (`usuario_usuario_id`, `equipo_equipo_id`)
SELECT u.usuario_id, eq.equipo_id
FROM `database`.`usuario` u, `database`.`equipo` eq
WHERE u.dni BETWEEN '80123456' AND '82345678' 
  AND eq.nombre_equipo = 'DevOps'
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_equipo` 
    WHERE `usuario_usuario_id` = u.usuario_id AND `equipo_equipo_id` = eq.equipo_id);

-- Product Management (PO users)
INSERT INTO `database`.`usuario_has_equipo` (`usuario_usuario_id`, `equipo_equipo_id`)
SELECT u.usuario_id, eq.equipo_id
FROM `database`.`usuario` u, `database`.`equipo` eq
WHERE u.dni BETWEEN '03456788' AND '08901233'
  AND eq.nombre_equipo = 'Product Management'
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_equipo` 
    WHERE `usuario_usuario_id` = u.usuario_id AND `equipo_equipo_id` = eq.equipo_id);

-- Security Team (DEV users 13-15, SA users)
INSERT INTO `database`.`usuario_has_equipo` (`usuario_usuario_id`, `equipo_equipo_id`)
SELECT u.usuario_id, eq.equipo_id
FROM `database`.`usuario` u, `database`.`equipo` eq
WHERE (u.dni BETWEEN '83456789' AND '85678901' OR u.dni BETWEEN '09012344' AND '09234566')
  AND eq.nombre_equipo = 'Security Team'
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_equipo` 
    WHERE `usuario_usuario_id` = u.usuario_id AND `equipo_equipo_id` = eq.equipo_id);

-- ================================================
-- INSERTS EXTENDIDOS PARA ROCÍO BELTRÁN Y MÁS PROYECTOS
-- Agregado: 2025-10-01
-- Propósito: Agregar muchos más proyectos, repositorios y relaciones
-- Usuario de prueba principal: rocio.beltran@company.com (QA012)
-- ================================================

-- ================================================
-- PROYECTOS PERSONALES PARA ROCÍO (Propietario: USUARIO)
-- ================================================

-- Proyecto personal 1 - Rocío como propietaria
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_id`, `created_at`, `slug`, `proyecto_key`)
SELECT 'QA Automation Framework', 'Framework automatizado de pruebas desarrollado por Rocío para mejorar la eficiencia en testing', 'PUBLICO', 'ORGANIZACION', 'USUARIO', 'EN_DESARROLLO', '2024-01-15', '2024-12-31', u.usuario_id, NOW(), 'qa-automation-framework', 'QAF-001'
FROM `database`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'qa-automation-framework');

-- Proyecto personal 2 - Rocío como propietaria
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_id`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Bug Tracking Dashboard', 'Dashboard personalizado para tracking y análisis de bugs encontrados en diferentes proyectos', 'PRIVADO', 'RESTRINGIDO', 'USUARIO', 'MANTENIMIENTO', '2023-09-01', NULL, u.usuario_id, NOW(), 'bug-tracking-dashboard', 'BTD-002'
FROM `database`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'bug-tracking-dashboard');

-- Proyecto personal 3 - Rocío como propietaria
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_id`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Test Data Generator', 'Herramienta para generar datos de prueba automáticamente para diferentes escenarios de testing', 'PUBLICO', 'CUALQUIER_PERSONA_CON_EL_ENLACE', 'USUARIO', 'PLANEADO', '2024-05-01', '2024-08-31', u.usuario_id, NOW(), 'test-data-generator', 'TDG-003'
FROM `database`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'test-data-generator');

-- Proyecto personal 4 - Rocío como propietaria
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_id`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Performance Test Suite', 'Suite completa de pruebas de rendimiento para aplicaciones web y móviles', 'PRIVADO', 'ORGANIZACION', 'USUARIO', 'EN_DESARROLLO', '2024-02-10', '2024-10-15', u.usuario_id, NOW(), 'performance-test-suite', 'PTS-004'
FROM `database`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'performance-test-suite');

-- Proyecto personal 5 - Rocío como propietaria
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_id`, `created_at`, `slug`, `proyecto_key`)
SELECT 'API Testing Toolkit', 'Conjunto de herramientas para testing automatizado de APIs REST y GraphQL', 'PUBLICO', 'ORGANIZACION', 'USUARIO', 'CERRADO', '2023-03-15', '2023-11-30', u.usuario_id, NOW(), 'api-testing-toolkit', 'ATT-005'
FROM `database`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'api-testing-toolkit');

-- ================================================
-- PROYECTOS DE EQUIPO (Propietario: GRUPO)
-- ================================================

-- Proyecto de equipo 1
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'E-Learning Platform QA', 'Proyecto de Quality Assurance para la nueva plataforma de e-learning de la empresa', 'PRIVADO', 'RESTRINGIDO', 'GRUPO', 'EN_DESARROLLO', '2024-03-01', '2024-11-30', NOW(), 'elearning-platform-qa', 'ELPQA-006'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'elearning-platform-qa');

-- Proyecto de equipo 2
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Mobile App Testing Framework', 'Framework integral para testing de aplicaciones móviles iOS y Android', 'PUBLICO', 'ORGANIZACION', 'GRUPO', 'EN_DESARROLLO', '2024-01-20', '2024-09-15', NOW(), 'mobile-app-testing-framework', 'MATF-007'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'mobile-app-testing-framework');

-- Proyecto de equipo 3
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Microservices Testing Strategy', 'Estrategia y herramientas para testing de arquitectura de microservicios', 'PRIVADO', 'RESTRINGIDO', 'GRUPO', 'PLANEADO', '2024-06-01', '2025-02-28', NOW(), 'microservices-testing-strategy', 'MTS-008'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'microservices-testing-strategy');

-- Proyecto de equipo 4
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'CI/CD Pipeline Testing', 'Implementación de testing automatizado en pipelines de CI/CD', 'PUBLICO', 'ORGANIZACION', 'GRUPO', 'MANTENIMIENTO', '2023-08-01', NULL, NOW(), 'cicd-pipeline-testing', 'CPT-009'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'cicd-pipeline-testing');

-- ================================================
-- MUCHOS MÁS PROYECTOS PÚBLICOS PARA "OTROS PROYECTOS"
-- ================================================

-- Proyectos públicos de diferentes empresas/equipos
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'React Component Library', 'Librería de componentes React reutilizables para aplicaciones empresariales', 'PUBLICO', 'CUALQUIER_PERSONA_CON_EL_ENLACE', 'EMPRESA', 'EN_DESARROLLO', '2024-01-10', '2024-07-31', NOW(), 'react-component-library', 'RCL-101'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'react-component-library');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'DevOps Monitoring Suite', 'Suite completa de monitoreo para infraestructura y aplicaciones', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'MANTENIMIENTO', '2023-05-15', NULL, NOW(), 'devops-monitoring-suite', 'DMS-102'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'devops-monitoring-suite');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Machine Learning Pipeline', 'Pipeline automatizado para entrenamiento y despliegue de modelos ML', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'EN_DESARROLLO', '2024-02-01', '2024-12-15', NOW(), 'machine-learning-pipeline', 'MLP-103'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'machine-learning-pipeline');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Blockchain Smart Contracts', 'Desarrollo de smart contracts para aplicaciones DeFi', 'PUBLICO', 'CUALQUIER_PERSONA_CON_EL_ENLACE', 'GRUPO', 'EN_DESARROLLO', '2024-03-20', '2024-11-30', NOW(), 'blockchain-smart-contracts', 'BSC-104'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'blockchain-smart-contracts');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Cloud Infrastructure as Code', 'Templates y módulos para automatización de infraestructura en la nube', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'CERRADO', '2023-01-15', '2023-10-31', NOW(), 'cloud-infrastructure-as-code', 'CIAC-105'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'cloud-infrastructure-as-code');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Cybersecurity Toolkit', 'Herramientas y scripts para auditorías de seguridad', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'MANTENIMIENTO', '2023-09-01', NULL, NOW(), 'cybersecurity-toolkit', 'CST-106'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'cybersecurity-toolkit');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Data Analytics Dashboard', 'Dashboard interactivo para análisis de datos empresariales', 'PUBLICO', 'CUALQUIER_PERSONA_CON_EL_ENLACE', 'GRUPO', 'EN_DESARROLLO', '2024-04-01', '2024-10-31', NOW(), 'data-analytics-dashboard', 'DAD-107'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'data-analytics-dashboard');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'IoT Device Management', 'Sistema de gestión para dispositivos IoT empresariales', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'PLANEADO', '2024-07-01', '2025-03-31', NOW(), 'iot-device-management', 'IDM-108'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'iot-device-management');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Microservices Architecture', 'Arquitectura de referencia para aplicaciones basadas en microservicios', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'EN_DESARROLLO', '2024-01-25', '2024-08-15', NOW(), 'microservices-architecture', 'MSA-109'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'microservices-architecture');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Progressive Web App Template', 'Template base para desarrollo de Progressive Web Apps', 'PUBLICO', 'CUALQUIER_PERSONA_CON_EL_ENLACE', 'GRUPO', 'CERRADO', '2023-06-01', '2023-12-15', NOW(), 'progressive-web-app-template', 'PWAT-110'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'progressive-web-app-template');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'API Documentation Generator', 'Herramienta para generar documentación automática de APIs', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'MANTENIMIENTO', '2023-11-01', NULL, NOW(), 'api-documentation-generator', 'ADG-111'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'api-documentation-generator');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Container Orchestration Tools', 'Herramientas para orquestación de contenedores Docker y Kubernetes', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'EN_DESARROLLO', '2024-02-15', '2024-09-30', NOW(), 'container-orchestration-tools', 'COT-112'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'container-orchestration-tools');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'E-commerce Analytics Engine', 'Motor de análisis para plataformas de comercio electrónico', 'PUBLICO', 'CUALQUIER_PERSONA_CON_EL_ENLACE', 'GRUPO', 'EN_DESARROLLO', '2024-03-10', '2024-12-20', NOW(), 'ecommerce-analytics-engine', 'EAE-113'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'ecommerce-analytics-engine');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Real-time Chat Application', 'Aplicación de chat en tiempo real con WebSocket y Redis', 'PUBLICO', 'ORGANIZACION', 'GRUPO', 'CERRADO', '2023-04-15', '2023-11-30', NOW(), 'realtime-chat-application', 'RCA-114'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'realtime-chat-application');

INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Content Management System', 'CMS modular y extensible para sitios web empresariales', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'MANTENIMIENTO', '2023-07-01', NULL, NOW(), 'content-management-system', 'CMS-115'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'content-management-system');

-- ================================================
-- REPOSITORIOS ADICIONALES PARA LOS NUEVOS PROYECTOS
-- ================================================

-- Repositorios para proyectos de Rocío
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'qa-automation-framework-repo', 'Repositorio del framework de automatización QA de Rocío', 'PUBLICO', 'ACTIVO', 'PERSONAL', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'qa-automation-framework-repo');

INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'bug-tracking-dashboard-repo', 'Repositorio del dashboard de tracking de bugs', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'bug-tracking-dashboard-repo');

INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'test-data-generator-repo', 'Repositorio del generador de datos de prueba', 'PUBLICO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'test-data-generator-repo');

-- Repositorios para proyectos públicos
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'react-components-lib', 'Librería de componentes React', 'PUBLICO', 'ACTIVO', 'PERSONAL', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'carlos.gomez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'react-components-lib');

INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'devops-monitoring-tools', 'Herramientas de monitoreo DevOps', 'PUBLICO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'maria.rodriguez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'devops-monitoring-tools');

INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'ml-pipeline-automation', 'Pipeline automatizado de Machine Learning', 'PUBLICO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'juan.perez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'ml-pipeline-automation');

-- ================================================
-- RELACIONES USUARIO-PROYECTO (Rocío con sus proyectos personales)
-- ================================================

-- Rocío como EDITOR en sus proyectos personales
INSERT INTO `database`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'EDITOR', NOW()
FROM `database`.`usuario` u, `database`.`proyecto` p
WHERE u.correo = 'rocio.beltran@company.com'
  AND p.slug IN ('qa-automation-framework', 'bug-tracking-dashboard', 'test-data-generator', 'performance-test-suite', 'api-testing-toolkit')
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

-- ================================================
-- RELACIONES EQUIPO-PROYECTO (QA Team participa en proyectos de equipo)  
-- ================================================

-- QA Team participa en proyectos de equipo con diferentes privilegios
INSERT INTO `database`.`equipo_has_proyecto` (`equipo_equipo_id`, `proyecto_proyecto_id`, `privilegio_equipo_proyecto`, `fecha_equipo_proyecto`)
SELECT eq.equipo_id, p.proyecto_id, 'EDITOR', NOW()
FROM `database`.`equipo` eq, `database`.`proyecto` p
WHERE eq.nombre_equipo = 'Quality Assurance'
  AND p.slug IN ('elearning-platform-qa', 'mobile-app-testing-framework', 'microservices-testing-strategy', 'cicd-pipeline-testing')
  AND NOT EXISTS (SELECT 1 FROM `database`.`equipo_has_proyecto` WHERE equipo_equipo_id = eq.equipo_id AND proyecto_proyecto_id = p.proyecto_id);

-- Backend Team participa en algunos proyectos públicos
INSERT INTO `database`.`equipo_has_proyecto` (`equipo_equipo_id`, `proyecto_proyecto_id`, `privilegio_equipo_proyecto`, `fecha_equipo_proyecto`)
SELECT eq.equipo_id, p.proyecto_id, 'LECTOR', NOW()
FROM `database`.`equipo` eq, `database`.`proyecto` p
WHERE eq.nombre_equipo = 'Backend Development'
  AND p.slug IN ('microservices-architecture', 'api-documentation-generator', 'container-orchestration-tools')
  AND NOT EXISTS (SELECT 1 FROM `database`.`equipo_has_proyecto` WHERE equipo_equipo_id = eq.equipo_id AND proyecto_proyecto_id = p.proyecto_id);

-- Frontend Team participa en proyectos de UI/UX
INSERT INTO `database`.`equipo_has_proyecto` (`equipo_equipo_id`, `proyecto_proyecto_id`, `privilegio_equipo_proyecto`, `fecha_equipo_proyecto`)
SELECT eq.equipo_id, p.proyecto_id, 'COMENTADOR', NOW()
FROM `database`.`equipo` eq, `database`.`proyecto` p
WHERE eq.nombre_equipo = 'Frontend Development'
  AND p.slug IN ('react-component-library', 'progressive-web-app-template', 'content-management-system')
  AND NOT EXISTS (SELECT 1 FROM `database`.`equipo_has_proyecto` WHERE equipo_equipo_id = eq.equipo_id AND proyecto_proyecto_id = p.proyecto_id);

-- ================================================
-- USUARIOS ADICIONALES PARTICIPAN EN PROYECTOS DE EQUIPO
-- ================================================

-- Algunos desarrolladores participan en proyectos de QA como colaboradores
INSERT INTO `database`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'COMENTADOR', NOW()
FROM `database`.`usuario` u, `database`.`proyecto` p
WHERE u.dni IN ('71234567', '72345678', '73456789') -- Carlos, Maria, Juan (primeros 3 DEV)
  AND p.slug IN ('elearning-platform-qa', 'mobile-app-testing-framework')
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

-- Algunos QA participan en proyectos públicos
INSERT INTO `database`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)  
SELECT u.usuario_id, p.proyecto_id, 'LECTOR', NOW()
FROM `database`.`usuario` u, `database`.`proyecto` p
WHERE u.dni IN ('91234567', '92345678', '93456789') -- Roberto, Silvia, Raul (primeros 3 QA)
  AND p.slug IN ('react-component-library', 'data-analytics-dashboard', 'cybersecurity-toolkit')
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

-- PO participan como editores en algunos proyectos estratégicos
INSERT INTO `database`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'EDITOR', NOW()
FROM `database`.`usuario` u, `database`.`proyecto` p
WHERE u.dni IN ('03456788', '04567899') -- Alejandro, Daniela (primeros 2 PO)
  AND p.slug IN ('machine-learning-pipeline', 'iot-device-management', 'ecommerce-analytics-engine')
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

-- ================================================
-- RELACIONES PROYECTO-REPOSITORIO
-- ================================================

-- Conectar proyectos de Rocío con sus repositorios
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE ((p.slug = 'qa-automation-framework' AND r.nombre_repositorio = 'qa-automation-framework-repo')
   OR (p.slug = 'bug-tracking-dashboard' AND r.nombre_repositorio = 'bug-tracking-dashboard-repo')
   OR (p.slug = 'test-data-generator' AND r.nombre_repositorio = 'test-data-generator-repo'))
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` WHERE proyecto_proyecto_id = p.proyecto_id AND repositorio_repositorio_id = r.repositorio_id);

-- Conectar proyectos públicos con repositorios
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE ((p.slug = 'react-component-library' AND r.nombre_repositorio = 'react-components-lib')
   OR (p.slug = 'devops-monitoring-suite' AND r.nombre_repositorio = 'devops-monitoring-tools')
   OR (p.slug = 'machine-learning-pipeline' AND r.nombre_repositorio = 'ml-pipeline-automation'))
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` WHERE proyecto_proyecto_id = p.proyecto_id AND repositorio_repositorio_id = r.repositorio_id);

-- ================================================
-- RELACIONES USUARIO-REPOSITORIO (Acceso directo a repos)
-- ================================================

-- Rocío tiene permisos de EDITOR en sus propios repositorios
INSERT INTO `database`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'EDITOR', NOW()
FROM `database`.`usuario` u, `database`.`repositorio` r
WHERE u.correo = 'rocio.beltran@company.com'
  AND r.nombre_repositorio IN ('qa-automation-framework-repo', 'bug-tracking-dashboard-repo', 'test-data-generator-repo')
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

-- Algunos desarrolladores tienen acceso de LECTOR a repositorios públicos
INSERT INTO `database`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'LECTOR', NOW()
FROM `database`.`usuario` u, `database`.`repositorio` r
WHERE u.dni IN ('71234567', '72345678', '73456789', '74567890', '75678901')
  AND r.nombre_repositorio IN ('react-components-lib', 'devops-monitoring-tools', 'ml-pipeline-automation')
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

-- ================================================
-- DATOS ESPECÍFICOS PARA DETAIL.HTML
-- Proyecto TelDevPortal basado en datos hardcodeados
-- ================================================

-- Insertar categoría "Desarrollo Web" si no existe
INSERT INTO `database`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Desarrollo Web', 'Proyectos de desarrollo web frontend y backend'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`categoria` WHERE `nombre_categoria` = 'Desarrollo Web');

-- Insertar proyecto principal TelDevPortal Project para Rocío
INSERT INTO `database`.`proyecto` (
    `nombre_proyecto`, 
    `descripcion_proyecto`, 
    `visibilidad_proyecto`, 
    `acceso_proyecto`, 
    `propietario_proyecto`, 
    `estado_proyecto`, 
    `fecha_inicio_proyecto`, 
    `fecha_fin_proyecto`, 
    `propietario_id`, 
    `created_at`, 
    `slug`, 
    `proyecto_key`
)
SELECT 
    'TelDevPortal Project', 
    'Portal de desarrollo para gestión de proyectos y repositorios del curso GTICS', 
    'PUBLICO', 
    'ORGANIZACION', 
    'USUARIO', 
    'EN_DESARROLLO', 
    '2024-10-01', 
    NULL, 
    u.usuario_id, 
    '2024-10-01 10:30:00', 
    'teldevportal-project', 
    'TDP-100'
FROM `database`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'teldevportal-project');

-- Relacionar proyecto TelDevPortal con categoría "Desarrollo Web"
INSERT INTO `database`.`categoria_has_proyecto` (`categoria_id_categoria`, `proyecto_proyecto_id`)
SELECT c.id_categoria, p.proyecto_id
FROM `database`.`categoria` c, `database`.`proyecto` p
WHERE c.nombre_categoria = 'Desarrollo Web'
  AND p.slug = 'teldevportal-project'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`categoria_has_proyecto` chp 
    WHERE chp.categoria_id_categoria = c.id_categoria 
    AND chp.proyecto_proyecto_id = p.proyecto_id
  );

-- Repositorio 1: backend-api (exacto del detail.html)
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 
    'backend-api', 
    'API REST del DevPortal con Spring Boot y autenticación OAuth2', 
    'PUBLICO',
    'ACTIVO', 
    'COLABORATIVO',
    u.usuario_id,
    '2024-03-20 17:30:00', 
    'develop'

FROM `database`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'backend-api');

-- Repositorio 2: frontend-web (exacto del detail.html)
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 
    'frontend-web', 
    'Interfaz web del portal con Thymeleaf y Bootstrap', 
    'PUBLICO',
    'ACTIVO', 
    'PERSONAL',
    u.usuario_id,
    '2025-09-20 14:30:00', 
    'main'
  
FROM `database`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'frontend-web');

-- Repositorio 3: database-scripts (exacto del detail.html)
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 
    'database-scripts', 
    'Scripts SQL para configuración y migración de base de datos', 
    'PUBLICO',
    'ACTIVO', 
    'COLABORATIVO',
    u.usuario_id,
    '2024-09-25 10:30:00', 
    'develop'

FROM `database`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'database-scripts');

-- Asociar repositorios con el proyecto TelDevPortal
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'teldevportal-project'
  AND r.nombre_repositorio IN ('backend-api', 'frontend-web', 'database-scripts')
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id 
    AND phr.repositorio_repositorio_id = r.repositorio_id
  );

-- Nodos/carpetas del proyecto TelDevPortal (basado en detail.html)
INSERT INTO `database`.`nodo` (`container_type`, `container_id`, `nombre`, `tipo`, `path`, `descripcion`, `creado_por`, `creado_en`)
SELECT 
    'PROYECTO', 
    p.proyecto_id, 
    'Documentación', 
    'CARPETA', 
    '/documentacion/', 
    'Documentos técnicos, manuales y guías del proyecto',
    u.usuario_id,
    '2024-10-01 10:35:00'
FROM `database`.`proyecto` p, `database`.`usuario` u
WHERE p.slug = 'teldevportal-project'
  AND u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`nodo` n 
    WHERE n.container_type = 'PROYECTO'
    AND n.container_id = p.proyecto_id 
    AND n.nombre = 'Documentación'
  );

INSERT INTO `database`.`nodo` (`container_type`, `container_id`, `nombre`, `tipo`, `path`, `descripcion`, `creado_por`, `creado_en`)
SELECT 
    'PROYECTO', 
    p.proyecto_id, 
    'Recursos', 
    'CARPETA', 
    '/recursos/', 
    'Imágenes, archivos de configuración y recursos del proyecto',
    u.usuario_id,
    '2024-10-01 10:40:00'
FROM `database`.`proyecto` p, `database`.`usuario` u
WHERE p.slug = 'teldevportal-project'
  AND u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`nodo` n 
    WHERE n.container_type = 'PROYECTO'
    AND n.container_id = p.proyecto_id 
    AND n.nombre = 'Recursos'
  );

-- Colaboradores del proyecto TelDevPortal (4 colaboradores + propietaria ya definida en proyecto.propietario_id)
-- Rocío es propietaria a través de proyecto.propietario_id, no necesita estar aquí

-- María González como EDITOR (administradora del proyecto)
INSERT INTO `database`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'EDITOR', NOW()
FROM `database`.`usuario` u, `database`.`proyecto` p
WHERE u.correo = 'maria.rodriguez@company.com'  -- María = "María González" en detail.html
  AND p.slug = 'teldevportal-project'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`usuario_has_proyecto` uhp 
    WHERE uhp.usuario_usuario_id = u.usuario_id 
    AND uhp.proyecto_proyecto_id = p.proyecto_id
  );

-- Colaboradores con privilegios de EDITOR
INSERT INTO `database`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'EDITOR', NOW()
FROM `database`.`usuario` u, `database`.`proyecto` p
WHERE u.correo IN ('carlos.gomez@company.com', 'ana.torres@company.com')  -- Carlos López, Ana López
  AND p.slug = 'teldevportal-project'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`usuario_has_proyecto` uhp 
    WHERE uhp.usuario_usuario_id = u.usuario_id 
    AND uhp.proyecto_proyecto_id = p.proyecto_id
  );

-- Pedro Silva como LECTOR
INSERT INTO `database`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'LECTOR', NOW()
FROM `database`.`usuario` u, `database`.`proyecto` p
WHERE u.correo = 'pedro.sanchez@company.com'  -- Pedro Silva
  AND p.slug = 'teldevportal-project'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`usuario_has_proyecto` uhp 
    WHERE uhp.usuario_usuario_id = u.usuario_id 
    AND uhp.proyecto_proyecto_id = p.proyecto_id
  );

-- Historial de actividad para el proyecto TelDevPortal
INSERT INTO `database`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `usuario_usuario_id`)
SELECT 
    'CREACION', 
    'PROYECTO', 
    p.proyecto_id, 
    'TelDevPortal Project fue creado por Roberto Beltrán', 
    '2024-09-25 09:00:00', 
    u.usuario_id
FROM `database`.`usuario` u, `database`.`proyecto` p
WHERE u.correo = 'rocio.beltran@company.com'
  AND p.slug = 'teldevportal-project'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`historial` h 
    WHERE h.entidad_afectada = 'PROYECTO'
    AND h.id_entidad_afectada = p.proyecto_id
    AND h.tipo_evento = 'CREACION'
  );

INSERT INTO `database`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `usuario_usuario_id`)
SELECT 
    'MODIFICACION', 
    'REPOSITORIO', 
    r.repositorio_id, 
    'Roberto Beltrán actualizó la configuración de OAuth2 en el repositorio backend-api', 
    '2024-10-01 14:30:00', 
    u.usuario_id
FROM `database`.`usuario` u, `database`.`proyecto` p, `database`.`repositorio` r
WHERE u.correo = 'rocio.beltran@company.com'
  AND p.slug = 'teldevportal-project'
  AND r.nombre_repositorio = 'backend-api'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`historial` h 
    WHERE h.entidad_afectada = 'REPOSITORIO'
    AND h.id_entidad_afectada = r.repositorio_id 
    AND h.fecha_evento = '2024-10-01 14:30:00'
  );

INSERT INTO `database`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `usuario_usuario_id`)
SELECT 
    'CREACION', 
    'REPOSITORIO', 
    r.repositorio_id, 
    'María González creó el repositorio database-scripts para almacenar los scripts de BD', 
    '2024-09-30 16:45:00', 
    u.usuario_id
FROM `database`.`usuario` u, `database`.`proyecto` p, `database`.`repositorio` r
WHERE u.correo = 'maria.rodriguez@company.com'
  AND p.slug = 'teldevportal-project'
  AND r.nombre_repositorio = 'database-scripts'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`historial` h 
    WHERE h.entidad_afectada = 'REPOSITORIO'
    AND h.id_entidad_afectada = r.repositorio_id 
    AND h.fecha_evento = '2024-09-30 16:45:00'
  );

INSERT INTO `database`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `usuario_usuario_id`)
SELECT 
    'MODIFICACION', 
    'PROYECTO', 
    p.proyecto_id, 
    'Pedro Silva fue invitado como colaborador al proyecto', 
    '2024-09-29 10:15:00', 
    u1.usuario_id
FROM `database`.`usuario` u1, `database`.`proyecto` p
WHERE u1.correo = 'pedro.sanchez@company.com'
  AND p.slug = 'teldevportal-project'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`historial` h 
    WHERE h.entidad_afectada = 'PROYECTO'
    AND h.id_entidad_afectada = p.proyecto_id 
    AND h.fecha_evento = '2024-09-29 10:15:00'
  );

INSERT INTO `database`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `usuario_usuario_id`)
SELECT 
    'CREACION', 
    'NODO', 
    n.nodo_id, 
    'Ana López creó la carpeta "Documentación" para organizar los manuales del proyecto', 
    '2024-09-28 13:20:00', 
    u.usuario_id
FROM `database`.`usuario` u, `database`.`proyecto` p, `database`.`nodo` n
WHERE u.correo = 'ana.torres@company.com'
  AND p.slug = 'teldevportal-project'
  AND n.container_type = 'PROYECTO'
  AND n.container_id = p.proyecto_id
  AND n.nombre = 'Documentación'
  AND NOT EXISTS (
    SELECT 1 FROM `database`.`historial` h 
    WHERE h.entidad_afectada = 'NODO'
    AND h.id_entidad_afectada = n.nodo_id 
    AND h.fecha_evento = '2024-09-28 13:20:00'
  );

SET FOREIGN_KEY_CHECKS = 1;

-- ================================================
-- NOTA: Script completo con datos para todas las tablas.
-- Distribución final: 3 SA, 6 PO, 12 QA, 20 DEV
-- Todos los INSERTs son idempotentes y seguros.
-- Incluye: APIs, Proyectos, Repositorios, Enlaces, 
-- Documentación, Versiones, Contenidos, Métricas,
-- Tickets, Notificaciones, Tokens, Historial, 
-- Feedback, Nodos y relaciones entre entidades.
-- ================================================
-- REPOSITORIOS ADICIONALES PARA DEMOSTRAR RELACIONES N:M COMPLEJAS
-- ================================================

-- Repositorio compartido que va a estar en múltiples proyectos
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'shared-utils-library', 'Librería de utilidades compartidas entre múltiples proyectos', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'carlos.gomez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'shared-utils-library');

-- Repositorios independientes (sin proyectos)
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'experimental-ai-features', 'Repositorio experimental con funcionalidades de IA', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'develop'
FROM `database`.`usuario` u
WHERE u.correo = 'maria.rodriguez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'experimental-ai-features');

INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'legacy-data-migration', 'Scripts de migración de datos heredados', 'PUBLICO', 'ACTIVO', 'PERSONAL', u.usuario_id, NOW(), 'master'
FROM `database`.`usuario` u
WHERE u.correo = 'juan.perez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'legacy-data-migration');

-- Repositorios para el proyecto Analytics Platform
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'analytics-data-collector', 'Colector de datos para analytics', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'laura.martinez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'analytics-data-collector');

INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'analytics-dashboard-ui', 'Dashboard UI para visualización de analytics', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'develop'
FROM `database`.`usuario` u
WHERE u.correo = 'pedro.sanchez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'analytics-dashboard-ui');

INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'analytics-reporting-service', 'Servicio de reportes analíticos', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'ana.torres@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'analytics-reporting-service');

-- Repositorios para DevOps Automation
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'kubernetes-deployment-configs', 'Configuraciones de despliegue en Kubernetes', 'PUBLICO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'miguel.castillo@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'kubernetes-deployment-configs');

INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'ci-cd-pipeline-templates', 'Templates para pipelines CI/CD', 'PUBLICO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'sofia.ramirez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'ci-cd-pipeline-templates');

-- ================================================
-- PROYECTOS ADICIONALES
-- ================================================

-- Proyecto que va a tener múltiples repositorios
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `slug`, `proyecto_key`)
SELECT 'Analytics Platform', 'Plataforma completa de análisis de datos empresariales', 'EMPRESA', 'EN_DESARROLLO', '2024-01-15', 'PRIVADO', 'ORGANIZACION', 'analytics-platform', 'AP-001'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'analytics-platform');

-- Proyecto de DevOps
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `slug`, `proyecto_key`)
SELECT 'DevOps Automation', 'Automatización de procesos DevOps y CI/CD', 'GRUPO', 'EN_DESARROLLO', '2024-02-01', 'PRIVADO', 'ORGANIZACION', 'devops-automation', 'DA-002'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'devops-automation');

-- Proyecto sin repositorios (en planificación)
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `slug`, `proyecto_key`)
SELECT 'Future AI Integration', 'Proyecto futuro para integración de IA (en planificación)', 'USUARIO', 'PLANEADO', '2024-08-01', 'PRIVADO', 'RESTRINGIDO', 'future-ai-integration', 'FAI-003'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'future-ai-integration');

-- Proyecto de Research
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `slug`, `proyecto_key`)
SELECT 'Blockchain Research', 'Investigación en tecnologías blockchain', 'GRUPO', 'EN_DESARROLLO', '2024-03-15', 'PUBLICO', 'CUALQUIER_PERSONA_CON_EL_ENLACE', 'blockchain-research', 'BR-004'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'blockchain-research');

-- ================================================
-- RELACIONES N:M COMPLEJAS PROYECTO-REPOSITORIO
-- ================================================

-- CASO 1: shared-utils-library usado en 4 proyectos diferentes
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'E-commerce Platform' AND r.nombre_repositorio = 'shared-utils-library'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'Mobile Banking App' AND r.nombre_repositorio = 'shared-utils-library'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'Analytics Platform' AND r.nombre_repositorio = 'shared-utils-library'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'DevOps Automation' AND r.nombre_repositorio = 'shared-utils-library'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

-- CASO 2: Analytics Platform con múltiples repositorios (3 repositorios)
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'Analytics Platform' AND r.nombre_repositorio = 'analytics-data-collector'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'Analytics Platform' AND r.nombre_repositorio = 'analytics-dashboard-ui'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'Analytics Platform' AND r.nombre_repositorio = 'analytics-reporting-service'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

-- CASO 3: DevOps Automation con 2 repositorios
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'DevOps Automation' AND r.nombre_repositorio = 'kubernetes-deployment-configs'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'DevOps Automation' AND r.nombre_repositorio = 'ci-cd-pipeline-templates'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

-- CASO 4: Algunos repositorios de Rocío vinculados a proyectos adicionales
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'Blockchain Research' AND r.nombre_repositorio = 'qa-automation-framework-repo'
  AND NOT EXISTS (SELECT 1 FROM `database`.`proyecto_has_repositorio` 
    WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

-- ================================================
-- RELACIONES USUARIO-REPOSITORIO PARA LOS NUEVOS REPOSITORIOS
-- ================================================

-- Rocío como propietaria de los repositorios que agregamos
INSERT INTO `database`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'EDITOR', NOW()
FROM `database`.`usuario` u, `database`.`repositorio` r
WHERE u.correo = 'rocio.beltran@company.com' 
  AND r.nombre_repositorio IN ('shared-utils-library', 'experimental-ai-features', 'legacy-data-migration')
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

-- Otros desarrolladores con acceso a algunos repositorios
INSERT INTO `database`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'EDITOR', NOW()
FROM `database`.`usuario` u, `database`.`repositorio` r
WHERE u.correo = 'carlos.gomez@company.com' 
  AND r.nombre_repositorio = 'shared-utils-library'
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

INSERT INTO `database`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'LECTOR', NOW()
FROM `database`.`usuario` u, `database`.`repositorio` r
WHERE u.correo = 'maria.rodriguez@company.com' 
  AND r.nombre_repositorio = 'analytics-data-collector'
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

-- ================================================
-- AGREGADO: Datos específicos del TelDevPortal Project
-- basados exactamente en el contenido de detail.html
-- ================================================

-- ================================================
-- CREDENCIALES DE ACCESO PARA TODAS LAS CUENTAS
-- ================================================
/*
IMPORTANTE: TODAS las cuentas de usuario usan la misma contraseña para pruebas:

CONTRASEÑA ÚNICA: DevPortal123

Ejemplos de login:
- Super Administradores: admin1, admin2, admin3 / DevPortal123  
- Product Owners: po1, po2, po3, po4, po5, po6 / DevPortal123
- QA Testers: qa1, qa2, qa3... qa12 / DevPortal123
- Desarrolladores: cgomez, mrodriguez, jperez... / DevPortal123

Hash BCrypt usado: $2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6
Este hash es compatible con Spring Security BCrypt Authentication.

VERIFICADO: El código Java usa BCrypt consistentemente
- DashboardController.java: ✅ Actualizado para usar BCrypt
- AuthController.java: ✅ Ya usaba BCrypt correctamente
- SecurityConfig.java: ✅ Proporciona BCryptPasswordEncoder
*/

-- ===============================================
-- INSERTS ADICIONALES PARA RELACIONES N:M COMPLEJAS
-- Estructura verificada con el schema de la base de datos
-- Agregado para demostrar mejor las relaciones repositorio-proyecto
-- ===============================================

-- ===============================================
-- 1. PROYECTOS ADICIONALES CON ESTRUCTURA CORRECTA
-- ===============================================

-- Analytics Platform
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `slug`, `proyecto_key`)
SELECT 'Analytics Platform', 'Plataforma completa de análisis de datos empresariales', 'EMPRESA', 'EN_DESARROLLO', '2024-01-15', 'PRIVADO', 'ORGANIZACION', 'analytics-platform', 'AP-001'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'analytics-platform');

-- Marketing Automation Suite
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `slug`, `proyecto_key`)
SELECT 'Marketing Automation Suite', 'Suite completa de automatización de marketing digital', 'EMPRESA', 'EN_DESARROLLO', '2024-02-01', '2024-12-31', 'PRIVADO', 'RESTRINGIDO', 'marketing-automation-suite', 'MAS-002'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'marketing-automation-suite');

-- Customer Relationship Management
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `slug`, `proyecto_key`)
SELECT 'Customer Relationship Management', 'Sistema avanzado de gestión de relaciones con clientes', 'GRUPO', 'EN_DESARROLLO', '2024-03-01', 'PRIVADO', 'ORGANIZACION', 'customer-relationship-management', 'CRM-003'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'customer-relationship-management');

-- Financial Data Processing
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `slug`, `proyecto_key`)
SELECT 'Financial Data Processing', 'Sistema de procesamiento y análisis de datos financieros en tiempo real', 'EMPRESA', 'MANTENIMIENTO', '2023-06-01', '2024-06-30', 'PRIVADO', 'RESTRINGIDO', 'financial-data-processing', 'FDP-004'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'financial-data-processing');

-- Supply Chain Management
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `slug`, `proyecto_key`)
SELECT 'Supply Chain Management', 'Plataforma integral de gestión de cadena de suministro', 'EMPRESA', 'PLANEADO', '2024-07-01', 'PUBLICO', 'CUALQUIER_PERSONA_CON_EL_ENLACE', 'supply-chain-management', 'SCM-005'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'supply-chain-management');

-- Human Resources Platform
INSERT INTO `database`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `slug`, `proyecto_key`)
SELECT 'Human Resources Platform', 'Plataforma completa de gestión de recursos humanos', 'GRUPO', 'EN_DESARROLLO', '2024-01-20', '2024-10-31', 'PRIVADO', 'ORGANIZACION', 'human-resources-platform', 'HRP-006'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `database`.`proyecto` WHERE `slug` = 'human-resources-platform');

-- ===============================================
-- 2. REPOSITORIOS COMPARTIDOS ADICIONALES
-- ===============================================

-- Common Authentication Service
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'common-auth-service', 'Servicio de autenticación común para todas las aplicaciones', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'diego.herrera@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'common-auth-service');

-- Database Migration Scripts
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'database-migration-scripts', 'Scripts de migración de base de datos compartidos', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'master'
FROM `database`.`usuario` u
WHERE u.correo = 'elena.morales@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'database-migration-scripts');

-- Email Service Library
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'email-service-library', 'Librería para servicios de email y notificaciones', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'ricardo.ortiz@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'email-service-library');

-- API Documentation Generator
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'api-documentation-generator', 'Generador automático de documentación de APIs', 'PUBLICO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'carmen.vega@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'api-documentation-generator');

-- Monitoring and Logging Framework
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'monitoring-logging-framework', 'Framework de monitoreo y logging para microservicios', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'develop'
FROM `database`.`usuario` u
WHERE u.correo = 'javier.rios@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'monitoring-logging-framework');

-- Data Validation Framework
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'data-validation-framework', 'Framework de validación de datos para todas las aplicaciones', 'PUBLICO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'patricia.medina@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'data-validation-framework');

-- Configuration Management System
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'config-management-system', 'Sistema centralizado de gestión de configuraciones', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'oscar.diaz@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'config-management-system');

-- Analytics Data Processor
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'analytics-data-processor', 'Procesador de datos para análisis empresarial', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'teresa.guerrero@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'analytics-data-processor');

-- Marketing Campaign Engine
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'marketing-campaign-engine', 'Motor de campañas de marketing automatizado', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'fernando.cordova@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'marketing-campaign-engine');

-- Customer Data Hub
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'customer-data-hub', 'Hub centralizado de datos de clientes', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'monica.serrano@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'customer-data-hub');

-- Financial Risk Calculator
INSERT INTO `database`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `estado_repositorio`, `tipo_repositorio`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'financial-risk-calculator', 'Calculadora de riesgo financiero', 'PRIVADO', 'ACTIVO', 'COLABORATIVO', u.usuario_id, NOW(), 'main'
FROM `database`.`usuario` u
WHERE u.correo = 'sebastian.naranjo@company.com'
  AND NOT EXISTS (SELECT 1 FROM `database`.`repositorio` WHERE `nombre_repositorio` = 'financial-risk-calculator');

-- ===============================================
-- 3. RELACIONES PROYECTO-REPOSITORIO (N:M) - CASOS COMPLEJOS
-- ===============================================

-- Analytics Platform usa múltiples repositorios (5 repositorios)
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'analytics-platform' AND r.nombre_repositorio = 'shared-utils-library'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'analytics-platform' AND r.nombre_repositorio = 'common-auth-service'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'analytics-platform' AND r.nombre_repositorio = 'database-migration-scripts'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'analytics-platform' AND r.nombre_repositorio = 'monitoring-logging-framework'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'analytics-platform' AND r.nombre_repositorio = 'analytics-data-processor'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

-- Marketing Automation Suite usa repositorios compartidos (4 repositorios)
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'marketing-automation-suite' AND r.nombre_repositorio = 'shared-utils-library'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'marketing-automation-suite' AND r.nombre_repositorio = 'email-service-library'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'marketing-automation-suite' AND r.nombre_repositorio = 'common-auth-service'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'marketing-automation-suite' AND r.nombre_repositorio = 'marketing-campaign-engine'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

-- Customer Relationship Management (4 repositorios)
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'customer-relationship-management' AND r.nombre_repositorio = 'shared-utils-library'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'customer-relationship-management' AND r.nombre_repositorio = 'data-validation-framework'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'customer-relationship-management' AND r.nombre_repositorio = 'email-service-library'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'customer-relationship-management' AND r.nombre_repositorio = 'customer-data-hub'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

-- Financial Data Processing (4 repositorios)
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'financial-data-processing' AND r.nombre_repositorio = 'legacy-data-migration'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'financial-data-processing' AND r.nombre_repositorio = 'database-migration-scripts'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'financial-data-processing' AND r.nombre_repositorio = 'data-validation-framework'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'financial-data-processing' AND r.nombre_repositorio = 'financial-risk-calculator'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

-- Supply Chain Management (3 repositorios)
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'supply-chain-management' AND r.nombre_repositorio = 'shared-utils-library'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'supply-chain-management' AND r.nombre_repositorio = 'kubernetes-deployment-configs'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'supply-chain-management' AND r.nombre_repositorio = 'ci-cd-pipeline-templates'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

-- Human Resources Platform (3 repositroios)
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'human-resources-platform' AND r.nombre_repositorio = 'common-auth-service'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'human-resources-platform' AND r.nombre_repositorio = 'email-service-library'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.slug = 'human-resources-platform' AND r.nombre_repositorio = 'config-management-system'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

-- ===============================================
-- 4. VINCULAR REPOSITORIOS COMPARTIDOS CON PROYECTOS EXISTENTES
-- ===============================================

-- Vincular shared-utils-library con proyectos existentes (repositorio MÁS USADO)
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'E-commerce Platform' AND r.nombre_repositorio = 'shared-utils-library'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'API Management System' AND r.nombre_repositorio = 'shared-utils-library'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'Mobile Banking App' AND r.nombre_repositorio = 'shared-utils-library'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

-- Vincular API Documentation Generator con proyectos que manejan APIs
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'API Management System' AND r.nombre_repositorio = 'api-documentation-generator'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

-- Vincular common-auth-service con múltiples proyectos que necesitan autenticación
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'Mobile Banking App' AND r.nombre_repositorio = 'common-auth-service'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'Payment Gateway' AND r.nombre_repositorio = 'common-auth-service'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'E-commerce Platform' AND r.nombre_repositorio = 'common-auth-service'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

-- Vincular email-service-library con proyectos que envían notificaciones
INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'E-commerce Platform' AND r.nombre_repositorio = 'email-service-library'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `database`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `database`.`proyecto` p, `database`.`repositorio` r
WHERE p.nombre_proyecto = 'Mobile Banking App' AND r.nombre_repositorio = 'email-service-library'
AND NOT EXISTS (
    SELECT 1 FROM `database`.`proyecto_has_repositorio` phr 
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

-- ===============================================
-- 5. ASIGNACIONES DE USUARIOS A REPOSITORIOS (CON COLUMNAS CORRECTAS)
-- ===============================================

-- Rocío como editora de los repositorios principales compartidos
INSERT INTO `database`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'EDITOR', NOW()
FROM `database`.`usuario` u, `database`.`repositorio` r
WHERE u.correo = 'rocio.beltran@company.com' 
  AND r.nombre_repositorio IN ('common-auth-service', 'email-service-library', 'api-documentation-generator')
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

-- Carlos como comentador de repositorios de infraestructura
INSERT INTO `database`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'EDITOR', NOW()
FROM `database`.`usuario` u, `database`.`repositorio` r
WHERE u.correo = 'carlos.gomez@company.com' 
  AND r.nombre_repositorio IN ('monitoring-logging-framework', 'config-management-system')
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

-- María como lectora de repositorios de datos
INSERT INTO `database`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'LECTOR', NOW()
FROM `database`.`usuario` u, `database`.`repositorio` r
WHERE u.correo = 'maria.rodriguez@company.com' 
  AND r.nombre_repositorio IN ('data-validation-framework', 'analytics-data-processor', 'customer-data-hub')
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

-- Asignar usuarios a proyectos nuevos
INSERT INTO `database`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'EDITOR', NOW()
FROM `database`.`usuario` u, `database`.`proyecto` p
WHERE u.correo = 'rocio.beltran@company.com' 
  AND p.slug IN ('analytics-platform', 'marketing-automation-suite')
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

INSERT INTO `database`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'COMENTADOR', NOW()
FROM `database`.`usuario` u, `database`.`proyecto` p
WHERE u.correo = 'carlos.gomez@company.com' 
  AND p.slug IN ('customer-relationship-management', 'financial-data-processing')
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

INSERT INTO `database`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'LECTOR', NOW()
FROM `database`.`usuario` u, `database`.`proyecto` p
WHERE u.correo = 'maria.rodriguez@company.com' 
  AND p.slug IN ('supply-chain-management', 'human-resources-platform')
  AND NOT EXISTS (SELECT 1 FROM `database`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

-- ===============================================
-- RESUMEN DE DATOS AGREGADOS:
-- - 6 proyectos nuevos con diferentes propietarios y estados
-- - 11 repositorios compartidos adicionales  
-- - 23+ relaciones proyecto-repositorio que demuestran N:M
-- - shared-utils-library: usado en 9+ proyectos (repositorio más popular)
-- - common-auth-service: usado en 7+ proyectos
-- - email-service-library: usado en 5+ proyectos
-- - Asignaciones de usuarios con diferentes privilegios
-- ===============================================

-- =====================================================================
-- LIMPIEZA: Convertir privilegios COMENTADOR a LECTOR en repositorios
-- (Ya que eliminamos COMENTADOR del sistema de repositorios)
-- =====================================================================

-- Desactiva temporalmente safe mode para estas actualizaciones masivas
SET SQL_SAFE_UPDATES = 0;

-- Actualizar usuarios con privilegio COMENTADOR en repositorios a LECTOR
UPDATE `database`.`usuario_has_repositorio` 
SET `privilegio_usuario_repositorio` = 'LECTOR' 
WHERE `privilegio_usuario_repositorio` = 'COMENTADOR';

-- Actualizar equipos con privilegio COMENTADOR en repositorios a LECTOR  
UPDATE `database`.`equipo_has_repositorio` 
SET `privilegio_equipo_repositorio` = 'LECTOR' 
WHERE `privilegio_equipo_repositorio` = 'COMENTADOR';

-- Reactiva safe mode
SET SQL_SAFE_UPDATES = 1;

SET FOREIGN_KEY_CHECKS = 1;
