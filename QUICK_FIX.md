# 🚨 QUICK FIX - Google Sign-In Error

## ❌ **Lỗi hiện tại:**
"Tài khoản Google không khả dụng" (Error code 10)

## ✅ **GIẢI PHÁP NHANH:**

### **1. Firebase Console Setup (5 phút):**
```
1. Vào: https://console.firebase.google.com/
2. Chọn project CookShare
3. Authentication → Sign-in method
4. Google → Enable
5. Chọn support email
6. Save
```

### **2. Thêm SHA-1 (2 phút):**
```
1. Project Settings (⚙️)
2. Your apps → Android app
3. SHA certificate fingerprints
4. Add: 10:10:58:64:12:05:E4:A3:41:52:DA:17:35:58:C3:07:99:AD:9C:7C
5. Save
```

### **3. Download google-services.json (1 phút):**
```
1. Project Settings
2. Your apps → Android app  
3. Download google-services.json
4. Thay thế file cũ
```

### **4. Test ngay:**
```
1. Clean project: .\gradlew clean
2. Rebuild: .\gradlew assembleDebug
3. Chạy app
4. Test Google Sign-In
```

## 🔧 **Nếu vẫn lỗi:**

### **Checklist:**
- [ ] Google Sign-In đã được Enable trong Firebase
- [ ] SHA-1 đã được thêm vào Firebase
- [ ] google-services.json đã được cập nhật
- [ ] Project đã được sync
- [ ] App đã được rebuild

### **Debug:**
1. Mở Logcat
2. Filter: "LoginActivity"
3. Xem error code chi tiết
4. Kiểm tra Firebase Console logs

## ⚡ **Nếu cần test ngay:**

### **Tạm thời dùng Email/Password:**
1. Tạo tài khoản trong Firebase Console
2. Authentication → Users → Add user
3. Test đăng nhập với email/password

---

**Làm theo các bước trên là OK ngay! 🚀**
