package com.gema.cobraplus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gema.cobraplus.R;
import com.gema.cobraplus.adapters.ConceptAdapter;
import com.gema.cobraplus.models.Concept;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ManagementConcepts extends AppCompatActivity {

    private RecyclerView rvConcepts;
    private MaterialButton btnNewConcept, btnReturn;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ArrayList<Concept> conceptList;
    private ConceptAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.management_concepts);

        initViews();
        initFirebase();
        initRecycler();
        setupListeners();
        loadConcepts();
    }

    //Inicializa los elementos
    private void initViews() {
        rvConcepts = findViewById(R.id.rvConcepts);
        btnNewConcept = findViewById(R.id.btnNewConcept);
        btnReturn = findViewById(R.id.btnReturn);
    }

    //Inicializa Firebase
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    //Inicializa RecyclerView
    private void initRecycler() {
        conceptList = new ArrayList<>();
        adapter = new ConceptAdapter(conceptList);

        //Listener para eliminar concepto
        adapter.setOnConceptDeleteListener(this::showDeleteConfirmation);

        rvConcepts.setLayoutManager(new LinearLayoutManager(this));
        rvConcepts.setAdapter(adapter);
    }

    //Asocia acciones a los botones
    private void setupListeners() {

        //Botón volver
        btnReturn.setOnClickListener(v -> finish());

        //Botón nuevo concepto
        btnNewConcept.setOnClickListener(v -> openNewConceptScreen());
    }

    //Abre la pantalla de nuevo concepto según el rol
    private void openNewConceptScreen() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) return;

                    Intent intent;

                    if ("admin".equals(document.getString("rol"))) {
                        intent = new Intent(this, NewConceptAdmin.class);
                    } else {
                        intent = new Intent(this, NewConcept.class);
                    }

                    startActivity(intent);
                })
                .addOnFailureListener(e -> showToast("Error al obtener rol de usuario"));
    }

    //Carga los conceptos según el tipo de usuario
    private void loadConcepts() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) return;

                    if ("admin".equals(document.getString("rol"))) {
                        loadBaseConcepts();
                    } else {
                        loadUserConcepts(uid);
                    }
                })
                .addOnFailureListener(e -> showToast("Error al cargar usuario"));
    }

    //Carga conceptos base creados por administrador
    private void loadBaseConcepts() {
        db.collection("concepts")
                .whereEqualTo("baseConcept", true)
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

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast("Error al cargar conceptos base"));
    }

    //Carga conceptos del usuario
    private void loadUserConcepts(String uid) {
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

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast("Error al cargar conceptos"));
    }

    //Elimina el concepto seleccionado de Firestore
    private void deleteConcept(@NonNull Concept concept) {
        db.collection("concepts")
                .document(concept.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    showToast("Concepto eliminado correctamente");
                    loadConcepts();
                })
                .addOnFailureListener(e -> showToast("Error al eliminar concepto"));
    }


    //Muestra confirmación antes de eliminar un concepto
    private void showDeleteConfirmation(Concept concept) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar concepto")
                .setMessage("¿Seguro que quieres eliminar este concepto?")
                .setPositiveButton("Sí", (dialog, which) -> deleteConcept(concept))
                .setNegativeButton("No", null)
                .show();
    }


    //Recarga los conceptos al volver a la pantalla
    @Override
    protected void onResume() {
        super.onResume();
        loadConcepts();
    }

    //Muestra mensaje
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
