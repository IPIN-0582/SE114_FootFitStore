package com.example.footfitstore.model;

public class Promotion {
    private int discount;
    private String startDate;
    private String endDate;

    // Empty constructor
    public Promotion() {
    }

    // Getters and Setters
    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}

