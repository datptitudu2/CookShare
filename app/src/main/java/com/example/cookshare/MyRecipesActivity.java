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

import java.util.List;

public class MyRecipesActivity extends AppCompatActivity {
    private static final String TAG = "MyRecipesActivity";

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private ViewGroup emptyStateText;
    private View loadingContainer;

    private FirebaseDatabaseService databaseService;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // Load user's recipes
        loadMyRecipes();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Công thức của tôi");
        }
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        loadingContainer = findViewById(R.id.loadingContainer);
    }

    private void setupRecyclerView() {
        recipeAdapter = new RecipeAdapter();
        recipeAdapter.setOnRecipeClickListener(recipe -> {
            // Navigate to RecipeDetailActivity
            Intent intent = new Intent(MyRecipesActivity.this, RecipeDetailActivity.class);
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
            intent.putExtra("recipe_rating_count", recipe.getRatingCount());

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

            startActivityForResult(intent, 200); // Request code 200 for recipe detail/edit/delete
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recipeAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            // Always reload when coming back from RecipeDetailActivity
            // This ensures UI is updated immediately after edit/delete/view/rating
            // Check if rating was updated
            boolean ratingUpdated = data != null && data.getBooleanExtra("rating_updated", false);
            if (ratingUpdated) {
                Log.d(TAG, "Rating was updated in RecipeDetailActivity, reloading recipes list...");
            } else {
                Log.d(TAG, "RecipeDetailActivity returned (resultCode: " + resultCode
                        + "), reloading recipes list immediately...");
            }
            // Small delay to ensure Firebase has synced
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                loadMyRecipes();
            }, 500); // 500ms delay to ensure Firebase sync
        }
    }

    private void loadMyRecipes() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading();

        // Use getUserRecipes from FirebaseDatabaseService to load from foods node
        databaseService.getUserRecipes(currentUser.getUid(), new FirebaseDatabaseService.RecipeListCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                hideLoading();

                if (recipes == null || recipes.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                    recipeAdapter.updateRecipes(recipes);
                    Log.d(TAG, "Loaded " + recipes.size() + " recipes");
                }
            }

            @Override
            public void onError(String error) {
                hideLoading();
                Log.e(TAG, "Error loading my recipes: " + error);
                Toast.makeText(MyRecipesActivity.this,
                        "Lỗi tải công thức: " + error,
                        Toast.LENGTH_SHORT).show();
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
            // Find TextView children and update text
            for (int i = 0; i < emptyStateText.getChildCount(); i++) {
                View child = emptyStateText.getChildAt(i);
                if (child instanceof TextView) {
                    TextView textView = (TextView) child;
                    String currentText = textView.getText().toString();
                    if (currentText.contains("yêu thích")) {
                        textView.setText("Chưa có công thức nào");
                    } else if (currentText.contains("Hãy thích")) {
                        textView.setText("Hãy đăng công thức đầu tiên của bạn!");
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
