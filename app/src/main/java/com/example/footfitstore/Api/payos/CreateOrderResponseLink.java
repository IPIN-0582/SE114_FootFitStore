package com.example.footfitstore.Api.payos;

import androidx.annotation.NonNull;

public class CreateOrderResponseLink {
    private String code;
    private String desc;
    private PayData data;
    private String signature;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public PayData getData() {
        return data;
    }

    public void setData(PayData data) {
        this.data = data;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @NonNull
    @Override
    public String toString() {
        return "CreatePayLinkResponse{" +
                "code='" + code + '\'' +
                ", desc='" + desc + '\'' +
                ", data=" + data +
                ", signature='" + signature + '\'' +
                '}';
    }
}
