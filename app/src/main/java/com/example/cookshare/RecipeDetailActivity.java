package com.example.cookshare;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.cookshare.models.Recipe;
import com.example.cookshare.models.UserProfile;
import com.example.cookshare.services.FirebaseDatabaseService;
import com.example.cookshare.EditRecipeActivity;
import com.example.cookshare.UserProfileActivity;
import com.example.cookshare.viewmodels.RecipeViewModel;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import android.widget.RatingBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.lifecycle.ViewModelProvider;

import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {
    private static final String TAG = "RecipeDetailActivity";

    public static final String EXTRA_RECIPE_ID = "recipe_id";
    public static final String EXTRA_RECIPE_TITLE = "recipe_title";
    public static final String EXTRA_RECIPE_DESCRIPTION = "recipe_description";
    public static final String EXTRA_RECIPE_IMAGE_URL = "recipe_image_url";
    public static final String EXTRA_RECIPE_AUTHOR_ID = "recipe_author_id";
    public static final String EXTRA_RECIPE_AUTHOR_NAME = "recipe_author_name";
    public static final String EXTRA_RECIPE_PREP_TIME = "recipe_prep_time";
    public static final String EXTRA_RECIPE_COOK_TIME = "recipe_cook_time";
    public static final String EXTRA_RECIPE_SERVINGS = "recipe_servings";
    public static final String EXTRA_RECIPE_DIFFICULTY = "recipe_difficulty";
    public static final String EXTRA_RECIPE_RATING = "recipe_rating";
    public static final String EXTRA_RECIPE_CATEGORIES = "recipe_categories";
    public static final String EXTRA_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String EXTRA_RECIPE_INSTRUCTIONS = "recipe_instructions";

    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private ImageView recipeImageLarge;
    private MaterialCardView authorCard;
    private ShapeableImageView authorAvatar;
    private TextView authorNameDetail;
    private TextView authorBio;
    private TextView authorStats;
    private MaterialButton followButton;
    private TextView cookTimeDetail;
    private TextView servingsDetail;
    private TextView difficultyDetail;
    private TextView ratingDetail;
    private TextView descriptionDetail;
    private ChipGroup categoriesChipGroup;
    private LinearLayout ingredientsContainer;
    private LinearLayout instructionsContainer;
    private FloatingActionButton fabLike;

    // Rating section
    private RatingBar recipeRatingBar;
    private TextView currentRatingText;
    private TextView ratingCountText;
    private TextView yourRatingText;
    private MaterialButton submitRatingButton;
    private float userRating = 0;

    private Recipe recipe;
    private boolean isLiked = false;
    private boolean isFollowing = false;
    private String recipeId;
    private String authorId;
    private RecipeViewModel recipeViewModel;
    private FirebaseDatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Initialize ViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        // Initialize Firebase Database Service
        databaseService = new FirebaseDatabaseService();

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Get recipe data from intent
        getRecipeDataFromIntent();

        // Load recipe from Firebase to get latest rating and ratingCount
        loadRecipeFromFirebase();

        // Lưu vào cooking history khi user xem recipe
        if (recipeId != null && !recipeId.isEmpty()) {
            databaseService.addToCookingHistory(recipeId);
            // Increment view count
            recipeViewModel.incrementViewCount(recipeId);
        }

        // Display recipe details
        displayRecipeDetails();

        // Load author profile
        loadAuthorProfile();

        // Setup FAB
        setupFab();

        // Setup author card click and follow button
        setupAuthorCard();

        // Setup rating section
        setupRatingSection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload recipe và user rating khi quay lại màn hình để đảm bảo hiển thị đúng
        if (recipeId != null && !recipeId.isEmpty()) {
            loadRecipeFromFirebase(); // Load lại recipe trước, nó sẽ gọi loadUserRating()
        }
    }

    private void initializeViews() {
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbar);
        recipeImageLarge = findViewById(R.id.recipeImageLarge);
        authorCard = findViewById(R.id.authorCard);
        authorAvatar = findViewById(R.id.authorAvatar);
        authorNameDetail = findViewById(R.id.authorNameDetail);
        authorBio = findViewById(R.id.authorBio);
        authorStats = findViewById(R.id.authorStats);
        followButton = findViewById(R.id.followButton);
        cookTimeDetail = findViewById(R.id.cookTimeDetail);
        servingsDetail = findViewById(R.id.servingsDetail);
        difficultyDetail = findViewById(R.id.difficultyDetail);
        ratingDetail = findViewById(R.id.ratingDetail);
        descriptionDetail = findViewById(R.id.descriptionDetail);
        categoriesChipGroup = findViewById(R.id.categoriesChipGroup);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        instructionsContainer = findViewById(R.id.instructionsContainer);
        fabLike = findViewById(R.id.fabLike);

        // Rating section views
        recipeRatingBar = findViewById(R.id.recipeRatingBar);
        currentRatingText = findViewById(R.id.currentRatingText);
        ratingCountText = findViewById(R.id.ratingCountText);
        yourRatingText = findViewById(R.id.yourRatingText);
        submitRatingButton = findViewById(R.id.submitRatingButton);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void getRecipeDataFromIntent() {
        recipe = new Recipe();
        recipeId = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        recipe.setId(recipeId);
        recipe.setTitle(getIntent().getStringExtra(EXTRA_RECIPE_TITLE));
        recipe.setDescription(getIntent().getStringExtra(EXTRA_RECIPE_DESCRIPTION));
        recipe.setImageUrl(getIntent().getStringExtra(EXTRA_RECIPE_IMAGE_URL));
        recipe.setAuthorId(getIntent().getStringExtra(EXTRA_RECIPE_AUTHOR_ID));
        recipe.setAuthorName(getIntent().getStringExtra(EXTRA_RECIPE_AUTHOR_NAME));
        authorId = recipe.getAuthorId();
        recipe.setPrepTime(getIntent().getIntExtra(EXTRA_RECIPE_PREP_TIME, 0));
        recipe.setCookTime(getIntent().getIntExtra(EXTRA_RECIPE_COOK_TIME, 0));
        recipe.setServings(getIntent().getIntExtra(EXTRA_RECIPE_SERVINGS, 0));
        recipe.setDifficulty(getIntent().getStringExtra(EXTRA_RECIPE_DIFFICULTY));
        recipe.setRating(getIntent().getDoubleExtra(EXTRA_RECIPE_RATING, 0.0));
        recipe.setRatingCount(getIntent().getIntExtra("recipe_rating_count", 0));

        // Get arrays
        String[] categories = getIntent().getStringArrayExtra(EXTRA_RECIPE_CATEGORIES);
        if (categories != null) {
            recipe.setCategories(java.util.Arrays.asList(categories));
        }

        String[] ingredients = getIntent().getStringArrayExtra(EXTRA_RECIPE_INGREDIENTS);
        if (ingredients != null) {
            recipe.setIngredients(java.util.Arrays.asList(ingredients));
        }

        String[] instructions = getIntent().getStringArrayExtra(EXTRA_RECIPE_INSTRUCTIONS);
        if (instructions != null) {
            recipe.setInstructions(java.util.Arrays.asList(instructions));
        }
    }

    private void loadRecipeFromFirebase() {
        if (recipeId == null || recipeId.isEmpty()) {
            return;
        }

        // Check foods node first (vì recipes được hiển thị từ foods node)
        DatabaseReference foodRef = FirebaseDatabase.getInstance()
                .getReference("foods")
                .child(recipeId);

        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && recipe != null) {
                    // Check if there's a linked recipeId (user-created recipe)
                    String linkedRecipeId = snapshot.child("recipeId").getValue(String.class);

                    if (linkedRecipeId != null && !linkedRecipeId.isEmpty()) {
                        // User-created recipe - load rating from recipes node (source of truth)
                        Log.d(TAG, "Loading rating from recipes node (linked recipeId: " + linkedRecipeId + ")");
                        DatabaseReference recipeRef = FirebaseDatabase.getInstance()
                                .getReference("recipes")
                                .child(linkedRecipeId);

                        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot recipeSnapshot) {
                                loadRatingFromRecipeSnapshot(recipeSnapshot);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                // Fallback to foods node
                                Log.w(TAG, "Error loading from recipes node, using foods node");
                                loadRatingFromRecipeSnapshot(snapshot);
                            }
                        });
                    } else {
                        // API recipe - load from foods node
                        Log.d(TAG, "Loading rating from foods node (API recipe)");
                        loadRatingFromRecipeSnapshot(snapshot);
                    }
                } else {
                    // Not in foods node, try recipes node
                    Log.d(TAG, "Recipe not in foods node, checking recipes node: " + recipeId);
                    DatabaseReference recipeRef = FirebaseDatabase.getInstance()
                            .getReference("recipes")
                            .child(recipeId);

                    recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot recipeSnapshot) {
                            loadRatingFromRecipeSnapshot(recipeSnapshot);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.e(TAG, "Error loading recipe from Firebase", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error loading recipe from Firebase", error.toException());
            }
        });
    }

    private void loadRatingFromRecipeSnapshot(DataSnapshot snapshot) {
        if (snapshot.exists() && recipe != null) {
            // Update rating and ratingCount from Firebase
            if (snapshot.hasChild("rating")) {
                Object ratingObj = snapshot.child("rating").getValue();
                if (ratingObj != null) {
                    double rating = ratingObj instanceof Double
                            ? (Double) ratingObj
                            : ratingObj instanceof Long
                                    ? ((Long) ratingObj).doubleValue()
                                    : ((Number) ratingObj).doubleValue();
                    recipe.setRating(rating);
                }
            }
            if (snapshot.hasChild("ratingCount")) {
                Object ratingCountObj = snapshot.child("ratingCount").getValue();
                if (ratingCountObj != null) {
                    int ratingCount = ratingCountObj instanceof Long
                            ? ((Long) ratingCountObj).intValue()
                            : ((Integer) ratingCountObj).intValue();
                    recipe.setRatingCount(ratingCount);
                }
            }

            // Update UI
            runOnUiThread(() -> {
                updateRatingDisplay();
                // Reload user rating sau khi đã load recipe để đảm bảo hiển thị đúng
                // Delay nhỏ để đảm bảo UI đã update xong và tránh race condition
                if (recipeRatingBar != null) {
                    recipeRatingBar.postDelayed(() -> {
                        Log.d(TAG, " Loading user rating after recipe loaded...");
                        loadUserRating();
                    }, 200);
                } else {
                    Log.d(TAG, " Loading user rating after recipe loaded (no delay)...");
                    loadUserRating();
                }
            });
        }
    }

    private void displayRecipeDetails() {
        // Set title
        collapsingToolbar.setTitle(recipe.getTitle());
        collapsingToolbar.setExpandedTitleColor(Color.WHITE);
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);

        // Load image
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(recipe.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(recipeImageLarge);
        }

        // Set author name (will be updated when profile loads)
        authorNameDetail.setText(recipe.getAuthorName() != null ? recipe.getAuthorName() : "Ẩm thực Việt Nam");

        // Set stats
        cookTimeDetail.setText(recipe.getFormattedTime());
        servingsDetail.setText(recipe.getServings() + " người");
        difficultyDetail.setText(getDifficultyText(recipe.getDifficulty()));
        ratingDetail.setText(recipe.getFormattedRating());

        // Update rating section
        updateRatingDisplay();

        // Set description
        descriptionDetail.setText(recipe.getDescription() != null ? recipe.getDescription() : "Chưa có mô tả");

        // Add categories as chips
        if (recipe.getCategories() != null && !recipe.getCategories().isEmpty()) {
            for (String category : recipe.getCategories()) {
                Chip chip = new Chip(this);
                chip.setText(category);
                chip.setChipBackgroundColorResource(R.color.primary_color);
                chip.setTextColor(Color.WHITE);
                chip.setClickable(false);
                categoriesChipGroup.addView(chip);
            }
        }

        // Add tags as chips too
        if (recipe.getTags() != null && !recipe.getTags().isEmpty()) {
            for (String tag : recipe.getTags()) {
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setChipBackgroundColorResource(R.color.accent_color);
                chip.setTextColor(Color.WHITE);
                chip.setClickable(false);
                categoriesChipGroup.addView(chip);
            }
        }

        // Add ingredients
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            for (int i = 0; i < recipe.getIngredients().size(); i++) {
                String ingredient = recipe.getIngredients().get(i);
                TextView ingredientView = new TextView(this);
                ingredientView.setText("• " + ingredient);
                ingredientView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                ingredientView.setTextSize(14);
                ingredientView.setPadding(0, 8, 0, 8);
                ingredientsContainer.addView(ingredientView);
            }
        } else {
            TextView noIngredientsView = new TextView(this);
            noIngredientsView.setText("Chưa có thông tin nguyên liệu");
            noIngredientsView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            noIngredientsView.setTextSize(14);
            ingredientsContainer.addView(noIngredientsView);
        }

        // Add instructions
        if (recipe.getInstructions() != null && !recipe.getInstructions().isEmpty()) {
            for (int i = 0; i < recipe.getInstructions().size(); i++) {
                String instruction = recipe.getInstructions().get(i);

                LinearLayout stepLayout = new LinearLayout(this);
                stepLayout.setOrientation(LinearLayout.HORIZONTAL);
                stepLayout.setPadding(0, 12, 0, 12);

                // Step number
                TextView stepNumber = new TextView(this);
                stepNumber.setText((i + 1) + ".");
                stepNumber.setTextColor(ContextCompat.getColor(this, R.color.primary_color));
                stepNumber.setTextSize(16);
                stepNumber.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                stepNumber.setPadding(0, 0, 16, 0);
                stepNumber.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                // Step text
                TextView stepText = new TextView(this);
                stepText.setText(instruction);
                stepText.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                stepText.setTextSize(14);
                stepText.setLineSpacing(4, 1);
                stepText.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1));

                stepLayout.addView(stepNumber);
                stepLayout.addView(stepText);
                instructionsContainer.addView(stepLayout);
            }
        } else {
            TextView noInstructionsView = new TextView(this);
            noInstructionsView.setText("Chưa có hướng dẫn nấu");
            noInstructionsView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            noInstructionsView.setTextSize(14);
            instructionsContainer.addView(noInstructionsView);
        }
    }

    private void setupFab() {
        if (fabLike == null) {
            Log.e("RecipeDetailActivity", "fabLike is null!");
            return;
        }

        try {
            // Check if recipe is already liked
            checkIfLiked();

            fabLike.setOnClickListener(v -> {
                try {
                    if (recipeId == null || recipeId.isEmpty()) {
                        Toast.makeText(this, "Không thể thích công thức này", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    isLiked = !isLiked;
                    updateFabIcon();

                    // Update in Firebase
                    if (recipeViewModel != null) {
                        recipeViewModel.updateLikeCount(recipeId, isLiked);
                    }

                    Toast.makeText(this,
                            isLiked ? "Đã thích công thức" : "Đã bỏ thích công thức",
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("RecipeDetailActivity", "Error in fabLike click", e);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("RecipeDetailActivity", "Error setting up FAB", e);
        }
    }

    private void checkIfLiked() {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null || recipeId == null) {
                isLiked = false;
                updateFabIcon();
                return;
            }

            DatabaseReference favoritesRef = FirebaseDatabase.getInstance()
                    .getReference("favorites")
                    .child(currentUser.getUid())
                    .child("recipeIds")
                    .child(recipeId);

            favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        isLiked = snapshot.exists() && snapshot.getValue() != null;
                        updateFabIcon();
                    } catch (Exception e) {
                        Log.e("RecipeDetailActivity", "Error parsing like state", e);
                        isLiked = false;
                        updateFabIcon();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("RecipeDetailActivity", "Error checking if liked: " + error.getMessage());
                    isLiked = false;
                    updateFabIcon();
                }
            });
        } catch (Exception e) {
            Log.e("RecipeDetailActivity", "Error checking if liked", e);
            isLiked = false;
            updateFabIcon();
        }
    }

    private void updateFabIcon() {
        try {
            if (fabLike != null) {
                fabLike.setImageResource(
                        isLiked ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
            }
        } catch (Exception e) {
            Log.e("RecipeDetailActivity", "Error updating FAB icon", e);
        }
    }

    private void loadAuthorProfile() {
        // Always show author card
        if (authorCard != null) {
            authorCard.setVisibility(View.VISIBLE);
        }

        // Check if system recipe or no authorId
        boolean isSystemRecipe = authorId == null || authorId.isEmpty() || authorId.equals("vietnamese_food");

        if (isSystemRecipe) {
            // Hide follow button and disable card click for system recipes
            if (followButton != null) {
                followButton.setVisibility(View.GONE);
            }
            if (authorCard != null) {
                authorCard.setClickable(false);
                authorCard.setFocusable(false);
            }
            // Hide bio and stats, show default name
            if (authorBio != null) {
                authorBio.setVisibility(View.GONE);
            }
            if (authorStats != null) {
                authorStats.setVisibility(View.GONE);
            }
            // Set name from recipe
            if (authorNameDetail != null && recipe.getAuthorName() != null && !recipe.getAuthorName().isEmpty()) {
                authorNameDetail.setText(recipe.getAuthorName());
            }
            return;
        }

        // Enable card click for user recipes
        if (authorCard != null) {
            authorCard.setClickable(true);
            authorCard.setFocusable(true);
        }

        // Check if viewing own recipe
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isOwnRecipe = currentUser != null && currentUser.getUid().equals(authorId);

        if (isOwnRecipe) {
            // Hide follow button for own recipes
            if (followButton != null) {
                followButton.setVisibility(View.GONE);
            }
        } else {
            // Show follow button and check follow status
            if (followButton != null) {
                followButton.setVisibility(View.VISIBLE);
            }
            checkFollowStatus();
        }

        // Load author profile to get updated stats and real name
        databaseService.getUserProfile(authorId, new FirebaseDatabaseService.UserProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                runOnUiThread(() -> {
                    if (profile != null) {
                        displayAuthorProfile(profile);
                    } else {
                        // Fallback: use authorName from recipe if profile is null
                        if (recipe.getAuthorName() != null && !recipe.getAuthorName().isEmpty()) {
                            authorNameDetail.setText(recipe.getAuthorName());
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading author profile: " + error);
                // Fallback: use authorName from recipe
                runOnUiThread(() -> {
                    if (recipe.getAuthorName() != null && !recipe.getAuthorName().isEmpty()) {
                        authorNameDetail.setText(recipe.getAuthorName());
                    }
                });
            }
        });
    }

    private void displayAuthorProfile(UserProfile profile) {
        if (profile == null) {
            return;
        }

        // Set name
        if (profile.getFullName() != null && !profile.getFullName().isEmpty()) {
            authorNameDetail.setText(profile.getFullName());
        }

        // Set bio
        if (profile.getBio() != null && !profile.getBio().isEmpty()) {
            authorBio.setText(profile.getBio());
            authorBio.setVisibility(View.VISIBLE);
        } else {
            authorBio.setVisibility(View.GONE);
        }

        // Set avatar
        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(profile.getAvatarUrl())
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .error(android.R.drawable.ic_menu_myplaces)
                    .circleCrop()
                    .into(authorAvatar);
        }

        // Set stats
        int recipesCount = profile.getRecipesCount();
        int followersCount = profile.getFollowersCount();
        String statsText = recipesCount + " công thức";
        if (followersCount > 0) {
            statsText += " • " + followersCount + " người theo dõi";
        }
        authorStats.setText(statsText);
    }

    private void setupAuthorCard() {
        if (authorCard == null) {
            return;
        }

        // Click on author card to navigate to profile
        authorCard.setOnClickListener(v -> {
            if (authorId != null && !authorId.isEmpty() && !authorId.equals("vietnamese_food")) {
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra(UserProfileActivity.EXTRA_USER_ID, authorId);
                startActivity(intent);
            }
        });

        // Follow button click - only setup if button is visible
        if (followButton != null) {
            followButton.setOnClickListener(v -> {
                if (authorId == null || authorId.isEmpty() || authorId.equals("vietnamese_food")) {
                    return; // Should not happen, but safety check
                }
                if (isFollowing) {
                    unfollowAuthor();
                } else {
                    followAuthor();
                }
            });
        }
    }

    private void checkFollowStatus() {
        if (authorId == null || authorId.isEmpty() || authorId.equals("vietnamese_food")) {
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(authorId)) {
            // Hide follow button for own recipes
            if (followButton != null) {
                followButton.setVisibility(View.GONE);
            }
            return;
        }

        // Show follow button
        if (followButton != null) {
            followButton.setVisibility(View.VISIBLE);
        }

        databaseService.isFollowing(authorId, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                isFollowing = snapshot.exists() && snapshot.getValue() != null;
                updateFollowButton();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error checking follow status", error.toException());
            }
        });
    }

    private void followAuthor() {
        if (authorId == null || authorId.isEmpty()) {
            return;
        }

        followButton.setEnabled(false);
        databaseService.followUser(authorId, new FirebaseDatabaseService.FollowCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    isFollowing = true;
                    updateFollowButton();
                    followButton.setEnabled(true);
                    Toast.makeText(RecipeDetailActivity.this, "Đã theo dõi", Toast.LENGTH_SHORT).show();

                    // Reload author profile to update followers count
                    // Add delay to ensure Firebase transaction has synced
                    if (followButton != null) {
                        followButton.postDelayed(() -> {
                            Log.d(TAG, "Reloading author profile after follow");
                            reloadAuthorProfile();
                        }, 500); // 500ms delay to ensure Firebase sync
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    followButton.setEnabled(true);
                    Toast.makeText(RecipeDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void unfollowAuthor() {
        if (authorId == null || authorId.isEmpty()) {
            return;
        }

        followButton.setEnabled(false);
        databaseService.unfollowUser(authorId, new FirebaseDatabaseService.FollowCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    isFollowing = false;
                    updateFollowButton();
                    followButton.setEnabled(true);
                    Toast.makeText(RecipeDetailActivity.this, "Đã bỏ theo dõi", Toast.LENGTH_SHORT).show();

                    // Reload author profile to update followers count
                    // Add delay to ensure Firebase transaction has synced
                    if (followButton != null) {
                        followButton.postDelayed(() -> {
                            Log.d(TAG, "Reloading author profile after unfollow");
                            reloadAuthorProfile();
                        }, 500); // 500ms delay to ensure Firebase sync
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    followButton.setEnabled(true);
                    Toast.makeText(RecipeDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Reload author profile to refresh stats (followers count, etc.)
     */
    private void reloadAuthorProfile() {
        if (authorId == null || authorId.isEmpty() || authorId.equals("vietnamese_food")) {
            return;
        }

        Log.d(TAG, "Reloading author profile for authorId: " + authorId);
        databaseService.getUserProfile(authorId, new FirebaseDatabaseService.UserProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                runOnUiThread(() -> {
                    if (profile != null) {
                        Log.d(TAG, "Author profile reloaded - followersCount: " + profile.getFollowersCount());
                        displayAuthorProfile(profile);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error reloading author profile: " + error);
            }
        });
    }

    private void updateFollowButton() {
        if (followButton == null) {
            return;
        }

        if (isFollowing) {
            followButton.setText("Đang theo dõi");
        } else {
            followButton.setText("Theo dõi");
        }
    }

    private void setupRatingSection() {
        if (recipeRatingBar == null || submitRatingButton == null) {
            return;
        }

        // Rating bar change listener - set BEFORE loading to avoid conflicts
        recipeRatingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            // Chỉ xử lý khi user thực sự thay đổi rating, không phải khi set
            // programmatically
            if (fromUser && !recipeRatingBar.isIndicator()) {
                userRating = rating;
                if (rating > 0) {
                    yourRatingText.setVisibility(View.VISIBLE);
                    yourRatingText.setText("Đánh giá của bạn: " + String.format("%.1f", rating) + " sao");
                    submitRatingButton.setVisibility(View.VISIBLE);
                } else {
                    yourRatingText.setVisibility(View.GONE);
                    submitRatingButton.setVisibility(View.GONE);
                }
            }
        });

        // Submit rating button
        submitRatingButton.setOnClickListener(v -> {
            if (userRating > 0) {
                submitRating(userRating);
            }
        });

        // Load user's current rating AFTER setting listener
        loadUserRating();
    }

    private void loadUserRating() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || recipeId == null || recipeId.isEmpty()) {
            Log.w(TAG, " Cannot load user rating: user=" + (currentUser != null) + ", recipeId=" + recipeId);
            return;
        }

        Log.d(TAG, " Loading user rating - recipeId: " + recipeId + ", authorId: " + authorId);

        // Check foods node first (vì recipes được hiển thị từ foods node)
        DatabaseReference foodRef = FirebaseDatabase.getInstance()
                .getReference("foods")
                .child(recipeId);

        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot foodSnapshot) {
                if (foodSnapshot.exists()) {
                    // Check if there's a linked recipeId (user-created recipe)
                    String linkedRecipeId = foodSnapshot.child("recipeId").getValue(String.class);
                    Log.d(TAG, "   Found in foods node, linkedRecipeId: " + linkedRecipeId);

                    // Try recipes node first if linkedRecipeId exists
                    if (linkedRecipeId != null && !linkedRecipeId.isEmpty()) {
                        DatabaseReference recipesRatingRef = FirebaseDatabase.getInstance()
                                .getReference("recipes")
                                .child(linkedRecipeId)
                                .child("ratings")
                                .child(currentUser.getUid());

                        Log.d(TAG, "   Trying recipes node: " + linkedRecipeId);
                        recipesRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Log.d(TAG, "    Found rating in recipes node");
                                    loadRatingFromSnapshot(snapshot);
                                } else {
                                    // Not found in recipes node, try foods node
                                    Log.d(TAG, "   Not in recipes node, trying foods node");
                                    tryFoodsNodeForRating(recipeId, currentUser.getUid());
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Log.e(TAG, "   Error loading from recipes node, trying foods node",
                                        error.toException());
                                tryFoodsNodeForRating(recipeId, currentUser.getUid());
                            }
                        });
                    } else {
                        // No linkedRecipeId, try foods node directly
                        Log.d(TAG, "   No linkedRecipeId, trying foods node directly");
                        tryFoodsNodeForRating(recipeId, currentUser.getUid());
                    }
                } else {
                    // Not in foods node, try recipes node directly
                    Log.d(TAG, "   Not in foods node, checking recipes node directly: " + recipeId);
                    DatabaseReference recipeRatingRef = FirebaseDatabase.getInstance()
                            .getReference("recipes")
                            .child(recipeId)
                            .child("ratings")
                            .child(currentUser.getUid());

                    recipeRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Log.d(TAG, "    Found rating in recipes node (direct)");
                                loadRatingFromSnapshot(snapshot);
                            } else {
                                Log.d(TAG, "    Rating not found in recipes node");
                                handleNoRatingFound();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.e(TAG, "   Error loading from recipes node", error.toException());
                            handleNoRatingFound();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "   Error checking foods node, trying recipes node", error.toException());
                // Try recipes node as fallback
                DatabaseReference recipeRatingRef = FirebaseDatabase.getInstance()
                        .getReference("recipes")
                        .child(recipeId)
                        .child("ratings")
                        .child(currentUser.getUid());

                recipeRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.d(TAG, "    Found rating in recipes node (fallback)");
                            loadRatingFromSnapshot(snapshot);
                        } else {
                            Log.d(TAG, "    Rating not found");
                            handleNoRatingFound();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error2) {
                        Log.e(TAG, "   Error loading from recipes node (fallback)", error2.toException());
                        handleNoRatingFound();
                    }
                });
            }
        });
    }

    private void tryFoodsNodeForRating(String foodId, String userId) {
        DatabaseReference foodsRatingRef = FirebaseDatabase.getInstance()
                .getReference("foods")
                .child(foodId)
                .child("ratings")
                .child(userId);

        foodsRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "    Found rating in foods node");
                    loadRatingFromSnapshot(snapshot);
                } else {
                    Log.d(TAG, "    Rating not found in foods node");
                    handleNoRatingFound();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "   Error loading from foods node", error.toException());
                handleNoRatingFound();
            }
        });
    }

    private void handleNoRatingFound() {
        // User chưa đánh giá - reset về trạng thái ban đầu
        runOnUiThread(() -> {
            // QUAN TRỌNG: Reset userRating về 0
            userRating = 0;

            if (recipeRatingBar != null) {
                // Luôn reset RatingBar về 0 và cho phép chỉnh sửa
                recipeRatingBar.setOnRatingBarChangeListener(null);
                recipeRatingBar.setRating(0);
                recipeRatingBar.setIsIndicator(false);
                recipeRatingBar.setOnRatingBarChangeListener((ratingBar, ratingValue, fromUser) -> {
                    if (fromUser && !ratingBar.isIndicator()) {
                        userRating = ratingValue;
                        if (ratingValue > 0) {
                            yourRatingText.setVisibility(View.VISIBLE);
                            yourRatingText.setText(
                                    "Đánh giá của bạn: " + String.format("%.1f", ratingValue) + " sao");
                            submitRatingButton.setVisibility(View.VISIBLE);
                        } else {
                            yourRatingText.setVisibility(View.GONE);
                            submitRatingButton.setVisibility(View.GONE);
                        }
                    }
                });
            }
            if (yourRatingText != null) {
                yourRatingText.setVisibility(View.GONE);
            }
            if (submitRatingButton != null) {
                submitRatingButton.setVisibility(View.GONE);
            }
        });
    }

    private void loadRatingFromSnapshot(DataSnapshot snapshot) {
        // This method will be called from loadUserRating() with the snapshot
        runOnUiThread(() -> {
            if (snapshot.exists()) {
                Object ratingObj = snapshot.getValue();
                if (ratingObj != null) {
                    float rating = ratingObj instanceof Double
                            ? ((Double) ratingObj).floatValue()
                            : ratingObj instanceof Long
                                    ? ((Long) ratingObj).floatValue()
                                    : ((Number) ratingObj).floatValue();

                    // QUAN TRỌNG: Luôn update userRating với giá trị từ Firebase
                    userRating = rating;

                    // User đã đánh giá rồi - hiển thị rating và không cho chỉnh sửa
                    if (recipeRatingBar != null) {
                        // Tạm thời remove listener để tránh trigger khi set programmatically
                        recipeRatingBar.setOnRatingBarChangeListener(null);
                        recipeRatingBar.setIsIndicator(true); // Không cho chỉnh sửa
                        recipeRatingBar.setRating(rating); // Set rating của USER
                        // Set lại listener sau khi đã set rating (nhưng sẽ không trigger vì isIndicator
                        // = true)
                        recipeRatingBar.setOnRatingBarChangeListener((ratingBar, ratingValue, fromUser) -> {
                            // Chỉ xử lý khi user thực sự thay đổi và không phải indicator
                            if (fromUser && !ratingBar.isIndicator()) {
                                userRating = ratingValue;
                                if (ratingValue > 0) {
                                    yourRatingText.setVisibility(View.VISIBLE);
                                    yourRatingText.setText(
                                            "Đánh giá của bạn: " + String.format("%.1f", ratingValue) + " sao");
                                    submitRatingButton.setVisibility(View.VISIBLE);
                                } else {
                                    yourRatingText.setVisibility(View.GONE);
                                    submitRatingButton.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                    if (yourRatingText != null) {
                        yourRatingText.setVisibility(View.VISIBLE);
                        yourRatingText.setText("Đánh giá của bạn: " + String.format("%.1f", rating) + " sao");
                    }
                    if (submitRatingButton != null) {
                        submitRatingButton.setVisibility(View.GONE); // Ẩn nút submit
                    }

                    Log.d(TAG, " Loaded user rating: " + rating + " stars");
                }
            } else {
                // User chưa đánh giá - reset về trạng thái ban đầu
                // QUAN TRỌNG: Reset userRating về 0
                userRating = 0;

                if (recipeRatingBar != null) {
                    // Luôn reset RatingBar về 0 và cho phép chỉnh sửa
                    recipeRatingBar.setOnRatingBarChangeListener(null);
                    recipeRatingBar.setRating(0);
                    recipeRatingBar.setIsIndicator(false); // Cho phép chỉnh sửa
                    // Set lại listener
                    recipeRatingBar.setOnRatingBarChangeListener((ratingBar, ratingValue, fromUser) -> {
                        if (fromUser && !ratingBar.isIndicator()) {
                            userRating = ratingValue;
                            if (ratingValue > 0) {
                                yourRatingText.setVisibility(View.VISIBLE);
                                yourRatingText.setText(
                                        "Đánh giá của bạn: " + String.format("%.1f", ratingValue) + " sao");
                                submitRatingButton.setVisibility(View.VISIBLE);
                            } else {
                                yourRatingText.setVisibility(View.GONE);
                                submitRatingButton.setVisibility(View.GONE);
                            }
                        }
                    });
                }
                if (yourRatingText != null) {
                    yourRatingText.setVisibility(View.GONE);
                }
                if (submitRatingButton != null) {
                    submitRatingButton.setVisibility(View.GONE);
                }

                Log.d(TAG, "ℹ️ User chưa đánh giá recipe này");
            }
        });
    }

    private void updateRatingDisplay() {
        if (currentRatingText == null || ratingCountText == null || ratingDetail == null) {
            return;
        }

        // Update current rating display (rating trung bình)
        currentRatingText.setText(recipe.getFormattedRating());
        ratingDetail.setText(recipe.getFormattedRating());

        // Update rating count
        int count = recipe.getRatingCount();
        if (count > 0) {
            ratingCountText.setText("(" + count + " đánh giá)");
        } else {
            ratingCountText.setText("(Chưa có đánh giá)");
        }

        // KHÔNG reset rating bar ở đây - để loadUserRating() xử lý
    }

    private void submitRating(float rating) {
        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, "Không thể đánh giá công thức này", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        submitRatingButton.setEnabled(false);

        // Submit rating through database service
        databaseService.submitRecipeRating(recipeId, rating, new FirebaseDatabaseService.RatingCallback() {
            @Override
            public void onSuccess(double newAverageRating, int newRatingCount) {
                runOnUiThread(() -> {
                    submitRatingButton.setEnabled(true);

                    // Update recipe object với rating trung bình mới
                    recipe.setRating(newAverageRating);
                    recipe.setRatingCount(newRatingCount);

                    // Update UI - hiển thị rating trung bình và count
                    updateRatingDisplay();

                    // Update rating bar với rating của USER (không phải trung bình)
                    // Và không cho phép chỉnh sửa nữa
                    if (recipeRatingBar != null) {
                        recipeRatingBar.setOnRatingBarChangeListener(null);
                        recipeRatingBar.setIsIndicator(true); // Không cho chỉnh sửa
                        recipeRatingBar.setRating(rating); // Set rating của USER
                        // Set lại listener (nhưng không bao giờ trigger vì isIndicator = true)
                        recipeRatingBar.setOnRatingBarChangeListener((ratingBar, ratingValue, fromUser) -> {
                            // Không làm gì vì đã là indicator
                        });
                    }

                    // Hide submit button and show rating text
                    submitRatingButton.setVisibility(View.GONE);
                    yourRatingText.setVisibility(View.VISIBLE);
                    yourRatingText.setText("Đánh giá của bạn: " + String.format("%.1f", rating) + " sao");

                    Log.d(TAG, " Rating submitted successfully: User=" + rating + ", Average=" + newAverageRating
                            + ", Count=" + newRatingCount);
                    Toast.makeText(RecipeDetailActivity.this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    submitRatingButton.setEnabled(true);
                    Log.e(TAG, " Error submitting rating: " + error);
                    Toast.makeText(RecipeDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String getDifficultyText(String difficulty) {
        if (difficulty == null)
            return "Không xác định";

        switch (difficulty.toLowerCase()) {
            case "easy":
            case "dễ":
                return "Dễ";
            case "medium":
            case "trung bình":
                return "Trung bình";
            case "hard":
            case "khó":
                return "Khó";
            default:
                return difficulty;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show edit menu if user is the author
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && recipe != null &&
                recipe.getAuthorId() != null && recipe.getAuthorId().equals(currentUser.getUid())) {
            getMenuInflater().inflate(R.menu.menu_recipe_detail, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            // Navigate to edit recipe
            navigateToEditRecipe();
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            // Delete recipe (confirm first)
            confirmDeleteRecipe();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToEditRecipe() {
        Intent intent = new Intent(this, EditRecipeActivity.class);
        intent.putExtra(EditRecipeActivity.EXTRA_RECIPE_ID, recipe.getId());
        intent.putExtra(EditRecipeActivity.EXTRA_RECIPE_TITLE, recipe.getTitle());
        intent.putExtra(EditRecipeActivity.EXTRA_RECIPE_DESCRIPTION, recipe.getDescription());
        intent.putExtra(EditRecipeActivity.EXTRA_RECIPE_IMAGE_URL, recipe.getImageUrl());
        intent.putExtra(EditRecipeActivity.EXTRA_RECIPE_PREP_TIME, recipe.getPrepTime());
        intent.putExtra(EditRecipeActivity.EXTRA_RECIPE_COOK_TIME, recipe.getCookTime());
        intent.putExtra(EditRecipeActivity.EXTRA_RECIPE_SERVINGS, recipe.getServings());
        intent.putExtra(EditRecipeActivity.EXTRA_RECIPE_DIFFICULTY, recipe.getDifficulty());

        if (recipe.getCategories() != null) {
            intent.putExtra(EditRecipeActivity.EXTRA_RECIPE_CATEGORIES,
                    recipe.getCategories().toArray(new String[0]));
        }
        if (recipe.getIngredients() != null) {
            intent.putExtra(EditRecipeActivity.EXTRA_RECIPE_INGREDIENTS,
                    recipe.getIngredients().toArray(new String[0]));
        }
        if (recipe.getInstructions() != null) {
            intent.putExtra(EditRecipeActivity.EXTRA_RECIPE_INSTRUCTIONS,
                    recipe.getInstructions().toArray(new String[0]));
        }

        startActivity(intent);
        finish(); // Close detail and refresh when coming back
    }

    private void confirmDeleteRecipe() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa công thức")
                .setMessage("Bạn có chắc chắn muốn xóa công thức này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteRecipe())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteRecipe() {
        com.example.cookshare.services.FirebaseDatabaseService service = new com.example.cookshare.services.FirebaseDatabaseService();
        service.deleteRecipe(recipe.getId());
        Toast.makeText(this, "Đã xóa công thức", Toast.LENGTH_SHORT).show();
        finish();
    }
}