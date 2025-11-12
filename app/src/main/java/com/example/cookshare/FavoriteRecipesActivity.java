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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRecipesActivity extends AppCompatActivity {
    private static final String TAG = "FavoriteRecipesActivity";

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
            setContentView(R.layout.activity_favorite_recipes);

            // Initialize Firebase
            mAuth = FirebaseAuth.getInstance();
            databaseService = new FirebaseDatabaseService();

            // Setup toolbar
            setupToolbar();

            // Initialize views
            initializeViews();

            // Setup RecyclerView
            setupRecyclerView();

            // Load favorite recipes
            loadFavoriteRecipes();
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
                    getSupportActionBar().setTitle("Đã thích");
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
            recipeAdapter.setOnRecipeClickListener(recipe -> { //
                try {
                    // Navigate to RecipeDetailActivity
                    Intent intent = new Intent(FavoriteRecipesActivity.this, RecipeDetailActivity.class); //
                    // ... (Tất cả các dòng intent.putExtra của bạn ở đây) ...
                    intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId()); //
                    intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_TITLE, recipe.getTitle()); //
                    intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_DESCRIPTION, recipe.getDescription()); //
                    intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_IMAGE_URL, recipe.getImageUrl()); //
                    intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_AUTHOR_ID, recipe.getAuthorId()); //
                    intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_AUTHOR_NAME, recipe.getAuthorName()); //
                    intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_PREP_TIME, recipe.getPrepTime()); //
                    intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_COOK_TIME, recipe.getCookTime()); //
                    intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_SERVINGS, recipe.getServings()); //
                    intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_DIFFICULTY, recipe.getDifficulty()); //
                    intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_RATING, recipe.getRating()); //

                    if (recipe.getCategories() != null) { //
                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_CATEGORIES,
                                recipe.getCategories().toArray(new String[0])); //
                    }
                    if (recipe.getIngredients() != null) { //
                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_INGREDIENTS,
                                recipe.getIngredients().toArray(new String[0])); //
                    }
                    if (recipe.getInstructions() != null) { //
                        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_INSTRUCTIONS,
                                recipe.getInstructions().toArray(new String[0])); //
                    }

                    startActivity(intent); //
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to RecipeDetailActivity", e);
                    Toast.makeText(FavoriteRecipesActivity.this, "Lỗi mở chi tiết công thức", Toast.LENGTH_SHORT)
                            .show();
                }
            });


            recipeAdapter.setOnLikeClickListener((recipe, isLiked) -> {
                if (!isLiked) {
                    Log.d(TAG, "User unliked recipe: " + recipe.getTitle());

                    if (databaseService != null) {
                        databaseService.removeFromFavorites(recipe.getId());
                    }

                    // ⭐ THÊM DÒNG NÀY ĐỂ HIỂN THỊ THÔNG BÁO
                    Toast.makeText(FavoriteRecipesActivity.this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();

                    // Xóa công thức khỏi danh sách trên UI
                    List<Recipe> currentRecipes = recipeAdapter.getRecipes();
                    List<Recipe> updatedRecipes = new ArrayList<>();
                    for (Recipe r : currentRecipes) {
                        if (!r.getId().equals(recipe.getId())) {
                            updatedRecipes.add(r);
                        }
                    }

                    recipeAdapter.updateRecipes(updatedRecipes, true);

                    if (updatedRecipes.isEmpty()) {
                        showEmptyState(); //
                    }
                }
            });


            recyclerView.setLayoutManager(new LinearLayoutManager(this)); //
            recyclerView.setAdapter(recipeAdapter); //
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void loadFavoriteRecipes() {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            showLoading();

            // Lấy danh sách recipe IDs từ favorites node
            DatabaseReference favoritesRef = databaseService.getFavoritesRef()
                    .child(currentUser.getUid()).child("recipeIds");

            Log.d(TAG, " Loading favorites from path: favorites/" + currentUser.getUid() + "/recipeIds");
            favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    hideLoading();

                    Log.d(TAG, "📊 Favorites snapshot exists: " + snapshot.exists());
                    Log.d(TAG, "📊 Favorites snapshot children count: " + snapshot.getChildrenCount());

                    if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                        Log.w(TAG, " No favorites found, showing empty state");
                        showEmptyState();
                        return;
                    }

                    List<String> recipeIds = new ArrayList<>();
                    for (DataSnapshot recipeIdSnapshot : snapshot.getChildren()) {
                        String recipeId = recipeIdSnapshot.getKey();
                        Object value = recipeIdSnapshot.getValue();
                        Log.d(TAG, " Found favorite recipeId: " + recipeId + ", value: " + value);
                        // Check if value is true (Boolean) or not null
                        boolean isValid = value != null && !value.equals(false);
                        if (recipeId != null && isValid) {
                            recipeIds.add(recipeId);
                        }
                    }

                    Log.d(TAG, " Total favorite recipe IDs: " + recipeIds.size());
                    if (recipeIds.isEmpty()) {
                        Log.w(TAG, " Recipe IDs list is empty, showing empty state");
                        showEmptyState();
                        return;
                    }

                    // Load chi tiết từng recipe từ foods node
                    loadRecipeDetails(recipeIds);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    hideLoading();
                    Log.e(TAG, "Error loading favorites: " + error.getMessage());
                    Toast.makeText(FavoriteRecipesActivity.this,
                            "Lỗi tải danh sách yêu thích: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadFavoriteRecipes", e);
            hideLoading();
            Toast.makeText(this, "Lỗi tải công thức yêu thích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRecipeDetails(List<String> recipeIds) {
        // Load từ foods node (nơi chứa VietnameseFood)
        // Sử dụng cùng database URL như FirebaseDatabaseService
        String databaseUrl = "https://cookshare-88d53-default-rtdb.asia-southeast1.firebasedatabase.app/";
        DatabaseReference foodsRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference("foods");
        final List<Recipe> favoriteRecipes = new ArrayList<>();
        final int[] loadedCount = { 0 };
        final int totalRecipes = recipeIds.size();

        Log.d(TAG, " Loading " + totalRecipes + " recipe details from foods node");
        Log.d(TAG, " Database URL: " + databaseUrl);
        Log.d(TAG, " Recipe IDs to load: " + recipeIds.toString());

        if (recipeIds.isEmpty()) {
            Log.w(TAG, " Recipe IDs list is empty");
            showEmptyState();
            return;
        }

        for (String recipeId : recipeIds) {
            Log.d(TAG, " Starting load for recipeId: " + recipeId);
            final String finalRecipeId = recipeId; // Capture for logging
            DatabaseReference recipeRef = foodsRef.child(recipeId);
            Log.d(TAG, " Querying path: foods/" + finalRecipeId);

            recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    synchronized (favoriteRecipes) {
                        try {
                            loadedCount[0]++;
                            Log.d(TAG, "📥 onDataChange for recipeId: " + finalRecipeId + ", exists: "
                                    + snapshot.exists() + " (Loaded: " + loadedCount[0] + "/" + totalRecipes + ")");

                            if (snapshot.exists()) {
                                String snapshotKey = snapshot.getKey();
                                Log.d(TAG, " Found recipe in foods node: key=" + snapshotKey + ", recipeId="
                                        + finalRecipeId);

                                // Log raw snapshot value để debug
                                Object rawValue = snapshot.getValue();
                                Log.d(TAG, "📦 Raw snapshot value type: "
                                        + (rawValue != null ? rawValue.getClass().getName() : "null"));

                                com.example.cookshare.models.VietnameseFood vietnameseFood = snapshot
                                        .getValue(com.example.cookshare.models.VietnameseFood.class);
                                if (vietnameseFood != null) {
                                    Log.d(TAG, " VietnameseFood parsed successfully: " + vietnameseFood.getName());
                                    Recipe recipe = vietnameseFood.toRecipe();
                                    // QUAN TRỌNG: Override ID với Firebase key (không phải VietnameseFood.id)
                                    recipe.setId(snapshot.getKey());
                                    Log.d(TAG,
                                            " Recipe created: id=" + recipe.getId() + ", title=" + recipe.getTitle());

                                    // Read dynamic values từ Firebase
                                    if (snapshot.hasChild("rating")) {
                                        Object ratingObj = snapshot.child("rating").getValue();
                                        if (ratingObj != null) {
                                            double rating = ratingObj instanceof Double ? (Double) ratingObj
                                                    : ratingObj instanceof Long ? ((Long) ratingObj).doubleValue()
                                                    : ((Number) ratingObj).doubleValue();
                                            recipe.setRating(rating);
                                            Log.d(TAG, " Set rating: " + rating);
                                        }
                                    }
                                    if (snapshot.hasChild("likeCount")) {
                                        Object likeCountObj = snapshot.child("likeCount").getValue();
                                        if (likeCountObj != null) {
                                            int likeCount = likeCountObj instanceof Long
                                                    ? ((Long) likeCountObj).intValue()
                                                    : ((Integer) likeCountObj).intValue();
                                            recipe.setLikeCount(likeCount);
                                            Log.d(TAG, " Set likeCount: " + likeCount);
                                        }
                                    }
                                    if (snapshot.hasChild("viewCount")) {
                                        Object viewCountObj = snapshot.child("viewCount").getValue();
                                        if (viewCountObj != null) {
                                            int viewCount = viewCountObj instanceof Long
                                                    ? ((Long) viewCountObj).intValue()
                                                    : ((Integer) viewCountObj).intValue();
                                            recipe.setViewCount(viewCount);
                                            Log.d(TAG, " Set viewCount: " + viewCount);
                                        }
                                    }
                                    if (snapshot.hasChild("cookTime")) {
                                        Object cookTimeObj = snapshot.child("cookTime").getValue();
                                        if (cookTimeObj != null) {
                                            int cookTime = cookTimeObj instanceof Long
                                                    ? ((Long) cookTimeObj).intValue()
                                                    : ((Integer) cookTimeObj).intValue();
                                            recipe.setCookTime(cookTime);
                                            Log.d(TAG, " Set cookTime: " + cookTime);
                                        }
                                    }
                                    if (snapshot.hasChild("prepTime")) {
                                        Object prepTimeObj = snapshot.child("prepTime").getValue();
                                        if (prepTimeObj != null) {
                                            int prepTime = prepTimeObj instanceof Long
                                                    ? ((Long) prepTimeObj).intValue()
                                                    : ((Integer) prepTimeObj).intValue();
                                            recipe.setPrepTime(prepTime);
                                            Log.d(TAG, " Set prepTime: " + prepTime);
                                        }
                                    }
                                    if (snapshot.hasChild("servings")) {
                                        Object servingsObj = snapshot.child("servings").getValue();
                                        if (servingsObj != null) {
                                            int servings = servingsObj instanceof Long
                                                    ? ((Long) servingsObj).intValue()
                                                    : ((Integer) servingsObj).intValue();
                                            recipe.setServings(servings);
                                            Log.d(TAG, " Set servings: " + servings);
                                        }
                                    }
                                    if (snapshot.hasChild("difficulty")) {
                                        Object difficultyObj = snapshot.child("difficulty").getValue();
                                        if (difficultyObj != null) {
                                            recipe.setDifficulty(difficultyObj.toString());
                                            Log.d(TAG, " Set difficulty: " + difficultyObj.toString());
                                        }
                                    }

                                    favoriteRecipes.add(recipe);
                                    Log.d(TAG, " Added recipe to favorites list: " + recipe.getTitle() + " (ID: "
                                            + recipe.getId() + "). Total in list: " + favoriteRecipes.size());
                                } else {
                                    Log.e(TAG, " VietnameseFood is null for recipeId: " + finalRecipeId);
                                    Log.e(TAG, " Snapshot value: " + rawValue);
                                }
                            } else {
                                Log.e(TAG, " Recipe NOT FOUND in foods node for recipeId: " + finalRecipeId
                                        + " (Path: foods/" + finalRecipeId + ")");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, " Error parsing recipe: " + finalRecipeId, e);
                            e.printStackTrace();
                        }

                        // Check if all recipes have been loaded
                        Log.d(TAG, "📊 Progress: " + loadedCount[0] + "/" + totalRecipes
                                + " recipes processed. Total favorite recipes: " + favoriteRecipes.size());

                        if (loadedCount[0] >= totalRecipes) {
                            // Đã load xong tất cả recipes
                            Log.d(TAG, " Finished loading all recipes. Total loaded: " + favoriteRecipes.size());

                            // Run on main thread để update UI
                            runOnUiThread(() -> { //
                                if (favoriteRecipes.isEmpty()) {
                                    Log.w(TAG, " No recipes loaded, showing empty state");
                                    showEmptyState();
                                } else {
                                    Log.d(TAG, " Updating adapter with " + favoriteRecipes.size()
                                            + " favorite recipes");
                                    hideEmptyState();
                                    if (recipeAdapter != null) {
                                        recipeAdapter.updateRecipes(new ArrayList<>(favoriteRecipes), true);
                                        // (Bằng cách truyền 'true', chúng ta buộc adapter
                                        // cache trạng thái "thích" ngay lập tức)

                                        Log.d(TAG, " Adapter updated. Item count: " + recipeAdapter.getItemCount());
                                    } else {
                                        Log.e(TAG, " RecipeAdapter is null!");
                                    }
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    synchronized (favoriteRecipes) {
                        loadedCount[0]++;
                        Log.e(TAG, " Error loading recipe " + finalRecipeId + ": " + error.getMessage() + ", Code: "
                                + error.getCode());

                        if (loadedCount[0] >= totalRecipes) {
                            runOnUiThread(() -> {
                                if (favoriteRecipes.isEmpty()) {
                                    showEmptyState();
                                } else {
                                    hideEmptyState();
                                    if (recipeAdapter != null) {
                                        recipeAdapter.updateRecipes(new ArrayList<>(favoriteRecipes));
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
        try {
            if (loadingContainer != null) {
                loadingContainer.setVisibility(View.VISIBLE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            }
            if (emptyStateText != null) {
                emptyStateText.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing loading", e);
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