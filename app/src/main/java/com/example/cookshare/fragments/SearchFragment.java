package com.example.cookshare.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.cookshare.adapters.RecipeAdapter;
import com.example.cookshare.models.Recipe;
import com.example.cookshare.viewmodels.RecipeViewModel;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class SearchFragment extends Fragment {

    private RecipeViewModel recipeViewModel;
    private RecipeAdapter recipeAdapter;
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;
    private TextInputEditText searchEditText;

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

        // Initialize UI components
        initializeViews(view);

        // Initialize ViewModel
        initializeViewModel();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup search functionality
        setupSearch();

        // Observe data
        observeData();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        searchEditText = view.findViewById(R.id.searchEditText);
    }

    private void initializeViewModel() {
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
    }

    private void setupRecyclerView() {
        recipeAdapter = new RecipeAdapter();

        // Set click listeners
        recipeAdapter.setOnRecipeClickListener(recipe -> {
            // TODO: Navigate to recipe detail
            Toast.makeText(getContext(), "Clicked: " + recipe.getTitle(), Toast.LENGTH_SHORT).show();
        });

        recipeAdapter.setOnLikeClickListener((recipe, isLiked) -> {
            // Update like count
            recipeViewModel.updateLikeCount(recipe.getId(), isLiked);
            Toast.makeText(getContext(),
                    isLiked ? "Đã thích công thức" : "Đã bỏ thích công thức",
                    Toast.LENGTH_SHORT).show();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recipeAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    // Search with minimum 2 characters
                    recipeViewModel.searchRecipes(query);
                } else if (query.isEmpty()) {
                    // Show all recipes when search is empty
                    recipeViewModel.loadAllRecipes();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void observeData() {
        // Observe recipes
        recipeViewModel.getRecipesLiveData().observe(getViewLifecycleOwner(), recipes -> {
            if (recipes != null && !recipes.isEmpty()) {
                recipeAdapter.updateRecipes(recipes);
                hideEmptyState();
            } else {
                showEmptyState();
            }
        });

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
        // TODO: Show empty state UI
        Toast.makeText(getContext(), "Không tìm thấy công thức nào", Toast.LENGTH_SHORT).show();
    }

    private void hideEmptyState() {
        // TODO: Hide empty state UI
    }

    private void showError(String error) {
        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
    }
}
