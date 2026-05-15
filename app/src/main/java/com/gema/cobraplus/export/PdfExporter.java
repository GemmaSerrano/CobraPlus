package com.gema.cobraplus.export;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;

import com.gema.cobraplus.models.Report;
import com.gema.cobraplus.models.ReportLine;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PdfExporter {

    // Devuelve el nombre del archivo PDF
    public String getFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return "informe_pdf_" + sdf.format(new Date()) + ".pdf";
    }

    // Exporta el informe a la Uri elegida por el usuario
    public void exportToUri(Context context, Uri uri, Report report, ArrayList<ReportLine> reportLineList) throws Exception {

        PdfDocument pdfDocument = new PdfDocument();

        // Título, texto normal y líneas
        Paint titlePaint = createTitlePaint();
        Paint textPaint = createTextPaint();
        Paint linePaint = createLinePaint();

        // Tamaño aproximado de una hoja A4 en PDF
        int pageWidth = 595;
        int pageHeight = 842;
        int marginLeft = 40;
        int y = 40;
        int pageNumber = 1;

        // Crear primera página
        PdfDocument.Page page = startNewPage(pdfDocument, pageWidth, pageHeight, pageNumber);
        Canvas canvas = page.getCanvas();

        // Cabecera del documento
        y = drawHeader(canvas, report, titlePaint, textPaint, linePaint, marginLeft, pageWidth, y);

        // Dibujar líneas del informe
        for (ReportLine line : reportLineList) {

            // Si no cabe más contenido, crear nueva página
            if (y > pageHeight - 60) {
                pdfDocument.finishPage(page);
                pageNumber++;
                page = startNewPage(pdfDocument, pageWidth, pageHeight, pageNumber);
                canvas = page.getCanvas();
                y = 40;
            }

            if (line.isSectionHeader()) {
                // Línea tipo "Empresa: TMB" o "Fecha: 21/04/2026"
                canvas.drawText(safeText(line.getSectionTitle()), marginLeft, y, titlePaint);
                y += 20;
            } else {
                // Línea normal del informe
                canvas.drawText(safeText(line.getConceptName()), marginLeft, y, textPaint);
                canvas.drawText(formatQuantity(line.getQuantity(), line.getUnitCalculation()), 300, y, textPaint);
                canvas.drawText(String.format(Locale.getDefault(), "%.2f €", line.getSubtotal()), 430, y, textPaint);
                y += 18;
            }
        }

        // Dibujar total
        y += 10;
        canvas.drawLine(marginLeft, y, pageWidth - marginLeft, y, linePaint);
        y += 25;

        double total = calculateTotal(reportLineList);
        canvas.drawText("TOTAL:", marginLeft, y, titlePaint);
        canvas.drawText(String.format(Locale.getDefault(), "%.2f €", total), 430, y, titlePaint);

        // Finalizar página actual
        pdfDocument.finishPage(page);

        // Escribir el PDF en la ubicación elegida
        OutputStream outputStream = context.getContentResolver().openOutputStream(uri);

        if (outputStream == null) {
            pdfDocument.close();
            throw new Exception("No se pudo abrir el archivo PDF");
        }

        pdfDocument.writeTo(outputStream);
        outputStream.close();
        pdfDocument.close();
    }

    // Crea estilo del título
    private Paint createTitlePaint() {
        Paint paint = new Paint();
        paint.setTextSize(18f);
        paint.setFakeBoldText(true);
        return paint;
    }

    // Crea estilo el texto normal
    private Paint createTextPaint() {
        Paint paint = new Paint();
        paint.setTextSize(12f);
        return paint;
    }

    // Crea estilo de líneas separadoras
    private Paint createLinePaint() {
        Paint paint = new Paint();
        paint.setStrokeWidth(1f);
        return paint;
    }

    // Crea una nueva página PDF
    private PdfDocument.Page startNewPage(PdfDocument pdfDocument, int pageWidth, int pageHeight, int pageNumber) {
        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        return pdfDocument.startPage(pageInfo);
    }

    // Dibuja la cabecera del informe y devuelve la nueva posición Y
    private int drawHeader(Canvas canvas, Report report, Paint titlePaint, Paint textPaint,
                           Paint linePaint, int marginLeft, int pageWidth, int y) {

        canvas.drawText("CobraPlus - Informe", marginLeft, y, titlePaint);
        y += 30;

        canvas.drawText("Nombre: " + safeText(report.getName()), marginLeft, y, textPaint);
        y += 20;

        String companyText = (report.getCompany() != null && !report.getCompany().trim().isEmpty())
                ? report.getCompany()
                : "Todas";
        canvas.drawText("Empresa: " + companyText, marginLeft, y, textPaint);
        y += 20;

        String periodText = "Sin periodo";
        if (report.getStartDate() != null && report.getEndDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            periodText = sdf.format(report.getStartDate()) + " - " + sdf.format(report.getEndDate());
        }
        canvas.drawText("Periodo: " + periodText, marginLeft, y, textPaint);
        y += 25;

        canvas.drawLine(marginLeft, y, pageWidth - marginLeft, y, linePaint);
        y += 20;

        canvas.drawText("Concepto", marginLeft, y, titlePaint);
        canvas.drawText("Cantidad", 300, y, titlePaint);
        canvas.drawText("Subtotal", 430, y, titlePaint);
        y += 15;

        canvas.drawLine(marginLeft, y, pageWidth - marginLeft, y, linePaint);
        y += 20;

        return y;
    }

    // Calcula el total sumando solo las líneas normales
    private double calculateTotal(ArrayList<ReportLine> reportLineList) {
        double total = 0;

        for (ReportLine line : reportLineList) {
            if (!line.isSectionHeader()) {
                total += line.getSubtotal();
            }
        }

        return total;
    }

    // Evita null al escribir texto
    private String safeText(String text) {
        return text != null ? text : "";
    }

    // Da formato a la cantidad con su unidad
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