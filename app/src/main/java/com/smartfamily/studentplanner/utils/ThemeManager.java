package com.smartfamily.studentplanner.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeManager {
    // SharedPreferences file name
    private static final String PREFS_NAME = "settings";
    // Key used to store the theme state (dark mode) in SharedPreferences
    private static final String THEME_KEY = "is_dark_mode";

    // Method to check if dark mode is enabled
    public static boolean isDarkModeEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(THEME_KEY, false);
    }

    // Method to enable or disable dark mode
    public static void setDarkModeEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(THEME_KEY, enabled).apply();
    }
}