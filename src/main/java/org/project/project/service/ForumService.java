package org.project.project.service;

import org.project.project.model.entity.ForoTema;
import org.project.project.model.entity.ForoRespuesta;
import org.project.project.model.entity.ForoVoto;
import org.project.project.model.entity.Usuario;
import org.project.project.model.entity.Categoria;
import org.project.project.repository.ForoTemaRepository;
import org.project.project.repository.ForoRespuestaRepository;
import org.project.project.repository.ForoVotoRepository;
import org.project.project.repository.UsuarioRepository;
import org.project.project.repository.CategoriaRepository;
import org.project.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;

@Service
@Transactional
public class ForumService {

    @Autowired
    private ForoTemaRepository foroTemaRepository;

    @Autowired
    private ForoRespuestaRepository foroRespuestaRepository;

    @Autowired
    private ForoVotoRepository foroVotoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // =================== MÉTODOS DE TEMAS ===================

    /**
     * Listar todos los temas del foro
     */
    @Transactional(readOnly = true)
    public List<ForoTema> listarTemas() {
        return foroTemaRepository.findAll();
    }

    /**
     * Listar temas con paginación
     */
    @Transactional(readOnly = true)
    public Page<ForoTema> listarTemasPaginados(Pageable pageable) {
        return foroTemaRepository.findAllTopicsOrdered(pageable);
    }

    /**
     * Listar temas abiertos ordenados por actividad
     */
    @Transactional(readOnly = true)
    public Page<ForoTema> listarTemasAbiertos(Pageable pageable) {
        return foroTemaRepository.findOpenTopicsOrderedByActivity(pageable);
    }

    /**
     * Buscar tema por ID
     */
    @Transactional(readOnly = true)
    public ForoTema buscarTemaPorId(Long id) {
        return foroTemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema del foro no encontrado con id: " + id));
    }

    /**
     * Buscar tema por slug
     */
    @Transactional(readOnly = true)
    public ForoTema buscarTemaPorSlug(String slug) {
        return foroTemaRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tema del foro no encontrado con slug: " + slug));
    }

    /**
     * Crear nuevo tema
     */
    public ForoTema crearTema(ForoTema tema, Long autorId, List<Long> categoriaIds) {
        // Asignar autor
        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + autorId));
        tema.setAutor(autor);

        // Configurar valores por defecto
        if (tema.getFechaCreacion() == null) {
            tema.setFechaCreacion(LocalDateTime.now());
        }
        if (tema.getEstadoTema() == null) {
            tema.setEstadoTema(ForoTema.EstadoTema.ABIERTO);
        }
        if (tema.getEsAnclado() == null) {
            tema.setEsAnclado(false);
        }
        if (tema.getEsBloqueado() == null) {
            tema.setEsBloqueado(false);
        }
        if (tema.getVistas() == null) {
            tema.setVistas(0);
        }
        if (tema.getRespuestasCount() == null) {
            tema.setRespuestasCount(0);
        }

        // Generar slug si no existe
        if (tema.getSlug() == null || tema.getSlug().isEmpty()) {
            tema.setSlug(generarSlug(tema.getTituloTema()));
        }

        // Asignar categorías si se proporcionaron
        if (categoriaIds != null && !categoriaIds.isEmpty()) {
            Set<Categoria> categorias = new HashSet<>(categoriaRepository.findAllById(categoriaIds));
            tema.setCategorias(categorias);
        }

        return foroTemaRepository.save(tema);
    }

    /**
     * Actualizar tema
     */
    public ForoTema actualizarTema(Long id, ForoTema temaDetalles) {
        ForoTema tema = buscarTemaPorId(id);

        tema.setTituloTema(temaDetalles.getTituloTema());
        tema.setContenidoTema(temaDetalles.getContenidoTema());

        if (temaDetalles.getEstadoTema() != null) {
            tema.setEstadoTema(temaDetalles.getEstadoTema());
        }

        return foroTemaRepository.save(tema);
    }

    /**
     * Eliminar tema
     */
    public void eliminarTema(Long id) {
        ForoTema tema = buscarTemaPorId(id);
        foroTemaRepository.delete(tema);
    }

    /**
     * Anclar/Desanclar tema
     */
    public ForoTema alternarAnclado(Long id) {
        ForoTema tema = buscarTemaPorId(id);
        tema.setEsAnclado(!tema.getEsAnclado());
        return foroTemaRepository.save(tema);
    }

    /**
     * Bloquear/Desbloquear tema
     */
    public ForoTema alternarBloqueado(Long id) {
        ForoTema tema = buscarTemaPorId(id);
        tema.setEsBloqueado(!tema.getEsBloqueado());
        return foroTemaRepository.save(tema);
    }

    /**
     * Incrementar vistas del tema
     */
    public void incrementarVistas(Long temaId) {
        foroTemaRepository.incrementViewCount(temaId);
    }

    /**
     * Buscar temas por categoría
     */
    @Transactional(readOnly = true)
    public Page<ForoTema> buscarTemasPorCategoria(Long categoriaId, Pageable pageable) {
        return foroTemaRepository.findByCategoriaId(categoriaId, pageable);
    }

    /**
     * Listar temas anclados
     */
    @Transactional(readOnly = true)
    public List<ForoTema> listarTemasAnclados() {
        return foroTemaRepository.findPinnedTopics();
    }

    // =================== MÉTODOS DE RESPUESTAS ===================

    /**
     * Listar respuestas de un tema
     */
    @Transactional(readOnly = true)
    public List<ForoRespuesta> listarRespuestasDeTema(Long temaId) {
        return foroRespuestaRepository.findByTema_TemaId(temaId);
    }

    /**
     * Listar respuestas raíz de un tema (sin padre)
     */
    @Transactional(readOnly = true)
    public List<ForoRespuesta> listarRespuestasRaiz(Long temaId) {
        return foroRespuestaRepository.findRootAnswersByTemaId(temaId);
    }

    /**
     * Listar respuestas hijas de una respuesta
     */
    @Transactional(readOnly = true)
    public List<ForoRespuesta> listarRespuestasHijas(Long parentId) {
        return foroRespuestaRepository.findRepliesByParentId(parentId);
    }

    /**
     * Buscar respuesta por ID
     */
    @Transactional(readOnly = true)
    public ForoRespuesta buscarRespuestaPorId(Long id) {
        return foroRespuestaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Respuesta no encontrada con id: " + id));
    }

    /**
     * Crear respuesta
     */
    public ForoRespuesta crearRespuesta(ForoRespuesta respuesta, Long temaId, Long autorId, Long parentRespuestaId) {
        // Asignar tema
        ForoTema tema = buscarTemaPorId(temaId);
        respuesta.setTema(tema);

        // Asignar autor
        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + autorId));
        respuesta.setAutor(autor);

        // Asignar respuesta padre si existe
        if (parentRespuestaId != null) {
            ForoRespuesta parent = buscarRespuestaPorId(parentRespuestaId);
            respuesta.setParentRespuesta(parent);
        }

        // Configurar valores por defecto
        if (respuesta.getFechaCreacion() == null) {
            respuesta.setFechaCreacion(LocalDateTime.now());
        }
        if (respuesta.getEsSolucion() == null) {
            respuesta.setEsSolucion(false);
        }
        if (respuesta.getVotosPositivos() == null) {
            respuesta.setVotosPositivos(0);
        }
        if (respuesta.getVotosNegativos() == null) {
            respuesta.setVotosNegativos(0);
        }
        if (respuesta.getPuntuacionTotal() == null) {
            respuesta.setPuntuacionTotal(0);
        }

        ForoRespuesta respuestaGuardada = foroRespuestaRepository.save(respuesta);

        // Actualizar contador de respuestas del tema y fecha de última respuesta (requiere usuarioId)
        foroTemaRepository.incrementAnswerCount(temaId, autorId);
        tema.setUltimaRespuestaFecha(LocalDateTime.now());
        foroTemaRepository.save(tema);

        return respuestaGuardada;
    }

    /**
     * Actualizar respuesta
     */
    public ForoRespuesta actualizarRespuesta(Long id, ForoRespuesta respuestaDetalles) {
        ForoRespuesta respuesta = buscarRespuestaPorId(id);
        respuesta.setContenidoRespuesta(respuestaDetalles.getContenidoRespuesta());
        return foroRespuestaRepository.save(respuesta);
    }

    /**
     * Eliminar respuesta
     */
    public void eliminarRespuesta(Long id) {
        ForoRespuesta respuesta = buscarRespuestaPorId(id);
        Long temaId = respuesta.getTema().getTemaId();

        foroRespuestaRepository.delete(respuesta);

        // Decrementar contador de respuestas del tema
        foroTemaRepository.decrementAnswerCount(temaId);
    }

    /**
     * Marcar respuesta como solución
     */
    public ForoRespuesta marcarComoSolucion(Long respuestaId) {
        ForoRespuesta respuesta = buscarRespuestaPorId(respuestaId);
        Long temaId = respuesta.getTema().getTemaId();

        // Desmarcar otras soluciones del mismo tema
        List<ForoRespuesta> solucionesActuales = foroRespuestaRepository.findSolutionByTemaId(temaId);
        for (ForoRespuesta sol : solucionesActuales) {
            sol.setEsSolucion(false);
            foroRespuestaRepository.save(sol);
        }

        // Marcar esta como solución
        respuesta.setEsSolucion(true);
        ForoRespuesta respuestaActualizada = foroRespuestaRepository.save(respuesta);

        // Actualizar estado del tema a RESUELTO
        ForoTema tema = respuesta.getTema();
        tema.setEstadoTema(ForoTema.EstadoTema.RESUELTO);
        foroTemaRepository.save(tema);

        return respuestaActualizada;
    }

    /**
     * Votar respuesta
     */
    public ForoRespuesta votarRespuesta(Long respuestaId, Long usuarioId, boolean esPositivo) {
        ForoRespuesta respuesta = buscarRespuestaPorId(respuestaId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        ForoVoto.TipoVoto tipoVoto = esPositivo ? ForoVoto.TipoVoto.POSITIVO : ForoVoto.TipoVoto.NEGATIVO;

        // Verificar si ya votó
        Optional<ForoVoto> votoExistenteOpt = foroVotoRepository.findByUsuario_UsuarioIdAndRespuesta_RespuestaId(usuarioId, respuestaId);

        if (votoExistenteOpt.isPresent()) {
            ForoVoto votoExistente = votoExistenteOpt.get();

            // Si el voto es del mismo tipo, eliminarlo
            if (votoExistente.getTipoVoto() == tipoVoto) {
                foroVotoRepository.delete(votoExistente);
                if (esPositivo) {
                    respuesta.setVotosPositivos(respuesta.getVotosPositivos() - 1);
                } else {
                    respuesta.setVotosNegativos(respuesta.getVotosNegativos() - 1);
                }
            } else {
                // Cambiar tipo de voto
                votoExistente.setTipoVoto(tipoVoto);
                foroVotoRepository.save(votoExistente);
                if (esPositivo) {
                    respuesta.setVotosPositivos(respuesta.getVotosPositivos() + 1);
                    respuesta.setVotosNegativos(respuesta.getVotosNegativos() - 1);
                } else {
                    respuesta.setVotosPositivos(respuesta.getVotosPositivos() - 1);
                    respuesta.setVotosNegativos(respuesta.getVotosNegativos() + 1);
                }
            }
        } else {
            // Crear nuevo voto
            ForoVoto nuevoVoto = new ForoVoto();
            nuevoVoto.setRespuesta(respuesta);
            nuevoVoto.setUsuario(usuario);
            nuevoVoto.setTipoVoto(tipoVoto);
            nuevoVoto.setFechaVoto(LocalDateTime.now());
            foroVotoRepository.save(nuevoVoto);

            if (esPositivo) {
                respuesta.setVotosPositivos(respuesta.getVotosPositivos() + 1);
            } else {
                respuesta.setVotosNegativos(respuesta.getVotosNegativos() + 1);
            }
        }

        // Actualizar puntuación total
        respuesta.setPuntuacionTotal(respuesta.getVotosPositivos() - respuesta.getVotosNegativos());
        return foroRespuestaRepository.save(respuesta);
    }

    // =================== MÉTODOS AUXILIARES ===================

    /**
     * Generar slug único a partir de un título
     */
    private String generarSlug(String titulo) {
        String slug = titulo.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        // Asegurar unicidad
        String slugBase = slug;
        int contador = 1;
        while (foroTemaRepository.findBySlug(slug).isPresent()) {
            slug = slugBase + "-" + contador;
            contador++;
        }

        return slug;
    }

    /**
     * Obtener solución de un tema
     */
    @Transactional(readOnly = true)
    public ForoRespuesta obtenerSolucionDeTema(Long temaId) {
        List<ForoRespuesta> soluciones = foroRespuestaRepository.findSolutionByTemaId(temaId);
        return soluciones.isEmpty() ? null : soluciones.get(0);
    }

    /**
     * Contar respuestas de un tema
     */
    @Transactional(readOnly = true)
    public long contarRespuestasDeTema(Long temaId) {
        return foroRespuestaRepository.countByTema_TemaId(temaId);
    }
}
