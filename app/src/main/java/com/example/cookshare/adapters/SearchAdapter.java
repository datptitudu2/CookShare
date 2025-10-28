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
 * Adapter for Search Results
 * Created for: Mai Đình Phúc (Search + Home Support)
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<Recipe> searchResults;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public SearchAdapter(List<Recipe> searchResults, OnRecipeClickListener listener) {
        this.searchResults = searchResults;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Recipe recipe = searchResults.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public void updateSearchResults(List<Recipe> newResults) {
        this.searchResults = newResults;
        notifyDataSetChanged();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {
        private ImageView recipeImage;
        private TextView recipeTitle;
        private TextView recipeDescription;
        private TextView recipeCategory;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipe_image);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
            recipeDescription = itemView.findViewById(R.id.recipe_description);
            recipeCategory = itemView.findViewById(R.id.recipe_category);
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
