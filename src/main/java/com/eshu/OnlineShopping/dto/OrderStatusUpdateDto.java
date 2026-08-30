package com.eshu.OnlineShopping.dto;

import com.eshu.OnlineShopping.enums.OrderStatus;

public class OrderStatusUpdateDto {
    private int orderItemId;
    private OrderStatus status;

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
