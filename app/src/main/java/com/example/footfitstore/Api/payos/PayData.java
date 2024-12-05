package com.example.footfitstore.Api.payos;

public class PayData {
    private String bin;
    private String accountNumber;
    private String accountName;
    private String currency;
    private String paymentLinkId;
    private int amount;
    private String description;
    private int orderCode;
    private String status;

    public void setBin(String bin) {
        this.bin = bin;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setPaymentLinkId(String paymentLinkId) {
        this.paymentLinkId = paymentLinkId;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOrderCode(int orderCode) {
        this.orderCode = orderCode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    private String checkoutUrl;
    private String qrCode;

    public String getBin() {
        return bin;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPaymentLinkId() {
        return paymentLinkId;
    }

    public int getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public int getOrderCode() {
        return orderCode;
    }

    public String getStatus() {
        return status;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public String getQrCode() {
        return qrCode;
    }
}
