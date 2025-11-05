package com.example.cookshare.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.cookshare.R;
import com.example.cookshare.models.UserProfile;
import com.example.cookshare.services.FirebaseStorageService;
import com.example.cookshare.viewmodels.ProfileViewModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

public class EditProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_PROFILE = "USER_PROFILE";

    private Toolbar toolbar;
    private ShapeableImageView editAvatar;
    private EditText etFullName, etBio, etPhone, etEmail;
    private Spinner spinnerGender;
    private Button btnSaveChanges;
    private ProgressBar progressBar;

    private ProfileViewModel viewModel;
    private UserProfile currentUserProfile;
    private ArrayAdapter<String> genderAdapter;

    // Image upload
    private Uri selectedAvatarUri;
    private FirebaseStorageService storageService;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        storageService = new FirebaseStorageService();

        // Setup image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            selectedAvatarUri = imageUri;
                            // Hiển thị ảnh đã chọn
                            Glide.with(this)
                                    .load(imageUri)
                                    .circleCrop()
                                    .into(editAvatar);
                        }
                    }
                });

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        initializeViews();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (getIntent().hasExtra(EXTRA_USER_PROFILE)) {
            currentUserProfile = (UserProfile) getIntent().getSerializableExtra(EXTRA_USER_PROFILE);
            if (currentUserProfile != null) {
                populateData();
            } else {
                Toast.makeText(this, "Không thể tải thông tin hồ sơ", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Thiếu thông tin hồ sơ", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
        setupObservers();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        editAvatar = findViewById(R.id.editAvatar);
        etFullName = findViewById(R.id.etFullName);
        etBio = findViewById(R.id.etBio);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        progressBar = findViewById(R.id.progressBar);
    }

    private void populateData() {
        etFullName.setText(currentUserProfile.getFullName());
        etBio.setText(currentUserProfile.getBio());
        etPhone.setText(currentUserProfile.getPhoneNumber());
        etEmail.setText(currentUserProfile.getEmail());

        Glide.with(this)
                .load(currentUserProfile.getAvatarUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .circleCrop()
                .into(editAvatar);

        String[] genders = { "Nam", "Nữ", "Khác" };
        genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        if (currentUserProfile.getGender() != null) {
            int spinnerPosition = genderAdapter.getPosition(currentUserProfile.getGender());
            spinnerGender.setSelection(spinnerPosition);
        }

        // Setup click listener cho avatar và camera icon để chọn ảnh mới
        editAvatar.setOnClickListener(v -> openImagePicker());

        ImageView cameraIcon = findViewById(R.id.cameraIcon);
        if (cameraIcon != null) {
            cameraIcon.setOnClickListener(v -> openImagePicker());
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void saveProfileChanges() {
        String newFullName = etFullName.getText().toString().trim();
        String newBio = etBio.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();
        String newGender = spinnerGender.getSelectedItem().toString();

        if (newFullName.isEmpty()) {
            etFullName.setError("Tên không được để trống");
            return;
        }

        currentUserProfile.setFullName(newFullName);
        currentUserProfile.setBio(newBio);
        currentUserProfile.setPhoneNumber(newPhone);
        currentUserProfile.setGender(newGender);

        // Nếu có ảnh avatar mới được chọn, upload lên Firebase Storage trước
        if (selectedAvatarUri != null) {
            uploadAvatarAndUpdateProfile();
        } else {
            // Không có ảnh mới, cập nhật profile trực tiếp
            viewModel.updateUserProfile(currentUserProfile);
        }
    }

    private void uploadAvatarAndUpdateProfile() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnSaveChanges.setEnabled(false);

        storageService.uploadProfileAvatar(selectedAvatarUri, userId, new FirebaseStorageService.ImageUploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                // Upload avatar thành công, cập nhật avatarUrl và save profile
                currentUserProfile.setAvatarUrl(downloadUrl);
                viewModel.updateUserProfile(currentUserProfile);
            }

            @Override
            public void onError(String error) {
                // Upload avatar thất bại, vẫn cập nhật profile (giữ avatar cũ)
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSaveChanges.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this,
                            "Lỗi upload ảnh đại diện: " + error + ". Đang lưu thông tin khác...", Toast.LENGTH_LONG)
                            .show();
                    // Vẫn cập nhật profile với avatar cũ
                    viewModel.updateUserProfile(currentUserProfile);
                });
            }
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                btnSaveChanges.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                btnSaveChanges.setEnabled(true);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUserProfile().observe(this, updatedProfile -> {
            if (updatedProfile != null && updatedProfile.getId().equals(currentUserProfile.getId())
                    && progressBar.getVisibility() == View.GONE) {
                Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
