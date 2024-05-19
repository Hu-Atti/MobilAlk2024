package hu.mobilalkfejl.model;

public class Stock {
    private String productId;
    private String storeId;
    private int price;

    public Stock() {
    }

    public Stock(String productId, String storeId, int price) {
        this.productId = productId;
        this.storeId = storeId;
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
