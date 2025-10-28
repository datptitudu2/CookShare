package com.example.cookshare.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cookshare.models.Recipe;
import com.example.cookshare.services.FirebaseDatabaseService;

/**
 * ViewModel for Add Recipe functionality
 * Created for: Mai Trung Hiếu (Add Recipe)
 */
public class AddRecipeViewModel extends ViewModel {

    private FirebaseDatabaseService firebaseService;
    private MutableLiveData<Boolean> isUploading = new MutableLiveData<>();
    private MutableLiveData<String> uploadMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> uploadSuccess = new MutableLiveData<>();

    public AddRecipeViewModel() {
        firebaseService = new FirebaseDatabaseService();
    }

    public LiveData<Boolean> getIsUploading() {
        return isUploading;
    }

    public LiveData<String> getUploadMessage() {
        return uploadMessage;
    }

    public LiveData<Boolean> getUploadSuccess() {
        return uploadSuccess;
    }

    public void createRecipe(Recipe recipe) {
        isUploading.setValue(true);
        uploadMessage.setValue("Đang tạo công thức...");
        uploadSuccess.setValue(false);

        firebaseService.createRecipe(recipe, new FirebaseDatabaseService.RecipeCallback() {
            @Override
            public void onSuccess(String recipeId) {
                isUploading.setValue(false);
                uploadMessage.setValue("Tạo công thức thành công!");
                uploadSuccess.setValue(true);
            }

            @Override
            public void onError(String error) {
                isUploading.setValue(false);
                uploadMessage.setValue("Lỗi: " + error);
                uploadSuccess.setValue(false);
            }
        });
    }

    public void resetUploadState() {
        uploadSuccess.setValue(false);
        uploadMessage.setValue(null);
    }
}
