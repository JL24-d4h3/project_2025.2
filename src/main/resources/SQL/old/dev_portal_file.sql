-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema database
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `database` ;

-- -----------------------------------------------------
-- Schema database
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `database` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `database` ;

-- -----------------------------------------------------
-- Table `database`.`usuario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`usuario` (
  `usuario_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_usuario` VARCHAR(100) NOT NULL,
  `apellido_paterno` VARCHAR(100) NOT NULL,
  `apellido_materno` VARCHAR(100) NOT NULL,
  `dni` VARCHAR(8) NOT NULL,
  `fecha_nacimiento` DATE NULL DEFAULT NULL,
  `sexo_usuario` ENUM('HOMBRE', 'MUJER') NULL DEFAULT NULL,
  `estado_civil` ENUM('SOLTERO', 'CASADO', 'VIUDO', 'DIVORCIADO') NULL DEFAULT NULL,
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
-- Table `database`.`conversacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`conversacion` (
  `conversacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `titulo_conversacion` VARCHAR(255) NULL DEFAULT NULL,
  `fecha_inicio_conversacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_fin_conversacion` DATETIME NULL DEFAULT NULL,
  `estado_conversacion` ENUM('ACTIVA', 'CERRADA') NOT NULL DEFAULT 'ACTIVA',
  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`conversacion_id`),
  INDEX `fk_conversacion_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
  CONSTRAINT `fk_conversacion_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`mensaje`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`mensaje` (
  `mensaje_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `remitente` ENUM('USUARIO', 'CHATBOT') NOT NULL,
  `contenido_mensaje` TEXT NULL DEFAULT NULL,
  `fecha_envio_mensaje` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `conversacion_conversacion_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`mensaje_id`),
  INDEX `fk_mensaje_conversacion1_idx` (`conversacion_conversacion_id` ASC) VISIBLE,
  CONSTRAINT `fk_mensaje_conversacion1`
    FOREIGN KEY (`conversacion_conversacion_id`)
    REFERENCES `database`.`conversacion` (`conversacion_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`adjunto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`adjunto` (
  `adjunto_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_archivo_adjunto` VARCHAR(128) NULL DEFAULT NULL,
  `tipo_archivo_adjunto` ENUM('PDF', 'TXT', 'IMAGEN', 'DOC', 'OTRO') NOT NULL,
  `url_archivo_adjunto` TEXT NOT NULL,
  `mensaje_mensaje_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`adjunto_id`),
  INDEX `fk_adjunto_mensaje1_idx` (`mensaje_mensaje_id` ASC) VISIBLE,
  CONSTRAINT `fk_adjunto_mensaje1`
    FOREIGN KEY (`mensaje_mensaje_id`)
    REFERENCES `database`.`mensaje` (`mensaje_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`api` (
  `api_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_api` VARCHAR(45) NOT NULL,
  `descripcion_api` TEXT NOT NULL,
  `estado_api` ENUM('PRODUCCION', 'QA', 'DEPRECATED') NOT NULL,
  `fecha_creacion_api` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`api_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`categoria`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`categoria` (
  `id_categoria` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_categoria` VARCHAR(45) NOT NULL,
  `descripcion_categoria` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id_categoria`),
  INDEX `idx_id_categoria` (`id_categoria` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`categoria_has_api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`categoria_has_api` (
  `categoria_id_categoria` BIGINT UNSIGNED NOT NULL,
  `api_api_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`categoria_id_categoria`, `api_api_id`),
  INDEX `fk_categoria_has_api_api1_idx` (`api_api_id` ASC) VISIBLE,
  INDEX `fk_categoria_has_api_categoria1_idx` (`categoria_id_categoria` ASC) VISIBLE,
  CONSTRAINT `fk_categoria_has_api_api1`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `database`.`api` (`api_id`),
  CONSTRAINT `fk_categoria_has_api_categoria1`
    FOREIGN KEY (`categoria_id_categoria`)
    REFERENCES `database`.`categoria` (`id_categoria`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`notificacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`notificacion` (
  `notificacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tipo_notificacion` ENUM('SISTEMA', 'TICKET', 'ALERTA', 'METRICA') NOT NULL,
  `asunto_notificacion` VARCHAR(255) NOT NULL,
  `mensaje_notificacion` TEXT NOT NULL,
  `estado_notificacion` ENUM('ENVIADA', 'RECIBIDA') NOT NULL,
  `inspeccion_notificacion` ENUM('LEIDA', 'NO_LEIDA') NOT NULL,
  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`notificacion_id`),
  INDEX `fk_notificacion_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
  CONSTRAINT `fk_notificacion_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`categoria_has_notificacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`categoria_has_notificacion` (
  `categoria_id_categoria` BIGINT UNSIGNED NOT NULL,
  `notificacion_notificacion_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`categoria_id_categoria`, `notificacion_notificacion_id`),
  INDEX `fk_categoria_has_notificacion_notificacion1_idx` (`notificacion_notificacion_id` ASC) VISIBLE,
  INDEX `fk_categoria_has_notificacion_categoria1_idx` (`categoria_id_categoria` ASC) VISIBLE,
  CONSTRAINT `fk_categoria_has_notificacion_categoria1`
    FOREIGN KEY (`categoria_id_categoria`)
    REFERENCES `database`.`categoria` (`id_categoria`),
  CONSTRAINT `fk_categoria_has_notificacion_notificacion1`
    FOREIGN KEY (`notificacion_notificacion_id`)
    REFERENCES `database`.`notificacion` (`notificacion_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`proyecto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`proyecto` (
  `proyecto_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_proyecto` VARCHAR(128) NOT NULL,
  `descripcion_proyecto` TEXT NULL DEFAULT NULL,
  `visibilidad_proyecto` ENUM('PUBLICO', 'PRIVADO') NOT NULL DEFAULT 'PRIVADO',
  `acceso_proyecto` ENUM('RESTRINGIDO', 'ORGANIZACION', 'CUALQUIER_PERSONA_CON_EL_ENLACE') NOT NULL DEFAULT 'RESTRINGIDO',
  `propietario_proyecto` ENUM('USUARIO', 'GRUPO', 'EMPRESA') NOT NULL,
  `estado_proyecto` ENUM('PLANEADO', 'EN_DESARROLLO', 'MANTENIMIENTO', 'CERRADO') NOT NULL DEFAULT 'PLANEADO',
  `fecha_inicio_proyecto` DATE NOT NULL,
  `fecha_fin_proyecto` DATE NULL DEFAULT NULL,
  `propietario_id` BIGINT UNSIGNED NULL DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` BIGINT UNSIGNED NULL DEFAULT NULL,
  `updated_at` DATETIME NULL DEFAULT NULL,
  `updated_by` BIGINT UNSIGNED NULL DEFAULT NULL,
  `slug` VARCHAR(200) NULL DEFAULT NULL,
  `proyecto_key` VARCHAR(64) NULL DEFAULT NULL,
  `root_node_id` BIGINT UNSIGNED NULL DEFAULT NULL,
  `web_url` VARCHAR(2048) NULL DEFAULT NULL,
  PRIMARY KEY (`proyecto_id`),
  INDEX `ix_proyecto_propietario` (`propietario_proyecto` ASC, `propietario_id` ASC) VISIBLE,
  INDEX `ix_proyecto_slug` (`slug` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`categoria_has_proyecto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`categoria_has_proyecto` (
  `categoria_id_categoria` BIGINT UNSIGNED NOT NULL,
  `proyecto_proyecto_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`categoria_id_categoria`, `proyecto_proyecto_id`),
  INDEX `fk_categoria_has_proyecto_proyecto1_idx` (`proyecto_proyecto_id` ASC) VISIBLE,
  INDEX `fk_categoria_has_proyecto_categoria1_idx` (`categoria_id_categoria` ASC) VISIBLE,
  CONSTRAINT `fk_categoria_has_proyecto_categoria1`
    FOREIGN KEY (`categoria_id_categoria`)
    REFERENCES `database`.`categoria` (`id_categoria`),
  CONSTRAINT `fk_categoria_has_proyecto_proyecto1`
    FOREIGN KEY (`proyecto_proyecto_id`)
    REFERENCES `database`.`proyecto` (`proyecto_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`repositorio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`repositorio` (
  `repositorio_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_repositorio` VARCHAR(128) NOT NULL,
  `descripcion_repositorio` TEXT NULL DEFAULT NULL,
  `visibilidad_repositorio` ENUM('PUBLICO', 'PRIVADO') NOT NULL DEFAULT 'PRIVADO',
  `estado_repositorio` ENUM('ACTIVO', 'ARCHIVADO') NULL DEFAULT 'ACTIVO',
  `tipo_repositorio` ENUM('PERSONAL', 'COLABORATIVO') NOT NULL DEFAULT 'PERSONAL',
  `creado_por_usuario_id` BIGINT UNSIGNED NOT NULL,
  `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `rama_principal_repositorio` VARCHAR(45) NOT NULL DEFAULT 'main',
  `fecha_actualizacion` DATETIME NULL DEFAULT NULL,
  `last_activity_at` DATETIME NULL DEFAULT NULL,
  `size_bytes` BIGINT UNSIGNED NULL DEFAULT NULL,
  `ultimo_commit_hash` VARCHAR(128) NULL DEFAULT NULL,
  `is_fork` BOOLEAN NOT NULL DEFAULT FALSE,
  `forked_from_repo_id` BIGINT UNSIGNED NULL DEFAULT NULL,
  `license` VARCHAR(128) NULL DEFAULT NULL,
  `root_node_id` BIGINT UNSIGNED NULL DEFAULT NULL,
  PRIMARY KEY (`repositorio_id`),
  INDEX `ix_repo_tipo` (`tipo_repositorio` ASC) VISIBLE,
  INDEX `ix_repo_creador` (`creado_por_usuario_id` ASC) VISIBLE,
  INDEX `ix_repo_visibility` (`visibilidad_repositorio` ASC) VISIBLE,
  INDEX `ix_repo_last_activity` (`last_activity_at` ASC) VISIBLE,
  CONSTRAINT `fk_repositorio_creador`
    FOREIGN KEY (`creado_por_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`categoria_has_repositorio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`categoria_has_repositorio` (
  `categoria_id_categoria` BIGINT UNSIGNED NOT NULL,
  `repositorio_repositorio_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`categoria_id_categoria`, `repositorio_repositorio_id`),
  INDEX `fk_categoria_has_repositorio_repositorio1_idx` (`repositorio_repositorio_id` ASC) VISIBLE,
  INDEX `fk_categoria_has_repositorio_categoria1_idx` (`categoria_id_categoria` ASC) VISIBLE,
  CONSTRAINT `fk_categoria_has_repositorio_categoria1`
    FOREIGN KEY (`categoria_id_categoria`)
    REFERENCES `database`.`categoria` (`id_categoria`),
  CONSTRAINT `fk_categoria_has_repositorio_repositorio1`
    FOREIGN KEY (`repositorio_repositorio_id`)
    REFERENCES `database`.`repositorio` (`repositorio_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`clasificacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`clasificacion` (
  `clasificacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tipo_contenido_texto` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`clasificacion_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`documentacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`documentacion` (
  `documentacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `seccion_documentacion` VARCHAR(128) NULL DEFAULT NULL,
  `api_api_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`documentacion_id`),
  UNIQUE INDEX `api_api_id_unique` (`api_api_id` ASC) VISIBLE,
  INDEX `fk_documentacion_api1_idx` (`api_api_id` ASC) VISIBLE,
  CONSTRAINT `fk_documentacion_api1`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `database`.`api` (`api_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`metrica_api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`metrica_api` (
  `metrica_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `fecha_hora` DATETIME NULL DEFAULT NULL,
  `cantidad_llamadas` INT NOT NULL DEFAULT '0',
  `cantidad_errores` INT NOT NULL DEFAULT '0',
  `latencia_ms` FLOAT NOT NULL DEFAULT '0',
  `costo_estimado` DECIMAL(12,2) NOT NULL DEFAULT '0.00',
  `entorno` ENUM('SANDBOX', 'QA', 'PROD') NULL DEFAULT NULL,
  PRIMARY KEY (`metrica_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`version_api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`version_api` (
  `version_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `numero_version` VARCHAR(45) NOT NULL,
  `descripcion_version` TEXT NULL DEFAULT NULL,
  `contrato_api_url` TEXT NOT NULL,
  `fecha_lanzamiento` DATE NOT NULL,
  `api_api_id` BIGINT UNSIGNED NOT NULL,
  `metrica_api_metrica_id` BIGINT UNSIGNED NOT NULL,
  `documentacion_documentacion_id` BIGINT UNSIGNED NOT NULL,
  `id_creador` BIGINT UNSIGNED NULL DEFAULT NULL,
  PRIMARY KEY (`version_id`),
  UNIQUE INDEX `uk_api_version_unique` (`api_api_id`, `numero_version` ASC) VISIBLE,
  INDEX `fk_version_api_api1_idx` (`api_api_id` ASC) VISIBLE,
  INDEX `fk_version_api_metrica_api1_idx` (`metrica_api_metrica_id` ASC) VISIBLE,
  INDEX `fk_version_api_documentacion1_idx` (`documentacion_documentacion_id` ASC) VISIBLE,
  INDEX `fk_version_api_creador_idx` (`id_creador` ASC) VISIBLE,
  CONSTRAINT `fk_version_api_api1`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `database`.`api` (`api_id`),
  CONSTRAINT `fk_version_api_documentacion1`
    FOREIGN KEY (`documentacion_documentacion_id`)
    REFERENCES `database`.`documentacion` (`documentacion_id`),
  CONSTRAINT `fk_version_api_metrica_api1`
    FOREIGN KEY (`metrica_api_metrica_id`)
    REFERENCES `database`.`metrica_api` (`metrica_id`),
  CONSTRAINT `fk_version_api_creador`
    FOREIGN KEY (`id_creador`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;



-- -----------------------------------------------------
-- Table `database`.`contenido`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`contenido` (
  `contenido_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `titulo_contenido` VARCHAR(45) NOT NULL,
  `fecha_creacion` DATETIME NOT NULL,
  `clasificacion_clasificacion_id` BIGINT UNSIGNED NOT NULL,
  `documentacion_documentacion_id` BIGINT UNSIGNED NOT NULL,
  `version_api_version_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`contenido_id`),
  INDEX `fk_contenido_clasificacion1_idx` (`clasificacion_clasificacion_id` ASC) VISIBLE,
  INDEX `fk_contenido_documentacion1_idx` (`documentacion_documentacion_id` ASC) VISIBLE,
  INDEX `fk_contenido_version_api1_idx` (`version_api_version_id` ASC) VISIBLE,
  CONSTRAINT `fk_contenido_clasificacion1`
    FOREIGN KEY (`clasificacion_clasificacion_id`)
    REFERENCES `database`.`clasificacion` (`clasificacion_id`),
  CONSTRAINT `fk_contenido_documentacion1`
    FOREIGN KEY (`documentacion_documentacion_id`)
    REFERENCES `database`.`documentacion` (`documentacion_id`),
  CONSTRAINT `fk_contenido_version_api1`
    FOREIGN KEY (`version_api_version_id`)
    REFERENCES `database`.`version_api` (`version_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`credencial_api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`credencial_api` (
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
    REFERENCES `database`.`api` (`api_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`enlace`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`enlace` (
  `enlace_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `direccion_almacenamiento` TEXT NOT NULL,
  `fecha_creacion_enlace` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `contexto_type` ENUM('REPOSITORIO', 'PROYECTO', 'NODO', 'FILE_VERSION') NOT NULL DEFAULT 'REPOSITORIO',
  `contexto_id` BIGINT UNSIGNED NOT NULL,
  `tipo_enlace` ENUM('STORAGE', 'METADATA', 'THUMBNAIL', 'BACKUP') NOT NULL DEFAULT 'STORAGE',
  `repositorio_repositorio_id` BIGINT UNSIGNED NULL DEFAULT NULL,
  PRIMARY KEY (`enlace_id`),
  INDEX `idx_enlace_contexto` (`contexto_type` ASC, `contexto_id` ASC) VISIBLE,
  INDEX `idx_enlace_tipo` (`tipo_enlace` ASC) VISIBLE,
  INDEX `fk_enlace_repositorio1` (`repositorio_repositorio_id` ASC) VISIBLE,
  CONSTRAINT `fk_enlace_repositorio1`
    FOREIGN KEY (`repositorio_repositorio_id`)
    REFERENCES `database`.`repositorio` (`repositorio_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`equipo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`equipo` (
  `equipo_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_equipo` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`equipo_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`equipo_has_proyecto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`equipo_has_proyecto` (
  `equipo_equipo_id` BIGINT UNSIGNED NOT NULL,
  `proyecto_proyecto_id` BIGINT UNSIGNED NOT NULL,
  `privilegio_equipo_proyecto` ENUM('EDITOR', 'LECTOR', 'COMENTADOR') NOT NULL DEFAULT 'LECTOR',
  `fecha_equipo_proyecto` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`equipo_equipo_id`, `proyecto_proyecto_id`),
  INDEX `fk_equipo_has_proyecto_proyecto1_idx` (`proyecto_proyecto_id` ASC) VISIBLE,
  INDEX `fk_equipo_has_proyecto_equipo1_idx` (`equipo_equipo_id` ASC) VISIBLE,
  CONSTRAINT `fk_equipo_has_proyecto_equipo1`
    FOREIGN KEY (`equipo_equipo_id`)
    REFERENCES `database`.`equipo` (`equipo_id`),
  CONSTRAINT `fk_equipo_has_proyecto_proyecto1`
    FOREIGN KEY (`proyecto_proyecto_id`)
    REFERENCES `database`.`proyecto` (`proyecto_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`equipo_has_repositorio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`equipo_has_repositorio` (
  `equipo_equipo_id` BIGINT UNSIGNED NOT NULL,
  `repositorio_repositorio_id` BIGINT UNSIGNED NOT NULL,
  `privilegio_equipo_repositorio` ENUM('EDITOR', 'LECTOR') NOT NULL DEFAULT 'LECTOR',
  `fecha_equipo_repositorio` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`equipo_equipo_id`, `repositorio_repositorio_id`),
  INDEX `fk_equipo_has_repositorio_repositorio1_idx` (`repositorio_repositorio_id` ASC) VISIBLE,
  INDEX `fk_equipo_has_repositorio_equipo1_idx` (`equipo_equipo_id` ASC) VISIBLE,
  CONSTRAINT `fk_equipo_has_repositorio_equipo1`
    FOREIGN KEY (`equipo_equipo_id`)
    REFERENCES `database`.`equipo` (`equipo_id`),
  CONSTRAINT `fk_equipo_has_repositorio_repositorio1`
    FOREIGN KEY (`repositorio_repositorio_id`)
    REFERENCES `database`.`repositorio` (`repositorio_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`etiqueta`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`etiqueta` (
  `tag_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_tag` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`tag_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`etiqueta_has_api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`etiqueta_has_api` (
  `etiqueta_tag_id` BIGINT UNSIGNED NOT NULL,
  `api_api_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`etiqueta_tag_id`, `api_api_id`),
  INDEX `fk_etiqueta_has_api_api1_idx` (`api_api_id` ASC) VISIBLE,
  INDEX `fk_etiqueta_has_api_etiqueta1_idx` (`etiqueta_tag_id` ASC) VISIBLE,
  CONSTRAINT `fk_etiqueta_has_api_api1`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `database`.`api` (`api_id`),
  CONSTRAINT `fk_etiqueta_has_api_etiqueta1`
    FOREIGN KEY (`etiqueta_tag_id`)
    REFERENCES `database`.`etiqueta` (`tag_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`faq`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`faq` (
  `faq_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `pregunta` TEXT NOT NULL,
  `respuesta` TEXT NOT NULL,
  PRIMARY KEY (`faq_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`feedback`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`feedback` (
  `feedback_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `comentario` TEXT NOT NULL,
  `puntuacion` DECIMAL(2,1) NOT NULL,
  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
  `documentacion_documentacion_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`feedback_id`),
  INDEX `fk_feedback_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
  INDEX `fk_feedback_documentacion1_idx` (`documentacion_documentacion_id` ASC) VISIBLE,
  CONSTRAINT `fk_feedback_documentacion1`
    FOREIGN KEY (`documentacion_documentacion_id`)
    REFERENCES `database`.`documentacion` (`documentacion_id`),
  CONSTRAINT `fk_feedback_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`nodo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`nodo` (
  `nodo_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `container_type` ENUM('PROYECTO', 'REPOSITORIO') NOT NULL,
  `container_id` BIGINT UNSIGNED NOT NULL,
  `parent_id` BIGINT UNSIGNED NULL DEFAULT NULL,
  `nombre` VARCHAR(255) NOT NULL,
  `tipo` ENUM('CARPETA', 'ARCHIVO') NOT NULL,
  `path` VARCHAR(2000) NOT NULL COMMENT 'Ruta completa desde la raíz del contenedor',
  `descripcion` TEXT NULL DEFAULT NULL,
  `size` BIGINT UNSIGNED NULL DEFAULT '0',
  `mime_type` VARCHAR(255) NULL DEFAULT NULL,
  `creado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
  `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` DATETIME NULL DEFAULT NULL,
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  `deleted_at` DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (`nodo_id`),
  INDEX `idx_container` (`container_type` ASC, `container_id` ASC) VISIBLE,
  INDEX `idx_container_parent` (`container_type` ASC, `container_id` ASC, `parent_id` ASC) VISIBLE,
  INDEX `idx_container_path` (`container_type` ASC, `container_id` ASC, `path`(255) ASC) VISIBLE,
  INDEX `idx_container_nombre` (`container_type` ASC, `container_id` ASC, `nombre` ASC) VISIBLE,
  INDEX `idx_parent_id` (`parent_id` ASC) VISIBLE,
  CONSTRAINT `fk_nodo_parent`
    FOREIGN KEY (`parent_id`)
    REFERENCES `database`.`nodo` (`nodo_id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`file_version`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`file_version` (
  `version_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nodo_id` BIGINT UNSIGNED NOT NULL,
  `version_label` VARCHAR(100) NULL DEFAULT NULL,
  `enlace_id` BIGINT UNSIGNED NOT NULL COMMENT 'Conexión al storage a través de enlace',
  `storage_key` VARCHAR(2000) NOT NULL COMMENT 'Clave de almacenamiento (mantener por redundancia)',
  `storage_bucket` VARCHAR(255) NULL DEFAULT NULL,
  `checksum` VARCHAR(128) NULL DEFAULT NULL,
  `tamaño` BIGINT UNSIGNED NOT NULL,
  `creado_por` BIGINT UNSIGNED NULL DEFAULT NULL,
  `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `vigente` BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (`version_id`),
  INDEX `idx_version_nodo` (`nodo_id` ASC) VISIBLE,
  INDEX `idx_version_enlace` (`enlace_id` ASC) VISIBLE,
  CONSTRAINT `fk_fileversion_enlace`
    FOREIGN KEY (`enlace_id`)
    REFERENCES `database`.`enlace` (`enlace_id`)
    ON DELETE RESTRICT,
  CONSTRAINT `fk_fileversion_nodo`
    FOREIGN KEY (`nodo_id`)
    REFERENCES `database`.`nodo` (`nodo_id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`historial`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`historial` (
  `historial_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tipo_evento` ENUM('CREACION', 'MODIFICACION', 'ELIMINACION', 'LOGIN', 'LOGOUT') NOT NULL,
  `entidad_afectada` VARCHAR(45) NULL DEFAULT NULL,
  `id_entidad_afectada` BIGINT NULL DEFAULT NULL,
  `descripcion_evento` TEXT NOT NULL,
  `fecha_evento` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ip_origen` VARCHAR(128) NULL DEFAULT NULL,
  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`historial_id`),
  INDEX `fk_historial_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
  CONSTRAINT `fk_historial_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`impersonacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`impersonacion` (
  `impersonacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `fecha_inicio_impersonacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_fin_impersonacion` DATETIME NULL DEFAULT NULL,
  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`impersonacion_id`),
  INDEX `fk_impersonacion_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
  CONSTRAINT `fk_impersonacion_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`nodo_has_enlace`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`nodo_has_enlace` (
  `nodo_id` BIGINT UNSIGNED NOT NULL,
  `enlace_id` BIGINT UNSIGNED NOT NULL,
  `proposito` ENUM('CONTENT', 'THUMBNAIL', 'METADATA', 'VERSION') NOT NULL DEFAULT 'CONTENT',
  `es_principal` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Si es el enlace principal del nodo',
  `fecha_asociacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`nodo_id`, `enlace_id`),
  INDEX `idx_nodo_enlace_proposito` (`nodo_id` ASC, `proposito` ASC) VISIBLE,
  INDEX `idx_nodo_enlace_principal` (`nodo_id` ASC, `es_principal` ASC) VISIBLE,
  INDEX `fk_nodo_enlace_enlace` (`enlace_id` ASC) VISIBLE,
  CONSTRAINT `fk_nodo_enlace_enlace`
    FOREIGN KEY (`enlace_id`)
    REFERENCES `database`.`enlace` (`enlace_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_nodo_enlace_nodo`
    FOREIGN KEY (`nodo_id`)
    REFERENCES `database`.`nodo` (`nodo_id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`nodo_permission`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`nodo_permission` (
  `nodo_permission_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nodo_id` BIGINT UNSIGNED NOT NULL,
  `usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL,
  `equipo_id` BIGINT UNSIGNED NULL DEFAULT NULL,
  `permiso` ENUM('read', 'WRITE', 'ADMIN') NOT NULL DEFAULT 'read',
  `inheritable` BOOLEAN NOT NULL DEFAULT TRUE,
  `creado_en` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`nodo_permission_id`),
  INDEX `idx_perm_nodo` (`nodo_id` ASC) VISIBLE,
  CONSTRAINT `fk_perm_nodo`
    FOREIGN KEY (`nodo_id`)
    REFERENCES `database`.`nodo` (`nodo_id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`nodo_tag_master`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`nodo_tag_master` (
  `tag_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`tag_id`),
  UNIQUE INDEX `ux_tag_nombre` (`nombre` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`nodo_tag`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`nodo_tag` (
  `nodo_id` BIGINT UNSIGNED NOT NULL,
  `tag_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`nodo_id`, `tag_id`),
  INDEX `fk_nt_tag` (`tag_id` ASC) VISIBLE,
  CONSTRAINT `fk_nt_nodo`
    FOREIGN KEY (`nodo_id`)
    REFERENCES `database`.`nodo` (`nodo_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_nt_tag`
    FOREIGN KEY (`tag_id`)
    REFERENCES `database`.`nodo_tag_master` (`tag_id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`payload`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`payload` (
  `payload_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tipo_payload` ENUM('BOTON', 'ENLACE', 'JSON', 'OTRO') NOT NULL,
  `contenido_payload` JSON NOT NULL,
  `mensaje_mensaje_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`payload_id`),
  INDEX `fk_payload_mensaje1_idx` (`mensaje_mensaje_id` ASC) VISIBLE,
  CONSTRAINT `fk_payload_mensaje1`
    FOREIGN KEY (`mensaje_mensaje_id`)
    REFERENCES `database`.`mensaje` (`mensaje_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`proyecto_has_enlace`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`proyecto_has_enlace` (
  `proyecto_id` BIGINT UNSIGNED NOT NULL,
  `enlace_id` BIGINT UNSIGNED NOT NULL,
  `proposito` ENUM('DOCUMENTACION', 'RECURSO', 'BACKUP', 'METADATA') NOT NULL DEFAULT 'RECURSO',
  `fecha_asociacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`proyecto_id`, `enlace_id`),
  INDEX `idx_proyecto_enlace_proposito` (`proyecto_id` ASC, `proposito` ASC) VISIBLE,
  INDEX `fk_proyecto_enlace_enlace` (`enlace_id` ASC) VISIBLE,
  CONSTRAINT `fk_proyecto_enlace_enlace`
    FOREIGN KEY (`enlace_id`)
    REFERENCES `database`.`enlace` (`enlace_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_proyecto_enlace_proyecto`
    FOREIGN KEY (`proyecto_id`)
    REFERENCES `database`.`proyecto` (`proyecto_id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`proyecto_has_repositorio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`proyecto_has_repositorio` (
  `proyecto_proyecto_id` BIGINT UNSIGNED NOT NULL,
  `repositorio_repositorio_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`proyecto_proyecto_id`, `repositorio_repositorio_id`),
  INDEX `fk_proyecto_has_repositorio_repositorio1_idx` (`repositorio_repositorio_id` ASC) VISIBLE,
  INDEX `fk_proyecto_has_repositorio_proyecto1_idx` (`proyecto_proyecto_id` ASC) VISIBLE,
  CONSTRAINT `fk_proyecto_has_repositorio_proyecto1`
    FOREIGN KEY (`proyecto_proyecto_id`)
    REFERENCES `database`.`proyecto` (`proyecto_id`),
  CONSTRAINT `fk_proyecto_has_repositorio_repositorio1`
    FOREIGN KEY (`repositorio_repositorio_id`)
    REFERENCES `database`.`repositorio` (`repositorio_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`recurso`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`recurso` (
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
    REFERENCES `database`.`contenido` (`contenido_id`),
  CONSTRAINT `fk_recurso_enlace1`
    FOREIGN KEY (`enlace_enlace_id`)
    REFERENCES `database`.`enlace` (`enlace_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`repositorio_has_enlace`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`repositorio_has_enlace` (
  `repositorio_repositorio_id` BIGINT UNSIGNED NOT NULL,
  `enlace_enlace_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`repositorio_repositorio_id`, `enlace_enlace_id`),
  INDEX `fk_repositorio_has_enlace_enlace1_idx` (`enlace_enlace_id` ASC) VISIBLE,
  INDEX `fk_repositorio_has_enlace_repositorio1_idx` (`repositorio_repositorio_id` ASC) VISIBLE,
  CONSTRAINT `fk_repositorio_has_enlace_enlace1`
    FOREIGN KEY (`enlace_enlace_id`)
    REFERENCES `database`.`enlace` (`enlace_id`),
  CONSTRAINT `fk_repositorio_has_enlace_repositorio1`
    FOREIGN KEY (`repositorio_repositorio_id`)
    REFERENCES `database`.`repositorio` (`repositorio_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`rol`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`rol` (
  `rol_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre_rol` ENUM('DEV', 'QA', 'PO', 'SA') NOT NULL COMMENT 'Tipo de rol global en la plataforma',
  `tipo_rol_proyecto` VARCHAR(100) NULL DEFAULT NULL COMMENT 'Nombre descriptivo del rol específico del proyecto (ej: Desarrollador Senior Frontend)',
  `descripcion_rol` TEXT NULL DEFAULT NULL COMMENT 'Descripción detallada del rol',
  `activo` BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Si el rol está activo',
  PRIMARY KEY (`rol_id`),
  UNIQUE INDEX `idx_nombre_rol_unique` (`tipo_rol_proyecto` ASC) VISIBLE,
  INDEX `idx_nombre_rol` (`nombre_rol` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`solicitud_acceso_api`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`solicitud_acceso_api` (
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
    REFERENCES `database`.`usuario` (`usuario_id`),
  CONSTRAINT `fk_solicitud_acceso_api_api1`
    FOREIGN KEY (`api_api_id`)
    REFERENCES `database`.`api` (`api_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`ticket`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`ticket` (
  `ticket_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `asunto_ticket` VARCHAR(255) NOT NULL,
  `cuerpo_ticket` TEXT NOT NULL,
  `fecha_creacion` DATETIME NOT NULL,
  `fecha_cierre` DATETIME NULL DEFAULT NULL,
  `estado_ticket` ENUM('ENVIADO', 'RECIBIDO') NOT NULL,
  `etapa_ticket` ENUM('PENDIENTE', 'EN_PROGRESO', 'RESUELTO', 'CERRADO', 'RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
  `tipo_ticket` ENUM('INCIDENCIA', 'CONSULTA', 'REQUERIMIENTO') NOT NULL DEFAULT 'CONSULTA',
  `prioridad_ticket` ENUM('BAJA', 'MEDIA', 'ALTA') NOT NULL DEFAULT 'MEDIA',
  `reportado_por_usuario_id` BIGINT UNSIGNED NOT NULL,
  `asignado_a_usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL,
  `respuesta_ticket` VARCHAR(255) NULL,
  PRIMARY KEY (`ticket_id`),
  INDEX `fk_ticket_usuario1_idx` (`reportado_por_usuario_id` ASC) VISIBLE,
  INDEX `fk_ticket_usuario2_idx` (`asignado_a_usuario_id` ASC) VISIBLE,
  INDEX `ix_ticket_estado_etapa` (`estado_ticket` ASC, `etapa_ticket` ASC) VISIBLE,
  CONSTRAINT `fk_ticket_usuario1`
    FOREIGN KEY (`reportado_por_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`),
  CONSTRAINT `fk_ticket_usuario2`
    FOREIGN KEY (`asignado_a_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`ticket_has_usuario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`ticket_has_usuario` (
  `ticket_ticket_id` BIGINT UNSIGNED NOT NULL,
  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`ticket_ticket_id`, `usuario_usuario_id`),
  INDEX `fk_ticket_has_usuario_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
  INDEX `fk_ticket_has_usuario_ticket1_idx` (`ticket_ticket_id` ASC) VISIBLE,
  CONSTRAINT `fk_ticket_has_usuario_ticket1`
    FOREIGN KEY (`ticket_ticket_id`)
    REFERENCES `database`.`ticket` (`ticket_id`),
  CONSTRAINT `fk_ticket_has_usuario_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`token`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`token` (
  `token_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `valor_token` VARCHAR(45) NULL DEFAULT NULL,
  `estado_token` ENUM('ACTIVO', 'REVOCADO') NULL DEFAULT NULL,
  `fecha_creacion_token` DATETIME NULL DEFAULT NULL,
  `fecha_expiracion_token` DATETIME NULL DEFAULT NULL,
  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`token_id`),
  INDEX `fk_token_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
  CONSTRAINT `fk_token_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`usuario_has_equipo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`usuario_has_equipo` (
  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
  `equipo_equipo_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`usuario_usuario_id`, `equipo_equipo_id`),
  INDEX `fk_usuario_has_equipo_equipo1_idx` (`equipo_equipo_id` ASC) VISIBLE,
  INDEX `fk_usuario_has_equipo_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
  CONSTRAINT `fk_usuario_has_equipo_equipo1`
    FOREIGN KEY (`equipo_equipo_id`)
    REFERENCES `database`.`equipo` (`equipo_id`),
  CONSTRAINT `fk_usuario_has_equipo_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`usuario_has_proyecto`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`usuario_has_proyecto` (
  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
  `proyecto_proyecto_id` BIGINT UNSIGNED NOT NULL,
  `privilegio_usuario_proyecto` ENUM('EDITOR', 'LECTOR', 'COMENTADOR') NOT NULL DEFAULT 'LECTOR',
  `fecha_usuario_proyecto` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`usuario_usuario_id`, `proyecto_proyecto_id`),
  INDEX `fk_usuario_has_proyecto_proyecto1_idx` (`proyecto_proyecto_id` ASC) VISIBLE,
  INDEX `fk_usuario_has_proyecto_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
  CONSTRAINT `fk_usuario_has_proyecto_proyecto1`
    FOREIGN KEY (`proyecto_proyecto_id`)
    REFERENCES `database`.`proyecto` (`proyecto_id`),
  CONSTRAINT `fk_usuario_has_proyecto_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`usuario_has_repositorio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`usuario_has_repositorio` (
  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
  `repositorio_repositorio_id` BIGINT UNSIGNED NOT NULL,
  `privilegio_usuario_repositorio` ENUM('EDITOR', 'LECTOR') NOT NULL DEFAULT 'LECTOR',
  `fecha_usuario_repositorio` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`usuario_usuario_id`, `repositorio_repositorio_id`),
  INDEX `fk_usuario_has_repositorio_repositorio1_idx` (`repositorio_repositorio_id` ASC) VISIBLE,
  INDEX `fk_usuario_has_repositorio_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
  CONSTRAINT `fk_usuario_has_repositorio_repositorio1`
    FOREIGN KEY (`repositorio_repositorio_id`)
    REFERENCES `database`.`repositorio` (`repositorio_id`),
  CONSTRAINT `fk_usuario_has_repositorio_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`usuario_has_rol`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`usuario_has_rol` (
  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
  `rol_rol_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`usuario_usuario_id`, `rol_rol_id`),
  INDEX `fk_usuario_has_rol_rol1_idx` (`rol_rol_id` ASC) VISIBLE,
  INDEX `fk_usuario_has_rol_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
  CONSTRAINT `fk_usuario_has_rol_rol1`
    FOREIGN KEY (`rol_rol_id`)
    REFERENCES `database`.`rol` (`rol_id`),
  CONSTRAINT `fk_usuario_has_rol_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `database`.`version_api_has_enlace`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `database`.`version_api_has_enlace` (
  `version_api_version_id` BIGINT UNSIGNED NOT NULL,
  `enlace_enlace_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`version_api_version_id`, `enlace_enlace_id`),
  INDEX `fk_version_api_has_enlace_enlace1_idx` (`enlace_enlace_id` ASC) VISIBLE,
  INDEX `fk_version_api_has_enlace_version_api1_idx` (`version_api_version_id` ASC) VISIBLE,
  CONSTRAINT `fk_version_api_has_enlace_enlace1`
    FOREIGN KEY (`enlace_enlace_id`)
    REFERENCES `database`.`enlace` (`enlace_id`),
  CONSTRAINT `fk_version_api_has_enlace_version_api1`
    FOREIGN KEY (`version_api_version_id`)
    REFERENCES `database`.`version_api` (`version_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
