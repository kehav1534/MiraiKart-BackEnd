package com.eshu.OnlineShopping.dto;

import com.eshu.OnlineShopping.enums.PaymentMethod;

public class CheckoutDto {
    private PaymentMethod paymentMode;

    public PaymentMethod getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMethod paymentMode) {
        this.paymentMode = paymentMode;
    }
}
