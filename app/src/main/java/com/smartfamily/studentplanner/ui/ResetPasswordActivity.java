package com.smartfamily.studentplanner.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.utils.Utils;

public class ResetPasswordActivity extends AppCompatActivity {
    private String oobCode;
    private FirebaseAuth auth;
    private TextInputEditText newPasswordInput;
    private TextInputEditText repeatPasswordInput;
    private TextView titlePageView;
    private Button confirmButton;
    private ImageButton backButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        auth = FirebaseAuth.getInstance();
        newPasswordInput = findViewById(R.id.newPasswordInput);
        repeatPasswordInput = findViewById(R.id.repeatPasswordInput);
        confirmButton = findViewById(R.id.confirmButton);
        backButton = findViewById(R.id.getBack);
        titlePageView = findViewById(R.id.titlePageView);

        titlePageView.setText(getString(R.string.login_title));
        backButton.setOnClickListener(view -> goToLoginActivity());
        newPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());

        Uri data = getIntent().getData();
        if (data != null) {
            oobCode = data.getQueryParameter(Utils.OOB_CODE);

            if (oobCode == null || oobCode.isEmpty()) {
                Toast.makeText(this, getString(R.string.invalid_code_error), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Check is code valid
            auth.verifyPasswordResetCode(oobCode)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this, getString(R.string.invalid_link_error), Toast.LENGTH_LONG).show();
                            finish();
                        }

                    });
        } else {
            Toast.makeText(this, getString(R.string.no_data_links_error), Toast.LENGTH_SHORT).show();
            finish();
        }

        confirmButton.setOnClickListener(v -> {
            String newPassword = newPasswordInput.getText().toString().trim();
            String repeatedPassword = repeatPasswordInput.getText().toString().trim();

            if (newPassword.isEmpty() || repeatedPassword.isEmpty()) {
                Toast.makeText(this, getString(R.string.both_password_required), Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < Utils.PASSWORD_LENGTH) {
                Toast.makeText(this, getString(R.string.error_weak_password), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(repeatedPassword)) {
                Toast.makeText(this, getString(R.string.mismatch_passwords_error), Toast.LENGTH_SHORT).show();
                return;
            }

            auth.confirmPasswordReset(oobCode, newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, getString(R.string.password_updated_successfully), Toast.LENGTH_LONG).show();
                            goToLoginActivity();
                        } else {
                            Toast.makeText(this, getString(R.string.failed_to_update_password_error), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void goToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
