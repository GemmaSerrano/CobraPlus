package com.gema.cobraplus.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gema.cobraplus.R;
import com.gema.cobraplus.models.Concept;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class NewConcept extends AppCompatActivity {

    private Spinner spBaseConcept;
    private EditText etNameConcept, etCompany, etUnitCalculation, etAmount;
    private MaterialButton btnSave, btnCancel;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ArrayList<Concept> baseConceptList;
    private ArrayList<String> baseConceptNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_concept);

        initViews();
        initFirebase();
        initLists();
        setupListeners();
        loadBaseConcepts();
    }

    //Inicializa los elementos
    private void initViews() {
        spBaseConcept = findViewById(R.id.spBaseConcept);

        etNameConcept = findViewById(R.id.etNameConcept);
        etCompany = findViewById(R.id.etCompany);
        etUnitCalculation = findViewById(R.id.etUnitCalculation);
        etUnitCalculation.setKeyListener(null); //no modificable
        etAmount = findViewById(R.id.etAmount);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    //Inicializa Firebase
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    //Inicializa las listas
    private void initLists() {
        baseConceptList = new ArrayList<>();
        baseConceptNames = new ArrayList<>();
    }

    //Asocia acciones a botones y spinner
    private void setupListeners() {

        //Muestra la unidad de cálculo del concepto base seleccionado
        spBaseConcept.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (baseConceptList.isEmpty()) return;

                Concept selectedConcept = baseConceptList.get(position);
                etUnitCalculation.setText(selectedConcept.getUnitCalculation());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Guarda el concepto del usuario
        btnSave.setOnClickListener(v -> saveUserConcept());

        //Vuelve a la pantalla anterior
        btnCancel.setOnClickListener(v -> finish());
    }

    //Carga en el spinner solo los conceptos base creados por el administrador
    private void loadBaseConcepts() {
        db.collection("concepts")
                .whereEqualTo("baseConcept", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    baseConceptList.clear();
                    baseConceptNames.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Concept concept = document.toObject(Concept.class);

                        if (concept != null) {
                            concept.setId(document.getId());
                            baseConceptList.add(concept);
                            baseConceptNames.add(concept.getName());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            baseConceptNames
                    );

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spBaseConcept.setAdapter(adapter);

                    //Muestra la unidad del primer concepto si existe
                    if (!baseConceptList.isEmpty()) {
                        etUnitCalculation.setText(baseConceptList.get(0).getUnitCalculation());
                    }
                })
                .addOnFailureListener(e -> showToast("Error al cargar conceptos base"));
    }


    //Guarda el concepto creado por el usuario
    private void saveUserConcept() {
        if (mAuth.getCurrentUser() == null) {
            showToast("Usuario no autenticado");
            return;
        }

        if (baseConceptList.isEmpty()) {
            showToast("No hay conceptos base disponibles");
            return;
        }

        if (isEmptyField(etNameConcept, "Introduce el nombre del concepto")) return;
        if (isEmptyField(etCompany, "Introduce la empresa")) return;
        if (isEmptyField(etAmount, "Introduce el importe")) return;

        double amount;

        try {
            amount = Double.parseDouble(etAmount.getText().toString().trim());
        } catch (NumberFormatException e) {
            etAmount.setError("Introduce un importe válido");
            etAmount.requestFocus();
            return;
        }

        Concept selectedBaseConcept = baseConceptList.get(spBaseConcept.getSelectedItemPosition());

        Concept concept = new Concept();
        concept.setId(db.collection("concepts").document().getId());
        concept.setName(etNameConcept.getText().toString().trim());
        concept.setUnitCalculation(selectedBaseConcept.getUnitCalculation());
        concept.setCompany(etCompany.getText().toString().trim());
        concept.setAmount(amount);
        concept.setUserId(mAuth.getCurrentUser().getUid());
        concept.setBaseConcept(false);
        concept.setBaseConceptId(selectedBaseConcept.getId());

        db.collection("concepts")
                .document(concept.getId())
                .set(concept)
                .addOnSuccessListener(unused -> {
                    showToast("Concepto guardado correctamente");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Error al guardar el concepto"));
    }

    //Valida si un campo está vacío
    private boolean isEmptyField(EditText editText, String message) {
        if (editText.getText().toString().trim().isEmpty()) {
            editText.setError(message);
            editText.requestFocus();
            return true;
        }
        return false;
    }

    //Muestra mensaje
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
