-- =================================================================
-- TABLAS ADICIONALES RECOMENDADAS PARA EL SISTEMA
-- =================================================================

-- Tabla para solicitudes de acceso a proyectos
-- Esta tabla permitirá que los usuarios soliciten acceso a proyectos privados/restringidos
CREATE TABLE IF NOT EXISTS `database`.`solicitud_acceso_proyecto` (
  `solicitud_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `proyecto_proyecto_id` BIGINT UNSIGNED NOT NULL,
  `solicitante_usuario_id` BIGINT UNSIGNED NOT NULL,
  `tipo_solicitud` ENUM('ACCESO_LECTOR', 'ACCESO_COMENTADOR', 'ACCESO_EDITOR') NOT NULL DEFAULT 'ACCESO_LECTOR',
  `estado_solicitud` ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO', 'CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
  `privilegio_solicitado` ENUM('LECTOR', 'COMENTADOR', 'EDITOR') NOT NULL DEFAULT 'LECTOR',
  `comentario_solicitud` TEXT NULL DEFAULT NULL,
  `fecha_solicitud` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_respuesta` DATETIME NULL DEFAULT NULL,
  `aprobador_usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL,
  `comentario_respuesta` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`solicitud_id`),
  INDEX `fk_solicitud_proyecto_proyecto1_idx` (`proyecto_proyecto_id` ASC) VISIBLE,
  INDEX `fk_solicitud_proyecto_solicitante1_idx` (`solicitante_usuario_id` ASC) VISIBLE,
  INDEX `fk_solicitud_proyecto_aprobador1_idx` (`aprobador_usuario_id` ASC) VISIBLE,
  INDEX `idx_solicitud_estado` (`estado_solicitud` ASC) VISIBLE,
  INDEX `idx_solicitud_fecha` (`fecha_solicitud` ASC) VISIBLE,
  UNIQUE INDEX `uk_solicitud_proyecto_usuario` (`proyecto_proyecto_id` ASC, `solicitante_usuario_id` ASC, `estado_solicitud` ASC) VISIBLE,
  CONSTRAINT `fk_solicitud_proyecto_proyecto1`
    FOREIGN KEY (`proyecto_proyecto_id`)
    REFERENCES `database`.`proyecto` (`proyecto_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_solicitud_proyecto_solicitante1`
    FOREIGN KEY (`solicitante_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_solicitud_proyecto_aprobador1`
    FOREIGN KEY (`aprobador_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
) ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

-- Tabla para solicitudes de acceso a repositorios
-- Similar a la anterior pero para repositorios
CREATE TABLE IF NOT EXISTS `database`.`solicitud_acceso_repositorio` (
  `solicitud_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `repositorio_repositorio_id` BIGINT UNSIGNED NOT NULL,
  `solicitante_usuario_id` BIGINT UNSIGNED NOT NULL,
  `tipo_solicitud` ENUM('ACCESO_LECTOR', 'ACCESO_COMENTADOR', 'ACCESO_EDITOR') NOT NULL DEFAULT 'ACCESO_LECTOR',
  `estado_solicitud` ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO', 'CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
  `privilegio_solicitado` ENUM('LECTOR', 'COMENTADOR', 'EDITOR') NOT NULL DEFAULT 'LECTOR',
  `comentario_solicitud` TEXT NULL DEFAULT NULL,
  `fecha_solicitud` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_respuesta` DATETIME NULL DEFAULT NULL,
  `aprobador_usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL,
  `comentario_respuesta` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`solicitud_id`),
  INDEX `fk_solicitud_repositorio_repositorio1_idx` (`repositorio_repositorio_id` ASC) VISIBLE,
  INDEX `fk_solicitud_repositorio_solicitante1_idx` (`solicitante_usuario_id` ASC) VISIBLE,
  INDEX `fk_solicitud_repositorio_aprobador1_idx` (`aprobador_usuario_id` ASC) VISIBLE,
  INDEX `idx_solicitud_repo_estado` (`estado_solicitud` ASC) VISIBLE,
  INDEX `idx_solicitud_repo_fecha` (`fecha_solicitud` ASC) VISIBLE,
  UNIQUE INDEX `uk_solicitud_repositorio_usuario` (`repositorio_repositorio_id` ASC, `solicitante_usuario_id` ASC, `estado_solicitud` ASC) VISIBLE,
  CONSTRAINT `fk_solicitud_repositorio_repositorio1`
    FOREIGN KEY (`repositorio_repositorio_id`)
    REFERENCES `database`.`repositorio` (`repositorio_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_solicitud_repositorio_solicitante1`
    FOREIGN KEY (`solicitante_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_solicitud_repositorio_aprobador1`
    FOREIGN KEY (`aprobador_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
) ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

-- Tabla para invitaciones a proyectos
-- Para cuando los propietarios/editores invitan directamente a otros usuarios
CREATE TABLE IF NOT EXISTS `database`.`invitacion_proyecto` (
  `invitacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `proyecto_proyecto_id` BIGINT UNSIGNED NOT NULL,
  `invitador_usuario_id` BIGINT UNSIGNED NOT NULL,
  `invitado_usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL,
  `email_invitado` VARCHAR(255) NULL DEFAULT NULL, -- Para invitar por email
  `privilegio_ofrecido` ENUM('LECTOR', 'COMENTADOR', 'EDITOR') NOT NULL DEFAULT 'LECTOR',
  `estado_invitacion` ENUM('PENDIENTE', 'ACEPTADA', 'RECHAZADA', 'EXPIRADA', 'CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
  `mensaje_invitacion` TEXT NULL DEFAULT NULL,
  `fecha_invitacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_expiracion` DATETIME NULL DEFAULT NULL,
  `fecha_respuesta` DATETIME NULL DEFAULT NULL,
  `token_invitacion` VARCHAR(128) NULL DEFAULT NULL, -- Para invitaciones por email
  PRIMARY KEY (`invitacion_id`),
  INDEX `fk_invitacion_proyecto_proyecto1_idx` (`proyecto_proyecto_id` ASC) VISIBLE,
  INDEX `fk_invitacion_proyecto_invitador1_idx` (`invitador_usuario_id` ASC) VISIBLE,
  INDEX `fk_invitacion_proyecto_invitado1_idx` (`invitado_usuario_id` ASC) VISIBLE,
  INDEX `idx_invitacion_proyecto_estado` (`estado_invitacion` ASC) VISIBLE,
  INDEX `idx_invitacion_proyecto_token` (`token_invitacion` ASC) VISIBLE,
  INDEX `idx_invitacion_proyecto_email` (`email_invitado` ASC) VISIBLE,
  CONSTRAINT `fk_invitacion_proyecto_proyecto1`
    FOREIGN KEY (`proyecto_proyecto_id`)
    REFERENCES `database`.`proyecto` (`proyecto_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_invitacion_proyecto_invitador1`
    FOREIGN KEY (`invitador_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_invitacion_proyecto_invitado1`
    FOREIGN KEY (`invitado_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`)
    ON DELETE CASCADE
) ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

-- Tabla para invitaciones a repositorios
-- Similar a la anterior pero para repositorios
CREATE TABLE IF NOT EXISTS `database`.`invitacion_repositorio` (
  `invitacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `repositorio_repositorio_id` BIGINT UNSIGNED NOT NULL,
  `invitador_usuario_id` BIGINT UNSIGNED NOT NULL,
  `invitado_usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL,
  `email_invitado` VARCHAR(255) NULL DEFAULT NULL, -- Para invitar por email
  `privilegio_ofrecido` ENUM('LECTOR', 'COMENTADOR', 'EDITOR') NOT NULL DEFAULT 'LECTOR',
  `estado_invitacion` ENUM('PENDIENTE', 'ACEPTADA', 'RECHAZADA', 'EXPIRADA', 'CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
  `mensaje_invitacion` TEXT NULL DEFAULT NULL,
  `fecha_invitacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_expiracion` DATETIME NULL DEFAULT NULL,
  `fecha_respuesta` DATETIME NULL DEFAULT NULL,
  `token_invitacion` VARCHAR(128) NULL DEFAULT NULL, -- Para invitaciones por email
  PRIMARY KEY (`invitacion_id`),
  INDEX `fk_invitacion_repositorio_repositorio1_idx` (`repositorio_repositorio_id` ASC) VISIBLE,
  INDEX `fk_invitacion_repositorio_invitador1_idx` (`invitador_usuario_id` ASC) VISIBLE,
  INDEX `fk_invitacion_repositorio_invitado1_idx` (`invitado_usuario_id` ASC) VISIBLE,
  INDEX `idx_invitacion_repositorio_estado` (`estado_invitacion` ASC) VISIBLE,
  INDEX `idx_invitacion_repositorio_token` (`token_invitacion` ASC) VISIBLE,
  INDEX `idx_invitacion_repositorio_email` (`email_invitado` ASC) VISIBLE,
  CONSTRAINT `fk_invitacion_repositorio_repositorio1`
    FOREIGN KEY (`repositorio_repositorio_id`)
    REFERENCES `database`.`repositorio` (`repositorio_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_invitacion_repositorio_invitador1`
    FOREIGN KEY (`invitador_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_invitacion_repositorio_invitado1`
    FOREIGN KEY (`invitado_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`)
    ON DELETE CASCADE
) ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

-- Tabla para favoritos de usuario (proyectos y repositorios)
-- Para que los usuarios puedan marcar proyectos/repositorios como favoritos
CREATE TABLE IF NOT EXISTS `database`.`favorito` (
  `favorito_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `usuario_usuario_id` BIGINT UNSIGNED NOT NULL,
  `entidad_tipo` ENUM('PROYECTO', 'REPOSITORIO') NOT NULL,
  `entidad_id` BIGINT UNSIGNED NOT NULL,
  `fecha_favorito` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `notas_favorito` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`favorito_id`),
  INDEX `fk_favorito_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
  INDEX `idx_favorito_entidad` (`entidad_tipo` ASC, `entidad_id` ASC) VISIBLE,
  INDEX `idx_favorito_fecha` (`fecha_favorito` ASC) VISIBLE,
  UNIQUE INDEX `uk_favorito_usuario_entidad` (`usuario_usuario_id` ASC, `entidad_tipo` ASC, `entidad_id` ASC) VISIBLE,
  CONSTRAINT `fk_favorito_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`)
    ON DELETE CASCADE
) ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

-- Tabla para seguimiento de visualizaciones (analytics básico)
-- Para saber qué proyectos/repositorios son más vistos
CREATE TABLE IF NOT EXISTS `database`.`visualizacion` (
  `visualizacion_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `usuario_usuario_id` BIGINT UNSIGNED NULL DEFAULT NULL, -- NULL para usuarios anónimos
  `entidad_tipo` ENUM('PROYECTO', 'REPOSITORIO', 'ARCHIVO') NOT NULL,
  `entidad_id` BIGINT UNSIGNED NOT NULL,
  `fecha_visualizacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ip_usuario` VARCHAR(45) NULL DEFAULT NULL,
  `user_agent` TEXT NULL DEFAULT NULL,
  `duracion_segundos` INT NULL DEFAULT NULL, -- Tiempo que pasó viendo
  `referrer` VARCHAR(500) NULL DEFAULT NULL, -- De dónde vino
  PRIMARY KEY (`visualizacion_id`),
  INDEX `fk_visualizacion_usuario1_idx` (`usuario_usuario_id` ASC) VISIBLE,
  INDEX `idx_visualizacion_entidad` (`entidad_tipo` ASC, `entidad_id` ASC) VISIBLE,
  INDEX `idx_visualizacion_fecha` (`fecha_visualizacion` ASC) VISIBLE,
  CONSTRAINT `fk_visualizacion_usuario1`
    FOREIGN KEY (`usuario_usuario_id`)
    REFERENCES `database`.`usuario` (`usuario_id`)
    ON DELETE SET NULL
) ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

-- =================================================================
-- ÍNDICES ADICIONALES RECOMENDADOS PARA OPTIMIZACIÓN
-- =================================================================

-- Índices para mejorar el rendimiento de las queries principales
ALTER TABLE `database`.`proyecto` 
ADD INDEX `idx_proyecto_propietario_fecha` (`propietario_proyecto` ASC, `propietario_id` ASC, `created_at` DESC),
ADD INDEX `idx_proyecto_visibilidad_estado` (`visibilidad_proyecto` ASC, `estado_proyecto` ASC),
ADD INDEX `idx_proyecto_fecha_creacion` (`created_at` DESC);

ALTER TABLE `database`.`repositorio` 
ADD INDEX `idx_repositorio_owner_fecha` (`owner_type` ASC, `owner_id` ASC, `fecha_creacion` DESC),
ADD INDEX `idx_repositorio_visibilidad_estado` (`visibilidad_repositorio` ASC, `estado_repositorio` ASC),
ADD INDEX `idx_repositorio_actividad` (`last_activity_at` DESC),
ADD INDEX `idx_repositorio_fork` (`is_fork` ASC, `forked_from_repo_id` ASC);

ALTER TABLE `database`.`nodo` 
ADD INDEX `idx_nodo_container_parent` (`container_type` ASC, `container_id` ASC, `parent_id` ASC),
ADD INDEX `idx_nodo_creado_por_fecha` (`creado_por` ASC, `creado_en` DESC),
ADD INDEX `idx_nodo_actualizado` (`actualizado_en` DESC),
ADD INDEX `idx_nodo_tipo_deleted` (`tipo` ASC, `is_deleted` ASC);

ALTER TABLE `database`.`file_version` 
ADD INDEX `idx_file_version_vigente` (`vigente` ASC, `creado_en` DESC),
ADD INDEX `idx_file_version_nodo_vigente` (`nodo_id` ASC, `vigente` ASC);

ALTER TABLE `database`.`usuario_has_proyecto` 
ADD INDEX `idx_usuario_proyecto_privilegio` (`privilegio_usuario_proyecto` ASC),
ADD INDEX `idx_usuario_proyecto_fecha` (`fecha_usuario_proyecto` DESC);

ALTER TABLE `database`.`usuario_has_repositorio` 
ADD INDEX `idx_usuario_repositorio_privilegio` (`privilegio_usuario_repositorio` ASC),
ADD INDEX `idx_usuario_repositorio_fecha` (`fecha_usuario_repositorio` DESC);

ALTER TABLE `database`.`equipo_has_proyecto` 
ADD INDEX `idx_equipo_proyecto_privilegio` (`privilegio_equipo_proyecto` ASC),
ADD INDEX `idx_equipo_proyecto_fecha` (`fecha_equipo_proyecto` DESC);

ALTER TABLE `database`.`equipo_has_repositorio` 
ADD INDEX `idx_equipo_repositorio_privilegio` (`privilegio_equipo_repositorio` ASC),
ADD INDEX `idx_equipo_repositorio_fecha` (`fecha_equipo_repositorio` DESC);