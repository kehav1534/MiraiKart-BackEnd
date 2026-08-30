package com.eshu.OnlineShopping.dto;

import java.math.BigDecimal;

public class ProductMetricsDto {
    private long productOrders;

    private long productSales;

    private BigDecimal grossRevenue;

    private BigDecimal netRevenue;

    private long returnCount;

    private BigDecimal returnRate;

    private long stockAvailable;

    public ProductMetricsDto(long productOrders, long productSales, BigDecimal grossRevenue, BigDecimal netRevenue,
                      long returnCount, BigDecimal returnRate, long stockAvailable){
        this.productOrders = productOrders;
        this.productSales = productSales;
        this.grossRevenue = grossRevenue;
        this.netRevenue = netRevenue;
        this.returnCount = returnCount;
        this.returnRate = returnRate;
        this.stockAvailable = stockAvailable;
    }

    public long getProductOrders() {
        return productOrders;
    }

    public long getProductSales() {
        return productSales;
    }

    public BigDecimal getGrossRevenue() {
        return grossRevenue;
    }

    public BigDecimal getNetRevenue() {
        return netRevenue;
    }

    public long getReturnCount() {
        return returnCount;
    }

    public BigDecimal getReturnRate() {
        return returnRate;
    }

    public long getStockAvailable() {
        return stockAvailable;
    }
}
