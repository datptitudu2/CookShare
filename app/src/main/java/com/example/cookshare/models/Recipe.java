package com.example.cookshare.models;

import java.util.List;
import java.util.Date;

public class Recipe {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String authorId;
    private String authorName;
    private String authorAvatar;
    private List<String> ingredients;
    private List<String> instructions;
    private int prepTime; // in minutes
    private int cookTime; // in minutes
    private int servings;
    private String difficulty; // Easy, Medium, Hard
    private List<String> categories; // Vietnamese, Chinese, etc.
    private List<String> tags; // vegetarian, spicy, etc.
    private double rating;
    private int ratingCount;
    private int viewCount;
    private int likeCount;
    private Date createdAt;
    private Date updatedAt;
    private boolean isPublished;

    // Constructors
    public Recipe() {
    }

    public Recipe(String title, String description, String authorId, String authorName) {
        this.title = title;
        this.description = description;
        this.authorId = authorId;
        this.authorName = authorName;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isPublished = false;
        this.rating = 0.0;
        this.ratingCount = 0;
        this.viewCount = 0;
        this.likeCount = 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public int getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public int getCookTime() {
        return cookTime;
    }

    public void setCookTime(int cookTime) {
        this.cookTime = cookTime;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
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

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public void setIsPublished(boolean isPublished) {
        this.isPublished = isPublished;
    }

    public void setCategory(String category) {
        if (this.categories == null) {
            this.categories = new java.util.ArrayList<>();
        }
        this.categories.clear();
        this.categories.add(category);
    }

    public void setArea(String area) {
        if (this.tags == null) {
            this.tags = new java.util.ArrayList<>();
        }
        this.tags.add(area);
    }

    // Helper methods
    public int getTotalTime() {
        return prepTime + cookTime;
    }

    public String getFormattedTime() {
        int total = getTotalTime();
        if (total < 60) {
            return total + " phút";
        } else {
            int hours = total / 60;
            int minutes = total % 60;
            if (minutes == 0) {
                return hours + " giờ";
            } else {
                return hours + " giờ " + minutes + " phút";
            }
        }
    }

    public String getFormattedRating() {
        return String.format("%.1f", rating);
    }
}