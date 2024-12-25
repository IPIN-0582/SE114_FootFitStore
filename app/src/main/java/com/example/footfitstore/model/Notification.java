package com.example.footfitstore.model;

import java.util.Objects;

public class Notification {
    private String productId;
    private String description;
    private String imgUrl;
    private boolean isRead;
    private String endDate;
    public void setDescription(String description) {
        this.description = description;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getDescription() {
        return description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(description, that.description) &&
                Objects.equals(imgUrl, that.imgUrl) &&
                Objects.equals(endDate, that.endDate);
    }
}
