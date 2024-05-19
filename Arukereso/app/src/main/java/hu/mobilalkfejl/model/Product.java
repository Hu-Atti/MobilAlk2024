package hu.mobilalkfejl.model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private String category;
    private float rating;
    private String description;
    private String image;

    private int lowestPrice;

    private List<String> storeIds;

    public List<String> getStoreIds() {
        return storeIds;
    }

    public void addStoreId(String storeId) {
        if (this.storeIds == null) {
            this.storeIds = new ArrayList<>();
        }
        this.storeIds.add(storeId);
    }

    public void setStoreIds(List<String> storeIds) {
        if (this.storeIds == null) {
            storeIds = new ArrayList<String>();
        }
        this.storeIds = storeIds;
    }

    public Product() {
    }

    public Product(String name, String category, String description, float rating, String image) {
        this.name = name;
        this.category = category;
        this.rating = rating;
        this.description = description;
        this.image = image;
        if (this.lowestPrice == 0) {
            this.lowestPrice = Integer.MAX_VALUE;
        }
    }

    public String _getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setRating(float rating) {
        this.rating = rating;
    }
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public float getRating() {
        return rating;
    }

    public int getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(int lowestPrice) {
        this.lowestPrice = lowestPrice;
    }
}
