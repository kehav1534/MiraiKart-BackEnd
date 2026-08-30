package com.eshu.OnlineShopping.dto;

import com.eshu.OnlineShopping.enums.OrderStatus;
import com.eshu.OnlineShopping.enums.PaymentMethod;
import com.eshu.OnlineShopping.enums.PaymentStatus;
import com.eshu.OnlineShopping.enums.ReturnStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Flat, seller-facing view of a single ordered line item.
 *
 * Built directly by a JPQL constructor-expression join (see
 * OrderItemRepository) rather than returned as the raw OrderItem/Order
 * entities, for two reasons:
 *  - OrderItem.orderId is @JsonBackReference'd, so order date/payment info
 *    would silently disappear if an OrderItem entity were serialized on its
 *    own (as the old /seller/apis/orders endpoint did).
 *  - The buyer's contact number/address are deliberately surfaced here
 *    (a seller needs them to fulfil the order), while everything else about
 *    the buyer's account - credentials, cart, other orders - is left out,
 *    which returning the User entity would not do.
 *
 * allowedNextStatuses is populated after the query runs (it's derived
 * business logic, not a column) and tells the frontend exactly which status
 * transitions this line item may legally make right now, so the UI never
 * has to duplicate SellerService's transition rules.
 */
public class SellerOrderDto {

    private final int orderItemId;
    private final int orderId;
    private final LocalDateTime orderDate;
    private final LocalDateTime updationDate;
    private final PaymentMethod paymentMode;
    private final PaymentStatus paymentStatus;

    private final int productId;
    private final String productName;
    private final String manufacturer;

    private final int qty;
    private final BigDecimal purchasePrice;
    private final BigDecimal lineTotal;
    private final OrderStatus status;
    private final ReturnStatus returnStatus;

    private final int buyerId;
    private final String buyerEmail;
    private final String buyerContactNo;
    private final String buyerAddress;

    private List<OrderStatus> allowedNextStatuses;

    public SellerOrderDto(int orderItemId, int orderId, LocalDateTime orderDate, LocalDateTime updationDate,
                           PaymentMethod paymentMode, PaymentStatus paymentStatus,
                           int productId, String productName, String manufacturer,
                           int qty, BigDecimal purchasePrice, OrderStatus status, ReturnStatus returnStatus,
                           int buyerId, String buyerEmail, String buyerContactNo, String buyerAddress) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.updationDate = updationDate;
        this.paymentMode = paymentMode;
        this.paymentStatus = paymentStatus;
        this.productId = productId;
        this.productName = productName;
        this.manufacturer = manufacturer;
        this.qty = qty;
        this.purchasePrice = purchasePrice;
        this.lineTotal = purchasePrice != null ? purchasePrice.multiply(BigDecimal.valueOf(qty)) : BigDecimal.ZERO;
        this.status = status;
        this.returnStatus = returnStatus;
        this.buyerId = buyerId;
        this.buyerEmail = buyerEmail;
        this.buyerContactNo = buyerContactNo;
        this.buyerAddress = buyerAddress;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public LocalDateTime getUpdationDate() {
        return updationDate;
    }

    public PaymentMethod getPaymentMode() {
        return paymentMode;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public int getQty() {
        return qty;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public ReturnStatus getReturnStatus() {
        return returnStatus;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public String getBuyerContactNo() {
        return buyerContactNo;
    }

    public String getBuyerAddress() {
        return buyerAddress;
    }

    public List<OrderStatus> getAllowedNextStatuses() {
        return allowedNextStatuses;
    }

    public void setAllowedNextStatuses(List<OrderStatus> allowedNextStatuses) {
        this.allowedNextStatuses = allowedNextStatuses;
    }
}
