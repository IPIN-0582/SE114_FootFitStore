package com.example.footfitstore.Utils;

public class OrderIdGenerator {
    public static int generateOrderId() {
        return (int) (Math.random() * 1000000);
    }
}
