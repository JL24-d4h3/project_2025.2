## ⚙️ Sentencias MySQL para Modificación de Tablas

USE `dev_portal_sql`;

### Modificación de la Tabla `enlace`

ALTER TABLE `dev_portal_sql`.`enlace`
    MODIFY COLUMN `tipo_enlace` ENUM(
        'STORAGE',
        'METADATA',
        'THUMBNAIL',
        'BACKUP',
        'TEMPORAL',
        'TEXTO_CONTENIDO',
        'ENLACE_EXTERNO'
        ) NOT NULL DEFAULT 'STORAGE' COMMENT 'Propósito del enlace';

---

### Modificación de la Tabla `chatbot_conversacion`

ALTER TABLE `dev_portal_sql`.`chatbot_conversacion`
-- Modificación de la columna 'estado_conversacion' para incluir el nuevo valor 'EN_REVISION'
    MODIFY COLUMN `estado_conversacion` ENUM(
        'ABIERTA',
        'CERRADA',
        'REPORTADA',
        'EN_REVISION'
        ) NOT NULL DEFAULT 'ABIERTA',

-- Adición de la nueva columna 'contexto_inicial_ia'
    ADD COLUMN `contexto_inicial_ia` VARCHAR(24) NULL DEFAULT NULL COMMENT 'ObjectId MongoDB -> Collection: context_ia' AFTER `ticket_generado_id`,

-- Adición del índice para la nueva columna
    ADD INDEX `idx_contexto_inicial` (`contexto_inicial_ia` ASC) VISIBLE;



### Modificación de la Tabla `feedback`

ALTER TABLE `dev_portal_sql`.`feedback` 
DROP FOREIGN KEY `fk_feedback_revisado_por`;

ALTER TABLE `dev_portal_sql`.`feedback` 
DROP INDEX `fk_feedback_revisado_por_idx`,
DROP INDEX `idx_tipo_estado`,
DROP INDEX `idx_nosql_detalle`;


ALTER TABLE `dev_portal_sql`.`feedback` 
DROP COLUMN `tipo_feedback`,
DROP COLUMN `estado_feedback`,
DROP COLUMN `fecha_revision`,
DROP COLUMN `nosql_detalle_id`,
DROP COLUMN `revisado_por`;

ALTER TABLE `dev_portal_sql`.`feedback` 
ADD CONSTRAINT `chk_puntuacion_rango` 
CHECK (`puntuacion` >= 1.0 AND `puntuacion` <= 5.0);