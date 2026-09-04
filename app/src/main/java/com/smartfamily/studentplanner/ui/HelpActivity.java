package com.smartfamily.studentplanner.ui;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.utils.ThemeManager;

public class HelpActivity extends AppCompatActivity {
    private ImageButton backHelpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isDarkMode = ThemeManager.isDarkModeEnabled(this);
        updateTheme(isDarkMode);

        EdgeToEdge.enable(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_help), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backHelpButton = findViewById(R.id.backHelpButton);
        backHelpButton.setOnClickListener(v -> finish());
    }

    private void updateTheme(boolean isChecked) {
        int mode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
