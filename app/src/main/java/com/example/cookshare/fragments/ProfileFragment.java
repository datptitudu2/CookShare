package com.example.cookshare.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cookshare.LoginActivity;
import com.example.cookshare.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;

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

        // Initialize profile functionality
        setupClickListeners(view);
        updateUserInfo(view);
    }

    private void setupClickListeners(View view) {
        // Logout button click listener
        MaterialCardView logoutCard = view.findViewById(R.id.logoutCard);
        logoutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        // Edit profile click listener
        MaterialCardView editProfileCard = view.findViewById(R.id.editProfileCard);
        editProfileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Chức năng chỉnh sửa hồ sơ sẽ được thêm sau", Toast.LENGTH_SHORT).show();
            }
        });

        // Favorites click listener
        MaterialCardView favoritesCard = view.findViewById(R.id.favoritesCard);
        favoritesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Chức năng yêu thích sẽ được thêm sau", Toast.LENGTH_SHORT).show();
            }
        });

        // My recipes click listener
        MaterialCardView myRecipesCard = view.findViewById(R.id.myRecipesCard);
        myRecipesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Chức năng công thức của tôi sẽ được thêm sau", Toast.LENGTH_SHORT).show();
            }
        });

        // Settings click listener
        MaterialCardView settingsCard = view.findViewById(R.id.settingsCard);
        settingsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Chức năng cài đặt sẽ được thêm sau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserInfo(View view) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Update user info in the UI
            // For now, we'll just show a toast
            Toast.makeText(getContext(), "Chào mừng " + currentUser.getDisplayName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    signOut();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void signOut() {
        mAuth.signOut();
        Toast.makeText(getContext(), "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
