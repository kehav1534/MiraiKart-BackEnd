package com.eshu.OnlineShopping.converters;

import com.eshu.OnlineShopping.dto.ProductDto;
import com.eshu.OnlineShopping.enums.ProductListingStatus;
import com.eshu.OnlineShopping.model.Product;

public class ProductConverter {
    public static Product convertProductDtoIntoProduct(ProductDto productDto){
        Product product = new Product();
        product.setName(productDto.getName());
        product.setCategory(productDto.getCategory());
        product.setDes(productDto.getDes());
        product.setDiscount(productDto.getDiscount());
        product.setManufacturer(productDto.getManufacturer());
        product.setPrice(productDto.getPrice());
        product.setListingStatus(ProductListingStatus.PENDING);
        return  product;
    }
}
