package com.example.cookshare.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cookshare.models.Recipe;
import com.example.cookshare.services.FirebaseRealtimeService;

import java.util.List;

/**
 * ViewModel for Search functionality
 * Created for: Mai Đình Phúc (Search + Home Support)
 */
public class SearchViewModel extends ViewModel {

    private FirebaseRealtimeService firebaseService;
    private MutableLiveData<List<Recipe>> searchResults = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public SearchViewModel() {
        firebaseService = new FirebaseRealtimeService();
    }

    public LiveData<List<Recipe>> getSearchResults() {
        return searchResults;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void searchRecipes(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchResults.setValue(null);
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        firebaseService.searchRecipes(query, new FirebaseRealtimeService.SearchCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                isLoading.setValue(false);
                searchResults.setValue(recipes);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void clearSearch() {
        searchResults.setValue(null);
        errorMessage.setValue(null);
    }
}
