package com.smartfamily.studentplanner.ui;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.smartfamily.studentplanner.R;
import com.smartfamily.studentplanner.data.viewModel.TaskViewModel;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView resetTextView;
    private EditText emailEditText, passwordEditText;
    private Button continueButton;
    private AlertDialog progressDialog;
    private LinearLayout googleLoginButton;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private TaskViewModel taskViewModel;
    private LoadingFragment loadingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // If the user is already logged in, we immediately go to the main screen
        if (taskViewModel.isLoggedIn()) {
            goToMainActivity();
            return;
        }

        emailEditText = findViewById(R.id.emailInput);
        passwordEditText = findViewById(R.id.passwordInput);
        continueButton = findViewById(R.id.continueButton);
        googleLoginButton = findViewById(R.id.googleLoginButton);
        resetTextView = findViewById(R.id.resetTextView);

        continueButton.setOnClickListener(v -> loginUser());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleLoginButton.setOnClickListener(v -> signInWithGoogle());
        resetTextView.setOnClickListener(v -> showResetPasswordConfirmationDialog());
    }

    private void signInWithGoogle() {
        showLoading();

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);

                hideLoading();
                String errorMessage = getGoogleSignInErrorMessage(e.getStatusCode());
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    hideLoading();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, getString(R.string.google_sign_in_success) + ": " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        taskViewModel.setLoggedIn(true);
                        goToMainActivity();
                    } else {
                        Log.w(TAG, "firebaseAuthWithGoogle failed", task.getException());
                        Exception exception = task.getException();
                        String errorMessage;

                        if (exception instanceof FirebaseAuthException) {
                            errorMessage = getErrorMessage(exception);
                        } else {
                            errorMessage = getString(R.string.google_sign_in_failed_firebase_auth);
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            hideLoading();
            Toast.makeText(this, getString(R.string.email_and_password_required), Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    hideLoading();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            taskViewModel.setLoggedIn(true);
                            goToMainActivity();
                        } else if (user != null) {
                            user.sendEmailVerification();
                            showVerificationWaiting(user);
                        }
                    } else {
                        Exception exception = task.getException();
                        String errorCode = "";

                        if (exception instanceof FirebaseAuthException) {
                            errorCode = ((FirebaseAuthException) exception).getErrorCode();
                        }

                        handleLoginError(errorCode, exception, email, password);
                    }
                });
    }

    private void handleLoginError(String errorCode, Exception exception, String email, String password) {
        switch (errorCode) {
            case "ERROR_USER_NOT_FOUND":
            case "ERROR_INVALID_CREDENTIAL":
                registerNewUser(email, password);
                break;
            case "ERROR_WRONG_PASSWORD":
                Toast.makeText(this, getString(R.string.error_invalid_password), Toast.LENGTH_SHORT).show();
                break;
            case "ERROR_INVALID_EMAIL":
                Toast.makeText(this, getString(R.string.error_bad_email_format), Toast.LENGTH_SHORT).show();
                break;
            default:
                assert exception != null;
                Toast.makeText(this, getString(R.string.unknown_error) + ": " + exception.getMessage(), Toast.LENGTH_LONG).show();
                break;
        }
    }

    private String getGoogleSignInErrorMessage(int statusCode) {
        switch (statusCode) {
            case CommonStatusCodes.CANCELED:
                return getString(R.string.google_sign_in_cancelled);
            case CommonStatusCodes.NETWORK_ERROR:
                return getString(R.string.error_network);
            case CommonStatusCodes.DEVELOPER_ERROR:
                return getString(R.string.google_sign_in_developer_error);
            case CommonStatusCodes.INTERNAL_ERROR:
                return getString(R.string.google_sign_in_internal_error);
            default:
                return getString(R.string.google_sign_in_failed_unknown);
        }
    }

    private void registerNewUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(registerTask -> {
                    if (registerTask.isSuccessful()) {
                        FirebaseUser newUser = mAuth.getCurrentUser();
                        if (newUser != null) {
                            newUser.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            showVerificationWaiting(newUser);
                                        } else {
                                            Toast.makeText(this, getString(R.string.failed_to_send_verification_email), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        String error = getErrorMessage(registerTask.getException());
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getErrorMessage(Exception e) {
        if (e == null) return getString(R.string.unknown_error);

        if (e instanceof FirebaseAuthWeakPasswordException) {
            return getString(R.string.error_weak_password);
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return getString(R.string.error_bad_email_format);
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            return getString(R.string.error_invalid_password);
        } else if (e instanceof FirebaseAuthInvalidUserException) {
            return getString(R.string.error_no_user);
        }

        return getString(R.string.unknown_error) + ": " + e.getMessage();
    }

    private void showVerificationWaiting(FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirmation_required))
                .setMessage(getString(R.string.check_email_to_verify_account));

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
            if (user != null && !user.isEmailVerified()) {
                user.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseAuth.getInstance().signOut();
                        taskViewModel.setLoggedIn(false);
                    }
                });
            }
            dialog.dismiss();
        });

        ProgressBar progressBar = new ProgressBar(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(progressBar);
        builder.setView(layout);

        progressDialog = builder.create();
        progressDialog.show();

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable checkEmailVerified = new Runnable() {
            @Override
            public void run() {
                user.reload().addOnCompleteListener(task -> {
                    if (user.isEmailVerified()) {
                        progressDialog.dismiss();
                        taskViewModel.setLoggedIn(true);
                        goToMainActivity();
                    } else {
                        handler.postDelayed(this, 3000);
                    }
                });
            }
        };

        handler.postDelayed(checkEmailVerified, 3000);
    }

    private void showResetPasswordConfirmationDialog() {
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
        messageText.setText(R.string.reset_password_question);

        btnYes.setOnClickListener(v1 -> {
            resetPassword();
            dialog.dismiss();

        });

        btnNo.setOnClickListener(v1 -> dialog.dismiss());
    }

    private void resetPassword() {
        if (!isInternetAvailable()) {
            Toast.makeText(this, getString(R.string.net_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, getString(R.string.email_require), Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    hideLoading();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.link_is_sent), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, getString(R.string.error_no_user), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showLoading() {
        if (loadingFragment == null) {
            loadingFragment = new LoadingFragment();
        }
        if (!loadingFragment.isAdded()) {
            loadingFragment.show(getSupportFragmentManager(), "loading_dialog");
        }
    }

    private void hideLoading() {
        if (loadingFragment != null && loadingFragment.isAdded()) {
            loadingFragment.dismiss();
            loadingFragment = null;
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }
}