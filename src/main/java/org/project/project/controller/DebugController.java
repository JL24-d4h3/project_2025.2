package org.project.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/cgomez-repositories")
    public Map<String, Object> debugCgomezRepositories() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. Encontrar el ID del usuario cgomez
            String userIdQuery = "SELECT usuario_id, username, nombre_usuario, apellido_paterno FROM usuario WHERE username = 'cgomez'";
            List<Map<String, Object>> userInfo = jdbcTemplate.queryForList(userIdQuery);
            result.put("user_info", userInfo);
            
            if (userInfo.isEmpty()) {
                result.put("error", "Usuario cgomez no encontrado");
                return result;
            }
            
            Long userId = ((Number) userInfo.get(0).get("usuario_id")).longValue();
            result.put("user_id", userId);
            
            // 2. Ver TODOS los repositorios donde cgomez tiene acceso
            String allReposQuery = """
                SELECT 
                    u.username,
                    r.repositorio_id,
                    r.nombre_repositorio,
                    r.estado_repositorio,
                    ur.privilegio_usuario_repositorio,
                    COUNT(phr.proyecto_proyecto_id) as proyectos_vinculados
                FROM usuario u
                INNER JOIN usuario_has_repositorio ur ON u.usuario_id = ur.usuario_usuario_id
                INNER JOIN repositorio r ON ur.repositorio_repositorio_id = r.repositorio_id
                LEFT JOIN proyecto_has_repositorio phr ON r.repositorio_id = phr.repositorio_repositorio_id
                WHERE u.username = 'cgomez'
                AND r.estado_repositorio = 'ACTIVO'
                GROUP BY r.repositorio_id, r.nombre_repositorio, ur.privilegio_usuario_repositorio
                ORDER BY r.nombre_repositorio
                """;
            List<Map<String, Object>> allRepos = jdbcTemplate.queryForList(allReposQuery);
            result.put("all_repositories", allRepos);
            result.put("total_accessible_repos", allRepos.size());
            
            // 3. REPOSITORIOS PERSONALES (privilegio EDITOR + sin proyectos)
            String personalReposQuery = """
                SELECT 
                    COUNT(*) as repositorios_personales
                FROM usuario u
                INNER JOIN usuario_has_repositorio ur ON u.usuario_id = ur.usuario_usuario_id
                INNER JOIN repositorio r ON ur.repositorio_repositorio_id = r.repositorio_id
                WHERE u.username = 'cgomez'
                AND ur.privilegio_usuario_repositorio = 'EDITOR'
                AND r.estado_repositorio = 'ACTIVO'
                AND NOT EXISTS (
                    SELECT 1 FROM proyecto_has_repositorio phr 
                    WHERE phr.repositorio_repositorio_id = r.repositorio_id
                )
                """;
            Integer personalCount = jdbcTemplate.queryForObject(personalReposQuery, Integer.class);
            result.put("personal_repositories", personalCount);
            
            // 4. REPOSITORIOS VINCULADOS A PROYECTOS (donde el usuario participa)
            String linkedReposQuery = """
                SELECT 
                    COUNT(DISTINCT r.repositorio_id) as repositorios_vinculados_proyectos
                FROM usuario u
                INNER JOIN usuario_has_repositorio ur ON u.usuario_id = ur.usuario_usuario_id
                INNER JOIN repositorio r ON ur.repositorio_repositorio_id = r.repositorio_id
                INNER JOIN proyecto_has_repositorio phr ON r.repositorio_id = phr.repositorio_repositorio_id
                INNER JOIN usuario_has_proyecto up ON phr.proyecto_proyecto_id = up.proyecto_proyecto_id
                WHERE u.username = 'cgomez'
                AND up.usuario_usuario_id = u.usuario_id
                AND r.estado_repositorio = 'ACTIVO'
                """;
            Integer linkedCount = jdbcTemplate.queryForObject(linkedReposQuery, Integer.class);
            result.put("linked_to_projects", linkedCount);
            
            // 5. REPOSITORIOS COLABORATIVOS (privilegio != EDITOR)
            String collaborativeReposQuery = """
                SELECT 
                    COUNT(*) as repositorios_colaborativos
                FROM usuario u
                INNER JOIN usuario_has_repositorio ur ON u.usuario_id = ur.usuario_usuario_id
                INNER JOIN repositorio r ON ur.repositorio_repositorio_id = r.repositorio_id
                WHERE u.username = 'cgomez'
                AND ur.privilegio_usuario_repositorio != 'EDITOR'
                AND r.estado_repositorio = 'ACTIVO'
                """;
            Integer collaborativeCount = jdbcTemplate.queryForObject(collaborativeReposQuery, Integer.class);
            result.put("collaborative_repositories", collaborativeCount);
            
            // 6. OTROS REPOSITORIOS PÚBLICOS (no tiene acceso)
            String otherPublicReposQuery = """
                SELECT 
                    COUNT(*) as otros_repositorios_publicos
                FROM repositorio r
                WHERE r.visibilidad_repositorio = 'PUBLICO'
                AND r.estado_repositorio = 'ACTIVO'
                AND NOT EXISTS (
                    SELECT 1 FROM usuario_has_repositorio ur 
                    INNER JOIN usuario u ON ur.usuario_usuario_id = u.usuario_id
                    WHERE ur.repositorio_repositorio_id = r.repositorio_id 
                    AND u.username = 'cgomez'
                )
                """;
            Integer otherPublicCount = jdbcTemplate.queryForObject(otherPublicReposQuery, Integer.class);
            result.put("other_public_repositories", otherPublicCount);
            
            // 7. TOTAL REPOSITORIOS EN EL SISTEMA
            String totalReposQuery = "SELECT COUNT(*) as total_repositorios_sistema FROM repositorio r WHERE r.estado_repositorio = 'ACTIVO'";
            Integer totalCount = jdbcTemplate.queryForObject(totalReposQuery, Integer.class);
            result.put("total_repositories_system", totalCount);
            
            // 8. Ver los proyectos donde cgomez participa
            String projectsQuery = """
                SELECT 
                    p.proyecto_id,
                    p.nombre_proyecto,
                    up.privilegio_usuario_proyecto,
                    COUNT(phr.repositorio_repositorio_id) as repositorios_en_proyecto
                FROM usuario u
                INNER JOIN usuario_has_proyecto up ON u.usuario_id = up.usuario_usuario_id
                INNER JOIN proyecto p ON up.proyecto_proyecto_id = p.proyecto_id
                LEFT JOIN proyecto_has_repositorio phr ON p.proyecto_id = phr.proyecto_proyecto_id
                WHERE u.username = 'cgomez'
                GROUP BY p.proyecto_id, p.nombre_proyecto, up.privilegio_usuario_proyecto
                ORDER BY p.nombre_proyecto
                """;
            List<Map<String, Object>> projects = jdbcTemplate.queryForList(projectsQuery);
            result.put("user_projects", projects);
            
            // Verificación matemática
            int totalAccessible = personalCount + linkedCount + collaborativeCount;
            result.put("calculation_check", Map.of(
                "personal", personalCount,
                "linked", linkedCount,
                "collaborative", collaborativeCount,
                "sum_of_accessible", totalAccessible,
                "should_equal_all_repos", allRepos.size(),
                "matches", totalAccessible == allRepos.size()
            ));
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
}