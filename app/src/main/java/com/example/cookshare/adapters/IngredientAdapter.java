package com.example.cookshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookshare.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for Recipe Ingredients in Add Recipe
 * Created for: Mai Trung Hiếu (Add Recipe)
 */
public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    private List<String> ingredients;
    private OnIngredientChangeListener listener;

    public interface OnIngredientChangeListener {
        void onIngredientChanged();

        void onIngredientRemoved(int position);
    }

    public IngredientAdapter(OnIngredientChangeListener listener) {
        this.ingredients = new ArrayList<>();
        this.listener = listener;
        // Add one empty ingredient by default
        this.ingredients.add("");
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        holder.bind(ingredients.get(position), position);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public void addIngredient() {
        ingredients.add("");
        notifyItemInserted(ingredients.size() - 1);
    }

    public void removeIngredient(int position) {
        if (ingredients.size() > 1) { // Keep at least one ingredient
            ingredients.remove(position);
            notifyItemRemoved(position);
            if (listener != null) {
                listener.onIngredientRemoved(position);
            }
        }
    }

    public List<String> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder {
        private EditText ingredientEditText;
        private ImageButton removeButton;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientEditText = itemView.findViewById(R.id.ingredient_edit_text);
            removeButton = itemView.findViewById(R.id.remove_ingredient_button);
        }

        public void bind(String ingredient, int position) {
            ingredientEditText.setText(ingredient);

            // Show remove button only if there's more than one ingredient
            removeButton.setVisibility(ingredients.size() > 1 ? View.VISIBLE : View.GONE);

            ingredientEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    ingredients.set(position, ingredientEditText.getText().toString().trim());
                    if (listener != null) {
                        listener.onIngredientChanged();
                    }
                }
            });

            removeButton.setOnClickListener(v -> removeIngredient(position));
        }
    }
}

