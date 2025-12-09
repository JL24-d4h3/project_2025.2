package org.project.project.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.jsoup.Jsoup;
import org.project.project.model.entity.Reporte;
import org.project.project.model.entity.ReporteAdjunto;
import org.project.project.repository.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ReportePdfExportService - Servicio profesional para exportar reportes a PDF
 * 
 * Genera PDFs corporativos y formales usando Apache PDFBox con:
 * - Diseño profesional tipo documento ejecutivo
 * - Portada con logo y título
 * - Tabla de metadatos estructurada
 * - Tipografía corporativa (Helvetica)
 * - Encabezados y pies de página
 * - Formato de reporte de issues formal
 * 
 * @author DevPortal Team
 * @since 2025-11-13
 */
@Service
@Slf4j
public class ReportePdfExportService {

    @Autowired
    private ReporteRepository reporteRepository;

    // Colores corporativos - Paleta monocromática profesional
    private static final Color COLOR_PRIMARY = new Color(30, 30, 30);          // Negro corporativo
    private static final Color COLOR_SECONDARY = new Color(90, 90, 90);        // Gris medio
    private static final Color COLOR_LIGHT = new Color(180, 180, 180);         // Gris claro
    private static final Color COLOR_BORDER = new Color(220, 220, 220);        // Gris muy claro
    private static final Color COLOR_BACKGROUND = new Color(250, 250, 250);    // Gris casi blanco
    private static final Color COLOR_ACCENT = new Color(60, 60, 60);           // Gris oscuro para acentos

    // Fuentes corporativas
    private static final PDFont FONT_TITLE = PDType1Font.HELVETICA_BOLD;
    private static final PDFont FONT_HEADING = PDType1Font.HELVETICA_BOLD;
    private static final PDFont FONT_BODY = PDType1Font.HELVETICA;
    private static final PDFont FONT_BODY_BOLD = PDType1Font.HELVETICA_BOLD;
    private static final PDFont FONT_MONO = PDType1Font.COURIER;

    // Márgenes y dimensiones
    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN);

    // Formateo de fechas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_SHORT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Exporta un reporte a PDF profesional
     */
    public ByteArrayOutputStream exportarReporteAPdf(Long reporteId) throws IOException {
        log.info("📄 Iniciando generación de PDF profesional para reporte ID={}", reporteId);

        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + reporteId));

        try (PDDocument document = new PDDocument()) {
            // Página 1: Portada
            crearPortada(document, reporte);

            // Página 2+: Contenido del reporte
            crearPaginaContenido(document, reporte);

            // Generar output
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            
            log.info("✅ PDF profesional generado exitosamente ({}KB) para reporte ID={}", 
                    outputStream.size() / 1024, reporteId);
            
            return outputStream;
        }
    }

    /**
     * Crea la portada profesional del reporte - Estilo formal y minimalista
     */
    private void crearPortada(PDDocument document, Reporte reporte) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
            float yPos = PAGE_HEIGHT - 100;

            // Línea superior delgada y elegante
            content.setStrokingColor(COLOR_PRIMARY);
            content.setLineWidth(1);
            content.moveTo(MARGIN, PAGE_HEIGHT - 70);
            content.lineTo(PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 70);
            content.stroke();

            // Logo/Nombre de la plataforma - Minimalista
            content.beginText();
            content.setNonStrokingColor(COLOR_PRIMARY);
            content.setFont(FONT_TITLE, 14);
            content.newLineAtOffset(MARGIN, PAGE_HEIGHT - 60);
            content.showText("DEVPORTAL");
            content.endText();

            content.beginText();
            content.setNonStrokingColor(COLOR_SECONDARY);
            content.setFont(FONT_BODY, 9);
            content.newLineAtOffset(MARGIN + 95, PAGE_HEIGHT - 60);
            content.showText("Developer Issue Tracking System");
            content.endText();

            yPos = PAGE_HEIGHT - 140;

            // Tipo de documento
            content.beginText();
            content.setNonStrokingColor(COLOR_SECONDARY);
            content.setFont(FONT_BODY, 10);
            content.newLineAtOffset(MARGIN, yPos);
            content.showText("REPORTE DE ISSUE");
            content.endText();

            yPos -= 10;

            // Línea decorativa debajo del tipo
            content.setStrokingColor(COLOR_BORDER);
            content.setLineWidth(0.5f);
            content.moveTo(MARGIN, yPos);
            content.lineTo(MARGIN + 120, yPos);
            content.stroke();

            yPos -= 50;

            // Título del reporte - Grande y elegante
            content.beginText();
            content.setNonStrokingColor(COLOR_PRIMARY);
            content.setFont(FONT_TITLE, 22);
            content.newLineAtOffset(MARGIN, yPos);
            
            // Dividir título si es muy largo
            String titulo = reporte.getTituloReporte();
            List<String> tituloLineas = dividirTexto(titulo, FONT_TITLE, 22, CONTENT_WIDTH);
            for (String linea : tituloLineas) {
                content.showText(linea);
                content.newLineAtOffset(0, -28);
                yPos -= 28;
            }
            content.endText();

            yPos -= 30;

            // ID y Tipo en una línea
            content.beginText();
            content.setNonStrokingColor(COLOR_SECONDARY);
            content.setFont(FONT_BODY, 9);
            content.newLineAtOffset(MARGIN, yPos);
            content.showText("ID: " + reporte.getReporteId() + "  |  TIPO: " + reporte.getTipoReporte().name() + 
                           "  |  ESTADO: " + reporte.getEstadoReporte().name());
            content.endText();

            yPos -= 50;

            // Tabla de metadatos - Diseño minimalista
            dibujarTablaMetadatosMinimalista(content, reporte, yPos);

            // Pie de página minimalista
            content.setStrokingColor(COLOR_BORDER);
            content.setLineWidth(0.5f);
            content.moveTo(MARGIN, 60);
            content.lineTo(PAGE_WIDTH - MARGIN, 60);
            content.stroke();

            content.beginText();
            content.setNonStrokingColor(COLOR_SECONDARY);
            content.setFont(FONT_BODY, 8);
            content.newLineAtOffset(MARGIN, 45);
            content.showText("Fecha de generacion: " + java.time.LocalDateTime.now().format(DATE_FORMATTER));
            content.endText();

            content.beginText();
            content.newLineAtOffset(PAGE_WIDTH - MARGIN - 80, 45);
            content.showText("Documento confidencial");
            content.endText();
        }
    }

    /**
     * Crea las páginas de contenido del reporte
     */
    private void crearPaginaContenido(PDDocument document, Reporte reporte) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream content = new PDPageContentStream(document, page);
        float yPos = PAGE_HEIGHT - MARGIN;

        // Encabezado de página
        dibujarEncabezadoPagina(content, reporte);
        yPos -= 80;

        // Sección: Descripción
        if (reporte.getDescripcionReporte() != null && !reporte.getDescripcionReporte().isEmpty()) {
            yPos = dibujarSeccion(content, "DESCRIPCION", reporte.getDescripcionReporte(), yPos, document, reporte);
            if (yPos < 0) {
                content.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                content = new PDPageContentStream(document, page);
                dibujarEncabezadoPagina(content, reporte);
                yPos = PAGE_HEIGHT - 100;
            }
        }

        // Sección: Contenido HTML (convertido a texto)
        if (reporte.getContenidoReporte() != null && !reporte.getContenidoReporte().isEmpty()) {
            String contenidoTexto = htmlATexto(reporte.getContenidoReporte());
            yPos = dibujarSeccion(content, "DETALLES DEL ISSUE", contenidoTexto, yPos, document, reporte);
            if (yPos < 0) {
                content.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                content = new PDPageContentStream(document, page);
                dibujarEncabezadoPagina(content, reporte);
                yPos = PAGE_HEIGHT - 100;
            }
        }

        // Sección: Adjuntos
        if (reporte.getAdjuntos() != null && !reporte.getAdjuntos().isEmpty()) {
            yPos = dibujarSeccionAdjuntos(content, new ArrayList<>(reporte.getAdjuntos()), yPos, document, reporte);
        }

        // Pie de página con número
        dibujarPiePagina(content, document.getNumberOfPages());
        content.close();
    }

    /**
     * Dibuja el encabezado de página - Estilo minimalista con entidad relacionada
     */
    private void dibujarEncabezadoPagina(PDPageContentStream content, Reporte reporte) throws IOException {
        // Línea superior delgada
        content.setStrokingColor(COLOR_BORDER);
        content.setLineWidth(0.5f);
        content.moveTo(MARGIN, PAGE_HEIGHT - MARGIN + 10);
        content.lineTo(PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN + 10);
        content.stroke();

        // Título pequeño y discreto
        content.beginText();
        content.setNonStrokingColor(COLOR_SECONDARY);
        content.setFont(FONT_BODY, 8);
        content.newLineAtOffset(MARGIN, PAGE_HEIGHT - MARGIN - 8);
        content.showText("DEVPORTAL" + (reporte != null ? " | Reporte #" + reporte.getReporteId() : ""));
        content.endText();

        if (reporte != null) {
            // Entidad relacionada (si existe)
            String entidadInfo = obtenerEntidadRelacionadaDetallada(reporte);
            if (entidadInfo != null && !entidadInfo.equals("Sin especificar")) {
                content.beginText();
                content.setNonStrokingColor(COLOR_SECONDARY);
                content.setFont(FONT_BODY, 8);
                content.newLineAtOffset(MARGIN + 200, PAGE_HEIGHT - MARGIN - 8);
                content.showText("Relacionado con: " + entidadInfo);
                content.endText();
            }
            
            // Estado en el lado derecho
            content.beginText();
            content.setNonStrokingColor(COLOR_SECONDARY);
            content.setFont(FONT_BODY, 8);
            content.newLineAtOffset(PAGE_WIDTH - MARGIN - 80, PAGE_HEIGHT - MARGIN - 8);
            content.showText(reporte.getEstadoReporte().name());
            content.endText();
        }
    }

    /**
     * Dibuja el pie de página con numeración
     */
    private void dibujarPiePagina(PDPageContentStream content, int numPagina) throws IOException {
        content.setStrokingColor(COLOR_BORDER);
        content.setLineWidth(0.5f);
        content.moveTo(MARGIN, 50);
        content.lineTo(PAGE_WIDTH - MARGIN, 50);
        content.stroke();

        content.beginText();
        content.setNonStrokingColor(COLOR_SECONDARY);
        content.setFont(FONT_BODY, 9);
        content.newLineAtOffset(PAGE_WIDTH / 2 - 20, 35);
        content.showText("Pagina " + numPagina);
        content.endText();
    }

    /**
     * Dibuja una tabla de metadatos con estilo minimalista y formal
     */
    private void dibujarTablaMetadatosMinimalista(PDPageContentStream content, Reporte reporte, float startY) throws IOException {
        float yPos = startY;
        float lineHeight = 22;
        float labelWidth = 150;

        String[][] datos = {
            {"Reportado por:", construirNombreCompleto(reporte.getAutor())},
            {"Fecha de creacion:", reporte.getCreadoEn() != null ? reporte.getCreadoEn().format(DATE_FORMATTER) : "N/A"},
            {"Estado:", reporte.getEstadoReporte().name()},
            {"Tipo de issue:", reporte.getTipoReporte().name()},
            {"Relacionado con:", obtenerEntidadRelacionadaDetallada(reporte)}
        };

        for (String[] fila : datos) {
            // Etiqueta (izquierda) - En gris
            content.beginText();
            content.setNonStrokingColor(COLOR_SECONDARY);
            content.setFont(FONT_BODY, 10);
            content.newLineAtOffset(MARGIN, yPos);
            content.showText(fila[0]);
            content.endText();

            // Valor (derecha) - En negro
            content.beginText();
            content.setNonStrokingColor(COLOR_PRIMARY);
            content.setFont(FONT_BODY_BOLD, 10);
            content.newLineAtOffset(MARGIN + labelWidth, yPos);
            
            String valor = fila[1] != null ? fila[1] : "No especificado";
            
            // Dividir si es muy largo
            if (valor.length() > 60) {
                List<String> lineasValor = dividirTexto(valor, FONT_BODY_BOLD, 10, CONTENT_WIDTH - labelWidth - 10);
                for (int i = 0; i < lineasValor.size() && i < 2; i++) {
                    content.showText(lineasValor.get(i));
                    if (i < lineasValor.size() - 1) {
                        content.newLineAtOffset(0, -lineHeight);
                        yPos -= lineHeight;
                    }
                }
            } else {
                content.showText(valor);
            }
            content.endText();

            // Línea sutil de separación
            content.setStrokingColor(COLOR_BORDER);
            content.setLineWidth(0.3f);
            content.moveTo(MARGIN, yPos - 8);
            content.lineTo(PAGE_WIDTH - MARGIN, yPos - 8);
            content.stroke();

            yPos -= lineHeight + 8;
        }
    }

    /**
     * Obtiene información detallada de la entidad relacionada
     */
    private String obtenerEntidadRelacionadaDetallada(Reporte reporte) {
        // Intentar obtener el nombre específico de la primera entidad relacionada
        if (reporte.getApisRelacionadas() != null && !reporte.getApisRelacionadas().isEmpty()) {
            try {
                var primeraApi = reporte.getApisRelacionadas().iterator().next();
                if (primeraApi.getApi() != null && primeraApi.getApi().getNombreApi() != null) {
                    return "API: " + primeraApi.getApi().getNombreApi();
                }
            } catch (Exception e) {
                // Continuar con el conteo
            }
            return "API (" + reporte.getApisRelacionadas().size() + " relacionada(s))";
        } else if (reporte.getTicketsRelacionados() != null && !reporte.getTicketsRelacionados().isEmpty()) {
            return "TICKET (" + reporte.getTicketsRelacionados().size() + " relacionado(s))";
        } else if (reporte.getProyectosRelacionados() != null && !reporte.getProyectosRelacionados().isEmpty()) {
            try {
                var primerProyecto = reporte.getProyectosRelacionados().iterator().next();
                if (primerProyecto.getProyecto() != null && primerProyecto.getProyecto().getNombreProyecto() != null) {
                    return "PROYECTO: " + primerProyecto.getProyecto().getNombreProyecto();
                }
            } catch (Exception e) {
                // Continuar con el conteo
            }
            return "PROYECTO (" + reporte.getProyectosRelacionados().size() + " relacionado(s))";
        } else if (reporte.getRepositoriosRelacionados() != null && !reporte.getRepositoriosRelacionados().isEmpty()) {
            try {
                var primerRepo = reporte.getRepositoriosRelacionados().iterator().next();
                if (primerRepo.getRepositorio() != null && primerRepo.getRepositorio().getNombreRepositorio() != null) {
                    return "REPOSITORIO: " + primerRepo.getRepositorio().getNombreRepositorio();
                }
            } catch (Exception e) {
                // Continuar con el conteo
            }
            return "REPOSITORIO (" + reporte.getRepositoriosRelacionados().size() + " relacionado(s))";
        } else if (reporte.getDocumentacionRelacionada() != null && !reporte.getDocumentacionRelacionada().isEmpty()) {
            return "DOCUMENTACION (" + reporte.getDocumentacionRelacionada().size() + " relacionada(s))";
        } else if (reporte.getForumRelacionado() != null && !reporte.getForumRelacionado().isEmpty()) {
            return "FORO (" + reporte.getForumRelacionado().size() + " tema(s) relacionado(s))";
        }
        return "General - Sin entidad especifica";
    }

    /**
     * Dibuja una tabla de metadatos (método antiguo - deprecado)
     */
    @Deprecated
    private void dibujarTablaMetadatos(PDPageContentStream content, Reporte reporte, float startY) throws IOException {
        float tableWidth = CONTENT_WIDTH;
        float rowHeight = 35;
        float yPos = startY;

        String[][] datos = {
            {"Reportado por:", construirNombreCompleto(reporte.getAutor())},
            {"Fecha de creacion:", reporte.getCreadoEn() != null ? reporte.getCreadoEn().format(DATE_FORMATTER) : "N/A"},
            {"Estado:", reporte.getEstadoReporte().name()},
            {"Prioridad:", reporte.getTipoReporte().name()},
            {"Entidad relacionada:", obtenerNombreEntidadRelacionada(reporte)}
        };

        for (String[] fila : datos) {
            // Fondo alternado
            content.setNonStrokingColor(COLOR_BACKGROUND);
            content.addRect(MARGIN, yPos - rowHeight + 5, tableWidth, rowHeight);
            content.fill();

            // Borde
            content.setStrokingColor(COLOR_BORDER);
            content.setLineWidth(0.5f);
            content.addRect(MARGIN, yPos - rowHeight + 5, tableWidth, rowHeight);
            content.stroke();

            // Etiqueta (izquierda)
            content.beginText();
            content.setNonStrokingColor(COLOR_SECONDARY);
            content.setFont(FONT_BODY_BOLD, 10);
            content.newLineAtOffset(MARGIN + 10, yPos - 20);
            content.showText(fila[0]);
            content.endText();

            // Valor (derecha)
            content.beginText();
            content.setNonStrokingColor(COLOR_PRIMARY);
            content.setFont(FONT_BODY, 10);
            content.newLineAtOffset(MARGIN + 200, yPos - 20);
            
            String valor = fila[1] != null ? fila[1] : "N/A";
            if (valor.length() > 50) {
                valor = valor.substring(0, 47) + "...";
            }
            content.showText(valor);
            content.endText();

            yPos -= rowHeight;
        }
    }

    /**
     * Dibuja una sección de contenido con formato justificado y márgenes profesionales
     */
    private float dibujarSeccion(PDPageContentStream content, String titulo, String textoContenido, 
                                  float yPos, PDDocument document, Reporte reporte) throws IOException {
        
        // Verificar si necesitamos nueva página
        if (yPos < 150) {
            content.close();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            dibujarEncabezadoPagina(content, reporte);
            yPos = PAGE_HEIGHT - 100;
        }

        // Título de sección - Minimalista
        content.beginText();
        content.setNonStrokingColor(COLOR_PRIMARY);
        content.setFont(FONT_HEADING, 12);
        content.newLineAtOffset(MARGIN, yPos);
        content.showText(titulo);
        content.endText();

        yPos -= 4;

        // Línea debajo del título - Delgada y elegante
        content.setStrokingColor(COLOR_PRIMARY);
        content.setLineWidth(0.5f);
        content.moveTo(MARGIN, yPos);
        content.lineTo(MARGIN + 100, yPos);
        content.stroke();

        yPos -= 20;

        // Marco de contenido con fondo sutil
        float contenidoInicio = yPos;
        float margenInterior = 15; // Margen interior del cuadro de contenido
        
        // Contenido con márgenes adecuados y justificado
        List<String> lineas = dividirTextoJustificado(textoContenido, FONT_BODY, 10, CONTENT_WIDTH - (margenInterior * 2));
        
        // Calcular altura del contenido
        float alturaContenido = lineas.size() * 14 + (margenInterior * 2);
        
        // Dibujar fondo sutil para el contenido
        content.setNonStrokingColor(COLOR_BACKGROUND);
        content.addRect(MARGIN, yPos - alturaContenido + margenInterior, CONTENT_WIDTH, alturaContenido);
        content.fill();

        // Borde del cuadro de contenido
        content.setStrokingColor(COLOR_BORDER);
        content.setLineWidth(0.5f);
        content.addRect(MARGIN, yPos - alturaContenido + margenInterior, CONTENT_WIDTH, alturaContenido);
        content.stroke();
        
        yPos -= margenInterior;
        
        for (String linea : lineas) {
            if (yPos < 80) {
                // Nueva página si no hay espacio
                content.close();
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                content = new PDPageContentStream(document, page);
                dibujarEncabezadoPagina(content, reporte);
                dibujarPiePagina(content, document.getNumberOfPages());
                yPos = PAGE_HEIGHT - 100;
            }

            content.beginText();
            content.setNonStrokingColor(COLOR_PRIMARY);
            content.setFont(FONT_BODY, 10);
            content.newLineAtOffset(MARGIN + margenInterior, yPos);
            content.showText(linea);
            content.endText();

            yPos -= 14;
        }

        return yPos - margenInterior - 20;
    }

    /**
     * Divide texto en líneas justificadas con límite de caracteres por línea para evitar desbordamiento
     */
    private List<String> dividirTextoJustificado(String texto, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lineas = new ArrayList<>();
        String[] palabras = texto.split("\\s+");
        StringBuilder lineaActual = new StringBuilder();
        
        // Límite de caracteres por línea para mantener márgenes formales (aproximadamente 80 caracteres)
        int maxCaracteresPorLinea = 85;

        for (String palabra : palabras) {
            String prueba = lineaActual.length() == 0 ? palabra : lineaActual + " " + palabra;
            float width = font.getStringWidth(prueba) / 1000 * fontSize;
            
            // Validar tanto por ancho visual como por número de caracteres
            boolean excedeLimiteAncho = width > maxWidth;
            boolean excedeLimiteCaracteres = prueba.length() > maxCaracteresPorLinea;

            if (excedeLimiteAncho || excedeLimiteCaracteres) {
                if (lineaActual.length() > 0) {
                    lineas.add(lineaActual.toString());
                    lineaActual = new StringBuilder(palabra);
                } else {
                    // Palabra muy larga, dividirla
                    lineas.add(palabra);
                }
            } else {
                if (lineaActual.length() > 0) {
                    lineaActual.append(" ");
                }
                lineaActual.append(palabra);
            }
        }

        if (lineaActual.length() > 0) {
            lineas.add(lineaActual.toString());
        }

        return lineas;
    }

    /**
     * Dibuja la sección de adjuntos
     */
    private float dibujarSeccionAdjuntos(PDPageContentStream content, List<ReporteAdjunto> adjuntos, 
                                         float yPos, PDDocument document, Reporte reporte) throws IOException {
        
        if (yPos < 200) {
            content.close();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            dibujarEncabezadoPagina(content, reporte);
            yPos = PAGE_HEIGHT - 100;
        }

        // Título
        content.beginText();
        content.setNonStrokingColor(COLOR_PRIMARY);
        content.setFont(FONT_HEADING, 14);
        content.newLineAtOffset(MARGIN, yPos);
        content.showText("ARCHIVOS ADJUNTOS (" + adjuntos.size() + ")");
        content.endText();

        yPos -= 8;
        content.setStrokingColor(COLOR_PRIMARY);
        content.setLineWidth(1.5f);
        content.moveTo(MARGIN, yPos);
        content.lineTo(MARGIN + 200, yPos);
        content.stroke();

        yPos -= 25;

        // Lista de adjuntos
        for (ReporteAdjunto adjunto : adjuntos) {
            if (yPos < 80) {
                content.close();
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                content = new PDPageContentStream(document, page);
                dibujarEncabezadoPagina(content, reporte);
                yPos = PAGE_HEIGHT - 100;
            }

            // Icono/bullet
            content.setNonStrokingColor(COLOR_SECONDARY);
            content.addRect(MARGIN + 10, yPos - 3, 4, 4);
            content.fill();

            // Nombre del archivo
            content.beginText();
            content.setNonStrokingColor(COLOR_PRIMARY);
            content.setFont(FONT_BODY_BOLD, 10);
            content.newLineAtOffset(MARGIN + 20, yPos);
            String nombreArchivo = adjunto.getNombreArchivo();
            if (nombreArchivo.length() > 60) {
                nombreArchivo = nombreArchivo.substring(0, 57) + "...";
            }
            content.showText(nombreArchivo);
            content.endText();

            // Tamaño
            content.beginText();
            content.setNonStrokingColor(COLOR_SECONDARY);
            content.setFont(FONT_BODY, 9);
            content.newLineAtOffset(MARGIN + 20, yPos - 12);
            content.showText("Tamano: " + formatearTamano(adjunto.getTamanoBytes()));
            content.endText();

            yPos -= 30;
        }

        return yPos - 20;
    }

    /**
     * Obtiene el color según el tipo de reporte
     */
    private Color getColorForType(Reporte.TipoReporte tipo) {
        switch (tipo) {
            case TICKET: return COLOR_ACCENT;
            case API: return COLOR_PRIMARY;
            case PROYECTO: return new Color(139, 92, 246); // Púrpura
            case REPOSITORIO: return new Color(34, 197, 94); // Verde
            case DOCUMENTACION: return new Color(245, 158, 11); // Amarillo
            case FORO: return new Color(236, 72, 153); // Rosa
            case GENERAL: return COLOR_SECONDARY;
            default: return COLOR_SECONDARY;
        }
    }

    /**
     * Construye el nombre completo del usuario
     */
    private String construirNombreCompleto(org.project.project.model.entity.Usuario usuario) {
        if (usuario == null) return "Usuario desconocido";
        
        String nombre = usuario.getNombreUsuario() != null ? usuario.getNombreUsuario() : "";
        String apellidoP = usuario.getApellidoPaterno() != null ? usuario.getApellidoPaterno() : "";
        String apellidoM = usuario.getApellidoMaterno() != null ? usuario.getApellidoMaterno() : "";
        
        return (nombre + " " + apellidoP + " " + apellidoM).trim();
    }

    /**
     * Obtiene el nombre de la entidad relacionada
     */
    private String obtenerNombreEntidadRelacionada(Reporte reporte) {
        // Verificar relaciones según el tipo de reporte
        if (reporte.getApisRelacionadas() != null && !reporte.getApisRelacionadas().isEmpty()) {
            return reporte.getApisRelacionadas().size() + " API(s)";
        } else if (reporte.getTicketsRelacionados() != null && !reporte.getTicketsRelacionados().isEmpty()) {
            return reporte.getTicketsRelacionados().size() + " Ticket(s)";
        } else if (reporte.getProyectosRelacionados() != null && !reporte.getProyectosRelacionados().isEmpty()) {
            return reporte.getProyectosRelacionados().size() + " Proyecto(s)";
        } else if (reporte.getRepositoriosRelacionados() != null && !reporte.getRepositoriosRelacionados().isEmpty()) {
            return reporte.getRepositoriosRelacionados().size() + " Repositorio(s)";
        } else if (reporte.getDocumentacionRelacionada() != null && !reporte.getDocumentacionRelacionada().isEmpty()) {
            return reporte.getDocumentacionRelacionada().size() + " Documentacion(es)";
        } else if (reporte.getForumRelacionado() != null && !reporte.getForumRelacionado().isEmpty()) {
            return reporte.getForumRelacionado().size() + " Tema(s) de foro";
        }
        return "General";
    }

    /**
     * Convierte HTML a texto plano
     */
    private String htmlATexto(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        return Jsoup.parse(html).text();
    }

    /**
     * Divide texto en líneas que caben en el ancho especificado
     */
    private List<String> dividirTexto(String texto, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lineas = new ArrayList<>();
        String[] palabras = texto.split("\\s+");
        StringBuilder lineaActual = new StringBuilder();

        for (String palabra : palabras) {
            String prueba = lineaActual.length() == 0 ? palabra : lineaActual + " " + palabra;
            float width = font.getStringWidth(prueba) / 1000 * fontSize;

            if (width > maxWidth) {
                if (lineaActual.length() > 0) {
                    lineas.add(lineaActual.toString());
                    lineaActual = new StringBuilder(palabra);
                } else {
                    lineas.add(palabra);
                }
            } else {
                if (lineaActual.length() > 0) {
                    lineaActual.append(" ");
                }
                lineaActual.append(palabra);
            }
        }

        if (lineaActual.length() > 0) {
            lineas.add(lineaActual.toString());
        }

        return lineas;
    }

    /**
     * Formatea el tamaño de archivo
     */
    private String formatearTamano(Long bytes) {
        if (bytes == null) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
