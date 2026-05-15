package com.gema.cobraplus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gema.cobraplus.R;
import com.gema.cobraplus.adapters.ReportAdapter;
import com.gema.cobraplus.models.Report;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ManagementReports extends AppCompatActivity {

    private RecyclerView rvReport;
    private MaterialButton btnNewReport, btnReturnReport;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ArrayList<Report> reportList;
    private ReportAdapter adapter;

    private String highlightReportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.management_report);

        initViews();
        initFirebase();
        readIntentData(getIntent());
        setupListeners();
        loadReports();
    }

    //Inicializa los elementos
    private void initViews() {
        rvReport = findViewById(R.id.rvReports);
        btnNewReport = findViewById(R.id.btnNewReport);
        btnReturnReport = findViewById(R.id.btnReturnReport);
    }

    //Inicializa Firebase
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    //inicializa RecyclerView
    private void initRecycler(boolean isAdmin) {
        reportList = new ArrayList<>();
        adapter = new ReportAdapter(reportList, isAdmin);
        adapter.setHighlightReportId(highlightReportId);

        adapter.setOnReportOptionsListener(new ReportAdapter.OnReportOptionsListener() {
            @Override
            public void onViewReport(Report report) {
                openSeeReport(report);
            }

            @Override
            public void onExportPdf(Report report) {
                exportReportToPdf(report);
            }

            @Override
            public void onExportExcel(Report report) {
                exportReportToExcel(report);
            }

            @Override
            public void onDeleteReport(Report report) {
                showDeleteConfirmation(report);
            }
        });

        rvReport.setLayoutManager(new LinearLayoutManager(this));
        rvReport.setAdapter(adapter);
    }

    //Asocia acciones a los botones
    private void setupListeners() {

        //Botón volver
        btnReturnReport.setOnClickListener(v -> finish());

        //Botón nuevo informe
        btnNewReport.setOnClickListener(v -> openNewReportScreen());
    }

    //Abre la pantalla de nuevo informe según el rol
    private void openNewReportScreen() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) return;

                    Intent intent;

                    if ("admin".equals(document.getString("rol"))) {
                        intent = new Intent(this, NewReportAdmin.class);
                    } else {
                        intent = new Intent(this, NewReport.class);
                    }
                    startActivity(intent);
                })
                .addOnFailureListener(e -> showToast("Error al obtener rol de usuario"));
    }

    //Carga los informes según el tipo de usuario
    private void loadReports() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) return;

                    boolean isAdmin = "admin".equals(document.getString("rol"));

                    // Inicializamos recycler con rol
                    initRecycler(isAdmin);

                    if (isAdmin) {
                        loadBaseReports();
                    } else {
                        loadUserReports();
                    }
                })
                .addOnFailureListener(e -> showToast("Error al cargar usuario"));
    }

    //Carga informes base creados por administrador
    private void loadBaseReports() {
        db.collection("reports")
                .whereEqualTo("baseReport", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    reportList.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Report report = document.toObject(Report.class);

                        if (report != null) {
                            report.setId(document.getId());
                            reportList.add(report);
                        }
                    }

                    //ordena de forma desccendiente por fecha creación
                    java.util.Collections.sort(reportList, (r1, r2) -> {
                        if (r1.getCreatedAt() == null && r2.getCreatedAt() == null) return 0;
                        if (r1.getCreatedAt() == null) return 1;
                        if (r2.getCreatedAt() == null) return -1;
                        return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                    });
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast("Error al cargar informes base"));
    }

    //Carga informes del usuario
    private void loadUserReports() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("reports")
                .whereEqualTo("baseReport", false)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    reportList.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Report report = document.toObject(Report.class);

                        if (report != null) {
                            report.setId(document.getId());
                            reportList.add(report);
                        }
                    }

                    //ORDENAR POR FECHA DESCENDENTE
                    java.util.Collections.sort(reportList, (r1, r2) -> {
                        if (r1.getCreatedAt() == null && r2.getCreatedAt() == null) return 0;
                        if (r1.getCreatedAt() == null) return 1;
                        if (r2.getCreatedAt() == null) return -1;
                        return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                    });

                    adapter.setHighlightReportId(highlightReportId);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast("Error al cargar informes"));
    }

    //Abre la pantalla para ver el informe
    private void openSeeReport(@NonNull Report report) {
        Intent intent = new Intent(this, SeeReport.class);
        intent.putExtra("reportId", report.getId());
        startActivity(intent);

    }

    private void readIntentData(Intent intent) {
        if (intent != null) {
            highlightReportId = intent.getStringExtra("highlightReportId");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readIntentData(intent);
        loadReports();
    }

    //Exporta informe a PDF
    private void exportReportToPdf(Report report) {
        Intent intent = new Intent(this, SeeReport.class);
        intent.putExtra("reportId", report.getId());
        intent.putExtra("exportPdf", true);
        startActivity(intent);
    }

    //Exporta informe a Excel
    private void exportReportToExcel(Report report) {
        Intent intent = new Intent(this, SeeReport.class);
        intent.putExtra("reportId", report.getId());
        intent.putExtra("exportExcel", true);
        startActivity(intent);
    }


    //Elimina el informe seleccionado de Firestore
    private void deleteReport(Report report) {
        db.collection("reports")
                .document(report.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    showToast("Informe eliminado correctamente");
                    loadReports();
                })
                .addOnFailureListener(e -> showToast("Error al eliminar informe"));
    }


    //Muestra confirmación antes de eliminar un informe
    private void showDeleteConfirmation(Report report) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar informe")
                .setMessage("¿Seguro que quieres eliminar este informe?")
                .setPositiveButton("Sí", (dialog, which) -> deleteReport(report))
                .setNegativeButton("No", null)
                .show();
    }


    //Recarga los informes al volver a la pantalla
    @Override
    protected void onResume() {
        super.onResume();
        loadReports();
    }

    //Muestra mensaje
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
