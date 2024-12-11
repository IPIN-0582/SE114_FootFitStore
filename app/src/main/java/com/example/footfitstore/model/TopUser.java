package com.example.footfitstore.model;

public class TopUser {
    private String firstName;
    private String lastName;
    private String email;
    private Double totalTransaction;
    private String imageUrl;
    private int gender;
    public TopUser() {
    }
    public TopUser(String firstName, String lastName, String email, Double totalTransaction, String imageUrl, int gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.totalTransaction = totalTransaction;
        this.imageUrl = imageUrl;
        this.gender = gender;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getTotalTransaction() {
        return totalTransaction;
    }

    public void setTotalTransaction(Double totalTransaction) {
        this.totalTransaction = totalTransaction;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getGender() {
        return gender;
    }
}
