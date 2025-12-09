/**
 * OpenAPI Validator - Cliente-side
 * 
 * Validador de contratos OpenAPI 3.0 usando SwaggerParser (CDN).
 * Este módulo complementa la validación del backend proporcionando
 * feedback inmediato al usuario mientras edita el contrato.
 * 
 * Dependencias:
 * - SwaggerParser (cargado por CDN en create-api.html)
 * 
 * @author GitHub Copilot
 * @since 2025-11-04
 */

const OpenAPIValidator = (function() {
    'use strict';

    // Estado interno
    let contratoValidado = null;
    let validacionEnProgreso = false;

    /**
     * Valida un contrato OpenAPI y muestra el resultado en el div especificado
     * 
     * @param {string} contenidoYaml - Contenido YAML/JSON del contrato OpenAPI
     * @param {HTMLElement} resultDiv - Elemento donde mostrar el resultado
     */
    async function validarContrato(contenidoYaml, resultDiv) {
        if (!resultDiv) {
            console.error('[OpenAPIValidator] resultDiv no proporcionado');
            return;
        }

        // Si ya hay una validación en progreso, ignorar
        if (validacionEnProgreso) {
            return;
        }

        // Limpiar resultado anterior
        resultDiv.innerHTML = '';
        resultDiv.className = 'validation-result';

        // Verificar que SwaggerParser esté disponible
        if (typeof SwaggerParser === 'undefined') {
            mostrarError(resultDiv, 
                'SwaggerParser no está disponible',
                [
                    'El validador de OpenAPI requiere conexión a internet para cargar la librería.',
                    'Por favor, recarga la página e intenta nuevamente.',
                    'Si el problema persiste, verifica tu conexión a internet.'
                ]
            );
            console.error('[OpenAPIValidator] SwaggerParser CDN no disponible. Verifica la conexión a internet.');
            return;
        }

        // Validar que hay contenido
        if (!contenidoYaml || contenidoYaml.trim().length === 0) {
            mostrarWarning(resultDiv, 'El contrato está vacío', []);
            contratoValidado = null;
            return;
        }

        try {
            validacionEnProgreso = true;
            mostrarCargando(resultDiv);

            // Parsear y validar con SwaggerParser
            const api = await SwaggerParser.validate(contenidoYaml);

            // Validación exitosa
            contratoValidado = {
                contenido: contenidoYaml,
                api: api,
                timestamp: new Date().toISOString()
            };

            mostrarExito(resultDiv, api);

        } catch (error) {
            // Error de validación
            contratoValidado = null;
            mostrarError(resultDiv, error.message, extraerDetallesError(error));

        } finally {
            validacionEnProgreso = false;
        }
    }

    /**
     * Obtiene el último contrato validado exitosamente
     * 
     * @returns {Object|null} Objeto con {contenido, api, timestamp} o null si no hay validación exitosa
     */
    function getContratoValidado() {
        return contratoValidado;
    }

    /**
     * Limpia el estado de validación
     */
    function limpiarValidacion() {
        contratoValidado = null;
    }

    /**
     * Muestra mensaje de carga
     */
    function mostrarCargando(div) {
        div.className = 'validation-result validating';
        div.innerHTML = `
            <div class="validation-message">
                <i class="fas fa-spinner fa-spin"></i>
                <span>Validando contrato OpenAPI...</span>
            </div>
        `;
    }

    /**
     * Muestra resultado exitoso
     */
    function mostrarExito(div, api) {
        div.className = 'validation-result valid';
        
        const version = api.openapi || api.swagger || 'Desconocida';
        const title = api.info?.title || 'Sin título';
        const apiVersion = api.info?.version || 'Sin versión';
        const pathsCount = Object.keys(api.paths || {}).length;
        const schemasCount = Object.keys(api.components?.schemas || {}).length;

        div.innerHTML = `
            <div class="validation-header">
                <i class="fas fa-check-circle"></i>
                <h5>✅ Contrato OpenAPI válido</h5>
            </div>
            <div class="validation-details">
                <div class="detail-row">
                    <span class="detail-label">Especificación:</span>
                    <span class="detail-value">OpenAPI ${version}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Título:</span>
                    <span class="detail-value">${escapeHtml(title)}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Versión:</span>
                    <span class="detail-value">${escapeHtml(apiVersion)}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Endpoints:</span>
                    <span class="detail-value">${pathsCount} path(s)</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Esquemas:</span>
                    <span class="detail-value">${schemasCount} schema(s)</span>
                </div>
            </div>
        `;
    }

    /**
     * Muestra error de validación
     */
    function mostrarError(div, mensaje, detalles) {
        div.className = 'validation-result invalid';
        
        let detallesHtml = '';
        if (detalles && detalles.length > 0) {
            detallesHtml = '<ul class="error-list">';
            detalles.forEach(detalle => {
                detallesHtml += `<li>${escapeHtml(detalle)}</li>`;
            });
            detallesHtml += '</ul>';
        }

        div.innerHTML = `
            <div class="validation-header">
                <i class="fas fa-times-circle"></i>
                <h5>❌ Error en el contrato OpenAPI</h5>
            </div>
            <div class="validation-error-message">
                ${escapeHtml(mensaje)}
            </div>
            ${detallesHtml}
        `;
    }

    /**
     * Muestra advertencia
     */
    function mostrarWarning(div, mensaje, detalles) {
        div.className = 'validation-result warning';
        
        div.innerHTML = `
            <div class="validation-header">
                <i class="fas fa-exclamation-triangle"></i>
                <h5>⚠️ ${escapeHtml(mensaje)}</h5>
            </div>
        `;
    }

    /**
     * Muestra mensaje informativo
     */
    function mostrarInfo(div, mensaje, submensaje) {
        div.className = 'validation-result info';
        
        let submensajeHtml = '';
        if (submensaje) {
            submensajeHtml = `<p class="validation-info-submessage">${escapeHtml(submensaje)}</p>`;
        }

        div.innerHTML = `
            <div class="validation-header">
                <i class="fas fa-info-circle"></i>
                <h5>ℹ️ ${escapeHtml(mensaje)}</h5>
            </div>
            ${submensajeHtml}
        `;
    }

    /**
     * Extrae detalles del error de SwaggerParser
     */
    function extraerDetallesError(error) {
        const detalles = [];
        
        // Error en path específico
        if (error.path) {
            detalles.push(`Path: ${error.path}`);
        }

        // Errores de JSON Schema
        if (error.details && Array.isArray(error.details)) {
            error.details.forEach(detail => {
                if (detail.path) {
                    detalles.push(`${detail.path}: ${detail.message}`);
                } else {
                    detalles.push(detail.message);
                }
            });
        }

        return detalles;
    }

    /**
     * Escapa HTML para prevenir XSS
     */
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // API pública
    return {
        validarContrato: validarContrato,
        getContratoValidado: getContratoValidado,
        limpiarValidacion: limpiarValidacion
    };
})();

// Log de inicialización y diagnóstico
console.log('[OpenAPIValidator] Módulo cargado correctamente ✅');

// Verificar que SwaggerParser esté disponible
if (typeof SwaggerParser !== 'undefined') {
    console.log('[OpenAPIValidator] SwaggerParser disponible ✅');
} else {
    console.warn('[OpenAPIValidator] ⚠️ SwaggerParser NO disponible. Esperando a que se cargue desde CDN...');
    
    // Verificar periódicamente (máximo 10 intentos = 10 segundos)
    let intentos = 0;
    const checkInterval = setInterval(() => {
        intentos++;
        if (typeof SwaggerParser !== 'undefined') {
            console.log(`[OpenAPIValidator] SwaggerParser disponible después de ${intentos} segundo(s) ✅`);
            clearInterval(checkInterval);
        } else if (intentos >= 10) {
            console.error('[OpenAPIValidator] ❌ SwaggerParser no se pudo cargar después de 10 segundos. Verifica tu conexión a internet.');
            clearInterval(checkInterval);
        }
    }, 1000);
}
