package com.example.cookshare.models;

import java.util.Date;
import java.util.List;

// UserProfile model for Firebase
// This class represents user profile data structure
public class UserProfile {
    private String id;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String bio;
    private String location;
    private String phoneNumber;
    private Date dateOfBirth;
    private String gender;
    private List<String> favoriteCategories;
    private List<String> favoriteRegions;
    private int recipesCount;
    private int favoritesCount;
    private int followersCount;
    private int followingCount;
    private Date createdAt;
    private Date updatedAt;
    private boolean isEmailVerified;
    private boolean isProfileComplete;
    
    // Constructors
    public UserProfile() {
    }
    
    public UserProfile(String id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isEmailVerified = false;
        this.isProfileComplete = false;
        this.recipesCount = 0;
        this.favoritesCount = 0;
        this.followersCount = 0;
        this.followingCount = 0;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public Date getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public List<String> getFavoriteCategories() {
        return favoriteCategories;
    }
    
    public void setFavoriteCategories(List<String> favoriteCategories) {
        this.favoriteCategories = favoriteCategories;
    }
    
    public List<String> getFavoriteRegions() {
        return favoriteRegions;
    }
    
    public void setFavoriteRegions(List<String> favoriteRegions) {
        this.favoriteRegions = favoriteRegions;
    }
    
    public int getRecipesCount() {
        return recipesCount;
    }
    
    public void setRecipesCount(int recipesCount) {
        this.recipesCount = recipesCount;
    }
    
    public int getFavoritesCount() {
        return favoritesCount;
    }
    
    public void setFavoritesCount(int favoritesCount) {
        this.favoritesCount = favoritesCount;
    }
    
    public int getFollowersCount() {
        return followersCount;
    }
    
    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }
    
    public int getFollowingCount() {
        return followingCount;
    }
    
    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isEmailVerified() {
        return isEmailVerified;
    }
    
    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }
    
    public boolean isProfileComplete() {
        return isProfileComplete;
    }
    
    public void setProfileComplete(boolean profileComplete) {
        isProfileComplete = profileComplete;
    }
}
