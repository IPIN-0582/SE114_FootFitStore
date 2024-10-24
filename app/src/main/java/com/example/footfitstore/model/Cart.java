package com.example.footfitstore.model;

public class Cart {

    private String productId;   // ID sản phẩm
    private String productName; // Tên sản phẩm (giày)
    private String size; // Kích thước giày
    private int quantity;       // Số lượng sản phẩm
    private double price;       // Giá sản phẩm

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSize() {
        return size;
    }

    public void setProductSize(String size) {
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

}
