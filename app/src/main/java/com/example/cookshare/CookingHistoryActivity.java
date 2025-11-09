package com.example.cookshare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookshare.adapters.RecipeAdapter;
import com.example.cookshare.models.Recipe;
import com.example.cookshare.services.FirebaseDatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CookingHistoryActivity extends AppCompatActivity {
    private static final String TAG = "CookingHistoryActivity";

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private ViewGroup emptyStateText;
    private View loadingContainer;

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

            // Load cooking history
            loadCookingHistory();
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
                    getSupportActionBar().setTitle("Lịch sử nấu ăn");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar", e);
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
            recipeAdapter = new RecipeAdapter();
            recipeAdapter.setOnRecipeClickListener(recipe -> {
                try {
                    // Navigate to RecipeDetailActivity
                    Intent intent = new Intent(CookingHistoryActivity.this, RecipeDetailActivity.class);
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
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to RecipeDetailActivity", e);
                    Toast.makeText(CookingHistoryActivity.this, "Lỗi mở chi tiết công thức", Toast.LENGTH_SHORT).show();
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(recipeAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void loadCookingHistory() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading();

        // Load cooking history từ Firebase
        databaseService.loadCookingHistory(new FirebaseDatabaseService.CookingHistoryCallback() {
            @Override
            public void onSuccess(List<String> recipeIds) {
                hideLoading();

                if (recipeIds == null || recipeIds.isEmpty()) {
                    Log.d(TAG, "No cooking history found");
                    showEmptyState();
                    return;
                }

                Log.d(TAG, "Found " + recipeIds.size() + " recipes in cooking history");
                // Load chi tiết từng recipe từ foods node
                loadRecipeDetails(recipeIds);
            }

            @Override
            public void onError(String error) {
                hideLoading();
                Log.e(TAG, "Error loading cooking history: " + error);
                Toast.makeText(CookingHistoryActivity.this, "Lỗi tải lịch sử: " + error, Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void loadRecipeDetails(List<String> recipeIds) {
        // Load từ foods node (nơi chứa VietnameseFood)
        String databaseUrl = "https://cookshare-88d53-default-rtdb.asia-southeast1.firebasedatabase.app/";
        com.google.firebase.database.DatabaseReference foodsRef = com.google.firebase.database.FirebaseDatabase
                .getInstance(databaseUrl)
                .getReference("foods");
        final List<Recipe> historyRecipes = new ArrayList<>();
        final int[] loadedCount = { 0 };
        final int totalRecipes = recipeIds.size();

        Log.d(TAG, "Loading " + totalRecipes + " recipe details from foods node");

        if (recipeIds.isEmpty()) {
            showEmptyState();
            return;
        }

        for (String recipeId : recipeIds) {
            final String finalRecipeId = recipeId;
            foodsRef.child(recipeId)
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            synchronized (historyRecipes) {
                                try {
                                    if (snapshot.exists()) {
                                        com.example.cookshare.models.VietnameseFood vietnameseFood = snapshot
                                                .getValue(com.example.cookshare.models.VietnameseFood.class);
                                        if (vietnameseFood != null) {
                                            Recipe recipe = vietnameseFood.toRecipe();
                                            recipe.setId(snapshot.getKey());

                                            // Read dynamic values từ Firebase
                                            if (snapshot.hasChild("rating")) {
                                                Object ratingObj = snapshot.child("rating").getValue();
                                                if (ratingObj != null) {
                                                    double rating = ratingObj instanceof Double ? (Double) ratingObj
                                                            : ratingObj instanceof Long
                                                                    ? ((Long) ratingObj).doubleValue()
                                                                    : ((Number) ratingObj).doubleValue();
                                                    recipe.setRating(rating);
                                                }
                                            }
                                            if (snapshot.hasChild("likeCount")) {
                                                Object likeCountObj = snapshot.child("likeCount").getValue();
                                                if (likeCountObj != null) {
                                                    int likeCount = likeCountObj instanceof Long
                                                            ? ((Long) likeCountObj).intValue()
                                                            : ((Integer) likeCountObj).intValue();
                                                    recipe.setLikeCount(likeCount);
                                                }
                                            }
                                            if (snapshot.hasChild("viewCount")) {
                                                Object viewCountObj = snapshot.child("viewCount").getValue();
                                                if (viewCountObj != null) {
                                                    int viewCount = viewCountObj instanceof Long
                                                            ? ((Long) viewCountObj).intValue()
                                                            : ((Integer) viewCountObj).intValue();
                                                    recipe.setViewCount(viewCount);
                                                }
                                            }
                                            if (snapshot.hasChild("cookTime")) {
                                                Object cookTimeObj = snapshot.child("cookTime").getValue();
                                                if (cookTimeObj != null) {
                                                    int cookTime = cookTimeObj instanceof Long
                                                            ? ((Long) cookTimeObj).intValue()
                                                            : ((Integer) cookTimeObj).intValue();
                                                    recipe.setCookTime(cookTime);
                                                }
                                            }
                                            if (snapshot.hasChild("prepTime")) {
                                                Object prepTimeObj = snapshot.child("prepTime").getValue();
                                                if (prepTimeObj != null) {
                                                    int prepTime = prepTimeObj instanceof Long
                                                            ? ((Long) prepTimeObj).intValue()
                                                            : ((Integer) prepTimeObj).intValue();
                                                    recipe.setPrepTime(prepTime);
                                                }
                                            }
                                            if (snapshot.hasChild("servings")) {
                                                Object servingsObj = snapshot.child("servings").getValue();
                                                if (servingsObj != null) {
                                                    int servings = servingsObj instanceof Long
                                                            ? ((Long) servingsObj).intValue()
                                                            : ((Integer) servingsObj).intValue();
                                                    recipe.setServings(servings);
                                                }
                                            }
                                            if (snapshot.hasChild("difficulty")) {
                                                Object difficultyObj = snapshot.child("difficulty").getValue();
                                                if (difficultyObj != null) {
                                                    recipe.setDifficulty(difficultyObj.toString());
                                                }
                                            }

                                            historyRecipes.add(recipe);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing recipe: " + finalRecipeId, e);
                                }

                                loadedCount[0]++;
                                if (loadedCount[0] >= totalRecipes) {
                                    runOnUiThread(() -> {
                                        if (historyRecipes.isEmpty()) {
                                            showEmptyState();
                                        } else {
                                            hideEmptyState();
                                            if (recipeAdapter != null) {
                                                recipeAdapter.updateRecipes(new ArrayList<>(historyRecipes));
                                            }
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                            synchronized (historyRecipes) {
                                loadedCount[0]++;
                                Log.e(TAG, "Error loading recipe " + finalRecipeId, error.toException());
                                if (loadedCount[0] >= totalRecipes) {
                                    runOnUiThread(() -> {
                                        if (historyRecipes.isEmpty()) {
                                            showEmptyState();
                                        } else {
                                            hideEmptyState();
                                            if (recipeAdapter != null) {
                                                recipeAdapter.updateRecipes(new ArrayList<>(historyRecipes));
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
        }
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
                        textView.setText("Chưa có lịch sử nấu ăn");
                    } else if (currentText.contains("Hãy thích")) {
                        textView.setText("Lịch sử nấu ăn sẽ được lưu khi bạn xem các công thức");
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
