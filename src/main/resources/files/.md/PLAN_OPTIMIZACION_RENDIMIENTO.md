# 🚀 PLAN DE OPTIMIZACIÓN DE RENDIMIENTO - DevPortal

**Fecha**: 4 de noviembre de 2025  
**Prioridad**: CRÍTICA  
**Impacto estimado**: Reducción del 70-90% en tiempos de carga  

---

## 📊 DIAGNÓSTICO ACTUAL

### Problemas Identificados:

#### 🔥 **CRÍTICOS** (Impacto: 70-80% de la lentitud)

1. **Problema N+1 en verificación de permisos**
   - **Ubicación**: `RepositoryService.obtenerPrivilegioUsuarioActual()` (línea 659)
   - **Síntoma**: Por cada repositorio listado ejecuta 2 queries adicionales:
     - Query 1: `usuarioRepository.findByUsername()`
     - Query 2: `usuarioHasRepositorioRepository.findById_UserIdAndId_RepositoryId()`
   - **Ejemplo real**: 
     - Listado de 20 repositorios = 1 query inicial + (20 × 2) = **41 queries**
     - Cada query ~50ms = **2+ segundos SOLO en permisos**
   - **Solución**: Caché de permisos por sesión

2. **Ausencia total de caché**
   - No hay `@Cacheable` en ningún método
   - Usuario autenticado se consulta repetidamente
   - Permisos se recalculan en cada request
   - Jerarquías de carpetas se consultan múltiples veces
   - **Impacto**: ~60% del tiempo se gasta en queries repetidas

3. **Falta de índices en columnas críticas**
   - Tabla `nodo`: NO tiene índice en `parent_id` (relación jerárquica)
   - Tabla `nodo`: NO tiene índice compuesto en `(container_type, container_id)`
   - Tabla `usuario_has_repositorio`: Índice compuesto subóptimo
   - **Impacto**: Queries de jerarquías y permisos son lentas (>100ms)

#### ⚠️ **IMPORTANTES** (Impacto: 15-20%)

4. **Sin paginación en listados**
   - Todos los repositorios se cargan de una vez
   - Todos los archivos de una carpeta se cargan de una vez
   - **Impacto**: Con 100+ elementos, la carga inicial es muy lenta

5. **Queries sin optimización**
   - No se usa `JOIN FETCH` para evitar N+1
   - No hay proyections para listados (se cargan entidades completas)
   - `obtenerHijos()` en NodoService no usa índices eficientemente

6. **Frontend sin optimización**
   - No hay lazy loading de imágenes
   - No hay debounce en búsquedas
   - Assets no están minimizados
   - Sin cache de respuestas HTTP

#### 📉 **MODERADOS** (Impacto: 5-10%)

7. **Connection pool pequeño**
   - HikariCP usa valores por defecto
   - Pool podría agotarse con múltiples usuarios
   
8. **Sin procesamiento asíncrono**
   - Emails se envían síncronamente
   - Uploads bloquean el request

---

## 🎯 PLAN DE IMPLEMENTACIÓN (10 FASES)

### 🔥 **FASE 1: IMPLEMENTAR CACHÉ DE SPRING** (Impacto: 60-70%)
**Prioridad**: CRÍTICA  
**Tiempo estimado**: 2-3 horas  
**Reducción de latencia**: 1.5-2.5 segundos

#### Acciones:

1. **Agregar dependencias** (`pom.xml`):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

2. **Configurar caché** (nueva clase `CacheConfig.java`):
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "usuarios",
            "permisos",
            "jerarquiasNodos",
            "repositorios"
        );
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }
    
    Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats();
    }
}
```

3. **Aplicar caché en UserService**:
```java
@Cacheable(value = "usuarios", key = "#username")
public Usuario findByUsername(String username) {
    return usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
}

@Cacheable(value = "usuarios", key = "#userId")
public Usuario findById(Long userId) {
    return usuarioRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
}
```

4. **Aplicar caché en permisos** (ver FASE 2)

**Validación**:
- Verificar hits/misses con logs
- Medir tiempo de respuesta antes/después

---

### 🔥 **FASE 2: OPTIMIZAR VALIDACIÓN DE PERMISOS** (Impacto: 40-50%)
**Prioridad**: CRÍTICA  
**Tiempo estimado**: 3-4 horas  
**Reducción de latencia**: 1-2 segundos

#### Problema actual:
```java
// ❌ MAL: Se ejecuta en CADA conversión de repositorio
private String obtenerPrivilegioUsuarioActual(Repositorio repositorio) {
    String currentUsername = authentication.getName();
    Usuario currentUser = usuarioRepository.findByUsername(currentUsername).orElse(null); // Query 1
    Optional<UsuarioHasRepositorio> usuarioHasRepoOpt = usuarioHasRepositorioRepository
        .findById_UserIdAndId_RepositoryId(currentUser.getUsuarioId(), repositorio.getRepositorioId()); // Query 2
    // ...
}
```

#### Solución:

1. **Crear servicio centralizado** (`PermissionService.java`):
```java
@Service
public class PermissionService {
    
    @Autowired
    private UsuarioHasRepositorioRepository usuarioHasRepositorioRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * Obtiene permiso con caché (10 minutos)
     */
    @Cacheable(value = "permisos", key = "#userId + '-' + #repositorioId")
    public String obtenerPermisoRepositorio(Long userId, Long repositorioId, Long creadoPorId) {
        // Verificar si es propietario
        if (creadoPorId != null && creadoPorId.equals(userId)) {
            return "PROPIETARIO";
        }
        
        // Buscar en tabla de permisos
        return usuarioHasRepositorioRepository
            .findById_UserIdAndId_RepositoryId(userId, repositorioId)
            .map(rel -> rel.getPrivilegio().toString())
            .orElse("SIN_ACCESO");
    }
    
    /**
     * Obtiene permisos en batch (evita N+1)
     */
    @Cacheable(value = "permisos", key = "#userId + '-batch-' + #repositorioIds.hashCode()")
    public Map<Long, String> obtenerPermisosRepositorios(Long userId, List<Long> repositorioIds) {
        List<UsuarioHasRepositorio> relaciones = usuarioHasRepositorioRepository
            .findByUsuarioIdAndRepositorioIdIn(userId, repositorioIds);
        
        return relaciones.stream()
            .collect(Collectors.toMap(
                rel -> rel.getId().getRepositoryId(),
                rel -> rel.getPrivilegio().toString()
            ));
    }
    
    /**
     * Invalida caché cuando cambian permisos
     */
    @CacheEvict(value = "permisos", allEntries = true)
    public void invalidarCachePermisos() {
        // Se llama después de actualizar permisos
    }
}
```

2. **Modificar RepositoryService**:
```java
@Autowired
private PermissionService permissionService;

// ✅ BIEN: Batch query para múltiples repos
public List<Map<String, Object>> listarRepositoriosUsuario(Long userId) {
    List<Repositorio> repositorios = repositorioRepository.findByUsuarioId(userId);
    
    // Obtener permisos en UNA SOLA QUERY
    List<Long> repoIds = repositorios.stream()
        .map(Repositorio::getRepositorioId)
        .collect(Collectors.toList());
    
    Map<Long, String> permisos = permissionService.obtenerPermisosRepositorios(userId, repoIds);
    
    // Convertir sin queries adicionales
    return repositorios.stream()
        .map(repo -> {
            Map<String, Object> map = convertirRepositorioAMapaSinPermisos(repo);
            map.put("privilegio_usuario_actual", 
                permisos.getOrDefault(repo.getRepositorioId(), "SIN_ACCESO"));
            return map;
        })
        .collect(Collectors.toList());
}
```

**Validación**:
- Listado de 20 repos: de 41 queries a 2 queries
- Tiempo: de ~2s a ~200ms

---

### 🔥 **FASE 3: CREAR ÍNDICES EN BASE DE DATOS** (Impacto: 50-90%)
**Prioridad**: CRÍTICA  
**Tiempo estimado**: 1 hora  
**Reducción de latencia**: 0.5-1.5 segundos

#### Script SQL (`crear_indices_rendimiento.sql`):

```sql
-- ============================================
-- ÍNDICES PARA OPTIMIZACIÓN DE RENDIMIENTO
-- ============================================

USE dev_portal_sql;

-- 1. TABLA NODO: Índices para jerarquías
-- Mejorar queries de obtenerHijos()
CREATE INDEX idx_nodo_parent_deleted 
ON nodo(parent_id, is_deleted, tipo, nombre);

-- Mejorar queries por contenedor
CREATE INDEX idx_nodo_container 
ON nodo(container_type, container_id, is_deleted, parent_id);

-- Mejorar búsquedas por path
CREATE INDEX idx_nodo_path 
ON nodo(path);

-- 2. TABLA USUARIO: Índice único en username
-- Ya debería existir, pero verificamos
CREATE UNIQUE INDEX idx_usuario_username 
ON usuario(username) 
IF NOT EXISTS;

-- 3. TABLA USUARIO_HAS_REPOSITORIO: Índice compuesto
-- Mejorar queries de permisos
CREATE INDEX idx_usuario_repo_permiso 
ON usuario_has_repositorio(usuario_id, repositorio_id, privilegio_usuario_repositorio);

-- 4. TABLA USUARIO_HAS_PROYECTO: Índice compuesto
CREATE INDEX idx_usuario_proyecto_permiso 
ON usuario_has_proyecto(usuario_id, proyecto_id, privilegio_usuario_proyecto);

-- 5. TABLA REPOSITORIO: Índice en creador
CREATE INDEX idx_repositorio_creador 
ON repositorio(creado_por_usuario_id);

-- 6. TABLA PROYECTO: Índice en creador
CREATE INDEX idx_proyecto_creador 
ON proyecto(created_by);

-- Verificar índices creados
SHOW INDEX FROM nodo;
SHOW INDEX FROM usuario_has_repositorio;
SHOW INDEX FROM usuario;
```

**Validación**:
```sql
-- Antes: ~150ms
EXPLAIN SELECT * FROM nodo 
WHERE parent_id = 85 AND is_deleted = 0 
ORDER BY tipo DESC, nombre ASC;

-- Después: ~5ms (usando índice idx_nodo_parent_deleted)
```

---

### ⚠️ **FASE 4: OPTIMIZAR QUERIES CON JOIN FETCH** (Impacto: 20-30%)
**Prioridad**: IMPORTANTE  
**Tiempo estimado**: 2-3 horas

#### Problema actual:
```java
// ❌ MAL: Lazy loading en loop (N+1)
List<Nodo> nodos = nodoRepository.findByParentId(parentId);
for (Nodo nodo : nodos) {
    System.out.println(nodo.getCreadoPor().getUsername()); // Query por cada nodo!
}
```

#### Soluciones:

1. **Agregar @EntityGraph en repositories**:

```java
public interface NodoRepository extends JpaRepository<Nodo, Long> {
    
    // ✅ BIEN: Cargar usuario en la misma query
    @EntityGraph(attributePaths = {"creadoPor", "actualizadoPor"})
    List<Nodo> findByParentIdAndIsDeletedFalse(Long parentId);
    
    // Alternativa con @Query y JOIN FETCH
    @Query("SELECT n FROM Nodo n " +
           "LEFT JOIN FETCH n.creadoPor " +
           "WHERE n.parentId = :parentId AND n.isDeleted = false " +
           "ORDER BY n.tipo DESC, n.nombre ASC")
    List<Nodo> findHijosConUsuario(@Param("parentId") Long parentId);
}
```

2. **Crear projections para listados**:

```java
// Interface projection (solo campos necesarios)
public interface NodoListProjection {
    Long getNodoId();
    String getNombre();
    String getTipo();
    Long getSize();
    LocalDateTime getCreadoEn();
    
    // No cargar entidades relacionadas
}

// En repository
List<NodoListProjection> findByParentIdAndIsDeletedFalse(Long parentId);
```

**Impacto**:
- Listado de 50 archivos: de 51 queries a 1 query
- Tiempo: de ~500ms a ~50ms

---

### ⚠️ **FASE 5: IMPLEMENTAR PAGINACIÓN** (Impacto: 15-25%)
**Prioridad**: IMPORTANTE  
**Tiempo estimado**: 2 horas

#### Modificar endpoints:

```java
@GetMapping("/{repoId}/files/{nodoId}")
public ResponseEntity<?> getFiles(
        @PathVariable Long repoId,
        @PathVariable Long nodoId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        Principal principal) {
    
    Pageable pageable = PageRequest.of(page, size, 
        Sort.by(Sort.Direction.DESC, "tipo")
            .and(Sort.by(Sort.Direction.ASC, "nombre")));
    
    Page<Nodo> nodosPage = nodoService.obtenerHijosPaginados(nodoId, pageable);
    
    return ResponseEntity.ok(Map.of(
        "files", nodosPage.getContent(),
        "totalElements", nodosPage.getTotalElements(),
        "totalPages", nodosPage.getTotalPages(),
        "currentPage", page
    ));
}
```

---

### ⚠️ **FASE 6: OPTIMIZAR FRONTEND** (Impacto: 10-20%)
**Prioridad**: IMPORTANTE  
**Tiempo estimado**: 3-4 horas

#### Optimizaciones:

1. **Lazy loading de imágenes**:
```html
<img src="/img/placeholder.png" 
     data-src="/uploads/profile.jpg" 
     loading="lazy" 
     class="lazy">
```

2. **Debounce en búsquedas**:
```javascript
let searchTimeout;
function onSearchInput(value) {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        performSearch(value);
    }, 300); // Espera 300ms antes de buscar
}
```

3. **Cache en localStorage**:
```javascript
function loadRepositories() {
    const cached = localStorage.getItem('repositories');
    if (cached && !isExpired(cached)) {
        renderRepositories(JSON.parse(cached));
    }
    
    fetch('/api/repositories').then(data => {
        localStorage.setItem('repositories', JSON.stringify({
            data: data,
            timestamp: Date.now()
        }));
        renderRepositories(data);
    });
}
```

---

### 📊 **FASE 7: HABILITAR LOGS DE SQL** (Diagnóstico)
**Prioridad**: DIAGNÓSTICO  
**Tiempo estimado**: 30 minutos

#### application.properties:
```properties
# Mostrar SQL queries
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Estadísticas de Hibernate
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=DEBUG
```

**Uso**: Identificar queries N+1 que no hayamos detectado

---

### 🚀 **FASE 8: CONFIGURAR CONNECTION POOL** (Optimización)
**Prioridad**: OPTIMIZACIÓN  
**Tiempo estimado**: 30 minutos

#### application.properties:
```properties
# HikariCP Configuration
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=60000
```

---

### 🚀 **FASE 9: IMPLEMENTAR ASYNC PROCESSING** (Avanzado)
**Prioridad**: AVANZADO  
**Tiempo estimado**: 2-3 horas

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class EmailService {
    
    @Async
    public CompletableFuture<Void> enviarEmailAsync(String to, String subject, String body) {
        // No bloquea el request principal
        enviarEmail(to, subject, body);
        return CompletableFuture.completedFuture(null);
    }
}
```

---

### ✅ **FASE 10: MEDIR Y VALIDAR MEJORAS** (Verificación)
**Prioridad**: VERIFICACIÓN  
**Tiempo estimado**: 1 hora

#### Agregar métricas:

1. **Spring Boot Actuator**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

2. **Medir endpoints**:
```java
@RestController
@Timed // Métricas automáticas
public class RepositoryFilesRestController {
    // ...
}
```

3. **Verificar en**: `http://localhost:8080/actuator/metrics`

---

## 📈 IMPACTO ESPERADO

### Antes de optimizaciones:
- **Listado de 20 repositorios**: ~3-4 segundos
- **Navegación en carpetas**: ~1-2 segundos
- **Carga de dashboard**: ~5-7 segundos
- **Queries por request**: 30-50 queries

### Después de FASES 1-3 (Críticas):
- **Listado de 20 repositorios**: ~300-500ms ✅ **85% más rápido**
- **Navegación en carpetas**: ~100-200ms ✅ **90% más rápido**
- **Carga de dashboard**: ~800ms-1.5s ✅ **80% más rápido**
- **Queries por request**: 2-5 queries ✅ **90% reducción**

### Después de TODAS las fases:
- **Tiempos de respuesta**: <500ms en promedio ✅ **95% más rápido**
- **Capacidad de usuarios**: 10x más usuarios concurrentes
- **Consumo de BD**: 80% menos queries

---

## 🎯 ORDEN DE EJECUCIÓN RECOMENDADO

### Sprint 1 (Día 1-2): CRÍTICAS
1. ✅ FASE 3: Crear índices (1 hora) - **HACER PRIMERO**
2. ✅ FASE 1: Implementar caché (2-3 horas)
3. ✅ FASE 2: Optimizar permisos (3-4 horas)

**Resultado esperado**: 70-80% de mejora

### Sprint 2 (Día 3-4): IMPORTANTES
4. ✅ FASE 7: Habilitar logs SQL (30 min) - Para validar
5. ✅ FASE 4: Optimizar queries (2-3 horas)
6. ✅ FASE 5: Implementar paginación (2 horas)

**Resultado esperado**: 85-90% de mejora

### Sprint 3 (Día 5): OPTIMIZACIONES
7. ✅ FASE 6: Optimizar frontend (3-4 horas)
8. ✅ FASE 8: Configurar pool (30 min)
9. ✅ FASE 10: Medir mejoras (1 hora)

**Resultado esperado**: 90-95% de mejora

### Sprint 4 (Opcional): AVANZADO
10. ✅ FASE 9: Async processing (2-3 horas)

---

## 📝 NOTAS IMPORTANTES

1. **Hacer backup de BD antes de crear índices**
2. **Probar cada fase en desarrollo antes de producción**
3. **Monitorear métricas de caché (hit rate debe ser >70%)**
4. **Validar que índices se usan con EXPLAIN**
5. **Documentar cambios en cada fase**

---

## 🔧 COMANDOS ÚTILES

### Verificar caché:
```bash
# Caffeine stats
http://localhost:8080/actuator/caches
```

### Verificar queries:
```bash
# Habilitar logs y buscar "select" en consola
tail -f logs/application.log | grep "select"
```

### Medir tiempos:
```bash
# Con curl
time curl http://localhost:8080/api/repositories

# Con Chrome DevTools: Network tab
```

---

**¿Listo para empezar con la FASE 1 (Caché)?**
