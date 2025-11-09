package com.example.cookshare.services;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cookshare.models.Recipe;
import com.example.cookshare.models.VietnameseFood;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
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
                try {
                    loadingLiveData.setValue(false);

                    if (dataSnapshot == null || !dataSnapshot.exists()) {
                        Log.w(TAG, "DataSnapshot is null or does not exist");
                        recipesLiveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<Recipe> recipes = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            String firebaseKey = snapshot.getKey(); // Lấy key từ Firebase (ví dụ: "0", "1", hoặc
                                                                    // timestamp)
                            if (firebaseKey == null || firebaseKey.isEmpty()) {
                                Log.w(TAG, "Skipping recipe with null or empty key");
                                continue;
                            }

                            VietnameseFood vietnameseFood = snapshot.getValue(VietnameseFood.class);
                            if (vietnameseFood != null) {
                                Recipe recipe = vietnameseFood.toRecipe();
                                // QUAN TRỌNG: Override ID từ snapshot key (đây là ID thực trong foods node)
                                // Đây là ID sẽ được dùng để lưu vào favorites
                                recipe.setId(firebaseKey);

                                // QUAN TRỌNG: Đọc authorId và authorName từ snapshot nếu có
                                if (snapshot.hasChild("authorId")) {
                                    String authorId = snapshot.child("authorId").getValue(String.class);
                                    if (authorId != null && !authorId.isEmpty()
                                            && !authorId.equals("vietnamese_food")) {
                                        recipe.setAuthorId(authorId);
                                        Log.d(TAG, " Loaded recipe with authorId: " + authorId);

                                        // Đọc authorName từ snapshot nếu có
                                        if (snapshot.hasChild("authorName")) {
                                            String authorName = snapshot.child("authorName").getValue(String.class);
                                            if (authorName != null && !authorName.isEmpty()) {
                                                recipe.setAuthorName(authorName);
                                                Log.d(TAG, " Loaded recipe with authorName: " + authorName);
                                            }
                                        }
                                    }
                                }

                                Log.d(TAG, " Loaded recipe: FirebaseKey=" + firebaseKey + ", RecipeId="
                                        + recipe.getId() + ", Title=" + recipe.getTitle() + ", AuthorId="
                                        + recipe.getAuthorId());
                                // Đọc TẤT CẢ các giá trị từ Firebase snapshot (nếu có) để override hardcoded
                                // values

                                // Rating và RatingCount
                                try {
                                    if (snapshot.hasChild("rating")) {
                                        Object ratingObj = snapshot.child("rating").getValue();
                                        if (ratingObj != null) {
                                            double rating = ratingObj instanceof Double ? (Double) ratingObj
                                                    : ratingObj instanceof Long ? ((Long) ratingObj).doubleValue()
                                                            : ((Number) ratingObj).doubleValue();
                                            recipe.setRating(rating);
                                        }
                                    }
                                    if (snapshot.hasChild("ratingCount")) {
                                        Object ratingCountObj = snapshot.child("ratingCount").getValue();
                                        if (ratingCountObj != null) {
                                            int ratingCount = ratingCountObj instanceof Long
                                                    ? ((Long) ratingCountObj).intValue()
                                                    : ((Integer) ratingCountObj).intValue();
                                            recipe.setRatingCount(ratingCount);
                                        }
                                    }

                                    // ViewCount và LikeCount
                                    if (snapshot.hasChild("viewCount")) {
                                        Object viewCountObj = snapshot.child("viewCount").getValue();
                                        if (viewCountObj != null) {
                                            int viewCount = viewCountObj instanceof Long
                                                    ? ((Long) viewCountObj).intValue()
                                                    : ((Integer) viewCountObj).intValue();
                                            recipe.setViewCount(viewCount);
                                        }
                                    }
                                    if (snapshot.hasChild("likeCount")) {
                                        Object likeCountObj = snapshot.child("likeCount").getValue();
                                        if (likeCountObj != null) {
                                            int likeCount = likeCountObj instanceof Long
                                                    ? ((Long) likeCountObj).intValue()
                                                    : ((Integer) likeCountObj).intValue();
                                            recipe.setLikeCount(likeCount);
                                        }
                                    }

                                    // Difficulty
                                    if (snapshot.hasChild("difficulty")) {
                                        Object difficultyObj = snapshot.child("difficulty").getValue();
                                        if (difficultyObj != null) {
                                            recipe.setDifficulty(difficultyObj.toString());
                                        }
                                    }

                                    // PrepTime và CookTime
                                    if (snapshot.hasChild("prepTime")) {
                                        Object prepTimeObj = snapshot.child("prepTime").getValue();
                                        if (prepTimeObj != null) {
                                            int prepTime = prepTimeObj instanceof Long
                                                    ? ((Long) prepTimeObj).intValue()
                                                    : ((Integer) prepTimeObj).intValue();
                                            recipe.setPrepTime(prepTime);
                                        }
                                    }
                                    if (snapshot.hasChild("cookTime")) {
                                        Object cookTimeObj = snapshot.child("cookTime").getValue();
                                        if (cookTimeObj != null) {
                                            int cookTime = cookTimeObj instanceof Long
                                                    ? ((Long) cookTimeObj).intValue()
                                                    : ((Integer) cookTimeObj).intValue();
                                            recipe.setCookTime(cookTime);
                                        }
                                    }

                                    // Servings
                                    if (snapshot.hasChild("servings")) {
                                        Object servingsObj = snapshot.child("servings").getValue();
                                        if (servingsObj != null) {
                                            int servings = servingsObj instanceof Long
                                                    ? ((Long) servingsObj).intValue()
                                                    : ((Integer) servingsObj).intValue();
                                            recipe.setServings(servings);
                                        }
                                    }

                                    // Ingredients
                                    if (snapshot.hasChild("ingredients")) {
                                        Object ingredientsObj = snapshot.child("ingredients").getValue();
                                        if (ingredientsObj != null) {
                                            @SuppressWarnings("unchecked")
                                            List<String> ingredients = (List<String>) ingredientsObj;
                                            recipe.setIngredients(ingredients);
                                        }
                                    }

                                    // Instructions
                                    if (snapshot.hasChild("instructions")) {
                                        Object instructionsObj = snapshot.child("instructions").getValue();
                                        if (instructionsObj != null) {
                                            @SuppressWarnings("unchecked")
                                            List<String> instructions = (List<String>) instructionsObj;
                                            recipe.setInstructions(instructions);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing recipe fields for " + snapshot.getKey(), e);
                                    // Continue with default values
                                }

                                recipes.add(recipe);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing recipe: " + snapshot.getKey(), e);
                            // Skip this recipe and continue
                        }
                    }

                    recipesLiveData.setValue(recipes);
                    Log.d(TAG, "Loaded " + recipes.size() + " Vietnamese foods from Realtime Database");
                } catch (Exception e) {
                    Log.e(TAG, "Error in onDataChange", e);
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue("Lỗi khi tải dữ liệu: " + e.getMessage());
                    recipesLiveData.setValue(new ArrayList<>());
                }
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
                                recipe.setId(snapshot.getKey());

                                // Đọc rating và ratingCount từ Firebase snapshot (nếu có)
                                try {
                                    if (snapshot.hasChild("rating")) {
                                        Object ratingObj = snapshot.child("rating").getValue();
                                        if (ratingObj != null) {
                                            double rating = ratingObj instanceof Double ? (Double) ratingObj
                                                    : ratingObj instanceof Long ? ((Long) ratingObj).doubleValue()
                                                            : ((Number) ratingObj).doubleValue();
                                            recipe.setRating(rating);
                                        }
                                    }
                                    if (snapshot.hasChild("ratingCount")) {
                                        Object ratingCountObj = snapshot.child("ratingCount").getValue();
                                        if (ratingCountObj != null) {
                                            int ratingCount = ratingCountObj instanceof Long
                                                    ? ((Long) ratingCountObj).intValue()
                                                    : ((Integer) ratingCountObj).intValue();
                                            recipe.setRatingCount(ratingCount);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error reading rating/ratingCount for recipe " + snapshot.getKey(), e);
                                }

                                // Đọc viewCount và likeCount từ Firebase snapshot (nếu có)
                                if (snapshot.hasChild("viewCount")) {
                                    Object viewCountObj = snapshot.child("viewCount").getValue();
                                    if (viewCountObj != null) {
                                        int viewCount = viewCountObj instanceof Long ? ((Long) viewCountObj).intValue()
                                                : ((Integer) viewCountObj).intValue();
                                        recipe.setViewCount(viewCount);
                                    }
                                }
                                if (snapshot.hasChild("likeCount")) {
                                    Object likeCountObj = snapshot.child("likeCount").getValue();
                                    if (likeCountObj != null) {
                                        int likeCount = likeCountObj instanceof Long ? ((Long) likeCountObj).intValue()
                                                : ((Integer) likeCountObj).intValue();
                                        recipe.setLikeCount(likeCount);
                                    }
                                }

                                // Đọc ingredients và instructions từ Firebase
                                try {
                                    if (snapshot.hasChild("ingredients")) {
                                        Object ingredientsObj = snapshot.child("ingredients").getValue();
                                        if (ingredientsObj != null) {
                                            @SuppressWarnings("unchecked")
                                            List<String> ingredients = (List<String>) ingredientsObj;
                                            recipe.setIngredients(ingredients);
                                        }
                                    }
                                    if (snapshot.hasChild("instructions")) {
                                        Object instructionsObj = snapshot.child("instructions").getValue();
                                        if (instructionsObj != null) {
                                            @SuppressWarnings("unchecked")
                                            List<String> instructions = (List<String>) instructionsObj;
                                            recipe.setInstructions(instructions);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error reading ingredients/instructions", e);
                                }

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
                                recipe.setId(snapshot.getKey());

                                // Đọc rating và ratingCount từ Firebase snapshot (nếu có)
                                try {
                                    if (snapshot.hasChild("rating")) {
                                        Object ratingObj = snapshot.child("rating").getValue();
                                        if (ratingObj != null) {
                                            double rating = ratingObj instanceof Double ? (Double) ratingObj
                                                    : ratingObj instanceof Long ? ((Long) ratingObj).doubleValue()
                                                            : ((Number) ratingObj).doubleValue();
                                            recipe.setRating(rating);
                                        }
                                    }
                                    if (snapshot.hasChild("ratingCount")) {
                                        Object ratingCountObj = snapshot.child("ratingCount").getValue();
                                        if (ratingCountObj != null) {
                                            int ratingCount = ratingCountObj instanceof Long
                                                    ? ((Long) ratingCountObj).intValue()
                                                    : ((Integer) ratingCountObj).intValue();
                                            recipe.setRatingCount(ratingCount);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error reading rating/ratingCount for recipe " + snapshot.getKey(), e);
                                }

                                // Đọc viewCount và likeCount từ Firebase snapshot (nếu có)
                                if (snapshot.hasChild("viewCount")) {
                                    Object viewCountObj = snapshot.child("viewCount").getValue();
                                    if (viewCountObj != null) {
                                        int viewCount = viewCountObj instanceof Long ? ((Long) viewCountObj).intValue()
                                                : ((Integer) viewCountObj).intValue();
                                        recipe.setViewCount(viewCount);
                                    }
                                }
                                if (snapshot.hasChild("likeCount")) {
                                    Object likeCountObj = snapshot.child("likeCount").getValue();
                                    if (likeCountObj != null) {
                                        int likeCount = likeCountObj instanceof Long ? ((Long) likeCountObj).intValue()
                                                : ((Integer) likeCountObj).intValue();
                                        recipe.setLikeCount(likeCount);
                                    }
                                }

                                // Đọc ingredients và instructions từ Firebase
                                try {
                                    if (snapshot.hasChild("ingredients")) {
                                        Object ingredientsObj = snapshot.child("ingredients").getValue();
                                        if (ingredientsObj != null) {
                                            @SuppressWarnings("unchecked")
                                            List<String> ingredients = (List<String>) ingredientsObj;
                                            recipe.setIngredients(ingredients);
                                        }
                                    }
                                    if (snapshot.hasChild("instructions")) {
                                        Object instructionsObj = snapshot.child("instructions").getValue();
                                        if (instructionsObj != null) {
                                            @SuppressWarnings("unchecked")
                                            List<String> instructions = (List<String>) instructionsObj;
                                            recipe.setInstructions(instructions);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error reading ingredients/instructions", e);
                                }

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
                        // Search - prioritize name and category, only search description if
                        // name/category don't match
                        String name = vietnameseFood.getName();
                        String description = vietnameseFood.getDescription();
                        String category = vietnameseFood.getCategory();

                        boolean matches = false;
                        boolean nameMatch = false;
                        boolean categoryMatch = false;

                        // Priority 1: Check name first (most important)
                        // Match nếu tên có chứa từ khóa
                        if (name != null) {
                            String lowerName = name.toLowerCase();
                            if (lowerName.contains(searchQuery)) {
                                matches = true;
                                nameMatch = true;
                            }
                        }

                        // Priority 2: Check category
                        if (category != null) {
                            String lowerCategory = category.toLowerCase();
                            if (lowerCategory.contains(searchQuery)) {
                                matches = true;
                                categoryMatch = true;
                            }
                        }

                        // Priority 3: BỎ QUA description để tránh match sai
                        // Không search trong description vì:
                        // - "bánh" trong "bánh tráng" không phải là món bánh
                        // - "bánh" trong "cuốn bánh tráng" không phải là món bánh
                        // Chỉ hiển thị món có "bánh" trong TÊN hoặc CATEGORY

                        if (matches) {

                            Recipe recipe = vietnameseFood.toRecipe();
                            recipe.setId(snapshot.getKey());

                            // Đọc authorId và authorName từ snapshot nếu có
                            if (snapshot.hasChild("authorId")) {
                                String authorId = snapshot.child("authorId").getValue(String.class);
                                if (authorId != null && !authorId.isEmpty() && !authorId.equals("vietnamese_food")) {
                                    recipe.setAuthorId(authorId);

                                    // Đọc authorName từ snapshot nếu có
                                    if (snapshot.hasChild("authorName")) {
                                        String authorName = snapshot.child("authorName").getValue(String.class);
                                        if (authorName != null && !authorName.isEmpty()) {
                                            recipe.setAuthorName(authorName);
                                        }
                                    }
                                }
                            }

                            // Đọc rating và ratingCount từ Firebase snapshot (nếu có) - QUAN TRỌNG!
                            try {
                                if (snapshot.hasChild("rating")) {
                                    Object ratingObj = snapshot.child("rating").getValue();
                                    if (ratingObj != null) {
                                        double rating = ratingObj instanceof Double ? (Double) ratingObj
                                                : ratingObj instanceof Long ? ((Long) ratingObj).doubleValue()
                                                        : ((Number) ratingObj).doubleValue();
                                        recipe.setRating(rating);
                                    }
                                }
                                if (snapshot.hasChild("ratingCount")) {
                                    Object ratingCountObj = snapshot.child("ratingCount").getValue();
                                    if (ratingCountObj != null) {
                                        int ratingCount = ratingCountObj instanceof Long
                                                ? ((Long) ratingCountObj).intValue()
                                                : ((Integer) ratingCountObj).intValue();
                                        recipe.setRatingCount(ratingCount);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading rating/ratingCount for recipe " + snapshot.getKey(), e);
                            }

                            // Đọc viewCount và likeCount từ Firebase snapshot (nếu có)
                            if (snapshot.hasChild("viewCount")) {
                                Object viewCountObj = snapshot.child("viewCount").getValue();
                                if (viewCountObj != null) {
                                    int viewCount = viewCountObj instanceof Long ? ((Long) viewCountObj).intValue()
                                            : ((Integer) viewCountObj).intValue();
                                    recipe.setViewCount(viewCount);
                                }
                            }
                            if (snapshot.hasChild("likeCount")) {
                                Object likeCountObj = snapshot.child("likeCount").getValue();
                                if (likeCountObj != null) {
                                    int likeCount = likeCountObj instanceof Long ? ((Long) likeCountObj).intValue()
                                            : ((Integer) likeCountObj).intValue();
                                    recipe.setLikeCount(likeCount);
                                }
                            }

                            // Đọc ingredients và instructions từ Firebase
                            try {
                                if (snapshot.hasChild("ingredients")) {
                                    Object ingredientsObj = snapshot.child("ingredients").getValue();
                                    if (ingredientsObj != null) {
                                        @SuppressWarnings("unchecked")
                                        List<String> ingredients = (List<String>) ingredientsObj;
                                        recipe.setIngredients(ingredients);
                                    }
                                }
                                if (snapshot.hasChild("instructions")) {
                                    Object instructionsObj = snapshot.child("instructions").getValue();
                                    if (instructionsObj != null) {
                                        @SuppressWarnings("unchecked")
                                        List<String> instructions = (List<String>) instructionsObj;
                                        recipe.setInstructions(instructions);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading ingredients/instructions", e);
                            }

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

    // Load popular recipes (sorted by viewCount)
    public void loadPopularRecipes() {
        loadingLiveData.setValue(true);
        foodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loadingLiveData.setValue(false);
                List<Recipe> recipes = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        VietnameseFood vietnameseFood = snapshot.getValue(VietnameseFood.class);
                        if (vietnameseFood != null) {
                            Recipe recipe = vietnameseFood.toRecipe();
                            // QUAN TRỌNG: Set ID từ snapshot key
                            recipe.setId(snapshot.getKey());

                            // Read viewCount và likeCount từ Firebase
                            if (snapshot.hasChild("viewCount")) {
                                Object viewCountObj = snapshot.child("viewCount").getValue();
                                if (viewCountObj != null) {
                                    int viewCount = viewCountObj instanceof Long
                                            ? ((Long) viewCountObj).intValue()
                                            : ((Integer) viewCountObj).intValue();
                                    recipe.setViewCount(viewCount);
                                }
                            }
                            if (snapshot.hasChild("likeCount")) {
                                Object likeCountObj = snapshot.child("likeCount").getValue();
                                if (likeCountObj != null) {
                                    int likeCount = likeCountObj instanceof Long
                                            ? ((Long) likeCountObj).intValue()
                                            : ((Integer) likeCountObj).intValue();
                                    recipe.setLikeCount(likeCount);
                                }
                            }

                            // Đọc rating và ratingCount từ Firebase
                            try {
                                if (snapshot.hasChild("rating")) {
                                    Object ratingObj = snapshot.child("rating").getValue();
                                    if (ratingObj != null) {
                                        double rating = ratingObj instanceof Double ? (Double) ratingObj
                                                : ratingObj instanceof Long ? ((Long) ratingObj).doubleValue()
                                                        : ((Number) ratingObj).doubleValue();
                                        recipe.setRating(rating);
                                    }
                                }
                                if (snapshot.hasChild("ratingCount")) {
                                    Object ratingCountObj = snapshot.child("ratingCount").getValue();
                                    if (ratingCountObj != null) {
                                        int ratingCount = ratingCountObj instanceof Long
                                                ? ((Long) ratingCountObj).intValue()
                                                : ((Integer) ratingCountObj).intValue();
                                        recipe.setRatingCount(ratingCount);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading rating/ratingCount", e);
                            }

                            // Đọc ingredients và instructions từ Firebase
                            try {
                                if (snapshot.hasChild("ingredients")) {
                                    Object ingredientsObj = snapshot.child("ingredients").getValue();
                                    if (ingredientsObj != null) {
                                        @SuppressWarnings("unchecked")
                                        List<String> ingredients = (List<String>) ingredientsObj;
                                        recipe.setIngredients(ingredients);
                                    }
                                }
                                if (snapshot.hasChild("instructions")) {
                                    Object instructionsObj = snapshot.child("instructions").getValue();
                                    if (instructionsObj != null) {
                                        @SuppressWarnings("unchecked")
                                        List<String> instructions = (List<String>) instructionsObj;
                                        recipe.setInstructions(instructions);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading ingredients/instructions", e);
                            }

                            recipes.add(recipe);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing recipe: " + snapshot.getKey(), e);
                        // Skip this recipe and continue
                    }
                }
                // Sort by viewCount descending
                recipes.sort((r1, r2) -> Integer.compare(r2.getViewCount(), r1.getViewCount()));
                recipesLiveData.setValue(recipes);
                Log.d(TAG, "Loaded " + recipes.size() + " popular recipes");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Không thể tải món phổ biến: " + databaseError.getMessage());
                Log.e(TAG, "Error loading popular recipes", databaseError.toException());
            }
        });
    }

    // Load favorite recipes (rating >= 4.0 stars, sorted by rating and likeCount)
    public void loadFavoriteRecipes() {
        loadingLiveData.setValue(true);

        // Lấy user hiện tại
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            loadingLiveData.setValue(false);
            recipesLiveData.setValue(new ArrayList<>());
            Log.w(TAG, "User not logged in, cannot load favorites");
            return;
        }

        // Lấy danh sách recipe IDs từ favorites node
        try {
            DatabaseReference favoritesRef = database.getReference("favorites")
                    .child(currentUser.getUid()).child("recipeIds");

            favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot favoritesSnapshot) {
                    if (!favoritesSnapshot.exists() || favoritesSnapshot.getChildrenCount() == 0) {
                        loadingLiveData.setValue(false);
                        recipesLiveData.setValue(new ArrayList<>());
                        Log.d(TAG, "No favorite recipes found");
                        return;
                    }

                    // Lấy danh sách recipe IDs (chỉ lấy những recipe có value = true)
                    List<String> recipeIds = new ArrayList<>();
                    for (DataSnapshot recipeIdSnapshot : favoritesSnapshot.getChildren()) {
                        String recipeId = recipeIdSnapshot.getKey();
                        Object value = recipeIdSnapshot.getValue();
                        // Chỉ add nếu recipeId không null và value không phải false
                        boolean isValid = recipeId != null && value != null && !value.equals(false);
                        if (isValid) {
                            recipeIds.add(recipeId);
                            Log.d(TAG, "Found favorite recipeId: " + recipeId);
                        }
                    }

                    if (recipeIds.isEmpty()) {
                        loadingLiveData.setValue(false);
                        recipesLiveData.setValue(new ArrayList<>());
                        Log.d(TAG, "No valid favorite recipe IDs found");
                        return;
                    }

                    Log.d(TAG, "Loading " + recipeIds.size() + " favorite recipes from foods node");

                    // Load chi tiết từng recipe từ foods node
                    final List<Recipe> favoriteRecipes = Collections.synchronizedList(new ArrayList<>());
                    final int[] loadedCount = { 0 };
                    final int totalRecipes = recipeIds.size();

                    for (String recipeId : recipeIds) {
                        final String finalRecipeId = recipeId; // Capture for use in inner class
                        foodsRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                synchronized (favoriteRecipes) {
                                    try {
                                        if (snapshot.exists()) {
                                            VietnameseFood vietnameseFood = snapshot.getValue(VietnameseFood.class);
                                            if (vietnameseFood != null) {
                                                Recipe recipe = vietnameseFood.toRecipe();
                                                recipe.setId(snapshot.getKey());

                                                // Read dynamic values từ Firebase
                                                if (snapshot.hasChild("rating")) {
                                                    Object ratingObj = snapshot.child("rating").getValue();
                                                    if (ratingObj != null) {
                                                        double rating = ratingObj instanceof Double ? (Double) ratingObj
                                                                : ratingObj instanceof Long
                                                                        ? ((Long) ratingObj).doubleValue()
                                                                        : ((Number) ratingObj).doubleValue();
                                                        recipe.setRating(rating);
                                                    }
                                                }
                                                if (snapshot.hasChild("likeCount")) {
                                                    Object likeCountObj = snapshot.child("likeCount").getValue();
                                                    if (likeCountObj != null) {
                                                        int likeCount = likeCountObj instanceof Long
                                                                ? ((Long) likeCountObj).intValue()
                                                                : ((Integer) likeCountObj).intValue();
                                                        recipe.setLikeCount(likeCount);
                                                    }
                                                }
                                                if (snapshot.hasChild("viewCount")) {
                                                    Object viewCountObj = snapshot.child("viewCount").getValue();
                                                    if (viewCountObj != null) {
                                                        int viewCount = viewCountObj instanceof Long
                                                                ? ((Long) viewCountObj).intValue()
                                                                : ((Integer) viewCountObj).intValue();
                                                        recipe.setViewCount(viewCount);
                                                    }
                                                }
                                                if (snapshot.hasChild("cookTime")) {
                                                    Object cookTimeObj = snapshot.child("cookTime").getValue();
                                                    if (cookTimeObj != null) {
                                                        int cookTime = cookTimeObj instanceof Long
                                                                ? ((Long) cookTimeObj).intValue()
                                                                : ((Integer) cookTimeObj).intValue();
                                                        recipe.setCookTime(cookTime);
                                                    }
                                                }
                                                if (snapshot.hasChild("prepTime")) {
                                                    Object prepTimeObj = snapshot.child("prepTime").getValue();
                                                    if (prepTimeObj != null) {
                                                        int prepTime = prepTimeObj instanceof Long
                                                                ? ((Long) prepTimeObj).intValue()
                                                                : ((Integer) prepTimeObj).intValue();
                                                        recipe.setPrepTime(prepTime);
                                                    }
                                                }
                                                if (snapshot.hasChild("servings")) {
                                                    Object servingsObj = snapshot.child("servings").getValue();
                                                    if (servingsObj != null) {
                                                        int servings = servingsObj instanceof Long
                                                                ? ((Long) servingsObj).intValue()
                                                                : ((Integer) servingsObj).intValue();
                                                        recipe.setServings(servings);
                                                    }
                                                }
                                                if (snapshot.hasChild("difficulty")) {
                                                    Object difficultyObj = snapshot.child("difficulty").getValue();
                                                    if (difficultyObj != null) {
                                                        recipe.setDifficulty(difficultyObj.toString());
                                                    }
                                                }

                                                favoriteRecipes.add(recipe);
                                                Log.d(TAG, "Added favorite recipe: " + recipe.getTitle() + " (ID: "
                                                        + recipe.getId() + ")");
                                            }
                                        } else {
                                            Log.w(TAG, "Recipe not found in foods node: " + finalRecipeId);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing recipe: " + finalRecipeId, e);
                                    }

                                    loadedCount[0]++;
                                    Log.d(TAG, "Progress: " + loadedCount[0] + "/" + totalRecipes + " recipes loaded");

                                    if (loadedCount[0] >= totalRecipes) {
                                        loadingLiveData.setValue(false);
                                        recipesLiveData.setValue(new ArrayList<>(favoriteRecipes));
                                        Log.d(TAG, "Loaded " + favoriteRecipes.size()
                                                + " favorite recipes from favorites node");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                synchronized (favoriteRecipes) {
                                    loadedCount[0]++;
                                    Log.e(TAG, "Error loading recipe " + finalRecipeId, databaseError.toException());

                                    if (loadedCount[0] >= totalRecipes) {
                                        loadingLiveData.setValue(false);
                                        recipesLiveData.setValue(new ArrayList<>(favoriteRecipes));
                                    }
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue("Không thể tải món yêu thích: " + databaseError.getMessage());
                    Log.e(TAG, "Error loading favorites", databaseError.toException());
                }
            });
        } catch (Exception e) {
            loadingLiveData.setValue(false);
            errorLiveData.setValue("Lỗi khi tải món yêu thích: " + e.getMessage());
            Log.e(TAG, "Exception in loadFavoriteRecipes", e);
            recipesLiveData.setValue(new ArrayList<>());
        }
    }

    // Load quick cook recipes (cookTime < 30 minutes)
    public void loadQuickCookRecipes() {
        loadingLiveData.setValue(true);
        foodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loadingLiveData.setValue(false);
                List<Recipe> recipes = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        VietnameseFood vietnameseFood = snapshot.getValue(VietnameseFood.class);
                        if (vietnameseFood != null) {
                            Recipe recipe = vietnameseFood.toRecipe();
                            // QUAN TRỌNG: Set ID từ snapshot key
                            recipe.setId(snapshot.getKey());

                            // Read cookTime từ Firebase
                            if (snapshot.hasChild("cookTime")) {
                                Object cookTimeObj = snapshot.child("cookTime").getValue();
                                if (cookTimeObj != null) {
                                    int cookTime = cookTimeObj instanceof Long
                                            ? ((Long) cookTimeObj).intValue()
                                            : ((Integer) cookTimeObj).intValue();
                                    recipe.setCookTime(cookTime);
                                }
                            }
                            // Read prepTime nếu có
                            if (snapshot.hasChild("prepTime")) {
                                Object prepTimeObj = snapshot.child("prepTime").getValue();
                                if (prepTimeObj != null) {
                                    int prepTime = prepTimeObj instanceof Long
                                            ? ((Long) prepTimeObj).intValue()
                                            : ((Integer) prepTimeObj).intValue();
                                    recipe.setPrepTime(prepTime);
                                }
                            }

                            // Đọc rating và ratingCount từ Firebase
                            try {
                                if (snapshot.hasChild("rating")) {
                                    Object ratingObj = snapshot.child("rating").getValue();
                                    if (ratingObj != null) {
                                        double rating = ratingObj instanceof Double ? (Double) ratingObj
                                                : ratingObj instanceof Long ? ((Long) ratingObj).doubleValue()
                                                        : ((Number) ratingObj).doubleValue();
                                        recipe.setRating(rating);
                                    }
                                }
                                if (snapshot.hasChild("ratingCount")) {
                                    Object ratingCountObj = snapshot.child("ratingCount").getValue();
                                    if (ratingCountObj != null) {
                                        int ratingCount = ratingCountObj instanceof Long
                                                ? ((Long) ratingCountObj).intValue()
                                                : ((Integer) ratingCountObj).intValue();
                                        recipe.setRatingCount(ratingCount);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading rating/ratingCount", e);
                            }

                            // Đọc ingredients và instructions từ Firebase
                            try {
                                if (snapshot.hasChild("ingredients")) {
                                    Object ingredientsObj = snapshot.child("ingredients").getValue();
                                    if (ingredientsObj != null) {
                                        @SuppressWarnings("unchecked")
                                        List<String> ingredients = (List<String>) ingredientsObj;
                                        recipe.setIngredients(ingredients);
                                    }
                                }
                                if (snapshot.hasChild("instructions")) {
                                    Object instructionsObj = snapshot.child("instructions").getValue();
                                    if (instructionsObj != null) {
                                        @SuppressWarnings("unchecked")
                                        List<String> instructions = (List<String>) instructionsObj;
                                        recipe.setInstructions(instructions);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading ingredients/instructions", e);
                            }

                            // Filter: tổng thời gian < 30 phút
                            int totalTime = recipe.getPrepTime() + recipe.getCookTime();
                            if (totalTime < 30) {
                                recipes.add(recipe);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing recipe: " + snapshot.getKey(), e);
                        // Skip this recipe and continue
                    }
                }
                // Sort by total time ascending
                recipes.sort((r1, r2) -> Integer.compare(
                        r1.getPrepTime() + r1.getCookTime(),
                        r2.getPrepTime() + r2.getCookTime()));
                recipesLiveData.setValue(recipes);
                Log.d(TAG, "Loaded " + recipes.size() + " quick cook recipes");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Không thể tải món nấu nhanh: " + databaseError.getMessage());
                Log.e(TAG, "Error loading quick cook recipes", databaseError.toException());
            }
        });
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
                            // Đọc viewCount và likeCount từ Firebase snapshot (nếu có)
                            if (snapshot.hasChild("viewCount")) {
                                Object viewCountObj = snapshot.child("viewCount").getValue();
                                if (viewCountObj != null) {
                                    int viewCount = viewCountObj instanceof Long ? ((Long) viewCountObj).intValue()
                                            : ((Integer) viewCountObj).intValue();
                                    recipe.setViewCount(viewCount);
                                }
                            }
                            if (snapshot.hasChild("likeCount")) {
                                Object likeCountObj = snapshot.child("likeCount").getValue();
                                if (likeCountObj != null) {
                                    int likeCount = likeCountObj instanceof Long ? ((Long) likeCountObj).intValue()
                                            : ((Integer) likeCountObj).intValue();
                                    recipe.setLikeCount(likeCount);
                                }
                            }
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
