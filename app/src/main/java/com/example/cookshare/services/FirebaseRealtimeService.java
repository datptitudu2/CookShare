package com.example.cookshare.services;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cookshare.models.Recipe;
import com.example.cookshare.models.VietnameseFood;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseRealtimeService {
    private static final String TAG = "FirebaseRealtimeService";
    private static final String DATABASE_URL = "https://cookshare-88d53-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String FOODS_NODE = "foods";

    private FirebaseDatabase database;
    private DatabaseReference foodsRef;

    private MutableLiveData<List<Recipe>> recipesLiveData;
    private MutableLiveData<String> errorLiveData;
    private MutableLiveData<Boolean> loadingLiveData;

    public FirebaseRealtimeService() {
        database = FirebaseDatabase.getInstance(DATABASE_URL);
        foodsRef = database.getReference(FOODS_NODE);

        recipesLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>();
    }

    public LiveData<List<Recipe>> getRecipesLiveData() {
        return recipesLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    // Load all Vietnamese foods from Firebase Realtime Database
    public void loadAllVietnameseFoods() {
        loadingLiveData.setValue(true);

        foodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loadingLiveData.setValue(false);

                List<Recipe> recipes = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    VietnameseFood vietnameseFood = snapshot.getValue(VietnameseFood.class);
                    if (vietnameseFood != null) {
                        Recipe recipe = vietnameseFood.toRecipe();
                        recipes.add(recipe);
                    }
                }

                recipesLiveData.setValue(recipes);
                Log.d(TAG, "Loaded " + recipes.size() + " Vietnamese foods from Realtime Database");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Không thể tải món ăn Việt Nam: " + databaseError.getMessage());
                Log.e(TAG, "Error loading Vietnamese foods", databaseError.toException());
            }
        });
    }

    // Load Vietnamese foods by category
    public void loadVietnameseFoodsByCategory(String category) {
        loadingLiveData.setValue(true);

        foodsRef.orderByChild("category").equalTo(category)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        loadingLiveData.setValue(false);

                        List<Recipe> recipes = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            VietnameseFood vietnameseFood = snapshot.getValue(VietnameseFood.class);
                            if (vietnameseFood != null) {
                                Recipe recipe = vietnameseFood.toRecipe();
                                recipes.add(recipe);
                            }
                        }

                        recipesLiveData.setValue(recipes);
                        Log.d(TAG, "Loaded " + recipes.size() + " Vietnamese foods in category: " + category);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Không thể tải món ăn theo danh mục: " + databaseError.getMessage());
                        Log.e(TAG, "Error loading Vietnamese foods by category", databaseError.toException());
                    }
                });
    }

    // Load Vietnamese foods by region
    public void loadVietnameseFoodsByRegion(String region) {
        loadingLiveData.setValue(true);

        foodsRef.orderByChild("region").equalTo(region)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        loadingLiveData.setValue(false);

                        List<Recipe> recipes = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            VietnameseFood vietnameseFood = snapshot.getValue(VietnameseFood.class);
                            if (vietnameseFood != null) {
                                Recipe recipe = vietnameseFood.toRecipe();
                                recipes.add(recipe);
                            }
                        }

                        recipesLiveData.setValue(recipes);
                        Log.d(TAG, "Loaded " + recipes.size() + " Vietnamese foods in region: " + region);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Không thể tải món ăn theo vùng: " + databaseError.getMessage());
                        Log.e(TAG, "Error loading Vietnamese foods by region", databaseError.toException());
                    }
                });
    }

    // Search Vietnamese foods
    public void searchVietnameseFoods(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadAllVietnameseFoods();
            return;
        }

        loadingLiveData.setValue(true);

        foodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loadingLiveData.setValue(false);

                List<Recipe> recipes = new ArrayList<>();
                String searchQuery = query.toLowerCase().trim();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    VietnameseFood vietnameseFood = snapshot.getValue(VietnameseFood.class);
                    if (vietnameseFood != null) {
                        // Search in name and description
                        if (vietnameseFood.getName().toLowerCase().contains(searchQuery) ||
                                vietnameseFood.getDescription().toLowerCase().contains(searchQuery) ||
                                vietnameseFood.getCategory().toLowerCase().contains(searchQuery)) {

                            Recipe recipe = vietnameseFood.toRecipe();
                            recipes.add(recipe);
                        }
                    }
                }

                recipesLiveData.setValue(recipes);
                Log.d(TAG, "Found " + recipes.size() + " Vietnamese foods for query: " + query);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Không thể tìm kiếm món ăn: " + databaseError.getMessage());
                Log.e(TAG, "Error searching Vietnamese foods", databaseError.toException());
            }
        });
    }

    // Upload Vietnamese foods to Firebase Realtime Database
    public void uploadVietnameseFoods() {
        // This method should only be used for initial data upload
        // After that, all data should come from Firebase API
        Log.w(TAG, "uploadVietnameseFoods() - This should only be used for initial setup");
        Log.w(TAG, "After initial upload, all data should come from Firebase API");

        // For now, just show a message that data should be uploaded manually
        // or through Firebase Console
        errorLiveData.setValue("Vui lòng upload data thông qua Firebase Console hoặc DataUploadActivity");
    }

    // Clear error
    public void clearError() {
        errorLiveData.setValue(null);
    }

    // Search recipes with callback
    public void searchRecipes(String query, SearchCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onError("Search query cannot be empty");
            return;
        }

        foodsRef.orderByChild("name").startAt(query.toLowerCase()).endAt(query.toLowerCase() + "\uf8ff")
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    List<Recipe> recipes = new java.util.ArrayList<>();
                    for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        VietnameseFood food = snapshot.getValue(VietnameseFood.class);
                        if (food != null) {
                            Recipe recipe = food.toRecipe();
                            recipes.add(recipe);
                        }
                    }
                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to search recipes", e);
                    callback.onError(e.getMessage());
                });
    }

    // Callback interface for search
    public interface SearchCallback {
        void onSuccess(List<Recipe> recipes);

        void onError(String error);
    }
}
