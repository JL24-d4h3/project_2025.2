# README - Configuración del Proyecto

## Archivos de Configuración Necesarios

Este proyecto requiere configurar archivos sensibles que **NO** están incluidos en el repositorio por seguridad.

### 1. application.properties

Copia `application.properties.example` como `application.properties` y configura:
- Credenciales de base de datos MySQL
- Configuración de Google Cloud Storage
- OAuth2 credentials (Google)

### 2. Claves de Google Cloud Storage

Coloca tus archivos de claves JSON en:
- `src/main/resources/dev-portal-storage-manager-key.json`
- `src/main/resources/devportal-storage-key.json`

### 3. application-local.properties (opcional)

Para desarrollo local, puedes crear este archivo con configuraciones específicas.

## Instalación

1. Clona el repositorio
2. Configura los archivos mencionados arriba
3. Ejecuta: `mvn clean install`
4. Inicia la aplicación: `mvn spring-boot:run`

## Optimizaciones Implementadas

- ✅ AJAX navigation en navegación de archivos
- ✅ Eliminación de N+1 queries con JOIN FETCH
- ✅ Cache de 5 minutos en frontend
- ✅ REST APIs para dashboards
- ✅ Navegación sin recarga de página

Para más detalles, consulta `OPTIMIZACIONES_RENDIMIENTO_COMPLETO.md`
