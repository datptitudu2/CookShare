package com.example.cookshare;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookshare.adapters.RecipeAdapter;
import com.example.cookshare.models.Recipe;
import com.example.cookshare.models.UserProfile;
import com.example.cookshare.services.FirebaseDatabaseService;
import com.example.cookshare.viewmodels.ProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";
    public static final String EXTRA_USER_ID = "user_id";

    private Toolbar toolbar;
    private ShapeableImageView profileAvatar;
    private TextView profileName;
    private TextView profileBio;
    private TextView profileLocation;
    private TextView recipesCount;
    private TextView followersCount;
    private TextView followingCount;
    private MaterialButton followButton;
    private RecyclerView recipesRecyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;

    private String userId;
    private boolean isFollowing = false;
    private boolean isOwnProfile = false;
    private ProfileViewModel profileViewModel;
    private FirebaseDatabaseService databaseService;
    private RecipeAdapter recipeAdapter;

    // Real-time listener for followersCount
    private DatabaseReference followersCountRef;
    private ValueEventListener followersCountListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Get user ID from intent
        userId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if viewing own profile
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        isOwnProfile = currentUser != null && currentUser.getUid().equals(userId);

        // Initialize
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        // Initialize services
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        databaseService = new FirebaseDatabaseService();

        // Load user profile
        loadUserProfile();
        checkFollowStatus();
        // Note: Real-time listener will be setup in onStart()
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        profileAvatar = findViewById(R.id.profileAvatar);
        profileName = findViewById(R.id.profileName);
        profileBio = findViewById(R.id.profileBio);
        profileLocation = findViewById(R.id.profileLocation);
        recipesCount = findViewById(R.id.recipesCount);
        followersCount = findViewById(R.id.followersCount);
        followingCount = findViewById(R.id.followingCount);
        followButton = findViewById(R.id.followButton);
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Hồ sơ");
        }

        // Set toolbar text color to white
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));

        // Set navigation icon color to white
        // Post to ensure toolbar is fully initialized
        toolbar.post(() -> {
            Drawable upArrow = toolbar.getNavigationIcon();
            if (upArrow != null) {
                upArrow = DrawableCompat.wrap(upArrow);
                DrawableCompat.setTint(upArrow.mutate(),
                        ContextCompat.getColor(UserProfileActivity.this, android.R.color.white));
                toolbar.setNavigationIcon(upArrow);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        recipeAdapter = new RecipeAdapter();
        recipeAdapter.setOnRecipeClickListener(recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_TITLE, recipe.getTitle());
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_DESCRIPTION, recipe.getDescription());
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_IMAGE_URL, recipe.getImageUrl());
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_AUTHOR_ID, recipe.getAuthorId());
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_AUTHOR_NAME, recipe.getAuthorName());
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_PREP_TIME, recipe.getPrepTime());
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_COOK_TIME, recipe.getCookTime());
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_SERVINGS, recipe.getServings());
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_DIFFICULTY, recipe.getDifficulty());
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_RATING, recipe.getRating());
            intent.putExtra("recipe_rating_count", recipe.getRatingCount());

            if (recipe.getCategories() != null) {
                intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_CATEGORIES,
                        recipe.getCategories().toArray(new String[0]));
            }
            if (recipe.getIngredients() != null) {
                intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_INGREDIENTS,
                        recipe.getIngredients().toArray(new String[0]));
            }
            if (recipe.getInstructions() != null) {
                intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_INSTRUCTIONS,
                        recipe.getInstructions().toArray(new String[0]));
            }

            startActivity(intent);
        });

        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipesRecyclerView.setAdapter(recipeAdapter);
    }

    private void setupClickListeners() {
        // Follow button click
        followButton.setOnClickListener(v -> {
            if (isFollowing) {
                unfollowUser();
            } else {
                followUser();
            }
        });
    }

    private void loadUserProfile() {
        showLoading();

        profileViewModel.loadUserProfile(userId);
        profileViewModel.getUserProfile().observe(this, profile -> {
            if (profile != null) {
                displayUserProfile(profile);
                hideLoading();
            }
        });

        profileViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                hideLoading();
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Load user recipes
        profileViewModel.loadUserRecipes(userId);
        profileViewModel.getUserRecipes().observe(this, recipes -> {
            if (recipes != null) {
                // Update recipesCount based on actual recipes count
                int actualRecipesCount = recipes.size();

                // Update UI với số lượng thực tế ngay lập tức
                recipesCount.setText(String.valueOf(actualRecipesCount));

                // Update recipesCount in database if different from stored value
                UserProfile currentProfile = profileViewModel.getUserProfile().getValue();
                if (currentProfile != null && currentProfile.getRecipesCount() != actualRecipesCount) {
                    // Update in database
                    updateRecipesCountInDatabase(actualRecipesCount);
                }

                if (recipes.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                    recipeAdapter.updateRecipes(recipes);
                }
            }
        });
    }

    private void displayUserProfile(UserProfile profile) {
        // Set name
        profileName.setText(profile.getFullName() != null ? profile.getFullName() : "Người dùng");

        // Set bio
        if (profile.getBio() != null && !profile.getBio().isEmpty()) {
            profileBio.setText(profile.getBio());
            profileBio.setVisibility(View.VISIBLE);
        } else {
            profileBio.setVisibility(View.GONE);
        }

        // Set location
        LinearLayout locationContainer = findViewById(R.id.profileLocationContainer);
        if (profile.getLocation() != null && !profile.getLocation().isEmpty()) {
            profileLocation.setText(profile.getLocation());
            if (locationContainer != null) {
                locationContainer.setVisibility(View.VISIBLE);
            }
        } else {
            if (locationContainer != null) {
                locationContainer.setVisibility(View.GONE);
            }
        }

        // Set avatar - load from profile or FirebaseUser
        String avatarUrl = null;
        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
            avatarUrl = profile.getAvatarUrl();
        } else {
            // Fallback: try to get from FirebaseUser if viewing own profile or same user
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null && firebaseUser.getUid().equals(userId)) {
                if (firebaseUser.getPhotoUrl() != null) {
                    avatarUrl = firebaseUser.getPhotoUrl().toString();
                }
            }
        }

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .error(android.R.drawable.ic_menu_myplaces)
                    .circleCrop()
                    .into(profileAvatar);
        } else {
            // Show default avatar
            profileAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

        // Set stats - recipesCount sẽ được cập nhật sau khi load recipes xong
        // Chỉ set nếu chưa có giá trị từ recipes
        if (recipesCount.getText().toString().isEmpty() || recipesCount.getText().toString().equals("0")) {
            recipesCount.setText(String.valueOf(profile.getRecipesCount()));
        }

        int followers = profile.getFollowersCount();
        int following = profile.getFollowingCount();
        Log.d(TAG, "Displaying profile stats - followersCount: " + followers + ", followingCount: " + following);

        followersCount.setText(String.valueOf(followers));
        followingCount.setText(String.valueOf(following));

        // Hide follow button if own profile
        if (isOwnProfile) {
            followButton.setVisibility(View.GONE);
        } else {
            followButton.setVisibility(View.VISIBLE);
        }
    }

    private void checkFollowStatus() {
        if (isOwnProfile) {
            return;
        }

        databaseService.isFollowing(userId, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                isFollowing = snapshot.exists() && snapshot.getValue() != null;
                updateFollowButton();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error checking follow status", error.toException());
            }
        });
    }

    private void followUser() {
        followButton.setEnabled(false);
        databaseService.followUser(userId, new FirebaseDatabaseService.FollowCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    isFollowing = true;
                    updateFollowButton();
                    followButton.setEnabled(true);
                    Toast.makeText(UserProfileActivity.this, "Đã theo dõi", Toast.LENGTH_SHORT).show();

                    // Real-time listener will automatically update followers count
                    // No need to reload manually
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    followButton.setEnabled(true);
                    Toast.makeText(UserProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void unfollowUser() {
        followButton.setEnabled(false);
        databaseService.unfollowUser(userId, new FirebaseDatabaseService.FollowCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    isFollowing = false;
                    updateFollowButton();
                    followButton.setEnabled(true);
                    Toast.makeText(UserProfileActivity.this, "Đã bỏ theo dõi", Toast.LENGTH_SHORT).show();

                    // Real-time listener will automatically update followers count
                    // No need to reload manually
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    followButton.setEnabled(true);
                    Toast.makeText(UserProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Update recipesCount in database
     */
    private void updateRecipesCountInDatabase(int count) {
        databaseService.updateUserProfileField(userId, "recipesCount", count,
                new FirebaseDatabaseService.UserProfileCallback() {
                    @Override
                    public void onSuccess(UserProfile profile) {
                        Log.d(TAG, "Updated recipesCount to " + count);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Failed to update recipesCount: " + error);
                    }
                });
    }

    /**
     * Reload user profile to refresh stats (followers count, etc.)
     */
    private void reloadUserProfile() {
        Log.d(TAG, "Reloading user profile for userId: " + userId);
        // Force reload from Firebase by calling loadUserProfile
        // The existing observer will automatically update the UI
        profileViewModel.loadUserProfile(userId);
    }

    private void updateFollowButton() {
        if (followButton == null) {
            return;
        }

        if (isFollowing) {
            followButton.setText("Đang theo dõi");
            // Filled button style when following
            followButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white)));
            followButton.setTextColor(ContextCompat.getColor(this, R.color.primary_color));
        } else {
            followButton.setText("Theo dõi");
            // Outlined button style when not following
            followButton.setBackgroundTintList(android.content.res.ColorStateList
                    .valueOf(ContextCompat.getColor(this, android.R.color.transparent)));
            followButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        }
        followButton.setEnabled(true);
    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (recipesRecyclerView != null) {
            recipesRecyclerView.setVisibility(View.GONE);
        }
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (recipesRecyclerView != null) {
            recipesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.VISIBLE);
        }
        if (recipesRecyclerView != null) {
            recipesRecyclerView.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }
        if (recipesRecyclerView != null) {
            recipesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Setup real-time listener when activity starts
        setupRealtimeFollowersListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove real-time listener when activity stops to save resources
        removeRealtimeFollowersListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure listener is removed
        removeRealtimeFollowersListener();
    }

    /**
     * Setup real-time listener to monitor followersCount changes
     */
    private void setupRealtimeFollowersListener() {
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "Cannot setup listener: userId is null or empty");
            return;
        }

        // Remove existing listener if any
        removeRealtimeFollowersListener();

        followersCountRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("followersCount");

        followersCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object followersCountObj = snapshot.getValue();
                    int followersCountValue = 0;
                    if (followersCountObj instanceof Long) {
                        followersCountValue = ((Long) followersCountObj).intValue();
                    } else if (followersCountObj instanceof Integer) {
                        followersCountValue = (Integer) followersCountObj;
                    }

                    // Create final copy for use in lambda expression
                    final int finalFollowersCount = followersCountValue;

                    Log.d(TAG,
                            "Real-time followersCount changed to: " + finalFollowersCount + " for userId: " + userId);

                    // Update UI immediately
                    if (followersCount != null) {
                        runOnUiThread(() -> {
                            followersCount.setText(String.valueOf(finalFollowersCount));
                            Log.d(TAG, "Updated followersCount UI to: " + finalFollowersCount);
                            // Force refresh the view
                            followersCount.invalidate();
                            followersCount.requestLayout();
                        });
                    } else {
                        Log.w(TAG, "WARNING: followersCount TextView is null, cannot update UI");
                    }
                } else {
                    Log.w(TAG, "followersCount snapshot does not exist for userId: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error listening to followersCount: " + error.getMessage());
            }
        };

        followersCountRef.addValueEventListener(followersCountListener);
        Log.d(TAG, "Real-time followersCount listener attached to: users/" + userId + "/followersCount");

        // Force initial read to ensure UI is updated
        followersCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object followersCountObj = snapshot.getValue();
                    int followersCountValue = 0;
                    if (followersCountObj instanceof Long) {
                        followersCountValue = ((Long) followersCountObj).intValue();
                    } else if (followersCountObj instanceof Integer) {
                        followersCountValue = (Integer) followersCountObj;
                    }

                    // Create final copy for use in lambda expression
                    final int finalFollowersCount = followersCountValue;

                    Log.d(TAG, "Initial followersCount read: " + finalFollowersCount);
                    if (followersCount != null) {
                        runOnUiThread(() -> {
                            followersCount.setText(String.valueOf(finalFollowersCount));
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error reading initial followersCount", error.toException());
            }
        });
    }

    /**
     * Remove real-time listener
     */
    private void removeRealtimeFollowersListener() {
        if (followersCountRef != null && followersCountListener != null) {
            followersCountRef.removeEventListener(followersCountListener);
            Log.d(TAG, "Real-time followersCount listener removed");
            followersCountRef = null;
            followersCountListener = null;
        }
    }
}
