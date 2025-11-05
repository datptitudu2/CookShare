# HƯỚNG DẪN CODE CHI TIẾT - COOKSHARE APP

## 1. MAINACTIVITY.JAVA - FILE CHÍNH (Entry Point)

### **Vai trò:**
- Đây là Activity chính khi user đăng nhập thành công
- Quản lý Bottom Navigation (Home, Search, Chatbot, Add Recipe, Profile)
- Container để hiển thị các Fragment

### **Các thành phần chính:**

#### **1.1. Biến quan trọng:**
```java
private FirebaseAuth mAuth;  // Quản lý authentication
private BottomNavigationView bottomNavigation;  // Thanh điều hướng dưới cùng
private View fragmentContainer;  // Nơi chứa các Fragment
private int lastSelectedTabId;  // Lưu tab đã chọn trước đó
```

#### **1.2. onCreate():**
- Khởi tạo Firebase Auth
- Setup Toolbar (thanh trên cùng)
- Setup Bottom Navigation
- Kiểm tra user đã đăng nhập chưa
- Khởi tạo foods default values (chỉ 1 lần)

#### **1.3. setupBottomNavigation():**
```java
// Khi user click vào tab:
- Home: Tạo HomeFragment() mới
- Search: Tạo SearchFragment() mới  
- Chatbot: Mở ChatbotActivity (Activity riêng, không phải Fragment)
- Add: Tạo AddRecipeFragment() mới
- Profile: Tạo ProfileFragment() mới
```

**Lưu ý quan trọng:**
- Mỗi lần click tab, tạo Fragment MỚI (new HomeFragment())
- Dùng `replace()` không phải `add()` để thay thế Fragment cũ
- Có delay 200ms để animation mượt

#### **1.4. checkUserSignIn():**
- Nếu chưa đăng nhập → Chuyển về LoginActivity
- Nếu đã đăng nhập → Hiển thị welcome message

#### **1.5. initializeFoodsDefaultValuesOnce():**
- Dùng SharedPreferences để chỉ chạy 1 lần
- Khởi tạo giá trị mặc định cho foods trong Firebase

---

## 2. CẤU TRÚC DỰ ÁN TỔNG QUAN

### **2.1. Fragments (Màn hình con):**
- `HomeFragment`: Trang chủ, hiển thị danh sách món ăn
- `SearchFragment`: Tìm kiếm món ăn
- `AddRecipeFragment`: Form đăng công thức mới
- `ProfileFragment`: Hồ sơ người dùng

### **2.2. Activities (Màn hình đầy đủ):**
- `LoginActivity`: Đăng nhập
- `RegisterActivity`: Đăng ký
- `RecipeDetailActivity`: Chi tiết công thức
- `MyRecipesActivity`: Công thức của tôi
- `UserProfileActivity`: Hồ sơ user khác
- `ChatbotActivity`: Trợ lý AI
- `FavoriteRecipesActivity`: Món yêu thích
- `CookingHistoryActivity`: Lịch sử nấu ăn
- `NotificationsActivity`: Thông báo

### **2.3. Services (Xử lý dữ liệu):**
- `FirebaseDatabaseService`: CRUD với Firebase Realtime Database
- `FirebaseRealtimeService`: Đọc dữ liệu từ Firebase
- `FirebaseStorageService`: Upload/download ảnh
- `OpenAIService`: Gọi API ChatGPT

### **2.4. ViewModels (Quản lý dữ liệu UI):**
- `RecipeViewModel`: Dữ liệu recipes
- `ProfileViewModel`: Dữ liệu profile
- `AddRecipeViewModel`: Xử lý đăng công thức

### **2.5. Models (Đối tượng dữ liệu):**
- `Recipe`: Món ăn
- `UserProfile`: Hồ sơ user
- `ChatMessage`: Tin nhắn chatbot

### **2.6. Adapters (Hiển thị danh sách):**
- `RecipeAdapter`: Hiển thị list recipes
- `ChatAdapter`: Hiển thị tin nhắn chatbot
- `NotificationAdapter`: Hiển thị thông báo

---

## 3. HOME FRAGMENT - TRANG CHỦ

**Vai trò:** Hiển thị danh sách món ăn, các card "Phổ biến", "Yêu thích", "Nấu nhanh"

**Luồng hoạt động:**
1. `onCreateView()`: Load layout XML
2. `onViewCreated()`: 
   - Khởi tạo ViewModel
   - Setup RecyclerView với RecipeAdapter
   - Gọi `recipeViewModel.loadAllRecipes()` → Load từ Firebase
3. `observeData()`: Lắng nghe LiveData từ ViewModel
   - Khi có dữ liệu mới → Cập nhật RecipeAdapter
4. User click món ăn → Navigate đến RecipeDetailActivity
5. User click "Like" → Gọi `recipeViewModel.updateLikeCount()`

---

## 4. RECIPE DETAIL ACTIVITY - CHI TIẾT MÓN ĂN

**Vai trò:** Hiển thị chi tiết 1 món ăn, cho phép like, đánh giá sao, follow author

**Dữ liệu nhận vào (Intent):**
- recipe_id, title, description, image_url, author_id...
- rating, rating_count

**Luồng hoạt động:**
1. `onCreate()`: Nhận Intent → Parse dữ liệu → Hiển thị
2. `loadRecipeFromFirebase()`: Reload rating/ratingCount từ Firebase
3. `loadUserRating()`: Load rating của user hiện tại (nếu đã đánh giá)
4. `setupRatingSection()`: 
   - Nếu user đã đánh giá → Hiển thị sao, ẩn nút Submit
   - Nếu chưa đánh giá → Hiển thị RatingBar cho phép chọn sao
5. `submitRating()`: Gọi `databaseService.submitRecipeRating()` → Lưu vào Firebase

**Lưu ý quan trọng về Rating:**
- Rating được lưu ở 2 nơi: `/recipes/{recipeId}/ratings/{userId}` và `/foods/{foodId}/ratings/{userId}`
- Đối với user-created recipes: Có `linkedRecipeId` → Sync cả 2 nodes
- Đối với API recipes: Chỉ lưu ở `/foods` node

---

## 5. FIREBASE DATABASE SERVICE - XỬ LÝ DỮ LIỆU

**Vai trò:** Thực hiện các thao tác CRUD với Firebase Realtime Database

### **5.1. Cấu trúc Firebase:**
```
/foods/{foodId}          → Danh sách món ăn để hiển thị (có rating, likeCount)
/recipes/{recipeId}      → Chi tiết món ăn do user tạo (source of truth)
/users/{userId}          → Thông tin user
/favorites/{userId}      → Món yêu thích của user
/notifications/{userId}  → Thông báo
```

### **5.2. Các method quan trọng:**

#### **submitRecipeRating():**
- Lưu rating của user
- Tính rating trung bình = tổng tất cả ratings / số lượng
- Cập nhật `rating` và `ratingCount` vào Firebase
- Tạo notification cho author

#### **createRecipe():**
- Tạo món ăn mới ở `/recipes` node
- Đồng thời thêm vào `/foods` node (để hiển thị ở HomeFragment)
- Link giữa 2 nodes bằng `recipeId`

#### **addToFavorites() / removeFromFavorites():**
- Thêm/xóa vào `/favorites/{userId}/recipeIds/{recipeId}`
- Tăng/giảm `likeCount` ở `/foods/{foodId}`
- Tăng/giảm `favoritesCount` ở `/users/{userId}`

---

## 6. FIREBASE REALTIME SERVICE - ĐỌC DỮ LIỆU

**Vai trò:** Đọc dữ liệu từ Firebase và trả về qua LiveData

**Luồng hoạt động:**
1. Gọi method như `loadAllVietnameseFoods()`
2. Listen Firebase → Khi có data mới → Parse thành List<Recipe>
3. Update LiveData → UI tự động cập nhật (Reactive Programming)

**Các method:**
- `loadAllVietnameseFoods()`: Load tất cả món ăn
- `searchVietnameseFoods(query)`: Tìm kiếm
- `loadVietnameseFoodsByCategory(category)`: Load theo danh mục
- `loadPopularRecipes()`: Load món phổ biến (theo likeCount)
- `loadQuickCookRecipes()`: Load món nấu nhanh (< 30 phút)

---

## 7. VIEWMODEL - QUẢN LÝ DỮ LIỆU UI

**Vai trò:** Là lớp trung gian giữa UI (Fragment) và Services

**Lợi ích:**
- Tách biệt logic business khỏi UI
- Quản lý dữ liệu qua LiveData (tự động cập nhật UI)
- Giữ data khi xoay màn hình (Configuration changes)

**Ví dụ RecipeViewModel:**
```java
// Fragment gọi:
recipeViewModel.loadAllRecipes();

// ViewModel gọi Service:
firebaseRealtimeService.loadAllVietnameseFoods();

// Service update LiveData → ViewModel → Fragment tự động nhận data
```

---

## 8. SEARCH FRAGMENT - TÌM KIẾM

**Luồng hoạt động:**
1. User nhập text vào SearchBox
2. Debounce 500ms (chờ user ngừng gõ) → Gọi `recipeViewModel.searchRecipes(query)`
3. ViewModel → FirebaseRealtimeService → Search trong Firebase
4. Hiển thị kết quả trong RecyclerView

---

## 9. ADD RECIPE FRAGMENT - ĐĂNG CÔNG THỨC

**Luồng hoạt động:**
1. User điền form (title, description, ingredients, instructions...)
2. Chọn ảnh (optional) → Upload lên Firebase Storage
3. Click "Đăng công thức"
4. `validateForm()` → Kiểm tra dữ liệu hợp lệ
5. `createRecipe()` → Gọi `AddRecipeViewModel.createRecipe()`
6. ViewModel → `FirebaseDatabaseService.createRecipe()`
7. Lưu vào `/recipes` node + `/foods` node
8. Hiển thị thông báo thành công

---

## 10. PROFILE FRAGMENT - HỒ SƠ

**Luồng hoạt động:**
1. Load profile từ ViewModel: `viewModel.loadUserProfile(userId)`
2. Hiển thị: Tên, email, avatar, số công thức, số yêu thích
3. Click "Chỉnh sửa" → Mở EditProfileActivity
4. Click "Công thức của tôi" → Mở MyRecipesActivity
5. Click "Yêu thích" → Mở FavoriteRecipesActivity
6. Click "Đăng xuất" → Logout → Về LoginActivity

---

## 11. CÁC ACTIVITY KHÁC

### **MyRecipesActivity:**
- Load recipes của user hiện tại từ `/foods` node (có likeCount, rating)
- Hiển thị trong RecyclerView
- Click món ăn → Mở RecipeDetailActivity

### **FavoriteRecipesActivity:**
- Load danh sách recipeIds từ `/favorites/{userId}/recipeIds`
- Với mỗi recipeId → Load detail từ `/foods` node
- Hiển thị danh sách món yêu thích

### **ChatbotActivity:**
- Sử dụng OpenAI API (ChatGPT)
- Gửi message với lịch sử chat (context)
- Nhận response → Hiển thị trong RecyclerView

---

## 12. KIẾN TRÚC TỔNG QUAN

```
UI Layer (Activities/Fragments)
    ↓
ViewModel Layer (Quản lý dữ liệu)
    ↓
Service Layer (Firebase, API)
    ↓
Firebase Realtime Database / Storage
```

**Nguyên tắc:**
- UI chỉ hiển thị, không xử lý logic
- ViewModel quản lý dữ liệu, expose LiveData
- Service xử lý API calls, Firebase operations
- Tách biệt concerns (Separation of Concerns)

---

## 13. CÁC TÍNH NĂNG CHÍNH

1. **Đăng nhập/Đăng ký:** Email/Password hoặc Google
2. **Xem danh sách món ăn:** Load từ Firebase, hiển thị với RecyclerView
3. **Tìm kiếm:** Search với debounce
4. **Chi tiết món ăn:** Rating, Like, Follow author
5. **Đăng công thức:** Form upload với ảnh
6. **Yêu thích:** Lưu món ăn yêu thích
7. **Thông báo:** Like, Rating, Follow
8. **Chatbot AI:** Tư vấn ẩm thực

---

## 14. ĐIỂM QUAN TRỌNG CẦN NHỚ KHI BẢO VỆ

1. **Firebase Structure:** Hiểu rõ cấu trúc `/foods` vs `/recipes`
2. **Rating System:** Rating được sync ở 2 nodes
3. **LiveData/ViewModel:** Reactive programming, tự động update UI
4. **Intent:** Truyền dữ liệu giữa Activities
5. **Fragment Transaction:** Replace Fragment trong MainActivity
6. **RecyclerView + Adapter:** Hiển thị danh sách hiệu quả
7. **Firebase Rules:** Security rules cho read/write

---

## 15. CÁC FILE QUAN TRỌNG NHẤT CẦN THUỘC

1. **MainActivity.java** - Entry point, navigation
2. **HomeFragment.java** - Hiển thị danh sách
3. **RecipeDetailActivity.java** - Chi tiết + Rating
4. **FirebaseDatabaseService.java** - CRUD operations
5. **FirebaseRealtimeService.java** - Read data
6. **RecipeViewModel.java** - Quản lý dữ liệu
7. **RecipeAdapter.java** - Hiển thị list

---

*Tạo file này để bạn có thể đọc lại trước khi bảo vệ. Tiếp tục giải thích chi tiết từng file?*

