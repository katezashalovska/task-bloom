package com.smartfamily.studentplanner.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.smartfamily.studentplanner.R;

public class ReauthenticateActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private TextInputEditText emailInput, passwordInput;
    private Button confirmButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reauthentication);

        auth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.emailInputTextView);
        passwordInput = findViewById(R.id.passwordInputTextView);
        confirmButton = findViewById(R.id.confirmButton);
        backButton = findViewById(R.id.getBack);

        backButton.setOnClickListener(v-> finish());
        confirmButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.email_and_password_required), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isInternetAvailable()) {
                Toast.makeText(this, getString(R.string.net_connection), Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

            auth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.successful_authentication_message), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, ChangePasswordActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.error_authentication_message), Toast.LENGTH_SHORT).show();
                    }
                });
        });
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }
}
