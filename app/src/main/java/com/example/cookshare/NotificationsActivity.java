package com.example.cookshare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookshare.R;
import com.example.cookshare.adapters.NotificationAdapter;
import com.example.cookshare.services.FirebaseDatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {
    private static final String TAG = "NotificationsActivity";

    private RecyclerView recyclerView;
    private ViewGroup emptyStateText;
    private View loadingContainer;
    private NotificationAdapter notificationAdapter;
    private FirebaseDatabaseService databaseService;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_favorite_recipes); // Dùng chung layout

            // Initialize Firebase
            mAuth = FirebaseAuth.getInstance();
            databaseService = new FirebaseDatabaseService();

            // Setup toolbar
            setupToolbar();

            // Initialize views
            initializeViews();

            // Setup RecyclerView
            setupRecyclerView();

            // Load notifications
            loadNotifications();

            // DEBUG: Thêm button test để tạo notification
            // TODO: Xóa sau khi test xong
            setupTestButton();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Lỗi khởi tạo màn hình: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupToolbar() {
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Thông báo");
                }
            }
        } catch (Exception e) {
            android.util.Log.e("NotificationsActivity", "Error setting up toolbar", e);
        }
    }

    private void initializeViews() {
        try {
            recyclerView = findViewById(R.id.recyclerView);
            emptyStateText = findViewById(R.id.emptyStateText);
            loadingContainer = findViewById(R.id.loadingContainer);

            if (recyclerView == null) {
                Log.e(TAG, "recyclerView is null!");
            }
            if (emptyStateText == null) {
                Log.e(TAG, "emptyStateText is null!");
            }
            if (loadingContainer == null) {
                Log.e(TAG, "loadingContainer is null!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    private void setupRecyclerView() {
        if (recyclerView == null) {
            Log.e(TAG, "Cannot setup RecyclerView: recyclerView is null");
            return;
        }

        try {
            notificationAdapter = new NotificationAdapter();
            notificationAdapter.setOnNotificationClickListener(notification -> {
                try {
                    // Mark as read
                    String notificationId = (String) notification.get("id");
                    if (notificationId != null) {
                        databaseService.markNotificationAsRead(notificationId);
                        // Update UI - reload notifications to refresh
                        loadNotifications();
                    }

                    // Navigate to recipe if available
                    String recipeId = (String) notification.get("recipeId");
                    if (recipeId != null && !recipeId.isEmpty()) {
                        // TODO: Navigate to recipe detail
                        Toast.makeText(this, "Chuyển đến công thức: " + recipeId, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error handling notification click", e);
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(notificationAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void loadNotifications() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading();

        databaseService.loadNotifications(new FirebaseDatabaseService.NotificationListCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> notifications) {
                hideLoading();

                if (notifications == null || notifications.isEmpty()) {
                    Log.d(TAG, "No notifications found");
                    showEmptyState();
                    return;
                }

                Log.d(TAG, "Found " + notifications.size() + " notifications");
                hideEmptyState();
                if (notificationAdapter != null) {
                    notificationAdapter.updateNotifications(notifications);
                }
            }

            @Override
            public void onError(String error) {
                hideLoading();
                Log.e(TAG, "Error loading notifications: " + error);
                Toast.makeText(NotificationsActivity.this, "Lỗi tải thông báo: " + error, Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showLoading() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.VISIBLE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.GONE);
        }
    }

    private void hideLoading() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.VISIBLE);
            // Update empty state text
            for (int i = 0; i < emptyStateText.getChildCount(); i++) {
                View child = emptyStateText.getChildAt(i);
                if (child instanceof TextView) {
                    TextView textView = (TextView) child;
                    String currentText = textView.getText().toString();
                    if (currentText.contains("yêu thích")) {
                        textView.setText("Chưa có thông báo");
                    } else if (currentText.contains("Hãy thích")) {
                        textView.setText("Các thông báo mới sẽ hiển thị ở đây");
                    }
                }
            }
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupTestButton() {
        // Tạo một FloatingActionButton để test notification
        // CHỈ DÙNG ĐỂ TEST - XÓA SAU KHI TEST XONG
        try {
            com.google.android.material.floatingactionbutton.FloatingActionButton testFab = new com.google.android.material.floatingactionbutton.FloatingActionButton(
                    this);
            testFab.setImageResource(android.R.drawable.ic_menu_add);
            testFab.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

            // Add to root view if possible
            View rootView = findViewById(android.R.id.content);
            if (rootView instanceof android.view.ViewGroup) {
                android.view.ViewGroup root = (android.view.ViewGroup) rootView;
                android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
                params.setMargins(16, 16, 16, 16);
                testFab.setLayoutParams(params);
                root.addView(testFab);

                testFab.setOnClickListener(v -> {
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        Log.d(TAG, "🧪 TEST: Creating test notification for user: " + currentUser.getUid());
                        databaseService.createNotification(
                                currentUser.getUid(),
                                "test",
                                "Test Notification",
                                "Đây là notification test để kiểm tra xem code có hoạt động không!",
                                "test-recipe-id");
                        Toast.makeText(this, "Đã tạo notification test! Refresh để xem.", Toast.LENGTH_SHORT).show();
                        // Reload notifications sau 1 giây
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            loadNotifications();
                        }, 1000);
                    } else {
                        Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up test button", e);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
