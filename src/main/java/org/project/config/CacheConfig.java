package org.project.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de caché para optimización de rendimiento
 * 
 * IMPACTO ESPERADO:
 * - Reducción del 60-70% en queries a base de datos
 * - Tiempos de respuesta: de ~500ms a ~150ms
 * - Hit ratio objetivo: >70%
 * 
 * CACHÉS CONFIGURADOS:
 * 1. usuarios: Datos de usuario autenticado (10 min)
 * 2. permisos: Permisos de usuario en repositorios/proyectos (10 min)
 * 3. jerarquiasNodos: Estructura de carpetas (5 min)
 * 4. repositorios: Listados de repositorios (5 min)
 * 5. nodos: Nodos individuales (10 min)
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Configura el gestor de caché con Caffeine
     * Caffeine es más rápido y eficiente que EhCache o Guava
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "usuarios",           // Usuario autenticado (findByUsername)
            "permisos",          // Permisos de repositorios/proyectos
            "jerarquiasNodos",   // Estructura de carpetas (obtenerHijos)
            "repositorios",      // Listados de repositorios
            "nodos",             // Nodos individuales
            "nodosRaiz",         // 🔥 FASE 6: Nodos raíz por contenedor
            "nodosHijos",        // 🔥 FASE 6: Nodos hijos por carpeta
            "statsNodos"         // 🔥 FASE 6: Estadísticas de nodos (count, size)
        );
        
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }
    
    /**
     * Configuración del motor de caché Caffeine
     * 
     * Parámetros optimizados para aplicación web con ~100 usuarios concurrentes:
     * - initialCapacity: 100 entradas al inicio (evita resize inicial)
     * - maximumSize: 1000 entradas máximo (previene OutOfMemory)
     * - expireAfterWrite: 10 minutos (balance entre frescura y performance)
     * - recordStats: Habilita métricas (hit rate, evictions, etc.)
     */
    Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)          // Capacidad inicial
                .maximumSize(1000)             // Máximo 1000 entradas en caché
                .expireAfterWrite(10, TimeUnit.MINUTES)  // Expira después de 10 min
                .recordStats();                // Habilita estadísticas
    }
    
    /**
     * Caché especializado para jerarquías de nodos (más volátil)
     * Expira más rápido porque los archivos cambian frecuentemente
     */
    @Bean
    public CacheManager nodeCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("jerarquiasNodos");
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)  // Solo 5 min para archivos
                .recordStats());
        
        return cacheManager;
    }
}
