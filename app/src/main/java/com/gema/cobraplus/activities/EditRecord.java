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
import com.gema.cobraplus.models.Record;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditRecord extends AppCompatActivity {

    private Spinner spCompanyEdit, spConceptEdit;
    private EditText etDateEdit, etQuantityEdit, etCommentsEdit;
    private MaterialButton btnSaveEditRecord, btnCancelEditRecord;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ArrayList<Concept> conceptList;
    private ArrayList<Concept> filteredConceptList;
    private ArrayList<String> companyList;
    private ArrayList<String> conceptNameList;

    private String recordId;
    private Record currentRecord;
    private Date selectedDate;
    private String currentConceptId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_record);

        initViews();
        initFirebase();
        initLists();
        getIntentData();
        setupListeners();
        loadRecord();
    }

    private void initViews() {
        spCompanyEdit = findViewById(R.id.spCompanyEdit);
        spConceptEdit = findViewById(R.id.spConceptEdit);

        etDateEdit = findViewById(R.id.etDateEdit);
        etQuantityEdit = findViewById(R.id.etQuantityEdit);
        etCommentsEdit = findViewById(R.id.etCommentsEdit);

        btnSaveEditRecord = findViewById(R.id.btnSaveEditRecord);
        btnCancelEditRecord = findViewById(R.id.btnCancelEditRecord);

        etDateEdit.setKeyListener(null);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void initLists() {
        conceptList = new ArrayList<>();
        filteredConceptList = new ArrayList<>();
        companyList = new ArrayList<>();
        conceptNameList = new ArrayList<>();
    }

    private void getIntentData() {
        recordId = getIntent().getStringExtra("recordId");
    }

    private void setupListeners() {
        etDateEdit.setOnClickListener(v -> openDatePicker());

        spCompanyEdit.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (companyList.isEmpty()) return;

                String selectedCompany = companyList.get(position);
                loadConceptsByCompany(selectedCompany);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        btnSaveEditRecord.setOnClickListener(v -> updateRecord());
        btnCancelEditRecord.setOnClickListener(v -> finish());
    }

    private void loadRecord() {
        if (recordId == null || recordId.trim().isEmpty()) {
            showToast("Registro no válido");
            finish();
            return;
        }

        db.collection("records")
                .document(recordId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        showToast("Registro no encontrado");
                        finish();
                        return;
                    }

                    currentRecord = document.toObject(Record.class);

                    String uid = mAuth.getCurrentUser().getUid();

                    if (currentRecord.getUserId() == null || !currentRecord.getUserId().equals(uid)) {
                        showToast("No tienes permiso para modificar este registro");
                        finish();
                        return;
                    }

                    if (currentRecord == null) {
                        showToast("Error al cargar registro");
                        finish();
                        return;
                    }

                    currentRecord.setId(document.getId());
                    currentConceptId = currentRecord.getConceptId();
                    selectedDate = currentRecord.getDate();

                    loadConcepts();
                })
                .addOnFailureListener(e -> {
                    showToast("Error al cargar registro");
                    finish();
                });
    }

    private void loadConcepts() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("concepts")
                .whereEqualTo("baseConcept", false)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    conceptList.clear();
                    companyList.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Concept concept = document.toObject(Concept.class);

                        if (concept != null) {
                            concept.setId(document.getId());
                            conceptList.add(concept);

                            if (concept.getCompany() != null &&
                                    !concept.getCompany().trim().isEmpty() &&
                                    !companyList.contains(concept.getCompany())) {
                                companyList.add(concept.getCompany());
                            }
                        }
                    }

                    loadCompaniesSpinner();
                    fillRecordData();
                })
                .addOnFailureListener(e -> showToast("Error al cargar conceptos"));
    }

    private void loadCompaniesSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                companyList
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCompanyEdit.setAdapter(adapter);
    }

    private void fillRecordData() {
        if (currentRecord == null) return;

        if (selectedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etDateEdit.setText(sdf.format(selectedDate));
        }

        etQuantityEdit.setText(String.valueOf(currentRecord.getQuantity() != null ? currentRecord.getQuantity() : 0));
        etCommentsEdit.setText(currentRecord.getDescription() != null ? currentRecord.getDescription() : "");

        Concept currentConcept = findConceptById(currentConceptId);
        if (currentConcept == null) return;

        int companyPosition = companyList.indexOf(currentConcept.getCompany());
        if (companyPosition >= 0) {
            spCompanyEdit.setSelection(companyPosition);
        }

        loadConceptsByCompany(currentConcept.getCompany());

        int conceptPosition = getConceptPositionById(currentConceptId);
        if (conceptPosition >= 0) {
            spConceptEdit.setSelection(conceptPosition);
        }
    }

    private void loadConceptsByCompany(String company) {
        filteredConceptList.clear();
        conceptNameList.clear();

        for (Concept concept : conceptList) {
            if (company.equals(concept.getCompany())) {
                filteredConceptList.add(concept);
                conceptNameList.add(concept.getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                conceptNameList
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spConceptEdit.setAdapter(adapter);

        int conceptPosition = getConceptPositionById(currentConceptId);
        if (conceptPosition >= 0) {
            spConceptEdit.setSelection(conceptPosition);
        }
    }

    private void updateRecord() {
        if (currentRecord == null) {
            showToast("Registro no disponible");
            return;
        }

        if (selectedDate == null) {
            etDateEdit.setError("Selecciona una fecha");
            etDateEdit.requestFocus();
            return;
        }

        if (isEmptyField(etQuantityEdit, "Introduce una cantidad")) return;

        double quantity;

        try {
            quantity = Double.parseDouble(etQuantityEdit.getText().toString().trim());
        } catch (NumberFormatException e) {
            etQuantityEdit.setError("Introduce una cantidad válida");
            etQuantityEdit.requestFocus();
            return;
        }

        if (filteredConceptList.isEmpty()) {
            showToast("No hay conceptos disponibles");
            return;
        }

        Concept selectedConcept = filteredConceptList.get(spConceptEdit.getSelectedItemPosition());

        currentRecord.setConceptId(selectedConcept.getId());
        currentRecord.setDate(selectedDate);
        currentRecord.setQuantity(quantity);
        currentRecord.setDescription(etCommentsEdit.getText().toString().trim());

        db.collection("records")
                .document(currentRecord.getId())
                .set(currentRecord)
                .addOnSuccessListener(unused -> {
                    showToast("Registro actualizado correctamente");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Error al actualizar el registro"));
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();

        if (selectedDate != null) {
            calendar.setTime(selectedDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0);
                    selectedCalendar.set(Calendar.MILLISECOND, 0);

                    selectedDate = selectedCalendar.getTime();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etDateEdit.setText(sdf.format(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private Concept findConceptById(String conceptId) {
        for (Concept concept : conceptList) {
            if (concept.getId() != null && concept.getId().equals(conceptId)) {
                return concept;
            }
        }
        return null;
    }

    private int getConceptPositionById(String conceptId) {
        for (int i = 0; i < filteredConceptList.size(); i++) {
            if (filteredConceptList.get(i).getId() != null &&
                    filteredConceptList.get(i).getId().equals(conceptId)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isEmptyField(EditText editText, String message) {
        if (editText.getText().toString().trim().isEmpty()) {
            editText.setError(message);
            editText.requestFocus();
            return true;
        }
        return false;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}