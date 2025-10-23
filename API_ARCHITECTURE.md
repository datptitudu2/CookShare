# 🚀 **COOKSHARE API ARCHITECTURE**

## 📋 **Tổng quan hệ thống**

CookShare sử dụng **MVVM Architecture** với **Firebase Firestore** làm backend và **Repository Pattern** để quản lý data.

## 🏗️ **Architecture Overview**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Fragments     │    │   Activities    │    │   Adapters      │
│   (UI Layer)    │◄──►│   (UI Layer)    │◄──►│   (UI Layer)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   ViewModels    │◄──►│   ViewModels    │◄──►│   ViewModels    │
│   (Logic Layer) │    │   (Logic Layer) │    │   (Logic Layer) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Repositories   │◄──►│  Repositories   │◄──►│  Repositories   │
│  (Data Layer)   │    │  (Data Layer)   │    │  (Data Layer)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ FirebaseService │◄──►│ FirebaseService │◄──►│ FirebaseService │
│   (API Layer)   │    │   (API Layer)   │    │   (API Layer)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Firebase Firestore│◄──►│ Firebase Auth  │◄──►│ Firebase Storage│
│   (Database)    │    │ (Authentication)│    │   (Files)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📦 **Dependencies đã thêm**

```kotlin
// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Architecture Components
implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.7.0")

// Image Loading
implementation("com.github.bumptech.glide:glide:4.16.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## 🗂️ **File Structure**

```
app/src/main/java/com/example/cookshare/
├── models/
│   ├── Recipe.java           # Recipe data model
│   ├── User.java             # User data model
│   └── ApiResponse.java      # Generic API response
├── services/
│   └── FirebaseService.java  # Firebase API calls
├── repositories/
│   ├── RecipeRepository.java # Recipe data management
│   └── UserRepository.java   # User data management
├── viewmodels/
│   ├── RecipeViewModel.java  # Recipe business logic
│   └── UserViewModel.java    # User business logic
└── utils/
    └── NetworkUtils.java     # Network utilities
```

## 🔥 **Firebase Collections**

### **Recipes Collection**
```json
{
  "id": "recipe_id",
  "title": "Phở Bò",
  "description": "Món phở truyền thống Việt Nam",
  "imageUrl": "https://...",
  "authorId": "user_id",
  "authorName": "Nguyễn Văn A",
  "authorAvatar": "https://...",
  "ingredients": ["Bánh phở", "Thịt bò", "Hành tây"],
  "instructions": ["Bước 1", "Bước 2"],
  "prepTime": 30,
  "cookTime": 60,
  "servings": 4,
  "difficulty": "Medium",
  "categories": ["Vietnamese", "Soup"],
  "tags": ["traditional", "beef"],
  "rating": 4.5,
  "ratingCount": 120,
  "viewCount": 1500,
  "likeCount": 89,
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z",
  "isPublished": true
}
```

### **Users Collection**
```json
{
  "id": "user_id",
  "email": "user@example.com",
  "displayName": "Nguyễn Văn A",
  "photoUrl": "https://...",
  "bio": "Đầu bếp tại nhà",
  "location": "Hà Nội",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z",
  "isVerified": false,
  "recipeCount": 12,
  "followerCount": 156,
  "followingCount": 89,
  "favoriteRecipeIds": ["recipe1", "recipe2"],
  "followingUserIds": ["user1", "user2"],
  "followerUserIds": ["user3", "user4"]
}
```

## 🔧 **API Methods**

### **RecipeRepository Methods**
```java
// Load data
void loadAllRecipes()
void loadRecipesByAuthor(String authorId)
void loadRecipe(String recipeId)
void searchRecipes(String query)

// CRUD operations
void createRecipe(Recipe recipe)
void updateRecipe(String recipeId, Recipe recipe)
void deleteRecipe(String recipeId)

// Interactions
void incrementViewCount(String recipeId)
void updateLikeCount(String recipeId, boolean isLiked)

// Error handling
void clearError()
```

### **UserRepository Methods**
```java
// Load data
void loadUser(String userId)

// CRUD operations
void saveUser(User user)

// Error handling
void clearError()
```

## 📱 **Usage trong Fragments**

### **HomeFragment Example**
```java
public class HomeFragment extends Fragment {
    private RecipeViewModel recipeViewModel;
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        
        // Observe data
        recipeViewModel.getRecipesLiveData().observe(getViewLifecycleOwner(), recipes -> {
            // Update UI with recipes
            updateRecipesList(recipes);
        });
        
        recipeViewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            // Show/hide loading indicator
            showLoading(isLoading);
        });
        
        recipeViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            // Show error message
            showError(error);
        });
        
        // Load data
        recipeViewModel.loadAllRecipes();
    }
}
```

## 🚨 **Error Handling**

### **Network Errors**
- **No Internet**: "Không có kết nối internet"
- **Server Error**: "Lỗi kết nối server"
- **Timeout**: "Kết nối quá chậm"
- **Generic**: "Đã xảy ra lỗi"

### **Firebase Errors**
- **Permission Denied**: "Không có quyền truy cập"
- **Document Not Found**: "Không tìm thấy dữ liệu"
- **Invalid Data**: "Dữ liệu không hợp lệ"

## 🔄 **Data Flow**

1. **Fragment** calls ViewModel method
2. **ViewModel** calls Repository method
3. **Repository** calls FirebaseService method
4. **FirebaseService** makes Firebase API call
5. **Response** flows back through the chain
6. **LiveData** updates UI automatically

## 🎯 **Next Steps**

1. **Integrate ViewModels** vào các Fragments
2. **Add RecyclerView** cho recipe lists
3. **Implement search** functionality
4. **Add image upload** với Firebase Storage
5. **Add offline support** với Room database
6. **Add push notifications** với FCM

---

**Hệ thống API đã sẵn sàng cho development! 🚀**
