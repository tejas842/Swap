`package com.swap.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.swap.app.R;
import com.swap.app.network.SupabaseClient;
import com.swap.app.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            SessionManager session = SessionManager.getInstance(this);
            if (session.isLoggedIn()) {
                SupabaseClient.getInstance().setAccessToken(session.getToken());
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, AuthActivity.class));
            }
            finish();
        }, 1500);
    }
}
