package com.example.cookshare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cookshare.models.Recipe;
import com.example.cookshare.services.FirebaseDatabaseService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Activity để chỉnh sửa công thức đã tạo
 * Created for: Mai Trung Hiếu (Edit Recipe)
 *
 * Intent Extras cần nhận:
 * - EXTRA_RECIPE_ID: String
 * - EXTRA_RECIPE_TITLE: String
 * - EXTRA_RECIPE_DESCRIPTION: String
 * - EXTRA_RECIPE_IMAGE_URL: String
 * - EXTRA_RECIPE_PREP_TIME: int
 * - EXTRA_RECIPE_COOK_TIME: int
 * - EXTRA_RECIPE_SERVINGS: int
 * - EXTRA_RECIPE_DIFFICULTY: String
 * - EXTRA_RECIPE_CATEGORIES: String[]
 * - EXTRA_RECIPE_INGREDIENTS: String[]
 * - EXTRA_RECIPE_INSTRUCTIONS: String[]
 */
public class EditRecipeActivity extends AppCompatActivity {

    // Constants for Intent extras
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    public static final String EXTRA_RECIPE_TITLE = "recipe_title";
    public static final String EXTRA_RECIPE_DESCRIPTION = "recipe_description";
    public static final String EXTRA_RECIPE_IMAGE_URL = "recipe_image_url";
    public static final String EXTRA_RECIPE_PREP_TIME = "recipe_prep_time";
    public static final String EXTRA_RECIPE_COOK_TIME = "recipe_cook_time";
    public static final String EXTRA_RECIPE_SERVINGS = "recipe_servings";
    public static final String EXTRA_RECIPE_DIFFICULTY = "recipe_difficulty";
    public static final String EXTRA_RECIPE_CATEGORIES = "recipe_categories";
    public static final String EXTRA_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String EXTRA_RECIPE_INSTRUCTIONS = "recipe_instructions";
    public static final String EXTRA_RECIPE_AUTHOR_ID = "recipe_author_id";
    public static final String EXTRA_RECIPE_AUTHOR_NAME = "recipe_author_name";
    public static final String EXTRA_RECIPE_CREATED_AT = "recipe_created_at";
    public static final String EXTRA_RECIPE_RATING = "recipe_rating";
    public static final String EXTRA_RECIPE_RATING_COUNT = "recipe_rating_count";
    public static final String EXTRA_RECIPE_VIEW_COUNT = "recipe_view_count";
    public static final String EXTRA_RECIPE_LIKE_COUNT = "recipe_like_count";
    public static final String EXTRA_RECIPE_IS_PUBLISHED = "recipe_is_published";

    // Services
    private FirebaseDatabaseService firebaseService;

    // Views
    private TextInputEditText titleEditText;
    private TextInputEditText descriptionEditText;
    private TextInputEditText cookTimeEditText;
    private TextInputEditText ingredientsEditText;
    private TextInputEditText instructionsEditText;
    private MaterialAutoCompleteTextView categorySpinner;
    private MaterialButton submitButton;

    // State containers
    private View loadingContainer;
    private View successContainer;

    // Recipe data from Intent
    private String recipeId;
    private String imageUrl;
    private String authorId;
    private String authorName;
    private Date createdAt;
    private double rating;
    private int ratingCount;
    private int viewCount;
    private int likeCount;
    private boolean isPublished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        // Initialize service
        firebaseService = new FirebaseDatabaseService();

        // 1. Setup toolbar với back button
        setupToolbar();

        // 2. Initialize views (findViewById)
        initializeViews();

        // 3. Setup form dropdowns
        setupForm();

        // 4. Load recipe data từ Intent
        loadRecipeDataFromIntent();

        // 5. Setup button click listener để update recipe
        setupClickListeners();
    }

    // TODO 1: Setup toolbar với back button
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Chỉnh sửa công thức");
            }
        }
    }

    // TODO 2: Initialize views (findViewById)
    private void initializeViews() {
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        cookTimeEditText = findViewById(R.id.cookTimeEditText);
        ingredientsEditText = findViewById(R.id.ingredientsEditText);
        instructionsEditText = findViewById(R.id.instructionsEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        submitButton = findViewById(R.id.submitButton);

        // State containers
        loadingContainer = findViewById(R.id.loadingContainer);
        successContainer = findViewById(R.id.successContainer);
    }

    private void setupForm() {
        // Setup category spinner
        String[] categoryOptions = { "Món chính", "Món phụ", "Canh", "Món nướng", "Chiên", "Hấp", "Tráng miệng",
                "Đồ uống" };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categoryOptions);
        categorySpinner.setAdapter(categoryAdapter);
    }

    // TODO 3: Load dữ liệu recipe từ Intent extras và Pre-fill form
    private void loadRecipeDataFromIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(this, "Lỗi: Không nhận được dữ liệu", Toast.LENGTH_LONG).show();
            // Delay finish để user thấy Toast
            new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 2000);
            return;
        }

        // Get recipe ID (required)
        recipeId = intent.getStringExtra(EXTRA_RECIPE_ID);
        if (TextUtils.isEmpty(recipeId)) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID công thức", Toast.LENGTH_LONG).show();
            // Delay finish để user thấy Toast
            new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 2000);
            return;
        }

        // Load basic info
        String title = intent.getStringExtra(EXTRA_RECIPE_TITLE);
        String description = intent.getStringExtra(EXTRA_RECIPE_DESCRIPTION);
        int cookTime = intent.getIntExtra(EXTRA_RECIPE_COOK_TIME, 0);

        // Load metadata (to preserve)
        imageUrl = intent.getStringExtra(EXTRA_RECIPE_IMAGE_URL);
        authorId = intent.getStringExtra(EXTRA_RECIPE_AUTHOR_ID);
        authorName = intent.getStringExtra(EXTRA_RECIPE_AUTHOR_NAME);
        createdAt = (Date) intent.getSerializableExtra(EXTRA_RECIPE_CREATED_AT);
        rating = intent.getDoubleExtra(EXTRA_RECIPE_RATING, 0.0);
        ratingCount = intent.getIntExtra(EXTRA_RECIPE_RATING_COUNT, 0);
        viewCount = intent.getIntExtra(EXTRA_RECIPE_VIEW_COUNT, 0);
        likeCount = intent.getIntExtra(EXTRA_RECIPE_LIKE_COUNT, 0);
        isPublished = intent.getBooleanExtra(EXTRA_RECIPE_IS_PUBLISHED, true);

        // Pre-fill form (with null checks)
        if (titleEditText != null) {
            titleEditText.setText(title != null ? title : "");
        }
        if (descriptionEditText != null) {
            descriptionEditText.setText(description != null ? description : "");
        }
        if (cookTimeEditText != null) {
            cookTimeEditText.setText(String.valueOf(cookTime));
        }

        // Load categories
        String[] categories = intent.getStringArrayExtra(EXTRA_RECIPE_CATEGORIES);
        if (categorySpinner != null && categories != null && categories.length > 0) {
            categorySpinner.setText(categories[0], false);
        }

        // Load ingredients (join with newlines)
        String[] ingredients = intent.getStringArrayExtra(EXTRA_RECIPE_INGREDIENTS);
        if (ingredientsEditText != null && ingredients != null && ingredients.length > 0) {
            StringBuilder ingredientsText = new StringBuilder();
            for (int i = 0; i < ingredients.length; i++) {
                if (i > 0)
                    ingredientsText.append("\n");
                ingredientsText.append(ingredients[i]);
            }
            ingredientsEditText.setText(ingredientsText.toString());
        }

        // Load instructions (join with newlines)
        String[] instructions = intent.getStringArrayExtra(EXTRA_RECIPE_INSTRUCTIONS);
        if (instructionsEditText != null && instructions != null && instructions.length > 0) {
            StringBuilder instructionsText = new StringBuilder();
            for (int i = 0; i < instructions.length; i++) {
                if (i > 0)
                    instructionsText.append("\n");
                instructionsText.append(instructions[i]);
            }
            instructionsEditText.setText(instructionsText.toString());
        }
    }

    // TODO 5: Setup button click listener để update recipe
    private void setupClickListeners() {
        submitButton.setOnClickListener(v -> {
            if (validateForm()) {
                updateRecipe();
            }
        });
    }

    // TODO 4: Validate input trước khi update
    private boolean validateForm() {
        // Validate title
        if (TextUtils.isEmpty(titleEditText.getText())) {
            titleEditText.setError("Vui lòng nhập tên món ăn");
            titleEditText.requestFocus();
            return false;
        }

        // Validate description
        if (TextUtils.isEmpty(descriptionEditText.getText())) {
            descriptionEditText.setError("Vui lòng nhập mô tả");
            descriptionEditText.requestFocus();
            return false;
        }

        // Validate cook time
        if (TextUtils.isEmpty(cookTimeEditText.getText())) {
            cookTimeEditText.setError("Vui lòng nhập thời gian nấu");
            cookTimeEditText.requestFocus();
            return false;
        }

        // Validate category
        if (TextUtils.isEmpty(categorySpinner.getText())) {
            categorySpinner.setError("Vui lòng chọn danh mục");
            categorySpinner.requestFocus();
            return false;
        }

        // Validate ingredients
        if (TextUtils.isEmpty(ingredientsEditText.getText())) {
            ingredientsEditText.setError("Vui lòng nhập nguyên liệu");
            ingredientsEditText.requestFocus();
            return false;
        }

        // Validate instructions
        if (TextUtils.isEmpty(instructionsEditText.getText())) {
            instructionsEditText.setError("Vui lòng nhập các bước nấu");
            instructionsEditText.requestFocus();
            return false;
        }

        return true;
    }

    // TODO 5 & 6: Call FirebaseDatabaseService.updateRecipe() và Show
    // loading/success/error states
    private void updateRecipe() {
        showLoading();

        // Create updated recipe object
        Recipe updatedRecipe = new Recipe();
        updatedRecipe.setId(recipeId); // Keep the same ID
        updatedRecipe.setTitle(titleEditText.getText().toString().trim());
        updatedRecipe.setDescription(descriptionEditText.getText().toString().trim());
        updatedRecipe.setAuthorId(authorId); // Keep original author
        updatedRecipe.setAuthorName(authorName); // Keep original author name

        // Parse cook time
        int cookTime = Integer.parseInt(cookTimeEditText.getText().toString());
        updatedRecipe.setPrepTime(0); // Default prep time
        updatedRecipe.setCookTime(cookTime);
        updatedRecipe.setServings(4); // Default servings
        updatedRecipe.setDifficulty("Trung bình"); // Default difficulty

        // Set categories
        List<String> categories = new ArrayList<>();
        categories.add(categorySpinner.getText().toString());
        updatedRecipe.setCategories(categories);

        // Parse ingredients (split by newline)
        String ingredientsText = ingredientsEditText.getText().toString().trim();
        List<String> ingredients = new ArrayList<>();
        if (!ingredientsText.isEmpty()) {
            ingredients = Arrays.asList(ingredientsText.split("\\n"));
        }
        updatedRecipe.setIngredients(ingredients);

        // Parse instructions (split by newline)
        String instructionsText = instructionsEditText.getText().toString().trim();
        List<String> instructions = new ArrayList<>();
        if (!instructionsText.isEmpty()) {
            instructions = Arrays.asList(instructionsText.split("\\n"));
        }
        updatedRecipe.setInstructions(instructions);

        // Keep original values
        updatedRecipe.setCreatedAt(createdAt);
        updatedRecipe.setUpdatedAt(new Date()); // Update timestamp
        updatedRecipe.setIsPublished(isPublished);
        updatedRecipe.setRating(rating);
        updatedRecipe.setRatingCount(ratingCount);
        updatedRecipe.setViewCount(viewCount);
        updatedRecipe.setLikeCount(likeCount);
        updatedRecipe.setImageUrl(imageUrl);

        // Call FirebaseDatabaseService.updateRecipe() với callback
        firebaseService.updateRecipe(recipeId, updatedRecipe, new FirebaseDatabaseService.RecipeCallback() {
            @Override
            public void onSuccess(String recipeId) {
                runOnUiThread(() -> {
                    hideLoading();
                    showSuccess();

                    // Close activity after 2 seconds
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Toast.makeText(EditRecipeActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    }, 2000);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    showError(error);
                });
            }
        });
    }

    // TODO 6: Show loading/success/error states
    private void showLoading() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.VISIBLE);
        }
        if (successContainer != null) {
            successContainer.setVisibility(View.GONE);
        }
        if (submitButton != null) {
            submitButton.setEnabled(false);
            submitButton.setText("Đang cập nhật...");
        }
    }

    private void hideLoading() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.GONE);
        }
        if (submitButton != null) {
            submitButton.setEnabled(true);
            submitButton.setText("Cập nhật công thức");
        }
    }

    private void showSuccess() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.GONE);
        }
        if (successContainer != null) {
            successContainer.setVisibility(View.VISIBLE);
        }
        if (submitButton != null) {
            submitButton.setEnabled(false);
        }
    }

    private void showError(String error) {
        Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}