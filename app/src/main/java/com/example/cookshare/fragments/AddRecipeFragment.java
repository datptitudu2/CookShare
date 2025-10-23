package com.example.cookshare.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cookshare.R;
import com.example.cookshare.models.Recipe;
import com.example.cookshare.viewmodels.AddRecipeViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AddRecipeFragment extends Fragment {

    private AddRecipeViewModel addRecipeViewModel;

    // Form fields
    private TextInputEditText titleEditText;
    private TextInputEditText descriptionEditText;
    private TextInputEditText cookTimeEditText;
    private TextInputEditText ingredientsEditText;
    private TextInputEditText instructionsEditText;
    private MaterialAutoCompleteTextView categorySpinner;
    private MaterialButton submitButton;
    private ImageButton backButton;
    private MaterialCardView uploadImageCard;

    // State containers
    private View loadingContainer;
    private View successContainer;

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
        addRecipeViewModel = new ViewModelProvider(this).get(AddRecipeViewModel.class);

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
        titleEditText = view.findViewById(R.id.titleEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        cookTimeEditText = view.findViewById(R.id.cookTimeEditText);
        ingredientsEditText = view.findViewById(R.id.ingredientsEditText);
        instructionsEditText = view.findViewById(R.id.instructionsEditText);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        submitButton = view.findViewById(R.id.submitButton);
        backButton = view.findViewById(R.id.backButton);
        uploadImageCard = view.findViewById(R.id.uploadImageCard);

        // State containers
        loadingContainer = view.findViewById(R.id.loadingContainer);
        successContainer = view.findViewById(R.id.successContainer);
    }

    private void setupForm() {
        // Setup category spinner
        String[] categoryOptions = {"Món chính", "Món phụ", "Canh", "Món nướng", "Chiên", "Hấp", "Tráng miệng", "Đồ uống"};
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

        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        uploadImageCard.setOnClickListener(v -> {
            // TODO: Implement image picker
            Toast.makeText(getContext(), "Chức năng chọn ảnh đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate title
        if (TextUtils.isEmpty(titleEditText.getText())) {
            titleEditText.setError("Vui lòng nhập tên món ăn");
            isValid = false;
        }

        // Validate description
        if (TextUtils.isEmpty(descriptionEditText.getText())) {
            descriptionEditText.setError("Vui lòng nhập mô tả");
            isValid = false;
        }

        // Validate cook time
        if (TextUtils.isEmpty(cookTimeEditText.getText())) {
            cookTimeEditText.setError("Vui lòng nhập thời gian nấu");
            isValid = false;
        }

        // Validate ingredients
        if (TextUtils.isEmpty(ingredientsEditText.getText())) {
            ingredientsEditText.setError("Vui lòng nhập nguyên liệu");
            isValid = false;
        }

        // Validate instructions
        if (TextUtils.isEmpty(instructionsEditText.getText())) {
            instructionsEditText.setError("Vui lòng nhập các bước nấu");
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
        recipe.setAuthorName(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail());

        // Parse cook time
        int cookTime = Integer.parseInt(cookTimeEditText.getText().toString());
        recipe.setPrepTime(0); // Default prep time
        recipe.setCookTime(cookTime);
        recipe.setServings(4); // Default servings
        recipe.setDifficulty("Trung bình"); // Default difficulty

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
        addRecipeViewModel.createRecipe(recipe);
    }

    private void observeData() {
        // Observe uploading state
        addRecipeViewModel.getIsUploading().observe(getViewLifecycleOwner(), isUploading -> {
            if (isUploading != null && isUploading) {
                showLoading();
            } else {
                hideLoading();
            }
        });

        // Observe upload message (errors)
        addRecipeViewModel.getUploadMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && message.startsWith("Lỗi:")) {
                showError(message);
            }
        });

        // Observe upload success
        addRecipeViewModel.getUploadSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                showSuccess();
            }
        });
    }

    private void showLoading() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.VISIBLE);
        }
        if (successContainer != null) {
            successContainer.setVisibility(View.GONE);
        }
        if (submitButton != null) {
            submitButton.setEnabled(false);
            submitButton.setText("Đang tạo...");
        }
    }

    private void hideLoading() {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(View.GONE);
        }
        if (submitButton != null) {
            submitButton.setEnabled(true);
            submitButton.setText("Đăng công thức");
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

        // Navigate back after 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }, 2000);
    }

    private void showError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }
}
