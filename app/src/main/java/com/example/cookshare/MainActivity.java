package com.example.cookshare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import com.example.cookshare.fragments.AddRecipeFragment;
import com.example.cookshare.fragments.HomeFragment;
import com.example.cookshare.fragments.ProfileFragment;
import com.example.cookshare.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigation;
    private CircularProgressIndicator progressIndicator;
    private View fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Setup bottom navigation
        setupBottomNavigation();

        // Check if user is signed in
        checkUserSignIn();
    }

    private void initializeViews() {
        progressIndicator = findViewById(R.id.progressIndicator);
        fragmentContainer = findViewById(R.id.fragment_container);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("CookShare");
            }
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation
                .setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        String title = "";

                        int itemId = item.getItemId();
                        if (itemId == R.id.nav_home) {
                            selectedFragment = new HomeFragment();
                            title = "Trang chủ";
                        } else if (itemId == R.id.nav_search) {
                            selectedFragment = new SearchFragment();
                            title = "Tìm kiếm";
                        } else if (itemId == R.id.nav_chatbot) {
                            // Start ChatbotActivity
                            Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
                            startActivity(intent);
                            return true;
                        } else if (itemId == R.id.nav_add) {
                            selectedFragment = new AddRecipeFragment();
                            title = "Thêm công thức";
                        } else if (itemId == R.id.nav_profile) {
                            selectedFragment = new ProfileFragment();
                            title = "Hồ sơ";
                        }

                        if (selectedFragment != null) {
                            // Show loading indicator
                            showLoading();

                            // Make variables final for lambda
                            final Fragment finalSelectedFragment = selectedFragment;
                            final String finalTitle = title;

                            // Simulate loading delay for better UX
                            fragmentContainer.postDelayed(() -> {
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, finalSelectedFragment)
                                        .commit();

                                if (getSupportActionBar() != null) {
                                    getSupportActionBar().setTitle(finalTitle);
                                }

                                // Hide loading indicator
                                hideLoading();
                            }, 300); // 300ms delay for smooth transition
                        }

                        return true;
                    }
                });

        // Set default fragment
        showLoading();
        fragmentContainer.postDelayed(() -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            hideLoading();
        }, 200);
    }

    private void checkUserSignIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not signed in, redirect to LoginActivity
            Log.d(TAG, "User not signed in, redirecting to LoginActivity");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // User is signed in, show welcome message
            Log.d(TAG, "User signed in: " + currentUser.getEmail());
            String displayName = currentUser.getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = currentUser.getEmail();
            }
            Toast.makeText(this, "Chào mừng " + displayName + "!", Toast.LENGTH_SHORT).show();
        }
    }

    // Removed toolbar menu - logout is now handled in ProfileFragment

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not signed in, redirect to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void showLoading() {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.VISIBLE);
        }
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.GONE);
        }
    }

    private void hideLoading() {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.GONE);
        }
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
        }
    }
}