package com.example.cookshare.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookshare.R;
import com.example.cookshare.adapters.MyRecipesAdapter;
import com.example.cookshare.models.Recipe;
import com.example.cookshare.services.FirebaseDatabaseService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MyRecipesFragment extends Fragment implements MyRecipesAdapter.OnRecipeActionListener {

    private FirebaseDatabaseService firebaseService;
    private MyRecipesAdapter adapter;

    // Views
    private RecyclerView recipesRecyclerView;
    private View loadingContainer;
    private View emptyContainer;
    private View errorContainer;
    private FloatingActionButton addRecipeButton;
    private MaterialButton emptyAddButton;
    private MaterialButton retryButton;

    public MyRecipesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseService = new FirebaseDatabaseService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadMyRecipes();
    }

    private void initializeViews(View view) {
        recipesRecyclerView = view.findViewById(R.id.recipesRecyclerView);
        loadingContainer = view.findViewById(R.id.loadingContainer);
        emptyContainer = view.findViewById(R.id.emptyContainer);
        errorContainer = view.findViewById(R.id.errorContainer);
        addRecipeButton = view.findViewById(R.id.addRecipeButton);
        emptyAddButton = view.findViewById(R.id.emptyAddButton);
        retryButton = view.findViewById(R.id.retryButton);
    }

    private void setupRecyclerView() {
        adapter = new MyRecipesAdapter(new ArrayList<>(), this);
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipesRecyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        addRecipeButton.setOnClickListener(v -> openAddRecipeForm());
        emptyAddButton.setOnClickListener(v -> openAddRecipeForm());
        retryButton.setOnClickListener(v -> loadMyRecipes());
    }

    private void loadMyRecipes() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showError("Vui lòng đăng nhập");
            return;
        }

        showLoading();

        firebaseService.getUserRecipes(currentUser.getUid(), new FirebaseDatabaseService.RecipeListCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    hideLoading();
                    if (recipes.isEmpty()) {
                        showEmpty();
                    } else {
                        showRecipes(recipes);
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    hideLoading();
                    showError(error);
                });
            }
        });
    }

    private void openAddRecipeForm() {
        if (getActivity() == null) return;

        // Navigate to AddRecipeFragmentSimple (form tạo công thức đơn giản)
        AddRecipeFragment addRecipeFragment = new AddRecipeFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, addRecipeFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onEditRecipe(Recipe recipe) {
        // Navigate to EditRecipeActivity
        Intent intent = new Intent(requireContext(), com.example.cookshare.EditRecipeActivity.class);
        
        // Basic info
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_ID, recipe.getId());
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_TITLE, recipe.getTitle());
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_DESCRIPTION, recipe.getDescription());
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_PREP_TIME, recipe.getPrepTime());
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_COOK_TIME, recipe.getCookTime());
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_SERVINGS, recipe.getServings());
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_DIFFICULTY, recipe.getDifficulty());
        
        // Categories
        if (recipe.getCategories() != null && !recipe.getCategories().isEmpty()) {
            intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_CATEGORIES, 
                recipe.getCategories().toArray(new String[0]));
        }
        
        // Ingredients
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_INGREDIENTS, 
                recipe.getIngredients().toArray(new String[0]));
        }
        
        // Instructions
        if (recipe.getInstructions() != null && !recipe.getInstructions().isEmpty()) {
            intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_INSTRUCTIONS, 
                recipe.getInstructions().toArray(new String[0]));
        }
        
        // Metadata
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_IMAGE_URL, recipe.getImageUrl());
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_AUTHOR_ID, recipe.getAuthorId());
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_AUTHOR_NAME, recipe.getAuthorName());
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_CREATED_AT, recipe.getCreatedAt());
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_RATING, recipe.getRating());
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_RATING_COUNT, recipe.getRatingCount());
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_VIEW_COUNT, recipe.getViewCount());
        intent.putExtra(com.example.cookshare.EditRecipeActivity.EXTRA_RECIPE_LIKE_COUNT, recipe.getLikeCount());

        startActivity(intent);
    }

    @Override
    public void onDeleteRecipe(Recipe recipe) {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa công thức \"" + recipe.getTitle() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteRecipe(recipe);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteRecipe(Recipe recipe) {
        if (recipe.getId() == null) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID công thức", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseService.deleteRecipe(recipe.getId());
        Toast.makeText(getContext(), "Đã xóa: " + recipe.getTitle(), Toast.LENGTH_SHORT).show();
        
        // Reload recipes
        loadMyRecipes();
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        // TODO: Navigate to RecipeDetailActivity or Fragment
        Toast.makeText(getContext(), "Xem: " + recipe.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void showLoading() {
        loadingContainer.setVisibility(View.VISIBLE);
        recipesRecyclerView.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingContainer.setVisibility(View.GONE);
    }

    private void showRecipes(List<Recipe> recipes) {
        recipesRecyclerView.setVisibility(View.VISIBLE);
        emptyContainer.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        adapter.updateRecipes(recipes);
    }

    private void showEmpty() {
        recipesRecyclerView.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.VISIBLE);
        errorContainer.setVisibility(View.GONE);
    }

    private void showError(String error) {
        recipesRecyclerView.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.GONE);
        errorContainer.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload recipes when returning from add/edit
        loadMyRecipes();
    }
}
