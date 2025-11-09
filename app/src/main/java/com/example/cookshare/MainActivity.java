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
import com.example.cookshare.services.FirebaseDatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigation;
    private CircularProgressIndicator progressIndicator;
    private View fragmentContainer;
    private View loadingContainer;
    private int lastSelectedTabId = R.id.nav_home; // Lưu tab được chọn trước đó

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

        // Initialize foods default values (chỉ chạy một lần)
        initializeFoodsDefaultValuesOnce();
    }

    private void initializeFoodsDefaultValuesOnce() {
        SharedPreferences prefs = getSharedPreferences("CookSharePrefs", MODE_PRIVATE);
        boolean hasInitialized = prefs.getBoolean("foods_initialized", false);

        if (!hasInitialized) {
            FirebaseDatabaseService databaseService = new FirebaseDatabaseService();
            databaseService.initializeFoodsDefaultValues();

            // Đánh dấu đã initialize
            prefs.edit().putBoolean("foods_initialized", true).apply();
            Log.d(TAG, "Initializing foods default values...");
        }
    }

    private void initializeViews() {
        progressIndicator = findViewById(R.id.progressIndicator);
        fragmentContainer = findViewById(R.id.fragment_container);
        loadingContainer = findViewById(R.id.loadingContainer);
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
                            lastSelectedTabId = R.id.nav_home;
                        } else if (itemId == R.id.nav_search) {
                            selectedFragment = new SearchFragment();
                            title = "Tìm kiếm";
                            lastSelectedTabId = R.id.nav_search;
                        } else if (itemId == R.id.nav_chatbot) {
                            // Start ChatbotActivity (không lưu tab này vì nó là activity riêng)
                            Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
                            startActivity(intent);
                            // Không update lastSelectedTabId vì chatbot là activity riêng
                            return true;
                        } else if (itemId == R.id.nav_add) {
                            selectedFragment = new AddRecipeFragment();
                            title = "Thêm công thức";
                            lastSelectedTabId = R.id.nav_add;
                        } else if (itemId == R.id.nav_profile) {
                            selectedFragment = new ProfileFragment();
                            title = "Hồ sơ";
                            lastSelectedTabId = R.id.nav_profile;
                        }

                        if (selectedFragment != null) {
                            // Show loading indicator ngay lập tức
                            showLoading();

                            // Make variables final for lambda
                            final Fragment finalSelectedFragment = selectedFragment;
                            final String finalTitle = title;

                            // Delay ngắn để loading hiển thị mượt mà hơn
                            fragmentContainer.postDelayed(() -> {
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, finalSelectedFragment)
                                        .commit();

                                if (getSupportActionBar() != null) {
                                    getSupportActionBar().setTitle(finalTitle);
                                }

                                // Hide loading indicator sau khi fragment đã được replace
                                fragmentContainer.postDelayed(() -> {
                                    hideLoading();
                                }, 100); // Delay nhỏ để đảm bảo fragment đã render
                            }, 200); // 200ms delay cho smooth transition
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
            fragmentContainer.postDelayed(() -> {
                hideLoading();
            }, 100);
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

    @Override
    protected void onResume() {
        super.onResume();
        // Khi quay lại từ ChatbotActivity, reset tab về tab đã chọn trước đó
        // (vì chatbot là activity riêng, không phải fragment)
        if (bottomNavigation != null) {
            int currentSelectedId = bottomNavigation.getSelectedItemId();
            // Nếu đang selected tab chatbot (không hợp lệ vì chatbot là activity), reset về
            // tab trước đó
            if (currentSelectedId == R.id.nav_chatbot) {
                // Set selected item mà không trigger listener (vì đã được handle trong
                // listener)
                bottomNavigation.post(() -> {
                    bottomNavigation.setSelectedItemId(lastSelectedTabId);
                });
            }
        }
    }

    private void showLoading() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.VISIBLE);
        }
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.GONE);
        }
    }

    private void hideLoading() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.GONE);
        }
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
        }
    }

    // Public method to navigate to HomeFragment
    public void navigateToHome() {
        if (bottomNavigation != null) {
            // Select Home tab in bottom navigation
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else {
            // Fallback: directly replace fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Trang chủ");
            }
        }
    }

    // Public method to navigate to AddRecipeFragment
    public void navigateToAddRecipe() {
        Log.d(TAG, "navigateToAddRecipe() called");

        // Trigger navigation bằng cách programmatically click vào nav_add item
        // Cách này sẽ trigger listener tự nhiên và có tất cả logic (loading, delay,
        // etc.)
        if (bottomNavigation != null) {
            MenuItem addItem = bottomNavigation.getMenu().findItem(R.id.nav_add);
            if (addItem != null) {
                // Trigger listener bằng cách set selected item (sẽ tự động trigger listener)
                bottomNavigation.setSelectedItemId(R.id.nav_add);
                Log.d(TAG, "Bottom navigation item selected");
            } else {
                Log.e(TAG, "nav_add menu item not found");
                // Fallback: replace fragment trực tiếp
                replaceFragmentDirectly(new AddRecipeFragment(), "Thêm công thức");
            }
        } else {
            Log.e(TAG, "bottomNavigation is null");
            // Fallback: replace fragment trực tiếp
            replaceFragmentDirectly(new AddRecipeFragment(), "Thêm công thức");
        }
    }

    // Helper method để replace fragment trực tiếp (fallback)
    private void replaceFragmentDirectly(Fragment fragment, String title) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}