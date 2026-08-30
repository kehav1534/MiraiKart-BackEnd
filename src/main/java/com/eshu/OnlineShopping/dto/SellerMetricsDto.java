package com.eshu.OnlineShopping.dto;

import com.eshu.OnlineShopping.model.Product;

import java.math.BigDecimal;
import java.util.List;

public class SellerMetricsDto {
    private long totalOrders;
    private long totalSales;
    private BigDecimal grossRevenue;
    private BigDecimal netRevenue;
    private BigDecimal orderReturnRate;
    private long returnItems;
    private List<TopSellingDto> hotProducts;
    private List<Product> itemsOutOfStocks;

    public SellerMetricsDto(long totalOrders, long totalSales, BigDecimal grossRevenue,
                            BigDecimal netRevenue, long returnedItems, BigDecimal orderReturnRate, List<TopSellingDto> hot,
                            List<Product> itemsOutOfStocks){
        this.totalOrders = totalOrders;
        this.totalSales = totalSales;
        this.grossRevenue = grossRevenue;
        this.netRevenue = netRevenue;
        this.returnItems = returnedItems;
        this.orderReturnRate = orderReturnRate;
        this.hotProducts = hot;
        this.itemsOutOfStocks = itemsOutOfStocks;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public long getTotalSales() {
        return totalSales;
    }

    public BigDecimal getGrossRevenue() {
        return grossRevenue;
    }

    public BigDecimal getNetRevenue() {
        return netRevenue;
    }

    public BigDecimal getOrderReturnRate() {
        return orderReturnRate;
    }

    public long getReturnItems() {
        return returnItems;
    }

    public List<TopSellingDto> getHotProducts() {
        return hotProducts;
    }

    public List<Product> getItemsOutOfStocks() {
        return itemsOutOfStocks;
    }
}
