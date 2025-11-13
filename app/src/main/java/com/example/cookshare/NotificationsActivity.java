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
import android.content.Intent;
import androidx.annotation.NonNull;
import com.example.cookshare.models.Recipe;
import com.example.cookshare.models.VietnameseFood;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Lỗi khởi tạo màn hình: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
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
                    // 1. Đánh dấu là đã đọc
                    String notificationId = (String) notification.get("id");
                    if (notificationId != null) {
                        databaseService.markNotificationAsRead(notificationId);
                        // GHI CHÚ: Đã xóa loadNotifications() ở đây.
                        // Danh sách sẽ được làm mới trong onResume() (đã thêm ở bước 1)
                        // khi người dùng quay lại màn hình này.
                    }

                    // 2. Lấy recipeId để điều hướng
                    String recipeId = (String) notification.get("recipeId");

                    // --- BẮT ĐẦU CODE HOÀN CHỈNH PHẦN TODO ---

                    // Kiểm tra xem recipeId có hợp lệ không
                    if (recipeId != null && !recipeId.isEmpty()) {

                        Log.d(TAG, "Đang điều hướng đến recipeId: " + recipeId);
                        Toast.makeText(this, "Đang mở công thức...", Toast.LENGTH_SHORT).show();

                        // Chúng ta phải tải chi tiết công thức đầy đủ từ Firebase
                        // vì RecipeDetailActivity yêu cầu tất cả dữ liệu qua Intent.
                        // Logic này được tham khảo từ FavoriteRecipesActivity

                        // (Giả định bạn có model VietnameseFood như trong FavoriteRecipesActivity)
                        String databaseUrl = "https://cookshare-88d53-default-rtdb.asia-southeast1.firebasedatabase.app/";
                        DatabaseReference recipeRef = FirebaseDatabase.getInstance(databaseUrl)
                                .getReference("foods")
                                .child(recipeId);

                        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    // Phân tích công thức từ node "foods"
                                    com.example.cookshare.models.VietnameseFood vietnameseFood = snapshot
                                            .getValue(com.example.cookshare.models.VietnameseFood.class);

                                    if (vietnameseFood != null) {
                                        Recipe recipe = vietnameseFood.toRecipe();
                                        recipe.setId(snapshot.getKey()); // QUAN TRỌNG: Dùng key của snapshot làm ID

                                        // Khởi chạy Intent với đầy đủ các extras
                                        // (Giống hệt FavoriteRecipesActivity)
                                        Intent intent = new Intent(NotificationsActivity.this, RecipeDetailActivity.class);
                                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
                                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_TITLE, recipe.getTitle());
                                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_DESCRIPTION, recipe.getDescription());
                                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_IMAGE_URL, recipe.getImageUrl());
                                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_AUTHOR_ID, recipe.getAuthorId());
                                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_AUTHOR_NAME, recipe.getAuthorName());
                                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_PREP_TIME, recipe.getPrepTime());
                                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_COOK_TIME, recipe.getCookTime());
                                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_SERVINGS, recipe.getServings());
                                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_DIFFICULTY, recipe.getDifficulty());
                                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_RATING, recipe.getRating());

                                        if (recipe.getCategories() != null) {
                                            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_CATEGORIES,
                                                    recipe.getCategories().toArray(new String[0]));
                                        }
                                        if (recipe.getIngredients() != null) {
                                            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_INGREDIENTS,
                                                    recipe.getIngredients().toArray(new String[0]));
                                        }
                                        if (recipe.getInstructions() != null) {
                                            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_INSTRUCTIONS,
                                                    recipe.getInstructions().toArray(new String[0]));
                                        }

                                        startActivity(intent);

                                    } else {
                                        Log.e(TAG, "Lỗi phân tích model VietnameseFood cho recipeId: " + recipeId);
                                        Toast.makeText(NotificationsActivity.this, "Lỗi: Không thể phân tích công thức", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Xử lý trường hợp công thức đã bị xóa nhưng thông báo vẫn còn
                                    Log.w(TAG, "Không tìm thấy công thức trong node 'foods' cho recipeId: " + recipeId);
                                    Toast.makeText(NotificationsActivity.this, "Lỗi: Không tìm thấy công thức", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Lỗi tải chi tiết công thức", error.toException());
                                Toast.makeText(NotificationsActivity.this, "Lỗi tải công thức: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

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


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
