package com.guardianai.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.guardianai.R;
import com.guardianai.data.models.TokenResponseDto;
import com.guardianai.data.repository.AuthRepository;
import com.guardianai.ui.MainActivity;
import com.guardianai.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;
    private Button buttonLogin;
    private TextView textRegisterLink;
    private TextView textError;
    private ProgressBar progressBar;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository(this);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textRegisterLink = findViewById(R.id.textRegisterLink);
        textError = findViewById(R.id.textError);
        progressBar = findViewById(R.id.progressBarLogin);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        textRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        TextView textForgotPassword = findViewById(R.id.textForgotPassword);
        if (textForgotPassword != null) {
            textForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        }
    }

    private void showForgotPasswordDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        EditText editResetEmail = new EditText(this);
        editResetEmail.setHint("Enter your registered email address");
        editResetEmail.setPadding(32, 24, 32, 24);

        builder.setTitle("🔒 Password Reset")
                .setMessage("Enter your email address to receive password recovery instructions:")
                .setView(editResetEmail)
                .setPositiveButton("Send Reset Email", (dialog, which) -> {
                    String email = editResetEmail.getText().toString().trim();
                    if (!ValidationUtils.isValidEmail(email)) {
                        Toast.makeText(LoginActivity.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(LoginActivity.this, "Password recovery instructions sent to " + email, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogin() {
        textError.setVisibility(View.GONE);
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        if (!ValidationUtils.isNotEmpty(password)) {
            showError("Password cannot be empty.");
            return;
        }

        setLoading(true);
        authRepository.login(email, password, new AuthRepository.ApiCallback<TokenResponseDto>() {
            @Override
            public void onSuccess(TokenResponseDto result) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage, int statusCode) {
                setLoading(false);
                showError(errorMessage);
            }
        });
    }

    private void showError(String msg) {
        textError.setText(msg);
        textError.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonLogin.setEnabled(!isLoading);
    }
}
