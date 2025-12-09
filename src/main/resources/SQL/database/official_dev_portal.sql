-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema dev_portal_sql
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `dev_portal_sql` ;

-- -----------------------------------------------------
-- Schema dev_portal_sql
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `dev_portal_sql` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `dev_portal_sql` ;

-- -----------------------------------------------------
-- Table `dev_portal_sql`.`usuario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`usuario` (
                                                          `usuario_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                          `nombre_usuario` VARCHAR(100) NOT NULL,
    `apellido_paterno` VARCHAR(100) NOT NULL,
    `apellido_materno` VARCHAR(100) NOT NULL,
    `dni` VARCHAR(8) NOT NULL,
    `fecha_nacimiento` DATE NULL DEFAULT NULL,
    `sexo_usuario` ENUM('HOMBRE', 'MUJER') NULL DEFAULT NULL,
    `estado_civil` ENUM('SOLTERO', 'CASADO', 'VIUDO', 'DIVORCIADO') NULL DEFAULT NULL,
    `codigo_pais` VARCHAR(5) NULL DEFAULT '+51',
    `telefono` VARCHAR(45) NULL DEFAULT NULL,
    `correo` VARCHAR(255) NOT NULL,
    `hashed_password` VARCHAR(512) NOT NULL,
    `proveedor` VARCHAR(20) NULL DEFAULT NULL COMMENT 'Proveedor de autenticacion (local, google, github, facebook)',
    `id_proveedor` VARCHAR(255) NULL DEFAULT NULL COMMENT 'ID unico del usuario en el proveedor',
    `direccion_usuario` VARCHAR(255) NOT NULL,
    `username` VARCHAR(100) NOT NULL,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ultima_conexion` DATETIME NULL DEFAULT NULL,
    `foto_perfil` VARCHAR(255) NULL DEFAULT NULL,
    `estado_usuario` ENUM('HABILITADO', 'INHABILITADO') NOT NULL,
    `actividad_usuario` ENUM('ACTIVO', 'INACTIVO') NOT NULL,
    `codigo_usuario` VARCHAR(45) NULL DEFAULT NULL,
    `acceso_usuario` ENUM('SI', 'NO') NOT NULL,
    PRIMARY KEY (`usuario_id`),
    UNIQUE INDEX `dni_unique` (`dni` ASC) VISIBLE,
    UNIQUE INDEX `username_unique` (`username` ASC) VISIBLE,
    UNIQUE INDEX `correo_unique` (`correo` ASC) VISIBLE,
    UNIQUE INDEX `codigo_usuario_unique` (`codigo_usuario` ASC) VISIBLE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`api` (
                                                      `api_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                      `nombre_api` VARCHAR(45) NOT NULL,
    `descripcion_api` TEXT NOT NULL,
    `estado_api` ENUM('BORRADOR', 'PRODUCCION', 'QA', 'DEPRECATED') NOT NULL,
    `creado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `actualizado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `actualizado_en` DATETIME NULL DEFAULT NULL,
    PRIMARY KEY (`api_id`),
    INDEX `fk_api_creado_por_idx` (`creado_por` ASC) VISIBLE,
    INDEX `fk_api_actualizado_por_idx` (`actualizado_por` ASC) VISIBLE,
    CONSTRAINT `fk_api_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_api_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`categoria`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`categoria` (
                                                            `id_categoria` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                            `nombre_categoria` VARCHAR(45) NOT NULL,
    `descripcion_categoria` VARCHAR(255) NULL DEFAULT NULL,
    `seccion_categoria` ENUM('APIS', 'PROYECTOS', 'REPOSITORIOS', 'FORO', 'OTRO') NULL DEFAULT NULL,
    PRIMARY KEY (`id_categoria`),
    INDEX `idx_id_categoria` (`id_categoria` ASC) VISIBLE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`categoria_has_api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`categoria_has_api` (
                                                                    `categoria_id_categoria` BIGINT UNSIGNED NOT NULL,
                                                                    `api_api_id` BIGINT UNSIGNED NOT NULL,
                                                                    PRIMARY KEY (`categoria_id_categoria`, `api_api_id`),
    INDEX `fk_categoria_has_api_api1_idx` (`api_api_id` ASC) VISIBLE,
    INDEX `fk_categoria_has_api_categoria1_idx` (`categoria_id_categoria` ASC) VISIBLE,
    CONSTRAINT `fk_categoria_has_api_api1`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `dev_portal_sql`.`api` (`api_id`),
    CONSTRAINT `fk_categoria_has_api_categoria1`
    FOREIGN KEY (`categoria_id_categoria`)
    REFERENCES `dev_portal_sql`.`categoria` (`id_categoria`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`proyecto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`proyecto` (
                                                           `proyecto_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                           `nombre_proyecto` VARCHAR(128) NOT NULL,
    `descripcion_proyecto` TEXT NULL DEFAULT NULL,
    `visibilidad_proyecto` ENUM('PUBLICO', 'PRIVADO') NOT NULL DEFAULT 'PRIVADO',
    `acceso_proyecto` ENUM('RESTRINGIDO', 'ORGANIZACION', 'CUALQUIER_PERSONA_CON_EL_ENLACE') NOT NULL DEFAULT 'RESTRINGIDO',
    `propietario_proyecto` ENUM('USUARIO', 'GRUPO', 'EMPRESA') NOT NULL,
    `estado_proyecto` ENUM('PLANEADO', 'EN_DESARROLLO', 'MANTENIMIENTO', 'CERRADO') NOT NULL DEFAULT 'PLANEADO',
    `fecha_inicio_proyecto` DATE NOT NULL,
    `fecha_fin_proyecto` DATE NULL DEFAULT NULL,
    `propietario_nombre` VARCHAR(128) NOT NULL COMMENT 'Nombre del propietario (usuario, grupo o empresa)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_by` BIGINT UNSIGNED NOT NULL,
    `updated_at` DATETIME NULL DEFAULT NULL,
    `updated_by` BIGINT UNSIGNED NULL DEFAULT NULL,
    `slug` VARCHAR(200) NULL DEFAULT NULL,
    `proyecto_key` VARCHAR(64) NULL DEFAULT NULL,
    `root_node_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    `web_url` VARCHAR(2048) NULL DEFAULT NULL,
    PRIMARY KEY (`proyecto_id`),
    INDEX `ix_proyecto_propietario` (`propietario_proyecto` ASC, `propietario_nombre` ASC) VISIBLE,
    INDEX `ix_proyecto_slug` (`slug` ASC) VISIBLE,
    INDEX `fk_proyecto_created_by_idx` (`created_by` ASC) VISIBLE,
    INDEX `fk_proyecto_updated_by_idx` (`updated_by` ASC) VISIBLE,
    INDEX `fk_proyecto_root_node_idx` (`root_node_id` ASC) VISIBLE,
    CONSTRAINT `fk_proyecto_created_by`
    FOREIGN KEY (`created_by`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_proyecto_updated_by`
    FOREIGN KEY (`updated_by`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_proyecto_root_node`
    FOREIGN KEY (`root_node_id`)
    REFERENCES `dev_portal_sql`.`nodo` (`nodo_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`categoria_has_proyecto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`categoria_has_proyecto` (
                                                                         `categoria_id_categoria` BIGINT UNSIGNED NOT NULL,
                                                                         `proyecto_proyecto_id` BIGINT UNSIGNED NOT NULL,
                                                                         PRIMARY KEY (`categoria_id_categoria`, `proyecto_proyecto_id`),
    INDEX `fk_categoria_has_proyecto_proyecto1_idx` (`proyecto_proyecto_id` ASC) VISIBLE,
    INDEX `fk_categoria_has_proyecto_categoria1_idx` (`categoria_id_categoria` ASC) VISIBLE,
    CONSTRAINT `fk_categoria_has_proyecto_categoria1`
    FOREIGN KEY (`categoria_id_categoria`)
    REFERENCES `dev_portal_sql`.`categoria` (`id_categoria`),
    CONSTRAINT `fk_categoria_has_proyecto_proyecto1`
    FOREIGN KEY (`proyecto_proyecto_id`)
    REFERENCES `dev_portal_sql`.`proyecto` (`proyecto_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`repositorio`
-- -----------------------------------------------------
-- LÓGICA DE PROPIEDAD SIMPLIFICADA:
-- - tipo_repositorio='PERSONAL' → propietario_id apunta a usuario.usuario_id
-- - tipo_repositorio='COLABORATIVO' → propietario_id apunta a equipo.equipo_id
-- El campo creado_por_usuario_id indica quién creó el repositorio (siempre un usuario)
-- El propietario puede ser diferente del creador (ej: usuario crea repo y lo asigna a equipo)
-- NOTA: No hay campo estado - los repositorios activos están en la BD, si se quiere eliminar se borra físicamente
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`repositorio` (
                                                              `repositorio_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                              `nombre_repositorio` VARCHAR(128) NOT NULL,
    `descripcion_repositorio` TEXT NULL DEFAULT NULL,
    `visibilidad_repositorio` ENUM('PUBLICO', 'PRIVADO') NOT NULL DEFAULT 'PRIVADO',
    `tipo_repositorio` ENUM('PERSONAL', 'COLABORATIVO') NULL COMMENT 'PERSONAL: propietario_id=usuario_id | COLABORATIVO: propietario_id=equipo_id',
    `propietario_id` BIGINT UNSIGNED NULL COMMENT 'ID del propietario: usuario_id si PERSONAL, equipo_id si COLABORATIVO',
    `creado_por_usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL COMMENT 'Usuario que creó el repositorio inicialmente',
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `actualizado_por_usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    `fecha_actualizacion` DATETIME NULL DEFAULT NULL,
    `rama_principal_repositorio` VARCHAR(45) NOT NULL DEFAULT 'main',
    `last_activity_at` DATETIME NULL DEFAULT NULL,
    `size_bytes` BIGINT UNSIGNED NULL DEFAULT NULL,
    `ultimo_commit_hash` VARCHAR(128) NULL DEFAULT NULL,
    `is_fork` TINYINT(1) NOT NULL DEFAULT '0',
    `forked_from_repo_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    `license` VARCHAR(128) NULL DEFAULT NULL,
    `root_node_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    PRIMARY KEY (`repositorio_id`),
    INDEX `ix_repo_visibility` (`visibilidad_repositorio` ASC) VISIBLE,
    INDEX `ix_repo_last_activity` (`last_activity_at` ASC) VISIBLE,
    INDEX `ix_repo_tipo` (`tipo_repositorio` ASC) VISIBLE,
    INDEX `ix_repo_propietario` (`tipo_repositorio` ASC, `propietario_id` ASC) VISIBLE COMMENT 'Buscar repositorios por tipo y propietario',
    INDEX `fk_repositorio_creado_por_idx` (`creado_por_usuario_id` ASC) VISIBLE,
    INDEX `fk_repositorio_actualizado_por_idx` (`actualizado_por_usuario_id` ASC) VISIBLE,
    INDEX `fk_repositorio_root_node_idx` (`root_node_id` ASC) VISIBLE,
    CONSTRAINT `fk_repositorio_creado_por`
    FOREIGN KEY (`creado_por_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_repositorio_actualizado_por`
    FOREIGN KEY (`actualizado_por_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_repositorio_root_node`
    FOREIGN KEY (`root_node_id`)
    REFERENCES `dev_portal_sql`.`nodo` (`nodo_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`categoria_has_repositorio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`categoria_has_repositorio` (
                                                                            `categoria_id_categoria` BIGINT UNSIGNED NOT NULL,
                                                                            `repositorio_repositorio_id` BIGINT UNSIGNED NOT NULL,
                                                                            PRIMARY KEY (`categoria_id_categoria`, `repositorio_repositorio_id`),
    INDEX `fk_categoria_has_repositorio_repositorio1_idx` (`repositorio_repositorio_id` ASC) VISIBLE,
    INDEX `fk_categoria_has_repositorio_categoria1_idx` (`categoria_id_categoria` ASC) VISIBLE,
    CONSTRAINT `fk_categoria_has_repositorio_categoria1`
    FOREIGN KEY (`categoria_id_categoria`)
    REFERENCES `dev_portal_sql`.`categoria` (`id_categoria`),
    CONSTRAINT `fk_categoria_has_repositorio_repositorio1`
    FOREIGN KEY (`repositorio_repositorio_id`)
    REFERENCES `dev_portal_sql`.`repositorio` (`repositorio_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`categoria_has_foro_tema`
-- Relación N:M entre categorías generales (con seccion='FORO') y temas de foro
-- Permite que un tema aparezca en múltiples categorías
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`categoria_has_foro_tema` (
                                                                          `categoria_id_categoria` BIGINT UNSIGNED NOT NULL,
                                                                          `foro_tema_tema_id` BIGINT UNSIGNED NOT NULL,
                                                                          `fecha_asignacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Cuándo se agregó el tema a esta categoría',
                                                                          PRIMARY KEY (`categoria_id_categoria`, `foro_tema_tema_id`),
    INDEX `fk_categoria_has_foro_tema_foro_tema1_idx` (`foro_tema_tema_id` ASC) VISIBLE,
    INDEX `fk_categoria_has_foro_tema_categoria1_idx` (`categoria_id_categoria` ASC) VISIBLE,
    CONSTRAINT `fk_categoria_has_foro_tema_categoria1`
    FOREIGN KEY (`categoria_id_categoria`)
    REFERENCES `dev_portal_sql`.`categoria` (`id_categoria`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_categoria_has_foro_tema_foro_tema1`
    FOREIGN KEY (`foro_tema_tema_id`)
    REFERENCES `dev_portal_sql`.`foro_tema` (`tema_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`clasificacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`clasificacion` (
                                                                `clasificacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                `tipo_contenido_texto` VARCHAR(45) NULL DEFAULT NULL,
    PRIMARY KEY (`clasificacion_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`documentacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`documentacion` (
                                                                `documentacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                `seccion_documentacion` VARCHAR(128) NULL DEFAULT NULL,
    `api_api_id` BIGINT UNSIGNED NOT NULL,
    `creado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `actualizado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `actualizado_en` DATETIME NULL DEFAULT NULL,
    PRIMARY KEY (`documentacion_id`),
    INDEX `fk_documentacion_api1_idx` (`api_api_id` ASC) VISIBLE,
    INDEX `fk_documentacion_creado_por_idx` (`creado_por` ASC) VISIBLE,
    INDEX `fk_documentacion_actualizado_por_idx` (`actualizado_por` ASC) VISIBLE,
    CONSTRAINT `fk_documentacion_api1`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `dev_portal_sql`.`api` (`api_id`),
    CONSTRAINT `fk_documentacion_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_documentacion_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`version_api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`version_api` (
                                                              `version_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                              `numero_version` VARCHAR(45) NOT NULL,
    `descripcion_version` TEXT NULL DEFAULT NULL,
    `contrato_api_url` TEXT NOT NULL,
    `fecha_lanzamiento` DATE NOT NULL,
    `api_api_id` BIGINT UNSIGNED NOT NULL,
    `documentacion_documentacion_id` BIGINT UNSIGNED NOT NULL,
    `creado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `actualizado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `actualizado_en` DATETIME NULL DEFAULT NULL,
    PRIMARY KEY (`version_id`),
    INDEX `fk_version_api_api1_idx` (`api_api_id` ASC) VISIBLE,
    INDEX `fk_version_api_documentacion1_idx` (`documentacion_documentacion_id` ASC) VISIBLE,
    INDEX `fk_version_api_creado_por_idx` (`creado_por` ASC) VISIBLE,
    INDEX `fk_version_api_actualizado_por_idx` (`actualizado_por` ASC) VISIBLE,
    CONSTRAINT `fk_version_api_api1`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `dev_portal_sql`.`api` (`api_id`),
    CONSTRAINT `fk_version_api_documentacion1`
    FOREIGN KEY (`documentacion_documentacion_id`)
    REFERENCES `dev_portal_sql`.`documentacion` (`documentacion_id`),
    CONSTRAINT `fk_version_api_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_version_api_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


USE dev_portal_sql;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`solicitud_publicacion_version_api`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`solicitud_publicacion_version_api` (
  `solicitud_publicacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  
  -- RELACIONES
  `api_id` BIGINT UNSIGNED NOT NULL,
  `version_api_id` BIGINT UNSIGNED NOT NULL,
  
  -- ACTORES
  `generado_por` BIGINT UNSIGNED NOT NULL,
  `aprobado_por` BIGINT UNSIGNED NULL,
  
  -- ESTADO
  `estado` ENUM(
    'PENDIENTE',
    'EN_REVISION',
    'APROBADO',
    'RECHAZADO',
    'CANCELADO'
  ) NOT NULL DEFAULT 'PENDIENTE',
  
  -- FECHA
  `fecha_resolucion` DATETIME NULL,
  
  -- AUDIT
  `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`solicitud_publicacion_id`),
  
  -- ÍNDICES
  INDEX `fk_solicitud_api_idx` (`api_id`),
  INDEX `fk_solicitud_version_idx` (`version_api_id`),
  INDEX `fk_generado_por_idx` (`generado_por`),
  INDEX `fk_aprobado_por_idx` (`aprobado_por`),
  INDEX `idx_estado` (`estado`),
  
  -- FOREIGN KEYS
  CONSTRAINT `fk_solicitud_pub_api`
    FOREIGN KEY (`api_id`) 
    REFERENCES `api` (`api_id`)
    ON DELETE CASCADE,
    
  CONSTRAINT `fk_solicitud_pub_version`
    FOREIGN KEY (`version_api_id`) 
    REFERENCES `version_api` (`version_id`)
    ON DELETE CASCADE,
    
  CONSTRAINT `fk_solicitud_pub_generado_por`
    FOREIGN KEY (`generado_por`) 
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE RESTRICT,
    
  CONSTRAINT `fk_solicitud_pub_aprobado_por`
    FOREIGN KEY (`aprobado_por`) 
    REFERENCES `usuario` (`usuario_id`)
    ON DELETE SET NULL
    
) ENGINE = InnoDB 
  DEFAULT CHARACTER SET = utf8mb4 
  COLLATE = utf8mb4_0900_ai_ci;




-- -----------------------------------------------------
-- Table `dev_portal_sql`.`contenido`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`contenido` (
                                                            `contenido_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                            `titulo_contenido` VARCHAR(255) NOT NULL COMMENT 'Título de la sección (ej: "Introducción", "Video tutorial")',
    `orden` INT NOT NULL DEFAULT 0 COMMENT 'Orden de presentación (1, 2, 3... para secuencia)',
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `clasificacion_clasificacion_id` BIGINT UNSIGNED NOT NULL,
    `documentacion_documentacion_id` BIGINT UNSIGNED NOT NULL,
    `version_api_version_id` BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (`contenido_id`),
    INDEX `fk_contenido_clasificacion1_idx` (`clasificacion_clasificacion_id` ASC) VISIBLE,
    INDEX `fk_contenido_documentacion1_idx` (`documentacion_documentacion_id` ASC) VISIBLE,
    INDEX `fk_contenido_version_api1_idx` (`version_api_version_id` ASC) VISIBLE,
    INDEX `idx_documentacion_orden` (`documentacion_documentacion_id` ASC, `orden` ASC) VISIBLE COMMENT 'Ordenar secciones de una documentación',
    CONSTRAINT `fk_contenido_clasificacion1`
    FOREIGN KEY (`clasificacion_clasificacion_id`)
    REFERENCES `dev_portal_sql`.`clasificacion` (`clasificacion_id`),
    CONSTRAINT `fk_contenido_documentacion1`
    FOREIGN KEY (`documentacion_documentacion_id`)
    REFERENCES `dev_portal_sql`.`documentacion` (`documentacion_id`),
    CONSTRAINT `fk_contenido_version_api1`
    FOREIGN KEY (`version_api_version_id`)
    REFERENCES `dev_portal_sql`.`version_api` (`version_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`credencial_api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`credencial_api` (
                                                                 `credencial_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                 `entorno_credencial` ENUM('SANDBOX', 'QA', 'PROD') NOT NULL,
    `tipo_credencial` ENUM('API_KEY', 'OAUTH_CLIENT') NOT NULL,
    `valor_publico` VARCHAR(255) NOT NULL,
    `valor_secreto_hash` VARCHAR(255) NOT NULL,
    `estado_credencial` ENUM('ACTIVO', 'REVOCADO') NOT NULL DEFAULT 'ACTIVO',
    `fecha_creacion_credencial` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_vencimiento_credencial` DATETIME NULL DEFAULT NULL,
    `api_api_id` BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (`credencial_id`),
    INDEX `fk_credencial_api1_idx` (`api_api_id` ASC) VISIBLE,
    CONSTRAINT `fk_credencial_api1`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `dev_portal_sql`.`api` (`api_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`enlace` (HÍBRIDA: SQL + MongoDB)
-- -----------------------------------------------------
-- ARQUITECTURA HÍBRIDA POLIMÓRFICA
--
-- SQL almacena:
--   - Metadatos buscables (contexto, tipo, estado)
--   - URL básica del archivo
--   - Fechas y auditoría
--
-- MongoDB almacena (collection: file_metadata):
--   - Metadata compleja (checksum, tamaño, mime_type, encoding)
--   - Historial de versiones del archivo
--   - Metadata específica por tipo (duración video, dimensiones imagen)
--   - TTL automático para archivos temporales
--
-- ¿Por qué NO tiene FKs físicas a proyecto/repositorio/nodo/contenido?
-- - POLIMORFISMO: Soporta cualquier entidad sin ALTER TABLE
-- - FLEXIBILIDAD: Agregar nuevos contextos sin migración
-- - PERFORMANCE: Un índice vs múltiples FKs NULL
--
-- Integridad garantizada en código (service layer + transacciones)
--
-- Ejemplo MongoDB:
-- {
--   "_id": ObjectId("..."),
--   "enlace_id": 12345,
--   "file_metadata": {
--     "size_bytes": 52428800,
--     "checksum_sha256": "abc123...",
--     "mime_type": "video/mp4",
--     "encoding": "H.264",
--     "duration_seconds": 180,
--     "resolution": "1920x1080"
--   },
--   "versions": [
--     { "version": 2, "url": "s3://.../file-v2.mp4", "uploaded_at": ISODate("...") },
--     { "version": 1, "url": "s3://.../file-v1.mp4", "uploaded_at": ISODate("...") }
--   ],
--   "expireAt": ISODate("2026-01-20T00:00:00Z")  // TTL para archivos temporales
-- }
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`enlace` (
                                                         `enlace_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                         `direccion_almacenamiento` TEXT NOT NULL COMMENT 'URL principal del archivo (S3, GCS, Azure Blob)',
                                                         `nombre_archivo` VARCHAR(255) NOT NULL COMMENT 'Nombre del archivo original (ej: tutorial.mp4)',
    `fecha_creacion_enlace` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_enlace` DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `contexto_type` ENUM('REPOSITORIO', 'PROYECTO', 'NODO', 'FILE_VERSION', 'CONTENIDO', 'RECURSO', 'DOCUMENTACION', 'API', 'TICKET') NOT NULL COMMENT 'Tipo de entidad padre',
    `contexto_id` BIGINT UNSIGNED NOT NULL COMMENT 'ID de la entidad padre (sin FK física - validado en código)',
    `tipo_enlace` ENUM('STORAGE', 'METADATA', 'THUMBNAIL', 'BACKUP', 'TEMPORAL') NOT NULL DEFAULT 'STORAGE' COMMENT 'Propósito del enlace',
    `estado_enlace` ENUM('ACTIVO', 'ARCHIVADO', 'ELIMINADO', 'PROCESANDO') NOT NULL DEFAULT 'ACTIVO' COMMENT 'Estado del archivo',
    `nosql_metadata_id` VARCHAR(24) NULL DEFAULT NULL COMMENT 'ObjectId de MongoDB (collection: file_metadata) para metadata compleja',
    `creado_por` BIGINT UNSIGNED NULL DEFAULT NULL COMMENT 'Usuario que subió el archivo',
    PRIMARY KEY (`enlace_id`),
    INDEX `idx_enlace_contexto` (`contexto_type` ASC, `contexto_id` ASC) VISIBLE COMMENT 'Búsqueda: todos los archivos de un proyecto',
    INDEX `idx_enlace_tipo` (`tipo_enlace` ASC, `estado_enlace` ASC) VISIBLE COMMENT 'Búsqueda por tipo y estado',
    INDEX `idx_nosql_metadata` (`nosql_metadata_id` ASC) VISIBLE COMMENT 'Validación de integridad con MongoDB',
    INDEX `fk_enlace_creado_por_idx` (`creado_por` ASC) VISIBLE,
    UNIQUE INDEX `uk_contexto_direccion` (`contexto_type` ASC, `contexto_id` ASC, `direccion_almacenamiento`(255) ASC) VISIBLE COMMENT 'Previene duplicados del mismo archivo',
    CONSTRAINT `fk_enlace_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
                                                           ON DELETE SET NULL
                                                           ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`equipo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`equipo` (
                                                         `equipo_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                         `nombre_equipo` VARCHAR(45) NOT NULL,
    `creado_por_usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`equipo_id`),
    INDEX `fk_equipo_creado_por_idx` (`creado_por_usuario_id` ASC) VISIBLE,
    CONSTRAINT `fk_equipo_creado_por`
    FOREIGN KEY (`creado_por_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`equipo_has_proyecto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`equipo_has_proyecto` (
                                                                      `equipo_equipo_id` BIGINT UNSIGNED NOT NULL,
                                                                      `proyecto_proyecto_id` BIGINT UNSIGNED NOT NULL,
                                                                      `privilegio_equipo_proyecto` ENUM('LECTOR', 'EDITOR', 'COMENTADOR', 'ADMINISTRADOR', 'PERSONALIZADO') NOT NULL DEFAULT 'LECTOR',
    `fecha_equipo_proyecto` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`equipo_equipo_id`, `proyecto_proyecto_id`),
    INDEX `fk_equipo_has_proyecto_proyecto1_idx` (`proyecto_proyecto_id` ASC) VISIBLE,
    INDEX `fk_equipo_has_proyecto_equipo1_idx` (`equipo_equipo_id` ASC) VISIBLE,
    CONSTRAINT `fk_equipo_has_proyecto_equipo1`
    FOREIGN KEY (`equipo_equipo_id`)
    REFERENCES `dev_portal_sql`.`equipo` (`equipo_id`),
    CONSTRAINT `fk_equipo_has_proyecto_proyecto1`
    FOREIGN KEY (`proyecto_proyecto_id`)
    REFERENCES `dev_portal_sql`.`proyecto` (`proyecto_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`equipo_has_repositorio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`equipo_has_repositorio` (
                                                                         `equipo_equipo_id` BIGINT UNSIGNED NOT NULL,
                                                                         `repositorio_repositorio_id` BIGINT UNSIGNED NOT NULL,
                                                                         `privilegio_equipo_repositorio` ENUM('LECTOR', 'EDITOR', 'ADMINISTRADOR', 'PERSONALIZADO') NOT NULL DEFAULT 'LECTOR',
    `fecha_equipo_repositorio` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`equipo_equipo_id`, `repositorio_repositorio_id`),
    INDEX `fk_equipo_has_repositorio_repositorio1_idx` (`repositorio_repositorio_id` ASC) VISIBLE,
    INDEX `fk_equipo_has_repositorio_equipo1_idx` (`equipo_equipo_id` ASC) VISIBLE,
    CONSTRAINT `fk_equipo_has_repositorio_equipo1`
    FOREIGN KEY (`equipo_equipo_id`)
    REFERENCES `dev_portal_sql`.`equipo` (`equipo_id`),
    CONSTRAINT `fk_equipo_has_repositorio_repositorio1`
    FOREIGN KEY (`repositorio_repositorio_id`)
    REFERENCES `dev_portal_sql`.`repositorio` (`repositorio_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`etiqueta`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`etiqueta` (
                                                           `tag_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                           `nombre_tag` VARCHAR(45) NOT NULL,
    PRIMARY KEY (`tag_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`etiqueta_has_api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`etiqueta_has_api` (
                                                                   `etiqueta_tag_id` BIGINT UNSIGNED NOT NULL,
                                                                   `api_api_id` BIGINT UNSIGNED NOT NULL,
                                                                   PRIMARY KEY (`etiqueta_tag_id`, `api_api_id`),
    INDEX `fk_etiqueta_has_api_api1_idx` (`api_api_id` ASC) VISIBLE,
    INDEX `fk_etiqueta_has_api_etiqueta1_idx` (`etiqueta_tag_id` ASC) VISIBLE,
    CONSTRAINT `fk_etiqueta_has_api_api1`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `dev_portal_sql`.`api` (`api_id`),
    CONSTRAINT `fk_etiqueta_has_api_etiqueta1`
    FOREIGN KEY (`etiqueta_tag_id`)
    REFERENCES `dev_portal_sql`.`etiqueta` (`tag_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`faq`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`faq` (
                                                      `faq_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                      `pregunta` TEXT NOT NULL,
                                                      `respuesta` TEXT NOT NULL,
                                                      `categoria_faq` ENUM('GENERAL', 'API', 'PROYECTO', 'REPOSITORIO', 'AUTENTICACION', 'TICKET', 'FORO', 'OTRO') NOT NULL DEFAULT 'GENERAL' COMMENT 'Categoría de la pregunta',
    `orden` INT NOT NULL DEFAULT 0 COMMENT 'Orden de presentación en la sección',
    `activo` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Si la FAQ está visible',
    `vistas` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Contador de visualizaciones',
    `autor_id` BIGINT UNSIGNED NULL DEFAULT NULL COMMENT 'Usuario que creó la FAQ',
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion` DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`faq_id`),
    INDEX `idx_categoria_orden` (`categoria_faq` ASC, `orden` ASC, `activo` ASC) VISIBLE COMMENT 'Listar FAQs ordenadas por categoría',
    INDEX `fk_faq_autor_idx` (`autor_id` ASC) VISIBLE,
    CONSTRAINT `fk_faq_autor`
    FOREIGN KEY (`autor_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
                                                    ON DELETE SET NULL
                                                    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`feedback` (HÍBRIDA: SQL + MongoDB)
-- -----------------------------------------------------
-- ARQUITECTURA HÍBRIDA PARA FEEDBACK COMPLETO
--
-- SQL almacena:
--   - Metadatos del feedback (puntuación, tipo, estado, fechas)
--   - Comentario breve
--   - Referencias (usuario, documentación)
--
-- MongoDB almacena (collection: feedback_details):
--   - Adjuntos (capturas de pantalla, logs, videos)
--   - Metadata del navegador (user agent, resolución, SO)
--   - Información de reproducción (pasos para replicar bug)
--   - Datos técnicos (versión de API, errores de consola)
--
-- Ejemplo MongoDB:
-- {
--   "_id": ObjectId("..."),
--   "feedback_id": 12345,
--   "adjuntos": [
--     {
--       "tipo": "SCREENSHOT",
--       "nombre": "error-screenshot.png",
--       "url": "s3://bucket/feedback/screenshot.png",
--       "size_bytes": 524288
--     },
--     {
--       "tipo": "LOG",
--       "nombre": "console-errors.txt",
--       "contenido": "TypeError: Cannot read property..."
--     }
--   ],
--   "metadata_navegador": {
--     "user_agent": "Mozilla/5.0...",
--     "resolucion": "1920x1080",
--     "navegador": "Chrome 119.0",
--     "sistema_operativo": "Windows 11"
--   },
--   "reproduccion_bug": {
--     "pasos": [
--       "1. Ir a /api/payments",
--       "2. Hacer click en 'Ver documentación'",
--       "3. Error aparece al cargar ejemplos"
--     ],
--     "esperado": "Debería mostrar ejemplos de código",
--     "obtenido": "Error 500 Internal Server Error"
--   },
--   "datos_tecnicos": {
--     "api_version": "2.0.1",
--     "endpoint_afectado": "/api/payments/charge",
--     "errores_consola": ["TypeError...", "CORS error..."]
--   }
-- }
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`feedback` (
                                                           `feedback_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                           `comentario` TEXT NOT NULL,
                                                           `puntuacion` DECIMAL(2,1) NOT NULL COMMENT 'Calificación de 0.0 a 5.0',
    `tipo_feedback` ENUM('BUG', 'SUGERENCIA', 'ELOGIO', 'PREGUNTA', 'OTRO') NOT NULL DEFAULT 'OTRO' COMMENT 'Tipo de feedback',
    `fecha_feedback` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `estado_feedback` ENUM('PENDIENTE', 'REVISADO', 'RESUELTO', 'ARCHIVADO') NOT NULL DEFAULT 'PENDIENTE' COMMENT 'Estado del feedback',
    `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
    `documentacion_documentacion_id` BIGINT UNSIGNED NOT NULL,
    `revisado_por` BIGINT UNSIGNED NULL DEFAULT NULL COMMENT 'Usuario SA/PO que revisó',
    `fecha_revision` DATETIME NULL DEFAULT NULL,
    `nosql_detalle_id` VARCHAR(24) NULL DEFAULT NULL COMMENT 'ObjectId de MongoDB (collection: feedback_details) para adjuntos y metadata',
    PRIMARY KEY (`feedback_id`),
    INDEX `fk_feedback_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
    INDEX `fk_feedback_documentacion1_idx` (`documentacion_documentacion_id` ASC) VISIBLE,
    INDEX `fk_feedback_revisado_por_idx` (`revisado_por` ASC) VISIBLE,
    INDEX `idx_tipo_estado` (`tipo_feedback` ASC, `estado_feedback` ASC) VISIBLE COMMENT 'Filtrar por tipo y estado',
    INDEX `idx_fecha` (`fecha_feedback` DESC) VISIBLE COMMENT 'Ordenar por fecha',
    INDEX `idx_nosql_detalle` (`nosql_detalle_id` ASC) VISIBLE COMMENT 'Validación con MongoDB',
    CONSTRAINT `fk_feedback_documentacion1`
    FOREIGN KEY (`documentacion_documentacion_id`)
    REFERENCES `dev_portal_sql`.`documentacion` (`documentacion_id`),
    CONSTRAINT `fk_feedback_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`),
    CONSTRAINT `fk_feedback_revisado_por`
    FOREIGN KEY (`revisado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`chatbot_conversacion` (HÍBRIDA: SQL + MongoDB)
-- -----------------------------------------------------
-- ARQUITECTURA HÍBRIDA PARA CHATBOT CON IA
--
-- SQL almacena:
--   - Metadatos de la conversación (usuario, estado, fechas, tema)
--   - Estadísticas (mensajes_count, tokens_usados)
--   - Búsquedas y reportes
--
-- MongoDB almacena (collection: chatbot_messages):
--   - Mensajes completos (usuario + IA)
--   - Context windows para IA
--   - Embeddings de mensajes
--   - Adjuntos y payloads
--
-- NOTA: El servicio de IA (OpenAI, Claude, etc.) es EXTERNO
-- Esta tabla solo registra las conversaciones, no gestiona la IA
--
-- Ejemplo MongoDB:
-- {
--   "_id": ObjectId("..."),
--   "conversacion_id": 12345,
--   "mensajes": [
--     {
--       "mensaje_id": 1,
--       "remitente": "USUARIO",
--       "contenido": "¿Cómo implemento OAuth2?",
--       "timestamp": ISODate("2025-10-22T10:00:00Z"),
--       "tokens": 15
--     },
--     {
--       "mensaje_id": 2,
--       "remitente": "IA",
--       "contenido": "Para implementar OAuth2...",
--       "timestamp": ISODate("2025-10-22T10:00:05Z"),
--       "tokens": 120,
--       "modelo": "gpt-4",
--       "confidence": 0.95
--     }
--   ],
--   "context_window": [...],  // Últimos 10 mensajes para IA
--   "expireAt": ISODate("2026-01-20T00:00:00Z")  // TTL 90 días
-- }
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`chatbot_conversacion` (
                                                                       `conversacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                       `usuario_id` BIGINT UNSIGNED NOT NULL COMMENT 'Usuario que inició la conversación',
                                                                       `titulo_conversacion` VARCHAR(255) NULL DEFAULT NULL COMMENT 'Título auto-generado o manual',
    `estado_conversacion` ENUM('ACTIVA', 'CERRADA', 'ARCHIVADA') NOT NULL DEFAULT 'ACTIVA',
    `tema_conversacion` ENUM('API', 'PROYECTO', 'REPOSITORIO', 'AUTENTICACION', 'ERROR', 'GENERAL') NULL DEFAULT 'GENERAL' COMMENT 'Tema principal detectado',
    `fecha_inicio` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_ultimo_mensaje` DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `mensajes_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Contador de mensajes',
    `tokens_totales_usados` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Tokens consumidos (para billing)',
    `modelo_ia_usado` VARCHAR(50) NULL DEFAULT NULL COMMENT 'Modelo de IA (gpt-4, claude-3, etc.)',
    `nosql_mensajes_id` VARCHAR(24) NULL DEFAULT NULL COMMENT 'ObjectId de MongoDB (collection: chatbot_messages)',
    `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`conversacion_id`),
    INDEX `fk_chatbot_usuario_idx` (`usuario_id` ASC) VISIBLE,
    INDEX `idx_estado_fecha` (`estado_conversacion` ASC, `fecha_ultimo_mensaje` DESC) VISIBLE COMMENT 'Listar conversaciones recientes',
    INDEX `idx_tema` (`tema_conversacion` ASC) VISIBLE COMMENT 'Filtrar por tema',
    INDEX `idx_nosql_mensajes` (`nosql_mensajes_id` ASC) VISIBLE COMMENT 'Validación con MongoDB',
    CONSTRAINT `fk_chatbot_usuario`
    FOREIGN KEY (`usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
                                                      ON DELETE CASCADE
                                                      ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`notificacion` (HÍBRIDA: SQL + MongoDB)
-- -----------------------------------------------------
-- ARQUITECTURA HÍBRIDA PARA NOTIFICACIONES
--
-- SQL almacena:
--   - Metadatos básicos (usuario, tipo, estado leído, fecha)
--   - Búsquedas rápidas: "notificaciones no leídas del usuario X"
--
-- MongoDB almacena (collection: notification_payload):
--   - Payload completo de la notificación
--   - Entidades relacionadas (API, proyecto, usuario origen)
--   - Enlaces de acción (URLs para redirigir)
--   - Metadata rica (avatares, previews, contexto)
--
-- Ejemplos de notificaciones:
-- 1. "cgomez solicitó crear nueva versión para tu API de Pagos"
--    → tipo: SOLICITUD_VERSION, entidad: api_id=42
--
-- 2. "mlopez calificó tu documentación con 4.5 estrellas"
--    → tipo: FEEDBACK, entidad: documentacion_id=15
--
-- 3. "Tu ticket #1234 fue resuelto por soporte"
--    → tipo: TICKET_RESUELTO, entidad: ticket_id=1234
--
-- MongoDB ejemplo:
-- {
--   "_id": ObjectId("..."),
--   "notificacion_id": 12345,
--   "payload": {
--     "titulo": "Nueva solicitud de versión",
--     "mensaje": "cgomez solicitó crear nueva versión para tu API de Pagos",
--     "usuario_origen": {
--       "usuario_id": 42,
--       "nombre": "Carlos Gomez",
--       "avatar_url": "https://..."
--     },
--     "entidad_relacionada": {
--       "tipo": "api",
--       "id": 100,
--       "nombre": "API de Pagos",
--       "url": "/api/100"
--     },
--     "accion": {
--       "texto": "Ver solicitud",
--       "url": "/solicitudes/5678",
--       "tipo": "PRIMARY"
--     },
--     "metadata": {
--       "icono": "fa-code-branch",
--       "color": "#3498db",
--       "prioridad": "ALTA"
--     }
--   },
--   "expireAt": ISODate("2026-01-20T00:00:00Z")  // TTL 90 días
-- }
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`notificacion` (
                                                               `notificacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                               `usuario_id` BIGINT UNSIGNED NOT NULL COMMENT 'Usuario que recibe la notificación',
                                                               `tipo_notificacion` ENUM('SOLICITUD_VERSION', 'FEEDBACK', 'TICKET_RESUELTO', 'TICKET_COMENTARIO', 'FORO_RESPUESTA', 'FORO_MENCION', 'API_ACTUALIZADA', 'PROYECTO_INVITACION', 'SISTEMA', 'OTRO') NOT NULL COMMENT 'Tipo de notificación',
    `leida` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Si la notificación fue leída',
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_lectura` DATETIME NULL DEFAULT NULL COMMENT 'Cuándo fue leída',
    `prioridad` ENUM('BAJA', 'NORMAL', 'ALTA', 'URGENTE') NOT NULL DEFAULT 'NORMAL',
    `nosql_payload_id` VARCHAR(24) NULL DEFAULT NULL COMMENT 'ObjectId de MongoDB (collection: notification_payload)',
    PRIMARY KEY (`notificacion_id`),
    INDEX `fk_notificacion_usuario_idx` (`usuario_id` ASC) VISIBLE,
    INDEX `idx_leida_fecha` (`usuario_id` ASC, `leida` ASC, `fecha_creacion` DESC) VISIBLE COMMENT 'Buscar notificaciones no leídas',
    INDEX `idx_tipo` (`tipo_notificacion` ASC, `fecha_creacion` DESC) VISIBLE COMMENT 'Filtrar por tipo',
    INDEX `idx_nosql_payload` (`nosql_payload_id` ASC) VISIBLE COMMENT 'Validación con MongoDB',
    CONSTRAINT `fk_notificacion_usuario`
    FOREIGN KEY (`usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`foro_tema`
-- NOTA: Usa la tabla general 'categoria' con seccion='FORO'
--       La relación N:M se gestiona en categoria_has_foro_tema
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`foro_tema` (
                                                            `tema_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                            `autor_usuario_id` BIGINT UNSIGNED NOT NULL,
                                                            `titulo_tema` VARCHAR(500) NOT NULL,
    `contenido_tema` TEXT NOT NULL,
    `slug` VARCHAR(600) NOT NULL,
    `estado_tema` ENUM('ABIERTO', 'CERRADO', 'RESUELTO', 'ARCHIVADO') NOT NULL DEFAULT 'ABIERTO',
    `es_anclado` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Temas destacados/sticky',
    `es_bloqueado` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'No permite nuevas respuestas',
    `vistas` INT UNSIGNED NOT NULL DEFAULT 0,
    `respuestas_count` INT UNSIGNED NOT NULL DEFAULT 0,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_actualizacion` DATETIME NULL DEFAULT NULL,
    `ultima_respuesta_fecha` DATETIME NULL DEFAULT NULL,
    `ultima_respuesta_usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    PRIMARY KEY (`tema_id`),
    INDEX `fk_tema_autor_idx` (`autor_usuario_id` ASC) VISIBLE,
    INDEX `fk_tema_ultima_respuesta_usuario_idx` (`ultima_respuesta_usuario_id` ASC) VISIBLE,
    INDEX `idx_anclado_fecha` (`es_anclado` DESC, `ultima_respuesta_fecha` DESC) VISIBLE,
    INDEX `idx_estado` (`estado_tema` ASC) VISIBLE,
    UNIQUE INDEX `uk_slug` (`slug` ASC) VISIBLE,
    CONSTRAINT `fk_tema_autor`
    FOREIGN KEY (`autor_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_tema_ultima_respuesta_usuario`
    FOREIGN KEY (`ultima_respuesta_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`foro_respuesta`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`foro_respuesta` (
                                                                 `respuesta_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                 `tema_id` BIGINT UNSIGNED NOT NULL,
                                                                 `parent_respuesta_id` BIGINT UNSIGNED NULL DEFAULT NULL COMMENT 'Para respuestas anidadas (threading)',
                                                                 `autor_usuario_id` BIGINT UNSIGNED NOT NULL,
                                                                 `contenido_respuesta` TEXT NOT NULL,
                                                                 `es_solucion` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Marca la respuesta como solución aceptada',
    `votos_positivos` INT NOT NULL DEFAULT 0,
    `votos_negativos` INT NOT NULL DEFAULT 0,
    `puntuacion_total` INT NOT NULL DEFAULT 0 COMMENT 'votos_positivos - votos_negativos',
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion` DATETIME NULL DEFAULT NULL,
    `editado` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`respuesta_id`),
    INDEX `fk_respuesta_tema_idx` (`tema_id` ASC) VISIBLE,
    INDEX `fk_respuesta_parent_idx` (`parent_respuesta_id` ASC) VISIBLE,
    INDEX `fk_respuesta_autor_idx` (`autor_usuario_id` ASC) VISIBLE,
    INDEX `idx_tema_fecha` (`tema_id` ASC, `fecha_creacion` ASC) VISIBLE,
    INDEX `idx_tema_puntuacion` (`tema_id` ASC, `puntuacion_total` DESC) VISIBLE,
    INDEX `idx_solucion` (`es_solucion` ASC) VISIBLE,
    CONSTRAINT `fk_respuesta_tema`
    FOREIGN KEY (`tema_id`)
    REFERENCES `dev_portal_sql`.`foro_tema` (`tema_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_respuesta_parent`
    FOREIGN KEY (`parent_respuesta_id`)
    REFERENCES `dev_portal_sql`.`foro_respuesta` (`respuesta_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_respuesta_autor`
    FOREIGN KEY (`autor_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`foro_voto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`foro_voto` (
                                                            `voto_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                            `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
                                                            `respuesta_id` BIGINT UNSIGNED NOT NULL,
                                                            `tipo_voto` ENUM('POSITIVO', 'NEGATIVO') NOT NULL,
    `fecha_voto` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`voto_id`),
    UNIQUE INDEX `uk_usuario_respuesta` (`usuario_usuario_id` ASC, `respuesta_id` ASC) VISIBLE COMMENT 'Un usuario solo puede votar una vez por respuesta',
    INDEX `fk_voto_respuesta_idx` (`respuesta_id` ASC) VISIBLE,
    CONSTRAINT `fk_voto_usuario`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_voto_respuesta`
    FOREIGN KEY (`respuesta_id`)
    REFERENCES `dev_portal_sql`.`foro_respuesta` (`respuesta_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`nodo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`nodo` (
                                                       `nodo_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                       `container_type` ENUM('PROYECTO', 'REPOSITORIO') NOT NULL,
    `container_id` BIGINT UNSIGNED NOT NULL,
    `parent_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    `nombre` VARCHAR(255) NOT NULL,
    `tipo` ENUM('CARPETA', 'ARCHIVO') NOT NULL,
    `path` VARCHAR(2000) NOT NULL COMMENT 'Ruta completa desde la raíz del contenedor',
    `descripcion` TEXT NULL DEFAULT NULL,
    `size_bytes` BIGINT UNSIGNED NULL DEFAULT '0',
    `mime_type` VARCHAR(255) NULL DEFAULT NULL,
    `creado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `actualizado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `actualizado_en` DATETIME NULL DEFAULT NULL,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT '0',
    `deleted_at` DATETIME NULL DEFAULT NULL,
    PRIMARY KEY (`nodo_id`),
    INDEX `idx_container` (`container_type` ASC, `container_id` ASC) VISIBLE,
    INDEX `idx_container_parent` (`container_type` ASC, `container_id` ASC, `parent_id` ASC) VISIBLE,
    INDEX `idx_container_path` (`container_type` ASC, `container_id` ASC, `path`(255) ASC) VISIBLE,
    INDEX `idx_container_nombre` (`container_type` ASC, `container_id` ASC, `nombre` ASC) VISIBLE,
    INDEX `idx_parent_id` (`parent_id` ASC) VISIBLE,
    INDEX `idx_is_deleted` (`is_deleted` ASC, `container_type` ASC, `container_id` ASC) VISIBLE,
    INDEX `fk_nodo_creado_por_idx` (`creado_por` ASC) VISIBLE,
    INDEX `fk_nodo_actualizado_por_idx` (`actualizado_por` ASC) VISIBLE,
    UNIQUE INDEX `ux_container_path` (`container_type` ASC, `container_id` ASC, `path`(500) ASC) VISIBLE,
    CONSTRAINT `fk_nodo_parent`
    FOREIGN KEY (`parent_id`)
    REFERENCES `dev_portal_sql`.`nodo` (`nodo_id`)
    ON DELETE SET NULL,
    CONSTRAINT `fk_nodo_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_nodo_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`version_archivo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`version_archivo` (
                                                                  `version_archivo_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                  `nodo_id` BIGINT UNSIGNED NOT NULL,
                                                                  `enlace_id` BIGINT UNSIGNED NOT NULL COMMENT 'Conexión al storage a través de enlace',
                                                                  `version_label` VARCHAR(100) NULL DEFAULT NULL,
    `storage_key` VARCHAR(2000) NOT NULL COMMENT 'Clave de almacenamiento (mantener por redundancia)',
    `storage_bucket` VARCHAR(255) NULL DEFAULT NULL,
    `checksum` VARCHAR(128) NULL DEFAULT NULL,
    `size_bytes` BIGINT UNSIGNED NULL DEFAULT NULL,
    `creado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `actualizado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `actualizado_en` DATETIME NULL DEFAULT NULL,
    `vigente` TINYINT(1) NOT NULL DEFAULT '1',
    PRIMARY KEY (`version_archivo_id`),
    INDEX `idx_version_nodo` (`nodo_id` ASC) VISIBLE,
    INDEX `idx_version_enlace` (`enlace_id` ASC) VISIBLE,
    INDEX `fk_version_archivo_creado_por_idx` (`creado_por` ASC) VISIBLE,
    INDEX `fk_version_archivo_actualizado_por_idx` (`actualizado_por` ASC) VISIBLE,
    CONSTRAINT `fk_fileversion_enlace`
    FOREIGN KEY (`enlace_id`)
    REFERENCES `dev_portal_sql`.`enlace` (`enlace_id`)
    ON DELETE RESTRICT,
    CONSTRAINT `fk_fileversion_nodo`
    FOREIGN KEY (`nodo_id`)
    REFERENCES `dev_portal_sql`.`nodo` (`nodo_id`)
    ON DELETE CASCADE,
    CONSTRAINT `fk_version_archivo_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_version_archivo_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`historial` (HÍBRIDA: SQL + MongoDB)
-- -----------------------------------------------------
-- ARQUITECTURA HÍBRIDA PARA AUDITORÍA COMPLETA
--
-- SQL almacena:
--   - Metadatos del evento (tipo, usuario, fecha, IP)
--   - Entidad afectada (tabla + ID)
--   - Descripción breve del cambio
--
-- MongoDB almacena (collection: audit_snapshots):
--   - Snapshot COMPLETO de la entidad ANTES del cambio
--   - Snapshot COMPLETO de la entidad DESPUÉS del cambio
--   - Diff detallado (campos modificados)
--   - Metadata adicional (user agent, geolocalización)
--
-- ¿Por qué híbrido?
-- - SQL: Búsquedas rápidas por usuario/fecha/tipo de evento
-- - MongoDB: Preservar estado completo para restauración (ej: "restaurar API eliminada")
-- - TTL: Eliminar auditorías muy antiguas automáticamente
--
-- Ejemplo: Usuario elimina una API
-- SQL:
--   tipo_evento = 'ELIMINACION'
--   entidad_afectada = 'api'
--   id_entidad_afectada = 42
--   nosql_snapshot_id = ObjectId("...")
--
-- MongoDB (audit_snapshots):
-- {
--   "_id": ObjectId("..."),
--   "historial_id": 12345,
--   "before": {
--     "api_id": 42,
--     "nombre_api": "Payment API",
--     "descripcion": "...",
--     "estado": "ACTIVO",
--     // ... todos los campos de la API
--     "versiones": [...],  // Relaciones embebidas
--     "endpoints": [...]
--   },
--   "after": null,  // null porque fue eliminación
--   "diff": {
--     "deleted": ["api_id", "nombre_api", ...]
--   },
--   "expireAt": ISODate("2030-10-22T00:00:00Z")  // TTL 5 años
-- }
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`historial` (
                                                            `historial_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                            `tipo_evento` ENUM('CREACION', 'MODIFICACION', 'ELIMINACION', 'LOGIN', 'LOGOUT', 'ACCESO_DENEGADO', 'CAMBIO_ROL', 'RESTAURACION') NOT NULL COMMENT 'Tipo de evento auditado',
    `entidad_afectada` VARCHAR(64) NULL DEFAULT NULL COMMENT 'Nombre de la tabla (api, proyecto, repositorio, usuario, etc.)',
    `id_entidad_afectada` BIGINT NULL DEFAULT NULL COMMENT 'ID de la entidad modificada/eliminada',
    `descripcion_evento` TEXT NOT NULL COMMENT 'Descripción breve del evento',
    `fecha_evento` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ip_origen` VARCHAR(128) NULL DEFAULT NULL COMMENT 'IP del cliente',
    `usuario_usuario_id` BIGINT UNSIGNED NOT NULL COMMENT 'Usuario que ejecutó la acción',
    `nosql_snapshot_id` VARCHAR(24) NULL DEFAULT NULL COMMENT 'ObjectId de MongoDB (collection: audit_snapshots) para snapshot completo',
    PRIMARY KEY (`historial_id`),
    INDEX `fk_historial_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
    INDEX `idx_entidad` (`entidad_afectada` ASC, `id_entidad_afectada` ASC) VISIBLE COMMENT 'Buscar historial de una entidad específica',
    INDEX `idx_tipo_fecha` (`tipo_evento` ASC, `fecha_evento` DESC) VISIBLE COMMENT 'Filtrar por tipo y ordenar por fecha',
    INDEX `idx_nosql_snapshot` (`nosql_snapshot_id` ASC) VISIBLE COMMENT 'Validación con MongoDB',
    CONSTRAINT `fk_historial_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`impersonacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`impersonacion` (
                                                                `impersonacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                `fecha_inicio_impersonacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                                `fecha_fin_impersonacion` DATETIME NULL DEFAULT NULL,
                                                                `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
                                                                PRIMARY KEY (`impersonacion_id`),
    INDEX `fk_impersonacion_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
    CONSTRAINT `fk_impersonacion_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`permiso_nodo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`permiso_nodo` (
                                                               `permiso_nodo_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                               `nodo_id` BIGINT UNSIGNED NOT NULL,
                                                               `permiso` ENUM('READ', 'WRITE', 'ADMIN') NOT NULL DEFAULT 'READ',
    `inheritable` TINYINT(1) NOT NULL DEFAULT '1',
    `creado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `actualizado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `actualizado_en` DATETIME NULL DEFAULT NULL,
    `usuario_usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    `equipo_equipo_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    PRIMARY KEY (`permiso_nodo_id`),
    INDEX `idx_perm_nodo` (`nodo_id` ASC) VISIBLE,
    INDEX `fk_permiso_nodo_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
    INDEX `fk_permiso_nodo_equipo1_idx` (`equipo_equipo_id` ASC) VISIBLE,
    INDEX `fk_permiso_nodo_creado_por_idx` (`creado_por` ASC) VISIBLE,
    INDEX `fk_permiso_nodo_actualizado_por_idx` (`actualizado_por` ASC) VISIBLE,
    CONSTRAINT `fk_perm_nodo`
    FOREIGN KEY (`nodo_id`)
    REFERENCES `dev_portal_sql`.`nodo` (`nodo_id`)
    ON DELETE CASCADE,
    CONSTRAINT `fk_permiso_nodo_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT `fk_permiso_nodo_equipo1`
    FOREIGN KEY (`equipo_equipo_id`)
    REFERENCES `dev_portal_sql`.`equipo` (`equipo_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT `fk_permiso_nodo_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_permiso_nodo_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `chk_permiso_nodo_target`
    CHECK ((`usuario_usuario_id` IS NOT NULL) OR (`equipo_equipo_id` IS NOT NULL)))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`nodo_tag_master`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`nodo_tag_master` (
                                                                  `tag_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                  `nombre_tag_master` VARCHAR(100) NOT NULL,
    PRIMARY KEY (`tag_id`),
    UNIQUE INDEX `ux_tag_nombre` (`nombre_tag_master` ASC) VISIBLE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`nodo_tag`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`nodo_tag` (
                                                           `nodo_id` BIGINT UNSIGNED NOT NULL,
                                                           `tag_id` BIGINT UNSIGNED NOT NULL,
                                                           PRIMARY KEY (`nodo_id`, `tag_id`),
    INDEX `fk_nt_tag` (`tag_id` ASC) VISIBLE,
    CONSTRAINT `fk_nt_nodo`
    FOREIGN KEY (`nodo_id`)
    REFERENCES `dev_portal_sql`.`nodo` (`nodo_id`)
    ON DELETE CASCADE,
    CONSTRAINT `fk_nt_tag`
    FOREIGN KEY (`tag_id`)
    REFERENCES `dev_portal_sql`.`nodo_tag_master` (`tag_id`)
    ON DELETE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`proyecto_has_repositorio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`proyecto_has_repositorio` (
                                                                           `proyecto_proyecto_id` BIGINT UNSIGNED NOT NULL,
                                                                           `repositorio_repositorio_id` BIGINT UNSIGNED NOT NULL,
                                                                           PRIMARY KEY (`proyecto_proyecto_id`, `repositorio_repositorio_id`),
    INDEX `fk_proyecto_has_repositorio_repositorio1_idx` (`repositorio_repositorio_id` ASC) VISIBLE,
    INDEX `fk_proyecto_has_repositorio_proyecto1_idx` (`proyecto_proyecto_id` ASC) VISIBLE,
    CONSTRAINT `fk_proyecto_has_repositorio_proyecto1`
    FOREIGN KEY (`proyecto_proyecto_id`)
    REFERENCES `dev_portal_sql`.`proyecto` (`proyecto_id`),
    CONSTRAINT `fk_proyecto_has_repositorio_repositorio1`
    FOREIGN KEY (`repositorio_repositorio_id`)
    REFERENCES `dev_portal_sql`.`repositorio` (`repositorio_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`recurso`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`recurso` (
                                                          `recurso_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                          `nombre_archivo` VARCHAR(255) NOT NULL,
    `tipo_recurso` ENUM('TEXTO', 'IMAGEN', 'AUDIO', 'VIDEO', 'CODIGO', 'DOCUMENTO', 'GRAFICA', 'OTRO') NOT NULL DEFAULT 'TEXTO',
    `formato_recurso` VARCHAR(10) NULL DEFAULT NULL,
    `mime_type` VARCHAR(50) NOT NULL,
    `contenido_contenido_id` BIGINT UNSIGNED NOT NULL,
    `enlace_enlace_id` BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (`recurso_id`),
    INDEX `fk_recurso_contenido1_idx` (`contenido_contenido_id` ASC) VISIBLE,
    INDEX `fk_recurso_enlace1_idx` (`enlace_enlace_id` ASC) VISIBLE,
    CONSTRAINT `fk_recurso_contenido1`
    FOREIGN KEY (`contenido_contenido_id`)
    REFERENCES `dev_portal_sql`.`contenido` (`contenido_id`),
    CONSTRAINT `fk_recurso_enlace1`
    FOREIGN KEY (`enlace_enlace_id`)
    REFERENCES `dev_portal_sql`.`enlace` (`enlace_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`rol`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`rol` (
                                                      `rol_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                      `nombre_rol` ENUM('DEV', 'QA', 'PO', 'SA') NOT NULL COMMENT 'Tipo de rol global en la plataforma',
    `descripcion_rol` TEXT NULL DEFAULT NULL COMMENT 'Descripción detallada del rol',
    `activo` TINYINT(1) NOT NULL DEFAULT '1' COMMENT 'Si el rol está activo',
    PRIMARY KEY (`rol_id`),
    INDEX `idx_nombre_rol` (`nombre_rol` ASC) VISIBLE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`solicitud_acceso_api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`solicitud_acceso_api` (
                                                                       `accesibilidad_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                       `tipo_entorno` ENUM('SANDBOX', 'QA', 'PROD') NOT NULL,
    `estado_solicitud` ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO') NOT NULL,
    `fecha_solicitud` DATETIME NOT NULL,
    `comentario_solicitud` VARCHAR(255) NULL DEFAULT NULL,
    `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
    `aprobador_usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    `api_api_id` BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (`accesibilidad_id`),
    INDEX `fk_acceso_api_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
    INDEX `fk_solicitud_acceso_api_api1_idx` (`api_api_id` ASC) VISIBLE,
    CONSTRAINT `fk_acceso_api_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`),
    CONSTRAINT `fk_solicitud_acceso_api_api1`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `dev_portal_sql`.`api` (`api_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`entorno_prueba`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`entorno_prueba` (
                                                                 `entorno_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                 `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
                                                                 `api_api_id` BIGINT UNSIGNED NOT NULL,
                                                                 `version_version_id` BIGINT UNSIGNED NULL DEFAULT NULL COMMENT 'Versión específica de la API',
                                                                 `nombre_entorno` VARCHAR(255) NOT NULL,
    `descripcion_entorno` TEXT NULL DEFAULT NULL,
    `estado_entorno` ENUM('ACTIVO', 'PAUSADO', 'EXPIRADO', 'ELIMINADO') NOT NULL DEFAULT 'ACTIVO',
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_expiracion` DATETIME NULL DEFAULT NULL,
    `limite_llamadas_dia` INT UNSIGNED NOT NULL DEFAULT 1000,
    `llamadas_realizadas` INT UNSIGNED NOT NULL DEFAULT 0,
    `ultima_llamada` DATETIME NULL DEFAULT NULL,
    PRIMARY KEY (`entorno_id`),
    INDEX `fk_entorno_usuario_idx` (`usuario_usuario_id` ASC) VISIBLE,
    INDEX `fk_entorno_api_idx` (`api_api_id` ASC) VISIBLE,
    INDEX `fk_entorno_version_idx` (`version_version_id` ASC) VISIBLE,
    INDEX `idx_estado_fecha` (`estado_entorno` ASC, `fecha_expiracion` ASC) VISIBLE,
    CONSTRAINT `fk_entorno_usuario`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_entorno_api`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `dev_portal_sql`.`api` (`api_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_entorno_version`
    FOREIGN KEY (`version_version_id`)
    REFERENCES `dev_portal_sql`.`version_api` (`version_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`api_suscripcion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`api_suscripcion` (
                                                                  `suscripcion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
                                                                  `api_api_id` BIGINT UNSIGNED NOT NULL,
                                                                  `plan_suscripcion` ENUM('FREE', 'BASIC', 'PRO', 'ENTERPRISE') NOT NULL DEFAULT 'FREE',
    `estado_suscripcion` ENUM('ACTIVA', 'SUSPENDIDA', 'CANCELADA', 'EXPIRADA') NOT NULL DEFAULT 'ACTIVA',
    `limite_llamadas_mes` INT UNSIGNED NOT NULL,
    `llamadas_mes_actual` INT UNSIGNED NOT NULL DEFAULT 0,
    `mes_periodo` DATE NOT NULL COMMENT 'Primer día del mes del periodo actual',
    `fecha_inicio` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_fin` DATETIME NULL DEFAULT NULL,
    `costo_mensual` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (`suscripcion_id`),
    INDEX `fk_suscripcion_usuario_idx` (`usuario_usuario_id` ASC) VISIBLE,
    INDEX `fk_suscripcion_api_idx` (`api_api_id` ASC) VISIBLE,
    INDEX `idx_usuario_api` (`usuario_usuario_id` ASC, `api_api_id` ASC) VISIBLE,
    INDEX `idx_estado` (`estado_suscripcion` ASC) VISIBLE,
    CONSTRAINT `fk_suscripcion_usuario`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_suscripcion_api`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `dev_portal_sql`.`api` (`api_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`api_test_case` (TABLA HÍBRIDA SQL+MongoDB)
-- -----------------------------------------------------
-- ARQUITECTURA HÍBRIDA:
-- - SQL: Metadatos básicos (nombre, endpoint, método, estado)
-- - MongoDB: Configuración completa (headers, body, assertions, scripts)
--
-- ¿Por qué híbrida?
-- - SQL: Búsquedas rápidas (por API, versión, estado)
-- - MongoDB: Schema flexible para configuraciones complejas
--
-- Campo clave: nosql_config_id
-- - VARCHAR(24) almacena ObjectId de MongoDB
-- - NO hay FK física (bases de datos separadas)
-- - Integridad garantizada por transacciones distribuidas en código
--
-- Colección MongoDB: test_case_config
-- {
--   "_id": ObjectId("..."),  // Mismo valor que nosql_config_id
--   "test_case_id": 123,
--   "request": { "headers": {...}, "body": {...} },
--   "validations": [...]
-- }
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`api_test_case` (
                                                                `test_case_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                `api_api_id` BIGINT UNSIGNED NOT NULL,
                                                                `version_version_id` BIGINT UNSIGNED NOT NULL,
                                                                `nombre_test` VARCHAR(255) NOT NULL,
    `descripcion_test` TEXT NULL DEFAULT NULL,
    `metodo_http` ENUM('GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS', 'HEAD') NOT NULL,
    `endpoint_path` VARCHAR(500) NOT NULL,
    `estado_test` ENUM('ACTIVO', 'INACTIVO', 'DEPRECADO') NOT NULL DEFAULT 'ACTIVO',
    `prioridad_test` ENUM('BAJA', 'MEDIA', 'ALTA', 'CRITICA') NOT NULL DEFAULT 'MEDIA',
    `nosql_config_id` VARCHAR(24) NULL DEFAULT NULL COMMENT 'ObjectId MongoDB → Collection: test_case_config',
    `creado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `actualizado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `actualizado_en` DATETIME NULL DEFAULT NULL,
    PRIMARY KEY (`test_case_id`),
    INDEX `fk_testcase_api_idx` (`api_api_id` ASC) VISIBLE,
    INDEX `fk_testcase_version_idx` (`version_version_id` ASC) VISIBLE,
    INDEX `idx_estado` (`estado_test` ASC) VISIBLE,
    INDEX `idx_nosql_config` (`nosql_config_id` ASC) VISIBLE COMMENT 'Para validación de integridad en código',
    INDEX `fk_testcase_creado_por_idx` (`creado_por` ASC) VISIBLE,
    INDEX `fk_testcase_actualizado_por_idx` (`actualizado_por` ASC) VISIBLE,
    CONSTRAINT `fk_testcase_api`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `dev_portal_sql`.`api` (`api_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_testcase_version`
    FOREIGN KEY (`version_version_id`)
    REFERENCES `dev_portal_sql`.`version_api` (`version_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_testcase_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_testcase_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`api_test_log` (TABLA HÍBRIDA SQL+MongoDB)
-- -----------------------------------------------------
-- ARQUITECTURA HÍBRIDA:
-- - SQL: Metadatos de ejecución (quién, cuándo, resultado, duración)
-- - MongoDB: Request/Response completos (pueden ser 10MB+)
--
-- ¿Por qué híbrida?
-- - SQL: Queries rápidas de histórico (últimas 100 ejecuciones)
-- - MongoDB: Almacenar payloads grandes sin ralentizar SQL
--
-- Colección MongoDB: test_log_detalle
-- {
--   "_id": ObjectId("..."),  // nosql_detalle_id
--   "test_log_id": 1234,
--   "request": { "headers": {...}, "body": "..." },
--   "response": { "headers": {...}, "body": "...", "size_bytes": 15420 }
-- }
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`api_test_log` (
                                                               `test_log_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                               `test_case_test_case_id` BIGINT UNSIGNED NOT NULL,
                                                               `ejecutado_por` BIGINT UNSIGNED NOT NULL,
                                                               `entorno_entorno_id` BIGINT UNSIGNED NULL DEFAULT NULL,
                                                               `fecha_ejecucion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                               `duracion_ms` INT UNSIGNED NULL DEFAULT NULL,
                                                               `status_code` INT NULL DEFAULT NULL,
                                                               `resultado` ENUM('EXITOSO', 'FALLIDO', 'ERROR', 'TIMEOUT') NOT NULL,
    `nosql_detalle_id` VARCHAR(24) NULL DEFAULT NULL COMMENT 'ObjectId MongoDB → Collection: test_log_detalle',
    PRIMARY KEY (`test_log_id`),
    INDEX `fk_testlog_testcase_idx` (`test_case_test_case_id` ASC) VISIBLE,
    INDEX `fk_testlog_usuario_idx` (`ejecutado_por` ASC) VISIBLE,
    INDEX `fk_testlog_entorno_idx` (`entorno_entorno_id` ASC) VISIBLE,
    INDEX `idx_fecha_resultado` (`fecha_ejecucion` DESC, `resultado` ASC) VISIBLE,
    INDEX `idx_nosql_detalle` (`nosql_detalle_id` ASC) VISIBLE,
    CONSTRAINT `fk_testlog_testcase`
    FOREIGN KEY (`test_case_test_case_id`)
    REFERENCES `dev_portal_sql`.`api_test_case` (`test_case_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_testlog_usuario`
    FOREIGN KEY (`ejecutado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_testlog_entorno`
    FOREIGN KEY (`entorno_entorno_id`)
    REFERENCES `dev_portal_sql`.`entorno_prueba` (`entorno_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`api_test_resultado`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`api_test_resultado` (
                                                                     `resultado_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                     `test_log_test_log_id` BIGINT UNSIGNED NOT NULL,
                                                                     `tipo_validacion` ENUM('STATUS_CODE', 'RESPONSE_TIME', 'SCHEMA', 'CONTENT', 'HEADER', 'CUSTOM') NOT NULL,
    `nombre_validacion` VARCHAR(255) NOT NULL,
    `esperado` TEXT NULL DEFAULT NULL,
    `obtenido` TEXT NULL DEFAULT NULL,
    `resultado` ENUM('PASS', 'FAIL') NOT NULL,
    `mensaje_error` TEXT NULL DEFAULT NULL,
    PRIMARY KEY (`resultado_id`),
    INDEX `fk_resultado_testlog_idx` (`test_log_test_log_id` ASC) VISIBLE,
    INDEX `idx_resultado` (`resultado` ASC) VISIBLE,
    CONSTRAINT `fk_resultado_testlog`
    FOREIGN KEY (`test_log_test_log_id`)
    REFERENCES `dev_portal_sql`.`api_test_log` (`test_log_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`api_mock_server` (TABLA HÍBRIDA SQL+MongoDB)
-- -----------------------------------------------------
-- ARQUITECTURA HÍBRIDA:
-- - SQL: Gestión del mock (nombre, estado, URL base)
-- - MongoDB: Configuración de endpoints y respuestas mock
--
-- ¿Por qué híbrida?
-- - SQL: Control de acceso y gestión (activar/desactivar mocks)
-- - MongoDB: Schema flexible para configurar múltiples endpoints
--
-- Colección MongoDB: mock_server_config
-- {
--   "_id": ObjectId("..."),
--   "mock_server_id": 5,
--   "endpoints": [
--     { "method": "GET", "path": "/users/:id", "response": {...} },
--     { "method": "POST", "path": "/users", "response": {...} }
--   ]
-- }
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`api_mock_server` (
                                                                  `mock_server_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                  `api_api_id` BIGINT UNSIGNED NOT NULL,
                                                                  `version_version_id` BIGINT UNSIGNED NULL DEFAULT NULL,
                                                                  `creado_por` BIGINT UNSIGNED NOT NULL,
                                                                  `nombre_mock` VARCHAR(255) NOT NULL,
    `descripcion_mock` TEXT NULL DEFAULT NULL,
    `base_url` VARCHAR(500) NOT NULL,
    `estado_mock` ENUM('ACTIVO', 'INACTIVO', 'MANTENIMIENTO') NOT NULL DEFAULT 'ACTIVO',
    `nosql_config_id` VARCHAR(24) NULL DEFAULT NULL COMMENT 'ObjectId MongoDB → Collection: mock_server_config',
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_actualizacion` DATETIME NULL DEFAULT NULL,
    PRIMARY KEY (`mock_server_id`),
    INDEX `fk_mock_api_idx` (`api_api_id` ASC) VISIBLE,
    INDEX `fk_mock_version_idx` (`version_version_id` ASC) VISIBLE,
    INDEX `fk_mock_creado_por_idx` (`creado_por` ASC) VISIBLE,
    INDEX `idx_estado` (`estado_mock` ASC) VISIBLE,
    INDEX `idx_nosql_config` (`nosql_config_id` ASC) VISIBLE,
    CONSTRAINT `fk_mock_api`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `dev_portal_sql`.`api` (`api_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_mock_version`
    FOREIGN KEY (`version_version_id`)
    REFERENCES `dev_portal_sql`.`version_api` (`version_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_mock_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`ticket`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`ticket` (
                                                         `ticket_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                         `asunto_ticket` VARCHAR(255) NOT NULL,
    `cuerpo_ticket` TEXT NOT NULL,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_cierre` DATETIME NULL DEFAULT NULL,
    `estado_ticket` ENUM('ENVIADO', 'RECIBIDO') NOT NULL,
    `tipo_ticket` ENUM('INCIDENCIA', 'CONSULTA', 'REQUERIMIENTO') NOT NULL DEFAULT 'CONSULTA',
    `prioridad_ticket` ENUM('BAJA', 'MEDIA', 'ALTA') NOT NULL DEFAULT 'MEDIA',
    `proyecto_id` BIGINT UNSIGNED NULL DEFAULT NULL COMMENT 'Proyecto asociado (NULL = ticket público)',
    `reportado_por_usuario_id` BIGINT UNSIGNED NOT NULL,
    `asignado_a_usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL,
    `etapa_ticket` ENUM('PENDIENTE', 'EN_PROGRESO', 'RESUELTO', 'CERRADO', 'RECHAZADO') NULL DEFAULT 'PENDIENTE',
    PRIMARY KEY (`ticket_id`),
    INDEX `fk_ticket_usuario1_idx` (`reportado_por_usuario_id` ASC) VISIBLE,
    INDEX `fk_ticket_usuario2_idx` (`asignado_a_usuario_id` ASC) VISIBLE,
    INDEX `fk_ticket_proyecto_idx` (`proyecto_id` ASC) VISIBLE,
    CONSTRAINT `fk_ticket_usuario1`
    FOREIGN KEY (`reportado_por_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT `fk_ticket_usuario2`
    FOREIGN KEY (`asignado_a_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_ticket_proyecto`
    FOREIGN KEY (`proyecto_id`)
    REFERENCES `dev_portal_sql`.`proyecto` (`proyecto_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`ticket_comentario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`ticket_comentario` (
                                                                    `comentario_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                    `ticket_ticket_id` BIGINT UNSIGNED NOT NULL,
                                                                    `parent_comentario_id` BIGINT UNSIGNED NULL DEFAULT NULL COMMENT 'Para respuestas anidadas',
                                                                    `autor_usuario_id` BIGINT UNSIGNED NOT NULL,
                                                                    `contenido_comentario` TEXT NOT NULL,
                                                                    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                                    `fecha_modificacion` DATETIME NULL DEFAULT NULL,
                                                                    `editado` TINYINT(1) NOT NULL DEFAULT '0',
    `tipo_comentario` ENUM('COMENTARIO', 'SOLUCION', 'NOTA_INTERNA') NOT NULL DEFAULT 'COMENTARIO',
    PRIMARY KEY (`comentario_id`),
    INDEX `fk_comentario_ticket_idx` (`ticket_ticket_id` ASC) VISIBLE,
    INDEX `fk_comentario_parent_idx` (`parent_comentario_id` ASC) VISIBLE,
    INDEX `fk_comentario_autor_idx` (`autor_usuario_id` ASC) VISIBLE,
    INDEX `idx_ticket_fecha` (`ticket_ticket_id` ASC, `fecha_creacion` DESC) VISIBLE,
    CONSTRAINT `fk_comentario_ticket`
    FOREIGN KEY (`ticket_ticket_id`)
    REFERENCES `dev_portal_sql`.`ticket` (`ticket_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_comentario_parent`
    FOREIGN KEY (`parent_comentario_id`)
    REFERENCES `dev_portal_sql`.`ticket_comentario` (`comentario_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_comentario_autor`
    FOREIGN KEY (`autor_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`ticket_has_usuario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`ticket_has_usuario` (
                                                                     `ticket_ticket_id` BIGINT UNSIGNED NOT NULL,
                                                                     `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
                                                                     PRIMARY KEY (`ticket_ticket_id`, `usuario_usuario_id`),
    INDEX `fk_ticket_has_usuario_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
    INDEX `fk_ticket_has_usuario_ticket1_idx` (`ticket_ticket_id` ASC) VISIBLE,
    CONSTRAINT `fk_ticket_has_usuario_ticket1`
    FOREIGN KEY (`ticket_ticket_id`)
    REFERENCES `dev_portal_sql`.`ticket` (`ticket_id`),
    CONSTRAINT `fk_ticket_has_usuario_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`token`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`token` (
                                                        `token_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                        `valor_token` VARCHAR(512) NULL DEFAULT NULL,
    `estado_token` ENUM('ACTIVO', 'REVOCADO') NULL DEFAULT NULL,
    `fecha_creacion_token` DATETIME NULL DEFAULT NULL,
    `fecha_expiracion_token` DATETIME NULL DEFAULT NULL,
    `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (`token_id`),
    INDEX `fk_token_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
    CONSTRAINT `fk_token_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`usuario_has_equipo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`usuario_has_equipo` (
                                                                     `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
                                                                     `equipo_equipo_id` BIGINT UNSIGNED NOT NULL,
                                                                     PRIMARY KEY (`usuario_usuario_id`, `equipo_equipo_id`),
    INDEX `fk_usuario_has_equipo_equipo1_idx` (`equipo_equipo_id` ASC) VISIBLE,
    INDEX `fk_usuario_has_equipo_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
    CONSTRAINT `fk_usuario_has_equipo_equipo1`
    FOREIGN KEY (`equipo_equipo_id`)
    REFERENCES `dev_portal_sql`.`equipo` (`equipo_id`),
    CONSTRAINT `fk_usuario_has_equipo_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`usuario_has_proyecto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`usuario_has_proyecto` (
                                                                       `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
                                                                       `proyecto_proyecto_id` BIGINT UNSIGNED NOT NULL,
                                                                       `privilegio_usuario_proyecto` ENUM('LECTOR', 'EDITOR', 'COMENTADOR', 'ADMINISTRADOR') NOT NULL DEFAULT 'LECTOR',
    `fecha_usuario_proyecto` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`usuario_usuario_id`, `proyecto_proyecto_id`),
    INDEX `fk_usuario_has_proyecto_proyecto1_idx` (`proyecto_proyecto_id` ASC) VISIBLE,
    INDEX `fk_usuario_has_proyecto_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
    CONSTRAINT `fk_usuario_has_proyecto_proyecto1`
    FOREIGN KEY (`proyecto_proyecto_id`)
    REFERENCES `dev_portal_sql`.`proyecto` (`proyecto_id`),
    CONSTRAINT `fk_usuario_has_proyecto_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`proyecto_invitaciones`
-- -----------------------------------------------------
-- Tabla para gestionar invitaciones pendientes a proyectos
-- Almacena invitaciones con estados: PENDIENTE, ACEPTADA, RECHAZADA, EXPIRADA
-- Los roles y equipos se guardan como JSON hasta que la invitación sea aceptada
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`proyecto_invitaciones` (
  `invitacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `proyecto_id` BIGINT UNSIGNED NOT NULL,
  `usuario_invitado_id` BIGINT UNSIGNED NOT NULL,
  `invitado_por_usuario_id` BIGINT UNSIGNED NOT NULL,
  `permiso` VARCHAR(50) NULL DEFAULT NULL,
  `roles_json` TEXT NULL DEFAULT NULL COMMENT 'Array JSON de IDs de roles: [1,2,3]',
  `equipos_json` TEXT NULL DEFAULT NULL COMMENT 'Array JSON de IDs de equipos: [10,20]',
  `tipo_invitacion` VARCHAR(50) NULL DEFAULT NULL,
  `estado` VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' COMMENT 'PENDIENTE, ACEPTADA, RECHAZADA, EXPIRADA',
  `fecha_invitacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_respuesta` DATETIME NULL DEFAULT NULL,
  `token` VARCHAR(255) NULL DEFAULT NULL,
  `fecha_expiracion` DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (`invitacion_id`),
  UNIQUE INDEX `token_UNIQUE` (`token` ASC) VISIBLE,
  INDEX `idx_proyecto_invitaciones` (`proyecto_id` ASC) VISIBLE,
  INDEX `idx_usuario_estado` (`usuario_invitado_id` ASC, `estado` ASC) VISIBLE,
  INDEX `idx_estado` (`estado` ASC) VISIBLE,
  INDEX `fk_invitacion_proyecto_idx` (`proyecto_id` ASC) VISIBLE,
  INDEX `fk_invitacion_usuario_invitado_idx` (`usuario_invitado_id` ASC) VISIBLE,
  INDEX `fk_invitacion_invitado_por_idx` (`invitado_por_usuario_id` ASC) VISIBLE,
  CONSTRAINT `fk_invitacion_proyecto`
    FOREIGN KEY (`proyecto_id`)
    REFERENCES `dev_portal_sql`.`proyecto` (`proyecto_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_invitacion_usuario_invitado`
    FOREIGN KEY (`usuario_invitado_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_invitacion_invitado_por`
    FOREIGN KEY (`invitado_por_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`repositorio_invitaciones`
-- -----------------------------------------------------
-- Tabla para gestionar invitaciones pendientes a repositorios
-- Almacena invitaciones con estados: PENDIENTE, ACEPTADA, RECHAZADA, EXPIRADA
-- Los equipos se guardan como JSON hasta que la invitación sea aceptada
-- DIFERENCIA CON PROYECTOS: NO tiene roles_json (repositorios solo tienen permisos LECTOR/EDITOR)
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`repositorio_invitaciones` (
  `invitacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `repositorio_id` BIGINT UNSIGNED NOT NULL COMMENT 'ID del repositorio al que se invita',
  `usuario_invitado_id` BIGINT UNSIGNED NOT NULL COMMENT 'Usuario que recibe la invitación',
  `invitado_por_usuario_id` BIGINT UNSIGNED NOT NULL COMMENT 'Usuario que envió la invitación',
  `permiso` VARCHAR(50) NOT NULL COMMENT 'LECTOR, EDITOR o ADMINISTRADOR',
  `equipos_json` TEXT NULL DEFAULT NULL COMMENT 'Array JSON de IDs de equipos: [10,20,30]',
  `estado` VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' COMMENT 'PENDIENTE, ACEPTADA, RECHAZADA, EXPIRADA',
  `fecha_invitacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_respuesta` DATETIME NULL DEFAULT NULL COMMENT 'Fecha en que el usuario respondió',
  `token` VARCHAR(255) NULL DEFAULT NULL COMMENT 'Token único para aceptar invitación por email',
  `fecha_expiracion` DATETIME NULL DEFAULT NULL COMMENT 'Fecha de expiración del token',
  PRIMARY KEY (`invitacion_id`),
  UNIQUE INDEX `token_UNIQUE` (`token` ASC) VISIBLE,
  INDEX `idx_repositorio_invitaciones` (`repositorio_id` ASC) VISIBLE,
  INDEX `idx_usuario_estado` (`usuario_invitado_id` ASC, `estado` ASC) VISIBLE,
  INDEX `idx_estado` (`estado` ASC) VISIBLE,
  INDEX `fk_invitacion_repositorio_idx` (`repositorio_id` ASC) VISIBLE,
  INDEX `fk_invitacion_repo_usuario_invitado_idx` (`usuario_invitado_id` ASC) VISIBLE,
  INDEX `fk_invitacion_repo_invitado_por_idx` (`invitado_por_usuario_id` ASC) VISIBLE,
  CONSTRAINT `fk_invitacion_repositorio`
    FOREIGN KEY (`repositorio_id`)
    REFERENCES `dev_portal_sql`.`repositorio` (`repositorio_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_invitacion_repo_usuario_invitado`
    FOREIGN KEY (`usuario_invitado_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_invitacion_repo_invitado_por`
    FOREIGN KEY (`invitado_por_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Invitaciones a repositorios - Similar a proyectos pero SIN roles';


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`usuario_has_repositorio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`usuario_has_repositorio` (
                                                                          `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
                                                                          `repositorio_repositorio_id` BIGINT UNSIGNED NOT NULL,
                                                                          `privilegio_usuario_repositorio` ENUM('LECTOR', 'EDITOR', 'ADMINISTRADOR') NOT NULL DEFAULT 'LECTOR',
    `fecha_usuario_repositorio` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`usuario_usuario_id`, `repositorio_repositorio_id`),
    INDEX `fk_usuario_has_repositorio_repositorio1_idx` (`repositorio_repositorio_id` ASC) VISIBLE,
    INDEX `fk_usuario_has_repositorio_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
    CONSTRAINT `fk_usuario_has_repositorio_repositorio1`
    FOREIGN KEY (`repositorio_repositorio_id`)
    REFERENCES `dev_portal_sql`.`repositorio` (`repositorio_id`),
    CONSTRAINT `fk_usuario_has_repositorio_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`usuario_has_rol`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`usuario_has_rol` (
                                                                  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
                                                                  `rol_rol_id` BIGINT UNSIGNED NOT NULL,
                                                                  PRIMARY KEY (`usuario_usuario_id`, `rol_rol_id`),
    INDEX `fk_usuario_has_rol_rol1_idx` (`rol_rol_id` ASC) VISIBLE,
    INDEX `fk_usuario_has_rol_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
    CONSTRAINT `fk_usuario_has_rol_rol1`
    FOREIGN KEY (`rol_rol_id`)
    REFERENCES `dev_portal_sql`.`rol` (`rol_id`),
    CONSTRAINT `fk_usuario_has_rol_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`))
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`rol_proyecto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`rol_proyecto` (
                                                               `rol_proyecto_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                               `proyecto_proyecto_id` BIGINT UNSIGNED NOT NULL,
                                                               `nombre_rol_proyecto` VARCHAR(100) NOT NULL,
    `descripcion_rol_proyecto` TEXT NULL DEFAULT NULL,
    `creado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `actualizado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    `actualizado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `activo` TINYINT NOT NULL DEFAULT 1,
    PRIMARY KEY (`rol_proyecto_id`),
    INDEX `fk_rol_proyecto_proyecto1_idx` (`proyecto_proyecto_id` ASC) INVISIBLE,
    UNIQUE INDEX `idx_rolproyecto_proyecto_rolid` (`proyecto_proyecto_id` ASC, `rol_proyecto_id` ASC) VISIBLE,
    INDEX `fk_rol_proyecto_creado_por_idx` (`creado_por` ASC) VISIBLE,
    INDEX `fk_rol_proyecto_actualizado_por_idx` (`actualizado_por` ASC) VISIBLE,
    CONSTRAINT `fk_rol_proyecto_proyecto1`
    FOREIGN KEY (`proyecto_proyecto_id`)
    REFERENCES `dev_portal_sql`.`proyecto` (`proyecto_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT `fk_rol_proyecto_creado_por`
    FOREIGN KEY (`creado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_rol_proyecto_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`reporte`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`reporte` (
                                                          `reporte_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                          `titulo_reporte` VARCHAR(255) NOT NULL,
    `descripcion_reporte` TEXT NULL DEFAULT NULL,
    `contenido_reporte` LONGTEXT NOT NULL COMMENT 'Contenido HTML desde TinyMCE',
    `tipo_reporte` ENUM('TICKET', 'API', 'PROYECTO', 'REPOSITORIO', 'DOCUMENTACION', 'FORO', 'GENERAL') NOT NULL,
    `estado_reporte` ENUM('BORRADOR', 'PUBLICADO', 'REVISADO', 'ARCHIVADO') NOT NULL DEFAULT 'BORRADOR',
    `autor_usuario_id` BIGINT UNSIGNED NOT NULL COMMENT 'Usuario que crea el reporte (FIJO)',
    `creado_en` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `actualizado_en` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `actualizado_por` BIGINT UNSIGNED NULL DEFAULT NULL COMMENT 'Usuario que editó o PO que rechazó',

    PRIMARY KEY (`reporte_id`),
    INDEX `idx_tipo_reporte` (`tipo_reporte` ASC) VISIBLE,
    INDEX `idx_estado_reporte` (`estado_reporte` ASC) VISIBLE,
    INDEX `idx_autor` (`autor_usuario_id` ASC) VISIBLE,
    INDEX `idx_creado_en` (`creado_en` DESC) VISIBLE,
    INDEX `idx_actualizado_por` (`actualizado_por` ASC) VISIBLE,  -- ✅ Corregido

    CONSTRAINT `fk_reporte_autor`
    FOREIGN KEY (`autor_usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
                                                              ON DELETE RESTRICT
                                                              ON UPDATE CASCADE,
    CONSTRAINT `fk_reporte_actualizado_por`
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
                                                              ON DELETE SET NULL
                                                              ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`reporte_adjunto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`reporte_adjunto` (
                                                                  `adjunto_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                                  `reporte_id` BIGINT UNSIGNED NOT NULL,
                                                                  `nombre_archivo` VARCHAR(255) NOT NULL,
    `ruta_archivo` VARCHAR(500) NOT NULL,
    `tipo_mime` VARCHAR(100) NULL DEFAULT NULL,
    `tamano_bytes` BIGINT UNSIGNED NULL DEFAULT NULL,
    `descripcion_adjunto` TEXT NULL DEFAULT NULL,
    `orden_visualizacion` INT NULL DEFAULT 0,
    `subido_en` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `subido_por` BIGINT UNSIGNED NULL DEFAULT NULL,

    -- ✅ CAMPOS GCS (CORREGIDOS)
    `gcs_file_id` VARCHAR(500) NULL DEFAULT NULL,
    `gcs_bucket_name` VARCHAR(100) NULL DEFAULT NULL,
    `gcs_file_path` VARCHAR(500) NULL DEFAULT NULL,
    `gcs_public_url` VARCHAR(1000) NULL DEFAULT NULL,
    `gcs_file_size_bytes` BIGINT UNSIGNED NULL DEFAULT NULL,  -- ✅ Con UNSIGNED

-- Versionado simple
    `version_numero` INT DEFAULT 1,
    `es_version_actual` BOOLEAN DEFAULT TRUE,

    -- Metadata
    `actualizado_por` BIGINT UNSIGNED NULL DEFAULT NULL,      -- ✅ BIGINT UNSIGNED (no solo BIGINT)
    `actualizado_en` TIMESTAMP NULL DEFAULT NULL,
    `gcs_migrado` BOOLEAN DEFAULT FALSE,

    PRIMARY KEY (`adjunto_id`),
    INDEX `idx_reporte` (`reporte_id` ASC) VISIBLE,
    INDEX `idx_subido_por` (`subido_por` ASC) VISIBLE,
    INDEX `idx_version_numero` (`version_numero` ASC) VISIBLE,
    INDEX `idx_es_version_actual` (`es_version_actual` ASC) VISIBLE,
    INDEX `idx_gcs_file_id` (`gcs_file_id` ASC) VISIBLE,
    INDEX `idx_actualizado_por` (`actualizado_por` ASC) VISIBLE,

    UNIQUE KEY `unique_gcs_file_id` (`gcs_file_id`),

    CONSTRAINT `fk_adjunto_reporte`
    FOREIGN KEY (`reporte_id`)
    REFERENCES `dev_portal_sql`.`reporte` (`reporte_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_adjunto_subido_por`
    FOREIGN KEY (`subido_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
    CONSTRAINT `fk_adjunto_actualizado_por`  -- ✅ FIXED: BIGINT UNSIGNED
    FOREIGN KEY (`actualizado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`reporte_has_ticket`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`reporte_has_ticket` (
                                                                     `reporte_id` BIGINT UNSIGNED NOT NULL,
                                                                     `ticket_id` BIGINT UNSIGNED NOT NULL,
                                                                     `nota_relacion` TEXT NULL DEFAULT NULL COMMENT 'Contexto de por qué este reporte está vinculado',
                                                                     `vinculado_en` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                                     PRIMARY KEY (`reporte_id`, `ticket_id`),
    INDEX `idx_ticket` (`ticket_id` ASC) VISIBLE,
    CONSTRAINT `fk_reporte_ticket_reporte`
    FOREIGN KEY (`reporte_id`)
    REFERENCES `dev_portal_sql`.`reporte` (`reporte_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_reporte_ticket_ticket`
    FOREIGN KEY (`ticket_id`)
    REFERENCES `dev_portal_sql`.`ticket` (`ticket_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`reporte_has_api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`reporte_has_api` (
                                                                  `reporte_id` BIGINT UNSIGNED NOT NULL,
                                                                  `api_id` BIGINT UNSIGNED NOT NULL,
                                                                  `version_id` BIGINT UNSIGNED NULL DEFAULT NULL COMMENT 'Opcional: vincular a versión específica',
                                                                  `nota_relacion` TEXT NULL DEFAULT NULL,
                                                                  `vinculado_en` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                                  PRIMARY KEY (`reporte_id`, `api_id`),
    INDEX `idx_api` (`api_id` ASC) VISIBLE,
    INDEX `idx_version` (`version_id` ASC) VISIBLE,
    CONSTRAINT `fk_reporte_api_reporte`
    FOREIGN KEY (`reporte_id`)
    REFERENCES `dev_portal_sql`.`reporte` (`reporte_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_reporte_api_api`
    FOREIGN KEY (`api_id`)
    REFERENCES `dev_portal_sql`.`api` (`api_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_reporte_api_version`
    FOREIGN KEY (`version_id`)
    REFERENCES `dev_portal_sql`.`version_api` (`version_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`reporte_has_proyecto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`reporte_has_proyecto` (
                                                                       `reporte_id` BIGINT UNSIGNED NOT NULL,
                                                                       `proyecto_id` BIGINT UNSIGNED NOT NULL,
                                                                       `nota_relacion` TEXT NULL DEFAULT NULL,
                                                                       `vinculado_en` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                                       PRIMARY KEY (`reporte_id`, `proyecto_id`),
    INDEX `idx_proyecto` (`proyecto_id` ASC) VISIBLE,
    CONSTRAINT `fk_reporte_proyecto_reporte`
    FOREIGN KEY (`reporte_id`)
    REFERENCES `dev_portal_sql`.`reporte` (`reporte_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_reporte_proyecto_proyecto`
    FOREIGN KEY (`proyecto_id`)
    REFERENCES `dev_portal_sql`.`proyecto` (`proyecto_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`reporte_has_repositorio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`reporte_has_repositorio` (
                                                                          `reporte_id` BIGINT UNSIGNED NOT NULL,
                                                                          `repositorio_id` BIGINT UNSIGNED NOT NULL,
                                                                          `nota_relacion` TEXT NULL DEFAULT NULL,
                                                                          `vinculado_en` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                                          PRIMARY KEY (`reporte_id`, `repositorio_id`),
    INDEX `idx_repositorio` (`repositorio_id` ASC) VISIBLE,
    CONSTRAINT `fk_reporte_repositorio_reporte`
    FOREIGN KEY (`reporte_id`)
    REFERENCES `dev_portal_sql`.`reporte` (`reporte_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_reporte_repositorio_repositorio`
    FOREIGN KEY (`repositorio_id`)
    REFERENCES `dev_portal_sql`.`repositorio` (`repositorio_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`reporte_has_documentacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`reporte_has_documentacion` (
                                                                            `reporte_id` BIGINT UNSIGNED NOT NULL,
                                                                            `documentacion_id` BIGINT UNSIGNED NOT NULL,
                                                                            `nota_relacion` TEXT NULL DEFAULT NULL,
                                                                            `vinculado_en` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                                            PRIMARY KEY (`reporte_id`, `documentacion_id`),
    INDEX `idx_documentacion` (`documentacion_id` ASC) VISIBLE,
    CONSTRAINT `fk_reporte_documentacion_reporte`
    FOREIGN KEY (`reporte_id`)
    REFERENCES `dev_portal_sql`.`reporte` (`reporte_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_reporte_documentacion_documentacion`
    FOREIGN KEY (`documentacion_id`)
    REFERENCES `dev_portal_sql`.`documentacion` (`documentacion_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`reporte_has_foro`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`reporte_has_foro` (
                                                                   `reporte_id` BIGINT UNSIGNED NOT NULL,
                                                                   `foro_tema_id` BIGINT UNSIGNED NOT NULL,
                                                                   `nota_relacion` TEXT NULL DEFAULT NULL COMMENT 'Contexto de por qué este reporte está vinculado al tema del foro',
                                                                   `vinculado_en` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                                   PRIMARY KEY (`reporte_id`, `foro_tema_id`),
    INDEX `idx_foro_tema` (`foro_tema_id` ASC) VISIBLE,
    CONSTRAINT `fk_reporte_foro_reporte`
    FOREIGN KEY (`reporte_id`)
    REFERENCES `dev_portal_sql`.`reporte` (`reporte_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_reporte_foro_tema`
    FOREIGN KEY (`foro_tema_id`)
    REFERENCES `dev_portal_sql`.`foro_tema` (`tema_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`usuario_has_reporte`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`usuario_has_reporte` (
                                                                      `usuario_id` BIGINT UNSIGNED NOT NULL,
                                                                      `reporte_id` BIGINT UNSIGNED NOT NULL,
                                                                      `rol_colaborador` ENUM('AUTOR', 'REVISOR', 'COLABORADOR', 'LECTOR') NOT NULL DEFAULT 'COLABORADOR',
    `puede_editar` BOOLEAN NOT NULL DEFAULT FALSE,
    `asignado_en` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `asignado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
    PRIMARY KEY (`usuario_id`, `reporte_id`),
    INDEX `idx_reporte` (`reporte_id` ASC) VISIBLE,
    INDEX `idx_rol` (`rol_colaborador` ASC) VISIBLE,
    INDEX `fk_usuario_reporte_asignado_por_idx` (`asignado_por` ASC) VISIBLE,
    CONSTRAINT `fk_usuario_reporte_usuario`
    FOREIGN KEY (`usuario_id`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_usuario_reporte_reporte`
    FOREIGN KEY (`reporte_id`)
    REFERENCES `dev_portal_sql`.`reporte` (`reporte_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT `fk_usuario_reporte_asignado_por`
    FOREIGN KEY (`asignado_por`)
    REFERENCES `dev_portal_sql`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `dev_portal_sql`.`asignacion_rol_proyecto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dev_portal_sql`.`asignacion_rol_proyecto` (
                                                                          `rol_proyecto_rol_proyecto_id` BIGINT UNSIGNED NOT NULL,
                                                                          `usuario_has_proyecto_usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
                                                                          `usuario_has_proyecto_proyecto_proyecto_id` BIGINT UNSIGNED NOT NULL,
                                                                          PRIMARY KEY (`rol_proyecto_rol_proyecto_id`, `usuario_has_proyecto_usuario_usuario_id`, `usuario_has_proyecto_proyecto_proyecto_id`),
    INDEX `fk_rol_proyecto_has_usuario_has_proyecto_usuario_has_proyec_idx` (`usuario_has_proyecto_usuario_usuario_id` ASC, `usuario_has_proyecto_proyecto_proyecto_id` ASC) VISIBLE,
    INDEX `fk_rol_proyecto_has_usuario_has_proyecto_rol_proyecto2_idx` (`rol_proyecto_rol_proyecto_id` ASC) VISIBLE,
    INDEX `fk_asignacion_rolproyecto_compuesta_idx` (`usuario_has_proyecto_proyecto_proyecto_id` ASC, `rol_proyecto_rol_proyecto_id` ASC) INVISIBLE,
    CONSTRAINT `fk_rol_proyecto_has_usuario_has_proyecto_rol_proyecto2`
    FOREIGN KEY (`rol_proyecto_rol_proyecto_id`)
    REFERENCES `dev_portal_sql`.`rol_proyecto` (`rol_proyecto_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT `fk_rol_proyecto_has_usuario_has_proyecto_usuario_has_proyecto2`
    FOREIGN KEY (`usuario_has_proyecto_usuario_usuario_id` , `usuario_has_proyecto_proyecto_proyecto_id`)
    REFERENCES `dev_portal_sql`.`usuario_has_proyecto` (`usuario_usuario_id` , `proyecto_proyecto_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    CONSTRAINT `fk_asignacion_rolproyecto_compuesta`
    FOREIGN KEY (`usuario_has_proyecto_proyecto_proyecto_id` , `rol_proyecto_rol_proyecto_id`)
    REFERENCES `dev_portal_sql`.`rol_proyecto` (`proyecto_proyecto_id` , `rol_proyecto_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
    ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
