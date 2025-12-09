@echo off
REM ============================================================================
REM SCRIPT DE TESTING PARA TEAMS - VERSIÓN WINDOWS
REM ============================================================================
REM Este script ejecuta las queries más importantes para verificar el sistema
REM
REM Requisitos:
REM   - MySQL debe estar instalado y en el PATH
REM   - Base de datos: dev_portal
REM   - Usuario: root
REM   - Contraseña: root (cambiar si es necesario)
REM
REM Uso:
REM   1. Abre una consola CMD
REM   2. Navega a la carpeta del proyecto
REM   3. Ejecuta: test_teams.bat [usuario_id]
REM   4. Si no especificas usuario_id, usará 1 por defecto
REM ============================================================================

SETLOCAL ENABLEDELAYEDEXPANSION

REM Configuración
SET DB_USER=root
SET DB_PASSWORD=root
SET DB_NAME=dev_portal
SET DB_HOST=localhost
SET USUARIO_ID=%1
IF "%USUARIO_ID%"=="" SET USUARIO_ID=1

CLS
ECHO.
ECHO ╔════════════════════════════════════════════════════════════════════╗
ECHO ║         TESTING DEL SISTEMA DE EQUIPOS - USER ID: %USUARIO_ID%          ║
ECHO ╚════════════════════════════════════════════════════════════════════╝
ECHO.
ECHO Conectando a base de datos: %DB_NAME%
ECHO Usuario: %DB_USER%
ECHO Servidor: %DB_HOST%
ECHO.

REM Test 1: Verificar usuario
ECHO ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
ECHO 1. VERIFICAR USUARIO
ECHO ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
mysql -h %DB_HOST% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SELECT usuario_id, nombre_usuario, username, correo FROM usuario WHERE usuario_id = %USUARIO_ID%;"
ECHO.

REM Test 2: Proyectos Grupales
ECHO ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
ECHO 2. PROYECTOS GRUPALES
ECHO ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
mysql -h %DB_HOST% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SELECT DISTINCT p.proyecto_id, p.nombre_proyecto, p.propietario_proyecto, uhp.privilegio_usuario_proyecto FROM proyecto p INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id WHERE uhp.usuario_usuario_id = %USUARIO_ID% AND p.propietario_proyecto = 'GRUPO' ORDER BY p.nombre_proyecto;"
ECHO.

REM Test 3: Proyectos Empresariales
ECHO ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
ECHO 3. PROYECTOS EMPRESARIALES
ECHO ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
mysql -h %DB_HOST% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SELECT DISTINCT p.proyecto_id, p.nombre_proyecto, p.propietario_proyecto, uhp.privilegio_usuario_proyecto FROM proyecto p INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id WHERE uhp.usuario_usuario_id = %USUARIO_ID% AND p.propietario_proyecto = 'EMPRESA' ORDER BY p.nombre_proyecto;"
ECHO.

REM Test 4: Repositorios Colaborativos
ECHO ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
ECHO 4. REPOSITORIOS COLABORATIVOS
ECHO ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
mysql -h %DB_HOST% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SELECT DISTINCT r.repositorio_id, r.nombre_repositorio, r.tipo_repositorio, uhr.privilegio_usuario_repositorio FROM repositorio r INNER JOIN usuario_has_repositorio uhr ON r.repositorio_id = uhr.repositorio_repositorio_id WHERE uhr.usuario_usuario_id = %USUARIO_ID% AND r.tipo_repositorio = 'COLABORATIVO' ORDER BY r.nombre_repositorio;"
ECHO.

REM Test 5: Totales
ECHO ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
ECHO 5. RESUMEN - TOTALES
ECHO ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
mysql -h %DB_HOST% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SELECT (SELECT COUNT(*) FROM proyecto p INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id WHERE uhp.usuario_usuario_id = %USUARIO_ID% AND p.propietario_proyecto = 'GRUPO') as 'Proyectos GRUPO', (SELECT COUNT(*) FROM proyecto p INNER JOIN usuario_has_proyecto uhp ON p.proyecto_id = uhp.proyecto_proyecto_id WHERE uhp.usuario_usuario_id = %USUARIO_ID% AND p.propietario_proyecto = 'EMPRESA') as 'Proyectos EMPRESA', (SELECT COUNT(*) FROM repositorio r INNER JOIN usuario_has_repositorio uhr ON r.repositorio_id = uhr.repositorio_repositorio_id WHERE uhr.usuario_usuario_id = %USUARIO_ID% AND r.tipo_repositorio = 'COLABORATIVO') as 'Repositorios COLABORATIVO';"
ECHO.

REM Test 6: Equipos
ECHO ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
ECHO 6. EQUIPOS CREADOS
ECHO ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
mysql -h %DB_HOST% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SELECT e.equipo_id, e.nombre_equipo, COUNT(DISTINCT uhe.usuario_usuario_id) as cantidad_miembros FROM equipo e LEFT JOIN usuario_has_equipo uhe ON e.equipo_id = uhe.equipo_equipo_id GROUP BY e.equipo_id, e.nombre_equipo;"
ECHO.

ECHO ╔════════════════════════════════════════════════════════════════════╗
ECHO ║                    TESTING COMPLETADO                             ║
ECHO ╚════════════════════════════════════════════════════════════════════╝
ECHO.
PAUSE
