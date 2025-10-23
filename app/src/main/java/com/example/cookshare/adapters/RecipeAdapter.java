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

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes;
    private OnRecipeClickListener onRecipeClickListener;
    private OnLikeClickListener onLikeClickListener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public interface OnLikeClickListener {
        void onLikeClick(Recipe recipe, boolean isLiked);
    }

    public RecipeAdapter() {
        this.recipes = new ArrayList<>();
    }

    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.onRecipeClickListener = listener;
    }

    public void setOnLikeClickListener(OnLikeClickListener listener) {
        this.onLikeClickListener = listener;
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes.clear();
        this.recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    public void addRecipes(List<Recipe> newRecipes) {
        int startPosition = this.recipes.size();
        this.recipes.addAll(newRecipes);
        notifyItemRangeInserted(startPosition, newRecipes.size());
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private ImageView recipeImage;
        private TextView recipeTitle;
        private TextView authorName;
        private TextView recipeDescription;
        private TextView cookTime;
        private TextView servings;
        private TextView difficulty;
        private TextView rating;
        private TextView ratingCount;
        private TextView viewCount;
        private ImageView likeButton;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            recipeImage = itemView.findViewById(R.id.recipe_image);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
            authorName = itemView.findViewById(R.id.authorName);
            recipeDescription = itemView.findViewById(R.id.recipe_description);
            cookTime = itemView.findViewById(R.id.cookTime);
            servings = itemView.findViewById(R.id.servings);
            difficulty = itemView.findViewById(R.id.difficulty);
            rating = itemView.findViewById(R.id.rating);
            ratingCount = itemView.findViewById(R.id.ratingCount);
            viewCount = itemView.findViewById(R.id.recipe_views);
            likeButton = itemView.findViewById(R.id.likeButton);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (onRecipeClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onRecipeClickListener.onRecipeClick(recipes.get(position));
                    }
                }
            });

            likeButton.setOnClickListener(v -> {
                if (onLikeClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Recipe recipe = recipes.get(position);
                        // Toggle like state (simplified for now)
                        boolean isLiked = likeButton.getTag() != null && (Boolean) likeButton.getTag();
                        onLikeClickListener.onLikeClick(recipe, !isLiked);
                    }
                }
            });
        }

        public void bind(Recipe recipe) {
            // Set recipe data
            recipeTitle.setText(recipe.getTitle());
            authorName.setText(recipe.getAuthorName());
            recipeDescription.setText(recipe.getDescription());

            // Format time
            cookTime.setText(recipe.getFormattedTime());

            // Format servings
            servings.setText(recipe.getServings() + " người");

            // Format difficulty
            String difficultyText = getDifficultyText(recipe.getDifficulty());
            difficulty.setText(difficultyText);

            // Format rating
            rating.setText(recipe.getFormattedRating());
            ratingCount.setText("(" + recipe.getRatingCount() + ")");

            // Format view count
            viewCount.setText(formatViewCount(recipe.getViewCount()));

            // Set like button state (simplified)
            boolean isLiked = false; // TODO: Check if user liked this recipe
            likeButton.setTag(isLiked);
            likeButton.setImageResource(
                    isLiked ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
            likeButton.setColorFilter(isLiked ? itemView.getContext().getResources().getColor(R.color.accent_color)
                    : itemView.getContext().getResources().getColor(R.color.text_secondary));

            // TODO: Load recipe image with Glide
            // Glide.with(itemView.getContext())
            // .load(recipe.getImageUrl())
            // .placeholder(R.drawable.placeholder_recipe)
            // .into(recipeImage);
        }

        private String getDifficultyText(String difficulty) {
            if (difficulty == null)
                return "Không xác định";

            switch (difficulty.toLowerCase()) {
                case "easy":
                    return "Dễ";
                case "medium":
                    return "Trung bình";
                case "hard":
                    return "Khó";
                default:
                    return difficulty;
            }
        }

        private String formatViewCount(int count) {
            if (count < 1000) {
                return count + " lượt xem";
            } else if (count < 1000000) {
                return String.format("%.1fK lượt xem", count / 1000.0);
            } else {
                return String.format("%.1fM lượt xem", count / 1000000.0);
            }
        }
    }
}
