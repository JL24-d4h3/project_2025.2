package org.project.project.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.project.project.model.dto.ReportMetricsDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * ReportExportService - Servicio para exportar métricas del dashboard a Excel/CSV
 * 
 * Exporta estadísticas de:
 * - Proyectos (por propietario, por estado)
 * - Repositorios (por tipo, por estado)
 * - Tickets (por estado, por prioridad)
 * 
 * Formatos soportados:
 * - Excel (.xlsx) - usando Apache POI
 * - CSV (.csv) - generación manual
 * 
 * @author jleon
 * @since 2025-01-23
 */
@Service
@Slf4j
public class ReportExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ==================== EXCEL EXPORT ====================

    /**
     * Exporta métricas del dashboard a formato Excel (.xlsx)
     * 
     * @param metrics DTO con métricas del dashboard (proyectos, repos, tickets)
     * @return ByteArrayOutputStream con el archivo Excel
     * @throws IOException Si hay error al generar el archivo
     */
    public ByteArrayOutputStream exportToExcel(ReportMetricsDTO metrics) throws IOException {
        log.info("Generando export Excel para usuario: {}", metrics.getUsuarioSolicitante());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ===== HOJA 1: RESUMEN =====
            Sheet resumenSheet = workbook.createSheet("Resumen");
            crearHojaResumen(workbook, resumenSheet, metrics);

            // ===== HOJA 2: PROYECTOS =====
            Sheet proyectosSheet = workbook.createSheet("Proyectos");
            crearHojaProyectos(workbook, proyectosSheet, metrics);

            // ===== HOJA 3: REPOSITORIOS =====
            Sheet repositoriosSheet = workbook.createSheet("Repositorios");
            crearHojaRepositorios(workbook, repositoriosSheet, metrics);

            // ===== HOJA 4: TICKETS =====
            Sheet ticketsSheet = workbook.createSheet("Tickets");
            crearHojaTickets(workbook, ticketsSheet, metrics);

            // Escribir y retornar
            workbook.write(out);
            log.info("Excel generado exitosamente. Total hojas: {}", workbook.getNumberOfSheets());

            return out;
        } catch (IOException e) {
            log.error("Error generando Excel: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Crea la hoja de resumen general
     */
    private void crearHojaResumen(Workbook workbook, Sheet sheet, ReportMetricsDTO metrics) {
        int rowNum = 0;

        // Estilos
        CellStyle headerStyle = crearEstiloHeader(workbook);
        CellStyle titleStyle = crearEstiloTitulo(workbook);

        // TÍTULO
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE MÉTRICAS - DASHBOARD");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));
        rowNum++; // Espacio

        // METADATA
        crearFilaInfo(sheet, rowNum++, "Usuario Solicitante:", metrics.getUsuarioSolicitante());
        crearFilaInfo(sheet, rowNum++, "Rol:", metrics.getRolUsuario());
        crearFilaInfo(sheet, rowNum++, "Fecha de Generación:", 
                metrics.getFechaGeneracion());
        rowNum++; // Espacio

        // TOTALES
        Row totalesHeaderRow = sheet.createRow(rowNum++);
        Cell totalesHeader = totalesHeaderRow.createCell(0);
        totalesHeader.setCellValue("TOTALES GENERALES");
        totalesHeader.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

        crearFilaInfo(sheet, rowNum++, "Total Proyectos:", String.valueOf(metrics.getTotalProyectos()));
        crearFilaInfo(sheet, rowNum++, "Total Repositorios:", String.valueOf(metrics.getTotalRepositorios()));
        crearFilaInfo(sheet, rowNum++, "Total Tickets:", String.valueOf(metrics.getTotalTickets()));

        // Autosize columnas
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /**
     * Crea la hoja de proyectos
     */
    private void crearHojaProyectos(Workbook workbook, Sheet sheet, ReportMetricsDTO metrics) {
        int rowNum = 0;

        CellStyle headerStyle = crearEstiloHeader(workbook);
        CellStyle titleStyle = crearEstiloTitulo(workbook);

        // TÍTULO
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("PROYECTOS");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));
        rowNum++; // Espacio

        // PROYECTOS POR PROPIETARIO
        Row headerPropRow = sheet.createRow(rowNum++);
        Cell headerProp1 = headerPropRow.createCell(0);
        headerProp1.setCellValue("Proyectos por Propietario");
        headerProp1.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

        Row subHeaderPropRow = sheet.createRow(rowNum++);
        Cell subHeaderProp1 = subHeaderPropRow.createCell(0);
        subHeaderProp1.setCellValue("Propietario");
        subHeaderProp1.setCellStyle(headerStyle);
        Cell subHeaderProp2 = subHeaderPropRow.createCell(1);
        subHeaderProp2.setCellValue("Cantidad");
        subHeaderProp2.setCellStyle(headerStyle);

        for (Map.Entry<String, Long> entry : metrics.getProyectosPorPropietario().entrySet()) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(entry.getKey());
            dataRow.createCell(1).setCellValue(entry.getValue());
        }

        rowNum++; // Espacio

        // PROYECTOS POR ESTADO
        Row headerEstadoRow = sheet.createRow(rowNum++);
        Cell headerEstado1 = headerEstadoRow.createCell(0);
        headerEstado1.setCellValue("Proyectos por Estado");
        headerEstado1.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

        Row subHeaderEstadoRow = sheet.createRow(rowNum++);
        Cell subHeaderEstado1 = subHeaderEstadoRow.createCell(0);
        subHeaderEstado1.setCellValue("Estado");
        subHeaderEstado1.setCellStyle(headerStyle);
        Cell subHeaderEstado2 = subHeaderEstadoRow.createCell(1);
        subHeaderEstado2.setCellValue("Cantidad");
        subHeaderEstado2.setCellStyle(headerStyle);

        for (Map.Entry<String, Long> entry : metrics.getProyectosPorEstado().entrySet()) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(entry.getKey());
            dataRow.createCell(1).setCellValue(entry.getValue());
        }

        // Autosize
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /**
     * Crea la hoja de repositorios
     */
    private void crearHojaRepositorios(Workbook workbook, Sheet sheet, ReportMetricsDTO metrics) {
        int rowNum = 0;

        CellStyle headerStyle = crearEstiloHeader(workbook);
        CellStyle titleStyle = crearEstiloTitulo(workbook);

        // TÍTULO
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPOSITORIOS");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));
        rowNum++; // Espacio

        // REPOSITORIOS POR TIPO
        Row headerTipoRow = sheet.createRow(rowNum++);
        Cell headerTipo1 = headerTipoRow.createCell(0);
        headerTipo1.setCellValue("Repositorios por Tipo");
        headerTipo1.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

        Row subHeaderTipoRow = sheet.createRow(rowNum++);
        Cell subHeaderTipo1 = subHeaderTipoRow.createCell(0);
        subHeaderTipo1.setCellValue("Tipo");
        subHeaderTipo1.setCellStyle(headerStyle);
        Cell subHeaderTipo2 = subHeaderTipoRow.createCell(1);
        subHeaderTipo2.setCellValue("Cantidad");
        subHeaderTipo2.setCellStyle(headerStyle);

        for (Map.Entry<String, Long> entry : metrics.getRepositoriosPorTipo().entrySet()) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(entry.getKey());
            dataRow.createCell(1).setCellValue(entry.getValue());
        }

        rowNum++; // Espacio

        // REPOSITORIOS POR ESTADO
        Row headerEstadoRow = sheet.createRow(rowNum++);
        Cell headerEstado1 = headerEstadoRow.createCell(0);
        headerEstado1.setCellValue("Repositorios por Estado");
        headerEstado1.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

        Row subHeaderEstadoRow = sheet.createRow(rowNum++);
        Cell subHeaderEstado1 = subHeaderEstadoRow.createCell(0);
        subHeaderEstado1.setCellValue("Estado");
        subHeaderEstado1.setCellStyle(headerStyle);
        Cell subHeaderEstado2 = subHeaderEstadoRow.createCell(1);
        subHeaderEstado2.setCellValue("Cantidad");
        subHeaderEstado2.setCellStyle(headerStyle);

        for (Map.Entry<String, Long> entry : metrics.getRepositoriosPorEstado().entrySet()) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(entry.getKey());
            dataRow.createCell(1).setCellValue(entry.getValue());
        }

        // Autosize
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /**
     * Crea la hoja de tickets
     */
    private void crearHojaTickets(Workbook workbook, Sheet sheet, ReportMetricsDTO metrics) {
        int rowNum = 0;

        CellStyle headerStyle = crearEstiloHeader(workbook);
        CellStyle titleStyle = crearEstiloTitulo(workbook);

        // TÍTULO
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("TICKETS");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));
        rowNum++; // Espacio

        // TICKETS POR ESTADO
        Row headerEstadoRow = sheet.createRow(rowNum++);
        Cell headerEstado1 = headerEstadoRow.createCell(0);
        headerEstado1.setCellValue("Tickets por Estado");
        headerEstado1.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

        Row subHeaderEstadoRow = sheet.createRow(rowNum++);
        Cell subHeaderEstado1 = subHeaderEstadoRow.createCell(0);
        subHeaderEstado1.setCellValue("Estado");
        subHeaderEstado1.setCellStyle(headerStyle);
        Cell subHeaderEstado2 = subHeaderEstadoRow.createCell(1);
        subHeaderEstado2.setCellValue("Cantidad");
        subHeaderEstado2.setCellStyle(headerStyle);

        for (Map.Entry<String, Long> entry : metrics.getTicketsPorEstado().entrySet()) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(entry.getKey());
            dataRow.createCell(1).setCellValue(entry.getValue());
        }

        rowNum++; // Espacio

        // TICKETS POR PRIORIDAD
        Row headerPrioridadRow = sheet.createRow(rowNum++);
        Cell headerPrioridad1 = headerPrioridadRow.createCell(0);
        headerPrioridad1.setCellValue("Tickets por Prioridad");
        headerPrioridad1.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

        Row subHeaderPrioridadRow = sheet.createRow(rowNum++);
        Cell subHeaderPrioridad1 = subHeaderPrioridadRow.createCell(0);
        subHeaderPrioridad1.setCellValue("Prioridad");
        subHeaderPrioridad1.setCellStyle(headerStyle);
        Cell subHeaderPrioridad2 = subHeaderPrioridadRow.createCell(1);
        subHeaderPrioridad2.setCellValue("Cantidad");
        subHeaderPrioridad2.setCellStyle(headerStyle);

        for (Map.Entry<String, Long> entry : metrics.getTicketsPorPrioridad().entrySet()) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(entry.getKey());
            dataRow.createCell(1).setCellValue(entry.getValue());
        }

        // Autosize
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    // ==================== CSV EXPORT ====================

    /**
     * Exporta métricas del dashboard a formato CSV
     * 
     * @param metrics DTO con métricas del dashboard
     * @return String con el contenido CSV
     */
    public String exportToCSV(ReportMetricsDTO metrics) {
        log.info("Generando export CSV para usuario: {}", metrics.getUsuarioSolicitante());

        try (StringWriter writer = new StringWriter()) {

            // METADATA
            writer.append("REPORTE DE MÉTRICAS - DASHBOARD\n");
            writer.append("\n");
            writer.append("Usuario Solicitante,").append(metrics.getUsuarioSolicitante()).append("\n");
            writer.append("Rol,").append(metrics.getRolUsuario()).append("\n");
            writer.append("Fecha de Generación,").append(metrics.getFechaGeneracion()).append("\n");
            writer.append("\n");

            // TOTALES
            writer.append("TOTALES GENERALES\n");
            writer.append("Métrica,Valor\n");
            writer.append("Total Proyectos,").append(String.valueOf(metrics.getTotalProyectos())).append("\n");
            writer.append("Total Repositorios,").append(String.valueOf(metrics.getTotalRepositorios())).append("\n");
            writer.append("Total Tickets,").append(String.valueOf(metrics.getTotalTickets())).append("\n");
            writer.append("\n");

            // PROYECTOS POR PROPIETARIO
            writer.append("PROYECTOS POR PROPIETARIO\n");
            writer.append("Propietario,Cantidad\n");
            for (Map.Entry<String, Long> entry : metrics.getProyectosPorPropietario().entrySet()) {
                writer.append(escapeCsvValue(entry.getKey())).append(",").append(String.valueOf(entry.getValue())).append("\n");
            }
            writer.append("\n");

            // PROYECTOS POR ESTADO
            writer.append("PROYECTOS POR ESTADO\n");
            writer.append("Estado,Cantidad\n");
            for (Map.Entry<String, Long> entry : metrics.getProyectosPorEstado().entrySet()) {
                writer.append(escapeCsvValue(entry.getKey())).append(",").append(String.valueOf(entry.getValue())).append("\n");
            }
            writer.append("\n");

            // REPOSITORIOS POR TIPO
            writer.append("REPOSITORIOS POR TIPO\n");
            writer.append("Tipo,Cantidad\n");
            for (Map.Entry<String, Long> entry : metrics.getRepositoriosPorTipo().entrySet()) {
                writer.append(escapeCsvValue(entry.getKey())).append(",").append(String.valueOf(entry.getValue())).append("\n");
            }
            writer.append("\n");

            // REPOSITORIOS POR ESTADO
            writer.append("REPOSITORIOS POR ESTADO\n");
            writer.append("Estado,Cantidad\n");
            for (Map.Entry<String, Long> entry : metrics.getRepositoriosPorEstado().entrySet()) {
                writer.append(escapeCsvValue(entry.getKey())).append(",").append(String.valueOf(entry.getValue())).append("\n");
            }
            writer.append("\n");

            // TICKETS POR ESTADO
            writer.append("TICKETS POR ESTADO\n");
            writer.append("Estado,Cantidad\n");
            for (Map.Entry<String, Long> entry : metrics.getTicketsPorEstado().entrySet()) {
                writer.append(escapeCsvValue(entry.getKey())).append(",").append(String.valueOf(entry.getValue())).append("\n");
            }
            writer.append("\n");

            // TICKETS POR PRIORIDAD
            writer.append("TICKETS POR PRIORIDAD\n");
            writer.append("Prioridad,Cantidad\n");
            for (Map.Entry<String, Long> entry : metrics.getTicketsPorPrioridad().entrySet()) {
                writer.append(escapeCsvValue(entry.getKey())).append(",").append(String.valueOf(entry.getValue())).append("\n");
            }

            log.info("CSV generado exitosamente");
            return writer.toString();

        } catch (IOException e) {
            log.error("Error generando CSV: {}", e.getMessage(), e);
            return "";
        }
    }

    // ==================== ESTILOS EXCEL ====================

    /**
     * Crea estilo para headers (fondo azul, texto blanco, negrita)
     */
    private CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Crea estilo para títulos (fondo gris, negrita, grande)
     */
    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * Crea una fila de información clave-valor
     */
    private void crearFilaInfo(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        
        // Estilo para label (negrita)
        CellStyle labelStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        labelStyle.setFont(font);
        labelCell.setCellStyle(labelStyle);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
    }

    /**
     * Escapa valores CSV (comillas, comas, saltos de línea)
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // Si contiene comillas, comas o saltos de línea, envolver en comillas
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // Duplicar comillas internas
            String escaped = value.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        
        return value;
    }
}
