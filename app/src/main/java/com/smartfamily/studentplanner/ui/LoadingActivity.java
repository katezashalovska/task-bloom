package com.smartfamily.studentplanner.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;

import com.smartfamily.studentplanner.R;

public class LoadingActivity extends BaseActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_loading);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, LanguageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 500);
    }
}
