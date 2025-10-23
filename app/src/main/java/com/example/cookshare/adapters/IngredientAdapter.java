package com.example.cookshare.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookshare.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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
        private TextInputEditText ingredientEditText;
        private MaterialButton removeButton;
        private TextWatcher textWatcher;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientEditText = itemView.findViewById(R.id.ingredient_edit_text);
            removeButton = itemView.findViewById(R.id.remove_ingredient_button);
        }

        public void bind(String ingredient, int position) {
            // Remove previous text watcher to avoid duplicate updates
            if (textWatcher != null) {
                ingredientEditText.removeTextChangedListener(textWatcher);
            }

            ingredientEditText.setText(ingredient);

            // Show remove button only if there's more than one ingredient
            removeButton.setVisibility(ingredients.size() > 1 ? View.VISIBLE : View.GONE);

            // Add text watcher for real-time updates
            textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (position < ingredients.size()) {
                        ingredients.set(position, s.toString().trim());
                        if (listener != null) {
                            listener.onIngredientChanged();
                        }
                    }
                }
            };
            ingredientEditText.addTextChangedListener(textWatcher);

            removeButton.setOnClickListener(v -> removeIngredient(position));
        }
    }
}

