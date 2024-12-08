package com.example.footfitstore.Api.payos;

public class MinimalizeShoeDescription {
    private String name;
    private int quantity;
    private String size;
    private double price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public MinimalizeShoeDescription(String name, int quantity, String size, double price) {
        this.name = name;
        this.quantity = quantity;
        this.size = size;
        this.price = price*quantity*25000;
    }
}
