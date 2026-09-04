package com.smartfamily.studentplanner.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String APP_SETTINGS = "app_settings";
    private static final String SELECTED_LANGUAGE = "selected_language";
    private static final String DEFAULT_LANGUAGE = "en";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        applySavedLocale();
        super.onCreate(savedInstanceState);
    }

    protected void applySavedLocale() {
        SharedPreferences preferences = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        String languageCode = preferences.getString(SELECTED_LANGUAGE, DEFAULT_LANGUAGE);
        LocaleListCompat locales = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(locales);
    }
}
