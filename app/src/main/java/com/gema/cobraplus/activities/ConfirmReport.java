package com.gema.cobraplus.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gema.cobraplus.R;
import com.gema.cobraplus.adapters.ConfirmReportAdapter;
import com.gema.cobraplus.models.Concept;
import com.gema.cobraplus.models.ConfirmReportItem;
import com.gema.cobraplus.models.Record;
import com.gema.cobraplus.models.Report;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ConfirmReport extends AppCompatActivity {

    private RecyclerView rvListRecords;
    private MaterialButton btnAcceptReport, btnReturnReport;

    private FirebaseFirestore db;

    private ArrayList<Record> recordList;
    private ArrayList<Concept> conceptList;
    private ArrayList<ConfirmReportItem> confirmItemList;
    private ConfirmReportAdapter adapter;

    private String reportName;
    private String company;
    private String groupBy;
    private String baseReportId;
    private Date startDate;
    private Date endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_report);

        initViews();
        initFirebase();
        initLists();
        getIntentData();
        initRecycler();
        setupListeners();
        loadConcepts();
    }

    // Inicializa elementos
    private void initViews() {
        rvListRecords = findViewById(R.id.rvListRecords);
        btnAcceptReport = findViewById(R.id.btnAcceptReport);
        btnReturnReport = findViewById(R.id.btnReturnReport);
    }

    // Inicializa Firebase
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    // Inicializa listas
    private void initLists() {
        recordList = new ArrayList<>();
        conceptList = new ArrayList<>();
        confirmItemList = new ArrayList<>();
    }

    // Recoge datos recibidos desde NewReport
    private void getIntentData() {
        reportName = getIntent().getStringExtra("reportName");
        company = getIntent().getStringExtra("company");
        groupBy = getIntent().getStringExtra("groupBy");
        baseReportId = getIntent().getStringExtra("baseReportId");

        long startMillis = getIntent().getLongExtra("startDate", -1);
        long endMillis = getIntent().getLongExtra("endDate", -1);

        if (startMillis != -1) startDate = new Date(startMillis);
        if (endMillis != -1) endDate = new Date(endMillis);
    }

    // Inicializa RecyclerView
    private void initRecycler() {
        adapter = new ConfirmReportAdapter(confirmItemList);

        adapter.setOnEditClickListener(item -> {
            android.content.Intent intent = new android.content.Intent(this, EditRecord.class);
            intent.putExtra("recordId", item.getRecordId());
            startActivity(intent);
        });

        rvListRecords.setLayoutManager(new LinearLayoutManager(this));
        rvListRecords.setAdapter(adapter);
    }

    // Asocia acciones
    private void setupListeners() {
        btnReturnReport.setOnClickListener(v -> finish());
        btnAcceptReport.setOnClickListener(v -> saveReport());
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

                    loadConfirmItems();
                })
                .addOnFailureListener(e -> showToast("Error al cargar registros"));
    }

    //Recarga después de editar
    @Override
    protected void onResume() {
        super.onResume();

        if (!conceptList.isEmpty()) {
            loadRecords();
        }
    }
    // Carga los registros que entran en el informe
    private void loadConfirmItems() {
        confirmItemList.clear();

        for (Record record : recordList) {
            if (!recordBelongsToReport(record)) continue;

            Concept concept = findConceptById(record.getConceptId());
            if (concept == null) continue;

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            String dateText = record.getDate() != null ? sdf.format(record.getDate()) : "";
            String companyText = concept.getCompany() != null ? concept.getCompany() : "";
            String conceptText = concept.getName() != null ? concept.getName() : "";
            String quantityText = formatQuantity(record.getQuantity(), concept.getUnitCalculation());
            String commentaryText = record.getDescription() != null ? record.getDescription() : "";

            ConfirmReportItem item = new ConfirmReportItem(
                    record.getId(),
                    dateText,
                    companyText,
                    conceptText,
                    quantityText,
                    commentaryText
            );

            confirmItemList.add(item);
        }
        // Ordenar por fecha ascendente: más antigua primero
        java.util.Collections.sort(confirmItemList, (i1, i2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date d1 = sdf.parse(i1.getDate());
                Date d2 = sdf.parse(i2.getDate());

                if (d1 == null && d2 == null) return 0;
                if (d1 == null) return 1;
                if (d2 == null) return -1;

                return d1.compareTo(d2);
            } catch (Exception e) {
                return 0;
            }
        });

        adapter.notifyDataSetChanged();
    }

    // Comprueba si un registro pertenece al informe
    private boolean recordBelongsToReport(Record record) {
        if (record == null || record.getDate() == null) return false;
        if (startDate == null || endDate == null) return false;

        boolean insidePeriod =
                !record.getDate().before(startDate) &&
                        !record.getDate().after(endDate);

        if (!insidePeriod) return false;

        if (company != null && !company.trim().isEmpty() && !company.equals("Todas")) {
            Concept concept = findConceptById(record.getConceptId());

            if (concept == null || concept.getCompany() == null) return false;

            return company.equals(concept.getCompany());
        }

        return true;
    }

    // Busca concepto por id
    private Concept findConceptById(String conceptId) {
        for (Concept concept : conceptList) {
            if (concept.getId() != null && concept.getId().equals(conceptId)) {
                return concept;
            }
        }
        return null;
    }

    // Guarda el informe definitivo
    private void saveReport() {
        String documentId = db.collection("reports").document().getId();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Report report = new Report();
        report.setId(documentId);
        report.setName(reportName);
        report.setCompany(company != null && company.equals("Todas") ? "" : company);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGroupBy(groupBy);
        report.setBaseReport(false);
        report.setBaseReportId(baseReportId != null ? baseReportId : "");
        report.setUserId(uid);
        report.setCreatedAt(new Date());

        db.collection("reports")
                .document(documentId)
                .set(report)
                .addOnSuccessListener(unused -> {
                    showToast("Informe generado correctamente");

                    android.content.Intent intent = new android.content.Intent(this, ManagementReports.class);
                    intent.putExtra("highlightReportId", documentId);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> showToast("Error al guardar informe"));
    }

    // Formatea cantidad
    private String formatQuantity(Double quantity, String unitCalculation) {
        if (quantity == null) return "";

        String quantityText;

        if (quantity == quantity.intValue()) {
            quantityText = String.valueOf(quantity.intValue());
        } else {
            quantityText = String.format(Locale.getDefault(), "%.2f", quantity);
        }

        if (unitCalculation != null && !unitCalculation.trim().isEmpty()) {
            return quantityText + " " + unitCalculation;
        }

        return quantityText;
    }

    // Muestra mensaje
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
