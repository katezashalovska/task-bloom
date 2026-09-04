package com.smartfamily.studentplanner.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.utils.Utils;

public class VerifyEmailActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private String oobCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        Uri data = getIntent().getData();
        if (data == null) {
            Toast.makeText(this, getString(R.string.no_data_links_error), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String mode = data.getQueryParameter(Utils.KEY_MODE);
        oobCode = data.getQueryParameter(Utils.OOB_CODE);

        if (!"verifyEmail".equals(mode) || oobCode == null || oobCode.isEmpty()) {
            Toast.makeText(this, getString(R.string.invalid_link_error), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        auth.applyActionCode(oobCode)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.email_verifed), Toast.LENGTH_LONG).show();

                        FirebaseUser user = auth.getCurrentUser();

                        if (user != null) {
                            // reload user info
                            user.reload().addOnCompleteListener(reloadTask -> {
                                if (user.isEmailVerified()) {

                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(this, getString(R.string.email_still_not_verifed), Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                }
                            });
                        } else {

                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.email_verification_failed), Toast.LENGTH_LONG).show();

                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }
                });
    }
}

