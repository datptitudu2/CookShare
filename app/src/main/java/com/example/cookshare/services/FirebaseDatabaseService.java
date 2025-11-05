package com.example.cookshare.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.cookshare.models.Recipe;
import com.example.cookshare.models.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
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
    private static final String COOKING_HISTORY_NODE = "cookingHistory";
    private static final String NOTIFICATIONS_NODE = "notifications";

    private FirebaseDatabase database;
    private DatabaseReference foodsRef;
    private DatabaseReference recipesRef;
    private DatabaseReference usersRef;
    private DatabaseReference favoritesRef;
    private DatabaseReference searchHistoryRef;
    private DatabaseReference cookingHistoryRef;
    private DatabaseReference notificationsRef;

    public FirebaseDatabaseService() {
        database = FirebaseDatabase.getInstance(DATABASE_URL);
        foodsRef = database.getReference(FOODS_NODE);
        recipesRef = database.getReference(RECIPES_NODE);
        usersRef = database.getReference(USERS_NODE);
        favoritesRef = database.getReference(FAVORITES_NODE);
        searchHistoryRef = database.getReference(SEARCH_HISTORY_NODE);
        cookingHistoryRef = database.getReference(COOKING_HISTORY_NODE);
        notificationsRef = database.getReference(NOTIFICATIONS_NODE);
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

    public DatabaseReference getCookingHistoryRef() {
        return cookingHistoryRef;
    }

    public DatabaseReference getNotificationsRef() {
        return notificationsRef;
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
        if (user == null) {
            Log.e(TAG, " Cannot add to favorites: User is null");
            return;
        }
        if (recipeId == null || recipeId.isEmpty()) {
            Log.e(TAG, " Cannot add to favorites: Recipe ID is null or empty");
            return;
        }

        String path = "favorites/" + user.getUid() + "/recipeIds/" + recipeId;
        Log.d(TAG, " Adding to favorites: " + path);
        favoritesRef.child(user.getUid()).child("recipeIds").child(recipeId).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, " Added to favorites successfully! Path: " + path);
                    // Tăng favoritesCount cho user
                    incrementUserFavoritesCount(user.getUid(), true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, " Failed to add to favorites! Path: " + path, e);
                });
    }

    public void removeFromFavorites(String recipeId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, " Cannot remove from favorites: User is null");
            return;
        }
        if (recipeId == null || recipeId.isEmpty()) {
            Log.e(TAG, " Cannot remove from favorites: Recipe ID is null or empty");
            return;
        }

        String path = "favorites/" + user.getUid() + "/recipeIds/" + recipeId;
        Log.d(TAG, " Removing from favorites: " + path);
        favoritesRef.child(user.getUid()).child("recipeIds").child(recipeId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, " Removed from favorites successfully! Path: " + path);
                    // Giảm favoritesCount cho user
                    incrementUserFavoritesCount(user.getUid(), false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, " Failed to remove from favorites! Path: " + path, e);
                });
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
                        Log.d(TAG, "Recipe created successfully in recipes node");

                        // Sau khi tạo recipe thành công, cũng thêm vào foods node
                        addRecipeToFoods(recipe, recipeId, callback);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to create recipe", e);
                        callback.onError(e.getMessage());
                    });
        } else {
            callback.onError("Failed to generate recipe ID");
        }
    }

    // Thêm recipe vào foods node để hiển thị trong HomeFragment
    private void addRecipeToFoods(Recipe recipe, String recipeId, RecipeCallback callback) {
        // Generate unique ID cho foods node dựa trên timestamp
        long timestamp = System.currentTimeMillis();
        String foodId = String.valueOf(timestamp);

        // Tạo ID integer từ timestamp (lấy 9 số cuối để tránh overflow)
        // Integer.MAX_VALUE = 2147483647 (10 chữ số), nên lấy 9 số cuối là an toàn
        int intId = (int) (timestamp % 1000000000L); // Lấy 9 số cuối
        if (intId < 0) {
            intId = Math.abs(intId); // Đảm bảo số dương
        }

        // Lấy category (default "Món chính" nếu không có)
        String category = "Món chính";
        if (recipe.getCategories() != null && !recipe.getCategories().isEmpty()) {
            category = recipe.getCategories().get(0);
        }

        // Tạo VietnameseFood object từ Recipe (format giống với Firebase)
        Map<String, Object> foodData = new HashMap<>();
        foodData.put("id", intId);
        foodData.put("name", recipe.getTitle() != null ? recipe.getTitle() : "Chưa có tên");
        foodData.put("description", recipe.getDescription() != null ? recipe.getDescription() : "");
        foodData.put("image", recipe.getImageUrl() != null ? recipe.getImageUrl() : "");
        foodData.put("category", category);
        foodData.put("region", "Miền Nam"); // Default region - có thể để user chọn sau

        // QUAN TRỌNG: Lưu authorId và recipeId để có thể tạo notification khi có like
        if (recipe.getAuthorId() != null && !recipe.getAuthorId().isEmpty()) {
            foodData.put("authorId", recipe.getAuthorId());
        }
        foodData.put("recipeId", recipeId); // Lưu recipeId để link với recipes node

        // Thêm các field từ recipe (giữ nguyên giá trị từ user, chỉ set default nếu
        // không có)
        foodData.put("viewCount", recipe.getViewCount());
        foodData.put("likeCount", recipe.getLikeCount());
        foodData.put("rating", recipe.getRating() > 0 ? recipe.getRating() : 4.5); // Default 4.5 nếu = 0
        foodData.put("ratingCount", recipe.getRatingCount() > 0 ? recipe.getRatingCount() : 100); // Default 100
        foodData.put("difficulty", recipe.getDifficulty() != null ? recipe.getDifficulty() : "Trung bình");
        // QUAN TRỌNG: Giữ nguyên prepTime từ user (có thể = 0), không set default
        // Chỉ set default nếu không có giá trị (null hoặc < 0)
        int prepTime = recipe.getPrepTime();
        if (prepTime < 0) {
            prepTime = 30; // Chỉ set default nếu < 0
        }
        foodData.put("prepTime", prepTime);

        // CookTime: chỉ set default nếu không có hoặc <= 0
        int cookTime = recipe.getCookTime();
        if (cookTime <= 0) {
            cookTime = 60; // Default 60 nếu không có
        }
        foodData.put("cookTime", cookTime);

        foodData.put("servings", recipe.getServings() > 0 ? recipe.getServings() : 4); // Default 4

        // Thêm ingredients và instructions nếu có (để giữ nguyên data từ user)
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            foodData.put("ingredients", recipe.getIngredients());
        }
        if (recipe.getInstructions() != null && !recipe.getInstructions().isEmpty()) {
            foodData.put("instructions", recipe.getInstructions());
        }

        // Tạo final variables để dùng trong lambda
        final int finalPrepTime = prepTime;
        final int finalCookTime = cookTime;

        // Thêm vào foods node với key là timestamp
        Log.d(TAG, "Attempting to add recipe to foods node. Food ID: " + foodId + ", Recipe: " + recipe.getTitle());
        Log.d(TAG, "Food data: " + foodData.toString());

        foodsRef.child(foodId).setValue(foodData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, " Recipe added to foods node successfully!");
                    Log.d(TAG, "   Food ID: " + foodId + ", Recipe ID: " + recipeId);
                    Log.d(TAG, "   Recipe name: " + recipe.getTitle());
                    Log.d(TAG, "   PrepTime: " + finalPrepTime + ", CookTime: " + finalCookTime + ", Total: "
                            + (finalPrepTime + finalCookTime));

                    // Tăng recipesCount cho user
                    incrementUserRecipesCount(recipe.getAuthorId());

                    // Callback thành công sau khi thêm vào cả 2 nodes
                    callback.onSuccess(recipeId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, " FAILED to add recipe to foods node!");
                    Log.e(TAG, "   Error: " + e.getMessage());
                    Log.e(TAG, "   Food ID: " + foodId + ", Recipe ID: " + recipeId);
                    Log.e(TAG, "   Recipe name: " + recipe.getTitle());
                    Log.e(TAG, "   Exception: ", e);
                    // Vẫn callback success vì đã tạo trong recipes node thành công
                    // Chỉ log warning, không fail toàn bộ operation
                    callback.onSuccess(recipeId);
                });
    }

    // Get user profile with callback
    public void getUserProfile(String userId, UserProfileCallback callback) {
        Log.d(TAG, "Getting user profile for userId: " + userId);
        usersRef.child(userId).get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        UserProfile profile = dataSnapshot.getValue(UserProfile.class);
                        if (profile != null) {
                            // Log followersCount để debug
                            if (dataSnapshot.hasChild("followersCount")) {
                                Object followersCountObj = dataSnapshot.child("followersCount").getValue();
                                int followersCount = followersCountObj instanceof Long
                                        ? ((Long) followersCountObj).intValue()
                                        : (followersCountObj instanceof Integer ? (Integer) followersCountObj : 0);
                                profile.setFollowersCount(followersCount);
                                Log.d(TAG, "Loaded followersCount from Firebase: " + followersCount + " for userId: "
                                        + userId);
                            } else {
                                Log.w(TAG, "followersCount field not found in Firebase for userId: " + userId);
                                profile.setFollowersCount(0); // Set default 0
                            }

                            if (dataSnapshot.hasChild("followingCount")) {
                                Object followingCountObj = dataSnapshot.child("followingCount").getValue();
                                int followingCount = followingCountObj instanceof Long
                                        ? ((Long) followingCountObj).intValue()
                                        : (followingCountObj instanceof Integer ? (Integer) followingCountObj : 0);
                                profile.setFollowingCount(followingCount);
                                Log.d(TAG, "Loaded followingCount from Firebase: " + followingCount + " for userId: "
                                        + userId);
                            } else {
                                profile.setFollowingCount(0); // Set default 0
                            }

                            Log.d(TAG, "UserProfile loaded - followersCount: " + profile.getFollowersCount()
                                    + ", followingCount: " + profile.getFollowingCount());
                        }
                        callback.onSuccess(profile);
                    } else {
                        // Profile doesn't exist, try to create it automatically
                        Log.w(TAG, "User profile not found for userId: " + userId + ", attempting to create it");
                        createUserProfileIfNotExists(userId, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get user profile for userId: " + userId, e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Create user profile if it doesn't exist
     */
    private void createUserProfileIfNotExists(String userId, UserProfileCallback callback) {
        // Try to get user info from FirebaseAuth
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String displayName = "Người dùng";
        String email = "";

        // If the userId matches current user, use FirebaseAuth info
        if (firebaseUser != null && firebaseUser.getUid().equals(userId)) {
            displayName = firebaseUser.getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                email = firebaseUser.getEmail();
                if (email != null && email.contains("@")) {
                    String emailName = email.substring(0, email.indexOf("@"));
                    if (!emailName.isEmpty()) {
                        displayName = emailName.substring(0, 1).toUpperCase() +
                                (emailName.length() > 1 ? emailName.substring(1) : "");
                    }
                }
            }
            if (email.isEmpty()) {
                email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
            }
        } else {
            // For other users, try to get from FirebaseAuth (if they're logged in)
            // Otherwise, use default values
            email = "";
        }

        // Create basic profile
        UserProfile newProfile = new UserProfile();
        newProfile.setId(userId);
        newProfile.setFullName(displayName);
        newProfile.setEmail(email);
        newProfile.setRecipesCount(0);
        newProfile.setFavoritesCount(0);
        newProfile.setFollowersCount(0);
        newProfile.setFollowingCount(0);

        // Save to Firebase
        usersRef.child(userId).setValue(newProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Auto-created user profile for userId: " + userId);
                    callback.onSuccess(newProfile);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to auto-create user profile for userId: " + userId, e);
                    // Still return the profile object even if save failed
                    callback.onSuccess(newProfile);
                });
    }

    // Get user recipes with callback - query from foods node where authorId matches
    public void getUserRecipes(String userId, RecipeListCallback callback) {
        // Query từ foods node vì recipes được lưu ở đó
        foodsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                List<Recipe> recipes = new java.util.ArrayList<>();

                if (dataSnapshot != null && dataSnapshot.exists()) {
                    for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            // Check if this food has authorId matching userId
                            if (snapshot.hasChild("authorId")) {
                                String authorId = snapshot.child("authorId").getValue(String.class);
                                if (authorId != null && authorId.equals(userId)) {
                                    // Convert VietnameseFood to Recipe
                                    com.example.cookshare.models.VietnameseFood vietnameseFood = snapshot
                                            .getValue(com.example.cookshare.models.VietnameseFood.class);
                                    if (vietnameseFood != null) {
                                        Recipe recipe = vietnameseFood.toRecipe();
                                        recipe.setId(snapshot.getKey());

                                        // Đọc authorId và authorName từ snapshot
                                        if (snapshot.hasChild("authorId")) {
                                            String authId = snapshot.child("authorId").getValue(String.class);
                                            if (authId != null && !authId.isEmpty()
                                                    && !authId.equals("vietnamese_food")) {
                                                recipe.setAuthorId(authId);

                                                if (snapshot.hasChild("authorName")) {
                                                    String authorName = snapshot.child("authorName")
                                                            .getValue(String.class);
                                                    if (authorName != null && !authorName.isEmpty()) {
                                                        recipe.setAuthorName(authorName);
                                                    }
                                                }
                                            }
                                        }

                                        // Đọc các giá trị khác từ snapshot
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

                                        // Đọc rating và ratingCount từ snapshot
                                        try {
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
                                            Log.e(TAG,
                                                    "Error reading rating/ratingCount for recipe " + snapshot.getKey(),
                                                    e);
                                        }

                                        // Đọc ingredients và instructions từ Firebase
                                        try {
                                            if (snapshot.hasChild("ingredients")) {
                                                Object ingredientsObj = snapshot.child("ingredients").getValue();
                                                if (ingredientsObj != null) {
                                                    @SuppressWarnings("unchecked")
                                                    java.util.List<String> ingredients = (java.util.List<String>) ingredientsObj;
                                                    recipe.setIngredients(ingredients);
                                                }
                                            }
                                            if (snapshot.hasChild("instructions")) {
                                                Object instructionsObj = snapshot.child("instructions").getValue();
                                                if (instructionsObj != null) {
                                                    @SuppressWarnings("unchecked")
                                                    java.util.List<String> instructions = (java.util.List<String>) instructionsObj;
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
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing recipe: " + snapshot.getKey(), e);
                        }
                    }
                }

                Log.d(TAG, "Found " + recipes.size() + " recipes for user: " + userId);
                callback.onSuccess(recipes);
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Failed to get user recipes", databaseError.toException());
                callback.onError(databaseError.getMessage());
            }
        });
    }

    /**
     * Update a specific field in user profile
     */
    public void updateUserProfileField(String userId, String fieldName, Object value, UserProfileCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("User ID is required");
            return;
        }

        usersRef.child(userId).child(fieldName).setValue(value)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Updated " + fieldName + " for user: " + userId);
                    // Reload profile to return updated profile
                    getUserProfile(userId, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update " + fieldName, e);
                    callback.onError(e.getMessage());
                });
    }

    // Update user profile with callback
    public void updateUserProfile(UserProfile profile, UserProfileCallback callback) {
        String userId = profile.getId();
        if (userId == null || userId.isEmpty()) {
            // Fallback: lấy UID từ FirebaseAuth
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                profile.setId(userId);
            } else {
                callback.onError("User ID is null");
                return;
            }
        }
        // Create final variable for lambda
        final String finalUserId = userId;
        usersRef.child(finalUserId).setValue(profile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile updated successfully");
                    // Reload profile to get latest data including updated fields
                    getUserProfile(finalUserId, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update user profile", e);
                    callback.onError(e.getMessage());
                });
    }

    // Recipe rating operations
    public void submitRecipeRating(String recipeId, float rating, RatingCallback callback) {
        if (recipeId == null || recipeId.isEmpty()) {
            callback.onError("Recipe ID is required");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }

        // Check foods node first (vì recipes được hiển thị từ foods node)
        DatabaseReference foodRef = foodsRef.child(recipeId);
        DatabaseReference recipeRef = recipesRef.child(recipeId);

        foodRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot foodSnapshot) {
                if (foodSnapshot.exists()) {
                    // Recipe exists in foods node
                    Log.d(TAG, " Recipe found in foods node: " + recipeId);

                    // Check if there's a linked recipeId (user-created recipe)
                    String linkedRecipeId = foodSnapshot.child("recipeId").getValue(String.class);
                    if (linkedRecipeId != null && !linkedRecipeId.isEmpty()) {
                        // User-created recipe - save rating to BOTH nodes
                        Log.d(TAG,
                                "   Linked recipeId found: " + linkedRecipeId + " - saving to recipes AND foods nodes");
                        DatabaseReference linkedRecipeRef = recipesRef.child(linkedRecipeId);
                        submitRatingToBothNodes(linkedRecipeRef, foodRef, recipeId, rating, callback);
                    } else {
                        // API recipe - only save to foods node
                        Log.d(TAG, "   No linked recipeId - API recipe, saving to foods node only");
                        submitRatingToNode(foodRef, recipeId, rating, callback);
                    }
                } else {
                    // Not in foods node, check recipes node
                    Log.d(TAG, "Recipe not in foods node, checking recipes node: " + recipeId);
                    recipeRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot recipeSnapshot) {
                            if (recipeSnapshot.exists()) {
                                Log.d(TAG, " Recipe found in recipes node: " + recipeId);
                                submitRatingToNode(recipeRef, recipeId, rating, callback);
                            } else {
                                Log.e(TAG, " Recipe not found in both nodes: " + recipeId);
                                callback.onError("Recipe not found");
                            }
                        }

                        @Override
                        public void onCancelled(com.google.firebase.database.DatabaseError error) {
                            Log.e(TAG, "Error checking recipes node", error.toException());
                            callback.onError("Error checking recipes node: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                // If error, try recipes node as fallback
                Log.w(TAG, "Error checking foods node, trying recipes node: " + error.getMessage());
                recipeRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot recipeSnapshot) {
                        if (recipeSnapshot.exists()) {
                            Log.d(TAG, " Recipe found in recipes node (fallback): " + recipeId);
                            submitRatingToNode(recipeRef, recipeId, rating, callback);
                        } else {
                            Log.e(TAG, " Recipe not found in recipes node: " + recipeId);
                            callback.onError("Recipe not found");
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError recipeError) {
                        Log.e(TAG, "Error checking recipes node", recipeError.toException());
                        callback.onError("Error checking recipes node: " + recipeError.getMessage());
                    }
                });
            }
        });
    }

    private void submitRatingToNode(DatabaseReference recipeRef, String recipeId, float rating,
            RatingCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }

        DatabaseReference ratingsRef = recipeRef.child("ratings");
        DatabaseReference userRatingRef = ratingsRef.child(user.getUid());

        // Step 1: Save user's rating
        userRatingRef.setValue(rating)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User rating saved: " + rating);

                    // Step 2: Calculate new average rating using transaction
                    ratingsRef.get().addOnSuccessListener(ratingsSnapshot -> {
                        double totalRating = 0.0;
                        int count = 0;

                        for (DataSnapshot child : ratingsSnapshot.getChildren()) {
                            Object ratingObj = child.getValue();
                            if (ratingObj != null) {
                                double ratingValue = ratingObj instanceof Double
                                        ? (Double) ratingObj
                                        : ratingObj instanceof Long
                                                ? ((Long) ratingObj).doubleValue()
                                                : ((Number) ratingObj).doubleValue();
                                totalRating += ratingValue;
                                count++;
                            }
                        }

                        final double newAverageRating = count > 0 ? totalRating / count : 0.0;
                        final int finalCount = count;

                        // Step 3: Update rating and ratingCount using updateChildren (simpler than
                        // transaction)
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("rating", newAverageRating);
                        updates.put("ratingCount", finalCount);

                        recipeRef.updateChildren(updates)
                                .addOnSuccessListener(aVoid2 -> {
                                    Log.d(TAG, "Rating updated successfully: " + newAverageRating + " (" + finalCount
                                            + " ratings)");

                                    // Tạo notification cho author khi có rating mới
                                    // Truyền recipeId gốc (từ foods node) để check cả 2 nodes
                                    createRatingNotification(recipeId, rating, user.getUid());

                                    callback.onSuccess(newAverageRating, finalCount);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating rating", e);
                                    callback.onError(e.getMessage());
                                });
                    })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error calculating average rating", e);
                                callback.onError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user rating", e);
                    callback.onError(e.getMessage());
                });
    }

    private void submitRatingToBothNodes(DatabaseReference recipeRef, DatabaseReference foodRef, String recipeId,
            float rating,
            RatingCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }

        DatabaseReference ratingsRef = recipeRef.child("ratings");
        DatabaseReference userRatingRef = ratingsRef.child(user.getUid());

        // Step 1: Save user's rating to recipes node
        userRatingRef.setValue(rating)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User rating saved to recipes node: " + rating);

                    // Step 2: Calculate new average rating
                    ratingsRef.get().addOnSuccessListener(ratingsSnapshot -> {
                        double totalRating = 0.0;
                        int count = 0;

                        for (DataSnapshot child : ratingsSnapshot.getChildren()) {
                            Object ratingObj = child.getValue();
                            if (ratingObj != null) {
                                double ratingValue = ratingObj instanceof Double
                                        ? (Double) ratingObj
                                        : ratingObj instanceof Long
                                                ? ((Long) ratingObj).doubleValue()
                                                : ((Number) ratingObj).doubleValue();
                                totalRating += ratingValue;
                                count++;
                            }
                        }

                        final double newAverageRating = count > 0 ? totalRating / count : 0.0;
                        final int finalCount = count;

                        // Step 3: Update rating and ratingCount in BOTH nodes
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("rating", newAverageRating);
                        updates.put("ratingCount", finalCount);

                        // Also save user's individual rating to foods node
                        Map<String, Object> foodUpdates = new HashMap<>(updates);
                        foodUpdates.put("ratings/" + user.getUid(), rating);

                        // Update recipes node first, then foods node
                        recipeRef.updateChildren(updates)
                                .addOnSuccessListener(aVoid2 -> {
                                    Log.d(TAG,
                                            "Rating updated successfully in recipes node: " + newAverageRating + " ("
                                                    + finalCount
                                                    + " ratings)");

                                    // Now update foods node
                                    foodRef.updateChildren(foodUpdates)
                                            .addOnSuccessListener(aVoid3 -> {
                                                Log.d(TAG,
                                                        "Rating updated successfully in foods node: " + newAverageRating
                                                                + " (" + finalCount
                                                                + " ratings)");

                                                // Tạo notification cho author khi có rating mới
                                                createRatingNotification(recipeId, rating, user.getUid());

                                                callback.onSuccess(newAverageRating, finalCount);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error updating rating in foods node", e);
                                                // Still call success because recipes node was updated
                                                callback.onSuccess(newAverageRating, finalCount);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating rating in recipes node", e);
                                    callback.onError(e.getMessage());
                                });
                    })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error calculating average rating", e);
                                callback.onError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user rating", e);
                    callback.onError(e.getMessage());
                });
    }

    // Recipe statistics operations
    // Update viewCount và likeCount trong foods node (vì code đang load từ foods)
    public void incrementViewCount(String recipeId) {
        if (recipeId == null || recipeId.isEmpty()) {
            Log.e(TAG, "Recipe ID is null or empty");
            return;
        }

        // Update trong foods node (vì code đang load từ foods)
        DatabaseReference foodRef = foodsRef.child(recipeId).child("viewCount");
        foodRef.get().addOnSuccessListener(dataSnapshot -> {
            int currentCount = 0;
            if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                Object value = dataSnapshot.getValue();
                if (value instanceof Long) {
                    currentCount = ((Long) value).intValue();
                } else if (value instanceof Integer) {
                    currentCount = (Integer) value;
                }
            }
            foodRef.setValue(currentCount + 1)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "View count incremented successfully in foods node"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to increment view count in foods node", e));
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get current view count from foods node", e);
            // Nếu không lấy được, set về 1
            foodRef.setValue(1);
        });
    }

    public void updateLikeCount(String recipeId, boolean isLiked) {
        if (recipeId == null || recipeId.isEmpty()) {
            Log.e(TAG, " Recipe ID is null or empty");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, " User is not logged in, cannot update like");
            return;
        }

        Log.d(TAG, " updateLikeCount called: recipeId=" + recipeId + ", isLiked=" + isLiked + ", userId="
                + user.getUid());
        Log.d(TAG, " RecipeId type: " + (recipeId != null ? recipeId.getClass().getSimpleName() : "null"));
        Log.d(TAG, " RecipeId length: " + (recipeId != null ? recipeId.length() : 0));

        // Lưu/xóa trong favorites node
        if (isLiked) {
            Log.d(TAG, " Calling addToFavorites for recipeId: " + recipeId);
            addToFavorites(recipeId);
        } else {
            Log.d(TAG, " Calling removeFromFavorites for recipeId: " + recipeId);
            removeFromFavorites(recipeId);
        }

        // Update trong foods node (vì code đang load từ foods)
        DatabaseReference foodRef = foodsRef.child(recipeId).child("likeCount");
        foodRef.get().addOnSuccessListener(dataSnapshot -> {
            int currentCount = 0;
            if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                Object value = dataSnapshot.getValue();
                if (value instanceof Long) {
                    currentCount = ((Long) value).intValue();
                } else if (value instanceof Integer) {
                    currentCount = (Integer) value;
                }
            }

            int newCount = isLiked ? currentCount + 1 : Math.max(0, currentCount - 1);
            foodRef.setValue(newCount)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Like count updated successfully in foods node: " + newCount);

                        // Tạo notification cho recipe author khi có like
                        if (isLiked) {
                            Log.d(TAG, " Checking if notification should be created for recipeId: " + recipeId);
                            Log.d(TAG, "   Current user ID: " + user.getUid());

                            // Lấy thông tin recipe từ foods node để tìm authorId
                            foodsRef.child(recipeId).addListenerForSingleValueEvent(
                                    new com.google.firebase.database.ValueEventListener() {
                                        @Override
                                        public void onDataChange(
                                                com.google.firebase.database.DataSnapshot foodSnapshot) {
                                            Log.d(TAG, " onDataChange: foods node snapshot exists: "
                                                    + foodSnapshot.exists());
                                            if (foodSnapshot.exists()) {
                                                // Thử lấy authorId trực tiếp từ foods node (đã được lưu khi tạo recipe)
                                                final String authorIdFromFood = foodSnapshot.child("authorId")
                                                        .getValue(String.class);
                                                final String recipeTitleFromFood = foodSnapshot.child("name")
                                                        .getValue(String.class);
                                                final String linkedRecipeId = foodSnapshot.child("recipeId")
                                                        .getValue(String.class);

                                                Log.d(TAG, " Recipe info from foods node:");
                                                Log.d(TAG, "   - authorId: " + authorIdFromFood);
                                                Log.d(TAG, "   - recipeTitle: " + recipeTitleFromFood);
                                                Log.d(TAG, "   - linkedRecipeId: " + linkedRecipeId);
                                                Log.d(TAG, "   - snapshot key: " + foodSnapshot.getKey());

                                                // Nếu không có authorId trong foods, thử tìm trong recipes node
                                                if ((authorIdFromFood == null || authorIdFromFood.isEmpty())
                                                        && linkedRecipeId != null && !linkedRecipeId.isEmpty()) {
                                                    Log.d(TAG,
                                                            " No authorId in foods, checking recipes node with recipeId: "
                                                                    + linkedRecipeId);
                                                    recipesRef.child(linkedRecipeId).addListenerForSingleValueEvent(
                                                            new com.google.firebase.database.ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(
                                                                        com.google.firebase.database.DataSnapshot recipeSnapshot) {
                                                                    Log.d(TAG,
                                                                            " Recipe snapshot from recipes node exists: "
                                                                                    + recipeSnapshot.exists());
                                                                    if (recipeSnapshot.exists()) {
                                                                        final String authorIdFromRecipe = recipeSnapshot
                                                                                .child("authorId")
                                                                                .getValue(String.class);
                                                                        final String recipeTitleFromRecipe = recipeSnapshot
                                                                                .child("title")
                                                                                .getValue(String.class);
                                                                        final String finalRecipeTitle = (recipeTitleFromRecipe != null
                                                                                && !recipeTitleFromRecipe.isEmpty())
                                                                                        ? recipeTitleFromRecipe
                                                                                        : recipeTitleFromFood;

                                                                        Log.d(TAG,
                                                                                " Recipe info from recipes node:");
                                                                        Log.d(TAG,
                                                                                "   - authorId: " + authorIdFromRecipe);
                                                                        Log.d(TAG, "   - recipeTitle: "
                                                                                + finalRecipeTitle);
                                                                        createNotificationIfNeeded(authorIdFromRecipe,
                                                                                finalRecipeTitle, recipeId,
                                                                                user.getUid());
                                                                    } else {
                                                                        Log.w(TAG,
                                                                                " Recipe not found in recipes node with linkedRecipeId: "
                                                                                        + linkedRecipeId);
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(
                                                                        com.google.firebase.database.DatabaseError error) {
                                                                    Log.e(TAG,
                                                                            " Failed to get recipe from recipes node: "
                                                                                    + error.getMessage(),
                                                                            error.toException());
                                                                }
                                                            });
                                                } else if (authorIdFromFood != null && !authorIdFromFood.isEmpty()) {
                                                    // Có authorId trong foods node, tạo notification trực tiếp
                                                    Log.d(TAG,
                                                            " Found authorId in foods node, creating notification");
                                                    createNotificationIfNeeded(authorIdFromFood, recipeTitleFromFood,
                                                            recipeId,
                                                            user.getUid());
                                                } else {
                                                    Log.w(TAG,
                                                            " No authorId found in foods node and no linkedRecipeId to check recipes node");
                                                    Log.w(TAG, "   - authorIdFromFood: " + authorIdFromFood);
                                                    Log.w(TAG, "   - linkedRecipeId: " + linkedRecipeId);
                                                }
                                            } else {
                                                Log.w(TAG,
                                                        " Recipe not found in foods node with recipeId: "
                                                                + recipeId);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(com.google.firebase.database.DatabaseError error) {
                                            Log.e(TAG,
                                                    " Failed to get recipe info for notification: "
                                                            + error.getMessage(),
                                                    error.toException());
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update like count in foods node", e));
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get current like count from foods node", e);
            // Nếu không lấy được và đang like, set về 1
            if (isLiked) {
                foodRef.setValue(1);
            }
        });
    }

    // Initialize default values for all foods in Firebase Database
    // Chạy method này MỘT LẦN để thêm các field mặc định vào tất cả foods
    public void initializeFoodsDefaultValues() {
        foodsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                final java.util.concurrent.atomic.AtomicInteger updatedCount = new java.util.concurrent.atomic.AtomicInteger(
                        0);
                final int totalFoods = (int) dataSnapshot.getChildrenCount();

                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String foodId = snapshot.getKey();
                    if (foodId != null) {
                        Map<String, Object> updates = new HashMap<>();

                        // Chỉ thêm field nếu chưa có
                        if (!snapshot.hasChild("viewCount")) {
                            updates.put("viewCount", 0);
                        }
                        if (!snapshot.hasChild("likeCount")) {
                            updates.put("likeCount", 0);
                        }
                        if (!snapshot.hasChild("rating")) {
                            updates.put("rating", 4.5);
                        }
                        if (!snapshot.hasChild("ratingCount")) {
                            updates.put("ratingCount", 100);
                        }
                        if (!snapshot.hasChild("difficulty")) {
                            // Lấy difficulty từ category
                            String category = snapshot.child("category").getValue(String.class);
                            String difficulty = getDifficultyFromCategory(category);
                            updates.put("difficulty", difficulty);
                        }
                        if (!snapshot.hasChild("prepTime")) {
                            updates.put("prepTime", 30);
                        }
                        if (!snapshot.hasChild("cookTime")) {
                            updates.put("cookTime", 60);
                        }
                        if (!snapshot.hasChild("servings")) {
                            updates.put("servings", 4);
                        }

                        // Update chỉ các field cần thiết
                        if (!updates.isEmpty()) {
                            final String finalFoodId = foodId;
                            foodsRef.child(foodId).updateChildren(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        int count = updatedCount.incrementAndGet();
                                        Log.d(TAG, "Updated food " + finalFoodId + " with default values. Progress: "
                                                + count + "/" + totalFoods);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to update food " + finalFoodId, e);
                                    });
                        }
                    }
                }
                Log.d(TAG, "Initialization started. Processing " + totalFoods + " foods...");
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Failed to initialize foods default values", databaseError.toException());
            }
        });
    }

    private String getDifficultyFromCategory(String category) {
        if (category == null)
            return "Trung bình";
        switch (category) {
            case "Ăn sáng":
            case "Ăn nhanh":
                return "Dễ";
            case "Món nước":
            case "Canh":
                return "Trung bình";
            case "Món nướng":
            case "Chiên":
                return "Khó";
            default:
                return "Trung bình";
        }
    }

    // Follow/Unfollow operations
    private static final String FOLLOWERS_NODE = "followers";
    private static final String FOLLOWING_NODE = "following";

    public interface FollowCallback {
        void onSuccess();

        void onError(String error);
    }

    public interface RatingCallback {
        void onSuccess(double newAverageRating, int newRatingCount);

        void onError(String error);
    }

    /**
     * Follow a user
     * 
     * @param targetUserId User ID to follow
     * @param callback     Callback for result
     */
    public void followUser(String targetUserId, FollowCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not logged in");
            return;
        }

        String currentUserId = currentUser.getUid();
        if (currentUserId.equals(targetUserId)) {
            callback.onError("Cannot follow yourself");
            return;
        }

        DatabaseReference followersRef = database.getReference(FOLLOWERS_NODE).child(targetUserId).child(currentUserId);
        DatabaseReference followingRef = database.getReference(FOLLOWING_NODE).child(currentUserId).child(targetUserId);

        // Add to followers and following
        Map<String, Object> updates = new HashMap<>();
        updates.put(FOLLOWERS_NODE + "/" + targetUserId + "/" + currentUserId, true);
        updates.put(FOLLOWING_NODE + "/" + currentUserId + "/" + targetUserId, true);

        database.getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Followed user: " + targetUserId);
                    Log.d(TAG, "Current user: " + currentUserId);
                    Log.d(TAG, "Target user: " + targetUserId);

                    // Create notification first (before count update)
                    // Try to get follower profile, but use FirebaseAuth user info as fallback
                    getUserProfile(currentUserId, new UserProfileCallback() {
                        @Override
                        public void onSuccess(UserProfile followerProfile) {
                            String followerName = followerProfile != null && followerProfile.getFullName() != null
                                    && !followerProfile.getFullName().isEmpty()
                                            ? followerProfile.getFullName()
                                            : getFollowerNameFromAuth();
                            Log.d(TAG, "Creating follow notification for user: " + targetUserId + ", follower: "
                                    + followerName);
                            createNotification(targetUserId, "follow",
                                    followerName + " đã theo dõi bạn",
                                    followerName + " đã bắt đầu theo dõi bạn",
                                    null, currentUserId, followerName);
                        }

                        @Override
                        public void onError(String error) {
                            Log.w(TAG, "Failed to get follower profile: " + error + ", using auth info as fallback");
                            // Use FirebaseAuth user info as fallback
                            String followerName = getFollowerNameFromAuth();
                            createNotification(targetUserId, "follow",
                                    followerName + " đã theo dõi bạn",
                                    followerName + " đã bắt đầu theo dõi bạn",
                                    null, currentUserId, followerName);
                        }
                    });

                    // Update following count first (for current user)
                    updateFollowingCount(currentUserId, 1);

                    // Wait for the new follower node to appear in database, then count
                    // This ensures we count the actual number of followers after Firebase sync
                    final String finalTargetUserId = targetUserId;
                    DatabaseReference newFollowerRef = database.getReference(FOLLOWERS_NODE)
                            .child(finalTargetUserId)
                            .child(currentUserId);

                    // Use a listener to wait for follower node to appear
                    com.google.firebase.database.ValueEventListener waitListener = new com.google.firebase.database.ValueEventListener() {
                        private boolean hasFired = false;

                        @Override
                        public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            if (hasFired) {
                                return; // Prevent multiple fires
                            }

                            if (snapshot.exists()) {
                                hasFired = true;
                                newFollowerRef.removeEventListener(this);
                                Log.d(TAG, "Follower node confirmed in database for user: " + finalTargetUserId);

                                // CRITICAL: Verify that the new follower appears in the followers/$targetUserId
                                // node
                                // before counting. This ensures Firebase has fully synced the data.
                                verifyFollowerInListAndCount(finalTargetUserId, currentUserId, 0, 10, callback);
                            } else {
                                Log.d(TAG, "Follower node not yet in database, waiting...");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                            Log.e(TAG, "Error waiting for follower node: " + error.getMessage());
                            newFollowerRef.removeEventListener(this);
                            // Fallback: count anyway after delay with more retries
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                countFollowersWithRetry(finalTargetUserId, 0, 10, () -> {
                                    callback.onSuccess();
                                });
                            }, 1500);
                        }
                    };

                    // Add listener - it will fire immediately if node exists, or when it appears
                    newFollowerRef.addValueEventListener(waitListener);

                    // Safety timeout: if follower doesn't appear in 5 seconds, count anyway
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        try {
                            newFollowerRef.removeEventListener(waitListener);
                            Log.d(TAG, "Timeout reached, counting followers anyway with retries");
                            countFollowersWithRetry(finalTargetUserId, 0, 10, () -> {
                                callback.onSuccess();
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error in timeout handler", e);
                            callback.onSuccess();
                        }
                    }, 5000); // 5 second timeout (increased from 3s)
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to follow user", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Unfollow a user
     * 
     * @param targetUserId User ID to unfollow
     * @param callback     Callback for result
     */
    public void unfollowUser(String targetUserId, FollowCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not logged in");
            return;
        }

        String currentUserId = currentUser.getUid();

        DatabaseReference followersRef = database.getReference(FOLLOWERS_NODE).child(targetUserId).child(currentUserId);
        DatabaseReference followingRef = database.getReference(FOLLOWING_NODE).child(currentUserId).child(targetUserId);

        // Remove from followers and following
        Map<String, Object> updates = new HashMap<>();
        updates.put(FOLLOWERS_NODE + "/" + targetUserId + "/" + currentUserId, null);
        updates.put(FOLLOWING_NODE + "/" + currentUserId + "/" + targetUserId, null);

        database.getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Unfollowed user: " + targetUserId);

                    // Update following count first (for current user)
                    updateFollowingCount(currentUserId, -1);

                    // Update followers count and wait for transaction to complete before calling
                    // callback
                    updateFollowersCount(targetUserId, -1, () -> {
                        // Call callback after transaction is complete
                        callback.onSuccess();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to unfollow user", e);
                    callback.onError(e.getMessage());
                });
    }

    /**
     * Check if current user is following target user
     * 
     * @param targetUserId User ID to check
     * @param callback     Callback with boolean result
     */
    public void isFollowing(String targetUserId, com.google.firebase.database.ValueEventListener callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String currentUserId = currentUser.getUid();
        DatabaseReference followingRef = database.getReference(FOLLOWING_NODE)
                .child(currentUserId)
                .child(targetUserId);

        followingRef.addListenerForSingleValueEvent(callback);
    }

    /**
     * Get follower name from FirebaseAuth as fallback
     */
    private String getFollowerNameFromAuth() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                return displayName;
            }
            // Fallback: extract name from email
            String email = currentUser.getEmail();
            if (email != null && email.contains("@")) {
                String emailName = email.substring(0, email.indexOf("@"));
                // Capitalize first letter
                if (!emailName.isEmpty()) {
                    return emailName.substring(0, 1).toUpperCase() +
                            (emailName.length() > 1 ? emailName.substring(1) : "");
                }
            }
        }
        return "Ai đó";
    }

    /**
     * Update followers count for a user
     */
    private void updateFollowersCount(String userId, int delta) {
        updateFollowersCount(userId, delta, null);
    }

    /**
     * Update followers count for a user with callback
     * Counts actual followers from followers node instead of using transaction
     * This is the source of truth - counts all children in followers/$userId node
     */
    private void updateFollowersCount(String userId, int delta, Runnable onComplete) {
        Log.d(TAG, "Updating followersCount for userId: " + userId + ", delta: " + delta);

        // Create final copies for use in lambda expressions
        final String finalUserId = userId;
        final int finalDelta = delta;
        final Runnable finalOnComplete = onComplete;

        // Count actual followers from followers node (source of truth)
        DatabaseReference followersNodeRef = database.getReference(FOLLOWERS_NODE).child(finalUserId);
        followersNodeRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                int actualCount = 0;
                if (snapshot.exists()) {
                    // Count all children (each follower) - this is the real count
                    actualCount = (int) snapshot.getChildrenCount();
                    Log.d(TAG, "Found " + actualCount + " followers in database for userId: " + finalUserId);
                } else {
                    Log.d(TAG, "No followers node found for userId: " + finalUserId + ", setting count to 0");
                }

                // Create final copy for use in lambda expressions
                final int finalActualCount = actualCount;

                Log.d(TAG,
                        "Updating followersCount to " + finalActualCount + " for userId: " + finalUserId);

                // Force update followersCount in users node - this ensures UI updates
                // immediately
                usersRef.child(finalUserId).child("followersCount").setValue(finalActualCount)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG,
                                    "Successfully updated followersCount to " + finalActualCount + " for userId: "
                                            + finalUserId);
                            // Verify by reading back the value
                            usersRef.child(finalUserId).child("followersCount")
                                    .addListenerForSingleValueEvent(
                                            new com.google.firebase.database.ValueEventListener() {
                                                @Override
                                                public void onDataChange(
                                                        @NonNull com.google.firebase.database.DataSnapshot verifySnapshot) {
                                                    if (verifySnapshot.exists()) {
                                                        Object verifyValue = verifySnapshot.getValue();
                                                        int verifyCount = verifyValue instanceof Long
                                                                ? ((Long) verifyValue).intValue()
                                                                : (verifyValue instanceof Integer
                                                                        ? (Integer) verifyValue
                                                                        : 0);
                                                        Log.d(TAG, "Verified followersCount is now: " + verifyCount
                                                                + " for userId: " + finalUserId);
                                                    }
                                                    if (finalOnComplete != null) {
                                                        finalOnComplete.run();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(
                                                        @NonNull com.google.firebase.database.DatabaseError error) {
                                                    Log.e(TAG, "Error verifying followersCount", error.toException());
                                                    if (finalOnComplete != null) {
                                                        finalOnComplete.run();
                                                    }
                                                }
                                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to update followersCount for userId: " + finalUserId, e);
                            // Still call callback to avoid hanging
                            if (finalOnComplete != null) {
                                finalOnComplete.run();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Log.e(TAG, "Error counting followers for userId: " + finalUserId, error.toException());
                // Fallback: try transaction method
                updateFollowersCountWithTransaction(finalUserId, finalDelta, finalOnComplete);
            }
        });
    }

    /**
     * Count followers with retry logic to ensure accurate count after Firebase sync
     */
    private void countFollowersWithRetry(String userId, int attempt, int maxAttempts, Runnable onComplete) {
        Log.d(TAG, "Counting followers with retry for userId: " + userId + ", attempt: " + (attempt + 1) + "/"
                + maxAttempts);

        // Count actual followers from followers node
        DatabaseReference followersNodeRef = database.getReference(FOLLOWERS_NODE).child(userId);
        followersNodeRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                int actualCount = 0;
                if (snapshot.exists()) {
                    actualCount = (int) snapshot.getChildrenCount();
                    Log.d(TAG,
                            "Attempt " + (attempt + 1) + ": Found " + actualCount + " followers for userId: " + userId);

                    // Log all follower IDs for debugging
                    Log.d(TAG, "List of followers:");
                    for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                        Log.d(TAG, "  - Follower ID: " + child.getKey());
                    }
                } else {
                    Log.w(TAG, "Attempt " + (attempt + 1) + ": followers node does not exist for userId: " + userId);
                }

                final int finalCount = actualCount;
                Log.d(TAG, "Setting followersCount to: " + finalCount + " for userId: " + userId);

                // Update followersCount in users node using setValue
                // This will trigger the real-time listener in ProfileFragment and
                // UserProfileActivity
                usersRef.child(userId).child("followersCount").setValue(finalCount)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "SUCCESS: Updated followersCount to " + finalCount + " for userId: " + userId);
                            // Force trigger listeners by setting the value again with a minimal delay
                            // This ensures all listeners (including remote ones) are notified
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                // Re-read to verify and trigger any missed listeners
                                usersRef.child(userId).child("followersCount")
                                        .addListenerForSingleValueEvent(
                                                new com.google.firebase.database.ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(
                                                            @NonNull com.google.firebase.database.DataSnapshot verifySnapshot) {
                                                        if (verifySnapshot.exists()) {
                                                            Object verifyValue = verifySnapshot.getValue();
                                                            int verifyCount = verifyValue instanceof Long
                                                                    ? ((Long) verifyValue).intValue()
                                                                    : (verifyValue instanceof Integer
                                                                            ? (Integer) verifyValue
                                                                            : 0);
                                                            Log.d(TAG, "VERIFIED: followersCount in database is now: "
                                                                    + verifyCount + " (expected: " + finalCount + ")");

                                                            // If count doesn't match, retry once more
                                                            if (verifyCount != finalCount
                                                                    && attempt < maxAttempts - 1) {
                                                                Log.w(TAG, "Count mismatch! Expected: " + finalCount
                                                                        + ", Got: " + verifyCount + ". Retrying...");
                                                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                                    countFollowersWithRetry(userId, attempt + 1,
                                                                            maxAttempts, onComplete);
                                                                }, 500);
                                                                return;
                                                            }
                                                        } else {
                                                            Log.w(TAG,
                                                                    "VERIFY FAILED: followersCount node does not exist after setValue");
                                                        }

                                                        if (onComplete != null) {
                                                            onComplete.run();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(
                                                            @NonNull com.google.firebase.database.DatabaseError error) {
                                                        Log.e(TAG, "Error verifying followersCount",
                                                                error.toException());
                                                        if (onComplete != null) {
                                                            onComplete.run();
                                                        }
                                                    }
                                                });
                            }, 300); // Increased delay to 300ms to ensure Firebase sync
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to update followersCount", e);
                            // Retry if haven't reached max attempts
                            if (attempt < maxAttempts - 1) {
                                Log.d(TAG, "Retrying count in 800ms...");
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    countFollowersWithRetry(userId, attempt + 1, maxAttempts, onComplete);
                                }, 800); // Increased delay between retries to 800ms
                            } else {
                                if (onComplete != null) {
                                    onComplete.run();
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Log.e(TAG, "Error counting followers", error.toException());
                // Retry if haven't reached max attempts
                if (attempt < maxAttempts - 1) {
                    Log.d(TAG, "Retrying count in 800ms after error...");
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        countFollowersWithRetry(userId, attempt + 1, maxAttempts, onComplete);
                    }, 800); // Use same delay as success retry
                } else {
                    Log.e(TAG, "Max retries reached, giving up on counting followers");
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        });
    }

    /**
     * Verify that a new follower appears in the followers list before counting
     * This ensures Firebase has fully synced the new follower before we count
     * 
     * @param targetUserId  User ID whose followers we're checking
     * @param newFollowerId The new follower ID to verify
     * @param attempt       Current attempt number (for retry limit)
     * @param maxAttempts   Maximum number of verification attempts
     * @param callback      Callback to call when done
     */
    private void verifyFollowerInListAndCount(String targetUserId, String newFollowerId, int attempt, int maxAttempts,
            FollowCallback callback) {
        Log.d(TAG, "Verifying follower " + newFollowerId + " is in followers list for user: " + targetUserId
                + " (attempt " + (attempt + 1) + "/" + maxAttempts + ")");

        DatabaseReference followersListRef = database.getReference(FOLLOWERS_NODE).child(targetUserId);
        followersListRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                boolean found = false;
                if (snapshot.exists()) {
                    // Check if new follower is in the list
                    for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                        if (newFollowerId.equals(child.getKey())) {
                            found = true;
                            Log.d(TAG, "SUCCESS: New follower " + newFollowerId + " found in followers list!");
                            break;
                        }
                    }
                }

                if (found) {
                    // Follower is confirmed in the list, now count with retries
                    Log.d(TAG, "Follower confirmed in list, starting count with retries for user: " + targetUserId);
                    countFollowersWithRetry(targetUserId, 0, 10, () -> {
                        Log.d(TAG, "Followers count updated successfully for user: " + targetUserId);
                        // Force trigger listener by reading once more after a short delay
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            usersRef.child(targetUserId).child("followersCount")
                                    .addListenerForSingleValueEvent(
                                            new com.google.firebase.database.ValueEventListener() {
                                                @Override
                                                public void onDataChange(
                                                        @NonNull com.google.firebase.database.DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        Object value = snapshot.getValue();
                                                        int count = value instanceof Long
                                                                ? ((Long) value).intValue()
                                                                : (value instanceof Integer
                                                                        ? (Integer) value
                                                                        : 0);
                                                        Log.d(TAG,
                                                                "Final followersCount value in database: " + count);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(
                                                        @NonNull com.google.firebase.database.DatabaseError error) {
                                                    Log.e(TAG, "Error reading final followersCount",
                                                            error.toException());
                                                }
                                            });
                        }, 300);
                        callback.onSuccess();
                    });
                } else {
                    // Follower not yet in list, wait and retry if haven't exceeded max attempts
                    if (attempt < maxAttempts - 1) {
                        Log.d(TAG, "New follower not yet in followers list, retrying in 500ms... (attempt "
                                + (attempt + 1) + "/" + maxAttempts + ")");
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            verifyFollowerInListAndCount(targetUserId, newFollowerId, attempt + 1, maxAttempts,
                                    callback);
                        }, 500);
                    } else {
                        // Max attempts reached, proceed with counting anyway
                        Log.w(TAG, "Max verification attempts reached, proceeding with count anyway");
                        countFollowersWithRetry(targetUserId, 0, 10, () -> {
                            callback.onSuccess();
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Log.e(TAG, "Error verifying follower in list: " + error.getMessage());
                // On error, proceed with counting anyway (better than failing)
                Log.d(TAG, "Proceeding with count despite verification error");
                countFollowersWithRetry(targetUserId, 0, 10, () -> {
                    callback.onSuccess();
                });
            }
        });
    }

    /**
     * Fallback method using transaction (when counting fails)
     */
    private void updateFollowersCountWithTransaction(String userId, int delta, Runnable onComplete) {
        Log.d(TAG, "Using transaction fallback for followersCount, userId: " + userId + ", delta: " + delta);
        usersRef.child(userId).child("followersCount")
                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(
                            com.google.firebase.database.MutableData mutableData) {
                        Integer current = mutableData.getValue(Integer.class);
                        int newValue;
                        if (current == null) {
                            // Node doesn't exist yet, initialize with delta
                            newValue = Math.max(0, delta);
                        } else {
                            newValue = Math.max(0, current + delta);
                        }
                        Log.d(TAG, "FollowersCount transaction: current=" + current + ", newValue=" + newValue);
                        mutableData.setValue(newValue);
                        return com.google.firebase.database.Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(com.google.firebase.database.DatabaseError error, boolean committed,
                            com.google.firebase.database.DataSnapshot snapshot) {
                        if (error != null) {
                            Log.e(TAG, "Error updating followers count for userId: " + userId, error.toException());
                            Log.e(TAG, "Error code: " + error.getCode() + ", message: " + error.getMessage());
                        } else if (committed) {
                            Integer newValue = snapshot.getValue(Integer.class);
                            Log.d(TAG, "Successfully updated followersCount for userId: " + userId + ", newValue: "
                                    + newValue);
                        } else {
                            Log.w(TAG, "FollowersCount transaction not committed for userId: " + userId);
                        }
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
    }

    /**
     * Update following count for a user
     */
    private void updateFollowingCount(String userId, int delta) {
        usersRef.child(userId).child("followingCount")
                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(
                            com.google.firebase.database.MutableData mutableData) {
                        Integer current = mutableData.getValue(Integer.class);
                        if (current == null) {
                            mutableData.setValue(Math.max(0, delta));
                        } else {
                            mutableData.setValue(Math.max(0, current + delta));
                        }
                        return com.google.firebase.database.Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(com.google.firebase.database.DatabaseError error, boolean committed,
                            com.google.firebase.database.DataSnapshot snapshot) {
                        if (error != null) {
                            Log.e(TAG, "Error updating following count", error.toException());
                        }
                    }
                });
    }

    // Cooking History operations
    public void addToCookingHistory(String recipeId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, " Cannot add to cooking history: User is null");
            return;
        }
        if (recipeId == null || recipeId.isEmpty()) {
            Log.e(TAG, " Cannot add to cooking history: Recipe ID is null or empty");
            return;
        }

        // Lưu với timestamp để sắp xếp theo thời gian
        long timestamp = System.currentTimeMillis();
        String path = "cookingHistory/" + user.getUid() + "/recipes/" + recipeId;
        Log.d(TAG, " Adding to cooking history: " + path);

        Map<String, Object> historyData = new HashMap<>();
        historyData.put("recipeId", recipeId);
        historyData.put("timestamp", timestamp);
        historyData.put("viewedAt", timestamp);

        cookingHistoryRef.child(user.getUid()).child("recipes").child(recipeId).setValue(historyData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, " Added to cooking history successfully! Path: " + path);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, " Failed to add to cooking history! Path: " + path, e);
                });
    }

    public void loadCookingHistory(CookingHistoryCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (callback != null) {
                callback.onError("User not logged in");
            }
            return;
        }

        cookingHistoryRef.child(user.getUid()).child("recipes")
                .orderByChild("timestamp")
                .limitToLast(50) // Chỉ lấy 50 recipe gần nhất
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    List<String> recipeIds = new java.util.ArrayList<>();
                    if (dataSnapshot.exists()) {
                        for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String recipeId = snapshot.getKey();
                            if (recipeId != null) {
                                recipeIds.add(recipeId);
                            }
                        }
                    }
                    // Reverse để hiển thị mới nhất trước
                    java.util.Collections.reverse(recipeIds);
                    if (callback != null) {
                        callback.onSuccess(recipeIds);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load cooking history", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    // Notification operations
    private void createNotificationIfNeeded(String authorId, String recipeTitle, String recipeId,
            String currentUserId) {
        Log.d(TAG, " createNotificationIfNeeded called:");
        Log.d(TAG, "   - authorId: " + authorId);
        Log.d(TAG, "   - recipeTitle: " + recipeTitle);
        Log.d(TAG, "   - recipeId: " + recipeId);
        Log.d(TAG, "   - currentUserId: " + currentUserId);

        if (authorId == null || authorId.isEmpty()) {
            Log.w(TAG, " No authorId, skipping notification");
            return;
        }

        // Không tạo notification cho chính mình
        if (authorId.equals(currentUserId)) {
            Log.d(TAG, " User liked their own recipe, skipping notification (authorId == currentUserId)");
            return;
        }

        // Lấy tên người thích để hiển thị trong notification
        getUserProfile(currentUserId, new UserProfileCallback() {
            @Override
            public void onSuccess(UserProfile likedByProfile) {
                String likedByName = likedByProfile != null && likedByProfile.getFullName() != null
                        && !likedByProfile.getFullName().isEmpty()
                                ? likedByProfile.getFullName()
                                : "Ai đó";

                String title = likedByName + " đã thích công thức của bạn!";
                String message = likedByName + " đã thích công thức '"
                        + (recipeTitle != null ? recipeTitle : "Món ăn") + "' của bạn.";

                Log.d(TAG, " Creating notification:");
                Log.d(TAG, "   - userId (authorId): " + authorId);
                Log.d(TAG, "   - likedByName: " + likedByName);
                Log.d(TAG, "   - title: " + title);
                Log.d(TAG, "   - message: " + message);
                Log.d(TAG, "   - recipeId: " + recipeId);
                Log.d(TAG, "   - type: like");

                createNotification(authorId, "like", title, message, recipeId, currentUserId, likedByName);
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, " Failed to get liked by user profile: " + error);
                // Fallback: tạo notification không có tên
                String title = "Có người đã thích công thức của bạn!";
                String message = "Công thức '" + (recipeTitle != null ? recipeTitle : "Món ăn")
                        + "' của bạn đã nhận được một lượt thích mới.";

                createNotification(authorId, "like", title, message, recipeId, currentUserId, null);
            }
        });
    }

    private void createRatingNotification(String recipeId, float rating, String raterUserId) {
        // Check foods node first để lấy authorId (vì recipes được hiển thị từ foods
        // node)
        DatabaseReference foodRef = foodsRef.child(recipeId);

        foodRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot foodSnapshot) {
                if (foodSnapshot.exists()) {
                    // Check foods node trước
                    String authorIdFromFood = foodSnapshot.child("authorId").getValue(String.class);
                    String recipeTitleFromFood = foodSnapshot.child("name").getValue(String.class);
                    String linkedRecipeId = foodSnapshot.child("recipeId").getValue(String.class);

                    Log.d(TAG, " Rating notification - foods node data:");
                    Log.d(TAG, "   - authorIdFromFood: " + authorIdFromFood);
                    Log.d(TAG, "   - recipeTitleFromFood: " + recipeTitleFromFood);
                    Log.d(TAG, "   - linkedRecipeId: " + linkedRecipeId);

                    // Nếu có linkedRecipeId, check recipes node để lấy thông tin chính xác
                    if (linkedRecipeId != null && !linkedRecipeId.isEmpty()) {
                        DatabaseReference linkedRecipeRef = recipesRef.child(linkedRecipeId);
                        linkedRecipeRef
                                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                                    @Override
                                    public void onDataChange(com.google.firebase.database.DataSnapshot recipeSnapshot) {
                                        if (recipeSnapshot.exists()) {
                                            String authorIdFromRecipe = recipeSnapshot.child("authorId")
                                                    .getValue(String.class);
                                            String recipeTitleFromRecipe = recipeSnapshot.child("title")
                                                    .getValue(String.class);

                                            Log.d(TAG, " Rating notification - recipes node data:");
                                            Log.d(TAG, "   - authorIdFromRecipe: " + authorIdFromRecipe);
                                            Log.d(TAG, "   - recipeTitleFromRecipe: " + recipeTitleFromRecipe);

                                            String finalAuthorId = authorIdFromRecipe != null
                                                    && !authorIdFromRecipe.isEmpty()
                                                            ? authorIdFromRecipe
                                                            : authorIdFromFood;
                                            String finalRecipeTitle = recipeTitleFromRecipe != null
                                                    && !recipeTitleFromRecipe.isEmpty()
                                                            ? recipeTitleFromRecipe
                                                            : recipeTitleFromFood;

                                            processRatingNotification(finalAuthorId, finalRecipeTitle, recipeId, rating,
                                                    raterUserId);
                                        } else {
                                            // Fallback to foods node data
                                            Log.d(TAG, " Recipe not found in recipes node, using foods node data");
                                            processRatingNotification(authorIdFromFood, recipeTitleFromFood, recipeId,
                                                    rating, raterUserId);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                                        Log.e(TAG, "Error getting recipe info from recipes node, using foods node",
                                                error.toException());
                                        processRatingNotification(authorIdFromFood, recipeTitleFromFood, recipeId,
                                                rating, raterUserId);
                                    }
                                });
                    } else {
                        // Không có linkedRecipeId, dùng data từ foods node
                        Log.d(TAG, " No linkedRecipeId, using foods node data directly");
                        processRatingNotification(authorIdFromFood, recipeTitleFromFood, recipeId, rating, raterUserId);
                    }
                } else {
                    // Không có trong foods node, check recipes node trực tiếp
                    DatabaseReference recipeRefCheck = recipesRef.child(recipeId);
                    recipeRefCheck
                            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                                @Override
                                public void onDataChange(com.google.firebase.database.DataSnapshot recipeSnapshot) {
                                    if (recipeSnapshot.exists()) {
                                        String authorId = recipeSnapshot.child("authorId").getValue(String.class);
                                        String recipeTitle = recipeSnapshot.child("title").getValue(String.class);
                                        processRatingNotification(authorId, recipeTitle, recipeId, rating, raterUserId);
                                    } else {
                                        Log.w(TAG, " Recipe not found in both nodes for notification");
                                    }
                                }

                                @Override
                                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                                    Log.e(TAG, "Error getting recipe info for rating notification",
                                            error.toException());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Log.e(TAG, "Error checking foods node for rating notification", error.toException());
                // Fallback: check recipes node
                DatabaseReference recipeRefCheck = recipesRef.child(recipeId);
                recipeRefCheck.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot recipeSnapshot) {
                        if (recipeSnapshot.exists()) {
                            String authorId = recipeSnapshot.child("authorId").getValue(String.class);
                            String recipeTitle = recipeSnapshot.child("title").getValue(String.class);
                            processRatingNotification(authorId, recipeTitle, recipeId, rating, raterUserId);
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error2) {
                        Log.e(TAG, "Error getting recipe info for rating notification (fallback)",
                                error2.toException());
                    }
                });
            }
        });
    }

    private void processRatingNotification(String authorId, String recipeTitle, String recipeId, float rating,
            String raterUserId) {
        if (authorId != null && !authorId.isEmpty() && !authorId.equals("vietnamese_food")) {
            // Chỉ tạo notification cho user-created recipes (không phải API recipes)
            if (!authorId.equals(raterUserId)) {
                // Không tạo notification cho chính mình
                Log.d(TAG, " Creating rating notification for author: " + authorId + ", recipe: " + recipeTitle);
                createRatingNotificationForAuthor(authorId, recipeTitle, recipeId, rating, raterUserId);
            } else {
                Log.d(TAG, " User rated their own recipe, skipping notification");
            }
        } else {
            Log.d(TAG, " Recipe is API recipe or no authorId (authorId: " + authorId + "), skipping notification");
        }
    }

    private void createRatingNotificationForAuthor(String authorId, String recipeTitle, String recipeId, float rating,
            String raterUserId) {
        // Lấy tên người đánh giá để hiển thị trong notification
        getUserProfile(raterUserId, new UserProfileCallback() {
            @Override
            public void onSuccess(UserProfile raterProfile) {
                String raterName = raterProfile != null && raterProfile.getFullName() != null
                        ? raterProfile.getFullName()
                        : "Ai đó";

                String title = "Có người đã đánh giá công thức của bạn!";
                String message = raterName + " đã đánh giá công thức '" +
                        (recipeTitle != null ? recipeTitle : "Món ăn") +
                        "' của bạn " + String.format("%.1f", rating) + " sao.";

                Log.d(TAG, " Creating rating notification:");
                Log.d(TAG, "   - authorId: " + authorId);
                Log.d(TAG, "   - raterName: " + raterName);
                Log.d(TAG, "   - raterUserId: " + raterUserId);
                Log.d(TAG, "   - recipeTitle: " + recipeTitle);
                Log.d(TAG, "   - rating: " + rating);
                Log.d(TAG, "   - recipeId: " + recipeId);

                createNotification(authorId, "rating", title, message, recipeId, raterUserId, raterName);
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, " Failed to get rater profile: " + error);
                // Fallback: tạo notification không có tên
                String title = "Có người đã đánh giá công thức của bạn!";
                String message = "Công thức '" + (recipeTitle != null ? recipeTitle : "Món ăn")
                        + "' của bạn đã nhận được đánh giá " + String.format("%.1f", rating) + " sao.";

                createNotification(authorId, "rating", title, message, recipeId, raterUserId, null);
            }
        });
    }

    public void createNotification(String userId, String type, String title, String message, String recipeId) {
        createNotification(userId, type, title, message, recipeId, null, null);
    }

    public void createNotification(String userId, String type, String title, String message, String recipeId,
            String likedById, String likedByName) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Cannot create notification: User ID is null or empty");
            return;
        }

        // Không tạo notification cho chính mình
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(userId)) {
            Log.d(TAG, "Skipping notification: User is trying to notify themselves (userId: " + userId + ")");
            return;
        }

        String notificationId = notificationsRef.child(userId).push().getKey();
        if (notificationId == null) {
            Log.e(TAG, "Failed to generate notification ID");
            return;
        }

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", type); // "like", "comment", "follow", etc.
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("recipeId", recipeId != null ? recipeId : "");
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("read", false);

        // Thêm thông tin người thích nếu có
        if (likedById != null && !likedById.isEmpty()) {
            notificationData.put("likedById", likedById);
        }
        if (likedByName != null && !likedByName.isEmpty()) {
            notificationData.put("likedByName", likedByName);
        }

        String path = "notifications/" + userId + "/" + notificationId;
        Log.d(TAG, "Creating notification at path: " + path);
        Log.d(TAG, "  - userId: " + userId);
        Log.d(TAG, "  - type: " + type);
        Log.d(TAG, "  - title: " + title);
        Log.d(TAG, "  - message: " + message);
        Log.d(TAG, "  - recipeId: " + recipeId);
        Log.d(TAG, "  - likedById: " + likedById);
        Log.d(TAG, "  - likedByName: " + likedByName);

        notificationsRef.child(userId).child(notificationId).setValue(notificationData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification created successfully!");
                    Log.d(TAG, "  Path: " + path);
                    Log.d(TAG, "  Notification ID: " + notificationId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create notification at path: " + path, e);
                    Log.e(TAG, "  Error: " + e.getMessage());
                    Log.e(TAG, "  Error details: " + e.toString());
                });
    }

    public void loadNotifications(NotificationListCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, " Cannot load notifications: User not logged in");
            if (callback != null) {
                callback.onError("User not logged in");
            }
            return;
        }

        String userId = user.getUid();
        String path = "notifications/" + userId;
        Log.d(TAG, "📥 Loading notifications from path: " + path);
        Log.d(TAG, "   - User ID: " + userId);

        notificationsRef.child(userId)
                .orderByChild("timestamp")
                .limitToLast(50) // Chỉ lấy 50 notification gần nhất
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    Log.d(TAG, "📥 Firebase query completed");
                    Log.d(TAG, "   - Snapshot exists: " + dataSnapshot.exists());
                    Log.d(TAG, "   - Snapshot has children: " + (dataSnapshot.exists() && dataSnapshot.hasChildren()));

                    List<Map<String, Object>> notifications = new java.util.ArrayList<>();
                    if (dataSnapshot.exists()) {
                        int count = 0;
                        for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            count++;
                            String notificationId = snapshot.getKey();
                            Log.d(TAG, "   - Found notification #" + count + ": ID=" + notificationId);

                            Map<String, Object> notification = (Map<String, Object>) snapshot.getValue();
                            if (notification != null) {
                                notification.put("id", notificationId);
                                notifications.add(notification);

                                // Log notification details
                                Object title = notification.get("title");
                                Object message = notification.get("message");
                                Object timestamp = notification.get("timestamp");
                                Log.d(TAG, "     Title: " + title);
                                Log.d(TAG, "     Message: " + message);
                                Log.d(TAG, "     Timestamp: " + timestamp);
                            } else {
                                Log.w(TAG, "   - Notification #" + count + " has null value!");
                            }
                        }
                        Log.d(TAG, "📥 Total notifications found: " + notifications.size());
                    } else {
                        Log.d(TAG, "📥 No notifications found (snapshot does not exist)");
                    }

                    // Reverse để hiển thị mới nhất trước
                    java.util.Collections.reverse(notifications);
                    Log.d(TAG, "📥 Returning " + notifications.size() + " notifications to callback");

                    if (callback != null) {
                        callback.onSuccess(notifications);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, " Failed to load notifications from path: " + path, e);
                    Log.e(TAG, "   - Error message: " + e.getMessage());
                    Log.e(TAG, "   - Error class: " + e.getClass().getName());
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    public void markNotificationAsRead(String notificationId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || notificationId == null || notificationId.isEmpty()) {
            return;
        }

        notificationsRef.child(user.getUid()).child(notificationId).child("read").setValue(true)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification marked as read: " + notificationId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to mark notification as read", e));
    }

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

    public interface CookingHistoryCallback {
        void onSuccess(List<String> recipeIds);

        void onError(String error);
    }

    public interface NotificationListCallback {
        void onSuccess(List<Map<String, Object>> notifications);

        void onError(String error);
    }

    // Increment user's recipes count
    private void incrementUserRecipesCount(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty");
            return;
        }

        DatabaseReference userRef = usersRef.child(userId).child("recipesCount");
        userRef.get().addOnSuccessListener(dataSnapshot -> {
            int currentCount = 0;
            if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                Object value = dataSnapshot.getValue();
                if (value instanceof Long) {
                    currentCount = ((Long) value).intValue();
                } else if (value instanceof Integer) {
                    currentCount = (Integer) value;
                }
            }
            userRef.setValue(currentCount + 1)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User recipes count incremented successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to increment user recipes count", e));
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get current recipes count for user", e);
            // Nếu không lấy được, set về 1
            userRef.setValue(1);
        });
    }

    // Increment or decrement user's favorites count
    private void incrementUserFavoritesCount(String userId, boolean increment) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty");
            return;
        }

        DatabaseReference userRef = usersRef.child(userId).child("favoritesCount");
        userRef.get().addOnSuccessListener(dataSnapshot -> {
            int currentCount = 0;
            if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                Object value = dataSnapshot.getValue();
                if (value instanceof Long) {
                    currentCount = ((Long) value).intValue();
                } else if (value instanceof Integer) {
                    currentCount = (Integer) value;
                }
            }
            int newCount = increment ? currentCount + 1 : Math.max(0, currentCount - 1);
            userRef.setValue(newCount)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User favorites count updated successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update user favorites count", e));
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get current favorites count for user", e);
            // Nếu không lấy được, set về 0 hoặc 1 tùy increment
            userRef.setValue(increment ? 1 : 0);
        });
    }
}
