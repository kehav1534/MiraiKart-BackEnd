package com.eshu.OnlineShopping.dto;

import com.eshu.OnlineShopping.enums.PaymentMethod;

public class BuyNowDto {
    private int productId;
    private int quantity;
    private PaymentMethod paymentMode;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public PaymentMethod getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMethod paymentMode) {
        this.paymentMode = paymentMode;
    }
}
