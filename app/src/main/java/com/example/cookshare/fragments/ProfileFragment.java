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
import com.example.cookshare.LoginActivity;
import com.example.cookshare.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
            // Gọi ViewModel tải profile VÀ recipes
            viewModel.loadUserProfile(userId);
            viewModel.loadUserRecipes(userId);
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
                this.currentUserProfile = profile;
                updateProfileUI(profile);
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
            statFollowers.setText(String.valueOf(profile.getFollowersCount()));
        }

        if (statFollowing != null) {
            statFollowing.setText(String.valueOf(profile.getFollowingCount()));
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
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Chức năng yêu thích sẽ được thêm sau", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // My recipes
        LinearLayout myRecipesCard = view.findViewById(R.id.myRecipesCard);
        if (myRecipesCard != null) {
            myRecipesCard.setOnClickListener(v -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Chức năng lịch sử nấu ăn sẽ được thêm sau", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }

        // Settings
        LinearLayout settingsCard = view.findViewById(R.id.settingsCard);
        if (settingsCard != null) {
            settingsCard.setOnClickListener(v -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Chức năng thông báo sẽ được thêm sau", Toast.LENGTH_SHORT).show();
                }
            });
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
        profileName = null;
        profileEmail = null;
        profileAvatar = null;
        loadingContainer = null;
    }
}
