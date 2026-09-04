package com.smartfamily.studentplanner.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;


public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private ImageButton backButton;
    private ImageView editNameIcon;
    private TextView userNameTextView;
    private TextView userNicknameTextView;
    private TextView passwordLabel;
    private TextInputLayout passwordInputLayout;
    private LinearLayout forgetPasswordLayout;
    private TextView resetTextView;
    private TextInputEditText usernameEditText;
    private TextInputEditText numberEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button logoutButton;

    private TaskViewModel taskViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        EdgeToEdge.enable(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_profile_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        resetTextView.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });
        backButton.setOnClickListener(v -> finish());
        editNameIcon.setOnClickListener(v -> Toast.makeText(ProfileActivity.this, R.string.edit_name, Toast.LENGTH_SHORT).show());

        logoutButton.setOnClickListener(v -> performLogout());
    }

    private void initViews() {
        auth = FirebaseAuth.getInstance();
        backButton = findViewById(R.id.backButton);
        editNameIcon = findViewById(R.id.editNameIcon);
        userNameTextView = findViewById(R.id.userNameTextView);
        userNicknameTextView = findViewById(R.id.userNicknameTextView);

        usernameEditText = findViewById(R.id.usernameEditText);
        numberEditText = findViewById(R.id.numberEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordLabel = findViewById(R.id.passwordLabel);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        forgetPasswordLayout = findViewById(R.id.forgetPasswordLayout);

        if (!isPasswordProviderExist()) {
            hidePasswordBlock();
        } else {
            showPasswordBlock();
        }

        resetTextView = findViewById(R.id.resetTextView);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void hidePasswordBlock() {
        passwordLabel.setVisibility(View.GONE);
        passwordEditText.setVisibility(View.GONE);
        passwordInputLayout.setVisibility(View.GONE);
        forgetPasswordLayout.setVisibility(View.GONE);
    }

    private void showPasswordBlock() {
        passwordLabel.setVisibility(View.VISIBLE);
        passwordEditText.setVisibility(View.VISIBLE);
        passwordInputLayout.setVisibility(View.VISIBLE);
        forgetPasswordLayout.setVisibility(View.VISIBLE);
    }

    private boolean isPasswordProviderExist() {
        if (auth.getCurrentUser() != null) {
           return auth.getCurrentUser().getProviderData().stream()
                   .map(UserInfo::getProviderId)
                   .anyMatch(p -> p.equals("password"));
        }
        return false;
    }

    private void performLogout() {
        taskViewModel.logout();

        Toast.makeText(ProfileActivity.this, R.string.successfully_logged_out, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
