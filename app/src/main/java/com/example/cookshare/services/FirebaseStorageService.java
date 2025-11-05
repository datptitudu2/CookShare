package com.example.cookshare.services;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

/**
 * Service để xử lý upload ảnh lên Firebase Storage
 * Trả về download URL để lưu vào Realtime Database
 */
public class FirebaseStorageService {
    private static final String TAG = "FirebaseStorageService";
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Storage paths
    private static final String RECIPE_IMAGES_PATH = "recipe_images";
    private static final String PROFILE_AVATARS_PATH = "profile_avatars";

    public FirebaseStorageService() {
        // Khởi tạo Firebase Storage với bucket URL cụ thể từ google-services.json
        // Bucket: cookshare-88d53.firebasestorage.app
        storage = FirebaseStorage.getInstance();
        // Đảm bảo sử dụng default bucket
        storageRef = storage.getReference();

        // Log để debug
        Log.d(TAG, "FirebaseStorage initialized. Bucket: " + storage.getApp().getOptions().getStorageBucket());
    }

    /**
     * Upload ảnh công thức lên Firebase Storage
     * 
     * @param imageUri URI của ảnh từ thiết bị
     * @param callback Callback để nhận kết quả (download URL hoặc error)
     */
    public void uploadRecipeImage(Uri imageUri, ImageUploadCallback callback) {
        if (imageUri == null) {
            if (callback != null) {
                callback.onError("Image URI is null");
            }
            return;
        }

        // Tạo unique filename
        String fileName = RECIPE_IMAGES_PATH + "/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        // Upload ảnh
        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Lấy download URL sau khi upload thành công
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                Log.d(TAG, "Recipe image uploaded successfully: " + downloadUrl);
                if (callback != null) {
                    callback.onSuccess(downloadUrl);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get download URL", e);
                if (callback != null) {
                    callback.onError("Failed to get download URL: " + e.getMessage());
                }
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to upload recipe image", e);
            if (callback != null) {
                callback.onError("Failed to upload image: " + e.getMessage());
            }
        }).addOnProgressListener(snapshot -> {
            // Có thể dùng để hiển thị progress bar
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            Log.d(TAG, "Upload progress: " + progress + "%");
        });
    }

    /**
     * Upload avatar profile lên Firebase Storage
     * 
     * @param imageUri URI của ảnh từ thiết bị
     * @param userId   ID của user (để đặt tên file)
     * @param callback Callback để nhận kết quả (download URL hoặc error)
     */
    public void uploadProfileAvatar(Uri imageUri, String userId, ImageUploadCallback callback) {
        if (imageUri == null) {
            if (callback != null) {
                callback.onError("Image URI is null");
            }
            return;
        }

        if (userId == null || userId.isEmpty()) {
            if (callback != null) {
                callback.onError("User ID is null");
            }
            return;
        }

        // Tạo filename từ userId (mỗi user chỉ có 1 avatar)
        String fileName = PROFILE_AVATARS_PATH + "/" + userId + ".jpg";
        StorageReference avatarRef = storageRef.child(fileName);

        Log.d(TAG, "Uploading avatar to: " + fileName);
        Log.d(TAG, "Image URI: " + imageUri.toString());
        Log.d(TAG, "Storage bucket: " + storage.getApp().getOptions().getStorageBucket());

        // Upload ảnh
        UploadTask uploadTask = avatarRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Lấy download URL sau khi upload thành công
            avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                Log.d(TAG, "Profile avatar uploaded successfully: " + downloadUrl);
                if (callback != null) {
                    callback.onSuccess(downloadUrl);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get download URL", e);
                if (callback != null) {
                    callback.onError("Failed to get download URL: " + e.getMessage());
                }
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to upload profile avatar", e);
            Log.e(TAG, "Error message: " + e.getMessage());
            Log.e(TAG, "Exception type: " + e.getClass().getName());
            if (callback != null) {
                String errorMsg = e.getMessage();
                // Thêm thông tin chi tiết hơn nếu có
                if (errorMsg != null && errorMsg.contains("Object does not exist")) {
                    errorMsg = "Lỗi: Firebase Storage chưa được bật hoặc bucket không tồn tại.\n" +
                            "Vui lòng kiểm tra Firebase Console: Storage > Get Started";
                } else if (errorMsg == null || errorMsg.isEmpty()) {
                    errorMsg = "Không thể upload ảnh. Vui lòng thử lại.";
                }
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Xóa ảnh khỏi Firebase Storage
     * 
     * @param imageUrl Download URL của ảnh cần xóa
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
            imageRef.delete().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Image deleted successfully");
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to delete image", e);
            });
        } catch (Exception e) {
            Log.e(TAG, "Invalid image URL: " + imageUrl, e);
        }
    }

    /**
     * Callback interface cho upload ảnh
     */
    public interface ImageUploadCallback {
        void onSuccess(String downloadUrl);

        void onError(String error);
    }
}
