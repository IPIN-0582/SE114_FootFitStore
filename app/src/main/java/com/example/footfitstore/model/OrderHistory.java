package com.example.footfitstore.model;

import java.util.List;

public class OrderHistory {
    String orderTime;
    List<CartRating> cartList;
    String orderStatus;
    String paymentMethod;
    Double transaction;
    String review = "";
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Double getTransaction() {
        return transaction;
    }

    public void setTransaction(Double transaction) {
        this.transaction = transaction;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
