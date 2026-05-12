package com.swap.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.swap.app.R;
import com.swap.app.network.SupabaseClient;
import com.swap.app.utils.SessionManager;
import org.json.JSONObject;

public class AuthActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etUsername;
    private Button btnAction;
    private TextView tvToggle, tvTitle, tvSubtitle;
    private LinearLayout layoutUsername;
    private ProgressBar progressBar;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        btnAction = findViewById(R.id.btnAction);
        tvToggle = findViewById(R.id.tvToggle);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        layoutUsername = findViewById(R.id.layoutUsername);
        progressBar = findViewById(R.id.progressBar);

        updateUI();

        btnAction.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isLoginMode) {
                doSignIn(email, password);
            } else {
                String username = etUsername.getText().toString().trim();
                if (username.isEmpty()) {
                    Toast.makeText(this, "Enter a username", Toast.LENGTH_SHORT).show();
                    return;
                }
                doSignUp(email, password, username);
            }
        });

        tvToggle.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateUI();
        });
    }

    private void updateUI() {
        if (isLoginMode) {
            tvTitle.setText("Welcome Back");
            tvSubtitle.setText("Sign in to continue");
            btnAction.setText("Sign In");
            tvToggle.setText("Don't have an account? Sign Up");
            layoutUsername.setVisibility(View.GONE);
        } else {
            tvTitle.setText("Create Account");
            tvSubtitle.setText("Join Swap today");
            btnAction.setText("Sign Up");
            tvToggle.setText("Already have an account? Sign In");
            layoutUsername.setVisibility(View.VISIBLE);
        }
    }

    private void doSignIn(String email, String password) {
        setLoading(true);
        SupabaseClient.getInstance().signIn(email, password, new SupabaseClient.AuthCallback() {
            @Override
            public void onSuccess(JSONObject data) {
                runOnUiThread(() -> {
                    setLoading(false);
                    try {
                        String token = data.optString("access_token");
                        String refreshToken = data.optString("refresh_token");
                        JSONObject userObj = data.optJSONObject("user");
                        String userId = userObj != null ? userObj.optString("id") : "";
                        String userEmail = userObj != null ? userObj.optString("email") : email;
                        SessionManager.getInstance(AuthActivity.this)
                                .saveSession(token, refreshToken, userId, userEmail);
                        goToMain();
                    } catch (Exception e) {
                        Toast.makeText(AuthActivity.this, "Login error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(AuthActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void doSignUp(String email, String password, String username) {
        setLoading(true);
        SupabaseClient.getInstance().signUp(email, password, username, new SupabaseClient.AuthCallback() {
            @Override
            public void onSuccess(JSONObject data) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(AuthActivity.this,
                            "Account created! Check your email to verify.", Toast.LENGTH_LONG).show();
                    isLoginMode = true;
                    updateUI();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(AuthActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnAction.setEnabled(!loading);
    }
  }
