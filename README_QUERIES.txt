╔════════════════════════════════════════════════════════════════════════════╗
║                   SISTEMA DE EQUIPOS - QUERIES PARA TESTING                  ║
║                              RESUMEN EJECUTIVO                                ║
╚════════════════════════════════════════════════════════════════════════════╝

📊 QUERIES PRINCIPALES (Copiar y Pegar)
═══════════════════════════════════════════════════════════════════════════════

1️⃣ PROYECTOS GRUPALES DEL USUARIO
   ─────────────────────────────────
   Verifica que SOLO se listan proyectos de tipo GRUPO donde el usuario participa

   SELECT DISTINCT 
       p.proyecto_id, p.nombre_proyecto, p.propietario_proyecto, 
       uhp.privilegio_usuario_proyecto
   FROM proyecto p
   INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
   WHERE uhp.usuario_usuario_id = 1        -- 👈 CAMBIAR 1
     AND p.propietario_proyecto = 'GRUPO'
   ORDER BY p.nombre_proyecto;

   ✓ Resultado esperado: Solo proyectos con propietario_proyecto = 'GRUPO'
   ✓ El usuario debe estar en la tabla usuario_has_proyecto


2️⃣ PROYECTOS EMPRESARIALES DEL USUARIO
   ────────────────────────────────────
   Verifica que SOLO se listan proyectos de tipo EMPRESA donde el usuario participa

   SELECT DISTINCT 
       p.proyecto_id, p.nombre_proyecto, p.propietario_proyecto, 
       uhp.privilegio_usuario_proyecto
   FROM proyecto p
   INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
   WHERE uhp.usuario_usuario_id = 1           -- 👈 CAMBIAR 1
     AND p.propietario_proyecto = 'EMPRESA'
   ORDER BY p.nombre_proyecto;

   ✓ Resultado esperado: Solo proyectos con propietario_proyecto = 'EMPRESA'
   ✓ El usuario debe estar en la tabla usuario_has_proyecto


3️⃣ REPOSITORIOS COLABORATIVOS DEL USUARIO
   ──────────────────────────────────────
   Verifica que SOLO se listan repositorios de tipo COLABORATIVO donde el usuario participa

   SELECT DISTINCT 
       r.repositorio_id, r.nombre_repositorio, r.tipo_repositorio, 
       uhr.privilegio_usuario_repositorio
   FROM repositorio r
   INNER JOIN usuario_has_repositorio uhr ON r.repositorio_id = uhr.repositorio_repositorio_id
   WHERE uhr.usuario_usuario_id = 1             -- 👈 CAMBIAR 1
     AND r.tipo_repositorio = 'COLABORATIVO'
   ORDER BY r.nombre_repositorio;

   ✓ Resultado esperado: Solo repositorios con tipo_repositorio = 'COLABORATIVO'
   ✓ El usuario debe estar en la tabla usuario_has_repositorio


═══════════════════════════════════════════════════════════════════════════════

4️⃣ TEST FINAL INTEGRAL
   ──────────────────
   Esta query simula EXACTAMENTE lo que el TeamService devuelve

   SELECT 
       'PROYECTO_GRUPO' as tipo, p.proyecto_id as id, p.nombre_proyecto as nombre, 
       p.descripcion_proyecto as descripcion, p.propietario_proyecto as propietario
   FROM proyecto p
   INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
   WHERE uhp.usuario_usuario_id = 1 AND p.propietario_proyecto = 'GRUPO'
   UNION ALL
   SELECT 
       'PROYECTO_EMPRESA', p.proyecto_id, p.nombre_proyecto, 
       p.descripcion_proyecto, p.propietario_proyecto
   FROM proyecto p
   INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
   WHERE uhp.usuario_usuario_id = 1 AND p.propietario_proyecto = 'EMPRESA'
   UNION ALL
   SELECT 
       'REPOSITORIO_COLABORATIVO', r.repositorio_id, r.nombre_repositorio, 
       r.descripcion_repositorio, r.tipo_repositorio
   FROM repositorio r
   INNER JOIN usuario_has_repositorio uhr ON r.repositorio_id = uhr.repositorio_repositorio_id
   WHERE uhr.usuario_usuario_id = 1 AND r.tipo_repositorio = 'COLABORATIVO'
   ORDER BY tipo, nombre;

   ✓ Compara el resultado con los dropdowns en la aplicación web
   ✓ Debe coincidir EXACTAMENTE con lo que ve el usuario


═══════════════════════════════════════════════════════════════════════════════

5️⃣ CONTAR TOTALES (Resumen Rápido)
   ──────────────────────────────
   SELECT 
       (SELECT COUNT(*) FROM proyecto p INNER JOIN usuario_has_proyecto uhp 
        ON p.proyecto_id = uhp.proyecto_proyecto_id 
        WHERE uhp.usuario_usuario_id = 1 AND p.propietario_proyecto = 'GRUPO') 
           as proyectos_grupo,
       (SELECT COUNT(*) FROM proyecto p INNER JOIN usuario_has_proyecto uhp 
        ON p.proyecto_id = uhp.proyecto_proyecto_id 
        WHERE uhp.usuario_usuario_id = 1 AND p.propietario_proyecto = 'EMPRESA') 
           as proyectos_empresa,
       (SELECT COUNT(*) FROM repositorio r INNER JOIN usuario_has_repositorio uhr 
        ON r.repositorio_id = uhr.repositorio_repositorio_id 
        WHERE uhr.usuario_usuario_id = 1 AND r.tipo_repositorio = 'COLABORATIVO') 
           as repositorios_colaborativos;

   ✓ Muestra un resumen rápido en una sola fila


═══════════════════════════════════════════════════════════════════════════════

6️⃣ LISTAR TODOS LOS EQUIPOS
   ─────────────────────────
   SELECT 
       e.equipo_id, e.nombre_equipo, 
       COUNT(DISTINCT uhe.usuario_usuario_id) as cantidad_miembros
   FROM equipo e
   LEFT JOIN usuario_has_equipo uhe ON e.equipo_id = uhe.equipo_equipo_id
   GROUP BY e.equipo_id, e.nombre_equipo;

   ✓ Verifica que los equipos se están creando correctamente
   ✓ Muestra cuántos miembros tiene cada equipo


═══════════════════════════════════════════════════════════════════════════════

⚙️ INFORMACIÓN DE CONEXIÓN
  ─────────────────────────
  Base de datos: dev_portal
  Usuario: root
  Contraseña: root
  Host: localhost
  Puerto: 3306


🎯 PASO A PASO PARA TESTING
════════════════════════════════════════════════════════════════════════════════

1. Encontrar un usuario válido:
   SELECT usuario_id, nombre_usuario, username FROM usuario LIMIT 5;
   
   ➜ Toma nota del usuario_id (ej: 1, 2, 3, etc.)

2. Reemplazar en todas las queries:
   Donde dice "WHERE uhp.usuario_usuario_id = 1"
   Cambiar 1 por el usuario_id que encontraste

3. Ejecutar las 3 queries principales (1️⃣, 2️⃣, 3️⃣):
   - Query de Proyectos GRUPO
   - Query de Proyectos EMPRESA
   - Query de Repositorios COLABORATIVO

4. Ejecutar el Test Final Integral (4️⃣):
   Debe devolver exactamente lo que ve en los dropdowns de la aplicación

5. Crear un equipo en la web:
   http://localhost:8080/devportal/USUARIO/username/teams/create-at-P

6. Verificar que el equipo aparece:
   Ejecutar Query 6️⃣ (Listar todos los equipos)


✅ VALIDACIÓN
════════════════════════════════════════════════════════════════════════════════

Después de ejecutar todas las queries, verifica que:

□ Proyectos GRUPO se listan correctamente
□ Proyectos EMPRESA se listan correctamente  
□ Repositorios COLABORATIVO se listan correctamente
□ Test Final devuelve los datos esperados
□ Los números coinciden con lo que ves en la aplicación
□ Se pueden crear equipos sin errores
□ Los equipos creados aparecen en la query de listar equipos


📁 ARCHIVOS DISPONIBLES
════════════════════════════════════════════════════════════════════════════════

1. test_teams_queries.sql
   ➜ Todas las queries en un archivo SQL puro
   ➜ Cópialo completo en MySQL Workbench

2. TESTING_GUIDE.md
   ➜ Guía completa paso a paso
   ➜ Con explicaciones detalladas

3. QUERIES_SUMMARY.md
   ➜ Resumen de todas las queries
   ➜ Sin tanta explicación, solo código

4. test_teams.bat (Windows)
   ➜ Script automático para Windows
   ➜ Ejecuta: test_teams.bat [usuario_id]

5. test_teams.sh (Linux/Mac)
   ➜ Script automático para Linux/Mac
   ➜ Ejecuta: bash test_teams.sh


🚀 EJECUCIÓN RÁPIDA
════════════════════════════════════════════════════════════════════════════════

Windows:
  test_teams.bat 1

Linux/Mac:
  bash test_teams.sh

MySQL Workbench:
  1. Copiar Query 1️⃣, reemplazar 1, ejecutar Ctrl+Enter
  2. Copiar Query 2️⃣, reemplazar 1, ejecutar Ctrl+Enter
  3. Copiar Query 3️⃣, reemplazar 1, ejecutar Ctrl+Enter
  4. Copiar Query 4️⃣, reemplazar 1, ejecutar Ctrl+Enter


💡 TIPS IMPORTANTES
════════════════════════════════════════════════════════════════════════════════

✓ SIEMPRE reemplaza "1" con tu usuario_id real
✓ ENUM values son case-sensitive: 'GRUPO' NO es 'grupo'
✓ Si no devuelve resultados, verifica que el usuario tiene participaciones
✓ Para encontrar usuarios activos: 
  SELECT usuario_id, nombre_usuario FROM usuario WHERE usuario_id IN 
  (SELECT DISTINCT usuario_usuario_id FROM usuario_has_proyecto LIMIT 5)


═══════════════════════════════════════════════════════════════════════════════
¡LISTO PARA TESTING! 🎉
═══════════════════════════════════════════════════════════════════════════════
