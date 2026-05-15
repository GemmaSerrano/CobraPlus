package com.gema.cobraplus.activities;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;

import com.gema.cobraplus.R;
import com.gema.cobraplus.models.User;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private MaterialButton btnLogin, btnRegister, btnForgotPassword, btnGoogle;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        initViews();
        initFirebase();
        setupListeners();
    }

    //Inicializa los elementos
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnGoogle = findViewById(R.id.btnGoogle);
    }

    //Inicializa Firebase
    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        credentialManager = CredentialManager.create(this);
    }

    //Asocia acciones a botones
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginWithEmail());

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnForgotPassword.setOnClickListener(v -> resetPassword());

        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    //Inicia sesión con email y contraseña
    private void loginWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (isEmptyField(etEmail, "Introduce tu email")) return;
        if (isEmptyField(etPassword, "Introduce tu contraseña")) return;

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> checkVerifiedUser())
                .addOnFailureListener(this::handleLoginError);
    }

    //Controla los mensajes de error de registro
    private void handleLoginError(Exception e) {
        showToast("Email o contraseña incorrectos");
    }


    //Comprueba si el usuario ha verificado el correo
    private void checkVerifiedUser() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            showToast("Error: usuario no autenticado");
            return;
        }

        user.reload().addOnCompleteListener(task -> {
            FirebaseUser updatedUser = mAuth.getCurrentUser();

            if (updatedUser != null && updatedUser.isEmailVerified()) {
                loadUserRoleAndOpenMain();
            } else {
                mAuth.signOut();
                showToast("Debes verificar tu correo antes de iniciar sesión");
            }
        });
    }

    //Envía correo de recuperación de contraseña
    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (isEmptyField(etEmail, "Introduce tu email")) return;

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> showToast("Correo de recuperación enviado"))
                .addOnFailureListener(e -> showToast("Error: " + e.getMessage()));
    }

    //Carga rol del usuario y abre pantalla principal
    private void loadUserRoleAndOpenMain() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            showToast("Error: usuario no autenticado");
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        showToast("Usuario no encontrado en Firestore");
                        return;
                    }
                    String rol = document.getString("rol");
                    openMainByRole(rol);
                })
                .addOnFailureListener(e -> showToast("Error al obtener datos"));
    }

    //Abre la pantalla principal según el rol
    private void openMainByRole(String rol) {
        Intent intent;

        if ("admin".equals(rol)) {
            intent = new Intent(this, MainActivityAdmin.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }

    //Inicia sesión con Google
    private void signInWithGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                Runnable::run,
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, androidx.credentials.exceptions.GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull androidx.credentials.exceptions.GetCredentialException e) {
                        runOnUiThread(() -> showToast("Error Google: " + e.getMessage()));
                    }
                }
        );
    }

    //Recibe credencial y extrae token
    private void handleSignIn(Credential credential) {
        if (!(credential instanceof CustomCredential)) {
            showToast("Tipo de credencial no soportado");
            return;
        }

        CustomCredential customCredential = (CustomCredential) credential;

        if (!TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
            showToast("Credencial de Google no válida");
            return;
        }

        GoogleIdTokenCredential googleIdTokenCredential =
                GoogleIdTokenCredential.createFrom(customCredential.getData());

        firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
    }

    //Autenticación en Firebase con Google
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> saveGoogleUserIfNeeded())
                .addOnFailureListener(e -> showToast("Error Firebase: " + e.getMessage()));
    }

    //Guarda usuario Google en Firestore si no existe
    private void saveGoogleUserIfNeeded() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            showToast("Error: usuario no disponible");
            return;
        }

        String uid = firebaseUser.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        loadUserRoleAndOpenMain();
                        return;
                    }

                    String name = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "";
                    String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";

                    User user = new User(uid, name, email);
                    user.setRol("user");

                    db.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(unused -> loadUserRoleAndOpenMain())
                            .addOnFailureListener(e -> showToast("Error al guardar usuario Google en Firestore"));
                })
                .addOnFailureListener(e -> showToast("Error al comprobar usuario Google"));
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

    //Muestra un mensaje
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}