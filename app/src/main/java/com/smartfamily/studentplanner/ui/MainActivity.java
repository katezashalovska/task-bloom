package com.smartfamily.studentplanner.ui;

import static com.smartfamily.studentplanner.ui.HomeFragment.TASK_ID_EXTRA;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;
import com.smartfamily.studentplanner.databinding.ActivityMainBinding;
import com.smartfamily.studentplanner.utils.ThemeManager;

public class MainActivity extends AppCompatActivity implements OnBottomSheetDismissListener {
    ActivityMainBinding binding;
    FloatingActionButton createTaskButton;
    BottomNavigationView bottomNavigationView;
    private TaskViewModel taskViewModel;

    private static final int REQUEST_CODE_NOTIFICATIONS = 1001;
    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_NOTIFICATION_PERMISSION_REQUESTED_EVER = "notification_permission_requested_ever";
    private TextView notificationsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Get the current theme state (dark mode or light mode) from settings
        boolean isDarkMode = ThemeManager.isDarkModeEnabled(this);
        // Apply the theme based on the saved state
        updateTheme(isDarkMode);
        // Apply the language based on the saved state
        applySavedLanguage();

        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        // Initialize the TaskViewModel
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Initialize the notificationsStatus TextView
        notificationsStatus = findViewById(R.id.notifications_status);

        // Initialize firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        // Check authenticate status at start
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && !currentUser.isEmailVerified()) {
            currentUser.delete();
            currentUser = null;
            mAuth.signOut();
        }

        if (currentUser == null) {
            navigateToLoginScreen();
            return;
        }


        // Check and request notification permissions
        if (notificationsStatus != null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean hasPermissionBeenRequestedBefore = prefs.getBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED_EVER, false);

            if (isNotificationPermissionAllowed(this)) {
                notificationsStatus.setText(R.string.notifications_enabled);
            } else {
                if (!hasPermissionBeenRequestedBefore) {
                    requestNotificationPermission();
                    prefs.edit().putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED_EVER, true).apply();
                }
                notificationsStatus.setText(R.string.notifications_disabled);
            }
        }

        // Apply window insets (adjust the padding for system bars)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            adjustBottomMargin(insets);
            return insets;
        });

        bottomNavigationView = binding.bottomNavigationView;

        Intent intent = getIntent();
        if (intent != null) {
            String notificationType = intent.getStringExtra("notification_type");
            if ("event".equals(notificationType)) {
                bottomNavigationView.post(() -> bottomNavigationView.setSelectedItemId(R.id.bottom_menu_event));
            }
            if ("routine".equals(notificationType)) {
                bottomNavigationView.post(() -> bottomNavigationView.setSelectedItemId(R.id.bottom_menu_routine));
            } else if (intent.hasExtra(TASK_ID_EXTRA)) {
                int receivedTaskId = intent.getIntExtra(TASK_ID_EXTRA, -1);
                if (receivedTaskId != -1) {
                    HomeFragment homeFragment = new HomeFragment();
                    Bundle args = new Bundle();
                    args.putInt(TASK_ID_EXTRA, receivedTaskId);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, homeFragment)
                            .commit();
                } else {
                    loadDefaultFragment();
                }
            } else {
                loadDefaultFragment();
            }
        } else {
            loadDefaultFragment();
        }

        createTaskButton = findViewById(R.id.createTaskButton);
        createTaskButton.setOnClickListener(v -> {
            showAddTaskBottomSheet();
        });

        binding.bottomNavigationView.setItemIconTintList(null);
        // Set the click listener for the bottom navigation menu
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            // Check which menu item is selected
            if (id == R.id.bottom_menu_home) {
                // Open HomeFragment when home menu item is selected
                HomeFragment homeFragment = new HomeFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, homeFragment)
                        .commit();
                createTaskButton.setOnClickListener(v -> {
                    showAddTaskBottomSheet();
                });
                return true;
            } else if (id == R.id.bottom_menu_checklist) {
                // Open CheckListFragment when checklist menu item is selected
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ChecklistContainer())
                        .commit();
                // Set new listener to create checklist
                createTaskButton.setOnClickListener(v -> showAddChecklistBottomSheet());
                return true;
            } else if (id == R.id.bottom_menu_routine) {
                // Open RoutineFragment when routine menu item is selected
                RoutineFragment routineFragment = new RoutineFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, routineFragment)
                        .commit();
                createTaskButton.setOnClickListener(v -> {
                    // add effect to button
                    showAddRoutineBottomSheet();
                });
                return true;
            } else if (id == R.id.bottom_menu_event) {
                // Open EventFragment when event menu item is selected
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new EventFragment())
                        .commit();

                createTaskButton.setOnClickListener(v -> {
                    // add effect to button
                    showAddEventBottomSheet();
                });
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        boolean isDarkMode = ThemeManager.isDarkModeEnabled(this);
        updateTheme(isDarkMode);
    }

    private boolean isNotificationPermissionAllowed(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        return notificationManagerCompat.areNotificationsEnabled();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATIONS);
            } else {
                if (notificationsStatus != null) {
                    notificationsStatus.setText(R.string.notifications_enabled);
                }
            }
        } else {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            if (!notificationManagerCompat.areNotificationsEnabled()) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivityForResult(intent, REQUEST_CODE_NOTIFICATIONS);
            } else {
                if (notificationsStatus != null) {
                    notificationsStatus.setText(R.string.notifications_enabled);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (notificationsStatus != null) {
                    notificationsStatus.setText(R.string.notifications_enabled);
                }
            } else {
                if (notificationsStatus != null) {
                    notificationsStatus.setText(R.string.notifications_disabled);
                }
            }
        }
    }

    private void loadDefaultFragment() {
        HomeFragment homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
    }

    private void navigateToLoginScreen() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showAddTaskBottomSheet() {
        // Create a new instance of AddTaskBottomSheetFragment
        AddTaskBottomSheetFragment bottomSheetFragment = new AddTaskBottomSheetFragment();

        // Create a bundle to pass arguments (mode = "add" for creating a new task)
        Bundle args = new Bundle();
        args.putString("mode", "add"); // Pass arguments to the fragment
        bottomSheetFragment.setArguments(args);

        // Show the bottom sheet fragment
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());

        //blue header
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            HomeFragment.makeBlur();
        }

        // Hide the BottomAppBar when the bottom sheet is shown
        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        if (bottomAppBar != null) {
            bottomAppBar.performHide();
        }
    }

    private void showAddRoutineBottomSheet() {
        // Create a new instance of AddTaskBottomSheetFragment
        AddRoutineBottomSheetFragment bottomSheetFragment = new AddRoutineBottomSheetFragment();

        // Create a bundle to pass arguments (mode = "add" for creating a new task)
        Bundle args = new Bundle();
        args.putString("mode", "add"); // Pass arguments to the fragment
        bottomSheetFragment.setArguments(args);

        // Show the bottom sheet fragment
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());

        //blue header
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RoutineFragment.makeBlur();
        }

        // Hide the BottomAppBar when the bottom sheet is shown
        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        if (bottomAppBar != null) {
            bottomAppBar.performHide();
        }
    }

    private void showAddEventBottomSheet() {
        // Create a new instance of AddTaskBottomSheetFragment
        AddEventBottomSheetFragment bottomSheetFragment = new AddEventBottomSheetFragment();

        // Create a bundle to pass arguments (mode = "add" for creating a new task)
        Bundle args = new Bundle();
        args.putString("mode", "add"); // Pass arguments to the fragment
        bottomSheetFragment.setArguments(args);

        // Show the bottom sheet fragment
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());

        //blue header
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            EventFragment.makeBlur();
        }

        // Hide the BottomAppBar when the bottom sheet is shown
        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        if (bottomAppBar != null) {
            bottomAppBar.performHide();
        }
    }

    private void showAddChecklistBottomSheet() {
        // Create a new instance of AddTaskBottomSheetFragment
        AddChecklistBottomSheetFragment checklistBottomSheetFragment = new AddChecklistBottomSheetFragment();

        // Create a bundle to pass arguments (mode = "add" for creating a new task)
        Bundle args = new Bundle();
        args.putString("mode", "add"); // Pass arguments to the fragment
        checklistBottomSheetFragment.setArguments(args);

        // Show the bottom sheet fragment
        checklistBottomSheetFragment.show(getSupportFragmentManager(), checklistBottomSheetFragment.getTag());

        //blue header
        ChecklistContainer.makeBlur();

        // Hide the BottomAppBar when the bottom sheet is shown
        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        if (bottomAppBar != null) {
            bottomAppBar.performHide();
        }
    }

    // Method to update the app's theme (dark mode or light mode)
    private void updateTheme(boolean isDarkMode) {
        // Set the night mode based on the theme state
        int mode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Get the currently selected item ID from BottomNavigationView
        int selectedItemId = binding.bottomNavigationView.getSelectedItemId();

        // Save the selected item ID into the Bundle with the key "selected_nav_item"
        outState.putInt("selected_nav_item", selectedItemId);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Retrieve the selected item ID from the Bundle; default to settings menu if not found
        int selectedItemId = savedInstanceState.getInt("selected_nav_item", R.id.toolbar_menu_settings);

        // Set the selected item ID back to the BottomNavigationView to restore the state
        binding.bottomNavigationView.setSelectedItemId(selectedItemId);
    }

    // Method to adjust the bottom margin of BottomNavigationView
    private void adjustBottomMargin(WindowInsetsCompat insets) {
        // Get insets for system gestures and navigation bars
        Insets navigationBarsInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
        Insets systemGestureInsets = insets.getInsets(WindowInsetsCompat.Type.systemGestures());

        // Determine the bottom insets
        int bottomInset = (navigationBarsInsets.bottom == systemGestureInsets.bottom)
                ? systemGestureInsets.bottom
                : navigationBarsInsets.bottom;

        // Logic to calculate the margin to set
        int marginToSet;
        marginToSet = -bottomInset; // Add a small compensating offset

        // Set the margin for BottomNavigationView
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) binding.bottomNavigationView.getLayoutParams();
        layoutParams.bottomMargin = marginToSet;
        binding.bottomNavigationView.setLayoutParams(layoutParams);
    }

    @Override
    public void onBottomSheetDismissed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Clear blur
            HomeFragment.clearBlur();
            RoutineFragment.clearBlur();
            ChecklistContainer.clearBlur();
            EventFragment.clearBlur();
        }

        // Show the BottomAppBar
        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        if (bottomAppBar != null) {
            bottomAppBar.performShow();
        }
    }
    private void applySavedLanguage() {
        String language = getSharedPreferences("app_settings", MODE_PRIVATE)
                .getString("selected_language", "en");

        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(language);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            if (isNotificationPermissionAllowed(this)) {
                if (notificationsStatus != null) {
                    notificationsStatus.setText(R.string.notifications_enabled);
                }
            } else {
                if (notificationsStatus != null) {
                    notificationsStatus.setText(R.string.notifications_disabled);
                }
            }
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Update current intent
    }

}