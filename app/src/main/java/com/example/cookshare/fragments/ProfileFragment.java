package com.example.cookshare.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private ShapeableImageView profileAvatar;
    private View loadingContainer;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
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
        updateUserInfo();
    }

    private void initializeViews(View view) {
        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        profileAvatar = view.findViewById(R.id.profileAvatar);
        loadingContainer = view.findViewById(R.id.loadingContainer);
    }

    private void setupClickListeners(View view) {
        // Logout button click listener
        LinearLayout logoutCard = view.findViewById(R.id.logoutCard);
        if (logoutCard != null) {
            logoutCard.setOnClickListener(v -> showLogoutDialog());
        }

        // Edit profile click listener
        LinearLayout editProfileCard = view.findViewById(R.id.editProfileCard);
        if (editProfileCard != null) {
            editProfileCard.setOnClickListener(v -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Chức năng chỉnh sửa hồ sơ sẽ được thêm sau", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Favorites click listener
        LinearLayout favoritesCard = view.findViewById(R.id.favoritesCard);
        if (favoritesCard != null) {
            favoritesCard.setOnClickListener(v -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Chức năng yêu thích sẽ được thêm sau", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // My recipes click listener
        LinearLayout myRecipesCard = view.findViewById(R.id.myRecipesCard);
        if (myRecipesCard != null) {
            myRecipesCard.setOnClickListener(v -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Chức năng lịch sử nấu ăn sẽ được thêm sau", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Settings click listener
        LinearLayout settingsCard = view.findViewById(R.id.settingsCard);
        if (settingsCard != null) {
            settingsCard.setOnClickListener(v -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Chức năng thông báo sẽ được thêm sau", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Add Recipe button
        MaterialCardView addRecipeCard = view.findViewById(R.id.addRecipeCard);
        if (addRecipeCard != null) {
            addRecipeCard.setOnClickListener(v -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Chức năng thêm công thức sẽ được thêm sau", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Saved Recipes button
        MaterialCardView savedRecipesCard = view.findViewById(R.id.savedRecipesCard);
        if (savedRecipesCard != null) {
            savedRecipesCard.setOnClickListener(v -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Chức năng công thức đã lưu sẽ được thêm sau", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Achievements click listener
        LinearLayout achievementsCard = view.findViewById(R.id.achievementsCard);
        if (achievementsCard != null) {
            achievementsCard.setOnClickListener(v -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Chức năng thành tích sẽ được thêm sau", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // View all recipes
        TextView viewAllRecipes = view.findViewById(R.id.viewAllRecipes);
        if (viewAllRecipes != null) {
            viewAllRecipes.setOnClickListener(v -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Xem tất cả công thức", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Update user name
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty() && profileName != null) {
                profileName.setText(displayName);
            } else if (profileName != null) {
                profileName.setText("Người dùng");
            }

            // Update email
            String email = currentUser.getEmail();
            if (email != null && !email.isEmpty() && profileEmail != null) {
                profileEmail.setText(email);
            } else if (profileEmail != null) {
                profileEmail.setText("Đầu bếp nghiệp dư");
            }

            // Load profile photo
            loadProfilePhoto(currentUser);

            if (getContext() != null) {
                String welcomeMessage = displayName != null ? "Chào mừng " + displayName : "Chào mừng";
                Toast.makeText(getContext(), welcomeMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadProfilePhoto(FirebaseUser user) {
        if (user == null || profileAvatar == null || getContext() == null) {
            return;
        }

        Uri photoUrl = user.getPhotoUrl();

        if (photoUrl != null) {
            // Load ảnh từ Firebase Auth profile
            Glide.with(this)
                    .load(photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_default_avatar) // Ảnh mặc định khi đang tải
                    .error(R.drawable.ic_default_avatar) // Ảnh mặc định khi lỗi
                    .circleCrop() // Cắt thành hình tròn
                    .into(profileAvatar);
        } else {
            // Nếu không có ảnh, hiển thị ảnh mặc định
            Glide.with(this)
                    .load(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(profileAvatar);
        }
    }

    private void showLogoutDialog() {
        if (getContext() == null) return;

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

            // Redirect to LoginActivity
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
        // Clear references to prevent memory leaks
        profileName = null;
        profileEmail = null;
        profileAvatar = null;
        loadingContainer = null;
    }
}