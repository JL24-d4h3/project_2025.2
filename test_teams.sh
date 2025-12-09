#!/bin/bash

# ============================================================================
# SCRIPT RÁPIDO PARA TESTING DEL SISTEMA DE EQUIPOS
# ============================================================================
# Este script ejecuta automáticamente todas las queries importantes
# 
# Uso: 
#   1. Modifica el USUARIO_ID en la línea 11
#   2. Ejecuta: bash test_teams.sh
# ============================================================================

USUARIO_ID=1  # 👈 CAMBIAR ESTO por el usuario_id que desees probar
DB_USER="root"
DB_PASSWORD="root"
DB_NAME="dev_portal"
DB_HOST="localhost"

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║         TESTING DEL SISTEMA DE EQUIPOS - USER ID: $USUARIO_ID          ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
echo ""

# Función auxiliar para ejecutar queries
run_query() {
    local title="$1"
    local query="$2"
    
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📊 $title"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    mysql -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "$query"
    echo ""
}

# Test 1: Verificar que el usuario existe
run_query "1️⃣  VERIFICAR USUARIO" "
SELECT usuario_id, nombre_usuario, username, correo 
FROM usuario 
WHERE usuario_id = $USUARIO_ID;
"

# Test 2: Proyectos Grupales
run_query "2️⃣  PROYECTOS GRUPALES" "
SELECT DISTINCT 
    p.proyecto_id,
    p.nombre_proyecto,
    p.propietario_proyecto,
    uhp.privilegio_usuario_proyecto
FROM proyecto p
INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
WHERE uhp.usuario_usuario_id = $USUARIO_ID
  AND p.propietario_proyecto = 'GRUPO'
ORDER BY p.nombre_proyecto;
"

# Test 3: Proyectos Empresariales
run_query "3️⃣  PROYECTOS EMPRESARIALES" "
SELECT DISTINCT 
    p.proyecto_id,
    p.nombre_proyecto,
    p.propietario_proyecto,
    uhp.privilegio_usuario_proyecto
FROM proyecto p
INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id
WHERE uhp.usuario_usuario_id = $USUARIO_ID
  AND p.propietario_proyecto = 'EMPRESA'
ORDER BY p.nombre_proyecto;
"

# Test 4: Repositorios Colaborativos
run_query "4️⃣  REPOSITORIOS COLABORATIVOS" "
SELECT DISTINCT 
    r.repositorio_id,
    r.nombre_repositorio,
    r.tipo_repositorio,
    uhr.privilegio_usuario_repositorio
FROM repositorio r
INNER JOIN usuario_has_repositorio uhr ON r.repositorio_id = uhr.repositorio_repositorio_id
WHERE uhr.usuario_usuario_id = $USUARIO_ID
  AND r.tipo_repositorio = 'COLABORATIVO'
ORDER BY r.nombre_repositorio;
"

# Test 5: Totales
run_query "5️⃣  TOTALES" "
SELECT 
    (SELECT COUNT(*) FROM proyecto p INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id WHERE uhp.usuario_usuario_id = $USUARIO_ID AND p.propietario_proyecto = 'GRUPO') as proyectos_grupo,
    (SELECT COUNT(*) FROM proyecto p INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id WHERE uhp.usuario_usuario_id = $USUARIO_ID AND p.propietario_proyecto = 'EMPRESA') as proyectos_empresa,
    (SELECT COUNT(*) FROM repositorio r INNER JOIN usuario_has_repositorio uhr ON r.repositorio_id = uhr.repositorio_repositorio_id WHERE uhr.usuario_usuario_id = $USUARIO_ID AND r.tipo_repositorio = 'COLABORATIVO') as repositorios_colaborativos;
"

# Test 6: Equipos
run_query "6️⃣  EQUIPOS CREADOS" "
SELECT 
    e.equipo_id,
    e.nombre_equipo,
    COUNT(DISTINCT uhe.usuario_usuario_id) as cantidad_miembros
FROM equipo e
LEFT JOIN usuario_has_equipo uhe ON e.equipo_id = uhe.equipo_equipo_id
GROUP BY e.equipo_id, e.nombre_equipo;
"

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║                    ✅ TESTING COMPLETADO                            ║"
echo "╚════════════════════════════════════════════════════════════════════╝"
