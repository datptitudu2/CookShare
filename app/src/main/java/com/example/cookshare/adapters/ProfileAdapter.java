package com.example.cookshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookshare.R;
import com.example.cookshare.models.Recipe;

import java.util.List;

/**
 * Adapter for User's Recipes in Profile
 * Created for: Lê Hải An (Profile)
 *
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileRecipeViewHolder> {

    private List<Recipe> userRecipes;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);

        void onEditRecipe(Recipe recipe);

        void onDeleteRecipe(Recipe recipe);
    }

    public ProfileAdapter(List<Recipe> userRecipes, OnRecipeClickListener listener) {
        this.userRecipes = userRecipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfileRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ProfileRecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileRecipeViewHolder holder, int position) {
        Recipe recipe = userRecipes.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return userRecipes.size();
    }

    public void updateUserRecipes(List<Recipe> newRecipes) {
        this.userRecipes = newRecipes;
        notifyDataSetChanged();
    }

    class ProfileRecipeViewHolder extends RecyclerView.ViewHolder {
        private ImageView recipeImage;
        private TextView recipeTitle;
        private TextView recipeDescription;
        private TextView recipeCategory;
        private TextView recipeViews;
        private TextView recipeLikes;

        public ProfileRecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipe_image);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
            recipeDescription = itemView.findViewById(R.id.recipe_description);
            recipeCategory = itemView.findViewById(R.id.recipe_category);
            recipeViews = itemView.findViewById(R.id.recipe_views);
            recipeLikes = itemView.findViewById(R.id.recipe_likes);
        }

        public void bind(Recipe recipe) {
            recipeTitle.setText(recipe.getTitle());
            recipeDescription.setText(recipe.getDescription());

            // Handle categories - get first category if available
            if (recipe.getCategories() != null && !recipe.getCategories().isEmpty()) {
                recipeCategory.setText(recipe.getCategories().get(0));
            } else {
                recipeCategory.setText("Chưa phân loại");
            }

            recipeViews.setText(String.valueOf(recipe.getViewCount()));
            recipeLikes.setText(String.valueOf(recipe.getLikeCount()));

            if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(recipe.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(recipeImage);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipe);
                }
            });
        }
    }
}
