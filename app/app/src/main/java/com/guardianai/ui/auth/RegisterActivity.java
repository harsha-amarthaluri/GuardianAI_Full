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
import com.guardianai.data.models.UserDto;
import com.guardianai.data.models.UserRegisterRequest;
import com.guardianai.data.repository.AuthRepository;
import com.guardianai.utils.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {

    private EditText editFullName;
    private EditText editEmail;
    private EditText editPhone;
    private EditText editPassword;
    private EditText editConfirmPassword;
    private Button buttonRegister;
    private TextView textLoginLink;
    private TextView textError;
    private ProgressBar progressBar;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authRepository = new AuthRepository(this);

        editFullName = findViewById(R.id.editRegFullName);
        editEmail = findViewById(R.id.editRegEmail);
        editPhone = findViewById(R.id.editRegPhone);
        editPassword = findViewById(R.id.editRegPassword);
        editConfirmPassword = findViewById(R.id.editRegConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textLoginLink = findViewById(R.id.textLoginLink);
        textError = findViewById(R.id.textRegError);
        progressBar = findViewById(R.id.progressBarRegister);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegister();
            }
        });

        textLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Return to Login
            }
        });
    }

    private void performRegister() {
        textError.setVisibility(View.GONE);

        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String password = editPassword.getText().toString();
        String confirmPassword = editConfirmPassword.getText().toString();

        if (!ValidationUtils.isNotEmpty(fullName)) {
            showError("Please enter your full name.");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            showError("Password must be at least 8 characters long.");
            return;
        }

        if (!ValidationUtils.isPasswordMatch(password, confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        setLoading(true);
        UserRegisterRequest request = new UserRegisterRequest(
                fullName,
                email,
                ValidationUtils.isNotEmpty(phone) ? phone : null,
                password
        );

        authRepository.register(request, new AuthRepository.ApiCallback<UserDto>() {
            @Override
            public void onSuccess(UserDto result) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Registration successful! Please sign in.", Toast.LENGTH_LONG).show();
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
        buttonRegister.setEnabled(!isLoading);
    }
}
