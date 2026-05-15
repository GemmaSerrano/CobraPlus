package com.gema.cobraplus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gema.cobraplus.R;
import com.gema.cobraplus.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNameRegister, etEmailRegister, etPasswordRegister, etConfirmPassword;
    private MaterialButton btnCreateAccount, btnReturnLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        initViews();
        initFirebase();
        setupListeners();
    }

    // Inicializa los elementos de la interfaz
    private void initViews() {
        etNameRegister = findViewById(R.id.etNameRegister);
        etEmailRegister = findViewById(R.id.etEmailRegister);
        etPasswordRegister = findViewById(R.id.etPasswordRegister);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnReturnLogin = findViewById(R.id.btnReturnLogin);
    }

    // Inicializa los servicios de Firebase
    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Asocia eventos a los botones de la pantalla
    private void setupListeners() {
        btnReturnLogin.setOnClickListener(v -> goToLogin());
        btnCreateAccount.setOnClickListener(v -> registerUser());
    }

    // Vuelve a la pantalla de login
    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        finish();
    }

    // Registra al usuario
    private void registerUser() {
        String name = etNameRegister.getText().toString().trim();
        String email = etEmailRegister.getText().toString().trim();
        String password = etPasswordRegister.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (isEmptyField(etNameRegister, "Introduce el nombre")) return;
        if (isEmptyField(etEmailRegister, "Introduce el email")) return;
        if (isEmptyField(etPasswordRegister, "Introduce la contraseña")) return;

        if (password.length() < 6) {
            etPasswordRegister.setError("La contraseña debe tener al menos 6 caracteres");
            etPasswordRegister.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();

                    if (firebaseUser == null) {
                        showToast("Error: usuario no disponible");
                        return;
                    }
                    saveUserInFirestore(firebaseUser, name, email);
                })
                .addOnFailureListener(this::handleRegisterError);
    }

    //Controla los mensajes de error de registro
    private void handleRegisterError(Exception e) {

        if (e instanceof FirebaseAuthUserCollisionException) {
            showToast("Ya existe una cuenta con ese email");
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            showToast("Introduce un email válido");
        } else {
            showToast("Error al registrar el usuario");
        }
    }

    //Guarda usuario en Firestore
    private void saveUserInFirestore(FirebaseUser firebaseUser, String nombre, String email) {
        User user = new User(firebaseUser.getUid(), nombre, email);
        user.setRol("user");

        db.collection("users")
                .document(firebaseUser.getUid())
                .set(user)
                .addOnSuccessListener(unused -> sendVerificationEmail(firebaseUser))
                .addOnFailureListener(e -> showToast("Error al guardar usuario en Firestore"));
    }

    //Envía correo de verificación
    private void sendVerificationEmail(FirebaseUser firebaseUser) {
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Usuario registrado. Verifica tu correo");
                    } else {
                        showToast("Usuario guardado, pero error al enviar email de verificación");
                    }

                    FirebaseAuth.getInstance().signOut();
                    goToLogin();
                });
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
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}