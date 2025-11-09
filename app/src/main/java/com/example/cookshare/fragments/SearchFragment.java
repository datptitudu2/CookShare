package com.example.cookshare.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final long SEARCH_DEBOUNCE_DELAY = 500; // 500ms delay

    private RecipeViewModel recipeViewModel;
    private RecipeAdapter recipeAdapter;
    private RecyclerView recyclerView;
    private LinearLayout loadingContainer;
    private LinearLayout emptyStateContainer;
    private TextInputEditText searchEditText;

    // Handler for debounce
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
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

            // Setup search functionality with debounce
            setupSearch();

            // Observe data
            observeData();

            // Initial state: hide all views
            hideAllViews();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi khởi tạo màn hình tìm kiếm: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancel pending search to avoid memory leaks
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        loadingContainer = view.findViewById(R.id.loadingContainer);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        searchEditText = view.findViewById(R.id.searchEditText);

        if (recyclerView == null) {
            Log.e(TAG, "recyclerView is null!");
        }
        if (loadingContainer == null) {
            Log.e(TAG, "loadingContainer is null!");
        }
        if (emptyStateContainer == null) {
            Log.e(TAG, "emptyStateContainer is null!");
        }
        if (searchEditText == null) {
            Log.e(TAG, "searchEditText is null!");
        }
    }

    private void initializeViewModel() {
        try {
            if (getActivity() == null) {
                Log.e(TAG, "Activity is null, cannot initialize ViewModel");
                return;
            }
            recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);
            Log.d(TAG, "ViewModel initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ViewModel", e);
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi khởi tạo ứng dụng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupRecyclerView() {
        try {
            if (recyclerView == null) {
                Log.e(TAG, "Cannot setup RecyclerView: recyclerView is null");
                return;
            }

            recipeAdapter = new RecipeAdapter();

            // Set click listener to navigate to recipe detail
            recipeAdapter.setOnRecipeClickListener(recipe -> {
                try {
                    if (recipe != null) {
                        navigateToRecipeDetail(recipe);
                    } else {
                        Log.e(TAG, "Recipe is null in click listener");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to recipe detail", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi mở công thức", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Set like click listener
            recipeAdapter.setOnLikeClickListener((recipe, isLiked) -> {
                try {
                    if (recipe != null && recipe.getId() != null && !recipe.getId().isEmpty()) {
                        Log.d(TAG, "Like clicked: recipeId=" + recipe.getId() + ", isLiked=" + isLiked);
                        if (recipeViewModel != null) {
                            recipeViewModel.updateLikeCount(recipe.getId(), isLiked);
                        }
                        if (getContext() != null) {
                            Toast.makeText(getContext(),
                                    isLiked ? "Đã thích công thức" : "Đã bỏ thích công thức",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Cannot like: recipe or recipeId is null");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in like click", e);
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(recipeAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void navigateToRecipeDetail(Recipe recipe) {
        try {
            if (getActivity() == null || recipe == null) {
                Log.e(TAG, "Cannot navigate: activity or recipe is null");
                return;
            }

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
            Log.d(TAG, "Navigated to RecipeDetailActivity for recipe: " + recipe.getId());
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to RecipeDetailActivity", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi mở công thức", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupSearch() {
        if (searchEditText == null) {
            Log.e(TAG, "Cannot setup search: searchEditText is null");
            return;
        }

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Cancel previous search if user is still typing
                if (searchHandler != null && searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous search
                if (searchHandler != null && searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                final String query = s.toString().trim();

                // Create new search runnable
                searchRunnable = () -> {
                    try {
                        // Check if fragment is still attached
                        if (getContext() == null || getActivity() == null) {
                            Log.w(TAG, "Fragment not attached, skipping search");
                            return;
                        }

                        if (query.length() >= 2) {
                            // Search with minimum 2 characters
                            Log.d(TAG, "Searching for: " + query);
                            if (recipeViewModel != null) {
                                recipeViewModel.searchRecipes(query);
                            } else {
                                Log.e(TAG, "recipeViewModel is null!");
                            }
                        } else if (query.isEmpty()) {
                            // Clear results when search is empty
                            Log.d(TAG, "Search query is empty, clearing results");
                            if (recipeAdapter != null) {
                                recipeAdapter.updateRecipes(null);
                            }
                            if (getContext() != null) {
                                hideAllViews();
                            }
                        } else {
                            // Query is too short (1 character), wait for more input
                            Log.d(TAG, "Query too short: " + query);
                            if (getContext() != null) {
                                hideAllViews();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in search runnable", e);
                        e.printStackTrace();
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi tìm kiếm: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

                // Schedule search after debounce delay
                if (searchHandler != null) {
                    searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY);
                } else {
                    Log.e(TAG, "searchHandler is null!");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void observeData() {
        try {
            if (recipeViewModel == null) {
                Log.e(TAG, "Cannot observe data: recipeViewModel is null");
                return;
            }

            // Observe recipes
            recipeViewModel.getRecipesLiveData().observe(getViewLifecycleOwner(), recipes -> {
                try {
                    // Check if fragment is still attached
                    if (getContext() == null || getActivity() == null) {
                        Log.w(TAG, "Fragment not attached, skipping update");
                        return;
                    }

                    if (recipes != null && !recipes.isEmpty()) {
                        Log.d(TAG, "Received " + recipes.size() + " search results");
                        if (recipeAdapter != null) {
                            recipeAdapter.updateRecipes(recipes);
                        }
                        showResults();
                    } else {
                        Log.d(TAG, "No search results found");
                        showEmptyState();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in recipes observer", e);
                    e.printStackTrace();
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi hiển thị kết quả: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Observe loading state
            recipeViewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
                try {
                    // Check if fragment is still attached
                    if (getContext() == null || getActivity() == null) {
                        Log.w(TAG, "Fragment not attached, skipping loading update");
                        return;
                    }

                    if (isLoading != null && isLoading) {
                        Log.d(TAG, "Loading started");
                        showLoading();
                    } else {
                        Log.d(TAG, "Loading finished");
                        // Don't hide loading here, let recipes observer handle visibility
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in loading observer", e);
                    e.printStackTrace();
                }
            });

            // Observe errors
            recipeViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
                try {
                    // Check if fragment is still attached
                    if (getContext() == null || getActivity() == null) {
                        Log.w(TAG, "Fragment not attached, skipping error handling");
                        return;
                    }

                    if (error != null) {
                        Log.e(TAG, "Error: " + error);
                        showError(error);
                        if (recipeViewModel != null) {
                            recipeViewModel.clearError();
                        }
                        // Show empty state on error
                        showEmptyState();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in error observer", e);
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up observers", e);
        }
    }

    private void hideAllViews() {
        try {
            if (getContext() == null || getActivity() == null) {
                return;
            }
            if (loadingContainer != null) {
                loadingContainer.setVisibility(View.GONE);
            }
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisibility(View.GONE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding views", e);
        }
    }

    private void showLoading() {
        try {
            if (getContext() == null || getActivity() == null) {
                return;
            }
            if (loadingContainer != null) {
                loadingContainer.setVisibility(View.VISIBLE);
            }
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisibility(View.GONE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing loading", e);
        }
    }

    private void showResults() {
        try {
            if (getContext() == null || getActivity() == null) {
                return;
            }
            if (loadingContainer != null) {
                loadingContainer.setVisibility(View.GONE);
            }
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisibility(View.GONE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing results", e);
        }
    }

    private void showEmptyState() {
        try {
            if (getContext() == null || getActivity() == null) {
                return;
            }
            if (loadingContainer != null) {
                loadingContainer.setVisibility(View.GONE);
            }
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisibility(View.VISIBLE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing empty state", e);
        }
    }

    private void showError(String error) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, "Error: " + error);
    }
}
