package com.example.footfitstore.Api.payos;

import com.example.footfitstore.Utils.SignatureGenerator;

import java.util.List;

public class CreateOrderLink {
    private int orderCode;
    private int amount;
    private String description;
    private String buyerName; // Not required
    private String buyerEmail; // Not required
    private String buyerPhone; // Not required
    private String buyerAddress; // Not required
    private List<MinimalizeShoeDescription> items;
    private String cancelUrl;
    private String returnUrl;
    private long expiredAt; // Not required, Unix Timestamp
    private String signature;

    // Remove not required fields
    public CreateOrderLink(int orderCode, int amount, String description, List<MinimalizeShoeDescription> items, String cancelUrl, String returnUrl, long expiredAt) {
        this.orderCode = orderCode;
        this.amount = amount;
        this.description = description;
        this.items = items;
        this.cancelUrl = cancelUrl;
        this.returnUrl = returnUrl;
        this.expiredAt = expiredAt;
        this.signature = SignatureGenerator.createPayOsSignature(amount, cancelUrl, description, String.valueOf(orderCode), returnUrl);
    }

    public CreateOrderLink(int orderCode, int amount, String description, String buyerName,
                           String buyerEmail, String buyerPhone, String buyerAddress,
                           List<MinimalizeShoeDescription> items, String cancelUrl, String returnUrl,
                           long expiredAt) {
        this.orderCode = orderCode;
        this.amount = amount;
        this.description = description;
        this.buyerName = buyerName;
        this.buyerEmail = buyerEmail;
        this.buyerPhone = buyerPhone;
        this.buyerAddress = buyerAddress;
        this.items = items;
        this.cancelUrl = cancelUrl;
        this.returnUrl = returnUrl;
        this.expiredAt = expiredAt;
        this.signature = SignatureGenerator.createPayOsSignature(amount, cancelUrl, description, String.valueOf(orderCode), returnUrl);
    }

    public int getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(int orderCode) {
        this.orderCode = orderCode;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getBuyerPhone() {
        return buyerPhone;
    }

    public void setBuyerPhone(String buyerPhone) {
        this.buyerPhone = buyerPhone;
    }

    public String getBuyerAddress() {
        return buyerAddress;
    }

    public void setBuyerAddress(String buyerAddress) {
        this.buyerAddress = buyerAddress;
    }

    public List<MinimalizeShoeDescription> getItems() {
        return items;
    }

    public void setItems(List<MinimalizeShoeDescription> items) {
        this.items = items;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public long getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(long expiredAt) {
        this.expiredAt = expiredAt;
    }

    public String getSignature() {
        return signature;
    }
}
