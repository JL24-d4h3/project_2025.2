# 📊 PLAN DE OPTIMIZACIÓN COMPLETO - TelDev DevPortal

## 🎯 OBJETIVO
Reducir tiempos de carga de **15 segundos a <2 segundos** en todas las vistas principales.

---

## ✅ COMPLETADO (Fases 1-4)

### FASE 1: Cache de Servicios Base ✅
- **UserService**: `@Cacheable` en métodos de consulta
- **NodoService**: `@Cacheable` en jerarquías de nodos
- **Resultado**: 20-30% mejora en consultas repetidas

### FASE 2: Batch Loading de Permisos ✅
- **RepositoryService**: 8 métodos optimizados
  - `obtenerOtrosRepositorios`: 15s → 9s (40% mejora)
  - Batch loading de permisos en lugar de N+1 queries
- **ProjectService**: 4 métodos optimizados
  - `obtenerOtrosProyectos`: 5s → 4.5s (10% mejora)

### FASE 3: Índices de Base de Datos ✅
- **13 índices creados** en tablas clave:
  - `usuario_proyecto` (usuario_id, proyecto_id)
  - `usuario_repositorio` (usuario_id, repositorio_id)
  - `nodo` (parent_id, container_type, container_id)
  - `proyecto` (nombre_proyecto, estado_proyecto)
  - `repositorio` (nombre_repositorio, visibilidad)
- **Resultado**: 85% mejora en queries complejas

### FASE 4: JOIN FETCH en Queries ✅
- **NodoRepository**: `findRootNodesWithUsers()` y `findChildrenWithUsers()`
- Previene problema N+1 en carga de usuarios relacionados

### FASE 5: Paginación Backend ✅
- **Implementado**: LIMIT/OFFSET con batch loading
- **12 items por página** en proyectos y repositorios
- **Navegación**: Botones Previous/Next + contador "Mostrando X-Y de Z"
- **Resultado**: Carga inicial muy rápida, paginación smooth

---

## 🚀 PENDIENTE - PRÓXIMAS FASES

### FASE 6: Optimización Sistema de Archivos (CRÍTICO)
**Problema Actual**: 4 segundos para cargar raíz, 2 segundos para navegar entre carpetas

#### 6.1 Cache Multinivel
```java
@Service
public class NodoService {
    
    @Cacheable(value = "nodosRaiz", key = "#containerType + '_' + #containerId")
    public List<NodoDTO> obtenerNodosRaizDTO(Nodo.ContainerType containerType, Long containerId) {
        // Cache por 5 minutos
    }
    
    @Cacheable(value = "nodosHijos", key = "#parentId")
    public List<NodoDTO> obtenerHijosDTO(Long parentId) {
        // Cache por 5 minutos
    }
    
    @Cacheable(value = "statsNodos", key = "#containerType + '_' + #containerId")
    public Map<String, Object> obtenerEstadisticasContenedor(
        Nodo.ContainerType containerType, Long containerId) {
        // Cache estadísticas (total archivos, carpetas, espacio)
    }
}
```

**Configuración Cache**:
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "usuarios", "jerarquiasNodos", "nodosRaiz", "nodosHijos", "statsNodos"
        );
    }
}
```

**Impacto Esperado**: 4s → 0.5s (87.5% mejora)

---

#### 6.2 Batch Loading de Usuarios en Nodos
**Problema**: Cada nodo carga `creadoPor` y `actualizadoPor` individualmente

```java
// NodoService.java
public List<NodoDTO> obtenerNodosRaizDTO(Nodo.ContainerType containerType, Long containerId) {
    List<Nodo> nodos = nodoRepository.findRootNodesWithUsers(containerType, containerId);
    
    // 🔧 Cargar todos los usuarios de una vez
    Set<Long> userIds = nodos.stream()
        .flatMap(n -> Stream.of(
            n.getCreadoPor() != null ? n.getCreadoPor().getUsuarioId() : null,
            n.getActualizadoPor() != null ? n.getActualizadoPor().getUsuarioId() : null
        ))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    
    Map<Long, Usuario> usuariosMap = userService.obtenerUsuariosPorIds(userIds)
        .stream()
        .collect(Collectors.toMap(Usuario::getUsuarioId, u -> u));
    
    return nodos.stream()
        .map(n -> convertirADTOConUsuarios(n, usuariosMap))
        .collect(Collectors.toList());
}
```

**Impacto Esperado**: 2s → 0.8s en navegación (60% mejora)

---

#### 6.3 Lazy Loading de Estadísticas
**Problema**: Se calculan estadísticas en CADA carga de carpeta

```javascript
// project/files.html - Cargar stats via AJAX
document.addEventListener('DOMContentLoaded', function() {
    // Cargar stats de forma asíncrona
    fetch(`/api/projects/${projectId}/stats`)
        .then(response => response.json())
        .then(stats => {
            document.getElementById('totalArchivos').textContent = stats.total_archivos;
            document.getElementById('totalCarpetas').textContent = stats.total_carpetas;
            document.getElementById('espacioUsado').textContent = formatBytes(stats.espacio_usado);
        });
    
    // Página carga INMEDIATAMENTE sin esperar stats
});
```

**Impacto Esperado**: Percepción de carga instantánea

---

### FASE 7: Optimización de Dashboard
**Problema**: Dashboard carga todas las estadísticas síncronamente

#### 7.1 Carga Progresiva de Widgets
```html
<!-- dashboard.html -->
<div class="stats-widget" id="projectsWidget">
    <div class="skeleton-loader"></div> <!-- Placeholder -->
</div>

<script>
// Cargar cada widget de forma independiente
Promise.all([
    fetch('/api/stats/projects'),
    fetch('/api/stats/repositories'),
    fetch('/api/stats/teams')
]).then(responses => {
    // Renderizar cada widget cuando esté listo
});
</script>
```

**Impacto**: Dashboard visible en <1s, stats completas en 2-3s

---

#### 7.2 WebSockets para Stats en Tiempo Real
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new StatsWebSocketHandler(), "/ws/stats")
                .setAllowedOrigins("*");
    }
}
```

**Uso**: Dashboard recibe actualizaciones automáticas sin recargar

---

### FASE 8: Optimización de Queries SQL
**Análisis Pendiente**: Habilitar logs SQL para identificar queries lentas

```properties
# application.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

#### 8.1 Queries Nativas Optimizadas
```java
@Repository
public interface RepositoryQueryRepository {
    
    @Query(value = """
        SELECT DISTINCT r.* 
        FROM repositorio r
        LEFT JOIN usuario_repositorio ur ON r.repositorio_id = ur.repositorio_id
        WHERE (ur.usuario_id = :userId OR r.visibilidad = 'PUBLICO')
        AND r.estado_repositorio = 'ACTIVO'
        ORDER BY r.fecha_creacion DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Repositorio> findOtrosRepositoriosPaginadoOptimizado(
        @Param("userId") Long userId,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
}
```

---

### FASE 9: Compresión y CDN
#### 9.1 GZIP Compression
```java
@Configuration
public class CompressionConfig {
    
    @Bean
    public FilterRegistrationBean<GZIPFilter> gzipFilter() {
        FilterRegistrationBean<GZIPFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new GZIPFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
```

#### 9.2 Static Assets Optimization
```yaml
# application.yml
spring:
  web:
    resources:
      cache:
        cachecontrol:
          max-age: 31536000 # 1 año para assets estáticos
      static-locations:
        - classpath:/static/
      chain:
        enabled: true
        compressed: true
```

**Impacto**: 40-60% reducción en tamaño de transferencia

---

### FASE 10: Frontend Optimizations
#### 10.1 Lazy Loading de Imágenes
```html
<!-- Agregar loading="lazy" a todas las imágenes -->
<img src="/uploads/avatar.jpg" loading="lazy" alt="Avatar">
```

#### 10.2 Skeleton Loaders
```html
<!-- Placeholders mientras carga contenido -->
<div class="repository-card skeleton">
    <div class="skeleton-header"></div>
    <div class="skeleton-body"></div>
</div>
```

```css
.skeleton {
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: loading 1.5s infinite;
}

@keyframes loading {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
}
```

#### 10.3 Virtual Scrolling para Listas Largas
```javascript
// Para listas con >100 items
const virtualScroll = new VirtualScroll({
    container: document.getElementById('repositoryList'),
    itemHeight: 120,
    buffer: 5
});
```

**Impacto**: Percepción de 80% más fluido

---

### FASE 11: Database Connection Pool
```properties
# application.properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

**Análisis**: Monitorear con actuator
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Endpoint**: `http://localhost:8080/actuator/metrics/hikaricp.connections`

---

### FASE 12: Async Processing
#### 12.1 Async Controllers
```java
@GetMapping("/dashboard")
public CompletableFuture<String> showDashboard(Model model) {
    CompletableFuture<Map<String, Object>> statsFuture = 
        dashboardService.getStatsAsync();
    CompletableFuture<List<Equipo>> teamsFuture = 
        dashboardService.getTeamsAsync();
    
    return CompletableFuture.allOf(statsFuture, teamsFuture)
        .thenApply(v -> {
            model.addAttribute("stats", statsFuture.join());
            model.addAttribute("teams", teamsFuture.join());
            return "dashboard";
        });
}
```

#### 12.2 @Async Service Methods
```java
@Service
public class DashboardService {
    
    @Async
    public CompletableFuture<Map<String, Object>> getStatsAsync(Long userId) {
        Map<String, Object> stats = calcularEstadisticas(userId);
        return CompletableFuture.completedFuture(stats);
    }
}
```

---

## 📊 MÉTRICAS DE ÉXITO

### Objetivo Por Vista
| Vista | Actual | Objetivo | Estrategia |
|-------|--------|----------|-----------|
| Dashboard | 3-4s | <1s | Cache + Async + Progressive |
| Repositorios (página 1) | 1s | <0.5s | ✅ Paginación + Cache |
| Proyectos (página 1) | 1s | <0.5s | ✅ Paginación + Cache |
| Sistema de Archivos (raíz) | 4s | <0.5s | Cache Multinivel + Batch |
| Navegación entre carpetas | 2s | <0.3s | Cache + Lazy Stats |
| Búsqueda | 2-3s | <1s | Índices + Full-Text Search |

### Métricas Técnicas
- **Time to First Byte (TTFB)**: <200ms
- **First Contentful Paint (FCP)**: <1s
- **Largest Contentful Paint (LCP)**: <2.5s
- **Total Blocking Time (TBT)**: <300ms
- **Cumulative Layout Shift (CLS)**: <0.1

---

## 🛠️ HERRAMIENTAS DE MONITOREO

### 1. Spring Boot Actuator
```properties
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

**Endpoints Clave**:
- `/actuator/metrics` - Métricas de rendimiento
- `/actuator/health` - Estado de la aplicación
- `/actuator/httptrace` - Trazas de HTTP requests

### 2. Logging Avanzado
```java
@Aspect
@Component
public class PerformanceLoggingAspect {
    
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        
        logger.info("⏱️ {} ejecutado en {}ms", 
            joinPoint.getSignature(), executionTime);
        
        if (executionTime > 1000) {
            logger.warn("⚠️ MÉTODO LENTO: {} tomó {}ms", 
                joinPoint.getSignature(), executionTime);
        }
        
        return result;
    }
}
```

### 3. Database Query Logging
```java
@Component
public class QueryCountInterceptor extends EmptyInterceptor {
    private ThreadLocal<Integer> queryCount = new ThreadLocal<>();
    
    @Override
    public String onPrepareStatement(String sql) {
        queryCount.set(queryCount.get() + 1);
        return super.onPrepareStatement(sql);
    }
    
    public int getQueryCount() {
        return queryCount.get();
    }
}
```

---

## 📈 ROADMAP DE IMPLEMENTACIÓN

### Semana 1: Sistema de Archivos (FASE 6)
- **Día 1-2**: Cache multinivel de nodos
- **Día 3-4**: Batch loading de usuarios
- **Día 5**: Lazy loading de estadísticas
- **Resultado Esperado**: 4s → 0.5s

### Semana 2: Dashboard (FASE 7)
- **Día 1-2**: Carga progresiva de widgets
- **Día 3-4**: WebSockets para stats
- **Día 5**: Testing y ajustes
- **Resultado Esperado**: 3s → <1s

### Semana 3: Queries SQL (FASE 8)
- **Día 1**: Habilitar logs SQL + análisis
- **Día 2-3**: Optimizar queries lentas
- **Día 4-5**: Query nativas optimizadas
- **Resultado Esperado**: 20-30% mejora general

### Semana 4: Frontend (FASE 10)
- **Día 1**: Lazy loading de imágenes
- **Día 2-3**: Skeleton loaders
- **Día 4**: Virtual scrolling
- **Día 5**: Testing UX
- **Resultado Esperado**: Percepción 80% más fluida

---

## ✅ CHECKLIST DE IMPLEMENTACIÓN

### Antes de Implementar
- [ ] Backup de base de datos
- [ ] Crear rama de feature (`git checkout -b feature/optimize-filesys`)
- [ ] Habilitar logs SQL para análisis
- [ ] Documentar tiempos actuales (baseline)

### Durante Implementación
- [ ] Escribir tests unitarios para nuevos métodos
- [ ] Validar cache correctamente invalidado en operaciones CRUD
- [ ] Monitorear uso de memoria (cache puede aumentarla)
- [ ] Logs de debugging para verificar hits/misses de cache

### Después de Implementar
- [ ] Medir tiempos nuevos vs baseline
- [ ] Testing en diferentes navegadores
- [ ] Testing con diferentes volúmenes de datos
- [ ] Documentar configuración de cache
- [ ] Code review y merge a develop

---

## 🚨 RIESGOS Y MITIGACIÓN

### Riesgo 1: Cache desactualizada
**Mitigación**: 
- TTL corto (5 minutos) en datos que cambian frecuentemente
- `@CacheEvict` en todos los métodos CRUD
- Health check endpoint para monitorear cache

### Riesgo 2: Aumento uso de memoria
**Mitigación**:
- Limitar tamaño de cache con `caffeine`
- Monitorear con Actuator
- Configurar eviction policies

### Riesgo 3: Complejidad aumentada
**Mitigación**:
- Documentación exhaustiva
- Tests de integración
- Feature flags para rollback rápido

---

## 📚 RECURSOS Y REFERENCIAS

### Documentación
- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [Hibernate Performance Tuning](https://hibernate.org/orm/documentation/)
- [Core Web Vitals](https://web.dev/vitals/)

### Herramientas
- [Lighthouse](https://developers.google.com/web/tools/lighthouse) - Auditoría de rendimiento
- [JMeter](https://jmeter.apache.org/) - Load testing
- [New Relic](https://newrelic.com/) - APM (opcional)

---

## 🎯 PRIORIDADES INMEDIATAS

1. **FASE 6.1**: Cache multinivel de nodos (MÁXIMA PRIORIDAD)
   - Impacto: 4s → 0.5s
   - Esfuerzo: 4 horas
   - ROI: ⭐⭐⭐⭐⭐

2. **FASE 6.2**: Batch loading de usuarios
   - Impacto: 2s → 0.8s
   - Esfuerzo: 3 horas
   - ROI: ⭐⭐⭐⭐

3. **FASE 10.2**: Skeleton loaders
   - Impacto: Percepción de carga instantánea
   - Esfuerzo: 2 horas
   - ROI: ⭐⭐⭐⭐⭐

4. **FASE 8**: Análisis de queries SQL
   - Impacto: Variable (10-30%)
   - Esfuerzo: 4 horas
   - ROI: ⭐⭐⭐⭐

---

**Última actualización**: 4 de noviembre de 2025
**Versión**: 1.0
