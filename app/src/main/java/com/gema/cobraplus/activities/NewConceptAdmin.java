package com.gema.cobraplus.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gema.cobraplus.R;
import com.gema.cobraplus.models.Concept;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;


public class NewConceptAdmin extends AppCompatActivity {

    private EditText etNameConceptAdmin, etUnitCalculationAdmin;
    private MaterialButton btnSaveAdmin, btnCancelAdmin;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_concept_admin);

        initViews();
        initFirebase();
        setupListeners();
    }

    //Inicializa los elementos
    private void initViews() {
        etNameConceptAdmin = findViewById(R.id.etNameConceptAdmin);
        etUnitCalculationAdmin = findViewById(R.id.etUnitCalculationAdmin);

        btnSaveAdmin = findViewById(R.id.btnSaveAdmin);
        btnCancelAdmin = findViewById(R.id.btnCancelAdmin);
    }

    //Inicializa Firebase
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    //Asocia acciones a botones
    private void setupListeners() {
        btnSaveAdmin.setOnClickListener(v -> saveBaseConcept());
        btnCancelAdmin.setOnClickListener(v -> finish());
    }

    //Guarda un concepto base creado por el administrador
    private void saveBaseConcept() {
        if (isEmptyField(etNameConceptAdmin, "Introduce el nombre del concepto")) return;
        if (isEmptyField(etUnitCalculationAdmin, "Introduce la unidad de cálculo")) return;

        Concept concept = new Concept();
        concept.setId(db.collection("concepts").document().getId());
        concept.setName(etNameConceptAdmin.getText().toString().trim());
        concept.setUnitCalculation(etUnitCalculationAdmin.getText().toString().trim());
        concept.setCompany("");
        concept.setAmount(0.0);
        concept.setUserId("");
        concept.setBaseConcept(true);
        concept.setBaseConceptId("");

        db.collection("concepts")
                .document(concept.getId())
                .set(concept)
                .addOnSuccessListener(unused -> {
                    showToast("Concepto base guardado correctamente");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Error al guardar el concepto base"));
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
