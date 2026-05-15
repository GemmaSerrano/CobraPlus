package com.gema.cobraplus.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gema.cobraplus.R;
import com.gema.cobraplus.models.Report;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class NewReportAdmin extends AppCompatActivity {
    private EditText etReportNameAdmin;
    private MaterialButton btnGenerateAdmin, btnCancelReportAdmin;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_report_admin);

        initViews();
        initFirebase();
        setupListeners();
    }

    //Inicializa los elementos
    private void initViews() {
        etReportNameAdmin = findViewById(R.id.etReportNameAdmin);

        btnGenerateAdmin = findViewById(R.id.btnGenerateAdmin);
        btnCancelReportAdmin = findViewById(R.id.btnCancelReportAdmin);
    }

    //Inicializa Firebase
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    //Asocia acciones a botones
    private void setupListeners() {
        btnGenerateAdmin.setOnClickListener(v -> saveBaseReport());
        btnCancelReportAdmin.setOnClickListener(v -> finish());
    }

    //Guarda un informe base creado por el administrador
    private void saveBaseReport() {
        if (isEmptyField(etReportNameAdmin, "Introduce el nombre del informe")) return;

        Report report = new Report();
        report.setId(db.collection("reports").document().getId());
        report.setName(etReportNameAdmin.getText().toString().trim());
        report.setCompany("");
        report.setStartDate(null);
        report.setEndDate(null);
        report.setGroupBy("");
        report.setBaseReport(true);
        report.setBaseReportId("");

        //Para poder añadir la fecha en informe (admin de momento no)
        report.setCreatedAt(new Date());

        db.collection("reports")
                .document(report.getId())
                .set(report)
                .addOnSuccessListener(unused -> {
                    showToast("Informe base guardado correctamente");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Error al guardar el informe"));
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
