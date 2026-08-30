package com.eshu.OnlineShopping.dto;

import com.eshu.OnlineShopping.enums.ProductListingStatus;

public class ProductListingStatusDto {
    private int productId;
    private ProductListingStatus status;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public ProductListingStatus getStatus() {
        return status;
    }

    public void setStatus(ProductListingStatus status) {
        this.status = status;
    }
}
