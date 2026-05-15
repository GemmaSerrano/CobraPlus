package com.gema.cobraplus.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gema.cobraplus.R;
import com.gema.cobraplus.models.Concept;

import com.gema.cobraplus.models.Record;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RecordActivity extends AppCompatActivity {

    private Spinner spCompany, spConcept;
    private EditText etDate, etQuantity, etComments;
    private MaterialButton btnSaveRecord, btnCancelRecord;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ArrayList<Concept> conceptList;
    private ArrayList<Concept> filteredConceptList;
    private ArrayList<String> companyList;
    private ArrayList<String> conceptNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_activity);

        initViews();
        initFirebase();
        initLists();
        setupListeners();
        loadUserConcepts();

    }

    //Inicializa los elementos
    private void initViews() {
        spCompany = findViewById(R.id.spCompany);
        spConcept = findViewById(R.id.spConcept);

        etDate = findViewById(R.id.etDate);
        etQuantity = findViewById(R.id.etQuantity);
        etComments = findViewById(R.id.etComments);

        btnSaveRecord = findViewById(R.id.btnSaveRecord);
        btnCancelRecord = findViewById(R.id.btnCancelRecord);
    }

    //Inicializa Firebase
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    //Inicializa las listas
    private void initLists() {
        conceptList = new ArrayList<>();
        filteredConceptList = new ArrayList<>();
        companyList = new ArrayList<>();
        conceptNameList = new ArrayList<>();
    }
    //Asocia acciones a controles
    private void setupListeners() {

        //Abre calendario al pulsar sobre la fecha
        etDate.setOnClickListener(v -> openDatePicker());

        //Carga los conceptos según la empresa seleccionada
        spCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (companyList.isEmpty()) return;

                String selectedCompany = companyList.get(position);
                loadConceptsByCompany(selectedCompany);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Guarda el registro
        btnSaveRecord.setOnClickListener(v -> saveRecord());

        //Vuelve a la pantalla anterior
        btnCancelRecord.setOnClickListener(v -> finish());
    }

    //Abre calendario para seleccionar fecha
    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etDate.setText(sdf.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    //Carga los conceptos del usuario desde Firestore
    private void loadUserConcepts() {
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
                    conceptList.clear();
                    companyList.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Concept concept = document.toObject(Concept.class);

                        if (concept != null) {
                            concept.setId(document.getId());
                            conceptList.add(concept);

                            if (concept.getCompany() != null && !concept.getCompany().trim().isEmpty()) {
                                if (!companyList.contains(concept.getCompany())) {
                                    companyList.add(concept.getCompany());
                                }
                            }
                        }
                    }

                    loadCompaniesSpinner();
                })
                .addOnFailureListener(e -> showToast("Error al cargar conceptos"));
    }


    //Carga empresas en el spinner
    private void loadCompaniesSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                companyList
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCompany.setAdapter(adapter);

        if (!companyList.isEmpty()) {
            loadConceptsByCompany(companyList.get(0));
        }
    }

    //Filtra conceptos por empresa y los carga en el spinner
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
        spConcept.setAdapter(adapter);
    }

    //Guarda el registro en Firestore
    private void saveRecord() {
        if (mAuth.getCurrentUser() == null) {
            showToast("Usuario no autenticado");
            return;
        }

        if (companyList.isEmpty()) {
            showToast("No hay empresas disponibles");
            return;
        }

        if (filteredConceptList.isEmpty()) {
            showToast("No hay conceptos disponibles");
            return;
        }

        if (isEmptyField(etDate, "Selecciona una fecha")) return;
        if (isEmptyField(etQuantity, "Introduce una cantidad")) return;

        String dateText = etDate.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String comments = etComments.getText().toString().trim();

        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            etQuantity.setError("Introduce una cantidad válida");
            etQuantity.requestFocus();
            return;
        }
        Date recordDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            recordDate = sdf.parse(dateText);
        } catch (ParseException e) {
            etDate.setError("Selecciona una fecha válida");
            etDate.requestFocus();
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        Concept selectedConcept = filteredConceptList.get(spConcept.getSelectedItemPosition());
        String documentId = db.collection("records").document().getId();

        Record record = new Record();
        record.setId(documentId);
        record.setUserId(uid);
        record.setConceptId(selectedConcept.getId());
        record.setQuantity(quantity);
        record.setDate(recordDate);
        record.setDescription(comments);

        db.collection("records")
                .document(documentId)
                .set(record)
                .addOnSuccessListener(unused -> {
                    showToast("Actividad guardada correctamente");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Error al guardar la actividad"));
    }

    //Valida si un campo está vacío
    private boolean isEmptyField(EditText editText, String message) {
        if (editText.getText().toString().trim().isEmpty()) {
            editText.setError(message);
            editText.requestFocus();
            showToast(message);
            return true;
        }
        return false;
    }


    //Muestra un mensaje
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}