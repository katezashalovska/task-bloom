package com.smartfamily.studentplanner.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.viewModel.EventTaskViewModel;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;
import com.smartfamily.studentplanner.reminder.EventReminderManager;
import com.smartfamily.studentplanner.reminder.TaskReminderManager;
import com.smartfamily.studentplanner.utils.ThemeManager;

public class SettingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_PUSH_NOTIFICATIONS = "push_notifications";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final int DELAY_MILLISECOND = 300;

    private ImageButton backSettingButton;
    private ConstraintLayout languageConstraintLayout;
    private ConstraintLayout helpConstraintLayout;
    private ConstraintLayout aboutApplicationConstraintLayout;
    private ImageButton selectLanguageButton;
    private ImageButton helpButton;
    private ImageButton aboutApplicationButton;
    private Switch pushNotificationSwitch;
    private Switch darkModeSwitch;
    private TaskViewModel taskViewModel;
    private EventTaskViewModel eventTaskViewModel;
    private LinearLayout deleteAllCompletedTasks;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme до setContentView
        boolean isDarkMode = ThemeManager.isDarkModeEnabled(this);
        updateTheme(isDarkMode);

        EdgeToEdge.enable(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_settings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initializing
        backSettingButton = findViewById(R.id.backSettingButton);
        pushNotificationSwitch = findViewById(R.id.pushNotificationSwitch);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        eventTaskViewModel = new ViewModelProvider(this).get(EventTaskViewModel.class);
        deleteAllCompletedTasks = findViewById(R.id.deleteAllCompletedTaskLayout);
        languageConstraintLayout = findViewById(R.id.languageLayout);
        helpConstraintLayout = findViewById(R.id.helpLayout);
        selectLanguageButton = findViewById(R.id.selectLanguageButton);
        helpButton = findViewById(R.id.helpButton);
        aboutApplicationConstraintLayout = findViewById(R.id.aboutAppLayout);
        aboutApplicationButton = findViewById(R.id.aboutAppButton);

        // Saved data loading
        pushNotificationSwitch.setChecked(preferences.getBoolean(KEY_PUSH_NOTIFICATIONS, false));
        darkModeSwitch.setChecked(preferences.getBoolean(KEY_DARK_MODE, false));

        backSettingButton.setOnClickListener(v -> finish());

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean pushNotificationsEnabled = preferences.getBoolean(KEY_PUSH_NOTIFICATIONS, true);
        pushNotificationSwitch.setChecked(pushNotificationsEnabled);

        pushNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, isChecked).apply();

            if (isChecked) {
                TaskReminderManager.rescheduleAllTaskReminders(SettingActivity.this, taskViewModel);
                EventReminderManager.rescheduleAllEventReminders(SettingActivity.this, eventTaskViewModel);
                Toast.makeText(this, R.string.notifications_receiving_condition, Toast.LENGTH_SHORT).show();
            } else {
                TaskReminderManager.cancelAllTaskReminders(SettingActivity.this, taskViewModel);
                EventReminderManager.cancelAllEventReminders(SettingActivity.this, eventTaskViewModel);
            }
        });

        darkModeSwitch.setChecked(ThemeManager.isDarkModeEnabled(this));
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.setDarkModeEnabled(this, isChecked);
            darkModeSwitch.postDelayed(() -> updateTheme(isChecked), DELAY_MILLISECOND);
        });

        deleteAllCompletedTasks.setOnClickListener(v -> showDeleteConfirmationDialog());

        languageConstraintLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LanguageActivity.class);
            startActivity(intent);
        });

        selectLanguageButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LanguageActivity.class);
            startActivity(intent);
        });

        helpConstraintLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        });

        helpButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        });

        aboutApplicationConstraintLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutApplicationActivity.class);
            startActivity(intent);
        });

        aboutApplicationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutApplicationActivity.class);
            startActivity(intent);
        });
    }

    private void updateTheme(boolean isChecked) {
        int mode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    private void showDeleteConfirmationDialog() {
        // Create the dialog

        Dialog dialog = new Dialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_delete, null);
        dialog.setContentView(dialogView);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0f;
            window.setAttributes(params);
        }

        dialog.show();

        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);
        TextView messageText = dialogView.findViewById(R.id.messageText);
        messageText.setText(R.string.delete_completed_tasks_alert_message);

        btnYes.setOnClickListener(v1 -> {
            taskViewModel.deleteAllCompletedTasks();
            dialog.dismiss();
            
            boolean isDarkMode = ThemeManager.isDarkModeEnabled(this);
            updateTheme(isDarkMode);
        });

        btnNo.setOnClickListener(v1 -> dialog.dismiss());
    }

    private void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}
