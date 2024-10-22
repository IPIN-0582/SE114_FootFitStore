package com.example.footfitstore.model;

import java.util.List;

public class User {

    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String mobileNumber;
    private int gender;
    private List<Cart> cartList;


    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User( String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstName;
    }

    public void setFirstname(String firstName) {
        this.firstName = firstName;
    }

    public String getLastname() {
        return lastName;
    }

    public void setLastname(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return mobileNumber;
    }

    public void setPhone(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public List<Cart> getCartList() {
        return cartList;
    }

    public void setCartList(List<Cart> cartList) {
        this.cartList = cartList;
    }
}
