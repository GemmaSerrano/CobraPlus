package com.gema.cobraplus.export;

import android.content.Context;
import android.net.Uri;

import com.gema.cobraplus.models.Report;
import com.gema.cobraplus.models.ReportLine;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ExcelExporter {

    // Nombre sugerido del archivo
    public String getFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return "informe_excel_" + sdf.format(new Date()) + ".xlsx";
    }

    // Exporta el informe a la Uri elegida por el usuario
    public void exportToUri(Context context, Uri uri, Report report, ArrayList<ReportLine> reportLineList) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Informe");

        CellStyle titleStyle = createBoldStyle(workbook, 14);
        CellStyle headerStyle = createBoldStyle(workbook, 11);
        CellStyle sectionStyle = createBoldStyle(workbook, 11);
        CellStyle totalStyle = createBoldStyle(workbook, 11);

        int rowIndex = 0;

        // Título
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("CobraPlus - Informe");
        row.getCell(0).setCellStyle(titleStyle);

        // Nombre
        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Nombre");
        row.getCell(0).setCellStyle(headerStyle);
        row.createCell(1).setCellValue(report.getName() != null ? report.getName() : "");

        // Empresa
        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Empresa");
        row.getCell(0).setCellStyle(headerStyle);
        row.createCell(1).setCellValue(
                report.getCompany() != null && !report.getCompany().trim().isEmpty()
                        ? report.getCompany()
                        : "Todas"
        );

        // Periodo
        String periodText = "Sin periodo";
        if (report.getStartDate() != null && report.getEndDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            periodText = sdf.format(report.getStartDate()) + " - " + sdf.format(report.getEndDate());
        }

        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Periodo");
        row.getCell(0).setCellStyle(headerStyle);
        row.createCell(1).setCellValue(periodText);


        // Línea en blanco
        rowIndex++;

        // Cabecera tabla
        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Concepto");
        row.createCell(1).setCellValue("Cantidad");
        row.createCell(2).setCellValue("Subtotal");

        row.getCell(0).setCellStyle(headerStyle);
        row.getCell(1).setCellStyle(headerStyle);
        row.getCell(2).setCellStyle(headerStyle);

        double total = 0;

        // Líneas del informe
        for (ReportLine line : reportLineList) {
            row = sheet.createRow(rowIndex++);

            if (line.isSectionHeader()) {
                row.createCell(0).setCellValue(line.getSectionTitle() != null ? line.getSectionTitle() : "");
                row.getCell(0).setCellStyle(sectionStyle);
            } else {
                row.createCell(0).setCellValue(line.getConceptName() != null ? line.getConceptName() : "");
                row.createCell(1).setCellValue(formatQuantity(line.getQuantity(), line.getUnitCalculation()));
                row.createCell(2).setCellValue(line.getSubtotal());

                total += line.getSubtotal();
            }
        }

        // Línea en blanco
        rowIndex++;

        // Total
        row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue("TOTAL");
        Cell totalCell = row.createCell(2);
        totalCell.setCellValue(total + " €");

        row.getCell(0).setCellStyle(totalStyle);
        row.getCell(2).setCellStyle(totalStyle);

        // Ajustar ancho de columnas
        sheet.setColumnWidth(0, 7000);
        sheet.setColumnWidth(1, 5000);
        sheet.setColumnWidth(2, 5000);

        OutputStream outputStream = context.getContentResolver().openOutputStream(uri);

        if (outputStream == null) {
            workbook.close();
            throw new Exception("No se pudo abrir el archivo Excel");
        }

        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
        workbook.close();
    }

    //Para crear estilos en títulos y cabecera
     private CellStyle createBoldStyle(Workbook workbook, int fontSize) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) fontSize);

        style.setFont(font);
        return style;
    }


    // Formatea la cantidad con la unidad
    private String formatQuantity(double quantity, String unitCalculation) {
        String quantityText;

        if (quantity == (int) quantity) {
            quantityText = String.valueOf((int) quantity);
        } else {
            quantityText = String.format(Locale.getDefault(), "%.2f", quantity);
        }

        if (unitCalculation != null && !unitCalculation.trim().isEmpty()) {
            return quantityText + " " + unitCalculation;
        }

        return quantityText;
    }
}