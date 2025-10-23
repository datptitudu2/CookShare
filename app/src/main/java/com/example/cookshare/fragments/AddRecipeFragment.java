package com.example.cookshare.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cookshare.R;
import com.example.cookshare.models.Recipe;
import com.example.cookshare.viewmodels.RecipeViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AddRecipeFragment extends Fragment {

    private RecipeViewModel recipeViewModel;
    private CircularProgressIndicator progressIndicator;

    // Form fields
    private TextInputEditText titleEditText;
    private TextInputEditText descriptionEditText;
    private TextInputEditText prepTimeEditText;
    private TextInputEditText cookTimeEditText;
    private TextInputEditText servingsEditText;
    private TextInputEditText ingredientsEditText;
    private TextInputEditText instructionsEditText;
    private MaterialAutoCompleteTextView difficultySpinner;
    private MaterialAutoCompleteTextView categorySpinner;
    private MaterialButton submitButton;

    public AddRecipeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        // Initialize UI components
        initializeViews(view);

        // Setup form
        setupForm();

        // Setup click listeners
        setupClickListeners();

        // Observe data
        observeData();
    }

    private void initializeViews(View view) {
        progressIndicator = view.findViewById(R.id.progressIndicator);
        titleEditText = view.findViewById(R.id.titleEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        prepTimeEditText = view.findViewById(R.id.prepTimeEditText);
        cookTimeEditText = view.findViewById(R.id.cookTimeEditText);
        servingsEditText = view.findViewById(R.id.servingsEditText);
        ingredientsEditText = view.findViewById(R.id.ingredientsEditText);
        instructionsEditText = view.findViewById(R.id.instructionsEditText);
        difficultySpinner = view.findViewById(R.id.difficultySpinner);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        submitButton = view.findViewById(R.id.submitButton);
    }

    private void setupForm() {
        // Setup difficulty spinner
        String[] difficultyOptions = { "Dễ", "Trung bình", "Khó" };
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, difficultyOptions);
        difficultySpinner.setAdapter(difficultyAdapter);

        // Setup category spinner
        String[] categoryOptions = { "Món chính", "Món phụ", "Canh", "Món nướng", "Chiên", "Hấp", "Tráng miệng",
                "Đồ uống" };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, categoryOptions);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void setupClickListeners() {
        submitButton.setOnClickListener(v -> {
            if (validateForm()) {
                createRecipe();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate title
        if (TextUtils.isEmpty(titleEditText.getText())) {
            titleEditText.setError("Vui lòng nhập tên công thức");
            isValid = false;
        }

        // Validate description
        if (TextUtils.isEmpty(descriptionEditText.getText())) {
            descriptionEditText.setError("Vui lòng nhập mô tả công thức");
            isValid = false;
        }

        // Validate prep time
        if (TextUtils.isEmpty(prepTimeEditText.getText())) {
            prepTimeEditText.setError("Vui lòng nhập thời gian chuẩn bị");
            isValid = false;
        }

        // Validate cook time
        if (TextUtils.isEmpty(cookTimeEditText.getText())) {
            cookTimeEditText.setError("Vui lòng nhập thời gian nấu");
            isValid = false;
        }

        // Validate servings
        if (TextUtils.isEmpty(servingsEditText.getText())) {
            servingsEditText.setError("Vui lòng nhập số người ăn");
            isValid = false;
        }

        // Validate ingredients
        if (TextUtils.isEmpty(ingredientsEditText.getText())) {
            ingredientsEditText.setError("Vui lòng nhập nguyên liệu");
            isValid = false;
        }

        // Validate instructions
        if (TextUtils.isEmpty(instructionsEditText.getText())) {
            instructionsEditText.setError("Vui lòng nhập hướng dẫn nấu");
            isValid = false;
        }

        // Validate difficulty
        if (TextUtils.isEmpty(difficultySpinner.getText())) {
            difficultySpinner.setError("Vui lòng chọn độ khó");
            isValid = false;
        }

        // Validate category
        if (TextUtils.isEmpty(categorySpinner.getText())) {
            categorySpinner.setError("Vui lòng chọn danh mục");
            isValid = false;
        }

        return isValid;
    }

    private void createRecipe() {
        showLoading();

        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showError("Vui lòng đăng nhập để tạo công thức");
            hideLoading();
            return;
        }

        // Create recipe object
        Recipe recipe = new Recipe();
        recipe.setTitle(titleEditText.getText().toString().trim());
        recipe.setDescription(descriptionEditText.getText().toString().trim());
        recipe.setAuthorId(currentUser.getUid());
        recipe.setAuthorName(
                currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail());
        recipe.setPrepTime(Integer.parseInt(prepTimeEditText.getText().toString()));
        recipe.setCookTime(Integer.parseInt(cookTimeEditText.getText().toString()));
        recipe.setServings(Integer.parseInt(servingsEditText.getText().toString()));
        recipe.setDifficulty(difficultySpinner.getText().toString());

        // Set categories
        List<String> categories = new ArrayList<>();
        categories.add(categorySpinner.getText().toString());
        recipe.setCategories(categories);

        // Parse ingredients (split by newline)
        String ingredientsText = ingredientsEditText.getText().toString().trim();
        List<String> ingredients = new ArrayList<>();
        if (!ingredientsText.isEmpty()) {
            ingredients = Arrays.asList(ingredientsText.split("\\n"));
        }
        recipe.setIngredients(ingredients);

        // Parse instructions (split by newline)
        String instructionsText = instructionsEditText.getText().toString().trim();
        List<String> instructions = new ArrayList<>();
        if (!instructionsText.isEmpty()) {
            instructions = Arrays.asList(instructionsText.split("\\n"));
        }
        recipe.setInstructions(instructions);

        // Set default values
        recipe.setCreatedAt(new Date());
        recipe.setUpdatedAt(new Date());
        recipe.setIsPublished(true);
        recipe.setRating(0.0);
        recipe.setRatingCount(0);
        recipe.setViewCount(0);
        recipe.setLikeCount(0);

        // Set default image URL
        recipe.setImageUrl("https://via.placeholder.com/300x200?text=Recipe+Image");

        // Create recipe in Firebase
        recipeViewModel.createRecipe(recipe);
    }

    private void observeData() {
        // Observe loading state
        recipeViewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });

        // Observe errors
        recipeViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showError(error);
                recipeViewModel.clearError();
            }
        });
    }

    private void showLoading() {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.VISIBLE);
        }
        if (submitButton != null) {
            submitButton.setEnabled(false);
            submitButton.setText("Đang tạo...");
        }
    }

    private void hideLoading() {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.GONE);
        }
        if (submitButton != null) {
            submitButton.setEnabled(true);
            submitButton.setText("Tạo công thức");
        }
    }

    private void showError(String error) {
        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
    }
}
