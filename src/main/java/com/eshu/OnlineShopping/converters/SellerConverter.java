package com.eshu.OnlineShopping.converters;

import com.eshu.OnlineShopping.dto.SellerDto;
import com.eshu.OnlineShopping.model.Seller;

public class SellerConverter {

    public static Seller convertSellerDtoIntoSeller(SellerDto sellerDto){
        Seller newSeller= new Seller();
        newSeller.setFullName(sellerDto.getFullName());
        newSeller.setContactNo(sellerDto.getContactNo());
        newSeller.setEntityName(sellerDto.getEntityName());
        return newSeller;
    }
}
