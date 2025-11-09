package com.example.cookshare.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import com.example.cookshare.models.UserProfile;
import com.example.cookshare.viewmodels.ProfileViewModel;
import com.example.cookshare.adapters.EditProfileActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.cookshare.CookingHistoryActivity;
import com.example.cookshare.FavoriteRecipesActivity;
import com.example.cookshare.LoginActivity;
import com.example.cookshare.MainActivity;
import com.example.cookshare.MyRecipesActivity;
import com.example.cookshare.NotificationsActivity;
import com.example.cookshare.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private TextView profileName;
    private TextView profileEmail;
    private TextView recipeCount, likeCount;
    private TextView statDays, statFollowers, statFollowing;

    private ImageView profileAvatar;
    private ProfileViewModel viewModel;
    private UserProfile currentUserProfile;
    private ActivityResultLauncher<Intent> editProfileLauncher;
    private View loadingContainer;
    private LinearLayout editProfileCard;

    // Real-time listener for followersCount
    private DatabaseReference followersCountRef;
    private ValueEventListener followersCountListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Người dùng đã lưu thành công, tải lại dữ liệu
                        Toast.makeText(getContext(), "Đang cập nhật hồ sơ...", Toast.LENGTH_SHORT).show();
                        loadDataFromViewModel();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews(view);

        // Initialize profile functionality
        setupClickListeners(view);

        // Setup Observers để lắng nghe dữ liệu từ ViewModel
        setupObservers();

        // Tải dữ liệu
        loadDataFromViewModel();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Setup real-time listener for followersCount
        setupRealtimeFollowersListener();
        // Reload data when user comes back to this tab
        Log.d("ProfileFragment", "onStart() - Reloading profile data");
        loadDataFromViewModel();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove real-time listener to save resources
        removeRealtimeFollowersListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data when user comes back to this tab
        // This ensures recipesCount and other stats are updated after edit/delete
        Log.d("ProfileFragment", "onResume() - Reloading profile data immediately");
        // Reload immediately, then reload again after delay to catch any Firebase sync
        // delays
        loadDataFromViewModel();
        // Also reload after a delay to ensure Firebase has synced if user just
        // edited/deleted a recipe
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            Log.d("ProfileFragment", "onResume() - Reloading profile data after delay for Firebase sync");
            loadDataFromViewModel();
        }, 500); // 500ms delay to ensure Firebase sync
    }

    private void initializeViews(View view) {
        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        profileAvatar = view.findViewById(R.id.profileAvatar);
        loadingContainer = view.findViewById(R.id.loadingContainer);
        editProfileCard = view.findViewById(R.id.editProfileCard);

        // Thêm các view khác từ layout
        recipeCount = view.findViewById(R.id.recipeCount);
        likeCount = view.findViewById(R.id.likeCount);
        statDays = view.findViewById(R.id.statDays);
        statFollowers = view.findViewById(R.id.statFollowers);
        statFollowing = view.findViewById(R.id.statFollowing);
    }

    private void loadDataFromViewModel() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d("ProfileFragment", "Loading profile data for userId: " + userId);
            // Gọi ViewModel tải profile VÀ recipes
            viewModel.loadUserProfile(userId);
            viewModel.loadUserRecipes(userId);
        } else {
            Log.w("ProfileFragment", "Current user is null, cannot load profile");
        }
    }

    private void setupObservers() {
        // Quan sát trạng thái loading
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                loadingContainer.setVisibility(View.VISIBLE);
            } else {
                loadingContainer.setVisibility(View.GONE);
            }
        });

        // Quan sát lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Quan sát user profile
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                Log.d("ProfileFragment", "Profile data updated - followersCount: " + profile.getFollowersCount()
                        + ", followingCount: " + profile.getFollowingCount());
                this.currentUserProfile = profile;
                updateProfileUI(profile);
            } else {
                Log.w("ProfileFragment", "Profile data is null");
            }
        });

        // Note: Số công thức hiển thị từ profile.getRecipesCount() thay vì đếm recipes
        // vì getUserRecipes() trả về TẤT CẢ recipes trong app, không phải của user
    }

    private void updateProfileUI(UserProfile profile) {
        if (profileName != null) {
            profileName.setText(profile.getFullName() != null ? profile.getFullName() : "Người dùng");
        }

        if (profileEmail != null) {
            profileEmail.setText(profile.getEmail() != null ? profile.getEmail() : "Không có email");
        }

        // Cập nhật các thống kê
        if (recipeCount != null) {
            recipeCount.setText(profile.getRecipesCount() + " công thức");
        }

        if (likeCount != null) {
            likeCount.setText(profile.getFavoritesCount() + " yêu thích");
        }

        // Cập nhật stats lớn (24, 156, 89)
        if (statDays != null) {
            statDays.setText(String.valueOf(profile.getRecipesCount()));
        }

        if (statFollowers != null) {
            int followersCount = profile.getFollowersCount();
            statFollowers.setText(String.valueOf(followersCount));
            Log.d("ProfileFragment", "Updated statFollowers UI: " + followersCount);
        }

        if (statFollowing != null) {
            int followingCount = profile.getFollowingCount();
            statFollowing.setText(String.valueOf(followingCount));
            Log.d("ProfileFragment", "Updated statFollowing UI: " + followingCount);
        }

        // Load ảnh đại diện
        if (getContext() != null && profileAvatar != null) {
            Glide.with(this)
                    .load(profile.getAvatarUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .circleCrop()
                    .into(profileAvatar);
        }
    }

    private void setupClickListeners(View view) {
        // Thêm công thức - Navigate đến AddRecipeFragment
        com.google.android.material.card.MaterialCardView addRecipeCard = view.findViewById(R.id.addRecipeCard);
        if (addRecipeCard != null) {
            addRecipeCard.setOnClickListener(v -> {
                Log.d("ProfileFragment", "Add recipe card clicked");
                if (getActivity() != null && getActivity() instanceof MainActivity && isAdded()) {
                    try {
                        Log.d("ProfileFragment", "Navigating to AddRecipeFragment");
                        ((MainActivity) getActivity()).navigateToAddRecipe();
                    } catch (Exception e) {
                        Log.e("ProfileFragment", "Error navigating to AddRecipeFragment", e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Không thể chuyển đến trang thêm công thức",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.e("ProfileFragment",
                            "Cannot navigate: getActivity()=" + getActivity() + ", isAdded()=" + isAdded());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không thể chuyển đến trang thêm công thức", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });
        } else {
            Log.e("ProfileFragment", "addRecipeCard is null!");
        }

        // Công thức của tôi - Navigate đến MyRecipesActivity
        com.google.android.material.card.MaterialCardView savedRecipesCard = view.findViewById(R.id.savedRecipesCard);
        if (savedRecipesCard != null) {
            savedRecipesCard.setOnClickListener(v -> {
                Log.d("ProfileFragment", "My recipes card clicked");
                if (getContext() != null && isAdded()) {
                    try {
                        Log.d("ProfileFragment", "Creating intent for MyRecipesActivity");
                        Intent intent = new Intent(getContext(), MyRecipesActivity.class);
                        Log.d("ProfileFragment", "Starting MyRecipesActivity");
                        startActivity(intent);
                        Log.d("ProfileFragment", "MyRecipesActivity started successfully");
                    } catch (Exception e) {
                        Log.e("ProfileFragment", "Error navigating to MyRecipesActivity", e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Log.e("ProfileFragment",
                            "Cannot navigate: getContext()=" + getContext() + ", isAdded()=" + isAdded());
                }
            });
        } else {
            Log.e("ProfileFragment", "savedRecipesCard is null!");
        }

        // Logout
        LinearLayout logoutCard = view.findViewById(R.id.logoutCard);
        if (logoutCard != null) {
            logoutCard.setOnClickListener(v -> showLogoutDialog());
        }

        // Edit profile - CODE MỚI CÓ LAUNCH ACTIVITY
        LinearLayout editProfileCard = view.findViewById(R.id.editProfileCard);
        if (editProfileCard != null) {
            editProfileCard.setOnClickListener(v -> {
                Log.d("ProfileFragment", "Edit profile clicked");
                Log.d("ProfileFragment", "currentUserProfile: "
                        + (currentUserProfile != null ? currentUserProfile.getFullName() : "NULL"));

                if (currentUserProfile != null) {
                    Log.d("ProfileFragment", "Launching EditProfileActivity");
                    Intent intent = new Intent(getContext(), EditProfileActivity.class);
                    intent.putExtra(EditProfileActivity.EXTRA_USER_PROFILE, currentUserProfile);
                    editProfileLauncher.launch(intent);
                } else {
                    Log.d("ProfileFragment", "currentUserProfile is null!");
                    Toast.makeText(getContext(), "Đang tải dữ liệu hồ sơ, vui lòng thử lại sau", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        } else {
            Log.e("ProfileFragment", "editProfileCard NOT FOUND!");
        }

        // Favorites
        LinearLayout favoritesCard = view.findViewById(R.id.favoritesCard);
        if (favoritesCard != null) {
            favoritesCard.setOnClickListener(v -> {
                Log.d("ProfileFragment", "Favorites card clicked");
                if (getContext() != null && isAdded()) {
                    try {
                        Log.d("ProfileFragment", "Creating intent for FavoriteRecipesActivity");
                        Intent intent = new Intent(getContext(), FavoriteRecipesActivity.class);
                        Log.d("ProfileFragment", "Starting FavoriteRecipesActivity");
                        startActivity(intent);
                        Log.d("ProfileFragment", "FavoriteRecipesActivity started successfully");
                    } catch (Exception e) {
                        Log.e("ProfileFragment", "Error navigating to FavoriteRecipesActivity", e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Log.e("ProfileFragment",
                            "Cannot navigate: getContext()=" + getContext() + ", isAdded()=" + isAdded());
                }
            });
        } else {
            Log.e("ProfileFragment", "favoritesCard is null!");
        }

        // Lịch sử nấu ăn - Navigate đến CookingHistoryActivity
        LinearLayout myRecipesCard = view.findViewById(R.id.myRecipesCard);
        if (myRecipesCard != null) {
            myRecipesCard.setOnClickListener(v -> {
                Log.d("ProfileFragment", "Cooking history card clicked");
                if (getContext() != null && isAdded()) {
                    try {
                        Log.d("ProfileFragment", "Creating intent for CookingHistoryActivity");
                        Intent intent = new Intent(getContext(), CookingHistoryActivity.class);
                        Log.d("ProfileFragment", "Starting CookingHistoryActivity");
                        startActivity(intent);
                        Log.d("ProfileFragment", "CookingHistoryActivity started successfully");
                    } catch (Exception e) {
                        Log.e("ProfileFragment", "Error navigating to CookingHistoryActivity", e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Log.e("ProfileFragment",
                            "Cannot navigate: getContext()=" + getContext() + ", isAdded()=" + isAdded());
                }
            });
        } else {
            Log.e("ProfileFragment", "myRecipesCard is null!");
        }

        // Thông báo - Navigate đến NotificationsActivity
        LinearLayout settingsCard = view.findViewById(R.id.settingsCard);
        if (settingsCard != null) {
            settingsCard.setOnClickListener(v -> {
                Log.d("ProfileFragment", "Notifications card clicked");
                if (getContext() != null && isAdded()) {
                    try {
                        Log.d("ProfileFragment", "Creating intent for NotificationsActivity");
                        Intent intent = new Intent(getContext(), NotificationsActivity.class);
                        Log.d("ProfileFragment", "Starting NotificationsActivity");
                        startActivity(intent);
                        Log.d("ProfileFragment", "NotificationsActivity started successfully");
                    } catch (Exception e) {
                        Log.e("ProfileFragment", "Error navigating to NotificationsActivity", e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Log.e("ProfileFragment",
                            "Cannot navigate: getContext()=" + getContext() + ", isAdded()=" + isAdded());
                }
            });
        } else {
            Log.e("ProfileFragment", "settingsCard is null!");
        }
    }

    private void showLogoutDialog() {
        if (getContext() == null)
            return;

        new AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> signOut())
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void signOut() {
        mAuth.signOut();

        if (getContext() != null) {
            Toast.makeText(getContext(), "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cleanup listener
        removeRealtimeFollowersListener();
        profileName = null;
        profileEmail = null;
        profileAvatar = null;
        loadingContainer = null;
    }

    /**
     * Setup real-time listener to monitor followersCount changes
     */
    private void setupRealtimeFollowersListener() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String userId = currentUser.getUid();
        followersCountRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("followersCount");

        followersCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object followersCountObj = snapshot.getValue();
                    int followersCount = 0;
                    if (followersCountObj instanceof Long) {
                        followersCount = ((Long) followersCountObj).intValue();
                    } else if (followersCountObj instanceof Integer) {
                        followersCount = (Integer) followersCountObj;
                    }

                    // Create final copy for use in lambda expression
                    final int finalFollowersCount = followersCount;

                    Log.d("ProfileFragment", "=== REAL-TIME FOLLOWERS COUNT CHANGED ===");
                    Log.d("ProfileFragment", "New followersCount: " + finalFollowersCount + " for userId: " + userId);

                    // Update UI immediately - ensure we're on UI thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (getView() != null && statFollowers != null) {
                                statFollowers.setText(String.valueOf(finalFollowersCount));
                                Log.d("ProfileFragment",
                                        "SUCCESS: Updated statFollowers UI to: " + finalFollowersCount);
                                // Force refresh the view
                                statFollowers.invalidate();
                                statFollowers.requestLayout();
                            } else {
                                Log.w("ProfileFragment",
                                        "WARNING: Cannot update UI - getView() or statFollowers is null");
                            }
                        });
                    } else {
                        Log.w("ProfileFragment", "WARNING: getActivity() is null, cannot update UI");
                    }

                    // Also update the profile object to keep it in sync
                    if (currentUserProfile != null) {
                        currentUserProfile.setFollowersCount(finalFollowersCount);
                        Log.d("ProfileFragment",
                                "Updated currentUserProfile.followersCount to: " + finalFollowersCount);
                    } else {
                        Log.w("ProfileFragment", "WARNING: currentUserProfile is null");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Error listening to followersCount: " + error.getMessage());
            }
        };

        followersCountRef.addValueEventListener(followersCountListener);
        Log.d("ProfileFragment", "Real-time followersCount listener attached to: users/" + userId + "/followersCount");

        // Force initial read to ensure UI is updated
        followersCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object followersCountObj = snapshot.getValue();
                    int followersCount = 0;
                    if (followersCountObj instanceof Long) {
                        followersCount = ((Long) followersCountObj).intValue();
                    } else if (followersCountObj instanceof Integer) {
                        followersCount = (Integer) followersCountObj;
                    }
                    final int finalFollowersCountInit = followersCount;
                    Log.d("ProfileFragment", "Initial followersCount read: " + finalFollowersCountInit);
                    if (statFollowers != null) {
                        statFollowers.setText(String.valueOf(finalFollowersCountInit));
                    }
                    if (currentUserProfile != null) {
                        currentUserProfile.setFollowersCount(finalFollowersCountInit);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Error reading initial followersCount", error.toException());
            }
        });
    }

    /**
     * Remove real-time listener
     */
    private void removeRealtimeFollowersListener() {
        if (followersCountRef != null && followersCountListener != null) {
            followersCountRef.removeEventListener(followersCountListener);
            Log.d("ProfileFragment", "Real-time followersCount listener removed");
            followersCountRef = null;
            followersCountListener = null;
        }
    }
}