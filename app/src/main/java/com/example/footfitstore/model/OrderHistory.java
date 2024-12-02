package com.example.footfitstore.model;

import java.util.List;

public class OrderHistory {
    String orderTime;
    List<CartRating> cartList;
    String orderStatus;

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public List<CartRating> getCartList() {
        return cartList;
    }

    public void setCartList(List<CartRating> cartList) {
        this.cartList = cartList;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
