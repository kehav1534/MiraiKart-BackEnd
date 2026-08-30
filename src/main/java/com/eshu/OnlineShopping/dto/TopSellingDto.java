package com.eshu.OnlineShopping.dto;

public class TopSellingDto {

    private Integer productId;
    private String productName;
    private Long unitsSold;

    public TopSellingDto(Integer productId,
                         String productName,
                         Long unitsSold) {
        this.productId = productId;
        this.productName = productName;
        this.unitsSold = unitsSold;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Long getUnitsSold() {
        return unitsSold;
    }
}