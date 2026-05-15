package com.gema.cobraplus.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gema.cobraplus.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private EditText etNameValue, etEmailValue;
    private EditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private MaterialButton btnSaveChanges, btnReturn;
    private TextView txtCurrentPassword, txtNewPassword, txtConfirmPassword;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        initFirebase();
        initViews();
        loadUserData();
        setupListeners();
        checkPasswordProvider();
    }

    //Inicializa los elementos
    private void initViews() {
        etNameValue = findViewById(R.id.etNameValue);
        etEmailValue = findViewById(R.id.etEmailValue);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmPassword);
        txtCurrentPassword = findViewById(R.id.txtCurrentPassword);
        txtNewPassword = findViewById(R.id.txtNewPassword);
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword);

        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnReturn = findViewById(R.id.btnReturn);
    }

    //Inicializa Firebase
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    //Carga los datos del usuario desde Firestore
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            showToast("Usuario no autenticado");
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        showToast("Perfil no encontrado");
                        return;
                    }

                    etNameValue.setText(document.getString("name"));
                    etEmailValue.setText(document.getString("email"));
                    etEmailValue.setEnabled(false);
                })
                .addOnFailureListener(e -> showToast("Error al cargar perfil"));
    }

    //Asocia acciones a botones
    private void setupListeners() {
        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
        btnReturn.setOnClickListener(v -> finish());
    }

    //Guarda los cambios del perfil (nombre + contraseña si procede)
    private void saveProfileChanges() {
        if (isEmptyField(etNameValue, "Introduce un nombre")) return;

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            showToast("Usuario no autenticado");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", etNameValue.getText().toString().trim());

        db.collection("users")
                .document(user.getUid())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    boolean wantsPasswordChange =
                            !etCurrentPassword.getText().toString().trim().isEmpty() ||
                                    !etNewPassword.getText().toString().trim().isEmpty() ||
                                    !etConfirmNewPassword.getText().toString().trim().isEmpty();

                    if (wantsPasswordChange) {
                        changePassword();
                    } else {
                        showToast("Nombre actualizado correctamente");
                    }
                })
                .addOnFailureListener(e -> showToast("Error al actualizar el nombre"));
    }

    //Cambia la contraseña del usuario
    private void changePassword() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null || user.getEmail() == null) {
            showToast("Usuario no autenticado");
            return;
        }

        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmNewPassword.getText().toString().trim();

        if (isEmptyField(etCurrentPassword, "Introduce la contraseña actual")) return;
        if (isEmptyField(etNewPassword, "Introduce la nueva contraseña")) return;

        if (newPassword.length() < 6) {
            etNewPassword.setError("Mínimo 6 caracteres");
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmNewPassword.setError("No coinciden");
            etConfirmNewPassword.requestFocus();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> user.updatePassword(newPassword)
                        .addOnSuccessListener(unused1 -> {
                            showToast("Perfil y contraseña actualizados");
                            etCurrentPassword.setText("");
                            etNewPassword.setText("");
                            etConfirmNewPassword.setText("");
                        })
                        .addOnFailureListener(e -> showToast("Error al cambiar contraseña")))
                .addOnFailureListener(e -> showToast("Contraseña actual incorrecta"));
    }

    //Oculta cambio de contraseña si el usuario es de Google
    private void checkPasswordProvider() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) return;

        boolean isPasswordUser = false;

        for (UserInfo info : user.getProviderData()) {
            if ("password".equals(info.getProviderId())) {
                isPasswordUser = true;
                break;
            }
        }

        if (!isPasswordUser) {
            etCurrentPassword.setVisibility(View.GONE);
            etNewPassword.setVisibility(View.GONE);
            etConfirmNewPassword.setVisibility(View.GONE);
            txtCurrentPassword.setVisibility(View.GONE);
            txtNewPassword.setVisibility(View.GONE);
            txtConfirmPassword.setVisibility(View.GONE);
        }
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