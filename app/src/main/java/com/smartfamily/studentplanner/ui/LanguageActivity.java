package com.smartfamily.studentplanner.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.utils.ThemeManager;

public class LanguageActivity extends AppCompatActivity {
    private static final String APP_SETTINGS = "app_settings";
    private static final String SELECTED_LANGUAGE = "selected_language";
    private static final String EN = "en";
    private static final String UK = "uk";
    private static final String ES = "es";
    private static final String FR = "fr";

    private ImageButton backButton;
    private RadioButton englishRadioButton;
    private RadioButton ukrainianRadioButton;
    private RadioButton spanishRadioButton;
    private RadioButton franceRadioButton;
    private ConstraintLayout englishConstraintLayout;
    private ConstraintLayout ukrainianConstraintLayout;
    private ConstraintLayout spanishConstraintLayout;
    private ConstraintLayout franceConstraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isDarkMode = ThemeManager.isDarkModeEnabled(this);
        updateTheme(isDarkMode);

        EdgeToEdge.enable(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_language), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backButton = findViewById(R.id.backLanguageButton);
        englishRadioButton = findViewById(R.id.englishRadioButton);
        ukrainianRadioButton = findViewById(R.id.ukrainianRadioButton);
        spanishRadioButton = findViewById(R.id.spanishRadioButton);
        franceRadioButton = findViewById(R.id.franceRadioButton);
        englishConstraintLayout = findViewById(R.id.englishLayout);
        ukrainianConstraintLayout = findViewById(R.id.ukrainianLayout);
        spanishConstraintLayout = findViewById(R.id.spanishLayout);
        franceConstraintLayout = findViewById(R.id.franceLayout);

        loadLanguagePreference();

        backButton.setOnClickListener(v -> finish());

        englishConstraintLayout.setOnClickListener(v -> {
            englishRadioButton.setChecked(true);
            ukrainianRadioButton.setChecked(false);
            spanishRadioButton.setChecked(false);
            franceRadioButton.setChecked(false);
            saveLanguagePreference(EN);
        });

        englishRadioButton.setOnClickListener(v -> {
            englishRadioButton.setChecked(true);
            ukrainianRadioButton.setChecked(false);
            spanishRadioButton.setChecked(false);
            franceRadioButton.setChecked(false);
            saveLanguagePreference(EN);
        });

        ukrainianConstraintLayout.setOnClickListener(v -> {
            ukrainianRadioButton.setChecked(true);
            englishRadioButton.setChecked(false);
            spanishRadioButton.setChecked(false);
            franceRadioButton.setChecked(false);
            saveLanguagePreference(UK);
        });

        ukrainianRadioButton.setOnClickListener(v -> {
            ukrainianRadioButton.setChecked(true);
            englishRadioButton.setChecked(false);
            spanishRadioButton.setChecked(false);
            franceRadioButton.setChecked(false);
            saveLanguagePreference(UK);
        });

        spanishConstraintLayout.setOnClickListener(v -> {
            spanishRadioButton.setChecked(true);
            ukrainianRadioButton.setChecked(false);
            englishRadioButton.setChecked(false);
            franceRadioButton.setChecked(false);
            saveLanguagePreference(ES);
        });

        spanishRadioButton.setOnClickListener(v -> {
            spanishRadioButton.setChecked(true);
            ukrainianRadioButton.setChecked(false);
            englishRadioButton.setChecked(false);
            franceRadioButton.setChecked(false);
            saveLanguagePreference(ES);
        });

        franceConstraintLayout.setOnClickListener(v -> {
            franceRadioButton.setChecked(true);
            ukrainianRadioButton.setChecked(false);
            spanishRadioButton.setChecked(false);
            englishRadioButton.setChecked(false);
            saveLanguagePreference(FR);
        });

        franceRadioButton.setOnClickListener(v -> {
            franceRadioButton.setChecked(true);
            ukrainianRadioButton.setChecked(false);
            spanishRadioButton.setChecked(false);
            englishRadioButton.setChecked(false);
            saveLanguagePreference(FR);
        });

    }

    private void updateTheme(boolean isChecked) {
        int mode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    private void saveLanguagePreference(String languageCode) {
        SharedPreferences preferences = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        preferences.edit().putString(SELECTED_LANGUAGE, languageCode).apply();

        LocaleListCompat locales = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(locales);

        //Intent intent = new Intent(this, LoadingActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //startActivity(intent);
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void loadLanguagePreference() {
        String language = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)
                .getString(SELECTED_LANGUAGE, EN); // by default "en"

        switch (language) {
            case EN:
                englishRadioButton.setChecked(true);
                break;
            case UK:
                ukrainianRadioButton.setChecked(true);
                break;
            case ES:
                spanishRadioButton.setChecked(true);
                break;
            case FR:
                franceRadioButton.setChecked(true);
                break;
        }
    }

    private void goToSettings() {
        startActivity(new Intent(this, SettingActivity.class));
        finish();
    }
}
