-- Script de INSERTs adaptado para official_dev_portal.sql
-- Creado: 2025-10-23 (v2 - Corregido size -> size_bytes)
-- DescripciÃ³n: Inserts idempotentes adaptados a la nueva estructura.
-- Se eliminaron inserts para tablas obsoletas (metrica_api, notificacion, etc.)
-- Se ajustaron columnas renombradas (fecha_creacion_api -> creado_en)
-- Se aÃ±adieron columnas requeridas (nombre_archivo en enlace)
-- Corregido nombre de columna size a size_bytes en tabla nodo.

SET FOREIGN_KEY_CHECKS = 0;

-- ================================================
-- ROLES
-- ================================================
INSERT INTO `dev_portal_sql`.`rol` (`nombre_rol`, `descripcion_rol`, `activo`)
SELECT 'DEV', 'Desarrollador', TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`rol` WHERE `nombre_rol` = 'DEV');

INSERT INTO `dev_portal_sql`.`rol` (`nombre_rol`, `descripcion_rol`, `activo`)
SELECT 'QA', 'Quality Assurance', TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`rol` WHERE `nombre_rol` = 'QA');

INSERT INTO `dev_portal_sql`.`rol` (`nombre_rol`, `descripcion_rol`, `activo`)
SELECT 'PO', 'Product Owner', TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`rol` WHERE `nombre_rol` = 'PO');

INSERT INTO `dev_portal_sql`.`rol` (`nombre_rol`, `descripcion_rol`, `activo`)
SELECT 'SA', 'Super Administrator', TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`rol` WHERE `nombre_rol` = 'SA');

-- ================================================
-- CATEGORÃAS
-- ================================================
INSERT INTO `dev_portal_sql`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Pagos', 'Funcionalidades relacionadas con pagos'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`categoria` WHERE `nombre_categoria` = 'Pagos');

INSERT INTO `dev_portal_sql`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Clientes', 'GestiÃ³n de clientes'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`categoria` WHERE `nombre_categoria` = 'Clientes');

INSERT INTO `dev_portal_sql`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'LogÃ­stica', 'LogÃ­stica y envÃ­os'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`categoria` WHERE `nombre_categoria` = 'LogÃ­stica');

INSERT INTO `dev_portal_sql`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'AutenticaciÃ³n', 'AutenticaciÃ³n y autorizaciÃ³n'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`categoria` WHERE `nombre_categoria` = 'AutenticaciÃ³n');

INSERT INTO `dev_portal_sql`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Notificaciones', 'Sistemas de notificaciones'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`categoria` WHERE `nombre_categoria` = 'Notificaciones');

INSERT INTO `dev_portal_sql`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'GeolocalizaciÃ³n', 'APIs de mapas y geo'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`categoria` WHERE `nombre_categoria` = 'GeolocalizaciÃ³n');

INSERT INTO `dev_portal_sql`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'AnalÃ­tica', 'MÃ©tricas y analÃ­tica'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`categoria` WHERE `nombre_categoria` = 'AnalÃ­tica');

INSERT INTO `dev_portal_sql`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Almacenamiento', 'Almacenamiento de objetos'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`categoria` WHERE `nombre_categoria` = 'Almacenamiento');

-- ================================================
-- ETIQUETAS
-- ================================================
INSERT INTO `dev_portal_sql`.`etiqueta` (`nombre_tag`)
SELECT 'REST' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`etiqueta` WHERE `nombre_tag` = 'REST');

INSERT INTO `dev_portal_sql`.`etiqueta` (`nombre_tag`)
SELECT 'GraphQL' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`etiqueta` WHERE `nombre_tag` = 'GraphQL');

INSERT INTO `dev_portal_sql`.`etiqueta` (`nombre_tag`)
SELECT 'Eventos' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`etiqueta` WHERE `nombre_tag` = 'Eventos');

INSERT INTO `dev_portal_sql`.`etiqueta` (`nombre_tag`)
SELECT 'WebSocket' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`etiqueta` WHERE `nombre_tag` = 'WebSocket');

INSERT INTO `dev_portal_sql`.`etiqueta` (`nombre_tag`)
SELECT 'JSON' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`etiqueta` WHERE `nombre_tag` = 'JSON');

INSERT INTO `dev_portal_sql`.`etiqueta` (`nombre_tag`)
SELECT 'XML' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`etiqueta` WHERE `nombre_tag` = 'XML');

INSERT INTO `dev_portal_sql`.`etiqueta` (`nombre_tag`)
SELECT 'Seguridad' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`etiqueta` WHERE `nombre_tag` = 'Seguridad');

INSERT INTO `dev_portal_sql`.`etiqueta` (`nombre_tag`)
SELECT 'Microservicios' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`etiqueta` WHERE `nombre_tag` = 'Microservicios');

INSERT INTO `dev_portal_sql`.`etiqueta` (`nombre_tag`)
SELECT 'Cloud' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`etiqueta` WHERE `nombre_tag` = 'Cloud');

INSERT INTO `dev_portal_sql`.`etiqueta` (`nombre_tag`)
SELECT 'Mobile' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`etiqueta` WHERE `nombre_tag` = 'Mobile');

-- ================================================
-- CLASIFICACIONES
-- ================================================
-- IMPORTANTE: Estos valores deben coincidir exactamente con los usados en:
-- 1. Frontend: templates/api/create-api.html (opciones del select de tipo de contenido)
-- 2. Backend: SeccionCmsDTO.tipoContenido (valores enviados desde el frontend)
-- ================================================
INSERT INTO `dev_portal_sql`.`clasificacion` (`tipo_contenido_texto`)
SELECT 'GUIA' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`clasificacion` WHERE `tipo_contenido_texto` = 'GUIA');

INSERT INTO `dev_portal_sql`.`clasificacion` (`tipo_contenido_texto`)
SELECT 'TUTORIAL' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`clasificacion` WHERE `tipo_contenido_texto` = 'TUTORIAL');

INSERT INTO `dev_portal_sql`.`clasificacion` (`tipo_contenido_texto`)
SELECT 'VIDEO' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`clasificacion` WHERE `tipo_contenido_texto` = 'VIDEO');

INSERT INTO `dev_portal_sql`.`clasificacion` (`tipo_contenido_texto`)
SELECT 'SNIPPET' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`clasificacion` WHERE `tipo_contenido_texto` = 'SNIPPET');

INSERT INTO `dev_portal_sql`.`clasificacion` (`tipo_contenido_texto`)
SELECT 'OTRO' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`clasificacion` WHERE `tipo_contenido_texto` = 'OTRO');

-- ================================================
-- EQUIPOS (Asociados Ãºnicamente a Proyectos o Repositorios)
-- NOTA: No se crean equipos base genÃ©ricos sin asociaciÃ³n
-- Los equipos se crean solo cuando se asocian a proyectos/repositorios
-- ================================================
-- EQUIPOS INICIALES SERÃN CREADOS DINÃMICAMENTE EN LA UI
-- NO insertamos equipos sin contexto (proyecto/repositorio)

-- ================================================
-- USUARIOS
-- ================================================
-- ... (Inserts de usuarios sin cambios) ...
-- ================================================
-- USUARIOS - 20 DEV
-- ================================================
INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Carlos', 'Gomez', 'Lopez', '71234567', '1990-05-15', 'HOMBRE', 'SOLTERO', '987654321', 'carlos.gomez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Siempre Viva 123, Lima', 'cgomez', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV001', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '71234567');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Maria', 'Rodriguez', 'Santos', '72345678', '1992-08-22', 'MUJER', 'CASADO', '987654322', 'maria.rodriguez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. Union 456, Lima', 'mrodriguez', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV002', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '72345678');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Juan', 'Perez', 'Garcia', '73456789', '1988-12-10', 'HOMBRE', 'CASADO', '987654323', 'juan.perez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Arequipa 789, Lima', 'jperez', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV003', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '73456789');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Laura', 'Martinez', 'Diaz', '74567890', '1991-03-25', 'MUJER', 'SOLTERO', '987654324', 'laura.martinez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle Las Flores 321, Lima', 'lmartinez', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV004', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '74567890');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Pedro', 'Sanchez', 'Vargas', '75678901', '1989-07-18', 'HOMBRE', 'SOLTERO', '987654325', 'pedro.sanchez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Javier Prado 654, Lima', 'psanchez', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV005', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '75678901');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Ana', 'Torres', 'Rojas', '76789012', '1993-11-05', 'MUJER', 'CASADO', '987654326', 'ana.torres@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Urb. Los PrÃ³ceres 987, Lima', 'atorres', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV006', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '76789012');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Miguel', 'Castillo', 'Flores', '77890123', '1990-02-14', 'HOMBRE', 'SOLTERO', '987654327', 'miguel.castillo@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. La Marina 147, Lima', 'mcastillo', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV007', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '77890123');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Sofia', 'Ramirez', 'Mendoza', '78901234', '1994-06-30', 'MUJER', 'SOLTERO', '987654328', 'sofia.ramirez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. de la UniÃ³n 258, Lima', 'sramirez', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV008', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '78901234');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Diego', 'Herrera', 'Castro', '79012345', '1987-09-12', 'HOMBRE', 'CASADO', '987654329', 'diego.herrera@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Brasil 369, Lima', 'dherrera', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV009', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '79012345');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Elena', 'Morales', 'Silva', '80123456', '1992-04-08', 'MUJER', 'SOLTERO', '987654330', 'elena.morales@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle Schell 753, Lima', 'emorales', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV010', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '80123456');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Ricardo', 'Ortiz', 'Chavez', '81234567', '1986-01-20', 'HOMBRE', 'CASADO', '987654331', 'ricardo.ortiz@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Angamos 951, Lima', 'rortiz', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV011', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '81234567');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Carmen', 'Vega', 'Paredes', '82345678', '1995-08-15', 'MUJER', 'SOLTERO', '987654332', 'carmen.vega@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. Carabaya 357, Lima', 'cvega', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV012', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '82345678');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Javier', 'Rios', 'Cabrera', '83456789', '1989-12-03', 'HOMBRE', 'SOLTERO', '987654333', 'javier.rios@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Tacna 159, Lima', 'jrios', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV013', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '83456789');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Patricia', 'Medina', 'Leon', '84567890', '1991-07-22', 'MUJER', 'CASADO', '987654334', 'patricia.medina@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Urb. San Felipe 486, Lima', 'pmedina', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV014', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '84567890');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Oscar', 'Diaz', 'Romero', '85678901', '1988-03-17', 'HOMBRE', 'SOLTERO', '987654335', 'oscar.diaz@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Petit Thouars 642, Lima', 'odiaz', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV015', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '85678901');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Teresa', 'Guerrero', 'Suarez', '86789012', '1993-10-28', 'MUJER', 'SOLTERO', '987654336', 'teresa.guerrero@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle Montevideo 825, Lima', 'tguerrero', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV016', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '86789012');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Fernando', 'Cordova', 'Aguilar', '87890123', '1990-05-11', 'HOMBRE', 'CASADO', '987654337', 'fernando.cordova@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Universitaria 741, Lima', 'fcordova', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV017', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '87890123');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Gabriela', 'Paredes', 'Torres', '88901234', '1994-02-09', 'MUJER', 'SOLTERO', '987654338', 'gabriela.paredes@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. Huallaga 963, Lima', 'gparedes', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV018', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '88901234');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Hector', 'Salazar', 'Mendez', '89012345', '1987-11-14', 'HOMBRE', 'SOLTERO', '987654339', 'hector.salazar@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Canada 528, Lima', 'hsalazar', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV019', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '89012345');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Lucia', 'Valdivia', 'Reyes', '90123456', '1992-06-25', 'MUJER', 'CASADO', '987654340', 'lucia.valdivia@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle BolÃ­var 417, Lima', 'lvaldivia', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'DEV020', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '90123456');

-- ================================================
-- USUARIOS - 12 QA
-- ================================================
INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Roberto', 'Mendoza', 'Castro', '91234567', '1985-04-18', 'HOMBRE', 'CASADO', '987654401', 'roberto.mendoza@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Salaverry 123, Lima', 'rmendoza', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'QA001', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '91234567');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Silvia', 'Quispe', 'Lopez', '92345678', '1989-09-22', 'MUJER', 'SOLTERO', '987654402', 'silvia.quispe@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. AzÃ¡ngaro 456, Lima', 'squispe', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'QA002', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '92345678');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Raul', 'Gonzales', 'Vera', '93456789', '1990-12-15', 'HOMBRE', 'SOLTERO', '987654403', 'raul.gonzales@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Alfonso Ugarte 789, Lima', 'rgonzales', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'QA003', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '93456789');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Veronica', 'Castro', 'Rios', '94567890', '1987-07-30', 'MUJER', 'CASADO', '987654404', 'veronica.castro@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle Roma 321, Lima', 'vcastro', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'QA004', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '94567890');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Mario', 'Torres', 'Diaz', '95678901', '1991-03-08', 'HOMBRE', 'SOLTERO', '987654405', 'mario.torres@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Petit Thouars 654, Lima', 'mtorres', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'QA005', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '95678901');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Fernando', 'Cruz', 'Lopez', '96789011', '1986-10-10', 'HOMBRE', 'CASADO', '987654406', 'fernando.cruz@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Arenales 111, Lima', 'fcruz', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'QA006', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '96789011');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Yolanda', 'Sierra', 'Paz', '97890122', '1988-02-20', 'MUJER', 'SOLTERO', '987654407', 'yolanda.sierra@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. Lampa 45, Lima', 'ysierra', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'QA007', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '97890122');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Diego', 'Mora', 'Quispe', '98901233', '1992-09-01', 'HOMBRE', 'SOLTERO', '987654408', 'diego.mora@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle Dos de Mayo 12, Lima', 'dmora', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'QA008', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '98901233');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Lucero', 'Vargas', 'Pinto', '99012344', '1991-12-12', 'MUJER', 'CASADO', '987654409', 'lucero.vargas@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Salaverry 222, Lima', 'lvargas', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'QA009', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '99012344');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Sergio', 'Alonso', 'Ramos', '00123455', '1984-06-06', 'HOMBRE', 'CASADO', '987654410', 'sergio.alonso@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. Puno 50, Lima', 'salonso', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'QA010', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '00123455');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Paty', 'Nunez', 'Lopez', '01234566', '1993-05-05', 'MUJER', 'SOLTERO', '987654411', 'paty.nunez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle San Martin 66, Lima', 'pnunez', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'QA011', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '01234566');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Rocio', 'Beltran', 'Cano', '02345677', '1990-11-11', 'MUJER', 'SOLTERO', '987654412', 'rocio.beltran@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Javier Prado 77, Lima', 'rbeltran', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'QA012', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '02345677');

-- ================================================
-- USUARIOS - 6 PO
-- ================================================
INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Alejandro', 'Vargas', 'Santos', '03456788', '1983-05-20', 'HOMBRE', 'CASADO', '987654501', 'alejandro.vargas@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Benavides 123, Lima', 'avargas', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'PO001', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '03456788');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Daniela', 'Rojas', 'Mendoza', '04567899', '1986-08-14', 'MUJER', 'SOLTERO', '987654502', 'daniela.rojas@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. de la UniÃ³n 456, Lima', 'drojas', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'PO002', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '04567899');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Arturo', 'Silva', 'Perez', '05678900', '1980-11-25', 'HOMBRE', 'CASADO', '987654503', 'arturo.silva@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Javier Prado 789, Lima', 'asilva', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'PO003', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '05678900');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Claudia', 'Morales', 'Garcia', '06789011', '1984-02-17', 'MUJER', 'SOLTERO', '987654504', 'claudia.morales@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Calle Schell 321, Lima', 'cmorales', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'PO004', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '06789011');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Pablo', 'Herrera', 'Castillo', '07890122', '1982-07-12', 'HOMBRE', 'CASADO', '987654505', 'pablo.herrera@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. La Marina 654, Lima', 'pherrera', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'PO005', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '07890122');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Mariana', 'Lopez', 'Sosa', '08901233', '1987-03-03', 'MUJER', 'SOLTERO', '987654506', 'mariana.lopez@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Jr. Amazonas 10, Lima', 'mlopez', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'PO006', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '08901233');

-- ================================================
-- USUARIOS - 3 SA
-- ================================================
INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Super', 'Admin', 'Uno', '09012344', '1978-01-01', 'HOMBRE', 'CASADO', '987654601', 'sa.one@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Administrador 1, Lima', 'sa1', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'SA001', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '09012344');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Super', 'Admin', 'Dos', '09123455', '1979-02-02', 'MUJER', 'CASADO', '987654602', 'sa.two@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Administrador 2, Lima', 'sa2', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'SA002', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '09123455');

INSERT INTO `dev_portal_sql`.`usuario` (`nombre_usuario`, `apellido_paterno`, `apellido_materno`, `dni`, `fecha_nacimiento`, `sexo_usuario`, `estado_civil`, `telefono`, `correo`, `hashed_password`, `proveedor`, `direccion_usuario`, `username`, `fecha_creacion`, `ultima_conexion`, `foto_perfil`, `estado_usuario`, `actividad_usuario`, `codigo_usuario`, `acceso_usuario`)
SELECT 'Super', 'Admin', 'Tres', '09234566', '1980-03-03', 'HOMBRE', 'CASADO', '987654603', 'sa.three@company.com', '$2a$12$838VcKNzXcSOAu.DiGjsjeU7E1L.fBm3EFRyNDklQilj47SsuF/I6', 'local', 'Av. Administrador 3, Lima', 'sa3', NOW(), NOW(), '/img/default-avatar.png', 'HABILITADO', 'ACTIVO', 'SA003', 'SI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario` WHERE `dni` = '09234566');

-- ================================================
-- ASIGNAR ROLES A USUARIOS
-- ================================================
INSERT INTO `dev_portal_sql`.`usuario_has_rol` (`usuario_usuario_id`, `rol_rol_id`)
SELECT u.usuario_id, r.rol_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`rol` r
WHERE u.dni BETWEEN '71234567' AND '90123456'
  AND r.nombre_rol = 'DEV'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`usuario_has_rol` ur
    WHERE ur.usuario_usuario_id = u.usuario_id AND ur.rol_rol_id = r.rol_id
);

INSERT INTO `dev_portal_sql`.`usuario_has_rol` (`usuario_usuario_id`, `rol_rol_id`)
SELECT u.usuario_id, r.rol_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`rol` r
WHERE (u.dni BETWEEN '91234567' AND '99999999' OR u.dni BETWEEN '00000000' AND '02345677')
  AND r.nombre_rol = 'QA'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`usuario_has_rol` ur
    WHERE ur.usuario_usuario_id = u.usuario_id AND ur.rol_rol_id = r.rol_id
);

INSERT INTO `dev_portal_sql`.`usuario_has_rol` (`usuario_usuario_id`, `rol_rol_id`)
SELECT u.usuario_id, r.rol_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`rol` r
WHERE u.dni BETWEEN '03456788' AND '08901233'
  AND r.nombre_rol = 'PO'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`usuario_has_rol` ur
    WHERE ur.usuario_usuario_id = u.usuario_id AND ur.rol_rol_id = r.rol_id
);

INSERT INTO `dev_portal_sql`.`usuario_has_rol` (`usuario_usuario_id`, `rol_rol_id`)
SELECT u.usuario_id, r.rol_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`rol` r
WHERE u.dni BETWEEN '09012344' AND '09234566'
  AND r.nombre_rol = 'SA'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`usuario_has_rol` ur
    WHERE ur.usuario_usuario_id = u.usuario_id AND ur.rol_rol_id = r.rol_id
);


SET FOREIGN_KEY_CHECKS = 1;

-- ================================================
-- APIS Y DATOS COMPLETOS
-- ================================================
INSERT INTO `dev_portal_sql`.`api` (`nombre_api`, `descripcion_api`, `estado_api`, `creado_en`)
SELECT 'Stripe Payment API', 'API para procesamiento de pagos en lÃ­nea', 'PRODUCCION', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`api` WHERE `nombre_api` = 'Stripe Payment API');

INSERT INTO `dev_portal_sql`.`api` (`nombre_api`, `descripcion_api`, `estado_api`, `creado_en`)
SELECT 'Twilio SMS API', 'API para envÃ­o y recepciÃ³n de mensajes SMS', 'PRODUCCION', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`api` WHERE `nombre_api` = 'Twilio SMS API');

INSERT INTO `dev_portal_sql`.`api` (`nombre_api`, `descripcion_api`, `estado_api`, `creado_en`)
SELECT 'Google Maps API', 'API de geolocalizaciÃ³n y mapas', 'PRODUCCION', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`api` WHERE `nombre_api` = 'Google Maps API');

INSERT INTO `dev_portal_sql`.`api` (`nombre_api`, `descripcion_api`, `estado_api`, `creado_en`)
SELECT 'AWS S3 API', 'API para almacenamiento de objetos', 'PRODUCCION', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`api` WHERE `nombre_api` = 'AWS S3 API');

INSERT INTO `dev_portal_sql`.`api` (`nombre_api`, `descripcion_api`, `estado_api`, `creado_en`)
SELECT 'Auth0 Authentication', 'API de autenticaciÃ³n y autorizaciÃ³n', 'QA', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`api` WHERE `nombre_api` = 'Auth0 Authentication');

-- ================================================
-- EQUIPOS (Propietarios de Repositorios Colaborativos)
-- ================================================
INSERT INTO `dev_portal_sql`.`equipo` (`nombre_equipo`, `fecha_creacion`)
SELECT 'Quality Assurance Team', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`equipo` WHERE `nombre_equipo` = 'Quality Assurance Team');

INSERT INTO `dev_portal_sql`.`equipo` (`nombre_equipo`, `fecha_creacion`)
SELECT 'Backend Development', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`equipo` WHERE `nombre_equipo` = 'Backend Development');

INSERT INTO `dev_portal_sql`.`equipo` (`nombre_equipo`, `fecha_creacion`)
SELECT 'Frontend Development', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`equipo` WHERE `nombre_equipo` = 'Frontend Development');

INSERT INTO `dev_portal_sql`.`equipo` (`nombre_equipo`, `fecha_creacion`)
SELECT 'DevOps', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`equipo` WHERE `nombre_equipo` = 'DevOps');

INSERT INTO `dev_portal_sql`.`equipo` (`nombre_equipo`, `fecha_creacion`)
SELECT 'Product Management', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`equipo` WHERE `nombre_equipo` = 'Product Management');

INSERT INTO `dev_portal_sql`.`equipo` (`nombre_equipo`, `fecha_creacion`)
SELECT 'Security Team', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`equipo` WHERE `nombre_equipo` = 'Security Team');

-- ================================================
-- PROYECTOS Y REPOSITORIOS - ELIMINADOS PARA VALIDACION DE LOGICA
-- ================================================

-- ================================================
-- LIMPIEZA / UPDATES
-- ================================================
SET SQL_SAFE_UPDATES = 0;

UPDATE `dev_portal_sql`.`usuario_has_repositorio`
SET `privilegio_usuario_repositorio` = 'LECTOR'
WHERE `privilegio_usuario_repositorio` = 'COMENTADOR';

UPDATE `dev_portal_sql`.`equipo_has_repositorio`
SET `privilegio_equipo_repositorio` = 'LECTOR'
WHERE `privilegio_equipo_repositorio` = 'COMENTADOR';

SET SQL_SAFE_UPDATES = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ================================================
-- REPOSITORIOS INICIALES
-- ================================================
INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'notification-system', 'Sistema de notificaciones', 'PUBLICO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'lmartinez' LIMIT 1), NOW(), 'master'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'notification-system');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'api-gateway', 'Gateway principal para todas las APIs', 'PRIVADO', 'PERSONAL',
       (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'psanchez' LIMIT 1),
       (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'psanchez' LIMIT 1), NOW(), 'main'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'api-gateway');

-- ================================================
-- ENLACES
-- ================================================
INSERT INTO `dev_portal_sql`.`enlace` (`direccion_almacenamiento`, `nombre_archivo`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://github.com/topics/stripe-integration', 'stripe-integration', NOW(), 'REPOSITORIO', r.repositorio_id, 'STORAGE'
FROM `dev_portal_sql`.`repositorio` r
WHERE r.nombre_repositorio = 'stripe-integration'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`enlace` WHERE `direccion_almacenamiento` = 'https://github.com/topics/stripe-integration');

INSERT INTO `dev_portal_sql`.`enlace` (`direccion_almacenamiento`, `nombre_archivo`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://github.com/rehan-adi/payment-processing-microservices', 'payment-processing-microservices', NOW(), 'REPOSITORIO', r.repositorio_id, 'STORAGE'
FROM `dev_portal_sql`.`repositorio` r
WHERE r.nombre_repositorio = 'payment-microservice'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`enlace` WHERE `direccion_almacenamiento` = 'https://github.com/rehan-adi/payment-processing-microservices');

INSERT INTO `dev_portal_sql`.`enlace` (`direccion_almacenamiento`, `nombre_archivo`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://github.com/company/auth-service', 'auth-service', NOW(), 'REPOSITORIO', r.repositorio_id, 'STORAGE'
FROM `dev_portal_sql`.`repositorio` r
WHERE r.nombre_repositorio = 'auth-service'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`enlace` WHERE `direccion_almacenamiento` = 'https://github.com/company/auth-service');

INSERT INTO `dev_portal_sql`.`enlace` (`direccion_almacenamiento`, `nombre_archivo`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://github.com/KeyAuth/KeyAuth-Source-Code', 'KeyAuth-Source-Code', NOW(), 'REPOSITORIO', r.repositorio_id, 'STORAGE'
FROM `dev_portal_sql`.`repositorio` r
WHERE r.nombre_repositorio = 'notification-system'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`enlace` WHERE `direccion_almacenamiento` = 'https://github.com/KeyAuth/KeyAuth-Source-Code');

INSERT INTO `dev_portal_sql`.`enlace` (`direccion_almacenamiento`, `nombre_archivo`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://github.com/topics/api-gateway', 'api-gateway', NOW(), 'REPOSITORIO', r.repositorio_id, 'STORAGE'
FROM `dev_portal_sql`.`repositorio` r
WHERE r.nombre_repositorio = 'api-gateway'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`enlace` WHERE `direccion_almacenamiento` = 'https://github.com/topics/api-gateway');

INSERT INTO `dev_portal_sql`.`enlace` (`direccion_almacenamiento`, `nombre_archivo`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://docs.stripe.com/?locale=es-419', 'docs.stripe.com', NOW(), 'REPOSITORIO', r.repositorio_id, 'METADATA'
FROM `dev_portal_sql`.`repositorio` r
WHERE r.nombre_repositorio = 'stripe-integration'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`enlace` WHERE `direccion_almacenamiento` = 'https://docs.stripe.com/?locale=es-419');

INSERT INTO `dev_portal_sql`.`enlace` (`direccion_almacenamiento`, `nombre_archivo`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://www.twilio.com/docs', 'docs.twilio.com', NOW(), 'REPOSITORIO', r.repositorio_id, 'METADATA'
FROM `dev_portal_sql`.`repositorio` r
WHERE r.nombre_repositorio = 'notification-system'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`enlace` WHERE `direccion_almacenamiento` = 'https://www.twilio.com/docs');

INSERT INTO `dev_portal_sql`.`enlace` (`direccion_almacenamiento`, `nombre_archivo`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://developers.google.com/maps/documentation', 'maps.google.com', NOW(), 'REPOSITORIO', r.repositorio_id, 'METADATA'
FROM `dev_portal_sql`.`repositorio` r
WHERE r.nombre_repositorio = 'api-gateway'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`enlace` WHERE `direccion_almacenamiento` = 'https://developers.google.com/maps/documentation');

INSERT INTO `dev_portal_sql`.`enlace` (`direccion_almacenamiento`, `nombre_archivo`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://docs.aws.amazon.com/s3/', 'docs.aws.amazon.com', NOW(), 'PROYECTO', p.proyecto_id, 'METADATA'
FROM `dev_portal_sql`.`proyecto` p
WHERE p.nombre_proyecto = 'API Management System'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`enlace` WHERE `direccion_almacenamiento` = 'https://docs.aws.amazon.com/s3/');

INSERT INTO `dev_portal_sql`.`enlace` (`direccion_almacenamiento`, `nombre_archivo`, `fecha_creacion_enlace`, `contexto_type`, `contexto_id`, `tipo_enlace`)
SELECT 'https://auth0.com/docs', 'auth0.com', NOW(), 'PROYECTO', p.proyecto_id, 'METADATA'
FROM `dev_portal_sql`.`proyecto` p
WHERE p.nombre_proyecto = 'Mobile Banking App'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`enlace` WHERE `direccion_almacenamiento` = 'https://auth0.com/docs');

-- ================================================
-- DOCUMENTACIÃ“N
-- ================================================
INSERT INTO `dev_portal_sql`.`documentacion` (`seccion_documentacion`, `api_api_id`)
SELECT 'DocumentaciÃ³n Principal Stripe', a.api_id
FROM `dev_portal_sql`.`api` a
WHERE a.nombre_api = 'Stripe Payment API'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`documentacion` WHERE `seccion_documentacion` = 'DocumentaciÃ³n Principal Stripe');

INSERT INTO `dev_portal_sql`.`documentacion` (`seccion_documentacion`, `api_api_id`)
SELECT 'DocumentaciÃ³n Principal Twilio', a.api_id
FROM `dev_portal_sql`.`api` a
WHERE a.nombre_api = 'Twilio SMS API'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`documentacion` WHERE `seccion_documentacion` = 'DocumentaciÃ³n Principal Twilio');

INSERT INTO `dev_portal_sql`.`documentacion` (`seccion_documentacion`, `api_api_id`)
SELECT 'DocumentaciÃ³n Principal Google Maps', a.api_id
FROM `dev_portal_sql`.`api` a
WHERE a.nombre_api = 'Google Maps API'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`documentacion` WHERE `seccion_documentacion` = 'DocumentaciÃ³n Principal Google Maps');

INSERT INTO `dev_portal_sql`.`documentacion` (`seccion_documentacion`, `api_api_id`)
SELECT 'DocumentaciÃ³n Principal AWS S3', a.api_id
FROM `dev_portal_sql`.`api` a
WHERE a.nombre_api = 'AWS S3 API'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`documentacion` WHERE `seccion_documentacion` = 'DocumentaciÃ³n Principal AWS S3');

INSERT INTO `dev_portal_sql`.`documentacion` (`seccion_documentacion`, `api_api_id`)
SELECT 'DocumentaciÃ³n Principal Auth0', a.api_id
FROM `dev_portal_sql`.`api` a
WHERE a.nombre_api = 'Auth0 Authentication'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`documentacion` WHERE `seccion_documentacion` = 'DocumentaciÃ³n Principal Auth0');

-- ================================================
-- VERSIONES API
-- ================================================
INSERT INTO `dev_portal_sql`.`version_api` (`numero_version`, `descripcion_version`, `contrato_api_url`, `fecha_lanzamiento`, `api_api_id`, `documentacion_documentacion_id`)
SELECT 'v1.0', 'VersiÃ³n inicial estable Stripe', 'https://docs.stripe.com/?locale=es-419', '2023-01-15',
       a.api_id, d.documentacion_id
FROM `dev_portal_sql`.`api` a, `dev_portal_sql`.`documentacion` d
WHERE a.nombre_api = 'Stripe Payment API'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal Stripe'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`version_api` WHERE `numero_version` = 'v1.0' AND `descripcion_version` = 'VersiÃ³n inicial estable Stripe' AND `api_api_id` = a.api_id);

INSERT INTO `dev_portal_sql`.`version_api` (`numero_version`, `descripcion_version`, `contrato_api_url`, `fecha_lanzamiento`, `api_api_id`, `documentacion_documentacion_id`)
SELECT 'v2.1', 'Soporte para nuevos mÃ©todos de pago', 'https://docs.stripe.com/payments/payment-methods/payment-method-support', '2023-06-20',
       a.api_id, d.documentacion_id
FROM `dev_portal_sql`.`api` a, `dev_portal_sql`.`documentacion` d
WHERE a.nombre_api = 'Stripe Payment API'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal Stripe'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`version_api` WHERE `numero_version` = 'v2.1' AND `descripcion_version` = 'Soporte para nuevos mÃ©todos de pago' AND `api_api_id` = a.api_id);

INSERT INTO `dev_portal_sql`.`version_api` (`numero_version`, `descripcion_version`, `contrato_api_url`, `fecha_lanzamiento`, `api_api_id`, `documentacion_documentacion_id`)
SELECT 'v2023-10', 'Documentos de Twilio', 'https://www.twilio.com/docs', '2023-10-01',
       a.api_id, d.documentacion_id
FROM `dev_portal_sql`.`api` a, `dev_portal_sql`.`documentacion` d
WHERE a.nombre_api = 'Twilio SMS API'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal Twilio'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`version_api` WHERE `numero_version` = 'v2023-10' AND `descripcion_version` = 'Documentos de Twilio' AND `api_api_id` = a.api_id);

INSERT INTO `dev_portal_sql`.`version_api` (`numero_version`, `descripcion_version`, `contrato_api_url`, `fecha_lanzamiento`, `api_api_id`, `documentacion_documentacion_id`)
SELECT 'v3.0', 'Google plataforma de mapas', 'https://developers.google.com/maps/documentation', '2023-03-10',
       a.api_id, d.documentacion_id
FROM `dev_portal_sql`.`api` a, `dev_portal_sql`.`documentacion` d
WHERE a.nombre_api = 'Google Maps API'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal Google Maps'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`version_api` WHERE `numero_version` = 'v3.0' AND `descripcion_version` = 'Google plataforma de mapas' AND `api_api_id` = a.api_id);

INSERT INTO `dev_portal_sql`.`version_api` (`numero_version`, `descripcion_version`, `contrato_api_url`, `fecha_lanzamiento`, `api_api_id`, `documentacion_documentacion_id`)
SELECT 'v2.4', 'DocumentaciÃ³n AWS S3', 'https://docs.aws.amazon.com/s3/', '2023-08-15',
       a.api_id, d.documentacion_id
FROM `dev_portal_sql`.`api` a, `dev_portal_sql`.`documentacion` d
WHERE a.nombre_api = 'AWS S3 API'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal AWS S3'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`version_api` WHERE `numero_version` = 'v2.4' AND `descripcion_version` = 'DocumentaciÃ³n AWS S3' AND `api_api_id` = a.api_id);

-- ================================================
-- CONTENIDOS
-- ================================================
INSERT INTO `dev_portal_sql`.`contenido` (`titulo_contenido`, `fecha_creacion`, `clasificacion_clasificacion_id`, `documentacion_documentacion_id`, `version_api_version_id`)
SELECT 'Getting Started with Stripe', NOW(),
       c.clasificacion_id, d.documentacion_id, v.version_id
FROM `dev_portal_sql`.`clasificacion` c, `dev_portal_sql`.`documentacion` d, `dev_portal_sql`.`version_api` v, `dev_portal_sql`.`api` a
WHERE c.tipo_contenido_texto = 'Tutorial'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal Stripe'
  AND v.numero_version = 'v1.0' AND v.api_api_id = a.api_id AND a.nombre_api = 'Stripe Payment API'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`contenido` WHERE `titulo_contenido` = 'Getting Started with Stripe' AND `version_api_version_id` = v.version_id);

INSERT INTO `dev_portal_sql`.`contenido` (`titulo_contenido`, `fecha_creacion`, `clasificacion_clasificacion_id`, `documentacion_documentacion_id`, `version_api_version_id`)
SELECT 'Payment Processing Tutorial', NOW(),
       c.clasificacion_id, d.documentacion_id, v.version_id
FROM `dev_portal_sql`.`clasificacion` c, `dev_portal_sql`.`documentacion` d, `dev_portal_sql`.`version_api` v, `dev_portal_sql`.`api` a
WHERE c.tipo_contenido_texto = 'Tutorial'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal Stripe'
  AND v.numero_version = 'v2.1' AND v.api_api_id = a.api_id AND a.nombre_api = 'Stripe Payment API'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`contenido` WHERE `titulo_contenido` = 'Payment Processing Tutorial' AND `version_api_version_id` = v.version_id);

INSERT INTO `dev_portal_sql`.`contenido` (`titulo_contenido`, `fecha_creacion`, `clasificacion_clasificacion_id`, `documentacion_documentacion_id`, `version_api_version_id`)
SELECT 'API Reference Guide', NOW(),
       c.clasificacion_id, d.documentacion_id, v.version_id
FROM `dev_portal_sql`.`clasificacion` c, `dev_portal_sql`.`documentacion` d, `dev_portal_sql`.`version_api` v, `dev_portal_sql`.`api` a
WHERE c.tipo_contenido_texto = 'Referencia API'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal Stripe'
  AND v.numero_version = 'v1.0' AND v.api_api_id = a.api_id AND a.nombre_api = 'Stripe Payment API'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`contenido` WHERE `titulo_contenido` = 'API Reference Guide' AND `version_api_version_id` = v.version_id);

INSERT INTO `dev_portal_sql`.`contenido` (`titulo_contenido`, `fecha_creacion`, `clasificacion_clasificacion_id`, `documentacion_documentacion_id`, `version_api_version_id`)
SELECT 'Error Handling Best Practices', NOW(),
       c.clasificacion_id, d.documentacion_id, v.version_id
FROM `dev_portal_sql`.`clasificacion` c, `dev_portal_sql`.`documentacion` d, `dev_portal_sql`.`version_api` v, `dev_portal_sql`.`api` a
WHERE c.tipo_contenido_texto = 'Mejores prÃ¡cticas'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal Stripe'
  AND v.numero_version = 'v2.1' AND v.api_api_id = a.api_id AND a.nombre_api = 'Stripe Payment API'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`contenido` WHERE `titulo_contenido` = 'Error Handling Best Practices' AND `version_api_version_id` = v.version_id);

INSERT INTO `dev_portal_sql`.`contenido` (`titulo_contenido`, `fecha_creacion`, `clasificacion_clasificacion_id`, `documentacion_documentacion_id`, `version_api_version_id`)
SELECT 'Twilio SMS Quick Start', NOW(),
       c.clasificacion_id, d.documentacion_id, v.version_id
FROM `dev_portal_sql`.`clasificacion` c, `dev_portal_sql`.`documentacion` d, `dev_portal_sql`.`version_api` v, `dev_portal_sql`.`api` a
WHERE c.tipo_contenido_texto = 'GuÃ­a de inicio rÃ¡pido'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal Twilio'
  AND v.numero_version = 'v2023-10' AND v.api_api_id = a.api_id AND a.nombre_api = 'Twilio SMS API'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`contenido` WHERE `titulo_contenido` = 'Twilio SMS Quick Start' AND `version_api_version_id` = v.version_id);

INSERT INTO `dev_portal_sql`.`contenido` (`titulo_contenido`, `fecha_creacion`, `clasificacion_clasificacion_id`, `documentacion_documentacion_id`, `version_api_version_id`)
SELECT 'Google Maps Integration Guide', NOW(),
       c.clasificacion_id, d.documentacion_id, v.version_id
FROM `dev_portal_sql`.`clasificacion` c, `dev_portal_sql`.`documentacion` d, `dev_portal_sql`.`version_api` v, `dev_portal_sql`.`api` a
WHERE c.tipo_contenido_texto = 'GuÃ­a de inicio rÃ¡pido'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal Google Maps'
  AND v.numero_version = 'v3.0' AND v.api_api_id = a.api_id AND a.nombre_api = 'Google Maps API'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`contenido` WHERE `titulo_contenido` = 'Google Maps Integration Guide' AND `version_api_version_id` = v.version_id);

-- ================================================
-- TICKETS
-- ================================================
INSERT INTO `dev_portal_sql`.`ticket` (`asunto_ticket`, `cuerpo_ticket`, `fecha_creacion`, `estado_ticket`, `etapa_ticket`, `tipo_ticket`, `prioridad_ticket`, `reportado_por_usuario_id`)
SELECT 'Error en procesamiento de pagos', 'Al procesar pagos con tarjeta internacional, error 500', NOW(), 'ENVIADO', 'EN_PROGRESO', 'INCIDENCIA', 'ALTA', u.usuario_id
FROM `dev_portal_sql`.`usuario` u
WHERE u.dni = '71234567'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`ticket` WHERE `asunto_ticket` = 'Error en procesamiento de pagos');

INSERT INTO `dev_portal_sql`.`ticket` (`asunto_ticket`, `cuerpo_ticket`, `fecha_creacion`, `fecha_cierre`, `estado_ticket`, `etapa_ticket`, `tipo_ticket`, `prioridad_ticket`, `reportado_por_usuario_id`)
SELECT 'Consulta sobre documentaciÃ³n Stripe', 'Necesito ayuda con webhooks', NOW(), NOW(), 'ENVIADO', 'RESUELTO', 'CONSULTA', 'MEDIA', u.usuario_id
FROM `dev_portal_sql`.`usuario` u
WHERE u.dni = '91234567'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`ticket` WHERE `asunto_ticket` = 'Consulta sobre documentaciÃ³n Stripe');

INSERT INTO `dev_portal_sql`.`ticket` (`asunto_ticket`, `cuerpo_ticket`, `fecha_creacion`, `estado_ticket`, `etapa_ticket`, `tipo_ticket`, `prioridad_ticket`, `reportado_por_usuario_id`)
SELECT 'Solicitud de nueva funcionalidad', 'Agregar soporte para Apple Pay', NOW(), 'ENVIADO', 'PENDIENTE', 'REQUERIMIENTO', 'MEDIA', u.usuario_id
FROM `dev_portal_sql`.`usuario` u
WHERE u.dni = '03456788'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`ticket` WHERE `asunto_ticket` = 'Solicitud de nueva funcionalidad');

INSERT INTO `dev_portal_sql`.`ticket` (`asunto_ticket`, `cuerpo_ticket`, `fecha_creacion`, `estado_ticket`, `etapa_ticket`, `tipo_ticket`, `prioridad_ticket`, `reportado_por_usuario_id`, `asignado_a_usuario_id`)
SELECT 'Problema de autenticaciÃ³n', 'Tokens JWT expiran antes de tiempo', NOW(), 'RECIBIDO', 'EN_PROGRESO', 'INCIDENCIA', 'ALTA', u.usuario_id,
       (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE dni = '09012344' LIMIT 1)
FROM `dev_portal_sql`.`usuario` u
WHERE u.dni = '72345678'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`ticket` WHERE `asunto_ticket` = 'Problema de autenticaciÃ³n');

-- ================================================
-- TOKENS
-- ================================================
INSERT INTO `dev_portal_sql`.`token` (`valor_token`, `estado_token`, `fecha_creacion_token`, `fecha_expiracion_token`, `usuario_usuario_id`)
SELECT 'abc123def456ghi789', 'ACTIVO', NOW(), DATE_ADD(NOW(), INTERVAL 1 HOUR), u.usuario_id
FROM `dev_portal_sql`.`usuario` u
WHERE u.dni = '71234567'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`token` WHERE `valor_token` = 'abc123def456ghi789');

INSERT INTO `dev_portal_sql`.`token` (`valor_token`, `estado_token`, `fecha_creacion_token`, `fecha_expiracion_token`, `usuario_usuario_id`)
SELECT 'jkl012mno345pqr678', 'ACTIVO', NOW(), DATE_ADD(NOW(), INTERVAL 1 HOUR), u.usuario_id
FROM `dev_portal_sql`.`usuario` u
WHERE u.dni = '72345678'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`token` WHERE `valor_token` = 'jkl012mno345pqr678');

INSERT INTO `dev_portal_sql`.`token` (`valor_token`, `estado_token`, `fecha_creacion_token`, `fecha_expiracion_token`, `usuario_usuario_id`)
SELECT 'stu901vwx234yz567', 'REVOCADO', NOW(), DATE_ADD(NOW(), INTERVAL 1 HOUR), u.usuario_id
FROM `dev_portal_sql`.`usuario` u
WHERE u.dni = '91234567'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`token` WHERE `valor_token` = 'stu901vwx234yz567');

-- ================================================
-- HISTORIAL
-- ================================================
INSERT INTO `dev_portal_sql`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `ip_origen`, `usuario_usuario_id`)
SELECT 'LOGIN', 'Usuario', u.usuario_id, 'Inicio de sesiÃ³n exitoso', NOW(), '192.168.1.100', u.usuario_id
FROM `dev_portal_sql`.`usuario` u
WHERE u.dni = '71234567'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`historial` WHERE `descripcion_evento` = 'Inicio de sesiÃ³n exitoso' AND `ip_origen` = '192.168.1.100');

INSERT INTO `dev_portal_sql`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `ip_origen`, `usuario_usuario_id`)
SELECT 'MODIFICACION', 'API', a.api_id, 'ActualizaciÃ³n de documentaciÃ³n de Stripe', NOW(), '10.0.0.50', u.usuario_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`api` a
WHERE u.dni = '09012344'
  AND a.nombre_api = 'Stripe Payment API'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`historial` WHERE `descripcion_evento` = 'ActualizaciÃ³n de documentaciÃ³n de Stripe' AND `ip_origen` = '10.0.0.50');

INSERT INTO `dev_portal_sql`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `ip_origen`, `usuario_usuario_id`)
SELECT 'CREACION', 'Ticket', t.ticket_id, 'Nuevo ticket creado para error en pagos', NOW(), '172.16.0.25', u.usuario_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`ticket` t
WHERE u.dni = '72345678'
  AND t.asunto_ticket = 'Error en procesamiento de pagos'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`historial` WHERE `descripcion_evento` = 'Nuevo ticket creado para error en pagos' AND `ip_origen` = '172.16.0.25');

-- ================================================
-- FEEDBACK
-- ================================================
INSERT INTO `dev_portal_sql`.`feedback` (`comentario`, `puntuacion`, `usuario_usuario_id`, `documentacion_documentacion_id`)
SELECT 'Excelente documentaciÃ³n, muy clara y completa', 5.0, u.usuario_id, d.documentacion_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`documentacion` d
WHERE u.dni = '71234567'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal Stripe'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`feedback` WHERE `comentario` = 'Excelente documentaciÃ³n, muy clara y completa');

INSERT INTO `dev_portal_sql`.`feedback` (`comentario`, `puntuacion`, `usuario_usuario_id`, `documentacion_documentacion_id`)
SELECT 'Faltan ejemplos en Python para la versiÃ³n 2.1', 3.5, u.usuario_id, d.documentacion_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`documentacion` d
WHERE u.dni = '91234567'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal Stripe'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`feedback` WHERE `comentario` = 'Faltan ejemplos en Python para la versiÃ³n 2.1');

INSERT INTO `dev_portal_sql`.`feedback` (`comentario`, `puntuacion`, `usuario_usuario_id`, `documentacion_documentacion_id`)
SELECT 'La guÃ­a de soluciÃ³n de problemas es muy Ãºtil', 4.5, u.usuario_id, d.documentacion_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`documentacion` d
WHERE u.dni = '03456788'
  AND d.seccion_documentacion = 'DocumentaciÃ³n Principal Twilio'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`feedback` WHERE `comentario` = 'La guÃ­a de soluciÃ³n de problemas es muy Ãºtil');

-- ================================================
-- NODOS (Adaptado: size -> size_bytes)
-- ================================================
INSERT INTO `dev_portal_sql`.`nodo` (`container_type`, `container_id`, `nombre`, `tipo`, `path`, `descripcion`, `size_bytes`, `mime_type`, `creado_en`)
SELECT 'REPOSITORIO', r.repositorio_id, '/', 'CARPETA', '/', 'RaÃ­z del repo', 0, NULL, NOW()
FROM `dev_portal_sql`.`repositorio` r
WHERE r.nombre_repositorio = 'stripe-integration'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`nodo` WHERE `path` = '/' AND `nombre` = '/' AND `container_type` = 'REPOSITORIO' AND `container_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`nodo` (`container_type`, `container_id`, `parent_id`, `nombre`, `tipo`, `path`, `descripcion`, `size_bytes`, `mime_type`, `creado_en`)
SELECT 'REPOSITORIO', r.repositorio_id, n.nodo_id, 'src', 'CARPETA', '/src', 'CÃ³digo fuente', 0, NULL, NOW()
FROM `dev_portal_sql`.`repositorio` r, `dev_portal_sql`.`nodo` n
WHERE r.nombre_repositorio = 'stripe-integration'
  AND n.path = '/' AND n.container_type = 'REPOSITORIO' AND n.container_id = r.repositorio_id
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`nodo` WHERE `path` = '/src' AND `container_id` = r.repositorio_id AND `container_type` = 'REPOSITORIO');

INSERT INTO `dev_portal_sql`.`nodo` (`container_type`, `container_id`, `parent_id`, `nombre`, `tipo`, `path`, `descripcion`, `size_bytes`, `mime_type`, `creado_en`)
SELECT 'REPOSITORIO', r.repositorio_id, n.nodo_id, 'README.md', 'ARCHIVO', '/src/README.md', 'Readme', 1024, 'text/markdown', NOW()
FROM `dev_portal_sql`.`repositorio` r, `dev_portal_sql`.`nodo` n
WHERE r.nombre_repositorio = 'stripe-integration'
  AND n.path = '/src' AND n.container_type = 'REPOSITORIO' AND n.container_id = r.repositorio_id
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`nodo` WHERE `path` = '/src/README.md' AND `container_id` = r.repositorio_id AND `container_type` = 'REPOSITORIO');

-- ================================================
-- RELACIONES PROYECTO-REPOSITORIO
-- ================================================
INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'E-commerce Platform' AND r.nombre_repositorio = 'stripe-integration'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'E-commerce Platform' AND r.nombre_repositorio = 'payment-microservice'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'Mobile Banking App' AND r.nombre_repositorio = 'auth-service'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'Mobile Banking App' AND r.nombre_repositorio = 'notification-system'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'API Management System' AND r.nombre_repositorio = 'api-gateway'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

-- ================================================
-- ASIGNAR USUARIOS A EQUIPOS
-- ================================================
INSERT INTO `dev_portal_sql`.`usuario_has_equipo` (`usuario_usuario_id`, `equipo_equipo_id`)
SELECT u.usuario_id, eq.equipo_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`equipo` eq
WHERE u.dni BETWEEN '71234567' AND '75678901'
  AND eq.nombre_equipo = 'Backend Development'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_equipo`
                  WHERE `usuario_usuario_id` = u.usuario_id AND `equipo_equipo_id` = eq.equipo_id);

INSERT INTO `dev_portal_sql`.`usuario_has_equipo` (`usuario_usuario_id`, `equipo_equipo_id`)
SELECT u.usuario_id, eq.equipo_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`equipo` eq
WHERE u.dni BETWEEN '76789012' AND '79012345'
  AND eq.nombre_equipo = 'Frontend Development'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_equipo`
                  WHERE `usuario_usuario_id` = u.usuario_id AND `equipo_equipo_id` = eq.equipo_id);

INSERT INTO `dev_portal_sql`.`usuario_has_equipo` (`usuario_usuario_id`, `equipo_equipo_id`)
SELECT u.usuario_id, eq.equipo_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`equipo` eq
WHERE (u.dni BETWEEN '91234567' AND '99999999' OR u.dni BETWEEN '00000000' AND '02345677')
  AND eq.nombre_equipo = 'Quality Assurance'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_equipo`
                  WHERE `usuario_usuario_id` = u.usuario_id AND `equipo_equipo_id` = eq.equipo_id);

INSERT INTO `dev_portal_sql`.`usuario_has_equipo` (`usuario_usuario_id`, `equipo_equipo_id`)
SELECT u.usuario_id, eq.equipo_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`equipo` eq
WHERE u.dni BETWEEN '80123456' AND '82345678'
  AND eq.nombre_equipo = 'DevOps'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_equipo`
                  WHERE `usuario_usuario_id` = u.usuario_id AND `equipo_equipo_id` = eq.equipo_id);

INSERT INTO `dev_portal_sql`.`usuario_has_equipo` (`usuario_usuario_id`, `equipo_equipo_id`)
SELECT u.usuario_id, eq.equipo_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`equipo` eq
WHERE u.dni BETWEEN '03456788' AND '08901233'
  AND eq.nombre_equipo = 'Product Management'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_equipo`
                  WHERE `usuario_usuario_id` = u.usuario_id AND `equipo_equipo_id` = eq.equipo_id);

INSERT INTO `dev_portal_sql`.`usuario_has_equipo` (`usuario_usuario_id`, `equipo_equipo_id`)
SELECT u.usuario_id, eq.equipo_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`equipo` eq
WHERE (u.dni BETWEEN '83456789' AND '85678901' OR u.dni BETWEEN '09012344' AND '09234566')
  AND eq.nombre_equipo = 'Security Team'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_equipo`
                  WHERE `usuario_usuario_id` = u.usuario_id AND `equipo_equipo_id` = eq.equipo_id);

-- ================================================
-- INSERTS EXTENDIDOS PARA ROCÃO
-- ================================================
-- Proyectos personales de RocÃ­o (propietario_nombre = username, created_by = usuario_id)
INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
SELECT 'QA Automation Framework', 'Framework automatizado de pruebas desarrollado por RocÃ­o para mejorar la eficiencia en testing', 'PUBLICO', 'ORGANIZACION', 'USUARIO', 'EN_DESARROLLO', '2024-01-15', '2024-12-31', u.username, u.usuario_id, NOW(), 'qa-automation-framework', 'QAF-001'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'qa-automation-framework');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Bug Tracking Dashboard', 'Dashboard personalizado para tracking y anÃ¡lisis de bugs encontrados en diferentes proyectos', 'PRIVADO', 'RESTRINGIDO', 'USUARIO', 'MANTENIMIENTO', '2023-09-01', NULL, u.username, u.usuario_id, NOW(), 'bug-tracking-dashboard', 'BTD-002'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'bug-tracking-dashboard');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Test Data Generator', 'Herramienta para generar datos de prueba automÃ¡ticamente para diferentes escenarios de testing', 'PUBLICO', 'CUALQUIER_PERSONA_CON_EL_ENLACE', 'USUARIO', 'PLANEADO', '2024-05-01', '2024-08-31', u.username, u.usuario_id, NOW(), 'test-data-generator', 'TDG-003'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'test-data-generator');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Performance Test Suite', 'Suite completa de pruebas de rendimiento para aplicaciones web y mÃ³viles', 'PRIVADO', 'ORGANIZACION', 'USUARIO', 'EN_DESARROLLO', '2024-02-10', '2024-10-15', u.username, u.usuario_id, NOW(), 'performance-test-suite', 'PTS-004'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'performance-test-suite');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
SELECT 'API Testing Toolkit', 'Conjunto de herramientas para testing automatizado de APIs REST y GraphQL', 'PUBLICO', 'ORGANIZACION', 'USUARIO', 'CERRADO', '2023-03-15', '2023-11-30', u.username, u.usuario_id, NOW(), 'api-testing-toolkit', 'ATT-005'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'api-testing-toolkit');

-- ================================================
-- PROYECTOS DE GRUPO (creados por RocÃ­o)
-- ================================================
INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
SELECT 'E-Learning Platform QA', 'Proyecto de Quality Assurance para la nueva plataforma de e-learning de la empresa', 'PRIVADO', 'RESTRINGIDO', 'GRUPO', 'EN_DESARROLLO', '2024-03-01', '2024-11-30', 'QA Team', u.usuario_id, NOW(), 'elearning-platform-qa', 'ELPQA-006'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'elearning-platform-qa');

-- COMMENTED OUT: Mantener solo 1 proyecto GRUPO para rbeltran
-- INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
-- SELECT 'Mobile App Testing Framework', 'Framework integral para testing de aplicaciones mÃ³viles iOS y Android', 'PUBLICO', 'ORGANIZACION', 'GRUPO', 'EN_DESARROLLO', '2024-01-20', '2024-09-15', 'Mobile Testing Team', u.usuario_id, NOW(), 'mobile-app-testing-framework', 'MATF-007'
-- FROM `dev_portal_sql`.`usuario` u
-- WHERE u.correo = 'rocio.beltran@company.com'
--   AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'mobile-app-testing-framework');

-- COMMENTED OUT: Mantener solo 1 proyecto GRUPO para rbeltran
-- INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
-- SELECT 'Microservices Testing Strategy', 'Estrategia y herramientas para testing de arquitectura de microservicios', 'PRIVADO', 'RESTRINGIDO', 'GRUPO', 'PLANEADO', '2024-06-01', '2025-02-28', 'Backend Testing Group', u.usuario_id, NOW(), 'microservices-testing-strategy', 'MTS-008'
-- FROM `dev_portal_sql`.`usuario` u
-- WHERE u.correo = 'rocio.beltran@company.com'
--   AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'microservices-testing-strategy');

-- COMMENTED OUT: Mantener solo 1 proyecto GRUPO para rbeltran
-- INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
-- SELECT 'CI/CD Pipeline Testing', 'ImplementaciÃ³n de testing automatizado en pipelines de CI/CD', 'PUBLICO', 'ORGANIZACION', 'GRUPO', 'MANTENIMIENTO', '2023-08-01', NULL, 'DevOps Testing Team', u.usuario_id, NOW(), 'cicd-pipeline-testing', 'CPT-009'
-- FROM `dev_portal_sql`.`usuario` u
-- WHERE u.correo = 'rocio.beltran@company.com'
--   AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'cicd-pipeline-testing');

-- ================================================
-- PROYECTOS DE EMPRESA (creados por RocÃ­o)
-- ================================================
INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
SELECT 'React Component Library', 'LibrerÃ­a de componentes React reutilizables para aplicaciones empresariales', 'PUBLICO', 'CUALQUIER_PERSONA_CON_EL_ENLACE', 'EMPRESA', 'EN_DESARROLLO', '2024-01-10', '2024-07-31', 'TechCorp Inc', u.usuario_id, NOW(), 'react-component-library', 'RCL-101'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'react-component-library');

-- COMMENTED OUT: Mantener solo 1 proyecto EMPRESA para rbeltran
-- INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
-- SELECT 'DevOps Monitoring Suite', 'Suite completa de monitoreo para infraestructura y aplicaciones', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'MANTENIMIENTO', '2023-05-15', NULL, 'TechCorp Inc', u.usuario_id, NOW(), 'devops-monitoring-suite', 'DMS-102'
-- FROM `dev_portal_sql`.`usuario` u
-- WHERE u.correo = 'rocio.beltran@company.com'
--   AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'devops-monitoring-suite');

-- COMMENTED OUT: Mantener solo 1 proyecto EMPRESA para rbeltran
-- INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
-- SELECT 'Machine Learning Pipeline', 'Pipeline automatizado para entrenamiento y despliegue de modelos ML', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'EN_DESARROLLO', '2024-02-01', '2024-12-15', 'TechCorp Inc', u.usuario_id, NOW(), 'machine-learning-pipeline', 'MLP-103'
-- FROM `dev_portal_sql`.`usuario` u
-- WHERE u.correo = 'rocio.beltran@company.com'
--   AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'machine-learning-pipeline');

-- COMMENTED OUT: Mantener solo 1 proyecto EMPRESA para rbeltran
-- INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
-- SELECT 'Cloud Infrastructure as Code', 'Templates y mÃ³dulos para automatizaciÃ³n de infraestructura en la nube', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'CERRADO', '2023-01-15', '2023-10-31', 'TechCorp Inc', u.usuario_id, NOW(), 'cloud-infrastructure-as-code', 'CIAC-105'
-- FROM `dev_portal_sql`.`usuario` u
-- WHERE u.correo = 'rocio.beltran@company.com'
--   AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'cloud-infrastructure-as-code');

-- COMMENTED OUT: Mantener solo 1 proyecto EMPRESA para rbeltran
-- INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
-- SELECT 'Cybersecurity Toolkit', 'Herramientas y scripts para auditorÃ­as de seguridad', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'MANTENIMIENTO', '2023-09-01', NULL, 'TechCorp Inc', u.usuario_id, NOW(), 'cybersecurity-toolkit', 'CST-106'
-- FROM `dev_portal_sql`.`usuario` u
-- WHERE u.correo = 'rocio.beltran@company.com'
--   AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'cybersecurity-toolkit');

-- COMMENTED OUT: Mantener solo 1 proyecto EMPRESA para rbeltran
-- INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
-- SELECT 'IoT Device Management', 'Sistema de gestiÃ³n para dispositivos IoT empresariales', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'PLANEADO', '2024-07-01', '2025-03-31', 'TechCorp Inc', u.usuario_id, NOW(), 'iot-device-management', 'IDM-108'
-- FROM `dev_portal_sql`.`usuario` u
-- WHERE u.correo = 'rocio.beltran@company.com'
--   AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'iot-device-management');

-- COMMENTED OUT: Mantener solo 1 proyecto EMPRESA para rbeltran
-- INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
-- SELECT 'Microservices Architecture', 'Arquitectura de referencia para aplicaciones basadas en microservicios', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'EN_DESARROLLO', '2024-01-25', '2024-08-15', 'TechCorp Inc', u.usuario_id, NOW(), 'microservices-architecture', 'MSA-109'
-- FROM `dev_portal_sql`.`usuario` u
-- WHERE u.correo = 'rocio.beltran@company.com'
--   AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'microservices-architecture');

-- COMMENTED OUT: Mantener solo 1 proyecto EMPRESA para rbeltran
-- INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
-- SELECT 'API Documentation Generator', 'Herramienta para generar documentaciÃ³n automÃ¡tica de APIs', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'MANTENIMIENTO', '2023-11-01', NULL, 'TechCorp Inc', u.usuario_id, NOW(), 'api-documentation-generator', 'ADG-111'
-- FROM `dev_portal_sql`.`usuario` u
-- WHERE u.correo = 'rocio.beltran@company.com'
--   AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'api-documentation-generator');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Container Orchestration Tools', 'Herramientas para orquestaciÃ³n de contenedores Docker y Kubernetes', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'EN_DESARROLLO', '2024-02-15', '2024-09-30', 'TechCorp Inc', (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'cgomez' LIMIT 1), NOW(), 'container-orchestration-tools', 'COT-112'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'container-orchestration-tools');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
SELECT 'E-commerce Analytics Engine', 'Motor de anÃ¡lisis para plataformas de comercio electrÃ³nico', 'PUBLICO', 'CUALQUIER_PERSONA_CON_EL_ENLACE', 'GRUPO', 'EN_DESARROLLO', '2024-03-10', '2024-12-20', 'Analytics Team', (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'rmendoza' LIMIT 1), NOW(), 'ecommerce-analytics-engine', 'EAE-113'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'ecommerce-analytics-engine');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Real-time Chat Application', 'AplicaciÃ³n de chat en tiempo real con WebSocket y Redis', 'PUBLICO', 'ORGANIZACION', 'GRUPO', 'CERRADO', '2023-04-15', '2023-11-30', 'Chat Team', (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'jperez' LIMIT 1), NOW(), 'realtime-chat-application', 'RCA-114'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'realtime-chat-application');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`, `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`)
SELECT 'Content Management System', 'CMS modular y extensible para sitios web empresariales', 'PUBLICO', 'ORGANIZACION', 'EMPRESA', 'MANTENIMIENTO', '2023-07-01', NULL, 'TechCorp Inc', (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'mrodriguez' LIMIT 1), NOW(), 'content-management-system', 'CMS-115'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'content-management-system');

-- ================================================
-- REPOSITORIOS ADICIONALES
-- ================================================
INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'qa-automation-framework-repo', 'Repositorio del framework de automatizaciÃ³n QA de RocÃ­o', 'PUBLICO', 'PERSONAL',
       u.usuario_id, u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'qa-automation-framework-repo');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'bug-tracking-dashboard-repo', 'Repositorio del dashboard de tracking de bugs', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Quality Assurance Team' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'bug-tracking-dashboard-repo');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'test-data-generator-repo', 'Repositorio del generador de datos de prueba', 'PUBLICO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Quality Assurance Team' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'test-data-generator-repo');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'react-components-lib', 'LibrerÃ­a de componentes React', 'PUBLICO', 'PERSONAL',
       u.usuario_id, u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'carlos.gomez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'react-components-lib');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'devops-monitoring-tools', 'Herramientas de monitoreo DevOps', 'PUBLICO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'DevOps' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'maria.rodriguez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'devops-monitoring-tools');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'ml-pipeline-automation', 'Pipeline automatizado de Machine Learning', 'PUBLICO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'juan.perez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'ml-pipeline-automation');

-- ================================================
-- RELACIONES USUARIO-PROYECTO
-- ================================================
-- rbeltran en sus proyectos PERSONALES
INSERT INTO `dev_portal_sql`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p
WHERE u.correo = 'rocio.beltran@company.com'
  AND p.slug IN ('qa-automation-framework', 'bug-tracking-dashboard', 'test-data-generator', 'performance-test-suite', 'api-testing-toolkit')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

-- rbeltran como CREADOR en proyectos GRUPO (aunque sea GRUPO, el creador debe estar en usuario_has_proyecto)
INSERT INTO `dev_portal_sql`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p
WHERE u.correo = 'rocio.beltran@company.com'
  AND p.slug IN ('elearning-platform-qa')
  AND p.propietario_proyecto = 'GRUPO'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

-- rbeltran como CREADOR en proyectos EMPRESA (aunque sea EMPRESA, el creador debe estar en usuario_has_proyecto)
INSERT INTO `dev_portal_sql`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p
WHERE u.correo = 'rocio.beltran@company.com'
  AND p.slug IN ('react-component-library')
  AND p.propietario_proyecto = 'EMPRESA'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

-- ================================================
-- RELACIONES EQUIPO-PROYECTO
-- ================================================
INSERT INTO `dev_portal_sql`.`equipo_has_proyecto` (`equipo_equipo_id`, `proyecto_proyecto_id`, `privilegio_equipo_proyecto`, `fecha_equipo_proyecto`)
SELECT eq.equipo_id, p.proyecto_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`equipo` eq, `dev_portal_sql`.`proyecto` p
WHERE eq.nombre_equipo = 'Quality Assurance'
  AND p.slug IN ('elearning-platform-qa', 'mobile-app-testing-framework', 'microservices-testing-strategy', 'cicd-pipeline-testing')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`equipo_has_proyecto` WHERE equipo_equipo_id = eq.equipo_id AND proyecto_proyecto_id = p.proyecto_id);

INSERT INTO `dev_portal_sql`.`equipo_has_proyecto` (`equipo_equipo_id`, `proyecto_proyecto_id`, `privilegio_equipo_proyecto`, `fecha_equipo_proyecto`)
SELECT eq.equipo_id, p.proyecto_id, 'LECTOR', NOW()
FROM `dev_portal_sql`.`equipo` eq, `dev_portal_sql`.`proyecto` p
WHERE eq.nombre_equipo = 'Backend Development'
  AND p.slug IN ('microservices-architecture', 'api-documentation-generator', 'container-orchestration-tools')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`equipo_has_proyecto` WHERE equipo_equipo_id = eq.equipo_id AND proyecto_proyecto_id = p.proyecto_id);

INSERT INTO `dev_portal_sql`.`equipo_has_proyecto` (`equipo_equipo_id`, `proyecto_proyecto_id`, `privilegio_equipo_proyecto`, `fecha_equipo_proyecto`)
SELECT eq.equipo_id, p.proyecto_id, 'COMENTADOR', NOW()
FROM `dev_portal_sql`.`equipo` eq, `dev_portal_sql`.`proyecto` p
WHERE eq.nombre_equipo = 'Frontend Development'
  AND p.slug IN ('react-component-library', 'progressive-web-app-template', 'content-management-system')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`equipo_has_proyecto` WHERE equipo_equipo_id = eq.equipo_id AND proyecto_proyecto_id = p.proyecto_id);

-- ================================================
-- USUARIOS ADICIONALES PARTICIPAN EN PROYECTOS
-- ================================================
INSERT INTO `dev_portal_sql`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'COMENTADOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p
WHERE u.dni IN ('71234567', '72345678', '73456789') -- Carlos, Maria, Juan (primeros 3 DEV)
  AND p.slug IN ('elearning-platform-qa', 'mobile-app-testing-framework')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

INSERT INTO `dev_portal_sql`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'LECTOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p
WHERE u.dni IN ('91234567', '92345678', '93456789') -- Roberto, Silvia, Raul (primeros 3 QA)
  AND p.slug IN ('react-component-library', 'data-analytics-dashboard', 'cybersecurity-toolkit')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

INSERT INTO `dev_portal_sql`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p
WHERE u.dni IN ('03456788', '04567899') -- Alejandro, Daniela (primeros 2 PO)
  AND p.slug IN ('machine-learning-pipeline', 'iot-device-management', 'ecommerce-analytics-engine')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

-- ================================================
-- RELACIONES PROYECTO-REPOSITORIO
-- ================================================
INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE ((p.slug = 'qa-automation-framework' AND r.nombre_repositorio = 'qa-automation-framework-repo')
    OR (p.slug = 'bug-tracking-dashboard' AND r.nombre_repositorio = 'bug-tracking-dashboard-repo')
    OR (p.slug = 'test-data-generator' AND r.nombre_repositorio = 'test-data-generator-repo'))
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` WHERE proyecto_proyecto_id = p.proyecto_id AND repositorio_repositorio_id = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE ((p.slug = 'react-component-library' AND r.nombre_repositorio = 'react-components-lib')
    OR (p.slug = 'devops-monitoring-suite' AND r.nombre_repositorio = 'devops-monitoring-tools')
    OR (p.slug = 'machine-learning-pipeline' AND r.nombre_repositorio = 'ml-pipeline-automation'))
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` WHERE proyecto_proyecto_id = p.proyecto_id AND repositorio_repositorio_id = r.repositorio_id);

-- ================================================
-- RELACIONES EQUIPO-REPOSITORIO (Repositorios Colaborativos)
-- ================================================
-- Quality Assurance Team es propietario de repositorios de QA
INSERT INTO `dev_portal_sql`.`equipo_has_repositorio` (`equipo_equipo_id`, `repositorio_repositorio_id`, `privilegio_equipo_repositorio`, `fecha_equipo_repositorio`)
SELECT eq.equipo_id, r.repositorio_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`equipo` eq, `dev_portal_sql`.`repositorio` r
WHERE eq.nombre_equipo = 'Quality Assurance Team'
  AND r.nombre_repositorio IN ('bug-tracking-dashboard-repo', 'test-data-generator-repo')
  AND r.tipo_repositorio = 'COLABORATIVO'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`equipo_has_repositorio` WHERE equipo_equipo_id = eq.equipo_id AND repositorio_repositorio_id = r.repositorio_id);

-- Backend Development es propietario de repositorios de backend
INSERT INTO `dev_portal_sql`.`equipo_has_repositorio` (`equipo_equipo_id`, `repositorio_repositorio_id`, `privilegio_equipo_repositorio`, `fecha_equipo_repositorio`)
SELECT eq.equipo_id, r.repositorio_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`equipo` eq, `dev_portal_sql`.`repositorio` r
WHERE eq.nombre_equipo = 'Backend Development'
  AND r.nombre_repositorio IN ('notification-system', 'backend-api', 'shared-utils-library', 'experimental-ai-features', 
                                'analytics-data-collector', 'analytics-reporting-service', 'ml-pipeline-automation',
                                'common-auth-service', 'email-service-library', 'api-documentation-generator', 
                                'data-validation-framework', 'analytics-data-processor', 'marketing-campaign-engine', 
                                'customer-data-hub', 'financial-risk-calculator')
  AND r.tipo_repositorio = 'COLABORATIVO'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`equipo_has_repositorio` WHERE equipo_equipo_id = eq.equipo_id AND repositorio_repositorio_id = r.repositorio_id);

-- Frontend Development es propietario de repositorios de frontend
INSERT INTO `dev_portal_sql`.`equipo_has_repositorio` (`equipo_equipo_id`, `repositorio_repositorio_id`, `privilegio_equipo_repositorio`, `fecha_equipo_repositorio`)
SELECT eq.equipo_id, r.repositorio_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`equipo` eq, `dev_portal_sql`.`repositorio` r
WHERE eq.nombre_equipo = 'Frontend Development'
  AND r.nombre_repositorio IN ('analytics-dashboard-ui')
  AND r.tipo_repositorio = 'COLABORATIVO'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`equipo_has_repositorio` WHERE equipo_equipo_id = eq.equipo_id AND repositorio_repositorio_id = r.repositorio_id);

-- DevOps es propietario de repositorios de infraestructura
INSERT INTO `dev_portal_sql`.`equipo_has_repositorio` (`equipo_equipo_id`, `repositorio_repositorio_id`, `privilegio_equipo_repositorio`, `fecha_equipo_repositorio`)
SELECT eq.equipo_id, r.repositorio_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`equipo` eq, `dev_portal_sql`.`repositorio` r
WHERE eq.nombre_equipo = 'DevOps'
  AND r.nombre_repositorio IN ('devops-monitoring-tools', 'dev_portal_sql-scripts', 'kubernetes-deployment-configs', 
                                'ci-cd-pipeline-templates', 'dev_portal_sql-migration-scripts', 'monitoring-logging-framework', 
                                'config-management-system')
  AND r.tipo_repositorio = 'COLABORATIVO'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`equipo_has_repositorio` WHERE equipo_equipo_id = eq.equipo_id AND repositorio_repositorio_id = r.repositorio_id);

-- ================================================
-- RELACIONES USUARIO-REPOSITORIO
-- ================================================
INSERT INTO `dev_portal_sql`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`repositorio` r
WHERE u.correo = 'rocio.beltran@company.com'
  AND r.nombre_repositorio IN ('qa-automation-framework-repo', 'bug-tracking-dashboard-repo', 'test-data-generator-repo')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'LECTOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`repositorio` r
WHERE u.dni IN ('71234567', '72345678', '73456789', '74567890', '75678901')
  AND r.nombre_repositorio IN ('devops-monitoring-tools', 'ml-pipeline-automation')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

-- ================================================
-- DATOS ESPECÃFICOS PARA DETAIL.HTML
-- ================================================
INSERT INTO `dev_portal_sql`.`categoria` (`nombre_categoria`, `descripcion_categoria`)
SELECT 'Desarrollo Web', 'Proyectos de desarrollo web frontend y backend'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`categoria` WHERE `nombre_categoria` = 'Desarrollo Web');

INSERT INTO `dev_portal_sql`.`proyecto` (
    `nombre_proyecto`, `descripcion_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`,
    `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `fecha_fin_proyecto`,
    `propietario_nombre`, `created_by`, `created_at`, `slug`, `proyecto_key`
)
SELECT
    'TelDevPortal Project', 'Portal de desarrollo para gestiÃ³n de proyectos y repositorios del curso GTICS',
    'PUBLICO', 'ORGANIZACION', 'USUARIO', 'EN_DESARROLLO', '2024-10-01', NULL,
    u.username, u.usuario_id, '2024-10-01 10:30:00', 'teldevportal-project', 'TDP-100'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'teldevportal-project');

INSERT INTO `dev_portal_sql`.`categoria_has_proyecto` (`categoria_id_categoria`, `proyecto_proyecto_id`)
SELECT c.id_categoria, p.proyecto_id
FROM `dev_portal_sql`.`categoria` c, `dev_portal_sql`.`proyecto` p
WHERE c.nombre_categoria = 'Desarrollo Web'
  AND p.slug = 'teldevportal-project'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`categoria_has_proyecto` chp
    WHERE chp.categoria_id_categoria = c.id_categoria
      AND chp.proyecto_proyecto_id = p.proyecto_id
);

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT
    'backend-api', 'API REST del DevPortal con Spring Boot y autenticaciÃ³n OAuth2',
    'PUBLICO', 'COLABORATIVO',
    (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
    u.usuario_id, '2024-03-20 17:30:00', 'develop'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'backend-api');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT
    'frontend-web', 'Interfaz web del portal con Thymeleaf y Bootstrap',
    'PUBLICO', 'PERSONAL', u.usuario_id, u.usuario_id, '2025-09-20 14:30:00', 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'frontend-web');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT
    'dev_portal_sql-scripts', 'Scripts SQL para configuraciÃ³n y migraciÃ³n de base de datos',
    'PUBLICO', 'COLABORATIVO',
    (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'DevOps' LIMIT 1),
    u.usuario_id, '2024-09-25 10:30:00', 'develop'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'dev_portal_sql-scripts');

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'teldevportal-project'
  AND r.nombre_repositorio IN ('backend-api', 'frontend-web', 'dev_portal_sql-scripts')
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id
      AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`nodo` (`container_type`, `container_id`, `nombre`, `tipo`, `path`, `descripcion`, `creado_por`, `creado_en`)
SELECT
    'PROYECTO', p.proyecto_id, 'DocumentaciÃ³n', 'CARPETA',
    '/documentacion/', 'Documentos tÃ©cnicos, manuales y guÃ­as del proyecto',
    u.usuario_id, '2024-10-01 10:35:00'
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`usuario` u
WHERE p.slug = 'teldevportal-project'
  AND u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`nodo` n
    WHERE n.container_type = 'PROYECTO'
      AND n.container_id = p.proyecto_id
      AND n.nombre = 'DocumentaciÃ³n'
);

INSERT INTO `dev_portal_sql`.`nodo` (`container_type`, `container_id`, `nombre`, `tipo`, `path`, `descripcion`, `creado_por`, `creado_en`)
SELECT
    'PROYECTO', p.proyecto_id, 'Recursos', 'CARPETA',
    '/recursos/', 'ImÃ¡genes, archivos de configuraciÃ³n y recursos del proyecto',
    u.usuario_id, '2024-10-01 10:40:00'
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`usuario` u
WHERE p.slug = 'teldevportal-project'
  AND u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`nodo` n
    WHERE n.container_type = 'PROYECTO'
      AND n.container_id = p.proyecto_id
      AND n.nombre = 'Recursos'
);

INSERT INTO `dev_portal_sql`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p
WHERE u.correo = 'maria.rodriguez@company.com'
  AND p.slug = 'teldevportal-project'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`usuario_has_proyecto` uhp
    WHERE uhp.usuario_usuario_id = u.usuario_id
      AND uhp.proyecto_proyecto_id = p.proyecto_id
);

INSERT INTO `dev_portal_sql`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p
WHERE u.correo IN ('carlos.gomez@company.com', 'ana.torres@company.com')
  AND p.slug = 'teldevportal-project'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`usuario_has_proyecto` uhp
    WHERE uhp.usuario_usuario_id = u.usuario_id
      AND uhp.proyecto_proyecto_id = p.proyecto_id
);

INSERT INTO `dev_portal_sql`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'LECTOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p
WHERE u.correo = 'pedro.sanchez@company.com'
  AND p.slug = 'teldevportal-project'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`usuario_has_proyecto` uhp
    WHERE uhp.usuario_usuario_id = u.usuario_id
      AND uhp.proyecto_proyecto_id = p.proyecto_id
);

INSERT INTO `dev_portal_sql`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `usuario_usuario_id`)
SELECT
    'CREACION', 'PROYECTO', p.proyecto_id,
    'TelDevPortal Project fue creado por RocÃ­o BeltrÃ¡n',
    '2024-09-25 09:00:00', u.usuario_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p
WHERE u.correo = 'rocio.beltran@company.com'
  AND p.slug = 'teldevportal-project'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`historial` h
    WHERE h.entidad_afectada = 'PROYECTO'
      AND h.id_entidad_afectada = p.proyecto_id
      AND h.tipo_evento = 'CREACION'
);

INSERT INTO `dev_portal_sql`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `usuario_usuario_id`)
SELECT
    'MODIFICACION', 'REPOSITORIO', r.repositorio_id,
    'RocÃ­o BeltrÃ¡n actualizÃ³ la configuraciÃ³n de OAuth2 en el repositorio backend-api',
    '2024-10-01 14:30:00', u.usuario_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`repositorio` r
WHERE u.correo = 'rocio.beltran@company.com'
  AND r.nombre_repositorio = 'backend-api'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`historial` h
    WHERE h.entidad_afectada = 'REPOSITORIO'
      AND h.id_entidad_afectada = r.repositorio_id
      AND h.fecha_evento = '2024-10-01 14:30:00'
);

INSERT INTO `dev_portal_sql`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `usuario_usuario_id`)
SELECT
    'CREACION', 'REPOSITORIO', r.repositorio_id,
    'MarÃ­a RodrÃ­guez creÃ³ el repositorio dev_portal_sql-scripts para almacenar los scripts de BD',
    '2024-09-30 16:45:00', u.usuario_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`repositorio` r
WHERE u.correo = 'maria.rodriguez@company.com'
  AND r.nombre_repositorio = 'dev_portal_sql-scripts'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`historial` h
    WHERE h.entidad_afectada = 'REPOSITORIO'
      AND h.id_entidad_afectada = r.repositorio_id
      AND h.fecha_evento = '2024-09-30 16:45:00'
);

INSERT INTO `dev_portal_sql`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `usuario_usuario_id`)
SELECT
    'MODIFICACION', 'PROYECTO', p.proyecto_id,
    'Pedro Sanchez fue invitado como colaborador al proyecto',
    '2024-09-29 10:15:00', u1.usuario_id
FROM `dev_portal_sql`.`usuario` u1, `dev_portal_sql`.`proyecto` p
WHERE u1.correo = 'pedro.sanchez@company.com'
  AND p.slug = 'teldevportal-project'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`historial` h
    WHERE h.entidad_afectada = 'PROYECTO'
      AND h.id_entidad_afectada = p.proyecto_id
      AND h.fecha_evento = '2024-09-29 10:15:00'
);

INSERT INTO `dev_portal_sql`.`historial` (`tipo_evento`, `entidad_afectada`, `id_entidad_afectada`, `descripcion_evento`, `fecha_evento`, `usuario_usuario_id`)
SELECT
    'CREACION', 'NODO', n.nodo_id,
    'Ana Torres creÃ³ la carpeta "DocumentaciÃ³n" para organizar los manuales del proyecto',
    '2024-09-28 13:20:00', u.usuario_id
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`nodo` n
WHERE u.correo = 'ana.torres@company.com'
  AND p.slug = 'teldevportal-project'
  AND n.container_type = 'PROYECTO'
  AND n.container_id = p.proyecto_id
  AND n.nombre = 'DocumentaciÃ³n'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`historial` h
    WHERE h.entidad_afectada = 'NODO'
      AND h.id_entidad_afectada = n.nodo_id
      AND h.fecha_evento = '2024-09-28 13:20:00'
);

SET FOREIGN_KEY_CHECKS = 1;

-- ================================================
-- REPOSITORIOS ADICIONALES
-- ================================================
INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'shared-utils-library', 'LibrerÃ­a de utilidades compartidas entre mÃºltiples proyectos', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'carlos.gomez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'shared-utils-library');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'experimental-ai-features', 'Repositorio experimental con funcionalidades de IA', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       u.usuario_id, NOW(), 'develop'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'maria.rodriguez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'experimental-ai-features');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'legacy-data-migration', 'Scripts de migraciÃ³n de datos heredados', 'PUBLICO', 'PERSONAL',
       u.usuario_id, u.usuario_id, NOW(), 'master'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'juan.perez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'legacy-data-migration');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'analytics-data-collector', 'Colector de datos para analytics', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'laura.martinez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'analytics-data-collector');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'analytics-dashboard-ui', 'Dashboard UI para visualizaciÃ³n de analytics', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Frontend Development' LIMIT 1),
       u.usuario_id, NOW(), 'develop'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'pedro.sanchez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'analytics-dashboard-ui');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'analytics-reporting-service', 'Servicio de reportes analÃ­ticos', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'ana.torres@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'analytics-reporting-service');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'kubernetes-deployment-configs', 'Configuraciones de despliegue en Kubernetes', 'PUBLICO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'DevOps' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'miguel.castillo@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'kubernetes-deployment-configs');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'ci-cd-pipeline-templates', 'Templates para pipelines CI/CD', 'PUBLICO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'DevOps' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'sofia.ramirez@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'ci-cd-pipeline-templates');

-- ================================================
-- PROYECTOS ADICIONALES
-- ================================================
INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_nombre`, `created_by`, `slug`, `proyecto_key`)
SELECT 'Analytics Platform', 'Plataforma completa de anÃ¡lisis de datos empresariales', 'EMPRESA', 'EN_DESARROLLO', '2024-01-15', 'PRIVADO', 'ORGANIZACION', 'TechCorp Inc', (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'cgomez' LIMIT 1), 'analytics-platform', 'AP-001'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'analytics-platform');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_nombre`, `created_by`, `slug`, `proyecto_key`)
SELECT 'DevOps Automation', 'AutomatizaciÃ³n de procesos DevOps y CI/CD', 'GRUPO', 'EN_DESARROLLO', '2024-02-01', 'PRIVADO', 'ORGANIZACION', 'DevOps Team', (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'rmendoza' LIMIT 1), 'devops-automation', 'DA-002'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'devops-automation');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_nombre`, `created_by`, `slug`, `proyecto_key`)
SELECT 'Future AI Integration', 'Proyecto futuro para integraciÃ³n de IA (en planificaciÃ³n)', 'USUARIO', 'PLANEADO', '2024-08-01', 'PRIVADO', 'RESTRINGIDO', u.username, u.usuario_id, 'future-ai-integration', 'FAI-003'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'rocio.beltran@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'future-ai-integration');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_nombre`, `created_by`, `slug`, `proyecto_key`)
SELECT 'Blockchain Research', 'InvestigaciÃ³n en tecnologÃ­as blockchain', 'GRUPO', 'EN_DESARROLLO', '2024-03-15', 'PUBLICO', 'CUALQUIER_PERSONA_CON_EL_ENLACE', 'Research Team', (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'jperez' LIMIT 1), 'blockchain-research', 'BR-004'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'blockchain-research');

-- ================================================
-- RELACIONES N:M COMPLEJAS
-- ================================================
INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'E-commerce Platform' AND r.nombre_repositorio = 'shared-utils-library'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'Mobile Banking App' AND r.nombre_repositorio = 'shared-utils-library'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'Analytics Platform' AND r.nombre_repositorio = 'shared-utils-library'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'DevOps Automation' AND r.nombre_repositorio = 'shared-utils-library'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'Analytics Platform' AND r.nombre_repositorio = 'analytics-data-collector'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'Analytics Platform' AND r.nombre_repositorio = 'analytics-dashboard-ui'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'Analytics Platform' AND r.nombre_repositorio = 'analytics-reporting-service'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'DevOps Automation' AND r.nombre_repositorio = 'kubernetes-deployment-configs'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'DevOps Automation' AND r.nombre_repositorio = 'ci-cd-pipeline-templates'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'Blockchain Research' AND r.nombre_repositorio = 'qa-automation-framework-repo'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio`
                  WHERE `proyecto_proyecto_id` = p.proyecto_id AND `repositorio_repositorio_id` = r.repositorio_id);

-- ================================================
-- RELACIONES USUARIO-REPOSITORIO
-- ================================================
INSERT INTO `dev_portal_sql`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`repositorio` r
WHERE u.correo = 'rocio.beltran@company.com'
  AND r.nombre_repositorio IN ('shared-utils-library', 'experimental-ai-features')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`repositorio` r
WHERE u.correo = 'carlos.gomez@company.com'
  AND r.nombre_repositorio = 'shared-utils-library'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'LECTOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`repositorio` r
WHERE u.correo = 'maria.rodriguez@company.com'
  AND r.nombre_repositorio = 'analytics-data-collector'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

-- ================================================
-- INSERTS ADICIONALES
-- ================================================
INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_nombre`, `created_by`, `slug`, `proyecto_key`)
SELECT 'Marketing Automation Suite', 'Suite completa de automatizaciÃ³n de marketing digital', 'EMPRESA', 'EN_DESARROLLO', '2024-02-01', 'PRIVADO', 'RESTRINGIDO', 'TechCorp Inc', (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'cgomez' LIMIT 1), 'marketing-automation-suite', 'MAS-002'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'marketing-automation-suite');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_nombre`, `created_by`, `slug`, `proyecto_key`)
SELECT 'Customer Relationship Management', 'Sistema avanzado de gestiÃ³n de relaciones con clientes', 'GRUPO', 'EN_DESARROLLO', '2024-03-01', 'PRIVADO', 'ORGANIZACION', 'CRM Team', (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'rmendoza' LIMIT 1), 'customer-relationship-management', 'CRM-003'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'customer-relationship-management');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_nombre`, `created_by`, `slug`, `proyecto_key`)
SELECT 'Financial Data Processing', 'Sistema de procesamiento y anÃ¡lisis de datos financieros en tiempo real', 'EMPRESA', 'MANTENIMIENTO', '2023-06-01', 'PRIVADO', 'RESTRINGIDO', 'TechCorp Inc', (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'mrodriguez' LIMIT 1), 'financial-data-processing', 'FDP-004'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'financial-data-processing');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_nombre`, `created_by`, `slug`, `proyecto_key`)
SELECT 'Supply Chain Management', 'Plataforma integral de gestiÃ³n de cadena de suministro', 'EMPRESA', 'PLANEADO', '2024-07-01', 'PUBLICO', 'CUALQUIER_PERSONA_CON_EL_ENLACE', 'TechCorp Inc', (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'jperez' LIMIT 1), 'supply-chain-management', 'SCM-005'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'supply-chain-management');

INSERT INTO `dev_portal_sql`.`proyecto` (`nombre_proyecto`, `descripcion_proyecto`, `propietario_proyecto`, `estado_proyecto`, `fecha_inicio_proyecto`, `visibilidad_proyecto`, `acceso_proyecto`, `propietario_nombre`, `created_by`, `slug`, `proyecto_key`)
SELECT 'Human Resources Platform', 'Plataforma completa de gestiÃ³n de recursos humanos', 'GRUPO', 'EN_DESARROLLO', '2024-01-20', 'PRIVADO', 'ORGANIZACION', 'HR Team', (SELECT usuario_id FROM `dev_portal_sql`.`usuario` WHERE username = 'lmartinez' LIMIT 1), 'human-resources-platform', 'HRP-006'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`proyecto` WHERE `slug` = 'human-resources-platform');

-- ===============================================
-- REPOSITORIOS COMPARTIDOS ADICIONALES
-- ===============================================
INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'common-auth-service', 'Servicio de autenticaciÃ³n comÃºn para todas las aplicaciones', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'diego.herrera@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'common-auth-service');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'dev_portal_sql-migration-scripts', 'Scripts de migraciÃ³n de base de datos compartidos', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'DevOps' LIMIT 1),
       u.usuario_id, NOW(), 'master'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'elena.morales@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'dev_portal_sql-migration-scripts');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'email-service-library', 'LibrerÃ­a para servicios de email y notificaciones', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'ricardo.ortiz@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'email-service-library');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'api-documentation-generator', 'Generador automÃ¡tico de documentaciÃ³n de APIs', 'PUBLICO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'carmen.vega@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'api-documentation-generator');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'monitoring-logging-framework', 'Framework de monitoreo y logging para microservicios', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'DevOps' LIMIT 1),
       u.usuario_id, NOW(), 'develop'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'javier.rios@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'monitoring-logging-framework');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'data-validation-framework', 'Framework de validaciÃ³n de datos para todas las aplicaciones', 'PUBLICO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'patricia.medina@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'data-validation-framework');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'config-management-system', 'Sistema centralizado de gestiÃ³n de configuraciones', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'DevOps' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'oscar.diaz@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'config-management-system');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'analytics-data-processor', 'Procesador de datos para anÃ¡lisis empresarial', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'teresa.guerrero@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'analytics-data-processor');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'marketing-campaign-engine', 'Motor de campaÃ±as de marketing automatizado', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'fernando.cordova@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'marketing-campaign-engine');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'customer-data-hub', 'Hub centralizado de datos de clientes', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'gabriela.paredes@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'customer-data-hub');

INSERT INTO `dev_portal_sql`.`repositorio` (`nombre_repositorio`, `descripcion_repositorio`, `visibilidad_repositorio`, `tipo_repositorio`, `propietario_id`, `creado_por_usuario_id`, `fecha_creacion`, `rama_principal_repositorio`)
SELECT 'financial-risk-calculator', 'Calculadora de riesgo financiero', 'PRIVADO', 'COLABORATIVO',
       (SELECT equipo_id FROM `dev_portal_sql`.`equipo` WHERE nombre_equipo = 'Backend Development' LIMIT 1),
       u.usuario_id, NOW(), 'main'
FROM `dev_portal_sql`.`usuario` u
WHERE u.correo = 'hector.salazar@company.com'
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`repositorio` WHERE `nombre_repositorio` = 'financial-risk-calculator');

-- ===============================================
-- RELACIONES PROYECTO-REPOSITORIO (N:M)
-- ===============================================
INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'analytics-platform' AND r.nombre_repositorio = 'common-auth-service'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'analytics-platform' AND r.nombre_repositorio = 'dev_portal_sql-migration-scripts'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'analytics-platform' AND r.nombre_repositorio = 'monitoring-logging-framework'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'analytics-platform' AND r.nombre_repositorio = 'analytics-data-processor'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'marketing-automation-suite' AND r.nombre_repositorio = 'email-service-library'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'marketing-automation-suite' AND r.nombre_repositorio = 'common-auth-service'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'marketing-automation-suite' AND r.nombre_repositorio = 'marketing-campaign-engine'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'customer-relationship-management' AND r.nombre_repositorio = 'data-validation-framework'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'customer-relationship-management' AND r.nombre_repositorio = 'email-service-library'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'customer-relationship-management' AND r.nombre_repositorio = 'customer-data-hub'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'financial-data-processing' AND r.nombre_repositorio = 'legacy-data-migration'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'financial-data-processing' AND r.nombre_repositorio = 'dev_portal_sql-migration-scripts'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'financial-data-processing' AND r.nombre_repositorio = 'data-validation-framework'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'financial-data-processing' AND r.nombre_repositorio = 'financial-risk-calculator'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'supply-chain-management' AND r.nombre_repositorio = 'kubernetes-deployment-configs'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'supply-chain-management' AND r.nombre_repositorio = 'ci-cd-pipeline-templates'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'human-resources-platform' AND r.nombre_repositorio = 'common-auth-service'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'human-resources-platform' AND r.nombre_repositorio = 'email-service-library'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.slug = 'human-resources-platform' AND r.nombre_repositorio = 'config-management-system'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'API Management System' AND r.nombre_repositorio = 'shared-utils-library'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'API Management System' AND r.nombre_repositorio = 'api-documentation-generator'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'Mobile Banking App' AND r.nombre_repositorio = 'common-auth-service'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'Payment Gateway' AND r.nombre_repositorio = 'common-auth-service'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'E-commerce Platform' AND r.nombre_repositorio = 'common-auth-service'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'E-commerce Platform' AND r.nombre_repositorio = 'email-service-library'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

INSERT INTO `dev_portal_sql`.`proyecto_has_repositorio` (`proyecto_proyecto_id`, `repositorio_repositorio_id`)
SELECT p.proyecto_id, r.repositorio_id
FROM `dev_portal_sql`.`proyecto` p, `dev_portal_sql`.`repositorio` r
WHERE p.nombre_proyecto = 'Mobile Banking App' AND r.nombre_repositorio = 'email-service-library'
  AND NOT EXISTS (
    SELECT 1 FROM `dev_portal_sql`.`proyecto_has_repositorio` phr
    WHERE phr.proyecto_proyecto_id = p.proyecto_id AND phr.repositorio_repositorio_id = r.repositorio_id
);

-- ===============================================
-- ASIGNACIONES DE USUARIOS A REPOSITORIOS
-- ===============================================
INSERT INTO `dev_portal_sql`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`repositorio` r
WHERE u.correo = 'rocio.beltran@company.com'
  AND r.nombre_repositorio IN ('common-auth-service', 'email-service-library', 'api-documentation-generator')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`repositorio` r
WHERE u.correo = 'carlos.gomez@company.com'
  AND r.nombre_repositorio IN ('monitoring-logging-framework', 'config-management-system')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`usuario_has_repositorio` (`usuario_usuario_id`, `repositorio_repositorio_id`, `privilegio_usuario_repositorio`, `fecha_usuario_repositorio`)
SELECT u.usuario_id, r.repositorio_id, 'LECTOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`repositorio` r
WHERE u.correo = 'maria.rodriguez@company.com'
  AND r.nombre_repositorio IN ('data-validation-framework', 'analytics-data-processor', 'customer-data-hub')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_repositorio` WHERE usuario_usuario_id = u.usuario_id AND repositorio_repositorio_id = r.repositorio_id);

INSERT INTO `dev_portal_sql`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'EDITOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p
WHERE u.correo = 'rocio.beltran@company.com'
  AND p.slug IN ('analytics-platform', 'marketing-automation-suite')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

INSERT INTO `dev_portal_sql`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'COMENTADOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p
WHERE u.correo = 'carlos.gomez@company.com'
  AND p.slug IN ('customer-relationship-management', 'financial-data-processing')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

INSERT INTO `dev_portal_sql`.`usuario_has_proyecto` (`usuario_usuario_id`, `proyecto_proyecto_id`, `privilegio_usuario_proyecto`, `fecha_usuario_proyecto`)
SELECT u.usuario_id, p.proyecto_id, 'LECTOR', NOW()
FROM `dev_portal_sql`.`usuario` u, `dev_portal_sql`.`proyecto` p
WHERE u.correo = 'maria.rodriguez@company.com'
  AND p.slug IN ('supply-chain-management', 'human-resources-platform')
  AND NOT EXISTS (SELECT 1 FROM `dev_portal_sql`.`usuario_has_proyecto` WHERE usuario_usuario_id = u.usuario_id AND proyecto_proyecto_id = p.proyecto_id);

-- ===============================================
-- LIMPIEZA / UPDATES
-- ===============================================
SET SQL_SAFE_UPDATES = 0;

UPDATE `dev_portal_sql`.`usuario_has_repositorio`
SET `privilegio_usuario_repositorio` = 'LECTOR'
WHERE `privilegio_usuario_repositorio` = 'COMENTADOR';

UPDATE `dev_portal_sql`.`equipo_has_repositorio`
SET `privilegio_equipo_repositorio` = 'LECTOR'
WHERE `privilegio_equipo_repositorio` = 'COMENTADOR';

SET SQL_SAFE_UPDATES = 1;

SET FOREIGN_KEY_CHECKS = 1;

