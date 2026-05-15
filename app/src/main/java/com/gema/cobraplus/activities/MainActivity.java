package com.gema.cobraplus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gema.cobraplus.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnRecordActivity, btnManagementReports, btnManagementConcepts,
            btnProfile, btnHelp, btnLogOut;
    private TextView txtMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        txtMain = findViewById(R.id.txtMain);
        btnProfile = findViewById(R.id.btnProfile);
        btnHelp = findViewById(R.id.btnHelp);
        btnLogOut = findViewById(R.id.btnLogOut);
        btnManagementConcepts = findViewById(R.id.btnManagementConcepts);
        btnRecordActivity = findViewById(R.id.btnRecordActivity);
        btnManagementReports = findViewById(R.id.btnManagementReports);
    }

    //Asocia acciones a los botones
    private void setupListeners() {

        //Acceso a perfil
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));

        //Acceso a gestión de conceptos
        btnManagementConcepts.setOnClickListener(v -> startActivity(new Intent(this, ManagementConcepts.class)));

        //Cerrar sesión
        btnLogOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        //Acceso a Registrar actividad
        btnRecordActivity.setOnClickListener(v->startActivity(new Intent(this, RecordActivity.class)));

        //Acceso a gestión de informes
        btnManagementReports.setOnClickListener(v -> startActivity(new Intent(this, ManagementReports.class)));

        //Acceso a ayuda
        btnHelp.setOnClickListener(v -> startActivity(new Intent(this,HelpActivity.class)));
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
                        txtMain.setText("Hola, " + document.getString("name"));
                    }
                });
    }
}