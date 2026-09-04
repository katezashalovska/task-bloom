package com.smartfamily.studentplanner.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.smartfamily.studentplanner.R;

public class AuthLinkHandlerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();

        if (data == null) {
            Toast.makeText(this, getString(R.string.no_data_links_error), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String mode = data.getQueryParameter("mode");

        if (mode == null) {
            Toast.makeText(this, getString(R.string.unknown_mode_error), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        switch (mode) {
            case "resetPassword":
                Intent resetIntent = new Intent(this, ResetPasswordActivity.class);
                resetIntent.setData(data);
                startActivity(resetIntent);
                break;

            case "verifyEmail":
                Intent emailVerificationIntent = new Intent(this, VerifyEmailActivity.class);
                emailVerificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                emailVerificationIntent.setData(data);
                startActivity(emailVerificationIntent);
                break;

            case "recoverEmail":
                // todo: implement in future

                break;

            default:
                Toast.makeText(this, getString(R.string.unknown_action_error) + mode, Toast.LENGTH_SHORT).show();
                break;
        }

        finish();
    }
}

