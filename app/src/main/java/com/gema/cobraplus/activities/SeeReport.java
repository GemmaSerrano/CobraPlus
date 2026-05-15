package com.gema.cobraplus.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.gema.cobraplus.R;
import com.gema.cobraplus.export.ExcelExporter;
import com.gema.cobraplus.export.PdfExporter;
import com.gema.cobraplus.models.Concept;
import com.gema.cobraplus.models.Record;
import com.gema.cobraplus.models.Report;
import com.gema.cobraplus.models.ReportLine;
import com.gema.cobraplus.helpers.ReportCalculator;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class SeeReport extends AppCompatActivity {

    private TextView txtReportNameValue, txtCompanyValue, txtPeriodValue, txtTotalValue;
    private LinearLayout layoutReportLines;
    private MaterialButton btnExportPdf, btnExportExcel, btnDeleteSeeReport, btnReturnSeeReport;

    private FirebaseFirestore db;

    private String reportId;
    private Report currentReport;

    private ArrayList<Concept> conceptList;
    private ArrayList<Record> recordList;
    private ArrayList<ReportLine> reportLineList;

    private boolean exportPdfDirectly;
    private boolean exportExcelDirectly;

    private ActivityResultLauncher<Intent> createPdfLauncher;
    private ActivityResultLauncher<Intent> createExcelLauncher;

    private PdfExporter pdfExporter;
    private ExcelExporter excelExporter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.see_report);

        exportPdfDirectly = getIntent().getBooleanExtra("exportPdf", false);
        exportExcelDirectly = getIntent().getBooleanExtra("exportExcel", false);

        initViews();
        initFirebase();
        initLists();
        initExporters();
        setupLaunchers();
        setupListeners();
        loadReport();
    }

    // Inicializa elementos
    private void initViews() {
        txtReportNameValue = findViewById(R.id.txtReportNameValue);
        txtCompanyValue = findViewById(R.id.txtCompanyValue);
        txtPeriodValue = findViewById(R.id.txtPeriodValue);
        txtTotalValue = findViewById(R.id.txtTotalValue);

        layoutReportLines = findViewById(R.id.layoutReportLines);

        btnExportPdf = findViewById(R.id.btnExportPdf);
        btnExportExcel = findViewById(R.id.btnExportExcel);
        btnDeleteSeeReport = findViewById(R.id.btnDeleteSeeReport);
        btnReturnSeeReport = findViewById(R.id.btnReturnSeeReport);
    }

    // Inicializa Firebase
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    // Inicializa listas
    private void initLists() {
        conceptList = new ArrayList<>();
        recordList = new ArrayList<>();
        reportLineList = new ArrayList<>();
    }

    // Inicializa exportadores
    private void initExporters() {
        pdfExporter = new PdfExporter();
        excelExporter = new ExcelExporter();
    }

    // Launchers para selector del sistema
    private void setupLaunchers() {
        createPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();

                        if (uri != null) {
                            try {
                                pdfExporter.exportToUri(this, uri, currentReport, reportLineList);
                                showToast("PDF guardado correctamente");
                            } catch (Exception e) {
                                showToast("Error al generar PDF");
                            }
                        } else {
                            showToast("No se seleccionó ubicación");
                        }
                    }
                }
        );

        createExcelLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();

                        if (uri != null) {
                            try {
                                excelExporter.exportToUri(this, uri, currentReport, reportLineList);
                                showToast("Excel guardado correctamente");
                            } catch (Exception e) {
                                showToast("Error al generar Excel");
                            }
                        } else {
                            showToast("No se seleccionó ubicación");
                        }
                    }
                }
        );
    }

    // Asocia acciones a botones
    private void setupListeners() {
        btnReturnSeeReport.setOnClickListener(v -> finish());

        btnDeleteSeeReport.setOnClickListener(v -> showDeleteConfirmation());

        btnExportPdf.setOnClickListener(v -> exportToPdf());

        btnExportExcel.setOnClickListener(v -> exportToExcel());
    }

    // Carga el informe
    private void loadReport() {
        reportId = getIntent().getStringExtra("reportId");

        if (reportId == null || reportId.trim().isEmpty()) {
            showToast("Informe no válido");
            finish();
            return;
        }

        db.collection("reports")
                .document(reportId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        showToast("Informe no encontrado");
                        finish();
                        return;
                    }

                    currentReport = document.toObject(Report.class);

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    if (!currentReport.isBaseReport()
                            && currentReport.getUserId() != null
                            && !currentReport.getUserId().equals(uid)) {
                        showToast("No tienes permiso para ver este informe");
                        finish();
                        return;
                    }
                    if (currentReport == null) {
                        showToast("Error al cargar informe");
                        finish();
                        return;
                    }

                    currentReport.setId(document.getId());
                    showReportHeader();
                    loadConcepts();
                })
                .addOnFailureListener(e -> {
                    showToast("Error al cargar informe");
                    finish();
                });
    }

    // Muestra cabecera
    private void showReportHeader() {
        txtReportNameValue.setText(currentReport.getName());

        if (currentReport.getCompany() != null && !currentReport.getCompany().trim().isEmpty()) {
            txtCompanyValue.setText(currentReport.getCompany());
        } else {
            txtCompanyValue.setText("Todas");
        }

        if (currentReport.getStartDate() != null && currentReport.getEndDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String period = sdf.format(currentReport.getStartDate()) + " - " + sdf.format(currentReport.getEndDate());
            txtPeriodValue.setText(period);
        } else {
            txtPeriodValue.setText("Sin periodo");
        }
    }

    // Carga conceptos
    private void loadConcepts() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("concepts")
                .whereEqualTo("baseConcept", false)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    conceptList.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Concept concept = document.toObject(Concept.class);

                        if (concept != null) {
                            concept.setId(document.getId());
                            conceptList.add(concept);
                        }
                    }

                    loadRecords();
                })
                .addOnFailureListener(e -> showToast("Error al cargar conceptos"));
    }

    // Carga registros
    private void loadRecords() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("records")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recordList.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Record record = document.toObject(Record.class);

                        if (record != null) {
                            record.setId(document.getId());
                            recordList.add(record);
                        }
                    }

                    generateReportLines();
                })
                .addOnFailureListener(e -> showToast("Error al cargar registros"));
    }

    // Genera líneas
    private void generateReportLines() {
        ReportCalculator calculator = new ReportCalculator(currentReport, recordList, conceptList);

        reportLineList.clear();
        reportLineList.addAll(calculator.generateLines());

        showReportLines();
        showTotal(calculator.calculateTotal(reportLineList));

        if (exportPdfDirectly) {
            exportToPdf();
            exportPdfDirectly = false;
        }

        if (exportExcelDirectly) {
            exportToExcel();
            exportExcelDirectly = false;
        }
    }

    // Muestra líneas
    private void showReportLines() {
        layoutReportLines.removeAllViews();

        for (ReportLine line : reportLineList) {
            if (line.isSectionHeader()) {
                addSectionHeader(line.getSectionTitle());
            } else {
                addLineView(line);
            }
        }
    }

    // Añade cabecera de sección
    private void addSectionHeader(String title) {
        View section = LayoutInflater.from(this)
                .inflate(R.layout.item_report_section, layoutReportLines, false);

        TextView txtSectionTitle = section.findViewById(R.id.txtSectionTitle);
        txtSectionTitle.setText(title);

        layoutReportLines.addView(section);
    }

    // Añade línea normal
    private void addLineView(ReportLine line) {
        View row = LayoutInflater.from(this)
                .inflate(R.layout.item_report_line, layoutReportLines, false);

        TextView txtLineConcept = row.findViewById(R.id.txtLineConcept);
        TextView txtLineQuantity = row.findViewById(R.id.txtLineQuantity);
        TextView txtLineSubtotal = row.findViewById(R.id.txtLineSubtotal);

        txtLineConcept.setText(line.getConceptName());
        txtLineQuantity.setText(formatQuantity(line.getQuantity(), line.getUnitCalculation()));
        txtLineSubtotal.setText(String.format(Locale.getDefault(), "%.2f €", line.getSubtotal()));

        layoutReportLines.addView(row);
    }

    // Muestra total
    private void showTotal(double total) {
        txtTotalValue.setText(String.format(Locale.getDefault(), "%.2f €", total));
    }

    // Exporta PDF usando selector
    private void exportToPdf() {
        if (reportLineList.isEmpty()) {
            showToast("No hay datos para exportar");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, pdfExporter.getFileName());

        createPdfLauncher.launch(intent);
    }

    // Exporta Excel usando selector
    private void exportToExcel() {
        if (reportLineList.isEmpty()) {
            showToast("No hay datos para exportar");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE, excelExporter.getFileName());

        createExcelLauncher.launch(intent);
    }

    // Formatea cantidad
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

    // Elimina informe
    private void deleteReport() {
        db.collection("reports")
                .document(reportId)
                .delete()
                .addOnSuccessListener(unused -> {
                    showToast("Informe eliminado correctamente");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Error al eliminar informe"));
    }

    // Confirmación de borrado
    private void showDeleteConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar informe")
                .setMessage("¿Seguro que quieres eliminar este informe?")
                .setPositiveButton("Sí", (dialog, which) -> deleteReport())
                .setNegativeButton("No", null)
                .show();
    }

    // Muestra mensaje
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}