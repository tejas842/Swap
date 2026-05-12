package com.swap.app.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.swap.app.R;
import com.swap.app.fragments.ChatFragment;
import com.swap.app.fragments.CommunitiesFragment;
import com.swap.app.fragments.SwapFragment;
import com.swap.app.network.SupabaseClient;
import com.swap.app.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ensure token is loaded
        SessionManager session = SessionManager.getInstance(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        SupabaseClient.getInstance().setAccessToken(session.getToken());

        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_chat) {
                loadFragment(new ChatFragment());
            } else if (id == R.id.nav_swap) {
                loadFragment(new SwapFragment());
            } else if (id == R.id.nav_communities) {
                loadFragment(new CommunitiesFragment());
            }
            return true;
        });

        // Default tab
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_chat);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
