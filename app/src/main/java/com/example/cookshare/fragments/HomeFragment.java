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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookshare.R;
import com.example.cookshare.RecipeDetailActivity;
import com.example.cookshare.adapters.RecipeAdapter;
import com.example.cookshare.models.Recipe;
import com.example.cookshare.viewmodels.RecipeViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

public class HomeFragment extends Fragment {

    private RecipeViewModel recipeViewModel;
    private RecipeAdapter recipeAdapter;
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;
    private MaterialCardView popularCard;
    private MaterialCardView favoriteCard;
    private MaterialCardView quickCookCard;
    private View emptyStateLayout;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Initialize UI components
            initializeViews(view);

            // Initialize ViewModel
            initializeViewModel();

            // Setup RecyclerView
            setupRecyclerView();

            // Setup click listeners
            setupClickListeners();

            // Observe data
            observeData();

            // Load recipes
            if (recipeViewModel != null) {
                recipeViewModel.loadAllRecipes();
            }
        } catch (Exception e) {
            android.util.Log.e("HomeFragment", "Error in onViewCreated", e);
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "Lỗi khởi tạo màn hình: " + e.getMessage(),
                        android.widget.Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            // Reload data khi quay lại fragment (ví dụ: sau khi đăng công thức mới)
            if (recipeViewModel != null) {
                recipeViewModel.loadAllRecipes();
            }
        } catch (Exception e) {
            android.util.Log.e("HomeFragment", "Error in onResume", e);
        }
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        popularCard = view.findViewById(R.id.popularCard);
        favoriteCard = view.findViewById(R.id.favoriteCard);
        quickCookCard = view.findViewById(R.id.quickCookCard);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
    }

    private void initializeViewModel() {
        try {
            recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        } catch (Exception e) {
            android.util.Log.e("HomeFragment", "Error initializing ViewModel", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi khởi tạo ứng dụng", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupRecyclerView() {
        try {
            if (recyclerView == null) {
                android.util.Log.e("HomeFragment", "recyclerView is null!");
                return;
            }

            recipeAdapter = new RecipeAdapter();

            // Set click listeners
            recipeAdapter.setOnRecipeClickListener(recipe -> {
                try {
                    // Navigate to recipe detail
                    navigateToRecipeDetail(recipe);
                } catch (Exception e) {
                    android.util.Log.e("HomeFragment", "Error navigating to recipe detail", e);
                }
            });

            recipeAdapter.setOnLikeClickListener((recipe, isLiked) -> {
                try {
                    // Update like count
                    String recipeId = recipe != null ? recipe.getId() : null;
                    android.util.Log.d("HomeFragment",
                            " Like clicked: recipeId=" + recipeId + ", isLiked=" + isLiked);
                    if (recipeViewModel != null && recipe != null && recipeId != null && !recipeId.isEmpty()) {
                        recipeViewModel.updateLikeCount(recipeId, isLiked);
                    } else {
                        android.util.Log.e("HomeFragment",
                                " Cannot like: recipeId=" + recipeId + ", recipe=" + recipe);
                    }
                    if (getContext() != null) {
                        android.widget.Toast.makeText(getContext(),
                                isLiked ? "Đã thích công thức" : "Đã bỏ thích công thức",
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("HomeFragment", "Error in like click", e);
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(recipeAdapter);
        } catch (Exception e) {
            android.util.Log.e("HomeFragment", "Error setting up RecyclerView", e);
        }
    }

    private void navigateToRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
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

        // Pass arrays
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
    }

    private void setupClickListeners() {
        // Popular recipes - sorted by viewCount
        if (popularCard != null) {
            popularCard.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Đang tải món phổ biến...", Toast.LENGTH_SHORT).show();
                recipeViewModel.loadPopularRecipes();
            });
        }

        // Favorite recipes - sorted by rating and likeCount
        if (favoriteCard != null) {
            favoriteCard.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Đang tải món yêu thích...", Toast.LENGTH_SHORT).show();
                recipeViewModel.loadFavoriteRecipes();
            });
        }

        // Quick cook recipes - cookTime < 30 minutes
        if (quickCookCard != null) {
            quickCookCard.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Đang tải món nấu nhanh...", Toast.LENGTH_SHORT).show();
                recipeViewModel.loadQuickCookRecipes();
            });
        }
    }

    private void observeData() {
        try {
            if (recipeViewModel == null) {
                android.util.Log.e("HomeFragment", "recipeViewModel is null!");
                return;
            }

            // Observe recipes
            recipeViewModel.getRecipesLiveData().observe(getViewLifecycleOwner(), recipes -> {
                try {
                    if (recipes != null && !recipes.isEmpty()) {
                        if (recipeAdapter != null) {
                            recipeAdapter.updateRecipes(recipes);
                        }
                        hideEmptyState();
                    } else {
                        showEmptyState();
                    }
                } catch (Exception e) {
                    android.util.Log.e("HomeFragment", "Error in recipes observer", e);
                }
            });

            // Observe loading state
            recipeViewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
                try {
                    if (isLoading != null && isLoading) {
                        showLoading();
                    } else {
                        hideLoading();
                    }
                } catch (Exception e) {
                    android.util.Log.e("HomeFragment", "Error in loading observer", e);
                }
            });

            // Observe errors
            recipeViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
                try {
                    if (error != null) {
                        showError(error);
                        if (recipeViewModel != null) {
                            recipeViewModel.clearError();
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("HomeFragment", "Error in error observer", e);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("HomeFragment", "Error setting up observers", e);
        }
    }

    private void showLoading() {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.VISIBLE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void hideLoading() {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.VISIBLE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String error) {
        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
    }
}
