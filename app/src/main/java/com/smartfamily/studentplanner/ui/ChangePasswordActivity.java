package com.smartfamily.studentplanner.ui;

import android.content.Intent;
import android.os.Bundle;
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

public class ChangePasswordActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private TextInputEditText newPasswordInput;
    private TextInputEditText repeatPasswordInput;
    private TextView titlePageView;
    private Button confirmButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        auth = FirebaseAuth.getInstance();
        newPasswordInput = findViewById(R.id.newPasswordInput);
        repeatPasswordInput = findViewById(R.id.repeatPasswordInput);
        confirmButton = findViewById(R.id.confirmButton);
        titlePageView = findViewById(R.id.titlePageView);
        backButton = findViewById(R.id.getBack);

        titlePageView.setText(getString(R.string.profile_title));
        backButton.setOnClickListener(v -> finish());

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

            if (auth.getCurrentUser() != null) {
                auth.getCurrentUser().updatePassword(newPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, getString(R.string.password_updated_successfully), Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, getString(R.string.reauthenticate_message), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, ReauthenticateActivity.class));
                                finish();
                            }
                        });
            } else {
                Toast.makeText(this, getString(R.string.unlogged_user), Toast.LENGTH_SHORT).show();
            }

        });

    }
}
