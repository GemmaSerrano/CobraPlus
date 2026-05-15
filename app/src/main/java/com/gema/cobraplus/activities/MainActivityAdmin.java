package com.gema.cobraplus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gema.cobraplus.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivityAdmin extends AppCompatActivity {

    private MaterialButton btnManagementReports, btnManagementConceptsAdmin, btnProfileAdmin, btnLogOutAdmin;
    private TextView txtMainAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        initViews();
        setupListeners();
    }

    //Carga de nuevo la página y actualiza el nombre
    @Override
    protected void onResume() {
        super.onResume();
        loadUserName();
    }

    //Inicializa los elementos
    private void initViews() {
        txtMainAdmin = findViewById(R.id.txtMainAdmin);
        btnProfileAdmin = findViewById(R.id.btnProfileAdmin);
        btnLogOutAdmin = findViewById(R.id.btnLogOutAdmin);
        btnManagementConceptsAdmin = findViewById(R.id.btnManagementConceptsAdmin);
        btnManagementReports = findViewById(R.id.btnManagementReports);
    }

    //Asocia acciones a los botones
    private void setupListeners() {

        //Acceso a perfil
        btnProfileAdmin.setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));

        //Acceso a gestión de conceptos
        btnManagementConceptsAdmin.setOnClickListener(v -> startActivity(new Intent(this, ManagementConcepts.class)));

        //Cerrar sesión
        btnLogOutAdmin.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        //Acceso a gestión de informes
        btnManagementReports.setOnClickListener(v -> startActivity(new Intent(this, ManagementReports.class)));
    }

    //Obtiene el nombre actualizado en Firestore y lo muestra en el saludo
    private void loadUserName() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        txtMainAdmin.setText("Hola, " + document.getString("name"));
                    }
                });
    }
}