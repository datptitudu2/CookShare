package com.example.cookshare.models;

import com.google.gson.annotations.SerializedName;

public class VietnameseFood {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("image")
    private String image;

    @SerializedName("category")
    private String category;

    @SerializedName("region")
    private String region;

    public VietnameseFood() {
    }

    public VietnameseFood(int id, String name, String description, String image, String category, String region) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
        this.category = category;
        this.region = region;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    // Convert to Recipe model
    public Recipe toRecipe() {
        Recipe recipe = new Recipe();
        recipe.setId(String.valueOf(this.id));
        recipe.setTitle(this.name);
        recipe.setDescription(this.description);
        recipe.setAuthorId("vietnamese_food");
        recipe.setAuthorName("Ẩm thực Việt Nam");
        recipe.setImageUrl(this.image);
        recipe.setCategory(this.category);
        recipe.setArea(this.region);

        // Set default values for Vietnamese food
        recipe.setPrepTime(30); // Default prep time
        recipe.setCookTime(60); // Default cook time
        recipe.setServings(4); // Default servings
        recipe.setDifficulty(getDifficultyFromCategory(this.category));
        recipe.setRating(4.5); // Default high rating for Vietnamese food
        recipe.setRatingCount(100); // Default rating count
        recipe.setViewCount(500); // Default view count
        recipe.setLikeCount(50); // Default like count
        recipe.setIsPublished(true);

        // Set ingredients based on category
        recipe.setIngredients(getDefaultIngredients(this.category));

        // Set instructions based on category
        recipe.setInstructions(getDefaultInstructions(this.name, this.category));

        // Set tags
        recipe.setTags(java.util.Arrays.asList(this.category, this.region, "Việt Nam"));

        return recipe;
    }

    private String getDifficultyFromCategory(String category) {
        if (category == null || category.isEmpty()) {
            return "Trung bình";
        }
        switch (category) {
            case "Ăn sáng":
            case "Ăn nhanh":
                return "Dễ";
            case "Món nước":
            case "Canh":
                return "Trung bình";
            case "Món nướng":
            case "Chiên":
                return "Khó";
            default:
                return "Trung bình";
        }
    }

    private java.util.List<String> getDefaultIngredients(String category) {
        java.util.List<String> ingredients = new java.util.ArrayList<>();

        if (category == null || category.isEmpty()) {
            ingredients.add("Nguyên liệu chính");
            ingredients.add("Gia vị");
            ingredients.add("Rau thơm");
            return ingredients;
        }

        switch (category) {
            case "Món nước":
                ingredients.add("Bánh phở");
                ingredients.add("Thịt bò");
                ingredients.add("Hành tây");
                ingredients.add("Gừng");
                ingredients.add("Quế");
                ingredients.add("Hoa hồi");
                break;
            case "Món nướng":
                ingredients.add("Thịt heo");
                ingredients.add("Nước mắm");
                ingredients.add("Đường");
                ingredients.add("Tỏi");
                ingredients.add("Ớt");
                break;
            case "Chiên":
                ingredients.add("Bột mì");
                ingredients.add("Trứng");
                ingredients.add("Dầu ăn");
                ingredients.add("Muối");
                ingredients.add("Tiêu");
                break;
            default:
                ingredients.add("Nguyên liệu chính");
                ingredients.add("Gia vị");
                ingredients.add("Rau thơm");
        }

        return ingredients;
    }

    private java.util.List<String> getDefaultInstructions(String name, String category) {
        java.util.List<String> instructions = new java.util.ArrayList<>();

        instructions.add("Chuẩn bị nguyên liệu tươi ngon");
        instructions.add("Sơ chế và rửa sạch các nguyên liệu");

        if (category == null || category.isEmpty()) {
            instructions.add("Trình bày đẹp mắt và thưởng thức");
            return instructions;
        }

        switch (category) {
            case "Món nước":
                instructions.add("Nấu nước dùng với xương và gia vị");
                instructions.add("Thêm thịt và rau củ vào nước dùng");
                instructions.add("Nêm nếm gia vị cho vừa miệng");
                break;
            case "Món nướng":
                instructions.add("Ướp thịt với gia vị trong 30 phút");
                instructions.add("Nướng thịt trên than hoa hoặc lò nướng");
                instructions.add("Trở đều để thịt chín vàng");
                break;
            case "Chiên":
                instructions.add("Tẩm bột hoặc nhúng trứng");
                instructions.add("Chiên vàng giòn trong dầu nóng");
                instructions.add("Vớt ra để ráo dầu");
                break;
        }

        instructions.add("Trình bày đẹp mắt và thưởng thức");

        return instructions;
    }
}
