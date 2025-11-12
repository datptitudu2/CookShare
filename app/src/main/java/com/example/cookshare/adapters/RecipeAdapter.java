package com.example.cookshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookshare.R;
import com.example.cookshare.models.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes;
    private OnRecipeClickListener onRecipeClickListener;
    private OnLikeClickListener onLikeClickListener;
    // Cache like state để tránh query Firebase nhiều lần
    private Map<String, Boolean> likeStateCache = new HashMap<>();

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

    public void updateRecipes(List<Recipe> newRecipes, Boolean defaultLikeState) {
        this.recipes.clear();
        if (newRecipes != null) {
            this.recipes.addAll(newRecipes);
        }


        // Nếu một trạng thái mặc định được cung cấp (ví dụ: true),
        // hãy khởi động bộ đệm cache.
        if (defaultLikeState != null && newRecipes != null) {
            for (Recipe recipe : newRecipes) {
                if (recipe.getId() != null) {
                    this.likeStateCache.put(recipe.getId(), defaultLikeState);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Hàm updateRecipes cũ - gọi hàm mới với trạng thái null (để tương thích ngược).
     */
    public void updateRecipes(List<Recipe> newRecipes) {
        updateRecipes(newRecipes, null); //
    }

    /**
     * Hàm chính để thêm công thức, có tùy chọn đặt trạng thái thích mặc định.
     */
    public void addRecipes(List<Recipe> newRecipes, Boolean defaultLikeState) {
        if (newRecipes == null || newRecipes.isEmpty()) return;

        int startPosition = this.recipes.size();
        this.recipes.addAll(newRecipes);


        // Khởi động bộ đệm cache cho các item mới.
        if (defaultLikeState != null) {
            for (Recipe recipe : newRecipes) {
                if (recipe.getId() != null) {
                    this.likeStateCache.put(recipe.getId(), defaultLikeState);
                }
            }
        }
        notifyItemRangeInserted(startPosition, newRecipes.size());
    }


    public List<Recipe> getRecipes() {
        return new ArrayList<>(this.recipes);
    }
    public void addRecipes(List<Recipe> newRecipes) {
        addRecipes(newRecipes, null); //
    }
    public void clearRecipes() {
        this.recipes.clear();
        notifyDataSetChanged();
    }

    public void updateLikeState(String recipeId, boolean isLiked) {
        if (recipeId != null) {
            likeStateCache.put(recipeId, isLiked);
        }
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
        private TextView recipeCategory;
        private TextView cookTime;
        private TextView servings;
        private TextView difficulty;
        private TextView rating;
        private TextView ratingCount;
        private TextView viewCount;
        private TextView likeCount;
        private ImageView likeButton;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            recipeImage = itemView.findViewById(R.id.recipe_image);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
            authorName = itemView.findViewById(R.id.authorName);
            recipeDescription = itemView.findViewById(R.id.recipe_description);
            recipeCategory = itemView.findViewById(R.id.recipe_category);
            cookTime = itemView.findViewById(R.id.cookTime);
            servings = itemView.findViewById(R.id.servings);
            difficulty = itemView.findViewById(R.id.difficulty);
            rating = itemView.findViewById(R.id.rating);
            ratingCount = itemView.findViewById(R.id.ratingCount);
            viewCount = itemView.findViewById(R.id.recipe_views);
            likeCount = itemView.findViewById(R.id.recipe_likes);
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
                        // Toggle like state
                        boolean isLiked = likeButton.getTag() != null && (Boolean) likeButton.getTag();
                        boolean newLikeState = !isLiked;

                        // Update cache ngay lập tức
                        String recipeId = recipe.getId();
                        if (recipeId != null) {
                            RecipeAdapter.this.likeStateCache.put(recipeId, newLikeState);
                        }

                        // Update UI ngay lập tức (optimistic update)
                        updateLikeButton(newLikeState);

                        // Update recipe like count
                        int currentLikeCount = recipe.getLikeCount();
                        recipe.setLikeCount(newLikeState ? currentLikeCount + 1 : Math.max(0, currentLikeCount - 1));
                        likeCount.setText(formatLikeCount(recipe.getLikeCount()));

                        // Call listener để update Firebase
                        onLikeClickListener.onLikeClick(recipe, newLikeState);
                    }
                }
            });
        }

        public void bind(Recipe recipe) {
            // Set recipe data
            recipeTitle.setText(recipe.getTitle() != null ? recipe.getTitle() : "Chưa có tên");
            authorName.setText(recipe.getAuthorName() != null ? recipe.getAuthorName() : "Ẩn danh");
            recipeDescription.setText(recipe.getDescription() != null ? recipe.getDescription() : "Chưa có mô tả");

            // Category
            if (recipe.getCategories() != null && !recipe.getCategories().isEmpty()) {
                recipeCategory.setText(recipe.getCategories().get(0));
                recipeCategory.setVisibility(View.VISIBLE);
            } else {
                recipeCategory.setVisibility(View.GONE);
            }

            // Format time
            if (recipe.getTotalTime() > 0) {
                cookTime.setText(recipe.getFormattedTime());
            } else {
                cookTime.setText("Chưa xác định");
            }

            // Format servings
            if (recipe.getServings() > 0) {
                servings.setText(recipe.getServings() + " người");
            } else {
                servings.setText("Chưa xác định");
            }

            // Format difficulty
            String difficultyText = getDifficultyText(recipe.getDifficulty());
            difficulty.setText(difficultyText);

            // Format rating
            rating.setText(recipe.getFormattedRating());
            ratingCount.setText("(" + recipe.getRatingCount() + ")");

            // Format view count
            viewCount.setText(formatViewCount(recipe.getViewCount()));

            // Format like count
            likeCount.setText(formatLikeCount(recipe.getLikeCount()));

            // Check like state from Firebase
            checkLikeState(recipe.getId());

            // Load recipe image with Glide
            if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(recipe.getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .centerCrop()
                        .into(recipeImage);
            } else {
                recipeImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
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

        private String formatLikeCount(int count) {
            if (count < 1000) {
                return count + " lượt thích";
            } else if (count < 1000000) {
                return String.format("%.1fK lượt thích", count / 1000.0);
            } else {
                return String.format("%.1fM lượt thích", count / 1000000.0);
            }
        }

        private void checkLikeState(String recipeId) {
            if (recipeId == null) {
                updateLikeButton(false);
                return;
            }

            // Kiểm tra cache trước (để tránh query Firebase mỗi lần bind)
            Boolean cachedState = RecipeAdapter.this.likeStateCache.get(recipeId);
            if (cachedState != null) {
                // Sử dụng cached state ngay lập tức
                updateLikeButton(cachedState);
                return;
            }

            // Nếu chưa có trong cache, query Firebase
            com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance()
                    .getCurrentUser();
            if (currentUser == null) {
                updateLikeButton(false);
                RecipeAdapter.this.likeStateCache.put(recipeId, false);
                return;
            }

            com.google.firebase.database.DatabaseReference favoritesRef = com.google.firebase.database.FirebaseDatabase
                    .getInstance()
                    .getReference("favorites")
                    .child(currentUser.getUid())
                    .child("recipeIds")
                    .child(recipeId);

            favoritesRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    boolean isLiked = snapshot.exists() && snapshot.getValue() != null
                            && !snapshot.getValue().equals(false);
                    // Cache kết quả để dùng lại sau
                    RecipeAdapter.this.likeStateCache.put(recipeId, isLiked);
                    updateLikeButton(isLiked);
                }

                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    // Default to false on error và cache lại
                    RecipeAdapter.this.likeStateCache.put(recipeId, false);
                    updateLikeButton(false);
                }
            });
        }

        private void updateLikeButton(boolean isLiked) {
            likeButton.setTag(isLiked);
            if (isLiked) {
                // Filled star với màu vàng/cam đẹp
                likeButton.setImageResource(android.R.drawable.btn_star_big_on);
                likeButton.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.accent_color));
            } else {
                // Outline star với màu xám
                likeButton.setImageResource(android.R.drawable.btn_star_big_off);
                likeButton.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            }
        }
    }
}

