package com.example.cookshare.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cookshare.models.Recipe;
import com.example.cookshare.services.FirebaseRealtimeService;

import java.util.List;

public class RecipeViewModel extends AndroidViewModel {
    private static final String TAG = "RecipeViewModel";
    private FirebaseRealtimeService firebaseRealtimeService;

    // LiveData
    private LiveData<List<Recipe>> recipesLiveData;
    private LiveData<Recipe> recipeLiveData;
    private LiveData<String> errorLiveData;
    private LiveData<Boolean> loadingLiveData;

    public RecipeViewModel(@NonNull Application application) {
        super(application);
        firebaseRealtimeService = new FirebaseRealtimeService();

        // Initialize LiveData from FirebaseRealtimeService (primary source for reads)
        recipesLiveData = firebaseRealtimeService.getRecipesLiveData();
        errorLiveData = firebaseRealtimeService.getErrorLiveData();
        loadingLiveData = firebaseRealtimeService.getLoadingLiveData();

        // Initialize single recipe LiveData
        recipeLiveData = new MutableLiveData<>();
    }

    // LiveData getters
    public LiveData<List<Recipe>> getRecipesLiveData() {
        return recipesLiveData;
    }

    public LiveData<Recipe> getRecipeLiveData() {
        return recipeLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    // Recipe operations - Now using Firebase Realtime Database
    public void loadAllRecipes() {
        Log.d(TAG, "Loading all Vietnamese foods from Firebase Realtime Database");
        firebaseRealtimeService.loadAllVietnameseFoods();
    }

    public void loadRecipesByAuthor(String authorId) {
        Log.d(TAG, "Loading recipes by author: " + authorId);
        // For Vietnamese foods, we'll load all foods
        firebaseRealtimeService.loadAllVietnameseFoods();
    }

    public void loadRecipe(String recipeId) {
        Log.d(TAG, "Loading recipe: " + recipeId);
        // For now, load all recipes and filter client-side
        firebaseRealtimeService.loadAllVietnameseFoods();
    }

    public void createRecipe(Recipe recipe) {
        Log.d(TAG, "Creating recipe: " + recipe.getTitle());
        // TODO: Implement recipe creation in Firebase Realtime Database
    }

    public void updateRecipe(String recipeId, Recipe recipe) {
        Log.d(TAG, "Updating recipe: " + recipeId);
        // TODO: Implement recipe update in Firebase Realtime Database
    }

    public void deleteRecipe(String recipeId) {
        Log.d(TAG, "Deleting recipe: " + recipeId);
        // TODO: Implement recipe deletion in Firebase Realtime Database
    }

    public void searchRecipes(String query) {
        Log.d(TAG, "Searching Vietnamese foods with query: " + query);
        firebaseRealtimeService.searchVietnameseFoods(query);
    }

    public void getMealsByCategory(String category) {
        Log.d(TAG, "Getting Vietnamese foods by category: " + category);
        firebaseRealtimeService.loadVietnameseFoodsByCategory(category);
    }

    public void loadVietnameseMeals() {
        Log.d(TAG, "Loading Vietnamese meals from Firebase Realtime Database");
        firebaseRealtimeService.loadAllVietnameseFoods();
    }

    public void loadVietnameseFoodsByRegion(String region) {
        Log.d(TAG, "Loading Vietnamese foods by region: " + region);
        firebaseRealtimeService.loadVietnameseFoodsByRegion(region);
    }

    public void uploadVietnameseFoods() {
        Log.d(TAG, "Uploading Vietnamese foods to Firebase Realtime Database");
        firebaseRealtimeService.uploadVietnameseFoods();
    }

    public void incrementViewCount(String recipeId) {
        // TODO: Implement view count increment in Firebase Realtime Database
    }

    public void updateLikeCount(String recipeId, boolean isLiked) {
        // TODO: Implement like count update in Firebase Realtime Database
    }

    public void clearError() {
        firebaseRealtimeService.clearError();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "RecipeViewModel cleared");
    }
}