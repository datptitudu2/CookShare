package com.example.cookshare.services;

import android.util.Log;

import com.example.cookshare.models.Recipe;
import com.example.cookshare.models.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import java.util.HashMap;
import java.util.Map;

// TODO: This service provides Firebase database operations for the team
// Responsibilities:
// - Manage Firebase Realtime Database operations
// - Handle CRUD operations for all data types
// - Provide database references for team members

public class FirebaseDatabaseService {
    private static final String TAG = "FirebaseDatabaseService";
    private static final String DATABASE_URL = "https://cookshare-88d53-default-rtdb.asia-southeast1.firebasedatabase.app/";

    // Database nodes
    private static final String FOODS_NODE = "foods";
    private static final String RECIPES_NODE = "recipes";
    private static final String USERS_NODE = "users";
    private static final String FAVORITES_NODE = "favorites";
    private static final String SEARCH_HISTORY_NODE = "searchHistory";

    private FirebaseDatabase database;
    private DatabaseReference foodsRef;
    private DatabaseReference recipesRef;
    private DatabaseReference usersRef;
    private DatabaseReference favoritesRef;
    private DatabaseReference searchHistoryRef;

    public FirebaseDatabaseService() {
        database = FirebaseDatabase.getInstance(DATABASE_URL);
        foodsRef = database.getReference(FOODS_NODE);
        recipesRef = database.getReference(RECIPES_NODE);
        usersRef = database.getReference(USERS_NODE);
        favoritesRef = database.getReference(FAVORITES_NODE);
        searchHistoryRef = database.getReference(SEARCH_HISTORY_NODE);
    }

    // Get database references for team members
    public DatabaseReference getFoodsRef() {
        return foodsRef;
    }

    public DatabaseReference getRecipesRef() {
        return recipesRef;
    }

    public DatabaseReference getUsersRef() {
        return usersRef;
    }

    public DatabaseReference getFavoritesRef() {
        return favoritesRef;
    }

    public DatabaseReference getSearchHistoryRef() {
        return searchHistoryRef;
    }

    // User operations
    public void createUserProfile(UserProfile userProfile) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            usersRef.child(user.getUid()).setValue(userProfile)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile created successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to create user profile", e));
        }
    }

    public void updateUserProfile(UserProfile userProfile) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            usersRef.child(user.getUid()).setValue(userProfile)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile updated successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update user profile", e));
        }
    }

    // Recipe operations
    public void createRecipe(Recipe recipe) {
        String recipeId = recipesRef.push().getKey();
        if (recipeId != null) {
            recipe.setId(recipeId);
            // Set default image URL if not provided
            if (recipe.getImageUrl() == null || recipe.getImageUrl().isEmpty()) {
                recipe.setImageUrl("https://via.placeholder.com/300x200?text=Recipe+Image");
            }
            recipesRef.child(recipeId).setValue(recipe)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Recipe created successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to create recipe", e));
        }
    }

    public void updateRecipe(String recipeId, Recipe recipe, RecipeCallback callback) {
        recipe.setUpdatedAt(new java.util.Date());
        recipesRef.child(recipeId).setValue(recipe)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Recipe updated successfully");
                    if (callback != null) {
                        callback.onSuccess(recipeId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update recipe", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    public void deleteRecipe(String recipeId) {
        recipesRef.child(recipeId).removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Recipe deleted successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete recipe", e));
    }

    // Favorites operations
    public void addToFavorites(String recipeId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            favoritesRef.child(user.getUid()).child("recipeIds").child(recipeId).setValue(true)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Added to favorites successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add to favorites", e));
        }
    }

    public void removeFromFavorites(String recipeId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            favoritesRef.child(user.getUid()).child("recipeIds").child(recipeId).removeValue()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Removed from favorites successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to remove from favorites", e));
        }
    }

    // Search history operations
    public void addToSearchHistory(String query) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            searchHistoryRef.child(user.getUid()).child("queries").child(timestamp).setValue(query)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Added to search history successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add to search history", e));
        }
    }

    public void clearSearchHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            searchHistoryRef.child(user.getUid()).child("queries").removeValue()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Search history cleared successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to clear search history", e));
        }
    }

    // Create recipe with callback
    public void createRecipe(Recipe recipe, RecipeCallback callback) {
        String recipeId = recipesRef.push().getKey();
        if (recipeId != null) {
            recipe.setId(recipeId);
            recipesRef.child(recipeId).setValue(recipe)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Recipe created successfully");
                        callback.onSuccess(recipeId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to create recipe", e);
                        callback.onError(e.getMessage());
                    });
        } else {
            callback.onError("Failed to generate recipe ID");
        }
    }

    // Get user profile with callback
    public void getUserProfile(String userId, UserProfileCallback callback) {
        usersRef.child(userId).get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        UserProfile profile = dataSnapshot.getValue(UserProfile.class);
                        callback.onSuccess(profile);
                    } else {
                        callback.onError("User profile not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get user profile", e);
                    callback.onError(e.getMessage());
                });
    }

    // Get user recipes with callback
    public void getUserRecipes(String userId, RecipeListCallback callback) {
        recipesRef.orderByChild("authorId").equalTo(userId).get()
                .addOnSuccessListener(dataSnapshot -> {
                    List<Recipe> recipes = new java.util.ArrayList<>();
                    for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Recipe recipe = snapshot.getValue(Recipe.class);
                        if (recipe != null) {
                            recipes.add(recipe);
                        }
                    }
                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get user recipes", e);
                    callback.onError(e.getMessage());
                });
    }

    // Update user profile with callback
    public void updateUserProfile(UserProfile profile, UserProfileCallback callback) {
        usersRef.child(profile.getId()).setValue(profile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile updated successfully");
                    callback.onSuccess(profile);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update user profile", e);
                    callback.onError(e.getMessage());
                });
    }

    // NOTE: Follow/Unfollow features chu kip lam
    // Co the them sau khi can thiet
    // Rules da san sang trong Firebase

    // Callback interfaces
    public interface RecipeCallback {
        void onSuccess(String recipeId);

        void onError(String error);
    }

    public interface UserProfileCallback {
        void onSuccess(UserProfile profile);

        void onError(String error);
    }

    public interface RecipeListCallback {
        void onSuccess(List<Recipe> recipes);

        void onError(String error);
    }
}