package com.gema.cobraplus.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gema.cobraplus.R;
import com.gema.cobraplus.models.Concept;
import com.gema.cobraplus.models.Report;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NewReport extends AppCompatActivity {

    private Spinner spReportName, spReportCompany, spGroupBy;
    private EditText etStartDate, etEndDate;
   private MaterialButton btnGenerate, btnCancel;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ArrayList<Report> baseReportList;
    private ArrayList<String> baseReportNameList;
    private ArrayList<String> companyList;
    private ArrayList<String> groupByList;

    private Date selectedStartDate;
    private Date selectedEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_report);

        initViews();
        initFirebase();
        initLists();
        setupListeners();
        loadBaseReports();
        loadCompanies();
        loadGroupByOptions();
    }

    // Inicializa los elementos
    private void initViews() {
        spReportName = findViewById(R.id.spReportName);
        spReportCompany = findViewById(R.id.spReportCompany);
        spGroupBy = findViewById(R.id.spGroupBy);

        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);

        btnGenerate = findViewById(R.id.btnGenerate);
        btnCancel = findViewById(R.id.btnCancel);

        // Evita escritura manual y permite usar calendario
        etStartDate.setKeyListener(null);
        etEndDate.setKeyListener(null);

    }

    // Inicializa Firebase
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    // Inicializa listas
    private void initLists() {
        baseReportList = new ArrayList<>();
        baseReportNameList = new ArrayList<>();
        companyList = new ArrayList<>();
        groupByList = new ArrayList<>();
    }

    // Asocia acciones
    private void setupListeners() {
        etStartDate.setOnClickListener(v -> openDatePicker(etStartDate, true));
        etEndDate.setOnClickListener(v -> openDatePicker(etEndDate, false));

        btnGenerate.setOnClickListener(v -> saveReport());
        btnCancel.setOnClickListener(v -> finish());
    }

    // Abre calendario para seleccionar fecha
    private void openDatePicker(EditText targetEditText, boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0);
                    selectedCalendar.set(Calendar.MILLISECOND, 0);

                    Date selectedDate = selectedCalendar.getTime();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    targetEditText.setText(sdf.format(selectedDate));

                    if (isStartDate) {
                        selectedStartDate = selectedDate;
                    } else {
                        selectedEndDate = selectedDate;
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    // Carga informes base creados por el administrador
    private void loadBaseReports() {
        db.collection("reports")
                .whereEqualTo("baseReport", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    baseReportList.clear();
                    baseReportNameList.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Report report = document.toObject(Report.class);

                        if (report != null) {
                            report.setId(document.getId());
                            baseReportList.add(report);
                            baseReportNameList.add(report.getName());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            baseReportNameList
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spReportName.setAdapter(adapter);
                })
                .addOnFailureListener(e -> showToast("Error al cargar informes base"));
    }

    // Carga empresas desde los conceptos del usuario
    private void loadCompanies() {
        if (mAuth.getCurrentUser() == null) {
            showToast("Usuario no autenticado");
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("concepts")
                .whereEqualTo("baseConcept", false)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    companyList.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Concept concept = document.toObject(Concept.class);

                        if (concept != null && concept.getCompany() != null && !concept.getCompany().trim().isEmpty()) {

                            // Evita duplicados manualmente
                            if (!companyList.contains(concept.getCompany())) {
                                companyList.add(concept.getCompany());
                            }
                        }
                    }

                    // Añadir "Todas" al principio
                    companyList.add(0, "Todas");

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            companyList
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spReportCompany.setAdapter(adapter);
                })
                .addOnFailureListener(e -> showToast("Error al cargar empresas"));
    }

    // Carga opciones de agrupación
    private void loadGroupByOptions() {
        groupByList.clear();
        groupByList.add("Sin agrupar");
        groupByList.add("Concepto");
        groupByList.add("Fecha");
        groupByList.add("Empresa");


        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                groupByList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGroupBy.setAdapter(adapter);
        spGroupBy.setSelection(1);  //default Concepto
    }

    // Guarda informe del usuario
    private void saveReport() {
        if (baseReportList.isEmpty()) {
            showToast("No hay informes base disponibles");
            return;
        }

        if (companyList.isEmpty()) {
            showToast("No hay empresas disponibles");
            return;
        }

        if (selectedStartDate == null) {
            etStartDate.setError("Selecciona la fecha de inicio");//muestra alerta
            etStartDate.requestFocus();
            showToast("Selecciona la fecha de inicio");//muestra mensaje
            return;
        }


        if (selectedEndDate == null) {
            etEndDate.setError("Selecciona la fecha de fin");//muestra alerta
            etEndDate.requestFocus();
            showToast("Selecciona la fecha de fin"); //muestra mensaje
            return;
        }

        if (selectedEndDate.before(selectedStartDate)) {
            etEndDate.setError("La fecha fin no puede ser anterior a la fecha inicio");
            etEndDate.requestFocus();
            showToast("La fecha fin no puede ser anterior a la fecha inicio");
            return;
        }

        Report selectedBaseReport = baseReportList.get(spReportName.getSelectedItemPosition());
        String selectedCompany = companyList.get(spReportCompany.getSelectedItemPosition());
        String selectedGroupBy = groupByList.get(spGroupBy.getSelectedItemPosition());

        android.content.Intent intent = new android.content.Intent(this, ConfirmReport.class);
        intent.putExtra("reportName", selectedBaseReport.getName());
        intent.putExtra("company", selectedCompany);
        intent.putExtra("groupBy", selectedGroupBy);
        intent.putExtra("baseReportId", selectedBaseReport.getId());
        intent.putExtra("startDate", selectedStartDate.getTime());
        intent.putExtra("endDate", selectedEndDate.getTime());

        startActivity(intent);
    }


    // Muestra mensaje
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
