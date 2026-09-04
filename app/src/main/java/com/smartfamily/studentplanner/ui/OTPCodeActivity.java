package com.smartfamily.studentplanner.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.smartfamily.studentplanner.R;

public class OTPCodeActivity extends AppCompatActivity {

    private EditText[] otpFields = new EditText[5];
    private boolean error = false;
    private TextView invalidCodeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otpcode);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_otpcode), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        invalidCodeTextView = findViewById(R.id.invalidCodeTextView);

        otpFields[0] = findViewById(R.id.otp1);
        otpFields[1] = findViewById(R.id.otp2);
        otpFields[2] = findViewById(R.id.otp3);
        otpFields[3] = findViewById(R.id.otp4);
        otpFields[4] = findViewById(R.id.otp5);

        EditText[] otpFields = {findViewById(R.id.otp1), findViewById(R.id.otp2), findViewById(R.id.otp3), findViewById(R.id.otp4), findViewById(R.id.otp5)};

        for (EditText otpField : otpFields) {
            otpField.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    otpField.setHint("");
                } else {
                    otpField.setHint("0");
                }
            });
        }

        // Add listeners for each OTP field
        for (int i = 0; i < otpFields.length; i++) {
            int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1) {
                        if (error) {
                            for (EditText otpField : otpFields) {
                                otpField.setBackground(ContextCompat.getDrawable(OTPCodeActivity.this, R.drawable.otp_background_selector));
                            }
                            error = false;
                        }
                        otpFields[index].setBackground(ContextCompat.getDrawable(OTPCodeActivity.this, R.drawable.otp_background_active));
                        if (index < otpFields.length - 1) {
                            otpFields[index + 1].requestFocus();
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0) {
                        otpFields[index].setBackground(ContextCompat.getDrawable(OTPCodeActivity.this, R.drawable.otp_background_selector));
                    }
                }
            });

            // Handle backspace/delete key press
            otpFields[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpFields[index].getText().toString().isEmpty() && index > 0) {
                        otpFields[index - 1].requestFocus();
                        otpFields[index - 1].setText("");
                        return true;
                    }
                }
                return false;
            });

            // Set click listener to focus on the last empty field
            otpFields[i].setOnClickListener(v -> moveToLastEmptyField());
        }
    }

    private void moveToLastEmptyField() {
        for (int i = 0; i < otpFields.length; i++) {
            if (otpFields[i].getText().toString().isEmpty()) {
                otpFields[i].requestFocus();
                return;
            }
        }
        otpFields[otpFields.length - 1].requestFocus();
    }

    private void resetOTPFields() {
        invalidCodeTextView.setVisibility(View.VISIBLE);
        for (EditText otpField : otpFields) {
            otpField.setText("");
            otpField.setBackground(ContextCompat.getDrawable(this, R.drawable.otp_background_error));
        }
        otpFields[0].requestFocus();
        error = true;
    }
}