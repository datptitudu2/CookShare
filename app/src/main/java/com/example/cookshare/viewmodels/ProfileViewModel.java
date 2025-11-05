package com.example.cookshare.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cookshare.models.Recipe;
import com.example.cookshare.models.UserProfile;
import com.example.cookshare.services.FirebaseDatabaseService;

import java.util.List;

/**
 * ViewModel for Profile functionality
 * Created for: Lê Hải An (Profile)
 */
public class ProfileViewModel extends ViewModel {

    private FirebaseDatabaseService firebaseService;
    private MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();
    private MutableLiveData<List<Recipe>> userRecipes = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProfileViewModel() {
        firebaseService = new FirebaseDatabaseService();
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public LiveData<List<Recipe>> getUserRecipes() {
        return userRecipes;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadUserProfile(String userId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        firebaseService.getUserProfile(userId, new FirebaseDatabaseService.UserProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                isLoading.setValue(false);
                userProfile.setValue(profile);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void loadUserRecipes(String userId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        firebaseService.getUserRecipes(userId, new FirebaseDatabaseService.RecipeListCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                isLoading.setValue(false);
                userRecipes.setValue(recipes);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void updateUserProfile(UserProfile profile) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        firebaseService.updateUserProfile(profile, new FirebaseDatabaseService.UserProfileCallback() {
            @Override
            public void onSuccess(UserProfile updatedProfile) {
                isLoading.setValue(false);
                userProfile.setValue(updatedProfile);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}
