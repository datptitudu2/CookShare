package com.example.cookshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookshare.R;
import com.example.cookshare.models.Recipe;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MyRecipesAdapter extends RecyclerView.Adapter<MyRecipesAdapter.RecipeViewHolder> {

    private List<Recipe> recipes;
    private OnRecipeActionListener listener;

    public interface OnRecipeActionListener {
        void onEditRecipe(Recipe recipe);
        void onDeleteRecipe(Recipe recipe);
        void onRecipeClick(Recipe recipe);
    }

    public MyRecipesAdapter(List<Recipe> recipes, OnRecipeActionListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe, listener);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        if (newRecipes == null) {
            this.recipes.clear();
            notifyDataSetChanged();
            return;
        }
        
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }
    
    public void clearRecipes() {
        this.recipes.clear();
        notifyDataSetChanged();
    }
    
    public int getRecipeCount() {
        return recipes != null ? recipes.size() : 0;
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        private ImageView recipeImageView;
        private TextView recipeTitleText;
        private TextView recipeDescriptionText;
        private TextView recipeTimeText;
        private TextView recipeServingsText;
        private MaterialButton editButton;
        private MaterialButton deleteButton;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImageView = itemView.findViewById(R.id.recipeImageView);
            recipeTitleText = itemView.findViewById(R.id.recipeTitleText);
            recipeDescriptionText = itemView.findViewById(R.id.recipeDescriptionText);
            recipeTimeText = itemView.findViewById(R.id.recipeTimeText);
            recipeServingsText = itemView.findViewById(R.id.recipeServingsText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(Recipe recipe, OnRecipeActionListener listener) {
            // Title
            recipeTitleText.setText(recipe.getTitle() != null ? recipe.getTitle() : "Chưa có tên");
            
            // Description
            if (recipe.getDescription() != null && !recipe.getDescription().isEmpty()) {
                recipeDescriptionText.setText(recipe.getDescription());
                recipeDescriptionText.setVisibility(View.VISIBLE);
            } else {
                recipeDescriptionText.setText("Chưa có mô tả");
                recipeDescriptionText.setVisibility(View.VISIBLE);
            }

            // Calculate total time
            int totalTime = recipe.getPrepTime() + recipe.getCookTime();
            if (totalTime > 0) {
                recipeTimeText.setText("⏱️ " + totalTime + " phút");
                recipeTimeText.setVisibility(View.VISIBLE);
            } else {
                recipeTimeText.setVisibility(View.GONE);
            }
            
            // Servings
            if (recipe.getServings() > 0) {
                recipeServingsText.setText("🍽️ " + recipe.getServings() + " người");
                recipeServingsText.setVisibility(View.VISIBLE);
            } else {
                recipeServingsText.setVisibility(View.GONE);
            }

            // TODO: Load image with Glide/Picasso
            // if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            //     Glide.with(itemView.getContext())
            //         .load(recipe.getImageUrl())
            //         .placeholder(R.drawable.placeholder_recipe)
            //         .error(R.drawable.placeholder_recipe)
            //         .into(recipeImageView);
            // }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipe);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditRecipe(recipe);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteRecipe(recipe);
                }
            });
        }
    }
}
